#!/bin/sh

sudo mkdir -p /usbb/commoncrawl && \
sudo chmod a+w /usbb/commoncrawl && \
wget -O /usbb/commoncrawl/index.data -v -c --limit-rate=1000k --progress=bar \
	https://commoncrawl.s3.amazonaws.com/projects/url-index/url-index.1356128792
