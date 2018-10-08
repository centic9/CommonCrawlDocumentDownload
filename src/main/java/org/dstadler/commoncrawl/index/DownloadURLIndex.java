package org.dstadler.commoncrawl.index;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Preconditions;
import com.google.common.io.CountingInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.archive.util.zip.GZIPMembersInputStream;
import org.dstadler.commoncrawl.Extensions;
import org.dstadler.commoncrawl.MimeTypes;
import org.dstadler.commoncrawl.Utils;
import org.dstadler.commons.collections.MappedCounter;
import org.dstadler.commons.collections.MappedCounterImpl;
import org.dstadler.commons.http.HttpClientWrapper;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.io.*;
import java.util.logging.Logger;

public class DownloadURLIndex {
    private static final Logger log = LoggerFactory.make();

	public static final String CURRENT_CRAWL = "CC-MAIN-2018-22";
	public static final File COMMON_CRAWL_FILE = new File("commoncrawl-" + CURRENT_CRAWL + ".txt");

	private static final int START_INDEX = 0;
    private static final int END_INDEX = 299;

    private static final String URL_FORMAT =
    		"https://commoncrawl.s3.amazonaws.com/cc-index/collections/" + CURRENT_CRAWL + "/indexes/cdx-%s.gz";

	private static final JsonFactory f = new JsonFactory();

    private static final MappedCounter<String> FOUND_MIME_TYPES = new MappedCounterImpl<>();

    private static int index = START_INDEX;

    public static void main(String[] args) throws Exception {
		LoggerFactory.initLogging();

		run();
    }

	public static void run() throws IOException, InterruptedException {
		log.info("Processing index files starting from index " + index + " with pattern " + URL_FORMAT);
		for(; index <= END_INDEX; index++) {
			try {
				try (HttpClientWrapper client = new HttpClientWrapper("", null, 600_000)) {
					handleCDXFile(client.getHttpClient(), index);
				}
			} catch (IOException e) {
				log.info("Retry once starting at file " + index + ": " + e);

				Thread.sleep(10_000);

				try (HttpClientWrapper client = new HttpClientWrapper("", null, 600_000)) {
					handleCDXFile(client.getHttpClient(), index);
				}
			}
		}
	}

	private static void handleCDXFile(CloseableHttpClient httpClient, int index) throws IOException {
		String indexStr = String.format("%05d", index);
		String url = String.format(URL_FORMAT, indexStr);

		log.info("Loading file " + index + " from " + url);

    	final HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
		    HttpEntity entity = Utils.checkAndFetch(response, url);

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

	protected static void handleInputStream(String url, InputStream stream, int index, long length)
			throws IOException {
		try (CountingInputStream content = new CountingInputStream(stream);
			CountingInputStream uncompressedStream = new CountingInputStream(new GZIPMembersInputStream(content));
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(uncompressedStream), 1024*1024)) {
			int count = 0;
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

				int endOfUrl = line.indexOf(' ');
				Preconditions.checkState(endOfUrl != -1, "could not find end of url");
				int endOfTimestamp = line.indexOf(' ', endOfUrl+1);
				Preconditions.checkState(endOfTimestamp != -1, "could not find end of timestamp");
				String json = line.substring(endOfTimestamp+1);

				handleJSON(json);

				count++;
				//System.out.print('.');
				if(count % 100000 == 0 || lastLog < (System.currentTimeMillis() - 10000)) {
					log.info("File " + index + ": " + count + " lines, compressed bytes: " + content.getCount() + " of " + length +
							"(" + String.format("%.2f", ((double)content.getCount())/length*100) + "%), bytes: " + uncompressedStream.getCount() + ": " +
							StringUtils.abbreviate(FOUND_MIME_TYPES.sortedMap().toString(), 95));
					lastLog = System.currentTimeMillis();
				}
			}
		}
	}

    private static void handleJSON(String json) throws IOException {
    	try (JsonParser jp = f.createParser(json)) {
	    	while(jp.nextToken() != JsonToken.END_OBJECT) {
	    		if(jp.getCurrentToken() == JsonToken.VALUE_STRING) {
	    			/* JSON: url, mime, status, digest, length, offset, filename */
		    		if("mime".equals(jp.getCurrentName())) {
		    			String mimeType = jp.getValueAsString().toLowerCase();
						FOUND_MIME_TYPES.addInt(mimeType, 1);

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
