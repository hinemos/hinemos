/*_############################################################################
  _## 
  _##  SNMP4J - TlsTmSecurityCallback.java  
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
 * The {@code TlsTmSecurityCallback} is implemented by the SnmpTlsMib (of SNMP4J-Agent), for example, to resolve
 * (lookup) the {@code tmSecurityName} for incoming requests.
 *
 * @param <C>
 *         The certificate type supported by this callback.
 *
 * @author Frank Fock
 * @version 3.3
 * @since 2.0
 */
public interface TlsTmSecurityCallback<C extends Certificate> {

    /**
     * Gets the tmSecurityName (see RFC 5953) from the certificate chain of the communication peer that needs to be
     * authenticated.
     *
     * @param peerCertificateChain
     *         an array of {@link Certificate}s with the peer's own certificate first followed by any CA authorities.
     *
     * @return the tmSecurityName as defined by RFC 5953.
     */
    OctetString getSecurityName(C[] peerCertificateChain);

    /**
     * Check if the supplied peer end certificate is accepted as client.
     *
     * @param peerEndCertificate
     *         a client Certificate instance to check acceptance for.
     *
     * @return {@code true} if the certificate is accepted, {@code false} otherwise, i.e. if verification could not
     * performed, i.e. because it was not configured sufficiently.
     * @throws CertificateException if the certificate is rejected.
     */
    boolean isClientCertificateAccepted(C peerEndCertificate) throws CertificateException;

    /**
     * Check if the supplied peer certificate chain is accepted as server.
     *
     * @param peerCertificateChain
     *         a server Certificate chain to check acceptance for.
     *
     * @return {@code true} if the certificate is accepted, {@code false} otherwise, i.e. if verification could not
     * performed, i.e. because it was not configured sufficiently.
     * @throws CertificateException if the certificate is rejected.
     */
    boolean isServerCertificateAccepted(C[] peerCertificateChain) throws CertificateException;

    /**
     * Check if the supplied issuer certificate is accepted as server.
     *
     * @param issuerCertificate
     *         an issuer Certificate instance to check acceptance for.
     *
     * @return {@code true} if the certificate is accepted, {@code false} otherwise, i.e. if verification could not
     * performed, i.e. because it was not configured sufficiently.
     * @throws CertificateException if the certificate is rejected.
     */
    boolean isAcceptedIssuer(C issuerCertificate) throws CertificateException;

    /**
     * Gets the local certificate alias to be used for the supplied target address.
     *
     * @param targetAddress
     *         a target address or {@code null} if the default local certificate alias needs to be retrieved.
     *
     * @return the requested local certificate alias, if known. Otherwise {@code null} is returned which could cause a
     * protocol violation if the local key store contains more than one certificate.
     */
    String getLocalCertificateAlias(Address targetAddress);

}
