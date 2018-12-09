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

import com.clustercontrol.jobmanagement.composite.JobTreeComposite;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブ[一覧]ビューのツリービューア用のSelectionChangedListenerです。
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class JobTreeSelectionChangedListener implements ISelectionChangedListener {
	/** ログ */
	private static Log m_log = LogFactory.getLog(JobTreeSelectionChangedListener.class);
	/**
	 * 選択変更時に呼び出されます。<BR>
	 * ジョブ[一覧]ビューのツリービューアを選択した際に、<BR>
	 * 選択したアイテムの内容でジョブ[一覧]ビューのテーブルビューアを更新し、<BR>
	 * 選択したアイテムの内容でジョブ[一覧]ビューのアクションの有効・無効を設定します。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントからジョブツリーアイテムを取得します。</li>
	 * <li>ジョブ[一覧]ビューのツリービューア用のコンポジットに、ジョブツリーアイテムを設定します。</li>
	 * <li>ジョブ[一覧]ビューのアクションの有効・無効を設定します。</li>
	 * <li>ジョブ[一覧]ビューのテーブルビューア用のコンポジットを更新します。</li>
	 * </ol>
	 *
	 * @param event 選択変更イベント
	 *
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		JobTreeItem selectItem = null;

		//ジョブ[登録]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(JobListView.ID);

		//選択アイテムを取得
		StructuredSelection selection = (StructuredSelection) event.getSelection();

		if (viewPart != null && selection != null) {
			selectItem = (JobTreeItem) selection.getFirstElement();
			List<?> list = selection.toList();
			List<JobTreeItem> itemList = new ArrayList<JobTreeItem>();
			for(Object obj : list) {
				if(obj instanceof JobTreeItem) {
					itemList.add((JobTreeItem)obj);
				}
			}

			JobListView view = (JobListView) viewPart.getAdapter(JobListView.class);

			if (view == null) {
				m_log.info("selection changed: view is null"); 
				return;
			}

			// Set last focus
			JobTreeComposite composite = view.getJobTreeComposite();
			if( composite != null && composite.getTree().isFocusControl() ){
				view.setLastFocusComposite( composite );
			}

			view.setEnabledAction( selectItem, itemList, true );
		}

	}
}
