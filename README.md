[![Build Status](https://buildhive.cloudbees.com/job/centic9/job/CommonCrawl/badge/icon)](https://buildhive.cloudbees.com/job/centic9/job/CommonCrawl/)

Small tool to download data from CommonCrawl indexes, currently only the URL Index is supported. Please note that 
a full download usually finds a huge number of files and thus downloading will require a large amount of disk space!

## Getting started

#### Grab it

    git clone git://github.com/centic9/CommonCrawl

#### Build it and create the distribution files

	cd CommonCrawl
	./gradlew check

#### Run it

    ./gradlew download

Starts downloading files found in the URL Index that have matching extensions.

## The longer stuff

#### Details

#### Adjust which files are found

There are two things that you can tweak:

* The file-extensions that are detcted as download-able files is handled in the class Extensions
* The starting Block-Number is currently set as constant in class org.dstadler.commoncrawl.ReadAndDownload, this way
  you can also re-start a download that was interrupted before

#### Change it

Create matching Eclipse project files

	./gradlew eclipse

Run unit tests

	./gradlew check jacocoTestReport

#### Related projects

* https://github.com/jvtm/pyambit 

#### Licensing

* CommonCrawl is licensed under the [BSD 2-Clause License].

[BSD 2-Clause License]: http://www.opensource.org/licenses/bsd-license.php
