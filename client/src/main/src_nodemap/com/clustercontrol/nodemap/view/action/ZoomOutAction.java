/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view.action;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.clustercontrol.nodemap.view.NodeMapView;

/**
 * ビューをコネクション編集可能モードに変更するクライアント側アクションクラス<BR>
 * @since 1.0.0
 */
public class ZoomOutAction extends AbstractHandler {
	/** アクションID */
	public static final String ID = OpenNodeMapAction.ActionIDBase + ZoomOutAction.class.getSimpleName();

	/** ビュー */
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		viewPart = null;
		window = null;
	}

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
		view.zoomOut();
		view.setFocus();
		
		return null;
	}
}
