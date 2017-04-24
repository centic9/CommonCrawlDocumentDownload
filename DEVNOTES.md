Some assorted notes taken while working on this project.

### Supporting the new URL Index

The new index announced at http://blog.commoncrawl.org/2015/04/announcing-the-common-crawl-index/ is supported now

Indexes are available at s3://commoncrawl/cc-index/collections/[CC-MAIN-YYYY-WW], 
e.g. https://commoncrawl.s3.amazonaws.com/cc-index/collections/CC-MAIN-2015-11/metadata.yaml

Indexing is done via https://github.com/ikreymer/webarchive-indexing

CDX file format is described at https://github.com/ikreymer/webarchive-indexing#cdx-file-format, also take a look at https://github.com/ikreymer/cc-index-server and https://github.com/ikreymer/cdx-index-client for a sample client.

#### Reading the data

For example, if you have the query result from http://index.commoncrawl.org/CC-MAIN-2015-11-index?url=commoncrawl.org&output=json&limit=1

    {"urlkey": "org,commoncrawl)/", "timestamp": "20150302032705", "url": "http://commoncrawl.org/", "length": "2526", "filename": "crawl-data/CC-MAIN-2015-11/segments/1424936462700.28/warc/CC-MAIN-20150226074102-00159-ip-10-28-5-156.ec2.internal.warc.gz", "digest": "QE4UUUWUJWEZBBK6PUG3CHFAGEKDMDBZ", "offset": "53235662"}

You can access the original resource via this url, using curl or wget:

    curl http://index.commoncrawl.org/CC-MAIN-2015-11/20150302032705id_/http://commoncrawl.org/

    wget http://index.commoncrawl.org/CC-MAIN-2015-11/20150302032705id_/http://commoncrawl.org/

Note the format here is: /CC-MAIN-2015-11/ + the timestamp + id_ + / url

Please note that this capability is part of the pywb replay software, and may change in the future for CommonCrawl. It's not guaranteed to work in all cases..

This replay serves the original response http headers as well, which may not be consistent with content and may not always work in the browser.

#### Mailing list snippets

##### Read CDX Index

Actually it looks like awscli knows how to output to stdout now too.  Here's a one-liner which will generate a pseudo-random ~1% sample of the first shard in the March 2015 index.  It runs in under 3 minutes on my laptop (using ~25 Mb/s download bandwidth).

 time aws s3 cp --no-sign-request s3://commoncrawl/cc-index/collections/CC-MAIN-2015-14/indexes/cdx-00000.gz - | gunzip | cut -f 4 -d '"' | awk 'BEGIN {srand()} !/^$/ { if (rand() <= .01) print $0}' | gzip > cdx-00000-url-1pct.txt.gz

You can adjust this by changing the probability threshold from .01 to something else or by tossing a grep stage into the pipe to filter on URLs matching certain patterns.

Somewhat surprisingly it is only twice as fast (1.5 minutes) when running on a EC2 c4.large instance where CPU time for the cut command dominates (but, of course, you can run them in parallel there to take advantage of the extra bandwidth).

##### Python code

I wrote this the other day before I learned that Ilya had added a bunch of powerful filters to the Common Crawl Index web service, but it might still be a useful starting point for someone who wants to do bulk analysis of the CDX files that make up the Common Crawl index in ways that aren't supported by the web service.

https://gist.github.com/tfmorris/ab89ed13e2e52830aa6c

It's designed to be self-contained and simple to run on any laptop without an AWS account, rather than being super high performance, but if you run it on a c4.large EC2 instance, it can chug through the index in about 6 hours at a total cost of  less than $0.10 at current spot prices.

For higher performance it could be converted to an EMR job.  If you only wanted to analyze certain TLDs or PLDs, you could extend it to make use of the information in cluster.idx to figure out which index shards to process.  This would be particularly useful if you weren't interested in the .com TLD which makes up 75% of the URLs.

##### Filter by mimetype

It sounds like you were using file extension, rather than the content type to determine the type of the target document.  That's going to miss URLs like this (from one of the new indexes):

cdx-00000-urls.gz:http://www.izha.edu.al/index.php?view=article&catid=38%3Abiblioteka-elektronike&id=114%3Aprograme-klasa-10&format=pdf&option=com_content&Itemid=18
cdx-00000-urls.gz:http://www.akbn.gov.al/index.php/sq/hidrokarburet/kuadri-ligjor?format=pdf
cdx-00000-urls.gz:http://www.nationalfilmcenter.gov.al/index.php?view=article&catid=42%3Arreth-nesh-&id=107%3Aorganizimi-&format=pdf&option=com_content&Itemid=109

If you're happy just looking for patterns in the URL, whether it be .pdf or format=pdf, and don't care about URLs where format=1 means PDF, you can adapt the technique that I just posted in another thread to process the CDX files.

Also, the new index (CC-MAIN-2015-14) now has mime type and status as field in the json block.

This allows you to filter like this:
http://index.commoncrawl.org/CC-MAIN-2015-14-index?url=en.wikipedia.org/*&filter=mime:text/html
(More details of the filter param: https://github.com/ikreymer/pywb/wiki/CDX-Server-API#filter)

which should filter out results that had a content type of text/html only. This has been added starting with the 2015-14 index.

###### Read binary data

Those fields are 'offset' and 'length' in the current index, and correspond to the WARC offset and compressed length that you would use as part of the range request.

{"urlkey": "org,commoncrawl)/", "timestamp": "20150302032705", "url": "http://commoncrawl.org/", "length": "2526", "filename": "crawl-data/CC-MAIN-2015-11/segments/1424936462700.28/warc/CC-MAIN-20150226074102-00159-ip-10-28-5-156.ec2.internal.warc.gz", "digest": "QE4UUUWUJWEZBBK6PUG3CHFAGEKDMDBZ", "offset": "53235662"}


53238187=53235662+2526-1

You could then do:

curl -r 53235662-53238187 https://commoncrawl.s3.amazonaws.com/crawl-data/CC-MAIN-2015-11/segments/1424936462700.28/warc/CC-MAIN-20150226074102-00159-ip-10-28-5-156.ec2.internal.warc.gz | zcat | less

to get the full WARC record.

There's not yet a UI for the query api, just the raw JSON result output.
