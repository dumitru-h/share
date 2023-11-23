package com.hsbc.events;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hsbc.events.Event.dataEvent;
import static com.hsbc.events.TestUtils.sleep1sec;
import static java.util.Arrays.asList;
import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultiThreadedBusTest {


	private static final Logger logger
			= LoggerFactory.getLogger(MultiThreadedBusTest.class);

	static final int NB_OF_EVENTS = 100;

	static final MultiThreadedBus bus = new MultiThreadedBus();
	static final Subscriber[] subscribers = {
			new Subscriber("Subscriber_" + 1),
			new Subscriber("Subscriber_" + 2),
			new Subscriber("Subscriber_" + 3),
			new Subscriber("Subscriber_" + 4)
	};

	private static void startAsync(Runnable r) {
		new Thread(r).start();
	}

	@BeforeAll
	static void beforeAll() {
		logger.info("beforeAll");

		logger.info("starting bus...");
		startAsync(bus);

		logger.info("starting subscribers ...");
		asList(subscribers)
				.forEach(s -> {
					startAsync(s);
					bus.addSubscriber(s);
				});
	}

	@Test
	void handleAllEventsUntilShutdown() {
		logger.info("start publishing events ...");

		rangeClosed(1, NB_OF_EVENTS)
				.forEach(i -> bus.publishEvent(generateDataEvent(i))
				);

		while (!bus.allEventsDispatched()) {
			sleep1sec();
		}
		bus.shutdown();

		// wait all finished processing
		asList(subscribers).forEach(s -> {
					while (!s.isStopped()) {
						sleep1sec();
					}
				}
		);

		asList(subscribers).forEach(s ->
				assertEquals(s.getProcessedEventsCount(), NB_OF_EVENTS + 1)
		);
	}

	private Event generateDataEvent(int i) {
		return dataEvent("data message " + i);
	}
}
