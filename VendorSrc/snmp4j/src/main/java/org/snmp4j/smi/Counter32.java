/*_############################################################################
  _## 
  _##  SNMP4J - Counter32.java  
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
 * The <code>Counter32</code> class allows all the functionality of unsigned
 * integers but is recognized as a distinct SMI type, which is used for
 * monotonically increasing values that wrap around at 2^32-1 (4294967295).
 *
 * @author Frank Fock
 * @version 1.7
 * @since 1.0
 */
public class Counter32 extends UnsignedInteger32 {

  private static final long serialVersionUID = 6140742767439142144L;
  public static final long MAX_COUNTER32_VALUE = 4294967295L;

  public Counter32() {
  }

  public Counter32(long value) {
    super(value);
  }

  public boolean equals(Object o) {
    return (o instanceof Counter32) && (((Counter32) o).getValue() == getValue());
  }

  public int getSyntax() {
    return SMIConstants.SYNTAX_COUNTER32;
  }

  public void encodeBER(OutputStream outputStream) throws IOException {
    BER.encodeUnsignedInteger(outputStream, BER.COUNTER32, getValue());
  }

  public void decodeBER(BERInputStream inputStream) throws IOException {
    BER.MutableByte type = new BER.MutableByte();
    long newValue = BER.decodeUnsignedInteger(inputStream, type);
    if (type.getValue() != BER.COUNTER32) {
      throw new IOException("Wrong type encountered when decoding Counter: "+
                            type.getValue());
    }
    setValue(newValue);
  }

  public Object clone() {
    return new Counter32(value);
  }

  /**
   * Increment the value of the counter by one. If the current value is
   * 2^32-1 (4294967295) then value will be set to zero.
   */
  public void increment() {
    if (value < MAX_COUNTER32_VALUE) {
      value++;
    }
    else {
      value = 0;
    }
  }

  /**
   * Increment the value by more than one in one step.
   * @param increment
   *   an increment value greater than zero.
   * @return
   *   the current value of the counter.
   * @since 2.4.2
   */
  public long increment(long increment) {
    if (increment > 0) {
      if (value + increment < MAX_COUNTER32_VALUE) {
        value += increment;
      }
      else {
        value = increment - (MAX_COUNTER32_VALUE - value);
      }
    }
    else if (increment < 0) {
      throw new IllegalArgumentException("Negative increments not allowed for counters: "+increment);
    }
    return value;
  }

  public OID toSubIndex(boolean impliedLength) {
    throw new UnsupportedOperationException();
  }

  public void fromSubIndex(OID subIndex, boolean impliedLength) {
    throw new UnsupportedOperationException();
  }

}

