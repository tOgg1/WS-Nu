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

package org.ntnunotif.wsnu.services.implementations.notificationconsumer;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEventSupport;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import static org.ntnunotif.wsnu.base.util.InternalMessage.STATUS_HAS_MESSAGE;
import static org.ntnunotif.wsnu.base.util.InternalMessage.STATUS_OK;

/**
 * The SimpleConsumer Web Service as defined per the Oasis WS-N Base specification
 * Created by tormod on 3/11/14.
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "SimpleConsumer")
public class NotificationConsumerImpl extends org.ntnunotif.wsnu.services.general.WebService implements org.oasis_open.docs.wsn.bw_2.NotificationConsumer{

    /**
     * Helper that deals with Notification events
     */
    private final NotificationEventSupport _eventSupport = new NotificationEventSupport(this);


    public NotificationConsumerImpl() {
        super();
    }

    /**
     * Constructor that takes in hub as an argument
     */
    public NotificationConsumerImpl(Hub hub) {
        super(hub);
        this.hub = hub;
    }


    @Override
    @WebMethod(operationName = "Notify")
    public void notify(@WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
                           Notify notify) {
        _eventSupport.fireNotificationEvent(notify, connection.getRequestInformation());
    }

    @WebMethod(exclude = true)
    public void addConsumerListener(ConsumerListener listener){
        _eventSupport.addNotificationListener(listener);
    }

    @WebMethod(exclude = true)
    public void removeConsumerListener(ConsumerListener listener){
        _eventSupport.removeNotificationListener(listener);
    }

    @WebMethod(exclude = true)
    public InternalMessage sendSubscriptionRequest(Subscribe subscriptionRequest, String address){
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, subscriptionRequest);
        message.getRequestInformation().setEndpointReference(address);
        return hub.acceptLocalMessage(message);
    }
}
