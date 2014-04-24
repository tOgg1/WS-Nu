package org.ntnunotif.wsnu.services.implementations.publisherregistrationmanager;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.services.eventhandling.PublisherChangedListener;
import org.ntnunotif.wsnu.services.eventhandling.PublisherRegistrationEvent;
import org.ntnunotif.wsnu.services.general.WebService;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationManager;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by tormod on 23.04.14.
 */
public abstract class AbstractPublisherRegistrationManager extends WebService implements PublisherRegistrationManager, Runnable {

    /**
     * Scheduleinterval
     */
    private long _scheduleInterval;

    /**
     * Scheduler
     */
    private final ScheduledExecutorService _scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Task to be executed by the scheduler (used for internal updating)
     */
    private ScheduledFuture<?> _task;

    /**
     * Listeners listening for a changed
     */
    private ArrayList<PublisherChangedListener> _listeners = new ArrayList<>();

    protected AbstractPublisherRegistrationManager(){}

    protected AbstractPublisherRegistrationManager(Hub hub) {
        super(hub);
    }

    public abstract void addPublisher(String endpointReference, long subscriptionEnd);

    public abstract void removePublisher(String endpointReference);

    public void firePublisherRegistrationChanged(String reference, PublisherRegistrationEvent.Type type){

    }

    /**
     * The function that is supposed to check for expired subscriptions. This is implementation specific, depending on, amongst other things, if persistent storage is used or not.
     */
    public abstract void update();

    @Override
    public void run() {
        this.update();
    }
}