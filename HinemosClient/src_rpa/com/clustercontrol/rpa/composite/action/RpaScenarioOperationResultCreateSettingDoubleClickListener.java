/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite.action;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.rpa.action.GetRpaScenarioOperationResultCreateSettingListTableDefine;
import com.clustercontrol.rpa.composite.RpaScenarioOperationResultCreateSettingListComposite;
import com.clustercontrol.rpa.dialog.RpaScenarioOperationResultCreateSettingDialog;

/**
 * RPA設定[シナリオ実績作成設定]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 */
public class RpaScenarioOperationResultCreateSettingDoubleClickListener implements IDoubleClickListener {
	/** RPA設定[シナリオ実績作成設定]ビュー用のコンポジット */
	private RpaScenarioOperationResultCreateSettingListComposite composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite RPA設定[シナリオ実績作成設定]ビュー用のコンポジット
	 */
	public RpaScenarioOperationResultCreateSettingDoubleClickListener(RpaScenarioOperationResultCreateSettingListComposite composite) {
		this.composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。
	 * RPA設定[シナリオ実績作成設定]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からRPAシナリオタグIDを取得します。</li>
	 * <li>RPAシナリオタグIDからRPAシナリオタグ情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.rpa.dialog.RpaScenarioOperationResultCreateSettingDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String settingId = null;

		//シナリオ実績作成設定IDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
					.getSelection()).getFirstElement();
			managerName = (String) info.get(GetRpaScenarioOperationResultCreateSettingListTableDefine.MANAGER_NAME);
			settingId = (String) info.get(GetRpaScenarioOperationResultCreateSettingListTableDefine.SETTING_ID);
		}

		if(settingId != null){
			// ダイアログを生成
			RpaScenarioOperationResultCreateSettingDialog dialog = null;
			dialog = new RpaScenarioOperationResultCreateSettingDialog(this.composite.getShell(), managerName, settingId, PropertyDefineConstant.MODE_MODIFY);
			
			// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				this.composite.update();
			}
		}
	}

}
