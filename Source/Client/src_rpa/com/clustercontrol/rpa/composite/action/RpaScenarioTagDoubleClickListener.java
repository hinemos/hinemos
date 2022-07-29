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
import com.clustercontrol.rpa.action.GetRpaScenarioTagListTableDefine;
import com.clustercontrol.rpa.composite.RpaScenarioTagListComposite;
import com.clustercontrol.rpa.dialog.RpaScenarioTagDialog;

/**
 * RPAシナリオタグビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 */
public class RpaScenarioTagDoubleClickListener implements IDoubleClickListener {
	/** RPAシナリオタグビュー用のコンポジット */
	private RpaScenarioTagListComposite composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite RPAシナリオタグビュー用のコンポジット
	 */
	public RpaScenarioTagDoubleClickListener(RpaScenarioTagListComposite composite) {
		this.composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。
	 * RPAシナリオタグビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からRPAシナリオタグIDを取得します。</li>
	 * <li>RPAシナリオタグIDからRPAシナリオタグ情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.rpa.dialog.RpaScenarioTagDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String tagId = null;

		//RPAシナリオタグIDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
					.getSelection()).getFirstElement();
			managerName = (String) info.get(GetRpaScenarioTagListTableDefine.MANAGER_NAME);
			tagId = (String) info.get(GetRpaScenarioTagListTableDefine.TAG_ID);
		}

		if(tagId != null){
			// ダイアログを生成
			RpaScenarioTagDialog dialog = null;
			dialog = new RpaScenarioTagDialog(this.composite.getShell(), managerName, tagId, PropertyDefineConstant.MODE_MODIFY);
			
			// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				this.composite.update();
			}
		}
	}

}
