package org.dstadler.commoncrawl;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A list of patterns of mimetypes that we are interested in.
 *
 * If you want to change which files are downloaded, just can
 * set a different list of mime-types here, e.g.
 *
 * <pre>
    	MIME_TYPES.add(Pair.of(Pattern.compile("application/pdf.*"), ".pdf"));
 </pre>
 *
 * would find and download PDF documents.
 */
public class MimeTypes {
    private static final List<Pair<Pattern, String>> MIME_TYPES = new ArrayList<>();
    static {
        // NOTE: Order is important to have more specialized ones first

    	// application/msword, application/vnd.ms-word
    	MIME_TYPES.add(Pair.of(Pattern.compile("image.*"), ".image"));
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
