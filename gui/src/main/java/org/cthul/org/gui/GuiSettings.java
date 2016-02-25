package org.cthul.org.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.cthul.log.CLogger;
import org.cthul.log.CLoggerFactory;

/**
 *
 */
public class GuiSettings implements AutoCloseable {
    
    private final CLogger log = CLoggerFactory.getClassLogger();

    private final Properties properties = new Properties(DEFAULTS);
    private final Path file;

    public GuiSettings(Path file) {
        this.file = file;
        if (Files.isReadable(file)) {
            try (BufferedReader r = Files.newBufferedReader(file)) {
                properties.load(r);
            } catch (IOException e) {
                log.warn(e, "Could not read settings from %s", file);
            }
        } else {
            log.warn("Could not read settings from %s", file);
        }
    }

    public GuiSettings(String file) throws IOException {
        this(Paths.get(file));
    }
    
    @Override
    public void close() {
        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            properties.store(w, null);
        } catch (IOException e) {
            log.warn(e, "Could not save settings to %s", file);
        }
    }
    
    public boolean useAutoOpen() {
        String v = getAutoOpenPath();
        return v != null && !v.isEmpty();
    }
    
    public String getAutoOpenPath() {
        return properties.getProperty(AUTO_OPEN);
    }
    
    public String getWindowSettings(String id) {
        return properties.getProperty("window." + id);
    }
    
    public void setWindowSettings(String id, String value) {
        properties.setProperty("window." + id, value);
    }

    private static final Properties DEFAULTS = new Properties();
    private static final String AUTO_OPEN = "autoOpen";
}
