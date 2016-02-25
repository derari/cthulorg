package org.cthul.org.model.task;

import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.junit.Test;

/**
 *
 */
public class TagCloudTest {

    @Test
    public void test_get_simple() {
        TagCloud tc = new TagCloud();
        Tag t = tc.get('#', true, "foo");
        assertThat(t.getTagCloud()).is(tc);
    }

    @Test
    public void test_get_from_default() {
        TagCloud tc = new TagCloud();
        Tag t = tc.get('!', true, "chat");
        assertThat(t.getTagCloud()).is(TagCloud.ROOT);
    }

    @Test
    public void test_get_nested_from_default() {
        TagCloud tc = new TagCloud();
        Tag t = tc.get('!', true, "chat/long");
        assertThat(t.getTagCloud()).is(tc);
        assertThat(t.getParent().getTagCloud()).is(TagCloud.ROOT);
    }

    @Test
    public void test_get_nested_from_default2() {
        TagCloud tc1 = new TagCloud();
        TagCloud tc2 = new TagCloud(tc1);
        Tag t = tc2.get('!', true, "chat/long/loud");
        assertThat(t.getTagCloud()).is(tc2);
        assertThat(t.getParent().getParent().getTagCloud()).is(TagCloud.ROOT);
    }

    @Test
    public void test_get_nested_from_default2b() {
        TagCloud tc1 = new TagCloud();
        tc1.get('!', true, "chat/long");
        TagCloud tc2 = new TagCloud(tc1);
        Tag t = tc2.get('!', true, "chat/long/loud");
        assertThat(t.getTagCloud()).is(tc1);
        assertThat(t.getParent().getParent().getTagCloud()).is(TagCloud.ROOT);
    }
    
}
