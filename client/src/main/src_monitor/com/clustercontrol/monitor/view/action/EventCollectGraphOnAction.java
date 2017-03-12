/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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

import com.clustercontrol.monitor.action.GetEventListTableDefine;
import com.clustercontrol.monitor.composite.EventListComposite;
import com.clustercontrol.monitor.util.ConvertListUtil;
import com.clustercontrol.monitor.util.MonitorEndpointWrapper;
import com.clustercontrol.monitor.view.EventView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.monitor.EventDataInfo;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * 監視[イベント]ビューの性能グラフ用フラグONアクションによるイベントの更新処理を行うアクライアント側アクションクラス<BR>
 *
 * @version 5.1.0
 * @since 1.0.0
 */
public class EventCollectGraphOnAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( EventCollectGraphOnAction.class );

	/** アクションID */
	public static final String ID = EventCollectGraphOnAction.class.getName();

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
	 * 監視[イベント]ビューの選択されたアイテムの性能グラフ用フラグをONに更新し、ビューを更新します。
	 * <p>
	 * <ol>
	 * <li>監視[イベント]ビューで、選択されているアイテムを取得します。</li>
	 * <li>取得したイベント情報の性能グラフ用フラグをONに更新します。 </li>
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

		EventListComposite composite = (EventListComposite) eventView.getListComposite();
		WidgetTestUtil.setTestId(this, null, composite);
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

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

		// 選択しているイベント情報の性能グラフ用フラグを「ON」にして、再描画します。
		if(map.isEmpty()) {
			return null;
		}

		for(Map.Entry<String, List<List<String>>> entry : map.entrySet()) {
			String managerName = entry.getKey();
			MonitorEndpointWrapper wrapper = MonitorEndpointWrapper.getWrapper(managerName);
			List<List<String>> records = entry.getValue();
			ArrayList<EventDataInfo> eventInfoList = ConvertListUtil.listToEventLogDataList(records);

			if (eventInfoList != null && eventInfoList.size()>0) {
				try {
					wrapper.modifyCollectGraphFlg(eventInfoList, Boolean.TRUE);
					eventView.update(false);
				} catch (InvalidRole_Exception e) {
					// アクセス権なしの場合、エラーダイアログを表示する
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} catch (MonitorNotFound_Exception e) {
					MessageDialog.openError(null, Messages.getString("message"),
							Messages.getString("message.monitor.60") + ", " + HinemosMessage.replace(e.getMessage()));
				} catch (HinemosUnknown_Exception e) {
					MessageDialog.openError(null, Messages.getString("message"),
							Messages.getString("message.monitor.60") + ", " + HinemosMessage.replace(e.getMessage()));
				} catch (Exception e) {
					m_log.warn("run(), " + e.getMessage(), e);
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				}
			}
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
				if(part instanceof EventView){
					// Enable button when 1 item is selected
					EventView eventView = null;
					try {
						eventView = (EventView) part.getAdapter(EventView.class);
						if (eventView == null) {
							m_log.info("execute: view is null");
							return;
						}
						EventListComposite composite = (EventListComposite) eventView.getListComposite();
						StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();
						int size = selection.size();
						if (size != 0) {
							editEnable = true;
						}
					} catch (Exception e) {
						// nop
					}
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
