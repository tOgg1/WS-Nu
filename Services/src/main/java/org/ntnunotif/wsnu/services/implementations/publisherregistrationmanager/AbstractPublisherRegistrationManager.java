package org.ntnunotif.wsnu.services.implementations.publisherregistrationmanager;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.services.eventhandling.SubscriptionChangedListener;
import org.ntnunotif.wsnu.services.general.WebService;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationManager;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by tormod on 23.04.14.
 */
public abstract class AbstractPublisherRegistrationManager extends WebService implements PublisherRegistrationManager {
    /**
     * Scheduleinterval
     */
    private long _scheduleInterval;

    private final ScheduledExecutorService _scheduler = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> _task;

    private ArrayList<SubscriptionChangedListener> _listeners = new ArrayList<>();

    protected AbstractPublisherRegistrationManager(){

    }

    protected AbstractPublisherRegistrationManager(Hub hub) {
        super(hub);
    }

    public abstract void addPublisher(String endpointReference, long subscriptionEnd);

    public abstract void removePublisher(String endpointReference);


}
