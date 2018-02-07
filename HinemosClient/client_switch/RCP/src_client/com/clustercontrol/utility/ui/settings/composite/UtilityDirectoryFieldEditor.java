/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.ui.settings.composite;

import org.eclipse.swt.widgets.Composite;

public class UtilityDirectoryFieldEditor extends org.eclipse.jface.preference.DirectoryFieldEditor{
    public UtilityDirectoryFieldEditor(String name, String labelText, Composite parent) {
    	super(name, labelText, parent);
	}
}
