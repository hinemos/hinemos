/*_############################################################################
  _## 
  _##  SNMP4J - DHOperationsTest.java  
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
package org.snmp4j.security.dh;

import org.junit.Test;
import org.snmp4j.smi.OctetString;

import javax.crypto.KeyAgreement;
import java.security.KeyPair;

import static org.junit.Assert.*;

public class DHOperationsTest {

    public static final String DH_DEFAULT_PARAMETERS = "10:69:02:61:00:ff:ff:ff:ff:ff:ff:ff:ff:c9:0f:" +
            "da:a2:21:68:c2:34:c4:c6:62:8b:80:dc:1c:d1:29:" +
            "02:4e:08:8a:67:cc:74:02:0b:be:a6:3b:13:9b:22:" +
            "51:4a:08:79:8e:34:04:dd:ef:95:19:b3:cd:3a:43:" +
            "1b:30:2b:0a:6d:f2:5f:14:37:4f:e1:35:6d:6d:51:" +
            "c2:45:e4:85:b5:76:62:5e:7e:c6:f4:4c:42:e9:a6:" +
            "3a:36:20:ff:ff:ff:ff:ff:ff:ff:ff:02:01:02:02:" +
            "01:10";

    private static final DHParameters DH_PARAMETERS = new DHParameters(DHGroups.P1, DHGroups.G, 16);

    @Test
    public void computeSharedKey() throws Exception {
        KeyPair keyPairA = DHOperations.generatePublicKey(DH_PARAMETERS);
        KeyPair keyPairB = DHOperations.generatePublicKey(DH_PARAMETERS);
        KeyAgreement keyAgreementA = DHOperations.getInitializedKeyAgreement(keyPairA);
        KeyAgreement keyAgreementB = DHOperations.getInitializedKeyAgreement(keyPairB);
        byte[] sharedKeyA = DHOperations.computeSharedKey(keyAgreementA, DHOperations.keyToBytes(keyPairB.getPublic()), DH_PARAMETERS);
        byte[] sharedKeyB = DHOperations.computeSharedKey(keyAgreementB, DHOperations.keyToBytes(keyPairA.getPublic()), DH_PARAMETERS);
        assertArrayEquals(sharedKeyA, sharedKeyB);
    }

    @Test
    public void createKeyPair() throws Exception {
        DHParameters pBER = DHParameters.getDHParametersFromBER(OctetString.fromHexString(DH_DEFAULT_PARAMETERS));
        assertEquals(DH_PARAMETERS, pBER);
        KeyPair keyPairA = DHOperations.generatePublicKey(pBER);
        KeyPair keyPairB = DHOperations.createKeyPair(DHOperations.derivePublicKey(keyPairA), DHOperations.derivePrivateKey(keyPairA), pBER);
        KeyAgreement keyAgreementA = DHOperations.getInitializedKeyAgreement(keyPairA);
        KeyAgreement keyAgreementB = DHOperations.getInitializedKeyAgreement(keyPairB);
        byte[] sharedKeyA = DHOperations.computeSharedKey(keyAgreementA, DHOperations.keyToBytes(keyPairB.getPublic()), DH_PARAMETERS);
        byte[] sharedKeyB = DHOperations.computeSharedKey(keyAgreementB, DHOperations.keyToBytes(keyPairA.getPublic()), DH_PARAMETERS);
        assertArrayEquals(sharedKeyA, sharedKeyB);
    }

    @Test
    public void dhParameters() throws Exception {
        DHParameters pBER = DHParameters.getDHParametersFromBER(OctetString.fromHexString(DH_DEFAULT_PARAMETERS));
        assertEquals(DH_PARAMETERS, pBER);
        KeyPair keyPairA = DHOperations.generatePublicKey(pBER);
        KeyPair keyPairB = DHOperations.createKeyPair(DHOperations.derivePublicKey(keyPairA), DHOperations.derivePrivateKey(keyPairA), pBER);
        assertEquals(keyPairA.getPrivate(), keyPairB.getPrivate());
        assertEquals(keyPairA.getPublic(), keyPairB.getPublic());
    }

}
