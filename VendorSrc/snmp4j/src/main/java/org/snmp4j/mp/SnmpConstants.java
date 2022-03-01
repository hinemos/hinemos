/*_############################################################################
  _## 
  _##  SNMP4J - SnmpConstants.java  
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
package org.snmp4j.mp;

import org.snmp4j.smi.OID;

/**
 * The {@code SnmpConstants} class holds constants, ObjectIDs and
 * Message strings used within SNMP4J.
 *
 * @author Frank Fock
 * @version 1.8
 */
public final class SnmpConstants {

  public static final int DEFAULT_COMMAND_RESPONDER_PORT = 161;
  public static final int DEFAULT_NOTIFICATION_RECEIVER_PORT = 162;

  public static final int MIN_PDU_LENGTH = 484;

  public static final int MILLISECOND_TO_NANOSECOND = 1000000;
  public static final int HUNDREDTHS_TO_NANOSECOND = 10000000;

  public static final int version1  = 0;
  public static final int version2c = 1;
  public static final int version3  = 3;

  // SNMP error conditions defined (indirectly) by the SNMP standards:
  /** Command responders did not respond within specified timeout interval. */
  public static final int SNMP_ERROR_TIMEOUT                 = -1;
  /** OIDs returned from a GETNEXT or GETBULK are less or equal than the requested one (which is not allowed by SNMP). */
  public static final int SNMP_ERROR_LEXICOGRAPHIC_ORDER     = -2;
  /** A unresolvable REPORT message was received while processing a request. */
  public static final int SNMP_ERROR_REPORT                  = -3;
  /** An IOException occurred during request processing. */
  public static final int SNMP_ERROR_IO                      = -4;

  // SNMP error codes defined by the protocol:
  public static final int SNMP_ERROR_SUCCESS                 = 0;
  public static final int SNMP_ERROR_TOO_BIG                 = 1;
  public static final int SNMP_ERROR_NO_SUCH_NAME            = 2;
  public static final int SNMP_ERROR_BAD_VALUE               = 3;
  public static final int SNMP_ERROR_READ_ONLY               = 4;
  public static final int SNMP_ERROR_GENERAL_ERROR           = 5;
  public static final int SNMP_ERROR_NO_ACCESS               = 6;
  public static final int SNMP_ERROR_WRONG_TYPE              = 7;
  public static final int SNMP_ERROR_WRONG_LENGTH            = 8;
  public static final int SNMP_ERROR_WRONG_ENCODING          = 9;
  public static final int SNMP_ERROR_WRONG_VALUE             =10;
  public static final int SNMP_ERROR_NO_CREATION             =11;
  public static final int SNMP_ERROR_INCONSISTENT_VALUE      =12;
  public static final int SNMP_ERROR_RESOURCE_UNAVAILABLE    =13;
  public static final int SNMP_ERROR_COMMIT_FAILED           =14;
  public static final int SNMP_ERROR_UNDO_FAILED             =15;
  public static final int SNMP_ERROR_AUTHORIZATION_ERROR     =16;
  public static final int SNMP_ERROR_NOT_WRITEABLE           =17;
  public static final int SNMP_ERROR_INCONSISTENT_NAME       =18;

  public static final int SNMP_MP_OK                          = 0;
  public static final int SNMP_MP_ERROR                       = -1400;
  public static final int SNMP_MP_UNSUPPORTED_SECURITY_MODEL  = -1402;
  public static final int SNMP_MP_NOT_IN_TIME_WINDOW          = -1403;
  public static final int SNMP_MP_DOUBLED_MESSAGE             = -1404;
  public static final int SNMP_MP_INVALID_MESSAGE             = -1405;
  public static final int SNMP_MP_INVALID_ENGINEID            = -1406;
  public static final int SNMP_MP_NOT_INITIALIZED             = -1407;
  public static final int SNMP_MP_PARSE_ERROR                 = -1408;
  public static final int SNMP_MP_UNKNOWN_MSGID               = -1409;
  public static final int SNMP_MP_MATCH_ERROR                 = -1410;
  public static final int SNMP_MP_COMMUNITY_ERROR             = -1411;
  public static final int SNMP_MP_WRONG_USER_NAME             = -1412;
  public static final int SNMP_MP_BUILD_ERROR                 = -1413;
  public static final int SNMP_MP_USM_ERROR                   = -1414;
  public static final int SNMP_MP_UNKNOWN_PDU_HANDLERS        = -1415;
  public static final int SNMP_MP_UNAVAILABLE_CONTEXT         = -1416;
  public static final int SNMP_MP_UNKNOWN_CONTEXT             = -1417;
  public static final int SNMP_MP_REPORT_SENT                 = -1418;

