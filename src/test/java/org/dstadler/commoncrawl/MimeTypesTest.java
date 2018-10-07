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

        assertTrue(MimeTypes.matches("application/emf"));
        assertTrue(MimeTypes.matches("application/x-emf"));
        assertTrue(MimeTypes.matches("image/x-emf"));
        assertTrue(MimeTypes.matches("image/x-mgx-emf"));
    }

    @Test
    public void toExtension() {
        assertEquals("", MimeTypes.toExtension(""));
        assertEquals("", MimeTypes.toExtension("some text"));
        assertEquals("", MimeTypes.toExtension("ms"));
        assertEquals("", MimeTypes.toExtension("application"));
        assertEquals("", MimeTypes.toExtension("application/binary"));
        assertEquals("", MimeTypes.toExtension(null));

        assertEquals(".emf", MimeTypes.toExtension("application/emf"));
        assertEquals(".emf", MimeTypes.toExtension("application/x-emf"));
        assertEquals(".emf", MimeTypes.toExtension("image/x-emf"));
        assertEquals(".mdb", MimeTypes.toExtension("image/x-mgx-emf"));
    }
}
