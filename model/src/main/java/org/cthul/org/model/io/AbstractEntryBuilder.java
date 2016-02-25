package org.cthul.org.model.io;

import org.commonmark.node.ListBlock;
import org.commonmark.node.ListItem;
import org.commonmark.node.Paragraph;
import org.cthul.org.model.task.Entry;
import org.cthul.org.model.task.EntryParent;
import org.cthul.org.model.task.EntryGroup;
import org.cthul.org.model.task.EntryText;

/**
 *
 */
public abstract class AbstractEntryBuilder implements EntryBuilder {
    
    protected abstract void initialize(Entry e);
    
    private <E extends Entry> E init(E e) {
        initialize(e);
        return e;
    }
    
    protected abstract EntryParent getParent();

    @Override
    public void add(Paragraph paragraph) {
        init(new EntryText(getParent(), paragraph));
    }

    @Override
    public EntryBuilder add(ListBlock listBlock, ListItem listItem) {
        return init(new EntryGroup(getParent(), listBlock, listItem)).getBuilder();
    }
}
