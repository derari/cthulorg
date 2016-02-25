package org.cthul.org.model.cal;

import java.time.LocalDate;

/**
 *
 */
public abstract class TaskTemporal {
    
    public abstract LocalDate getFirstDay();
    
    public abstract LocalDate getLastDay();
}
