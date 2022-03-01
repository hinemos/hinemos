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
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

public class DeleteInfraModuleAction extends AbstractHandler implements IElementUpdater {
	// ログ
	private static Log m_log = LogFactory.getLog( DeleteInfraModuleAction.class );

	/** アクションID */
	public static final String ID = DeleteInfraModuleAction.class.getName();

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

		if (infraModuleView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		StructuredSelection selection = null;
		if(infraModuleView.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) infraModuleView.getComposite().getTableViewer().getSelection();
		}

		List<String> moduleIds = new ArrayList<String>();
		StringBuffer strModuleIds = new StringBuffer();
		String tmpModuleId = null;
		if(selection != null){
			for(Object object: selection.toList()){
				tmpModuleId = (String) ((ArrayList<?>)object).get(GetInfraModuleTableDefine.MODULE_ID);
				moduleIds.add(tmpModuleId);
				strModuleIds.append(tmpModuleId + ", ");
			}
			strModuleIds.setLength(strModuleIds.length() - 2);
		}

		InfraManagementInfoResponse info = null;
		String managerName = infraModuleView.getComposite().getManagerName();
		try {
			InfraRestClientWrapper wrapper = InfraRestClientWrapper
					.getWrapper(managerName);
			info = wrapper.getInfraManagement(infraModuleView.getComposite().getManagementId());
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InfraManagementNotFound
				| InvalidSetting e) {
			m_log.debug("execute getInfraManagement, " + e.getMessage());
		}

		List<CommandModuleInfoResponse> cmdList = null;
		if (info != null) { // findbugs対応 nullチェック追加
			cmdList = info.getCommandModuleInfoList();
		}
		List<CommandModuleInfoResponse> cmdDeleteList = new ArrayList<>();
		if(info != null && cmdList != null){
			for(String moduleId: moduleIds){
				for(CommandModuleInfoResponse module: cmdList){
					if(module.getModuleId().equals(moduleId)){
						cmdDeleteList.add(module);
						break;
					}
				}
			}
			cmdList.removeAll(cmdDeleteList);
		}
		
		List<FileTransferModuleInfoResponse> fileList = null;
		if (info != null) { // findbugs対応 nullチェック追加
			fileList = info.getFileTransferModuleInfoList();
		}
		List<FileTransferModuleInfoResponse> fileDeleteList = new ArrayList<>();
		if(info != null && fileList != null){
			for(String moduleId: moduleIds){
				for(FileTransferModuleInfoResponse module: fileList){
					if(module.getModuleId().equals(moduleId)){
						fileDeleteList.add(module);
						break;
					}
				}
			}
			fileList.removeAll(fileDeleteList);
		}
		
		List<ReferManagementModuleInfoResponse> referList = null;
		if (info != null) { // findbugs対応 nullチェック追加
			referList = info.getReferManagementModuleInfoList();
		}
		List<ReferManagementModuleInfoResponse> referDeleteList = new ArrayList<>();
		if(info != null && fileList != null){
			for(String moduleId: moduleIds){
				for(ReferManagementModuleInfoResponse module: referList){
					if(module.getModuleId().equals(moduleId)){
						referDeleteList.add(module);
						break;
					}
				}
			}
			referList.removeAll(referDeleteList);
		}
		
		if(!cmdDeleteList.isEmpty() || !fileDeleteList.isEmpty() || !referDeleteList.isEmpty()) {
			if (MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString("message.infra.confirm.action", new Object[]{Messages.getString("infra.module"), Messages.getString("delete"), strModuleIds})))
			{
				try {
					InfraRestClientWrapper wrapper = InfraRestClientWrapper
							.getWrapper(managerName);
					String managementId = info.getManagementId();
					ModifyInfraManagementRequest dtoReq = new ModifyInfraManagementRequest();
					RestClientBeanUtil.convertBean(info, dtoReq);
					InfraDtoConverter.convertInfoToDto(info, dtoReq);
					wrapper.modifyInfraManagement(managementId, dtoReq);
					MessageDialog.openInformation(null, Messages
							.getString("successful"), Messages.getString(
									"message.infra.action.result",
									new Object[] {
											Messages.getString("infra.module"),
											Messages.getString("delete") + "(" + managerName +")",
											Messages.getString("successful"),
											strModuleIds }));
				} catch (InvalidRole e) {
					// 権限なし
					MessageDialog.openError(null, Messages.getString("failed"), 
							Messages.getString("message.accesscontrol.16") + "(" + managerName +")");
					return null;
				} catch (RestConnectFailed | NotifyDuplicate | NotifyNotFound | HinemosUnknown | InvalidUserPass
						| InvalidSetting | InfraManagementNotFound | InfraManagementDuplicate e) {
					m_log.debug("execute modifyInfraManagement, " + e.getMessage());
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.infra.action.result", 
									new Object[]{
											Messages.getString("infra.module"), 
											Messages.getString("delete"), 
											Messages.getString("failed"), 
											managerName + " : " + HinemosMessage.replace(e.getMessage())}));
					return null;
				}

				infraModuleView.update(infraModuleView.getComposite().getManagerName(), info.getManagementId());
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

	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

}