  public static final int SNMPv1v2c_CSM_OK                       = 0;
  public static final int SNMPv1v2c_CSM_BAD_COMMUNITY_NAME       = 1501;
  public static final int SNMPv1v2c_CSM_BAD_COMMUNITY_USE        = 1502;


  public static final int SNMPv3_USM_OK                          = 0;
  public static final int SNMPv3_USM_ERROR                       = 1401;
  public static final int SNMPv3_USM_UNSUPPORTED_SECURITY_LEVEL  = 1403;
  public static final int SNMPv3_USM_UNKNOWN_SECURITY_NAME       = 1404;
  public static final int SNMPv3_USM_ENCRYPTION_ERROR            = 1405;
  public static final int SNMPv3_USM_DECRYPTION_ERROR            = 1406;
  public static final int SNMPv3_USM_AUTHENTICATION_ERROR        = 1407;
  public static final int SNMPv3_USM_AUTHENTICATION_FAILURE      = 1408;
  public static final int SNMPv3_USM_PARSE_ERROR                 = 1409;
  public static final int SNMPv3_USM_UNKNOWN_ENGINEID            = 1410;
  public static final int SNMPv3_USM_NOT_IN_TIME_WINDOW          = 1411;
  public static final int SNMPv3_USM_UNSUPPORTED_AUTHPROTOCOL    = 1412;
  public static final int SNMPv3_USM_UNSUPPORTED_PRIVPROTOCOL    = 1413;
  public static final int SNMPv3_USM_ADDRESS_ERROR               = 1414;
  public static final int SNMPv3_USM_ENGINE_ID_TOO_LONG          = 1415;
  public static final int SNMPv3_USM_SECURITY_NAME_TOO_LONG      = 1416;

  public static final int SNMPv3_TSM_OK                          = 0;
  public static final int SNMPv3_TSM_UNKNOWN_PREFIXES            = 1601;
  public static final int SNMPv3_TSM_INVALID_CACHES              = 1602;
  public static final int SNMPv3_TSM_INADEQUATE_SECURITY_LEVELS  = 1603;

  public static final int SNMP_MD_OK                        = 0;
  public static final int SNMP_MD_ERROR                     = 1701;
  public static final int SNMP_MD_UNSUPPORTED_MP_MODEL      = 1702;
  public static final int SNMP_MD_UNSUPPORTED_ADDRESS_CLASS = 1703;
  public static final int SNMP_MD_UNSUPPORTED_SNMP_VERSION  = 1704;


  // USM security protocol OIDs
  public static final OID usmNoAuthProtocol =
      new OID(new int[]{1,3,6,1,6,3,10,1,1,1});
  public static final OID usmHMACMD5AuthProtocol =
      new OID(new int[]{1,3,6,1,6,3,10,1,1,2});
  public static final OID usmHMACSHAAuthProtocol =
      new OID(new int[]{1,3,6,1,6,3,10,1,1,3});
  public static final OID usmNoPrivProtocol =
      new OID(new int[]{1,3,6,1,6,3,10,1,2,1});
  public static final OID usmDESPrivProtocol =
      new OID(new int[]{1,3,6,1,6,3,10,1,2,2});
  public static final OID usm3DESEDEPrivProtocol =
      new OID(new int[]{1,3,6,1,6,3,10,1,2,3});
  public static final OID usmAesCfb128Protocol =
      new OID(new int[]{1,3,6,1,6,3,10,1,2,4});

