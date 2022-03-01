/*_############################################################################
  _## 
  _##  SNMP4J - TestCounter64.java  
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


package org.snmp4j.smi;

import junit.framework.TestCase;
import org.snmp4j.asn1.BER;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class TestCounter64 extends TestCase {

  public TestCounter64(String s) {
    super(s);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testToString() {
    Counter64 counter64 = new Counter64(0xFFFFFFFFFFFFFFFFl);
    String stringRet = counter64.toString();
    assertEquals("18446744073709551615", stringRet);
  }
  public void testCompareTo() {
    Counter64 counter64 = new Counter64(0xFFFFFFFFFFFFFFFFl);
    Counter64 counter32 = new Counter64(0x00000000FFFFFFFFl);
    Counter64 counter0=  new Counter64(0);
    Counter64 counter1=  new Counter64(1);
    int intRet = counter64.compareTo(counter0);
    assertEquals(1, intRet);
    assertEquals(-1, counter0.compareTo(counter64));
    assertEquals(-1, counter32.compareTo(counter64));
    assertEquals(1, counter64.compareTo(counter32));
    assertEquals(0, counter32.compareTo(counter32));
    assertEquals(0, counter64.compareTo(counter64));
    assertEquals(0, counter0.compareTo(counter0));
    assertEquals(1, counter1.compareTo(counter0));
    assertEquals(-1, counter0.compareTo(counter1));

    long l = 0;
    for (int i=0; i<64; i++) {
      Counter64 lesser = new Counter64(l);
      Counter64 greater = new Counter64(1l << i);
      assertEquals(-1, lesser.compareTo(greater));
      assertEquals(1, greater.compareTo(lesser));
      l = greater.getValue();
    }
  }
  public void testEquals() {
    Counter64 counter64 = new Counter64(0xFFFFFFFFFFFFFFFFl);
    Variable o1=  new Counter64(0xFFFFFFFFFFFFFFFFl);
    boolean booleanRet = counter64.equals(o1);
    assertTrue(booleanRet);
  }

  public void testBER0() {
    ByteArrayOutputStream bos64 = new ByteArrayOutputStream();
    try {
      BER.encodeUnsignedInt64(bos64, (byte) SMIConstants.SYNTAX_COUNTER64, 0);
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    OctetString os64 = new OctetString(bos64.toByteArray());
    ByteArrayOutputStream bos32 = new ByteArrayOutputStream();
    try {
      BER.encodeUnsignedInteger(bos32, (byte) SMIConstants.SYNTAX_COUNTER64, 0);
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    OctetString os32 = new OctetString(bos32.toByteArray());
    assertEquals(os32, os64);

  }

  public void testBER3() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      BER.encodeUnsignedInt64(bos, (byte) SMIConstants.SYNTAX_COUNTER64, 3);
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    OctetString os = new OctetString(bos.toByteArray());
    assertEquals(OctetString.fromHexString("46:01:03"), os);

  }

}
