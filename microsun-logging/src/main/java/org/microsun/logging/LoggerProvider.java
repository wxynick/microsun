package org.microsun.logging;
import java.util.Map;

public interface LoggerProvider {
    Logger getLogger(String name);

    Object putMdc(String key, Object value);

    Object getMdc(String key);

    void removeMdc(String key);

    Map<String, Object> getMdcMap();

    void clearNdc();

    String getNdc();

    int getNdcDepth();

    String popNdc();

    String peekNdc();

    void pushNdc(String message);

    void setNdcMaxDepth(int maxDepth);
}
