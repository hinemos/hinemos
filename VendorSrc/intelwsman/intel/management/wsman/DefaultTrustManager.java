/*
Copyright (c) 2009 - 2010, Intel Corporation
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
      in the documentation and/or other materials provided with the distribution.
    * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from
      this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

//----------------------------------------------------------------------------
//
//  File:       DefaultTrustManager.java
//
//  Contents:   Default Trust Manager for testing purposes only
//
//  Notes:      For testing purposes only, this class trusts all certificates
//
//----------------------------------------------------------------------------

package intel.management.wsman;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;


/**
 * Verifies certificate trust for TLS connections
 *
 * <P>
 *
 * The DefaultTrustManager is primarily intended for testing and application development.
 * This trust manager <b><u>trusts any</u></b> certificate returned during a TLS connection.
 * In a secure deployment, an application would implement its own trust manager that
 * only trusts specific certificates found in its own certificate store.
 *
 *
 * @see #getAcceptedIssuers()
 * @see #verify(String,SSLSession)
 *
 */

public class DefaultTrustManager
        implements X509TrustManager, HostnameVerifier {


     /**
     * Returns an array of certificate authorities to trust.
     *
     *
     *
     * @return
     * The list of certificate issuers or authorities to trust.
     *
     */
    public X509Certificate[] getAcceptedIssuers() {
        X509Certificate[] certs = {};
        return certs;
    }

     /**
     * Returns <code>true</code> if a client certificate is trusted.
     *
     *
     * @param certs The certificates sent by the client
     * @param authType The type of authorization requested
     *
     *
     *
     */
    public void checkClientTrusted(X509Certificate[] certs, String authType) {
    }

    /**
     * Returns <code>true</code> if a server certificate is trusted.
     *
     * @param certs The certificates received from the service
     * @param authType The type of authorization requested
     *
     *
     *
     */
    public void checkServerTrusted(X509Certificate[] certs, String authType) {
    }

    /**
     * Returns <code>true</code> if the host name is trusted for the SSLSession.
     *
     *
     * @param hostname The name of the host
     * @param session The SSLSession being used
     *
     * @return
     * <code>true</code> if the host is trusted for SSLSession
     */
    public boolean verify(String hostname, SSLSession session) {
        System.out.println(session.toString());
        System.out.println(WsmanUtils.getBase64String(session.getId()));
        return true;
    }

}
