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

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CharacterDataSelector;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.NegativeSelector;
import org.w3c.css.sac.ProcessingInstructionSelector;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorFactory;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;


/**
 * SelectorFacory implementation for parsing RAP theme files. All returned
 * selectors implement the interface {@link SelectorExt}.
 */
public class SelectorFactoryImpl implements SelectorFactory {

  private final CssFileReader reader;

  public SelectorFactoryImpl( CssFileReader reader ) {
    this.reader = reader;
  }

  public ElementSelector createElementSelector( String namespaceURI, String tagName )
    throws CSSException
  {
    return new ElementSelectorImpl( tagName );
  }

  public ConditionalSelector createConditionalSelector( SimpleSelector selector, Condition condition )
    throws CSSException
  {
    return new ConditionalSelectorImpl( selector, condition );
  }

  // ==========================================================================
  // Not supported by RAP

  public DescendantSelector createChildSelector( Selector parent, SimpleSelector child )
    throws CSSException
  {
    //    return new ChildSelectorImpl( parent, child );
    String mesg = "Child selectors not supported by RAP - ignored";
    reader.addProblem( new CSSException( mesg ) );
    return new NullDescendantSelector();
  }

  public ElementSelector createPseudoElementSelector( String namespaceURI, String pseudoName )
    throws CSSException
  {
    String mesg = "Pseudo element selectors not supported by RAP - ignored";
    reader.addProblem( new CSSException( mesg ) );
    return new NullElementSelector();
  }

  public DescendantSelector createDescendantSelector( Selector parent, SimpleSelector descendant )
    throws CSSException
  {
    String mesg = "Descendant selectors not supported by RAP - ignored";
    reader.addProblem( new CSSException( mesg ) );
    return new NullDescendantSelector();
  }

  public SiblingSelector createDirectAdjacentSelector( short nodeType,
                                                       Selector child,
                                                       SimpleSelector directAdjacent )
    throws CSSException
  {
    String mesg = "Sibling selectors not supported by RAP - ignored";
    reader.addProblem( new CSSException( mesg ) );
    return new NullSiblingSelector();
  }

  // ==========================================================================
  // Not implemented in CSS 2

  public SimpleSelector createRootNodeSelector() throws CSSException {
    throw new CSSException( "Root node selectors not supported by CSS2" );
  }

  public CharacterDataSelector createTextNodeSelector( String data ) throws CSSException {
    throw new CSSException( "Text node selectors not supported by CSS2" );
  }

  public CharacterDataSelector createCDataSectionSelector( String data ) throws CSSException {
    throw new CSSException( "CData section selectors not supported by CSS2" );
  }

  public ProcessingInstructionSelector createProcessingInstructionSelector( String target,
                                                                            String data )
    throws CSSException
  {
    throw new CSSException( "Processing instruction selectors not supported by CSS2" );
  }

  public CharacterDataSelector createCommentSelector( String data ) throws CSSException {
    throw new CSSException( "Comment selectors not supported by CSS2" );
  }

  public SimpleSelector createAnyNodeSelector() throws CSSException {
    throw new CSSException( "Any-node selectors not supported by CSS2" );
  }

  public NegativeSelector createNegativeSelector( SimpleSelector selector ) throws CSSException {
    throw new CSSException( "Negative selectors not supported by CSS2" );
  }

}
