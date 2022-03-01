/*_############################################################################
  _## 
  _##  SNMP4J - TestPrivDES.java  
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
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.smi.OctetString;

public class TestPrivDES extends TestCase {

  private static LogAdapter cat = LogFactory.getLogger(TestPrivDES.class);

  public static String asHex(byte buf[]) {
    return new OctetString(buf).toHexString();
  }

  public TestPrivDES(String name) {
    super(name);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public static void testEncrypt()
  {
      PrivDES pd = new PrivDES();
      DecryptParams pp = new DecryptParams();
      byte[] key = "1234567890123456".getBytes();
      byte[] plaintext =
          "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".
          getBytes();
      byte[] ciphertext = null;
      byte[] decrypted = null;
      int engine_boots = 1;
      int engine_time = 2;

      cat.debug("Cleartext: " + asHex(plaintext));
      ciphertext = pd.encrypt(plaintext, 0, plaintext.length, key, engine_boots, engine_time, pp);
      cat.debug("Encrypted: " + asHex(ciphertext));
      decrypted = pd.decrypt(ciphertext, 0, ciphertext.length, key, engine_boots, engine_time, pp);
      cat.debug("Cleartext: " + asHex(decrypted));

      for (int i = 0; i < plaintext.length; i++) {
	      assertEquals(plaintext[i], decrypted[i]);
      }
      cat.info("pp length is: " + pp.length);
      assertEquals(8, pp.length);
    }
}
