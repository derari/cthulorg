package org.cthul.org.model.task;

/**
 *
 */
public class Tag {

    private final TagCloud cloud;
    private final char key;
    private final Tag parent;
    private final String name;
    private String imageId = null;
    private String stateKey = null;

    public Tag(TagCloud cloud, char key, Tag parent, String name) {
        this.cloud = cloud;
        this.key = key;
        this.parent = parent;
        this.name = name;
    }

    public TagCloud getTagCloud() {
        return cloud;
    }

    public char getKey() {
        return key;
    }

    protected StringBuilder buildFullName(StringBuilder sb) {
        if (parent != null) {
            parent.buildFullName(sb).append('/');
        }
        sb.append(getName());
        return sb;
    }
    
    public String getFullName() {
        return buildFullName(new StringBuilder()).toString();
    }
    
    public String getName() {
        return name;
    }

    public Tag getParent() {
        return parent;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageId() {
        return imageId;
    }
    
    public String getIconName() {
        String i = getImageId();
        if (i != null) return i;
        if (parent == null) {
            return key + "/" + getName();
        } else {
            return parent.getIconName() + "/" + getName();
        }
    }
    
    public boolean isHidden() {
        return stateKey != null;
    }

    public String getStateKey() {
        if (stateKey == null) return "";
        return stateKey;
    }
    
    public Tag alias(String... names) {
        for (String n: names) {
            cloud.alias(key, getFullName(), n);
        }
        return this;
    }
    
    public Tag asStateKey() {
        return stateKey(getName());
    }
    
    public Tag stateKey(String stateKey) {
        this.stateKey = stateKey;
        return this;
    }
}
