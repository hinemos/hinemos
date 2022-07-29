/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.dialog;

import java.util.List;

/**
 * Utility用確認ダイアログ
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class ClientUtilityDialogService implements IUtilityDialogService {

	@Override
	public UtilityProcessDialog createImportProcessDialog(String message) {
		return new ClientImportProcessDialog(message);
	}

	@Override
	public DeleteProcessDialog createDeleteProcessDialog(String message) {
		return new ClientDeleteProcessDialog(message);
	}
	
	@Override
	public UtilityResultDialog createImportResultDialog(String pluginId, String title, String mainMessage,
			List<String> detailList) {
		return new ClientImportResultDialog(pluginId, title, mainMessage, detailList);
	}
	
	@Override
	public UtilityProcessDialog createImportContinueDialog(String message) {
		return new ClientImportContinueDialog(message);
	}
		
}
