package org.dstadler.commoncrawl.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.dstadler.commoncrawl.Utils;
import org.dstadler.commons.http.HttpClientWrapper;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import static org.dstadler.commoncrawl.index.DownloadURLIndex.COMMON_CRAWL_FILE;

/**
 * Specialized Processor which reads the position in the Common Crawl
 * from a file 'commoncrawl-CC-MAIN-&lt;year>-&lt;crawl>.txt' and uses the information
 * to download and unwrap the actual document in one go.
 *
 * @author dominik.stadler
 */
public class DownloadFromCommonCrawl {
	private static final Logger log = LoggerFactory.make();

    public static void main(String[] args) throws Exception {
		LoggerFactory.initLogging();

		Utils.ensureDownloadDir();

    	try (final HttpClientWrapper client = new HttpClientWrapper("", null, 600_000);
    		BufferedReader reader = new BufferedReader(new FileReader(COMMON_CRAWL_FILE), 1024*1024)) {
			int count = 0, downloaded = 0;
			long bytes = 0;
			while(true) {
				String line = reader.readLine();
				if(line == null) {
					log.info("End of file " + COMMON_CRAWL_FILE + " reached after " + count + " items");
					break;
				}

				double percentage = (double)(bytes)/COMMON_CRAWL_FILE.length()*100;
				log.info("Downloading line " + (count+1) + ": " + String.format("%.4f", percentage) + "%, having " + downloaded + " downloaded: " + StringUtils.abbreviate(line, 50));
				CDXItem item = CDXItem.parse(line);

				File file = Utils.downloadFileFromCommonCrawl(client.getHttpClient(), item.url, item.getDocumentLocation(), true);
				if(file != null) {
					downloaded++;
				}

				bytes+=line.length()+1;
				count++;
			}
    	}
    }
}
