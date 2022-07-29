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
import com.clustercontrol.rpa.action.GetRpaScenarioListTableDefine;
import com.clustercontrol.rpa.composite.RpaScenarioListComposite;
import com.clustercontrol.rpa.dialog.RpaScenarioDialog;

/**
 * RPAシナリオビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 */
public class RpaScenarioDoubleClickListener implements IDoubleClickListener {
	/** RPAシナリオビュー用のコンポジット */
	private RpaScenarioListComposite composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite RPAシナリオビュー用のコンポジット
	 */
	public RpaScenarioDoubleClickListener(RpaScenarioListComposite composite) {
		this.composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。
	 * RPAシナリオビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からRPAシナリオIDを取得します。</li>
	 * <li>RPAシナリオIDからRPAシナリオ情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.rpa.dialog.RpaScenarioDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = "";
		String scenarioId = "";

		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event.getSelection()).getFirstElement();

			managerName = (String)info.get(GetRpaScenarioListTableDefine.MANAGER_NAME);
			scenarioId = (String) info.get(GetRpaScenarioListTableDefine.SCENARIO_ID);
		}

		if(scenarioId != null){
			// ダイアログを生成
			RpaScenarioDialog dialog = null;
			dialog = new RpaScenarioDialog(this.composite.getShell(), managerName, scenarioId, PropertyDefineConstant.MODE_MODIFY);
			
			// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				this.composite.update();
			}
		}
	}

}
