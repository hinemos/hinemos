/*_############################################################################
  _## 
  _##  SNMP4J - UsmUserEntry.java  
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

import java.io.Serializable;

import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.OID;

/**
 * The <code>UsmUserEntry</code> class represents a user in the
 * Local Configuration Datastore (LCD).
 *
 * @author Frank Fock
 * @version 2.5.7
 */
public class UsmUserEntry implements Serializable, Comparable {

  private static final long serialVersionUID = -3021438367015187166L;

  private OctetString engineID;
  private OctetString userName;
  private UsmUser usmUser;
  private byte[] authenticationKey;
  private byte[] privacyKey;
  private SnmpConstants.StorageTypeEnum storageType = SnmpConstants.StorageTypeEnum.nonVolatile;

  /**
   * Creates a new user entry with empty engine ID and empty user.
   */
  public UsmUserEntry() {
    engineID = new OctetString();
    userName = new OctetString();
    usmUser = new UsmUser(new OctetString(), null, null, null, null);
  }

  /**
   * Creates a user with user name and associated {@link UsmUser}.
   * @param userName
   *    the user name of the new entry.
   * @param user
   *    the <code>UsmUser</code> representing the security information of the
   *    user.
   */
  public UsmUserEntry(OctetString userName, UsmUser user) {
    this.userName = userName;
    this.usmUser = user;
    if (user.isLocalized()) {
      this.engineID = user.getLocalizationEngineID();
      if ((user.getAuthenticationProtocol() != null) &&
          (user.getAuthenticationPassphrase() != null)) {
        authenticationKey = user.getAuthenticationPassphrase().getValue();
        if ((user.getPrivacyProtocol() != null) &&
            (user.getPrivacyPassphrase() != null)) {
          privacyKey = user.getPrivacyPassphrase().getValue();
        }
      }
    }
  }

  /**
   * Creates a user with user name and associated {@link UsmUser}.
   * @param userName
   *    the user name of the new entry.
   * @param engineID
   *    the authoritative engine ID associated with the user.
   * @param user
   *    the <code>UsmUser</code> representing the security information of the
   *    user.
   */
  public UsmUserEntry(OctetString userName,
                      OctetString engineID,
                      UsmUser user) {
    this(userName, user);
    this.engineID = engineID;
  }

  /**
   * Creates a localized user entry.
   * @param engineID
   *    the engine ID for which the users has bee localized.
   * @param securityName
   *    the user and security name of the new entry.
   * @param authProtocol
   *    the authentication protocol ID.
   * @param authKey
   *    the authentication key.
   * @param privProtocol
   *    the privacy protocol ID.
   * @param privKey
   *    the privacy key.
   */
  public UsmUserEntry(byte[] engineID, OctetString securityName,
                      OID authProtocol, byte[] authKey,
                      OID privProtocol, byte[] privKey) {
    this.engineID = (engineID == null) ? null : new OctetString(engineID);
    this.userName = securityName;
    this.authenticationKey = authKey;
    this.privacyKey = privKey;
    this.usmUser =
        new UsmUser(userName, authProtocol,
                    ((authenticationKey != null) ?
                     new OctetString(authenticationKey) : null),
                    privProtocol,
                    ((privacyKey != null) ?
                     new OctetString(privacyKey) : null), this.engineID);
  }

  public OctetString getEngineID() {
    return engineID;
  }
  public void setEngineID(OctetString engineID) {
    this.engineID = engineID;
  }
  public void setUserName(OctetString userName) {
    this.userName = userName;
  }
  public OctetString getUserName() {
    return userName;
  }
  public void setUsmUser(UsmUser usmUser) {
    this.usmUser = usmUser;
  }
  public UsmUser getUsmUser() {
    return usmUser;
  }
  public void setAuthenticationKey(byte[] authenticationKey) {
    this.authenticationKey = authenticationKey;
  }
  public byte[] getAuthenticationKey() {
    return authenticationKey;
  }
  public void setPrivacyKey(byte[] privacyKey) {
    this.privacyKey = privacyKey;
  }
  public byte[] getPrivacyKey() {
    return privacyKey;
  }

  /**
   * Compares this user entry with another one by engine ID then by their user
   * names.
   *
   * @param o
   *    a <code>UsmUserEntry</code> instance.
   * @return
   *    a negative integer, zero, or a positive integer as this object is
   *    less than, equal to, or greater than the specified object.
   */
  public int compareTo(Object o) {
    UsmUserEntry other = (UsmUserEntry)o;
    int result = 0;
    if ((engineID != null) && (other.engineID != null)) {
      result = engineID.compareTo(other.engineID);
    }
    else if ((engineID != null) && (other.engineID == null)) {
      result = 1;
    }
    else if ((engineID == null) && (other.engineID != null)) {
      result = -1;
    }
    if (result == 0) {
      result = userName.compareTo(other.userName);
      if (result == 0) {
        result = usmUser.compareTo(other.usmUser);
      }
    }
    return result;
  }

  public SnmpConstants.StorageTypeEnum getStorageType() {
    return storageType;
  }

  public void setStorageType(SnmpConstants.StorageTypeEnum storageType) {
    this.storageType = storageType;
  }

  public String toString() {
    return "UsmUserEntry[userName="+userName+",usmUser="+usmUser+",storageType="+storageType+"]";
  }

}

