package org.dstadler.commoncrawl.oldindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

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
            assertTrue(file.exists(),
					"Failed for " + file);
            assertTrue(file.length() > 0,
					"Failed for " + file);

            final long length = file.length();

            // with append=false
            try (BlockProcessor process = new ProcessImpl(file, false)) {
                byte[] block = FileUtils.readFileToByteArray(new File("src/test/data/block1.bin"));

                process.offer(block, 0);
            } // this will flush the queue

            // need to do this outside the block to let process.close() join the Thread
            assertTrue(file.exists(),
					"Failed for " + file);
            assertTrue(file.length() > 0,
					"Failed for " + file);

            assertEquals(length, file.length(), "When re-creating the file the length should be the same as before");

            // with append=false
            try (BlockProcessor process = new ProcessImpl(file, true)) {
                byte[] block = FileUtils.readFileToByteArray(new File("src/test/data/block1.bin"));

                process.offer(block, 0);
            } // this will flush the queue

            // need to do this outside the block to let process.close() join the Thread
            assertTrue(file.exists(),
					"Failed for " + file);
            assertTrue(file.length() > 0,
					"Failed for " + file);

            assertEquals(length*2, file.length(), "When appending, the length should be double");
        } finally {
            assertTrue(file.delete());
        }
    }
}
