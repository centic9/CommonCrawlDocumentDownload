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
        assertFalse(MimeTypes.matches("application/binary"));
        //assertFalse(MimeTypes.matches(null));

        assertTrue(MimeTypes.matches("application/msword"));
        assertTrue(MimeTypes.matches("application/vnd.ms-word"));
        assertTrue(MimeTypes.matches("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        assertTrue(MimeTypes.matches("application/msexcel"));
        assertTrue(MimeTypes.matches("application/vnd.ms-excel"));
        assertTrue(MimeTypes.matches("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertTrue(MimeTypes.matches("application/vnd.ms-excel.addin.macroEnabled.12"));
        assertTrue(MimeTypes.matches("application/vnd.ms-excel.sheet.binary.macroEnabled.12"));
        assertTrue(MimeTypes.matches("application/vnd.ms-powerpoint"));
        assertTrue(MimeTypes.matches("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        assertTrue(MimeTypes.matches("application/vnd.ms-powerpoint.presentation.macroenabled.12"));
        assertTrue(MimeTypes.matches("application/vnd.ms-powerpoint.slideshow.macroenabled.12"));
        assertTrue(MimeTypes.matches("application/vnd.openxmlformats-officedocument.presentationml.template"));
        assertTrue(MimeTypes.matches("application/vnd.ms-officetheme"));
        assertTrue(MimeTypes.matches("application/vnd.ms-tnef"));
        assertTrue(MimeTypes.matches("application/vnd.openxmlformats-officedocument.drawingml.chart+xml"));
        assertTrue(MimeTypes.matches("application/vnd.openxmlformats-officedocument.vmlDrawing"));
        assertTrue(MimeTypes.matches("application/vnd.visio"));
        assertTrue(MimeTypes.matches("application/vnd.ms-visio.drawing.main+xml"));
        assertTrue(MimeTypes.matches("application/vnd.ms-visio.template.main+xml"));
        assertTrue(MimeTypes.matches("application/vnd.ms-visio.stencil.main+xml"));
        assertTrue(MimeTypes.matches("application/vnd.ms-visio.drawing.macroEnabled.main+xml"));
        assertTrue(MimeTypes.matches("application/vnd.ms-visio.template.macroEnabled.main+xml"));
        assertTrue(MimeTypes.matches("application/vnd.ms-visio.stencil.macroEnabled.main+xml"));
        assertTrue(MimeTypes.matches("application/vnd.ms-outlook"));
        assertTrue(MimeTypes.matches("application/x-mspublisher"));
        assertTrue(MimeTypes.matches("application/vnd.openxmlformats-officedocument"));
        assertTrue(MimeTypes.matches("application/x-tika-ooxml"));
        assertTrue(MimeTypes.matches("application/x-tika-ooxml-protected"));
        assertTrue(MimeTypes.matches("application/x-tika-msoffice"));
    }

    @Test
    public void toExtension() {
        assertEquals("", MimeTypes.toExtension(""));
        assertEquals("", MimeTypes.toExtension("some text"));
        assertEquals("", MimeTypes.toExtension("ms"));
        assertEquals("", MimeTypes.toExtension("application"));
        assertEquals("", MimeTypes.toExtension("application/binary"));
        assertEquals("", MimeTypes.toExtension(null));

        assertEquals(".doc", MimeTypes.toExtension("application/msword"));
        assertEquals(".doc", MimeTypes.toExtension("application/vnd.ms-word"));
        assertEquals(".docx", MimeTypes.toExtension("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        assertEquals(".xls", MimeTypes.toExtension("application/msexcel"));
        assertEquals(".xls", MimeTypes.toExtension("application/vnd.ms-excel"));
        assertEquals(".xlsx", MimeTypes.toExtension("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertEquals(".xlsm", MimeTypes.toExtension("application/vnd.ms-excel.addin.macroEnabled.12"));
        assertEquals(".xlsb", MimeTypes.toExtension("application/vnd.ms-excel.sheet.binary.macroEnabled.12"));
        assertEquals(".ppt", MimeTypes.toExtension("application/vnd.ms-powerpoint"));
        assertEquals(".pptx", MimeTypes.toExtension("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        assertEquals(".pptm", MimeTypes.toExtension("application/vnd.ms-powerpoint.presentation.macroenabled.12"));
        assertEquals(".ppsm", MimeTypes.toExtension("application/vnd.ms-powerpoint.slideshow.macroenabled.12"));
        assertEquals(".potx", MimeTypes.toExtension("application/vnd.openxmlformats-officedocument.presentationml.template"));
        assertEquals(".thmx", MimeTypes.toExtension("application/vnd.ms-officetheme"));
        assertEquals(".msg", MimeTypes.toExtension("application/vnd.ms-tnef"));
        assertEquals(".dwg", MimeTypes.toExtension("application/vnd.openxmlformats-officedocument.drawingml.chart+xml"));
        assertEquals(".dwg", MimeTypes.toExtension("application/vnd.openxmlformats-officedocument.vmlDrawing"));
        assertEquals(".vsd", MimeTypes.toExtension("application/vnd.visio"));
        assertEquals(".vsdx", MimeTypes.toExtension("application/vnd.ms-visio.drawing.main+xml"));
        assertEquals(".vstx", MimeTypes.toExtension("application/vnd.ms-visio.template.main+xml"));
        assertEquals(".vssx", MimeTypes.toExtension("application/vnd.ms-visio.stencil.main+xml"));
        assertEquals(".vsdm", MimeTypes.toExtension("application/vnd.ms-visio.drawing.macroEnabled.main+xml"));
        assertEquals(".vstm", MimeTypes.toExtension("application/vnd.ms-visio.template.macroEnabled.main+xml"));
        assertEquals(".vssm", MimeTypes.toExtension("application/vnd.ms-visio.stencil.macroEnabled.main+xml"));
        assertEquals(".msg", MimeTypes.toExtension("application/vnd.ms-outlook"));
        assertEquals(".pub", MimeTypes.toExtension("application/x-mspublisher"));
        assertEquals(".ooxml", MimeTypes.toExtension("application/vnd.openxmlformats-officedocument"));
        assertEquals(".ooxml", MimeTypes.toExtension("application/x-tika-ooxml"));
        assertEquals(".ooxml", MimeTypes.toExtension("application/x-tika-ooxml-protected"));
        assertEquals(".ole2", MimeTypes.toExtension("application/x-tika-msoffice"));
    }
}
