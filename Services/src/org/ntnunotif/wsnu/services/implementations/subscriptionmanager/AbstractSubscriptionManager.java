package org.ntnunotif.wsnu.services.implementations.subscriptionmanager;


import org.ntnunotif.wsnu.services.general.SubscriptionManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class for all internal methods any implementation of a SubscriptionManager should have in this system.
 * The storage of subscribers and termination times is implementation specific and should be handled in implementing classes.
 * @author Tormod Haugland
 */
public abstract class AbstractSubscriptionManager implements SubscriptionManager, Runnable{

    /**
     * The scheduleinterval
     */
    private long _scheduleInterval;

    /**
     * The scheduler variable.
     */
    private final ScheduledExecutorService _scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Reference to the scheduled task.
     */
    private ScheduledFuture<?> _future;

    /**
     * Sets the interval of scheduling.
     * @param seconds
     */
    public void setScheduleInterval(long seconds){
        _scheduleInterval = seconds;
    }

    /**
     * Resets the scheduler
     */
    private void resetScheduler()
    {
        _future.cancel(false);
        _future = _scheduler.scheduleAtFixedRate(this, 0, this._scheduleInterval, TimeUnit.SECONDS);
    }

    /**
     * Adds a subscriber. This function has overlapping functionality with the SubscriptionManager interface shell.
     * However, this function is assumed to be callable internally as well as from the WebMethod unsubscribe/renew.
     * @param endpointReference
     */
    public abstract void addSubscriber(String endpointReference, long subscriptionEnd);

    /**
     * Removes a subscriber. This function has overlapping functionality with the SubscriptionManager interface shell.
     * However, this function is assumed to be callable internally as well as from the WebMethod unsubscribe/renew.
     * @param endpointReference
     */
    public abstract void removeSubscriber(String endpointReference);

    /**
     * The function that is supposed to check for expired subscriptions. This is implementation specific, depending on, amongst other things, if persistent storage is used or not.
     */
    public abstract void update();

    /**
     * The implementation of Runnable's run. This only calls the only relevant function, update.
     */
    @Override
    public void run(){
        this.update();
    }
}
