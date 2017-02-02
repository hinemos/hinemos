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
//  File:       WsmanException.java
//
//  Contents: Contains the definition of the WsmanException
//
//  Notes:
//
//----------------------------------------------------------------------------

package intel.management.wsman;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents an WS-Management fault or transport error that occured when performing
 * a WS-Management operation.
 *
 *
 * <P>
 *
 * @see #getReason()
 *
 *
 *
 */
public class WsmanException  extends Exception {

    /**
     * The XML document containing the fault.
     *
     */
    protected Document document;

    /**
     * The WsmanConnection associated with the fault.
     *
     */
    protected WsmanConnection connection;


     /**
     * Construct a WsmanException from an XML document and a WsmanConnection.
     *

     *
     * @param connection The connection to use for all Wsman operations
     * @param document The XML containing the fault
     *
     *
     *
     */
    WsmanException(WsmanConnection connection, Document document) {
        this.document=document;
        this.connection=connection;
    }
    /**
     * Construct a WsmanException from a java Exception.
     *
     *
     * @param cause The source of the exception
     *
     *
     *
     */
    WsmanException(Exception cause) {
        super(cause.getMessage());

        initCause(cause);
        document=null;
    }

     /**
     * Get the fault code (see WS-Management spec).
     *
     *
     *
     *
     * @return
     * The fault code.
     *
     */
    public String getCode() {
        String code ="Unknown";

        if (isFault()) {
            
            Element bodyElt=WsmanUtils.findChild(document.getDocumentElement(),
                    WsmanUtils.SOAP_NAMESPACE,
                    "Body");
            
            Element faultElt=WsmanUtils.findChild(bodyElt,
                    WsmanUtils.SOAP_NAMESPACE,
                    "Fault");
            
            Element codeElt=WsmanUtils.findChild(faultElt,
                    WsmanUtils.SOAP_NAMESPACE,
                    "Code");

            Element valueElt=WsmanUtils.findChild(codeElt,
                    WsmanUtils.SOAP_NAMESPACE,
                    "Value");

            code = valueElt.getTextContent();
        }
        else if (getCause()!=null) {
            code = getCause().getClass().getSuperclass().toString();
        }
        return code;
    }

 /**
     * Get the fault SubCode (see WS-Management spec).
     *
     *
     *
     * @return
     * The fault subcode
     *
     */
    public String getSubCode() {
        String subCode = "Unknown";

        if (isFault()) {
            
            Element bodyElt=WsmanUtils.findChild(document.getDocumentElement(),
                    WsmanUtils.SOAP_NAMESPACE,
                    "Body");

            Element faultElt=WsmanUtils.findChild(bodyElt,
                    WsmanUtils.SOAP_NAMESPACE,
                    "Fault");

            Element codeElt=WsmanUtils.findChild(faultElt,
                    WsmanUtils.SOAP_NAMESPACE,
                    "Code");

            Element subCodeElt=WsmanUtils.findChild(codeElt,
                    WsmanUtils.SOAP_NAMESPACE,
                    "SubCode");

            Element valueElt=WsmanUtils.findChild(subCodeElt,
                    WsmanUtils.SOAP_NAMESPACE,
                    "Value");

            subCode = valueElt.getTextContent();

        }
        else if (getCause() !=null ) {
            subCode = getCause().getClass().toString();
        }
            

        return subCode;
    }

 /**
     * Get the reason the fault occured
     *
     *
     *
     * @return
     * The reason the fault occured
     *
     */
    public String getReason() {
        String reason = getMessage();

        if (isFault()) {
            
            Element bodyElt=WsmanUtils.findChild(document.getDocumentElement(),
                    WsmanUtils.SOAP_NAMESPACE,
                    "Body");

            Element faultElt=WsmanUtils.findChild(bodyElt,
                    WsmanUtils.SOAP_NAMESPACE,
                    "Fault");

            Element codeElt=WsmanUtils.findChild(faultElt,
                    WsmanUtils.SOAP_NAMESPACE,
                    "Reason");

            Element textElt=WsmanUtils.findChild(codeElt,
                    WsmanUtils.SOAP_NAMESPACE,
                    "Text");

            reason = textElt.getTextContent();
        }

        return reason;
    }

 /**
     * Get detailed information about the fault
     *
     *
     *
     * @return
     * A description about the fault
     *
     */
    public String getDetail() {
        String detail = "";

        if (isFault()) {

            Element bodyElt=WsmanUtils.findChild(document.getDocumentElement(),
                    WsmanUtils.SOAP_NAMESPACE,
                    "Body");

            Element faultElt=WsmanUtils.findChild(bodyElt,
                    WsmanUtils.SOAP_NAMESPACE,
                    "Fault");

            Element detailElt=WsmanUtils.findChild(faultElt,
                    WsmanUtils.SOAP_NAMESPACE,
                    "Detial");

            if (detailElt!=null) {
                Element faultDetailElt=WsmanUtils.findChild(detailElt,
                        WsmanUtils.WSMAN_NAMESPACE,
                        "FaultDetial");
                if (faultDetailElt!=null)
                    detail = faultDetailElt.getTextContent();
            }
        }
        return detail;
    }

 /**
     * Does the exception represent a WS-Management fault returned by a service.
     *
     *
     *
     * @return
     * <code>true</code> if the exception is a result of a WS-Management fault
     *
     */
    public boolean isFault() {
        return connection!=null && document!=null;
    }


}
