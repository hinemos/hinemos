/*_############################################################################
  _## 
  _##  SNMP4J - AuthGeneric.java  
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
import org.snmp4j.smi.OctetString;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * The abstract class AuthGeneric implements common operations for SNMP authentication protocols, such as MD5 and SHA.
 *
 * @author Frank Fock
 * @author Jochen Katz
 * @version 1.0
 */

public abstract class AuthGeneric implements AuthenticationProtocol {

    private static final LogAdapter logger = LogFactory.getLogger(AuthGeneric.class);
    private static final long serialVersionUID = 4035708925348178888L;

    // HMAC size defined as 1MB by RFC RFC3414 section A.2.2
    private static final int HMAC_BUFFER_SIZE = 1024*1024;

    public static int HMAC_BLOCK_SIZE = 64;
    private static int DEFAULT_AUTHENTICATION_CODE_LENGTH = 12;

    protected int hmacBlockSize;
    private int authenticationCodeLength;
    private final int digestLength;
    private final String protoName;

    /**
     * Creates an authentication protocol with the specified name (ID) and digest length and using the {@link
     * #DEFAULT_AUTHENTICATION_CODE_LENGTH} default code length.
     *
     * @param protoName
     *         the name (ID) of the authentication protocol. Only names that are supported by the used security provider
     *         can be used.
     * @param digestLength
     *         the digest length.
     */
    public AuthGeneric(String protoName, int digestLength) {
        this.hmacBlockSize = HMAC_BLOCK_SIZE;
        this.authenticationCodeLength = DEFAULT_AUTHENTICATION_CODE_LENGTH;
        this.protoName = protoName;
        this.digestLength = digestLength;
    }

    /**
     * Creates an authentication protocol with the specified name (ID) and digest length and using the {@link
     * #DEFAULT_AUTHENTICATION_CODE_LENGTH} default code length.
     *
     * @param protoName
     *         the name (ID) of the authentication protocol. Only names that are supported by the used security provider
     *         can be used.
     * @param digestLength
     *         the digest length.
     * @param authenticationCodeLength
     *         the length of the hash output (i.e., the authentication code length).
     *
     * @since 2.4.0
     */
    public AuthGeneric(String protoName, int digestLength, int authenticationCodeLength) {
        this(protoName, digestLength);
        this.authenticationCodeLength = authenticationCodeLength;
    }

    /**
     * Creates an authentication protocol with the specified name (ID) and digest length and using the {@link
     * #DEFAULT_AUTHENTICATION_CODE_LENGTH} default code length.
     *
     * @param protoName
     *         the name (ID) of the authentication protocol. Only names that are supported by the used security provider
     *         can be used.
     * @param digestLength
     *         the digest length.
     * @param authenticationCodeLength
     *         the length of the hash output (i.e., the authentication code length).
     * @param hmacBlockSize
     *         the HMAC block size of the authentication protocol.
     *
     * @since 2.5.4
     */
    public AuthGeneric(String protoName, int digestLength, int authenticationCodeLength, int hmacBlockSize) {
        this(protoName, digestLength, authenticationCodeLength);
        this.hmacBlockSize = hmacBlockSize;
    }

    /**
     * Gets the length of the message digest used by this authentication protocol.
     *
     * @return the number of octets in the digest.
     */
    public int getDigestLength() {
        return digestLength;
    }

    public int getMaxKeyLength() {
        return getDigestLength();
    }

    /**
     * The length of the authentication code (the hashing output length) in octets.
     *
     * @return the length of the authentication code.
     * @since 2.4.0
     */
    @Override
    public int getAuthenticationCodeLength() {
        return authenticationCodeLength;
    }

