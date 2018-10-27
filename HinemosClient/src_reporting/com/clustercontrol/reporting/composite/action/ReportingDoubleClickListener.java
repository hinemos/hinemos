/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.composite.action;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.reporting.action.GetReportingScheduleTableDefine;
import com.clustercontrol.reporting.composite.ReportingScheduleListComposite;
import com.clustercontrol.reporting.dialog.ReportingScheduleDialog;

/**
 * レポーティング[スケジュール]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 * 
 * @version 5.0.a
 */
public class ReportingDoubleClickListener implements IDoubleClickListener {
	/** レポーティング[スケジュール]ビュー用のコンポジット */
	private ReportingScheduleListComposite m_composite;

	/**
	 * コンストラクタ
	 * 
	 * @param composite
	 *            レポーティング[スケジュール]ビュー用のコンポジット
	 */
	public ReportingDoubleClickListener(ReportingScheduleListComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * レポーティング[スケジュール]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からスケジュールIDを取得します。</li>
	 * <li>メンテナンスIDからメンテナンス情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 * 
	 * @param event
	 *            イベント
	 * 
	 * @see com.clustercontrol.reporting.dialog.ReportingScheduleDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String scheduleId = null;

		// スケジュールIDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
					.getSelection()).getFirstElement();
			managerName = (String) info.get(GetReportingScheduleTableDefine.MANAGER_NAME); 
			scheduleId = (String) info.get(GetReportingScheduleTableDefine.REPORT_SCHEDULE_ID);
		}

		if (scheduleId != null) {
			// ダイアログを生成
			ReportingScheduleDialog dialog = new ReportingScheduleDialog(
					m_composite.getShell(), managerName, scheduleId,
					PropertyDefineConstant.MODE_MODIFY);

			// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				Table table = m_composite.getTableViewer().getTable();
				int selectIndex = table.getSelectionIndex();
				m_composite.update();
				table.setSelection(selectIndex);
			}
		}
	}

}
