/*_############################################################################
  _## 
  _##  SNMP4J - TestOctetString.java  
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

import junit.framework.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

public class TestOctetString
    extends TestCase {
  private OctetString octetString = null;

  protected void setUp() throws Exception {
    super.setUp();
    octetString = new OctetString();
  }

  protected void tearDown() throws Exception {
    octetString = null;
    super.tearDown();
  }

  public void testConstructors() {
    byte[] ba = {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'};

    octetString = new OctetString(ba);

    assertEquals(octetString.toString(), "abcdefghi");

    octetString = new OctetString(ba, 2, 2);
    assertEquals(octetString.toString(), "cd");
  }

  public void testSlip() {
    String s = "A short string with several delimiters  and a short word!";
    OctetString sp = new OctetString(s);
    Collection<OctetString> words = OctetString.split(sp, new OctetString("! "));
    StringTokenizer st = new StringTokenizer(s, "! ");
    for (Iterator<OctetString> it = words.iterator(); it.hasNext();) {
      OctetString os = it.next();
      assertEquals(os.toString(), st.nextToken());
    }
    assertFalse(st.hasMoreTokens());
  }

  public void testIsPrintable() {
    OctetString nonPrintable = OctetString.fromHexString("1C:32:41:1C:4E:38");
    assertFalse(nonPrintable.isPrintable());
  }
}
