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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;

/**
 * ビューをアイコン移動可能モードに変更するクライアント側アクションクラス<BR>
 * @since 1.0.0
 */
public class SetFloatingModeAction extends AbstractHandler implements IElementUpdater {
	/** アクションID */
	public static final String ID = OpenNodeMapAction.ActionIDBase + SetFloatingModeAction.class.getSimpleName();

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
		
	
		view.setMode(NodeMapView.Mode.FLOATING_MODE);
		view.setFocus();
		
		// 組み込みスコープの場合はコンテキストメニュー選択不能にする
		String scopeFacilityId = view.getController().getCurrentScope();
		boolean isScopeBuiltin = FacilityTreeAttributeConstant.isBuiltinScope(scopeFacilityId);
	
		if( isScopeBuiltin ){
			view.setEnabled(false);
		}
		else{
			view.setEnabled(true);			
		}
	
		return null;
	}
	
	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return;
		}
		viewPart = page.getActivePart();
		
		element.setChecked(false);
		if (viewPart instanceof NodeMapView) {
			NodeMapView view = (NodeMapView)viewPart;
			if (view.getMode() == NodeMapView.Mode.FLOATING_MODE) {
				element.setChecked(true);
			}
		}
	}
}
