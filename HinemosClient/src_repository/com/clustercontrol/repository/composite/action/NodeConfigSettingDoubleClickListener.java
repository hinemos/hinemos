/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.composite.action;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;

import com.clustercontrol.repository.action.GetNodeConfigSettingListTableDefine;
import com.clustercontrol.repository.composite.NodeConfigSettingInfoListComposite;
import com.clustercontrol.repository.dialog.NodeConfigSettingCreateDialog;

/**
 * リポジトリ[構成情報収集設定]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 * @version 6.2.0
 */
public class NodeConfigSettingDoubleClickListener implements IDoubleClickListener {
	/** リポジトリ[構成情報収集設定]ビュー用のコンポジット */
	private NodeConfigSettingInfoListComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite リポジトリ[構成情報収集設定]ビュー用のコンポジット
	 */
	public NodeConfigSettingDoubleClickListener(NodeConfigSettingInfoListComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * リポジトリ[構成情報収集設定]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行から構成情報収集設定IDを取得します。</li>
	 * <li>構成情報収集設定IDから構成情報収集設定を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.repository.dialog.NodeConfigSettingCreateDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String nodeConfigSettingId = null;

		//ファシリティIDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
					.getSelection()).getFirstElement();
			managerName = (String) info.get(GetNodeConfigSettingListTableDefine.MANAGER_NAME);
			nodeConfigSettingId = (String) info.get(GetNodeConfigSettingListTableDefine.GET_CONFIG_ID);
		}

		if(nodeConfigSettingId != null){
			// ダイアログを生成
			NodeConfigSettingCreateDialog dialog = 
					new NodeConfigSettingCreateDialog(m_composite.getShell(), managerName, nodeConfigSettingId, true);

			// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				m_composite.update();
			}
		}
	}

}
