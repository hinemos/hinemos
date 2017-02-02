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
//  File:       DefaultKeyManager.java
//
//  Contents:   Default Key Manager for testing purposes only
//
//  Notes:      Simplifies testing of TLS connections
//
//----------------------------------------------------------------------------

package intel.management.wsman;


import javax.net.ssl.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.net.*;
import java.io.*;



/**
 * Allows X.509 private keys to be loaded from PKCS12 key stores
 *
 * <P>
 *
 *@see #getPrivateKey(String)
 *
 *
 */
public class DefaultKeyManager extends X509ExtendedKeyManager {
    
    private KeyStore store;
    private String password;
        


     /**
     * Constructs the KeyManager given a path to a PKCS12 file and its password
     *
     *
     * @param path Path to the PKSC12 file
     * @param password Password protecting the private key
     *
     *
     *
     */
    public DefaultKeyManager(String path,String password) throws IOException {
        
        InputStream stream = null;
        this.password=password;
        try {
            store = KeyStore.getInstance("PKCS12");
            stream = new FileInputStream(path);
            store.load(stream, password.toCharArray());
            
        } catch (KeyStoreException e) {
            throw new WsmanRuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new WsmanRuntimeException(e);
        } catch (CertificateException e) {
            throw new WsmanRuntimeException(e);
        } finally {
            if (stream!=null) stream.close();
        }

    }


     /**
     * Constructs the KeyManager given a KeyStore and a password
     *

     *
     * @param store The key store containing the private key
     * @param password Password protecting the private key
     *
     *
     *
     */
    public DefaultKeyManager(KeyStore store,String password) {
        this.password=password;
        this.store=store;
    }


     /**
     * Returns all names of the client keys known to the manager
     *
     *

     * @return
     * The list of aliases or names
     */
    public String[] getClientAliases(String keyType,
                          Principal[] issuers) {
        return null;
    }


    /**
     * Selects a specific client alias using the given key type and issuer information
     *
     * @param keyType The type of key to look for
     * @param issuers The principals associated with the key
     * @param socket The socket the client is using

     * @return
     * The alias or name of the chosen key
     */
    public String chooseClientAlias(String[] keyType,
                         Principal[] issuers,
                         Socket socket) {
        String alias=null;
         try {
            Enumeration aliases = store.aliases();
            while (aliases.hasMoreElements()) {
                alias= aliases.nextElement().toString();
            }

        }
        catch (Exception e) {
        }
        return alias;
    }


     /**
     * Returns all names of the server keys known to the manager
     *
     *

     * @return
     * The list of aliases or names
     */
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return null;
    }


     /**
     * Selectes a specific server alias using the given key type and issuer information
     *
     * @param keyType The type of key to look for
     * @param issuers The principals associated with the key

     * @return
     * The  alias or name of the chosen key
     */
    public String chooseServerAlias(String keyType,
                         Principal[] issuers,
                         Socket socket) {
        return null;
    }

     /**
     * Gets the entire certificate chain for a given alias
     *
     * @param alias The alias or name known to the key manager
    *
     * @return
     * The list of cerificates in the chain
     */
    public X509Certificate[] getCertificateChain(String alias) {
        X509Certificate[] result=null;
        try {
            java.security.cert.Certificate[] certs = store.getCertificateChain(alias);
            if (certs!=null) {
                result = new X509Certificate[certs.length];
                System.arraycopy(certs, 0, result, 0, certs.length);
            }

        } catch (java.security.KeyStoreException e) {
        }
        return result;
    }


       /**
     * Gets a private key from the key manager
     *
     * @param alias The alias or name known to the key manager
    *
     * @return
     * The private key
     */
    public PrivateKey getPrivateKey(String alias) {

        Key key=null;
        try {
            key = store.getKey(alias, password.toCharArray());
        } catch (java.security.KeyStoreException e) {
        } catch (java.security.NoSuchAlgorithmException e) {
        } catch (java.security.UnrecoverableKeyException e) {
        }

        return (PrivateKey)key;
    }

}
