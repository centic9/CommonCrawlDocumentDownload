package org.dstadler.commoncrawl;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.dstadler.commons.http.HttpClientWrapper;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Main application to read the URLs from the CommonCrawl URL Index
 * and pass the resulting blocks to the BlockProcessor.
 * 
 * You can change the SKIP_BLOCK constant to start at a different
 * block index, e.g. if you handled a number of blocks before already.
 * 
 * You can switch to another BlockProcessor if you want to use a 
 * different way of handling the blocks, e.g. class Processor will
 * write the URLs into a file without downloading the actual binary
 * data.
 */
public class ReadAndDownload {
    private final static Logger log = LoggerFactory.make();
    
    private static final int SKIP_BLOCKS = 0;

    public static void main(String[] args) throws IllegalStateException, IOException {
        try (HttpClientWrapper client = new HttpClientWrapper("", null, 1800_000)) {
            // TODO: disabled until new data is published to save us one additional HTTP Request during startup
//            Pair<Long, Long> values = readStartPos(client.getHttpClient());
//            long startPos = values.getRight();
//            int blockSize = values.getLeft().intValue();
            int blockSize = Utils.BLOCK_SIZE; 
            long startPos = (long)Utils.HEADER_BLOCK_SIZE + (blockSize * Utils.INDEX_BLOCK_COUNT) + ((long)blockSize * SKIP_BLOCKS); 

            readBlocks(client.getHttpClient(), startPos, blockSize);
        }
        
        log.info("Done");
    }

    private static void readBlocks(CloseableHttpClient client, 
            long startPos, int blockSize) throws IOException, ClientProtocolException {
        Preconditions.checkArgument(startPos > 0);

        log.info("Reading blocks starting at " + startPos + " from " + Utils.INDEX_URL);
        HttpGet httpGet = new HttpGet(Utils.INDEX_URL);
        httpGet.addHeader("Range", "bytes=" + startPos + "-");
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            HttpEntity entity = Utils.checkAndFetch(response, Utils.INDEX_URL);
   
            long startTs = System.currentTimeMillis();
            //try (InputStream stream = new BufferedInputStream(entity.getContent(), 5*blockSize)) {
            try (InputStream stream = entity.getContent()) {
                try (BlockProcessor processor = new ProcessAndDownload(Utils.COMMONURLS_PATH, true)){
                    for(long blockIndex = SKIP_BLOCKS;true;blockIndex++) {
                        Utils.logProgress(startPos, blockSize, SKIP_BLOCKS, startTs, blockIndex, 200, 233689120776l);
                        if(!Utils.handleBlock(blockIndex, blockSize, stream, processor)) {
                            break;
                        }
                    }
                } finally {
                    // always abort reading here inside the finally block of the InputStream as
                    // otherwise HttpClient tries to read the stream fully, which is at least 270GB...
                    httpGet.abort();
                }
            }
        }
    }
}
