/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.composite.action;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.maintenance.action.GetMaintenanceListTableDefine;
import com.clustercontrol.maintenance.composite.MaintenanceListComposite;
import com.clustercontrol.maintenance.dialog.MaintenanceDialog;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * メンテナンス[履歴情報削除]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 * @version 4.1.0
 */
public class MaintenanceDoubleClickListener implements IDoubleClickListener {
	/** メンテナンス[履歴情報削除]ビュー用のコンポジット */
	private MaintenanceListComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite メンテナンス[履歴情報削除]ビュー用のコンポジット
	 */
	public MaintenanceDoubleClickListener(MaintenanceListComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * メンテナンス[履歴情報削除]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からメンテナンスIDを取得します。</li>
	 * <li>メンテナンスIDからメンテナンス情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.maintenance.dialog.MaintenanceDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String maintenanceId = null;

		//メンテナンスIDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
					.getSelection()).getFirstElement();
			managerName = (String) info.get(GetMaintenanceListTableDefine.MANAGER_NAME);
			maintenanceId = (String) info.get(GetMaintenanceListTableDefine.MAINTENANCE_ID);
		}

		if(maintenanceId != null){
			// ダイアログを生成
			MaintenanceDialog dialog = new MaintenanceDialog(
					m_composite.getShell(), managerName, maintenanceId,
					PropertyDefineConstant.MODE_MODIFY);

			// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				Table table = m_composite.getTableViewer().getTable();
				WidgetTestUtil.setTestId(this, null, table);
				int selectIndex = table.getSelectionIndex();
				m_composite.update();
				table.setSelection(selectIndex);
			}
		}
	}

}
