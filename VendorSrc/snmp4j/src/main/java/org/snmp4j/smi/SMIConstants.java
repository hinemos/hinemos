/*_############################################################################
  _## 
  _##  SNMP4J - SMIConstants.java  
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

import org.snmp4j.asn1.BER;

/**
 * The <code>SMIConstants</code> defines the tag values for SMI syntax types.
 *
 * @author Frank Fock
 * @version 1.5
 */
public final class SMIConstants {

  public static final int SYNTAX_INTEGER = BER.ASN_INTEGER;
  public static final int SYNTAX_OCTET_STRING = BER.ASN_OCTET_STR;
  public static final int SYNTAX_NULL = BER.ASN_NULL;
  public static final int SYNTAX_OBJECT_IDENTIFIER = BER.ASN_OBJECT_ID;

  public static final int SYNTAX_IPADDRESS = BER.IPADDRESS;
  public static final int SYNTAX_INTEGER32 = BER.ASN_INTEGER;
  public static final int SYNTAX_COUNTER32 = BER.COUNTER32;
  public static final int SYNTAX_GAUGE32   = BER.GAUGE32;
  public static final int SYNTAX_UNSIGNED_INTEGER32 = BER.GAUGE32;
  public static final int SYNTAX_TIMETICKS = BER.TIMETICKS;
  public static final int SYNTAX_OPAQUE = BER.OPAQUE;
  public static final int SYNTAX_COUNTER64 = BER.COUNTER64;

  public static final int SYNTAX_BITS = SYNTAX_OCTET_STRING;

  public static final int EXCEPTION_NO_SUCH_OBJECT = BER.NOSUCHOBJECT;
  public static final int EXCEPTION_NO_SUCH_INSTANCE = BER.NOSUCHINSTANCE;
  public static final int EXCEPTION_END_OF_MIB_VIEW = BER.ENDOFMIBVIEW;

}

