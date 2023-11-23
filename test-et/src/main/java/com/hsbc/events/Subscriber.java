package com.hsbc.events;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static com.hsbc.events.EventBus.EVENT_TYPE_SHUTDOWN;

@Slf4j
public class Subscriber implements Consumer<Event>, Runnable {
	public static final int MAX_QUE_SIZE = 1000;

	private static final Logger logger
			= LoggerFactory.getLogger(Subscriber.class);

	private BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>(MAX_QUE_SIZE);
	private final String id;
	private volatile boolean stopped = false;

	public Subscriber(String id) {
		this.id = id;
	}

	public long getProcessedEventsCount() {
		return processedEventsCount.get();
	}

	private AtomicLong processedEventsCount = new AtomicLong(0);

	public void setStopRequest(boolean stopRequest) {
		this.stopRequest = stopRequest;
	}

	private volatile boolean stopRequest = false;

	public void accept(Event event) {
		if (!stopRequest) {
			try {
				eventQueue.put(event.copy());
			} catch (InterruptedException x) {
				stopRequest = true;
				log.info("thread interrupted on put. Subscriber[" + id + "] will stop processing", x);
			}
		}
	}

	@Override
	public void run() {
		logger.info("Subscriber[" + id + "] START");

		while (!stopRequest) {
			if (!eventQueue.isEmpty())
				processEvents();

			Thread.yield();
		}
		stopped = true;
		logger.info("Subscriber[" + id + "] STOPPED");
	}

	boolean isStopped() {
		return stopped;
	}

	@Override
	public java.lang.String toString() {
		return id;
	}

	private void processEvents() {
		try {
			Event newEvent = eventQueue.poll();
			if (EVENT_TYPE_SHUTDOWN.equals(newEvent.getEventType())) {
				log.info("Subscriber[" + id + "] received shutdown event. Will stop processing after {} events. {} remain unprocessed", processedEventsCount.get(), eventQueue.size());
				stopRequest = true;
			} else {
				log.info("Subscriber[" + id + "] processed {}", newEvent);
			}
			processedEventsCount.incrementAndGet();
		} catch (Throwable throwable) {
			log.error("Failed processEvents", throwable);
		}
	}
}
