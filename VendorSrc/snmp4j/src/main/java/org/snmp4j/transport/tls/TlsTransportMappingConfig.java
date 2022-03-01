/*_############################################################################
  _## 
  _##  SNMP4J - TlsTransportMappingConfig.java  
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


import java.security.cert.Certificate;

/**
 * The {@code TlsTransportMappingConfig} interface provides means to plug in a {@link TlsTmSecurityCallback} into
 * the {@link org.snmp4j.TransportMapping} implementation and to control other TLS specific settings.
 *
 * @param <C> the certificate type supported by the {@link TlsTmSecurityCallback} hook provided by this transport
 *           mapping.
 *
 * @author Frank Fock
 * @since 3.0
 */
public interface TlsTransportMappingConfig<C extends Certificate> {

    /**
     * Gets the {@link TlsTmSecurityCallback} associated with this {@link org.snmp4j.TransportMapping} hook which is
     * called by the transport mapping to lookup TLS security parameters from external configuration.
     * @return
     *    a {@link TlsTmSecurityCallback} instance.
     * @since 3.0
     */
    TlsTmSecurityCallback<C> getSecurityCallback();

    /**
     * Sets the {@link TlsTmSecurityCallback} associated with this {@link org.snmp4j.TransportMapping} hook. This hook
     * will be called to lookup the security name based on the TLS peer certificate, for example.
     * See {@link TlsTmSecurityCallback} for details.
     *
     * @param securityCallback
     *   a {@link TlsTmSecurityCallback} instance. Setting this hook to {@code null} will disable incoming request
     *   processing because these request will be rejected due to an authorization error (no mathing SNMPv3 view).
     * @since 3.0
     */
    void setSecurityCallback(TlsTmSecurityCallback<C> securityCallback);

    String getKeyStore();

    void setKeyStore(String keyStore);

    String getKeyStorePassword();

    void setKeyStorePassword(String keyStorePassword);

    String getTrustStore();

    void setTrustStore(String trustStore);

    String getTrustStorePassword();

    void setTrustStorePassword(String trustStorePassword);

    /**
     * Sets the certificate alias used for client and server authentication
     * by this TLSTM. Setting this property to a value other than {@code null}
     * filters out any certificates which are not in the chain of the given
     * alias.
     *
     * @param localCertificateAlias a certificate alias which filters a single certification chain from
     *                              the {@code javax.net.ssl.keyStore} key store to be used to
     *                              authenticate this TLS transport mapping. If {@code null} no
     *                              filtering appears, which could lead to more than a single chain
     *                              available for authentication by the peer, which would violate the
     *                              (D)TLSTM standard requirements.
     */
    void setLocalCertificateAlias(String localCertificateAlias);

    /**
     * Sets the (D)TLS protocols/versions that this {@link TlsTransportMappingConfig} should use during handshake.
     *
     * @param protocolVersions
     *         an array of (D)TLS protocol (version) names supported by the SunJSSE provider.
     *         The order in the array defines which protocol is tried during handshake
     *         first.
     *
     * @since 3.0
     */
    void setProtocolVersions(String[] protocolVersions);

    /**
     * Return the (D)TLS protocol versions used by this transport mapping.
     * @return
     *    an array of SunJSSE TLS/DTLS provider (depending on the transport mapping type).
     */
    String[] getProtocolVersions();

    /**
     * Returns the property name that is used by this transport mapping to determine the protocol versions
     * from system properties.
     * @return
     *    a property name like {@link org.snmp4j.util.SnmpConfigurator#P_TLS_VERSION} or
     *    {@link org.snmp4j.util.SnmpConfigurator#P_DTLS_VERSION}.
     */
    String getProtocolVersionPropertyName();
}
