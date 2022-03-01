/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.view.action;

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

import com.clustercontrol.fault.InvalidRole;import com.clustercontrol.monitor.composite.MonitorListComposite;
import com.clustercontrol.monitor.run.action.GetMonitorListTableDefine;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.monitor.view.MonitorListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * 監視[一覧]ビューの削除アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class MonitorDeleteAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( MonitorDeleteAction.class );

	/** アクションID */
	public static final String ID = MonitorDeleteAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		MonitorListView monitorListView = null;
		try {
			monitorListView = (MonitorListView) this.viewPart.getAdapter(MonitorListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (monitorListView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		MonitorListComposite composite = (MonitorListComposite) monitorListView.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();
		List<String[]> argsList = new ArrayList<String[]>();
		if(list != null && list.size() > 0){
			for (Object obj : list) {
				List<?> objList = (List<?>)obj;
				String[] args = new String[3];
				args[0] = (String) objList.get(GetMonitorListTableDefine.MANAGER_NAME);
				args[1] = (String) objList.get(GetMonitorListTableDefine.MONITOR_TYPE_ID);
				args[2] = (String) objList.get(GetMonitorListTableDefine.MONITOR_ID);
				argsList.add(args);
			}
		}

		// 選択アイテムがある場合に、削除処理を呼び出す
		if(argsList.isEmpty() ) {
			return null;
		}
		// 削除を実行してよいかの確認ダイアログの表示
		String msg = null;
		String[] msgArgs = new String[2];
		if(argsList.isEmpty() == false) {
			if (argsList.size() == 1) {
				msgArgs[0] = argsList.get(0)[2] + "(" + argsList.get(0)[0] + ")";
				msg = "message.monitor.39";
			} else {
				msgArgs[0] = Integer.toString(argsList.size());
				msg = "message.monitor.81";
			}
		}

		if (!MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString(msg, msgArgs))) {

			// OKが押されない場合は終了
			return null;
		}

		Map<String, List<String>> deleteMap = null;

		for(String[] args : argsList) {
			String managerName = args[0];
			String monitorId = args[2];
			if(deleteMap == null) {
				deleteMap = new ConcurrentHashMap<String, List<String>>();
			}
			if(deleteMap.get(managerName) == null) {
				deleteMap.put(managerName, new ArrayList<String>());
			}
			deleteMap.get(managerName).add(monitorId);
		}

		String errMessage = "";
		int errCount = 0;
		int successCount = 0;
		List<String> errManagerNameList = new ArrayList<String>();
		for(Map.Entry<String, List<String>> map : deleteMap.entrySet()) {
			try {
				MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(map.getKey());
				wrapper.deleteMonitor(String.join(",", map.getValue()));
				successCount = successCount + map.getValue().size();
			} catch(InvalidRole e) {				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16") + "(" + map.getKey() + ")");
				return null;
			} catch(Exception e) {
				errCount = errCount + map.getValue().size();
				errMessage = HinemosMessage.replace(e.getMessage());
				errManagerNameList.add(map.getKey());
			}
		}
		String message = null;
		if (errCount > 0) {
			if (errCount == 1) {
				msgArgs[1] = errMessage;
				message = Messages.getString("message.monitor.38", msgArgs);
			} else {
				message = Messages.getString("message.monitor.83", 
						new String[]{Integer.toString(errCount),  String.join(",", errManagerNameList), errMessage});
			}
			MessageDialog.openError(null, Messages.getString("failed"), message);
		}
		if (successCount > 0) {
			if (successCount == 1) {
				message = Messages.getString("message.monitor.37", msgArgs);
			} else {
				message = Messages.getString("message.monitor.82", new String[]{Integer.toString(successCount)});
			}
			MessageDialog.openInformation(null, Messages.getString("successful"), message);
			monitorListView.update();
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

				boolean editEnable = false;
				if(part instanceof MonitorListView){
					// Enable button when 1 item is selected
					MonitorListView view = (MonitorListView)part;

					if(view.getSelectedNum() > 0) {
						editEnable = true;
					}
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}

}
