package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.RegisterPublisher;
import org.oasis_open.docs.wsn.bw_2.UnacceptableTerminationTimeFault;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;
import static org.ntnunotif.wsnu.services.general.ServiceUtilities.createArrayOfEquals;

/**
 *
 */
public class WsnUtilities {

    /**
     * String that is used in the publisher registration urls. I.e. http://domain.com/thisWebService?publisherregistration=1234
     */
    public static final String publisherRegistrationString = "publisherregistration";

    /**
     * String that is used in the subscription urls. I.e. http://domain.com/thisWebService?subscription=1234
     */
    public static final String subscriptionString = "subscription";

    /**
     * Creates a {@link org.oasis_open.docs.wsn.b_2.Notify}-object.
     * @param messageCount The count of {@link org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType}, i.e. NotificationMessages.
     * @param messageContent The content of all messages. Must be of same length as the messageCount parameter.
     * @param endpoint The endpoint's of all messages. Must be of same length as the messageCount parameter.
     * @param producerReference The reference to the producer of the NotificationMessages. Must be of same length as the messageCount parameter.
     * @param topic The topics for each message. Must be of same length as the messageCount parameter.
     * @param any Anything else. Must be of same length as the messageCount parameter.
     * @return A {@link org.oasis_open.docs.wsn.b_2.Notify} object.
     */
    public static Notify createNotify(int messageCount, @Nonnull Object[] messageContent, @Nonnull String[] endpoint, @Nullable String[] producerReference, @Nullable TopicExpressionType[] topic, @Nullable Object[] any){

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

    /**
     * See {@link #createNotify(int, Object[], String[], String[], org.oasis_open.docs.wsn.b_2.TopicExpressionType[], Object[])}
     * @param messageContent
     * @param endpoint
     * @param producerReference
     * @param topic
     * @return
     */
    public static Notify createNotify(@Nonnull Object messageContent, @Nonnull String endpoint, @Nullable String producerReference, @Nullable TopicExpressionType topic){
        return createNotify(1, new Object[]{messageContent}, new String[]{endpoint}, new String[]{producerReference}, new TopicExpressionType[]{topic}, null);
    }

    /**
     * See {@link #createNotify(int, Object[], String[], String[], org.oasis_open.docs.wsn.b_2.TopicExpressionType[], Object[])}
     * @param messageContent
     * @param endpoint
     * @param topic
     * @return
     */
    public static Notify createNotify(@Nonnull Object messageContent, @Nonnull String endpoint, @Nullable TopicExpressionType topic){
        return createNotify(1, new Object[]{messageContent}, new String[]{endpoint}, null,  new TopicExpressionType[]{topic}, null);
    }

    /**
     * See {@link #createNotify(int, Object[], String[], String[], org.oasis_open.docs.wsn.b_2.TopicExpressionType[], Object[])}
     * @param messageContent
     * @param endpoint
     * @param producerReference
     * @return
     */
    public static Notify createNotify(@Nonnull Object messageContent, @Nonnull String endpoint, @Nullable String producerReference){
        return createNotify(1, new Object[]{messageContent}, new String[]{endpoint}, new String[]{producerReference}, null, null);
    }

    /**
     * See {@link #createNotify(int, Object[], String[], String[], org.oasis_open.docs.wsn.b_2.TopicExpressionType[], Object[])}
     * @param messageContent
     * @param endpoint
     * @return
     */
    public static Notify createNotify(@Nonnull Object messageContent, @Nonnull String endpoint){
        return createNotify(1, new Object[]{messageContent}, new String[]{endpoint}, null, null, null);
    }



    /* ===== Multiple message functions */

    /**
     * See {@link #createNotify(int, Object[], String[], String[], org.oasis_open.docs.wsn.b_2.TopicExpressionType[], Object[])}
     * @param messageContent
     * @param endpoint
     * @return
     */
    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String[] endpoint){
        return createNotify(messageContent.length, messageContent, endpoint, null, null, null);
    }

