/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.composite.action;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.infra.action.GetInfraManagementTableDefine;
import com.clustercontrol.infra.dialog.InfraManagementDialog;

/**
 * 環境構築[構築・チェック]ビューまたは環境構築[モジュール]ビューのテーブルビューア用のDoubleClickListenerです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraManagementDoubleClickListener implements IDoubleClickListener {

	/** 環境構築[構築・チェック]ビュー、環境構築[モジュール]ビュー、環境構築[スケジュール予定]用のコンポジット */
	private Composite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite 環境構築[構築・チェック]ビュー、環境構築[モジュール]ビュー、環境構築[スケジュール予定]用のコンポジット
	 */
	public InfraManagementDoubleClickListener(Composite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * 環境構築[構築・チェック]ビュー、
	 * 環境構築[モジュール]ビュー
	 * のテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行から環境構築IDを取得します。</li>
	 * <li>環境構築IDから環境構築情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			String managerName = (String)((ArrayList<?>)((StructuredSelection) event.getSelection()).getFirstElement()).get(GetInfraManagementTableDefine.MANAGER_NAME);
			String managementId = (String)((ArrayList<?>)((StructuredSelection) event.getSelection()).getFirstElement()).get(GetInfraManagementTableDefine.MANAGEMENT_ID);

			if(managementId != null){
				// 環境構築[構築・チェックの作成・変更]ダイアログを開く
				InfraManagementDialog dialog = new InfraManagementDialog(
						m_composite.getShell(), managerName, managementId,
						PropertyDefineConstant.MODE_MODIFY);
				// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_composite.update();
				}
			}
		}
	}

}
