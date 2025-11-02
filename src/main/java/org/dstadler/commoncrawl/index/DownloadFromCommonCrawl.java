package org.dstadler.commoncrawl.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.dstadler.commoncrawl.Utils;
import org.dstadler.commons.http5.HttpClientWrapper5;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import static org.dstadler.commoncrawl.index.DownloadURLIndex.COMMON_CRAWL_FILE;

/**
 * Specialized Processor which reads the position in the Common Crawl
 * from a file 'commoncrawl-CC-MAIN-&lt;year&gt;-&lt;crawl&gt;.txt' and uses the information
 * to download and unwrap the actual document in one go.
 *
 * @author dominik.stadler
 */
public class DownloadFromCommonCrawl {
	private static final Logger log = LoggerFactory.make();

    public static void main(String[] args) throws Exception {
		LoggerFactory.initLogging();

		Utils.ensureDownloadDir();

    	try (final HttpClientWrapper5 client = new HttpClientWrapper5("", null, 600_000);
    		BufferedReader reader = new BufferedReader(new FileReader(COMMON_CRAWL_FILE), 1024*1024)) {
			int count = 0, downloaded = 0, fileNameTooLong = 0;
			long bytes = 0;
			while(true) {
				String line = reader.readLine();
				if(line == null) {
					log.info("End of file " + COMMON_CRAWL_FILE + " reached after " + count + " items");
					break;
				}

				double percentage = (double)(bytes)/COMMON_CRAWL_FILE.length()*100;
				log.info("Downloading line " + (count+1) + ": " + "%.4f".formatted(percentage) + "%, having " +
                        downloaded + " downloaded: " + StringUtils.abbreviate(line, 50) +
						(fileNameTooLong > 0 ? ", " + fileNameTooLong + " file-names too long" : ""));
				CDXItem item = CDXItem.parse(line);

				try {
					File file = Utils.downloadFileFromCommonCrawl(client.getHttpClient(), item.url, item.getDocumentLocation(), true);
					if (file != null) {
						downloaded++;

//						Utils.throttleDownloads();
					}
				} catch (IOException e) {
					// skip files that we cannot store locally,
					// Exception text is provided by the OS and thus is localized
					// for me, add your own translation if you run into this as well
					if(e.getMessage().contains("Der Dateiname ist zu lang")) {
                        fileNameTooLong++;
                    } else {
						throw e;
					}
				}

				bytes+=line.length()+1;
				count++;
			}
    	}
    }
}
