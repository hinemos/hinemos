/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import com.clustercontrol.jobmap.view.JobMapView;

public class ZoomAdjustAction extends BaseAction {
	public static final String ID = ActionIdBase + ZoomAdjustAction.class.getSimpleName();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);

		this.window = HandlerUtil.getActiveWorkbenchWindow(event);

		if (null == window || !isEnabled()) {
			return null;
		}
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		JobMapView view = (JobMapView) viewPart;

		if (view == null) {
			return null;
		}

		ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
		Command command = commandService.getCommand(ID);
		boolean isChecked = !HandlerUtil.toggleCommandState(command);

		// 自動調整ボタンの有効・無効
		if (isChecked) {
			view.setZoomAdjust(true);
		} else {
			view.setZoomAdjust(false);
		}
		view.updateNotManagerAccess();

		return null;
	}

}