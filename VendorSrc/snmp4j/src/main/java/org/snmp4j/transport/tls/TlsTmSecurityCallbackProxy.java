/*_############################################################################
  _## 
  _##  SNMP4J - TlsTmSecurityCallbackProxy.java  
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

import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * The {@code TlsTmSecurityCallbackProxy} class implements a {@link TlsTmSecurityCallback} by using an
 * internal reference to another {@link TlsTmSecurityCallback} instance. It can be used to defer the creation
 * of the backing security callback to a later time.
 *
 * @param <C> the certificate class supported by this security callback.
 * @author Frank Fock
 * @version 3.3.0
 * @since 3.2.0
 */
public class TlsTmSecurityCallbackProxy<C extends Certificate> implements TlsTmSecurityCallback<C> {

    private TlsTmSecurityCallback<C> tlsTmSecurityCallback;

    public TlsTmSecurityCallbackProxy() {
    }

    public TlsTmSecurityCallback<C> getTlsTmSecurityCallback() {
        return tlsTmSecurityCallback;
    }

    /**
     * Sets the security callback to be used when this proxy is being called.
     * @param tlsTmSecurityCallback
     *         the actually used security callback. If {@code null}, then the security callback methods will always
     *         return {@code false} and {@code null} respectively.
     */
    public void setTlsTmSecurityCallback(TlsTmSecurityCallback<C> tlsTmSecurityCallback) {
        this.tlsTmSecurityCallback = tlsTmSecurityCallback;
    }

    @Override
    public OctetString getSecurityName(C[] peerCertificateChain) {
        return (tlsTmSecurityCallback == null) ? null : tlsTmSecurityCallback.getSecurityName(peerCertificateChain);
    }

    @Override
    public boolean isClientCertificateAccepted(C peerEndCertificate) throws CertificateException {
        return (tlsTmSecurityCallback != null) && tlsTmSecurityCallback.isClientCertificateAccepted(peerEndCertificate);
    }

    @Override
    public boolean isServerCertificateAccepted(C[] peerCertificateChain) throws CertificateException {
        return (tlsTmSecurityCallback != null) && tlsTmSecurityCallback.isServerCertificateAccepted(peerCertificateChain);
    }

    @Override
    public boolean isAcceptedIssuer(C issuerCertificate) throws CertificateException {
        return (tlsTmSecurityCallback != null) && tlsTmSecurityCallback.isAcceptedIssuer(issuerCertificate);
    }

    @Override
    public String getLocalCertificateAlias(Address targetAddress) {
        return (tlsTmSecurityCallback == null) ? null : tlsTmSecurityCallback.getLocalCertificateAlias(targetAddress);
    }
}
