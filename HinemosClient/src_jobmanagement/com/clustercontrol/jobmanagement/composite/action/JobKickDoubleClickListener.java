/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.composite.action;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.action.GetJobKickTableDefine;
import com.clustercontrol.jobmanagement.bean.JobKickTypeMessage;
import com.clustercontrol.jobmanagement.composite.JobKickListComposite;
import com.clustercontrol.jobmanagement.dialog.JobKickDialog;
import com.clustercontrol.jobmanagement.view.JobKickListView;

/**
 * ジョブ[実行契機]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 * @version 4.1.0
 */
public class JobKickDoubleClickListener implements IDoubleClickListener {
	/** ログ */
	private static Log m_log = LogFactory.getLog(JobKickDoubleClickListener.class);

	/**
	 * コンストラクタ
	 *
	 * @param jobKickListComposite ジョブ[実行契機]ビュー用のコンポジット
	 */
	public JobKickDoubleClickListener(JobKickListComposite jobKickListComposite) {
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * ジョブ[実行契機]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からジョブIDを取得します。</li>
	 * <li>ジョブ[実行契機]ビュー用のコンポジットからジョブツリーアイテムを取得します。</li>
	 * <li>取得したジョブツリーアイテムから、ジョブIDが一致するジョブツリーアイテムを取得します。</li>
	 * <li>ジョブ[実行契機]ビュー用のコンポジットに、ジョブIDが一致するジョブツリーアイテムを設定します。</li>
	 * <li>ジョブIDからジョブ情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.jobmanagement.dialog.JobDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {

		//ジョブ[実行契機]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(JobKickListView.ID);

		if (viewPart instanceof JobKickListView) {
			JobKickListView view = (JobKickListView) viewPart
					.getAdapter(JobKickListView.class);
			if (view == null) {
				m_log.info("double click: view is null"); 
				return;
			}
			JobKickListComposite composite = view.getComposite();

			ArrayList<?> item = composite.getSelectItem();
			if (item != null) {
				String id = view.getSelectedIdList().get(0);
				ArrayList<?> list = (ArrayList<?>)item.get(0);
				String managerName = (String) list.get(GetJobKickTableDefine.MANAGER_NAME);
				String type = (String) list.get(GetJobKickTableDefine.TYPE);
				int TypeNum = JobKickTypeMessage.stringToType(type);
				JobKickDialog dialog = new JobKickDialog(PlatformUI
						.getWorkbench().getActiveWorkbenchWindow().getShell(), managerName, id, TypeNum, PropertyDefineConstant.MODE_MODIFY);
				dialog.open();
			}
			view.update();
		}
	}
}
