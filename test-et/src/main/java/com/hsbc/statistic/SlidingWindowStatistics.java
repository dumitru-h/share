package com.hsbc.statistic;

import com.hsbc.events.Event;

import java.util.function.Consumer;

public interface SlidingWindowStatistics {
	void add(int measurement);

	// subscriber will have a callback that'll deliver a Statistics instance (push)
	void subscribeForStatistics(Consumer<Event> eventConsumer, String eventType);

	// get latest statistics (poll)
	Statistics getLatestStatistics();

	interface Statistics {
		float getMean();
		int getMode(); // ??
		int getPctile(int pctile);
	}
}
