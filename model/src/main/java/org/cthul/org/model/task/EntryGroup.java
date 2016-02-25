package org.cthul.org.model.task;

import java.util.ArrayList;
import java.util.List;
import org.commonmark.node.ListBlock;
import org.commonmark.node.ListItem;
import org.cthul.org.model.TaskModel;
import org.cthul.org.model.io.AbstractEntryBuilder;
import org.cthul.org.model.io.EntryBuilder;
import org.cthul.org.model.task.TagCloud;

/**
 *
 */
public class EntryGroup extends Entry implements EntryParent {

    private final ListBlock list;
    private final List<Entry> children = new ArrayList<>();

    public EntryGroup(EntryParent parent, ListBlock list, ListItem node) {
        super(parent, node);
        this.list = list;
    }

    @Override
    public TaskModel getTaskModel() {
        return getParent().getTaskModel();
    }

    @Override
    public TagCloud getTagCloud() {
        return getParent().getTagCloud();
    }

    public List<Entry> getChildren() {
        return children;
    }

    public ListBlock getListBlock() {
        return list;
    }

    public EntryBuilder getBuilder() {
        return new AbstractEntryBuilder() {
            @Override
            protected void initialize(Entry e) {
                children.add(e);
                e.initialize();
            }
            @Override
            protected EntryParent getParent() {
                return EntryGroup.this;
            }
        };
    }
}
