/*_############################################################################
  _## 
  _##  SNMP4J - SnmpConfigurator.java  
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
package org.snmp4j.util;

import org.snmp4j.*;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.security.nonstandard.PrivAES192With3DESKeyExtension;
import org.snmp4j.security.nonstandard.PrivAES256With3DESKeyExtension;
import org.snmp4j.smi.*;
import org.snmp4j.transport.tls.PropertiesTlsTmSecurityCallback;
import org.snmp4j.transport.tls.X509TlsTransportMappingConfig;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The <code>SnmpConfigurator</code> class configures a {@link Snmp} instance
 * with settings taken from a <code>Map</code> conforming to the format returned
 * by {@link ArgumentParser#parse(String[] args)}.
 * In addition, a {@link PDUFactory} and {@link Target} can be created using
 * settings from a <code>Map</code> too.
 *
 * @author Frank Fock
 * @version 3.0
 * @since 1.10
 */
public class SnmpConfigurator {

    public static final String O_VERSION = "v";
    public static final String P_VERSION = "org.snmp4j.arg.version";
    public static final String F_VERSION = "s<1|2c|3>";

    public static final String O_LOCAL_ENGINE_ID = "l";
    public static final String P_LOCAL_ENGINE_ID = "org.snmp4j.arg.localEngineID";
    public static final String F_LOCAL_ENGINE_ID = "o<\\n\\n[:\\n\\n]*>";

    public static final String O_AUTHORITATIVE_ENGINE_ID = "e";
    public static final String P_AUTHORITATIVE_ENGINE_ID = "org.snmp4j.arg.authoritativeEngineID";
    public static final String F_AUTHORITATIVE_ENGINE_ID = "o<\\n\\n[:\\n\\n]*>";

    public static final String O_COMMUNITY = "c";
    public static final String P_COMMUNITY = "org.snmp4j.arg.community";
    public static final String F_COMMUNITY = "s{=public}";

    public static final String O_CONTEXT_NAME = "n";
    public static final String P_CONTEXT_NAME = "org.snmp4j.arg.contextName";
    public static final String F_CONTEXT_NAME = "s{=}";

    public static final String O_CONTEXT_ENGINE_ID = "E";
    public static final String P_CONTEXT_ENGINE_ID = "org.snmp4j.arg.contextEngineID";
    public static final String F_CONTEXT_ENGINE_ID = "o<\\n\\n[:\\n\\n]*>";

    public static final String O_SECURITY_NAME = "u";
    public static final String P_SECURITY_NAME = "org.snmp4j.arg.securityName";
    public static final String F_SECURITY_NAME = "s";

    public static final String O_RETRIES = "r";
    public static final String P_RETRIES = "org.snmp4j.arg.retries";
    public static final String F_RETRIES = "i";

    public static final String O_TIMEOUT = "t";
    public static final String P_TIMEOUT = "org.snmp4j.arg.timeout";
    public static final String F_TIMEOUT = "l";

    public static final String O_ADDRESS = "address";
    public static final String P_ADDRESS = "org.snmp4j.arg.address";
    public static final String F_ADDRESS = "s<(udp|tcp|dtls|tls):.*[/[0-9]+]?>";

    public static final String O_BIND_ADDRESS = "b";
    public static final String P_BIND_ADDRESS = "org.snmp4j.arg.bindAddress";
    public static final String F_BIND_ADDRESS = "s<(udp|tcp|dtls|tls):.*[/[0-9]+]?>";

    public static final String O_AUTH_PASSPHRASE = "A";
    public static final String P_AUTH_PASSPHRASE = "org.snmp4j.arg.authPassphrase";
    public static final String F_AUTH_PASSPHRASE = "s<.*>";

    public static final String O_PRIV_PASSPHRASE = "Y";
    public static final String P_PRIV_PASSPHRASE = "org.snmp4j.arg.privPassphrase";
    public static final String F_PRIV_PASSPHRASE = "s<.*>";

    public static final String O_AUTH_PROTOCOL = "a";
    public static final String P_AUTH_PROTOCOL = "org.snmp4j.arg.authProtocol";
    public static final String F_AUTH_PROTOCOL = "s<(MD5|SHA|SHA224|SHA256|SHA384|SHA512)>";

