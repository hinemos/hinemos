/*_############################################################################
  _## 
  _##  SNMP4J - AuthHMAC128SHA224Test.java  
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

import junit.framework.TestCase;

public class AuthHMAC128SHA224Test extends TestCase {

    public void testAuthHMAC128SHA224() throws Exception {
        AuthHMAC128SHA224 authHMAC128SHA224 = new AuthHMAC128SHA224();
        assertTrue(authHMAC128SHA224.isSupported());
    }

}
