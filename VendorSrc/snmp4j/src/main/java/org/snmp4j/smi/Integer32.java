/*_############################################################################
  _## 
  _##  SNMP4J - Integer32.java  
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
package org.snmp4j.smi;

import java.io.*;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;

/**
 * The <code>Integer32</code> represents 32bit signed integer values for SNMP.
 *
 * @author Frank Fock
 * @version 1.8
 */
public class Integer32 extends AbstractVariable
    implements AssignableFromInteger, AssignableFromString {

  private static final long serialVersionUID = 5046132399890132416L;

  private int value = 0;

  /**
   * Creates an <code>Integer32</code> with a zero value.
   */
  public Integer32() {
  }

  /**
   * Creates an <code>Integer32</code> variable with the supplied value.
   * @param value
   *    an integer value.
   */
  public Integer32(int value) {
    setValue(value);
  }

  public void encodeBER(OutputStream outputStream) throws java.io.IOException {
    BER.encodeInteger(outputStream, BER.INTEGER, value);
  }

  public void decodeBER(BERInputStream inputStream) throws java.io.IOException {
    BER.MutableByte type = new BER.MutableByte();
    int newValue = BER.decodeInteger(inputStream, type);
    if (type.getValue() != BER.INTEGER) {
      throw new IOException("Wrong type encountered when decoding Counter: "+type.getValue());
    }
    setValue(newValue);
  }

  public int getSyntax() {
    return SMIConstants.SYNTAX_INTEGER;
  }

  public int hashCode() {
    return value;
  }

  public int getBERLength() {
    if ((value <   0x80) &&
        (value >= -0x80)) {
      return 3;
    }
    else if ((value <   0x8000) &&
             (value >= -0x8000)) {
      return 4;
    }
    else if ((value <   0x800000) &&
             (value >= -0x800000)) {
      return 5;
    }
    return 6;
  }

  public boolean equals(Object o) {
    return (o instanceof Integer32) && (((Integer32) o).value == value);
  }

  public int compareTo(Variable o) {
    return value - ((Integer32)o).value;
  }

  public String toString() {
    return Integer.toString(value);
  }

  public final void setValue(String value) {
    this.value = Integer.parseInt(value);
  }

  /**
   * Sets the value of this integer.
   * @param value
   *    an integer value.
   */
  public final void setValue(int value) {
    this.value = value;
  }

  /**
   * Gets the value.
   * @return
   *    an integer.
   */
  public final int getValue() {
    return value;
  }

  public Object clone() {
    return new Integer32(value);
  }

  public final int toInt() {
    return getValue();
  }

  public final long toLong() {
    return getValue();
  }

  public OID toSubIndex(boolean impliedLength) {
    return new OID(new int[] { value });
  }

  public void fromSubIndex(OID subIndex, boolean impliedLength) {
    setValue(subIndex.get(0));
  }

}

