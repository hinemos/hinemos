/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.jobmanagement.action.GetJobTableDefine;
import com.clustercontrol.jobmanagement.composite.JobListComposite;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.view.JobListView;

/**
 * ジョブ[一覧]ビューのテーブルビューア用のSelectionChangedListenerです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobListSelectionChangedListener implements ISelectionChangedListener {
	/** ログ */
	private static Log m_log = LogFactory.getLog(JobListSelectionChangedListener.class);
	/** ジョブ[一覧]ビュー用のコンポジット */
	private JobListComposite m_list;

	/**
	 * コンストラクタ
	 *
	 * @param list ジョブ[一覧]ビュー用のコンポジット
	 */
	public JobListSelectionChangedListener(JobListComposite list) {
		m_list = list;
	}

	/**
	 * 選択変更時に呼び出されます。<BR>
	 * ジョブ[一覧]ビューのテーブルビューアを選択した際に、選択した行の内容でビューのアクションの有効・無効を設定します。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントから選択行を取得し、選択行からジョブIDを取得します。</li>
	 * <li>ジョブ[一覧]ビュー用のコンポジットからジョブツリーアイテムを取得します。</li>
	 * <li>取得したジョブツリーアイテムから、ジョブIDが一致するジョブツリーアイテムを取得します。</li>
	 * <li>ジョブ[一覧]ビュー用のコンポジットに、ジョブIDが一致するジョブツリーアイテムを設定します。</li>
	 * <li>ジョブ[一覧]ビューのアクションの有効・無効を設定します。</li>
	 * </ol>
	 *
	 * @param event 選択変更イベント
	 *
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		//ジョブ[登録]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(JobListView.ID);

		//選択アイテムを取得
		StructuredSelection selection = (StructuredSelection) event.getSelection();

		if ( viewPart != null && selection != null ){
			JobListView view = (JobListView) viewPart.getAdapter(JobListView.class);

			if (view == null) {
				m_log.info("selection changed: view is null"); 
				return;
			}

			// Set last focus
			JobListComposite composite = view.getJobListComposite();
			if( composite != null && composite.getTable().isFocusControl() ){
				view.setLastFocusComposite( composite );
			}

			JobTreeItemWrapper selectJobTreeItem = null;

			List<?> list = selection.toList();
			List<JobTreeItemWrapper> itemList = new ArrayList<JobTreeItemWrapper>();
			for(Object obj : list) {
				if (obj instanceof ArrayList) {
					ArrayList<?> item = (ArrayList<?>)obj;
					String jobId = (String) item.get(GetJobTableDefine.JOB_ID);

					if (m_list.getJobTreeItem() != null) {
						List<JobTreeItemWrapper> items = m_list.getJobTreeItem().getChildren();

						for (int i = 0; i < items.size(); i++) {
							if (jobId.equals(items.get(i).getData().getId())) {
								selectJobTreeItem = items.get(i);
								break;
							}
						}
						itemList.add(selectJobTreeItem);
					}
				}
			}

			view.setEnabledAction(selectJobTreeItem, itemList, false);
		}
	}
}
