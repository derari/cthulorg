package org.cthul.org.model.report;

import java.util.List;
import org.cthul.org.model.task.Entry;
import org.cthul.org.model.task.EntryGroup;
import org.cthul.org.model.task.EntryText;

/**
 *
 */
public class Result {
    
    private final Section section;

    public Result(Section section) {
        this.section = section;
    }

    public void accept(ResultVisitor visitor) {
        visitor.visit(this, section);
    }
    
    public void visitChildren(ResultVisitor visitor, Section section) {
        section.visitChildren(this, visitor);
    }

    public void visitChildren(ResultVisitor visitor, Entry entry) {
        if (entry instanceof EntryGroup) {
            visitAll(visitor, ((EntryGroup) entry).getChildren());
        }
    }
    
    protected void visitAll(ResultVisitor visitor, List<Entry> entries) {
        entries.forEach(e -> {
            boolean v = isVisible(e);
            if (e instanceof EntryGroup) {
                visitor.visit(this, (EntryGroup) e, v);
            } else {
                visitor.visit(this, (EntryText) e, v);
            }
        });
    }
    
    public boolean isVisible(Entry entry) {
        return true;
    }
    
    public static class Section {
        
        private final int level;

        public Section(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
        
        protected void visitChildren(Result result, ResultVisitor visitor) { }
    }
    
    protected static class SuperSection extends Section {
        
        private final List<Section> sections;

        public SuperSection(List<Section> sections, int level) {
            super(level);
            this.sections = sections;
        }

        public List<Section> getSections() {
            return sections;
        }

        @Override
        protected void visitChildren(Result result, ResultVisitor visitor) {
            sections.forEach(s -> visitor.visit(result, s));
        }
    }
    
    protected static class EntrySection extends Section {
        
        private final List<Entry> entries;

        public EntrySection(List<Entry> entries, int level) {
            super(level);
            this.entries = entries;
        }

        @Override
        protected void visitChildren(Result result, ResultVisitor visitor) {
            result.visitAll(visitor, entries);
        }
    }
}
