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
//  File:       WsmanEnumeration.java
//
//  Contents:   Contains the definition of the WsmanEnumerate class
//
//  Notes:
//
//----------------------------------------------------------------------------

package intel.management.wsman;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Iterate through a collection of items.
 *
 * <P>
 *
 * <code>WsmanEnumeration</code> allows the results of an enumeration
 * returned by <code>ManagedReference.enumerate()</code> to be traversed.  If an
 * error occurs during the enumeration, <code>java.util.NoSuchElementException</code>
 * will be thrown.  If <code>java.util.NoSuchElementException</code> is a result
 * of a networking error then hasNext() will be true (meaning enumeration is not
 * complete but an exception occurred before it was completed).
 * If hasNext() returns false, then the enumeration
 * was successfully traversed.

 *
 * 
 * @see #hasNext()
 * @see #next()
 */
public class WsmanEnumeration implements java.util.Iterator {


    /**
     * The WsmanConnection  associated with the enumeration.
     *
     */
    protected WsmanConnection connection;

    
    /**
     * The enumeration context returned by a CIM service.
     *
     */
    protected String context;
    /**
     * The response items returned by a CIM service.
     *
     */
    protected Document response;

    /**
     * The endpoint reference used in the enumeration.
     *
     */
    protected Document epr;


     /**
     * Constructs the enumerator from a XML document and a WsmanConnection.
     *
     * <P>
     * <code>intel.management.wsman.WsmanUtils.LoadDocument()</code> can be used to create a
     * document object from a XML string or stream.
     * 
     * @param connection The connection to use for all Wsman operations
     * @param epr An endpoint reference to enumerate
     * @param context The enumeration context returned by a service
     *
     *
     * 
     */
    protected WsmanEnumeration(WsmanConnection connection,
            Document epr,
            String context) {
        this.connection=connection;
        this.context=context;
        this.epr=epr;
        response=null;
    }

    private Element peek() throws WsmanException {

        if (response==null)
            fetch();


        Element bodyElt=WsmanUtils.findChild(response.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");


        Element enumElt=WsmanUtils.findChild(bodyElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "PullResponse");

        Element ctxElt=WsmanUtils.findChild(enumElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "EnumerationContext");

        if (ctxElt!=null)
            context=ctxElt.getTextContent();


        Element itemsElt=WsmanUtils.findChild(enumElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "Items");

        // ITEMS
        Element item=WsmanUtils.findChild(itemsElt);


        // no item found
        if (item==null) {

            Element endElt=WsmanUtils.findChild(enumElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "EndOfSequence");
            if (endElt==null) {
                response=null;
                item=peek();
            }
        }

        return item;

    }

    private void fetch() throws WsmanException {

        Document reqDoc =
                connection.getXmlLoader().loadDocument("PullXMLDoc.xml");
        
        WsmanUtils.setEpr(epr, reqDoc);

        Element bodyElt=WsmanUtils.findChild(reqDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Element enumElt=WsmanUtils.findChild(bodyElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "Pull");

        Element ctxElt=WsmanUtils.findChild(enumElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "EnumerationContext");
        
        ctxElt.setTextContent(context);

        response = connection.SendRequest(reqDoc);
    }

    /**
     *
     * Returns <code>true</code> if the enumeration has not been fully traversed (meaning potentially more items)
     *
     *
     * @return
     * <code>true</code> if there are more items in the enumerator
     *
     */
    public boolean hasNext()  {

        boolean r=true; // assume more items
        try {
            r=  peek()!=null; // if peek is null then no more items
        }
        catch (WsmanException exception) {
            r=true;
            // result will be true because the enumeration is not complete.
            // next() should throw java.util.NoSuchElementException in this has
            // if hasNext() is true but calling next() results in a
            //java.util.NoSuchElementException then the item was not able
            // to be fetched and the enumeration remains incomplete
        }
        return r;

    }

     /**
     *
     * Returns the next item in the enumerator
     *
     *
     * @return
     * The next element in the enumerator
     *
     *
     *@throws java.util.NoSuchElementException when retrieving the next item fails
      *
      *  @see #hasNext()
     */
    public Object next() throws java.util.NoSuchElementException {

        Object result=null;
        Element elt =null;
        try {
            elt = peek();
            if (elt==null)
                throw new java.util.NoSuchElementException();
        } catch (WsmanException exp) {
            java.util.NoSuchElementException e=
                   new java.util.NoSuchElementException();
            e.initCause(exp);
            throw e;
        }


        String ns=elt.getNamespaceURI();
        String nodeName = elt.getLocalName();
        
        // check firmware bug of no namespace
        if (ns==null && nodeName.equals("EndpointReference"))
            ns=WsmanUtils.ADDRESSING_NAMESPACE;

        // enumerating eprs
        if (ns.equals(WsmanUtils.ADDRESSING_NAMESPACE)) {
          result = new ManagedReference(connection,WsmanUtils.loadDocument(elt));
        }
        else if (ns.equals(WsmanUtils.WSMAN_NAMESPACE) && nodeName.equals("Item"))
        {
            // Enumerating objects and EPRS
            ManagedReference ref=null;
            ManagedInstance inst=null;
            Node node = elt.getFirstChild();
            while (node!=null) {
                String nse = node.getNamespaceURI();

                //check firmware bug of no Namespace
                if (nse==null && node.getLocalName().equals("EndpointReference"))
                    nse=WsmanUtils.ADDRESSING_NAMESPACE;

                if (nse.equals(WsmanUtils.ADDRESSING_NAMESPACE))
                    ref = new ManagedReference(connection,
                            WsmanUtils.loadDocument((Element)node));
                else if (node.getNodeType()==Node.ELEMENT_NODE)
                    inst = new ManagedInstance(connection,
                        WsmanUtils.loadDocument((Element)node));

                node = node.getNextSibling();

                // check if we found both EPR and Object
                if (ref!=null && inst !=null) {
                    result = new WsmanItem(inst,ref);
                    break;
                }

            }
        }
        else
        {
            // enumerating instances only
            result = new ManagedInstance(connection,
                    WsmanUtils.loadDocument(elt));
        }

        elt.getParentNode().removeChild(elt);

        return result;
    }

    /**
     *
     * Not supported.  Optional per Java specification.
     *
     *
     *
     *
     *@throws UnsupportedOperationException
     */
    public void remove() throws  UnsupportedOperationException  {
        throw new UnsupportedOperationException();
    }

     /**
     *
     * Informs the CIM service to release the enumerator
     *
     *
     *
     *@throws WsmanException when releasing fails
     */
    public void release() throws WsmanException {


        Document reqDoc =
                connection.getXmlLoader().loadDocument("ReleaseXMLDoc.xml");

        Element bodyElt=WsmanUtils.findChild(reqDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Element enumElt=WsmanUtils.findChild(bodyElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "Release");

        Element ctxElt=WsmanUtils.findChild(enumElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "EnumerationContext");

        ctxElt.setTextContent(context);
        connection.SendRequest(reqDoc);

    }
       


}
