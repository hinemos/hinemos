/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view.action;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.nodemap.view.NodeMapView;

/**
 * ビューをコネクション編集可能モードに変更するクライアント側アクションクラス<BR>
 * @since 1.0.0
 */
public class ZoomAdjustAction extends AbstractHandler implements IElementUpdater {
	/** アクションID */
	public static final String ID = OpenNodeMapAction.ActionIDBase + ZoomAdjustAction.class.getSimpleName();

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
		view.setAdjust(!view.isAdjust());
		view.updateNotManagerAccess();
		view.setFocus();
		
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return;
		}
		
		IWorkbenchPart viewPart = page.getActivePart();
		
		if (viewPart instanceof NodeMapView) {
			element.setChecked(((NodeMapView)viewPart).isAdjust());
		}
	}
}
