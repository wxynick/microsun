package org.microsun.logging;


import java.security.AccessController;
import java.security.PrivilegedAction;

final class LoggerProviders {
    static final String LOGGING_PROVIDER_KEY = "org.microsun.logging.provider";

    static final LoggerProvider PROVIDER = find();

    private static LoggerProvider find() {
        final LoggerProvider result = findProvider();
        // Log a debug message indicating which logger we are using
        result.getLogger("org.microsun.logging").debugf("Logging Provider: %s", result.getClass().getName());
        return result;
    }

    private static LoggerProvider findProvider() {
        // Since the impl classes refer to the back-end frameworks directly, if this classloader can't find the target
        // log classes, then it doesn't really matter if they're possibly available from the TCCL because we won't be
        // able to find it anyway
        final ClassLoader cl = LoggerProviders.class.getClassLoader();
        try {
            // Check the system property
            final String loggerProvider = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(LOGGING_PROVIDER_KEY);
                }
            });
            if (loggerProvider != null) {
               if ("jdk".equalsIgnoreCase(loggerProvider)) {
                    return tryJDK();
                } else if ("log4j".equalsIgnoreCase(loggerProvider)) {
                    return tryLog4j(cl);
                } else if ("slf4j".equalsIgnoreCase(loggerProvider)) {
                    return trySlf4j();
                }
            }
        } catch (Throwable t) {
        }
        try {
            return tryLog4j(cl);
        } catch (Throwable t) {
            // nope...
        }
        try {
            // only use slf4j if Logback is in use
            Class.forName("ch.qos.logback.classic.Logger", false, cl);
            return trySlf4j();
        } catch (Throwable t) {
            // nope...
        }
        return tryJDK();
    }

    private static JDKLoggerProvider tryJDK() {
        return new JDKLoggerProvider();
    }

    private static LoggerProvider trySlf4j() {
        return new Slf4jLoggerProvider();
    }

    private static LoggerProvider tryLog4j(final ClassLoader cl) throws ClassNotFoundException {
        Class.forName("org.apache.log4j.LogManager", true, cl);
        // JBLOGGING-65 - slf4j can disguise itself as log4j.  Test for a class that slf4j doesn't provide.
        Class.forName("org.apache.log4j.Hierarchy", true, cl);
        return new Log4jLoggerProvider();
    }



    private LoggerProviders() {
    }
}
