/*_############################################################################
  _## 
  _##  SNMP4J - PrivDES.java  
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
import org.snmp4j.log.*;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;
import org.snmp4j.smi.OctetString;

import java.security.NoSuchAlgorithmException;

/**
 * Privacy protocol class for DES.
 *
 * This class uses DES in CBC mode to encrypt the data. The protocol
 * is defined in the IETF standard "User-based Security Model (USM)
 * for SNMPv3".
 *
 * @author Jochen Katz
 * @version 2.5.0
 */
public class PrivDES extends PrivacyGeneric {

  private static final long serialVersionUID = 2526070176429255416L;

  /**
   * Unique ID of this privacy protocol.
   */
  public static final OID ID = new OID("1.3.6.1.6.3.10.1.2.2");

  private static final String PROTOCOL_ID = "DES/CBC/NoPadding";
  private static final String PROTOCOL_CLASS = "DES";
  private static final int DECRYPT_PARAMS_LENGTH = 8;
  private static final int INIT_VECTOR_LENGTH = 8;
  private static final int INPUT_KEY_LENGTH = 16;
  private static final int KEY_LENGTH = 8;
  protected Salt salt;

  private static final LogAdapter logger = LogFactory.getLogger(PrivDES.class);

  public PrivDES()
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
      logger.error("Wrong Key length: need at least 16 bytes, is " +
                   encryptionKey.length +
                   " bytes.");
      throw new IllegalArgumentException("encryptionKey has illegal length "
                                         + encryptionKey.length
                                         + " (should be at least 16).");
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

    byte[] iv = new byte[8];

    // last eight bytes of key xored with decrypt params are used as iv
    if (logger.isDebugEnabled()) {
      logger.debug("Preparing iv for encryption.");
    }
    for (int i = 0; i < 8; ++i) {
      iv[i] = (byte) (encryptionKey[8 + i] ^ decryptParams.array[i]);
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

  /**
   * Decrypts a message using a given decryption key, engine boots count, and
   * engine ID.
   *
   * @param cryptedData
   *    the data to decrypt.
   * @param offset
   *    the offset into <code>cryptedData</code> to start decryption.
   * @param length
   *    the length of the data to decrypt.
   * @param decryptionKey
   *    the decryption key.
   * @param engineBoots
   *    the engine boots counter.
   * @param engineTime
   *    the engine time value.
   * @return
   *    the decrypted data, or <code>null</code> if decryption failed.
   */
  public byte[] decrypt(byte[] cryptedData,
                        int offset,
                        int length,
                        byte[] decryptionKey,
                        long engineBoots,
                        long engineTime,
                        DecryptParams decryptParams) {
    if ((length % 8 != 0) ||
        (length < 8) ||
        (decryptParams.length != 8)) {
      throw new IllegalArgumentException(
          "Length (" + length +
          ") is not multiple of 8 or decrypt params has not length 8 ("
          + decryptParams.length + ").");
    }
    if (decryptionKey.length < INPUT_KEY_LENGTH) {
      logger.error("Wrong Key length: need at least 16 bytes, is " +
                   decryptionKey.length +
                   " bytes.");
      throw new IllegalArgumentException("decryptionKey has illegal length "
                                         + decryptionKey.length
                                         + " (should be at least 16).");
    }

    byte[] iv = new byte[8];

    // last eight bytes of key xored with decrypt params are used as iv
    for (int i = 0; i < 8; ++i) {
      iv[i] = (byte) (decryptionKey[8 + i] ^ decryptParams.array[i]);
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

  @Override
  public boolean isSupported() {
    Cipher alg;
    try {
      alg = cipherPool.reuseCipher();
      if (alg == null) {
        Cipher.getInstance("DESede/CBC/NoPadding");
      }
      return true;
    } catch (NoSuchPaddingException e) {
      return false;
    } catch (NoSuchAlgorithmException e) {
      return false;
    }
  }

  public int getEncryptedLength(int scopedPDULength) {
    if (scopedPDULength % 8 == 0) {
      return scopedPDULength;
    }
    return 8 * ( (scopedPDULength / 8) + 1);
  }

  public int getMinKeyLength() {
    return 16;
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
    return shortKey;
  }


}
