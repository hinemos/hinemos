/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.util;

import java.text.MessageFormat;


/**
 * <p>Utility class for doing common method parameter checks.</p>
 */
public final class ParamCheck {

  private static final String NOT_NULL_TEXT = "The parameter ''{0}'' must not be null.";
  private static final String NOT_EMPTY_TEXT = "The parameter ''{0}'' must not be empty.";

  private ParamCheck() {
    // prevent instantiation
  }

  /** <p>Checks whether the given <code>param</code> is <code>null</code> and
   *  throws a <code>NullPointerException</code> with the message 'The
   *  parameter <em>paramName</em> must not be null.' if so.</p>
   *
   *  @param param the object which must not be null.
   *  @param paramName the human-readable name of the <code>param</code>.
   */
  public static void notNull( Object param, String paramName ) {
    if ( param == null ) {
      Object[] args = new Object[] { paramName };
      String msg = MessageFormat.format( NOT_NULL_TEXT, args );
      throw new NullPointerException( msg );
    }
  }

  /**
   * <p>Ensures that the given String <code>param</code> is not
   * <code>null</code>, not empty or composed entirely of whitespace.
   *
   * @param param the String which must be non-null and non-empty
   * @param paramName the human-readable name of the <code>param</code>.
   * @throws NullPointerException if param is <code>null</code>
   * @throws IllegalArgumentException if param is empty or composed
   *         entirely of whitespace
   */
  public static void notNullOrEmpty( String param, String paramName ) {
    ParamCheck.notNull( param, paramName );
    if( param.trim().length() == 0 ) {
      Object[] args = new Object[] { paramName };
      String msg = MessageFormat.format( NOT_EMPTY_TEXT, args );
      throw new IllegalArgumentException( msg );
    }
  }
}
