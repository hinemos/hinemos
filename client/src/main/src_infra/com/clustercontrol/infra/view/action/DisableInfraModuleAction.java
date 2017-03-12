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

import com.clustercontrol.infra.action.GetInfraModuleTableDefine;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.infra.view.InfraModuleView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.infra.HinemosUnknown_Exception;
import com.clustercontrol.ws.infra.InfraManagementDuplicate_Exception;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.clustercontrol.ws.infra.InfraManagementNotFound_Exception;
import com.clustercontrol.ws.infra.InfraModuleInfo;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidSetting_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;
import com.clustercontrol.ws.infra.NotifyDuplicate_Exception;
import com.clustercontrol.ws.infra.NotifyNotFound_Exception;

public class DisableInfraModuleAction extends AbstractHandler  implements IElementUpdater {
	// ログ
	private static Log m_log = LogFactory.getLog( DisableInfraModuleAction.class );

	/** アクションID */
	public static final String ID = DisableInfraModuleAction.class.getName();

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
		if (!(viewPart instanceof InfraModuleView)) {
			return null;
		}
		
		InfraModuleView infraModuleView = null;
		try {
			infraModuleView = (InfraModuleView) viewPart.getAdapter(InfraModuleView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if( infraModuleView == null ){
			m_log.info("execute: view is null"); 
			return null;
		}

		StructuredSelection selection = null;
		if(infraModuleView.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) infraModuleView.getComposite().getTableViewer().getSelection();
		}

		StringBuffer strModuleIds = new StringBuffer();
		List<String> moduleIds = new ArrayList<>();

		if(selection != null){

			for(Object object: selection.toList()){
				String tmpModuleId = (String) ((ArrayList<?>)object).get(GetInfraModuleTableDefine.MODULE_ID);
				moduleIds.add(tmpModuleId);
				strModuleIds.append(tmpModuleId + ", ");
			}
			strModuleIds.setLength(strModuleIds.length() - 2);
		}

		String managerName = infraModuleView.getComposite().getManagerName();
		if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.infra.confirm.action", new Object[]{Messages.getString("infra.module.id"), Messages.getString("infra.disable.setting"), strModuleIds})) == false)
		{
			return null;
		}

		InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
		try {
			InfraManagementInfo info = wrapper.getInfraManagement(infraModuleView.getComposite().getManagementId());
			for (String moduleId : moduleIds) {
				for (InfraModuleInfo module : info.getModuleList()) {
					if (module.getModuleId().equals(moduleId)) {
						module.setValidFlg(false);
						break;
					}
				}
			}

			try {
				wrapper.modifyInfraManagement(info);
			} catch (InvalidRole_Exception e) {
				// 権限なし
				MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.accesscontrol.16"));
				return null;
			} catch (InvalidSetting_Exception | NotifyDuplicate_Exception | HinemosUnknown_Exception | InvalidUserPass_Exception | InfraManagementNotFound_Exception | InfraManagementDuplicate_Exception e) {
				m_log.debug("execute modifyInfraManagement, " + e.getMessage());
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module.id"), Messages.getString("infra.disable.setting"), Messages.getString("failed"), strModuleIds}));
				return null;
			}
		} catch (InvalidRole_Exception e){
			MessageDialog.openError(null, Messages.getString("failed"),  Messages.getString("message.accesscontrol.16"));
		} catch (HinemosUnknown_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception | InfraManagementNotFound_Exception e) {
			m_log.debug("execute getInfraManagement, " + e.getMessage());
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					HinemosMessage.replace(e.getMessage()));

		}

		MessageDialog.openInformation(
				null,
				Messages.getString("successful"),
				Messages.getString(
						"message.infra.action.result",
						new Object[] {
								Messages.getString("infra.module.id"),
								Messages.getString("infra.disable.setting") + "(" + managerName +")",
								Messages.getString("successful"),
								strModuleIds }));
		infraModuleView.update(infraModuleView.getComposite().getManagerName(), infraModuleView.getComposite().getManagementId());
		return null;
	}

	/**
 * Dispose
 */
@Override
public void dispose() {
	this.viewPart = null;
	this.window = null;
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

				if(part instanceof InfraModuleView){
					InfraModuleView view = (InfraModuleView) part.getAdapter(InfraModuleView.class);
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
}