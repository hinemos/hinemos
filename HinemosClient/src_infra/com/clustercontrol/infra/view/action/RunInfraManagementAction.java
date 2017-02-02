/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.infra.action.GetInfraManagementTableDefine;
import com.clustercontrol.infra.dialog.RunDialog;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.infra.util.AccessUtil;
import com.clustercontrol.infra.util.ModuleUtil;
import com.clustercontrol.infra.view.InfraManagementView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.infra.AccessInfo;
import com.clustercontrol.ws.infra.FacilityNotFound_Exception;
import com.clustercontrol.ws.infra.HinemosUnknown_Exception;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.clustercontrol.ws.infra.InfraManagementNotFound_Exception;
import com.clustercontrol.ws.infra.InfraModuleInfo;
import com.clustercontrol.ws.infra.InfraModuleNotFound_Exception;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidSetting_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;
import com.clustercontrol.ws.infra.ModuleResult;
import com.clustercontrol.ws.infra.NotifyNotFound_Exception;
import com.clustercontrol.ws.infra.SessionNotFound_Exception;

public class RunInfraManagementAction extends AbstractHandler implements IElementUpdater {
	// ログ
	private static Log m_log = LogFactory.getLog( RunInfraManagementAction.class );

	/** アクションID */
	public static final String ID = RunInfraManagementAction.class.getName();
	/** dispose*/
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		if(!(viewPart instanceof InfraManagementView)){
			return null;
		}

