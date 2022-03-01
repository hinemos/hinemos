/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.view.action;

import java.util.ArrayList;
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
import org.openapitools.client.model.ReferManagementModuleInfoResponse;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.infra.action.GetInfraModuleTableDefine;
import com.clustercontrol.infra.dialog.CommandModuleDialog;
import com.clustercontrol.infra.dialog.FileTransferModuleDialog;
import com.clustercontrol.infra.dialog.ReferManagementModuleDialog;
import com.clustercontrol.infra.util.InfraRestClientWrapper;
import com.clustercontrol.infra.view.InfraModuleView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

public class ModifyInfraModuleAction extends AbstractHandler implements IElementUpdater {
	// ログ
	private static Log m_log = LogFactory.getLog( ModifyInfraModuleAction.class );

	/** アクションID */
	public static final String ID = ModifyInfraModuleAction.class.getName();

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

		if ( infraModuleView == null ) {
			m_log.info("execute: view is null");
			return null;
		}

		StructuredSelection selection = null;

		String managerName = null;
		if(infraModuleView.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) infraModuleView.getComposite().getTableViewer().getSelection();
			managerName = infraModuleView.getComposite().getManagerName();
		}

		String moduleId = null;
		if(selection != null){
			moduleId = (String) ((ArrayList<?>)selection.getFirstElement()).get(GetInfraModuleTableDefine.MODULE_ID);
		}

		String managementId = infraModuleView.getComposite().getManagementId();
		InfraManagementInfoResponse info = null;
		try {
			InfraRestClientWrapper wrapper = InfraRestClientWrapper
					.getWrapper(infraModuleView.getComposite().getManagerName());
			info = wrapper.getInfraManagement(infraModuleView.getComposite().getManagementId());
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.accesscontrol.16"));
			return null;
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InfraManagementNotFound
				| InvalidSetting e) {
			m_log.debug("execute getInfraManagement, " + e.getMessage());
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					HinemosMessage.replace(e.getMessage()));
			return null;
		}

		boolean moduleIsCmd = false;
		boolean moduleIsFile = false;
		boolean moduleIsRefer = false;
		if(info != null) {
			if(info.getCommandModuleInfoList() != null) {
				for(CommandModuleInfoResponse tmpModule: info.getCommandModuleInfoList()) {
					if(tmpModule.getModuleId().equals(moduleId)){
						moduleIsCmd = true;
						break;
					}
				}
			}
			if(info.getFileTransferModuleInfoList() != null) {
				for(FileTransferModuleInfoResponse tmpModule: info.getFileTransferModuleInfoList()) {
					if(tmpModule.getModuleId().equals(moduleId)){
						moduleIsFile = true;
						break;
					}
				}
			}
			if(info.getReferManagementModuleInfoList() != null) {
				for(ReferManagementModuleInfoResponse tmpModule: info.getReferManagementModuleInfoList()) {
					if(tmpModule.getModuleId().equals(moduleId)){
						moduleIsRefer = true;
						break;
					}
				}
			}
			
			CommonDialog dialog = null;
			if(moduleId != null) {
				if(moduleIsCmd) {
					dialog = new CommandModuleDialog(infraModuleView.getSite().getShell(), managerName, managementId, moduleId, PropertyDefineConstant.MODE_MODIFY);
				} else if (moduleIsFile) {
					dialog = new FileTransferModuleDialog(infraModuleView.getSite().getShell(), managerName, managementId, moduleId, PropertyDefineConstant.MODE_MODIFY);
				} else if (moduleIsRefer) {
					dialog = new ReferManagementModuleDialog(infraModuleView.getSite().getShell(), managerName, managementId, moduleId, PropertyDefineConstant.MODE_MODIFY);
				} else {
					throw new InternalError("dialog is null");
				}
			}
			
			if (dialog != null)
				dialog.open();
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
					if(selection != null && selection.size() == 1){
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
