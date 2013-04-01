package org.microsun.logging;
import java.text.MessageFormat;

public final class Log4jLogger extends Logger {

    private static final long serialVersionUID = -5446154366955151335L;

    private final org.apache.log4j.Logger logger;

    Log4jLogger(final String name) {
        super(name);
        logger = org.apache.log4j.Logger.getLogger(name);
    }

    public boolean isEnabled(final Level level) {
        final org.apache.log4j.Level l = translate(level);
        return logger.isEnabledFor(l) && l.isGreaterOrEqual(logger.getEffectiveLevel());
    }

    protected void doLog(final Level level, final String loggerClassName, final Object message, final Object[] parameters, final Throwable thrown) {
        logger.log(loggerClassName, translate(level), parameters == null || parameters.length == 0 ? message : MessageFormat.format(String.valueOf(message), parameters), thrown);
    }

    protected void doLogf(final Level level, final String loggerClassName, final String format, final Object[] parameters, final Throwable thrown) {
        logger.log(loggerClassName, translate(level), parameters == null ? String.format(format) : String.format(format, parameters), thrown);
    }

    private static org.apache.log4j.Level translate(final Level level) {
        if (level != null) switch (level) {
            case FATAL: return org.apache.log4j.Level.FATAL;
            case ERROR: return org.apache.log4j.Level.ERROR;
            case WARN:  return org.apache.log4j.Level.WARN;
            case INFO:  return org.apache.log4j.Level.INFO;
            case DEBUG: return org.apache.log4j.Level.DEBUG;
            case TRACE: return org.apache.log4j.Level.TRACE;
        }
        return org.apache.log4j.Level.ALL;
    }
}
