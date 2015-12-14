#!/bin/sh
#
#
# Merges multiple resulting text-files that resulted from downloading different URL-Indexes
# and removes any duplicates that were encounterd

wc commoncrawl-CC-MAIN-*
cat commoncrawl-CC-MAIN-* | sort -u > commoncrawl.txt
wc commoncrawl.txt
