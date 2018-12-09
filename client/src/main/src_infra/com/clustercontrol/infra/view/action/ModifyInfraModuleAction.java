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

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.infra.action.GetInfraModuleTableDefine;
import com.clustercontrol.infra.dialog.CommandModuleDialog;
import com.clustercontrol.infra.dialog.FileTransferModuleDialog;
import com.clustercontrol.infra.dialog.ReferManagementModuleDialog;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.infra.view.InfraModuleView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.infra.CommandModuleInfo;
import com.clustercontrol.ws.infra.FileTransferModuleInfo;
import com.clustercontrol.ws.infra.HinemosUnknown_Exception;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.clustercontrol.ws.infra.InfraManagementNotFound_Exception;
import com.clustercontrol.ws.infra.InfraModuleInfo;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;
import com.clustercontrol.ws.infra.NotifyNotFound_Exception;
import com.clustercontrol.ws.infra.ReferManagementModuleInfo;

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
		InfraManagementInfo info = null;
		try {
			InfraEndpointWrapper wrapper = InfraEndpointWrapper
					.getWrapper(infraModuleView.getComposite().getManagerName());
			info = wrapper.getInfraManagement(infraModuleView.getComposite().getManagementId());
		} catch (InvalidRole_Exception e) {
			// 権限なし
			MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.accesscontrol.16"));
			return null;
		} catch (HinemosUnknown_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception | InfraManagementNotFound_Exception e) {
			m_log.debug("execute getInfraManagement, " + e.getMessage());
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					HinemosMessage.replace(e.getMessage()));
			return null;
		}

		InfraModuleInfo module = null;

		if(info != null && info.getModuleList() != null){
			for(InfraModuleInfo tmpModule: info.getModuleList()){
				if(tmpModule.getModuleId().equals(moduleId)){
					module = tmpModule;
					break;
				}
			}

			CommonDialog dialog = null;
			if(moduleId != null){
				if(module instanceof CommandModuleInfo){
					dialog = new CommandModuleDialog(infraModuleView.getSite().getShell(), managerName, managementId, moduleId, PropertyDefineConstant.MODE_MODIFY);
				} else if (module instanceof FileTransferModuleInfo) {
					dialog = new FileTransferModuleDialog(infraModuleView.getSite().getShell(), managerName, managementId, moduleId, PropertyDefineConstant.MODE_MODIFY);
				} else if (module instanceof ReferManagementModuleInfo) {
					dialog = new ReferManagementModuleDialog(infraModuleView.getSite().getShell(), managerName, managementId, moduleId, PropertyDefineConstant.MODE_MODIFY);
				} else {
					throw new InternalError("dialog is null");
				}
			}
			if (dialog != null)
				dialog.open();

			infraModuleView.update(managerName, managementId);
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
