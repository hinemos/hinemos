/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.view.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.dialog.NodeAssignDialog;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.repository.view.ScopeListView;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * ノード選択ダイアログによる、ノードのスコープ割り当て処理を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class NodeAssignAction extends AbstractHandler implements IElementUpdater {
	public static final String ID = NodeAssignAction.class.getName();

	//	 ----- instance フィールド ----- //
	/** ログ */
	private static Log m_log = LogFactory.getLog(NodeAssignAction.class);

	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	// ----- instance メソッド ----- //

	/**
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// スコープ一覧より、選択されているアイテムを取得
		// スコープツリーより、選択されているスコープを取得
		this.viewPart = HandlerUtil.getActivePart(event);
		ScopeListView scopeListView = null;
		try {
			scopeListView = (ScopeListView) this.viewPart
					.getAdapter(ScopeListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}
		
		if (scopeListView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		FacilityTreeItem item = scopeListView.getSelectedScopeItem();
		// 未選択の場合は、処理終了
		if( null == item ){
			return null;
		}

		// スコープ以外を選択している場合は、処理終了
		FacilityInfo info = item.getData();
		if (info.getFacilityType() != FacilityConstant.TYPE_SCOPE) {
			return null;
		}

		FacilityTreeItem manager = ScopePropertyUtil.getManager(item);
		String managerName = manager.getData().getFacilityId();

		// ダイアログを生成
		String facilityId = info.getFacilityId();
		NodeAssignDialog dialog = new NodeAssignDialog(this.viewPart.getSite()
				.getShell(), managerName, facilityId);

		// ダイアログにて変更された場合、ビューを更新する
		if (dialog.open() == IDialogConstants.OK_ID) {
			scopeListView.update();
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				boolean editEnable = false;
				if(part instanceof ScopeListView){
					// Enable button when 1 item is selected
					ScopeListView view = (ScopeListView)part;

					if(view.getBuiltin() == false &&
							(view.getScopeTreeComposite().getTree().isFocusControl() ||
							view.getComposite().getTable().isFocusControl())) {

						switch(view.getType()) {
							case FacilityConstant.TYPE_COMPOSITE:
								break;
							case FacilityConstant.TYPE_SCOPE:
								editEnable = !view.getNotReferFlg();
								break;
							case FacilityConstant.TYPE_NODE:
								break;
							default: // 既定の対処はスルー。
								break;
						}
					}
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
