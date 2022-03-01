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
import org.openapitools.client.model.SetStatusMonitorRequest;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.monitor.composite.MonitorListComposite;
import com.clustercontrol.monitor.run.action.GetMonitorListTableDefine;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.monitor.view.MonitorListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * 監視[一覧]ビューの監視有効アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class MonitorEnableAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( MonitorEnableAction.class );

	/** アクションID */
	public static final String ID = MonitorEnableAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	/**
	 * アクション実行
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		MonitorListView view = null;
		try {
			view = (MonitorListView) this.viewPart.getAdapter(MonitorListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		MonitorListComposite composite = (MonitorListComposite) view.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

		Object [] objs = selection.toArray();

		// 1つも選択されていない場合
		if(objs.length == 0){
			MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString("message.monitor.1"));
			return null;
		}


		// 1つ以上選択されている場合
		String managerName = null;
		String monitorId = null;
		String monitorTypeId = null;
		String[] args;
		StringBuffer targetList = new StringBuffer();
		StringBuffer successList = new StringBuffer();
		StringBuffer failureList = new StringBuffer();

		Map<String, List<String[]>> dataMap = new ConcurrentHashMap<String, List<String[]>>();
		for (int i = 0; i < objs.length; i++) {
			managerName = (String) ((ArrayList<?>)objs[i]).get(GetMonitorListTableDefine.MANAGER_NAME);
			if(dataMap.get(managerName) == null) {
				dataMap.put(managerName, new ArrayList<String[]>());
			}
		}

		for (int i = 0; i < objs.length; i++) {
			managerName = (String) ((ArrayList<?>)objs[i]).get(GetMonitorListTableDefine.MANAGER_NAME);
			monitorId = (String) ((ArrayList<?>)objs[i]).get(GetMonitorListTableDefine.MONITOR_ID);
			monitorTypeId = (String) ((ArrayList<?>)objs[i]).get(GetMonitorListTableDefine.MONITOR_TYPE_ID);

			String[] arg = {monitorId, monitorTypeId};
			dataMap.get(managerName).add(arg);

			if (targetList.length() > 0) {
				targetList.append(", ");

			}
			targetList.append(monitorId);
		}

		// 実行確認(NG→終了)
		args = new String[]{ targetList.toString() } ;
		if (!MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.monitor.68", args))) {
			return null;
		}

		boolean hasRole = true;
		// 実行
		for(Map.Entry<String, List<String[]>> map : dataMap.entrySet()) {
			String mgrName = map.getKey();
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(mgrName);

			List<String> monitorIdList = new ArrayList<>();
			for(String[] strArgs : map.getValue()) {
				monitorIdList.add(strArgs[0]);
			}
			try{
				SetStatusMonitorRequest info = new SetStatusMonitorRequest();
				info.setMonitorIds(monitorIdList);
				info.setValidFlg(true);
				wrapper.setStatusMonitor(info);
				if (successList.length() > 0) {
					successList.append("\n");
				}
				successList.append(mgrName + " : " + String.join(", ", monitorIdList));
			} catch (InvalidRole e) {
				if (failureList.length() > 0) {
					failureList.append("\n");
				}
				failureList.append(HinemosMessage.replace(e.getMessage()) + " : " + String.join(",", monitorIdList));
				m_log.warn("run() setStatusMonitor monitorId=" + String.join(",", monitorIdList) + ", " + e.getMessage(), e);
				hasRole = false;
			}catch (Exception e) {
				if (failureList.length() > 0) {
					failureList.append("\n");
				}
				failureList.append(HinemosMessage.replace(e.getMessage()) + " : " + String.join(",", monitorIdList));
				m_log.warn("run() setStatusMonitor monitorId=" + String.join(",", monitorIdList) + ", " + e.getMessage(), e);
			}
		}


		if (!hasRole) {
			// 権限がない場合にはエラーメッセージを表示する
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		}

		// 成功ダイアログ
		if(successList.length() != 0){
			args = new String[]{ successList.toString() } ;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.monitor.69", args));
		}

		// 失敗ダイアログ
		if(failureList.length() != 0){
			args = new String[]{ failureList.toString() } ;
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.monitor.70", args));
		}

		// ビューコンポジット更新
		composite.update();

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
