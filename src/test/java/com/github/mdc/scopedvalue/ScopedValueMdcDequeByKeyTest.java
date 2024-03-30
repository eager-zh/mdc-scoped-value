package com.github.mdc.scopedvalue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = MdcScopedValueApplication.class)
public class ScopedValueMdcDequeByKeyTest extends ScopedValueMdcTestBase {
	
	@Test
	public void testGetCopyOfDequeByKey() throws Exception {
		final MDCAdapter mdcAdapter = MDC.getMDCAdapter();
	
		MDC.pushByKey(ROOT_VALUE_NAME, ROOT_VALUE);
		Assertions.assertArrayEquals(mdcAdapter.getCopyOfDequeByKey(ROOT_VALUE_NAME).toArray(), new Object[] {ROOT_VALUE});
//		MDC.pushByKey(ROOT_VALUE_NAME, SCOPED_VALUE);
//		Assertions.assertArrayEquals(mdcAdapter.getCopyOfDequeByKey(ROOT_VALUE_NAME).toArray(), new Object[] {SCOPED_VALUE, ROOT_VALUE});
		
		runForked( () -> {
			Assertions.assertArrayEquals(mdcAdapter.getCopyOfDequeByKey(ROOT_VALUE_NAME).toArray(), new Object[] {ROOT_VALUE});
			MDC.pushByKey(ROOT_VALUE_NAME, SCOPED_VALUE);
			Assertions.assertArrayEquals(mdcAdapter.getCopyOfDequeByKey(ROOT_VALUE_NAME).toArray(), new Object[] {SCOPED_VALUE, ROOT_VALUE});
		});
		
		Assertions.assertArrayEquals(mdcAdapter.getCopyOfDequeByKey(ROOT_VALUE_NAME).toArray(), new Object[] {ROOT_VALUE});
		
	}

}