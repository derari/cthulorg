package org.cthul.org.model.io;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListBlock;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.Parser;
import org.cthul.org.model.task.Section;

/**
 *
 */
public class TaskFile {
    
    private final TaskFileSet fileSet;
    private final Path uri;

    public TaskFile(TaskFileSet fileSet, Path uri) {
        this.fileSet = fileSet;
        this.uri = uri;
    }
    
    public void initialize() throws IOException {
        Path file = fileSet.toAbsolutePath(uri);
        Node document;
        try (Reader r = Files.newBufferedReader(file)) {
            document = MD_PARSER.parseReader(r);
        }
        document.accept(new LoadTasksVisitor());
    }
    
    class LoadTasksVisitor extends AbstractVisitor {
        
        Section currentSection = fileSet;
        EntryBuilder currentBuilder = currentSection.getBuilder();
        ListBlock currentList = null;

        @Override
        public void visit(Heading heading) {
            currentSection = currentSection.newSection(heading.getLevel());
            currentSection.getTitle().setText(heading);
            currentBuilder = currentSection.getBuilder();
        }

        @Override
        public void visit(Paragraph paragraph) {
            currentBuilder.add(paragraph);
        }
        
        @Override
        public void visit(BulletList bulletList) {
            ListBlock parent = currentList;
            currentList = bulletList;
            super.visit(bulletList);
            currentList = parent;
        }

        @Override
        public void visit(OrderedList orderedList) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void visit(ListItem listItem) {
            EntryBuilder parent = currentBuilder;
            currentBuilder = parent.add(currentList, listItem);
            super.visit(listItem);
            currentBuilder = parent;
        }

        private RuntimeException expectedParagraph() {
            return new UnsupportedOperationException("Should be wrapped in paragraph");
        }

        @Override
        public void visit(Text text) {
            throw expectedParagraph();
        }
        
        @Override
        public void visit(BlockQuote blockQuote) {
            throw expectedParagraph();
        }

        @Override
        public void visit(Code code) {
            throw expectedParagraph();
        }

        @Override
        public void visit(Emphasis emphasis) {
            throw expectedParagraph();
        }

        @Override
        public void visit(FencedCodeBlock fencedCodeBlock) {
            throw expectedParagraph();
        }

        @Override
        public void visit(HardLineBreak hardLineBreak) {
            throw expectedParagraph();
        }

        @Override
        public void visit(ThematicBreak thematicBreak) {
            throw expectedParagraph();
        }

        @Override
        public void visit(HtmlInline htmlInline) {
            throw expectedParagraph();
        }

        @Override
        public void visit(HtmlBlock htmlBlock) {
            throw expectedParagraph();
        }

        @Override
        public void visit(Image image) {
            throw expectedParagraph();
        }

        @Override
        public void visit(IndentedCodeBlock indentedCodeBlock) {
            throw expectedParagraph();
        }

        @Override
        public void visit(Link link) {
            throw expectedParagraph();
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            throw expectedParagraph();
        }

        @Override
        public void visit(StrongEmphasis strongEmphasis) {
            throw expectedParagraph();
        }

        @Override
        public void visit(CustomBlock customBlock) {
            throw expectedParagraph();
        }

        @Override
        public void visit(CustomNode customNode) {
            throw expectedParagraph();
        }
    }
    
    private static final Parser MD_PARSER;
    
    static {
        MD_PARSER = Parser.builder()
                .customBlockParserFactory(new TaskListParser.Factory())
                .postProcessor(new TagProcessor())
                .build();
    }
}
