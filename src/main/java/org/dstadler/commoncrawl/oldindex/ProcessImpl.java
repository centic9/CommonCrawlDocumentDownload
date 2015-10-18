package org.dstadler.commoncrawl.oldindex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.dstadler.commoncrawl.Extensions;
import org.dstadler.commons.logging.jdk.LoggerFactory;

/**
 * Handler for blocks, it provides a queue where blocks are pushed to and 
 * starts a separate thread internally which will do the actual processing
 * of the block so that the main thread can spend all its time downloading.
 * 
 * Typically the Thread is mostly waiting for blocks as the bottleneck usually 
 * will be the downloading of data in the main thread unless you have a very high 
 * bandwidth.
 *
 * @author dominik.stadler
 */
public class ProcessImpl implements BlockProcessor {
    private final static Logger log = LoggerFactory.make();
    
    private final BlockingDeque<Pair<byte[],Long>> queue = new LinkedBlockingDeque<>(1000);
    private final BufferedWriter writer;
    private final Processor processor;
    
    private static final Charset CHARSET_ASCII = Charset.forName("ASCII");
    public ProcessImpl(File file, boolean append) throws IOException {
        writer = new BufferedWriter(new FileWriter(file, append));
        processor = new Processor();
        processor.start();
    }
    
    /* (non-Javadoc)
     * @see org.dstadler.commoncrawl.BlockProcessor#add(byte[], long)
     */
    @Override
    public void offer(byte[] block, long blockIndex) {
        // offer with timeout to print out "w"s whenever the queue is full and 
        // pushing in blocks is actually blocked. 
        // This will only happen with fast download speed and very low CPU or I/O speed locally
        // as usually there is much less data written than read and local I/O should be much faster
        // than network bandwidth
        for(int i = 0;i < 60*60;i++) {
            try {
                if(queue.offerLast(ImmutablePair.of(block, blockIndex), 1, TimeUnit.SECONDS)) {
                    return;
                }
            } catch (InterruptedException e) {
                log.log(Level.WARNING, "Offer was interrupted", e);
            }
            
            System.out.print("w");
        }
        
        throw new IllegalStateException("Could not add a single block after one hour!");
    }

    /**
     * Thread which reads from the queue and processes the blocks.
     *
     * @author dominik.stadler
     */
    private class Processor extends Thread {
        private volatile boolean shouldStop = false;
        
        public Processor() {
            super("Processor");
        }

        public void shouldStop() {
            shouldStop = true;
        }
        
        @Override
        public void run() {
            while(true) {
                try {
                    Pair<byte[],Long> item = queue.poll(1, TimeUnit.SECONDS);
                    if(item != null) {
                        splitBlock(item.getLeft(), item.getRight().longValue());
                    } else if(shouldStop) {     // only stop if there are no more blocks to handle...
                        return;
                    } else {
                        // indicate in the output whenever we are idle
                        System.out.print("z");
                    }
                } catch (InterruptedException e) {
                    // no action needed
                } catch (Exception e) {
                    log.log(Level.WARNING, "Had Exception while handling block", e);
                }
            }
        }
        
        private void splitBlock(byte[] block, long blockIndex) throws IOException {
            int index = 0;
            while(index < block.length) {
                int offset = index;
                while(index < block.length && block[index] != 0) {
                    index++;
                }
                if(index == offset) {
                    break;
                }

                // cut off ":http" and ":https" at the end by adjusting the length
                int length = index-offset;
                if(length > 5 && 
                        block[index-1] == 'p' &&
                        block[index-2] == 't' &&
                        block[index-3] == 't' &&
                        block[index-4] == 'h' &&
                        block[index-5] == ':') {
                    length-=5;
                } else if (length > 5 && 
                        block[index-1] == 's' &&
                        block[index-2] == 'p' &&
                        block[index-3] == 't' &&
                        block[index-4] == 't' &&
                        block[index-5] == 'h' &&
                        block[index-6] == ':') {
                    // TODO: is removing https necessary?
                    length-=6;
                }
                
                // only look at the last 5 characters as we are only interested in the extension
                // this makes it easier for new String() and toLowerCase() below...
                int start = offset;
                if(length > 5) {
                    start += (length - 5);
                    length = 5;
                }
                
                //System.out.println("Found(" + index + "): " + url);
                if(Extensions.matches(new String(block, start, length, CHARSET_ASCII).toLowerCase())) {
                    String url = new String(block, offset, (index-offset), CHARSET_ASCII);
                    writer.write(url + "\n");

                    //handle header if wanted, use +1 to skip 0-terminator
                    handle(url, block, index+1, blockIndex);
                    
                    System.out.print(".");
                } 
                
                // skip 0 byte
                index++;
                
                // skip location information
                index+= ITEM_DATA_SIZE;
            }
        }
    }
    
    protected void handle(String url, byte[] block, int headerStart, long blockIndex) throws IOException {
        // NOP here
    }

    /* (non-Javadoc)
     * @see org.dstadler.commoncrawl.BlockProcessor#close()
     */
    @Override
    public void close() throws IOException {
        processor.shouldStop();
        try {
            processor.join();
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "Join was interrupted", e);
        }

        // now we have all items written
        writer.close();
    }
}
