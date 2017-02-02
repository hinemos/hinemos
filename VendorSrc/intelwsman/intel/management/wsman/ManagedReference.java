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
//  File:       ManagedReference.java
//
//  Contents:   Contains the definition of the ManagedReference class
//
//  Notes:
//
//----------------------------------------------------------------------------

package intel.management.wsman;


import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a reference to a CIM resource. When retrieving and updating an
 * instance of a class, the WS-Management operations get ,put, create, and
 * delete are used.
 *
 * <P>
 * Note: If a resource has more than one instance,
 * one or more selectors may be used to distinguish which instance is targeted.
 * Selectors can be added to the reference as required to identify
 * the precise instance of the resource class.   The use of selectors
 * may be optional if only one instance of the resource exists.
 *
 *
 * @see #addSelector(java.lang.String, java.lang.String)
 * @see #addSelector(String,ManagedReference)
 *
 *
 */
public class ManagedReference {

    /**
     * The XML document representing the instance
     *
     */
    protected Document document;

     /**
     * The WsmanConnection associated with the reference
     *
     */
    protected WsmanConnection connection;

 

    /**
     * Constructs the reference from a XML document and a WsmanConnection.
     *
     * <P>
     * <code>intel.management.wsman.WsmanUtils.LoadDocument()</code> can be used to create a
     * document object from a XML string or stream.
     *
     * @param connection The connection to use for all WSMan operations
     * @param document The XML representing the reference
     *
     *
     *  
     */
    public ManagedReference(WsmanConnection connection,
            Document document) {

        this.connection=connection;
        this.document=document;
    }



    private void clearSelectors() {

        Element refElt=WsmanUtils.findChild(document.getDocumentElement(),
                WsmanUtils.ADDRESSING_NAMESPACE,
                "ReferenceParameters");

        Element resElt=WsmanUtils.findChild(refElt,
                WsmanUtils.WSMAN_NAMESPACE,
                "ResourceURI");

        Element selectorsElt=WsmanUtils.findChild(refElt,
                WsmanUtils.WSMAN_NAMESPACE,
                "SelectorSet");

        if (selectorsElt!=null)
            WsmanUtils.removeChildren(selectorsElt);

         Element selElt=document.createElementNS(WsmanUtils.WSMAN_NAMESPACE,
                      "SelectorSet");

         selElt.setPrefix(resElt.getPrefix());


         document.getDocumentElement().appendChild(selElt);

    }


    /**
     * Set the name of the resource or instance to access. The ResourceURI
     * is identical to the XML namespace URI of the schema for the class
     * being referenced.
     *
     *
     * @param resourceURI The URI of the resource class
     * or instance to be accessed

     *
     */
    public void setResourceURI(String resourceURI) {

        resourceURI = WsmanUtils.getFullResourceURI(resourceURI);
        Element refElt=WsmanUtils.findChild(document.getDocumentElement(),
                WsmanUtils.ADDRESSING_NAMESPACE,
                "ReferenceParameters");

        Element resElt=WsmanUtils.findChild(refElt,
                WsmanUtils.WSMAN_NAMESPACE,
                "ResourceURI");

        resElt.setTextContent(resourceURI);

        clearSelectors();
    }

