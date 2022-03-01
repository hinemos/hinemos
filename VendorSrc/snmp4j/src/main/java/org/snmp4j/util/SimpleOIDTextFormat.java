/*_############################################################################
  _## 
  _##  SNMP4J - SimpleOIDTextFormat.java  
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
package org.snmp4j.util;

import java.text.*;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.OID;
import java.util.StringTokenizer;

/**
 * The <code>SimpleOIDTextFormat</code> implements a simple textual
 * representation for object IDs as dotted string.
 *
 * @author Frank Fock
 * @version 2.2
 * @since 1.10
 */
public class SimpleOIDTextFormat implements OIDTextFormat {

  /**
   * Creates a simple OID text format.
   */
  public SimpleOIDTextFormat() {
  }

  /**
   * Returns a textual representation of a raw object ID as dotted
   * string ("1.3.6.1.4").
   *
   * @param value
   *    the OID value to format.
   * @return
   *    the textual representation.
   */
  public static String formatOID(int[] value) {
    StringBuilder buf = new StringBuilder(3*value.length);
    for (int i=0; i<value.length; i++) {
      if (i != 0) {
        buf.append('.');
      }
      buf.append((value[i] & 0xFFFFFFFFL));
    }
    return buf.toString();
  }

  /**
   * Returns a textual representation of a raw object ID as dotted
   * string ("1.3.6.1.4").
   *
   * @param value
   *    the OID value to format.
   * @return
   *    the textual representation.
   */
  public String format(int[] value) {
    return SimpleOIDTextFormat.formatOID(value);
  }

  /**
   * Returns a textual representation of a raw object ID as dotted
   * string ("1.3.6.1.4"). This method is the same as {@link #format(int[])}.
   *
   * @param value
   *    the OID value to format.
   * @return
   *    the textual representation.
   */
  public String formatForRoundTrip(int[] value) {
    return format(value);
  }


  /**
    * Parses a textual representation of an object ID as dotted string
    * (e.g. "1.3.6.1.2.1.1") and returns its raw value.
    *
    * @param text
    *    a textual representation of an OID.
    * @return
    *    the raw OID value.
    * @throws ParseException
    *    if the OID cannot be parsed successfully.
    */
  public static int[] parseOID(String text) throws ParseException {
    StringTokenizer st = new StringTokenizer(text, ".", true);
    int size = st.countTokens();
    int[] value = new int[size];
    size = 0;
    StringBuffer buf = null;
    while (st.hasMoreTokens()) {
      String t = st.nextToken();
      if ((buf == null) && t.startsWith("'")) {
        buf = new StringBuffer();
        t = t.substring(1);
      }
      if ((buf != null) && (t.endsWith("'"))) {
        buf.append(t.substring(0, t.length()-1));
        OID o = new OctetString(buf.toString()).toSubIndex(true);
        int[] h = value;
        value = new int[st.countTokens()+h.length+o.size()];
        System.arraycopy(h, 0, value, 0, size);
        System.arraycopy(o.getValue(), 0, value, size, o.size());
        size += o.size();
        buf = null;
      }
      else if (buf != null) {
        buf.append(t);
      }
      else if (!".".equals(t)) {
        value[size++] = (int) Long.parseLong(t.trim());
      }
    }
    if (size < value.length) {
      int[] h = value;
      value = new int[size];
      System.arraycopy(h, 0, value, 0, size);
    }
    return value;
  }

  /**
   * Parses a textual representation of an object ID as dotted string
   * (e.g. "1.3.6.1.2.1.1") and returns its raw value.
   *
   * @param text
   *    a textual representation of an OID.
   * @return
   *    the raw OID value.
   * @throws ParseException
   *    if the OID cannot be parsed successfully.
   */
  public int[] parse(String text) throws ParseException {
    return SimpleOIDTextFormat.parseOID(text);
  }
}
