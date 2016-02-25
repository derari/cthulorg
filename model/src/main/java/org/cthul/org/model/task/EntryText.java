package org.cthul.org.model.task;

import org.cthul.org.model.task.Task;
import java.util.ArrayList;
import java.util.List;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Emphasis;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.cthul.org.model.io.Token;
import org.cthul.org.model.task.Tag;
import org.cthul.org.model.task.TagCloud;

/**
 *
 */
public class EntryText extends Entry {
    
    private final List<Task> tasks = new ArrayList<>();
    
    public EntryText(EntryParent parent, Paragraph node) {
        super(parent, node);
    }

    @Override
    public void initialize() {
        getNode().accept(new TaskBuilder());
    }

    public List<Task> getTasks() {
        return tasks;
    }
    
    protected void newTask(Token.Task taskToken, List<Token.Tag> tagTokens, int prefixTags, List<Node> content) {
        TagCloud tagCloud = getParent().getTagCloud();
        List<Tag> tags = new ArrayList<>();
        tagTokens.stream()
                .map(Token.Tag::getLiteral)
                .map(tagCloud::get)
                .forEach(tags::add);
        Task task = new Task(this, taskToken, tags, prefixTags, content);
        tasks.add(task);
    }
    
    class TaskBuilder extends AbstractVisitor {
        
        private Token.Task taskToken = null;
        private List<Token.Tag> tags = new ArrayList<>();
        private int prefixTags = 0;
        private boolean textContent = false;
        private boolean inEmphasis = false;
        private List<Node> content = new ArrayList<>();
        
        protected void closeTask() {
            if (taskToken == null && tags.isEmpty() && content.isEmpty()) {
                return;
            }
            newTask(taskToken, tags, prefixTags, content);
            taskToken = null;
            tags = new ArrayList<>();
            prefixTags = 0;
            textContent = false;
            content = new ArrayList<>();
        }

        @Override
        public void visit(Paragraph paragraph) {
            super.visit(paragraph);
            closeTask();
        }

        @Override
        public void visit(Text text) {
            if (!textContent && !text.getLiteral().trim().isEmpty()) {
                textContent = true;
            }
            if (!inEmphasis) {
                content.add(text);
            }
            super.visit(text);
        }

        @Override
        public void visit(CustomNode node) {
            if (node instanceof Token.Task) {
                closeTask();
                taskToken = (Token.Task) node;
            } else if (node instanceof Token.Tag) {
                tags.add((Token.Tag) node);
                if (!textContent) prefixTags++;
            }
        }

        @Override
        public void visit(Emphasis emphasis) {
            if (inEmphasis) {
                super.visit(emphasis);
            } else {
                inEmphasis = true;
                content.add(emphasis);
                super.visit(emphasis);
                inEmphasis = false;
            }
        }

        @Override
        public void visit(StrongEmphasis emphasis) {
            if (inEmphasis) {
                super.visit(emphasis);
            } else {
                inEmphasis = true;
                content.add(emphasis);
                super.visit(emphasis);
                inEmphasis = false;
            }
        }
    }
}
