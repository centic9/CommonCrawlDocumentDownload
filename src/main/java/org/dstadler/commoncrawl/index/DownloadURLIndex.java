package org.dstadler.commoncrawl.index;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Preconditions;
import com.google.common.io.CountingInputStream;

public class DownloadURLIndex {
    private static final Logger log = LoggerFactory.make();
    
    private static final int START_INDEX = 0;
    
    private static final String URL_FORMAT = 
    		"https://aws-publicdatasets.s3.amazonaws.com/common-crawl/cc-index/collections/CC-MAIN-2015-35/indexes/cdx-%s.gz";
    
	private static final JsonFactory f = new JsonFactory();
    
    private static final MappedCounter<String> FOUND_MIME_TYPES = new MappedCounterImpl<>();

    public static void main(String[] args) throws Exception {
        log.info("Processing index files starting from index " + START_INDEX + " with pattern " + URL_FORMAT);
        try (HttpClientWrapper client = new HttpClientWrapper("", null, 120_000)) {
            int index = START_INDEX;
            while(true) {
                String indexStr = String.format("%05d", index);    
            	String url = String.format(URL_FORMAT, indexStr);
            	
            	handleCDXFile(client.getHttpClient(), url, index);
            	
                index++;
            }
        }
    }
    
    private static void handleCDXFile(CloseableHttpClient httpClient, String url, int index) throws Exception {
    	log.info("Loading file " + index + " from " + url);

    	final HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
		    HttpEntity entity = Utils.checkAndFetch(response, url);

		    log.info("File " + index + " has " + entity.getContentLength()  + " bytes");
		    try {
		    	handleInputStream(httpClient, url, entity.getContent());
			} finally {
				// ensure all content is taken out to free resources
				EntityUtils.consume(entity);
			}
		}
	}

	protected static void handleInputStream(Closeable httpClient, String url, InputStream stream)
			throws IOException, Exception {
		CountingInputStream content = new CountingInputStream(stream);
		CountingInputStream uncompressedStream = new CountingInputStream(new GZIPMembersInputStream(content));
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(uncompressedStream), 1024*1024)) {
			try {
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
		            	log.info(count + " lines, compressed bytes: " + content.getCount() + 
		            			", bytes: " + uncompressedStream.getCount() + ": " + 
		            			StringUtils.abbreviate(FOUND_MIME_TYPES.sortedMap().toString(), 100));
		            	lastLog = System.currentTimeMillis();
		            }
		        }
			} catch (Exception e) {
				// try to stop processing in case of Exceptions in order to not download the whole file 
				// in the implicit close()
				httpClient.close();

				throw e;
			}
		}
	}

    private static void handleJSON(String json) throws JsonParseException, IOException {
    	try (JsonParser jp = f.createParser(json)) {
	    	while(jp.nextToken() != JsonToken.END_OBJECT) {
	    		if(jp.getCurrentToken() == JsonToken.VALUE_STRING) { 
	    			/* JSON: url, mime, status, digest, length, offset, filename */
		    		if("mime".equals(jp.getCurrentName())) {
		    			String mimeType = jp.getValueAsString().toLowerCase();
						FOUND_MIME_TYPES.addInt(mimeType, 1);
		    			
		    			if(MimeTypes.matches(mimeType)) {
		    				log.info("Found-Mimetype: " + json);
		    				FileUtils.writeStringToFile(new File("commoncrawl.txt"), json + "\n", true);
		    			}
		    		} else if("url".equals(jp.getCurrentName())) {
		    			String url = jp.getValueAsString().toLowerCase();
		    			if(Extensions.matches(url)) {
		    				log.info("Found-URL: " + json);
		    				FileUtils.writeStringToFile(new File("commoncrawl.txt"), json + "\n", true);
		    			}
		    		}
	    		}
	    	}
    	}
    }
}
