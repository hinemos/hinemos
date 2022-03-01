/*_############################################################################
  _## 
  _##  SNMP4J - UsmSecurityStateReference.java  
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
package org.snmp4j.security;

/**
 * The <code>UsmSecurityStateReference</code> holds cached security data
 * for the {@link USM} security model.
 *
 * @author Frank Fock
 * @version 1.1
 */
public class UsmSecurityStateReference implements SecurityStateReference {

  private byte[] userName;
  private byte[] securityName;
  private byte[] securityEngineID;
  private AuthenticationProtocol authenticationProtocol;
  private PrivacyProtocol privacyProtocol;
  private byte[] authenticationKey;
  private byte[] privacyKey;
  private int securityLevel;

  public UsmSecurityStateReference() {
  }
  public byte[] getUserName() {
    return userName;
  }
  public void setUserName(byte[] userName) {
    this.userName = userName;
  }
  public void setSecurityName(byte[] securityName) {
    this.securityName = securityName;
  }
  public byte[] getSecurityName() {
    return securityName;
  }
  public void setSecurityEngineID(byte[] securityEngineID) {
    this.securityEngineID = securityEngineID;
  }
  public byte[] getSecurityEngineID() {
    return securityEngineID;
  }
  public void setAuthenticationProtocol(AuthenticationProtocol authenticationProtocol) {
    this.authenticationProtocol = authenticationProtocol;
  }
  public AuthenticationProtocol getAuthenticationProtocol() {
    return authenticationProtocol;
  }
  public void setPrivacyProtocol(PrivacyProtocol privacyProtocol) {
    this.privacyProtocol = privacyProtocol;
  }
  public PrivacyProtocol getPrivacyProtocol() {
    return privacyProtocol;
  }
  public void setAuthenticationKey(byte[] authenticationKey) {
    this.authenticationKey = authenticationKey;
  }
  public byte[] getAuthenticationKey() {
    return authenticationKey;
  }
  public void setPrivacyKey(byte[] privacyKey) {
    this.privacyKey = privacyKey;
  }
  public byte[] getPrivacyKey() {
    return privacyKey;
  }
  public void setSecurityLevel(int securityLevel) {
    this.securityLevel = securityLevel;
  }
  public int getSecurityLevel() {
    return securityLevel;
  }
}
