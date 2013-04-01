package org.microsun.logging;
import java.util.Map;

import org.apache.log4j.NDC;
import org.apache.log4j.MDC;

final class Log4jLoggerProvider implements LoggerProvider {

    public Logger getLogger(final String name) {
        return new Log4jLogger(name);
    }

    public Object getMdc(String key) {
        return MDC.get(key);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMdcMap() {
        return MDC.getContext();
    }

    public Object putMdc(String key, Object val) {
        try {
            return MDC.get(key);
        } finally {
            MDC.put(key, val);
        }
    }

    public void removeMdc(String key) {
        MDC.remove(key);
    }

    public void clearNdc() {
        NDC.clear();
    }

    public String getNdc() {
        return NDC.get();
    }

    public int getNdcDepth() {
        return NDC.getDepth();
    }

    public String peekNdc() {
        return NDC.peek();
    }

    public String popNdc() {
        return NDC.pop();
    }

    public void pushNdc(String message) {
        NDC.push(message);
    }

    public void setNdcMaxDepth(int maxDepth) {
        NDC.setMaxDepth(maxDepth);
    }
}