package org.dstadler.commoncrawl;

import java.util.Locale;

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
        ",DCX",
        ".BMP",
        ".CUR",
        ".EXIF",
        ".DCX",
        ".GIF",
        ".GIFF",
        ".ICNS",
        ".ICNS",
        ".ICO",
        ".ICON",
        ".JBIG2",
        ".JPEG",
        ".JPG",
        ".PAM",
        ".PBM",
        ".PCX",
        ".PGM",
        ".PNG",
        ".PNM",
        ".PPM",
        ".PSD",
        ".RGBE",
        ".TGA",
        ".TIF",
        ".TIFF",
        ".WBMP",
        ".XBM",
        ".XMP",
        ".XPM",
        ".WMF",
        ".EMF",
    };

    public static boolean matches(String url) {
        for(String ext : EXTENSIONS) {
            if(url.toLowerCase(Locale.ROOT).endsWith(ext.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        return false;
    }
}
