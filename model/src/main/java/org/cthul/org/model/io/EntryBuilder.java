package org.cthul.org.model.io;

import org.commonmark.node.ListBlock;
import org.commonmark.node.ListItem;
import org.commonmark.node.Paragraph;

public interface EntryBuilder {

    void add(Paragraph paragraph);

    EntryBuilder add(ListBlock listBlock, ListItem listItem);
}