  // SNMP4J security protocol OIDs
  public static final OID oosnmpUsmAesCfb192Protocol =
      new OID(new int[]{1,3,6,1,4,1,4976,2,2,1,1,1});
  public static final OID oosnmpUsmAesCfb256Protocol =
      new OID(new int[]{1,3,6,1,4,1,4976,2,2,1,1,2});
  public static final OID oosnmpUsmAesCfb192ProtocolWith3DESKeyExtension =
      new OID(new int[]{1,3,6,1,4,1,4976,2,2,1,2,1});
  public static final OID oosnmpUsmAesCfb256ProtocolWith3DESKeyExtension =
      new OID(new int[]{1,3,6,1,4,1,4976,2,2,1,2,2});

  public static final OID usmStatsUnsupportedSecLevels =
      new OID(new int[]{1, 3, 6, 1, 6, 3, 15, 1, 1, 1, 0 });
  public static final OID usmStatsNotInTimeWindows =
      new OID(new int[]{1, 3, 6, 1, 6, 3, 15, 1, 1, 2, 0 });
  public static final OID usmStatsUnknownUserNames =
      new OID(new int[]{1, 3, 6, 1, 6, 3, 15, 1, 1, 3, 0 });
  public static final OID usmStatsUnknownEngineIDs =
      new OID(new int[]{1, 3, 6, 1, 6, 3, 15, 1, 1, 4, 0 });
  public static final OID usmStatsWrongDigests =
      new OID(new int[]{1, 3, 6, 1, 6, 3, 15, 1, 1, 5, 0 });
  public static final OID usmStatsDecryptionErrors =
      new OID(new int[]{1, 3, 6, 1, 6, 3, 15, 1, 1, 6, 0 });

  public static final OID snmpEngineID =
      new OID(new int[] { 1, 3, 6, 1, 6, 3, 10, 2, 1, 1, 0 });

  public static final OID snmpUnknownSecurityModels =
      new OID(new int[] { 1, 3, 6, 1, 6, 3, 11, 2, 1, 1, 0 });
  public static final OID snmpInvalidMsgs =
      new OID(new int[] { 1, 3, 6, 1, 6, 3, 11, 2, 1, 2, 0 });
  public static final OID snmpUnknownPDUHandlers =
      new OID(new int[] { 1, 3, 6, 1, 6, 3, 11, 2, 1, 3, 0 });

  // SNMP counters (obsoleted counters are not listed)
  public static final OID snmpInPkts =
    new OID(new int[] { 1,3,6,1,2,1,11,1,0 });
  public static final OID snmpInBadVersions =
    new OID(new int[] { 1,3,6,1,2,1,11,3,0 });
  public static final OID snmpInBadCommunityNames =
    new OID(new int[] { 1,3,6,1,2,1,11,4,0 });
  public static final OID snmpInBadCommunityUses =
    new OID(new int[] { 1,3,6,1,2,1,11,5,0 });
  public static final OID snmpInASNParseErrs =
    new OID(new int[] { 1,3,6,1,2,1,11,6,0 });
  public static final OID snmpSilentDrops =
    new OID(new int[] { 1,3,6,1,2,1,11,31,0 });
  public static final OID snmpProxyDrops =
    new OID(new int[] { 1,3,6,1,2,1,11,32,0 });

  public static final OID snmpTrapOID =
     new OID(new int[] { 1,3,6,1,6,3,1,1,4,1,0 });
   public static final OID snmpTrapEnterprise =
     new OID(new int[] { 1,3,6,1,6,3,1,1,4,3,0 });

  // generic trap prefix
  public static final OID snmpTraps =
      new OID(new int[] { 1,3,6,1,6,3,1,1,5 });
  // standard traps
  public static final OID coldStart =
      new OID(new int[] { 1,3,6,1,6,3,1,1,5,1 });
  public static final OID warmStart =
      new OID(new int[] { 1,3,6,1,6,3,1,1,5,2 });
  public static final OID authenticationFailure =
      new OID(new int[] { 1,3,6,1,6,3,1,1,5,5 });
  public static final OID linkDown =
    new OID(new int[] { 1,3,6,1,6,3,1,1,5,3 });
  public static final OID linkUp =
    new OID(new int[] { 1,3,6,1,6,3,1,1,5,4 });

