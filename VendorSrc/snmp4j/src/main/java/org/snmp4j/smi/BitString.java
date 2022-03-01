/*_############################################################################
  _## 
  _##  SNMP4J - BitString.java  
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

import org.snmp4j.asn1.BER;
import java.io.IOException;
import java.io.OutputStream;
import org.snmp4j.asn1.BERInputStream;

/**
 * The <code>BitString</code> class represents the obsolete SMI type
 * BIT STRING which has been defined in RFC 1442 (an SNMPv2 draft) but
 * which has been obsoleteted by RFC 1902 and RFC 2578. This type is
 * provided for compatibility only and should not be used for new
 * applications.
 *
 * @author Frank Fock
 * @version 1.7.4
 * @since 1.7.4
 */
public class BitString extends OctetString {

  private static final long serialVersionUID = -8739361280962307248L;

  /**
   * Creates a BIT STRING value.
   * @deprecated
   *    The BIT STRING type has been temporarily defined in RFC 1442
   *    and obsoleted by RFC 2578. Use OctetString (i.e. BITS syntax)
   *    instead.
   */
  public BitString() {
  }

  public int getSyntax() {
    return BER.ASN_BIT_STR;
  }

  public void encodeBER(OutputStream outputStream) throws java.io.IOException {
    BER.encodeString(outputStream, BER.BITSTRING, getValue());
  }

  public void decodeBER(BERInputStream inputStream) throws java.io.IOException {
    BER.MutableByte type = new BER.MutableByte();
    byte[] v = BER.decodeString(inputStream, type);
    if (type.getValue() != BER.BITSTRING) {
      throw new IOException("Wrong type encountered when decoding BitString: "+
                            type.getValue());
    }
    setValue(v);
  }

  public Object clone() {
    BitString clone = new BitString();
    clone.setValue(super.getValue());
    return clone;
  }
}
