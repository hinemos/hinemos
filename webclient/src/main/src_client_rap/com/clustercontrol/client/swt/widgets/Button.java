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

package com.clustercontrol.client.swt.widgets;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.events.MouseTrackAdapter;

/**
 * Single-sourcing implementation for Button Widget
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class Button extends org.eclipse.swt.widgets.Button {

	public Button(Composite parent, int style) {
		super(parent, style);
	}

	/*
	 * @see org.eclipse.swt.widgets.Control#addMouseListener(MouseListener listener)
	 */
	public void addMouseTrackListener(MouseTrackAdapter mouseTrackAdapter) {
		// Do nothing
	}
}