    /**
     * See {@link #createNotify(int, Object[], String[], String[], org.oasis_open.docs.wsn.b_2.TopicExpressionType[], Object[])}
     * @param messageContent
     * @param endpoint
     * @param producerReference
     * @return
     */
    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String[] endpoint, @Nullable String producerReference[]){
        return createNotify(messageContent.length, messageContent, endpoint, producerReference, null, null);
    }

    /**
     * See {@link #createNotify(int, Object[], String[], String[], org.oasis_open.docs.wsn.b_2.TopicExpressionType[], Object[])}
     * @param messageContent
     * @param endpoint
     * @param topic
     * @return
     */
    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String[] endpoint, @Nullable TopicExpressionType topic[]){
        return createNotify(messageContent.length, messageContent, endpoint, null, topic, null);
    }

    /**
     * See {@link #createNotify(int, Object[], String[], String[], org.oasis_open.docs.wsn.b_2.TopicExpressionType[], Object[])}
     * @param messageContent
     * @param endpoint
     * @param producerReference
     * @param topic
     * @return
     */
    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String[] endpoint, @Nullable String producerReference[], @Nullable TopicExpressionType[] topic){
        return createNotify(messageContent.length, messageContent, endpoint, producerReference, topic, null);
    }


    /* ===== Multiple message functions with single endpointreference ===== */

    /**
     * See {@link #createNotify(int, Object[], String[], String[], org.oasis_open.docs.wsn.b_2.TopicExpressionType[], Object[])}
     * @param messageContent
     * @param endpoint
     * @return
     */
    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String endpoint){
        return createNotify(messageContent.length, messageContent, createArrayOfEquals(endpoint, messageContent.length), null, null, null);
    }

    /**
     * See {@link #createNotify(int, Object[], String[], String[], org.oasis_open.docs.wsn.b_2.TopicExpressionType[], Object[])}
     * @param messageContent
     * @param endpoint
     * @param producerReference
     * @return
     */
    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String endpoint, @Nullable String producerReference[]){
        return createNotify(messageContent.length, messageContent, createArrayOfEquals(endpoint, messageContent.length), producerReference, null, null);
    }

    /**
     * See {@link #createNotify(int, Object[], String[], String[], org.oasis_open.docs.wsn.b_2.TopicExpressionType[], Object[])}
     * @param messageContent
     * @param endpoint
     * @param topic
     * @return
     */
    public static Notify createNotify(@Nonnull Object[] messageContent, @Nonnull String endpoint, @Nullable  TopicExpressionType topic[]){
        return createNotify(messageContent.length, messageContent, createArrayOfEquals(endpoint, messageContent.length), null, topic, null);
    }

    /**
     * See {@link #createNotify(int, Object[], String[], String[], org.oasis_open.docs.wsn.b_2.TopicExpressionType[], Object[])}
     * @param messageContent
     * @param endpoint
     * @param producerReference
     * @param topic
     * @return
     */
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


    /**
     * Sends a {@link org.oasis_open.docs.wsn.b_2.Subscribe}, i.e. a subscription request. This method does not require
     * a specified termination date, and will send a termination time in exactly 1 year.
     *
     * @param address The address of the producer or broker that is to be subscribed to.
     * @param consumerReference The consumer reference. I.e. the NotificationConsumerImpl that is to receive notifications.
     * @param hub A reference to a hub we can send our message through.
     * @return An InternalMessage with the result of the request.
     */
    public static InternalMessage sendSubscriptionRequest(String address, String consumerReference, Hub hub){
        return sendSubscriptionRequest(address, "P1D", consumerReference, hub);
    }

    /**
     * Sends a {@link org.oasis_open.docs.wsn.b_2.Subscribe}, i.e. a subscription request.
     *
     * @param address The address of the producer or broker that is to be subscribed to.
     * @param consumerReference The consumer reference. I.e. the NotificationConsumerImpl that is to receive notifications.
     * @param hub A reference to a hub we can send our message through.
     * @return An InternalMessage with the result of the request.
     */
    public static InternalMessage sendSubscriptionRequest(String address, String terminationTime, String consumerReference, Hub hub){
        Subscribe subscribe = new Subscribe();
        subscribe.setConsumerReference(ServiceUtilities.buildW3CEndpointReference(consumerReference));
        subscribe.setInitialTerminationTime(ServiceUtilities.baseFactory.createSubscribeInitialTerminationTime(terminationTime));

        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, subscribe);
        message.getRequestInformation().setEndpointReference(address);
        return hub.acceptLocalMessage(message);
    }

    /**
     * Sends a {@link org.oasis_open.docs.wsn.b_2.Unsubscribe}, i.e. a request to unsubscribe.
     *
     * @param subscriptionEndpoint The endpoint of the subscription. Typically something like "http://domain.com/someWebService?subscription=123123
     * @param hub A reference to a hub we can send our message through.
     * @return An InternalMessage with the result of the request.
     */
    public static InternalMessage sendUnsubscribeRequest(String subscriptionEndpoint, Hub hub){
        Unsubscribe unsubscribe = new Unsubscribe();

        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, unsubscribe);
        message.getRequestInformation().setEndpointReference(subscriptionEndpoint);
        return hub.acceptLocalMessage(message);
    }

    /**
     * Sends a {@link org.oasis_open.docs.wsn.b_2.Renew}, i.e. a request to renew. This method does not require
     * a specified termination date, and will send a termination time in exactly 1 year.
     *
     * @param subscriptionEndpoint The endpoint of the subscription. Typically something like "http://domain.com/someWebService?subscription=123123
     * @param hub A reference to a hub we can send our message through.
     * @return An InternalMessage with the result of the request.
     */
    public static InternalMessage sendRenewRequest(String subscriptionEndpoint, Hub hub){
        return sendRenewRequest(subscriptionEndpoint, "P1D", hub);
    }

    /**
     * Sends a {@link org.oasis_open.docs.wsn.b_2.Renew}, i.e. a request to renew. This method does not require
     * a specified termination date, and will send a termination time in exactly 1 year.
     *
     * @param subscriptionEndpoint The endpoint of the subscription. Typically something like "http://domain.com/someWebService?subscription=123123
     * @param hub A reference to a hub we can send our message through.
     * @return An InternalMessage with the result of the request.
     */
    public static InternalMessage sendRenewRequest(String subscriptionEndpoint, String terminationTime, Hub hub){
        Renew renew = new Renew();
        renew.setTerminationTime(terminationTime);
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, renew);
        message.getRequestInformation().setEndpointReference(subscriptionEndpoint);
        return hub.acceptLocalMessage(message);
    }

    /**
     * Sends a {@link org.oasis_open.docs.wsn.b_2.PauseSubscription}, i.e. a request to pause a subscription.
     *
     * @param subscriptionEndpoint The endpoint of the subscription. Typically something like "http://domain.com/someWebService?subscription=123123
     * @param hub A reference to a hub we can send our message through.
     * @return An InternalMessage with the result of the request.
     */
    public static InternalMessage sendPauseRequest(String subscriptionEndpoint, Hub hub){
        PauseSubscription pauseSubscription = new PauseSubscription();
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, pauseSubscription);
        message.getRequestInformation().setEndpointReference(subscriptionEndpoint);
        return hub.acceptLocalMessage(message);
    }

    /**
     * Sends a {@link org.oasis_open.docs.wsn.b_2.ResumeSubscription}, i.e. a request to resume a subscription.
     *
     * @param subscriptionEndpoint The endpoint of the subscription. Typically something like "http://domain.com/someWebService?subscription=123123
     * @param hub A reference to a hub we can send our message through.
     * @return An InternalMessage with the result of the request.
     */
    public static InternalMessage sendResumeRequest(String subscriptionEndpoint, Hub hub){
        ResumeSubscription resumeSubscription = new ResumeSubscription();
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, resumeSubscription);
        message.getRequestInformation().setEndpointReference(subscriptionEndpoint);
        return hub.acceptLocalMessage(message);
    }

    /**
     * Sends a {@link org.oasis_open.docs.wsn.br_2.RegisterPublisher}, i.e. a request to register a publisher with a broker.
     * This method does not take in a termination time or a demand-flag, and thus sets the termination time automatically to exactly
     * one year from when the method is called, and sets demand-based publishing to false.
     *
     * @param brokerEndpoint The endpoint of the NotificationBroker.g
     * @param publisherReference A reference to the publisher registrating with the broker.
     * @param hub A reference to a hub we can send our message through.
     * @return
     */
    public static InternalMessage sendPublisherRegistrationRequest(String brokerEndpoint, String publisherReference, Hub hub) {
        return sendPublisherRegistrationRequest(brokerEndpoint, System.currentTimeMillis() + 86400, false, publisherReference, hub);
    }

    /**
     * Sends a {@link org.oasis_open.docs.wsn.br_2.RegisterPublisher}, i.e. a request to register a publisher with a broker.
     *
     * @param brokerEndpoint The endpoint of the NotificationBroker.
     * @param terminationTime The termination time of the registration.
     * @param demand A flag to use demand-based publishing
     * @param publisherReference A reference to the publisher registrating with the broker.
     * @param hub A reference to a hub we can send our message through.
     * @return
     */
    public static InternalMessage sendPublisherRegistrationRequest(String brokerEndpoint, long terminationTime, boolean demand, String publisherReference, Hub hub){
        RegisterPublisher registerPublisher = new RegisterPublisher();
        registerPublisher.setPublisherReference(ServiceUtilities.buildW3CEndpointReference(publisherReference));
        registerPublisher.setDemand(demand);
        try {
            GregorianCalendar now = new GregorianCalendar();
            now.setTime(new Date(terminationTime));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            registerPublisher.setInitialTerminationTime(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("WebService", "Something went wrong while creating XMLGregoriancalendar");
        }
        InternalMessage outMessage = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, registerPublisher);
        outMessage.getRequestInformation().setEndpointReference(brokerEndpoint);
        return hub.acceptLocalMessage(outMessage);
    }

    /**
     * Sends a {@link org.oasis_open.docs.wsn.br_2.RegisterPublisher}, i.e. a request to register a publisher with a broker.
     *
     * @param brokerEndpoint The endpoint of the NotificationBroker.g
     * @param date A date encoded as either a XsdDuration or XsdDatetime timestamp
     * @param publisherReference A reference to the publisher registrating with the broker.
     * @param hub A reference to a hub we can send our message through.
     * @return
     */
    public static InternalMessage sendPublisherRegistrationRequest(String brokerEndpoint, String date, boolean demand, String publisherReference, Hub hub) {
        try {
            long rawDate = ServiceUtilities.interpretTerminationTime(date);
            return sendPublisherRegistrationRequest(brokerEndpoint, rawDate, demand, publisherReference, hub);
        } catch (UnacceptableTerminationTimeFault unacceptableTerminationTimeFault) {
            Log.e("WebService", "Could not parse date");
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INTERNAL_ERROR, null);
        }
    }

    /**
     * Sends a {@link org.oasis_open.docs.wsn.b_2.GetCurrentMessage}, i.e. a request to get the latest message on a topic.
     *
     * @param endpoint The endpoint of the producer or broker.
     * @param topic The topic we want to get the latest message on
     * @param hub
     * @return
     */
    public static InternalMessage sendGetCurrentMessage(String endpoint, TopicExpressionType topic, Hub hub){
        GetCurrentMessage getCurrentMessage = new GetCurrentMessage();
        getCurrentMessage.setTopic(topic);
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, getCurrentMessage);

        RequestInformation requestInformation = new RequestInformation();
        requestInformation.setEndpointReference(endpoint);
        message.setRequestInformation(requestInformation);

        return hub.acceptLocalMessage(message);
    }

    /**
     * Fetches a remote wsdl-file. The method expects a valid endpoint reference, and then attaches a "?wsdl"
     * parameter to it.
     *
     * @param endpoint The endpoint reference of the Web Service we want to fetch a wsdl from.
     * @return The wsdl-file in the form of a string if the wsdl file is found. If not, null is returned.
     */
    public static String fetchRemoteWsdl(String endpoint){
        String uri = endpoint + "?wsdl";
        InternalMessage returnMessage = ServiceUtilities.sendRequest(uri);

        if((returnMessage.statusCode & STATUS_HAS_MESSAGE ) == 0){
            Log.e("WebService.fetchRemoteWsdl", "Wsdl not found");
        }

        if((returnMessage.statusCode & STATUS_FAULT) > 0){
            if((returnMessage.statusCode & STATUS_FAULT_INTERNAL_ERROR) > 0){
                Log.e("WebService.fetchRemoteWsdl", "Some error occured remotely: " + returnMessage.getRequestInformation().getHttpStatus());
            }else{
                Log.e("WebService.fetchRemoteWsdl", "Some internal error occured" + returnMessage.statusCode);
            }
        }

        if((returnMessage.statusCode & STATUS_OK) > 0){
            try{
                return (String)returnMessage.getMessage();
            }catch(ClassCastException e){
                try{
                    return new String((byte[])returnMessage.getMessage());
                }catch(ClassCastException f){
                    Log.e("WebService.fetchRemoteWsdl", "The returnMessage was not a String, or a byte[]. Please use either of these" +
                            "when returning data to this method.");
                    return null;
                }
            }
        }

        Log.e("WebService.fetchRemoteWsdl", "Incorrect flags was set before the the InternalMessage was returned to this method." +
                "Please set STATUS_OK | STATUS_HAS_MESSAGE | STATUS_MESSAGE_IS_INPUTSTREAM if everything went okey");
        return null;
    }
}
