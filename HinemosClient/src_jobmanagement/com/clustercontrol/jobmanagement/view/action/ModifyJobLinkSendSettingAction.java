/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.JobLinkSendInfoResponse;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.action.GetJobLinkSendSettingTableDefine;
import com.clustercontrol.jobmanagement.composite.JobLinkSendSettingListComposite;
import com.clustercontrol.jobmanagement.dialog.JobLinkSendSettingDialog;
import com.clustercontrol.jobmanagement.view.JobLinkSendSettingListView;

/**
 * ジョブ連携送信設定の変更コマンドを実行します。
 *
 */
public class ModifyJobLinkSendSettingAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	private static Log m_log = LogFactory.getLog(ModifyJobLinkSendSettingAction.class);
	/** アクションID */
	public static final String ID = ModifyJobLinkSendSettingAction.class.getName();
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
	 * 指定されたジョブ連携送信設定IDを元にダイアログを開く
	 * 
	 * @param shell
	 * @param managerName
	 * @param joblinkSendSettingId ジョブ連携送信設定ID
	 * @return
	 */
	public int dialogOpen(Shell shell, String managerName, String joblinkSendSettingId) {
		JobLinkSendSettingDialog dialog = new JobLinkSendSettingDialog(
				shell, managerName, joblinkSendSettingId, PropertyDefineConstant.MODE_MODIFY);
		return dialog.open();
	}

	/**
	 * 指定されたジョブ連携送信設定IDを元にダイアログを開く
	 * 
	 * @param shell
	 * @param managerName
	 * @param jobLinkSendSetting ジョブ連携送信設定
	 * @return
	 */
	public int dialogOpen(
			Shell shell,
			String managerName,
			JobLinkSendInfoResponse jobLinkSendInfo) {
		JobLinkSendSettingDialog dialog = new JobLinkSendSettingDialog(
				shell,
				managerName,
				jobLinkSendInfo);
		return dialog.open();
	}

	/**
	 * ジョブ設定[ジョブ連携送信設定]ビューの「変更」が押された場合に、変更処理を行います。
	 * <p>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.dialog.JobLinkSendSettingDialog
	 * @see com.clustercontrol.jobmanagement.view.JobLinkSendSettingListView
	 * @see com.clustercontrol.jobmanagement.composite.JobLinkSendSettingListComposite
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

		if (!(viewPart instanceof JobLinkSendSettingListView)) {
			return null;
		}

		JobLinkSendSettingListView  view = null;
		try {
			view = (JobLinkSendSettingListView) viewPart.getAdapter(JobLinkSendSettingListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		JobLinkSendSettingListComposite composite = (JobLinkSendSettingListComposite)view.getComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();
		List<?> list = (List<?>) selection.getFirstElement();
		String managerName = null;
		String id = null;
		if(list != null && list.size() > 0){
			managerName = (String) list.get(GetJobLinkSendSettingTableDefine.MANAGER_NAME);
			id = (String) list.get(GetJobLinkSendSettingTableDefine.JOBLINK_SEND_SETTING_ID);
		}

		if (id != null) {
			dialogOpen(view.getComposite().getShell(), managerName, id);
		}
		view.update();
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
					if(view.getSelectedNum() == 1) {
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
