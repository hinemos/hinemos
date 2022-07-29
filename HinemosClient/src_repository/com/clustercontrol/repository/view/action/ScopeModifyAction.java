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
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.repository.dialog.ScopeCreateDialog;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.repository.view.ScopeListView;

/**
 * スコープの作成・変更ダイアログによる、スコープ変更を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class ScopeModifyAction extends AbstractHandler implements IElementUpdater {
	public static final String ID = ScopeModifyAction.class.getName();

	//	 ----- instance フィールド ----- //
	/** ログ */
	private static Log m_log = LogFactory.getLog(ScopeModifyAction.class);

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

		FacilityTreeItemResponse item = scopeListView.getSelectedScopeItem();
		// 未選択もしくはノードを選択している場合は、処理終了
		if( null == item ){
			return null;
		}

		// コンポジット・ノードを選択している場合は、処理終了
		FacilityInfoResponse info = item.getData();
		if (info.getFacilityType() == FacilityTypeEnum.COMPOSITE
				|| info.getFacilityType() == FacilityTypeEnum.NODE) {
			return null;
		}

		FacilityTreeItemResponse manager = ScopePropertyUtil.getManager(item);
		String managerName = manager.getData().getFacilityId();

		// ダイアログを生成
		String facilityId = info.getFacilityId();
		ScopeCreateDialog dialog = new ScopeCreateDialog(this.viewPart
				.getSite().getShell(), managerName, facilityId, true);

		// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
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
							case COMPOSITE:
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
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
