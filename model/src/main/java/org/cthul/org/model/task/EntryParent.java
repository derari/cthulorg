package org.cthul.org.model.task;

import org.cthul.org.model.TaskModel;
import org.cthul.org.model.task.TagCloud;

/**
 *
 */
public interface EntryParent {
    
    TaskModel getTaskModel();
    
    TagCloud getTagCloud();
}
