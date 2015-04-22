package org.dstadler.commoncrawl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class ExtensionsTest {

    @Test
    public void testMatches() throws Exception {
        assertFalse(Extensions.matches(""));
        assertFalse(Extensions.matches("12345"));
        assertFalse(Extensions.matches("abc.ds"));
        assertFalse(Extensions.matches("abcds.xlsxa"));
        assertFalse(Extensions.matches("abcd.doc.1"));
        assertFalse(Extensions.matches("bdcs.tar.gz"));
        assertFalse(Extensions.matches("http://at.xls.at"));
        assertFalse(Extensions.matches("bdcsdoc"));
        assertFalse(Extensions.matches("bdcs_doc"));
        
        assertTrue(Extensions.matches("1.xls"));
        assertTrue(Extensions.matches(".xlsx"));
        assertTrue(Extensions.matches("a/b/c/d/s/%20/.ads.doc.sawe.dlaasd.pptx"));
    }

    // helper method to get coverage of the unused constructor
    @Test
    public void testPrivateConstructor() throws Exception {
        org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(Extensions.class);
    }
}