    public static final String O_PRIV_PROTOCOL = "y";
    public static final String P_PRIV_PROTOCOL = "org.snmp4j.arg.privProtocol";
    public static final String F_PRIV_PROTOCOL = "s<(DES|3DES|AES|AES128|AES192|AES256)>";

    public static final String O_OPERATION = "o";
    public static final String P_OPERATION = "org.snmp4j.arg.operation";
    public static final String F_OPERATION = "s<(?i)(GET|GETNEXT|GETBULK|TRAP|NOTIFICATION|SET|INFORM)>";

    public static final String O_MAX_REPETITIONS = "Cr";
    public static final String P_MAX_REPETITIONS = "org.snmp4j.arg.maxRepetitions";
    public static final String F_MAX_REPETITIONS = "i{=10}";

    public static final String O_NON_REPEATERS = "Cn";
    public static final String P_NON_REPEATERS = "org.snmp4j.arg.nonRepeaters";
    public static final String F_NON_REPEATERS = "i{=0}";

    public static final String O_TRAPV1_AGENT_ADDR = "Ta";
    public static final String P_TRAPV1_AGENT_ADDR = "org.snmp4j.arg.trapv1.agentAddr";
    public static final String F_TRAPV1_AGENT_ADDR = "s{=0.0.0.0}<\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}>";

    public static final String O_TRAP_OID = "To";
    public static final String P_TRAP_OID = "org.snmp4j.arg.trap.trapOID";
    public static final String F_TRAP_OID = "s{=1.3.6.1.6.3.1.1.5.1}<([a-zA-Z\\-0-9]*:)?[0-9a-zA-Z\\-\\.]*>";

    public static final String O_TRAP_UPTIME = "Tu";
    public static final String P_TRAP_UPTIME = "org.snmp4j.arg.trap.trapUpTime";
    public static final String F_TRAP_UPTIME = "l{=0}";

    public static final String O_TRAPV1_ENTERPRISE = "Te";
    public static final String P_TRAPV1_ENTERPRISE = "org.snmp4j.arg.trap.trapv1.enterprise";
    public static final String F_TRAPV1_ENTERPRISE = "s{=0.0}<([a-zA-Z\\-0-9]*:)?[0-9a-zA-Z\\-\\.]*>";

    public static final String O_TRAPV1_SPECIFIC_ID = "Ts";
    public static final String P_TRAPV1_SPECIFIC_ID = "org.snmp4j.arg.trap.trapv1.specificID";
    public static final String F_TRAPV1_SPECIFIC_ID = "i{=0}";

    public static final String O_TRAPV1_GENERIC_ID = "Tg";
    public static final String P_TRAPV1_GENERIC_ID = "org.snmp4j.arg.trap.trapv1.genericID";
    public static final String F_TRAPV1_GENERIC_ID = "i{=0}";

    public static final String O_BOOT_COUNTER = "bc";
    public static final String P_BOOT_COUNTER = "org.snmp4j.arg.bootCounter";
    public static final String F_BOOT_COUNTER = "i{=0}";

    public static final String O_SEC_LEVEL = "sl";
    public static final String P_SEC_LEVEL = "org.snmp4j.arg.securityLevel";
    public static final String F_SEC_LEVEL = "i";

    public static final String O_SEC_MODEL = "sm";
    public static final String P_SEC_MODEL = "org.snmp4j.arg.securityModel";
    public static final String F_SEC_MODEL = "i";

    public static final String O_TLS_LOCAL_ID = "tls-local-id";
    public static final String P_TLS_LOCAL_ID = "org.snmp4j.arg.tlsLocalID";
    public static final String F_TLS_LOCAL_ID = "s";

    public static final String O_TLS_PEER_ID = "tls-peer-id";
    public static final String P_TLS_PEER_ID = "org.snmp4j.arg.tlsPeerID";
    public static final String F_TLS_PEER_ID = "s";

