package com.github.mdc.scopevalue.support;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LogbackServiceProvider;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.util.StatusPrinter;

/** 
 * An extension of {@link LogbackServiceProvider}. 
 * It registers a custom implementation of {@link MDCAdapter} - {@link ScopedValueMdcAdapter}.
 * This {@link org.slf4j.spi.SLF4JServiceProvider SLF4JServiceProvider} has to be defined in 
 * system property {@code slf4j.provider}, for example, 
 * in command line with {@code -Dslf4j.provider=com.github.mdc.scopevalue.support.ScopedValueServiceProvider} JVM argument.
 * 
 * <br/><br/>Due to restrictions, imposed by superclass {@link LogbackServiceProvider}, 
 * most of the class' methods copy-pasted from there. 
 * This, in turn, limits the portability and maintainability of the class.   
 */
public class ScopedValueServiceProvider extends LogbackServiceProvider {

	private final MDCAdapter mdcAdapter = new ScopedValueMdcAdapter();

	/**
	 * Adopted from {@link LogbackServiceProvider}
	 */
	private final LoggerContext defaultLoggerContext = new LoggerContext();

	/**
	 * Adopted from {@link LogbackServiceProvider}
	 */
	private final BasicMarkerFactory markerFactory = new BasicMarkerFactory();

	@Override
	public MDCAdapter getMDCAdapter() {
		return mdcAdapter;
	}
	
	@Override
    public ILoggerFactory getLoggerFactory() {
        return defaultLoggerContext;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }
	
	/**
	 * Adopted from {@link LogbackServiceProvider}
	 */
    @Override
    public void initialize() {
        defaultLoggerContext.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
        initializeLoggerContext();
        defaultLoggerContext.start();
         // set the MDCAdapter for the defaultLoggerContext immediately
        defaultLoggerContext.setMDCAdapter(mdcAdapter);
    }
    
	/**
	 * Adopted from {@link LogbackServiceProvider}
	 */
   private void initializeLoggerContext() {
        try {
            try {
                new ContextInitializer(defaultLoggerContext).autoConfig();
            } catch (JoranException je) {
            	report("Failed to auto configure default logger context", je);
            }
            // LOGBACK-292
            if (!StatusUtil.contextHasStatusListener(defaultLoggerContext)) {
                StatusPrinter.printInCaseOfErrorsOrWarnings(defaultLoggerContext);
            }
            // contextSelectorBinder.init(defaultLoggerContext, KEY);

        } catch (Exception t) { // see LOGBACK-1159
            report("Failed to instantiate [" + LoggerContext.class.getName() + "]", t);
        }
    }

	/**
	 * Adopted from {@link LogbackServiceProvider}
	 */
	private void report(String msg, Exception t) {
      System.err.println(msg);
      System.err.println("Reported exception:");
      t.printStackTrace();
	}

}
