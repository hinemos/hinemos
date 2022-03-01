/*_############################################################################
  _## 
  _##  SNMP4J - UsmUser.java  
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

import org.snmp4j.SNMP4JSettings;
import org.snmp4j.User;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.OID;

/**
 * The <code>UsmUser</code> class represents USM user providing information
 * to secure SNMPv3 message exchange. A user is characterized by its security
 * name and optionally by a authentication protocol and passphrase as well as
 * a privacy protocol and passphrase.
 * <p>
 * There are no setters for the attributes of this class, to prevent
 * inconsistent states in the USM, when a user is changed from outside.
 *
 * @author Frank Fock
 * @version 2.4.3
 */
public class UsmUser implements User, Comparable, Cloneable {

  private static final long serialVersionUID = -2258973598142206767L;

  private OctetString securityName;
  private OctetString authenticationPassphrase;
  private OctetString privacyPassphrase;
  private OID authenticationProtocol;
  private OID privacyProtocol;
  private OctetString localizationEngineID;

  /**
   * Creates a USM user.
   * @param securityName
   *    the security name of the user (typically the user name).
   * @param authenticationProtocol
   *    the authentication protcol ID to be associated with this user. If set
   *    to <code>null</code>, this user only supports unauthenticated messages.
   * @param authenticationPassphrase
   *    the authentication passphrase. If not <code>null</code>,
   *    <code>authenticationProtocol</code> must also be not <code>null</code>.
   *    RFC3414 §11.2 requires passphrases to have a minimum length of 8 bytes.
   *    If the length of <code>authenticationPassphrase</code> is less than 8
   *    bytes an <code>IllegalArgumentException</code> is thrown.
   * @param privacyProtocol
   *    the privacy protcol ID to be associated with this user. If set
   *    to <code>null</code>, this user only supports unencrypted messages.
   * @param privacyPassphrase
   *    the privacy passphrase. If not <code>null</code>,
   *    <code>privacyProtocol</code> must also be not <code>null</code>.
   *    RFC3414 §11.2 requires passphrases to have a minimum length of 8 bytes.
   *    If the length of <code>authenticationPassphrase</code> is less than 8
   *    bytes an <code>IllegalArgumentException</code> is thrown.
   */
  public UsmUser(OctetString securityName,
                 OID authenticationProtocol,
                 OctetString authenticationPassphrase,
                 OID privacyProtocol,
                 OctetString privacyPassphrase) {
    if (securityName == null) {
      throw new NullPointerException();
    }
    if (SNMP4JSettings.isCheckUsmUserPassphraseLength()) {
      if ((authenticationProtocol != null) &&
          ((authenticationPassphrase != null) &&
           (authenticationPassphrase.length() < 8))) {
        throw new IllegalArgumentException(
            "USM passphrases must be at least 8 bytes long (RFC3414 §11.2)");
      }
      if ((privacyProtocol != null) &&
          ((privacyPassphrase != null) &&
              (privacyPassphrase.length() < 8))) {
        throw new IllegalArgumentException(
            "USM passphrases must be at least 8 bytes long (RFC3414 §11.2)");
      }
    }
    this.securityName = securityName;
    this.authenticationProtocol = authenticationProtocol;
    this.authenticationPassphrase = authenticationPassphrase;
    this.privacyProtocol = privacyProtocol;
    this.privacyPassphrase = privacyPassphrase;
  }

  /**
   * Creates a localized USM user.
   * @param securityName
   *    the security name of the user (typically the user name).
   * @param authenticationProtocol
   *    the authentication protcol ID to be associated with this user. If set
   *    to <code>null</code>, this user only supports unauthenticated messages.
   * @param authenticationPassphrase
   *    the authentication passphrase. If not <code>null</code>,
   *    <code>authenticationProtocol</code> must also be not <code>null</code>.
   *    RFC3414 §11.2 requires passphrases to have a minimum length of 8 bytes.
   *    If the length of <code>authenticationPassphrase</code> is less than 8
   *    bytes an <code>IllegalArgumentException</code> is thrown.
   * @param privacyProtocol
   *    the privacy protcol ID to be associated with this user. If set
   *    to <code>null</code>, this user only supports unencrypted messages.
   * @param privacyPassphrase
   *    the privacy passphrase. If not <code>null</code>,
   *    <code>privacyProtocol</code> must also be not <code>null</code>.
   *    RFC3414 §11.2 requires passphrases to have a minimum length of 8 bytes.
   *    If the length of <code>authenticationPassphrase</code> is less than 8
   *    bytes an <code>IllegalArgumentException</code> is thrown.
   * @param localizationEngineID
   *    if not <code>null</code>, the localizationEngineID specifies the
   *    engine ID for which the supplied passphrases are already localized.
   *    Such an USM user can only be used with the target whose engine ID
   *    equals localizationEngineID.
   */
  public UsmUser(OctetString securityName,
                 OID authenticationProtocol,
                 OctetString authenticationPassphrase,
                 OID privacyProtocol,
                 OctetString privacyPassphrase,
                 OctetString localizationEngineID) {
    this(securityName, authenticationProtocol, authenticationPassphrase,
         privacyProtocol, privacyPassphrase);
    this.localizationEngineID = localizationEngineID;
  }

