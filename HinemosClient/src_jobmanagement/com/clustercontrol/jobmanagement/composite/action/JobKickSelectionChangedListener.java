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
import org.openapitools.client.model.JobKickFilterInfoRequest.JobkickTypeEnum;

import com.clustercontrol.jobmanagement.action.GetJobKickTableDefine;
import com.clustercontrol.jobmanagement.bean.JobKickTypeMessage;
import com.clustercontrol.jobmanagement.composite.JobKickListComposite;
import com.clustercontrol.jobmanagement.view.JobKickListView;

/**
 * ジョブ[実行契機]ビューのテーブルビューア用のSelectionChangedListenerです。
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class JobKickSelectionChangedListener implements ISelectionChangedListener {
	/** ログ */
	private static Log m_log = LogFactory.getLog(JobKickSelectionChangedListener.class);
	/** ジョブ[スケジュール]ビュー用のコンポジット */
	private JobKickListComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite ジョブ[実行契機]ビュー用のコンポジット
	 */
	public JobKickSelectionChangedListener(JobKickListComposite composite) {
		m_composite = composite;
	}

	/**
	 * 選択変更時に呼び出されます。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントから選択アイテムを取得します。</li>
	 * <li>選択アイテムをジョブ[スケジュール]ビュー用のコンポジットに設定します。</li>
	 * </ol>
	 *
	 * @param event 選択変更イベント
	 *
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ArrayList<ArrayList<?>> info = new ArrayList<ArrayList<?>>();
		List<?> list;
		JobkickTypeEnum jobKickType = null;

		//ジョブ[実行契機]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(JobKickListView.ID);

		//選択行を取得
		StructuredSelection selection = (StructuredSelection) event.getSelection();

		if (selection.getFirstElement() != null) {
			list = ((StructuredSelection) event.getSelection()).toList();
			for(Object obj : list) {
				if(obj instanceof ArrayList) {
					ArrayList<?> item = (ArrayList<?>)obj;
					jobKickType = JobkickTypeEnum.valueOf(
							JobKickTypeMessage.stringToTypeEnumValue((String)item.get(GetJobKickTableDefine.TYPE)));
					info.add(item);
				}
			}

			if (viewPart != null) {
				JobKickListView view = (JobKickListView) viewPart.getAdapter(JobKickListView.class);
				if (view == null) {
					m_log.info("selection changed: view is null"); 
					return;
				}
				//選択アイテムの確認/未確認の種別でボタン（アクション）の使用可/不可を設定する
				view.setEnabledAction(selection.size(), jobKickType, selection);
			}
		} else {
			if (viewPart != null) {
				JobKickListView view = (JobKickListView) viewPart.getAdapter(JobKickListView.class);
				if (view == null) {
					m_log.info("selection changed: view is null"); 
					return;
				}
				//選択アイテムの確認/未確認の種別でボタン（アクション）の使用可/不可を設定する
				view.setEnabledAction(0, jobKickType, selection);
			}
		}

		m_composite.setSelectItem(info);
	}
}

