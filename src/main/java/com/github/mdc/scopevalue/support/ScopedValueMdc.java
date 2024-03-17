package com.github.mdc.scopevalue.support;

/**
 * Utility that allows to run a task with special MDC {@link java.lang.ScopedValue ScopedValue} defined.
 * A typical usage looks like the following:
 * {@snippet lang=java :
 * 		MDC.put("key1", value1);
 * 		ScopedValueMdc.runWhere( () -> {
 * 			try (var scope = new StructuredTaskScope<>()) {
 * 				MDC.put("key2", value2);
 * 				scope.fork(() -> {
 *     					logger.info("1: ");
 *     				});
 * 			}
 * 		}
 * }
 * 
 * In the snippet above MDC context value of the key {@code key1} 
 * is known during the execution of a thread, forked by {@code scope.fork()}.
 * Similarly, the MDC context value of {@code key2}, defined during this execution, has a local nature 
 * in a sense that when this forked thread finishes, it becomes unknown in the MDC context. 
 * In addition to this policy, the concrete implementation of {@link org.slf4j.spi.MDCAdapter MDCAdapter}, 
 * {@link ScopedValueMdcAdapter}, defines other policies of interacting and overwriting of the MDC context values,
 * defined in a parent and forked threads. Please see {@link ScopedValueMdcAdapter} for details. 
 * 
 * @see ScopedValueMdcAdapter
 */
public abstract class ScopedValueMdc {
	
	private ScopedValueMdc() {
	}
	
	public static void runWhere(Runnable op) {
		ScopedValueMdcAdapter.runWhere(op);
	}

}
