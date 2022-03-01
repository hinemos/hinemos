/*_############################################################################
  _## 
  _##  SNMP4J - ByteArrayWindow.java  
  _## 
  _##  Copyright (C) 2003-2020  Frank Fock and Jochen Katz (SNMP4J.org)
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
package org.snmp4j.security;

/**
 * The <code>ByteArrayWindow</code> provides windowed access to a subarray
 * of a byte array.
 * @author Frank Fock
 * @version 1.0
 */
public class ByteArrayWindow {

  private byte[] value;
  private int offset;
  private int length;

  /**
   * Creates a byte array window that provides access to the bytes in the
   * supplied array between the position starting at the supplied offset.
   * @param value
   *    the underlying byte array.
   * @param offset
   *    the starting position of the created window.
   * @param length
   *    the length of the window.
   */
  public ByteArrayWindow(byte[] value, int offset, int length) {
    this.value = value;
    this.offset = offset;
    this.length = length;
  }

  public byte[] getValue() {
    return value;
  }

  public void setValue(byte[] value) {
    this.value = value;
  }

  public int getOffset() {
    return offset;
  }

  public void set(int i, byte b) {
    if (i >= length) {
      throw new IndexOutOfBoundsException("" + i + " >= " + length);
    }
    if (i < 0) {
      throw new IndexOutOfBoundsException("" + i);
    }
    this.value[i+offset] = b;
  }

  public byte get(int i) {
    if (i >= length) {
      throw new IndexOutOfBoundsException("" + i + " >= " + length);
    }
    if (i < 0) {
      throw new IndexOutOfBoundsException("" + i);
    }
    return value[i+offset];
  }

  public int getLength() {
    return length;
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   *
   * @param obj the reference object with which to compare.
   * @return <code>true</code> if this object is the same as the obj argument;
   *   <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    if (obj instanceof ByteArrayWindow) {
      ByteArrayWindow other = (ByteArrayWindow) obj;
      if (other.length != length) {
        return false;
      }
      for (int i=0; i<length; i++) {
        if (other.value[i] != value[i]) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  public boolean equals(ByteArrayWindow other, int maxBytesToCompare) {
    if ((other.length < maxBytesToCompare) ||
        (length < maxBytesToCompare)) {
      return false;
    }
    for (int i=0; i<maxBytesToCompare; i++) {
      if (value[offset+i] != other.value[other.offset+i]) {
        return false;
      }
    }
    return true;
  }
}
