/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.infra.dialog.InfraFileDialog;
import com.clustercontrol.infra.util.InfraFileUtil;
import com.clustercontrol.infra.view.InfraFileManagerView;


public class ModifyInfraFileAction extends InfraFileManagerBaseAction {
	/** ログ */
	private static Log m_log = LogFactory.getLog(ModifyInfraFileAction.class);

	/** アクションID */
	public static final String ID = ModifyInfraFileAction.class.getName();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		InfraFileManagerView view = getView(event);
		if (view == null) {
			m_log.info("execute: view is null");
			return null;
		}

		//アップロードダイアログを開く
		InfraFileDialog dialog = new InfraFileDialog(
				this.viewPart.getSite().getShell(),
				InfraFileUtil.getManagerName(view),
				PropertyDefineConstant.MODE_MODIFY,
				InfraFileUtil.getSelectedInfraFileInfo(view));
		dialog.open();

		// ビューの更新
		view.update();

		return null;
	}
}
