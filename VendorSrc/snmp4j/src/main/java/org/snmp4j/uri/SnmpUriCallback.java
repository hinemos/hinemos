/*_############################################################################
  _## 
  _##  SNMP4J - SnmpUriCallback.java  
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

import java.net.URI;

/**
 * The <code>SnmpUriCallback</code> interface is used by asynchronous
 * methods of the {@link SnmpURI} class to provide instances of 
 * {@link SnmpUriResponse} to the caller.
 * 
 * @author Frank Fock
 * @since 2.1
 */
public interface SnmpUriCallback {

  /**
   * Process a response on the request
   * @param response
   *    a {@link SnmpUriResponse} instance with some or all
   *    of the requested data or an error status.
   *    If the {@link org.snmp4j.uri.SnmpUriResponse#getResponseType()}
   *    is {@link org.snmp4j.uri.SnmpUriResponse.Type#NEXT} then
   *    additional calls for this request will follow, otherwise not.
   * @param url
   *    the URI that was used as request for this response.
   * @param userObject
   *    an arbitrary object provided on the asynchronous call
   *    on the request processor.
   * @return
   *    <code>true</code> if the request should be cancelled,
   *    <code>false</code> otherwise.
   */
  boolean onResponse(SnmpUriResponse response, URI url, Object userObject);
  
}

