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
//  File:       ManagedInstance.java
//
//  Contents:   Class to create and manipulate an instance of a resource
//
//  Notes:
//
//----------------------------------------------------------------------------

package intel.management.wsman;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.List;
/**
 * Represents an instance of a CIM resource.  Instances are represented according
 * to the XML namespace defined by its ResourceURI. Individual CIM properties
 * can be accessed by <code>getProperty()</code> and <code>setProperty()</code> methods.  Managed instances can be
 * created in memory by using the <code>intel.management.wsman.WsmanConnection.newInstance()</code>
 * method.   Property values can be represented by strings, arrays, CIM references, or
 * embedded instances. Instances of this class are normally obtained from
 * <code>WsmanConnection.newInstance()</code> or from the ManagedReference class.
 *
 *
 * <P>
 *
 * @see #getProperty(java.lang.String)
 * @see #setProperty(java.lang.String,java.lang.String)
 *
 *
 */
public class ManagedInstance {


    /**
     * The XML document representing the instance.
     *
     */
    protected Document document;

     /**
     * The WsmanConnection associated with the instance.
     *
     */
    protected WsmanConnection connection;


     /**
     * Constructs the instance from a XML document.
     *
     * <P>
     * intel.management.wsman.WsmanUtils.LoadDocument() can be used to create a
     * document object from a XML string or stream.
     *
     *
     * @param document The xml representing the instance
     *
     
     *
     */
    public ManagedInstance(Document document) {
        this.document = document;
        this.connection=null;
    }



     /**
     * Constructs the instance from a XML document and a WsmanConnection.
     *
     * <P>
     * <code>intel.management.wsman.WsmanUtils.LoadDocument()</code> can be used to create a
     * document object from a XML string or stream.
     *
     * @param connection The connection to use for any create() operation
     * @param document The xml representing the instance
     *
     *
     *  @see #getProperty(java.lang.String)
     */
    protected ManagedInstance(WsmanConnection connection, Document document) {
        this.document = document;
        this.connection=connection;
    }
    
    private Object getObject(Element elt) {
        
        Object result=null;

        // if the element has no child elements then its a string
        Element foundElt = WsmanUtils.findChild(elt);
  
        if (foundElt==null) {
            result=elt.getTextContent();
        }
        else {

            Element addressElt=WsmanUtils.findChild(elt,
                WsmanUtils.ADDRESSING_NAMESPACE,
                "Address");
            
            Element resElt=WsmanUtils.findChild(elt,
                WsmanUtils.ADDRESSING_NAMESPACE,
                "ReferenceParameters");

            if (addressElt!=null && resElt!=null) {
                result = new ManagedReference(connection,
                        WsmanUtils.createEpr(addressElt, resElt));
            }
            else if (addressElt==null && resElt==null) {

                result = new ManagedInstance(connection,
                        WsmanUtils.loadDocument(elt));
            }
            // the XML would be invalid for any other case the result will be null
        }
        return result;            
    }

     /**
     * Builds a ManagedReference from the instance using specified key properties.
     *
     *
     * @param keyProps The key properties to use for constructing the reference
     *
     * @return
     * A reference to the instance
     *
     */
     public ManagedReference toReference(String[] keyProps) {

        ManagedReference ref = connection.newReference(getResourceURI());

        for (int i=0;i< keyProps.length;i++) {
            Object value = getProperty(keyProps[i]);
            if (value instanceof ManagedReference)
                ref.addSelector(keyProps[i], (ManagedReference)value);
            else if (value instanceof String)
                ref.addSelector(keyProps[i], value.toString());

        }
        return ref;
    }


    /**
     * Get the names of all properties contained in the instance.
     *
     *
     * @return
     * An array of property names contained in the instance.
     *
     */
    public String[] getPropertyNames() {
        List<String> list = new ArrayList<String>();
        String[] r= new String[0];
        NodeList nodes=null;
        if (document!=null)
            nodes = document.getDocumentElement().getChildNodes();
        for (int i=0; nodes!=null && i< nodes.getLength();i++) {
            if (!list.contains(nodes.item(i).getLocalName()))
                list.add(nodes.item(i).getLocalName());
        }

        r =list.toArray(r);
        return r;
    }