    public static final String O_TLS_CERT_FINGERPRINT = "tls-cert-fingerprint";
    public static final String P_TLS_CERT_FINGERPRINT = "org.snmp4j.arg.tlsCertFingerprint";
    public static final String F_TLS_CERT_FINGERPRINT = "o<\\n\\n[:\\n\\n]*>";

    public static final String O_TLS_TRUST_CA = "tls-trust-ca";
    public static final String P_TLS_TRUST_CA = "org.snmp4j.arg.tlsTrustCA";
    public static final String F_TLS_TRUST_CA = "s";

    public static final String O_TLS_VERSION = "tls-version";
    public static final String P_TLS_VERSION = "org.snmp4j.arg.tlsVersion";
    public static final String F_TLS_VERSION = "s{=TLSv1.2}<TLSv1[\\.1|\\.2|\\.3]?[,TLSv1[\\.1|\\.2|\\.3]?]*>";

    public static final String O_DTLS_VERSION = "dtls-version";
    public static final String P_DTLS_VERSION = "org.snmp4j.arg.dtlsVersion";
    public static final String F_DTLS_VERSION = "s{=DTLSv1.2}<DTLSv1[\\.0|\\.2|\\.3]?[,DTLSv1[\\.0|\\.2|\\.3]?]*>";

    private String oVersion;
    private String oLocalEngineID;
    private String oRetries;
    private String oTimeout;
    private String oAddress;
    private String oBindAddress;
    private String oCommunity;
    private String oAuthoritativeEngineID;
    private String oSecurityName;
    private String oAuthPassphrase;
    private String oAuthProtocol;
    private String oPrivPassphrase;
    private String oPrivProtocol;
    private String oOperation;
    private String oMaxRepetitions;
    private String oNonRepeaters;
    private String oBootCounter;
    private String oContextName;
    private String oContextEngineID;
    private String oSecLevel;
    private String oSecModel;
    private String oAgentAddr;
    private String oTrapOID;
    private String oTrapSysUpTime;
    private String oGenericID;
    private String oSpecificID;
    private String oEnterprise;
    private String oTlsLocalID;
    private String oTlsTrustCA;
    private String oTlsPeerID;
    private String oTlsVersion;
    private String oDtlsVersion;
    private String oTlsCertFingerprint;
    private boolean commandResponder;

    public SnmpConfigurator() {
        this(new Properties());
    }

    public SnmpConfigurator(Properties props) {
        oVersion = props.getProperty(P_VERSION, O_VERSION);
        oLocalEngineID = props.getProperty(P_LOCAL_ENGINE_ID, O_LOCAL_ENGINE_ID);
        oRetries = props.getProperty(P_RETRIES, O_RETRIES);
        oTimeout = props.getProperty(P_TIMEOUT, O_TIMEOUT);
        oAddress = props.getProperty(P_ADDRESS, O_ADDRESS);
        oBindAddress = props.getProperty(P_BIND_ADDRESS, O_BIND_ADDRESS);
        oCommunity = props.getProperty(P_COMMUNITY, O_COMMUNITY);
        oSecurityName = props.getProperty(P_SECURITY_NAME, O_SECURITY_NAME);
        oAuthoritativeEngineID = props.getProperty(P_AUTHORITATIVE_ENGINE_ID, O_AUTHORITATIVE_ENGINE_ID);
        oAuthPassphrase = props.getProperty(P_AUTH_PASSPHRASE, O_AUTH_PASSPHRASE);
        oAuthProtocol = props.getProperty(P_AUTH_PROTOCOL, O_AUTH_PROTOCOL);
        oPrivPassphrase = props.getProperty(P_PRIV_PASSPHRASE, O_PRIV_PASSPHRASE);
        oPrivProtocol = props.getProperty(P_PRIV_PROTOCOL, O_PRIV_PROTOCOL);
        oOperation = props.getProperty(P_OPERATION, O_OPERATION);
        oMaxRepetitions = props.getProperty(P_MAX_REPETITIONS, O_MAX_REPETITIONS);
        oNonRepeaters = props.getProperty(P_NON_REPEATERS, O_NON_REPEATERS);
        oBootCounter = props.getProperty(P_BOOT_COUNTER, O_BOOT_COUNTER);
        oContextName = props.getProperty(P_CONTEXT_NAME, O_CONTEXT_NAME);
        oContextEngineID = props.getProperty(P_CONTEXT_ENGINE_ID, O_CONTEXT_ENGINE_ID);
        oSecLevel = props.getProperty(P_SEC_LEVEL, O_SEC_LEVEL);
        oSecModel = props.getProperty(P_SEC_MODEL, O_SEC_MODEL);
        oAgentAddr = props.getProperty(P_TRAPV1_AGENT_ADDR, O_TRAPV1_AGENT_ADDR);
        oTrapOID = props.getProperty(P_TRAP_OID, O_TRAP_OID);
        oTrapSysUpTime = props.getProperty(P_TRAP_UPTIME, O_TRAP_UPTIME);
        oSpecificID = props.getProperty(P_TRAPV1_SPECIFIC_ID, O_TRAPV1_SPECIFIC_ID);
        oGenericID = props.getProperty(P_TRAPV1_GENERIC_ID, O_TRAPV1_GENERIC_ID);
        oEnterprise = props.getProperty(P_TRAPV1_ENTERPRISE, O_TRAPV1_ENTERPRISE);
        oTlsLocalID = props.getProperty(P_TLS_LOCAL_ID, O_TLS_LOCAL_ID);
        oTlsPeerID = props.getProperty(P_TLS_PEER_ID, O_TLS_PEER_ID);
        oTlsTrustCA = props.getProperty(P_TLS_TRUST_CA, O_TLS_TRUST_CA);
        oTlsVersion = props.getProperty(P_TLS_VERSION, O_TLS_VERSION);
        oDtlsVersion = props.getProperty(P_DTLS_VERSION, O_DTLS_VERSION);
        oTlsCertFingerprint = props.getProperty(P_TLS_CERT_FINGERPRINT, O_TLS_CERT_FINGERPRINT);
    }

