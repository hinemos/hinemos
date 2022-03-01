/*_############################################################################
  _## 
  _##  SNMP4J - MessageLength.java  
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

import java.io.*;

/**
 * The <code>MessageLength</code> object contains information about the
 * length of a message and the length of its header.
 *
 * @author Frank Fock
 * @version 1.7
 * @since 1.7
 */
public class MessageLength implements Serializable {

  private static final long serialVersionUID = -2722178759367760246L;

  private int payloadLength;
  private int headerLength;

  /**
   * Constructs a MessageLength object.
   * @param headerLength
   *    the length in bytes of the message header.
   * @param payloadLength
   *    the length of the payload.
   */
  public MessageLength(int headerLength, int payloadLength) {
    this.payloadLength = payloadLength;
    this.headerLength = headerLength;
  }

  /**
   * Returns the length of the payload.
   * @return
   *    the length in bytes.
   */
  public int getPayloadLength() {
    return payloadLength;
  }

  /**
   * Returns the length of the header.
   * @return
   *    the length in bytes.
   */
  public int getHeaderLength() {
    return headerLength;
  }

  /**
   * Returns the total message length (header+payload).
   * @return
   *    the sum of {@link #getHeaderLength()} and {@link #getPayloadLength()}.
   */
  public int getMessageLength() {
    return headerLength + payloadLength;
  }

  public String toString() {
    return MessageLength.class.getName()+
        "[headerLength="+headerLength+",payloadLength="+payloadLength+"]";
  }
}
