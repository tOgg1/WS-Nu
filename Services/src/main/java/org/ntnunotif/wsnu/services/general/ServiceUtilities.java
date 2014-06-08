//-----------------------------------------------------------------------------
// Copyright (C) 2014 Tormod Haugland and Inge Edward Haulsaunet
//
// This file is part of WS-Nu.
//
// WS-Nu is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// WS-Nu is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with WS-Nu. If not, see <http://www.gnu.org/licenses/>.
//-----------------------------------------------------------------------------

package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.oasis_open.docs.wsn.b_2.QueryExpressionType;
import org.oasis_open.docs.wsn.bw_2.UnacceptableTerminationTimeFault;
import org.trmd.ntsh.NothingToSeeHere;
import org.w3c.dom.Node;

import javax.xml.bind.DatatypeConverter;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ntnunotif.wsnu.base.util.InternalMessage.STATUS_HAS_MESSAGE;
import static org.ntnunotif.wsnu.base.util.InternalMessage.STATUS_OK;

/**
 *
 * Created by tormod on 24.03.14.
 */
public class ServiceUtilities {

    private final static W3CEndpointReferenceBuilder w3CEndpointReferenceBuilder = new W3CEndpointReferenceBuilder();

    /**
     * Takes a termination-time string, represented either as XsdDuration or XsdDatetime, and returns it specified (end)date.
     * @param time The termination-time string: either XsdDuration or XsdDatetime.
     * @return The parsed termination time as a timestamp, long.
     * @throws UnacceptableTerminationTimeFault If the passed in {@link java.lang.String} was not a valid XsdDuration or XsdDatetime
     * time, or if some {@link java.lang.RuntimeException} occurred during the extraction. This can be caused by the {@link javax.xml.bind.DatatypeConverter}
     * which is used to parse XsdDatetime.
     */
    public static long interpretTerminationTime(String time) throws UnacceptableTerminationTimeFault{
        try{

            /* Try XsdDuration first */
            if(isXsdDuration(time)){
                return extractXsdDuration(time);
            }else if(isXsdDatetime(time)){
                return extractXsdDatetime(time);
            }else{
                 /* Neither worked, send an unacceptableTerminationTimeFault*/
                ExceptionUtilities.throwUnacceptableTerminationTimeFault("en", "Could not interpret termination time, could not translate: " + time);
            }
        }catch(RuntimeException e){
            ExceptionUtilities.throwUnacceptableTerminationTimeFault("en", "Could not interpret termination time, reason given: " + e.getMessage());
        }

        // Compiler pleasing
        return -1;
    }

    /**
     * Extracts the endtime specified by a XsdDuration string. This method will return the duration specified, added on to
     * the systems current local time.
     * @param time The duration as specified by a XsdDuration string.
     * @return A timestamp
     */
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

    /**
     * Extracts the timestamp of a XsdDateTime string.
     * @param string A XsdDatetime represented as a {@link java.lang.String}.
     * @return A timestamp.
     */
    public static long extractXsdDatetime(String string){
        return DatatypeConverter.parseDateTime(string).getTimeInMillis();
    }

    /**
     * Checks if a string is formatted in XsdDatetime. This function might return true on strings that are validly formatted,
     * but contains invalid months. E.g. 2014-13-11T36:00:00Z-25:00, which is an invalid date in three places (date, hour and subtracted hour).
     * @param
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

    /**
     * Generates a SHA1 hash from a string.
     * @param input The string that is to be hashed.
     * @return A SHA1 hash
     * @throws NoSuchAlgorithmException If the SHA1 hash algorithm is not available on the system.
     */
    public static String generateSHA1Key(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        String hash;

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
     * Tries to unwrap a {@link javax.xml.ws.wsaddressing.W3CEndpointReference} to a String. Does this by multiple application
     * of reflection.
     * @param endpoint The endpoint that is to be unwrapped.
     * @return A string-representation of the endpoint reference
     */
    public static String getAddress(W3CEndpointReference endpoint) {
        try{
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
        } catch(IllegalAccessException e){
            Log.e("ServiceUtilities", "getAddress was stopped by an IllegalAccessException, this should not happen. " +
                  "Please contact at https://github.com/tOgg1/WS-Nu. Returning blank string");
            return "";
        }
    }

    /**
     * Builds a {@link javax.xml.ws.wsaddressing.W3CEndpointReference} from a String.
     * @param endpoint
     * @return
     */
    public static W3CEndpointReference buildW3CEndpointReference(String endpoint){
        w3CEndpointReferenceBuilder.address(endpoint);
        return w3CEndpointReferenceBuilder.build();
    }

    /**
     * Filters out a url's address in an endpointreference. Note that this functon does not check if the uri is valid, as long as it matches the following regex:
     * [[^https?://[a-zA-Z0-9.:]+([.][a-zA-Z]+)?/]].
     * This method is now deprecated, see {@link org.ntnunotif.wsnu.base.util.Utilities#stripUrlOfProtocolAndHost(String)}.
     * @param endpointReference
     * @return
     */
    @Deprecated
    public static String filterEndpointReference(String endpointReference) {
        return endpointReference.replaceAll("^https?://[a-zA-Z0-9.:]+([.][a-zA-Z]+)?/", "");
    }

    /**
     * Fetches the external ip through amazonaw's utility. Note that this method may break at any given point, do not use
     * for essential functionality
     * @return Your external ip
     */
    public static String getExternalIp() {
        try {
            URL url = new URL("http://checkip.amazonaws.com/");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            return in.readLine();
        } catch (MalformedURLException e) {
            // This will deterministically never happen
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Helper method that creates an array filled with with the same element of type {@link T}, of length 'length'.
     * @param t The value which the array is to be filled with.
     * @param length The length of the array.
     * @param <T> The parameter type
     * @return An as specified above.
     */
    @SuppressWarnings("unchecked")
    public static<T> T[] createArrayOfEquals(T t, int length){
        T ts[] = (T[]) Array.newInstance(t.getClass(), length);

        for (int i = 0; i < length; i++) {
            ts[i] = t;
        }
        return ts;
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
            } else {
                throw new IllegalArgumentException("The QueryExpressionType had too complex content");
            }
        }
        if (retValue == null) {
            throw new IllegalArgumentException("The QueryExpressionType did not contain any String");
        } else {
            return retValue;
        }
    }

    //TODO
    /**
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static InternalMessage sendRequest(String url) {
        InternalMessage message = new InternalMessage(STATUS_OK, null);
        RequestInformation requestInformation = new RequestInformation();
        requestInformation.setEndpointReference(url);
        message.setRequestInformation(requestInformation);
        return ApplicationServer.getInstance().sendMessage(message);
    }

    //TODO
    /**
     *
     * @param endpoint
     * @param node
     * @param hub
     * @return
     */
    public static InternalMessage sendNode(String endpoint, Node node, Hub hub){
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, null);

        RequestInformation requestInformation = new RequestInformation();
        requestInformation.setEndpointReference(endpoint);
        message.setRequestInformation(requestInformation);

        message.setMessage(node);

        return hub.acceptLocalMessage(message);
    }
}