    public SnmpConfigurator(boolean commandResponder) {
        this();
        this.commandResponder = commandResponder;
    }

    public SnmpConfigurator(Properties props, boolean commandResponder) {
        this(props);
        this.commandResponder = commandResponder;
    }

    public boolean isCommandResponder() {
        return commandResponder;
    }

    public void setCommandResponder(boolean commandResponder) {
        this.commandResponder = commandResponder;
    }

    public void configure(Snmp snmp, Map<String, List<Object>> settings) {
        configure(snmp, snmp.getUSM(), snmp.getMessageDispatcher(), settings);
    }

    public void configure(Snmp snmp, USM usm, MessageDispatcher messageDispatcher, Map<String,List<Object>> settings) {
        if (usm != null) {
            configureEngine(snmp, usm, settings);
            String sn = (String)
                    ArgumentParser.getValue(settings, oSecurityName, 0);
            if (sn != null) {
                String authPP =
                        (String) ArgumentParser.getValue(settings, oAuthPassphrase, 0);
                String privPP =
                        (String) ArgumentParser.getValue(settings, oPrivPassphrase, 0);
                String authP =
                        (String) ArgumentParser.getValue(settings, oAuthProtocol, 0);
                String privP =
                        (String) ArgumentParser.getValue(settings, oPrivProtocol, 0);
                OID authProtocol = getAuthProtocolOid(authP);
                OID privProtocol = getPrivProtocolOid(privP);
                OctetString un = createOctetString(sn, null);
                usm.addUser(un, new UsmUser(un,
                        authProtocol,
                        createOctetString(authPP, null),
                        privProtocol,
                        createOctetString(privPP, null)));
            }
        }
        Properties tlsProps = getTlsProperties(settings);
        for (TransportMapping<?> tm : messageDispatcher.getTransportMappings()) {
            if (tm instanceof X509TlsTransportMappingConfig) {
                X509TlsTransportMappingConfig tlsTM = (X509TlsTransportMappingConfig) tm;
                tlsTM.setSecurityCallback(new PropertiesTlsTmSecurityCallback(tlsProps, commandResponder));
                if (tlsProps.getProperty(tlsTM.getProtocolVersionPropertyName()) != null) {
                    String[] versions = tlsProps.getProperty(tlsTM.getProtocolVersionPropertyName()).split(",");
                    tlsTM.setProtocolVersions(versions);
                }
            }
        }
    }