    public void setResourceURIForCreate(String resourceURI) {
    	resourceURI = WsmanUtils.getFullResourceURI(resourceURI);
    	
        Element headerElt=WsmanUtils.findChild(document.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Header");

        Element resElt=WsmanUtils.findChild(headerElt,
                WsmanUtils.WSMAN_NAMESPACE,
                "ResourceURI");

        resElt.setTextContent(resourceURI);
    }

     /**
     * Get the name of the resource or instance to access. The ResourceURI
     * is identical to the XML namespace URI of the schema for the class
     * being reference.
     *
     *

     * @return
     * The full resource name
     */
    public String getResourceURI() {

        Element refElt=WsmanUtils.findChild(document.getDocumentElement(),
                WsmanUtils.ADDRESSING_NAMESPACE,
                "ReferenceParameters");

        Element resElt=WsmanUtils.findChild(refElt,
                WsmanUtils.WSMAN_NAMESPACE,
                "ResourceURI");

        return resElt.getTextContent();
    }


    /**
     * Add a selector to the reference
     *
     *
     * @param name The resource-relative name of the selector
     * @param value The value of the selector

     *
     */
    public void addSelector(String name, String value) {


        Element refElt=WsmanUtils.findChild(document.getDocumentElement(), 
                WsmanUtils.ADDRESSING_NAMESPACE,
                "ReferenceParameters");

        Element resElt=WsmanUtils.findChild(refElt,
                WsmanUtils.WSMAN_NAMESPACE,
                "ResourceURI");
        
        Element selectorsElt=WsmanUtils.findChild(refElt, 
                WsmanUtils.WSMAN_NAMESPACE,
                "SelectorSet");

        Element elt=document.createElementNS(selectorsElt.getNamespaceURI(),
                      "Selector");

        elt.setPrefix(resElt.getPrefix());
        
        elt.setAttribute("Name", name);

        elt.setTextContent(value.toString());

        selectorsElt.appendChild(elt);
    }


    /**
     * Add a selector to the reference
     *
     *
     * @param name The resource-relative name of the selector
     * @param value The reference value of the selector

     *
     */
    public void addSelector(String name, ManagedReference value) {

        Element refElt=WsmanUtils.findChild(document.getDocumentElement(),
                WsmanUtils.ADDRESSING_NAMESPACE,
                "ReferenceParameters");

        Element resElt=WsmanUtils.findChild(refElt,
                WsmanUtils.WSMAN_NAMESPACE,
                "ResourceURI");

        Element selectorsElt=WsmanUtils.findChild(refElt,
                WsmanUtils.WSMAN_NAMESPACE,
                "SelectorSet");

        Element selElt=document.createElementNS(selectorsElt.getNamespaceURI(),
                      "Selector");

        Element elt=document.createElementNS(WsmanUtils.ADDRESSING_NAMESPACE,
                      "EndpointReference");

        elt.setPrefix(refElt.getPrefix());

         refElt=WsmanUtils.findChild(value.document.getDocumentElement(),
                WsmanUtils.ADDRESSING_NAMESPACE,
                "ReferenceParameters");
         Element addrElt=WsmanUtils.findChild(value.document.getDocumentElement(),
                WsmanUtils.ADDRESSING_NAMESPACE,
                "Address");

        selElt.setPrefix(resElt.getPrefix());

        selElt.setAttribute("Name", name);
        selElt.appendChild(elt);

        Node node = document.importNode(addrElt, true);
        elt.appendChild(node);

        node = document.importNode(refElt, true);
        elt.appendChild(node);
        
        selectorsElt.appendChild(selElt);
    }


     /**
     * Get the representation of the instance
     *
     * @return
     * A <code>ManagedInstance</code> containing the representation
     * of the instance
     *
     * @throws WsmanException when retrieving the resource fails
     *
     * @see #enumerate()
     * @see #put(ManagedInstance)
     * @see #delete()
     * @see #invoke(ManagedInstance)
     */
    public ManagedInstance get() throws WsmanException {

        WsmanUtils util = new WsmanUtils();
        Document reqDoc = util.loadDocument("GetXMLDoc.xml");

        WsmanUtils.setEpr(document, reqDoc);
       
        Document resDoc = connection.SendRequest(reqDoc);
        
        Element bodyElt=WsmanUtils.findChild(resDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Element itemElt = WsmanUtils.findChild(bodyElt);

        Document itemDoc = WsmanUtils.loadDocument(itemElt);

        return new ManagedInstance(connection,itemDoc);
    }


    /**
     * Changes the properties of the resource
     *
     *
     * @param instance A <code>ManagedInstance</code> containing the
     * new properties for the resource
     *
     *
     * @return
     * A <code>ManagedInstance</code> containing the updated resource
     *
     * @throws WsmanException when updating the resource fails
     *
     * @see #get()
     * @see #delete()
     *
     */
    public ManagedInstance put(ManagedInstance instance) throws WsmanException {

        WsmanUtils util = new WsmanUtils();
        Document reqDoc = util.loadDocument("PutXMLDoc.xml");

        WsmanUtils.setEpr(document, reqDoc);

        Element bodyElt=WsmanUtils.findChild(reqDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Node node = reqDoc.importNode(instance.document.getDocumentElement(), true);
        bodyElt.appendChild(node);

        Document resDoc = connection.SendRequest(reqDoc);

        bodyElt=WsmanUtils.findChild(resDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");


        Element itemElt = WsmanUtils.findChild(bodyElt);

        Document itemDoc=null;
        if (itemElt!=null) {
            itemDoc = WsmanUtils.loadDocument(itemElt);
            return new ManagedInstance(connection,itemDoc);
        }

        return instance;

    }

    /**
     *
     * Delete the instance
     *
     * @throws WsmanException when deleting the resource fails
     *
     *
     */
    public void delete() throws WsmanException  {

        WsmanUtils util = new WsmanUtils();
        Document reqDoc = util.loadDocument("DeleteXMLDoc.xml");

        WsmanUtils.setEpr(document, reqDoc);

        connection.SendRequest(reqDoc);

    }


     /**
     *
     * Enumerate the instances of a resource with an option filter and mode
     *
     * <P>
     * Possible Enumeration modes are:
     * <P>
     * <code>intel.management.wsman.WsmanUtils.ENUMERATE_EPR</code><BR>
     * <code>intel.management.wsman.WsmanUtils.ENUMERATE_OBJECT_AND_EPR</code><BR>
     *
     *
     * @param filter A <code>ManagedInstance</code> representing the filter properties
     * @param mode The enumeration mode (e.g. EnumerateEPR)
     *
     *
     * @return
     * A <code>WsmanEnumeration</code> object that can be used to retrieve
     * each instance
     *
     * @throws WsmanException when creating the enumerator fails
     *
     * @see #get()
     *
     */
    public WsmanEnumeration enumerate(ManagedInstance filter,String mode) throws WsmanException {
    
        WsmanUtils util = new WsmanUtils();
        Document reqDoc = util.loadDocument("EnumXMLDoc.xml");
        
        WsmanUtils.setEpr(document, reqDoc);

        Element headerElt=WsmanUtils.findChild(reqDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Header");

        Element resElt=WsmanUtils.findChild(headerElt,
                WsmanUtils.WSMAN_NAMESPACE,
                "ResourceURI");

       

        Element bodyElt=WsmanUtils.findChild(reqDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Element enumElt=WsmanUtils.findChild(bodyElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "Enumerate");


        if (filter != null) {
             Element filterElt = reqDoc.createElementNS(
                WsmanUtils.WSMAN_NAMESPACE,
                "Filter");

            filterElt.setPrefix(resElt.getPrefix());
            Node importNode=null;

            //check the filter type
            if (filter.getResourceURI().equals(WsmanUtils.SELECTOR_DIALECT))
            {
                // this is a selector dialict
                filterElt.setAttribute("Dialect",WsmanUtils.SELECTOR_DIALECT);
                
                ManagedReference ref = filter.toReference(filter.getPropertyNames());

                String t = WsmanUtils.getXML(filter);
                String tt = WsmanUtils.getXML(ref);
                
                 Element refElt=WsmanUtils.findChild(ref.document.getDocumentElement(),
                            WsmanUtils.ADDRESSING_NAMESPACE,"ReferenceParameters");
                 
                 Element setElt=WsmanUtils.findChild(refElt,
                            WsmanUtils.WSMAN_NAMESPACE,"SelectorSet");
                
                //get the selector set only
                importNode = reqDoc.importNode(setElt, true);
                
            }
            else 
            {
                //This must be an Association Dialect
                filterElt.setAttribute("Dialect",WsmanUtils.ASSOCIATION_DIALECT);
                importNode = reqDoc.importNode(
                    filter.document.getDocumentElement(), true);
            }
        
            filterElt.appendChild(importNode);

            enumElt.appendChild(filterElt);
        }

        if (mode!=null) {
              Element modeElt = reqDoc.createElementNS(
                WsmanUtils.WSMAN_NAMESPACE,
                "EnumerationMode");

              modeElt.setPrefix(resElt.getPrefix());

              modeElt.setTextContent(mode);

              enumElt.appendChild(modeElt);

        }


        Document resDoc = connection.SendRequest(reqDoc);

               
        bodyElt=WsmanUtils.findChild(resDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        enumElt=WsmanUtils.findChild(bodyElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "EnumerateResponse");

        if (enumElt==null)
            throw new WsmanException(connection,resDoc);

         Element ctxElt=WsmanUtils.findChild(enumElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "EnumerationContext");

         return new WsmanEnumeration(connection,
                 (Document)document.cloneNode(true),
                 ctxElt.getTextContent());

    }
    /**
    *
    * Enumerate the instances of a resource with WQL
    *
    * @param query
    *
    * @return
    * A <code>WsmanEnumeration</code> object that can be used to retrieve
    * each instance
    *
    * @throws WsmanException when creating the enumerator fails
    *
    * @see #get()
    *
    */
    public WsmanEnumeration enumerate(String query) throws WsmanException {

        WsmanUtils util = new WsmanUtils();
        Document reqDoc = util.loadDocument("EnumXMLDoc.xml");
        
        WsmanUtils.setEpr(document, reqDoc);

        Element headerElt=WsmanUtils.findChild(reqDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Header");

        Element resElt=WsmanUtils.findChild(headerElt,
                WsmanUtils.WSMAN_NAMESPACE,
                "ResourceURI");

       

        Element bodyElt=WsmanUtils.findChild(reqDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Element enumElt=WsmanUtils.findChild(bodyElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "Enumerate");
        
        if (query != null && query.length() != 0) {
            Element filterElt = reqDoc.createElementNS(
                    WsmanUtils.WSMAN_NAMESPACE, "Filter");

            filterElt.setPrefix(resElt.getPrefix());
            
            filterElt.setAttribute("Dialect",WsmanUtils.WQL_DIALECT);
            
            filterElt.setTextContent(query);
            
            enumElt.appendChild(filterElt);
        }
        
        Document resDoc = connection.SendRequest(reqDoc);

        
        bodyElt=WsmanUtils.findChild(resDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        enumElt=WsmanUtils.findChild(bodyElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "EnumerateResponse");

        if (enumElt==null)
            throw new WsmanException(connection,resDoc);

         Element ctxElt=WsmanUtils.findChild(enumElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "EnumerationContext");

         return new WsmanEnumeration(connection,
                 (Document)document.cloneNode(true),
                 ctxElt.getTextContent());
    
    }



    /**
     *
     * Enumerate the instances of a resource
     *

     * @return
     * A <code>WsmanEnumeration</code> object that can be used to retrieve
     * each instance
     *
     * @throws WsmanException when creating the enumerator fails
     *
     * @see #get()
     *
     */
    public WsmanEnumeration enumerate() throws WsmanException {

        WsmanUtils util = new WsmanUtils();
        Document reqDoc = util.loadDocument("EnumXMLDoc.xml");

        WsmanUtils.setEpr(document, reqDoc);

        Document resDoc = connection.SendRequest(reqDoc);

        Element bodyElt=WsmanUtils.findChild(resDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Element enumElt=WsmanUtils.findChild(bodyElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "EnumerateResponse");

         Element ctxElt=WsmanUtils.findChild(enumElt,
                WsmanUtils.ENUMERATION_NAMESPACE,
                "EnumerationContext");

         return new WsmanEnumeration(connection,
                 (Document)document.cloneNode(true),
                 ctxElt.getTextContent());

    }


    /**
     * Create an input parameter object for a resource-specific method.
     * Input parameters can be added to a resulting instance and sent using
     * the <code>Invoke()</code> method.
     * <P>
     *
     *
     * @param methodName Resource-specific method name

     * @return
     * New instance of a <code>ManagedInstance</code> that can be used to add
     * input parameters
     *
     *
     * @see #invoke(ManagedInstance)
     *
     */
    public ManagedInstance createMethodInput(String methodName ) {

        Document input = document.getImplementation().createDocument(
                getResourceURI(),
                methodName+"_INPUT", null);

        input.getDocumentElement().setPrefix("p");

        return new ManagedInstance(connection,input);
    }

    public ManagedInstance createMethod(String namespace, String methodName ) {
        Document node = document.getImplementation().createDocument(
                namespace,
                methodName, null);

        node.getDocumentElement().setPrefix("p");

        return new ManagedInstance(connection, node);
    }

     /**
     * Allows an application to express interest in receiving events from a resource.
     *
     * <P>
     *
     *
     * @param deliveryMode Delivery Mode URI
     * @param deliverTo The remote address to deliver the event to

     * @return
     * A <code>ManagedReference</code> to a subscription service
     * input parameters
     *
     * @throws WsmanException when subscription fails
     * @see #Unsubscribe()
     * @see intel.management.wsman.WsmanUtils
     *
     */
    public ManagedReference Subscribe(String deliveryMode, String deliverTo ) throws WsmanException {

        WsmanUtils util = new WsmanUtils();
        Document input =util.loadDocument("SubscribeXMLDoc.xml");

        WsmanUtils.setEpr(document, input);

        Element headerElt=WsmanUtils.findChild(input.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Header");

        Element aElt=WsmanUtils.findChild(headerElt,
                WsmanUtils.ADDRESSING_NAMESPACE,
                "To");



        Element bodyElt=WsmanUtils.findChild(input.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Element subElt=WsmanUtils.findChild(bodyElt,
                WsmanUtils.EVENTING_NAMESPACE,
                "Subscribe");


        Element delElt = input.createElementNS(
                WsmanUtils.EVENTING_NAMESPACE,
                "Delivery");

        delElt.setPrefix(subElt.getPrefix());

        delElt.setAttribute("Mode", deliveryMode);

        Element toElt = input.createElementNS(
                WsmanUtils.EVENTING_NAMESPACE,
                "NotifyTo");

        toElt.setPrefix(subElt.getPrefix());

        Element addrElt = input.createElementNS(
                WsmanUtils.ADDRESSING_NAMESPACE,
                "Address");

        addrElt.setPrefix(aElt.getPrefix());

        addrElt.setTextContent(deliverTo);

        toElt.appendChild(addrElt);
        delElt.appendChild(toElt);
        subElt.appendChild(delElt);

        Element expElt = input.createElementNS(
                WsmanUtils.EVENTING_NAMESPACE,
                "Expires");

        expElt.setPrefix(subElt.getPrefix());

        expElt.setTextContent("PT0.000000S");

        subElt.appendChild(expElt);

        // print headers
        if (System.getProperty("intel.management.wsman.debug","false").equals("true"))
            {
            System.out.println();
            System.out.println(util.formatDocument(input));
            System.out.println();
            }
      
        Document out = connection.SendRequest(input);

       
        ManagedReference service =  new ManagedReference(connection,
                  util.loadDocument("EprXMLDoc.xml"));

        bodyElt=WsmanUtils.findChild(out.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Element resElt=WsmanUtils.findChild(bodyElt,
                WsmanUtils.EVENTING_NAMESPACE,
                "SubscribeResponse");

        Element serElt=WsmanUtils.findChild(resElt,
                WsmanUtils.EVENTING_NAMESPACE,
                "SubscriptionManager");

            
        WsmanUtils.setEpr(serElt, service.document);

      
       return service;
    }

    /**
     * Allows a client to cancel a subscription
     *
     * <P>
     * @see #Subscribe(String,String)
     *
     * @throws WsmanException when unsubscribing fails
     *
     */
    public void Unsubscribe() throws WsmanException {

        WsmanUtils util = new WsmanUtils();
        Document input = util.loadDocument("UnsubscribeXMLDoc.xml");

        WsmanUtils.setEpr(document, input);

        connection.SendRequest(input);

    }

    
    public ManagedInstance create(ManagedInstance input) throws WsmanException {

        WsmanUtils util = new WsmanUtils();
        Document reqDoc = util.loadDocument("CreateXMLDoc.xml");

        WsmanUtils.setEpr(document, reqDoc);

        Element bodyElt=WsmanUtils.findChild(reqDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Node node = reqDoc.importNode(input.document.getDocumentElement(), true);
        bodyElt.appendChild(node);

        Document resDoc = connection.SendRequest(reqDoc);

        bodyElt=WsmanUtils.findChild(resDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Document itemDoc = WsmanUtils.loadDocument(bodyElt);

        return new ManagedInstance(connection,itemDoc);
    }



     /**
     * Invokes a method on a resource.
     *
     *
     * @param input A <code>ManagedInstance</code> containing the input
     * parameters of the method

     * @return
     * A <code>ManagedInstance</code> object containing the output parameters
     * returned from the invoke
     *
     * @throws WsmanException when invoking the method fails
     *
     * @see #createMethodInput(String)
     *
     */
    public ManagedInstance invoke(ManagedInstance input) throws WsmanException {

        WsmanUtils util = new WsmanUtils();
        Document reqDoc = util.loadDocument("PutXMLDoc.xml");

        WsmanUtils.setEpr(document, reqDoc);

        Element headerElt=WsmanUtils.findChild(reqDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Header");

        Element actionElt=WsmanUtils.findChild(headerElt,
                WsmanUtils.ADDRESSING_NAMESPACE,
                "Action");

        StringBuffer buffer = new StringBuffer();
        buffer.append(input.document.getDocumentElement().getNamespaceURI());
        buffer.append("/");
        buffer.append(input.document.getDocumentElement().getLocalName().substring(0,
                input.document.getDocumentElement().getLocalName().length()-6));

        actionElt.setTextContent(buffer.toString());

        Element bodyElt=WsmanUtils.findChild(reqDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Node node = reqDoc.importNode(input.document.getDocumentElement(), true);
        bodyElt.appendChild(node);

        Document resDoc = connection.SendRequest(reqDoc);

        bodyElt=WsmanUtils.findChild(resDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Element itemElt = WsmanUtils.findChild(bodyElt);

        Document itemDoc = WsmanUtils.loadDocument(itemElt);

        return new ManagedInstance(connection,itemDoc);
    }

    public ManagedInstance invoke(ManagedInstance input, String actionName) throws WsmanException {

        WsmanUtils util = new WsmanUtils();
        Document reqDoc = util.loadDocument("PutXMLDoc.xml");

        WsmanUtils.setEpr(document, reqDoc);

        Element headerElt=WsmanUtils.findChild(reqDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Header");

        Element actionElt=WsmanUtils.findChild(headerElt,
                WsmanUtils.ADDRESSING_NAMESPACE,
                "Action");

        actionElt.setTextContent(actionName);

        Element bodyElt=WsmanUtils.findChild(reqDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Node node = reqDoc.importNode(input.document.getDocumentElement(), true);
        bodyElt.appendChild(node);

        Document resDoc = connection.SendRequest(reqDoc);

        bodyElt=WsmanUtils.findChild(resDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Element itemElt = WsmanUtils.findChild(bodyElt);

        Document itemDoc = WsmanUtils.loadDocument(itemElt);

        return new ManagedInstance(connection,itemDoc);
    }


    /**
     * Invokes a method on a resource.
     *
     *
     * @param methodName resource-specific method name

     * @return
     * A <code>ManagedInstance</code> object containing the output parameters
     * returned from the invoke
     *
     * @throws WsmanException when invoking the method fails
     *
     * @see #invoke(ManagedInstance)
     *
     */
    public ManagedInstance invoke(String methodName) throws WsmanException {
        return invoke(this.createMethodInput(methodName));
    }


     /**
     * Get all of the selector names associated with the reference.
     *
     *
     * @return
     * A String array containing all the selector names
     *
     *
     * @see #getSelectorValue(String)
     *
     */
    public String[] getSelectorNames() {

        List<String> list = new ArrayList<String>();
        String[] r= new String[0];
        NodeList nodes=null;

        Element refElt=WsmanUtils.findChild(document.getDocumentElement(),
                WsmanUtils.ADDRESSING_NAMESPACE,
                "ReferenceParameters");


        Element selectorsElt=WsmanUtils.findChild(refElt,
                WsmanUtils.WSMAN_NAMESPACE,
                "SelectorSet");

        if (selectorsElt!=null)
            nodes  = selectorsElt.getChildNodes();

        for (int i=0; nodes!=null && i< nodes.getLength();i++) {
            Node node = nodes.item(i);
            if (node.getNodeType()==Node.ELEMENT_NODE &&
                    node.getLocalName().equals("Selector") &&
                    node.getNamespaceURI().endsWith(WsmanUtils.WSMAN_NAMESPACE))
                list.add(((Element)node).getAttribute("Name"));
        }

        r =list.toArray(r);
        return r;
    }

     /**
     * Get the value of the given selector name
     *
     *
     * @return
     * An object containing the value of the selector
     *
     *
     * @see #getSelectorNames()
     *
     */
    public Object getSelectorValue(String name) {

        NodeList nodes=null;
        Object result=null;

        Element refElt=WsmanUtils.findChild(document.getDocumentElement(),
                WsmanUtils.ADDRESSING_NAMESPACE,
                "ReferenceParameters");


        Element selectorsElt=WsmanUtils.findChild(refElt,
                WsmanUtils.WSMAN_NAMESPACE,
                "SelectorSet");

        if (selectorsElt!=null)
            nodes  = selectorsElt.getChildNodes();
        for (int i=0; nodes!=null && i< nodes.getLength();i++) {
            Node node = nodes.item(i);
            if (node.getNodeType()==Node.ELEMENT_NODE &&
                    node.getLocalName().equals("Selector") &&
                    node.getNamespaceURI().endsWith(WsmanUtils.WSMAN_NAMESPACE) &&
                    ((Element)node).hasAttribute("Name") &&
                    ((Element)node).getAttribute("Name").equals(name))

            {
                Element eRefElt=WsmanUtils.findChild((Element)node,
                            WsmanUtils.ADDRESSING_NAMESPACE,
                            "ReferenceParameters");

                Element eAddrElt=WsmanUtils.findChild((Element)node,
                            WsmanUtils.ADDRESSING_NAMESPACE,
                            "Address");

                if (eRefElt!=null && eAddrElt !=null) {
                     result = new ManagedReference(connection,
                        WsmanUtils.createEpr(eAddrElt, eRefElt));
                }
                else
                    result = node.getTextContent();
            }

        }
        return result;
    }




}
