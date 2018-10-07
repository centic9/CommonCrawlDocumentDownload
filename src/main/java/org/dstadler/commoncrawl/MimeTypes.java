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

		// application/emf, application/x-emf, image/x-emf, image/x-mgx-emf
    	MIME_TYPES.add(Pair.of(Pattern.compile(".*/(?:emf|x-emf|x-mgx-emf).*"), ".emf"));
    	MIME_TYPES.add(Pair.of(Pattern.compile(".*/(?:emz|x-emz).*"), ".emz"));
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
    	if(mime == null) {
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
