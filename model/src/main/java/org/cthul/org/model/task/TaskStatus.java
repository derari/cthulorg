package org.cthul.org.model.task;

/**
 *
 */
public enum TaskStatus {
    
    OPEN,
    PROGRESS,
    AUTO,
    DONE,
    MOVED,
    CANCELED;
    
    public String getName() {
        return toString().toLowerCase();
    }
    
    public boolean isClosed() {
        return ordinal() < AUTO.ordinal();
    }
}
