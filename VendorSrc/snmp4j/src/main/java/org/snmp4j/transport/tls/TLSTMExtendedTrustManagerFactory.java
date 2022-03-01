/*_############################################################################
  _## 
  _##  SNMP4J - TLSTMExtendedTrustManagerFactory.java  
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

import org.snmp4j.TransportStateReference;
import org.snmp4j.mp.CounterSupport;
import org.snmp4j.transport.TLSTM;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * X509ExtendedTrustManager factory for TLSTM.
 * @author Frank Fock
 * @since 2.5.7
 */
public class TLSTMExtendedTrustManagerFactory implements TLSTM.TLSTMTrustManagerFactory {

  private TlsTmSecurityCallback<X509Certificate> securityCallback;
  private CounterSupport counterSupport;

  public TLSTMExtendedTrustManagerFactory(CounterSupport counterSupport,
                                          TlsTmSecurityCallback<X509Certificate> tlsTmSecurityCallback) {
    this.counterSupport = counterSupport;
    this.securityCallback = tlsTmSecurityCallback;
  }

  @Override
  public X509TrustManager create(X509TrustManager trustManager, boolean useClientMode,
                                 TransportStateReference tmStateReference) {
    return new TLSTMExtendedTrustManager(counterSupport, securityCallback,
            trustManager, useClientMode, tmStateReference);
  }
}
