package org.dstadler.commoncrawl.oldindex;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.dstadler.commoncrawl.Utils;
import org.junit.Ignore;
import org.junit.Test;

public class ProcessAndDownloadTest {
	@Ignore("Does not work any with emf-file downloads, ignore for now")
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

				assertTrue("Should have resulting file, but did not find it at " + destFile, destFile.exists());
	        } finally {
	            assertTrue(file.delete());
	        }
        } finally {
        	FileUtils.deleteDirectory(tempDir);
        	Utils.DOWNLOAD_DIR = prevDir;
        }
    }
}
