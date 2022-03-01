/*_############################################################################
  _## 
  _##  SNMP4J - TLSTMExtendedTrustManager.java  
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

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * TLSTM trust manager that implements the X509ExtendedTrustManager interface.
 *
 * @author Frank Fock
 * @since 2.5.7
 */
public class TLSTMExtendedTrustManager extends X509ExtendedTrustManager {

    private static final LogAdapter logger = LogFactory.getLogger(TLSTMExtendedTrustManager.class);

    X509TrustManager trustManager;
    private boolean useClientMode;
    private TransportStateReference tmStateReference;
    private CounterSupport tlstmCounters;
    private TlsTmSecurityCallback<X509Certificate> securityCallback;

    public TLSTMExtendedTrustManager(CounterSupport tlstmCounters,
                                     TlsTmSecurityCallback<X509Certificate> securityCallback,
                                     X509TrustManager trustManager,
                                     boolean useClientMode, TransportStateReference tmStateReference) {
        this.tlstmCounters = tlstmCounters;
        this.securityCallback = securityCallback;
        this.trustManager = trustManager;
        this.useClientMode = useClientMode;
        this.tmStateReference = tmStateReference;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        if (!checkClientTrustedIntern(x509Certificates)) {
            try {
                trustManager.checkClientTrusted(x509Certificates, s);
            } catch (CertificateException cex) {
                tlstmCounters.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpenErrors));
                tlstmCounters.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionInvalidClientCertificates));
                logger.warn("Client certificate validation failed for '" + x509Certificates[0] + "'");
                throw cex;
            }
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        if (preCheckServerTrusted(x509Certificates)) return;
        try {
            trustManager.checkServerTrusted(x509Certificates, s);
        } catch (CertificateException cex) {
            tlstmCounters.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpenErrors));
            tlstmCounters.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionInvalidServerCertificates));
            logger.warn("Server certificate validation failed for '" + x509Certificates[0] + "'");
            throw cex;
        }
        postCheckServerTrusted(x509Certificates);
    }

    private boolean isMatchingFingerprint(X509Certificate[] x509Certificates, OctetString fingerprint,
                                          boolean useClientMode) throws CertificateException
    {
        return TLSTMUtil.isMatchingFingerprint(x509Certificates, fingerprint, useClientMode, tlstmCounters, logger, this);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return TlsTrustManager.getAcceptedIssuers(trustManager, securityCallback);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
        logger.debug("checkClientTrusted with socket");
        if (!checkClientTrustedIntern(x509Certificates)) {
            try {
                if (trustManager instanceof X509ExtendedTrustManager) {
                    logger.debug("Extended checkClientTrusted with socket");
                    ((X509ExtendedTrustManager) trustManager).checkClientTrusted(x509Certificates, s, socket);
                } else {
                    trustManager.checkClientTrusted(x509Certificates, s);
                }
            } catch (CertificateException cex) {
                tlstmCounters.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpenErrors));
                tlstmCounters.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionInvalidClientCertificates));
                logger.warn("Client certificate validation failed for '" + x509Certificates[0] + "'");
                throw cex;
            }
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
        logger.debug("checkClientTrusted with socket");
        if (preCheckServerTrusted(x509Certificates)) return;
        try {
            if (trustManager instanceof X509ExtendedTrustManager) {
                logger.debug("extended checkClientTrusted with socket");
                ((X509ExtendedTrustManager) trustManager).checkServerTrusted(x509Certificates, s, socket);
            } else {
                trustManager.checkServerTrusted(x509Certificates, s);
                postCheckServerTrusted(x509Certificates);
            }
        } catch (CertificateException cex) {
            tlstmCounters.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpenErrors));
            tlstmCounters.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionInvalidServerCertificates));
            logger.warn("Server certificate validation failed for '" + x509Certificates[0] + "'");
            throw cex;
        }
    }

    private void postCheckServerTrusted(X509Certificate[] x509Certificates) throws CertificateException {
        if (useClientMode && (securityCallback != null)) {
            securityCallback.isServerCertificateAccepted(x509Certificates);
        }
    }

    private boolean preCheckServerTrusted(X509Certificate[] x509Certificates) throws CertificateException {
        if (tmStateReference.getCertifiedIdentity() != null) {
            OctetString fingerprint = tmStateReference.getCertifiedIdentity().getServerFingerprint();
            if (TLSTMUtil.isMatchingFingerprint(x509Certificates, fingerprint, true, tlstmCounters, logger, this)) {
                return true;
            }
        }
        Object entry = null;
        try {
            entry = TLSTMUtil.getSubjAltName(x509Certificates[0].getSubjectAlternativeNames(), 2);
        } catch (CertificateParsingException e) {
            logger.error("CertificateParsingException while verifying server certificate " +
                    Arrays.asList(x509Certificates));
        }
        if (entry == null) {
            X500Principal x500Principal = x509Certificates[0].getSubjectX500Principal();
            if (x500Principal != null) {
                entry = x500Principal.getName();
            }
        }
        if (entry != null) {
            String dNSName = ((String) entry).toLowerCase();
            String hostName = ((IpAddress) tmStateReference.getAddress())
                    .getInetAddress().getCanonicalHostName();
            if (dNSName.length() > 0) {
                if (dNSName.charAt(0) == '*') {
                    int pos = hostName.indexOf('.');
                    hostName = hostName.substring(pos);
                    dNSName = dNSName.substring(1);
                }
                if (hostName.equalsIgnoreCase(dNSName)) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Peer hostname " + hostName + " matches dNSName " + dNSName);
                    }
                    return true;
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Peer hostname " + hostName + " did not match dNSName " + dNSName);
            }
        }
        return false;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
        logger.debug("checkClientTrusted with sslEngine");
        boolean clientTrusted = checkClientTrustedIntern(x509Certificates);
        if (!clientTrusted) {
            try {
                if (trustManager instanceof X509ExtendedTrustManager) {
                    logger.debug("extended checkClientTrusted with sslEngine");
                    ((X509ExtendedTrustManager) trustManager).checkClientTrusted(x509Certificates, s, sslEngine);
                } else {
                    trustManager.checkClientTrusted(x509Certificates, s);
                }
            } catch (CertificateException cex) {
                tlstmCounters.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpenErrors));
                tlstmCounters.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionInvalidClientCertificates));
                logger.warn("Client certificate validation failed for '" + x509Certificates[0] + "'");
                throw cex;
            }
        }
    }

    private boolean checkClientTrustedIntern(X509Certificate[] x509Certificates) throws CertificateException {
        if ((tmStateReference != null) && (tmStateReference.getCertifiedIdentity() != null)) {
            OctetString fingerprint = tmStateReference.getCertifiedIdentity().getClientFingerprint();
            if (isMatchingFingerprint(x509Certificates, fingerprint, true)) {
                return true;
            }
        }
        if (!useClientMode && (securityCallback != null)) {
            if (securityCallback.isClientCertificateAccepted(x509Certificates[0])) {
                if (logger.isInfoEnabled()) {
                    logger.info("Client is trusted with certificate '" + x509Certificates[0] + "'");
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
        logger.debug("checkServerTrusted with sslEngine");
        if (preCheckServerTrusted(x509Certificates)) return;
        try {
            if (trustManager instanceof X509ExtendedTrustManager) {
                logger.debug("extended checkServerTrusted with sslEngine");
                ((X509ExtendedTrustManager) trustManager).checkServerTrusted(x509Certificates, s, sslEngine);
            } else {
                trustManager.checkServerTrusted(x509Certificates, s);
            }
        } catch (CertificateException cex) {
            tlstmCounters.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpenErrors));
            tlstmCounters.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionInvalidServerCertificates));
            logger.warn("Server certificate validation failed for '" + x509Certificates[0] + "'");
            throw cex;
        }
        postCheckServerTrusted(x509Certificates);
    }

}

