/*_############################################################################
  _## 
  _##  SNMP4J - PropertiesTlsTmSecurityCallback.java  
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

import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;
import org.snmp4j.util.SnmpConfigurator;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Properties;

/**
 * The {@code PropertiesTlsTmSecurityCallback} resolves the
 * {@code tmSecurityName} for incoming requests by using the
 * (system) properties
 * {@code org.snmp4j.arg.securityName}
 * {@code org.snmp4j.arg.tlsLocalID}
 * {@code org.snmp4j.arg.tlsTrustCA}
 * {@code org.snmp4j.arg.tlsPeerID}
 *
 * @author Frank Fock
 * @since 2.0
 * @version 3.3.0
 */
public class PropertiesTlsTmSecurityCallback implements TlsTmSecurityCallback<X509Certificate> {

    private static final LogAdapter LOGGER = LogFactory.getLogger(PropertiesTlsTmSecurityCallback.class);

    private boolean serverMode;
    private Properties properties;

    public PropertiesTlsTmSecurityCallback(boolean serverMode) {
        this(System.getProperties(), serverMode);
    }

    public PropertiesTlsTmSecurityCallback(Properties properties, boolean serverMode) {
        this.serverMode = serverMode;
        if (properties == null) {
            throw new NullPointerException();
        }
        this.properties = properties;
    }

    @Override
    public OctetString getSecurityName(X509Certificate[] peerCertificateChain) {
        String securityName = properties.getProperty(SnmpConfigurator.P_SECURITY_NAME, null);
        if (securityName != null) {
            return new OctetString(securityName);
        }
        return null;
    }

    @Override
    public boolean isClientCertificateAccepted(X509Certificate peerEndCertificate) throws CertificateException {
        String accepted = properties.getProperty(SnmpConfigurator.P_TLS_LOCAL_ID, "");
        if (serverMode) {
            accepted = properties.getProperty(SnmpConfigurator.P_TLS_PEER_ID, "");
        }
        if (peerEndCertificate != null && peerEndCertificate.getSubjectDN().getName().equals(accepted)) {
            return true;
        }
        else if (accepted.length() == 0) {
            return false;
        }
        throw new CertificateException("Client certificate "+peerEndCertificate+
                " rejected because subject DN does not match "+accepted);
    }

    @Override
    public boolean isServerCertificateAccepted(X509Certificate[] peerCertificateChain) throws CertificateException {
        if (peerCertificateChain == null || peerCertificateChain.length == 0) {
            throw new CertificateException("Server certificate chain is empty");
        }
        String fingerprint = properties.getProperty(SnmpConfigurator.P_TLS_CERT_FINGERPRINT, "");
        if (fingerprint.length() > 0) {
            OctetString acceptedFingerprint = OctetString.fromHexString(fingerprint);
            int accepted = -1;
            for (X509Certificate cert : peerCertificateChain) {
                OctetString certFingerprint = TLSTMUtil.getFingerprint(cert);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Matching server fingerprint " + certFingerprint + " against accepted " + fingerprint);
                }
                if ((certFingerprint != null) && (certFingerprint.equals(acceptedFingerprint))) {
                    accepted = 1;
                    break;
                }
                else {
                    accepted = 0;
                }
            }
            if (accepted <= 0) {
                throw new CertificateException("Server certificate chain "+ Arrays.asList(peerCertificateChain)+
                        " does not match accepted fingerprint "+acceptedFingerprint);
            }
        }
        String acceptedPeer = properties.getProperty(SnmpConfigurator.P_TLS_PEER_ID, "");
        if (serverMode) {
            acceptedPeer = properties.getProperty(SnmpConfigurator.P_TLS_LOCAL_ID, "");
        }
        String subject = peerCertificateChain[0].getSubjectDN().getName();
        if (subject.equals(acceptedPeer)) {
            return true;
        } else if (acceptedPeer.length() > 0) {
            throw new CertificateException("Certificate subject '" + subject +
                    "' does not match accepted peer '" + acceptedPeer + "'");
        }
        String acceptedCA = properties.getProperty(SnmpConfigurator.P_TLS_TRUST_CA, "");
        if (acceptedCA.length() == 0) {
            return false;
        }
        for (int i = 1; i < peerCertificateChain.length; i++) {
            String ca = peerCertificateChain[i].getIssuerDN().getName();
            if (ca.equals(acceptedCA)) {
                return true;
            } else {
                LOGGER.debug("Certification authority '" + ca + "' does not match accepted CA '" + acceptedCA + "'");
            }
        }
        throw new CertificateException("Certification authorities for certificate chain " +
                Arrays.asList(peerCertificateChain) + " does not match accepted CA '" + acceptedCA + "'");
    }

    @Override
    public boolean isAcceptedIssuer(X509Certificate issuerCertificate) throws CertificateException {
        String acceptedCA = properties.getProperty(SnmpConfigurator.P_TLS_TRUST_CA, "");
        if (acceptedCA.length() == 0) {
            return false;
        }
        if (issuerCertificate != null && issuerCertificate.getIssuerDN().getName().equals(acceptedCA)) {
            return true;
        }
        throw new CertificateException("Issuer certificate "+issuerCertificate+
                " does not have accepted DN: "+acceptedCA);
    }

    @Override
    public String getLocalCertificateAlias(Address targetAddress) {
        return properties.getProperty(SnmpConfigurator.P_TLS_LOCAL_ID);
    }

}
