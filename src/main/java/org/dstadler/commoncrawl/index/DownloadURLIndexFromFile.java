package org.dstadler.commoncrawl.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import org.dstadler.commons.logging.jdk.LoggerFactory;

/**
 * Sample to read from locally stored cdx-00000.gz files
 *
 * @author dstadler
 *
 */
public class DownloadURLIndexFromFile {
    private static final Logger log = LoggerFactory.make();

    private static final int START_INDEX = 0;
    private static final int END_INDEX = 1;

	public static void main(String[] args) throws Exception {
		LoggerFactory.initLogging();

		for(int i = START_INDEX;i <= END_INDEX;i++) {
			File file = new File(String.format("cdx-%05d.gz", i));

	    	log.info("Loading data from " + file + " which has " + file.length()  + " bytes");
		    try (InputStream stream = new FileInputStream(file)) {
		    	DownloadURLIndex.handleInputStream(file.getAbsolutePath(), stream, i, file.length());
	        }
		}
	}
}
