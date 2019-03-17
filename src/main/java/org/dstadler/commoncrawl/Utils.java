package org.dstadler.commoncrawl;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.HexDump;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.dstadler.poi.util.LittleEndian;
import org.archive.io.warc.WARCRecord;
import org.archive.util.LaxHttpParser;
import org.commoncrawl.hadoop.mapred.ArcRecord;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;


public class Utils {
    private final static Logger log = LoggerFactory.make();

    // avoid having to read the header data always during testing, can be removed later...
    public static final int INDEX_BLOCK_COUNT = 2644;
    public static final int BLOCK_SIZE = 65536;

    public static final String INDEX_URL = "https://commoncrawl.s3.amazonaws.com/projects/url-index/url-index.1356128792";
    public static final int HEADER_BLOCK_SIZE = 8;
    public static File DOWNLOAD_DIR = new File("../download");
    public static File BACKUP_DIR = new File("../backup");
    public static final File COMMONURLS_PATH = new File("commonurls.txt");

    public static HttpEntity checkAndFetch(CloseableHttpResponse response, String url) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode > 206) {
            String msg = "Had HTTP StatusCode " + statusCode + " for request: " + url + ", response: " +
                    response.getStatusLine().getReasonPhrase();
            log.warning(msg);

            throw new IOException(msg);
        }
        return response.getEntity();
    }

    public static Pair<Long, Long> readStartPos(CloseableHttpClient client) throws IOException {
        log.info("Reading header from " + INDEX_URL);
        HttpGet httpGet = new HttpGet(INDEX_URL);
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            HttpEntity entity = Utils.checkAndFetch(response, INDEX_URL);

            try (InputStream stream = entity.getContent()) {
                try {
                    // try with the first few bytes initially
                    byte[] header = new byte[HEADER_BLOCK_SIZE];
                    IOUtils.read(stream, header);

                    HexDump.dump(header, 0, System.out, 0);

                    long blockSize = LittleEndian.getUInt(header, 0);
                    long indexBlockCount = LittleEndian.getUInt(header, 4);
                    log.info("Header: blockSize " + blockSize + ", indexBlockCount: " + indexBlockCount);

                    // JDK 11: abort and close here to be able to return successfully even when the expeted exception
                    // is thrown
                    httpGet.abort();
                    try {
                        stream.close();
                    } catch (ConnectionClosedException e) {
                        // ignore exception thrown because of the aborting
                        if(!e.getMessage().contains("Premature end of Content-Length")) {
                            throw e;
                        }
                    }

                    return ImmutablePair.of(blockSize, HEADER_BLOCK_SIZE + (blockSize * indexBlockCount));
                } finally {
                    // always abort reading here inside the finally block of the InputStream as
                    // otherwise HttpClient tries to read the stream fully, which is at least 270GB...
                    httpGet.abort();
                }
            }
        }
    }

    public static String reverseDomain(String host) {
        if(StringUtils.isEmpty(host)) {
            return host;
        }

        String[] parts = host.split("\\.");
        Preconditions.checkState(parts.length > 0, "Should have some parts, but did not found any for %s", host);

        StringBuilder builder = new StringBuilder();
        for(int i = parts.length-1;i>= 0;i--) {
            builder.append(parts[i]).append(".");
        }

        // remove trailing dot
        builder.setLength(builder.length()-1);

        return builder.toString();
    }

    public static URI convertUrl(String urlStr) throws URISyntaxException {
        if(urlStr.endsWith(":http")) {
            urlStr = "http://" + StringUtils.removeEnd(urlStr, ":http");
        } else if (urlStr.endsWith(":https")) {
            urlStr = "https://" + StringUtils.removeEnd(urlStr, ":https");
        } else {
            urlStr = "http://" + urlStr;
        }

        // some URLs contain invalid characters, for now remove them although this
        // likely makes the URL invalid unless it is some query-parameter which is not
        // actually significant for the URL
        URI url = new URI(urlStr.replace("[", "").replace("]", ""));

        // convert the host of the URL from tld-first
        return new URI(url.getScheme(), url.getUserInfo(), Utils.reverseDomain(url.getHost()),
                url.getPort(), url.getPath(), url.getQuery(), url.getFragment());
    }

    public static void logProgress(long startPos, int blockSize, int skipBlocks, long startTs, long blockIndex, int logStep, long fileLength) {
        if(blockIndex % logStep == 0) {
            long diff = System.currentTimeMillis()-startTs;
            double diffSec = ((double)diff)/1000;
            long readBlocks = blockIndex - skipBlocks;
            long currentPos = startPos + (readBlocks * blockSize);

            log.info("Reading block " + blockIndex + " at position " + currentPos +
                    " fetched " + readBlocks + " blocks in " + String.format("%.2f", diffSec) + " s," +
                    " with " + String.format("%.2f", readBlocks/diffSec) + " per second" +
                    (fileLength > 0 ? ", " + String.format("%.2f", (((double)currentPos)/fileLength*100)) + "% of " + fileLength + " bytes done" : ""));
        }
    }


    public static boolean isCorruptDownload(File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            byte[] start = new byte[100];
            IOUtils.read(stream, start);
            String string = new String(start, StandardCharsets.UTF_8).trim().toLowerCase();
            if(string.startsWith("<!doctype html") ||
                    string.startsWith("<html") ||
                    string.startsWith("<!--[if ie")) {
                return true;
            }
        }
        return false;
    }

    public static File computeDownloadFileName(String urlStr, String postfix) {
        return computeDownloadFileName(DOWNLOAD_DIR, urlStr, postfix);
    }

    public static File computeDownloadFileName(File rootDir, String urlStr, String postfix) {
        // try to simply use the full url with only slashes replaced
    	String replace = StringUtils.removeStart(urlStr, "http://");
    	replace = StringUtils.removeStart(replace, "https://");
        replace = StringUtils.removeEnd(replace, ":http").
                replace("/", "_").replace("[", "(").
                replace("]", ")").replace("?", "_").
                replace(":", "_").replace("%",  "_").
                replace("+", "_").replace("*", "_");
        if(replace.length() > 240) {
            String extension = FilenameUtils.getExtension(replace);
            // don't use an extension that would make the overall filename length become more than 250 characters
            if(extension.length() > 10) {
            	extension = "";
            }
			replace = replace.substring(0, 240) + "..." + extension;
        }
        return new File(rootDir, replace.endsWith(postfix) ? replace : replace + postfix);
    }

	public static File downloadFileFromCommonCrawl(CloseableHttpClient httpClient, String url, DocumentLocation header, boolean useWARC)
			throws IOException {
		// check if we already have that file
        File destFile = Utils.computeDownloadFileName(url, MimeTypes.toExtension(header.getMime()));
        // also look in the directory where we store duplicates
        File backupFile = Utils.computeDownloadFileName(BACKUP_DIR, url, MimeTypes.toExtension(header.getMime()));
        if(destFile.exists() || backupFile.exists()) {
            log.info("File " + destFile + " already downloaded: " + destFile.exists() + "/" + backupFile.exists());
            return null;
        }

        try {
        	downloadFileFromCommonCrawl(httpClient, url, header, useWARC, destFile);
        } catch (IOException e) {
        	// retry once for HTTP 500 that we see sometimes
        	if(e.getMessage().contains("HTTP StatusCode 500")) {
        		downloadFileFromCommonCrawl(httpClient, url, header, useWARC, destFile);
        	} else {
        		throw e;
        	}
        }

        return destFile;
	}

	public static void downloadFileFromCommonCrawl(CloseableHttpClient httpClient, String url, DocumentLocation header, boolean useWARC,
			File destFile) throws IOException {
		log.info("Reading file for " + url + " at " + header.getRangeHeader() + " from " + header.getUrl() + " to " + destFile);

        // do a range-query for exactly this document in the Common Crawl dataset
        HttpGet httpGet = new HttpGet(header.getUrl());
        httpGet.addHeader("Range", header.getRangeHeader());
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = Utils.checkAndFetch(response, header.getUrl());

            try (InputStream stream = new GZIPInputStream(entity.getContent())) {
            	// we get differently formatted files depending on the version of CommonCrawl that we look at...
            	if(useWARC) {
					try (WARCRecord record = new WARCRecord(new FastBufferedInputStream(stream), destFile.getName(), 0)) {
						// use the parser to remove the HTTP headers
						LaxHttpParser.parseHeaders(record,"UTF-8");

    	                try {
    	                	FileUtils.copyInputStreamToFile(record, destFile);
    	                } catch (IllegalStateException e) {
    	                	throw new IOException(e);
    	                }
            		}
            	} else {
            		ArcRecord record = new ArcRecord();
	            	record.readFrom(stream);
	                try {
	                	FileUtils.copyInputStreamToFile(record.getHttpResponse().getEntity().getContent(), destFile);
	                } catch (IllegalStateException  | HttpException e) {
	                	throw new IOException(e);
	                }
            	}
            }
        }
	}

	public static void ensureDownloadDir() {
		if(!Utils.DOWNLOAD_DIR.exists()) {
            if(!Utils.DOWNLOAD_DIR.mkdirs()) {
                throw new IllegalStateException("Could not create directory " + Utils.DOWNLOAD_DIR);
            }
        }
	}
}
