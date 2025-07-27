package com.side.springtestbed.utils.event;

import java.io.Serializable;

public abstract class Event implements Serializable {
    private static final long serialVersionUID = 279420714392857536L;
    private final String uniqueName;

    protected Event(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public String getUniqueName() {
        return this.uniqueName;
    }
}
