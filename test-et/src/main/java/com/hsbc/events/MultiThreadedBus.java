package com.hsbc.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import static java.util.Collections.emptySet;


public class MultiThreadedBus implements EventBus, Runnable {

	private static Logger logger = LoggerFactory.getLogger(MultiThreadedBus.class);

	private static int MAX_QUEUE_EVENTS = 1000;

	private Set<Consumer<Event>> allEventSubscribers = new CopyOnWriteArraySet<>();

	private Map<String, Set<Consumer<Event>>> filteredEventSubscribers = new ConcurrentHashMap<>();

	private BlockingQueue<Event> events = new ArrayBlockingQueue<>(MAX_QUEUE_EVENTS);
	private volatile boolean stopRequest =false;

	private void startAsync(Runnable r) {
		new Thread(r).start();
	}

	public void setStopRequest(boolean stopRequest) {
		this.stopRequest = stopRequest;
	}

	boolean allEventsDispatched() {
		return events.isEmpty();
	}

	@Override
	public void publishEvent(Event event) {
		if(!stopRequest)
			try {
				logger.info("publishEvent({})", event);
				events.put(event);
			} catch (InterruptedException x) {
				stopRequest = true;
				logger.info("thread interrupted on put event. event buffer contains "+events.size()+" will stop processing", x);
			}
	}

	@Override
	public void addSubscriber(Consumer<Event> subscriber) {
		allEventSubscribers.add(subscriber);

		logger.info("added ( {} )", subscriber);
	}

	@Override
	public void addSubscriberForFilteredEvents(Consumer<Event> subscriber, String eventType, Optional<String> dataType) {
		final String typeKey = Event.mkTypeKey(eventType, dataType);

		if (!allEventSubscribers.contains(subscriber)) {
			filteredEventSubscribers
					.computeIfAbsent(typeKey, key -> new CopyOnWriteArraySet<>())
					.add(subscriber);
		}
	}

	public void shutdown() {
		setStopRequest(true);
		allEventSubscribers.forEach(subscriber -> subscriber.accept(EventBus.shutdownEvent()));
	}

	@Override
	public void run() {
		logger.info("{} START", this.getClass().getSimpleName());

		while(!stopRequest) {
			try {
				logger.info("waiting for event ... ");
				Event event = events.take();
				logger.info("received {}", event);

				allEventSubscribers.forEach(consumer ->
					startAsync(() -> consumer.accept(event))
				);
				logger.info("allEventSubscribers dispatched");

				filteredEventSubscribers.getOrDefault(event.typeKey(), emptySet())
						.forEach(subscriber -> {
							if (!allEventSubscribers.contains(subscriber)) {
								startAsync(() -> subscriber.accept(event));
							}
						});

				logger.info("filteredEventSubscribers dispatched");
			} catch (InterruptedException e) {
				logger.error("received interrupt. Will stop processing", e);
				stopRequest=true;
			}
		}

		logger.info("{} STOPPED", this.getClass().getSimpleName());
	}
}
