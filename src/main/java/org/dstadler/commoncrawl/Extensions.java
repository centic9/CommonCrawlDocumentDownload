package org.dstadler.commoncrawl;

/**
 * Which extensions we are interested in.
 *
 * If you want to change which files are downloaded, just can
 * set a different list of file-extensions here, e.g.
 *
 * <pre>
           ".pdf"
 </pre>
 *
 * would find and download PDF documents.
 */
public class Extensions {

    private static final String[] EXTENSIONS = new String[] {
        ".gpx",
		".GPX",
    };

    public static boolean matches(String url) {
        for(String ext : EXTENSIONS) {
            if(url.endsWith(ext)) {
                return true;
            }
        }

        return false;
    }
}
