package org.dstadler.commoncrawl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.HexDump;
import org.dstadler.poi.util.LittleEndian;
import org.junit.Test;
import org.dstadler.commoncrawl.oldindex.BlockProcessor;
import org.dstadler.commons.net.UrlUtils;

public class DocumentLocationTest {
    @Test
	    public void testReadFromOldIndexBlockOne() throws IOException {
	        byte[] block = FileUtils.readFileToByteArray(new File("src/test/data/block0.bin"));
	        int index = 0;
	        while(index < block.length && block[index] != 0) {
	            index++;
	        }
	
	        DocumentLocation header = DocumentLocation.readFromOldIndexBlock(block, index+1);
	
	        assertEquals("https://commoncrawl.s3.amazonaws.com/parse-output/segment/1346876860779/1346958145255_226.arc.gz", 
	                header.getUrl());
	
	        assertEquals("bytes=77856771-77862431", header.getRangeHeader());
	    }

    @Test
	    public void testReadFromOldIndexBlock() throws IOException {
	        int count = 5;
	        
	        byte[] block = FileUtils.readFileToByteArray(new File("src/test/data/block1.bin"));
	        int index = 0;
	        while(index < block.length) {
	            int offset = index;
	            while(index < block.length && block[index] != 0) {
	                index++;
	            }
	            if(index == offset) {
	                break;
	            }
	
	            System.out.println("URL: " + new String(block, offset, (index-offset), Charset.forName("ASCII")));
	            DocumentLocation header = DocumentLocation.readFromOldIndexBlock(block, index+1);
	            System.out.println("Download: " + header.getUrl());
	            System.out.println("Range (" + header.arcFileOffset + "/" + header.arcFileSize + "): " + header.getRangeHeader());
	            
	            // only do a few Url-Requests to make the test run quickly
	            count--;
	            if(count >= 0) {
	                assertTrue(UrlUtils.isAvailable(
	                        "https://commoncrawl.s3.amazonaws.com/parse-output/segment/1346876860609/1346967937731_3908.arc.gz", 
	                        false, 10_000));
	            }
	
	            // skip 0 byte
	            index++;
	            
	            // skip location information
	            index+= BlockProcessor.ITEM_DATA_SIZE;
	        }
	    }
    
    @Test
    public void testGetLong() throws IOException {
        byte[] array = new byte[] { (byte)0x91, (byte)0xE9, 0x1D, (byte)0x98, 0x39, 0x01, 0x00, 0x00 };
        HexDump.dump(array, 0, System.out, 0);

        int offset = 0;
//        System.out.println(((long) (array[offset + 7] & 0xff) << 56));
//        System.out.println(((long) (array[offset + 6] & 0xff) << 48));
//        System.out.println(((long) (array[offset + 5] & 0xff) << 40));
//        System.out.println(((long) (array[offset + 4] & 0xff) << 32));
//        System.out.println(((long) (array[offset + 3] & 0xff) << 24));
//        System.out.println(((long) (array[offset + 2] & 0xff) << 16));
//        System.out.println(((long) (array[offset + 1] & 0xff) << 8));
//        System.out.println(((long) (array[offset + 0] & 0xff) << 0));
        
//        System.out.println(((long) (array[offset + 5] & 0xff) << 40) |
//                ((long) (array[offset + 4] & 0xff) << 32));
        
//        System.out.println(BlockHeader.getLong(array, offset));
        
        assertEquals(1346876860817L, LittleEndian.getLong(array, offset));
    }
}
