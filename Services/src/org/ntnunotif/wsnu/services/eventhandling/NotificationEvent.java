package org.ntnunotif.wsnu.services.eventhandling;

import com.google.common.io.ByteStreams;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import sun.misc.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tormod on 3/13/14.
 */
//TODO: What more methods do we need?
public class NotificationEvent {

    private Notify _notification;

    public NotificationEvent(Notify _notification) {
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
    //TODO: Perhaps do something more here?
    public List<NotificationMessageHolderType.Message> getMessage(){
        List<NotificationMessageHolderType> messageHolderType = _notification.getNotificationMessage();

        List<NotificationMessageHolderType.Message> messages = new ArrayList<>();
        for(NotificationMessageHolderType notificationMessageHolderType : messageHolderType){
            messages.add(notificationMessageHolderType.getMessage());
        }
        return messages;
    }
}
