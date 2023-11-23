package com.hsbc.events;

public interface Throttler {

    // check if we can proceed (poll)
    ThrottleResult shouldProceed();

    // subscribe to be told when we can proceed (Push)
    void notifyWhenCanProceed(EventBus bus);

    enum ThrottleResult {
        PROCEED , // publish, aggregate etc
        DO_NOT_PROCEED //
    }
}
