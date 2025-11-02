package org.dstadler.commoncrawl.index;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.archive.util.zip.GZIPMembersInputStream;
import org.dstadler.commons.http5.HttpClientWrapper5;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.dstadler.commoncrawl.Utils.COMMON_CRAWL_URL;

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
public class TestDownloadFile {
    private static final String URL =
			COMMON_CRAWL_URL + "cc-index/collections/CC-MAIN-2015-48/indexes/cdx-00000.gz";

    public static void main(String[] args) throws Exception {
    	HttpClientBuilder builder = HttpClients.custom();

		PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
				.setSSLSocketFactory(
						SSLConnectionSocketFactoryBuilder.create()
								.setSslContext(SSLContextBuilder.create()
										.loadTrustMaterial(TrustAllStrategy.INSTANCE)
										.build())
								.setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
								.build())
				.build();

		builder.setConnectionManager(connectionManager);

        try (CloseableHttpClient httpClient = builder.build()) {
        	System.out.println("Loading data from " + URL);

        	final HttpGet httpGet = new HttpGet(URL);
            httpClient.execute(httpGet, (HttpClientResponseHandler<Void>) response -> {
                HttpEntity entity = HttpClientWrapper5.checkAndFetch(response, URL);

                System.out.println("Content has " + entity.getContentLength()  + " bytes");
                try (InputStream stream = entity.getContent()) {
                    InputStream uncompressedStream = new GZIPMembersInputStream(stream);
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(uncompressedStream), 1024*1024)) {
                        try {
                            int count = 0;
                            while(true) {
                                String line = reader.readLine();
                                if(line == null) {
                                    System.out.println("End of stream reached for " + URL + " after " + count + " lines, ");
                                    System.out.println(uncompressedStream.read() + " read, "
                                    );

                                    // TODO: close the client here for now until we find out why it stops before the stream
                                    // is actually fully processed
                                    httpClient.close();

                                    break;
                                }

                                count++;
                                //System.out.print('.');
                                if(count % 100000 == 0) {
                                    System.out.println(count + " lines");
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

                return null;
            });
        }
    }
}
