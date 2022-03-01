/*_############################################################################
  _## 
  _##  SNMP4J - SMIAddress.java  
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

/**
 * A <code>SMIAddress</code> is an address that is defined by the Structure
 * of Management Information (SMI) and can be thereby serialized through the
 * Basic Encoding Rules (BER) used by the SNMP protocol.
 *
 * @author Frank Fock
 * @version 1.8
 * @since 1.5
 */
public abstract class SMIAddress extends AbstractVariable implements Address {

}