    public static OID getPrivProtocolOid(String privP) {
        OID privProtocol = null;
        if ("DES".equals(privP)) {
            privProtocol = PrivDES.ID;
        } else if ("3DES".equals(privP)) {
            privProtocol = Priv3DES.ID;
        } else if ("AES".equals(privP) || "AES128".equals(privP)) {
            privProtocol = PrivAES128.ID;
        } else if ("AES192".equals(privP)) {
            privProtocol = PrivAES192.ID;
        } else if ("AES256".equals(privP)) {
            privProtocol = PrivAES256.ID;
        } else if ("AES192p".equals(privP)) {
            privProtocol = PrivAES192With3DESKeyExtension.ID;
        } else if ("AES256p".equals(privP)) {
            privProtocol = PrivAES256With3DESKeyExtension.ID;
        }
        return privProtocol;
    }

    public static OID getAuthProtocolOid(String authP) {
        OID authProtocol = null;
        if ("MD5".equals(authP)) {
            authProtocol = AuthMD5.ID;
        } else if ("SHA".equals(authP)) {
            authProtocol = AuthSHA.ID;
        } else if ("SHA224".equals(authP)) {
            authProtocol = AuthHMAC128SHA224.ID;
        } else if ("SHA256".equals(authP)) {
            authProtocol = AuthHMAC192SHA256.ID;
        } else if ("SHA384".equals(authP)) {
            authProtocol = AuthHMAC256SHA384.ID;
        } else if ("SHA512".equals(authP)) {
            authProtocol = AuthHMAC384SHA512.ID;
        }
        return authProtocol;
    }

    private void configureEngine(Snmp snmp, USM usm, Map<String,List<Object>> settings) {
        int engineBoots = 0;
        Integer bc =
                (Integer) ArgumentParser.getValue(settings, oBootCounter, 0);
        if (bc != null) {
            engineBoots = bc;
            usm.setEngineBoots(engineBoots);
        }
        int engineTime = 0;
        OctetString localEngineID =
                createOctetString((String)
                                ArgumentParser.getValue(settings, oLocalEngineID, 0),
                        null);
        if (localEngineID == null) {
            if (snmp.getLocalEngineID() == null) {
                snmp.setLocalEngine(MPv3.createLocalEngineID(), engineBoots,
                        engineTime);
            }
        } else {
            snmp.setLocalEngine(localEngineID.getValue(), engineBoots, engineTime);
        }
    }

    private Properties getTlsProperties(Map<String,List<Object>> settings) {
        Properties tlsProps = new Properties();
        String tlsLocalID = (String) ArgumentParser.getValue(settings, oTlsLocalID, 0);
        if (tlsLocalID != null) {
            tlsProps.setProperty(P_TLS_LOCAL_ID, tlsLocalID);
        }
        String tlsTrustCA = (String) ArgumentParser.getValue(settings, oTlsTrustCA, 0);
        if (tlsTrustCA != null) {
            tlsProps.setProperty(P_TLS_TRUST_CA, tlsTrustCA);
        }
        String tlsPeerID = (String) ArgumentParser.getValue(settings, oTlsPeerID, 0);
        if (tlsPeerID != null) {
            tlsProps.setProperty(P_TLS_PEER_ID, tlsPeerID);
        }
        String sn = (String)
                ArgumentParser.getValue(settings, oSecurityName, 0);
        if (sn != null) {
            tlsProps.setProperty(P_SECURITY_NAME, sn);
        }
        String tlsVersion = (String) ArgumentParser.getValue(settings, oTlsVersion, 0);
        if (tlsVersion != null) {
            tlsProps.setProperty(P_TLS_VERSION, tlsVersion);
        }
        String dtlsVersion = (String) ArgumentParser.getValue(settings, oDtlsVersion, 0);
        if (dtlsVersion != null) {
            tlsProps.setProperty(P_DTLS_VERSION, dtlsVersion);
        }
        String tlsCertFingerprint = (String) ArgumentParser.getValue(settings, oTlsCertFingerprint, 0);
        if (tlsCertFingerprint != null) {
            tlsProps.setProperty(P_TLS_CERT_FINGERPRINT, tlsCertFingerprint);
        }
        return tlsProps;
    }

