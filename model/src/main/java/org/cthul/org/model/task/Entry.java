package org.cthul.org.model.task;

import org.commonmark.node.Node;

/**
 *
 */
public class Entry {
    
    private final EntryParent parent;
    private final Entry root;
    private final Node node;

    @SuppressWarnings("LeakingThisInConstructor")
    public Entry(EntryParent parent, Node node) {
        this.parent = parent;
        this.node = node;
        if (parent instanceof Entry) {
            root = ((Entry) parent).getRoot();
        } else {
            root = this;
        }
    }
    
    public void initialize() { }

    public EntryParent getParent() {
        return parent;
    }

    public Entry getRoot() {
        return root;
    }

    public Node getNode() {
        return node;
    }
}
