package org.cthul.org.model;

import org.cthul.org.model.task.Section;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;

/**
 *
 */
public class TaskModel {
    
    private final List<Section> sections;
    private final Map<Runnable, Integer> changeListener = new LinkedHashMap<>();
    private final AtomicBoolean invalid = new AtomicBoolean(false);
            
    public TaskModel(List<? extends Section> sections) {
        this.sections = Collections.unmodifiableList(sections);
    }
    
    public List<Section> getSections() {
        return sections;
    }

    public void addListener(Runnable onChange) {
        changeListener.compute(onChange, (r, i) -> i == null ? 1 : i+1);
    }
    
    public void removeListener(Runnable onChange) {
        changeListener.compute(onChange, (r, i) -> i == 1 ? null : i-1);
    }
    
    public void notifyReload() {
        if (!invalid.getAndSet(true)) {
            Platform.runLater(() -> {
                if (invalid.getAndSet(false)) {
                    changeListener.keySet().forEach(Runnable::run);
                }
            });
        }
    }
}
