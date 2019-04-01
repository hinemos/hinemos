/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite.action;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ErrorViewPart;

import com.clustercontrol.jobmanagement.action.GetHistoryTableDefine;
import com.clustercontrol.jobmanagement.composite.HistoryComposite;
import com.clustercontrol.jobmanagement.view.JobDetailView;
import com.clustercontrol.jobmanagement.view.JobHistoryView;
import com.clustercontrol.jobmanagement.view.JobMapViewIF;
import com.clustercontrol.util.ViewUtil;

/**
 * ジョブ[履歴]ビューのテーブルビューア用のSelectionChangedListenerです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class HistorySelectionChangedListener implements ISelectionChangedListener {

	// ログ
	private static Log m_log = LogFactory.getLog( HistorySelectionChangedListener.class );

	/** ジョブ[履歴]ビュー用のコンポジット */
	private HistoryComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite ジョブ[履歴]ビュー用のコンポジット
	 */
	public HistorySelectionChangedListener(HistoryComposite composite) {
		m_composite = composite;
	}

	/**
	 * 選択変更時に呼び出されます。<BR>
	 * ジョブ[履歴]ビューのテーブルビューアを選択した際に、選択した行の内容で
	 * ジョブ[ジョブ詳細]ビューとジョブマップ[履歴]を更新します。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントから選択行を取得し、選択行からセッションIDとジョブIDを取得します。</li>
	 * <li>セッションIDとジョブIDをジョブ[履歴]ビュー用のコンポジットに設定します。</li>
	 * <li>ジョブ[ジョブ詳細]ビューを更新します。</li>
	 * </ol>
	 *
	 * @param event 選択変更イベント
	 *
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		String managerName = null;
		String sessionId = null;
		String jobunitId = null;
		String jobId = null;

		//セッションIDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
					.getSelection()).getFirstElement();
			managerName = (String) info.get(GetHistoryTableDefine.MANAGER_NAME);
			sessionId = (String) info.get(GetHistoryTableDefine.SESSION_ID);
			jobunitId = (String) info.get(GetHistoryTableDefine.JOBUNIT_ID);
			jobId = (String) info.get(GetHistoryTableDefine.JOB_ID);
			//マネージャ名を設定
			m_composite.setManagerName(managerName);
			//セッションIDを設定
			m_composite.setSessionId(sessionId);
			//ジョブIDを設定
			m_composite.setJobId(jobId);
			//ジョブユニットIDを設定
			m_composite.setJobunitId(jobunitId);
		}

		String viewClass = m_composite.getView().getClass().getSimpleName();
		//アクティブページを手に入れる
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		if (JobHistoryView.class.getSimpleName().equals(viewClass)) {
			//ジョブ[詳細]ビューを更新する
			m_log.debug("selectionChanged() : job detail update");
			IViewPart viewPartDetail = page.findView(JobDetailView.ID);
			if (viewPartDetail != null) {
				JobDetailView view = (JobDetailView) viewPartDetail
						.getAdapter(JobDetailView.class);
				if (view == null) {
					m_log.info("selection changed: view is null"); 
					return;
				}
				view.update(managerName, sessionId, jobunitId);
				// 同時実行制御ビューに隠れている可能性があるのでアクティブ化が必要
				ViewUtil.activate(JobDetailView.class);
				// アクティブなビューを戻しておかないと、その後の処理に影響がある
				ViewUtil.activate(JobHistoryView.class);
			}
		} else {
			//ジョブマップ[履歴]ビューを更新する
			m_log.debug("selectionChanged() : jobmap detail update");
			IViewReference viewReference = page.findViewReference("com.clustercontrol.jobmap.view.JobMapHistoryView");
			if (viewReference == null){
				return;
			}
			IViewPart viewPart = viewReference.getView(false);
			if (viewPart != null && !(viewPart instanceof ErrorViewPart)) {
				m_log.debug("viewPart " + viewPart.getClass().getName());
				JobMapViewIF view = (JobMapViewIF) viewPart;
				view.update(managerName, sessionId, null);
			}
		}
	}
}

