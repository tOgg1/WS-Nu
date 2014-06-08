package org.ntnunotif.wsnu.services.general;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.util.List;

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

}
