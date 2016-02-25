package org.cthul.org.model.report;

import org.cthul.org.model.task.EntryGroup;
import org.cthul.org.model.task.EntryText;

/**
 *
 */
public class AbstractResultVisitor implements ResultVisitor {

    @Override
    public void visit(Result result, Result.Section section) {
        result.visitChildren(this, section);
    }

    @Override
    public void visit(Result result, EntryGroup group, boolean visible) {
        if (visible) {
            visit(result, group);
        }
    }

    @Override
    public void visit(Result result, EntryText text, boolean visible) {
        if (visible) {
            visit(result, text);
        }
    }

    protected void visit(Result result, EntryGroup group) {
        result.visitChildren(this, group);
    }

    protected void visit(Result result, EntryText text) {
        result.visitChildren(this, text);
    }
}
