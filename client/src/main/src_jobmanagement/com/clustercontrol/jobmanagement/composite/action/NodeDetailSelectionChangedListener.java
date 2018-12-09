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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.jobmanagement.action.GetNodeDetailTableDefine;
import com.clustercontrol.jobmanagement.composite.NodeDetailComposite;
import com.clustercontrol.jobmanagement.view.JobNodeDetailView;

/**
 * ジョブ[ノード詳細]ビューのテーブルビューア用のSelectionChangedListenerです。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class NodeDetailSelectionChangedListener implements ISelectionChangedListener {
	/** ログ */
	private static Log m_log = LogFactory.getLog(NodeDetailSelectionChangedListener.class);
	/** ジョブ[ノード詳細]ビュー用のコンポジット */
	private NodeDetailComposite m_composite;

	/**
	 * コンストラクタ
	 * 
	 * @param composite ジョブ[ノード詳細]ビュー用のコンポジット
	 */
	public NodeDetailSelectionChangedListener(NodeDetailComposite composite) {
		m_composite = composite;
	}

	/**
	 * 選択変更時に呼び出されます。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントから選択行を取得し、選択行からファシリティIDを取得します。</li>
	 * <li>ファシリティIDをジョブ[ノード詳細]ビュー用のコンポジットに設定します。</li>
	 * </ol>
	 * 
	 * @param event 選択変更イベント
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		String facilityId = null;

		//セッションIDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
					.getSelection()).getFirstElement();

			facilityId = (String) info.get(GetNodeDetailTableDefine.FACILITY_ID);
		}

		//アクティブページを手に入れる
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(JobNodeDetailView.ID);
		//ジョブ[ノード詳細]ビューのボタン（アクション）の使用可/不可を設定する
		if (viewPart != null && event.getSelection() != null) {
			JobNodeDetailView view = (JobNodeDetailView) viewPart.getAdapter(JobNodeDetailView.class);
			if (view == null) {
				m_log.info("selection changed: view is null");
				return;
			}
			view.setEnabledAction(((StructuredSelection)event.getSelection()).size(), event.getSelection());
		}

		m_composite.setFacilityId(facilityId);
	}
}

