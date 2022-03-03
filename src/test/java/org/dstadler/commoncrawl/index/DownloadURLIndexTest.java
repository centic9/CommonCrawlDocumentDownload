package org.dstadler.commoncrawl.index;

import static org.dstadler.commoncrawl.Utils.COMMON_CRAWL_URL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.dstadler.commoncrawl.Utils;
import org.dstadler.commons.http.HttpClientWrapper;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.CountingInputStream;

public class DownloadURLIndexTest {
    private static final Logger log = LoggerFactory.make();

    @Ignore("Not an actual unit-test...")
	@Test
	public void testRead() throws Exception {
        try (HttpClientWrapper client = new HttpClientWrapper("", null, 30_000)) {

        	String url = COMMON_CRAWL_URL + "cc-index/collections/CC-MAIN-2015-48/indexes/cdx-00000.gz";
        	log.info("Loading data from " + url);

        	final HttpGet httpGet = new HttpGet(url);
    		try (CloseableHttpResponse response = client.getHttpClient().execute(httpGet)) {
    		    HttpEntity entity = Utils.checkAndFetch(response, url);

    		    log.info("Content has " + entity.getContentLength()  + " bytes");
    	        InputStream content = entity.getContent();
    			InputStream uncompressedStream = new GZIPInputStream(content);
    			try (BufferedReader reader = new BufferedReader(
    	        		new InputStreamReader(uncompressedStream, StandardCharsets.UTF_8), 1024*1024)) {
    				try {
    		        	int count = 0;
    		        	long length = 0;
    		        	long lastLog = System.currentTimeMillis();
    	                while(true) {
    	                    String line = reader.readLine();
    	                    if(line == null) {
    	                    	log.info("End of stream reached for " + url + " after " + count + " lines, bytes: " + length + ", ");
    	                    	log.info(content.available() + " available, "
    	                    			+ content.read() + " read, "
    	                    			+ uncompressedStream.available() + " available, "
    	                    			+ uncompressedStream.read() + " read, "
    	                    			);

    	                    	// TODO: close the client here for now until we find out why it stops before the stream
    	                    	// is actually fully processed
    	                    	client.getHttpClient().close();

    	                    	break;
    	                    }

    	                    count++;
    	                    length+=line.length() + 1;
    	                    if(count % 100000 == 0 || lastLog < (System.currentTimeMillis() - 10000)) {
    	                    	log.info(count + " lines, bytes: " + length);
    	                    	lastLog = System.currentTimeMillis();
    	                    }
    	                }
    				} catch (Exception e) {
    					// try to stop processing in case of Exceptions in order to not download the whole file
    					// in the implicit close()
    					client.getHttpClient().close();

    					throw e;
    				}
            	} finally {
            		// ensure all content is taken out to free resources
            		EntityUtils.consume(entity);
            	}
    		}
        }
	}

    @Ignore("Not an actual unit-test...")
	@Test
	public void testReadDirectly() throws Exception {
        try (HttpClientWrapper client = new HttpClientWrapper("", null, 30_000)) {

        	String url = COMMON_CRAWL_URL + "cc-index/collections/CC-MAIN-2015-48/indexes/cdx-00000.gz";
        	log.info("Loading data from " + url);

        	final HttpGet httpGet = new HttpGet(url);
    		try (CloseableHttpResponse response = client.getHttpClient().execute(httpGet)) {
    		    HttpEntity entity = Utils.checkAndFetch(response, url);

    		    log.info("Content has " + entity.getContentLength()  + " bytes");
    	        try (InputStream content = entity.getContent()) {
				try (InputStream uncompressedStream = new GZIPInputStream(content)) {
		        	int count = 0;
		        	long lastLog = System.currentTimeMillis();
	                while(true) {
	                    int n = uncompressedStream.read();
	                    if(n == -1) {
	                    	log.info("End of stream reached for " + url + " after " + count + " bytes, ");
	                    	log.info(content.available() + " available, "
	                    			+ content.read() + " read, "
	                    			+ uncompressedStream.available() + " available, "
	                    			+ uncompressedStream.read() + " read, "
	                    			);

	                    	// TODO: close the client here for now until we find out why it stops before the stream
	                    	// is actually fully processed
	                    	client.getHttpClient().close();

	                    	break;
	                    }

	                    count++;
	                    if(count % 1000000 == 0 || lastLog < (System.currentTimeMillis() - 10000)) {
	                    	log.info(count + " bytes");
	                    	lastLog = System.currentTimeMillis();
	                    }
	                }
				} catch (Exception e) {
					// try to stop processing in case of Exceptions in order to not download the whole file
					// in the implicit close()
					client.getHttpClient().close();

					throw e;
            	} finally {
            		// ensure all content is taken out to free resources
            		EntityUtils.consume(entity);
            	} }
    		}
        }
	}

    @SuppressWarnings("UnstableApiUsage")
	@Ignore("Not an actual unit-test...")
	@Test
	public void testReadFromFile() throws Exception {
		File file = new File("cdx-00000.gz");

    	log.info("Loading data from " + file);

	    log.info("Content has " + file.length()  + " bytes");
        try (CountingInputStream content = new CountingInputStream(new FileInputStream(file))) {
        	CountingInputStream uncompressedStream = new CountingInputStream(new GZIPInputStream(content));
			try (BufferedReader reader = new BufferedReader(
        		new InputStreamReader(uncompressedStream, StandardCharsets.UTF_8), 1024*1024)) {
	        	long count = 0;
	        	long length = 0;
	        	long lastLog = System.currentTimeMillis();
                while(true) {
                    String line = reader.readLine();
                    if(line == null) {
                    	log.info("End of stream reached for " + file + " after " + count + " lines, bytes: " + length + ", ");
                    	log.info(content.getCount() + " compressed bytes, "
                    			+ content.available() + " available, "
                    			+ content.read() + " read, "
                    			+ uncompressedStream.getCount() + " uncompressed bytes, "
                    			+ uncompressedStream.available() + " available, "
                    			+ uncompressedStream.read() + " read, "
                    			);

                    	break;
                    }

                    count++;
                    length+=line.length() + 1;
                    if(count % 100000 == 0 || lastLog < (System.currentTimeMillis() - 10000)) {
                    	log.info(count + " lines, bytes: " + length
                    			+ ", compressed bytes: " + content.getCount()
                    			+ ", bytes: " + uncompressedStream.getCount()
                    			);
                    	lastLog = System.currentTimeMillis();
                    }
                }
        	}
		}
	}
}
