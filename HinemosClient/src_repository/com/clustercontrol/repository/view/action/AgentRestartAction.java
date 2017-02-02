/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.repository.action.GetAgentListTableDefine;
import com.clustercontrol.repository.bean.AgentCommandConstant;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.repository.view.AgentListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.repository.InvalidRole_Exception;

/**
 * ノードの作成・変更ダイアログによる、ノード登録を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class AgentRestartAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( AgentRestartAction.class );

	public static final String ID = AgentRestartAction.class.getName();

	//	 ----- instance フィールド ----- //

	/** ビュー */
	private IWorkbenchPart viewPart;

	// ----- instance メソッド ----- //
	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.viewPart = HandlerUtil.getActivePart(event);
		AgentListView view = null;
		try {
			view = (AgentListView) this.viewPart
					.getAdapter(AgentListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		StructuredSelection selection = (StructuredSelection) view
				.getComposite().getTableViewer().getSelection();

		List<?> selectionList = selection.toList();
		if (selectionList.size() == 0) {
			return null;
		}
		Map<String, ArrayList<String>> map = new ConcurrentHashMap<String, ArrayList<String>>();

		for (Object o : selectionList) {
			List<?> list = (List<?>) o;
			if (list == null) {
				continue;
			}
			String managerName = (String) list.get(GetAgentListTableDefine.MANAGER_NAME);
			if(map.get(managerName) == null) {
				map.put(managerName, new ArrayList<String>());
			}
		}

		StringBuffer message = new StringBuffer();
		boolean flag = true;
		for (Object o : selectionList) {
			List<?> list = (List<?>) o;
			String managerName = null;
			String facilityId = null;
			String facilityName = null;
			if (list != null) {
				managerName = (String) list.get(GetAgentListTableDefine.MANAGER_NAME);
				facilityId = (String) list.get(GetAgentListTableDefine.FACILITY_ID);
				facilityName = (String) list.get(GetAgentListTableDefine.FACILITY_NAME);
				map.get(managerName).add(facilityId);
			}

			// ファシリティIDがおかしい場合は、リターンする。
			if (facilityId == null) {
				return null;
			} else if ("".equals(facilityId)) {
				return null;
			}

			// 複数選択した場合はカンマでつなげる。
			if (flag) {
				flag = false;
			} else {
				message.append(", ");
			}
			message.append(facilityName + "(" + facilityId + ")");
		}

		// 確認する。
		String[] args = { message.toString() };
		if (!MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.repository.44", args))) {
			m_log.debug("cancel");
			return null;
		}

		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for(Map.Entry<String, ArrayList<String>> entry : map.entrySet()) {
			String managerName = entry.getKey();
			ArrayList<String> facilityIdList = entry.getValue();
			try {
				RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
				wrapper.restartAgent(facilityIdList, AgentCommandConstant.RESTART);
			} catch (InvalidRole_Exception e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				Object[] arg = {managerName};
				errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16", arg));
			} catch (Exception e) {
				m_log.warn("run(), " + e.getMessage(), e);
				errorMsgs.put(managerName, Messages.getString("message.hinemos.failure.unexpected") + HinemosMessage.replace(e.getMessage()));
			}
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
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
				if(part instanceof AgentListView){
					// Enable button when 1 item is selected
					AgentListView view = (AgentListView)part;

					if(view.getSelectedNum() > 0) {
						editEnable = true;
					}
				}
				this.setBaseEnabled(editEnable);
			}
		}
	}
}
