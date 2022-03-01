/*_############################################################################
  _## 
  _##  SNMP4J - BEROutputStream.java  
  _## 
  _##  Copyright (C) 2003-2020  Frank Fock (SNMP4J.org)
  _##  
  _##  Licensed under the Apache License, Version 2.0 (the "License");
  _##  you may not use this file except in compliance with the License.
  _##  You may obtain a copy of the License at
  _##  
  _##      http://www.apache.org/licenses/LICENSE-2.0
  _##  
  _##  Unless required by applicable law or agreed to in writing, software
  _##  distributed under the License is distributed on an "AS IS" BASIS,
  _##  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  _##  See the License for the specific language governing permissions and
  _##  limitations under the License.
  _##  
  _##########################################################################*/
package org.snmp4j.asn1;

import java.io.*;
import java.nio.ByteBuffer;


/**
 * The <code>BEROutputStream</code> class wraps a <code>ByteBuffer</code>
 * to support BER encoding. The backing buffer can be accessed directly to
 * optimize performance and memory usage.
 *
 * @author Frank Fock
 * @version 1.0
 */
public class BEROutputStream extends OutputStream {

  private ByteBuffer buffer;
  private int offset = 0;

  /**
   * Creates a BER output stream without a backing buffer set. In order to
   * be able to write anything to the stream,
   * {@link #setBuffer(ByteBuffer buffer)} has to be
   * called before. Otherwise a <code>NullPointerException</code> will be
   * thrown when calling one of the <code>write</code> operations.
   */
  public BEROutputStream() {
    this.buffer = null;
  }

  /**
   * Create a <code>BEROutputStream</code> that uses the supplied buffer
   * as backing buffer.
   * @param buffer
   *    a <code>ByteBuffer</code> whose limit and capacity must be greater or
   *    equal that the length of the encoded BER stream.
   */
  public BEROutputStream(ByteBuffer buffer) {
    this.buffer = buffer;
    this.offset = buffer.position();
  }

  public void write(int b) throws java.io.IOException {
    buffer.put((byte)b);
  }

  public void write(byte[] b) throws IOException {
    buffer.put(b);
  }

  public void write(byte[] b, int off, int len) throws IOException {
      buffer.put(b, off, len);
  }

  public void close() throws IOException {
  }

  public void flush() throws IOException {
  }

  /**
   * Rewinds backing buffer and returns it. In contrast to the backing buffer's
   * rewind method, this method sets the position of the buffer to the first
   * byte of this output stream rather than to the first byte of the underlying
   * byte array!
   * @return
   *    the ByteBuffer backing this output stream with its current position
   *    set to the begin of the output stream.
   */
  public ByteBuffer rewind() {
    return (ByteBuffer) buffer.position(offset);
  }

  /**
   * Gets the backing buffer.
   * @return
   *    the <code>ByteBuffer</code> backing this output stream.
   */
  public ByteBuffer getBuffer() {
    return buffer;
  }

  /**
   * Sets the backing buffer to the supplied one and sets the offset used by
   * {@link #rewind()} to the buffers current position.
   * @param buffer
   *    a <code>ByteBuffer</code> whose limit and capacity must be greater or
   *    equal that the length of the encoded BER stream.
   */
  public void setBuffer(ByteBuffer buffer) {
    this.buffer = buffer;
    this.offset = buffer.position();
  }

  /**
   * Sets the backing buffer and sets the current position of the stream to
   * the buffers limit (end).
   * @param buffer
   *    a <code>ByteBuffer</code> whose limit and capacity must be greater or
   *    equal that the length of the encoded BER stream.
   */
  public void setFilledBuffer(ByteBuffer buffer) {
    this.buffer = buffer;
    this.offset = buffer.position();
    buffer.position(buffer.limit());
  }

}
