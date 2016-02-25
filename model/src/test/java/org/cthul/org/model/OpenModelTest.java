package org.cthul.org.model;

import org.cthul.org.model.task.EntryText;
import org.cthul.org.model.task.EntryGroup;
import org.cthul.org.model.task.Section;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.cthul.org.model.io.CorgConfiguration;
import org.cthul.org.model.io.TestConfiguration;
import org.junit.Test;
import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.cthul.org.model.task.Task;
import org.cthul.org.model.task.TaskStatus;
import org.cthul.org.model.task.TaskType;

/**
 *
 */
public class OpenModelTest {

    private Section open(String testGroup, String testId) throws IOException {
        Path file = Paths.get("src/test/resources", testGroup, "cfg.properties");
        Properties prop = new Properties();
        prop.setProperty("files.test", testId + ".txt");
        CorgConfiguration cfg = new TestConfiguration(file, prop);
        cfg.initialize();
        return cfg.getModel().getSections().get(0);
    }
    
    @Test
    public void test_section() throws IOException {
        Section file = open("test/heading", "section");
        assertThat(file.getSections()).size().is(1);
        assertThat(file.getSections().get(0))
                .get("level", s -> s.getLevel()).is(2);
    }
    
    @Test
    public void test_chapter() throws IOException {
        Section file = open("test/heading", "chapter");
        assertThat(file.getSections()).size().is(1);
        assertThat(file.getSections().get(0))
                .get("level", s -> s.getLevel()).is(1);
    }
    
    @Test
    public void test_chapter_section() throws IOException {
        Section file = open("test/heading", "chapter.section");
        assertThat(file.getSections()).size().is(2);
    }
    
    @Test
    public void test_paragraphs() throws IOException {
        Section file = open("test/text", "paragraphs");
        assertThat(file.getRootEntries()).size().is(2);
    }
    
    @Test
    public void test_bullets() throws IOException {
        Section file = open("test/text", "bullets");
        assertThat(file.getRootEntries()).size().is(2);
    }
    
    @Test
    public void test_simple_task() throws IOException {
        Section file = open("test/task", "simple");
        assertThat(file.getRootEntries()).size().is(1);
        EntryGroup eg = (EntryGroup) file.getRootEntries().get(0);
        EntryText et = (EntryText) eg.getChildren().get(0);
        assertThat(et.getTasks()).size().is(1);
        Task t = et.getTasks().get(0);
        assertThat(t.getType()).is(TaskType.TASK);
        assertThat(t.getStatus()).is(TaskStatus.OPEN);
    }
    
    @Test
    public void test_complex_line_task() throws IOException {
        Section file = open("test/task", "complex-line");
        assertThat(file.getRootEntries()).size().is(1);
        EntryGroup eg = (EntryGroup) file.getRootEntries().get(0);
        EntryText et = (EntryText) eg.getChildren().get(0);
        assertThat(et.getTasks()).size().is(3);
    }
    
    @Test
    public void test_bullet_task() throws IOException {
        Section file = open("test/task", "bullets");
        assertThat(file.getRootEntries()).size().is(2);
        EntryGroup g1 = (EntryGroup) file.getRootEntries().get(0);
        EntryText t = (EntryText) g1.getChildren().get(0);
        assertThat(getText(t.getNode())).is(" first");
    }
    
    @Test
    public void test_nested_bullet_task() throws IOException {
        Section file = open("test/task", "bullets-nested");
        assertThat(file.getRootEntries()).size().is(1);
        
        EntryGroup g1 = (EntryGroup) file.getRootEntries().get(0);
        EntryGroup g2 = (EntryGroup) g1.getChildren().get(1);
        EntryText t = (EntryText) g2.getChildren().get(0);
        assertThat(getText(t.getNode())).is(" second");
    }
    
    private static String getText(Node n) {
        class V extends AbstractVisitor {
            String s;
            @Override
            public void visit(Text text) {
                s = text.getLiteral();
            }
        }
        V v = new V();
        n.accept(v);
        return v.s;
    }
}
