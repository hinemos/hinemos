/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.ui.settings.dialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

public class UtilityImportCommandDialog {
	public UtilityImportCommandDialog(Shell parent) {
	}

	public String getFilePath() {
		return null;
	}
	
	public int open(){
		return Window.OK;
	}
}