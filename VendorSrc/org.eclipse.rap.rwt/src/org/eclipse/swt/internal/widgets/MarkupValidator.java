/*******************************************************************************
 * Copyright (c) 2012, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.swt.widgets.Widget;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;


public class MarkupValidator {

  // Used by Eclipse Scout project
  public static final String MARKUP_VALIDATION_DISABLED
    = "org.eclipse.rap.rwt.markupValidationDisabled";

  private static final String DTD = createDTD();
  private static final Map<String, String[]> SUPPORTED_ELEMENTS = createSupportedElementsMap();
  private final SAXParser saxParser;

  public static MarkupValidator getInstance() {
    return SingletonUtil.getSessionInstance( MarkupValidator.class );
  }

  public MarkupValidator() {
    saxParser = createSAXParser();
  }

  public void validate( String text ) {
    StringBuilder markup = new StringBuilder();
    markup.append( DTD );
    markup.append( "<html>" );
    markup.append( text );
    markup.append( "</html>" );
    InputSource inputSource = new InputSource( new StringReader( markup.toString() ) );
    try {
      saxParser.parse( inputSource, new MarkupHandler() );
    } catch( RuntimeException exception ) {
      throw exception;
    } catch( Exception exception ) {
      throw new IllegalArgumentException( "Failed to parse markup text", exception );
    }
  }

  public static boolean isValidationDisabledFor( Widget widget ) {
    return Boolean.TRUE.equals( widget.getData( MARKUP_VALIDATION_DISABLED ) );
  }

  private static SAXParser createSAXParser() {
    SAXParser result = null;
    SAXParserFactory parserFactory = SAXParserFactory.newInstance();
    try {
      result = parserFactory.newSAXParser();
    } catch( Exception exception ) {
      throw new RuntimeException( "Failed to create SAX parser", exception );
    }
    return result;
  }

  private static String createDTD() {
    StringBuilder result = new StringBuilder();
    result.append( "<!DOCTYPE html [" );
    result.append( "<!ENTITY quot \"&#34;\">" );
    result.append( "<!ENTITY amp \"&#38;\">" );
    result.append( "<!ENTITY apos \"&#39;\">" );
    result.append( "<!ENTITY lt \"&#60;\">" );
    result.append( "<!ENTITY gt \"&#62;\">" );
    result.append( "<!ENTITY nbsp \"&#160;\">" );
    result.append( "<!ENTITY ensp \"&#8194;\">" );
    result.append( "<!ENTITY emsp \"&#8195;\">" );
    result.append( "<!ENTITY ndash \"&#8211;\">" );
    result.append( "<!ENTITY mdash \"&#8212;\">" );
    result.append( "]>" );
    return result.toString();
  }

  private static Map<String, String[]> createSupportedElementsMap() {
    Map<String, String[]> result = new HashMap<String, String[]>();
    result.put( "html", new String[ 0 ] );
    result.put( "br", new String[ 0 ] );
    result.put( "b", new String[] { "style" } );
    result.put( "strong", new String[] { "style" } );
    result.put( "i", new String[] { "style" } );
    result.put( "em", new String[] { "style" } );
    result.put( "sub", new String[] { "style" } );
    result.put( "sup", new String[] { "style" } );
    result.put( "big", new String[] { "style" } );
    result.put( "small", new String[] { "style" } );
    result.put( "del", new String[] { "style" } );
    result.put( "ins", new String[] { "style" } );
    result.put( "code", new String[] { "style" } );
    result.put( "samp", new String[] { "style" } );
    result.put( "kbd", new String[] { "style" } );
    result.put( "var", new String[] { "style" } );
    result.put( "cite", new String[] { "style" } );
    result.put( "dfn", new String[] { "style" } );
    result.put( "q", new String[] { "style" } );
    result.put( "abbr", new String[] { "style", "title" } );
    result.put( "span", new String[] { "style" } );
    result.put( "img", new String[] { "style", "src", "width", "height", "title", "alt" } );
    result.put( "a", new String[] { "style", "href", "target", "title" } );
    return result;
  }

  private static class MarkupHandler extends DefaultHandler {

    @Override
    public void startElement( String uri, String localName, String name, Attributes attributes ) {
      checkSupportedElements( name, attributes );
      checkSupportedAttributes( name, attributes );
      checkMandatoryAttributes( name, attributes );
    }

    private static void checkSupportedElements( String elementName, Attributes attributes ) {
      if( !SUPPORTED_ELEMENTS.containsKey( elementName ) ) {
        throw new IllegalArgumentException( "Unsupported element in markup text: " + elementName );
      }
    }

    private static void checkSupportedAttributes( String elementName, Attributes attributes ) {
      if( attributes.getLength() > 0 ) {
        List<String> supportedAttributes = Arrays.asList( SUPPORTED_ELEMENTS.get( elementName ) );
        int index = 0;
        String attributeName = attributes.getQName( index );
        while( attributeName != null ) {
          if( !supportedAttributes.contains( attributeName ) ) {
            String message = "Unsupported attribute \"{0}\" for element \"{1}\" in markup text";
            message = MessageFormat.format( message, new Object[] { attributeName, elementName } );
            throw new IllegalArgumentException( message );
          }
          index++;
          attributeName = attributes.getQName( index );
        }
      }
    }

    private static void checkMandatoryAttributes( String elementName, Attributes attributes ) {
      checkIntAttribute( elementName, attributes, "img", "width" );
      checkIntAttribute( elementName, attributes, "img", "height" );
    }

    private static void checkIntAttribute( String elementName,
                                           Attributes attributes,
                                           String checkedElementName,
                                           String checkedAttributeName )
    {
      if( checkedElementName.equals( elementName ) ) {
        String attribute = attributes.getValue( checkedAttributeName );
        try {
          Integer.parseInt( attribute );
        } catch( NumberFormatException exception ) {
          String message
            = "Mandatory attribute \"{0}\" for element \"{1}\" is missing or not a valid integer";
          Object[] arguments = new Object[] { checkedAttributeName, checkedElementName };
          message = MessageFormat.format( message, arguments );
          throw new IllegalArgumentException( message );
        }
      }
    }

  }

}
