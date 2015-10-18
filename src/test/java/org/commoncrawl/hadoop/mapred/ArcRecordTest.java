package org.commoncrawl.hadoop.mapred;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpException;
import org.archive.io.arc.ARCRecord;
import org.junit.Ignore;
import org.junit.Test;

public class ArcRecordTest {

	@Test
	public void testArcRecord() throws Exception {
        ArcRecord record = new ArcRecord();
        try (InputStream stream = new GZIPInputStream(new FileInputStream("src/test/data/record.bin"))) {
        	record.readFrom(stream);
            try {
            	assertNotNull(record.getHttpResponse());
            	assertNotNull(record.getHttpResponse().getEntity());
            	assertNotNull(record.getHttpResponse().getEntity().getContent());
            } catch (IllegalStateException  | HttpException e) {
            	throw new IOException(e);
            }
        }
	}
	
	@Ignore("The resulting file is not correctly written, HTTP Headers are ignored!")
	@Test
	public void testArcRecordCommons() throws Exception {
        try (InputStream stream = new GZIPInputStream(new FileInputStream("src/test/data/record.bin"))) {
            try (ARCRecord record = new ARCRecord(stream, "name", 0, true, true, true)) {
            	assertEquals(72, record.read());
            } catch (IllegalStateException e) {
            	throw new IOException(e);
            }
        }
	}
}
