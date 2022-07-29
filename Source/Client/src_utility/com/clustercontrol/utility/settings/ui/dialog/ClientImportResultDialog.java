/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.settings.ui.dialog;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import com.clustercontrol.utility.ui.dialog.ErrorDialogWithScroll;

public class ClientImportResultDialog implements UtilityResultDialog {
	
	private ErrorDialogWithScroll dialog;
	public ClientImportResultDialog(String pluginId , String title, String mainMessage, List<String> detailList){
		MultiStatus mStatus = new MultiStatus(pluginId, IStatus.OK, mainMessage, null);
		for (String resMessage : detailList) {
			mStatus.add(new Status(IStatus.WARNING, pluginId, IStatus.OK, resMessage, null));
		}
		this.dialog = new ErrorDialogWithScroll(null, title, null, (IStatus) mStatus,
				IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
	}
	
	@Override
	public int open() {
		return dialog.open();
	}
}
