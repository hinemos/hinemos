/*


This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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

import com.clustercontrol.hub.action.GetTransferTableDefine;
import com.clustercontrol.hub.util.HubEndpointWrapper;
import com.clustercontrol.hub.view.TransferView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.hub.HinemosUnknown_Exception;
import com.clustercontrol.ws.hub.InvalidRole_Exception;
import com.clustercontrol.ws.hub.InvalidSetting_Exception;
import com.clustercontrol.ws.hub.InvalidUserPass_Exception;
import com.clustercontrol.ws.hub.LogTransferNotFound_Exception;
import com.clustercontrol.ws.hub.TransferInfo;

public class LogTransferDisableAction extends AbstractHandler  implements IElementUpdater {
	// ログ
	private static Log m_log = LogFactory.getLog( LogTransferDisableAction.class );

	/** アクションID */
	public static final String ID = LogTransferDisableAction.class.getName();

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

		TransferView logTransferView = null;
		try {
			logTransferView = (TransferView) viewPart.getAdapter(TransferView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}
		if(logTransferView == null){
			return null;
		}

		StructuredSelection selection = null;
		if(logTransferView.getLogTransferComposite().getTableViewer().getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) logTransferView.getLogTransferComposite().getTableViewer().getSelection();
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
								Messages.getString("hub.log.transfer.disable.setting"), idbuf.toString()})) == false)
		{
			return null;
		}

		StringBuffer sucTransferIds = new StringBuffer();
		StringBuffer failTransferIds = new StringBuffer();

		for(Map.Entry<String, List<String>> entry : transferIdMap.entrySet()) {
			String managerName = entry.getKey();
			HubEndpointWrapper wrapper = HubEndpointWrapper.getWrapper(managerName);
			for (String transferId : entry.getValue()) {
				try {
					TransferInfo info = wrapper.getTransferInfo(transferId);
					info.setValidFlg(false);
					try {
						wrapper.modifyTransferInfo(info);
						sucTransferIds.append(transferId + "("+ managerName + "), ");
					}
					catch (InvalidRole_Exception | InvalidUserPass_Exception | InvalidSetting_Exception | LogTransferNotFound_Exception e) {
						m_log.debug("execute modifyLogTransfer, " + e.getMessage());
						failTransferIds.append(transferId + ", ");
					}
				} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
					m_log.debug("execute getLogTransfer, " + e.getMessage());
					failTransferIds.append(transferId + ", ");
				}
			}
		}

		//無効化に成功したものを表示
		if(sucTransferIds.length() > 0) {
			sucTransferIds.setLength(sucTransferIds.length() - 2);
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.hub.log.transfer.action.result",
							new Object[]{Messages.getString("hub.log.transfer.id"),
									Messages.getString("hub.log.transfer.disable.setting"),
									Messages.getString("successful"), sucTransferIds}));
		}
		//無効化に失敗したものを表示
		if(failTransferIds.length() > 0) {
			failTransferIds.setLength(failTransferIds.length() - 2);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hub.log.transfer.action.result",
							new Object[]{Messages.getString("hub.log.transfer.id"),
									Messages.getString("hub.log.transfer.disable.setting"),
									Messages.getString("failed"), failTransferIds}));
		}
		logTransferView.update();
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