package com.hsbc.events;

import lombok.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class SingleThreadedBus implements EventBus {

	private Set<Consumer<Event>> allEventSubscribers = new CopyOnWriteArraySet<>();

	private Map<String, Set<Consumer<Event>>> filteredEventSubscribers = new ConcurrentHashMap<>();

	@Override
	public void publishEvent(Event event) {
		allEventSubscribers.forEach(subscriber -> subscriber.accept(event));

		filteredEventSubscribers.get(event.typeKey()).forEach(subscriber -> {
			if (!allEventSubscribers.contains(subscriber)) {
				subscriber.accept(event);
			}
		});
	}

	@Override
	public void addSubscriber(Consumer<Event> subscriber) {
		allEventSubscribers.add(subscriber);
	}

	/**
	 *
	 * @param subscriber
	 * @param eventType
	 * @param dataType
	 */
	@Override
	public void addSubscriberForFilteredEvents(@NonNull Consumer<Event> subscriber,
											   @NonNull String eventType,
											   Optional<String> dataType
											   ) {
		final String typeKey = Event.mkTypeKey(eventType, dataType);

		if (!allEventSubscribers.contains(subscriber)) {
			filteredEventSubscribers
					.computeIfAbsent(typeKey, key -> new CopyOnWriteArraySet<>())
					.add(subscriber);
		}
	}

	public void shutdown() {
		allEventSubscribers.forEach(subscriber -> subscriber.accept(EventBus.shutdownEvent()));
	}

}
