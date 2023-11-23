package com.hsbc.events;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

import static com.hsbc.events.Event.dataEvent;
import static com.hsbc.events.TestUtils.sleep1sec;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class SubscriberTest {

	private static final Logger logger
			= LoggerFactory.getLogger(SubscriberTest.class);

	@Test
	public void processUntilShutdown() {
		final int NB_OF_EVENTS = 100;

		Subscriber subscriber = new Subscriber("Subscriber_1");
		Future future = runAsync(subscriber);

		rangeClosed(1,NB_OF_EVENTS).forEach(i -> {
			logger.info("generated event {}", i);
			subscriber.accept(generateDataEvent(i));
		});
		subscriber.accept(EventBus.shutdownEvent());

		while(!future.isDone()) { sleep1sec(); }

		// nb_of_events + 1 shutdown event
		assertEquals(NB_OF_EVENTS+1, subscriber.getProcessedEventsCount());
	}

	private Event generateDataEvent(int i) {
		return dataEvent("data message "+i);
	}
}