    public PDUFactory getPDUFactory(Map<String,List<Object>> settings) {
        return new InnerPDUFactory(settings);
    }

    public Target getTarget(Map<String,List<Object>> settings) {
        String version =
                (String) ArgumentParser.getValue(settings, oVersion, 0);
        OctetString community =
                createOctetString((String)
                                ArgumentParser.getValue(settings, oCommunity, 0),
                        "public");
        Integer securityModel = (Integer) ArgumentParser.getValue(settings, oSecModel, 0);
        Target t;
        if ("1".equals(version)) {
            t = new CommunityTarget();
            t.setVersion(SnmpConstants.version1);
            ((CommunityTarget) t).setCommunity(community);
        } else if ("2c".equals(version)) {
            t = new CommunityTarget();
            t.setVersion(SnmpConstants.version2c);
            ((CommunityTarget) t).setCommunity(community);
        } else if (((securityModel != null) && (securityModel == SecurityModel.SECURITY_MODEL_TSM)) ||
                ((securityModel == null) && (getTlsProperties(settings).size() > 1))) {
            String sn = (String) ArgumentParser.getValue(settings, oSecurityName, 0);
            if (sn == null) {
                throw new IllegalArgumentException("Mandatory securityName for TLS target not specified with option -"+
                        oSecurityName);
            }
            CertifiedTarget ct = new CertifiedTarget(new OctetString(sn));
            ct.setSecurityModel(SecurityModel.SECURITY_MODEL_TSM);
            setSecurityLevelOnTarget(settings, ct);
            t = ct;
        } else {
            UserTarget ut = new UserTarget();
            t = ut;
            String ae = (String) ArgumentParser.getValue(settings, oAuthoritativeEngineID, 0);
            if (ae != null) {
                ut.setAuthoritativeEngineID(createOctetString(ae, null).getValue());
            }
            ut.setSecurityModel(USM.SECURITY_MODEL_USM);
            String sn = (String)
                    ArgumentParser.getValue(settings, oSecurityName, 0);
            if (sn != null) {
                ut.setSecurityName(createOctetString(sn, null));
            }
            setSecurityLevelOnTarget(settings, ut);
        }
        int retries = 0;
        Number r = (Number) ArgumentParser.getValue(settings, oRetries, 0);
        if (r != null) {
            retries = r.intValue();
        }
        t.setRetries(retries);
        long timeout = 5000;
        Number to = (Number) ArgumentParser.getValue(settings, oTimeout, 0);
        if (to != null) {
            timeout = to.longValue();
        }
        t.setTimeout(timeout);
        String addr = (String) ArgumentParser.getValue(settings, oAddress, 0);
        if (addr != null) {
            if (addr.indexOf('/') <= 0) {
                addr += "/161";
            }
            t.setAddress(GenericAddress.parse(addr));
        }
        return t;
    }

    private void setSecurityLevelOnTarget(Map<String,List<Object>> settings, AbstractTarget target) {
        Integer secLevel = (Integer) ArgumentParser.getValue(settings, oSecLevel, 0);
        if (secLevel == null) {
            if (settings.containsKey(oPrivPassphrase) || (target instanceof CertifiedTarget)) {
                target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
            } else if (settings.containsKey(oAuthPassphrase)) {
                target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
            } else {
                target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
            }
        } else {
            target.setSecurityLevel(secLevel);
        }
    }

    public Address getBindAddress(Map<String,List<Object>> settings) {
        String addr = (String) ArgumentParser.getValue(settings, oBindAddress, 0);
        if (addr != null) {
            if (addr.indexOf('/') <= 0) {
                addr += "/0";
            }
            return GenericAddress.parse(addr);
        }
        return null;
    }