  // most important system group OIDs
  public static final OID sysDescr =
    new OID(new int[] { 1,3,6,1,2,1,1,1,0 });
  public static final OID sysObjectID =
    new OID(new int[] { 1,3,6,1,2,1,1,2,0 });
  public static final OID sysUpTime =
    new OID(new int[] { 1,3,6,1,2,1,1,3,0 });
  public static final OID sysContact =
    new OID(new int[] { 1,3,6,1,2,1,1,4,0 });
  public static final OID sysName =
    new OID(new int[] { 1,3,6,1,2,1,1,5,0 });
  public static final OID sysLocation =
    new OID(new int[] { 1,3,6,1,2,1,1,6,0 });
  public static final OID sysServices =
    new OID(new int[] { 1,3,6,1,2,1,1,7,0 });
  public static final OID sysOREntry =
    new OID(new int[] { 1,3,6,1,2,1,1,9,1 });
  public static final OID system =
          new OID(new int[] { 1,3,6,1,2,1,1 });

  // contexts
  public static final OID snmpUnavailableContexts =
    new OID(new int[] { 1,3,6,1,6,3,12,1,4,0 });
  public static final OID snmpUnknownContexts =
    new OID(new int[] { 1,3,6,1,6,3,12,1,5,0 });

  // coexistance
  public static final OID snmpTrapAddress =
    new OID(new int[] { 1,3,6,1,6,3,18,1,3,0 });
  public static final OID snmpTrapCommunity =
    new OID(new int[] { 1,3,6,1,6,3,18,1,4,0 });

  public static final OID zeroDotZero = new OID(new int[] { 0,0 });

  // SNMP-TSM-MIB
  public static final OID snmpTsmInvalidCaches =
    new OID(new int[] { 1,3,6,1,2,1,190,1,1,1,0 });
  public static final OID snmpTsmInadequateSecurityLevels =
    new OID(new int[] { 1,3,6,1,2,1,190,1,1,2,0 });
  public static final OID snmpTsmUnknownPrefixes =
    new OID(new int[] { 1,3,6,1,2,1,190,1,1,3,0 });
  public static final OID snmpTsmInvalidPrefixes =
    new OID(new int[] { 1,3,6,1,2,1,190,1,1,4,0 });
  public static final OID snmpTsmConfigurationUsePrefix =
    new OID(new int[] { 1,3,6,1,2,1,190,1,2,1,0 });

  // SNMP-TLS-TM-MIB
    public static final OID snmpTlstmSessionOpens =
    new OID(new int[] { 1,3,6,1,2,1,198,2,1,1,0 });
  public static final OID snmpTlstmSessionClientCloses =
    new OID(new int[] { 1,3,6,1,2,1,198,2,1,2,0 });
  public static final OID snmpTlstmSessionOpenErrors =
    new OID(new int[] { 1,3,6,1,2,1,198,2,1,3,0 });
  public static final OID snmpTlstmSessionAccepts =
    new OID(new int[] { 1,3,6,1,2,1,198,2,1,4,0 });
  public static final OID snmpTlstmSessionServerCloses =
    new OID(new int[] { 1,3,6,1,2,1,198,2,1,5,0 });
  public static final OID snmpTlstmSessionNoSessions =
    new OID(new int[] { 1,3,6,1,2,1,198,2,1,6,0 });
  public static final OID snmpTlstmSessionInvalidClientCertificates =
    new OID(new int[] { 1,3,6,1,2,1,198,2,1,7,0 });
  public static final OID snmpTlstmSessionUnknownServerCertificate =
    new OID(new int[] { 1,3,6,1,2,1,198,2,1,8,0 });
  public static final OID snmpTlstmSessionInvalidServerCertificates =
    new OID(new int[] { 1,3,6,1,2,1,198,2,1,9,0 });
  public static final OID snmpTlstmSessionInvalidCaches =
    new OID(new int[] { 1,3,6,1,2,1,198,2,1,10,0 });

