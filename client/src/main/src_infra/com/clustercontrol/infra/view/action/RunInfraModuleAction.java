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

import com.clustercontrol.infra.action.GetInfraModuleTableDefine;
import com.clustercontrol.infra.bean.InfraNodeInputConstant;
import com.clustercontrol.infra.dialog.RunDialog;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.infra.util.AccessUtil;
import com.clustercontrol.infra.util.ModuleUtil;
import com.clustercontrol.infra.view.InfraModuleView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
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

public class RunInfraModuleAction extends AbstractHandler implements IElementUpdater {
	// ログ
	private static Log m_log = LogFactory.getLog( RunInfraModuleAction.class );

	/** アクションID */
	public static final String ID = RunInfraModuleAction.class.getName();
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
		if(!(viewPart instanceof InfraModuleView)) {
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

		InfraManagementInfo management = null;
		String managerName = infraModuleView.getComposite().getManagerName();
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
							Messages.getString("infra.module.run"), Messages.getString("failed"), HinemosMessage.replace(e.getMessage())}));
			return null;
		}

		if(selection == null || management == null){
			return null;
		}
		String moduleId = (String) ((ArrayList<?>)selection.getFirstElement()).get(GetInfraModuleTableDefine.MODULE_ID);

		boolean allRun = false;
		RunDialog dialog = new RunDialog(null, 
				Messages.getString("message.infra.confirm.action",
						new Object[]{Messages.getString("infra.module.id"),
						Messages.getString("infra.module.run"), moduleId}));
		if (dialog.open() == IDialogConstants.CANCEL_ID) {
			return null;
		}
		allRun = dialog.isAllRun();

		// moduleの取得
		InfraModuleInfo module = null;
		for (InfraModuleInfo m : management.getModuleList()) {
			if (moduleId.equals(m.getModuleId())) {
				module = m;
				break;
			}
		}
		if (module == null) {
			return null;
		}

		List<String> moduleIdList = new ArrayList<String>();
		moduleIdList.add(moduleId);

		List<AccessInfo> accessInfoList = null;
		if (infraModuleView.getNodeInputType() == InfraNodeInputConstant.TYPE_DIALOG) {
			accessInfoList = AccessUtil.getAccessInfoList(
				viewPart.getSite().getShell(), management, moduleIdList, managerName);
			// ユーザ、パスワード、ポートの入力画面でキャンセルをクリックすると、nullが返ってくる。
			// その場合は、処理中断。
			if (accessInfoList == null) {
				return null;
			}
		}
		try {
			InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
			String sessionId = wrapper.createSession(management.getManagementId(), moduleIdList, infraModuleView.getNodeInputType(), accessInfoList);
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
			MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.accesscontrol.16"));
			return null;
		} catch (HinemosUnknown_Exception | InvalidUserPass_Exception |
				InfraManagementNotFound_Exception | InfraModuleNotFound_Exception | SessionNotFound_Exception | FacilityNotFound_Exception | InvalidSetting_Exception e) {
			m_log.error("execute() :  " + e.getClass().getName() + ", " + e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module.id"), 
					Messages.getString("infra.module.run"), Messages.getString("failed"), HinemosMessage.replace(e.getMessage())}));
			return null;
		} catch (Exception e) {
			m_log.error("execute() :  " + e.getClass().getName() + ", " + e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module.id"), 
					Messages.getString("infra.module.run"), Messages.getString("failed"), HinemosMessage.replace(e.getMessage())}));
			return null;
		}

		infraModuleView.update(infraModuleView.getComposite().getManagerName(), infraModuleView.getComposite().getManagementId());
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
