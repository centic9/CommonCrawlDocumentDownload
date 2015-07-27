package org.commoncrawl.hadoop.mapred;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.IOUtils;
// Apache HTTP Components classes
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.io.AbstractSessionInputBuffer;
import org.apache.http.impl.io.DefaultHttpResponseParser;
import org.apache.http.message.BasicLineParser;
import org.apache.http.params.BasicHttpParams;
// Hadoop classes
//import org.apache.hadoop.io.Writable;
// Apache log4j classes
import org.apache.log4j.Logger;
// Jsoup classes
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * An entry in an ARC (Internet Archive) data file.
 * 
 * Note: This file was taken from https://github.com/commoncrawl/commoncrawl-examples/blob/master/src/java/org/commoncrawl/hadoop/mapred/ArcRecord.java
 * Changes include removing dependency on Hadoop and using IOUtils.read() in order to read all data in all cases
 *
 * @author Chris Stephens
 */
@SuppressWarnings("deprecation")
public class ArcRecord
    //implements Writable
    {

  private static final Logger LOG = Logger.getLogger(ArcRecord.class);

  // ARC v1 metadata
  private String _url;
  private String _ipAddress;
  private Date   _archiveDate;
  private String _contentType;
  private int    _contentLength;

  // ARC v2 metadata
//private int    _resultCode;
//private String _checksum;
//private String _location;
//private long   _offset;
//private String _filename;

  private byte[] _payload;

  private HttpResponse _httpResponse;

  //private int _httpContentStart;

  /**
   * <p>Creates an empty ARC record.</p>
   */
  public ArcRecord() { }

  private void _clear() {
    this._url = null;
    this._ipAddress = null;
    this._archiveDate = null;
    this._contentType = null;
    this._contentLength = 0;
    this._payload = null;
    this._httpResponse = null;
  }

  private String _readLine(InputStream in)
      throws IOException, EOFException {

    StringBuffer line = new StringBuffer(128);

    // read a line of content
    int b = in.read();
    //int n = 1;

    // if -1 is returned, we are at EOF
    if (b == -1) {
        throw new EOFException();
    }

    // read until an NL
    do {

      if (((char) b) == '\n') {
        break;
    }

      line.append((char) b);

      b = in.read();
      //n++;
    }
    while (b != -1);

    return line.toString();
  }

  /**
   * <p>Parses the ARC record header and payload (content) from a stream.</p>
   *
   * @return TRUE if the ARC record was parsed and loaded successfully, FALSE if not.
   */
  public boolean readFrom(InputStream in)
      throws IOException, EOFException {

    if (in == null) {
      LOG.error("ArcRecord cannot be created from NULL/missing input stream.");
      return false;
    }

    // Clear any current values assigned to the object.
    this._clear();

    // ReadAndDownload the ARC header from the stream.
    String arcRecordHeader = this._readLine(in);

    try {
      this.setArcRecordHeader(arcRecordHeader);
      this.setPayload(in);
    }
    catch (IOException ex) {
      throw ex;
    }
    catch (Exception ex) {
      LOG.error("Exception thrown while parsing ARC record", ex);
      return false;
    }

    return true;
  }

  /**
   * <p>Parses and sets the ARC record header fields.</p>
   * <p>Currently, this method expects the ARC record header string to contain
   * the following fields, in order, separated by space:
   * <ul>
   * <li>URL</li>
   * <li>IP Address</li>
   * <li>Archive Date</li>
   * <li>Content Type</li>
   * <li>Content Length</li>
   * </ul>
   * </p>
   * <p>For more information on the arc file format, see
   * {@link http://www.archive.org/web/researcher/ArcFileFormat.php}.</p>
   *
   * @param arcRecordHeader The first line of an ARC file entry - the header
   *                        line for an ARC file item.
   */
  public void setArcRecordHeader(String arcRecordHeader)
      throws IllegalArgumentException, ParseException {

    if (arcRecordHeader == null || arcRecordHeader.equals("")) {
        throw new IllegalArgumentException("ARC v1 record header string is empty.");
    }

    String[] metadata = arcRecordHeader.split(" ");

    if (metadata.length != 5) {
      LOG.info(" [ "+arcRecordHeader+" ] ");
      throw new IllegalArgumentException("ARC v1 record header must be 5 fields.");
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

    this._url            =  metadata[0];
    this._ipAddress      =  metadata[1];
    this._archiveDate    =  format.parse(metadata[2]);
    this._contentType    =  metadata[3];
    this._contentLength  = (new Integer(metadata[4])).intValue();
  }

  /**
   * <p>Reads and sets the ARC record payload from an input stream.</p>
   *
   * @param in An input stream positioned at the start of the ARC record payload.
   */
  public void setPayload(InputStream in)
      throws IllegalArgumentException, ParseException, IOException {

    if (in == null) {
        throw new IllegalArgumentException("ArcRecord cannot be created from NULL/missing input stream.");
    }

    int bufferSize = this._contentLength;

    this._payload = new byte[bufferSize];

    int n = IOUtils.read(in, this._payload, 0, this._payload.length);

    if (n < this._payload.length) {
      LOG.warn("Expecting "+bufferSize+" bytes in ARC record payload, found "+n+" bytes.  Performing array copy.");
      this._payload = Arrays.copyOf(this._payload, n);
    }

    // After this, we should be at the end of this GZIP member.  Let the
    // calling function verify the position of the stream.
  }

  public void addToPayload(byte[] data) {
    this.addToPayload(data, data.length);
  }

  public void addToPayload(byte[] data, int length) {

    LOG.warn("Content Length must have been incorrect - someone needed to add more data to the payload.");

    if (this._payload == null) {
      this._payload = Arrays.copyOf(data, length);
    }
    else {
      int i = this._payload.length;
      int n = this._payload.length + length;

      // resize the payload buffer
      this._payload = Arrays.copyOf(this._payload, n);

      // copy in the additional data
      System.arraycopy(data, 0, this._payload, i, length);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
public String toString() {
    return this._url + " - " + this._archiveDate.toString() + " - " + this._contentType;
  }

  /**
   * {@inheritDoc}
   */
  public void write(DataOutput out)
      throws IOException {

    // write out ARC header info
    out.writeUTF(this._url);
    out.writeUTF(this._ipAddress);
    out.writeUTF(this._contentType);
    out.writeLong(this._archiveDate.getTime());
    out.writeInt(this._contentLength);

    // write out the payload
    out.writeInt(this._payload.length);
    out.write(this._payload, 0, this._payload.length);
  }

  /**
   * {@inheritDoc}
   */
  public void readFields(DataInput in)
      throws IOException {

    // read in ARC header info
    this._url           = in.readUTF();
    this._ipAddress     = in.readUTF();
    this._contentType   = in.readUTF();
    this._archiveDate   = new Date(in.readLong());
    this._contentLength = in.readInt();

    // read in the payload
    int payloadLength = in.readInt();

    // resize the payload buffer if necessary
    if (this._payload == null || this._payload.length != payloadLength) {
        this._payload = new byte[payloadLength];
    }

    try {
      in.readFully(this._payload, 0, payloadLength);
    }
    catch (EOFException ex) {
      throw new IOException("End of input reached before payload was fully deserialized.");
    }

    // assume that if a new payload was loaded, HTTP response will need to be reparsed.
    this._httpResponse = null;
  }

  /**
   * <p>Returns the full ARC record payload.  This is usually a complete HTTP
   * response.</p>
   *
   * @return The raw ARC record content.
   */
  public byte[] getPayload() {
    return this._payload;
  }

  /**
   * <p>Returns the URL from the ARC record header.</p>
   *
   * @return The URL for this entry.
   */
  public String getURL() {
    return this._url;
  }

  /**
   * <p>Returns the IP address from the ARC record header.</p>
   *
   * @return The IP address for this entry.
   */
  public String getIpAddress() {
    return this._ipAddress;
  }

  /**
   * <p>Returns the archive date from the ARC record header.</p>
   *
   * @return The archive date for this entry.
   */
  public Date getArchiveDate() {
    return this._archiveDate;
  }

  /**
   * <p>Returns the MIME content type from the ARC record header.</p>
   * <p>Note: The MIME content type in the ARC record header is not necessarily the
   * same as the <code>Content-Type</code> HTTP header inside the content body
   * (if one is present).</p>
   *
   * @return The MIME content type for this entry.
   */
  public String getContentType() {
    return this._contentType;
  }

  /**
   * <p>Returns the content length from the ARC record header.</p>
   * <p>Note: The content length in the ARC record header is not necessarily the
   * same as the <code>Content-Length</code> HTTP header inside the content body
   * (if one is present).</p>
   *
   * @return The content length for this entry.
   */
  public int getContentLength() {
    return this._contentLength;
  }

  /**
   * <p>Returns the HTTP status code.</p>
   * <p>If the payload could not be parsed as an HTTP response, returns -1.</p>
   * <p>Warning: if the payload has not yet been parsed as an HTTP response,
   * calling this function parses the full response.  Parsing is only performed
   * once - parsed data is retained for subsequent calls.</p>
   *
   * @return The HTTP status code.
   */
  public int getHttpStatusCode()
      throws IOException, HttpException {

    HttpResponse httpResponse = this.getHttpResponse();

    if (httpResponse == null) {
        return -1;
    }

    return httpResponse.getStatusLine().getStatusCode();
  }

  /**
   * <p>Returns an array of HTTP headers.</p>
   * <p>If the payload could not be parsed as an HTTP response, returns <code>null</code>.</p>
   * <p>Warning: if the payload has not yet been parsed as an HTTP response,
   * calling this function parses the full response.  Parsing is only performed
   * once - parsed data is retained for subsequent calls.</p>
   *
   * @return An array of HTTP headers.
   */
  public Header[] getHttpHeaders()
      throws IOException, HttpException {

    HttpResponse httpResponse = this.getHttpResponse();

    if (httpResponse == null) {
        return null;
    }

    return httpResponse.getAllHeaders();
  }

  /**
   *
   */
  public static class ByteArraySessionInputBuffer
      extends AbstractSessionInputBuffer {

    public ByteArraySessionInputBuffer(byte[] buf) {
      BasicHttpParams params = new BasicHttpParams();
      this.init(new ByteArrayInputStream(buf), 4096, params);
    }

    public ByteArraySessionInputBuffer(byte[] buf, int offset, int length) {
      BasicHttpParams params = new BasicHttpParams();
      this.init(new ByteArrayInputStream(buf, offset, length), 4096, params);
    }

    @Override
    public boolean isDataAvailable(int timeout) {
      return true;
    }
  }

  /**
   * <p>Helper function to search a byte array for CR-LF-CR-LF (the end of
   * HTTP headers in the payload buffer).</p>
   *
   * @return The offset of the end of HTTP headers, after the last CRLF.
   */
  private int _searchForCRLFCRLF(byte[] data) {

    final byte CR = (byte)'\r';
    final byte LF = (byte)'\n';

    int i;
    int s = 0;

    for (i = 0; i < data.length; i++) {

      if      (data[i] == CR) {
        if      (s == 0) {
            s = 1;
        } else if (s == 1) {
            s = 0;
        } else if (s == 2) {
            s = 3;
        } else if (s == 3) {
            s = 0;
        }
      }
      else if (data[i] == LF) {
        if      (s == 0) {
            s = 0;
        } else if (s == 1) {
            s = 2;
        } else if (s == 2) {
            s = 0;
        } else if (s == 3) {
            s = 4;
        }
      }
      else {
        s = 0;
      }

      if (s == 4) {
        return i + 1;
    }
    }

    return -1;
  }

  /**
   * <p>Returns an HTTP response object parsed from the ARC record payload.<p>
   * <p>Note: The payload is parsed on-demand, but is only parsed once.  The
   * parsed data is saved for subsequent calls.</p>
   *
   * @return The ARC record payload as an HTTP response object.  See the Apache
   * HttpComponents project.
   */
  public HttpResponse getHttpResponse()
      throws IOException, HttpException {

    if (this._httpResponse != null) {
        return this._httpResponse;
    }

    if (this._payload == null) {
      LOG.error("Unable to parse HTTP response: Payload has not been set"); return null;
    }

    if (this._url != null && !this._url.startsWith("http://") && !this._url.startsWith("https://")) {
      LOG.error("Unable to parse HTTP response: URL protocol is not HTTP"); return null;
    }

    this._httpResponse = null;

    // Find where the HTTP headers stop
    int end = this._searchForCRLFCRLF(this._payload);

    if (end == -1) {
      LOG.error("Unable to parse HTTP response: End of HTTP headers not found"); return null;
    }

    // Parse the HTTP status line and headers
    DefaultHttpResponseParser parser =
      new DefaultHttpResponseParser(
        new ByteArraySessionInputBuffer(this._payload, 0, end),
        new BasicLineParser(),
        new DefaultHttpResponseFactory(),
        new BasicHttpParams()
      );

    this._httpResponse = parser.parse();

    if (this._httpResponse == null) {
      LOG.error("Unable to parse HTTP response"); return null;
    }

    // Set the reset of the payload as the HTTP entity.  Use an InputStreamEntity
    // to avoid a memory copy.
    InputStreamEntity entity = new InputStreamEntity(new ByteArrayInputStream(this._payload, end, this._payload.length - end), this._payload.length - end);
    entity.setContentType(this._httpResponse.getFirstHeader("Content-Type"));
    entity.setContentEncoding(this._httpResponse.getFirstHeader("Content-Encoding"));
    this._httpResponse.setEntity(entity);

    return this._httpResponse;
  }

  /**
   * <p>Returns a Jsoup HTML document, parsed using the Charset in the
   * "Content-Type" header.  If the document charset cannot be found, parse is
   * attempted using </p>
   *
   * @return A Jsoup parsed HTML document from the HTTP response content.
   */
  public Document getParsedHTML()
      throws IOException {

    if (this._url == null) {
      LOG.error("Unable to parse HTML: URL from ARC header has not been set");
      return null;
    }

    // if response has not been parsed yet, this parses it
    try {
      this.getHttpResponse();
    }
    catch (HttpException ex) {
      LOG.error("Unable to parse HTML: Exception during HTTP response parsing"); return null;
    }

    if (this._httpResponse == null) {
      LOG.error("Unable to parse HTML: Exception during HTTP response parsing"); return null;
    }

    if (this._httpResponse.getEntity() == null) {
      LOG.error("Unable to parse HTML: No HTTP response entity found"); return null;
    }

    if (!this._contentType.toLowerCase().contains("html")) {
      LOG.warn("Unable to parse HTML: Content is not HTML"); return null;
    }

    String charset = null;

    try {
      // Default value returned is "text/plain" with charset of ISO-8859-1.
      charset = ContentType.getOrDefault(this._httpResponse.getEntity()).getCharset().name();
    }
    catch (Throwable ex) {
        // ignore exceptions here
    }

    // if anything goes wrong, try ISO-8859-1
    if (charset == null) {
        charset = "ISO-8859-1";
    }

    // parse the content using the derived charset and the URL from the ARC header
    return Jsoup.parse(this._httpResponse.getEntity().getContent(), charset, this._url);
  }
}

