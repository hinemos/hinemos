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

package com.clustercontrol.repository.composite.action;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;

import com.clustercontrol.repository.action.GetNodeListTableDefine;
import com.clustercontrol.repository.composite.NodeListComposite;
import com.clustercontrol.repository.dialog.NodeCreateDialog;

/**
 * リポジトリ[ノード]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 * @version 4.1.0
 */
public class NodeDoubleClickListener implements IDoubleClickListener {
	/** リポジトリ[ノード]ビュー用のコンポジット */
	private NodeListComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite リポジトリ[ノード]ビュー用のコンポジット
	 */
	public NodeDoubleClickListener(NodeListComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * リポジトリ[ノード]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からファシリティIDを取得します。</li>
	 * <li>ファシリティIDからノード情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.repository.dialog.NodeCreateDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String facilityId = null;

		//ファシリティIDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
					.getSelection()).getFirstElement();
			managerName = (String) info.get(GetNodeListTableDefine.MANAGER_NAME);
			facilityId = (String) info.get(GetNodeListTableDefine.FACILITY_ID);
		}

		if(facilityId != null){
			// ダイアログを生成
			NodeCreateDialog dialog = new NodeCreateDialog(m_composite.getShell(), managerName, facilityId, true);

			// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				m_composite.update();
			}
		}
	}

}
