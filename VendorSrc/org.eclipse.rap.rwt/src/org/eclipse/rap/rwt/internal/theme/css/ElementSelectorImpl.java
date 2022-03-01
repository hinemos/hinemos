/*******************************************************************************
 * Copyright (c) 2008, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme.css;

import org.w3c.css.sac.ElementSelector;


public class ElementSelectorImpl implements ElementSelector, SelectorExt {

  private static final String[] EMPTY_STRING_ARRAY = new String[ 0 ];
  private final String tagName;

  public ElementSelectorImpl( String tagName ) {
    this.tagName = tagName;
  }

  public String getLocalName() {
    return tagName;
  }

  public String getNamespaceURI() {
    return null;
  }

  public short getSelectorType() {
    return SAC_ELEMENT_NODE_SELECTOR;
  }

  public int getSpecificity() {
    return tagName != null ? ELEMENT_SPEC : 0;
  }

  public String getElementName() {
    return tagName;
  }

  public String[] getConstraints() {
    return EMPTY_STRING_ARRAY;
  }

  public String toString() {
    return tagName != null ? tagName : "*";
  }
}
