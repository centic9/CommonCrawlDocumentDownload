package org.dstadler.commoncrawl.index;

import static org.dstadler.commoncrawl.Utils.COMMON_CRAWL_URL;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Preconditions;
import com.google.common.io.CountingInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.archive.util.zip.GZIPMembersInputStream;
import org.dstadler.commoncrawl.Extensions;
import org.dstadler.commoncrawl.MimeTypes;
import org.dstadler.commoncrawl.Utils;
import org.dstadler.commons.collections.MappedCounter;
import org.dstadler.commons.collections.MappedCounterImpl;
import org.dstadler.commons.http5.HttpClientWrapper5;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.io.*;
import java.util.logging.Logger;

public class DownloadURLIndex {
    private static final Logger log = LoggerFactory.make();

    // https://commoncrawl.org/blog/
    public static final String CURRENT_CRAWL = "CC-MAIN-2024-42";
	public static final File COMMON_CRAWL_FILE = new File("commoncrawl-" + CURRENT_CRAWL + ".txt");

	private static final int START_INDEX = 0;
    private static final int END_INDEX = 299;

    private static final String URL_FORMAT = COMMON_CRAWL_URL +
			"cc-index/collections/" + CURRENT_CRAWL + "/indexes/cdx-%05d.gz";

	private static final JsonFactory JSON_FACTORY = new JsonFactory();

    private static final MappedCounter<String> FOUND_MIME_TYPES = new MappedCounterImpl<>();

    private static int index = START_INDEX;

    public static void main(String[] args) throws Exception {
		LoggerFactory.initLogging();

		log.info("Processing index files starting from index " + index + " with pattern " + URL_FORMAT);
		for(; index <= END_INDEX; index++) {
			try {
				try (HttpClientWrapper5 client = new HttpClientWrapper5("", null, 600_000)) {
					handleCDXFile(client.getHttpClient(), index);
				}
			} catch (IOException e) {
				log.info("Retry once starting at file " + index + ": " + e);

				if (e.getMessage().contains("503")) {
					// wait longer if we get a "503" which is how the server indicates "Please reduce your request rate"
					Thread.sleep(300_000);
				} else {
					Thread.sleep(10_000);
				}

				try (HttpClientWrapper5 client = new HttpClientWrapper5("", null, 600_000)) {
					handleCDXFile(client.getHttpClient(), index);
				}
			}
		}
	}

	private static void handleCDXFile(CloseableHttpClient httpClient, int index) throws IOException {
		String url = URL_FORMAT.formatted(index);

		log.info("Loading file " + index + " from " + url);

    	final HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
		    HttpEntity entity = HttpClientWrapper5.checkAndFetch(response, url);

		    log.info("File " + index + " has " + entity.getContentLength()  + " bytes");
		    try {
				handleInputStream(url, entity.getContent(), index, entity.getContentLength());
			} catch (Exception e) {
				// try to stop processing in case of Exceptions in order to not download the whole file
				// in the implicit close()
				httpClient.close();

				throw e;
			} finally {
				// ensure all content is taken out to free resources
				EntityUtils.consume(entity);
			}
		}

		FileUtils.writeStringToFile(new File("mimetypes.txt"),
				FOUND_MIME_TYPES.sortedMap().toString().replace(",", "\n"), "UTF-8");
	}

	@SuppressWarnings("UnstableApiUsage")
	protected static void handleInputStream(String url, InputStream stream, int index, long length)
			throws IOException {
		// use buffered reading to read large chunks of data at once
		try (CountingInputStream content = new CountingInputStream(new BufferedInputStream(stream, 10*1024*1024));
			CountingInputStream uncompressedStream = new CountingInputStream(new GZIPMembersInputStream(content));
			BufferedReader reader = new BufferedReader(new InputStreamReader(uncompressedStream), 1024*1024)) {
			int count = 0;
			long start = System.currentTimeMillis();
			long lastLog = System.currentTimeMillis();
			while(true) {
				String line = reader.readLine();
				if(line == null) {
					log.info("End of stream reached for " + url + " after " + count + " lines, ");
//		            	log.info(content.available() + " available, "
//		            			+ content.getCount() + " compressed bytes, "
//		            			+ content.read() + " read, "
//		            			+ uncompressedStream.available() + " available, "
//		            			+ uncompressedStream.getCount() + " uncompressed bytes, "
//		            			+ uncompressedStream.read() + " read, "
//		            			);

					break;
				}

				// 107,42,163,148)/dewalink/sarangkartu 20211204015133 {"url": "http://148.163.42.107/dewalink/sarangkartu/", "mime": "text/html", "mime-detected": "text/html", "status": "200", "digest": "VVYPAWNGX5TLK5GHYY5GBEQZ2535FWRJ", "length": "16889", "offset": "138135", "filename": "crawl-data/CC-MAIN-2021-49/segments/1637964362923.11/warc/CC-MAIN-20211204003045-20211204033045-00629.warc.gz", "charset": "UTF-8", "languages": "ind"}
				int endOfUrl = line.indexOf(' ');
				Preconditions.checkState(endOfUrl != -1, "could not find end of url");
				int endOfTimestamp = line.indexOf(' ', endOfUrl+1);
				Preconditions.checkState(endOfTimestamp != -1, "could not find end of timestamp");
				String json = line.substring(endOfTimestamp+1);

				handleJSON(json);

				count++;
				if(count % 100000 == 0 || lastLog < (System.currentTimeMillis() - 10000)) {
					long time = (System.currentTimeMillis() - start)/1000;
					long linesPerSecond = time == 0 ? count : count/time;

					log.info("File " + index + ": " + count + " lines, compressed bytes: " + content.getCount() + " of " + length +
							" (" + "%.2f".formatted(((double)content.getCount()) / length * 100) + "%), bytes: " + uncompressedStream.getCount() + ": " +
							"linesPerSecond: " + linesPerSecond + ": " +
							StringUtils.abbreviate(FOUND_MIME_TYPES.sortedMap().toString(), 95));
					lastLog = System.currentTimeMillis();

					Utils.throttleDownloads();
				}
			}
		}
	}

    private static void handleJSON(String json) throws IOException {
    	try (JsonParser jp = JSON_FACTORY.createParser(json)) {
	    	while(jp.nextToken() != JsonToken.END_OBJECT) {
	    		if(jp.getCurrentToken() == JsonToken.VALUE_STRING) {
	    			/* JSON: url, mime, mime-detected, status, digest, length, offset, filename, charset, language */
		    		if("mime".equals(jp.getCurrentName())) {
		    			String mimeType = jp.getValueAsString().toLowerCase();
						FOUND_MIME_TYPES.inc(mimeType);

		    			if(MimeTypes.matches(mimeType)) {
		    				log.info("Found-Mimetype: " + json);
		    				FileUtils.writeStringToFile(COMMON_CRAWL_FILE, json + "\n", "UTF-8", true);
		    			}
		    		} else if("url".equals(jp.getCurrentName())) {
		    			String url = jp.getValueAsString().toLowerCase();
		    			if(Extensions.matches(url)) {
		    				log.info("Found-URL: " + json);
		    				FileUtils.writeStringToFile(COMMON_CRAWL_FILE, json + "\n", "UTF-8", true);
		    			}
		    		}
	    		}
	    	}
    	}
    }
}
