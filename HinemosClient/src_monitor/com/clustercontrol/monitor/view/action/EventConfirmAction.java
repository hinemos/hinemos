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
import org.openapitools.client.model.EventLogInfoRequest;
import org.openapitools.client.model.ModifyConfirmRequest;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.monitor.action.GetEventListTableDefine;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.composite.EventListComposite;
import com.clustercontrol.monitor.preference.MonitorPreferencePage;
import com.clustercontrol.monitor.util.ConvertListUtil;
import com.clustercontrol.monitor.util.MonitorResultRestClientWrapper;
import com.clustercontrol.monitor.view.EventView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 監視[イベント]ビューの確認アクションによるイベントの確認の更新処理を行うアクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class EventConfirmAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( EventConfirmAction.class );

	/** アクションID */
	public static final String ID = EventConfirmAction.class.getName();

	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	/**
	 * 監視[イベント]ビューの選択されたアイテムを確認済に更新し、ビューを更新します。
	 * <p>
	 * <ol>
	 * <li>監視[イベント]ビューで、選択されているアイテムを取得します。</li>
	 * <li>取得したイベント情報の確認状態を確認済に更新します。 </li>
	 * <li>監視[イベント]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.monitor.view.EventView
	 * @see com.clustercontrol.monitor.view.EventView#update()
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		EventView eventView = null;
		try {
			eventView = (EventView) this.viewPart.getAdapter(EventView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (eventView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		EventListComposite composite = (EventListComposite) eventView
				.getListComposite();
				WidgetTestUtil.setTestId(this, null, composite);
		StructuredSelection selection = (StructuredSelection) composite
				.getTableViewer().getSelection();

		List<?> list = selection.toList();

		Map<String, List<List<String>>> map = new ConcurrentHashMap<String, List<List<String>>>();
		for(Object obj : list) {
			List<?> objList = (List<?>)obj;
			String managerName = (String) objList.get(GetEventListTableDefine.MANAGER_NAME);
			if(map.get(managerName) == null) {
				map.put(managerName, new ArrayList<List<String>>());
			}
		}

		for(Object obj : list) {
			@SuppressWarnings("unchecked")
			List<String> objList = (List<String>)obj;
			String managerName = (String) objList.get(GetEventListTableDefine.MANAGER_NAME);
			map.get(managerName).add(objList);
		}

		// 選択しているイベント情報を「確認済」にして、再描画します。
		if(map.isEmpty()) {
			return null;
		}

		// 確認ダイアログを表示するかどうかのフラグをPreferenceから取得
		if (ClusterControlPlugin.getDefault().getPreferenceStore().getBoolean(MonitorPreferencePage.P_EVENT_CONFIRM_DIALOG_FLG) 
			&& !MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString("dialog.monitor.events.confirmed.confirm"))) {
			// OKが押されない場合は処理しない
			return null;
		}

		for(Map.Entry<String, List<List<String>>> entry : map.entrySet()) {
			String managerName = entry.getKey();
			MonitorResultRestClientWrapper wrapper = MonitorResultRestClientWrapper.getWrapper(managerName);

			List<List<String>> records = entry.getValue();
			List<EventLogInfoRequest> eventInfoList = ConvertListUtil.listToEventLogDataList(records);

			if (eventInfoList != null && eventInfoList.size()>0) {
				try {
					ModifyConfirmRequest modifyConfirmRequest = new ModifyConfirmRequest();
					modifyConfirmRequest.setList(eventInfoList);
					modifyConfirmRequest.setConfirmType(ConfirmConstant.TYPE_CONFIRMED);
					wrapper.modifyConfirm(modifyConfirmRequest);
					eventView.update(false);
				} catch (InvalidRole e) {
					// アクセス権なしの場合、エラーダイアログを表示する
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16") + "(" + managerName + ")");
				} catch (HinemosUnknown e) {
					MessageDialog.openError(null, Messages.getString("message"),
							Messages.getString("message.monitor.60") + "(" + managerName + ") " + HinemosMessage.replace(e.getMessage()));
				} catch (Exception e) {
					m_log.warn("run(), " + e.getMessage(), e);
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.hinemos.failure.unexpected") + "(" + managerName + ") " + HinemosMessage.replace(e.getMessage()));
				}
			}
		}

		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
   		if (window == null) {
			return;
		}
		
		IWorkbenchPage page = window.getActivePage();

		if (page == null) {
			this.setBaseEnabled(false);
			return;
		}
		
			
		IWorkbenchPart part = page.getActivePart();
		
		if (!(part instanceof EventView)) {
			this.setBaseEnabled(false);
			return;
		}

		// Enable button when not confirming items were selected		

		boolean editEnable = true;

		EventView view = (EventView)part;
		
		if (view.getConfirmTypeList() == null ||
				view.getConfirmTypeList().contains(ConfirmConstant.TYPE_CONFIRMED)) {
				//全く選択されていないか、確認済が選択されている場合、非活性
				editEnable = false;
		}
		this.setBaseEnabled(editEnable);
	}
}
