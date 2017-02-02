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
//  File:       DefaultAuthenticator.java
//
//  Contents:   Default Authenticator class that extends Authenticator
//
//  Notes:      For test purposes only.  Provides a default callback method should
//              digest authentication fail.
//
//----------------------------------------------------------------------------

package intel.management.wsman;

import java.net.Authenticator;
import java.net.PasswordAuthentication;



/**
 * Provides user name/password authentication for WS-Managment connections
 *
 * <P>
 *This class is used to provide credentials for connections that require user name/password authentication.
 *Use addCredential() to provide the user name and password for a specific address and
 *java.net.Authenticator.setDefault() to set authenticator as the source
 *for user name/passwords.  Applications can define their own password storage and retrieval mechanisms
 * by extending Authenticator and implementing getPasswordAuthentication().
 *
 *
 * @see #addCredential(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
 *
 */
public class DefaultAuthenticator extends Authenticator {

    java.util.Hashtable urlTable;
    
    
   /**
     * Constructs a default authenticator
     */
    public DefaultAuthenticator() {
        urlTable = new java.util.Hashtable();


    }

     /**
     * Specifies the user name and password for a specific URL
     *
     *
     * @param url the transport address of the service authenticating to
     * @param scheme the authentication scheme (e.g. "digest")
     * @param userName the user name of the account
     * @param password the account password
     *
     */
    public void addCredential(String url,
            String scheme,
            String userName,
            String password) {

        String key= scheme.toLowerCase()+url;
        if (scheme.equalsIgnoreCase("basic")) {
            //java.net.URI uri = new
        }

        urlTable.put(key,
                new PasswordAuthentication(userName,
                                password.toCharArray()));
       
    }


     /**
     *  Returns the password authentication.   
     *
     * @return
     * the PawsswordAuthentication
     *
     */
    public PasswordAuthentication getPasswordAuthentication() {

      PasswordAuthentication pa= null;


         pa = (PasswordAuthentication)urlTable.get(
                    getRequestingScheme().toLowerCase()+
                    getRequestingURL().toString().toLowerCase());



        return pa;
    }

}
