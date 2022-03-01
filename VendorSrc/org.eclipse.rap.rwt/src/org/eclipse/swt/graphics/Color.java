/*******************************************************************************
 * Copyright (c) 2002, 2013 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.internal.graphics.ColorUtil;


/**
 * Instances of this class manage resources that implement SWT's RGB color
 * model.
 *
 * @see RGB
 * @see Device#getSystemColor
 *
 * @since 1.0
 */
public class Color extends Resource {

  /**
   * Holds the color values within one integer.
   */
  private int colorNr;

  /**
   * Prevents uninitialized instances from being created outside the package.
   */
  private Color( int colorNr ) {
    super( null );
    this.colorNr = colorNr;
  }

  /**
   * Constructs a new instance of this class given a device and an
   * <code>RGB</code> describing the desired red, green and blue values.
   * On limited color devices, the color instance created by this call
   * may not have the same RGB values as the ones specified by the
   * argument. The RGB values on the returned instance will be the color
   * values of the operating system color.
   * <p>
   * You must dispose the color when it is no longer required.
   * </p>
   *
   * @param device the device on which to allocate the color
   * @param rgb the RGB values of the desired color
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_NULL_ARGUMENT - if the rgb argument is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the red, green or blue components of the argument are not between 0 and 255</li>
   * </ul>
   *
   * @see #dispose
   * @since 1.3
   */
  public Color( Device device, RGB rgb ) {
    super( checkDevice( device ) );
    if( rgb == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    colorNr = ColorUtil.computeColorNr( rgb.red, rgb.green, rgb.blue );
  }

  /**
   * Constructs a new instance of this class given a device and the
   * desired red, green and blue values expressed as ints in the range
   * 0 to 255 (where 0 is black and 255 is full brightness). On limited
   * color devices, the color instance created by this call may not have
   * the same RGB values as the ones specified by the arguments. The
   * RGB values on the returned instance will be the color values of
   * the operating system color.
   * <p>
   * You must dispose the color when it is no longer required.
   * </p>
   *
   * @param device the device on which to allocate the color
   * @param red the amount of red in the color
   * @param green the amount of green in the color
   * @param blue the amount of blue in the color
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the red, green or blue argument is not between 0 and 255</li>
   * </ul>
   *
   * @see #dispose
   * @since 1.3
   */
  public Color( Device device, int red, int green, int blue ) {
    super( checkDevice( device ) );
    colorNr = ColorUtil.computeColorNr( red, green, blue );
  }

  /**
   * Returns the amount of blue in the color, from 0 to 255.
   *
   * @return the blue component of the color
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been
   *              disposed</li>
   *              </ul>
   */
  public int getBlue() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    return ( colorNr & 0xFF0000 ) >> 16;
  }

  /**
   * Returns the amount of green in the color, from 0 to 255.
   *
   * @return the green component of the color
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been
   *              disposed</li>
   *              </ul>
   */
  public int getGreen() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    return ( colorNr & 0xFF00 ) >> 8;
  }

  /**
   * Returns the amount of red in the color, from 0 to 255.
   *
   * @return the red component of the color
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been
   *              disposed</li>
   *              </ul>
   */
  public int getRed() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    return colorNr & 0xFF;
  }

  /**
   * Returns an <code>RGB</code> representing the receiver.
   *
   * @return the RGB for the color
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been
   *              disposed</li>
   *              </ul>
   */
  public RGB getRGB() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    return new RGB( getRed(), getGreen(), getBlue() );
  }

  /**
   * Compares the argument to the receiver, and returns true if they represent
   * the <em>same</em> object using a class specific comparison.
   *
   * @param object the object to compare with this object
   * @return <code>true</code> if the object is the same as this object and
   *         <code>false</code> otherwise
   * @see #hashCode
   */
  @Override
  public boolean equals( Object object ) {
    boolean result;
    if( object == this ) {
      result = true;
    } else if( object instanceof Color ) {
      Color color = ( Color )object;
      result = color.colorNr == this.colorNr;
    } else {
      result = false;
    }
    return result;
  }

  /**
   * Returns an integer hash code for the receiver. Any two objects that return
   * <code>true</code> when passed to <code>equals</code> must return the
   * same value for this method.
   *
   * @return the receiver's hash
   * @see #equals
   */
  @Override
  public int hashCode() {
    return colorNr;
  }

  /**
   * Returns a string containing a concise, human-readable description of the
   * receiver.
   *
   * @return a string representation of the receiver
   */
  @Override
  public String toString() {
    String result;
    if( isDisposed() ) {
      result = "Color {*DISPOSED*}";
    } else {
      result
        = "Color {" + getRed() + ", " + getGreen() + ", " + getBlue() + "}";
    }
    return result;
  }
}