		InfraManagementView infraManagementView = null;
		try {
			infraManagementView = (InfraManagementView) viewPart.getAdapter(InfraManagementView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}
		if(infraManagementView == null) {
			m_log.info("execute: view is null");
			return null;
		}

		StructuredSelection selection = null;
		if(!(infraManagementView.getComposite().getTableViewer().getSelection() instanceof StructuredSelection)){
			return null;
		}
		selection = (StructuredSelection) infraManagementView.getComposite().getTableViewer().getSelection();
		if(selection == null){
			return null;
		}

		List<?> sList = (List<?>) selection.toList();
		Map<String, List<String>> managementIdMap = new ConcurrentHashMap<String, List<String>>();

		for (Object obj : sList) {
			List<?> list = (List<?>)obj;
			String managerName = null;
			if (list == null) {
				continue;
			}
			managerName = (String) list.get(GetInfraManagementTableDefine.MANAGER_NAME);
			if(managementIdMap.get(managerName) == null) {
				managementIdMap.put(managerName, new ArrayList<String>());
			}
		}

		for (Object obj : sList) {
			List<?> list = (List<?>)obj;
			String managementId = null;
			String managerName = null;
			if (list != null) {
				managementId = (String) list.get(GetInfraManagementTableDefine.MANAGEMENT_ID);
				managerName = (String) list.get(GetInfraManagementTableDefine.MANAGER_NAME);
				managementIdMap.get(managerName).add(managementId);
			}
		}

		Map<String, List<InfraManagementInfo>> managementMap = new ConcurrentHashMap<String, List<InfraManagementInfo>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		StringBuffer idbuf = new StringBuffer();
		int size = 0;
		for(Map.Entry<String, List<String>> entry : managementIdMap.entrySet()) {
			String managerName = entry.getKey();
			if(managementMap.get(managerName) == null) {
				managementMap.put(managerName, new ArrayList<InfraManagementInfo>());
			}
			for(String managementId : entry.getValue()) {
				try {
					InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
					InfraManagementInfo management = wrapper.getInfraManagement(managementId);
					managementMap.get(managerName).add(management);
					if(size > 0) {
						idbuf.append(", ");
					}
					idbuf.append(managementId);
				} catch (InvalidRole_Exception e) {
					// 権限なし
					errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
				} catch (HinemosUnknown_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception | InfraManagementNotFound_Exception e) {
					m_log.error("execute() : " + e.getClass().getName() + ", " + e.getMessage());
					Object[] arg = new Object[]{Messages.getString("infra.module.id"), Messages.getString("infra.module.run"),
							Messages.getString("failed"), HinemosMessage.replace(e.getMessage())};
					errorMsgs.put(managerName, Messages.getString("message.infra.action.result", arg));
				}
				size++;
			}
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
			return null;
		}

		boolean allRun = false;
		RunDialog dialog = new RunDialog(null, 
				Messages.getString("message.infra.confirm.action",
						new Object[]{Messages.getString("infra.management.id"),
						Messages.getString("infra.module.run"), idbuf.toString()}));
		if (dialog.open() == IDialogConstants.CANCEL_ID) {
			return null;
		}
		allRun = dialog.isAllRun();

		errorMsgs = new ConcurrentHashMap<>();
		for(Map.Entry<String, List<InfraManagementInfo>> entry : managementMap.entrySet()) {
			String managerName = entry.getKey();
			for(InfraManagementInfo management : entry.getValue()) {
				List<AccessInfo> accessInfoList = AccessUtil.getAccessInfoList(
						viewPart.getSite().getShell(), management.getFacilityId(), management.getOwnerRoleId(), managerName, infraManagementView.isUseNodeProp());
				// ユーザ、パスワード、ポートの入力画面でキャンセルをクリックすると、nullが返ってくる。
				// その場合は、処理中断。
				if (accessInfoList == null) {
					continue;
				}
				List<String> moduleIdList = new ArrayList<String>();
				for (InfraModuleInfo info : management.getModuleList()) {
					moduleIdList.add(info.getModuleId());
				}
				String managementId = management.getManagementId();

				try {
					InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
					String sessionId = wrapper.createSession(managementId, moduleIdList, accessInfoList);

					while (true) {
						ModuleResult moduleResult = wrapper.runInfraModule(sessionId);
						if (!allRun && !ModuleUtil.displayResult(moduleResult.getModuleId(), moduleResult)) {
							break;
						}
						if(!moduleResult.isHasNext()) {
							break;
						}
					}
					wrapper.deleteSession(sessionId);
					MessageDialog.openInformation(null, Messages.getString("message"), Messages.getString("message.infra.management.run.end"));
				} catch (InvalidRole_Exception e) {
					// 権限なし
					errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
					continue;
				} catch (HinemosUnknown_Exception | InvalidUserPass_Exception |
						InfraManagementNotFound_Exception | InfraModuleNotFound_Exception | SessionNotFound_Exception | FacilityNotFound_Exception | InvalidSetting_Exception e) {
					m_log.error("execute() :  " + e.getClass().getName() + ", " + e.getMessage());
					Object[] arg = new Object[]{Messages.getString("infra.module.id"), 
							Messages.getString("infra.module.run"), Messages.getString("failed"), HinemosMessage.replace(e.getMessage())};
					errorMsgs.put(managerName, Messages.getString("message.infra.action.result", arg));
					continue;
				} catch (Exception e) {
					m_log.error("execute() :  " + e.getClass().getName() + ", " + e.getMessage());
					Object[] arg = new Object[]{Messages.getString("infra.module.id"), 
							Messages.getString("infra.module.run"), Messages.getString("failed"), HinemosMessage.replace(e.getMessage())};
					errorMsgs.put(managerName, Messages.getString("message.infra.action.result", arg));
					continue;
				}
			}
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		infraManagementView.update();
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		boolean enable = false;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				if(part instanceof InfraManagementView){
					InfraManagementView view = (InfraManagementView) part.getAdapter(InfraManagementView.class);
					if (view == null) {
						m_log.info("execute: view is null");
						return;
					}
					// Enable button when 1 item is selected
					StructuredSelection selection = null;
					if(view.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
						selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();
					}
					if(selection != null && selection.size() > 0){
						enable = true;
					}
				}
				this.setBaseEnabled(enable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}
}
