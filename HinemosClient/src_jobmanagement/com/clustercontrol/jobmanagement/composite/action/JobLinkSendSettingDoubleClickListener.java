/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite.action;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.action.GetJobLinkSendSettingTableDefine;
import com.clustercontrol.jobmanagement.dialog.JobLinkSendSettingDialog;
import com.clustercontrol.jobmanagement.view.JobLinkSendSettingListView;

/**
 * ジョブ設定[ジョブ連携送信設定]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 */
public class JobLinkSendSettingDoubleClickListener implements IDoubleClickListener {

	/** ジョブ設定[ジョブ連携送信設定]ビュー用のコンポジット */
	private Composite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite ジョブ設定[ジョブ連携送信設定]ビュー用のコンポジット
	 */
	public JobLinkSendSettingDoubleClickListener(Composite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * ジョブ設定[ジョブ連携送信設定]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 *
	 * @param event イベント
	 *
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {

		//ジョブ設定[ジョブ連携送信設定]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(JobLinkSendSettingListView.ID);

		if (viewPart instanceof JobLinkSendSettingListView) {
			String managerName = null;
			String id = null;

			//ジョブ連携送信設定IDを取得
			if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
				ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event.getSelection()).getFirstElement();
				managerName = (String) info.get(GetJobLinkSendSettingTableDefine.MANAGER_NAME);
				id = (String) info.get(GetJobLinkSendSettingTableDefine.JOBLINK_SEND_SETTING_ID);
			}

			if(id != null){
				JobLinkSendSettingDialog dialog = new JobLinkSendSettingDialog(PlatformUI
						.getWorkbench().getActiveWorkbenchWindow().getShell(), managerName, id, PropertyDefineConstant.MODE_MODIFY);
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_composite.update();
				}
			}
		}
	}
}
