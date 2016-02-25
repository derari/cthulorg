package org.cthul.org.model.task;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TagCloud {
    
    private final TagCloud parent;
    private final Map<Character, Map<String, Tag>> tags = new HashMap<>();
    
    protected TagCloud(boolean empty) {
        if (!empty) throw new IllegalArgumentException("Internal");
        this.parent = null;
    }

    public TagCloud() {
        this.parent = ROOT;
    }

    public TagCloud(TagCloud parent) {
        this.parent = parent;
    }
    
    protected Tag peek(char key, String name) {
        Map<String, Tag> m = tags.get(key);
        if (m != null) {
            Tag t = m.get(name);
            if (t != null) return t;
        }
        if (parent != null) {
            return parent.peek(key, name);
        }
        return null;
    }
    
    public Tag get(String definition) {
        definition = definition.trim();
        if (definition.length() < 2) {
            throw new IllegalArgumentException();
        }
        char key = definition.charAt(0);
        boolean create = definition.charAt(1) == ':';
        if (create && definition.length() < 3) {
            throw new IllegalArgumentException();
        }
        String fullName = definition.substring(create ? 2 : 1);
        return get(key, create, fullName);
    }
    
    protected Map<String, Tag> map(char key) {
        return tags.computeIfAbsent(key, k -> new HashMap<>());
    }
    
    public Tag get(char key, boolean create, String fullName) {
        return map(key).computeIfAbsent(fullName, name -> {
            TagCloud cloud = this;
            Tag parentTag = null;
            if (parent != null) {
                parentTag = parent.peek(key, name);
                if (parentTag != null) return parentTag;
            }
            int slash = fullName.lastIndexOf('/');
            if (slash > 0) {
                parentTag = get(key, create, name.substring(0, slash));
                name = name.substring(slash+1);
                if (parentTag != null) {
                    cloud = parentTag.getTagCloud();
                }
            }
            if (cloud == ROOT || cloud == this) {
                return new Tag(this, key, parentTag, name);
//            } else if (cloud == ROOT) {
//                return parent.get(key, create, fullName);
            } else {
                return cloud.get(key, create, fullName);
            }
        });
    }
    
    public void alias(char key, String name, String alias) {
        map(key).put(alias, map(key).get(name));
    }
    
    static final TagCloud ROOT;
    static {
        ROOT = new TagCloud(true);
        ROOT.get('!', true, "!").alias("important");
        for (int i = 1; i < 9; i++) {
            ROOT.get('!', true, String.valueOf(i));
        }
        ROOT.get('!', true, "chat").alias("talk");
        ROOT.get('!', true, "phone").alias("call", "telephone");
        ROOT.get('!', true, "mail").alias("letter");
        ROOT.get('!', true, "email");
        ROOT.get('!', true, "www").alias("web", "internet");
        
        ROOT.get('!', true, TaskType.TAG_TYPE_TASK).asStateKey().alias("todo");
        ROOT.get('!', true, TaskType.TAG_TYPE_WAITING).asStateKey();
        ROOT.get('!', true, TaskType.TAG_TYPE_INBOX).asStateKey().alias("in");
        ROOT.get('!', true, TaskType.TAG_TYPE_NOTE).asStateKey();
        ROOT.get('!', true, TaskType.TAG_TYPE_EVENT).asStateKey();
        
        ROOT.get('!', true, TaskType.TAG_TYPE_DONE).asStateKey().alias("ok", "+", "x");
        ROOT.get('!', true, TaskType.TAG_TYPE_CANCELED).asStateKey().alias("cancel", "cancelled", "-", "delete", "del");
        ROOT.get('!', true, TaskType.TAG_TYPE_MOVED).asStateKey().alias(">");
    }
}
