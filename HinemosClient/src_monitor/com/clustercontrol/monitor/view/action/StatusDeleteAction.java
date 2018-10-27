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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.monitor.action.GetStatusListTableDefine;
import com.clustercontrol.monitor.composite.StatusListComposite;
import com.clustercontrol.monitor.util.ConvertListUtil;
import com.clustercontrol.monitor.util.MonitorEndpointWrapper;
import com.clustercontrol.monitor.view.StatusView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.StatusDataInfo;

/**
 * 監視[ステータス]ビューの削除アクションによるステータスの削除処理を行うクライアント側アクションクラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class StatusDeleteAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( StatusDeleteAction.class );

	/** アクションID */
	public static final String ID = StatusDeleteAction.class.getName();

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
	 * 監視[ステータス]ビューの選択されたアイテムを削除し、ビューを更新します。
	 * <p>
	 * <ol>
	 * <li>監視[ステータス]ビューで、選択されているアイテムを取得します。</li>
	 * <li>取得したステータス情報を削除します。 </li>
	 * <li>監視[ステータス]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.monitor.view.StatusView
	 * @see com.clustercontrol.monitor.view.StatusView#update()
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
		StatusView statusView = null;
		try {
			statusView 	= (StatusView) this.viewPart.getAdapter(StatusView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (statusView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		StatusListComposite composite = (StatusListComposite) statusView
				.getListComposite();
		WidgetTestUtil.setTestId(this, null, composite);
		StructuredSelection selection = (StructuredSelection) composite
				.getTableViewer().getSelection();

		List<?> list = selection.toList();

		Map<String, List<List<String>>> map = new ConcurrentHashMap<String, List<List<String>>>();
		for(Object obj : list) {
			List<?> objList = (List<?>)obj;
			String managerName = (String) objList.get(GetStatusListTableDefine.MANAGER_NAME);
			if(map.get(managerName) == null) {
				map.put(managerName, new ArrayList<List<String>>());
			}
		}

		for(Object obj : list) {
			@SuppressWarnings("unchecked")
			List<String> objList = (List<String>)obj;
			String managerName = (String) objList.get(GetStatusListTableDefine.MANAGER_NAME);
			map.get(managerName).add(objList);
		}

		if(map.isEmpty()) {
			return null;
		}

		for(Map.Entry<String, List<List<String>>> entry : map.entrySet()) {
			String managerName = entry.getKey();
			MonitorEndpointWrapper wrapper = MonitorEndpointWrapper.getWrapper(managerName);
			List<?> records = entry.getValue();

			// 選択しているステータス情報を削除して、再描画します。
			ArrayList<StatusDataInfo> statusList = ConvertListUtil.listToStatusInfoDataList(records);
			
			ConcurrentHashMap<String, String>errMsg = new ConcurrentHashMap<>();

			if (statusList != null && statusList.size()>0) {
				try {
					wrapper.deleteStatus(statusList);
					statusView.update(false);
				} catch (InvalidRole_Exception e) {
					// アクセス権なしの場合、エラーダイアログを表示する
					errMsg.put(managerName, Messages.getString("message.accesscontrol.16"));
				} catch (MonitorNotFound_Exception e) {
					errMsg.put(managerName, Messages.getString("message.monitor.61") + ", " + HinemosMessage.replace(e.getMessage()));
				} catch (HinemosUnknown_Exception e) {
					errMsg.put(managerName, Messages.getString("message.monitor.61") + ", " + HinemosMessage.replace(e.getMessage()));
				} catch (Exception e) {
					m_log.warn("run(), " + e.getMessage(), e);
					errMsg.put(managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				}
			}
			
			if (!errMsg.isEmpty()) {
				UIManager.showMessageBox(errMsg, true);
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
				if(part instanceof StatusView){
					// Enable button when 1 item is selected
					StatusView view = (StatusView)part;

					if(view.getRowNum() > 0){
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
