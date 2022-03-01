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


public class PseudoClassConditionImpl implements AttributeCondition, ConditionExt {

  private final String value;

  public PseudoClassConditionImpl( String value ) {
    this.value = value;
  }

  public String getLocalName() {
    return null;
  }

  public String getNamespaceURI() {
    return null;
  }

  public boolean getSpecified() {
    return false;
  }

  public String getValue() {
    return value;
  }

  public short getConditionType() {
    return SAC_PSEUDO_CLASS_CONDITION;
  }

  public int getSpecificity() {
    return ATTR_SPEC;
  }

  public String[] getConstraints() {
    return new String[] { ":" + value };
  }

  public String toString() {
    return ":" + value;
  }
}
