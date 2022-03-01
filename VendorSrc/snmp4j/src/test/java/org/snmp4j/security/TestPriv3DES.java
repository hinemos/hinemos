/*_############################################################################
  _## 
  _##  SNMP4J - TestPriv3DES.java  
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

package org.snmp4j.security;

import junit.framework.*;
import org.snmp4j.smi.OctetString;
import org.snmp4j.log.LogFactory;
import org.snmp4j.log.JavaLogFactory;
import org.snmp4j.asn1.BER;

public class TestPriv3DES
    extends TestCase {
     static {
       BER.setCheckSequenceLength(false);
     }

  public static String asHex(byte buf[]) {
    return new OctetString(buf).toHexString();
  }

  public TestPriv3DES(String name) {
    super(name);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public static void testKey()
  {
    SecurityProtocols protos = SecurityProtocols.getInstance();
    protos.addDefaultProtocols();
    protos.addPrivacyProtocol(new Priv3DES());
    OctetString engineid = OctetString.fromHexString("00:00:00:00:00:00:00:00:00:00:00:02");
    OctetString password = new OctetString("maplesyrup");
    byte[] expectedKey =
        OctetString.fromHexString("52:6f:5e:ed:9f:cc:e2:6f:89:64:c2:93:07:87:d8:2b:79:ef:f4:4a:90:65:0e:e0:a3:a4:0a:bf:ac:5a:cc:12").toByteArray();
    byte[] key = protos.passwordToKey(Priv3DES.ID, AuthMD5.ID, password, engineid.toByteArray());

    for (int i = 0; i < expectedKey.length; i++) {
      assertEquals(expectedKey[i], key[i]);
    }
  }

  public static void testEncrypt()
  {

      Priv3DES pd = new Priv3DES();
      DecryptParams pp = new DecryptParams();
      byte[] key = "12345678901234561234567890123456".getBytes();
      byte[] plaintext =
          "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".
          getBytes();
      byte[] ciphertext = null;
      byte[] decrypted = null;
      int engine_boots = 1;
      int engine_time = 2;

      ciphertext = pd.encrypt(plaintext, 0, plaintext.length, key, engine_boots, engine_time, pp);
      decrypted = pd.decrypt(ciphertext, 0, ciphertext.length, key, engine_boots, engine_time, pp);

      for (int i = 0; i < plaintext.length; i++) {
        assertEquals(plaintext[i], decrypted[i]);
      }
      assertEquals(8, pp.length);
    }
  /*
    public static void testPasswordToKeyMD5() {

      AuthMD5 md5 = new AuthMD5();
      Priv3DES tripleDES = new Priv3DES();

      byte[] engineID = {
          (byte)00, (byte)00, (byte)00, (byte)00,
          (byte)00, (byte)00, (byte)00, (byte)00,
          (byte)00, (byte)00, (byte)00, (byte)02 };

      byte[] key = SecurityProtocols.getInstance().
          passwordToKey(tripleDES.getID(), md5.getID(),
                        new OctetString("maplesyrup"), engineID);
      OctetString expectedKey = OctetString.fromHexString(
      "52 6f 5e ed 9f cc e2 6f 89 64 c2 93 07 87 d8 2b " +
      "79 ef f4 4a 90 65 0e e0 a3 a4 0a bf ac 5a cc 12", ' ');

      assertEquals(expectedKey, new OctetString(key));

    }

    public static void testPasswordToKeySHA() {
      AuthSHA sha = new AuthSHA();
      Priv3DES tripleDES = new Priv3DES();

      byte[] engineID = {
          (byte)00, (byte)00, (byte)00, (byte)00,
          (byte)00, (byte)00, (byte)00, (byte)00,
          (byte)00, (byte)00, (byte)00, (byte)02 };

      byte[] key = SecurityProtocols.getInstance().
          passwordToKey(tripleDES.getID(), sha.getID(),
                        new OctetString("maplesyrup"), engineID);
      OctetString expectedKey = OctetString.fromHexString(
      "66 95 fe bc 92 88 e3 62 82 23 5f c7 15 1f 12 84 97 b3 8f 3f " +
      "9b 8b 6d 78 93 6b a6 e7 d1 9d fd 9c d2 d5 06 55 47 74 3f b5", ' ');

      assertEquals(expectedKey, new OctetString(key));

    }

    public static void testKeyChange() {

      AuthSHA sha = new AuthSHA();
      Priv3DES tripleDES = new Priv3DES();

      byte[] engineID = {
          (byte)00, (byte)00, (byte)00, (byte)00,
          (byte)00, (byte)00, (byte)00, (byte)00,
          (byte)00, (byte)00, (byte)00, (byte)02 };

      byte[] oldKey = SecurityProtocols.getInstance().
          passwordToKey(tripleDES.getID(), sha.getID(),
                        new OctetString("maplesyrup"), engineID);
      byte[] key = SecurityProtocols.getInstance().
          passwordToKey(tripleDES.getID(), sha.getID(),
                        new OctetString("newsyrup"), engineID);
      OctetString expectedKey = OctetString.fromHexString(
      "78 e2 dc ce 79 d5 94 03 b5 8c 1b ba a5 bf f4 63"+
      " 91 f1 cd 25 97 74 35 55 f9 fc f9 4a c3 e7 e9 22", ' ');
      assertEquals(expectedKey, new OctetString(key));

      OctetString random = OctetString.fromHexString(
      "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "+
      "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00", ' ');
      byte[] delta = sha.changeDelta(oldKey, key, random.getValue());
      OctetString expected = OctetString.fromHexString(
      "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "+
      "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "+
      "ce 13 28 fb 9a 9c 19 ce c1 51 a3 5a 77 f9 20 39 "+
      "ca ff 00 c9 b3 9b 19 a0 5e 01 75 55 94 37 6a 57", ' ');
      assertEquals(expected, new OctetString(delta));
    }
    */
}
