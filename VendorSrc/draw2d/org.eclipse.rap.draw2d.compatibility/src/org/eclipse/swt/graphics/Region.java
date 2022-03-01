/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;

public final class Region extends Resource {

//	public Sprite object;
	
public Region () {
	this(null);
}

public Region (Device device) {
	super(device);
//	object = new Sprite();
//	if (object == null) SWT.error(SWT.ERROR_NO_HANDLES);
//	object.blendMode = BlendMode.LAYER;
//	object.cacheAsBitmap = true;
//	init();
}

public void add (int[] pointArray) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
//	if (pointArray == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
//	Shape shape = new Shape();
//	shape.blendMode = BlendMode.ADD;
//	Graphics graphics = shape.graphics;
//	graphics.beginFill(0xFFFFFF, 1);
//	graphics.moveTo(pointArray[0], pointArray[1]);
//	for (int i = 2; i < pointArray.length; i += 2) {
//		graphics.lineTo(pointArray[i], pointArray[i+1]);
//	}
//	graphics.lineTo(pointArray[0], pointArray[1]);
//	object.addChild(shape);
}

public void add (Rectangle rect) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (rect == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	add (rect.x, rect.y, rect.width, rect.height);
}

public void add (int x, int y, int width, int height) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (width < 0 || height < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
//	Shape shape = new Shape();
//	shape.blendMode = BlendMode.ADD; 
//	Graphics graphics = shape.graphics;
//	graphics.beginFill(0xFFFFFF, 1);
//	graphics.drawRect(x, y, width, height);
//	object.addChild(shape);
}

public void add (Region region) {
}

public boolean contains (int x, int y) {
	return false;
}

public boolean contains (Point pt) {
	return false;
}

void destroy () {
//	object = null;
}

public boolean equals (Object object) {
	if (this == object) return true;
	if (!(object instanceof Region)) return false;
	Region rgn = (Region)object;
//	return object == rgn.object;
	return super.equals( object );
}

public Rectangle getBounds () {
//	return new Rectangle((int)object.x, (int)object.y, (int)object.width, (int)object.height);
  return new Rectangle(0,0,10,10);
}

public int hashCode () {
	if (isDisposed()) return 0;
	return super.hashCode();
}

public void intersect (Rectangle rect) {
}

public void intersect (int x, int y, int width, int height) {
}

public void intersect (Region region) {
}

public boolean intersects (int x, int y, int width, int height) {
	return false;
}

public boolean intersects (Rectangle rect) {
	return false;
}

public boolean isDisposed () {
//	return object == null;
  return false;
}

public boolean isEmpty () {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
//	return object.numChildren == 0;
	return true;
}

public void subtract (int[] pointArray) {
}

public void subtract (Rectangle rect) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (rect == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	subtract (rect.x, rect.y, rect.width, rect.height);
}

public void subtract (int x, int y, int width, int height) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (width < 0 || height < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
//	Shape shape = new Shape();
//	shape.blendMode = BlendMode.ERASE; 
//	Graphics graphics = shape.graphics;
//	graphics.beginFill(0xFFFFFF, 1);
//	graphics.moveTo(x, y);
//	graphics.lineTo(x, y + height);
//	graphics.lineTo(x + width, y + height);
//	graphics.lineTo(x + width, y);
//	graphics.lineTo(x, y);
//	object.addChild(shape);
}

public void subtract (Region region) {
}

public void translate (int x, int y) {
}

public void translate (Point pt) {
}

public String toString () {
	if (isDisposed()) return "Region {*DISPOSED*}";
//	return "Region {" + object + "}";
	return "Region";
}

public static Region flex_new (Device device, Object handle) {
	return null;
}

}