    /**
     * Create an OctetString from a String value.
     *
     * @param s
     *         the string value to convert. If it starts with <code>0x</code> its
     *         trailing value will be interpreted as a hex string with colon (:)
     *         separator.
     * @param defaultString
     *         an optional default if <code>s</code> is <code>null</code>.
     *
     * @return an OctetString or <code>null</code> if <code>s</code> is
     * <code>null</code>.
     * @since 1.10.2
     */
    public static OctetString createOctetString(String s, String defaultString) {
        OctetString octetString = null;
        if (s == null) {
            s = defaultString;
        }
        if ((s != null) && s.startsWith("0x")) {
            octetString = OctetString.fromHexString(s.substring(2), ':');
        } else if (s != null) {
            octetString = new OctetString(s);
        }
        return octetString;
    }

    public class InnerPDUFactory implements PDUFactory {

        private Map<String,List<Object>> settings;

        public InnerPDUFactory(Map<String,List<Object>> settings) {
            this.settings = settings;
        }

        public PDU createPDU(Target target) {
            int type = getPduType();
            PDU pdu = DefaultPDUFactory.createPDU(target, type);
            configurePDU(type, pdu);
            return pdu;
        }

        private void configurePDU(int type, PDU pdu) {
            if ((type == PDU.V1TRAP) && (!(pdu instanceof PDUv1))) {
                throw new RuntimeException("V1TRAP can only be sent using SNMPv1");
            }
            if (type == PDU.GETBULK) {
                Integer maxRep = getMaxRepetitions();
                if (maxRep != null) {
                    pdu.setMaxRepetitions(maxRep);
                }
                Integer nonRepeaters = getNonRepeaters();
                if (nonRepeaters != null) {
                    pdu.setNonRepeaters(nonRepeaters);
                }
            } else if ((type == PDU.TRAP) || (type == PDU.INFORM)) {
                Number tu = (Number) ArgumentParser.getValue(settings, oTrapSysUpTime, 0);
                if (tu != null) {
                    pdu.add(new VariableBinding(SnmpConstants.sysUpTime,
                            new TimeTicks(tu.longValue())));
                }
                String to = (String) ArgumentParser.getValue(settings, oTrapOID, 0);
                if (to != null) {
                    pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(to)));
                }
            } else if (type == PDU.V1TRAP) {
                PDUv1 pduV1 = (PDUv1) pdu;
                String aa =
                        (String) ArgumentParser.getValue(settings, oAgentAddr, 0);
                if (aa != null) {
                    pduV1.setAgentAddress(new IpAddress(aa));
                }
                Integer gid = (Integer) ArgumentParser.getValue(settings, oGenericID, 0);
                if (gid != null) {
                    pduV1.setGenericTrap(gid);
                }
                Integer sid = (Integer) ArgumentParser.getValue(settings, oSpecificID, 0);
                if (sid != null) {
                    pduV1.setSpecificTrap(sid);
                }
                String e =
                        (String) ArgumentParser.getValue(settings, oEnterprise, 0);
                if (e != null) {
                    pduV1.setEnterprise(new OID(e));
                }
            }
            if (pdu instanceof ScopedPDU) {
                ScopedPDU scoped = (ScopedPDU) pdu;
                String cEngineID =
                        (String) ArgumentParser.getValue(settings, oContextEngineID, 0);
                if (cEngineID != null) {
                    scoped.setContextEngineID(createOctetString(cEngineID, null));
                }
                String cn =
                        (String) ArgumentParser.getValue(settings, oContextName, 0);
                if (cn != null) {
                    scoped.setContextName(createOctetString(cn, null));
                }
            }
        }

        private int getPduType() {
            String pduType = (String) ArgumentParser.getValue(settings, oOperation, 0);
            if (pduType == null) {
                pduType = "GET";
            }
            pduType = pduType.toUpperCase();
            return PDU.getTypeFromString(pduType);
        }

        @Override
        public PDU createPDU(MessageProcessingModel messageProcessingModel) {
            int type = getPduType();
            PDU pdu = DefaultPDUFactory.createPDU(messageProcessingModel, type);
            configurePDU(type, pdu);
            return pdu;
        }


        public Integer getMaxRepetitions() {
            return (Integer) ArgumentParser.getValue(settings, oMaxRepetitions, 0);
        }

        public Integer getNonRepeaters() {
            return (Integer) ArgumentParser.getValue(settings, oNonRepeaters, 0);
        }
    }
}
