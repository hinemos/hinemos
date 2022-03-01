/*_############################################################################
  _## 
  _##  SNMP4J - TlsX509CertifiedTarget.java  
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

import org.snmp4j.CertifiedTarget;
import org.snmp4j.Target;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;

import java.security.cert.X509Certificate;

/**
 * The {@code TlsCertifiedTarget} extends the {@link org.snmp4j.CertifiedTarget} class by means to provide
 * a {@link TlsTmSecurityCallback} reference directly with the target as needed according to RFC 6353 §5.3.1 when
 * establishing a connection based on the SNMP-TARGET-MIB as client. If the provided {@link TlsTmSecurityCallback}
 * is {@code null} this class behaves identical to its superclass {@link CertifiedTarget}.
 *
 * @author Frank Fock
 * @since 3.3.0
 */
public class TlsX509CertifiedTarget extends CertifiedTarget {

    private static final long serialVersionUID = -1980959130605037036L;
    private TlsTmSecurityCallback<X509Certificate> tlsTmSecurityCallback;

    public TlsX509CertifiedTarget(Address address, OctetString identity,
                              OctetString serverFingerprint, OctetString clientFingerprint,
                              TlsTmSecurityCallback<X509Certificate> tlsTmSecurityCallback) {
        super(address, identity, serverFingerprint, clientFingerprint);
        this.tlsTmSecurityCallback = tlsTmSecurityCallback;
    }

    /**
     * Gets the {@link TlsTmSecurityCallback} information needed to validate a client-server connection.
     * @return a {@link TlsTmSecurityCallback} instance or {@code null} if such information is not provided then
     * the transport mapping has to block (drop) the connection creation.
     */
    public TlsTmSecurityCallback<X509Certificate> getTlsTmSecurityCallback() {
        return tlsTmSecurityCallback;
    }

    @Override
    public Target duplicate() {
        TlsX509CertifiedTarget copy =
                new TlsX509CertifiedTarget(this.getAddress(), getIdentity(),
                        getServerFingerprint(), getClientFingerprint(), tlsTmSecurityCallback);
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
