package org.microsun.logging;

final class JDKLoggerProvider extends AbstractMdcLoggerProvider implements LoggerProvider {

    public Logger getLogger(final String name) {
        return new JDKLogger(name);
    }
}