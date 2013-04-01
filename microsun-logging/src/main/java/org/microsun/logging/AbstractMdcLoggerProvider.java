package org.microsun.logging;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractMdcLoggerProvider extends AbstractLoggerProvider {

    private final ThreadLocal<Map<String, Object>> mdcMap = new ThreadLocal<Map<String, Object>>();

    public Object getMdc(String key) {
        return mdcMap.get() == null ? null : mdcMap.get().get(key);
    }

    public Map<String, Object> getMdcMap() {
        return mdcMap.get();
    }

    public Object putMdc(String key, Object value) {
        Map<String, Object> map = mdcMap.get();
        if (map == null) {
            map = new HashMap<String, Object>();
            mdcMap.set(map);
        }
        return map.put(key, value);
    }

    public void removeMdc(String key) {
        Map<String, Object> map = mdcMap.get();
        if (map == null)
            return;
        map.remove(key);
    }
}