    /**
     * Get a fresh MessageDigest object of the Algorithm specified in the constructor.
     *
     * @return a new, fresh Message Digest object.
     */
    protected MessageDigest getDigestObject() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(protoName);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new InternalError(protoName + " not supported in this VM.");
        }
        // not needed the first time: md.reset();
        return md;
    }

    @Override
    public boolean isSupported() {
        try {
            MessageDigest.getInstance(protoName);
            return true;
        } catch (java.security.NoSuchAlgorithmException e) {
            return false;
        }
    }

    public boolean authenticate(byte[] authenticationKey,
                                byte[] message,
                                int messageOffset,
                                int messageLength,
                                ByteArrayWindow digest) {
        MessageDigest md = getDigestObject();

        byte[] authKey = authenticationKey;

        byte[] newDigest;
        byte[] k_ipad = new byte[hmacBlockSize]; /* inner padding - key XORd with ipad */
        byte[] k_opad = new byte[hmacBlockSize]; /* outer padding - key XORd with opad */

        // clear the bytes for the digest
        for (int i = 0; i < authenticationCodeLength; ++i) {
            digest.set(i, (byte) 0);
        }

        if (authKey.length > hmacBlockSize) {
            authKey = md.digest(authenticationKey);
        }
        /*
         * the HMAC_MD transform looks like:
         *
         * MD(K XOR opad, MD(K XOR ipad, msg))
         *
         * where K is an n byte key
         * ipad is the byte 0x36 repeated 64 times
         * opad is the byte 0x5c repeated 64 times
         * and text is the data being protected
         */
        /* start out by storing key, ipad and opad in pads */
        for (int i = 0; i < authKey.length; ++i) {
            k_ipad[i] = (byte) (authKey[i] ^ 0x36);
            k_opad[i] = (byte) (authKey[i] ^ 0x5c);
        }
        for (int i = authKey.length; i < hmacBlockSize; ++i) {
            k_ipad[i] = 0x36;
            k_opad[i] = 0x5c;
        }

        /* perform inner MD */
        md.update(k_ipad); /* start with inner pad      */
        md.update(message, messageOffset, messageLength); /* then text of msg  */
        newDigest = md.digest(); /* finish up 1st pass        */
        /* perform outer MD */
        md.reset(); /* init hash function for 2nd pass     */
        md.update(k_opad); /* start with outer pad      */
        md.update(newDigest); /* then results of 1st hash  */
        newDigest = md.digest(); /* finish up 2nd pass        */

        // copy the digest into the message (authenticationCodeLength bytes only!)
        for (int i = 0; i < authenticationCodeLength; ++i) {
            digest.set(i, newDigest[i]);
        }
        return true;
    }

    public boolean isAuthentic(byte[] authenticationKey,
                               byte[] message,
                               int messageOffset,
                               int messageLength,
                               ByteArrayWindow digest) {
        // copy digest from message
        ByteArrayWindow origDigest =
                new ByteArrayWindow(new byte[authenticationCodeLength], 0,
                        authenticationCodeLength);
        System.arraycopy(digest.getValue(), digest.getOffset(),
                origDigest.getValue(), 0,
                authenticationCodeLength);

        // use the authenticate() method to recalculate the digest
        if (!authenticate(authenticationKey, message, messageOffset,
                messageLength, digest)) {
            return false;
        }
        return digest.equals(origDigest, authenticationCodeLength);
    }

    public byte[] changeDelta(byte[] oldKey,
                              byte[] newKey,
                              byte[] random) {
        // algorithm according to USM-document textual convention KeyChange
        // works with SHA and MD5
        // modifications needed to support variable length keys
        // algorithm according to USM-document textual convention KeyChange
        MessageDigest hash = getDigestObject();

        int digestLength = hash.getDigestLength();

        if (logger.isDebugEnabled()) {
            logger.debug(protoName + "oldKey: " +
                    new OctetString(oldKey).toHexString());
            logger.debug(protoName + "newKey: " +
                    new OctetString(newKey).toHexString());
            logger.debug(protoName + "random: " +
                    new OctetString(random).toHexString());
        }
        int iterations = (oldKey.length - 1) / hash.getDigestLength();

        OctetString tmp = new OctetString(oldKey);
        OctetString delta = new OctetString();
        for (int k = 0; k < iterations; k++) {
            tmp.append(random);
            hash.update(tmp.getValue());
            tmp.setValue(hash.digest());
            delta.append(new byte[digestLength]);
            for (int kk = 0; kk < digestLength; kk++) {
                delta.set(k * digestLength + kk,
                        (byte) (tmp.get(kk) ^ newKey[k * digestLength + kk]));
            }
        }

        tmp.append(random);
        hash.update(tmp.getValue());
        tmp = new OctetString(hash.digest(), 0, oldKey.length - delta.length());
        for (int j = 0; j < tmp.length(); j++) {
            tmp.set(j, (byte) (tmp.get(j) ^ newKey[iterations * digestLength + j]));
        }
        byte[] keyChange = new byte[random.length + delta.length() + tmp.length()];
        System.arraycopy(random, 0, keyChange, 0, random.length);
        System.arraycopy(delta.getValue(), 0, keyChange,
                random.length, delta.length());
        System.arraycopy(tmp.getValue(), 0, keyChange,
                random.length + delta.length(), tmp.length());

        if (logger.isDebugEnabled()) {
            logger.debug(protoName + "keyChange:" +
                    new OctetString(keyChange).toHexString());
        }
        return keyChange;
    }

    public byte[] passwordToKey(OctetString passwordString, byte[] engineID) {

        MessageDigest md = getDigestObject();

        byte[] digest;
        ByteBuffer buf = ByteBuffer.allocate(HMAC_BUFFER_SIZE);
        int password_index = 0;
        int count = 0;
        byte[] password = passwordString.getValue();

        /* Use while loop until we've done 1 Megabyte */
        while (count < HMAC_BUFFER_SIZE) {
            for (int i = 0; i < hmacBlockSize; ++i) {
                /* Take the next octet of the password, wrapping */
                /* to the beginning of the password as necessary.*/
                buf.put(password[password_index++ % password.length]);
            }
            count += hmacBlockSize;
        }
        buf.flip();
        md.update(buf);
        digest = md.digest();
        if (logger.isDebugEnabled()) {
            logger.debug(protoName + "First digest: " + new OctetString(digest).toHexString());
        }

        /*****************************************************/
        /* Now localize the key with the engine_id and pass  */
        /* through MD to produce final key                   */
        /*****************************************************/
        md.reset();
        md.update(digest);
        md.update(engineID);
        md.update(digest);
        digest = md.digest();
        if (logger.isDebugEnabled()) {
            logger.debug(protoName + "localized key: " +
                    new OctetString(digest).toHexString());
        }

        return digest;
    }

    public byte[] hash(byte[] data) {
        MessageDigest md = getDigestObject();
        md.update(data);
        return md.digest();
    }

    public byte[] hash(byte[] data, int offset, int length) {
        MessageDigest md = getDigestObject();
        md.update(data, offset, length);
        return md.digest();
    }

}