  // SNMP-SSH-TM-MIB
  public static final OID snmpSshtmSessionOpens =
    new OID(new int[] { 1,3,6,1,2,1,189,1,1,1,0 });
  public static final OID snmpSshtmSessionCloses =
    new OID(new int[] { 1,3,6,1,2,1,189,1,1,2,0 });
  public static final OID snmpSshtmSessionOpenErrors =
    new OID(new int[] { 1,3,6,1,2,1,189,1,1,3,0 });
  public static final OID snmpSshtmSessionUserAuthFailures =
    new OID(new int[] { 1,3,6,1,2,1,189,1,1,4,0 });
  public static final OID snmpSshtmSessionNoChannels =
    new OID(new int[] { 1,3,6,1,2,1,189,1,1,5,0 });
  public static final OID snmpSshtmSessionNoSubsystems =
    new OID(new int[] { 1,3,6,1,2,1,189,1,1,6,0 });
  public static final OID snmpSshtmSessionNoSessions =
    new OID(new int[] { 1,3,6,1,2,1,189,1,1,7,0 });
  public static final OID snmpSshtmSessionInvalidCaches =
    new OID(new int[] { 1,3,6,1,2,1,189,1,1,8,0 });


  // SNMP4J-STATISTICS-MIB
  /**
   * The total number of requests that timed out (Counter32).
   */
  public static final OID snmp4jStatsRequestTimeouts =
      new OID(new int[] { 1,3,6,1,4,1,4976,10,1,1,4,1,1,1,0 });
  /**
   * The total number of retries sent on behalf of
   * requests. The first message, thus the request
   * itself is not counted (Counter32).
   */
  public static final OID snmp4jStatsRequestRetries =
      new OID(new int[] { 1,3,6,1,4,1,4976,10,1,1,4,1,1,2,0 });
  /**
   * The total number of milliseconds this SNMP
   * entity spend waiting for responses on its own
   * requests (Counter64).
   */
  public static final OID snmp4jStatsRequestWaitTime =
      new OID(new int[] { 1,3,6,1,4,1,4976,10,1,1,4,1,1,3,0 });
  /**
   * The number of milliseconds a successful request took
   * from sending the request to receiving the corresponding response
   * with the same msgID. Note, for community based SNMP version, only
   * the same request ID is used to correlate request and response.
   * Thus, only for SNMPv3 the counter can distinguish which retry
   * has been successfully responded.
   */
  public static final OID snmp4jStatsRequestRuntime =
      new OID(new int[] { 1,3,6,1,4,1,4976,10,1,1,4,1,1,4,0 });

  /**
   * The total number of requests that timed out for this target (Counter32).
   */
  public static final OID snmp4jStatsReqTableTimeouts =
      new OID(new int[] { 1,3,6,1,4,1,4976,10,1,1,4,1,1,10,3,1,4 });
  /**
   * The total number of retries sent on behalf of
   * requests to this target. The first message, thus the request
   * itself is not counted.
   */
  public static final OID snmp4jStatsReqTableRetries =
      new OID(new int[] { 1,3,6,1,4,1,4976,10,1,1,4,1,1,10,3,1,5 });
  /**
   * The total number of milliseconds this SNMP
   * entity spend waiting for responses on its own
   * requests to this target.
   */
  public static final OID snmp4jStatsReqTableWaitTime =
      new OID(new int[] { 1,3,6,1,4,1,4976,10,1,1,4,1,1,10,3,1,6 });
  /**
   * The number of milliseconds a successful request took
   * from sending the request to receiving the corresponding response
   * with the same msgID for this target.
   * Note, for community based SNMP version, only the same request ID
   * is used to correlate request and response. Thus, only for SNMPv3
   * the counter can distinguish which retry has been successfully responded.
   */
  public static final OID snmp4jStatsReqTableRuntime =
      new OID(new int[] { 1,3,6,1,4,1,4976,10,1,1,4,1,1,10,3,1,7 });


