package com.hsbc.events;

import java.util.Optional;
import java.util.function.Consumer;

public interface EventBus {

    String EVENT_TYPE_SHUTDOWN = "EventBus.shutdown";

    /*
     Feel free to replace Object with something more specific,
     but be prepared to justify it
     */
    void publishEvent(Event e);

    // How would you denote the subscriber?

    /**
     * subscriber for all events
     * @param subscriber
     */
    void addSubscriber(Consumer<Event> subscriber);


    // Would you allow clients to filter the events they receive? How would the interface look like?

    /**
     * Subscribe to an eventType only and eventualy for a specific dataType
     * @param subscriber
     * @param eventType
     */
    void addSubscriberForFilteredEvents(Consumer<Event> subscriber,
                                        String eventType,
                                        Optional<String> dataType);

    public static Event shutdownEvent() {
        return new Event(EVENT_TYPE_SHUTDOWN, null, null);
    }
}
