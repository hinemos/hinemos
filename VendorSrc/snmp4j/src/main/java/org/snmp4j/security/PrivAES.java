/*_############################################################################
  _## 
  _##  SNMP4J - PrivAES.java  
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

import javax.crypto.*;

import org.snmp4j.log.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.snmp4j.smi.OctetString;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


/**
 * Base class for PrivAES128, PrivAES192 and PrivAES256.
 *
 * This class uses AES in CFB mode to encrypt the data. The protocol
 * is defined in draft-blumenthal-aes-usm-08.txt (AES128) and
 * draft-blumenthal-aes-usm-04.txt (AES192 and AES256).
 *
 * @author Jochen Katz
 * @version 2.5.0
 */
public abstract class PrivAES extends PrivacyGeneric
    implements PrivacyProtocol {

  private static final String PROTOCOL_ID = "AES/CFB/NoPadding";
  private static final String PROTOCOL_CLASS = "AES";
  private static final int DECRYPT_PARAMS_LENGTH = 8;
  private static final int INIT_VECTOR_LENGTH = 16;

  private static final LogAdapter logger = LogFactory.getLogger(PrivAES.class);
  protected Salt salt;

  /**
   * Constructor.
   *
   * @param keyBytes
   *    Length of key, must be 16, 24 or 32.
   * @throws IllegalArgumentException
   *    if keyBytes is illegal
   */
  public PrivAES(int keyBytes) {
    super.initVectorLength = INIT_VECTOR_LENGTH;
    super.protocolId = PROTOCOL_ID;
    super.protocolClass = PROTOCOL_CLASS;
    if ((keyBytes != 16) && (keyBytes != 24) && (keyBytes != 32)) {
      throw new IllegalArgumentException(
          "Only 128, 192 and 256 bit AES is allowed. Requested ("
          + (8 * keyBytes) + ").");
    }
    this.keyBytes = keyBytes;
    this.salt = Salt.getInstance();
    cipherPool = new CipherPool();
  }

  public byte[] encrypt(byte[] unencryptedData, int offset, int length,
                        byte[] encryptionKey, long engineBoots,
                        long engineTime, DecryptParams decryptParams) {

    byte[] initVect = new byte[INIT_VECTOR_LENGTH];
    long my_salt = salt.getNext();

    if (encryptionKey.length != keyBytes) {
      throw new IllegalArgumentException(
          "Needed key length is " + keyBytes +
          ". Got " + encryptionKey.length +
          ".");
    }

    if ((decryptParams.array == null) ||
        (decryptParams.length < DECRYPT_PARAMS_LENGTH)) {
      decryptParams.array = new byte[DECRYPT_PARAMS_LENGTH];
    }
    decryptParams.length = DECRYPT_PARAMS_LENGTH;
    decryptParams.offset = 0;

    /* Set IV as engine_boots + engine_time + salt */
    initVect[0] = (byte) ( (engineBoots >> 24) & 0xFF);
    initVect[1] = (byte) ( (engineBoots >> 16) & 0xFF);
    initVect[2] = (byte) ( (engineBoots >> 8) & 0xFF);
    initVect[3] = (byte) ( (engineBoots) & 0xFF);
    initVect[4] = (byte) ( (engineTime >> 24) & 0xFF);
    initVect[5] = (byte) ( (engineTime >> 16) & 0xFF);
    initVect[6] = (byte) ( (engineTime >> 8) & 0xFF);
    initVect[7] = (byte) ( (engineTime) & 0xFF);
    for (int i = 56, j = 8; i >= 0; i -= 8, j++) {
      initVect[j] = (byte) ( (my_salt >> i) & 0xFF);
    }
    System.arraycopy(initVect, 8, decryptParams.array, 0, 8);
    if (logger.isDebugEnabled()) {
      logger.debug("initVect is " + asHex(initVect));
    }

    // allocate space for encrypted text
    byte[] encryptedData = null;
    try {
      Cipher alg = doInit(encryptionKey, initVect);
      encryptedData =  alg.doFinal(unencryptedData, offset, length);
      cipherPool.offerCipher(alg);

      if (logger.isDebugEnabled()) {
        logger.debug("aes encrypt: Data to encrypt " + asHex(unencryptedData));

        logger.debug("aes encrypt: used key " + asHex(encryptionKey));

        logger.debug("aes encrypt: created privacy_params " +
                     asHex(decryptParams.array));

        logger.debug("aes encrypt: encrypted Data  " +
                     asHex(encryptedData));
      }
    }
    catch (Exception e) {
      logger.error("Encrypt Exception " + e);
    }

    return encryptedData;
  }

  public byte[] decrypt(byte[] cryptedData, int offset, int length,
                        byte[] decryptionKey, long engineBoots, long engineTime,
                        DecryptParams decryptParams) {

    byte[] initVect = new byte[16];

    if (decryptionKey.length != keyBytes) {
      throw new IllegalArgumentException(
          "Needed key length is " + keyBytes +
          ". Got " + decryptionKey.length +
          ".");
    }

    /* Set IV as engine_boots + engine_time + decrypt params */
    initVect[0] = (byte) ( (engineBoots >> 24) & 0xFF);
    initVect[1] = (byte) ( (engineBoots >> 16) & 0xFF);
    initVect[2] = (byte) ( (engineBoots >> 8) & 0xFF);
    initVect[3] = (byte) ( (engineBoots) & 0xFF);
    initVect[4] = (byte) ( (engineTime >> 24) & 0xFF);
    initVect[5] = (byte) ( (engineTime >> 16) & 0xFF);
    initVect[6] = (byte) ( (engineTime >> 8) & 0xFF);
    initVect[7] = (byte) ( (engineTime) & 0xFF);
    System.arraycopy(decryptParams.array, decryptParams.offset, initVect, 8, 8);
    if (logger.isDebugEnabled()) {
      logger.debug("initVect is " + asHex(initVect));
    }

    return doDecrypt(cryptedData, offset, length, decryptionKey, initVect);
  }

  public int getEncryptedLength(int scopedPDULength) {
    return scopedPDULength;
  }

  /**
   * Turns array of bytes into string
   *
   * @param buf	Array of bytes to convert to hex string
   * @return	Generated hex string
   */
  public static String asHex(byte buf[]) {
    return new OctetString(buf).toHexString();
  }

  public int getMinKeyLength() {
    return keyBytes;
  }

  public int getMaxKeyLength() {
    return getMinKeyLength();
  }

  public int getDecryptParamsLength() {
    return DECRYPT_PARAMS_LENGTH;
  }

  public byte[] extendShortKey(byte[] shortKey, OctetString password,
                               byte[] engineID,
                               AuthenticationProtocol authProtocol) {
    // we have to extend the key, currently only the AES draft
    // defines this algorithm, so this may have to be changed for other
    // privacy protocols
    byte[] extKey = new byte[getMinKeyLength()];
    int length = shortKey.length;
    System.arraycopy(shortKey, 0, extKey, 0, length);

    while (length < extKey.length)
    {
      byte[] hash = authProtocol.hash(extKey, 0, length);

      if (hash == null) {
        return null;
      }
      int bytesToCopy = extKey.length - length;
      if (bytesToCopy > authProtocol.getDigestLength()) {
        bytesToCopy = authProtocol.getDigestLength();
      }
      System.arraycopy(hash, 0, extKey, length, bytesToCopy);
      length += bytesToCopy;
    }
    return extKey;
  }

}
