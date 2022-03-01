/*_############################################################################
  _## 
  _##  SNMP4J - SNMPv3SecurityModel.java  
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

import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The abstract class <code>SNMPv3SecurityModel</code> implements
 * common methods and fields for security models for the SNMPv3
 * message processing model.
 * @author Frank Fock
 * @version 2.0
 * @since 2.0
 */
public abstract class SNMPv3SecurityModel implements SecurityModel {

  protected OctetString localEngineID;

  /**
   * Returns the local engine ID.
   * @return
   *    the local engine ID.
   * @since 1.2
   */
  public OctetString getLocalEngineID() {
    return localEngineID;
  }

  protected static byte[] buildWholeMessage(Integer32 snmpVersion,
                                            byte[] scopedPdu,
                                            byte[] globalData,
                                            SecurityParameters securityParameters)
      throws IOException
  {
    int length =
        snmpVersion.getBERLength() +
        globalData.length +
        securityParameters.getBERLength() +
        scopedPdu.length;
    int totalLength = BER.getBERLengthOfLength(length) + length + 1;

    ByteArrayOutputStream os = new ByteArrayOutputStream(totalLength);
    BER.encodeHeader(os, BER.SEQUENCE, length);
    snmpVersion.encodeBER(os);
    os.write(globalData);
    securityParameters.encodeBER(os);
    os.write(scopedPdu);
    int secParamsPos = 1 + snmpVersion.getBERLength() +
        BER.getBERLengthOfLength(length)  + globalData.length;
    securityParameters.setSecurityParametersPosition(secParamsPos);
    return os.toByteArray();
  }

  protected static byte[] buildMessageBuffer(BERInputStream scopedPDU)
      throws IOException
  {
    scopedPDU.mark(16);
    int readLengthBytes = (int)scopedPDU.getPosition();
    BER.MutableByte mutableByte = new BER.MutableByte();
    int length = BER.decodeHeader(scopedPDU, mutableByte);
    readLengthBytes = (int)scopedPDU.getPosition() - readLengthBytes;
    byte[] buf = new byte[length + readLengthBytes];
    scopedPDU.reset();

    int offset = 0;
    int avail = scopedPDU.available();
    while ((offset < buf.length) && (avail > 0)) {
      int read = scopedPDU.read(buf, offset, buf.length - offset);
      if (read < 0) {
        break;
      }
      offset += read;
    }
    return buf;
  }

}
