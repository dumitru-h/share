package com.hsbc.events;

import lombok.Data;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Optional;

import static com.hsbc.events.EventBus.EVENT_TYPE_SHUTDOWN;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

@Data
public class Event {
	public static String EVENT_TYPE_DATA = "data";

	public static String DATA_TYPE_STRING = "string";
	public static String DATA_TYPE_BINARY = "binary";

	private final Optional<byte[]> data;
	private final String eventType;
	private final Optional<String> dataType;

	public Event(@NonNull String type, byte[] data, String dataType) {
		this.eventType = type;
		if (EVENT_TYPE_DATA.equals(type) && data ==null)
			throw new IllegalArgumentException("Event of type data must have non-null data");

		this.data = Optional.ofNullable(data).map(dta -> Arrays.copyOf(dta, dta.length));
		this.dataType = Optional.ofNullable(dataType);
	}

	public static Event dataEvent(byte[] data, @NonNull String dataType) {
		return new Event(EVENT_TYPE_DATA, data, dataType);
	}

	public static Event dataEvent(String data) {
		return new Event(EVENT_TYPE_DATA, data.getBytes(), DATA_TYPE_STRING);
	}

	public Event copy() {
		return new Event(this.eventType, this.data.orElse(null), this.dataType.orElse(null));
	}

	@Override
	public String toString() {
		if (dataType.isPresent()){
			if(DATA_TYPE_STRING.equals(dataType.orElse(null)))
				return data.map(bytes -> new String(bytes)).orElse("null");
		}

		if (EVENT_TYPE_SHUTDOWN.equals(eventType))
			return EVENT_TYPE_SHUTDOWN;

		return "Event{" +
				"type='" + eventType + "\', " +
				(data.isPresent() ? "data="+ data.map(d-> "[binary_data]") : "") +
				((dataType.isPresent()) ? ", dataType=" + dataType.orElse("null"):"") +
				'}';
	}

	public String typeKey() {
		return mkTypeKey(eventType, dataType);
	}

	public static String mkTypeKey(String eventType, Optional<String> dataType) {
		return asList(eventType, dataType.orElse(null)).stream()
				.filter(str -> str!=null)
				.collect(joining("-"));
	}
}
