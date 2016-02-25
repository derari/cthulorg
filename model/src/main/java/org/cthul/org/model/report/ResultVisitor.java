package org.cthul.org.model.report;

import org.cthul.org.model.task.EntryGroup;
import org.cthul.org.model.task.EntryText;

/**
 *
 */
public interface ResultVisitor {
    
    void visit(Result result, Result.Section section);
    
    void visit(Result result, EntryGroup group, boolean visible);
    
    void visit(Result result, EntryText text, boolean visible);
    
}
