/*_############################################################################
  _## 
  _##  SNMP4J - SecurityNameMapping.java  
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

package org.snmp4j.transport.tls;

import org.snmp4j.smi.OctetString;

/**
 * The <tt>SecurityNameMapping</tt> maps a X509 certificate identified by its
 * fingerprint to a security name based on a mapping defined by
 * {@link CertMappingType}.
 *
 * @author Frank Fock
 * @since 2.0
 */
public class SecurityNameMapping {

  public enum CertMappingType
  { Specified, SANRFC822Name, SANDNSName, SANIpAddress, SANAny, CommonName }

  private OctetString fingerprint;
  private OctetString data;
  private CertMappingType type;
  private OctetString securityName;

  public SecurityNameMapping(OctetString fingerprint, OctetString data, CertMappingType type,
                             OctetString securityName) {
    this.fingerprint = fingerprint;
    this.data = data;
    this.type = type;
    this.securityName = securityName;
  }

  public OctetString getFingerprint() {
    return fingerprint;
  }

  public OctetString getData() {
    return data;
  }

  public CertMappingType getType() {
    return type;
  }

  public OctetString getSecurityName() {
    return securityName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    org.snmp4j.transport.tls.SecurityNameMapping that = (org.snmp4j.transport.tls.SecurityNameMapping) o;

    if (data != null ? !data.equals(that.data) : that.data != null) return false;
    if (fingerprint != null ? !fingerprint.equals(that.fingerprint) : that.fingerprint != null) return false;
    if (type != that.type) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = fingerprint != null ? fingerprint.hashCode() : 0;
    result = 31 * result + (data != null ? data.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "SecurityNameMapping{" +
        "fingerprint=" + fingerprint +
        ", data=" + data +
        ", type=" + type +
        ", securityName=" + securityName +
        '}';
  }
}
