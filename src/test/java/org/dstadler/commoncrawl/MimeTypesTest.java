package org.dstadler.commoncrawl;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

public class MimeTypesTest {
    @Test
    public void matches() {
        assertFalse(MimeTypes.matches(""));
        assertFalse(MimeTypes.matches("some text"));
        assertFalse(MimeTypes.matches("ms"));
        assertFalse(MimeTypes.matches("application"));
        assertFalse(MimeTypes.matches("application/binary"));
        //assertFalse(MimeTypes.matches(null));

        assertFalse(MimeTypes.matches("application/msword"));
        assertFalse(MimeTypes.matches("application/vnd.ms-word"));
        assertFalse(MimeTypes.matches("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        assertFalse(MimeTypes.matches("application/msexcel"));
        assertFalse(MimeTypes.matches("application/vnd.ms-excel"));
        assertFalse(MimeTypes.matches("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertFalse(MimeTypes.matches("application/vnd.ms-excel.addin.macroEnabled.12"));
        assertFalse(MimeTypes.matches("application/vnd.ms-excel.sheet.binary.macroEnabled.12"));
        assertFalse(MimeTypes.matches("application/vnd.ms-powerpoint"));
        assertFalse(MimeTypes.matches("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        assertFalse(MimeTypes.matches("application/vnd.ms-powerpoint.presentation.macroenabled.12"));
        assertFalse(MimeTypes.matches("application/vnd.ms-powerpoint.slideshow.macroenabled.12"));
        assertFalse(MimeTypes.matches("application/vnd.openxmlformats-officedocument.presentationml.template"));
        assertFalse(MimeTypes.matches("application/vnd.ms-officetheme"));
        assertFalse(MimeTypes.matches("application/vnd.ms-tnef"));
        assertFalse(MimeTypes.matches("application/vnd.openxmlformats-officedocument.drawingml.chart+xml"));
        assertFalse(MimeTypes.matches("application/vnd.openxmlformats-officedocument.vmlDrawing"));
        assertFalse(MimeTypes.matches("application/vnd.visio"));
        assertFalse(MimeTypes.matches("application/vnd.ms-visio.drawing.main+xml"));
        assertFalse(MimeTypes.matches("application/vnd.ms-visio.template.main+xml"));
        assertFalse(MimeTypes.matches("application/vnd.ms-visio.stencil.main+xml"));
        assertFalse(MimeTypes.matches("application/vnd.ms-visio.drawing.macroEnabled.main+xml"));
        assertFalse(MimeTypes.matches("application/vnd.ms-visio.template.macroEnabled.main+xml"));
        assertFalse(MimeTypes.matches("application/vnd.ms-visio.stencil.macroEnabled.main+xml"));
        assertFalse(MimeTypes.matches("application/vnd.ms-outlook"));
        assertFalse(MimeTypes.matches("application/x-mspublisher"));
        assertFalse(MimeTypes.matches("application/vnd.openxmlformats-officedocument"));
        assertFalse(MimeTypes.matches("application/x-tika-ooxml"));
        assertFalse(MimeTypes.matches("application/x-tika-ooxml-protected"));
        assertFalse(MimeTypes.matches("application/x-tika-msoffice"));

		assertTrue(MimeTypes.matches("application/gpx"));
		assertTrue(MimeTypes.matches("application/gpx+xml"));
		assertTrue(MimeTypes.matches("application/xml-gpx"));
		assertTrue(MimeTypes.matches("application/x-gpx+xml"));
    }

    @Test
    public void toExtension() {
        assertEquals("", MimeTypes.toExtension(""));
        assertEquals("", MimeTypes.toExtension("some text"));
        assertEquals("", MimeTypes.toExtension("ms"));
        assertEquals("", MimeTypes.toExtension("application"));
        assertEquals("", MimeTypes.toExtension("application/binary"));
        assertEquals("", MimeTypes.toExtension(null));

        assertEquals("", MimeTypes.toExtension("application/msword"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-word"));
        assertEquals("", MimeTypes.toExtension("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        assertEquals("", MimeTypes.toExtension("application/msexcel"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-excel"));
        assertEquals("", MimeTypes.toExtension("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-excel.addin.macroEnabled.12"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-excel.sheet.binary.macroEnabled.12"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-powerpoint"));
        assertEquals("", MimeTypes.toExtension("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-powerpoint.presentation.macroenabled.12"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-powerpoint.slideshow.macroenabled.12"));
        assertEquals("", MimeTypes.toExtension("application/vnd.openxmlformats-officedocument.presentationml.template"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-officetheme"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-tnef"));
        assertEquals("", MimeTypes.toExtension("application/vnd.openxmlformats-officedocument.drawingml.chart+xml"));
        assertEquals("", MimeTypes.toExtension("application/vnd.openxmlformats-officedocument.vmlDrawing"));
        assertEquals("", MimeTypes.toExtension("application/vnd.visio"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-visio.drawing.main+xml"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-visio.template.main+xml"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-visio.stencil.main+xml"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-visio.drawing.macroEnabled.main+xml"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-visio.template.macroEnabled.main+xml"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-visio.stencil.macroEnabled.main+xml"));
        assertEquals("", MimeTypes.toExtension("application/vnd.ms-outlook"));
        assertEquals("", MimeTypes.toExtension("application/x-mspublisher"));
        assertEquals("", MimeTypes.toExtension("application/vnd.openxmlformats-officedocument"));
        assertEquals("", MimeTypes.toExtension("application/x-tika-ooxml"));
        assertEquals("", MimeTypes.toExtension("application/x-tika-ooxml-protected"));
        assertEquals("", MimeTypes.toExtension("application/x-tika-msoffice"));

        assertEquals(".gpx", MimeTypes.toExtension("application/gpx"));
        assertEquals(".gpx", MimeTypes.toExtension("application/gpx+xml"));
        assertEquals(".gpx", MimeTypes.toExtension("application/xml-gpx"));
        assertEquals(".gpx", MimeTypes.toExtension("application/x-gpx+xml"));
	}
}
