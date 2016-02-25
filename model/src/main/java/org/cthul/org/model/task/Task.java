package org.cthul.org.model.task;

import java.util.List;
import org.commonmark.node.Node;
import org.cthul.org.model.io.Token;

/**
 *
 */
public class Task {
    
    private final Token.Task token;
    private final List<Tag> tags;
    private int prefixTags;
    private final List<Node> content;
    private final TaskType type;
    private TaskStatus status;

    public Task(Entry entry, Token.Task token, List<Tag> tags, int prefixTags, List<Node> content) {
        this.token = token;
        this.tags = tags;
        this.prefixTags = prefixTags;
        this.content = content;
        type = TaskType.detect(entry, token, tags);
        status = type.detectStatus(token, tags);
    }

    public List<Tag> getTags() {
        return tags;
    }

    public List<Tag> getPrefixTags() {
        return tags.subList(0, prefixTags);
    }
    
    public List<Tag> getContentTags() {
        return tags.subList(prefixTags, tags.size());
    }

    public TaskType getType() {
        return type;
    }

    public TaskStatus getStatus() {
        return status;
    }
    
    public boolean hasToken() {
        return token != null;
    }
    
    public String getIconName() {
        return "00/" + type.getName() + "/" + status.getName();
    }
}
