package org.dstadler.commoncrawl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A list of patterns of mimetypes that we are interested in.
 * 
 *
 * @author dominik.stadler
 */
public class MimeTypes {
    private static final Map<Pattern, String> MIME_TYPES = new HashMap<>();
    static {
    	// application/msword
    	MIME_TYPES.put(Pattern.compile(".*msword.*"), ".doc");
    	// application/vnd.ms-word
    	MIME_TYPES.put(Pattern.compile(".*ms-word.*"), ".doc");
    	// application/vnd.openxmlformats-officedocument.wordprocessingml.document
    	MIME_TYPES.put(Pattern.compile(".*wordprocessingml.*"), ".docx");
    	
    	// application/msexcel
    	MIME_TYPES.put(Pattern.compile(".*msexcel.*"), ".xls");
    	// application/vnd.ms-excel
    	MIME_TYPES.put(Pattern.compile(".*ms-excel.*"), ".xls");
    	// application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
    	MIME_TYPES.put(Pattern.compile(".*spreadsheetml.*"), ".xlsx");
    	// 
    	MIME_TYPES.put(Pattern.compile("application/vnd.ms-excel.addin.macroEnabled.12"), ".xlsm");
    	// 
    	MIME_TYPES.put(Pattern.compile("application/vnd.ms-excel.sheet.binary.macroEnabled.12"), ".xlsb");

    	// application/vnd.openxmlformats-officedocument.presentationml.presentation
    	MIME_TYPES.put(Pattern.compile(".*presentationml.*"), ".pptx");

    	// application/vnd.ms-tnef
    	MIME_TYPES.put(Pattern.compile(".*ms-tnef.*"), ".msg");
    	
    	// application/vnd.openxmlformats-officedocument.drawingml.chart+xml
    	MIME_TYPES.put(Pattern.compile(".*drawingml.*"), ".dwg");

    	// application/vnd.openxmlformats-officedocument.vmlDrawing
    	MIME_TYPES.put(Pattern.compile(".*vmlDrawing.*"), ".dwg");
    	
    	// application/vnd.visio
    	MIME_TYPES.put(Pattern.compile(".*visio.*"), ".vsd");
    	
//    	MIME_TYPE_MATCHER.put(Pattern.compile(".*.*");
    	
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
        for(Pattern pattern : MIME_TYPES.keySet()) {
            if(pattern.matcher(mime).matches()) {
                return true;
            }
        }
        
        return false;
    }
    
    public static String toExtension(String mime) {
    	if(mime == null) {
    		return "";
    	}

        for(Map.Entry<Pattern, String> entry : MIME_TYPES.entrySet()) {
            if(entry.getKey().matcher(mime).matches()) {
            	return entry.getValue();
            }
        }
        
        return "";
    }
}
