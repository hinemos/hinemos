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
import org.openapitools.client.model.CreateAccessInfoListForDialogResponse;
import org.openapitools.client.model.CreateSessionRequest;
import org.openapitools.client.model.InfraManagementInfoResponse;
import org.openapitools.client.model.InfraSessionResponse;
import org.openapitools.client.model.ModuleResultResponse;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InfraModuleNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.SessionNotFound;
import com.clustercontrol.infra.action.GetInfraModuleTableDefine;
import com.clustercontrol.infra.bean.InfraNodeInputConstant;
import com.clustercontrol.infra.dialog.RunDialog;
import com.clustercontrol.infra.util.AccessUtil;
import com.clustercontrol.infra.util.InfraDtoConverter;
import com.clustercontrol.infra.util.InfraRestClientWrapper;
import com.clustercontrol.infra.util.ModuleUtil;
import com.clustercontrol.infra.view.InfraModuleView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

public class CheckInfraModuleAction extends AbstractHandler implements IElementUpdater {
	// ログ
	private static Log m_log = LogFactory.getLog( CheckInfraModuleAction.class );

	/** アクションID */
	public static final String ID = CheckInfraModuleAction.class.getName();

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

		InfraManagementInfoResponse management = null;
		String managerName = infraModuleView.getComposite().getManagerName();

		try {
			InfraRestClientWrapper wrapper = InfraRestClientWrapper.getWrapper(managerName);
			management = wrapper.getInfraManagement(infraModuleView.getComposite().getManagementId());
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.accesscontrol.16"));
			return null;
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InfraManagementNotFound | InvalidSetting e) {
			m_log.error("execute() : " + e.getClass().getName() + ", " + e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module.id"),
							Messages.getString("infra.module.check"), Messages.getString("failed"), HinemosMessage.replace(e.getMessage())}));
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
						Messages.getString("infra.module.check"), moduleId}));
		if (dialog.open() == IDialogConstants.CANCEL_ID) {
			return null;
		}
		allRun = dialog.isAllRun();

		List<CreateAccessInfoListForDialogResponse> accessInfoList = null;
		List<String> moduleIdList = new ArrayList<String>();
		moduleIdList.add(moduleId);
		if (infraModuleView.getNodeInputType() == InfraNodeInputConstant.TYPE_DIALOG) {
			accessInfoList = AccessUtil.getAccessInfoList(
				viewPart.getSite().getShell(), management, moduleIdList, managerName);
			// ユーザ、パスワード、ポートの入力画面でキャンセルをクリックすると、nullが返ってくる。
			// その場合は、処理中断。
			if (accessInfoList == null) {
				return null;
			}
		}
		
		InfraRestClientWrapper wrapper = null;
		InfraSessionResponse session = null;
		try {
			wrapper = InfraRestClientWrapper.getWrapper(managerName);
			CreateSessionRequest dtoReq = InfraDtoConverter.getCreateSessionRequest(management.getManagementId(), 
					moduleIdList, infraModuleView.getNodeInputType(), accessInfoList);
			session = wrapper.createSession(dtoReq);
			while (true) {
				ModuleResultResponse moduleResult = wrapper.checkInfraModule(session.getSessionId(), !allRun);
				if (!allRun && !ModuleUtil.displayResult(moduleResult.getModuleId(), moduleResult)) {
					break;
				}
				if(!moduleResult.getHasNext()) {
					break;
				}
			}
			MessageDialog.openInformation(null, Messages.getString("message"), Messages.getString("message.infra.management.check.end"));
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.accesscontrol.16"));
			return null;
		} catch (HinemosUnknown | InvalidUserPass | InfraManagementNotFound | InfraModuleNotFound |
				SessionNotFound | FacilityNotFound | InvalidSetting e) {
			m_log.error("execute() :  " + e.getClass().getName() + ", " + e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module.id"), 
					Messages.getString("infra.module.check"), Messages.getString("failed"), HinemosMessage.replace(e.getMessage())}));
			return null;
		} catch (Exception e) {
			m_log.error("execute() :  " + e.getClass().getName() + ", " + e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module.id"), 
					Messages.getString("infra.module.check"), Messages.getString("failed"), HinemosMessage.replace(e.getMessage())}));
			return null;
		} finally {
			if (session != null) {
				try {
					wrapper.deleteSession(session.getSessionId());
				} catch (InvalidRole e) {
					// 権限なし
					MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.accesscontrol.16"));
					return null;
				} catch (RestConnectFailed | SessionNotFound | InfraManagementNotFound | InvalidUserPass | HinemosUnknown e) {
					m_log.error("execute() :  " + e.getClass().getName() + ", " + e.getMessage());
					MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module.id"), 
							Messages.getString("infra.module.check"), Messages.getString("failed"), HinemosMessage.replace(e.getMessage())}));
					return null;
				} catch (Exception e) {
					m_log.error("execute() :  " + e.getClass().getName() + ", " + e.getMessage());
					MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module.id"), 
							Messages.getString("infra.module.check"), Messages.getString("failed"), HinemosMessage.replace(e.getMessage())}));
					return null;
				}
			}
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