package playing.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilTest {
    @Test
    public void should_return_prefix() throws Exception {
        assertEquals("", StringUtil.prefix("", ""));
        assertEquals("", StringUtil.prefix("", "A"));
        assertEquals("", StringUtil.prefix("A", ""));

        assertEquals("A", StringUtil.prefix("AA", "A"));
        assertEquals("A", StringUtil.prefix("A", "AA"));

        assertEquals("A", StringUtil.prefix("AB", "AA"));
        assertEquals("A", StringUtil.prefix("AA", "AB"));
    }
}