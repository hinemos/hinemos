/*_############################################################################
  _## 
  _##  SNMP4J - PrivacyGeneric.java  
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

import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * The PrivacyGeneric abstract class implements common functionality of privacy protocols.
 * @author Frank Fock
 * @since 2.5.0
 */
public abstract class PrivacyGeneric implements PrivacyProtocol {

  private static final LogAdapter logger = LogFactory.getLogger(PrivacyGeneric.class);

  protected String protocolId;
  protected String protocolClass;
  protected int keyBytes;
  protected Salt salt;
  protected CipherPool cipherPool;
  protected int initVectorLength;


  protected Cipher doInit(byte[] encryptionKey, byte[] initVect) throws
      NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
    // now do CFB encryption of the plaintext
    Cipher alg = cipherPool.reuseCipher();
    if (alg == null) {
      alg = Cipher.getInstance(protocolId);
    }
    SecretKeySpec key =
        new SecretKeySpec(encryptionKey, 0, keyBytes, protocolClass);
    IvParameterSpec ivSpec = new IvParameterSpec(initVect);
    alg.init(Cipher.ENCRYPT_MODE, key, ivSpec);
    return alg;
  }

  protected byte[] doFinal(byte[] unencryptedData, int offset, int length, Cipher alg)
      throws BadPaddingException, IllegalBlockSizeException, ShortBufferException {
    return  alg.doFinal(unencryptedData, offset, length);
  }

  protected byte[] doFinalWithPadding(byte[] unencryptedData, int offset, int length, Cipher alg)
      throws IllegalBlockSizeException, BadPaddingException, ShortBufferException {
    byte[] encryptedData;
    if (length % 8 == 0) {
      encryptedData = alg.doFinal(unencryptedData, offset, length);
    }
    else {
      if (logger.isDebugEnabled()) {
        logger.debug("Using padding.");
      }

      encryptedData = new byte[8 * ( (length / 8) + 1)];
      byte[] tmp = new byte[8];

      int encryptedLength = alg.update(unencryptedData, offset, length,
          encryptedData);
      alg.doFinal(tmp, 0, 8 - (length % 8), encryptedData, encryptedLength);
    }
    return encryptedData;
  }

  protected byte[] doDecrypt(byte[] cryptedData, int offset, int length, byte[] decryptionKey, byte[] iv) {
    byte[] decryptedData = null;
    try {
      Cipher alg = cipherPool.reuseCipher();
      if (alg == null) {
        alg = Cipher.getInstance(protocolId);
      }
      SecretKeySpec key =
          new SecretKeySpec(decryptionKey, 0, keyBytes, protocolClass);
      IvParameterSpec ivSpec = new IvParameterSpec(iv);
      alg.init(Cipher.DECRYPT_MODE, key, ivSpec);
      decryptedData = alg.doFinal(cryptedData, offset, length);
      cipherPool.offerCipher(alg);
    }
    catch (Exception e) {
      logger.error(e);
      if (logger.isDebugEnabled()) {
        e.printStackTrace();
      }
    }
    return decryptedData;
  }

  @Override
  public boolean isSupported() {
    Cipher alg;
    try {
      alg = cipherPool.reuseCipher();
      if (alg == null) {
        alg = Cipher.getInstance(protocolId);
      }
      byte[] initVect = new byte[initVectorLength];
      byte[] encryptionKey = new byte[keyBytes];
      SecretKeySpec key =
          new SecretKeySpec(encryptionKey, 0, keyBytes, protocolClass);
      IvParameterSpec ivSpec = new IvParameterSpec(initVect);
      alg.init(Cipher.ENCRYPT_MODE, key, ivSpec);
      return true;
    } catch (NoSuchPaddingException e) {
      if (logger.isDebugEnabled()) {
        logger.debug(protocolClass + " privacy not available without padding");
      }
      return false;
    } catch (NoSuchAlgorithmException e) {
      if (logger.isDebugEnabled()) {
        logger.debug(protocolClass + " privacy not available");
      }
      return false;
    } catch (InvalidAlgorithmParameterException e) {
      if (logger.isDebugEnabled()) {
        logger.debug(protocolClass+" privacy not available due to invalid parameter: " + e.getMessage());
      }
      return false;
    } catch (InvalidKeyException e) {
      if (logger.isDebugEnabled()) {
        logger.debug(protocolClass+" privacy with key length " + keyBytes + " not supported");
      }
      return false;
    }
  }


}
