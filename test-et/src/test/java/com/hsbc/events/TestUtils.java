package com.hsbc.events;

import java.util.concurrent.TimeUnit;

public interface TestUtils {

	static void sleep1sec() {
		try {
			TimeUnit.SECONDS.sleep(1);
			System.out.println(".");
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
