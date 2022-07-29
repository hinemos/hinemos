/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

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
import org.openapitools.client.model.InfraCheckResultResponse;
import org.openapitools.client.model.InfraManagementInfoResponse;

import com.clustercontrol.dialog.TextAreaDialog;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.infra.action.GetInfraModuleTableDefine;
import com.clustercontrol.infra.composite.InfraModuleComposite;
import com.clustercontrol.infra.util.InfraRestClientWrapper;
import com.clustercontrol.infra.view.InfraModuleView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

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
		List<InfraCheckResultResponse> allResultList = null;
		try {
			InfraRestClientWrapper wrapper = InfraRestClientWrapper.getWrapper(managerName);
			allResultList = wrapper.getCheckResultList(managementId);
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting e) {
			m_log.error("getStatusString() getCheckResultList, " + e.getMessage());
		}
		if(allResultList == null){
			return null;
		}

		List<InfraCheckResultResponse> resultList = new ArrayList<>();
		for (InfraCheckResultResponse result : allResultList) {
			if( moduleId.equals(result.getModuleId()) ){
				resultList.add( result );
			}
		}

		List<String> okList = new ArrayList<>();
		List<String> ngList = new ArrayList<>();
		String newline = System.getProperty("line.separator");
		for (InfraCheckResultResponse result : resultList) {
			if(result.getResult() == InfraCheckResultResponse.ResultEnum.OK){
				okList.add(result.getNodeId());
			} else if(result.getResult() == InfraCheckResultResponse.ResultEnum.NG){
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
		InfraManagementInfoResponse management = null;

		try {
			InfraRestClientWrapper wrapper = InfraRestClientWrapper.getWrapper(managerName);
			management = wrapper.getInfraManagement(infraModuleView.getComposite().getManagementId());
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.accesscontrol.16"));
			return null;
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InfraManagementNotFound
				| InvalidSetting e) {
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
