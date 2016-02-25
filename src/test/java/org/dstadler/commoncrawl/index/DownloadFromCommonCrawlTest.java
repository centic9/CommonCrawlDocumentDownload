package org.dstadler.commoncrawl.index;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.dstadler.commoncrawl.DocumentLocation;
import org.dstadler.commoncrawl.Utils;
import org.dstadler.commons.http.HttpClientWrapper;
import org.junit.Ignore;
import org.junit.Test;

public class DownloadFromCommonCrawlTest {
	private static final String line = "{\"url\": \"http://www.gjk.gov.al/include_php/previewdoc.php?id_kerkesa_vendimi=1359&nr_vendim=1\", \"mime\": \"application/msword\", \"status\": \"200\", \"digest\": \"FMRMNDSLHYJCIYGOPKBLYQDFE5LJB4SS\", \"length\": \"12298\", \"offset\": \"460161775\", \"filename\": \"common-crawl/crawl-data/CC-MAIN-2015-35/segments/1440645199297.56/warc/CC-MAIN-20150827031319-00038-ip-10-171-96-226.ec2.internal.warc.gz\"}";
	private static final String line_2015_48 = "{\"url\": \"http://ddp.org.za/programme-events/local-government/conference/2008/Municipal%20Services%20Partnerships%20Local%20Perspective.doc/\", \"mime\": \"application/msword\", \"status\": \"200\", \"digest\": \"QVDVT5WG6SWRQBISUEXRNX6RAEXKX26K\", \"length\": \"32901\", \"offset\": \"62916137\", \"filename\": \"common-crawl/crawl-data/CC-MAIN-2015-48/segments/1448398468971.92/warc/CC-MAIN-20151124205428-00118-ip-10-71-132-137.ec2.internal.warc.gz\"}";

	@Ignore("Just for local testing")
	@Test
	public void testMain() throws Exception {
		CDXItem item = CDXItem.parse(line);
		
    	try (final HttpClientWrapper client = new HttpClientWrapper("", null, 30_000)) {
    		File file = Utils.downloadFileFromCommonCrawl(client.getHttpClient(), item.url, item.getDocumentLocation(), true);
    		assertNotNull(file);
    	}
	}

	@Ignore("Just for local testing")
	@Test
	public void testBare() throws Exception {
		CDXItem item = CDXItem.parse(line);
		DocumentLocation header = item.getDocumentLocation();
		
    	try (final HttpClientWrapper client = new HttpClientWrapper("", null, 30_000)) {
	        HttpGet httpGet = new HttpGet(header.getUrl());
	        httpGet.addHeader("Range", header.getRangeHader());
	        try (CloseableHttpResponse response = client.getHttpClient().execute(httpGet)) {
	            HttpEntity entity = Utils.checkAndFetch(response, header.getUrl());
	            
	            try (InputStream stream = new GZIPInputStream(entity.getContent())) {
	            	FileUtils.copyInputStreamToFile(stream, new File("/tmp/test.bin"));
	            }
	        }
    	}
	}
	
	@Test
	public void testDownload() throws Exception {
		downloadFile(line);
	}


	@Test
	public void testDownload2015_48() throws Exception {
		downloadFile(line_2015_48);
	}

	private void downloadFile(String json) throws IOException {
		try (final HttpClientWrapper client = new HttpClientWrapper("", null, 30_000)) {
			File file = File.createTempFile("DownloadFromCommonCrawl", ".tst");
			try {
				CDXItem item = CDXItem.parse(json);
				Utils.downloadFileFromCommonCrawl(client.getHttpClient(), item.url, item.getDocumentLocation(), true, file);
			} finally {
				assertTrue(file.exists());
				assertTrue(file.delete());
			}
		}
	}
}
