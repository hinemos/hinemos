/*_############################################################################
  _## 
  _##  SNMP4J - UsmSecurityParameters.java  
  _## 
  _##  Copyright (C) 2003-2020  Frank Fock and Jochen Katz (SNMP4J.org)
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
package org.snmp4j.security;

import java.io.*;
import org.snmp4j.asn1.*;
import org.snmp4j.asn1.BER.*;
import org.snmp4j.smi.*;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.log.LogFactory;
import org.snmp4j.log.LogAdapter;

public class UsmSecurityParameters implements SecurityParameters {

  private static final LogAdapter logger =
      LogFactory.getLogger(UsmSecurityParameters.class);

  private static final int MAX_BER_LENGTH_WITHOU_SEC_PARAMS =
      32+2+ 6 + 6 + 32+2;

  private OctetString authoritativeEngineID = new OctetString();
  private Integer32 authoritativeEngineBoots = new Integer32();
  private Integer32 authoritativeEngineTime = new Integer32();
  private OctetString userName = new OctetString();
  private AuthenticationProtocol authenticationProtocol = null;
  private PrivacyProtocol privacyProtocol = null;
  private byte[] authenticationKey;
  private byte[] privacyKey;
  private OctetString privacyParameters = new OctetString();
  private OctetString authenticationParameters = new OctetString();
  private int securityParametersPosition = -1;
  private int authParametersPosition = -1;
  private int decodedLength = -1;
  private int sequencePosition = -1;

  public UsmSecurityParameters() {
  }

  public UsmSecurityParameters(OctetString authoritativeEngineID,
                               Integer32 authoritativeEngineBoots,
                               Integer32 authoritativeEngineTime,
                               OctetString userName,
                               AuthenticationProtocol authenticationProtocol,
                               PrivacyProtocol privacyProtocol) {
    this.authoritativeEngineID = authoritativeEngineID;
    this.authoritativeEngineBoots = authoritativeEngineBoots;
    this.authoritativeEngineTime = authoritativeEngineTime;
    this.privacyProtocol = privacyProtocol;
    this.userName = userName;
    this.authenticationProtocol = authenticationProtocol;
  }

  public byte[] getAuthoritativeEngineID() {
    return authoritativeEngineID.getValue();
  }

  public void setAuthoritativeEngineID(byte[] authoritativeEngineID) {
    if (authoritativeEngineID == null) {
      throw new NullPointerException("Authoritative engine ID must not be null");
    }
    this.authoritativeEngineID.setValue(authoritativeEngineID);
  }
  public void setAuthoritativeEngineBoots(int authoritativeEngineBoots) {
    this.authoritativeEngineBoots.setValue(authoritativeEngineBoots);
  }
  public int getAuthoritativeEngineBoots() {
    return authoritativeEngineBoots.getValue();
  }
  public void setAuthoritativeEngineTime(int authoritativeEngineTime) {
    this.authoritativeEngineTime.setValue(authoritativeEngineTime);
  }
  public int getAuthoritativeEngineTime() {
    return authoritativeEngineTime.getValue();
  }
  public void setUserName(org.snmp4j.smi.OctetString userName) {
    this.userName = userName;
  }
  public org.snmp4j.smi.OctetString getUserName() {
    return userName;
  }
  public void setAuthenticationProtocol(AuthenticationProtocol authenticationProtocol) {
    this.authenticationProtocol = authenticationProtocol;
  }
  public AuthenticationProtocol getAuthenticationProtocol() {
    return authenticationProtocol;
  }
  public void setPrivacyProtocol(PrivacyProtocol privacyProtocol) {
    this.privacyProtocol = privacyProtocol;
  }
  public PrivacyProtocol getPrivacyProtocol() {
    return privacyProtocol;
  }

  public int getBERLength() {
    int length = getBERPayloadLength();
    return length + BER.getBERLengthOfLength(length) + 1;
  }

  public int getBERPayloadLength() {
    int length = getBERUsmPayloadLength();
    length += BER.getBERLengthOfLength(length)+1;
    return length;
  }



  public void decodeBER(BERInputStream inputStream) throws IOException {
    int pos = (int)inputStream.getPosition();
    this.decodedLength = pos;
    MutableByte mutableByte = new MutableByte();
    int octetLength = BER.decodeHeader(inputStream, mutableByte);
    long startPos = inputStream.getPosition();
    if (mutableByte.getValue() != BER.OCTETSTRING) {
      String txt =
          "BER decoding error: Expected BER OCTETSTRING but found: " +
          mutableByte.getValue();
      logger.warn(txt);
      throw new IOException(txt);
    }
    sequencePosition = (int)inputStream.getPosition();
    int length = BER.decodeHeader(inputStream, mutableByte);
    long startPosSeq = inputStream.getPosition();
    if (mutableByte.getValue() != BER.SEQUENCE) {
      String txt =
          "BER decoding error: Expected BER SEQUENCE but found: " +
          mutableByte.getValue();
      logger.warn(txt);
      throw new IOException(txt);
    }
    authoritativeEngineID.decodeBER(inputStream);
    authoritativeEngineBoots.decodeBER(inputStream);
    authoritativeEngineTime.decodeBER(inputStream);
    userName.decodeBER(inputStream);
    this.authParametersPosition = (int)(inputStream.getPosition() - pos);
    pos = (int)inputStream.getPosition();
    authenticationParameters.decodeBER(inputStream);
    this.authParametersPosition +=
        (inputStream.getPosition() - pos) -
        authenticationParameters.getBERPayloadLength();

    privacyParameters.decodeBER(inputStream);
    this.decodedLength = (int) (inputStream.getPosition() - decodedLength);
    if (BER.isCheckSequenceLength()) {
      // check length
      BER.checkSequenceLength(length,
                              (int) (inputStream.getPosition() - startPosSeq),
                              this);
      BER.checkSequenceLength(octetLength,
                              (int) (inputStream.getPosition() - startPos),
                              this);
    }
  }

  private int getBEREncodedAuthParamsPosition() {
    int length = getBERLength()-
        (authenticationParameters.getBERPayloadLength() +
         privacyParameters.getBERLength());
    return length;
  }

  public int getSequencePosition() {
    return sequencePosition;
  }

  public void encodeBER(OutputStream outputStream) throws IOException {
    BER.encodeHeader(outputStream, BER.OCTETSTRING, getBERPayloadLength());
    BER.encodeHeader(outputStream, BER.SEQUENCE, getBERUsmPayloadLength());
    authoritativeEngineID.encodeBER(outputStream);
    authoritativeEngineBoots.encodeBER(outputStream);
    authoritativeEngineTime.encodeBER(outputStream);
    userName.encodeBER(outputStream);
    authenticationParameters.encodeBER(outputStream);
    privacyParameters.encodeBER(outputStream);
  }

  /**
   * getBERUsmPayloadLength
   *
   * @return int
   */
  public int getBERUsmPayloadLength() {
    int length = authoritativeEngineID.getBERLength();
    length += authoritativeEngineBoots.getBERLength();
    length += authoritativeEngineTime.getBERLength();
    length += userName.getBERLength();
    length += authenticationParameters.getBERLength();
    length += privacyParameters.getBERLength();
    return length;
  }

  public int getBERMaxLength(int securityLevel) {
    SecurityProtocols secProtocol = SecurityProtocols.getInstance();
    int securityParamsLength = 2;
    if (securityLevel > SecurityLevel.NOAUTH_NOPRIV) {
      securityParamsLength = secProtocol.getMaxAuthDigestLength() +
          BER.getBERLengthOfLength(secProtocol.getMaxAuthDigestLength()) + 1;

      if (securityLevel == SecurityLevel.AUTH_PRIV) {
        securityParamsLength += secProtocol.getMaxPrivDecryptParamsLength() +
            BER.getBERLengthOfLength(secProtocol.getMaxPrivDecryptParamsLength())
            + 1;
      }
    }
    return MAX_BER_LENGTH_WITHOU_SEC_PARAMS + securityParamsLength +
        BER.getBERLengthOfLength(MAX_BER_LENGTH_WITHOU_SEC_PARAMS +
                                 securityParamsLength) + 1;
  }

  public byte[] getAuthenticationKey() {
    return authenticationKey;
  }
  public void setAuthenticationKey(byte[] authenticationKey) {
    this.authenticationKey = authenticationKey;
  }
  public byte[] getPrivacyKey() {
    return privacyKey;
  }
  public void setPrivacyKey(byte[] privacyKey) {
    this.privacyKey = privacyKey;
  }
  public org.snmp4j.smi.OctetString getPrivacyParameters() {
    return privacyParameters;
  }
  public void setPrivacyParameters(org.snmp4j.smi.OctetString privacyParameters) {
    this.privacyParameters = privacyParameters;
  }
  public org.snmp4j.smi.OctetString getAuthenticationParameters() {
    return authenticationParameters;
  }
  public void setAuthenticationParameters(org.snmp4j.smi.OctetString authenticationParameters) {
    this.authenticationParameters = authenticationParameters;
  }
  public int getSecurityParametersPosition() {
    return securityParametersPosition;
  }
  public void setSecurityParametersPosition(int securityParametersPosition) {
    this.securityParametersPosition = securityParametersPosition;
  }
  public int getAuthParametersPosition() {
    if (authParametersPosition >= 0) {
      return authParametersPosition;
    }
    else {
      return getBEREncodedAuthParamsPosition();
    }
  }

  /**
   * getScopedPduPosition
   *
   * @return int
   */
  public int getScopedPduPosition() {
    if (decodedLength >= 0) {
      return decodedLength + getSecurityParametersPosition();
    }
    else {
      return getSecurityParametersPosition()+getBERLength();
    }
  }

}
