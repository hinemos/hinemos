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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.repository.dialog.ScopeCreateDialog;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.repository.view.ScopeListView;

/**
 * ノードの作成・変更ダイアログによる、ノード登録を行うアクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class ScopeAddAction extends AbstractHandler implements IElementUpdater {
	public static final String ID = ScopeAddAction.class.getName();

	//	 ----- instance フィールド ----- //
	/** ログ */
	private static Log m_log = LogFactory.getLog(ScopeAddAction.class);

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

		FacilityTreeItemResponse item = scopeListView.getSelectedScopeItem();
		// 未選択もしくはノードを選択している場合は、処理終了
		if( null == item || item.getData().getFacilityType() == FacilityTypeEnum.NODE ){
			return null;
		}

		FacilityTreeItemResponse manager = ScopePropertyUtil.getManager(item);
		String managerName = manager.getData().getFacilityId();

		// ダイアログを生成
		ScopeCreateDialog dialog = new ScopeCreateDialog(
				this.viewPart.getSite().getShell(), managerName, null, false);
		String parentId = item.getData().getFacilityId();
		if(managerName.equals(parentId)) {
			parentId = "";
		}
		dialog.setParentFacilityId(parentId);

		// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
		if( dialog.open() == IDialogConstants.OK_ID ){
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
				IViewReference viewRef = page.findViewReference( ScopeListView.ID );
				
				if (viewRef == null)
					return;
				
				IViewPart part = viewRef.getView( false );

				boolean editEnable = false;
				if(part instanceof ScopeListView){
					// Enable button when 1 item is selected
					ScopeListView view = (ScopeListView)part;

					if(view.getBuiltin() == false &&
						(view.getScopeTreeComposite().getTree().isFocusControl() ||
						view.getComposite().getTable().isFocusControl())) {

						switch(view.getType()) {
							case COMPOSITE:
								break;
							case MANAGER:
								editEnable = true;
								break;
							case SCOPE:
								editEnable = !view.getNotReferFlg();
								break;
							case NODE:
								break;
							default: // 既定の対処はスルー。
								break;
						}
					}
				}
				this.setBaseEnabled(editEnable);
			}
		}
	}
}
