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
//  File:       WsmanConnection.java
//
//  Contents: Contains the definition of the WsmanConnection class
//
//  Notes:
//
//----------------------------------------------------------------------------

package intel.management.wsman;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import javax.net.ssl.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.codec.binary.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.auth.AuthScheme;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.apache.hc.client5.http.auth.StandardAuthScheme;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicSchemeFactory;
import org.apache.hc.client5.http.impl.auth.KerberosSchemeFactory;
import org.apache.hc.client5.http.impl.auth.NTLMSchemeFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Timeout;
import org.ietf.jgss.*;

import sun.nio.cs.ext.IBM037;

/**
 * Represents a connection to a WS-Management service.
 *
 * <P>
 * See <A HREF="http://www.dmtf.org/standards/wsman/">
 * http://www.dmtf.org/standards/wsman/</A>
 * for more details on the WS-Management specification.
 * <P>
 * Managed resources can be exchanged with a WS-Management
 * compliant services using this class.
 *
 * @see #newInstance(java.lang.String)
 * @see #newReference(java.lang.String)
 *
 *
 *
 */
public class WsmanConnection {


  /**
   * A dictionary of connection properties.
   *
   */
    protected Dictionary properties;

   /**
     * Constructs a new instance of a <code>WsmanConnection</code>
     *
     * @param address The transport address of the service
     *
     *
     *
     */
    protected WsmanConnection(String address) {

        properties = new java.util.Hashtable();
        properties.put("Address", address);

    }


     /**
     * Get the XML loader used to load XML documents.
     *
     *
     * @return
     * The XML loader for the connection
     *
     */
    protected WsmanUtils getXmlLoader() {
        WsmanUtils loader = (WsmanUtils)properties.get("XMLLoader");

        if (loader==null) {
            try {
                loader = new WsmanUtils();
                loader.enablePrettyFormatting(true);
                properties.put("XMLLoader", loader);
            }
            catch(Exception exception) {
                throw new WsmanRuntimeException(exception);
            }
        }

        return loader;
    }

