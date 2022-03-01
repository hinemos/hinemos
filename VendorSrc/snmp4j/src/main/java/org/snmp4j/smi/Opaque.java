/*_############################################################################
  _## 
  _##  SNMP4J - Opaque.java  
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
 * The <code>Opaque</code> class represents the SMI type Opaque which is used
 * to transparently exchange BER encoded values.
 *
 * @author Frank Fock
 * @version 1.7.6
 */
public class Opaque extends OctetString {

  private static final long serialVersionUID = -17056771587100877L;

  public Opaque() {
    super();
  }

  public Opaque(byte[] bytes) {
    super(bytes);
  }

  public int getSyntax() {
    return SMIConstants.SYNTAX_OPAQUE;
  }

  public void encodeBER(OutputStream outputStream) throws IOException {
    BER.encodeString(outputStream, BER.OPAQUE, getValue());
  }

  public void decodeBER(BERInputStream inputStream) throws IOException {
    BER.MutableByte type = new BER.MutableByte();
    byte[] v = BER.decodeString(inputStream, type);
    if (type.getValue() != (BER.ASN_APPLICATION | 0x04)) {
      throw new IOException("Wrong type encountered when decoding OctetString: "+
                            type.getValue());
    }
    setValue(v);
  }

  public void setValue(OctetString value) {
    this.setValue(new byte[0]);
    append(value);
  }

  public String toString() {
    return super.toHexString();
  }

  public Object clone() {
    return new Opaque(super.getValue());
  }

}

