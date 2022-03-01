/*_############################################################################
  _## 
  _##  SNMP4J - ScopedPDU.java  
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
package org.snmp4j;

import org.snmp4j.smi.AbstractVariable;
import org.snmp4j.smi.OctetString;
import org.snmp4j.asn1.BER;
import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.asn1.BER.MutableByte;

/**
 * The <code>ScopedPDU</code> class represents a SNMPv3 scoped PDU.
 *
 * @author Frank Fock
 * @version 1.0
 */

public class ScopedPDU extends PDU {

  private static final long serialVersionUID = 4343157159110407279L;

  private OctetString contextEngineID = new OctetString();
  private OctetString contextName = new OctetString();

  /**
   * Create a empty ScopedPDU.
   */
  public ScopedPDU() {
  }

  /**
   * Copy constructor.
   * @param other
   *    a <code>ScopedPDU</code> instance.
   */
  public ScopedPDU(ScopedPDU other) {
    super(other);
    this.contextEngineID = (OctetString) other.contextEngineID.clone();
    this.contextName = (OctetString) other.contextName.clone();
  }

  /**
   * Sets the context engine ID field of the scoped PDU.
   * @param contextEngineID
   *    an <code>OctetString</code> instance (must not be <code>null</code>).
   * @throws NullPointerException if contextEngineID == null
   */
  public void setContextEngineID(OctetString contextEngineID) {
    if (contextEngineID == null) {
      throw new NullPointerException("Context engine ID must not be null");
    }
    this.contextEngineID = contextEngineID;
  }

  /**
   * Gets the context engine ID of this scoped PDU.
   * @return
   *    an <code>OctetString</code> instance.
   */
  public OctetString getContextEngineID() {
    return contextEngineID;
  }

  /**
   * Sets the context name field of this scoped PDU.
   * @param contextName
   *    an <code>OctetString</code> instance (must not be <code>null</code>).
   */
  public void setContextName(OctetString contextName) {
    if (contextName == null) {
      throw new NullPointerException("Context name must not be null");
    }
    this.contextName = contextName;
  }

  /**
   * Gets the context name of this scoped PDU.
   * @return
   *    an <code>OctetString</code> instance.
   */
  public OctetString getContextName() {
    return contextName;
  }

  public int getBERLength() {
    int length = getBERPayloadLength();
    length += 1 + BER.getBERLengthOfLength(length);
    return length;
  }

  public int getBERPayloadLength() {
    int length = super.getBERLength();
    int cid = (contextEngineID == null) ? 0 : contextEngineID.length();
    int cn = (contextName == null) ? 0 : contextName.length();
    length += BER.getBERLengthOfLength(cid) + 1
        + cid + BER.getBERLengthOfLength(cn) + 1 + cn;
    return length;
  }

  public void encodeBER(OutputStream outputStream) throws IOException {
    BER.encodeHeader(outputStream, BER.SEQUENCE, getBERPayloadLength());
    contextEngineID.encodeBER(outputStream);
    contextName.encodeBER(outputStream);
    super.encodeBER(outputStream);
  }



  public Object clone() {
    return new ScopedPDU(this);
  }

  /**
   * Decodes a <code>ScopedPDU</code> from an <code>InputStream</code>.
   *
   * @param inputStream an <code>InputStream</code> containing a BER encoded
   *   byte stream.
   * @throws IOException
   *   if there is a BER encoding error or other IO exception in the input stream.
   */
  public void decodeBER(BERInputStream inputStream) throws IOException {
    MutableByte mutableByte = new MutableByte();
    int length = BER.decodeHeader(inputStream, mutableByte);
    long startPos = inputStream.getPosition();
    contextEngineID.decodeBER(inputStream);
    contextName.decodeBER(inputStream);
    super.decodeBER(inputStream);
    if (BER.isCheckSequenceLength()) {
      BER.checkSequenceLength(length,
                              (int) (inputStream.getPosition() - startPos),
                              this);
    }
  }

  /**
   * Returns a string representation of the object.
   *
   * @return a string representation of the object.
   */
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(getTypeString(type));
    buf.append("[{contextEngineID=").append(contextEngineID).append(", contextName=").
            append(contextName).append("}, requestID=");
    buf.append(requestID);
    buf.append(", errorStatus=");
    buf.append(errorStatus);
    buf.append(", errorIndex=");
    buf.append(errorIndex);
    buf.append(", VBS[");
    for (int i = 0; i < variableBindings.size(); i++) {
      buf.append(variableBindings.get(i));
      if (i + 1 < variableBindings.size()) {
        buf.append("; ");
      }
    }
    buf.append("]]");
    return buf.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ScopedPDU) {
      ScopedPDU o = (ScopedPDU)obj;
      return super.equals(obj) &&
        AbstractVariable.equal(contextEngineID, o.contextEngineID) &&
        AbstractVariable.equal(contextName, o.contextName);
    }
    return super.equals(obj);    //To change body of overridden methods use File | Settings | File Templates.
  }

}
