package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableTerminationTimeFault;
import org.trmd.ntsh.NothingToSeeHere;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Created by tormod on 24.03.14.
 */
public class ServiceUtilities {

    public static final class EndpointTerminationTuple{
        public final String endpoint;
        public final long termination;

        public EndpointTerminationTuple(String endpoint, long termination) {
            this.endpoint = endpoint;
            this.termination = termination;
        }
    }

    public static final class Tuple<X,Y>{
        public final X x;
        public final Y y;

        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }

        public ArrayList<X> getFirsts(ArrayList<Tuple<X,Y>> list){
            ArrayList<X> xList = new ArrayList<>();
            for (Tuple<X, Y> xyTuple : list) {
                xList.add(xyTuple.x);
            }
            return xList;
        }


        public ArrayList<Y> getSeconds(ArrayList<Tuple<X,Y>> list){
            ArrayList<Y> yList = new ArrayList<>();
            for (Tuple<X, Y> xyTuple : list) {
                yList.add(xyTuple.y);
            }
            return yList;
        }
    }

    public static class InputManager extends Thread {

        private HashMap<String, Method> _methodRerouting;
        private HashMap<String, String> _matchCommand;
        private HashMap<String, Boolean> _methodTakesCommandString;
        private HashMap<String, Integer> _commandArgPosition;
        private HashMap<String, ArrayList<Tuple<Integer, Object>>> _defaultArguments;
        private HashMap<String, Boolean> _methodUsesRegex;
        private HashMap<String, Object> _theInvokables;

        public InputManager() {
            _methodRerouting = new HashMap<>();
            _theInvokables = new HashMap<>();
            _methodTakesCommandString = new HashMap<>();
            _matchCommand = new HashMap<>();
            _methodUsesRegex = new HashMap<>();
            _commandArgPosition = new HashMap<>();
            _defaultArguments = new HashMap<>();
        }

        /**
         * Adds a method-rerouting, routing a method with unique name command, matching the input String, to method rerouteTo, invoked on invokable.
         * @param command The unique identifier, can be virtually anything.
         * @param matchCommand The string that is to be matched.
         * @param regex Whether or not the matchstring above should be checked with regex or not. If not it will just be checked for contains.
         * @param rerouteTo
         * @param invokable
         */
        public void addMethodReroute(String command, String matchCommand, boolean regex,
                                     Method rerouteTo, Object invokable){
            addMethodReroute(command, matchCommand, regex, rerouteTo, invokable, new ArrayList<Tuple<Integer, Object>>(), 0);
        }

        public void addMethodReroute(String command, String matchCommand, boolean regex, Method rerouteTo,
                                     Object invokable, ArrayList<Tuple<Integer, Object>> defaultArguments){
            _methodRerouting.put(command, rerouteTo);
            _theInvokables.put(command, invokable);
            _defaultArguments.put(command, defaultArguments);
            _methodTakesCommandString.put(command, rerouteTo.getParameterTypes().length != 0
                    && defaultArguments.size() != rerouteTo.getParameterTypes().length);

            if(!_methodTakesCommandString.get(command)) {
                _commandArgPosition.put(command, -1);
            /* We have to find the argument position */
            }else{
                if(defaultArguments.size() != 0){
                    _commandArgPosition.put(command, findFirstEmptyPosition(defaultArguments.get(0).getFirsts(defaultArguments)));
                }else{
                    _commandArgPosition.put(command, 0);
                }
            }

            _matchCommand.put(command, matchCommand);
            _methodUsesRegex.put(command, regex);
        }

        public void addMethodReroute(String command, String matchCommand, boolean regex, Method rerouteTo,
                                     Object invokable, Tuple<Integer, Object>[] defaultArguments) {

            ArrayList<Tuple<Integer, Object>> newDefaultArguments = new ArrayList<>();
            for (Tuple<Integer, Object> defaultArgument : defaultArguments) {
                newDefaultArguments.add(defaultArgument);
            }

            addMethodReroute(command, matchCommand, regex, rerouteTo, invokable, newDefaultArguments);

        }

        public void addMethodReroute(String command, String matchCommand, boolean regex, Method rerouteTo,
                                     Object invokable, Tuple<Integer, Object>[] defaultArguments, int commandArgumentPosition){
            ArrayList<Tuple<Integer, Object>> newDefaultArguments = new ArrayList<>();
            for (Tuple<Integer, Object> defaultArgument : defaultArguments) {
                newDefaultArguments.add(defaultArgument);
            }

            addMethodReroute(command, matchCommand, regex, rerouteTo, invokable, newDefaultArguments, commandArgumentPosition);
        }

        public void addMethodReroute(String command, String matchCommand, boolean regex, Method rerouteTo,
                                     Object invokable, ArrayList<Tuple<Integer, Object>> defaultArguments, int commandArgumentPosition){

            if(defaultArguments.size() != 0){
                if(containsDuplicates(defaultArguments.get(0).getFirsts(defaultArguments))){
                    throw new IllegalArgumentException("Passed in defaultArguments contains duplicate indices");
                }else if(hasElementsLargerThanSize(defaultArguments.get(0).getFirsts(defaultArguments), rerouteTo.getParameterTypes().length)){
                    throw new IllegalArgumentException("Passed in defaultArguments contains indices larger than the methods paramter count");
                }
            }

            _methodRerouting.put(command, rerouteTo);
            _theInvokables.put(command, invokable);
            _defaultArguments.put(command, defaultArguments);
            _methodTakesCommandString.put(command, rerouteTo.getParameterTypes().length != 0
                                                   && defaultArguments.size() != rerouteTo.getParameterTypes().length);
            _commandArgPosition.put(command, commandArgumentPosition);
            _matchCommand.put(command, matchCommand);
            _methodUsesRegex.put(command, regex);

        }

        public void removeMethodReroute(String command){
            _methodRerouting.remove(command);
            _methodUsesRegex.remove(command);
            _methodTakesCommandString.remove(command);
            _matchCommand.remove(command);
            _theInvokables.remove(command);
        }

        @Override
        public void run(){

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String in;

            try {
                while((in = reader.readLine()) != null){
                    handleCommand(in);
                }
            }catch(IOException e){
                Log.e("InputManager", "Something went wrong " + e.getMessage());
            }
        }

        public void handleCommand(String command){
            boolean wasInvoked = false;
                /* Check for method rerouting */
            for (Map.Entry<String, Method> stringMethodEntry : _methodRerouting.entrySet()) {
                String key = stringMethodEntry.getKey();

                boolean matches = _methodUsesRegex.get(key) ? command.matches(_matchCommand.get(key)) : command.contains(_matchCommand.get(key));

                /* If the command matches a preroute */
                if (_methodUsesRegex.get(key) ? command.matches(_matchCommand.get(key)) : command.contains(_matchCommand.get(key))) {
                    try {
                        Object[] args = new Object[_defaultArguments.get(key).size() + (_methodTakesCommandString.get(key) ? 1 : 0)];
                        if (args.length == 0) {
                            _methodRerouting.get(key).invoke(_theInvokables.get(key));
                            continue;
                        }

                        if(_methodTakesCommandString.get(key)){
                            args[_commandArgPosition.get(key)] = command;
                        }

                        if (_defaultArguments.get(key).size() != 0) {
                            for (Tuple<Integer, Object> integerObjectTuple : _defaultArguments.get(key)) {
                                args[integerObjectTuple.x] = integerObjectTuple.y;
                            }
                        }

                        _methodRerouting.get(key).invoke(_theInvokables.get(key), args);
                        continue;

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        Log.e("InputManager", "Method passed in was not allowed to be invoked, does it take more than one argument?");
                        continue;
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                        Log.e("InputManager", "Something went wrong in the method: " + e.getTargetException().getMessage());
                        continue;
                    }
                }
            }
            /* If the method already has invoked we won't run it as a system-command */
            if(wasInvoked){
                return;
            }

            try {
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();

                BufferedReader normalOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String out;

                while((out = normalOutputReader.readLine()) != null){
                    System.out.println(out);
                }

                BufferedReader errorOutputReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                while((out = errorOutputReader.readLine()) != null){
                    System.out.println(out);
                }

            } catch (Exception e) {
                Log.e("InputManager", "Something went wrong when trying to run the command: " + e.getMessage());
            }
        }
    }

    /**
     * Class to manage content. Implements basic string-checking by checking one of three things:
     *
     * 1. Regex
     * 2. Containment
     * 3. Count
     *
     * Can be set to be either inclusive or exclusive.
     */
    public static class ContentManager{

        /**
         * The endpoint of the service using this ContentManager
         */
        private final String serviceEndpoint;

        /**
         * Boolean that decides whether or not content is to be included or excluded if a match is found
         */
        private boolean inclusive = false;

        /**
         * List of regex limitations
         */
        private ArrayList<String> regexLimitations;

        /**
         * List of contain limitations
         */
        private ArrayList<String> containLimitations;

        /**
         * Map of count limitations
         */
        private HashMap<String, Integer> countLimitations;

        /**
         * Constructor taking the service endpoint
         * @param serviceEndpoint
         */
        public ContentManager(String serviceEndpoint) {
            this.serviceEndpoint = serviceEndpoint;
            regexLimitations = new ArrayList<>();
            containLimitations = new ArrayList<>();
            countLimitations = new HashMap<>();
        }

        /**
         * Changes the endpoint by returning a new contentmanager, as the endpoint is final.
         * @param endpoint
         * @return
         */
        public ContentManager changeserviceEndpoint(String endpoint){
            ContentManager manager = new ContentManager(endpoint);
            manager.setRegexLimitations(this.regexLimitations);
            manager.setCountLimitations(this.countLimitations);
            manager.setContainLimitations(this.containLimitations);
            return new ContentManager(endpoint);
        }

        /**
         * Checks if a request is accepted by this contentmanager.
         * @param request
         * @return
         */
        public boolean accepts(String request){
            request = request.replaceAll("^"+serviceEndpoint, "");

            /* Check regex */
            for(String contentLimitation : regexLimitations) {
                if(request.matches(contentLimitation)){
                    return inclusive;
                }
            }

            /* Check contains */
            for(String containLimitation : containLimitations) {
                if(request.contains(containLimitation)){
                    return inclusive;
                }
            }

            /* Check count */
            for (Map.Entry<String, Integer> stringIntegerEntry : countLimitations.entrySet()) {
                if(request.split(stringIntegerEntry.getKey()).length >= stringIntegerEntry.getValue()+1){
                    return inclusive;
                }
            }
            return !inclusive;
        }

        /**
         * Sets the content manager to be inclusive. That is, only accept requests if they match any limitation.
         */
        public void setInclusive(){
            inclusive = true;
        }

        /**
         * Sets the content manager to be exclusive. That is, only accept requests if they do not match any limitation.
         * This is the default setting.
         */
        public void setExclusive(){
            inclusive = false;
        }

        /**
         * Adds a regex-string as a limitation
         */
        public void addRegex(String regex){
            regexLimitations.add(regex);
        }

        /**
         * Removes a regex-string as a limitation.
         */
        public void removeRegex(String regex){
            regexLimitations.remove(regex);
        }

        /**
         * Adds a phrase as a containment-limitation. I.e. if a request contains the phrase, it is matched.
         */
        public void addContains(String phrase){
            containLimitations.add(phrase);
        }

        /**
         * Removes a phrase as a containment-limitation.
         * @param phrase
         */
        public void removeContains(String phrase){
            containLimitations.remove(phrase);
        }

        /**
         * Adds a phrase as a count limitation. I.e if a request contains the phrase more than maxCount times, it is
         * matched.
         */
        public void addCountLimitation(String phrase, int maxCount){
            countLimitations.put(phrase, maxCount);
        }

        public void setRegexLimitations(ArrayList<String> regexLimitations) {
            this.regexLimitations = regexLimitations;
        }

        public void setContainLimitations(ArrayList<String> containLimitations) {
            this.containLimitations = containLimitations;
        }

        public void setCountLimitations(HashMap<String, Integer> countLimitations) {
            this.countLimitations = countLimitations;
        }
    }

    /**
     * Finds the first integer not covered by the list. E.g. if the list is [1 2 4 7] this method will return 3.
     * Or if the list is [1 3 5 2 4 5 8 7] the method will return 6.
     * @param list
     * @return The smallest integer not covered by the list, or -1 if the list covers all integers given by the length of the list.
     */
    public static int findFirstEmptyPosition(ArrayList<Integer> list){
        Collections.sort(list);

        for (int i = 0; i < list.size(); i++) {
            if(list.get(i) != i) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Takes a list and checks if it has duplicates in it. E.g. if the list is [2 3 2] this method will return true.
     * But if the list is [1 2 3 4 5 7 6] it will return false. Runs in O(n log(n))
     * @param list
     * @return
     */
    public static boolean containsDuplicates(ArrayList<? extends Comparable> list){
        Collections.sort(list);

        for (int i = 1; i < list.size(); i++) {
            if(list.get(i).equals(list.get(i-1))){
                return true;
            }
        }
        return false;
    }

    /**
     * Takes a list of non-comparables and checks if it has duplicates. E.g if the list is [Car.blue Car.red Car.blue] it returns true.
     * But if the list is [Dinosaur.Tyrannosaurus Dinosaur.Brontosaurus Dinosaur.Pterodactyl] it returns false. Checks equality using the
     * {@link #equals(Object)} function. Note that this method is O(n^2), use with care.
     * @param list
     * @return
     */
    public static boolean containsDuplicatesNonComparable(ArrayList<Object> list){
        ArrayList<Object> oList = new ArrayList(list);

        for (Object o : list) {
            for (Object o1 : oList) {
                if(o1.equals(o)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if any of the integer elements of the list is larger than the size-1
     * @param list
     * @return
     */
    public static boolean hasElementsLargerThanSize(ArrayList<Integer> list, int max){

        for (Integer integer : list) {
            if(integer > max){
                return true;
            }
        }
        return false;
    }



    /**
     * Get termination from a time-string
     * @param time
     * @return
     * @throws UnacceptableTerminationTimeFault
     */
    public static long interpretTerminationTime(String time) throws UnacceptableTerminationTimeFault{

        /* Try XsdDuration first */
        if(isXsdDuration(time)){
            return extractXsdDuration(time);
        }else if(isXsdDatetime(time)){
            try{
                return extractXsdDatetime(time);
            }catch(RuntimeException e){
                throw new UnacceptableTerminationTimeFault();
            }
        }else{
             /* Neither worked, send an unacceptableTerminationTimeFault*/
            throw new UnacceptableTerminationTimeFault();
        }

    }

    public static long extractXsdDuration(String time){
        Pattern years, months, days, hours, minutes, seconds;
        years = Pattern.compile("[0-9]+Y");
        months = Pattern.compile("[0-9]+M");
        days = Pattern.compile("[0-9]+D");
        hours = Pattern.compile("[0-9]+H");
        minutes = Pattern.compile("[0-9]+M");
        seconds = Pattern.compile("[0-9]+S");

        long currentTimeStamp = System.currentTimeMillis();

        String times[] = time.split("T");

        Matcher matcher = years.matcher(times[0]);
        if(matcher.find()){
            currentTimeStamp += 24*365*3600*1000*Long.parseLong(matcher.group().replace("Y", ""));
        }

        matcher = months.matcher(times[0]);
        if(matcher.find()){
            currentTimeStamp += 24*30.5*3600*1000*Long.parseLong(matcher.group().replace("M", ""));
        }

        matcher = days.matcher(times[0]);
        if(matcher.find()){
            currentTimeStamp += 24*30.5*3600*1000*Long.parseLong(matcher.group().replace("D", ""));
        }

        if(times.length != 2) {
            return currentTimeStamp;
        }

        matcher = hours.matcher(times[1]);
        if(matcher.find()){
            currentTimeStamp += 3600*1000*Long.parseLong(matcher.group().replace("H", ""));
        }

        matcher = minutes.matcher(times[1]);
        if(matcher.find()){
            currentTimeStamp += 60*1000*Long.parseLong(matcher.group().replace("M", ""));
        }

        matcher = seconds.matcher(times[1]);
        if(matcher.find()){
            currentTimeStamp += 1000*Long.parseLong(matcher.group().replace("S", ""));
        }

        return currentTimeStamp;
    }

    public static long extractXsdDatetime(String string){
        return DatatypeConverter.parseDateTime(string).getTimeInMillis();
    }

    /**
     * Checks if a string is formatted in XsdDatetime. This function might return true on strings that are validly formatted,
     * but contains invalid months. E.g. 2014-13-11T36:00:00Z-25:00, which is an invalid date in three places (date, hour and subtracted hour).
     * @param time
     * @return
     */
    public static boolean isXsdDatetime(String time){
        return time.matches("[0-9]{4}-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-6][0-9]:[0-6][0-9](Z|-[0-2][0-9]:[0-6][0-9])?");
    }

    /**
     * Checks if a string is a Xs:duration string with a regular expression.
     * @param time
     * @return
     */
    public static boolean isXsdDuration(String time) {
        return time.matches("^(-P|P)((([0-9]+Y)?([0-9]+M)?([0-9]+D))?)?(?:(T([0-9]+H)?([0-9]+M)?([0-9]+S)?))?");
    }

    public static String generateSHA1Key(String input) throws NoSuchAlgorithmException{
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        String hash = "";

        byte[] bytes = digest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();

        for (int i=0; i < bytes.length; i++) {

            sb.append( Integer.toString( ( bytes[i] & 0xff ) + 0x100, 16).substring( 1 ));
        }
        hash = sb.toString();
        return hash;
    }

    public static String generateNTSHKey(String input){
        return NothingToSeeHere.t(input);
    }

    public static String parseW3CEndpoint(String s) throws SubscribeCreationFailedFault{
        Pattern pattern = Pattern.compile("<(wsa:)?Address>[a-z0-9A-Z.: /\n]*</(wsa:)?Address>");
        Matcher matcher = pattern.matcher(s);

        if(matcher.find()){
            String raw = matcher.group().replaceAll("</?(wsa:)?Address>", "").replaceAll(" ", "").replaceAll("[\n]", "");
            if(!raw.matches("^https?://")){
                raw = "http://" + raw;
            }
            return raw;
        }else{
            throw new SubscribeCreationFailedFault();
        }
    }
}
