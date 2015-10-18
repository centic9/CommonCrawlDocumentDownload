package org.dstadler.commoncrawl.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
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
    
    // https://aws-publicdatasets.s3.amazonaws.com/common-crawl/cc-index/collections/CC-MAIN-2015-35/indexes/cdx-00000.gz
    
	private static final JsonFactory f = new JsonFactory();
    
    private static final MappedCounter<String> FOUND_MIME_TYPES = new MappedCounterImpl<>();
    
    public static void main(String[] args) throws Exception {
        if(!Utils.DOWNLOAD_DIR.exists()) {
            if(!Utils.DOWNLOAD_DIR.mkdirs()) {
                throw new IllegalStateException("Could not create directory " + Utils.DOWNLOAD_DIR);
            }
        }
        
        log.info("Processing index files starting from index " + START_INDEX + " with pattern " + URL_FORMAT);
        try (HttpClientWrapper client = new HttpClientWrapper("", null, 30_000)) {
            int index = START_INDEX;
            while(true) {
                String indexStr = String.format("%05d", index);    
            	String url = String.format(URL_FORMAT, indexStr);
            	
            	handleCDXFile(client.getHttpClient(), url);
            	
                index++;
                
                // TODO: for now only process one file to not exhaust Internet connection limits
                break;
            }
        }
    }
    
    private static void handleCDXFile(CloseableHttpClient httpClient, String url) throws IOException {
    	log.info("Loading data from " + url);

    	final HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
		    HttpEntity entity = Utils.checkAndFetch(response, url);

	        CountingInputStream content = new CountingInputStream(entity.getContent());
			CountingInputStream uncompressedStream = new CountingInputStream(new GZIPInputStream(content));
			try (BufferedReader reader = new BufferedReader(
	        		new InputStreamReader(uncompressedStream))) {
				try {
		        	int count = 0;
		        	long lastLog = System.currentTimeMillis();
	                while(true) {
	                    String line = reader.readLine();
	                    if(line == null) {
	                    	log.info("End of stream reached for " + url + " after " + count + " lines, " + 
	                    			content.getCount() + " compressed bytes, " + 
	                    			uncompressedStream.getCount() + " uncompressed bytes");
	                    	
	                    	// TODO: close the client here for now until we find out why it stops before the stream
	                    	// is actually fully processed
	                    	httpClient.close();
	                    	
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
	                    			", bytes: " + uncompressedStream.getCount() + ": " + FOUND_MIME_TYPES.sortedMap());
	                    	lastLog = System.currentTimeMillis();
	                    }
	                }
				} catch (Exception e) {
					// try to stop processing in case of Exceptions in order to not download the whole file 
					// in the implicit close()
					httpClient.close();
					
					throw e;
				}
        	} finally {
        		// ensure all content is taken out to free resources
        		EntityUtils.consume(entity);
        	}
		}
	}

    private static void handleJSON(String json) throws JsonParseException, IOException {
    	try (JsonParser jp = f.createParser(json)) {
	    	while(jp.nextToken() != JsonToken.END_OBJECT) {
	    		if(jp.getCurrentToken() == JsonToken.VALUE_STRING) { 
	    			/* JSON: url, mime, status, digest, length, offset, filename */
		    		if("mime".equals(jp.getCurrentName())) {
		    			String mimeType = jp.getValueAsString();
						FOUND_MIME_TYPES.addInt(mimeType, 1);
		    			
		    			if(MimeTypes.matches(mimeType)) {
		    				log.info("Found-Mimetype: " + json);
		    			}
		    		} else if("url".equals(jp.getCurrentName())) {
		    			String url = jp.getValueAsString();
		    			if(Extensions.matches(url)) {
		    				log.info("Found-URL: " + json);
		    			}
		    		}
	    		}
	    	}
    	}
    }

	private static void download(CloseableHttpClient client, int index, String urlStr, File destFile, URI url) throws IOException, ClientProtocolException {
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            HttpEntity entity = Utils.checkAndFetch(response, url.toString());
            try (InputStream content = entity.getContent()) {
                storeFile(urlStr, destFile, content);
            }
        }
    }

    public static void storeFile(String urlStr, File destFile, InputStream content) throws IOException {
        File file = File.createTempFile("commoncrawl", ".tmp");
        try {
            // first copy to temporary file to only store it permanently after it is fully downloaded
            FileUtils.copyInputStreamToFile(content, file);

            if(Utils.isCorruptDownload(file)) {
                String msg = "URL " + urlStr + " was corrupt after downloading";
                log.warning(msg);
                FileUtils.write(Utils.computeDownloadFileName(urlStr, ".failed"), msg);
                return;
            }

            // now move the file 
            FileUtils.moveFile(file, destFile);
        } finally {
            // usually not there any more, but ensure that we delete it in any case 
            if(file.exists() && !file.delete()) {
                throw new IllegalStateException("Could not delete temporary file " + file);
            }
        }
    }
}
