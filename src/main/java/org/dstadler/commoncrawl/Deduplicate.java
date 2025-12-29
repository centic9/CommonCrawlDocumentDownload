package org.dstadler.commoncrawl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
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

    private final static Set<String> SCAN_EXCLUDES = ImmutableSet.of(
            "**/.svn/**",
            "lost+found",
            "**/.git/**"
    );

    public static void main(String[] args) throws IOException {
        LoggerFactory.initLogging();

        // iterate all files and sort them into buckets of equal size
        TreeMultimap<Long, String> sizes = scanAndSortFiles();

        NavigableSet<Long> sizesKeys = sizes.keySet();
        log.info("Having " + sizesKeys.size() + " different sizes between " + sizesKeys.getFirst() + " and " + sizesKeys.getLast());

        int duplicates = 0;
        int count = 0;

        // compare hashes for each size-bucket
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
                        log.info("Dups: " + duplicates + ", Count: " + count + ", SizeKey: " + sizesKey +
                                ": File " + file + " is the same as " + hashes.get(hash));

                        FileUtils.moveFile(new File(DOWNLOAD_DIR, file), new File(BACKUP_DIR, file));
                    } else {
                        hashes.put(hash, file);
                    }
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

    private static TreeMultimap<Long, String> scanAndSortFiles() throws IOException {
        log.info("Scanning for files in " + DOWNLOAD_DIR);

        AtomicLong count = new AtomicLong();
        final TreeMultimap<Long, String> sizes = TreeMultimap.create();
        Files.walkFileTree(DOWNLOAD_DIR.toPath(), EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                Integer.MAX_VALUE, new SimpleFileVisitor<>() {
                    @Nonnull
                    @Override
                    public FileVisitResult preVisitDirectory(@Nonnull Path dir, @Nonnull BasicFileAttributes attrs) {
                        if (SCAN_EXCLUDES.contains(dir.toFile().getName())) {
                            log.info("Skipping directory " + dir);
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        log.info("Entering directory " + dir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Nonnull
                    @Override
                    public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) {
                        long current = count.getAndIncrement();
                        if (current % 10000 == 0) {
                            log.info("Handling file " + current + ": " + file);
                        }

                        sizes.put(file.toFile().length(),
                                Strings.CS.removeStart(file.toFile().toString(), DOWNLOAD_DIR.toString() + "/"));

                        return FileVisitResult.CONTINUE;
                    }
                });

        log.info("Found " + sizes.values().size() + " files");

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
