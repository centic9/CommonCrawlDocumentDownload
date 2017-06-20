package org.dstadler.commoncrawl.index;

import java.io.IOException;

import org.dstadler.commoncrawl.DocumentLocation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Class which holds information about one item from the CDX-index.
 *
 * @author dstadler
 *
 */
public class CDXItem {
	private static final JsonFactory f = new JsonFactory();

	public String url;
	public String mime;
	public String status;
	public String digest;
	public long length;
	public long offset;
	public String filename;

	@SuppressWarnings("StatementWithEmptyBody")
	public static CDXItem parse(String json) throws IOException {
		/*
{"url": "http://www.malthus.com.br/rw/forense/o_alcoolismo_e_a_lei.ppt", "mime": "application/vnd.ms-powerpoint", "status": "200",
"digest": "PRKAHBCWKV2357EMC4H5Q2I56SSL34KB", "length": "474522", "offset": "548823139",
"filename": "crawl-data/CC-MAIN-2015-35/segments/1440645293619.80/warc/CC-MAIN-20150827031453-00044-ip-10-171-96-226.ec2.internal.warc.gz"}		 */
		CDXItem item = new CDXItem();

    	try (JsonParser jp = f.createParser(json)) {
	    	while(jp.nextToken() != JsonToken.END_OBJECT) {
	    		if(jp.getCurrentToken() == JsonToken.VALUE_STRING) {
	    			String name = jp.getCurrentName();
					if("url".equals(name)) {
	    				item.url = jp.getValueAsString().toLowerCase();
	    			} else if ("mime".equals(name)) {
	    				item.mime = jp.getValueAsString().toLowerCase();
	    			} else if ("status".equals(name)) {
	    				item.status = jp.getValueAsString();
	    			} else if ("digest".equals(name)) {
	    				item.digest = jp.getValueAsString();
	    			} else if ("length".equals(name)) {
	    				item.length = jp.getValueAsLong();
	    			} else if ("offset".equals(name)) {
	    				item.offset = jp.getValueAsLong();
	    			} else if ("filename".equals(name)) {
	    				item.filename = jp.getValueAsString();
					} else if ("mime-detected".equals(name)) {
						// ignored for now
	    			} else {
	    				throw new IllegalStateException("Unknown field found: " + name);
	    			}
	    		}
	    	}
    	}

    	return item;
	}

	public DocumentLocation getDocumentLocation() {
		DocumentLocation location = new DocumentLocation();
		location.arcFileOffset = offset;
		location.arcFileSize = length;
		location.filename = filename;
		location.mime = mime;

		return location;
	}
}
