# mdc-scoped-value

This is a small POC to illustrate a discussion on StackOverflow question [Logback: using a ScopedValue inside a log message](https://stackoverflow.com/questions/78142173/logback-using-a-scopedvalue-inside-a-log-message).

To see in the console log a MDC-bound value, accessed by a thread, forked by `StructuredTaskScope.fork`, run Spring Boot `com.github.mdc.scopedvalue.MdcScopedValueApplication` with a JVM argument `-Dslf4j.provider=com.github.mdc.scopevalue.support.ScopedValueServiceProvider` and then run a [request to a controller](http://localhost:8080/handle) in a browser. 

Due to restrictions, imposed by a class `LogbackServiceProvider`, the solution has limited portability and maintainability. For the details, see the Javadocs of the classes in `com.github.mdc.scopevalue.support` package. 