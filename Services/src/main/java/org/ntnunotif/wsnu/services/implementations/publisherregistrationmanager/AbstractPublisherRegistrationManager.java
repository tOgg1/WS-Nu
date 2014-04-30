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
import java.util.concurrent.TimeUnit;

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

    /**
     * Constructor using the default schedule interval of 60 seconds
     */
    protected AbstractPublisherRegistrationManager(){
        super();
        _scheduleInterval = 60;
        setScheduleInterval(_scheduleInterval);
    }

    /**
     * Constructor taking in a hub as an argument
     * @param hub
     */
    protected AbstractPublisherRegistrationManager(Hub hub) {
        super(hub);
        _scheduleInterval = 60;
        setScheduleInterval(_scheduleInterval);
    }

    /**
     * Constructor taking in the schedule interval as an argument.
     */
    protected AbstractPublisherRegistrationManager(int scheduleInterval){
        super();
        _scheduleInterval = scheduleInterval;
        setScheduleInterval(scheduleInterval);
    }

    /**
     * Constructor taking both a hub and the scheduleinterval as arguments.
     */
    protected AbstractPublisherRegistrationManager(Hub hub, int scheduleInterval){
        super(hub);
        _scheduleInterval = scheduleInterval;
        setScheduleInterval(scheduleInterval);
    }

    /**
     * Sets the interval of scheduling.
     * @param seconds
     */
    public void setScheduleInterval(long seconds){
        _scheduleInterval = seconds;
        resetScheduler();
    }

    /**
     * Resets the scheduler
     */
    private void resetScheduler()
    {
        if(_task != null){
            _task.cancel(false);
        }
        _task = _scheduler.scheduleAtFixedRate(this, 0, this._scheduleInterval, TimeUnit.SECONDS);
    }

    public abstract void addPublisher(String endpointReference, long subscriptionEnd);

    public abstract void removePublisher(String endpointReference);

    public void firePublisherRegistrationChanged(String reference, PublisherRegistrationEvent.Type type){
        PublisherRegistrationEvent event = new PublisherRegistrationEvent(reference, type);
        for (PublisherChangedListener listener : _listeners) {
            listener.publisherChanged(event);
        }
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
