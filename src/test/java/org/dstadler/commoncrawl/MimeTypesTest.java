package org.dstadler.commoncrawl;

import org.junit.Test;

import static org.junit.Assert.*;

public class MimeTypesTest {
    @Test
    public void matches() {
        assertFalse(MimeTypes.matches(""));
        assertFalse(MimeTypes.matches("some text"));
        assertFalse(MimeTypes.matches("ms"));
        assertFalse(MimeTypes.matches("application"));
        assertFalse(MimeTypes.matches("application/binary"));
        //assertFalse(MimeTypes.matches(null));

        assertTrue(MimeTypes.matches("application/msaccess"));
        assertTrue(MimeTypes.matches("application/ms-access"));
    }

    @Test
    public void toExtension() {
        assertEquals("", MimeTypes.toExtension(""));
        assertEquals("", MimeTypes.toExtension("some text"));
        assertEquals("", MimeTypes.toExtension("ms"));
        assertEquals("", MimeTypes.toExtension("application"));
        assertEquals("", MimeTypes.toExtension("application/binary"));
        assertEquals("", MimeTypes.toExtension(null));

        assertEquals(".mdb", MimeTypes.toExtension("application/msaccess"));
    }
}
