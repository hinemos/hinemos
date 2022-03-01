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

import org.w3c.css.sac.AttributeCondition;


public class OneOfAttributeCondition implements AttributeCondition, ConditionExt {

  private final String localName;
  private final String value;
  private final boolean specified;

  public OneOfAttributeCondition( String localName, String value, boolean specified ) {
    this.localName = localName;
    this.value = value;
    this.specified = specified;
  }

  public String getLocalName() {
    return localName;
  }

  public String getNamespaceURI() {
    return null;
  }

  public boolean getSpecified() {
    return specified;
  }

  public String getValue() {
    return value;
  }

  public short getConditionType() {
    return SAC_ONE_OF_ATTRIBUTE_CONDITION;
  }

  public int getSpecificity() {
    return ATTR_SPEC;
  }

  public String[] getConstraints() {
    throw new UnsupportedOperationException();
  }

  public String toString() {
    return "[" + getLocalName() + "~=\"" + getValue() + "\"]";
  }
}
