package org.microsun.logging;
import java.text.MessageFormat;

public final class Slf4jLogger extends Logger {

    private static final long serialVersionUID = 8685757928087758380L;

    private final org.slf4j.Logger logger;

    Slf4jLogger(final String name, final org.slf4j.Logger logger) {
        super(name);
        this.logger = logger;
    }

    public boolean isEnabled(final Level level) {
        if (level != null) switch (level) {
            case FATAL: return logger.isErrorEnabled();
            case ERROR: return logger.isErrorEnabled();
            case WARN:  return logger.isWarnEnabled();
            case INFO:  return logger.isInfoEnabled();
            case DEBUG: return logger.isDebugEnabled();
            case TRACE: return logger.isTraceEnabled();
        }
        return true;
    }

    protected void doLog(final Level level, final String loggerClassName, final Object message, final Object[] parameters, final Throwable thrown) {
        if (isEnabled(level)) try {
            final String text = parameters == null || parameters.length == 0 ? String.valueOf(message) : MessageFormat.format(String.valueOf(message), parameters);
            switch (level) {
                case FATAL:
                case ERROR:
                    logger.error(text, thrown);
                    return;
                case WARN:
                    logger.warn(text, thrown);
                    return;
                case INFO:
                    logger.info(text, thrown);
                    return;
                case DEBUG:
                    logger.debug(text, thrown);
                    return;
                case TRACE:
                    logger.trace(text, thrown);
                    return;
            }
        } catch (Throwable ignored) {}
    }

    protected void doLogf(final Level level, final String loggerClassName, final String format, final Object[] parameters, final Throwable thrown) {
        if (isEnabled(level)) try {
            final String text = parameters == null ? String.format(format) : String.format(format, parameters);
            switch (level) {
                case FATAL:
                case ERROR:
                    logger.error(text, thrown);
                    return;
                case WARN:
                    logger.warn(text, thrown);
                    return;
                case INFO:
                    logger.info(text, thrown);
                    return;
                case DEBUG:
                    logger.debug(text, thrown);
                    return;
                case TRACE:
                    logger.trace(text, thrown);
                    return;
            }
        } catch (Throwable ignored) {}
    }
}
