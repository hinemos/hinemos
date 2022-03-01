/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import org.openapitools.client.model.CommandModuleInfoResponse;
import org.openapitools.client.model.FileTransferModuleInfoResponse;
import org.openapitools.client.model.InfraManagementInfoResponse;
import org.openapitools.client.model.ModifyInfraManagementRequest;
import org.openapitools.client.model.ReferManagementModuleInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementDuplicate;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.infra.action.GetInfraModuleTableDefine;
import com.clustercontrol.infra.util.InfraDtoConverter;
import com.clustercontrol.infra.util.InfraRestClientWrapper;
import com.clustercontrol.infra.view.InfraModuleView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

public class EnableInfraModuleAction extends AbstractHandler  implements IElementUpdater {
	// ログ
	private static Log m_log = LogFactory.getLog( EnableInfraModuleAction.class );

	/** アクションID */
	public static final String ID = EnableInfraModuleAction.class.getName();

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

		if(infraModuleView == null) {
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
				Messages.getString("message.infra.confirm.action", new Object[]{Messages.getString("infra.module.id"), Messages.getString("infra.enable.setting"), strModuleIds})) == false)
		{
			return null;
		}

		InfraRestClientWrapper wrapper = InfraRestClientWrapper.getWrapper(managerName);
		try {
			InfraManagementInfoResponse info = wrapper.getInfraManagement(infraModuleView.getComposite().getManagementId());
			for (String moduleId : moduleIds) {
				for (CommandModuleInfoResponse module : info.getCommandModuleInfoList()) {
					if (module.getModuleId().equals(moduleId)) {
						module.setValidFlg(true);
						break;
					}
				}
				for (FileTransferModuleInfoResponse module : info.getFileTransferModuleInfoList()) {
					if (module.getModuleId().equals(moduleId)) {
						module.setValidFlg(true);
						break;
					}
				}
				for (ReferManagementModuleInfoResponse module : info.getReferManagementModuleInfoList()) {
					if (module.getModuleId().equals(moduleId)) {
						module.setValidFlg(true);
						break;
					}
				}
			}

			try {
				String managementId = info.getManagementId();
				ModifyInfraManagementRequest dtoReq = new ModifyInfraManagementRequest();
				RestClientBeanUtil.convertBean(info, dtoReq);
				InfraDtoConverter.convertInfoToDto(info, dtoReq);
				wrapper.modifyInfraManagement(managementId, dtoReq);
			} catch (InvalidRole e) {
				// 権限なし
				MessageDialog.openError(null, Messages.getString("failed"), 
						Messages.getString("message.accesscontrol.16") + "(" + managerName + ")");
				return null;
			} catch (RestConnectFailed | NotifyDuplicate | NotifyNotFound | HinemosUnknown | InvalidUserPass | 
					InvalidSetting | InfraManagementNotFound | InfraManagementDuplicate e) {
				m_log.debug("execute modifyInfraManagement, " + e.getMessage());
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.infra.action.result", 
								new Object[]{
										Messages.getString("infra.module.id"), 
										Messages.getString("infra.enable.setting"), 
										Messages.getString("failed"), 
										managerName + " : " + strModuleIds}));
				return null;
			}
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InfraManagementNotFound
				| InvalidSetting e) {
			m_log.debug("execute getInfraManagement, " + e.getMessage());
		}

		MessageDialog.openInformation(
				null,
				Messages.getString("successful"),
				Messages.getString(
						"message.infra.action.result",
						new Object[] {
								Messages.getString("infra.module.id"),
								Messages.getString("infra.enable.setting") + "(" + managerName + ")",
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