  /**
   * Gets the user's security name.
   * @return
   *    a clone of the user's security name.
   */
  public OctetString getSecurityName() {
    return (OctetString) securityName.clone();
  }

  /**
   * Gets the authentication protocol ID.
   * @return
   *    a clone of the authentication protocol ID or <code>null</code>.
   */
  public OID getAuthenticationProtocol() {
    if (authenticationProtocol == null) {
      return null;
    }
    return (OID) authenticationProtocol.clone();
  }

  /**
   * Gets the privacy protocol ID.
   * @return
   *    a clone of the privacy protocol ID or <code>null</code>.
   */
  public OID getPrivacyProtocol() {
    if (privacyProtocol == null) {
      return null;
    }
    return (OID) privacyProtocol.clone();
  }

  /**
   * Gets the authentication passphrase.
   * @return
   *    a clone of the authentication passphrase or <code>null</code>.
   */
  public OctetString getAuthenticationPassphrase() {
    if (authenticationPassphrase == null) {
      return null;
    }
    return (OctetString) authenticationPassphrase.clone();
  }

  /**
   * Gets the privacy passphrase.
   * @return
   *    a clone of the privacy passphrase or <code>null</code>.
   */
  public OctetString getPrivacyPassphrase() {
    if (privacyPassphrase == null) {
      return null;
    }
    return (OctetString) privacyPassphrase.clone();
  }

  /**
   * Returns the localization engine ID for which this USM user has been already
   * localized.
   * @return
   *    <code>null</code> if this USM user is not localized or the SNMP engine
   *    ID of the target for which this user has been localized.
   * @since 1.6
   */
  public OctetString getLocalizationEngineID() {
    return localizationEngineID;
  }

  /**
   * Indicates whether the passphrases of this USM user need to be localized
   * or not (<code>true</code> is returned in that case).
   * @return
   *    <code>true</code> if the passphrases of this USM user represent
   *    localized keys.
   * @since 1.6
   */
  public boolean isLocalized() {
    return (localizationEngineID != null);
  }

  /**
   * Gets the security model ID of the USM.
   * @return
   *    {@link USM#getID()}
   */
  public int getSecurityModel() {
    return SecurityModel.SECURITY_MODEL_USM;
  }

  /**
   * Compares two USM users by their security names.
   * @param o
   *    another <code>UsmUser</code> instance.
   * @return
   *    a negative integer, zero, or a positive integer as this object is
   *    less than, equal to, or greater than the specified object.
   */
  public int compareTo(Object o) {
    // allow only comparison with UsmUsers
    UsmUser other = (UsmUser)o;
    return securityName.compareTo(other.securityName);
  }

  public Object clone() {
    UsmUser copy = new UsmUser(this.securityName, this.authenticationProtocol,
                               this.authenticationPassphrase,
                               this.privacyProtocol, this.privacyPassphrase,
                               this.localizationEngineID);
    return copy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UsmUser usmUser = (UsmUser) o;

    if (!securityName.equals(usmUser.securityName)) return false;
    if (authenticationPassphrase != null ? !authenticationPassphrase.equals(usmUser.authenticationPassphrase) : usmUser.authenticationPassphrase != null)
      return false;
    if (privacyPassphrase != null ? !privacyPassphrase.equals(usmUser.privacyPassphrase) : usmUser.privacyPassphrase != null)
      return false;
    if (authenticationProtocol != null ? !authenticationProtocol.equals(usmUser.authenticationProtocol) : usmUser.authenticationProtocol != null)
      return false;
    if (privacyProtocol != null ? !privacyProtocol.equals(usmUser.privacyProtocol) : usmUser.privacyProtocol != null)
      return false;
    if (localizationEngineID != null ? !localizationEngineID.equals(usmUser.localizationEngineID) : usmUser.localizationEngineID != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    return securityName.hashCode();
  }

  public String toString() {
    return "UsmUser[secName="+securityName+
        ",authProtocol="+authenticationProtocol+
        ",authPassphrase="+authenticationPassphrase+
        ",privProtocol="+privacyProtocol+
        ",privPassphrase="+privacyPassphrase+
        ",localizationEngineID="+getLocalizationEngineID()+"]";
  }

}
