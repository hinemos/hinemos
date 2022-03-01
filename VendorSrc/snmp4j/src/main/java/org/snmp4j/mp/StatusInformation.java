/*_############################################################################
  _## 
  _##  SNMP4J - StatusInformation.java  
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
package org.snmp4j.mp;

import org.snmp4j.smi.VariableBinding;
import org.snmp4j.smi.Integer32;
import java.io.Serializable;

/**
 * The <code>StatusInformation</code> class represents status information
 * of a SNMPv3 message that is needed to return a report message.
 * @author Frank Fock
 * @version 1.0
 */
public class StatusInformation implements Serializable {

  private static final long serialVersionUID = 9053403603288070071L;

  private VariableBinding errorIndication;
  private byte[] contextName;
  private byte[] contextEngineID;
  private Integer32 securityLevel;

  public StatusInformation() {
  }

  public StatusInformation(VariableBinding errorIndication,
                           byte[] contextName,
                           byte[] contextEngineID,
                           Integer32 securityLevel) {
    this.errorIndication = errorIndication;
    this.contextName = contextName;
    this.contextEngineID = contextEngineID;
    this.securityLevel = securityLevel;
  }

  public VariableBinding getErrorIndication() {
    return errorIndication;
  }
  public void setErrorIndication(VariableBinding errorIndication) {
    this.errorIndication = errorIndication;
  }
  public void setContextName(byte[] contextName) {
    this.contextName = contextName;
  }
  public byte[] getContextName() {
    return contextName;
  }
  public void setContextEngineID(byte[] contextEngineID) {
    this.contextEngineID = contextEngineID;
  }
  public byte[] getContextEngineID() {
    return contextEngineID;
  }
  public void setSecurityLevel(org.snmp4j.smi.Integer32 securityLevel) {
    this.securityLevel = securityLevel;
  }
  public org.snmp4j.smi.Integer32 getSecurityLevel() {
    return securityLevel;
  }

  public String toString() {
    if (errorIndication == null) {
      return "noError";
    }
    return errorIndication.toString();
  }
}

