/*_############################################################################
  _## 
  _##  SNMP4J - TlsTrustManager.java  
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

import org.snmp4j.TransportStateReference;
import org.snmp4j.event.CounterEvent;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.CounterSupport;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OctetString;

import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The {@code TlsTrustManager} verifies the trust for clients and servers connected based on the certificates, and
 * fingerprints provided.
 *
 * @author Frank Fock
 * @version 3.3.0
 */
public class TlsTrustManager implements X509TrustManager {

    private static LogAdapter LOGGER = LogFactory.getLogger(TlsTrustManager.class);

    X509TrustManager trustManager;
    private boolean useClientMode;
    private TransportStateReference tmStateReference;
    private CounterSupport tlstmCounters;
    private TlsTmSecurityCallback<X509Certificate> securityCallback;

    /**
     * Creates a new {@link TlsTrustManager}.
     *
     * @param trustManager
     *         the X509 trust manager to be used to validate certificates.
     * @param useClientMode
     *         determines if the trust is established as client ({@code true}) or server ({@code false}).
     * @param tmStateReference
     *         the {@link TransportStateReference} that optionally contains a {@link TlsTmSecurityCallback} which will
     *         then take precedence over the {@link TlsTmSecurityCallback} provided as parameter (which could then be
     *         {@code null}).
     * @param tlstmCounters
     *         the {@link CounterSupport} for recording events created by this trust manager.
     * @param securityCallback
     *         the {@link TlsTmSecurityCallback} to be used (if {@code tmStateReference} does not provide some) to
     *         validate peers.
     */
    public TlsTrustManager(X509TrustManager trustManager, boolean useClientMode,
                           TransportStateReference tmStateReference, CounterSupport tlstmCounters,
                           TlsTmSecurityCallback<X509Certificate> securityCallback) {
        this.trustManager = trustManager;
        this.useClientMode = useClientMode;
        this.tmStateReference = tmStateReference;
        this.tlstmCounters = tlstmCounters;
        this.securityCallback = securityCallback;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        if ((tmStateReference != null) && (tmStateReference.getCertifiedIdentity() != null)) {
            OctetString fingerprint = tmStateReference.getCertifiedIdentity().getClientFingerprint();
            if (isMatchingFingerprint(x509Certificates, fingerprint, false)) {
                return;
            }
            else {
                tlstmCounters.fireIncrementCounter(new CounterEvent(this,
                        SnmpConstants.snmpTlstmSessionInvalidClientCertificates));
                throw new CertificateException("Client certificate validation by fingerprint failed for '" +
                        x509Certificates[0] + "' (does not match "+fingerprint.toHexString()+")");
            }
        }
        TlsTmSecurityCallback<X509Certificate> callback = getSecurityCallback();
        try {
            if (!useClientMode && (callback != null)) {
                if (callback.isClientCertificateAccepted(x509Certificates[0])) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Client is trusted with certificate '" + x509Certificates[0] + "'");
                    }
                    return;
                }
                else {
                    tlstmCounters.fireIncrementCounter(new CounterEvent(this,
                            SnmpConstants.snmpTlstmSessionInvalidClientCertificates));
                    throw new CertificateException("Client certificate validation by fingerprint failed for '" +
                            x509Certificates[0] + "'");
                }
            }
            trustManager.checkClientTrusted(x509Certificates, s);
        } catch (CertificateException cex) {
            tlstmCounters.fireIncrementCounter(
                    new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpenErrors));
            tlstmCounters.fireIncrementCounter(
                    new CounterEvent(this, SnmpConstants.snmpTlstmSessionInvalidClientCertificates));
            LOGGER.warn("Client certificate validation failed for '" + x509Certificates[0] + "'");
            throw cex;
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        OctetString fingerprint = null;
        if (tmStateReference.getCertifiedIdentity() != null) {
            fingerprint = tmStateReference.getCertifiedIdentity().getServerFingerprint();
            if (isMatchingFingerprint(x509Certificates, fingerprint, true)) {
                return;
            }
        }
        Object entry = null;
        try {
            entry = TLSTMUtil.getSubjAltName(x509Certificates[0].getSubjectAlternativeNames(), 2);
        } catch (CertificateParsingException e) {
            tlstmCounters.fireIncrementCounter(
                    new CounterEvent(this, SnmpConstants.snmpTlstmSessionInvalidServerCertificates));
            LOGGER.warn("CertificateParsingException while verifying server certificate " +
                    Arrays.asList(x509Certificates));
        }
        if (entry == null) {
            X500Principal x500Principal = x509Certificates[0].getSubjectX500Principal();
            if (x500Principal != null) {
                entry = x500Principal.getName();
            }
        }
        if (entry != null && fingerprint != null && fingerprint.length() == 0 &&
                tmStateReference.getCertifiedIdentity() != null &&
                tmStateReference.getCertifiedIdentity().getIdentity() != null) {
            String dNSName = ((String) entry).toLowerCase();
            String hostName = tmStateReference.getCertifiedIdentity().getIdentity().toString();
            if (hostName.length() > 0) {
                if (hostName.charAt(0) == '*') {
                    int pos = dNSName.indexOf('.');
                    if (pos > 0) {
                        dNSName = dNSName.substring(pos);
                    }
                    hostName = hostName.substring(1);
                }
                if (hostName.equalsIgnoreCase(dNSName)) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Peer hostname " + hostName + " matches dNSName " + dNSName);
                    }
                    return;
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Peer hostname " + hostName + " did not match dNSName " + dNSName);
            }
        }
        try {
            trustManager.checkServerTrusted(x509Certificates, s);
        } catch (CertificateException cex) {
            tlstmCounters.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpenErrors));
            tlstmCounters.fireIncrementCounter(
                    new CounterEvent(this, SnmpConstants.snmpTlstmSessionInvalidServerCertificates));
            LOGGER.warn("Server certificate validation failed for '" + x509Certificates[0] + "'");
            throw cex;
        }
        TlsTmSecurityCallback<X509Certificate> callback = getSecurityCallback();
        if (useClientMode && (callback != null)) {
            Boolean isServerCertificateAccepted = callback.isServerCertificateAccepted(x509Certificates);
            if (isServerCertificateAccepted != null && !isServerCertificateAccepted) {
                LOGGER.info("Server is NOT trusted with certificate '" + Arrays.asList(x509Certificates) + "'");
                throw new CertificateException("Server's certificate is not trusted by this application (although it was trusted by the JRE): " +
                        Arrays.asList(x509Certificates));
            }
        }
    }

    private boolean isMatchingFingerprint(X509Certificate[] x509Certificates, OctetString fingerprint,
                                          boolean useClientMode) throws CertificateException
    {
        if ((fingerprint != null) && (fingerprint.length() > 0)) {
            for (X509Certificate cert : x509Certificates) {
                OctetString certFingerprint = null;
                certFingerprint = TLSTMUtil.getFingerprint(cert);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Comparing certificate fingerprint " + certFingerprint +
                            " with " + fingerprint);
                }
                if (certFingerprint == null) {
                    LOGGER.error("Failed to determine fingerprint for certificate " + cert +
                            " and algorithm " + cert.getSigAlgName());
                } else if (certFingerprint.equals(fingerprint)) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Peer is trusted by fingerprint '" + fingerprint + "' of certificate: '" + cert + "'");
                    }
                    return true;
                }
            }
            tlstmCounters.fireIncrementCounter(new CounterEvent(this, (useClientMode) ?
                    SnmpConstants.snmpTlstmSessionInvalidServerCertificates :
                    SnmpConstants.snmpTlstmSessionInvalidClientCertificates));
            throw new CertificateException("No fingerprint of provided certificates "+Arrays.asList(x509Certificates)+
                    " matched "+fingerprint.toHexString());
        }
        return false;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return getAcceptedIssuers(trustManager, getSecurityCallback());
    }

    /**
     * Gets the accepted {@link X509Certificate}s from the given {@link X509TrustManager} and security callback.
     *
     * @param trustManager
     *         a X509TrustManager providing the accepted issuers.
     * @param securityCallback
     *         a security callback that is ask to accept any returned issuer.
     *
     * @return a probably empty or {@code null} array of accepted issuers.
     * @since 3.1.1
     */
    public static X509Certificate[] getAcceptedIssuers(X509TrustManager trustManager,
                                                       TlsTmSecurityCallback<X509Certificate> securityCallback) {
        X509Certificate[] accepted = trustManager.getAcceptedIssuers();
        if ((accepted != null) && (securityCallback != null)) {
            ArrayList<X509Certificate> acceptedIssuers = new ArrayList<>(accepted.length);
            for (X509Certificate cert : accepted) {
                try {
                    if (securityCallback.isAcceptedIssuer(cert)) {
                        acceptedIssuers.add(cert);
                    }
                }
                catch (CertificateException certex) {
                    // ignore
                    LOGGER.debug("Security callback "+securityCallback+" rejected "+cert);
                }
            }
            return acceptedIssuers.toArray(new X509Certificate[0]);
        }
        return accepted;
    }

    protected TlsTmSecurityCallback<X509Certificate> getSecurityCallback() {
        if (tmStateReference.getCertifiedIdentity() instanceof TlsX509CertifiedTarget) {
            TlsX509CertifiedTarget tlsCertifiedTarget =
                    ((TlsX509CertifiedTarget)tmStateReference.getCertifiedIdentity());
            if (tlsCertifiedTarget != null && tlsCertifiedTarget.getTlsTmSecurityCallback() != null) {
                return tlsCertifiedTarget.getTlsTmSecurityCallback();
            }
        }
        return securityCallback;
    }
}
