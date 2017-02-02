/**********************************************************************
 * Copyright (C) 2014 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package org.eclipse.jface.text;

import org.eclipse.swt.widgets.Text;

/**
 * ITextViewer for RAP
 * 
 * Used by com.clustercontrol.dialog.TextAreaDialog
 * 
 */
public interface ITextViewer {

	Text getTextWidget();
	void setEditable(boolean editable);
	void setDocument( Document document, int modelRangeOffset, int modelRangeLength );
}
