/*_############################################################################
  _## 
  _##  SNMP4J - TestOID.java  
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
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.SnmpConstants;

/**
 * @author Frank Fock
 * @version 1.0
 */

public class TestOID extends TestCase {

  private final static LogAdapter logger = LogFactory.getLogger(TestOID.class);

  private OID oID = null;

  protected void setUp() throws Exception {
    super.setUp();
    oID = new OID(SnmpConstants.usmStatsUnknownEngineIDs);
  }

  protected void tearDown() throws Exception {
    oID = null;
    super.tearDown();
  }

  public void testCompareTo() {
    OID o = SnmpConstants.usmStatsNotInTimeWindows;
    int expectedReturn = 1;
    int actualReturn = oID.compareTo(o);
    assertEquals(expectedReturn, actualReturn);
    o = SnmpConstants.usmStatsUnknownEngineIDs;
    expectedReturn = 0;
    actualReturn = oID.compareTo(o);
    assertEquals(expectedReturn, actualReturn);
    o = SnmpConstants.usmStatsWrongDigests;
    expectedReturn = -1;
    actualReturn = oID.compareTo(o);
    assertEquals(expectedReturn, actualReturn);

    OID a = new OID(new int[]{ 1,2,3,6,0x80000000});
    OID b = new OID(new int[]{ 1,2,3,6,0x80000001});
    expectedReturn = 1;
    actualReturn = b.compareTo(a);
    assertEquals(expectedReturn, actualReturn);

    expectedReturn = -1;
    actualReturn = a.compareTo(b);
    assertEquals(expectedReturn, actualReturn);
  }

  public void testLeftMostCompare() {
    OID other = SnmpConstants.snmpInASNParseErrs;
    int n = Math.min(other.size(), oID.size());
    int expectedReturn = 1;
    int actualReturn = oID.leftMostCompare(n, other);
    assertEquals(expectedReturn, actualReturn);
  }

  public void testRightMostCompare() {
    int n = 2;
    OID other = SnmpConstants.usmStatsUnsupportedSecLevels;
    int expectedReturn = 1;
    int actualReturn = oID.rightMostCompare(n, other);
    assertEquals(expectedReturn, actualReturn);
  }

  public void testPredecessor() {
    OID oid = new OID("1.3.6.4.1.5");
    printOIDs(oid);
    assertEquals(oid.predecessor().successor(), oid);
    oid = new OID("1.3.6.4.1.5.0");
    printOIDs(oid);
    assertEquals(oid.predecessor().successor(), oid);
    oid = new OID("1.3.6.4.1.5.2147483647");
    printOIDs(oid);
    assertEquals(oid.predecessor().successor(), oid);
  }

  private static void printOIDs(OID oid) {
    if (logger.isDebugEnabled()) {
      logger.debug("OID="+oid+", predecessor="+oid.predecessor()+
                   ",successor="+oid.successor());
    }
  }

  public void testStartsWith() {
    OID other = new OID(SnmpConstants.usmStatsDecryptionErrors.getValue());
    other.removeLast();
    other.removeLast();
    boolean expectedReturn = true;
    boolean actualReturn = oID.startsWith(other);
    assertEquals("Return value", expectedReturn, actualReturn);

    other = new OID(SnmpConstants.usmStatsUnknownEngineIDs.getValue());
    expectedReturn = true;
    actualReturn = oID.startsWith(other);
    assertEquals("Return value", expectedReturn, actualReturn);

    other = new OID(SnmpConstants.usmStatsUnknownEngineIDs.getValue());
    other.append("33.44");
    expectedReturn = false;
    actualReturn = oID.startsWith(other);
    assertEquals("Return value", expectedReturn, actualReturn);
  }

  public void testStringParse() {
    OID a = new OID("1.3.6.2.1.5.'hallo'.1");
    OID b = new OID("1.3.6.2.1.5.104.97.108.108.111.1");
    assertEquals(a, b);
    a = new OID("1.3.6.2.1.5.'hal.lo'.1");
    b = new OID("1.3.6.2.1.5.104.97.108.46.108.111.1");
    assertEquals(a, b);
    a = new OID("1.3.6.2.1.5.'hal.'.'''.'lo'.1");
    b = new OID("1.3.6.2.1.5.104.97.108.46.39.108.111.1");
    assertEquals(a, b);
  }

}
