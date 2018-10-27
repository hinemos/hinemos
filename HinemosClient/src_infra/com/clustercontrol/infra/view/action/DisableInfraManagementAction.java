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
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.infra.HinemosUnknown_Exception;
import com.clustercontrol.ws.infra.InfraManagementDuplicate_Exception;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.clustercontrol.ws.infra.InfraManagementNotFound_Exception;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidSetting_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;
import com.clustercontrol.ws.infra.NotifyDuplicate_Exception;
import com.clustercontrol.ws.infra.NotifyNotFound_Exception;

public class DisableInfraManagementAction extends AbstractHandler  implements IElementUpdater {
	// ログ
	private static Log m_log = LogFactory.getLog( DisableInfraManagementAction.class );

	/** アクションID */
	public static final String ID = DisableInfraManagementAction.class.getName();

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
		if (!(viewPart instanceof InfraManagementView)) {
			return null;
		}

		InfraManagementView infraManagementView = null;
		try {
			infraManagementView = (InfraManagementView) viewPart.getAdapter(InfraManagementView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}
		if(infraManagementView == null){
			m_log.info("execute: view is null"); 
			return null;
		}

		StructuredSelection selection = null;
		if(infraManagementView.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) infraManagementView.getComposite().getTableViewer().getSelection();
		}

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

		StringBuffer idbuf = new StringBuffer();
		int size = 0;
		for (Object obj : sList) {
			List<?> list = (List<?>)obj;
			String managementId = null;
			String managerName = null;
			if (list != null) {
				managementId = (String) list.get(GetInfraManagementTableDefine.MANAGEMENT_ID);
				managerName = (String) list.get(GetInfraManagementTableDefine.MANAGER_NAME);
				managementIdMap.get(managerName).add(managementId);
				if(size > 0) {
					idbuf.append(", ");
				}
				idbuf.append(managementId);
				size++;
			}
		}

		if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.infra.confirm.action",
						new Object[]{Messages.getString("infra.management.id"),
								Messages.getString("infra.disable.setting"), idbuf.toString()})) == false)
		{
			return null;
		}

		StringBuffer sucManagementIds = new StringBuffer();
		StringBuffer failManagementIds = new StringBuffer();

		for(Map.Entry<String, List<String>> entry : managementIdMap.entrySet()) {
			String managerName = entry.getKey();
			InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
			for (String managementId : entry.getValue()) {
				try {
					InfraManagementInfo info = wrapper.getInfraManagement(managementId);
					info.setValidFlg(false);
					try {
						wrapper.modifyInfraManagement(info);
						sucManagementIds.append(managementId + "("+ managerName + "), ");
					} catch (InvalidSetting_Exception | NotifyDuplicate_Exception | HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | InfraManagementNotFound_Exception | InfraManagementDuplicate_Exception e) {
						m_log.debug("execute modifyInfraManagement, " + e.getMessage());
						failManagementIds.append(managementId + ", ");
					}
				} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception | InfraManagementNotFound_Exception e) {
					m_log.debug("execute getInfraManagement, " + e.getMessage());
					failManagementIds.append(managementId + ", ");
				}
			}
		}

		//有効化に成功したものを表示
		if(sucManagementIds.length() > 0) {
			sucManagementIds.setLength(sucManagementIds.length() - 2);
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.infra.action.result",
							new Object[]{Messages.getString("infra.management.id"),
									Messages.getString("infra.disable.setting"),
									Messages.getString("successful"), sucManagementIds}));
		}
		//有効化に失敗したものを表示
		if(failManagementIds.length() > 0) {
			failManagementIds.setLength(failManagementIds.length() - 2);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.infra.action.result",
							new Object[]{Messages.getString("infra.management.id"),
									Messages.getString("infra.disable.setting"),
									Messages.getString("failed"), failManagementIds}));
		}
		infraManagementView.update();
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
}