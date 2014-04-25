package org.ntnunotif.wsnu.services.eventhandling;

import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventObject;
import java.util.List;

/**
 * Created by tormod on 3/13/14.
 */
//TODO: What more methods do we need?
public final class NotificationEvent extends EventObject{

    private Notify _notification;
    private RequestInformation _requestInformation;
    private Calendar _timestamp = Calendar.getInstance();

    /**
     * Creates an event with source, content and request information
     *
     * @param source the source where this event occurred
     * @param _notification the content of the event
     * @param requestInformation information about the request
     */
    public NotificationEvent(Object source, Notify _notification, RequestInformation requestInformation) {
        super(source);
        this._notification = _notification;
        this._requestInformation = requestInformation;
    }

    /**
     * Creates an event with source and content
     *
     * @param source the source where this event occurred
     * @param _notification the content of the event
     */
    public NotificationEvent(Object source, Notify _notification) {
        super(source);
        this._notification = _notification;
    }

    /**
     * Get the raw data of Notify-message
     */
    public Notify getRaw(){
        return _notification;
    }

    /**
     * Get the content in XML-form.
     */
    public String getXML() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLParser.writeObjectToStream(_notification, outputStream);

        byte[] bytes = outputStream.toByteArray();

        return new String(bytes);
    }

    /**
     * Extract and return the message from the notification
     * @return
     */
    public List<NotificationMessageHolderType.Message> getMessage(){
        List<NotificationMessageHolderType> messageHolderType = _notification.getNotificationMessage();

        List<NotificationMessageHolderType.Message> messages = new ArrayList<>();
        for(NotificationMessageHolderType notificationMessageHolderType : messageHolderType){
            messages.add(notificationMessageHolderType.getMessage());
        }
        return messages;
    }

    /**
     * Gets the request information, if it was available when event occurred
     * @return the {@link org.ntnunotif.wsnu.base.util.RequestInformation} belonging to this event, or <code>null</code>
     * if it is not available
     */
    public RequestInformation getRequestInformation() {
        return _requestInformation;
    }

    /**
     * The timestamp when this event occurred
     * @return timestamp of the event
     */
    public Calendar getTimestamp() {
        return _timestamp;
    }
}
