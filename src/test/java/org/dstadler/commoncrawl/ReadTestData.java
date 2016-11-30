package org.dstadler.commoncrawl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import org.dstadler.commons.http.HttpClientWrapper;
import org.dstadler.commons.logging.jdk.LoggerFactory;

/**
 * Small helper app to load a chunk of data from the URL Crawl index 
 * for testing/analyzing the data locally.
 *
 * @author dominik.stadler
 */
public class ReadTestData {
    private final static Logger log = LoggerFactory.make();
    
    private static final int DOWNLOAD_DATA_SIZE = 10*1024*1024;
    
    private static final int SKIP_BLOCKS = 0;

    public static void main(String[] args) throws IOException {
        try (HttpClientWrapper client = new HttpClientWrapper("", null, 1800_000)) {
            // TODO: disabled until new data is published to save us one additional HTTP Request during startup
//            Pair<Long, Long> values = readStartPos(client.getHttpClient());
//            long startPos = values.getRight();
//            int blockSize = values.getLeft().intValue();
            int blockSize = Utils.BLOCK_SIZE;
            long startPos = (long)Utils.HEADER_BLOCK_SIZE + (blockSize * Utils.INDEX_BLOCK_COUNT) + ((long)blockSize * SKIP_BLOCKS);

            readTestData(client.getHttpClient(), startPos);
        }

        log.info("Done");
    }

    public static void readTestData(CloseableHttpClient client, long startPos) throws IOException {
        log.info("Reading data starting at " + startPos + " from " + Utils.INDEX_URL);
        HttpGet httpGet = new HttpGet(Utils.INDEX_URL);
        httpGet.addHeader("Range", "bytes=" + startPos + "-");
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            HttpEntity entity = Utils.checkAndFetch(response, Utils.INDEX_URL);
   
            try (InputStream stream = entity.getContent()) {
                try {
                    log.info("Reading data");
                    byte[] block = new byte[DOWNLOAD_DATA_SIZE];
                    IOUtils.read(stream, block);
                    
                    FileUtils.writeByteArrayToFile(new File("/tmp/data.bin"), block);
                } finally {
                    // always abort reading here inside the finally block of the InputStream as
                    // otherwise HttpClient tries to read the stream fully, which is at least 270GB...
                    httpGet.abort();
                }
            }
        }
    }
    
}
