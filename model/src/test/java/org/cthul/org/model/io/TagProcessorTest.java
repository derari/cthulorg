package org.cthul.org.model.io;

import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.junit.Test;

/**
 *
 */
public class TagProcessorTest {
    
    private Node paragraph(String text) {
        Paragraph p = new Paragraph();
        Text t = new Text(text);
        p.appendChild(t);
        return p;
    }
    
    @Test
    public void test_dollar_tag() {
        Node p = paragraph("text $home");
        p.accept(new TagProcessor());
        assertThat(p.getLastChild())
                .isA(Token.Tag.class);
    }
    
    @Test
    public void test_dollar_not_a_tag() {
        Node p = paragraph("text $2.99");
        p.accept(new TagProcessor());
        assertThat(p.getFirstChild()).is(p.getLastChild());
    }
    
    @Test
    public void test_priority_tag() {
        Node p = paragraph("text !1");
        p.accept(new TagProcessor());
        assertThat(p.getLastChild())
                .isA(Token.Tag.class);
    }
    
    @Test
    public void test_hash_number_not_a_tag() {
        Node p = paragraph("text #1");
        p.accept(new TagProcessor());
        assertThat(p.getFirstChild()).is(p.getLastChild());
    }
    
}
