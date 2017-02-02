/**********************************************************************
 * Copyright (C) 2015 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package com.clustercontrol.infra.view.action;

import java.util.ArrayList;
import java.util.Collections;
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

import com.clustercontrol.dialog.TextAreaDialog;
import com.clustercontrol.infra.action.GetInfraModuleTableDefine;
import com.clustercontrol.infra.bean.OkNgConstant;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.infra.view.InfraModuleView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.infra.HinemosUnknown_Exception;
import com.clustercontrol.ws.infra.InfraCheckResult;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.clustercontrol.ws.infra.InfraManagementNotFound_Exception;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;
import com.clustercontrol.ws.infra.NotifyNotFound_Exception;

/**
 * [チェック状態ダ]イアログを表示
 *
 * @version 5.0.0
 * @since 5.0.0
 *
 */
public class ReviewCheckStatusAction extends AbstractHandler implements IElementUpdater {
	// ログ
	private static Log m_log = LogFactory.getLog( ReviewCheckStatusAction.class );

	/** アクションID */
	public static final String ID = ReviewCheckStatusAction.class.getName();

	/** dispose*/
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * 
	 * @see InfraModuleComposite#getStatusString(String, String)
	 */
	private String getStatusString( String managerName, String managementId, String moduleId ) {
		List<InfraCheckResult> allResultList = null;
		try {
			InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
			allResultList = wrapper.getCheckResultList(managementId);
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
			m_log.error("getStatusString() getCheckResultList, " + e.getMessage());
		}
		if(allResultList == null){
			return null;
		}

		List<InfraCheckResult> resultList = new ArrayList<>();
		for (InfraCheckResult result : allResultList) {
			if( moduleId.equals(result.getModuleId()) ){
				resultList.add( result );
			}
		}

		List<String> okList = new ArrayList<>();
		List<String> ngList = new ArrayList<>();
		String newline = System.getProperty("line.separator");
		for (InfraCheckResult result : resultList) {
			if(result.getResult() == OkNgConstant.TYPE_OK){
				okList.add(result.getNodeId());
			} else if (result.getResult() == OkNgConstant.TYPE_NG){
				ngList.add(result.getNodeId());
			} else {
				m_log.warn("getStatusString : " + result.getNodeId() + ", " + result.getResult()); // ここには到達しないはず。
			}
		}
		Collections.sort(okList);
		Collections.sort(ngList);
		StringBuilder message = new StringBuilder();
		message.append("### NG (" + ngList.size()+ ") ###" + newline);
		for (String nodeId : ngList) {
			message.append(nodeId + newline);
		}
		message.append(newline);
		message.append("### OK (" + okList.size()+ ") ###" + newline);
		for (String nodeId : okList) {
			message.append(nodeId + newline);
		}
		return message.toString();
	}

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

		if (infraModuleView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		StructuredSelection selection = null;
		if(infraModuleView.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) infraModuleView.getComposite().getTableViewer().getSelection();
		}

		String managerName = infraModuleView.getComposite().getManagerName();
		InfraManagementInfo management = null;

		try {
			InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
			management = wrapper.getInfraManagement(infraModuleView.getComposite().getManagementId());
		} catch (InvalidRole_Exception e) {
			// 権限なし
			MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.accesscontrol.16"));
			return null;
		} catch (HinemosUnknown_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception | InfraManagementNotFound_Exception e) {
			m_log.error("execute() : " + e.getClass().getName() + ", " + e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module.id"),
							Messages.getString("infra.module.check"), Messages.getString("failed"), HinemosMessage.replace(e.getMessage())}));
			return null;
		}

		if( null == selection || null == management ){
			return null;
		}

		String moduleId = (String) ((ArrayList<?>)selection.getFirstElement()).get(GetInfraModuleTableDefine.MODULE_ID);
		if( null == moduleId ){
			return null;
		}

		TextAreaDialog dialog = new TextAreaDialog(null, Messages.getString("infra.management.check.state"), false, false); 
		dialog.setText(getStatusString( managerName, management.getManagementId(), moduleId ));
		dialog.setCancelButtonText(Messages.getString("close"));
		dialog.open();

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
					if(selection != null && 1 == selection.size() ){
						// Only enable when result exists (= String length > 0 )
						String result = (String) ((ArrayList<?>)selection.getFirstElement()).get(GetInfraModuleTableDefine.CHECK_CONDITION);
						if( null != result ){
							enable = true;
						}
					}
				}
				this.setBaseEnabled(enable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}

	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

}
