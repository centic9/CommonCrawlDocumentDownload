[![Build Status](https://buildhive.cloudbees.com/job/centic9/job/CommonCrawlDocumentDownload/badge/icon)](https://buildhive.cloudbees.com/job/centic9/job/CommonCrawlDocumentDownload/)

This is a small tool to download binary data from the CommonCrawl indexes.

Currently only the URL Index as described at https://github.com/trivio/common_crawl_index and 
http://blog.commoncrawl.org/2013/01/common-crawl-url-index/ is supported. 

We are evaluating support for the newer URL Index announced at http://blog.commoncrawl.org/2015/04/announcing-the-common-crawl-index/
but we did not yet find a way to search for extensions instead of parts of the url in this new index.

Please note that a full run usually finds a huge number of files and thus downloading will require a large amount 
of time and lots of disk-space if the data is stored locally!

## Getting started

### Grab it

    git clone git://github.com/centic9/CommonCrawl

### Build it and create the distribution files

	cd CommonCrawl
	./gradlew check

### Run it

    ./gradlew download

Starts downloading files found in the URL Index that have matching extensions.

## The longer stuff

### Change it

Create matching Eclipse project files

	./gradlew eclipse

Run unit tests

	./gradlew check jacocoTestReport

#### Adjust which files are found

There are two things that you can tweak:

* The file-extensions that are detcted as download-able files is handled in the class Extensions
* The starting Block-Number is currently set as constant in class org.dstadler.commoncrawl.ReadAndDownload, this way
  you can also re-start a download that was interrupted before

### Ideas

* By adding a new implementation of BlockProcesser (likely re-using existing stuff by deriving from one of the
available implementations), you can do things like streaming processing of the file instead of storing the file
locally, which will avoid using too much disk-space

### Supporting the new URL Index

There was a new index announced at http://blog.commoncrawl.org/2015/04/announcing-the-common-crawl-index/, in order to 
support using it, we will need to review how https://github.com/ikreymer/cc-index-server downloads the binary tree of
urls so we can use it to look for URLs by extensions.

### Related projects

* https://github.com/jvtm/pyambit 

### Licensing

* CommonCrawlDocumentDownload is licensed under the [BSD 2-Clause License].

[BSD 2-Clause License]: http://www.opensource.org/licenses/bsd-license.php
