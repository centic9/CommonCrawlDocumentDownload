package org.dstadler.commoncrawl;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Which extensions we are interested in.
 *
 * @author dominik.stadler
 */
public class MimeTypes {
    private static final Set<Pattern> MIME_TYPES = new HashSet<>();
    static {
    	// application/msword
    	MIME_TYPES.add(Pattern.compile(".*msword.*"));
    	// application/vnd.ms-word
    	MIME_TYPES.add(Pattern.compile(".*ms-word.*"));
    	// application/vnd.openxmlformats-officedocument.wordprocessingml.document
    	MIME_TYPES.add(Pattern.compile(".*wordprocessingml.*"));
    	
    	// application/msexcel
    	MIME_TYPES.add(Pattern.compile(".*msexcel.*"));
    	// application/vnd.ms-excel
    	MIME_TYPES.add(Pattern.compile(".*ms-excel.*"));
    	// application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
    	MIME_TYPES.add(Pattern.compile(".*spreadsheetml.*"));

    	// application/vnd.openxmlformats-officedocument.presentationml.presentation
    	MIME_TYPES.add(Pattern.compile(".*presentationml.*"));

    	// application/vnd.ms-tnef
    	MIME_TYPES.add(Pattern.compile(".*ms-tnef.*"));
    	
    	// application/vnd.openxmlformats-officedocument.drawingml.chart+xml
    	MIME_TYPES.add(Pattern.compile(".*drawingml.*"));

    	// application/vnd.openxmlformats-officedocument.vmlDrawing
    	MIME_TYPES.add(Pattern.compile(".*vmlDrawing.*"));
    	
    	// application/vnd.visio
    	MIME_TYPES.add(Pattern.compile(".*visio.*"));
    	
//    	MIME_TYPE_MATCHER.add(Pattern.compile(".*.*"));
    	
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
        for(Pattern pattern : MIME_TYPES) {
            if(pattern.matcher(mime).matches()) {
                return true;
            }
        }
        
        return false;
    }
}
