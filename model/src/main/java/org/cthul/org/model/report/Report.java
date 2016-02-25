package org.cthul.org.model.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.cthul.org.model.TaskModel;
import org.cthul.org.model.task.Entry;
import org.cthul.org.model.task.Section;

/**
 *
 */
public class Report {
    
    private final TaskModel taskModel;

    public Report(TaskModel taskModel) {
        this.taskModel = taskModel;
    }
    
    public void addListener(Runnable onChange) {
        taskModel.addListener(onChange);
    }

    public void removeListener(Runnable onChange) {
        taskModel.removeListener(onChange);
    }
    
    public void accept(ResultVisitor visitor) {
        buildResult().accept(visitor);
    }
    
    protected Result buildResult() {
        List<Result.Section> resultData = collectData(taskModel.getSections(), 2);
        return new Result(new Result.SuperSection(resultData, 1));
    }
    
    protected List<Result.Section> collectData(List<Section> content, int level) {
        List<Result.Section> resultData = new ArrayList<>();
        content.forEach(s -> resultData.add(collectData(s, level)));
        return resultData;
    }

    private Result.Section collectData(Section s, int level) {
        List<Result.Section> resultData = null;
        if (!s.getSections().isEmpty()) {
            resultData = collectData(s.getSections(), level+1);
        }
        if (resultData != null) {
            resultData.add(collectEntries(s.getRootEntries(), level+1));
            return new Result.SuperSection(resultData, level);
        } else if (s.getRootEntries().isEmpty()) {
            return new Result.SuperSection(Collections.emptyList(), level);
        } else {
            return collectEntries(s.getRootEntries(), level);
        }
    }

    private Result.EntrySection collectEntries(List<Entry> rootEntries, int i) {
        return new Result.EntrySection(rootEntries, i);
    }
}
