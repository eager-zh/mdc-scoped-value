package com.github.mdc.scopedvalue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = MdcScopedValueApplication.class)
public class ScopedValueMdcPushPopTest extends ScopedValueMdcTestBase {
	
	@Test
	public void testMdcPushPopByKey() throws Exception {
	
		MDC.pushByKey(ROOT_VALUE_NAME, ROOT_VALUE);
		Assertions.assertEquals(ROOT_VALUE, MDC.popByKey(ROOT_VALUE_NAME));
		
		runForked( () -> {
			MDC.pushByKey(SCOPED_VALUE_NAME, SCOPED_VALUE);
			Assertions.assertEquals(SCOPED_VALUE, MDC.popByKey(SCOPED_VALUE_NAME));       			
			MDC.pushByKey(SCOPED_VALUE_NAME, SCOPED_VALUE);
		});
		
		Assertions.assertNull(MDC.get(SCOPED_VALUE_NAME));
	} 

}