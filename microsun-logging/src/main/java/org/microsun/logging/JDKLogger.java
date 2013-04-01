package org.microsun.logging;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class JDKLogger extends Logger {

    private static final long serialVersionUID = 2563174097983721393L;

    @SuppressWarnings({ "NonConstantLogger" })
    private transient final java.util.logging.Logger logger;

    public JDKLogger(final String name) {
        super(name);
        logger = java.util.logging.Logger.getLogger(name);
    }

    protected void doLog(final Level level, final String loggerClassName, final Object message, final Object[] parameters, final Throwable thrown) {
        if (isEnabled(level)) {
            final MicrosunLogRecord rec = new MicrosunLogRecord(translate(level), String.valueOf(message), loggerClassName);
            if (thrown != null) rec.setThrown(thrown);
            rec.setLoggerName(getName());
            rec.setParameters(parameters);
            rec.setResourceBundleName(logger.getResourceBundleName());
            rec.setResourceBundle(logger.getResourceBundle());
            logger.log(rec);
        }
    }

    protected void doLogf(final Level level, final String loggerClassName, String format, final Object[] parameters, final Throwable thrown) {
        if (isEnabled(level)) {
            final ResourceBundle resourceBundle = logger.getResourceBundle();
            if (resourceBundle != null) try {
                format = resourceBundle.getString(format);
            } catch (MissingResourceException e) {
                // ignore
            }
            final String msg = parameters == null ? String.format(format) : String.format(format, parameters);
            final MicrosunLogRecord rec = new MicrosunLogRecord(translate(level), msg, loggerClassName);
            if (thrown != null) rec.setThrown(thrown);
            rec.setLoggerName(getName());
            rec.setResourceBundleName(logger.getResourceBundleName());
            // we've done all the business
            rec.setResourceBundle(null);
            rec.setParameters(null);
            logger.log(rec);
        }
    }

    private static java.util.logging.Level translate(final Level level) {
        if (level != null) switch (level) {
            case FATAL: return JDKLevel.FATAL;
            case ERROR: return JDKLevel.ERROR;
            case WARN:  return JDKLevel.WARN;
            case INFO:  return JDKLevel.INFO;
            case DEBUG: return JDKLevel.DEBUG;
            case TRACE: return JDKLevel.TRACE;
        }
        return JDKLevel.ALL;
    }

    public boolean isEnabled(final Level level) {
        return logger.isLoggable(translate(level));
    }
}

