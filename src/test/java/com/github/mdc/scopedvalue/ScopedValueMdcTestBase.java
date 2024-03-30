package com.github.mdc.scopedvalue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mdc.scopevalue.support.ScopedValueMdc;

public abstract class ScopedValueMdcTestBase {

	protected static final Logger log = LoggerFactory.getLogger(ScopedValueMdcTestBase.class);
	protected static final String SCOPED_VALUE_NAME = "scopedKey";
	protected static final String SCOPED_VALUE = "scopedValue";
	protected static final String ROOT_VALUE = "rootValue";
	protected static final String ROOT_VALUE_NAME = "rootKey";

	protected void runForked(Runnable action) {
		ScopedValueMdc.runWhere( () -> {
			try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
				scope.fork( () -> {
					action.run();
					return null;
				});
				scope.join().throwIfFailed();
	 		} catch (InterruptedException  e) {
	 			Assertions.fail("Unexpected interuption");
			} catch (ExecutionException e) {
				final Throwable cause = e.getCause();
				log.error(null, cause);
				if (cause instanceof AssertionFailedError) {
	     			Assertions.fail("Assertion failed");
				} else {
	     			Assertions.fail("Unexpeted exception");
				}
			} 
	    });
	}

	protected static void assertMapsEqual(Map<String, String> actual, String ... expected) {
		if (!mapsEqual(actual, expected)) {
			Assertions.fail("Maps not equal");
		}
	}

	private static boolean mapsEqual(Map<String, String> actual, String ... expected) {
		final Map<String, String> expectedMap = new HashMap<>();
		for (int i = 0; i < expected.length; i += 2) {
			expectedMap.put(expected[i], expected[i + 1]);
			if (!actual.get(expected[i]).equals(expected[i + 1])) {
				return false;
			}
		}
		return actual.size() == expectedMap.size();
	}
	
	protected static Map<String, String> toMap(String ... expected) {
		final Map<String, String> expectedMap = new HashMap<>();
		for (int i = 0; i < expected.length; i += 2) {
			expectedMap.put(expected[i], expected[i + 1]);
		}
		return expectedMap;
	}

}
