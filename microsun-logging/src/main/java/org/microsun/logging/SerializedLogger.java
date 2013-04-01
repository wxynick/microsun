package org.microsun.logging;
import java.io.Serializable;

final class SerializedLogger implements Serializable {

    private static final long serialVersionUID = 508779982439435831L;

    private final String name;

    SerializedLogger(final String name) {
        this.name = name;
    }

    protected Object readResolve() {
        return Logger.getLogger(name);
    }
}
