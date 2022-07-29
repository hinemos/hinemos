/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.jface.text;

import org.eclipse.swt.widgets.Text;

/**
 * IViewportListener for RAP
 * 
 * Used by org.eclipse.jface.text.TextViewer
 * 
 */
public interface IViewportListener {

	Text getTextWidget();
	void setEditable(boolean editable);
	void setDocument( Document document, int modelRangeOffset, int modelRangeLength );
}
