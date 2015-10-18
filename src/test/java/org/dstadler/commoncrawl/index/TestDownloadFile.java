package org.dstadler.commoncrawl.index;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.archive.util.zip.GZIPMembersInputStream;
import org.dstadler.commoncrawl.Utils;

/**
 * Simple test-app to reproduce and narrow down the problem with
 * early EOF in GZipInputStream, see e.g. http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7036144 
 * and {@link GZIPMembersInputStream} for some details.
 *  
 * Also http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7021870 and http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4691425
 * might be related? 
 *  
 * @author dstadler
 *
 */
@SuppressWarnings("deprecation")
public class TestDownloadFile {
    private static final String URL = 
    		"https://aws-publicdatasets.s3.amazonaws.com/common-crawl/cc-index/collections/CC-MAIN-2015-35/indexes/cdx-00000.gz";
    
    public static void main(String[] args) throws Exception {
    	HttpClientBuilder builder = HttpClients.custom();
    	
    	builder = builder.setHostnameVerifier(new AllowAllHostnameVerifier());
    	
        try (CloseableHttpClient httpClient = builder.build()) {
        	System.out.println("Loading data from " + URL);

        	final HttpGet httpGet = new HttpGet(URL);
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
    		    HttpEntity entity = Utils.checkAndFetch(response, URL);

    		    System.out.println("Content has " + entity.getContentLength()  + " bytes");
    		    try {
    		    	InputStream stream = entity.getContent();
    				InputStream uncompressedStream = new GZIPMembersInputStream(stream);
    				try (BufferedReader reader = new BufferedReader(
    						new InputStreamReader(uncompressedStream), 1024*1024)) {
    					try {
    				    	int count = 0;
    				        while(true) {
    				            String line = reader.readLine();
    				            if(line == null) {
    				            	System.out.println("End of stream reached for " + URL + " after " + count + " lines, ");
    				            	System.out.println(//stream.available() + " available, "
    				            			//+ content.getCount() + " compressed bytes, "
    				            			//+ stream.read() + " read, "
    				            			//+ uncompressedStream.available() + " available, "
    				            			//+ uncompressedStream.getCount() + " uncompressed bytes, "
    				            			+ uncompressedStream.read() + " read, "
    				            			);
    				            	
    				            	// TODO: close the client here for now until we find out why it stops before the stream
    				            	// is actually fully processed
    				            	httpClient.close();

    				                break;
    				            }

    				            count++;
    				            //System.out.print('.');
    				            if(count % 100000 == 0) {
    				            	System.out.println(count + " lines"
    				            			//+ ", compressed bytes: " + content.getCount()
    				            			//+ ", bytes: " + uncompressedStream.getCount()
    				            			);
    				            }
    				        }
    					} catch (Exception e) {
    						// try to stop processing in case of Exceptions in order to not download the whole file 
    						// in the implicit close()
    						httpClient.close();
    						
    						throw e;
    					}
    				}
    			} finally {
    				// ensure all content is taken out to free resources
    				EntityUtils.consume(entity);
    			}
    		}
        }
    }
}
