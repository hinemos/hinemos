/*_############################################################################
  _## 
  _##  SNMP4J - AuthenticationProtocol.java  
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
import org.snmp4j.smi.OctetString;

/**
 * The <code>AuthenticationProtocol</code> interface defines a common
 * interface for all SNMP authentication protocols.
 *
 * @author Frank Fock
 * @version 2.4.0
 */
public interface AuthenticationProtocol extends SecurityProtocol {

  /**
   * Authenticates an outgoing message.
   *
   * This method fills the authentication parameters field of the
   * given message. The parameter <code>digestOffset</code> offset is pointing
   * inside the message buffer and must be zeroed before the authentication
   * value is computed.
   *
   * @param authenticationKey
   *    the authentication key to be used for authenticating the message.
   * @param message
   *    the entire message for which the digest should be determined.
   * @param messageOffset
   *    the offset in <code>message</code> where the message actually starts.
   * @param messageLength
   *    the actual message length (may be smaller than
   *    <code>message.length</code>).
   * @param digest
   *    the offset in <code>message</code> where to store the digest.
   * @return
   *    <code>true</code> if the message digest has been successfully computed
   *    and set, <code>false</code> otherwise.
   */
  boolean authenticate(byte[] authenticationKey,
                       byte[] message,
                       int messageOffset,
                       int messageLength,
                       ByteArrayWindow digest);

  /**
   * Authenticates an incoming message.
   *
   * This method checks if the value in the authentication parameters
   * field of the message is valid.
   *
   * The following procedure is used to verify the authenitcation value
   * <UL>
   * <LI> copy the authentication value to a temp buffer
   * <LI> zero the auth field
   * <LI> recalculate the authenthication value
   * <LI> compare the two authentcation values
   * <LI> write back the received authentication value
   * </UL>
   *
   * @param authenticationKey
   *    the authentication key to be used for authenticating the message.
   * @param message
   *    the entire message for which the digest should be determined.
   * @param messageOffset
   *    the offset in <code>message</code> where the message actually starts.
   * @param messageLength
   *    the actual message length (may be smaller than
   *    <code>message.length</code>).
   * @param digest
   *    the digest of the <code>message</code>.
   * @return
   *    <code>true</code> if the message is authentic, <code>false</code>
   *   otherwise.
   */
  boolean isAuthentic(byte[] authenticationKey,
                      byte[] message,
                      int messageOffset,
                      int messageLength,
                      ByteArrayWindow digest);


  /**
   * Computes the delta digest needed to remotely change an user's
   * authenitcation key. The length of the old key (e.g. 16 for MD5,
   * 20 for SHA) must match the length of the new key.
   *
   * @param oldKey
   *    the old authentication/privacy key.
   * @param newKey
   *    the new authentication/privacy key.
   * @param random
   *    the random 'seed' to be used to produce the digest.
   * @return
   *   the byte array representing the delta for key change operations.
   *   To obtain the key change value, append this delta to the
   *   <code>random</code> array.
   */
  byte[] changeDelta(byte[] oldKey,
                     byte[] newKey,
                     byte[] random);

  /**
   * Gets the OID uniquely identifying the authentication protocol.
   * @return
   *    an <code>OID</code> instance.
   */
  OID getID();

  /**
   * Generates the localized key for the given password and engine id.
   *
   * @param passwordString
   *    the authentication pass phrase.
   * @param engineID
   *    the engine ID of the authoritative engine.
   * @return
   *    the localized authentication key.
   */
  byte[] passwordToKey(OctetString passwordString, byte[] engineID);

  /**
   * Generates a hash value for the given data.
   *
   * @param data
   *    the data
   * @return
   *    the generated hash.
   */
  byte[] hash(byte[] data);

  /**
   * Generates a hash value for the given data.
   *
   * @param data
   *    the data
   * @param offset
   *     offset into data
   *  @param length
   *     length of data to hash
   * @return
   *    the generated hash.
   */
  byte[] hash(byte[] data, int offset, int length);

  /**
   * Gets the length of the digest generated by this authentication protocol.
   * This value can be used to compute the BER encoded length of the security
   * parameters for authentication.
   *
   * @return
   *    the number of bytes of digests generated by this authentication
   *    protocol.
   */
  int getDigestLength();

  /**
   * The length of the authentication code (the hashing output length) in octets.
   * @return
   *    the length of the authentication code.
   * @since 2.4.0
   */
  int getAuthenticationCodeLength();

}

