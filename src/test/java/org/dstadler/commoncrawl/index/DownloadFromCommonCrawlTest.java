package org.dstadler.commoncrawl.index;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.dstadler.commoncrawl.DocumentLocation;
import org.dstadler.commoncrawl.Utils;
import org.dstadler.commons.http5.HttpClientWrapper5;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DownloadFromCommonCrawlTest {
	private static final String line = "{\"url\": \"http://www.gjk.gov.al/include_php/previewdoc.php?id_kerkesa_vendimi=1359&nr_vendim=1\", \"mime\": \"application/msword\", \"status\": \"200\", \"digest\": \"FMRMNDSLHYJCIYGOPKBLYQDFE5LJB4SS\", \"length\": \"12298\", \"offset\": \"460161775\", \"filename\": \"crawl-data/CC-MAIN-2015-35/segments/1440645199297.56/warc/CC-MAIN-20150827031319-00038-ip-10-171-96-226.ec2.internal.warc.gz\"}";
	private static final String line_2015_48 = "{\"url\": \"http://ddp.org.za/programme-events/local-government/conference/2008/Municipal%20Services%20Partnerships%20Local%20Perspective.doc/\", \"mime\": \"application/msword\", \"status\": \"200\", \"digest\": \"QVDVT5WG6SWRQBISUEXRNX6RAEXKX26K\", \"length\": \"32901\", \"offset\": \"62916137\", \"filename\": \"crawl-data/CC-MAIN-2015-48/segments/1448398468971.92/warc/CC-MAIN-20151124205428-00118-ip-10-71-132-137.ec2.internal.warc.gz\"}";

	@Disabled("Just for local testing")
	@Test
	public void testMain() throws Exception {
		CDXItem item = CDXItem.parse(line);

    	try (final HttpClientWrapper5 client = new HttpClientWrapper5("", null, 30_000)) {
    		File file = Utils.downloadFileFromCommonCrawl(client.getHttpClient(), item.url, item.getDocumentLocation(), true);
    		assertNotNull(file);
    	}
	}

	@Disabled("Just for local testing")
	@Test
	public void testBare() throws Exception {
		CDXItem item = CDXItem.parse(line);
		DocumentLocation header = item.getDocumentLocation();

    	try (final HttpClientWrapper5 client = new HttpClientWrapper5("", null, 30_000)) {
	        HttpGet httpGet = new HttpGet(header.getUrl());
	        httpGet.addHeader("Range", header.getRangeHeader());
            client.getHttpClient().execute(httpGet, (HttpClientResponseHandler<Void>) response -> {
                HttpEntity entity = HttpClientWrapper5.checkAndFetch(response, header.getUrl());

                try (InputStream stream = new GZIPInputStream(entity.getContent())) {
                    FileUtils.copyInputStreamToFile(stream, new File("/tmp/test.bin"));
                }

                return null;
            });
    	}
	}

	@Disabled("Downloads from common-crawl and is sometimes flaky")
	@Test
	public void testDownload() throws Exception {
		downloadFile(line);
	}

	@Disabled("Downloads from common-crawl and is sometimes flaky")
	@Test
	public void testDownload2015_48() throws Exception {
		downloadFile(line_2015_48);
	}

	private void downloadFile(String json) throws IOException {
		try (final HttpClientWrapper5 client = new HttpClientWrapper5("", null, 30_000)) {
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
