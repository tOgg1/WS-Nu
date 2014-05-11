package org.ntnunotif.wsnu.services.eventhandling;
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
 * This class contains a Notify object received by NotificationConsumer.
 *
 * This is the main object passed into the {@link org.ntnunotif.wsnu.services.eventhandling.ConsumerListener#notify(NotificationEvent)}
 * method.
 *
 * This class also contains a timestamp of when the event occured. As well as any request information. E.g. the ip of the producer sending
 * the notification.
 */
public final class NotificationEvent extends EventObject {

    private Notify _notify;
    private RequestInformation _requestInformation;
    private Calendar _timestamp = Calendar.getInstance();

    /**
     * Creates an event with source, content and request information
     *
     * @param source the source where this event occurred
     * @param notify the content of the event
     * @param requestInformation information about the request
     */
    public NotificationEvent(Object source, Notify notify, RequestInformation requestInformation) {
        super(source);
        _notify = notify;
        _requestInformation = requestInformation;
    }

    /**
     * Creates an event with source and content
     *
     * @param source the source where this event occurred
     * @param notify the content of the event
     */
    public NotificationEvent(Object source, Notify notify) {
        super(source);
        _notify = notify;
    }

    /**
     * Get the raw data of Notify-message.
     *
     * @return The actual Notify object. Not to be confused with uses of the word raw in the WS-N specification.
     */
    public Notify getRaw(){
        return _notify;
    }

    /**
     * Get the content in XML-form. The notify will be attempted unmarshalled by the XMLParser.
     *
     * @return A string representing the notify in XML-form.
     */
    public String getXML() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLParser.writeObjectToStream(_notify, outputStream);

        byte[] bytes = outputStream.toByteArray();

        return new String(bytes);
    }

    /**
     * Extract and return the message from the notification
     *
     * @return A list of {@link org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType} objects.
     */
    public List<NotificationMessageHolderType.Message> getMessage(){
        List<NotificationMessageHolderType> messageHolderType = _notify.getNotificationMessage();

        List<NotificationMessageHolderType.Message> messages = new ArrayList<>();
        for(NotificationMessageHolderType notificationMessageHolderType : messageHolderType){
            messages.add(notificationMessageHolderType.getMessage());
        }
        return messages;
    }

    /**
     * Gets the request information, if it was available when event occurred
     *
     * @return the {@link org.ntnunotif.wsnu.base.util.RequestInformation} belonging to this event, or <code>null</code>
     * if it is not available
     */
    public RequestInformation getRequestInformation() {
        return _requestInformation;
    }

    /**
     * The timestamp when this event occurred
     *
     * @return timestamp of the event
     */
    public Calendar getTimestamp() {
        return _timestamp;
    }
}
