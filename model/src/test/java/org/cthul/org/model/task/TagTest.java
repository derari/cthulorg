package org.cthul.org.model.task;

import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.junit.Test;

/**
 *
 */
public class TagTest {
    
    @Test
    public void test_getImageName() {
        TagCloud tc = new TagCloud();
        Tag t1 = new Tag(tc, '#', null, "foo");
        Tag t2 = new Tag(tc, '#', t1, "bar");
        assertThat(t2.getIconName()).is("#/foo/bar");
    }

}
