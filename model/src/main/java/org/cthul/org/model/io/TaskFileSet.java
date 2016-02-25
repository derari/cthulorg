package org.cthul.org.model.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.cthul.org.model.task.Section;
import org.cthul.org.model.TaskModel;
import org.cthul.org.model.task.TagCloud;

/**
 *
 */
public class TaskFileSet extends Section {
    
    private final CorgConfiguration cfg;
    private final TagCloud tagCloud;
    private final String key;
    private final Path dir;
    private final PathMatcher matcher;
    private final List<TaskFile> files = new ArrayList<>();
    private final Set<Path> watchedPaths = new HashSet<>();
    private final List<WatchKey> watchKeys = new ArrayList<>();

    public TaskFileSet(CorgConfiguration cfg, TagCloud tagCloud, String key, Path dir, PathMatcher matcher) {
        super(null, 0);
        this.cfg = cfg;
        this.tagCloud = tagCloud;
        this.key = key;
        this.dir = dir.toAbsolutePath();
        this.matcher = matcher;
    }

    public void initialize() throws IOException {
        if (!files.isEmpty()) throw new IllegalStateException();
        load();
        new RefreshJob().schedule();
    }
    
    protected void watchPath(Path p) throws IOException {
        if (watchedPaths.add(p)) {
            WatchKey watchKey = p.register(cfg.getWatchService(), 
                    StandardWatchEventKinds.ENTRY_CREATE, 
                    StandardWatchEventKinds.ENTRY_DELETE, 
                    StandardWatchEventKinds.ENTRY_MODIFY);
            watchKeys.add(watchKey);
        }
    }
    
    public void reload() throws IOException {
        getRootEntries().clear();
        getSections().clear();
        files.clear();
        load();
        getTaskModel().notifyReload();
    }
    
    public void load() throws IOException {
        if (!files.isEmpty()) throw new IllegalStateException();
        List<Path> paths = Files.walk(dir).filter(p -> Files.isRegularFile(p)
                && matcher.matches(dir.relativize(p)) && !isArchiveFile(p))
                .collect(Collectors.toList());
        for (Path p: paths) {
            Path uri = dir.relativize(p);
            TaskFile f = new TaskFile(this, uri);
            f.initialize();
            files.add(f);
        }
        List<Path> dirs = Files.walk(dir).filter(p -> Files.isDirectory(p))
                        .collect(Collectors.toList());
        for (Path d: dirs) {
            watchPath(d);
        }
    }
    
    private boolean isArchiveFile(Path p) {
        return false;
    }

    public Path toAbsolutePath(Path uri) {
        return dir.resolve(uri);
    }

    public CorgConfiguration getConfiguration() {
        return cfg;
    }

    @Override
    public TaskModel getTaskModel() {
        return cfg.getModel();
    }

    @Override
    public TagCloud getTagCloud() {
        return tagCloud;
    }
    
    
    class RefreshJob implements Runnable {
        
        int minDelay = 0;
        int maxDelay = 0;
        boolean reload = false;
        
        public void schedule() {
            int len = maxDelay > 1 ? 3 : 1;
            JobManager.schedule(this, 1, TimeUnit.SECONDS);
        }
        
        @Override
        public void run() {
            try {
                for (WatchKey watchKey: watchKeys) {
                    Path dir = (Path) watchKey.watchable();
                    dir = TaskFileSet.this.dir.relativize(dir);
                    for (WatchEvent<?> evt: watchKey.pollEvents()) {
                        Path p = (Path) evt.context();
                        p = dir.resolve(p);
                        if (matcher.matches(p)) {
                            reload = true;
                            minDelay = 1;
                            break;
                        }
                    }
                }
                if (reload && (minDelay == 0 || maxDelay == 0)) {
                    reload();
                    maxDelay = 4;
                }
                if (minDelay > 0) minDelay--;
                if (maxDelay > 0) maxDelay--;
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            schedule();
        }
    }
}
