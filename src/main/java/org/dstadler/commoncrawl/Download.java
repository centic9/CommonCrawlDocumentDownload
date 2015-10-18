package org.dstadler.commoncrawl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.dstadler.commoncrawl.oldindex.ProcessAndDownload;
import org.dstadler.commons.http.HttpClientWrapper;
import org.dstadler.commons.logging.jdk.LoggerFactory;

/**
 * Download files from a list of urls stored in a file, convert the reversed domain
 * to normal URLs and try to download them.
 * 
 *  Note: This is now superseeded by reading the binary data directly 
 *  from the Common Crawl archive via {@link ProcessAndDownload}. 
 *
 * @author dominik.stadler
 */
public class Download {
    private final static Logger log = LoggerFactory.make();
    
    public static void main(String[] args) throws Exception {
        Utils.ensureDownloadDir();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(Utils.COMMONURLS_PATH))) {
            try (HttpClientWrapper client = new HttpClientWrapper("", null, 30_000)) {
                int index = 0;
                while(true) {
                    String url = reader.readLine();
                    if(url == null) {
                        break;
                    }
                    
                    File failedFile = Utils.computeDownloadFileName(url, ".failed");
                    if(failedFile.exists()) {
                        log.info("Skipping download that failed before:" + url);
                        continue;
                    }
                    try {
                        readURL(client.getHttpClient(), index, url);
                    } catch (IOException | URISyntaxException e) {
                        String msg = "Download failed for URL:" + url + ": " + e;
                        log.info(msg);
                        FileUtils.write(failedFile, msg + "\n" + ExceptionUtils.getStackTrace(e));
                    } catch (Exception e) {
                        throw new Exception("Failed for url " + url, e);
                    }
                    
                    index++;
                }
            }
        }
        
        log.info("Done");
    }

    private static void readURL(CloseableHttpClient client, int index, String urlStr) 
            throws IllegalStateException, IOException, URISyntaxException {
        File destFile = Utils.computeDownloadFileName(urlStr, "");
        if(destFile.exists()) {
            log.info("File " + destFile + " already downloaded");
            return;
        }

        // convert url, host is in inverted order
        URI url = Utils.convertUrl(urlStr);
        
        log.info("Reading file from " + url + " to " + destFile + " based on input " + urlStr);
        download(client, index, urlStr, destFile, url);
    }

    private static void download(CloseableHttpClient client, int index, String urlStr, File destFile, URI url) throws IOException, ClientProtocolException {
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            HttpEntity entity = Utils.checkAndFetch(response, url.toString());
            try (InputStream content = entity.getContent()) {
                storeFile(urlStr, destFile, content);
            }
        }
    }

    public static void storeFile(String urlStr, File destFile, InputStream content) throws IOException {
        File file = File.createTempFile("commoncrawl", ".tmp");
        try {
            // first copy to temporary file to only store it permanently after it is fully downloaded
            FileUtils.copyInputStreamToFile(content, file);

            if(Utils.isCorruptDownload(file)) {
                String msg = "URL " + urlStr + " was corrupt after downloading";
                log.warning(msg);
                FileUtils.write(Utils.computeDownloadFileName(urlStr, ".failed"), msg);
                return;
            }

            // now move the file 
            FileUtils.moveFile(file, destFile);
        } finally {
            // usually not there any more, but ensure that we delete it in any case 
            if(file.exists() && !file.delete()) {
                throw new IllegalStateException("Could not delete temporary file " + file);
            }
        }
    }
}
