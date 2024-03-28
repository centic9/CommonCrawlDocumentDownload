package org.dstadler.commoncrawl.oldindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ProcessImplTest {

    @Test
    public void testOffer() throws Exception {
        File file = File.createTempFile("ProcessImplTest", ".tmp");
        try {
            assertTrue(file.delete());

            try (BlockProcessor process = new ProcessImpl(file, false)) {
                byte[] block = FileUtils.readFileToByteArray(new File("src/test/data/block1.bin"));

                process.offer(block, 0);
            } // this will flush the queue

            // need to do this outside the block to let process.close() join the Thread
            assertTrue("Failed for " + file,
					file.exists());
            assertTrue("Failed for " + file,
					file.length() > 0);

            final long length = file.length();

            // with append=false
            try (BlockProcessor process = new ProcessImpl(file, false)) {
                byte[] block = FileUtils.readFileToByteArray(new File("src/test/data/block1.bin"));

                process.offer(block, 0);
            } // this will flush the queue

            // need to do this outside the block to let process.close() join the Thread
            assertTrue("Failed for " + file,
					file.exists());
            assertTrue("Failed for " + file,
					file.length() > 0);

            assertEquals("When re-creating the file the length should be the same as before",
                    length, file.length());

            // with append=false
            try (BlockProcessor process = new ProcessImpl(file, true)) {
                byte[] block = FileUtils.readFileToByteArray(new File("src/test/data/block1.bin"));

                process.offer(block, 0);
            } // this will flush the queue

            // need to do this outside the block to let process.close() join the Thread
            assertTrue("Failed for " + file,
					file.exists());
            assertTrue("Failed for " + file,
					file.length() > 0);

            assertEquals("When appending, the length should be double",
                    length*2, file.length());
        } finally {
            assertTrue(file.delete());
        }
    }
}
