/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.dialog;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ダイアログをまとめて管理する
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class UtilityDialogInjector {
	private static Log logger = LogFactory.getLog(UtilityDialogInjector.class);

	private static IUtilityDialogService _service = null;

	// setter for dependency injection	
	public static void setService(IUtilityDialogService service) {
		logger.info("setService() " + service);
		if(null != _service){
			// 発生しないはず
			logger.warn("setService() has been run twice!");
		}
		UtilityDialogInjector._service = service;
	}

	public static UtilityProcessDialog createImportProcessDialog(Object shell, String message) {
		assert shell == null : "Shell must be null here";
		
		return _service.createImportProcessDialog(message);
	}

	public static DeleteProcessDialog createDeleteProcessDialog(Object shell, String message) {
		assert shell == null : "Shell must be null here";

		return _service.createDeleteProcessDialog(message);
	}

	public static UtilityResultDialog createImportResultDialog(Object shell, String pluginId, String title,
			String mainMessage, List<String> detailList) {
		assert shell == null : "Shell must be null here";

		return _service.createImportResultDialog(pluginId, title, mainMessage, detailList);
	}
	
	public static UtilityProcessDialog createImportContinueDialog(Object shell, String message) {
		assert shell == null : "Shell must be null here";

		return _service.createImportContinueDialog(message);
	}

}
