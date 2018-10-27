/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.ui.settings.composite;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class UtilityDirectoryFieldEditor extends StringFieldEditor {
	
    public UtilityDirectoryFieldEditor(String name, String labelText, Composite parent) {
    	super(name, labelText, parent);
	}

    @Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
    	super.doFillIntoGrid(parent, numColumns/* - 1*/);
    	getTextControl().setEditable(false);
    }
    
	public void setChangeButtonText(String string) {
	}
}
