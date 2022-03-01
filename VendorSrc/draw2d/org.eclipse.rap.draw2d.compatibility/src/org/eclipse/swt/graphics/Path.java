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

public class Path extends Resource {

public Path (Device device) {
	super(device);
}

public void addArc (float x, float y, float width, float height, float startAngle, float arcAngle) {
}

public void addPath (Path path) {
}

public void addRectangle (float x, float y, float width, float height) {
}

public void addString (String string, float x, float y, Font font) {
}

public void close () {
}

public boolean contains (float x, float y, GC gc, boolean outline) {
	return false;
}

public void cubicTo (float cx1, float cy1, float cx2, float cy2, float x, float y) {
}

void destroy () {
}

public void getBounds (float[] bounds) {
}

public void getCurrentPoint (float[] point) {
}

public PathData getPathData () {
	return null;
}

public void lineTo (float x, float y) {
}

public boolean isDisposed () {
	return false;
}

public void moveTo (float x, float y) {
}

public void quadTo (float cx, float cy, float x, float y) {
}

public String toString () {
	return "";
}

}
