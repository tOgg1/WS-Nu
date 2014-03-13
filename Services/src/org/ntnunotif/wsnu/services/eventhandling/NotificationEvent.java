package org.ntnunotif.wsnu.services.eventhandling;

import org.oasis_open.docs.wsn.b_2.Notify;

/**
 * Created by tormod on 3/13/14.
 */
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
     * Get the content in XML-form
     */
    public String getXML(){
        return "";
    }

    /**
     * Get a specific datafield of the Notify Message
     */
    public String getDataField(){
        return null;
    }

}
