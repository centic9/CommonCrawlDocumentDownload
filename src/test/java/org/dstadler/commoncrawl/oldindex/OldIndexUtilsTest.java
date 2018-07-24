package org.dstadler.commoncrawl.oldindex;

import org.apache.commons.io.FileUtils;
import org.dstadler.commoncrawl.Utils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class OldIndexUtilsTest {
    @Test
    public void testHandleBlock() throws IOException {
        byte[] block = FileUtils.readFileToByteArray(new File("src/test/data/block0.bin"));

        final AtomicBoolean called = new AtomicBoolean();
        try (BlockProcessor processor = new BlockProcessor() {

            @Override
            public void close() {
                // nothing needed here
            }

            @Override
            public void offer(byte[] array, long blockIndex) {
                assertEquals(Utils.BLOCK_SIZE, array.length);
                assertEquals(0, blockIndex);

                called.set(true);
            }
        }) {
            assertTrue(OldIndexUtils.handleBlock(0, Utils.BLOCK_SIZE, new ByteArrayInputStream(block), processor));
        }

        assertTrue(called.get());

        assertFalse(OldIndexUtils.handleBlock(0, Utils.BLOCK_SIZE, new ByteArrayInputStream(new byte[0]), null));
    }
}
