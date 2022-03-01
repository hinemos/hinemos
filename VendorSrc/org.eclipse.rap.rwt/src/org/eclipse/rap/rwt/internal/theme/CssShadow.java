/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;


public class CssShadow implements CssValue {

  public static final CssShadow NONE = new CssShadow( false, 0 , 0, 0, 0, null, 0 );

  public final boolean inset;
  public final int offsetX;
  public final int offsetY;
  public final int blur;
  public final int spread;
  public final String color;
  public final float opacity;

  private CssShadow( boolean inset,
                    int offsetX,
                    int offsetY,
                    int blur,
                    int spread,
                    String color,
                    float opacity )
  {
    this.inset = inset;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.blur = blur;
    this.spread = spread;
    this.color = color;
    this.opacity = opacity;
  }

  public static CssShadow create( boolean inset,
                                 int offsetX,
                                 int offsetY,
                                 int blur,
                                 int spread,
                                 CssColor color )
  {
    String msg;
    if( blur < 0 ) {
      msg = "Shadow blur distance can't be negative";
      throw new IllegalArgumentException( msg );
    }
    if( spread != 0 ) {
      msg = "Shadow spread distance is not supported";
      throw new IllegalArgumentException( msg );
    }
    if( color == null ) {
      throw new NullPointerException( "null argument" );
    }
    String htmlColor = CssColor.toHtmlString( color.red, color.green, color.blue );
    return new CssShadow( inset, offsetX, offsetY, blur, spread, htmlColor, color.alpha );
  }

  @Override
  public boolean equals( Object object ) {
    if( object == this ) {
      return true;
    }
    if( object instanceof CssShadow ) {
      CssShadow other = ( CssShadow )object;
      return       other.inset == inset
                && other.offsetX == offsetX
                && other.offsetY == offsetY
                && other.blur == blur
                && other.spread == spread
                && ( color == null ? other.color == null : color.equals( other.color ) )
                && other.opacity == opacity;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result += 11 * result + offsetX;
    result += 11 * result + offsetY;
    result += 11 * result + blur;
    result += 11 * result + spread;
    if( color != null ) {
      result += 11 * result + color.hashCode();
    }
    result += 11 * result + Float.floatToIntBits( opacity );
    result += inset ? 0 : 11 * result + 13;
    return result;
  }

  public String toDefaultString() {
    StringBuilder buffer = new StringBuilder();
    if( color == null ) {
      buffer.append( "none" );
    } else {
      if( inset ) {
        buffer.append( "inset " );
      }
      buffer.append( offsetX );
      buffer.append( "px " );
      buffer.append( offsetY );
      buffer.append( "px " );
      buffer.append( blur );
      buffer.append( "px " );
      buffer.append( spread );
      buffer.append( "px " );
      buffer.append( "rgba( " );
      CssColor qxColor = CssColor.valueOf( color );
      buffer.append( qxColor.red );
      buffer.append( ", " );
      buffer.append( qxColor.green );
      buffer.append( ", " );
      buffer.append( qxColor.blue );
      buffer.append( ", " );
      buffer.append( opacity );
      buffer.append( " )" );
    }
    return buffer.toString();
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append( inset );
    buffer.append( ", " );
    buffer.append( offsetX );
    buffer.append( ", " );
    buffer.append( offsetY );
    buffer.append( ", " );
    buffer.append( blur );
    buffer.append( ", " );
    buffer.append( spread );
    buffer.append( ", " );
    buffer.append( color );
    buffer.append( ", " );
    buffer.append( opacity );
    return "CssShadow{ " + buffer.toString() + " }";
  }

}
