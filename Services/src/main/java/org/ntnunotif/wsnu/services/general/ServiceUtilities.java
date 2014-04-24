package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.PublisherRegistrationFailedFaultType;
import org.oasis_open.docs.wsn.br_2.ResourceNotDestroyedFaultType;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationFailedFault;
import org.oasis_open.docs.wsn.brw_2.ResourceNotDestroyedFault;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.bf_2.BaseFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.omg.PortableInterceptor.RequestInfo;
import org.trmd.ntsh.NothingToSeeHere;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
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

    /**
     * InputManager utility-class. This class contains functionality for handling inputs to stdin.
     * An inputmanager instance keeps several hashmaps containing information about <b>Method rerouting</b>. I.e. commands
     * that are to be rerouted to certain methods. If such a command is registered to stdin, the method registered for rerouting
     * on this command will be called.
     * Any other command will be run as a terminal command on the local operating system.
     *
     */
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
                            wasInvoked = true;
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
                        wasInvoked = true;
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
                throwUnacceptableTerminationTimeFault("en", "Could not interpret termination time, reason given: " + e.getMessage());
            }
        }else{
             /* Neither worked, send an unacceptableTerminationTimeFault*/
            throwUnacceptableTerminationTimeFault("en", "Could not interpret termination time, could not translate: " + time);
        }
    return -1;
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
     * @param timeøvde å knuse vindu