  /**
   * The number of response processings that ended
   * due to an internal timeout before that maximum
   * number of response variables (GETBULK) has been
   * reached. For other request types than GETBULK,
   * an internal timeout would return a SNMP error
   * (e.g. genErr) to the command sender.
   */
  public static final OID snmp4jStatsResponseTimeouts =
      new OID(new int[] { 1,3,6,1,4,1,4976,10,1,1,4,1,2,1,0 });
  /**
   * The total number of retries ignored by the command
   * responder while processing requests.
   */
  public static final OID snmp4jStatsResponseIgnoredRetries =
      new OID(new int[] { 1,3,6,1,4,1,4976,10,1,1,4,1,2,2,0 });
  /**
   * The total number of milliseconds the command
   * responder took to process a request.
   */
  public static final OID snmp4jStatsResponseProcessTime =
      new OID(new int[] { 1,3,6,1,4,1,4976,10,1,1,4,1,2,3,0 });

  // SNMP framework
  public static final OID snmpSetSerialNo =
      new OID(new int[] { 1,3,6,1,6,3,1,1,6,1,0 });

  public static final String[] SNMP_TP_ERROR_MESSAGES = {
      "Request timed out"
  };

  public static final String[] SNMP_ERROR_MESSAGES = {
      "Success",
      "PDU encoding too big",
      "No such name",
      "Bad value",
      "Variable is read-only",
      "General variable binding error",
      "No access",
      "Wrong type",
      "Variable binding data with incorrect length",
      "Variable binding data with wrong encoding",
      "Wrong value",
      "Unable to create object",
      "Inconsistent value",
      "Resource unavailable",
      "Commit failed",
      "Undo failed",
      "Authorization error",
      "Not writable",
      "Inconsistent naming used"
  };
  public static String[][] MD_ERROR_MESSAGES = {
      { ""+SNMP_MD_ERROR, "Message Dispatcher error" },
      { ""+SNMP_MD_UNSUPPORTED_MP_MODEL, "Unsupported message processing model" },
      { ""+SNMP_MD_UNSUPPORTED_ADDRESS_CLASS, "Unsupported address class" },
      { ""+SNMP_MD_UNSUPPORTED_SNMP_VERSION, "Unsupported address class" }
  };

  public static String[][] MP_ERROR_MESSAGES = {
      { ""+SNMP_MP_ERROR, "MP error" },
      { ""+SNMP_MP_UNSUPPORTED_SECURITY_MODEL, "Unsupported security model" },
      { ""+SNMP_MP_NOT_IN_TIME_WINDOW, "Message not in time window"},
      { ""+SNMP_MP_DOUBLED_MESSAGE, "Doubled message" },
      { ""+SNMP_MP_INVALID_MESSAGE, "Invalid message" },
      { ""+SNMP_MP_INVALID_ENGINEID, "Invalid engine ID" },
      { ""+SNMP_MP_NOT_INITIALIZED, "MP not initialized" },
      { ""+SNMP_MP_PARSE_ERROR, "MP parse error"},
      { ""+SNMP_MP_UNKNOWN_MSGID, "Unknown message ID"},
      { ""+SNMP_MP_MATCH_ERROR, "MP match error"},
      { ""+SNMP_MP_COMMUNITY_ERROR, "MP community error"},
      { ""+SNMP_MP_WRONG_USER_NAME, "Wrong user name"},
      { ""+SNMP_MP_BUILD_ERROR, "MP build error"},
      { ""+SNMP_MP_USM_ERROR, "USM error"},
      { ""+SNMP_MP_UNKNOWN_PDU_HANDLERS, "Unknown PDU handles"},
      { ""+SNMP_MP_UNAVAILABLE_CONTEXT, "Unavailable context"},
      { ""+SNMP_MP_UNKNOWN_CONTEXT, "Unknown context"},
      { ""+SNMP_MP_REPORT_SENT, "Report sent"}
  };

