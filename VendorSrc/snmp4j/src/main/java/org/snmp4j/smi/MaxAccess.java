/*_############################################################################
  _## 
  _##  SNMP4J - MaxAccess.java  
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

package org.snmp4j.smi;

/**
 * The <code>MaxAccess</code> enumerates the MAX-ACCESS values of SMIv2 and the deprecated {@link #writeOnly}
 * of SMIv1.
 *
 * @author Frank Fock
 * @since 2.5.0
 */
public enum MaxAccess {

  notAccessible("not-accessible"),
  accessibleForNotify("accessible-for-notify"),
  writeOnly("write-only"),
  readOnly("read-only"),
  readWrite("read-write"),
  readCreate("read-create");

  private String smiValue;

  private MaxAccess(String smiValue) {
    this.smiValue = smiValue;
  }

  /**
   * Gets the MAX-ACCESS (or ACCESS in SMIv1) clause string.
   * @return
   *    the SMI access string.
   */
  public String getSmiValue() {
    return smiValue;
  }

  /**
   * Gets the {@link org.snmp4j.smi.MaxAccess} from a MAX-ACCESS (or ACCESS in SMIv1) clause string.
   * @param smiValue
   *    the SMI access string.
   * @return
   *    <code>null</code> if <code>smiValue</code> is not a valid ACCESS/MAX-ACCESS clause value or
   *    the matching enumeration value.
   */
  public static MaxAccess fromSmiValue(String smiValue) {
    for (MaxAccess v : values()) {
      if (v.getSmiValue().equals(smiValue)) {
        return v;
      }
    }
    return null;
  }

  /**
   * Checks if the maximum access is {@link #readOnly}.
   * @return
   *    <code>true</code> if this maximum access equals {@link #readOnly}.
   */
  public boolean isReadOnly() {
    return this == readOnly;
  }

  /**
   * Checks if the maximum access is writable.
   * @return
   *    <code>true</code> if this maximum access equals {@link #readWrite} or {@link #readCreate}.
   */
  public boolean isWritable() {
    return ordinal() >= readWrite.ordinal();
  }

  /**
   * Checks if the maximum access is creatable.
   * @return
   *    <code>true</code> if this maximum access equals {@link #readCreate}.
   */
  public boolean isCreatable() {
    return this == readCreate;
  }

}