   /**
     * Stamp a Wsman message with a unique message Id and set the address.
     *
     *
     * @param document The XML message to stamp
     *
     *  @return
     * The stamped XML
     */
    protected String stampRequest(Document document) {
        String messageId=null;
        Element headerElt=WsmanUtils.findChild(document.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Header");


        Element msgElt=WsmanUtils.findChild(headerElt,
                WsmanUtils.ADDRESSING_NAMESPACE,
                "MessageID");

        if (msgElt != null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("uuid:");
            buffer.append(
                    java.util.UUID.randomUUID().toString().toUpperCase());
            messageId=buffer.toString();
            msgElt.setTextContent(messageId);
        }

        Element toElt=WsmanUtils.findChild(headerElt,
                WsmanUtils.ADDRESSING_NAMESPACE,
                "To");


        if (toElt !=null)
            toElt.setTextContent((String)properties.get("Address"));

        return messageId;
    }


    /**
     * Get the response from a HTTP connection.
     *
     *
     * @param conn The <code>HttpURLConnection</code>
     *
     * @return
     * The response object (e.g Exception object or Xml Object)
     */
    protected Object getResponse(HttpURLConnection conn) {

        Object result=null;
        try {

            // print headers
            if (System.getProperty("intel.management.wsman.debug","false").equals("true")) {
                System.out.println();
                for (int i=0; i< conn.getHeaderFields().size();i++) {
                    System.out.print(
                    conn.getHeaderFieldKey(i));
                       System.out.print("=") ;

                       System.out.println(conn.getHeaderField(i));
                }
            }
            switch (conn.getResponseCode()) {

                case HttpURLConnection.HTTP_UNAUTHORIZED:

                    properties.put("Authorization",
                            conn.getHeaderField("WWW-Authenticate"));
                    break;
                case HttpURLConnection.HTTP_BAD_REQUEST:
                case HttpURLConnection.HTTP_INTERNAL_ERROR:
                    result = getXmlLoader().loadDocument(conn.getErrorStream());
                    break;

            }
        }
        catch (Exception e) {
            result=e;
        }

        return result;
    }

    /**
     * Send a XML request using WS-Management protocol.
     *
     *
     * @param request The XML request document
     *
     * @return
     * The response XML document
     *
     * @throws WsmanException when sending the request fails
     */
    protected Document SendRequest(Document request) throws WsmanException {

        HttpURLConnection conn=null;
        Document response=null;
        CloseableHttpResponse httpResponse = null;
        stampRequest(request);
        boolean printDebug=
                System.getProperty("intel.management.wsman.debug","false").equals("true");


        int retry=2; //inital retry count for connection
        while (retry>0) {
            try {
                Object auth = properties.get("AuthScheme");
                URL url = new URL((String)properties.get("Address"));

                SSLSocketFactory factory =
                        (SSLSocketFactory)properties.get("SSLSocketFactory");
                
                X509TrustManager tm=
                        (X509TrustManager)properties.get("X509TrustManager");

                HostnameVerifier verifier=
                        (HostnameVerifier)properties.get("HostnameVerifier");

                X509KeyManager km=
                        (X509KeyManager)properties.get("X509KeyManager");



                if (factory==null && (km!=null || tm!=null)) {
                    X509KeyManager[] keys = null;
                    X509TrustManager[] trusts = null;

                    SSLContext sc = SSLContext.getInstance("SSL");

                    if (km!=null) {
                        keys = new X509KeyManager[1];
                        keys[0]=km;
                    }

                    if (tm!=null) {
                        trusts = new X509TrustManager[1];
                        trusts[0]=tm;
                    }

                    sc.init(keys, trusts, null);
                    factory = sc.getSocketFactory();

                    properties.put("SSLSocketFactory",factory);
                }

                // NTLM認証の場合はHttpClientで通信を行う
                if (auth != null && auth.equals("ntlm")) {
                    NTCredentials ntCreds = new NTCredentials(getUsername(), getUserpassword().toCharArray(), null, null);

                    CredentialsStore credsProvider = new BasicCredentialsProvider();

                    AuthScope targetScope = new AuthScope(url.getProtocol(), url.getHost(), url.getPort(), null, "ntlm");
                    credsProvider.setCredentials(targetScope, ntCreds);
                    

                    Registry<AuthSchemeFactory> authSchemeRegistry = RegistryBuilder.<AuthSchemeFactory>create()
                            .register(StandardAuthScheme.BASIC, BasicSchemeFactory.INSTANCE)
                            .register(StandardAuthScheme.SPNEGO, new SpNegoNTLMSchemeFactory())
                            .register(StandardAuthScheme.KERBEROS, KerberosSchemeFactory.DEFAULT)
                            .build();

                    RequestConfig config = RequestConfig.custom()
                            .setTargetPreferredAuthSchemes(Arrays.asList(StandardAuthScheme.NTLM
                                    , StandardAuthScheme.KERBEROS, StandardAuthScheme.SPNEGO))
                            .setConnectTimeout(Timeout.ofMilliseconds(getTimeout()))
                            .setConnectionRequestTimeout(Timeout.ofMilliseconds(getTimeout()))
                            .build();

                    SSLConnectionSocketFactory sslSocketFactory = null;
                    if (factory != null) {
                        sslSocketFactory = new SSLConnectionSocketFactory(factory, verifier);
                    }

                    // 対象のホストがignoreHostListに存在する場合はプロキシ設定を行わない
                    HttpHost httpHost = null;
                    List<String> proxyIgnoreHostList = (List<String>)properties.get("ProxyIgnoreHostList");
                    if (proxyIgnoreHostList != null && proxyIgnoreHostList.contains(url.getHost())) {
                        // do nothing
                    } else {
                        // プロキシ設定
                        String proxyHost = (String)properties.get("ProxyHost");
                        Integer proxyPort = (Integer)properties.get("ProxyPort");
                        if (proxyHost != null && proxyPort != null) {
                            httpHost = new HttpHost(proxyHost, proxyPort);
                            AuthScope proxyScope = new AuthScope(proxyHost, proxyPort);

                            // 認証情報の設定
                            String proxyUsername = (String)properties.get("ProxyUsername");
                            String proxyPassword = (String)properties.get("ProxyPassword");
                            if (proxyUsername != null && proxyPassword != null) {
                                Credentials proxyCreds = new UsernamePasswordCredentials(proxyUsername, proxyPassword.toCharArray());
                                credsProvider.setCredentials(proxyScope, proxyCreds);
                            }
                        }
                    }

                    HttpClientBuilder builder = HttpClients.custom()
                            .setDefaultCredentialsProvider(credsProvider);
                    builder.setProxy(httpHost);

                    PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();
                    connectionManagerBuilder.setSSLSocketFactory(sslSocketFactory);
                    SocketConfig socketConfig = SocketConfig.custom()
                            .setSoTimeout(Timeout.ofMilliseconds(getTimeout())).build();
                    connectionManagerBuilder.setDefaultSocketConfig(socketConfig);

                    CloseableHttpClient closeableClient = builder.setConnectionManager(connectionManagerBuilder.build()).build();

                    HttpPost httpPost = new HttpPost(getAddress()); 
                    httpPost.addHeader("Content-Type", "application/soap+xml;charset=UTF-8");
                    String soapxml = getXmlLoader().formatDocument(request);
                    String body = soapxml;
                    StringEntity entity = new StringEntity(body, StandardCharsets.UTF_8);
                    httpPost.setEntity(entity);

                    HttpClientContext context = HttpClientContext.create();
                    context.setCredentialsProvider(credsProvider);
                    context.setAuthSchemeRegistry(authSchemeRegistry);
                    context.setRequestConfig(config);

                    httpResponse = closeableClient.execute(httpPost, context);
                    response = getXmlLoader().loadDocument(httpResponse.getEntity().getContent());
                    return response;
                } else {
                    if (conn!=null) {

                        conn.disconnect();
                    }

                    Proxy proxy = (Proxy)properties.get("HttpProxy");
                    if (proxy!=null)
                        conn= (HttpURLConnection)url.openConnection(proxy);
                    else
                        conn= (HttpURLConnection)url.openConnection();
                    if (conn instanceof HttpsURLConnection) {
                        HttpsURLConnection sslConn = (HttpsURLConnection)conn;

                        if (factory!=null)
                            sslConn.setSSLSocketFactory(factory);

                        if (verifier!=null)
                            sslConn.setHostnameVerifier(verifier);
                    }




                    if (auth!=null && auth.equals("kerberos")) {
                        // MecOid for Kerberos authorizaton (see Kerberos spec)
                        Oid spnegoMecOid= new Oid("1.3.6.1.5.5.2");
                        GSSManager manager =
                            org.ietf.jgss.GSSManager.getInstance();

                        String spnName="HTTP/" + url.getHost();

                        int spnPort = url.getPort();
                        // force SPN port on hardware ports
                        if (spnPort==16992|| spnPort==16993 ||
                                spnPort==623|| spnPort==624 ) {
                           spnName=spnName+":"+Integer.toString(spnPort);
                        }

                        GSSName gssName=manager.createName(spnName, null);

                        GSSContext context =
                            manager.createContext(gssName,
                            spnegoMecOid,
                            null,
                            GSSCredential.DEFAULT_LIFETIME);

                        context.requestCredDeleg(true);
                        byte[] token = new byte[0];

                        token = context.initSecContext(token, 0, token.length);


                        String tokenStr = WsmanUtils.getBase64String(token);


                        conn.addRequestProperty("Authorization",
                                "Negotiate " +tokenStr );

                    }
                    else if (auth!=null && auth.equals("basic")) {

                    	// Comment out by NTT DATA Corporation
                    	/*
                        java.net.Authenticator.requestPasswordAuthentication(url.getHost(),
                                null, url.getPort(), url.getProtocol(),"", "basic");

                        String tokenStr="";
                        conn.addRequestProperty("Authorization",
                                "Basic " +tokenStr );
                        */

                    	// Add by NTT DATA Corporation
                    	String userAndPassword = getUsername() + ":" + getUserpassword();
                        byte[] userAndPasswordByte = org.apache.commons.codec.binary.Base64.encodeBase64(userAndPassword.getBytes());  
                        conn.addRequestProperty("Authorization",
                                "Basic " + new String(userAndPasswordByte) );

                    }

                    conn.setRequestMethod("POST");
                    conn.addRequestProperty("Content-Type",
                        "application/soap+xml;charset=UTF-8");

                    conn.setDoOutput(true);
                    conn.setReadTimeout(getTimeout()); // Add by NTT DATA Corporation
                    conn.setConnectTimeout(getTimeout()); // Add by NTT DATA Corporation

                    if (printDebug)
                        System.out.println(getXmlLoader().formatDocument(request));

                    getXmlLoader().saveDocument(request,
                            conn.getOutputStream());



                    InputStream s=conn.getInputStream();
                    response= getXmlLoader().loadDocument(s);
                    if (printDebug) {

                        System.out.println(getXmlLoader().formatDocument(response));
                    }
 
                    // we got a doc so must be Http status 200 OK
                    conn.getResponseCode();
                    retry=0;
                    conn.disconnect();
                    conn=null;

                }

            }
            catch (IOException ioException) {

                retry--;

                if (conn != null) {
                    int max = conn.getHeaderFields().size();
                    for (int i=0;i<max;i++)
                    {
                        String t = conn.getHeaderField(i);


                        t.toString();
                    }

                    conn.getRequestProperty("Authorization");
                    conn.getHeaderField("Authorization");
                    Object errObj = getResponse(conn);
                    if (errObj !=null && errObj instanceof Document) {
                        response=(Document)errObj;
                        retry=0;
                        throw new WsmanException(this,response);
                    }
                    else if (errObj!=null)
                        throw new WsmanException(ioException);
                }
                if (retry==0)
                    throw new WsmanException(ioException);
            }
            catch (WsmanException wsmanException) {
                if (httpResponse != null) {
                    StatusLine statusLine = new StatusLine(httpResponse);
                    if (statusLine != null) {
                        retry=0;
                        throw new WsmanException(wsmanException, statusLine.toString());
                        }
                }
                retry=0;
                throw wsmanException;
            }
            catch (Exception exception) {
                retry=0;
                throw new WsmanException(exception);
            }
        }
        return response;
    }
    private class SpNegoNTLMSchemeFactory extends NTLMSchemeFactory {

        @Override
        public AuthScheme create(HttpContext context) {
        	return new ApacheSpnegoScheme();
        }
    }

  /**
     *  Get the transport address used by the connection.
     *
     * @return
     * The transport address of the service
     *
     */
    public String getAddress() {

        return (String)properties.get("Address");

    }


     /**
     * Set the transport address used by the connection.
     *
     *
     * @param address The transport address of the service
     *
     */
    public void setAddress(String address) {

        properties.put("Address",address);
        properties.remove("Authorization");

    }

    /**
     * Set the username which is used when authenticating the connection.
     *
     * @param username The name of the user credential
     */
    public void setUsername(String username) {
        properties.put("username",username);
        properties.remove("Authorization");
    }

     /**
     * Get the username which is used to authenticate the connection.
     *
     * @return
     * The name of the user being used for authentication
     *
     */
    public String getUsername() {
        return (String)properties.get("username");
    }

    /**
     * Add by NTT DATA Corporation
     * @param userpassword
     */
    public void setUserpassword(String userpassword) {
        properties.put("userpassword",userpassword);
        properties.remove("Authorization");
    }

    /**
     * Add by NTT DATA Corporation
     * @return
     */
    public String getUserpassword() {
        return (String)properties.get("userpassword");
    }

    /**
     * Add by NTT DATA Corporation
     * @param timeout
     */
    public void setTimeout(int timeout) {
        properties.put("timeout",new Integer(timeout));//msec
    }
    
    /**
     * Add by NTT DATA Corporation
     * @return
     */
    public int getTimeout() {
    	Object obj = properties.get("timeout");
    	Integer timeout;
    	if(obj != null && obj instanceof Integer){
    		timeout = (Integer)obj;
    		return timeout.intValue();
    	}
    	else{
    		return 3000;//msec
    	}
    }
    
     /**
     * Set the authentication scheme the connection should expect.
     *
     * @param scheme The authentication scheme
     *
     *
     */
    public void setAuthenticationScheme(String scheme) {
        properties.put("AuthScheme",scheme.toLowerCase());
    }

     /**
     * Set the HTTP proxy for requests. Useful for remote connections.
     *
     * @param proxy Proxy setting information
     *
     *
     */
    public void setProxy(Proxy proxy) {
        properties.put("HttpProxy",proxy);
    }

    /**
     * Set the proxy server hostname.
     * @param proxyHost
     */
    public void setProxyHost(String proxyHost) {
        properties.put("ProxyHost", proxyHost);
    }

    /**
     * Set the proxy server port.
     * @param proxyPort
     */
    public void setProxyPort(Integer proxyPort) {
        properties.put("ProxyPort", proxyPort);
    }

    /**
     * Set the proxy server username.
     * @param proxyUsername
     */
    public void setProxyUsername(String proxyUsername) {
        properties.put("ProxyUsername", proxyUsername);
    }

    /**
     * Set the proxy server password.
     * @param proxyPassword
     */
    public void setProxyPassword(String proxyPassword) {
        properties.put("ProxyPassword", proxyPassword);
    }

    /**
     * Set the proxy server ignore hosts.
     * @param ignoreHostList
     */
    public void setProxyIgnoreHostList(List<String> proxyIgnoreHostList) {
        properties.put("ProxyIgnoreHostList", proxyIgnoreHostList);
    }

    /**
     * Get the authentication scheme the connection is using
     *
     * @return
     * The name of authentication scheme
     *
     *
     */
    public String getAuthenticationScheme() {
       Object scheme=  properties.get("AuthScheme");
       if (scheme==null)
           scheme="digest";

       return scheme.toString();
    }

   /**
     * Set the trust manager to be used for verifiying certificates
     *
     * @param manager Trust Manager
     *
     *
     *
     */
    public void setTrustManager(X509TrustManager manager) {
        properties.put("X509TrustManager",manager);
    }

    /**
     * Set the key manager which is used for exchanging RSA material
     *
     * @param manager Key Manager
     *
     *
     *
     */
    public void setKeyManager(X509KeyManager manager) {
        properties.put("X509KeyManager",manager);
    }


     /**
     * Set the host name verifier
     *
     * @param verifier Host name verifier
     *
     *
     *
     */
    public void setHostnameVerifier(HostnameVerifier verifier ) {
            properties.put("HostnameVerifier",verifier);
    }



    /**
     * Create a new instance of a <code>WsmanConnection</code>
     *
     * @param address The transport address of the service
     *
     * @return
     * New instance of a <code>WsmanConnection</code>
     *
     *
     */
    public static WsmanConnection createConnection(String address) {
        return new WsmanConnection(address);
    }



    /**
     * Create a new instance that represents a Managed Resource
     *
     *
     * @param resourceURI The URI of the resource class
     * or instance to be created

     * @return
     * New instance of a <code>ManagedInstance</code>
     *
     * @see ManagedInstance
     *
     */
    public ManagedInstance newInstance(String resourceURI) {
        return new ManagedInstance(this,
                getXmlLoader().createDocument(resourceURI));
    }




     /**
     * Create a reference to a managed resource
     *
     *
     * @param resourceURI The URI of the resource class
     * or instance to be accessed

     * @return
     * New instance of a <code>ManagedReference</code>
     *
     * @see ManagedReference
     *
     */
    public ManagedReference newReference(String resourceURI) {
        ManagedReference ref=null;
        try {
            ref = new ManagedReference(this,
                 getXmlLoader().loadDocument("EprXMLDoc.xml"));
            ref.setResourceURI(resourceURI);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        return ref;
    }




}
