/*_############################################################################
  _## 
  _##  SNMP4J - PrivAES192With3DESKeyExtension.java  
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
package org.snmp4j.security.nonstandard;

import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;

/**
 * This class is provided for interoperability with some broken AES 192bit implementations of major
 * network device manufactures which use a key extension algorithm that was specified for
 * {@link org.snmp4j.security.Priv3DES} but was never specified for AES 192 and 256 bit.
 *
 * Note: DO NOT USE THIS CLASS IF YOU WANT TO COMPLY WITH draft-blumenthal-aes-usm-04.txt!
 *
 * @author Frank Fock
 * @version 2.2.3
 * @since 2.2.3
 */
public class PrivAES192With3DESKeyExtension extends PrivAESWith3DESKeyExtension implements NonStandardSecurityProtocol {

  /**
   * Unique ID of this privacy protocol.
   */
  public static OID ID = new OID(SnmpConstants.oosnmpUsmAesCfb192ProtocolWith3DESKeyExtension);

  /**
   * Constructor.
   */
  public PrivAES192With3DESKeyExtension() {
    super(24);
  }

  @Override
  public OID getDefaultID() {
    return (OID)ID.clone();
  }
}
