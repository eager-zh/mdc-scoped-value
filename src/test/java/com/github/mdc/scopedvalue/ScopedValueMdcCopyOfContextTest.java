package com.github.mdc.scopedvalue;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = MdcScopedValueApplication.class)
public class ScopedValueMdcCopyOfContextTest extends ScopedValueMdcTestBase {

	@Test
	public void testMdcPutAndGet() throws Exception {
		MDC.put(ROOT_VALUE_NAME, ROOT_VALUE);
		assertMapsEqual(MDC.getCopyOfContextMap(), ROOT_VALUE_NAME, ROOT_VALUE);
		
		runForked( () -> {
			assertMapsEqual(MDC.getCopyOfContextMap(), ROOT_VALUE_NAME, ROOT_VALUE);
			MDC.put(SCOPED_VALUE_NAME, SCOPED_VALUE);
			assertMapsEqual(MDC.getCopyOfContextMap(), ROOT_VALUE_NAME, ROOT_VALUE, SCOPED_VALUE_NAME, SCOPED_VALUE);
		});
		
		assertMapsEqual(MDC.getCopyOfContextMap(), ROOT_VALUE_NAME, ROOT_VALUE);
	}

}