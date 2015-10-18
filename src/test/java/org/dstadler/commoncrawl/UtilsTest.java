package org.dstadler.commoncrawl;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.dstadler.commoncrawl.oldindex.BlockProcessor;
import org.dstadler.commons.http.HttpClientWrapper;
import org.dstadler.commons.http.NanoHTTPD;
import org.dstadler.commons.testing.MockRESTServer;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.Test;


public class UtilsTest {
    //private final static Logger log = LoggerFactory.make();
    
    @Test
    public void testReverseDomain() throws Exception {
        assertEquals("www.actualicese.com", Utils.reverseDomain("com.actualicese.www"));
        assertEquals("a", Utils.reverseDomain("a"));
        assertEquals("a.b", Utils.reverseDomain("b.a"));
        assertEquals("a.", Utils.reverseDomain(".a"));
        assertEquals("a", Utils.reverseDomain("a."));
        assertEquals("a.b.c.d.e.f.g.h.i.j.k.l.m.n.o", Utils.reverseDomain("o.n.m.l.k.j.i.h.g.f.e.d.c.b.a"));
        assertEquals("www.ac_tualicese.-.com", Utils.reverseDomain("com.-.ac_tualicese.www"));
        
        assertEquals("", Utils.reverseDomain(""));
        assertEquals(null, Utils.reverseDomain(null));
    }

    @Test
    public void testConvertUrl() throws Exception {
        assertEquals(
                new URI("http://a"),
                Utils.convertUrl("a"));

        assertEquals(
                new URI("http://a"),
                Utils.convertUrl("a:http"));

        assertEquals(
                new URI("http://a:1234"),
                Utils.convertUrl("a:1234"));

        assertEquals(
                new URI("http://www.actualicese.com/editorial/especiales/navidad/2005/informerosc/roscchile.doc"),
                Utils.convertUrl("com.actualicese.www/editorial/especiales/navidad/2005/informerosc/roscchile.doc"));

        assertEquals(
                new URI("http://www.actualicese.com"),
                Utils.convertUrl("com.actualicese.www"));

        assertEquals(
                new URI("http://co.ar.au"),
                Utils.convertUrl("au.ar.co"));
        
        assertEquals(
                new URI("http://coa.ar.au"),
                Utils.convertUrl("au.ar.co[a]"));

        assertEquals(
                new URI("http://167.86.200.110/portals/7/language%20arts/smith/research%20on%20the%20elizabethan%20per--macbeth.doc"),
                Utils.convertUrl("110.200.86.167/portals/7/language%20arts/smith/research%20on%20the%20elizabethan%20per--macbeth.doc"));
        
        assertEquals(
                new URI("http://199.91.152.175/hi0kbnp7eudg/nozym54zojh/monkey%5c's+book+of+opposites.pptx"),
                Utils.convertUrl("175.152.91.199/hi0kbnp7eudg/nozym54zojh/monkey%5c%27s+book+of+opposites.pptx"));
        assertEquals(
                new URI("http://79.170.40.7/inactive.cgi?site=hottubsforsale.uk.com;key=829fe36e8e393613101497cd08630341;path=/downloads/chemical%20safety.doc"),
                Utils.convertUrl("7.40.170.79/inactive.cgi?site=hottubsforsale.uk.com;key=829fe36e8e393613101497cd08630341;path=/downloads/chemical%20safety.doc"));
        assertEquals(
                new URI("http://79.170.40.7/inactive.cgi?site=hottubsforsale.uk.com;key=829fe36e8e393613101497cd08630341;path=/downloads/chemical%20safety.doc"),
                Utils.convertUrl("7.40.170.79/inactive.cgi?site=hottubsforsale.uk.com;key=829fe36e8e393613101497cd08630341;path=/downloads/chemical%20safety.doc:http"));
        assertEquals(
                new URI("https://79.170.40.7/inactive.cgi?site=hottubsforsale.uk.com;key=829fe36e8e393613101497cd08630341;path=/downloads/chemical%20safety.doc"),
                Utils.convertUrl("7.40.170.79/inactive.cgi?site=hottubsforsale.uk.com;key=829fe36e8e393613101497cd08630341;path=/downloads/chemical%20safety.doc:https"));
    }

    @Test
    public void testIsCorruptDownload() throws Exception {
        File file = File.createTempFile("UtilsTest", ".tmp");
        try {
            // empty file
            assertFalse(Utils.isCorruptDownload(file));
            
            // some data
            FileUtils.writeStringToFile(file, "somedata");
            assertFalse(Utils.isCorruptDownload(file));
            
            // specific data
            FileUtils.writeStringToFile(file, "<!DOCTYPE HTML");
            assertTrue(Utils.isCorruptDownload(file));

            FileUtils.writeStringToFile(file, "<!DOCTYPE html");
            assertTrue(Utils.isCorruptDownload(file));
            
            FileUtils.writeStringToFile(file, "<!DOCTYPE html>");
            assertTrue(Utils.isCorruptDownload(file));
            
            FileUtils.writeStringToFile(file, "<!doctype html");
            assertTrue(Utils.isCorruptDownload(file));
            
            FileUtils.writeStringToFile(file, "<html");
            assertTrue(Utils.isCorruptDownload(file));
            FileUtils.writeStringToFile(file, "\n<html");
            assertTrue(Utils.isCorruptDownload(file));
            FileUtils.writeStringToFile(file, "<!--[if IE");
            assertTrue(Utils.isCorruptDownload(file));

//            FileUtils.writeByteArrayToFile(file, ArrayUtils.addAll(new byte[] { 0xEF, 0xBB, 0xBF }, "<!doctype html".getBytes("ASCII")));
//            assertTrue(Utils.isCorruptDownload(file));
        } finally {
            assertTrue(file.delete());
        }
    }

