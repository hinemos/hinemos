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
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.infra.view.InfraManagementView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.infra.HinemosUnknown_Exception;
import com.clustercontrol.ws.infra.InfraManagementNotFound_Exception;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;
import com.clustercontrol.ws.infra.NotifyNotFound_Exception;

public class DeleteInfraManagementAction extends AbstractHandler implements IElementUpdater {
	// ログ
	private static Log m_log = LogFactory.getLog( DeleteInfraManagementAction.class );

	/** アクションID */
	public static final String ID = DeleteInfraManagementAction.class.getName();
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

		InfraManagementView view = (InfraManagementView) viewPart;

		StructuredSelection selection = null;
		if(view.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();
		}

		StringBuffer strManagementIds = new StringBuffer();
		String tmpManagementId = null;
		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
		if(selection != null){
			for(Object object: selection.toList()){
				String managerName = (String) ((ArrayList<?>)object).get(GetInfraManagementTableDefine.MANAGER_NAME);
				if(map.get(managerName) == null) {
					map.put(managerName, new ArrayList<String>());
				}
			}

			for(Object object: selection.toList()){
				String managerName = (String) ((ArrayList<?>)object).get(GetInfraManagementTableDefine.MANAGER_NAME);
				tmpManagementId = (String) ((ArrayList<?>)object).get(GetInfraManagementTableDefine.MANAGEMENT_ID);
				map.get(managerName).add(tmpManagementId);
				strManagementIds.append(tmpManagementId + ", ");

			}
			strManagementIds.setLength(strManagementIds.length() - 2);

			if (MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString("message.infra.confirm.action",
							new Object[]{Messages.getString("infra.management.id"), Messages.getString("delete"), strManagementIds})))
			{
				Map<String, String> errMsg = new ConcurrentHashMap<String, String>();
				for(Map.Entry<String, List<String>> entry : map.entrySet()) {
					String managerName = entry.getKey();
					InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
					List<String> managementIds = entry.getValue();
					try {
						wrapper.deleteInfraManagement(managementIds);
					} catch (InvalidRole_Exception e){
						// 権限なし
						errMsg.put(managerName, Messages.getString("message.accesscontrol.16"));
					} catch (InfraManagementNotFound_Exception | HinemosUnknown_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception e) {
						m_log.debug("execute() : " + e.getClass() + e.getMessage());
						String arg = Messages.getString(
								"message.infra.action.result",
								new Object[] {
										Messages.getString("infra.management.id"),
										Messages.getString("delete"),
										Messages.getString("failed"),
										HinemosMessage.replace(e.getMessage()) });
						errMsg.put(managerName, arg);
					}
				}

				if(errMsg.isEmpty()) {
					MessageDialog.openInformation(null,
							Messages.getString("confirmed"),
							Messages.getString("message.infra.action.result",
									new Object[]{Messages.getString("infra.management.id"),
											Messages.getString("delete"),
											Messages.getString("successful"), strManagementIds}));
				} else {
					UIManager.showMessageBox(errMsg, true);
				}
				view.update();
			}
		}
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
					InfraManagementView view = (InfraManagementView) part;
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
