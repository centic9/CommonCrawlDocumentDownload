package org.dstadler.commoncrawl.index;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.logging.Logger;

import org.dstadler.commoncrawl.Utils;
import org.dstadler.commons.http.HttpClientWrapper;
import org.dstadler.commons.logging.jdk.LoggerFactory;

/**
 * Specialized Processor which reads the position in the Common Crawl
 * from the URL in the Block and then downloads and unwraps the actual
 * document in one go.
 *
 * @author dominik.stadler
 */
public class DownloadFromCommonCrawl {
    private static final Logger log = LoggerFactory.make();

    public static void main(String[] args) throws Exception {
        Utils.ensureDownloadDir();
        
    	try (final HttpClientWrapper client = new HttpClientWrapper("", null, 30_000)) {
    		try (BufferedReader reader = new BufferedReader(new FileReader("commoncrawl.txt"), 1024*1024)) {
    			int count = 0;
    			while(true) {
    				String line = reader.readLine();
    				if(line == null) {
    					log.info("End of file reached after " + count + " items");
    				}
    				
    				log.info("Downloading " + line);
    				CDXItem item = CDXItem.parse(line);
    				
    				Utils.downloadFileFromCommonCrawl(client.getHttpClient(), item.url, item.getDocumentLocation(), true);
    			}
    		}
    	}
    }
}
