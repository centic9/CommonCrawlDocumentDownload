package org.dstadler.commoncrawl.oldindex;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.dstadler.commoncrawl.Utils;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Read the blocks of URLs from a local file that was downloaded
 * via the download.sh script.
 *
 *  Note: This is now superseded by reading the binary data directly
 *  from the Common Crawl archive via {@link ProcessAndDownload}.
 */
public class ReadFromFile {
    private final static Logger log = LoggerFactory.make();

    private static final File DATA_FILE = new File("index.data");
    private static final int SKIP_BLOCKS = 0;

    public static void main(String[] args) throws IllegalStateException, IOException {
        LoggerFactory.initLogging();

        int blockSize = Utils.BLOCK_SIZE;
        long startPos = (long)Utils.HEADER_BLOCK_SIZE + (blockSize * Utils.INDEX_BLOCK_COUNT) + ((long)blockSize * SKIP_BLOCKS);

        readBlocks(startPos, blockSize);
    }

    private static void readBlocks(long startPos, int blockSize) throws IOException {
        Preconditions.checkArgument(startPos > 0);

        log.info("Reading blocks starting at " + startPos + " from " + DATA_FILE);

        long startTs = System.currentTimeMillis();
        long fileLength = DATA_FILE.length();
        // 10MB => 485blocks per second
        try (InputStream stream = new BufferedInputStream(new FileInputStream(DATA_FILE), 10*1024*1024);
            BlockProcessor processor = new ProcessAndDownload(Utils.COMMONURLS_PATH, false)) {
            long skipped = stream.skip(startPos);
            if(skipped != startPos) {
                throw new IOException("Tried to skip " + startPos + " bytes, but could only skip " + skipped +
                        " bytes while reading file " + DATA_FILE);
            }
            for(long blockIndex = SKIP_BLOCKS;true;blockIndex++) {
                Utils.logProgress(startPos, blockSize, SKIP_BLOCKS, startTs, blockIndex, 500, fileLength);
                if(!OldIndexUtils.handleBlock(blockIndex, blockSize, stream, processor)) {
                    break;
                }
            }
        }

        log.info("Done after " + (System.currentTimeMillis() - startTs)/1000 + " s");
    }
}
