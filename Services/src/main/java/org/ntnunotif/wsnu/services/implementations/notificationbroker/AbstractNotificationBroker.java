package org.ntnunotif.wsnu.services.implementations.notificationbroker;


import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.services.eventhandling.PublisherChangedListener;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.AbstractNotificationProducer;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.brw_2.NotificationBroker;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationManager;

import java.util.List;

/**
 * Created by tormod on 3/11/14.
 */
public abstract class AbstractNotificationBroker extends AbstractNotificationProducer implements NotificationBroker, PublisherChangedListener {

    protected boolean demandRegistered;
    protected boolean cacheMessages;

    protected PublisherRegistrationManager registrationManager;

    protected AbstractNotificationBroker() {
        super();
    }

    protected AbstractNotificationBroker(Hub hub) {
        super(hub);
    }

    public void setDemandRegistered(boolean demandRegistered) {
        this.demandRegistered = demandRegistered;
    }

    public void setCacheMessages(boolean cacheMessages) {
        this.cacheMessages = cacheMessages;
    }

    public void clearRegistrationManager(){
        registrationManager = null;
    }

    public void setRegistrationManager(PublisherRegistrationManager registrationManager) {
        this.registrationManager = registrationManager;
    }

    public static class PublisherHandle {
        public final ServiceUtilities.EndpointTerminationTuple endpointTerminationTuple;
        public final List<TopicExpressionType> registeredTopics;
        public final boolean demand;

        public PublisherHandle(ServiceUtilities.EndpointTerminationTuple endpointTerminationTuple, List<TopicExpressionType> registeredTopics, boolean demand) {
            this.endpointTerminationTuple = endpointTerminationTuple;
            this.registeredTopics = registeredTopics;
            this.demand = demand;
        }
    }

}
