/*_############################################################################
  _## 
  _##  SNMP4J - TsmSecurityParameters.java  
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

import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.smi.OctetString;

import java.io.IOException;

/**
 * {@link SecurityParameters} implementation for the {@link TSM}
 * security model.
 * @author Frank Fock
 * @version 2.0
 * @since 2.0
 */
public class TsmSecurityParameters extends OctetString implements SecurityParameters {

  private int securityParametersPosition;
  private int decodedLength = -1;

  public TsmSecurityParameters() {
    super();
  }

  @Override
  public int getSecurityParametersPosition() {
    return securityParametersPosition;
  }

  @Override
  public void setSecurityParametersPosition(int pos) {
    this.securityParametersPosition = pos;
  }

  @Override
  public int getBERMaxLength(int securityLevel) {
    return getBERLength();
  }

  @Override
  public void decodeBER(BERInputStream inputStream) throws IOException {
    long startPos = inputStream.getPosition();
    super.decodeBER(inputStream);
    decodedLength = (int) (inputStream.getPosition() - startPos);
  }

  /**
   * Gets the position of the {@link org.snmp4j.ScopedPDU}.
   *
   * @return
   *    the start position in the {@link BERInputStream}.
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
