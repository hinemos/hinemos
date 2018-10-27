/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.view.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.infra.dialog.InfraFileDialog;
import com.clustercontrol.infra.view.InfraFileManagerView;


public class AddInfraFileAction extends InfraFileManagerBaseAction {
	
	/** ログ */
	private static Log m_log = LogFactory.getLog(AddInfraFileAction.class);

	/** アクションID */
	public static final String ID = AddInfraFileAction.class.getName();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		InfraFileManagerView view = getView(event);
		if (view == null) {
			m_log.info("execute: view is null");
			return null;
		}

		//アップロードダイアログを開く
		InfraFileDialog dialog = new InfraFileDialog(null);
		dialog.open();

		// ビューの更新
		view.update();

		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
	}
}
