/*_############################################################################
  _## 
  _##  SNMP4J - SnmpUriResponse.java  
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

package org.snmp4j.uri;

import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.VariableBinding;

import java.util.List;

/**
 * The <code>SnmpUriResponse</code> contains the data returned by a SNMP URI
 * operation. See {@link SnmpURI} for details.
 *
 * @author Frank Fock
 * @since 2.1
 */
public class SnmpUriResponse {

  public enum Type { FINAL, NEXT, TIMEOUT, SNMP_ERROR, IO_ERROR, SECURITY_ERROR, LEXICOGRAPHIC_ORDER_ERROR };

  private List<VariableBinding[]> data;  
  private int errorStatus = PDU.noError;
  private Type responseType = Type.FINAL;
  private String errorMessage;

  public SnmpUriResponse(List<VariableBinding[]> data) {
    this.data = data;
  }

  public SnmpUriResponse(Type responseType) {
    this.responseType = responseType;
  }

  public SnmpUriResponse(Type responseType, String errorMessage) {
    this.responseType = responseType;
    this.errorMessage = errorMessage;
  }

  public SnmpUriResponse(int errorStatus) {
    this.errorStatus = errorStatus;
    this.responseType = Type.SNMP_ERROR;
  }

  public SnmpUriResponse(List<VariableBinding[]> vbs, int errorStatus) {
    this(vbs);
    this.errorStatus = errorStatus;
    this.responseType = Type.SNMP_ERROR;
  }

  public SnmpUriResponse(List<VariableBinding[]> vbs, Type responseType) {
    this(vbs);
    this.responseType = responseType;
  }

  public List<VariableBinding[]> getData() {
    return data;
  }

  public int getErrorStatus() {
    return errorStatus;
  }

  public void setResponseType(Type responseType) {
    this.responseType = responseType;
  }

  public Type getResponseType() {
    return responseType;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public String toString() {
    return "SnmpUriResponse{" +
               "data=" + data +
               ", errorStatus=" + errorStatus +
               ", responseType=" + responseType +
               ", errorMessage='" + errorMessage + '\'' +
               '}';
  }
}


