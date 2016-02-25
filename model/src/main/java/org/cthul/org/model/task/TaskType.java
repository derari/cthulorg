package org.cthul.org.model.task;

import java.util.List;
import org.commonmark.node.BulletList;
import org.commonmark.node.ListBlock;
import org.cthul.org.model.io.Token;

/**
 *
 */
public class TaskType {
    
    public static final String TAG_TYPE_TASK = "task";
    public static final String TAG_TYPE_WAITING = "waiting";
    public static final String TAG_TYPE_INBOX = "inbox";
    public static final String TAG_TYPE_NOTE = "note";
    public static final String TAG_TYPE_EVENT = "event";
    
    public static final String TAG_TYPE_DONE = "done";
    public static final String TAG_TYPE_CANCELED = "canceled";
    public static final String TAG_TYPE_MOVED = "moved";

    public static TaskType detect(Entry entry, Token.Task token, List<Tag> tags) {
        for (Tag t: tags) {
            switch (t.getStateKey()) {
                case TAG_TYPE_TASK:
                    return TASK;
                case TAG_TYPE_INBOX:
                    return INBOX;
                case TAG_TYPE_WAITING:
                    return WAITING;
                case TAG_TYPE_NOTE:
                    return NOTE;
                case TAG_TYPE_EVENT:
                    return EVENT;
            }
        }
        if (token == null) {
            while (entry instanceof EntryText) {
                EntryParent ep = entry.getParent();
                entry = ep instanceof Entry ? (Entry) ep : null;
            }
            ListBlock lb = entry != null ? ((EntryGroup) entry).getListBlock() : null;
            char bullet = lb instanceof BulletList ? ((BulletList) lb).getBulletMarker() : '-';
            if (bullet == '-') {
                return NOTE;
            } else {
                return EVENT;
            }
        } else {
            switch (token.getOpenChar()) {
                case '(':
                    return WAITING;
                default:
                    return TASK;
            }
        }
    }
    
    private final boolean stateful;
    private final String name;

    public TaskType(boolean stateful, String name) {
        this.stateful = stateful;
        this.name = name;
    }

    public boolean isStateful() {
        return stateful;
    }
    
    public TaskStatus detectStatus(Token.Task token, List<Tag> tags) {
        for (Tag t: tags) {
            switch (t.getStateKey()) {
                case TAG_TYPE_DONE:
                    return TaskStatus.DONE;
                case TAG_TYPE_CANCELED:
                    return TaskStatus.CANCELED;
            }
        }
        if (!isStateful()) {
            return TaskStatus.AUTO;
        }
        if (token != null) {
            switch (token.getStatusChar()) {
                case '\0':
                case ' ':
                    return TaskStatus.OPEN;
                case '%':
                    return TaskStatus.PROGRESS;
                case '?':
                    return TaskStatus.AUTO;
                case '+':
                case 'x':
                    return TaskStatus.DONE;
                case '>':
                    return TaskStatus.MOVED;
                case '-':
                    return TaskStatus.CANCELED;
            }
        }
        return TaskStatus.AUTO;
    }

    public String getName() {
        return name;
    }
    
    public static final TaskType NOTE = new TaskType(false, "note");
    public static final TaskType EVENT = new TaskType(false, "event");
    public static final TaskType TASK = new TaskType(true, "task");
    public static final TaskType WAITING = new TaskType(true, "waiting");
    public static final TaskType INBOX = new TaskType(true, "inbox");

}
