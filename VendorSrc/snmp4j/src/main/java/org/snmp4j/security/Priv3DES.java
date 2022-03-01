/*_############################################################################
  _## 
  _##  SNMP4J - Priv3DES.java  
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

import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.log.*;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.snmp4j.smi.OctetString;

import java.security.NoSuchAlgorithmException;

/**
 * Privacy protocol class for Triple DES (DESEDE).
 *
 * This class uses DES-EDE in CBC mode to encrypt the data. The protocol
 * is defined by the Internet Draft 'Extension to the User-Based Security
 * Model (USM) to Support Triple-DES EDE in "Outside" CBC Mode'.
 *
 * @author Frank Fock, Jochen Katz
 * @version 2.2.2
 * @since 1.9
 */
public class Priv3DES extends PrivacyGeneric {

  /**
   * Unique ID of this privacy protocol.
   */
  public static final OID ID = new OID(SnmpConstants.usm3DESEDEPrivProtocol);

  private static final String PROTOCOL_ID = "DESede/CBC/NoPadding";
  private static final String PROTOCOL_CLASS = "DESede";
  private static final int DECRYPT_PARAMS_LENGTH = 8;
  private static final int INIT_VECTOR_LENGTH = 8;
  private static final int INPUT_KEY_LENGTH = 32;
  private static final int KEY_LENGTH = 24;
  protected Salt salt;

  private static final LogAdapter logger = LogFactory.getLogger(Priv3DES.class);

  public Priv3DES()
  {
    super.initVectorLength = INIT_VECTOR_LENGTH;
    super.protocolId = PROTOCOL_ID;
    super.protocolClass = PROTOCOL_CLASS;
    super.keyBytes = KEY_LENGTH;
    this.salt = Salt.getInstance();
    cipherPool = new CipherPool();
  }

  public byte[] encrypt(byte[] unencryptedData,
                        int offset,
                        int length,
                        byte[] encryptionKey,
                        long engineBoots,
                        long engineTime,
                        DecryptParams decryptParams) {
    int mySalt = (int)salt.getNext();

    if (encryptionKey.length < INPUT_KEY_LENGTH) {
      logger.error("Wrong Key length: need at least 32 bytes, is " +
                   encryptionKey.length +
                   " bytes.");
      throw new IllegalArgumentException("encryptionKey has illegal length "
                                         + encryptionKey.length
                                         + " (should be at least 32).");
    }

    if ( (decryptParams.array == null) || (decryptParams.length < 8)) {
      decryptParams.array = new byte[8];
    }
    decryptParams.length = 8;
    decryptParams.offset = 0;

    // put salt in decryption_params (sent as priv params)
    if (logger.isDebugEnabled()) {
      logger.debug("Preparing decrypt_params.");
    }
    for (int i = 0; i < 4; ++i) {
      decryptParams.array[3 - i] = (byte) (0xFF & (engineBoots >> (8 * i)));
      decryptParams.array[7 - i] = (byte) (0xFF & (mySalt >> (8 * i)));
    }

    byte[] iv = new byte[INIT_VECTOR_LENGTH];

    // last eight bytes of key xored with decrypt params are used as iv
    if (logger.isDebugEnabled()) {
      logger.debug("Preparing iv for encryption.");
    }
    for (int i = 0; i < 8; ++i) {
      iv[i] = (byte) (encryptionKey[KEY_LENGTH + i] ^ decryptParams.array[i]);
    }

    byte[] encryptedData = null;

    try {
      // now do CBC encryption of the plaintext
      Cipher alg = doInit(encryptionKey, iv);
      encryptedData = doFinalWithPadding(unencryptedData, offset, length, alg);
      cipherPool.offerCipher(alg);
    }
    catch (Exception e) {
      logger.error(e);
      if (logger.isDebugEnabled()) {
        e.printStackTrace();
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Encryption finished.");
    }
    return encryptedData;
  }

  public byte[] decrypt(byte[] cryptedData,
                        int offset,
                        int length,
                        byte[] decryptionKey,
                        long engineBoots,
                        long engineTime,
                        DecryptParams decryptParams) {
    if ( (length % 8 != 0) ||
        (length < 8) ||
        (decryptParams.length != 8)) {
      throw new IllegalArgumentException("Length (" + length +
                                         ") is not multiple of 8 or decrypt "+
                                         "params has not length 8 ("
                                         + decryptParams.length + ").");
    }
    if (decryptionKey.length < INPUT_KEY_LENGTH) {
      logger.error("Wrong Key length: need at least 32 bytes, is " +
                   decryptionKey.length +
                   " bytes.");
      throw new IllegalArgumentException("decryptionKey has illegal length "
                                         + decryptionKey.length
                                         + " (should be at least 32).");
    }

    byte[] iv = new byte[8];

    // last eight bytes of key xored with decrypt params are used as iv
    for (int i = 0; i < 8; ++i) {
      iv[i] = (byte) (decryptionKey[KEY_LENGTH + i] ^ decryptParams.array[i]);
    }

    byte[] decryptedData = doDecrypt(cryptedData, offset, length, decryptionKey, iv);
    return decryptedData;
  }

  /**
   * Gets the OID uniquely identifying the privacy protocol.
   * @return
   *    an <code>OID</code> instance.
   */
  public OID getID() {
    return (OID) ID.clone();
  }

  public int getEncryptedLength(int scopedPDULength) {
    if (scopedPDULength % 8 == 0) {
      return scopedPDULength;
    }
    return 8 * ( (scopedPDULength / 8) + 1);
  }

  public int getMinKeyLength() {
    return INPUT_KEY_LENGTH;
  }

  public int getDecryptParamsLength() {
    return DECRYPT_PARAMS_LENGTH;
  }

  public int getMaxKeyLength() {
    return getMinKeyLength();
  }

  public byte[] extendShortKey(byte[] shortKey, OctetString password,
                               byte[] engineID,
                               AuthenticationProtocol authProtocol) {
    int length = shortKey.length;
    byte[] extendedKey = new byte[getMinKeyLength()];
    System.arraycopy(shortKey, 0, extendedKey, 0, shortKey.length);

    byte[] key = new byte[getMinKeyLength()];
    System.arraycopy(shortKey, 0, key, 0, shortKey.length);
    while (length < getMinKeyLength()) {
      key = authProtocol.passwordToKey(new OctetString(key, 0, length), engineID);
      int copyBytes = Math.min(getMinKeyLength() - length,
                               authProtocol.getDigestLength());
      System.arraycopy(key, 0, extendedKey, length, copyBytes);
      length += copyBytes;
    }
    return extendedKey;
  }
}
