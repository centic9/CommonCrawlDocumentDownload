package org.dstadler.commoncrawl.oldindex;

import java.io.Closeable;



/**
 * Interface for handling one block of data from the 
 * Common Crawl URL index.
 *
 * @author dominik.stadler
 */
public interface BlockProcessor extends Closeable {

    int ITEM_DATA_SIZE = 32;

    void offer(byte[] block, long blockIndex);
}