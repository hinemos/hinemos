/*_############################################################################
  _## 
  _##  SNMP4J - BERSerializable.java  
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

/**
 * The interface <code>BERSerializable</code> has to be implemented by
 * any data type class that needs to be serialized using the Basic Encoding
 * Rules (BER) that provides enconding rules for ASN.1 data types.
 *
 * @author Frank Fock
 * @author Jochen Katz
 * @version 1.0
 */
public interface BERSerializable /*extends Serializable*/ {

  /**
   * Returns the length of this <code>BERSerializable</code> object
   * in bytes when encoded according to the Basic Encoding Rules (BER).
   * @return
   *    the BER encoded length of this variable.
   */
  int getBERLength();

  /**
   * Returns the length of the payload of this <code>BERSerializable</code> object
   * in bytes when encoded according to the Basic Encoding Rules (BER).
   * @return
   *    the BER encoded length of this variable.
   */
  int getBERPayloadLength();

  /**
   * Decodes a <code>Variable</code> from an <code>InputStream</code>.
   * @param inputStream
   *    an <code>InputStream</code> containing a BER encoded byte stream.
   * @throws IOException
   *    if the stream could not be decoded by using BER rules.
   */
  void decodeBER(BERInputStream inputStream) throws IOException;

  /**
   * Encodes a <code>Variable</code> to an <code>OutputStream</code>.
   * @param outputStream
   *    an <code>OutputStream</code>.
   * @throws IOException
   *    if an error occurs while writing to the stream.
   */
  void encodeBER(OutputStream outputStream) throws IOException;

}
