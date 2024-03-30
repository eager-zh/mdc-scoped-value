package com.github.mdc.scopedvalue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = MdcScopedValueApplication.class)
public class ScopedValueMdcClearTest extends ScopedValueMdcTestBase {
	
	@Test
	public void testClear() throws Exception {
	
		MDC.put(ROOT_VALUE_NAME, ROOT_VALUE);
		MDC.clear();
		Assertions.assertNull(MDC.get(ROOT_VALUE_NAME));
		MDC.put(ROOT_VALUE_NAME, ROOT_VALUE);
		
		runForked( () -> {
			MDC.put(SCOPED_VALUE_NAME, SCOPED_VALUE);
			MDC.clear();
			Assertions.assertNull(MDC.get(SCOPED_VALUE_NAME));
			Assertions.assertNull(MDC.get(ROOT_VALUE_NAME));
		});
		
		Assertions.assertEquals(ROOT_VALUE, MDC.get(ROOT_VALUE_NAME));
	}

}