    @Test
    public void testIsCorruptDownloadInvalidFile() throws Exception {
        try {
            Utils.isCorruptDownload(new File("doesnotexist"));
            fail("Should catch exception");
        } catch (FileNotFoundException e) {
            // expected here
        }
    }
    
    @Test
    public void testLogProgress() throws Exception {
        // no output
        Utils.logProgress(1, 2, 3, 4, 5, 6, 12322);
        
        // output
        Utils.logProgress(1, 2, 3, 4, 12, 6, 0);
        Utils.logProgress(1, 2, 3, System.currentTimeMillis(), 12, 6, 12);
        
        // now with reasonable values
        int SKIP_BLOCKS = 2116400;
        int blockSize = 65536; 
        long startPos = (long)Utils.HEADER_BLOCK_SIZE + (blockSize * 2644) + ((long)blockSize * SKIP_BLOCKS); 
        Utils.logProgress(startPos, blockSize, SKIP_BLOCKS, System.currentTimeMillis()-5000, SKIP_BLOCKS + 500, 20, 233689120776l);
        
        // results not easily testable...
    }

    @Test
    public void testCheckAndFetch() throws Exception {
        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "Ok")) {
            try (HttpClientWrapper client = new HttpClientWrapper("", null, 30_000)) {
                HttpGet httpGet = new HttpGet("http://localhost:" + server.getPort());
                try (CloseableHttpResponse response = client.getHttpClient().execute(httpGet)) {
                    HttpEntity entity = Utils.checkAndFetch(response, "http://localhost:" + server.getPort());
                    assertEquals("Ok", IOUtils.toString(entity.getContent()));
                }
            }
        }
    }

    @Test
    public void testCheckAndFetchFails() throws Exception {
        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_INTERNALERROR, "text/plain", "Ok")) {
            try (HttpClientWrapper client = new HttpClientWrapper("", null, 30_000)) {
                HttpGet httpGet = new HttpGet("http://localhost:" + server.getPort());
                try (CloseableHttpResponse response = client.getHttpClient().execute(httpGet)) {
                    try {
                        Utils.checkAndFetch(response, "http://localhost:" + server.getPort());
                        fail("Expect to catch Exception here");
                    } catch (IOException e) {
                        TestHelpers.assertContains(e, "500", "localhost", Integer.toString(server.getPort()));
                    }
                }
            }
        }
    }

    @Test
    public void testComputeDownloadFileName() {
        assertEquals(Utils.DOWNLOAD_DIR, Utils.computeDownloadFileName("", ""));
        assertEquals(new File(Utils.DOWNLOAD_DIR, "12"), Utils.computeDownloadFileName("1", "2"));
        assertEquals(new File(Utils.DOWNLOAD_DIR, "com.corp.www_file123234"), Utils.computeDownloadFileName("com.corp.www/file123234", ""));
        
        // remove :http
        assertEquals(new File(Utils.DOWNLOAD_DIR, "com.corp.www_file123234"), Utils.computeDownloadFileName("com.corp.www/file123234:http", ""));
        
        // replace some characters
        assertEquals(new File(Utils.DOWNLOAD_DIR, "com.corp.www_file123234()123()123_"), Utils.computeDownloadFileName("com.corp.www/file123234()123[]123/", ""));
        
        // overlong names
        assertEquals(new File(Utils.DOWNLOAD_DIR, StringUtils.repeat("1", 240 ) + "..."), Utils.computeDownloadFileName(StringUtils.repeat("1", 500), ""));
    }

    // helper method to get coverage of the unused constructor
    @Test
    public void testPrivateConstructor() throws Exception {
        org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(Utils.class);
    }

    @Test
    public void testHandleBlock() throws IOException {
        byte[] block = FileUtils.readFileToByteArray(new File("src/test/data/block0.bin"));
        
        final AtomicBoolean called = new AtomicBoolean();
        try (BlockProcessor processor = new BlockProcessor() {
            
            @Override
            public void close() throws IOException {
                // nothing needed here
            }
            
            @Override
            public void offer(byte[] block, long blockIndex) {
                assertEquals(Utils.BLOCK_SIZE, block.length);
                assertEquals(0, blockIndex);
                
                called.set(true);
            }
        }) {
            assertTrue(Utils.handleBlock(0, Utils.BLOCK_SIZE, new ByteArrayInputStream(block), processor));
        }
        
        assertTrue(called.get());
        
        assertFalse(Utils.handleBlock(0, Utils.BLOCK_SIZE, new ByteArrayInputStream(new byte[0]), null));
    }
}
