package org.dstadler.commoncrawl;

import java.io.Closeable;



/**
 * Interface for handling one block of data from the 
 * Common Crawl URL index.
 *
 * @author dominik.stadler
 */
public interface BlockProcessor extends Closeable {

    public static final int ITEM_DATA_SIZE = 32;

    public abstract void offer(byte[] block, long blockIndex);
}