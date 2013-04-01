package org.microsun.logging;

import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;
import org.slf4j.MDC;

final class Slf4jLoggerProvider extends AbstractLoggerProvider implements LoggerProvider {

    public Logger getLogger(final String name) {
        org.slf4j.Logger l = LoggerFactory.getLogger(name);
        try {
            return new Slf4jLocationAwareLogger(name, (LocationAwareLogger) l);
        } catch (Throwable ignored) {}
        return new Slf4jLogger(name, l);
    }

    public Object putMdc(final String key, final Object value) {
        try {
            return MDC.get(key);
        } finally {
            if (value == null) {
                MDC.remove(key);
            } else {
                MDC.put(key, String.valueOf(value));
            }
        }
    }

    public Object getMdc(final String key) {
        return MDC.get(key);
    }

    public void removeMdc(final String key) {
        MDC.remove(key);
    }

    @SuppressWarnings({ "unchecked" })
    public Map<String, Object> getMdcMap() {
        return MDC.getCopyOfContextMap();
    }
}

