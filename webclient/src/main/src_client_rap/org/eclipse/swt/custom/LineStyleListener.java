/**********************************************************************
 * Copyright (C) 2006 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package org.eclipse.swt.custom;

import org.eclipse.swt.internal.SWTEventListener;

/**
 * LineStyleListener for RAP
 * 
 * @since 5.0.0
 */
@SuppressWarnings("restriction")
public interface LineStyleListener extends SWTEventListener {

	public void lineGetStyle(LineStyleEvent event);
}
