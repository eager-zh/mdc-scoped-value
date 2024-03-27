package com.github.mdc.scopevalue.support;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;

import ch.qos.logback.classic.util.LogbackMDCAdapter;

/**
 * An implementation of {@link MDCAdapter} which allows to store and access MDC context by subtasks, 
 * created by {@link java.util.concurrent.StructuredTaskScope#fork(java.util.concurrent.Callable) StructuredTaskScope.fork}
 * , or, in general, by a thread, started by {@link java.lang.ScopedValue.Carrier#run(Runnable) ScopedValue.Carrier.run} method.
 * Initially, until {@link #runWhere(Runnable)} method is called, the class redirects all the invocations 
 * to an instance of {@link MDCAdapter}, stored in {@link #rootContext} field.
 * When {@link #runWhere(Runnable)} method is called, the <i>root</i>MDC context of this adapter 
 * is copied into the {@link ScopedValue}-bound instance of {@link SubtaskContext}. 
 * Since then and until {@link #SUBTASK_CONTEXT} remains bound, the changes to MDC context
 * (methods {@link #put(String, String)}, {@link #clear()}, {@link #remove(String)}, {@link #pushByKey(String, String)}, 
 * {@link #popByKey(String)}, {@link #clearDequeByKey(String)}, {@link #setContextMap(Map)}) do not propagate 
 * to the "parent" {@link #rootContext} context, i.e. remain "local" to this {@link ScopedValue}.
 * 
 * <br/><br/>This is an internal class and it is not supposed to be used directly by a developer.
 * Instead, a developer is supposed to use {@link ScopedValueMdc} utility class.
 * 
 * <br/><br/>This {@link MDCAdapter} has to be registered by a {@link org.slf4j.spi.SLF4JServiceProvider SLF4JServiceProvider},
 * for example, by {@link ScopedValueServiceProvider}. 
 * 
 * @see java.util.concurrent.StructuredTaskScope StructuredTaskScope
 * @see java.lang.ScopedValue ScopedValue
 * @see java.lang.ScopedValue.Carrier ScopedValue.Carrier
 * @see ScopedValueServiceProvider
 * @see ScopedValueMdc
 */
class ScopedValueMdcAdapter implements MDCAdapter {
	
	/**
	 * An implementation of {@link MDCAdapter} which stores MDC context 
	 * in fields {@link #values} and {@link #deques}.
	 * Upon constructing it copies the MDC context from current {@link MDCAdapter},
	 * which is expected to be of type {@link ScopedValueMdcAdapter}.
	 */
	private static class SubtaskContext implements MDCAdapter {
		
		private final Map<String, String> values = new HashMap<>();
		private final Map<String, Deque<String>> deques = new HashMap<>();
		
		public SubtaskContext() {
			copyFromRoot();
		}

		private void copyFromRoot() throws InternalError {
			values.putAll(MDC.getCopyOfContextMap());
			final MDCAdapter mdcAdapter = MDC.getMDCAdapter();
			if (!(mdcAdapter instanceof ScopedValueMdcAdapter)) {
				throw new IllegalStateException("MDC Adapter supposed to be of type " + ScopedValueMdcAdapter.class.getSimpleName() +
						", actual type is " + (mdcAdapter != null ? mdcAdapter.getClass().getSimpleName() : "<null>"));
			}
			final ScopedValueMdcAdapter scopedValueMdcAdapter = (ScopedValueMdcAdapter) mdcAdapter;
			scopedValueMdcAdapter.dequeKeys.stream().forEach( (key) -> {
				final Deque<String> deque = scopedValueMdcAdapter.getCopyOfDequeByKey(key);
				if (deque != null) {
					deques.put(key, deque);
				}
			});
		}

		public void put(String key, String val) {
			values.put(key, val);
		}

		public String get(String key) {
			return values.get(key);
		}

		@Override
		public void remove(String key) {
			values.remove(key);
		}

		@Override
		public void clear() {
			values.clear();
		}

		@Override
		public Map<String, String> getCopyOfContextMap() {
			return Collections.unmodifiableMap(values);
		}

		@Override
		public void setContextMap(Map<String, String> contextMap) {
			values.clear();
			values.putAll(contextMap);
		}

		@Override
		public void pushByKey(String key, String value) {
			getDequeByKey(key).push(value);
		}

		@Override
		public String popByKey(String key) {
			return getDequeByKey(key).pollFirst();
		}

		@Override
		public Deque<String> getCopyOfDequeByKey(String key) {
			return new LinkedList<>(getDequeByKey(key));
		}

		@Override
		public void clearDequeByKey(String key) {
			getDequeByKey(key).clear();
		}
		
		private Deque<String> getDequeByKey(String key) {
			return deques.putIfAbsent(key, new LinkedList<>());
		}
	}
	
	/**
	 * Stores {@link SubtaskContext} MDC context bound to a current thread
	 * by {@link ScopedValue#runWhere(ScopedValue, Object, Runnable)} method.
	 */
	private static final ScopedValue<SubtaskContext> SUBTASK_CONTEXT = ScopedValue.newInstance();
	
	/**
	 * Run an operation operation {@code op} bound to {@link SUBTASK_CONTEXT} value.
	 * It is a convenient wrapper over @link ScopedValue#runWhere(ScopedValue, Object, Runnable)} method.
	 */
	static void runWhere(Runnable op) {
		ScopedValue.runWhere(SUBTASK_CONTEXT, new SubtaskContext(), op);
	}
	
	/**
	 * Root {@link MDCAdapter}. It is used when {@link #SUBTASK_CONTEXT} is not bound to current thread 
	 */
	private MDCAdapter rootContext = new LogbackMDCAdapter();
	
	/** {@link Deque} keys stored by {@link #pushByKey(String, String)} method. 
	 * They will later be used to retrieve context values saved in {@link Deque}s
	 */
	private final Set<String> dequeKeys = new HashSet<>();

	@Override
	public void put(String key, String val) {
		getCurrentContext().put(key, val);
	}

	@Override
	public String get(String key) {
		return getCurrentContext().get(key);
	}
	
	@Override
	public void remove(String key) {
		getCurrentContext().remove(key);
	}

	@Override
	public void clear() {
		getCurrentContext().clear();
	}

	@Override
	public Map<String, String> getCopyOfContextMap() {
		final Map<String, String> contextMap = new HashMap<>();
		final Map<String, String> copyOfContextMap = getCurrentContext().getCopyOfContextMap();
		if (copyOfContextMap != null) {
			contextMap.putAll(copyOfContextMap);
		}
		return Collections.unmodifiableMap(contextMap);
	}

	@Override
	public void setContextMap(Map<String, String> contextMap) {
		getCurrentContext().setContextMap(contextMap);
	}

	@Override
	public void pushByKey(String key, String value) {
		dequeKeys.add(key);
		getCurrentContext().pushByKey(key, value);
	}

	@Override
	public String popByKey(String key) {
		return getCurrentContext().popByKey(key);
	}

	@Override
	public Deque<String> getCopyOfDequeByKey(String key) {
		return getCurrentContext().getCopyOfDequeByKey(key);
	}

	@Override
	public void clearDequeByKey(String key) {
		getCurrentContext().clearDequeByKey(key);
	}
	
	private MDCAdapter getCurrentContext() {
		return SUBTASK_CONTEXT.isBound() ? SUBTASK_CONTEXT.get() : rootContext;
	}

	public MDCAdapter getRootContext() {
		return rootContext;
	}

	public void setRootContext(MDCAdapter rootContext) {
		this.rootContext = rootContext;
	}
	
}
