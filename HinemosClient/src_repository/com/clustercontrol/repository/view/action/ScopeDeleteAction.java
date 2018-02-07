/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import com.clustercontrol.repository.action.DeleteScopeProperty;
import com.clustercontrol.repository.action.GetScopeListTableDefine;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.repository.view.ScopeListView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * スコープの削除を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class ScopeDeleteAction extends AbstractHandler implements IElementUpdater {
	public static final String ID = ScopeDeleteAction.class.getName();

	//	 ----- instance フィールド ----- //
	/** ログ */
	private static Log m_log = LogFactory.getLog(ScopeDeleteAction.class);

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
			scopeListView = (ScopeListView) this.viewPart.getAdapter(ScopeListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (scopeListView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		List<?> selectionList = scopeListView.getSelectedScopeItems();
		if( null == selectionList ){
			return null;
		}

		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
		List<String> facilityNameList = new ArrayList<String>();

		for (Object obj : selectionList) {
			if  (obj instanceof FacilityTreeItem) {
				FacilityTreeItem item= (FacilityTreeItem)obj;
				FacilityTreeItem manager = ScopePropertyUtil.getManager(item);
				String managerName = null;
				if(manager == null) {
					managerName = item.getChildren().get(0).getData().getFacilityId();
				} else {
					managerName = manager.getData().getFacilityId();
				}
				if(map.get(managerName) == null) {
					map.put(managerName, new ArrayList<String>());
				}
			}
		}

		int size = 0;
		String facilityId = null;
		for (Object obj : selectionList) {
			String managerName = null;
			if  (obj instanceof FacilityTreeItem) {
				FacilityTreeItem item= (FacilityTreeItem)obj;
				// コンポジット・ノードを選択している場合は、処理終了
				FacilityInfo info = item.getData();
				if (info.getFacilityType() == FacilityConstant.TYPE_COMPOSITE
						|| info.getFacilityType() == FacilityConstant.TYPE_NODE) {
					continue;
				}

				FacilityTreeItem manager = ScopePropertyUtil.getManager(item);
				if(manager == null) {
					managerName = item.getChildren().get(0).getData().getFacilityId();
				} else {
					managerName = manager.getData().getFacilityId();
				}

				facilityId = item.getData().getFacilityId();
				map.get(managerName).add(facilityId);
				facilityNameList.add(item.getData().getFacilityName());
			} else {
				List<?> sList = (List<?>)obj;
				facilityId = null;
				String facilityName = null;
				if (sList != null) {
					managerName = (String) sList.get(GetScopeListTableDefine.MANAGER_NAME);
					facilityId = (String) sList.get(GetScopeListTableDefine.FACILITY_ID);
					facilityName = (String) sList.get(GetScopeListTableDefine.FACILITY_NAME);
					facilityNameList.add(facilityName);
					map.get(managerName).add(facilityId);
				}
			}
			size++;
		}

		// 確認ダイアログにて変更が選択された場合、削除処理を行う。
		if (map.isEmpty()) {
			return null;
		}

		// 確認ダイアログにて変更が選択された場合、削除処理を行う。
		String msg = "";
		String[] args = new String[2];
		if (size == 1) {
			args[0] = facilityNameList.get(0);
			args[1] = facilityId;
			msg = "message.repository.3";
		} else {
			args[0] = Integer.toString(size);
			msg = "message.repository.52";
		}

		if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString(msg, args)) == false) {

			return null;
		}

		for(Map.Entry<String, List<String>> entry : map.entrySet()) {
			String managerName = entry.getKey();
			List<String> facilityIdList = entry.getValue();
			new DeleteScopeProperty().delete(managerName, facilityIdList);
		}

		// ビューを更新
		scopeListView.update();
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
