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

import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;


public class ChildSelectorImpl implements DescendantSelector, SelectorExt {

  private final Selector parent;
  private final SimpleSelector child;

  public ChildSelectorImpl( Selector parent, SimpleSelector child ) {
    this.parent = parent;
    this.child = child;
  }

  public Selector getAncestorSelector() {
    return parent;
  }

  public SimpleSelector getSimpleSelector() {
    return child;
  }

  public short getSelectorType() {
    return SAC_CHILD_SELECTOR;
  }

  public String getElementName() {
    return ( ( SelectorExt )child ).getElementName();
  }

  public int getSpecificity() {
    return ( ( Specific )parent ).getSpecificity()
           + ( ( Specific )child ).getSpecificity();
  }

  public String[] getConstraints() {
    throw new UnsupportedOperationException();
  }

  public String toString() {
    return parent.toString() + " > " + child.toString();
  }
}