  public static String[][] USM_ERROR_MESSAGES = {
      { ""+SNMPv3_USM_OK, "USM OK" },
      { ""+SNMPv3_USM_ERROR, "USM error" },
      { ""+SNMPv3_USM_UNSUPPORTED_SECURITY_LEVEL, "Unsupported security level" },
      { ""+SNMPv3_USM_UNKNOWN_SECURITY_NAME, "Unknown security name"},
      { ""+SNMPv3_USM_ENCRYPTION_ERROR, "Encryption error"},
      { ""+SNMPv3_USM_DECRYPTION_ERROR, "Decryption error"},
      { ""+SNMPv3_USM_AUTHENTICATION_ERROR, "Authentication error"},
      { ""+SNMPv3_USM_AUTHENTICATION_FAILURE, "Authentication failure"},
      { ""+SNMPv3_USM_PARSE_ERROR, "USM parse error"},
      { ""+SNMPv3_USM_UNKNOWN_ENGINEID, "Unknown engine ID"},
      { ""+SNMPv3_USM_NOT_IN_TIME_WINDOW, "Not in time window"},
      { ""+SNMPv3_USM_UNSUPPORTED_AUTHPROTOCOL, "Unsupported authentication protocol"},
      { ""+SNMPv3_USM_UNSUPPORTED_PRIVPROTOCOL, "Unsupported privacy protocol"},
      { ""+SNMPv3_USM_ADDRESS_ERROR, "Address error"},
      { ""+SNMPv3_USM_ENGINE_ID_TOO_LONG, "Engine ID too long"},
      { ""+SNMPv3_USM_SECURITY_NAME_TOO_LONG, "Security name too long"}
  };

  public static String mpErrorMessage(int status) {
    String s = ""+status;
    for (String[] MP_ERROR_MESSAGE : MP_ERROR_MESSAGES) {
      if (s.equals(MP_ERROR_MESSAGE[0])) {
        return MP_ERROR_MESSAGE[1];
      }
    }
    // MPv3 uses USM so scan the USM error messages too:
    s = errorMessage(status, USM_ERROR_MESSAGES);
    if (s == null) {
      s = errorMessage(status, MD_ERROR_MESSAGES);
      return (s == null) ? ""+status : s;
    }
    return s;
  }

  public static String usmErrorMessage(int status) {
    String s = errorMessage(status, USM_ERROR_MESSAGES);
    return (s == null) ? ""+status : s;
  }

  private static String errorMessage(int status, String[][] errorMessages) {
    String s = ""+status;
    for (String[] errorMessage : errorMessages) {
      if (s.equals(errorMessage[0])) {
        return errorMessage[1];
      }
    }
    return null;
  }

  /**
   * Gets the generic trap ID from a notification OID.
   * @param oid
   *    an OID.
   * @return
   *    -1 if the supplied OID is not a generic trap, otherwise a zero or positive value
   *    will be returned that denotes the generic trap ID.
   */
  public static int getGenericTrapID(OID oid) {
    if ((oid == null) || (oid.size() != snmpTraps.size()+1)) {
      return -1;
    }
    if (oid.leftMostCompare(snmpTraps.size(), snmpTraps) == 0) {
      return oid.get(oid.size() - 1) - 1;
    }
    return -1;
  }

  public static OID getTrapOID(OID enterprise, int genericID, int specificID) {
    OID oid;
    if (genericID != 6) {
      oid = new OID(snmpTraps);
      oid.append(genericID+1);
    }
    else {
      oid = new OID(enterprise);
      oid.append(0);
      oid.append(specificID);
    }
    return oid;
  }

  /**
   * Enumeration of the textual convention StorageType defined in SNMPv2-TC MIB.
   * @since 2.5.7
   */
  public enum StorageTypeEnum {
    other(1),
    _volatile(2),
    nonVolatile(3),
    permanent(4),
    readOnly(5);

    private int smiValue;

    StorageTypeEnum(int value) {
      this.smiValue = value;
    }

    /**
     * Gets the SMI value of the enumeration for setting a StorageType OBJECT-TYPE.
     * @return
     *    the SMI integer representation value.
     */
    public int getSmiValue() {
      return smiValue;
    }
  }
}

