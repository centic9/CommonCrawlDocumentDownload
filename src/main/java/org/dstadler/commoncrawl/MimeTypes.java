package org.dstadler.commoncrawl;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A list of patterns of mimetypes that we are interested in.
 *
 *
 * @author dominik.stadler
 */
public class MimeTypes {
    private static final List<Pair<Pattern, String>> MIME_TYPES = new ArrayList<>();
    static {
        // NOTE: Order is important to have more specialized ones first

    	// application/msword, application/vnd.ms-word
    	MIME_TYPES.add(Pair.of(Pattern.compile(".*(?:msword|ms-word).*"), ".doc"));
    	// application/vnd.openxmlformats-officedocument.wordprocessingml.document
    	MIME_TYPES.add(Pair.of(Pattern.compile(".*wordprocessingml.*"), ".docx"));

    	// application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
    	MIME_TYPES.add(Pair.of(Pattern.compile(".*spreadsheetml.*"), ".xlsx"));
    	//
    	MIME_TYPES.add(Pair.of(Pattern.compile("application/vnd.ms-excel.addin.macroEnabled.12"), ".xlsm"));
    	//
    	MIME_TYPES.add(Pair.of(Pattern.compile("application/vnd.ms-excel.sheet.binary.macroEnabled.12"), ".xlsb"));
        // application/msexcel, application/vnd.ms-excel
        MIME_TYPES.add(Pair.of(Pattern.compile(".*(?:msexcel|ms-excel).*"), ".xls"));

        // application/vnd.openxmlformats-officedocument.presentationml.template
        MIME_TYPES.add(Pair.of(Pattern.compile("application/vnd.openxmlformats-officedocument.presentationml.template"), ".potx"));
        // application/vnd.openxmlformats-officedocument.presentationml.presentation
        MIME_TYPES.add(Pair.of(Pattern.compile(".*presentationml.*"), ".pptx"));
        // application/vnd.ms-powerpoint.presentation.macroenabled.12
        MIME_TYPES.add(Pair.of(Pattern.compile("application/vnd.ms-powerpoint.presentation.macroenabled.12"), ".pptm"));
        // application/vnd.ms-powerpoint.slideshow.macroenabled.12
        MIME_TYPES.add(Pair.of(Pattern.compile("application/vnd.ms-powerpoint.slideshow.macroenabled.12"), ".ppsm"));
        // application/vnd.ms-powerpoint
        MIME_TYPES.add(Pair.of(Pattern.compile("application/vnd.ms-powerpoint.*"), ".ppt"));
        // application/vnd.ms-officetheme
        MIME_TYPES.add(Pair.of(Pattern.compile("application/vnd.ms-officetheme"), ".thmx"));

        // application/vnd.ms-tnef
    	MIME_TYPES.add(Pair.of(Pattern.compile(".*ms-tnef.*"), ".msg"));

    	// application/vnd.openxmlformats-officedocument.drawingml.chart+xml
        // application/vnd.openxmlformats-officedocument.vmlDrawing
    	MIME_TYPES.add(Pair.of(Pattern.compile(".*(?:drawingml|vmlDrawing).*"), ".dwg"));

        // Visio 2013
        MIME_TYPES.add(Pair.of(Pattern.compile("application/vnd.ms-visio.drawing.main\\+xml"), ".vsdx"));
        MIME_TYPES.add(Pair.of(Pattern.compile("application/vnd.ms-visio.template.main\\+xml"), ".vstx"));
        MIME_TYPES.add(Pair.of(Pattern.compile("application/vnd.ms-visio.stencil.main\\+xml"), ".vssx"));
        MIME_TYPES.add(Pair.of(Pattern.compile("application/vnd.ms-visio.drawing.macroEnabled.main\\+xml"), ".vsdm"));
        MIME_TYPES.add(Pair.of(Pattern.compile("application/vnd.ms-visio.template.macroEnabled.main\\+xml"), ".vstm"));
        MIME_TYPES.add(Pair.of(Pattern.compile("application/vnd.ms-visio.stencil.macroEnabled.main\\+xml"), ".vssm"));
        // application/vnd.visio
        MIME_TYPES.add(Pair.of(Pattern.compile(".*visio.*"), ".vsd"));

        // application/vnd.ms-outlook
        MIME_TYPES.add(Pair.of(Pattern.compile("application/vnd.ms-outlook"), ".msg"));

        // application/x-mspublisher
        MIME_TYPES.add(Pair.of(Pattern.compile("application/x-mspublisher"), ".pub"));

        MIME_TYPES.add(Pair.of(Pattern.compile("application/" +
				"(?:vnd.openxmlformats-officedocument|" +
                "x-tika-ooxml|" +
                "x-tika-ooxml-protected)"), ".ooxml"));
        MIME_TYPES.add(Pair.of(Pattern.compile("application/x-tika-msoffice"), ".ole2"));

//    	MIME_TYPE_MATCHER.put(Pattern.compile(".*.*"));

    	/*
vnd.openxmlformats-officedocument.custom-properties+xml 	application/vnd.openxmlformats-officedocument.custom-properties+xml
vnd.openxmlformats-officedocument.customXmlProperties+xml 	application/vnd.openxmlformats-officedocument.customXmlProperties+xml
vnd.openxmlformats-officedocument.drawing+xml 	application/vnd.openxmlformats-officedocument.drawing+xml
vnd.openxmlformats-officedocument.extended-properties+xml 	application/vnd.openxmlformats-officedocument.extended-properties+xml
vnd.openxmlformats-officedocument.theme+xml 	application/vnd.openxmlformats-officedocument.theme+xml
vnd.openxmlformats-officedocument.themeOverride+xml 	application/vnd.openxmlformats-officedocument.themeOverride+xml
vnd.openxmlformats-package.core-properties+xml 	application/vnd.openxmlformats-package.core-properties+xml
vnd.openxmlformats-package.digital-signature-xmlsignature+xml 	application/vnd.openxmlformats-package.digital-signature-xmlsignature+xml
vnd.openxmlformats-package.relationships+xml 	application/vnd.openxmlformats-package.relationships+xml
    	 */
    }

    public static boolean matches(String mime) {
		// optimize to exclude most of the values before even using a regex
		if(mime == null || !mime.startsWith("application/")) {
			return false;
		}

		for(Pair<Pattern,String> entry : MIME_TYPES) {
            if(entry.getKey().matcher(mime).matches()) {
                return true;
            }
        }

        return false;
    }

    public static String toExtension(String mime) {
		// optimize to exclude most of the values before even using a regex
		if(mime == null || !mime.startsWith("application/")) {
    		return "";
    	}

        for(Pair<Pattern, String> entry : MIME_TYPES) {
            if(entry.getKey().matcher(mime).matches()) {
            	return entry.getValue();
            }
        }

        return "";
    }
}
