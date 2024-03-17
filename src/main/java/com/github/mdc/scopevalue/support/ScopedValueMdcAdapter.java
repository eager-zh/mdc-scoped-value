package com.github.mdc.scopevalue.support;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;

import ch.qos.logback.classic.util.LogbackMDCAdapter;

/**
 * Custom {@link MDCAdapter} which allows to store MDC values in {@link ScopedValue}.
 * Initially, until {@link #runWhere(Runnable)} method is called, the class redirects all the invocations 
 * to an ordinary {@link LogbackMDCAdapter} instance, stored in {@link #fallbackMdcAdapter} field.
 * When @link #runWhere(Runnable)} method is called, the MDC context of this adapter 
 * is copied into the {@link ScopedValue}-bound instance of {@link MdcScopedValue}. 
 * Since then and until {@link #SCOPED_VALUE} remains bound, the changes to MDC context
 * (methods {@link #put(String, String)}, {@link #clear()}, {@link #remove(String)}, {@link #pushByKey(String, String)}, 
 * {@link #popByKey(String)}, {@link #clearDequeByKey(String)}, {@link #setContextMap(Map)}) do not propagate 
 * to the "parent" {@link #fallbackMdcAdapter} context, i.e. remain "local" to this {@link ScopedValue}.
 * 
 * <br/><br/>This is an internal class which is not supposed to be used directly by a developer.
 * Instead, a developer is supposed to use {@link ScopedValueMdc} utility class.
 * 
 * <br/><br/>This {@link MDCAdapter} has to be registered as a {@link org.slf4j.spi.SLF4JServiceProvider SLF4JServiceProvider},
 * for example, by {@link ScopedValueServiceProvider}. 
 * 
 * @see ScopedValueServiceProvider
 * @see ScopedValueMdc
 */
class ScopedValueMdcAdapter implements MDCAdapter {
	
	private static class MdcScopedValue implements MDCAdapter {
		
		private final Map<String, String> values = new HashMap<>();
		private final Map<String, Deque<String>> deques = new HashMap<>();
		
		public MdcScopedValue() {
			values.putAll(MDC.getCopyOfContextMap());
			final MDCAdapter mdcAdapter = MDC.getMDCAdapter();
			if (!(mdcAdapter instanceof ScopedValueMdcAdapter)) {
				throw new InternalError("MDCAdapter supposed to be of type " + ScopedValueMdcAdapter.class.getSimpleName() +
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
	
	private static final ScopedValue<MdcScopedValue> SCOPED_VALUE = ScopedValue.newInstance();
	
	static void runWhere(Runnable op) {
		ScopedValue.runWhere(SCOPED_VALUE, new MdcScopedValue(), op);
	}
	
	private final MDCAdapter fallbackMdcAdapter = new LogbackMDCAdapter();
	
	private final Set<String> dequeKeys = new HashSet<>();

	@Override
	public void put(String key, String val) {
		getCurrentMdcAdapter().put(key, val);
	}

	@Override
	public String get(String key) {
		return getCurrentMdcAdapter().get(key);
	}
	
	@Override
	public void remove(String key) {
		getCurrentMdcAdapter().remove(key);
	}

	@Override
	public void clear() {
		getCurrentMdcAdapter().clear();
	}

	@Override
	public Map<String, String> getCopyOfContextMap() {
		final Map<String, String> contextMap = new HashMap<>();
		getMdcAdapters().forEach(a -> {
			final Map<String, String> copyOfContextMap = a.getCopyOfContextMap();
			if (copyOfContextMap != null) {
				contextMap.putAll(copyOfContextMap);
			}
		});
		return Collections.unmodifiableMap(contextMap);
	}

	@Override
	public void setContextMap(Map<String, String> contextMap) {
		getCurrentMdcAdapter().setContextMap(contextMap);
	}

	@Override
	public void pushByKey(String key, String value) {
		dequeKeys.add(key);
		getCurrentMdcAdapter().pushByKey(key, value);
	}

	@Override
	public String popByKey(String key) {
		return getCurrentMdcAdapter().popByKey(key);
	}

	@Override
	public Deque<String> getCopyOfDequeByKey(String key) {
		return getCurrentMdcAdapter().getCopyOfDequeByKey(key);
	}

	@Override
	public void clearDequeByKey(String key) {
		getCurrentMdcAdapter().clearDequeByKey(key);
	}
	
	private MDCAdapter getCurrentMdcAdapter() {
		return SCOPED_VALUE.isBound() ? SCOPED_VALUE.get() : fallbackMdcAdapter;
	}
	
	private Stream<MDCAdapter> getMdcAdapters() {
		return SCOPED_VALUE.isBound() ? Stream.of(SCOPED_VALUE.get(), fallbackMdcAdapter) : Stream.of(fallbackMdcAdapter);
	}

}
