package org.dstadler.commoncrawl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class MimeTypesTest {
    @Test
    public void matches() {
        assertFalse(MimeTypes.matches(""));
        assertFalse(MimeTypes.matches("some text"));
        assertFalse(MimeTypes.matches("ms"));
        assertFalse(MimeTypes.matches("application"));
        //assertFalse(MimeTypes.matches(null));

		assertTrue(MimeTypes.matches("image/binary"));
		assertTrue(MimeTypes.matches("image/msword"));
        assertTrue(MimeTypes.matches("image/vnd.ms-word"));
        assertTrue(MimeTypes.matches("image/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        assertTrue(MimeTypes.matches("image/msexcel"));
        assertTrue(MimeTypes.matches("image/vnd.ms-excel"));
        assertTrue(MimeTypes.matches("image/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertTrue(MimeTypes.matches("image/vnd.ms-excel.addin.macroEnabled.12"));
        assertTrue(MimeTypes.matches("image/vnd.ms-excel.sheet.binary.macroEnabled.12"));
        assertTrue(MimeTypes.matches("image/vnd.ms-powerpoint"));
        assertTrue(MimeTypes.matches("image/vnd.openxmlformats-officedocument.presentationml.presentation"));
        assertTrue(MimeTypes.matches("image/vnd.ms-powerpoint.presentation.macroenabled.12"));
        assertTrue(MimeTypes.matches("image/vnd.ms-powerpoint.slideshow.macroenabled.12"));
        assertTrue(MimeTypes.matches("image/vnd.openxmlformats-officedocument.presentationml.template"));
        assertTrue(MimeTypes.matches("image/vnd.ms-officetheme"));
        assertTrue(MimeTypes.matches("image/vnd.ms-tnef"));
        assertTrue(MimeTypes.matches("image/vnd.openxmlformats-officedocument.drawingml.chart+xml"));
        assertTrue(MimeTypes.matches("image/vnd.openxmlformats-officedocument.vmlDrawing"));
        assertTrue(MimeTypes.matches("image/vnd.visio"));
        assertTrue(MimeTypes.matches("image/vnd.ms-visio.drawing.main+xml"));
        assertTrue(MimeTypes.matches("image/vnd.ms-visio.template.main+xml"));
        assertTrue(MimeTypes.matches("image/vnd.ms-visio.stencil.main+xml"));
        assertTrue(MimeTypes.matches("image/vnd.ms-visio.drawing.macroEnabled.main+xml"));
        assertTrue(MimeTypes.matches("image/vnd.ms-visio.template.macroEnabled.main+xml"));
        assertTrue(MimeTypes.matches("image/vnd.ms-visio.stencil.macroEnabled.main+xml"));
        assertTrue(MimeTypes.matches("image/vnd.ms-outlook"));
        assertTrue(MimeTypes.matches("image/x-mspublisher"));
        assertTrue(MimeTypes.matches("image/vnd.openxmlformats-officedocument"));
        assertTrue(MimeTypes.matches("image/x-tika-ooxml"));
        assertTrue(MimeTypes.matches("image/x-tika-ooxml-protected"));
        assertTrue(MimeTypes.matches("image/x-tika-msoffice"));
    }

    @Test
    public void toExtension() {
        assertEquals("", MimeTypes.toExtension(""));
        assertEquals("", MimeTypes.toExtension("some text"));
        assertEquals("", MimeTypes.toExtension("ms"));
        assertEquals("", MimeTypes.toExtension("application"));
        assertEquals("", MimeTypes.toExtension(null));

		assertEquals(".image", MimeTypes.toExtension("image/binary"));
		assertEquals(".image", MimeTypes.toExtension("image/msword"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-word"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        assertEquals(".image", MimeTypes.toExtension("image/msexcel"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-excel"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-excel.addin.macroEnabled.12"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-excel.sheet.binary.macroEnabled.12"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-powerpoint"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.openxmlformats-officedocument.presentationml.presentation"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-powerpoint.presentation.macroenabled.12"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-powerpoint.slideshow.macroenabled.12"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.openxmlformats-officedocument.presentationml.template"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-officetheme"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-tnef"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.openxmlformats-officedocument.drawingml.chart+xml"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.openxmlformats-officedocument.vmlDrawing"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.visio"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-visio.drawing.main+xml"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-visio.template.main+xml"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-visio.stencil.main+xml"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-visio.drawing.macroEnabled.main+xml"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-visio.template.macroEnabled.main+xml"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-visio.stencil.macroEnabled.main+xml"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.ms-outlook"));
        assertEquals(".image", MimeTypes.toExtension("image/x-mspublisher"));
        assertEquals(".image", MimeTypes.toExtension("image/vnd.openxmlformats-officedocument"));
        assertEquals(".image", MimeTypes.toExtension("image/x-tika-ooxml"));
        assertEquals(".image", MimeTypes.toExtension("image/x-tika-ooxml-protected"));
        assertEquals(".image", MimeTypes.toExtension("image/x-tika-msoffice"));
    }
}
