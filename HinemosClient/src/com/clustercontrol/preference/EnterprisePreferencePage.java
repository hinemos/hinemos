/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.preference;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Enterprise page folder<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class EnterprisePreferencePage extends PreferencePage  implements IWorkbenchPreferencePage {

	@Override
	public void init(IWorkbench workbench) {}

	@Override
	protected Control createContents(Composite parent) {
		return null;
	}
	
}
