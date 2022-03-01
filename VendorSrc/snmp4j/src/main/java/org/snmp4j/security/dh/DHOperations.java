/*_############################################################################
  _## 
  _##  SNMP4J - DHOperations.java  
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

package org.snmp4j.security.dh;

import org.snmp4j.Session;
import org.snmp4j.Target;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.*;
import org.snmp4j.util.PDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPrivateKeySpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

/**
 * Implementation of Diffie Hellman operations for SNMP as defined by RFC 2786.
 *
 * @author Frank Fock
 * @since 2.6.0
 */
public class DHOperations {

    private static final LogAdapter LOGGER = LogFactory.getLogger(DHOperations.class);

    public static final String DIFFIE_HELLMAN = "DH";
    public static final String PBKDF2 = "PBKDF2WithHmacSHA1";
    private static final int PBKDF2_ITERATION_COUNT = 500;
    private static final OctetString PBKDF2_AUTH_SALT = OctetString.fromHexStringPairs("98dfb5ac");
    private static final OctetString PBKDF2_PRIV_SALT = OctetString.fromHexStringPairs("d1310ba6");


    public static final String DH_KICKSTART_SEC_NAME = "dhKickstart";
    public static final String DH_KICKSTART_VIEW_NAME = "dhKickRestricted";

    public static final OID oidUsmDHKickstartMyPublic = new OID(new int[] { 1,3,6,1,3,101,1,2,1,1,2 });
    public static final OID oidUsmDHKickstartMgrPublic = new OID(new int[] { 1,3,6,1,3,101,1,2,1,1,3 });;
    public static final OID oidUsmDHKickstartSecurityName = new OID(new int[] { 1,3,6,1,3,101,1,2,1,1,4 });;

    /**
     * Property name for private keys of Diffie Hellman key exchange property files.
     */
    public static final String DH_PRIVATE_KEY_PROPERTY = "dh.privateKey.";
    /**
     * Property name for public keys of Diffie Hellman key exchange property files.
     */
    public static final String DH_PUBLIC_KEY_PROPERTY = "dh.publicKey.";
    /**
     * Property name for authentication protocol OID of the kickstart user entry.
     */
    public static final String DH_AUTH_PROTOCOL_PROPERTY = "dh.authProtocol.";
    /**
     * Property name for privacy protocol OID of the kickstart user entry.
     */
    public static final String DH_PRIV_PROTOCOL_PROPERTY = "dh.privProtocol.";

    /**
     * Property name for VACM role of the kickstart user entry.
     */
    public static final String DH_VACM_ROLE_PROPERTY = "dh.vacm.role.";

    /**
     * Property name to reset an USM user with a kickstart user entry.
     */
    public static final String DH_RESET_PROPERTY = "dh.reset.";

    /**
     * Property name with the {@link DHParameters} used for the kickstart.
     */
    public static final String DH_PARAMS_PROPERTY = "dh.params";


    public enum KeyType {
        authKeyChange,
        privKeyChange
    }

    public static byte[] computeSharedKey(KeyAgreement keyAgreement, byte[] publicKey, DHParameters dhParameters) {
        if (keyAgreement == null) {
            return null;
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(DIFFIE_HELLMAN);
            BigInteger y = bytesToBigInteger(publicKey);
            DHPublicKeySpec dhPublicKeySpec =
                    new DHPublicKeySpec(y, dhParameters.getPrime(), dhParameters.getGenerator());
            PublicKey pubKey = keyFactory.generatePublic(dhPublicKeySpec);
            keyAgreement.doPhase(pubKey, true);
            byte[] secret = keyAgreement.generateSecret();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Computing shared key "+new OctetString(secret).toHexString()+" from public key "+
                new OctetString(publicKey).toHexString()+" and parameters "+dhParameters);
            }
            return secret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert a byte array to a {@link BigInteger}.
     * Adds a leading zero-byte to ensure a positive {@link BigInteger}.
     *
     * @param bytes
     *      The byte array to convert.
     *
     * @return
     *      the {@link BigInteger} containing the provided bytes as unsigned integer.
     */
    public static BigInteger bytesToBigInteger(byte[] bytes){
		// Pad with 0x00 to avoid a negative BigInteger
        ByteBuffer key = ByteBuffer.allocate(bytes.length + 1);
        key.put((byte)0x00);
        key.put(bytes);
        return new BigInteger(key.array());
    }