    /**
     * Informs the CIM Service to create the instance.
     *
     *
     * @return
     * The reference returned by the CIM service after it created the instance.
     * @throws WsmanException if the service fails to create the instance
     */
    public ManagedReference create() throws WsmanException {

        Document reqDoc = connection.getXmlLoader().loadDocument("CreateXMLDoc.xml");

    
        Element headerElt=WsmanUtils.findChild(reqDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Header");

        Element resElt=WsmanUtils.findChild(headerElt,
                WsmanUtils.WSMAN_NAMESPACE,
                "ResourceURI");

        resElt.setTextContent(getResourceURI());

        Element bodyElt=WsmanUtils.findChild(reqDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Node node = reqDoc.importNode(document.getDocumentElement(), true);
        bodyElt.appendChild(node);

        Document resDoc = connection.SendRequest(reqDoc);

        bodyElt=WsmanUtils.findChild(resDoc.getDocumentElement(),
                WsmanUtils.SOAP_NAMESPACE,
                "Body");

        Element itemElt = WsmanUtils.findChild(bodyElt);

        Document itemDoc = WsmanUtils.loadDocument(itemElt);

        return new ManagedReference(connection,itemDoc);
    }


    /**
     * Get the name of the resource.
     *
     *
     * @return
     * The name of the full resource URI
     *
     */
    public String getResourceURI() {
        return document.getDocumentElement().getNamespaceURI();
    }

    /**
     * Get the name of the resource without a namespace qualifier.
     *
     *
     * @return
     * The name of the resource without the namespace prefix.
     *
     */
    public String getSimpleName() {
        return document.getDocumentElement().getLocalName();
    }


    /**
     * Removes a property from the instance.
     *
     *
     * @param name The name of the property to remove.
     *
     */
    public void removeProperty(String name) {
        Node node = document.getDocumentElement().getFirstChild();
        while (node!=null) {

            if (node.getNodeType()==Node.ELEMENT_NODE &&
                    node.getLocalName().equals(name)) {
                node.getParentNode().removeChild(node);
            }

            node = node.getNextSibling();
        }

    }



    /**
     * Get the value of an array property.
     *
     *
     *@param name The name of the property to get the value of.
     *
     *@return
     *An array of values defined by the property.
     *
     *@see #addProperty(String,String)
     *@see #addProperty(String,ManagedReference)
     *
     */
    public Object[] getPropertyArray(String name) {

        Object[] result= {};
        Object object = getProperty(name);
        if (object!=null) {
            if (object.getClass().isArray()) {
                result = (Object[])object;
            }
            else {
                result = new Object[1];
                result[0]=object;
            }
        }

        return result;
    }


    /**
     * Get the value of a property.
     *
     *
     * @param name The name of the property to get the value of.
     *
     * @return
     * The value of the property or null if the property does not exist.
     */
    public Object getProperty(String name) {

        ArrayList list = new ArrayList();
        Object result=null;

        Node node = document.getDocumentElement().getFirstChild();
        while (node!=null) {
            if (node.getNodeType()==Node.ELEMENT_NODE) {
               if (node.getLocalName().equals(name)) {
                   list.add(getObject((Element)node));
               }
            }
            node=node.getNextSibling();
        }

        if (list.size()==1)
           result = list.get(0);
        else if (list.size()>1)
           result = list.toArray();

        return result;
    }


     /**
     *  Sets the value of a string property.
     *
     *
     * @param name The name of the property to set.
     * @param value The string value of the property.
     *
     */
    public void setProperty(String name, String value) {
        // delete all elemets with the name
        removeProperty(name);
        if(value != null)  addProperty(name,value);

    }

     /**
     *  Set the value of a reference property.
     *
     *
     * @param name The name of the property to set.
     * @param value The reference value of the property.
     *
     */
    public void setProperty(String name, ManagedReference value) {
        removeProperty(name);
        if(value != null)  addProperty(name,value);

    }


     /**
     *  Set the value of an embedded instance property.
     *
     *
     * @param name The name of the property to set.
     * @param value The embedded instance value.
     *
     */
    public void setProperty(String name, ManagedInstance value) {
        removeProperty(name);
        if(value != null)  addProperty(name,value);

    }


    /**
     *  Add a property to the instance.
     *
     *
     * @param name The name of the property to add.
     * @param value The string value to add.
     *
     * @see #setProperty(String,String)
     *
     */
    public Element addProperty(String name, String value) {
        
        Element elt = document.createElementNS(
                document.getDocumentElement().getNamespaceURI(),
                name);

        String p = document.getDocumentElement().getPrefix();
        if (p!=null && p.length()>0)
            elt.setPrefix(p);

        elt.setTextContent(value);
        document.getDocumentElement().appendChild(elt);

        return elt;
    }


    /**
     *  Add a reference property to the instance.
     *
     *
     * @param name The name of the property to add.
     * @param value The reference value to add.
     *
     * @see #setProperty(String,ManagedReference)
     *
     */
    public void addProperty(String name, ManagedReference value) {

        Element elt = document.createElementNS(
                document.getDocumentElement().getNamespaceURI(),
                name);

        String p = document.getDocumentElement().getPrefix();
        if (p!=null && p.length()>0)
            elt.setPrefix(p);

        Element addressElt=WsmanUtils.findChild(value.document.getDocumentElement(),
                WsmanUtils.ADDRESSING_NAMESPACE,
                "Address");

        Element resElt=WsmanUtils.findChild(value.document.getDocumentElement(),
                WsmanUtils.ADDRESSING_NAMESPACE,
                "ReferenceParameters");

      
        if (addressElt.getPrefix().length()>0) {
           String ns=document.getDocumentElement().getAttribute("xmlns:"+
                   addressElt.getPrefix());
           if (ns==null)
               document.getDocumentElement().setAttribute("xmlns:"+
                       addressElt.getPrefix(),
                       WsmanUtils.ADDRESSING_NAMESPACE);
        }

        Node node = document.importNode(addressElt, true);
        elt.appendChild(node);

        node = document.importNode(resElt, true);
        elt.appendChild(node);
       
        document.getDocumentElement().appendChild(elt);

    }

    /**
     *  Add an embedded instance property.
     *
     *
     * @param name The name of the property to add.
     * @param value The embedded instance value.
     *
     */
    public void addProperty(String name, ManagedInstance value) {
        
        Element elt = document.createElementNS(
                document.getDocumentElement().getNamespaceURI(),
                name);

        String p = document.getDocumentElement().getPrefix();
        if (p!=null && p.length()>0)
            elt.setPrefix(p);
        
        Node node = document.importNode(value.document.getDocumentElement(), true);
        elt.appendChild(node);
        document.getDocumentElement().appendChild(elt);
    }


     /**
     * Get the text value of the Instance (ex cim:DateTime instance).
     *
     *
     * @return
     * The text contents of the Instance.
     *
     *
     * @see #setText(java.lang.String)
     *
     */
    public String getText() {
        return document.getDocumentElement().getTextContent();
    }
    
    public Element getBody() {
    	return document.getDocumentElement();
    }

    /**
     * Set the text value of the Instance. Used for simple instances such as
     * cim:datetime.
     *
     
     *
     * @see #getText()
     *
     */
    public void setText(String text) {
        document.getDocumentElement().setTextContent(text);
    }

}
