/*_############################################################################
  _## 
  _##  SNMP4J - AuthSHA2.java  
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

import org.snmp4j.smi.OID;

/**
 * The <code>SHA-2</code> class implements the Secure Hash Authentication 2.
 *
 * @author Frank Fock
 * @version 2.4.0
 */
public class AuthSHA2 extends AuthGeneric {

  private static final long serialVersionUID = 1L;

  /** The object identifier that identifies this authentication protocol.*/
  private OID protocolID;

  /**
   * Creates a SHA authentication protocol with the specified digest length.
   * @param protocolName
   *   the SHA protocol name (i.e., "SHA-256").
   * @param protocolOID
   *   the OID of the protocol as defined in RFC 7630.
   * @param digestLength
   *   the digest length.
   * @param authenticationCodeLength
   *   the length of the authentication hash output in octets.
   * @deprecated Use {@link #AuthSHA2(String, OID, int, int, int)} instead to specify the correct
   *   HMAC block size
   */
  public AuthSHA2(String protocolName, OID protocolOID, int digestLength, int authenticationCodeLength) {
    super(protocolName, digestLength, authenticationCodeLength);
    this.protocolID = protocolOID;
  }

  /**
   * Creates a SHA authentication protocol with the specified digest length.
   * @param protocolName
   *   the SHA protocol name (i.e., "SHA-256").
   * @param protocolOID
   *   the OID of the protocol as defined in RFC 7630.
   * @param digestLength
   *   the digest length.
   * @param authenticationCodeLength
   *   the length of the authentication hash output in octets.
   * @param hmacBlockSize
   *   the HMAC block size of the authentication protocol.
   */
  public AuthSHA2(String protocolName, OID protocolOID,
                  int digestLength, int authenticationCodeLength, int hmacBlockSize) {
    super(protocolName, digestLength, authenticationCodeLength, hmacBlockSize);
    this.protocolID = protocolOID;
  }

  public OID getID() {
    return (OID) protocolID.clone();
  }


}
