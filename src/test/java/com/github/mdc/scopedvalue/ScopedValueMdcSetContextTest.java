package com.github.mdc.scopedvalue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = MdcScopedValueApplication.class)
public class ScopedValueMdcSetContextTest extends ScopedValueMdcTestBase {

	@Test
	public void testMdcPutAndGet() throws Exception {
		MDC.setContextMap(toMap(ROOT_VALUE_NAME, ROOT_VALUE));
		Assertions.assertEquals(ROOT_VALUE, MDC.get(ROOT_VALUE_NAME));
		
		runForked( () -> {
			Assertions.assertEquals(ROOT_VALUE, MDC.get(ROOT_VALUE_NAME));
			MDC.setContextMap(toMap(ROOT_VALUE_NAME, ROOT_VALUE, SCOPED_VALUE_NAME, SCOPED_VALUE));
			Assertions.assertEquals(ROOT_VALUE, MDC.get(ROOT_VALUE_NAME));
			Assertions.assertEquals(SCOPED_VALUE, MDC.get(SCOPED_VALUE_NAME)); 
		});
		
		Assertions.assertEquals(ROOT_VALUE, MDC.get(ROOT_VALUE_NAME));
		Assertions.assertNull(MDC.get(SCOPED_VALUE_NAME));
	}

}