/*_############################################################################
  _## 
  _##  SNMP4J - MessageLengthDecoder.java  
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

package org.snmp4j.transport;

import java.nio.ByteBuffer;
import java.io.IOException;

/**
 * The <code>MessageLengthDecoder</code> needs to be implemented for connection
 * oriented transport mappings, because those transport mappings have no message
 * boundaries. To determine the message length, the message header is decoded
 * in a protocol specific way.
 *
 * @author Frank Fock
 * @version 1.7
 * @since 1.7
 */
public interface MessageLengthDecoder {

  /**
   * Returns the minimum length of the header to be decoded. Typically this
   * is a constant value.
   * @return
   *    the minimum length in bytes.
   */
  int getMinHeaderLength();

  /**
   * Returns the total message length to read (including header) and
   * the actual header length.
   * @param buf
   *    a ByteBuffer with a minimum of {@link #getMinHeaderLength()}.
   * @return
   *    the total message length in bytes and the actual header length in bytes.
   * @throws IOException
   *    if the header cannot be decoded.
   */
  MessageLength getMessageLength(ByteBuffer buf) throws IOException;

}
