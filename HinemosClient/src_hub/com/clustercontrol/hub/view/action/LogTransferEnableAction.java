/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.view.action;

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
import org.openapitools.client.model.SetTransferValidRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.hub.action.GetTransferTableDefine;
import com.clustercontrol.hub.util.HubRestClientWrapper;
import com.clustercontrol.hub.view.TransferView;
import com.clustercontrol.util.Messages;


public class LogTransferEnableAction extends AbstractHandler  implements IElementUpdater {
	// ログ
	private static Log m_log = LogFactory.getLog( LogTransferEnableAction.class );

	/** アクションID */
	public static final String ID = LogTransferEnableAction.class.getName();

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
		if (!(viewPart instanceof TransferView)) {
			return null;
		}

		TransferView logTransfarView = null;
		try {
			logTransfarView = (TransferView) viewPart.getAdapter(TransferView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}		
		if(logTransfarView == null){
			return null;
		}

		StructuredSelection selection = null;
		if(logTransfarView.getLogTransferComposite().getTableViewer().getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) logTransfarView.getLogTransferComposite().getTableViewer().getSelection();
		}

		if(selection == null){
			return null;
		}

		List<?> sList = (List<?>) selection.toList();
		Map<String, List<String>> transferIdMap = new ConcurrentHashMap<String, List<String>>();

		for (Object obj : sList) {
			List<?> list = (List<?>)obj;
			String managerName = null;
			if (list == null) {
				continue;
			}
			managerName = (String) list.get(GetTransferTableDefine.MANAGER_NAME);
			if(transferIdMap.get(managerName) == null) {
				transferIdMap.put(managerName, new ArrayList<String>());
			}
		}

		StringBuffer idbuf = new StringBuffer();
		int size = 0;
		for (Object obj : sList) {
			List<?> list = (List<?>)obj;
			String transferId = null;
			String managerName = null;
			if (list != null) {
				transferId = (String) list.get(GetTransferTableDefine.TRANSFER_ID);
				managerName = (String) list.get(GetTransferTableDefine.MANAGER_NAME);
				transferIdMap.get(managerName).add(transferId);
				if(size > 0) {
					idbuf.append(", ");
				}
				idbuf.append(transferId);
				size++;
			}
		}

		if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.hub.log.transfer.confirm.action",
						new Object[]{Messages.getString("hub.log.transfer.id"),
								Messages.getString("hub.log.transfer.enable.setting"), idbuf.toString()})) == false)
		{
			return null;
		}

		StringBuffer successList = new StringBuffer();
		StringBuffer failureList = new StringBuffer();
		StringBuffer invalidRoleList = new StringBuffer();
		for(Map.Entry<String, List<String>> entry : transferIdMap.entrySet()) {
			String managerName = entry.getKey();
			HubRestClientWrapper wrapper = HubRestClientWrapper.getWrapper(managerName);
			try{
				SetTransferValidRequest request = new SetTransferValidRequest();
				request.setTransferIdList(entry.getValue());
				request.setFlg(false);
				wrapper.setTransferValid(request);
				for (String transferId : entry.getValue()) {
					successList.append(transferId +"(" + managerName + ")" + "\n");
				}
			} catch (InvalidRole e) {
				String targetIds = "{" + String.join(",", entry.getValue()) + "}";
				for(String targetId : entry.getValue()) {
					invalidRoleList.append(targetId + "\n");
				}
				m_log.warn("run() setNotifyValid targetIds=" + targetIds + ", " + e.getMessage(), e);
			} catch (Exception e) {
				String transferIds = "{" + String.join(",", entry.getValue()) + "}";
				for(String transferId : entry.getValue()) {
					failureList.append(transferId + "\n");
				}
				m_log.warn("run() setNotifyValid targetIds=" + transferIds + ", " + e.getMessage(), e);
			}
		}

		if (invalidRoleList.length() != 0) {
			// 権限がない場合にはエラーメッセージを表示する
			MessageDialog.openInformation(
					null, 
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16") + "\n" + invalidRoleList);
		}

		// 成功ダイアログ
		if(successList.length() != 0){
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.hub.log.transfer.action.result",
							new Object[]{Messages.getString("hub.log.transfer.id"),
									Messages.getString("hub.log.transfer.enable.setting"),
									Messages.getString("successful"), successList}));

		}

		// 失敗ダイアログ
		if(failureList.length() != 0){
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hub.log.transfer.action.result",
							new Object[]{Messages.getString("hub.log.transfer.id"),
									Messages.getString("hub.log.transfer.enable.setting"),
									Messages.getString("failed"), failureList}));

		}

		logTransfarView.update();
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

				if(part instanceof TransferView){
					TransferView view = (TransferView) part.getAdapter(TransferView.class);
					// Enable button when 1 item is selected
					StructuredSelection selection = null;
					if(view.getLogTransferComposite().getTableViewer().getSelection() instanceof StructuredSelection){
						selection = (StructuredSelection) view.getLogTransferComposite().getTableViewer().getSelection();
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