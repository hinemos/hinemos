/*_############################################################################
  _## 
  _##  SNMP4J - DefaultTlsTmSecurityCallback.java  
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
import org.snmp4j.transport.TLSTM;

import javax.security.auth.x500.X500Principal;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * The {@code DefaultTlsTmSecurityCallback} resolves the
 * {@code tmSecurityName} for incoming requests through
 * a mapping table based on the peer certificates,
 * resolves the local certificate alias through a mapping table
 * based on the target address and accepts peer certificates
 * based on a list of trusted peer and issuer certificates.
 *
 * @author Frank Fock
 * @since 2.0
 * @version 2.8.1
 */
public class DefaultTlsTmSecurityCallback implements TlsTmSecurityCallback<X509Certificate> {

    private LogAdapter LOGGER = LogFactory.getLogger(DefaultTlsTmSecurityCallback.class);

    private Map<SecurityNameMapping, OctetString> securityNameMapping = new HashMap<SecurityNameMapping, OctetString>();
    private Map<Address, String> localCertMapping = new HashMap<Address, String>();
    private Set<String> acceptedSubjectDN = new HashSet<String>();
    private Set<String> acceptedIssuerDN = new HashSet<String>();

    @Override
    public OctetString getSecurityName(X509Certificate[] peerCertificateChain) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Getting security name for peer certificate chain: "+Arrays.asList(peerCertificateChain));
        }
        OctetString fallbackSecurityName = null;
        for (Map.Entry<SecurityNameMapping, OctetString> entry : securityNameMapping.entrySet()) {
            OctetString fingerprint = entry.getKey().getFingerprint();
            for (X509Certificate cert : peerCertificateChain) {
                OctetString certFingerprint = null;
                certFingerprint = TLSTMUtil.getFingerprint(cert);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Matching peer cert fingerprint "+certFingerprint+ " against local "+fingerprint);
                }
                if (((fingerprint.length()) == 0) ||
                        ((certFingerprint != null) && (certFingerprint.equals(fingerprint)))) {
                    // possible match found -> now try to map to tmSecurityName
                    org.snmp4j.transport.tls.SecurityNameMapping.CertMappingType mappingType = entry.getKey().getType();
                    OctetString data = entry.getKey().getData();
                    OctetString tmSecurityName = null;
                    try {
                        tmSecurityName = mapCertToTSN(cert, mappingType, data);
                        // Fallback to explicit security name mapping if there is no standard mapping available:
                        if (fallbackSecurityName == null && tmSecurityName == null) {
                            fallbackSecurityName = entry.getKey().getSecurityName();
                        }
                    } catch (CertificateParsingException e) {
                        LOGGER.warn("Failed to parse client certificate: " + e.getMessage());
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Matched security name: "+tmSecurityName);
                    }
                    if ((tmSecurityName != null) && (tmSecurityName.length() <= 32)) {
                        return tmSecurityName;
                    }
                }
            }
        }
        if (fallbackSecurityName != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Matched security name '"+fallbackSecurityName+"' by fallback mapping");
            }
            return fallbackSecurityName;
        }
        return null;
    }

    private OctetString mapCertToTSN(X509Certificate cert,
                                     org.snmp4j.transport.tls.SecurityNameMapping.CertMappingType mappingType, OctetString data)
            throws CertificateParsingException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Mapping cert to security name "+cert+ " with type "+mappingType+ " and date "+data);
        }
        switch (mappingType) {
            case Specified: {
                return data;
            }
            case SANAny:
            case SANRFC822Name: {
                Object entry = TLSTMUtil.getSubjAltName(cert.getSubjectAlternativeNames(), 1);
                if (entry != null) {
                    String[] rfc822Name = ((String) entry).split("@");
                    return new OctetString(rfc822Name[0] + "@" + rfc822Name[1].toLowerCase());
                }
                // fall through SANAny
            }
            case SANDNSName: {
                Object entry = TLSTMUtil.getSubjAltName(cert.getSubjectAlternativeNames(), 2);
                if (entry != null) {
                    String dNSName = ((String) entry).toLowerCase();
                    return new OctetString(dNSName);
                }
                // fall through SANAny
            }
            case SANIpAddress: {
                OctetString address = TLSTMUtil.getIpAddressFromSubjAltName(cert.getSubjectAlternativeNames());
                if (address != null) {
                    return address;
                }
                // fall through SANAny
            }
            case CommonName: {
                X500Principal x500Principal = cert.getSubjectX500Principal();
                return new OctetString(x500Principal.getName());
            }
        }
        return null;
    }

    @Override
    public boolean isClientCertificateAccepted(X509Certificate peerEndCertificate) throws CertificateException {
        if (acceptedSubjectDN.isEmpty()) {
            return false;
        }
        if (peerEndCertificate == null || !acceptedSubjectDN.contains(peerEndCertificate.getSubjectDN().getName())) {
            throw new CertificateException("Client certificate "+peerEndCertificate+" has no accepted subject DN: "+acceptedSubjectDN);
        }
        return true;
    }

    @Override
    public boolean isServerCertificateAccepted(X509Certificate[] peerCertificateChain) throws CertificateException {
        if (peerCertificateChain == null || peerCertificateChain.length == 0) {
            throw new CertificateException("Server certificate chain is empty");
        }
        int accepted = -1;
        for (Map.Entry<SecurityNameMapping, OctetString> entry : securityNameMapping.entrySet()) {
            OctetString fingerprint = entry.getKey().getFingerprint();
            for (X509Certificate cert : peerCertificateChain) {
                OctetString certFingerprint = null;
                certFingerprint = TLSTMUtil.getFingerprint(cert);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Matching server fingerprint " + certFingerprint + " against accepted " + fingerprint);
                }
                if (((fingerprint.length()) == 0) ||
                        ((certFingerprint != null) && (certFingerprint.equals(fingerprint)))) {
                    accepted = 1;
                    break;
                }
                else {
                    accepted = 0;
                }
            }
        }
        if (accepted == 0) {
            throw new CertificateException("Server certificate chain "+Arrays.asList(peerCertificateChain)+
                    " does not match accepted fingerprints: "+securityNameMapping);
        }
        String subject = peerCertificateChain[0].getSubjectDN().getName();
        if (acceptedSubjectDN.contains(subject)) {
            return true;
        }
        for (X509Certificate cert : peerCertificateChain) {
            Principal issuerDN = cert.getIssuerDN();
            if ((issuerDN != null) && acceptedIssuerDN.contains(issuerDN.getName())) {
                return true;
            }
        }
        if (acceptedSubjectDN.isEmpty() && acceptedIssuerDN.isEmpty()) {
            return false;
        }
        throw new CertificateException("Server certificate chain "+Arrays.asList(peerCertificateChain)+
                " rejected because issuer and subject DN not accepted");
    }

    @Override
    public boolean isAcceptedIssuer(X509Certificate issuerCertificate) throws CertificateException {
        Principal issuerDN = issuerCertificate.getIssuerDN();
        if (acceptedIssuerDN.isEmpty()) {
            return false;
        }
        if ((issuerDN != null) && acceptedIssuerDN.contains(issuerDN.getName())) {
            return true;
        }
        throw new CertificateException("Issuer certificate "+issuerCertificate+
                " does not have accepted DN: "+acceptedIssuerDN);
    }

    @Override
    public String getLocalCertificateAlias(Address targetAddress) {
        String localCert = localCertMapping.get(targetAddress);
        if (localCert == null) {
            return localCertMapping.get(null);
        }
        return localCert;
    }

    /**
     * Adds a mapping to derive a security name from a certificate. A mapping corresponds to a row
     * in the snmpTlstmCertToTSNTable of RFC 5953.
     *
     * @param fingerprint
     *         an (optional) cryptographic hash of a X.509 certificate. Whether the trusted CA in
     *         the certificate validation path or the certificate itself is matched against the
     *         fingerprint is specified by the {@code type} parameter.
     * @param type
     *         specifies the mapping type of the security name derivation from a certificate.
     * @param data
     *         auxiliary data used as optional configuration information for some mapping types.
     *         It must be ignored for any mapping type that does not use auxiliary data.
     * @param securityName
     *         specifies the mapped security name. This parameter is optional and only required if
     *         the mapping type does not dictate a method to derive the security name from a
     *         certificates meta data (like subjectAltName).
     */
    public void addSecurityNameMapping(OctetString fingerprint,
                                       org.snmp4j.transport.tls.SecurityNameMapping.CertMappingType type,
                                       OctetString data,
                                       OctetString securityName) {
        securityNameMapping.put(new SecurityNameMapping(fingerprint, data, type, securityName), securityName);
    }

    public OctetString removeSecurityNameMapping(OctetString fingerprint, SecurityNameMapping.CertMappingType type,
                                                 OctetString data) {
        return securityNameMapping.remove(new SecurityNameMapping(fingerprint, data, type, null));
    }

    public void addAcceptedIssuerDN(String issuerDN) {
        acceptedIssuerDN.add(issuerDN);
    }

    public boolean removeAcceptedIssuerDN(String issuerDN) {
        return acceptedIssuerDN.remove(issuerDN);
    }

    public void addAcceptedSubjectDN(String subjectDN) {
        acceptedSubjectDN.add(subjectDN);
    }

    public boolean removeAcceptedSubjectDN(String subjectDN) {
        return acceptedSubjectDN.remove(subjectDN);
    }

    /**
     * Map a target address to a local certificate alias. The security mapping
     * will use the certificate {@code certAlias} for a target address
     * {@code address} when applied to a client mode {@link TLSTM}.
     *
     * @param address
     *         a {@link org.snmp4j.smi.TlsAddress} instance or {@code null}
     *         if the local certificate should mapped to any target address.
     * @param certAlias
     *         the certificate alias in the local key store to be used to authenticate
     *         at TLS server instances.
     */
    public void addLocalCertMapping(Address address, String certAlias) {
        localCertMapping.put(address, certAlias);
    }

    /**
     * Remove the local certificate mapping for the given target address.
     *
     * @param address
     *         a {@link org.snmp4j.smi.TlsAddress} instance or {@code null}
     *         if the default local certificate mapping should be removed.
     *
     * @return the removed mapping or {@code null} if there is no such mapping.
     */
    public String removeLocalCertMapping(Address address) {
        return localCertMapping.remove(address);
    }

}

