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

    	// application/x-msaccess, application/msaccess, application/vnd.msaccess,
        // application/vnd.ms-access, application/mdb, application/x-mdb, zz-application/zz-winassoc-mdb
    	MIME_TYPES.add(Pair.of(Pattern.compile(".*(?:msaccess|ms-access).*"), ".mdb"));
    	MIME_TYPES.add(Pair.of(Pattern.compile(".*/(?:mdb|x-mdb).*"), ".mdb"));
    }
    
    public static boolean matches(String mime) {
        for(Pair<Pattern,String> entry : MIME_TYPES) {
            if(entry.getKey().matcher(mime).matches()) {
                return true;
            }
        }
        
        return false;
    }
    
    public static String toExtension(String mime) {
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
