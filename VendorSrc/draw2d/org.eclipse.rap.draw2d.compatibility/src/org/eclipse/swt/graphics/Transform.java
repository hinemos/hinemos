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

public class Transform /*extends Resource*/ {

//	public Matrix object;
private Device device;

public Transform (Device device) {
	this(device, 1, 0, 0, 1, 0, 0);
}

public Transform (Device device, float[] elements) {
	this (device, checkTransform(elements)[0], elements[1], elements[2], elements[3], elements[4], elements[5]);
}

public Transform (Device device, float m11, float m12, float m21, float m22, float dx, float dy) {
	this.device = device;
//	object = new Matrix(m11, m12, m21, m22, dx, dy);
//	if (object == null) SWT.error(SWT.ERROR_NO_HANDLES);
//	init();
}

static float[] checkTransform(float[] elements) {
	if (elements == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (elements.length < 6) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	return elements;
}

public void dispose(){
	destroy();
}

void destroy () {
//	object = null;
}

public void getElements (float[] elements) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (elements == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (elements.length < 6) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
//	elements[0] = (float)object.a;
//	elements[1] = (float)object.b;
//	elements[2] = (float)object.c;
//	elements[3] = (float)object.d;
//	elements[4] = (float)object.tx;
//	elements[5] = (float)object.ty;
}

public void identity() {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
//	object.identity();
}

public void invert () {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
//	object.invert();
}

public boolean isDisposed () {
//	return object == null;
  return false;
}

public boolean isIdentity () {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
//	return object.a == 1 && object.b == 0 && object.c == 0 && object.d == 1 && object.tx == 0 && object.ty == 0;
	return true;
}

public void multiply (Transform matrix) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (matrix == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (matrix.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
//	Matrix m = (Matrix)matrix.object.clone();
//	m.concat(object);
//	object.a = m.a;
//	object.b = m.b;
//	object.c = m.c;
//	object.d = m.d;
//	object.tx = m.tx;
//	object.ty = m.ty;
}

public void rotate (float angle) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
//	object.rotate(angle * (float)Compatibility.PI / 180);
}

public void scale (float scaleX, float scaleY) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
//	object.scale(scaleX, scaleY);
}

public void setElements (float m11, float m12, float m21, float m22, float dx, float dy) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
//	object.a = m11;
//	object.b = m12;
//	object.c = m21;
//	object.d = m22;
//	object.tx = dx;
//	object.ty = dy;
}

public void shear(float shearX, float shearY) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
//	Matrix matrix = new Matrix(1, shearX, shearY, 1, 0, 0);
//	matrix.concat(object);
//	object.a = matrix.a;
//	object.b = matrix.b;
//	object.c = matrix.c;
//	object.d = matrix.d;
//	object.tx = matrix.tx;
//	object.ty = matrix.ty;
}

public void transform (float[] pointArray) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (pointArray == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	//TODO
//	intrinsic.flash.geom.Point point = new intrinsic.flash.geom.Point(0, 0);
//	for (int i = 0; i < pointArray.length; i+=2) {
//		point.x = pointArray[i];
//		point.y = pointArray[i+1];
//		point = object.transformPoint(point);
//		pointArray[i] = (float)point.x;
//		pointArray[i+1] = (float)point.y;
//	}
}

public void translate (float offsetX, float offsetY) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
//	object.translate(offsetX, offsetY);
}

public String toString () {
	if (isDisposed()) return "Transform {*DISPOSED*}";
	float[] elements = new float[6];
	getElements(elements);
	return "Transform {" + elements [0] + "," + elements [1] + "," +elements [2] + "," +elements [3] + "," +elements [4] + "," +elements [5] + "}";
}

}
