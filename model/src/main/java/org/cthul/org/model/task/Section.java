package org.cthul.org.model.task;

import org.cthul.org.model.io.EntryBuilder;
import java.util.ArrayList;
import java.util.List;
import org.commonmark.node.Heading;
import org.cthul.org.model.TaskModel;
import org.cthul.org.model.io.AbstractEntryBuilder;
import org.cthul.org.model.task.TagCloud;

/**
 *
 */
public class Section implements EntryParent {
    
    private final Section parent;
    private final int level;
    private final Title title;
    private final List<Section> sections = new ArrayList<>();
    private final List<Entry> rootEntries = new ArrayList<>();

    protected Section(Section parent, int level) {
        this.parent = parent;
        this.level = level;
        this.title = new Title();
    }
    
    public Section getParent() {
        return parent;
    }

    public int getLevel() {
        return level;
    }

    public Title getTitle() {
        return title;
    }

    public EntryBuilder getBuilder() {
        return new AbstractEntryBuilder() {
            @Override
            protected void initialize(Entry e) {
                rootEntries.add(e);
                e.initialize();
            }
            @Override
            protected EntryParent getParent() {
                return Section.this;
            }
        };
    }
    
    public Section newSection(int level) {
        if (level <= getLevel()) {
            return getParent().newSection(level);
        }
        Section s = new Section(this, level);
        sections.add(s);
        return s;
    }

    public List<Entry> getRootEntries() {
        return rootEntries;
    }

    public List<Section> getSections() {
        return sections;
    }

    @Override
    public TaskModel getTaskModel() {
        return getParent().getTaskModel();
    }

    @Override
    public TagCloud getTagCloud() {
        return getParent().getTagCloud();
    }
    
    public static class Title {

        Heading text;
        
        public void setText(Heading text) {
            this.text = text;
        }
    }
}