Deretter begynte «Maria» å synke.
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

    /**
     * This method is deprecated, please use {@link #getAddress(javax.xml.ws.wsaddressing.W3CEndpointReference)}
     * instead.
     */
    @Deprecated
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
            throwSubscribeCreationFailedFault("en", "Could not understand endpoint");
            return null;
        }
    }

    public static String getAddress(W3CEndpointReference endpoint) throws IllegalAccessException {
        Log.d("ServiceUtilities", "Retrieving address from W3CEndpointReference");
        Field field = null;
        try {
            field = endpoint.getClass().getDeclaredField("address");
            field.setAccessible(true);

        } catch (NoSuchFieldException e) {
            Log.w("ServiceUtilities", "getAddress did not find the field, trying harder");
            for (Field sfield : endpoint.getClass().getDeclaredFields()) {
                if(sfield.getName().matches("(.*)?[aA][dD][dD]?[rR]([eE][sS][sS])?") && sfield.getType().equals(String.class)){
                    field = sfield;
                    break;
                }
            }
            if (field == null) {
                Log.e("ServiceUtilities", "getAddress did not find the field, returning blank String");
                return "";
            }
        }

        Log.d("ServiceUtilities", "getAddress found field address, accessing field");
        field.setAccessible(true);
        Object fieldInst = field.get(endpoint);

        // If field was null, we should return empty address
        if (fieldInst == null) {
            Log.e("ServiceUtilities", "getAddress was called on endpoint with null address field, returning blank string");
            return "";
        }

        try {
            Log.d("ServiceUtilities", "getAddress found field address, finding uri");
            Field uri = fieldInst.getClass().getDeclaredField("uri");
            uri.setAccessible(true);
            Log.d("ServiceUtilities", "getAddress found uri, casting and returning");
            return (String)uri.get(fieldInst);
        } catch (NoSuchFieldException e) {
            Log.e("ServiceUtilities", "getAddress could not find actual uri, returning empty String");
            return "";
        }

    }

    /**
     * Filters out a url's address in an endpointreference. Note that this functon does not check if the uri is valid, as long as it matches the following regex:
     * [[^https?://[a-zA-Z0-9.:]+([.][a-zA-Z]+)?/]]
     * @param endpointReference
     * @return
     */
    public static String filterEndpointReference(String endpointReference) {
        return endpointReference.replaceAll("^https?://[a-zA-Z0-9.:]+([.][a-zA-Z]+)?/", "");
    }

    public static<T> T[] createArrayOfEquals(T t, int length){
        T ts[] = (T[]) Array.newInstance(t.getClass(), length);

        for (int i = 0; i < length; i++) {
            ts[i] = t;
        }
        return ts;
    }

    public static Notify createNotify(int messageCount, @Nonnull Object[] messageContent, @Nonnull String[] endpoint, @Nullable String[] producerReference, @Nonnull TopicExpressionType[] topic, @Nullable Object[] any){

        if(messageCount <= 0){
            throw new IllegalArgumentException("MessageCount has to be larger than 0");
        }

        if(producerReference != null){
           if(messageCount != producerReference.length){
               throw new IllegalArgumentException("The MessageCount passed in did not match the count of producerreference");
           }
        }

        if(topic != null){
            if(messageCount != topic.length){
                throw new IllegalArgumentException("The MessageCount passed in did not match the count of topics");
            }
        }

        if(messageCount != endpoint.length){
            throw new IllegalArgumentException("The MessageCount passed in did not match the count of endpoints");
        }

        if(messageCount != messageContent.length){
            throw new IllegalArgumentException("The MessageCount passed in did not match the count of Messages");
        }

        Notify notify = new Notify();

        List<NotificationMessageHolderType> notificationMessages = notify.getNotificationMessage();
        for (int i = 0; i < messageCount; i++) {
            NotificationMessageHolderType notificationMessage = new NotificationMessageHolderType();
            NotificationMessageHolderType.Message message = new NotificationMessageHolderType.Message();

            /* Set message */
            Class messageClass = messageContent[i].getClass();
            message.setAny(messageClass.cast(messageContent[i]));
            notificationMessage.setMessage(message);

            /* Create endpoint reference */
            W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
            builder.address(endpoint[i]);
            notificationMessage.setSubscriptionReference(builder.build());

            /* Create producer reference */
            if(producerReference != null){
                builder.address(producerReference[i]);
                notificationMessage.setProducerReference(builder.build());
            }

            if(topic != null){
                notificationMessage.setTopic(topic[i]);
            }

            notificationMessages.add(notificationMessage);
        }

        if(any != null){
            for (Object o : any) {
                notify.getAny().add(o);
            }
        }
        return notify;
    }

    /* ===== Single message functions ==== */

    public static Notify createNotify(@Nonnull Object messageContent, @Nonnull String endpoint, @Nullable String producerReference, @Nullable TopicExpressionType topic){
        return createNotify(1, new Object[]{messageContent}, new String[]{endpoint}, new String[]{producerReference}, new TopicExpressionType[]{topic}, null);
    }

    public static Notify createNotify(@Nonnull Object messageContent, @Nonnull String endpoint, @Nullable TopicExpressionType topic){
        return createNotify(1, new Object[]{messageContent}, new String[]{endpoint}, null,  new TopicExpressionType[]{topic}, null);
    }

    public static Notify createNotify(@Nonnull Object messageContent, @Nonnull String endpoint, @Nullable String producerReference){
        return createNotify(1, new Object[]{messageContent}, new String[]{endpoint}, new String[]{producerReference}, null, null);
    }

    public static Notify createNotify(@Nonnull Object messageContent, @Nonnull String endpoint){
        return createNotify(1, new Object[]{messageContent}, new String[]{endpoint}, null, null, null);
    }



    /* ===== Multiple message functions */

    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String[] endpoint){
        return createNotify(messageContent.length, messageContent, endpoint, null, null, null);
    }

    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String[] endpoint, @Nullable String producerReference[]){
        return createNotify(messageContent.length, messageContent, endpoint, producerReference, null, null);
    }

    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String[] endpoint, @Nullable  TopicExpressionType topic[]){
        return createNotify(messageContent.length, messageContent, endpoint, null, topic, null);
    }

    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String[] endpoint, @Nullable String producerReference[], @Nullable TopicExpressionType[] topic){
        return createNotify(messageContent.length, messageContent, endpoint, producerReference, topic, null);
    }


    /* ===== Multiple message functions with single endpointreference ===== */

    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String endpoint){
        return createNotify(messageContent.length, messageContent, createArrayOfEquals(endpoint, messageContent.length), null, null, null);
    }

    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String endpoint, @Nullable String producerReference[]){
        return createNotify(messageContent.length, messageContent, createArrayOfEquals(endpoint, messageContent.length), producerReference, null, null);
    }

    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String endpoint, @Nullable  TopicExpressionType topic[]){
        return createNotify(messageContent.length, messageContent, createArrayOfEquals(endpoint, messageContent.length), null, topic, null);
    }

    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String endpoint, @Nullable String producerReference[], @Nullable TopicExpressionType[] topic){
        return createNotify(messageContent.length, messageContent, createArrayOfEquals(endpoint, messageContent.length), producerReference, topic, null);
    }

    /**
     * Creates a shallow clone of the {@link org.oasis_open.docs.wsn.b_2.Notify} given. That is, it does not clone
     * the actual content, only the holders in the <code>Notify</code>.
     *
     * @param notify The <code>Notify</code> to clone
     * @return a shallow clone of the <code>Notify</code>
     */
    public static Notify cloneNotifyShallow(Notify notify) {
        Notify returnValue = new Notify();

        List<NotificationMessageHolderType> returnHolders = returnValue.getNotificationMessage();
        List<Object> returnAny = returnValue.getAny();
        for (NotificationMessageHolderType notificationMessageHolderType : notify.getNotificationMessage())
                returnHolders.add(notificationMessageHolderType);

        for (Object any : notify.getAny())
            returnAny.add(any);

        return returnValue;
    }

    public static void throwUnacceptableInitialTerminationTimeFault(String language, String description) throws UnacceptableInitialTerminationTimeFault{
        UnacceptableInitialTerminationTimeFaultType type = new UnacceptableInitialTerminationTimeFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            type.setMinimumTime(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }

        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        type.getDescription().add(desc);

        throw new UnacceptableInitialTerminationTimeFault(description, type);
    }

    public static void throwUnacceptableTerminationTimeFault(String language, String description) throws UnacceptableTerminationTimeFault {
        UnacceptableTerminationTimeFaultType type = new UnacceptableTerminationTimeFaultType();

        // TODO Should maxtime be set?
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            type.setMinimumTime(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }

        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        type.getDescription().add(desc);

        throw new UnacceptableTerminationTimeFault(description, type);
    }

    public static void throwPublisherRegistrationFailedFault(String language, String description) throws PublisherRegistrationFailedFault {
        PublisherRegistrationFailedFaultType type = new PublisherRegistrationFailedFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            type.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }

        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        type.getDescription().add(desc);

        throw new PublisherRegistrationFailedFault(description, type);

    }

    public static void throwResourceUnknownFault(String language, String description) throws ResourceUnknownFault {
        ResourceUnknownFaultType type = new ResourceUnknownFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            type.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }

        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);

        type.getDescription().add(desc);

        throw new ResourceUnknownFault(description, type);
    }

    public static void throwUnableToDestroySubscriptionFault(String language, String description) throws UnableToDestroySubscriptionFault {
        UnableToDestroySubscriptionFaultType type = new UnableToDestroySubscriptionFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            type.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }

        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);

        type.getDescription().add(desc);

        throw new UnableToDestroySubscriptionFault(description, type);
    }

    public static void throwSubscribeCreationFailedFault(String language, String description) throws SubscribeCreationFailedFault {
        SubscribeCreationFailedFaultType type = new SubscribeCreationFailedFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            type.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }

        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);

        type.getDescription().add(desc);

        throw new SubscribeCreationFailedFault(description, type);
    }

    public static void throwPublisherRegistrationFault(String language, String description) throws PublisherRegistrationFailedFault {
        PublisherRegistrationFailedFaultType type = new PublisherRegistrationFailedFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            type.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }

        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);

        type.getDescription().add(desc);

        throw new PublisherRegistrationFailedFault(description, type);
    }


    /**
     * Builds and throws an {@link org.oasis_open.docs.wsn.b_2.InvalidFilterFaultType}
     *
     * @param language the language of the description, as defined in {@link org.oasis_open.docs.wsrf.bf_2.BaseFaultType.Description}
     * @param description the description of the fault, as defined in {@link org.oasis_open.docs.wsrf.bf_2.BaseFaultType.Description}
     * @param filterName the name of the filter that was not understood
     * @throws InvalidFilterFault
     */
    public static void throwInvalidFilterFault(String language, String description, QName filterName) throws
            InvalidFilterFault {

        InvalidFilterFaultType faultType = new InvalidFilterFaultType();
        faultType.getUnknownFilter().add(filterName);
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            faultType.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);
        throw new InvalidFilterFault(description, faultType);
    }

    public static void throwInvalidMessageContentExpressionFault(String language, String description) throws
            InvalidMessageContentExpressionFault {

        InvalidMessageContentExpressionFaultType faultType = new InvalidMessageContentExpressionFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            faultType.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);
        throw new InvalidMessageContentExpressionFault(description, faultType);
    }

    /**
     * Attempts to extract a {@link java.lang.String} representing the content of the
     * {@link org.oasis_open.docs.wsn.b_2.QueryExpressionType}.
     *
     * @param expressionType the expression to examine
     * @return the content of the expression as a <code>String</code>.
     * @throws java.lang.IllegalArgumentException if the expression contains no or multiple <code>String</code>s.
     */
    public static String extractQueryExpressionString(QueryExpressionType expressionType) {
        String retValue = null;
        for (Object o : expressionType.getContent()) {
            if (o instanceof String) {
                if (retValue!= null) {
                    throw new IllegalArgumentException("The QueryExpressionType had too complex content");
                } else
                    retValue = (String)o;
            }
        }
        if (retValue == null) {
            throw new IllegalArgumentException("The QueryExpressionType did not contain any String");
        } else {
            return retValue;
        }
    }

    public static void throwNoCurrentMessageOnTopicFault(String language, String description) throws NoCurrentMessageOnTopicFault {
        NoCurrentMessageOnTopicFaultType faultType = new NoCurrentMessageOnTopicFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            faultType.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);
        throw new NoCurrentMessageOnTopicFault(description, faultType);
    }

    public static void throwTopicNotSupportedFault(String language, String description) throws TopicNotSupportedFault{
        TopicNotSupportedFaultType faultType = new TopicNotSupportedFaultType();
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);

        throw new TopicNotSupportedFault(description, faultType);
    }

    public static void throwResouceNotDestroyedFault(String language, String description) throws ResourceNotDestroyedFault{
        ResourceNotDestroyedFaultType faultType = new ResourceNotDestroyedFaultType();
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);

        throw new ResourceNotDestroyedFault(description, faultType);
    }

    public static void throwPauseFailedFault(String language, String description) throws PauseFailedFault {
        PauseFailedFaultType faultType = new PauseFailedFaultType();
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);

        throw new PauseFailedFault(description, faultType);
    }

    public static void throwResumeFailedFault(String language, String description) throws ResumeFailedFault {
        ResumeFailedFaultType faultType = new ResumeFailedFaultType();
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);

        throw new ResumeFailedFault(description, faultType);
    }

    public static InternalMessage sendRequest(String url) throws Exception{
        InternalMessage message = new InternalMessage(InternalMessage.STATUS_OK, null);
        RequestInformation requestInformation = new RequestInformation();
        requestInformation.setEndpointReference(url);
        message.setRequestInformation(requestInformation);
        return ApplicationServer.getInstance().sendMessage(message);
    }
}
