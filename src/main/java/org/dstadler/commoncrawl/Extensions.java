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
        // Excel
        ".xls",
        ".xlsx",
        ".xlsm",
        ".xltx",
        ".xlsb",

        // Word
        ".doc",
        ".docx",
        ".dotx",
        ".docm",
        ".ooxml",

        // Powerpoint
        ".ppt",
        ".pot",
        ".pptx",
        ".pptm",
        ".ppsm",
        ".ppsx",
        ".thmx",
        ".potx",

        // Outlook
        ".msg",

        // Publisher
        ".pub",

        // Visio - binary
        ".vsd",
        ".vss",
        ".vst",
        ".vsw",
        
        // Visio - ooxml (currently unsupported)
        ".vsdm",
        ".vsdx",
        ".vssm",
        ".vssx",
        ".vstm",
        ".vstx",

        // POIFS
        ".ole2",

        // Microsoft Admin Template?
        ".adm",

        // Microsoft TNEF
        // ".dat", new HMEFFileHandler());
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
