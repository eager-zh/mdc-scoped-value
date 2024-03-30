package com.github.mdc.scopedvalue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = MdcScopedValueApplication.class)
public class ScopedValueMdcRemoveTest extends ScopedValueMdcTestBase {
	
	@Test
	public void testRemove() throws Exception {
		MDC.put(ROOT_VALUE_NAME, ROOT_VALUE);
		MDC.remove(ROOT_VALUE_NAME);
		Assertions.assertNull(MDC.get(ROOT_VALUE_NAME));
		MDC.put(ROOT_VALUE_NAME, ROOT_VALUE);
		
		runForked( () -> {
			MDC.put(SCOPED_VALUE_NAME, SCOPED_VALUE);
			MDC.remove(SCOPED_VALUE_NAME);
			MDC.remove(ROOT_VALUE_NAME);
			Assertions.assertNull(MDC.get(SCOPED_VALUE_NAME));
			Assertions.assertNull(MDC.get(ROOT_VALUE_NAME));
		});
		
		Assertions.assertEquals(MDC.get(ROOT_VALUE_NAME), ROOT_VALUE);
	}

}