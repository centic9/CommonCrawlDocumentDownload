/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.commons.codec.CharEncoding;

/**
 * dump data in hexadecimal format
 */
public class HexDump {
    public static final String EOL = System.getProperty("line.separator");
    public static final Charset UTF8 = Charset.forName(CharEncoding.UTF_8);

    private HexDump() {
        // all static methods, so no need for a public constructor
    }

    /**
     * dump an array of bytes to an OutputStream
     *
     * @param data the byte array to be dumped
     * @param offset its offset, whatever that might mean
     * @param stream the OutputStream to which the data is to be
     *               written
     * @param index initial index into the byte array
     * @param length number of characters to output
     *
     * @exception IOException is thrown if anything goes wrong writing
     *            the data to stream
     * @exception ArrayIndexOutOfBoundsException if the index is
     *            outside the data array's bounds
     * @exception IllegalArgumentException if the output stream is
     *            null
     */
    public static void dump(final byte [] data, final long offset,
        final OutputStream stream, final int index, final int length)
    throws IOException, ArrayIndexOutOfBoundsException, IllegalArgumentException {
        if (stream == null) {
            throw new IllegalArgumentException("cannot write to nullstream");
        }

        OutputStreamWriter osw = new OutputStreamWriter(stream, UTF8);
        osw.write(dump(data, offset, index, length));
        osw.flush();
    }

    /**
     * dump an array of bytes to an OutputStream
     *
     * @param data the byte array to be dumped
     * @param offset its offset, whatever that might mean
     * @param stream the OutputStream to which the data is to be
     *               written
     * @param index initial index into the byte array
     *
     * @exception IOException is thrown if anything goes wrong writing
     *            the data to stream
     * @exception ArrayIndexOutOfBoundsException if the index is
     *            outside the data array's bounds
     * @exception IllegalArgumentException if the output stream is
     *            null
     */

    public synchronized static void dump(final byte [] data, final long offset,
            final OutputStream stream, final int index)
    throws IOException, ArrayIndexOutOfBoundsException, IllegalArgumentException {
        dump(data, offset, stream, index, Integer.MAX_VALUE);
    }

    /**
     * dump an array of bytes to a String
     *
     * @param data the byte array to be dumped
     * @param offset its offset, whatever that might mean
     * @param index initial index into the byte array
     *
     * @exception ArrayIndexOutOfBoundsException if the index is
     *            outside the data array's bounds
     * @return output string
     */

    public static String dump(final byte [] data, final long offset, final int index) {
        return dump(data, offset, index, Integer.MAX_VALUE);
    }    
    
    /**
     * dump an array of bytes to a String
     *
     * @param data the byte array to be dumped
     * @param offset its offset, whatever that might mean
     * @param index initial index into the byte array
     * @param length number of characters to output
     *
     * @exception ArrayIndexOutOfBoundsException if the index is
     *            outside the data array's bounds
     * @return output string
     */

    public static String dump(final byte [] data, final long offset, final int index, final int length) {
        if (data == null || data.length == 0) {
            return "No Data"+EOL;
        }

        int data_length = (length == Integer.MAX_VALUE || length < 0 || index+length < 0)
            ? data.length
            : Math.min(data.length,index+length);
        
        
        if ((index < 0) || (index >= data.length)) {
            String err = "illegal index: "+index+" into array of length "+data.length;
            throw new ArrayIndexOutOfBoundsException(err);
        }
        
        long  display_offset = offset + index;
        StringBuilder buffer = new StringBuilder(74);
        
        for (int j = index; j < data_length; j += 16) {
            int chars_read = data_length - j;

            if (chars_read > 16) {
                chars_read = 16;
            }
            
            buffer.append(xpad(display_offset, 8, ""));
            for (int k = 0; k < 16; k++) {
                if (k < chars_read) {
                    buffer.append(xpad(data[ k + j ], 2, " "));
                } else {
                    buffer.append("   ");
                }
            }
            buffer.append(' ');
            for (int k = 0; k < chars_read; k++) {
                buffer.append(toAscii(data[ k + j ]));
            }
            buffer.append(EOL);
            display_offset += chars_read;
        }
        return buffer.toString();
    }

    public static char toAscii(int dataB) {
        char charB = (char)(dataB & 0xFF);
        if (Character.isISOControl(charB)) return '.';
        
        switch (charB) {
        // printable, but not compilable with current compiler encoding
        case 0xFF: case 0xDD:
            charB = '.';
            break;
        }
        return charB;
    }

    private static String xpad(long value, int pad, String prefix) {
        String sv = Long.toHexString(value).toUpperCase(Locale.ROOT);
        int len = sv.length();
        if ((pad == 0 || len == pad) && "".equals(prefix)) return sv;
        StringBuilder sb = new StringBuilder(prefix);
        if (len < pad) {
            sb.append("0000000000000000", 0, pad-len);
            sb.append(sv);
        } else if (len > pad) {
            sb.append(sv, len-pad, len);
        } else {
            sb.append(sv);
        }
        return sb.toString();
    }
}
