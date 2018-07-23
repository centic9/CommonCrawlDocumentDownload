package org.dstadler.commoncrawl;

/**
 * Which extensions we are interested in.
 *
 * @author dominik.stadler
 */
public class Extensions {

    private static final String[] EXTENSIONS = new String[] {
        // Microsoft Access
        ".mdb",
        ".mde",
        ".accdb",
        ".accdr",
        ".accdt",
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
