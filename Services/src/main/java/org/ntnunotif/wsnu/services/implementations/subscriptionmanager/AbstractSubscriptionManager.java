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

package org.ntnunotif.wsnu.services.implementations.subscriptionmanager;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.services.eventhandling.SubscriptionChangedListener;
import org.ntnunotif.wsnu.services.eventhandling.SubscriptionEvent;
import org.ntnunotif.wsnu.services.general.WebService;
import org.oasis_open.docs.wsn.bw_2.SubscriptionManager;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class for all internal methods any implementation of a SubscriptionManager should have in this system.
 * The storage of subscribers and termination times is implementation specific and should be handled in implementing classes.
 * @author Tormod Haugland
 */
public abstract class AbstractSubscriptionManager extends WebService implements SubscriptionManager, Runnable{

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
    private ScheduledFuture<?> _task;

    /**
     * SubscriptionListeners
     * @param hub
     */
    private final ArrayList<SubscriptionChangedListener> _listeners = new ArrayList<>();

    /**
     * Default constructor
     */
    protected AbstractSubscriptionManager() {
        setScheduleInterval(60);
    }

    /**
     *
     * @param hub
     */
    protected AbstractSubscriptionManager(Hub hub) {
        super(hub);
        setScheduleInterval(60);
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

    public abstract boolean keyExists(String key);

    /**
     * Method to check if the manager has a subscription.
     * @param subscriptionReference
     * @return True if and only if the manager has the subscription currently in its system. This should return true even
     * if the manager has paused the subscription
     */
    public abstract boolean hasSubscription(String subscriptionReference);

    /**
     * Adds a subscriber. This function has overlapping functionality with the SubscriptionManager interface shell.
     * However, this function is assumed to be callable internally (particularly from a Producer).
     * @param endpointReference
     */
    public abstract void addSubscriber(String endpointReference, long subscriptionEnd);

    /**
     * Removes a subscriber. This function might seem to have overlapping functionality with the SubscriptionManager interface shell.
     * However, this function is assumed to be callable internally as well as from the WebMethod unsubscribe/renew.
     * @param endpointReference
     */
    public abstract void removeSubscriber(String endpointReference);

    /**
     * Adds a listener
     * @param listener
     */
    public void addSubscriptionChangedListener(SubscriptionChangedListener listener){
        _listeners.add(listener);
    }

    /**
     * Removes a listener
     * @param listener
     */
    public void removeSubscriptionChangedListener(SubscriptionChangedListener listener){
        _listeners.remove(listener);
    }

    /**
     * Fire subscription changed event
     * @param endpoint
     * @param type
     */
    protected void fireSubscriptionChanged(String endpoint ,SubscriptionEvent.Type type){
        SubscriptionEvent event = new SubscriptionEvent(endpoint, type);
        for (SubscriptionChangedListener listener : _listeners) {
            listener.subscriptionChanged(event);
        }
    }

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
