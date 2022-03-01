/*_############################################################################
  _## 
  _##  SNMP4J - CertifiedTarget.java  
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

package org.snmp4j;

import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TlsAddress;
import org.snmp4j.transport.TLSTM;

import java.io.Serializable;

/**
 * The <code>CertifiedTarget</code> class implements a {@link SecureTarget}
 * for usage with {@link org.snmp4j.security.SecurityModel}s that support
 * secured connections using client and server certificates.
 *
 * @author Frank Fock
 * @version 2.7.1
 * @since 2.0
 */
public class CertifiedTarget extends SecureTarget implements CertifiedIdentity, Serializable {

  private static final long serialVersionUID = 1L;

  private OctetString serverFingerprint;
  private OctetString clientFingerprint;

  public CertifiedTarget(OctetString identity) {
    super(new TlsAddress(), identity);
    this.setSecurityModel(4);
  }

  public CertifiedTarget(Address address, OctetString identity,
                         OctetString serverFingerprint, OctetString clientFingerprint) {
    super(address, identity);
    this.serverFingerprint = serverFingerprint;
    this.clientFingerprint = clientFingerprint;
    this.setSecurityModel(4);
  }

  public OctetString getServerFingerprint() {
    return serverFingerprint;
  }

  public OctetString getClientFingerprint() {
    return clientFingerprint;
  }

  public OctetString getIdentity() {
    return super.getSecurityName();
  }

  @Override
  public String toString() {
    return "CertifiedTarget[" + toStringAbstractTarget()+
        ", serverFingerprint=" + serverFingerprint +
        ", clientFingerprint=" + clientFingerprint +
        ']';
  }

  @Override
  public Target duplicate() {
    CertifiedTarget copy =
            new CertifiedTarget(this.getAddress(), getIdentity(), serverFingerprint, clientFingerprint);
    copy.setRetries(getRetries());
    copy.setTimeout(getTimeout());
    copy.setMaxSizeRequestPDU(getMaxSizeRequestPDU());
    copy.setPreferredTransports(getPreferredTransports());
    copy.setVersion(getVersion());
    copy.setSecurityLevel(getSecurityLevel());
    copy.setSecurityModel(getSecurityModel());
    return copy;
  }


}
