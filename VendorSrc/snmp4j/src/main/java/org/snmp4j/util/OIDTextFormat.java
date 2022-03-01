/*_############################################################################
  _## 
  _##  SNMP4J - OIDTextFormat.java  
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

import java.text.ParseException;

/**
 * The <code>OIDTextFormat</code> provides a textual representation of a raw
 * object ID.
 *
 * @author Frank Fock
 * @version 2.2
 * @since 1.10
 */
public interface OIDTextFormat {

  /**
   * Returns a textual representation of a raw object ID, for example as
   * dotted string ("1.3.6.1.4") or object name ("ifDescr") depending on the
   * formats representation rules.
   *
   * @param value
   *    the OID value to format.
   * @return
   *    the textual representation.
   */
  String format(int[] value);

  /**
   * Returns a textual representation of a raw object ID, for example as
   * dotted string ("1.3.6.1.4"), object name plus numerical index ("ifDescr.0"),
   * or other formats that can be parsed again with {@link #parse(String)} to a
   * the same OID value.
   *
   * @param value
   *    the OID value to format.
   * @return
   *    the textual representation.
   */
  String formatForRoundTrip(int[] value);

  /**
   * Parses a textual representation of an object ID and returns its raw value.
   * @param text
   *    a textual representation of an OID.
   * @return
   *    the raw OID value
   * @throws ParseException
   *    if the OID cannot be parsed successfully.
   */
  int[] parse(String text) throws ParseException;

}
