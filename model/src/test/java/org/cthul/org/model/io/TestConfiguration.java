package org.cthul.org.model.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

/**
 *
 */
public class TestConfiguration extends CorgConfiguration {

    public TestConfiguration(Path file, Properties properties) throws IOException {
        super(file, properties);
    }

    @Override
    public void close() { }
}
