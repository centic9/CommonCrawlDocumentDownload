[![Build Status](https://github.com/centic9/CommonCrawlDocumentDownload/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/centic9/CommonCrawlDocumentDownload/actions)
[![Gradle Status](https://gradleupdate.appspot.com/centic9/CommonCrawlDocumentDownload/status.svg?branch=master)](https://gradleupdate.appspot.com/centic9/CommonCrawlDocumentDownload/status)
[![Release](https://img.shields.io/github/release/centic9/CommonCrawlDocumentDownload.svg)](https://github.com/centic9/CommonCrawlDocumentDownload/releases)
[![GitHub release](https://img.shields.io/github/release/centic9/CommonCrawlDocumentDownload.svg?label=changelog)](https://github.com/centic9/CommonCrawlDocumentDownload/releases/latest)
[![Tag](https://img.shields.io/github/tag/centic9/CommonCrawlDocumentDownload.svg)](https://github.com/centic9/CommonCrawlDocumentDownload/tags)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.dstadler/commoncrawldownload/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.dstadler/commoncrawldownload) 
[![Maven Central](https://img.shields.io/maven-central/v/org.dstadler/commoncrawldownload.svg)](https://maven-badges.herokuapp.com/maven-central/org.dstadler/commoncrawldownload)

This is a small tool to find matching URLs and download the corresponding binary data from the CommonCrawl indexes.

Support for the newer URL Index (http://blog.commoncrawl.org/2015/04/announcing-the-common-crawl-index/) is available, older URL Index as described at https://github.com/trivio/common_crawl_index and 
http://blog.commoncrawl.org/2013/01/common-crawl-url-index/ is still available in the "oldindex" package.

Please note that a full run usually finds a huge number of files and thus downloading will require a large 
amount of time and lots of disk-space if the data is stored locally!

**_NOTE_** This project does not implement backoff on HTTP errors about too many requests. Due to the current high rate of access by many GPT/LLM experiments, the CommonCrawl S3 bucket very often returns HTTP errors about rate exceeded. See https://github.com/tballison/commoncrawl-fetcher-lite for a newer implementation of this with more advanced functionality that work more reliably.

> **_NOTE:_**  CommonCrawl only stores up to 1MB per file and cuts off any bytes exceeding this length. So larger documents will be truncated and might not be valid and parsable any more. You can try to download the original file via the URL that is part of the crawl-data, but this project does not implement this due to potential "crawling" restrictions on target websites.

## Getting started

### Grab it

    git clone https://github.com/centic9/CommonCrawlDocumentDownload.git

### Build it and create the distribution files

    cd CommonCrawlDocumentDownload
    ./gradlew check

### Run it

#### Fetch a list of interesting documents

    ./gradlew lookupURLs
    
Reads the current Common Crawl URL index data and extracts all URLs for 
interesting mime-types or file extensions, stores the URLs in a file 
called `commoncrawl-CC-MAIN-<year>-<crawl>.txt`
        
#### Download documents

    ./gradlew downloadDocuments

Uses the URLs listed in `commoncrawl-CC-MAIN-<year>-<crawl>.txt` to 
download the documents from the Common Crawl

#### Deduplicate files

    ./gradlew deduplicate

Some files have equal content, this task will detect these based on file-size
and content-hash and move all duplicates to a backup-directory to leave only
unique files in place.

#### Deprecated: Download documents from the old-index

    ./gradlew downloadOldIndex

Starts downloading the URL index files from the old index and looks 
at each URL, downloading binary data from the common crawl archives.

## The longer stuff

### Change it

Run unit tests

    ./gradlew check jacocoTestReport

#### Adjust which files are found

There are a few things that you can tweak:

* The file-extensions that are detected as download-able files are handled in the class `Extensions`.
* The mime-types that are detected as download-able files isare handled in the class `MimeTypes`.
* Adjust the name of the list of found files in `DownloadURLIndex.COMMON_CRAWL_FILE`.
* Adjust the location where files are downloaded to in `Utils.DOWNLOAD_DIR`.
* The starting file-index (of the approximately 300 cdx-files) is currently set as constant 
in class `org.dstadler.commoncrawl.index.DownloadURLIndex`, this way you can also 
re-start a download that was interrupted before.

### Adjust which commoncrawl-index is fetched

CommonCrawl periodically runs crawls and publishes them. You can switch to newer crawls by 
adjusting the constant `CURRENT_CRAWL` in [DownloadURLIndex.java](src/main/java/org/dstadler/commoncrawl/index/DownloadURLIndex.java#L32) to the proper `<year>-<week>` 
number of the newer crawl. 

See [https://commoncrawl.org/connect/blog/](https://commoncrawl.org/blog) for announcemnts of the latest crawls.

### Ideas

* Old Index: By adding a new implementation of `BlockProcesser` (likely re-using existing stuff by deriving from one of the
available implementations), you can do things like streaming processing of the file instead of storing the file
locally, which will avoid using too much disk-space

### Estimates (based on Old Index)

* Size of overall URL Index is 233689120776, i.e. 217GB
* Header: 6 Bytes
* Index-Blocks: 2644
* Block-Size: 65536
* => Data-Blocks: 3563169
* Aprox. Files per Block: 2.421275
* Resulint aprox. number of files: 8627412
* Avg. size per file: 221613
* Needed storage: 1911954989425 bytes = 1.7TB!

### Related projects/pages

* http://commoncrawl.org/
* http://commoncrawl.org/the-data/examples/
* https://github.com/trivio/common_crawl_index
* https://github.com/wiseman/common_crawl_index
* https://github.com/ikreymer/cc-index-server
* https://github.com/ikreymer/webarchive-indexing
* https://github.com/ikreymer/cdx-index-client
* https://github.com/internetarchive/webarchive-commons
* https://github.com/iipc/webarchive-commons
* https://github.com/ikreymer/pywb
* http://decalage.info/download_mso_files

### Release it

    ./gradlew --console=plain release && ./gradlew closeAndReleaseRepository
    
* This should automatically release the new version on MavenCentral
* Afterwards go to the [Github releases page](https://github.com/centic9/commons-dost/releases) and add release-notes

## Support this project

If you find this library useful and would like to support it, you can [Sponsor the author](https://github.com/sponsors/centic9)

## Licensing

* CommonCrawlDocumentDownload is licensed under the [BSD 2-Clause License].

[BSD 2-Clause License]: https://www.opensource.org/licenses/bsd-license.php