    /**
     * Convert a {@link Key} to a byte array. Uses X or Y values
     * of a key depending on key type (private or public). Cut off
     * a leading zero-byte if key length is not divisible by 8.
     *
     * @param key
     *      The {@link Key} to convert.
     *
     * @return
     *      the byte array representation of the key or {@code null}.
     */
    public static byte[] keyToBytes(Key key) {
        byte[] bytes = null;
        if (key instanceof DHPublicKey){
            bytes = ((DHPublicKey)key).getY().toByteArray();
        }
        else if (key instanceof DHPrivateKey){
            bytes = ((DHPrivateKey)key).getX().toByteArray();
        }
        if (bytes == null){
            return null;
        }
		// Cut off leading zero-byte if key length is not divisible by 8.
        if ((bytes.length % 8 != 0) && (bytes[0] == 0x00)) {
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return bytes;
    }


    public static KeyPair createKeyPair(OctetString publicKeyOctets, OctetString privateKeyOctets, DHParameters dhParameters)
    {
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance(DIFFIE_HELLMAN);
            BigInteger y = bytesToBigInteger(publicKeyOctets.getValue());
            DHPublicKeySpec dhPublicKeySpec = new DHPublicKeySpec(y, dhParameters.getPrime(), dhParameters.getGenerator());
            PublicKey publicKey = keyFactory.generatePublic(dhPublicKeySpec);
            BigInteger x = bytesToBigInteger(privateKeyOctets.getValue());
            DHPrivateKeySpec dhPrivateKeySpec = new DHPrivateKeySpec(x, dhParameters.getPrime(), dhParameters.getGenerator());
            PrivateKey privateKey = keyFactory.generatePrivate(dhPrivateKeySpec);
            return new KeyPair(publicKey, privateKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static OctetString derivePublicKey(KeyPair keyPair) {
        return new OctetString(keyToBytes(keyPair.getPublic()));
    }

    public static OctetString derivePrivateKey(KeyPair keyPair) {
        return new OctetString(keyToBytes(keyPair.getPrivate()));
    }

    public static KeyPair generatePublicKey(DHParameters dhParameters)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
    {
        DHParameterSpec dhParameterSpec = new DHParameterSpec(dhParameters.getPrime(), dhParameters.getGenerator(),
                dhParameters.getPrivateValueLength());
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(DIFFIE_HELLMAN);
        keyPairGenerator.initialize(dhParameterSpec);
        return keyPairGenerator.generateKeyPair();
    }

    public static KeyAgreement getInitializedKeyAgreement(KeyPair keyPair) {
        KeyAgreement keyAgreement = null;
        try {
            keyAgreement = KeyAgreement.getInstance(DIFFIE_HELLMAN);
            keyAgreement.init(keyPair.getPrivate());
            return keyAgreement;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Derive the USM key from the Diffie Hellman key exchange.
     * @param sharedKey
     *    the shared key (z).
     * @param keyLength
     *    the key length of the resulting key in bytes.
     * @return
     *    the USM key as byte array of length {@code keyLength}.
     */
    public static byte[] deriveKey(byte[] sharedKey, int keyLength) {
        byte[] derivedKey = new byte[keyLength];
        System.arraycopy(sharedKey, sharedKey.length-keyLength, derivedKey, 0, keyLength);
        return derivedKey;
    }

    public static byte[] deriveKeyPBKDF2(byte[] shareKey, int keyLength,
                                         SecurityProtocols.SecurityProtocolType securityProtocolType) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2);
            byte[] salt = (securityProtocolType == SecurityProtocols.SecurityProtocolType.authentication) ?
                    PBKDF2_AUTH_SALT.getValue() : PBKDF2_PRIV_SALT.getValue();
            String keyString = new String(shareKey);
            PBEKeySpec spec = new PBEKeySpec(keyString.toCharArray(), salt, PBKDF2_ITERATION_COUNT, keyLength*8);
            return skf.generateSecret(spec).getEncoded();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get the public keys of the agent's kickstart table that match the local public keys provided from a remote agent.
     * @param session
     *   the SNMP {@link Session} to use.
     * @param pduFactory
     *   the {@link PDUFactory} to be used to create SNMP PDUs for requesting the data.
     * @param target
     *   the SNMP agent target.
     * @param managerPublic
     *   a set of public keys of this manager for which public keys of the agent should be retrieved.
     * @return
     *   a map that maps the manager's public keys for which an agent public key has been found, to a two-element array
     *   with the first element being the agent public key and the second the associated user/security name.
     * @throws IOException
     *   if the SNMP communication fails.
     */
    public static Map<OctetString, OctetString[]> getDHKickstartPublicKeys(Session session, PDUFactory pduFactory,
                                                                           Target target,
                                                                           Set<OctetString> managerPublic)
            throws IOException
    {
        OctetString dhKickstart = new OctetString(DHOperations.DH_KICKSTART_SEC_NAME);
        target.setSecurityName(dhKickstart);
        // get DH public key and parameters
        TableUtils tableUtils = new TableUtils(session, pduFactory);
        OID[] columns =
                new OID[] { oidUsmDHKickstartMyPublic, oidUsmDHKickstartMgrPublic, oidUsmDHKickstartSecurityName };
        List<TableEvent> rows = tableUtils.getTable(target, columns, null, null);

        HashMap<OctetString, OctetString[]> publicKeys = new HashMap<OctetString, OctetString[]>();
        for (TableEvent row : rows) {
            if (row.getStatus() == TableEvent.STATUS_OK) {
                Variable remoteManagerPublic = row.getColumns()[1].getVariable();
                if (!row.isError() && remoteManagerPublic instanceof OctetString &&
                        managerPublic.contains(remoteManagerPublic)) {
                    publicKeys.put((OctetString) remoteManagerPublic,
                            new OctetString[]{(OctetString) row.getColumns()[0].getVariable(),
                                    (OctetString) row.getColumns()[2].getVariable()});
                } else if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("DH kickstart table retrieval from '" + target + "' returned error: " + row.getErrorMessage());
                }
            }
        }
        return publicKeys;
    }

    /**
     * The {@code DHSharedKeyInfo} provides DH key exchange information that associates a user name with a key
     * (private or shared) and authentication and privacy protocol OIDs necessary to create an {@link USM} user
     * during a DH kick-start operation.
     * @since 3.4.1
     */
    public static class DHKeyInfo implements Serializable {
        private static final long serialVersionUID = -3564364027967850951L;

        private final OctetString userName;
        private byte[] privateKey;
        private byte[] authKey;
        private byte[] privKey;
        private final OID authProtocol;
        private final OID privProtocol;

        public DHKeyInfo(OctetString userName, byte[] privateKey, OID authProtocol, OID privProtocol) {
            this.userName = userName;
            this.privateKey = privateKey;
            this.authProtocol = authProtocol;
            this.privProtocol = privProtocol;
        }

        public OctetString getUserName() {
            return userName;
        }

        public byte[] getPrivateKey() {
            return privateKey;
        }

        public byte[] getAuthKey() {
            return authKey;
        }

        public byte[] getPrivKey() {
            return privKey;
        }

        public void setAuthKey(byte[] authKey) {
            this.authKey = authKey;
        }

        public void setPrivKey(byte[] privKey) {
            this.privKey = privKey;
        }

        public OID getAuthProtocol() {
            return authProtocol;
        }

        public OID getPrivProtocol() {
            return privProtocol;
        }

    }

}
