/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.util.Messages;

/**
 * ビューの更新を行うクライアント側アクションクラス<BR>
 * @since 1.0.0
 */
public class UpdateAction extends AbstractHandler {
	/** アクションID */
	public static final String ID = OpenNodeMapAction.ActionIDBase + UpdateAction.class.getSimpleName();

	/** ビュー */
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == window || !isEnabled() ){
			return null;
		}
		
		

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		
		NodeMapView view = (NodeMapView) viewPart.getAdapter(NodeMapView.class);
		if (view == null) {
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					com.clustercontrol.nodemap.messages.Messages.getString("view.select.prompt"));
			return null;
		}
		
		// 編集中の情報を解除してよいか確認
		if (view.isEditing()) {
			if (!MessageDialog.openQuestion(
					null,
					Messages.getString("confirm"),
					com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
				return null;
			}
		}
		
		view.reload();
		
		return null;
	}
}
