package org.cthul.org.model.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import org.cthul.log.CLogger;
import org.cthul.log.CLoggerFactory;
import org.cthul.org.model.TaskModel;
import org.cthul.org.model.task.TagCloud;

/**
 *
 */
public class CorgConfiguration implements AutoCloseable {
    
    private static final CLogger LOG = CLoggerFactory.getClassLogger();

    private final Properties properties;
    private final Path file;
    private final String prefix;
    private final TaskModel model;
    private final List<TaskFileSet> fileSets;
    private final Map<String, TagCloud> tagClouds = new HashMap<>();
    private final WatchService watchService;

    public CorgConfiguration(Path file) throws IOException {
        this(file, new Properties(DEFAULTS));
    }

    public CorgConfiguration(String file) throws IOException {
        this(Paths.get(file));
    }

    public CorgConfiguration(CorgConfiguration parent, Path file, String prefix) {
        this.watchService = parent.watchService;
        this.fileSets = parent.fileSets;
        this.model = parent.model;
        this.properties = new Properties();
        this.file = file;
        this.prefix = prefix;
        throw new UnsupportedOperationException("tag clouds, ...");
    }

    /* test */ CorgConfiguration(Path file, Properties properties) throws IOException {
        this.properties = properties;
        this.fileSets = new ArrayList<>();
        this.model = new TaskModel(fileSets);
        this.prefix = "";
        this.file = file;
        if (file != null && Files.isReadable(file)) {
            try (BufferedReader r = Files.newBufferedReader(file)) {
                properties.load(r);
            } catch (IOException e) {
                LOG.warn(e, "Could not read settings from %s", file);
            }
        } else {
            LOG.warn("Could not read settings from %s", file);
        }
        this.watchService = file == null
                ? FileSystems.getDefault().newWatchService()
                : file.getFileSystem().newWatchService();
    }
    
    @Override
    public void close() {
        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            properties.store(w, null);
        } catch (IOException e) {
            LOG.warn(e, "Could not save settings to %s", file);
        }
    }

    public TaskModel getModel() {
        return model;
    }

    public WatchService getWatchService() {
        return watchService;
    }
    
    public void initialize() throws IOException {
        collectFileSets(fileSets::add);
        for (TaskFileSet fs: fileSets) {
            fs.initialize();
        }
    }
    
    protected TagCloud getTagCloud(String key) {
        return tagClouds.computeIfAbsent(key, k -> {
            int dot = k.lastIndexOf('.');
            TagCloud tc;
            if (dot < 0) {
                tc = new TagCloud();
            } else {
                TagCloud parent = getTagCloud(k.substring(0, dot));
                tc = new TagCloud(parent);
            }
            initTagCloud(tc, k);
            return tc;
        });
    }
    
    protected void initTagCloud(TagCloud tagCloud, String key) {
        
    }
    
    protected void collectFileSets(Consumer<TaskFileSet> bag) {
        Path dir = file.getParent() != null ? file.getParent() : Paths.get(".");
        properties.stringPropertyNames().stream()
                    .filter(this::isFileSetKey).forEach(str -> {
            String key = str.substring("files.".length());
            String path = properties.getProperty(str);
            PathMatcher pm = dir.getFileSystem().getPathMatcher("glob:" + path);
            bag.accept(new TaskFileSet(this, getTagCloud(key), key, dir, pm));
        });
    }
    
    private boolean isFileSetKey(String str) {
        if (!str.startsWith("files.")) return false;
        int d = str.lastIndexOf('.');
        return !FS_PROPERTY_KEYS.contains(str.substring(d+1));
    }

    private static final Properties DEFAULTS = new Properties();
    private static final Set<String> FS_PROPERTY_KEYS = new HashSet<>(asList("archive"));
}
