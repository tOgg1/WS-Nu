package org.ntnunotif.wsnu.services.implementations.subscriptionmanager;

import org.oasis_open.docs.wsn.bw_2.PausableSubscriptionManager;

/**
 * Created by tormod on 23.04.14.
 */
public abstract class AbstractPausableSubscriptionManager extends AbstractSubscriptionManager implements PausableSubscriptionManager{

    /**
     * Abstract method checking if a subscriptionIsPaused.
     * @return True if and only if the subscription exists, and it is currently paused.
     */
    public abstract boolean subscriptionIsPaused(String subscriptionReference);
}
