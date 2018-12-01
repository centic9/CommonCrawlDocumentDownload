package org.dstadler.commoncrawl;

import org.dstadler.poi.util.LittleEndian;


/**
 * Helper class to store the position information of a URL 
 * in the URL index which allows to download the file from 
 * the global Common Crawl data.
 *
 * @author dominik.stadler
 */
public class DocumentLocation {
    public long segmentId;
    
    public String filename;
    
    // the next 8 bytes represents the ARC file creation date, 
    public long arcCreationDate;

    
    // followed by 4 bytes that represent the ARC file partition, 
    public long arcFilePartition;

    
    // followed by 8 bytes that represent the offset within the ARC file 
    public long arcFileOffset;

    // and then finally the last 4 bytes represent the size of compressed data stored inside the ARC file.
    public long arcFileSize;

    public String mime;
    
    public String getUrl() {
    	if(filename != null) {
            return "https://commoncrawl.s3.amazonaws.com/" + filename;
    	}
        return "https://commoncrawl.s3.amazonaws.com/parse-output/segment/" + 
                segmentId + "/" + arcCreationDate + "_" + arcFilePartition + ".arc.gz";
    }

    public String getRangeHeader() {
        return "bytes=" + arcFileOffset + "-" + (arcFileOffset+arcFileSize-1);
    }
    

    public static DocumentLocation readFromOldIndexBlock(byte[] block, int index) {
//        System.out.println(HexDump.dump(Arrays.copyOfRange(block, index-8, index), 0, 0));
//        System.out.println(HexDump.dump(Arrays.copyOfRange(block, index, index+8), 0, 0));
//        System.out.println(HexDump.dump(Arrays.copyOfRange(block, index+8, index+16), 0, 0));
//        System.out.println(HexDump.dump(Arrays.copyOfRange(block, index+16, index+20), 0, 0));
//        System.out.println(HexDump.dump(Arrays.copyOfRange(block, index+20, index+28), 0, 0));
//        System.out.println(HexDump.dump(Arrays.copyOfRange(block, index+28, index+32), 0, 0));

        DocumentLocation header = new DocumentLocation();
        
        header.segmentId = LittleEndian.getLong(block, index);
        
        int offset = 8;
        header.arcCreationDate = LittleEndian.getLong(block, index+offset);
        offset += 8;
        
        header.arcFilePartition = LittleEndian.getUInt(block, index+offset);
        offset += 4;
        
        header.arcFileOffset = LittleEndian.getLong(block, index+offset);
        offset += 8;
        
        header.arcFileSize = LittleEndian.getUInt(block, index+offset);
        
        //System.out.println(segmentId + "/" + arcCreationDate + "/" + arcFilePartition + "/" + arcFileOffset + "/" + arcFileSize);
        
        return header;
    }

	public String getMime() {
		return mime;
	}
}
