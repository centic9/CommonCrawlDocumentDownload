package org.dstadler.commoncrawl;

import com.google.common.collect.TreeMultimap;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.dstadler.commoncrawl.Utils.BACKUP_DIR;
import static org.dstadler.commoncrawl.Utils.DOWNLOAD_DIR;

/**
 * Find duplicates in the test-corpus and copy them to a
 * backup-directory to reduce the run-time of the regression-tests
 * by not testing duplicate files multiple times.
 */
public class Deduplicate {
    private final static Logger log = LoggerFactory.make();

    private final static String[] SCAN_EXCLUDES = new String[] {
            "**/.svn/**",
            "lost+found",
            "**/.git/**"
    };


    public static void main(String[] args) throws IOException {
        LoggerFactory.initLogging();

        log.info("Scanning for files in " + DOWNLOAD_DIR);
        String[] files = scanForFiles();
        log.info("Handling " + files.length + " files");

        TreeMultimap<Long, String> sizes = readFileSizes(files);

        //log.info("Having files with 2 bytes: " + sizes.get(2L));
        NavigableSet<Long> sizesKeys = sizes.keySet();
        log.info("Having " + sizesKeys.size() + " different sizes between " + sizesKeys.first() + " and " + sizesKeys.last());

        int duplicates = 0;
        int count = 0;
        for (Long sizesKey : sizesKeys) {
            NavigableSet<String> sizeFiles = sizes.get(sizesKey);
            if (sizeFiles.size() <= 1 /*||
                    // used to not start at the beginning when continuing a previous run that stopped for some reason
                    sizesKey <= 524037*/) {
                log.info("Only having " + sizeFiles.size() + " files with size " + sizesKey + ", " + (sizes.size() - count) + " files left");
                count += sizeFiles.size();
                continue;
            }
            log.info("Looking at " + sizeFiles.size() + " files with size " + sizesKey + ", " + (sizes.size() - count) + " files left");

            Map<String, String> hashes = new HashMap<>();
            for (String file : sizeFiles) {
                count++;
                try {
                    String hash = hash(new File(DOWNLOAD_DIR, file));
                    if (hashes.containsKey(hash)) {
                        duplicates++;
                        log.info(duplicates + "/" + count + "/" + sizesKey + ": File " + file + " is the same as " + hashes.get(hash));

                        FileUtils.moveFile(new File(DOWNLOAD_DIR, file), new File(BACKUP_DIR, file));
                    }

                    hashes.put(hash, file);
                } catch (FileNotFoundException e) {
                    log.log(Level.WARNING, "Could not read file '" + new File(DOWNLOAD_DIR, file).getAbsolutePath() +
                            "' for size " + sizesKey + ", probably the filename contains unexpected characters", e);
                } catch (IOException | RuntimeException e) {
                    throw new IOException("Failed for file '" + new File(DOWNLOAD_DIR, file).getAbsolutePath() +
                            "' for size " + sizesKey, e);
                }
            }
        }
        log.info("Found " + duplicates + " duplicate files");
    }

    private static String[] scanForFiles() {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(DOWNLOAD_DIR);
        scanner.setExcludes(SCAN_EXCLUDES);
        scanner.scan();
        return scanner.getIncludedFiles();
    }

    private static TreeMultimap<Long, String> readFileSizes(String[] files) {
        TreeMultimap<Long, String> sizes = TreeMultimap.create();
        for (String fileName : files) {
            sizes.put(new File(DOWNLOAD_DIR, fileName).length(), fileName);
        }
        return sizes;
    }

    private static String hash(File file) throws IOException {
        // buffer up to one MB per file to speed up hashing
        final int buf;
        if(file.length() > 1024*1024) {
            buf = 1024 * 1024;
        } else if (file.length() == 0) {
            buf = 1024;
        } else {
            buf = (int)file.length();
        }

        try (InputStream fis = new BufferedInputStream(new FileInputStream(file), buf)) {
            return org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
        }
    }
}
