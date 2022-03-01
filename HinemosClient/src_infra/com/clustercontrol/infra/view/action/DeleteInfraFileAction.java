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
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraFileBeingUsed;
import com.clustercontrol.fault.InfraFileNotFound;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.infra.action.GetInfraFileManagerTableDefine;
import com.clustercontrol.infra.util.InfraFileUtil;
import com.clustercontrol.infra.util.InfraRestClientWrapper;
import com.clustercontrol.infra.view.InfraFileManagerView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;


public class DeleteInfraFileAction extends InfraFileManagerBaseAction {
	// ログ
	private static Log m_log = LogFactory.getLog( DeleteInfraManagementAction.class );

	/** アクションID */
	public static final String ID = DeleteInfraFileAction.class.getName();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		InfraFileManagerView view = getView(event);
		if (view == null) {
			m_log.info("execute: view is null");
			return null;
		}

		List<String> fileIdList = getSelectedInfraFileIdList(view);
		if (fileIdList.isEmpty()) {
			return null;
		}

		StructuredSelection selection = null;
		if(view.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();
		}
		if(selection == null || selection.isEmpty()){
			return null;
		}
		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
		for(Object object: selection.toList()){
			String managerName = (String) ((ArrayList<?>)object).get(GetInfraFileManagerTableDefine.MANAGER_NAME);
			if(map.get(managerName) == null) {
				map.put(managerName, new ArrayList<String>());
			}
		}

		StringBuffer strFileIds = new StringBuffer();
		String tmpFileId = null;
		for(Object object: selection.toList()){
			String managerName = (String) ((ArrayList<?>)object).get(GetInfraFileManagerTableDefine.MANAGER_NAME);
			tmpFileId = (String) ((ArrayList<?>)object).get(GetInfraFileManagerTableDefine.FILE_ID);
			map.get(managerName).add(tmpFileId);
			if (strFileIds.length() == 0) {
				strFileIds.append(tmpFileId + "(" + managerName + ")");
			} else {
				strFileIds.append(", " + tmpFileId + "(" + managerName + ")");
			}
		}

		if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.infra.confirm.action",
						new Object[]{Messages.getString("file"), Messages.getString("delete"), strFileIds}))) {

			Map<String, String> errMsg = new ConcurrentHashMap<String, String>();
			for(Map.Entry<String, List<String>> entry : map.entrySet()) {
				String managerName = entry.getKey();
				InfraRestClientWrapper wrapper = InfraRestClientWrapper.getWrapper(managerName);
				try {
					List<String> files = entry.getValue();
					wrapper.deleteInfraFileList(files.stream().collect(Collectors.joining(",")));
				} catch (InvalidRole e){
					// 権限なし
					errMsg.put(managerName, Messages.getString("message.accesscontrol.16"));
				} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidSetting | InfraManagementNotFound |
						InfraFileNotFound | InfraFileBeingUsed e) {
					m_log.debug("execute() : " + e.getClass() + e.getMessage());
					String arg = Messages.getString(
							"message.infra.action.result",
							new Object[] {
									Messages.getString("infra.filemanager.file.id"),
									Messages.getString("delete"),
									Messages.getString("failed"),
									HinemosMessage.replace(e.getMessage()) });
					errMsg.put(managerName, arg);
				}
			}

			if(errMsg.isEmpty()) {
				String action = Messages.getString("delete");
				InfraFileUtil.showSuccessDialog(action, strFileIds.toString());
			} else {
				UIManager.showMessageBox(errMsg, true);
			}
			// ビューの更新
			view.update();
		}

		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				boolean enable = false;
				if (part instanceof InfraFileManagerView) {
					InfraFileManagerView view = (InfraFileManagerView) part.getAdapter(InfraFileManagerView.class);

					if (view == null) {
						m_log.info("execute: view is null");
						return;
					}

					StructuredSelection selection = null;
					if (view.getComposite().getTableViewer().getSelection() instanceof StructuredSelection) {
						selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();
					}

					if (selection != null && selection.size() >= 1) {
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
