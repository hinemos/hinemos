/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

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
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmanagement.action.DeleteJobLinkSendSetting;
import com.clustercontrol.jobmanagement.action.GetJobLinkSendSettingTableDefine;
import com.clustercontrol.jobmanagement.composite.JobLinkSendSettingListComposite;
import com.clustercontrol.jobmanagement.view.JobLinkSendSettingListView;
import com.clustercontrol.util.Messages;

/**
 * ジョブ連携送信設定の削除コマンドを実行します。
 *
 */
public class DeleteJobLinkSendSettingAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( DeleteJobLinkSendSettingAction.class );

	/** アクションID */
	public static final String ID = DeleteJobLinkSendSettingAction.class.getName();

	/** ビュー */
	private IWorkbenchPart m_viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		m_viewPart = null;
	}

	/**
	 * ジョブ設定[ジョブ連携送信設定]ビューの「削除」が押された場合に、
	 * ジョブ連携送信設定を削除します。
	 *
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		m_viewPart = HandlerUtil.getActivePart(event);
		JobLinkSendSettingListView view = null;
		try {
			view = (JobLinkSendSettingListView) m_viewPart.getAdapter(JobLinkSendSettingListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		JobLinkSendSettingListComposite composite = (JobLinkSendSettingListComposite) view.getComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();
		List<?> list = (List<?>)selection.toList();

		Map<String, List<String>> idMap = new ConcurrentHashMap<>();
		int size = 0;
		if(list != null && list.size() > 0){
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				String managerName = (String) objList.get(GetJobLinkSendSettingTableDefine.MANAGER_NAME);
				if(idMap.get(managerName) == null) {
					idMap.put(managerName, new ArrayList<>());
				}
			}
			String id = null;
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				id = (String) objList.get(GetJobLinkSendSettingTableDefine.JOBLINK_SEND_SETTING_ID);
				String managerName = (String) objList.get(GetJobLinkSendSettingTableDefine.MANAGER_NAME);
				idMap.get(managerName).add(id);
				size++;
			}

			String[] args = new String[1];
			String message = null;
			if(size > 0){
				if(size == 1) {
					args[0] = id;
					message = "message.joblinksendsetting.delete.confirm";
				} else {
					args[0] = Integer.toString(size);
					message = "message.joblinksendsetting.delete.multiple.confirm";
				}
			}

			// 選択アイテムがある場合に、削除処理を呼び出す
			DeleteJobLinkSendSetting deleteJobLinkSendSetting = new DeleteJobLinkSendSetting();

			if (!MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString(message, args))) {
				return null;
			}
			boolean result = false;
			for(Map.Entry<String, List<String>> map : idMap.entrySet()) {
				result = result | deleteJobLinkSendSetting.delete(map.getKey(), map.getValue());
			}
			if(result){
				composite.update();
			}
		} else {
			MessageDialog.openWarning(
					null,
					Messages.getString("warning"),
					Messages.getString("message.joblinksendsetting.noselect"));
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
				if( part instanceof JobLinkSendSettingListView ){
					// Enable button when 1 item is selected
					JobLinkSendSettingListView view = (JobLinkSendSettingListView)part;
					if(view.getSelectedNum() > 0) {
						editEnable = true;
					}
				}
				this.setBaseEnabled( editEnable );
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
