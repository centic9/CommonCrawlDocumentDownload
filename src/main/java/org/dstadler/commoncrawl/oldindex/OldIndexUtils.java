package org.dstadler.commoncrawl.oldindex;

import org.apache.commons.io.IOUtils;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class OldIndexUtils {
    private final static Logger log = LoggerFactory.make();

    public static boolean handleBlock(long blockIndex, int blockSize, InputStream stream, BlockProcessor processor) throws IOException {
        byte[] block = new byte[blockSize];
        int read = IOUtils.read(stream, block);
        if(read == 0) {
            log.info("EOF => stopping processing");
            return false;
        }

        //FileUtils.writeByteArrayToFile(new File("/tmp/block" + i + ".bin"), block);
        //HexDump.dump(block, 0, System.out, 0, block.length);
        //log.info("Block number " + i);

        processor.offer(block, blockIndex);
        return true;
    }
}
