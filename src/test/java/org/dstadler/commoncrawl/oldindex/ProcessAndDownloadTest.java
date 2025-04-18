package org.dstadler.commoncrawl.oldindex;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.dstadler.commoncrawl.Utils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ProcessAndDownloadTest {
	@Disabled("Downloads from common-crawl and is sometimes flaky")
    @Test
    public void testOffer() throws Exception {
    	File tempDir = File.createTempFile("ProcessAndDownloadTest", ".dir");
    	assertTrue(tempDir.delete());
    	File prevDir = Utils.DOWNLOAD_DIR;
    	Utils.DOWNLOAD_DIR = tempDir;

        File destFile = new File(tempDir, "11.123.196.205_iraz4x2bg0xg_2sf4659mhyqd2dy_Word_Study_Handout.doc");
        try {
	        File file = File.createTempFile("ProcessAndDownloadTest", ".tmp");
	        try {
	            assertTrue(file.delete());

	            try (BlockProcessor process = new ProcessAndDownload(file, false)) {
	                byte[] block = FileUtils.readFileToByteArray(new File("src/test/data/block1.bin"));

	                process.offer(block, 0);
	            } // the implicit close() will wait for the queue to be empty...

	            // need to do this outside the block to let process.close() join the Thread
	            assertTrue(file.exists());
	            assertTrue(file.length() > 0);

				assertTrue(destFile.exists(),
						"Should have resulting file, but did not find it at " + destFile +
								"\nfound: " + Arrays.toString(destFile.getParentFile().list()));
	        } finally {
	            assertTrue(file.delete());
	        }
        } finally {
        	FileUtils.deleteDirectory(tempDir);
        	Utils.DOWNLOAD_DIR = prevDir;
        }
    }
}
