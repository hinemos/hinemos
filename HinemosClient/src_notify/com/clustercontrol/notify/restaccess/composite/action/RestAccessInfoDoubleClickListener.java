/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.composite.action;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.notify.restaccess.action.GetRestAccessInfoListTableDefine;
import com.clustercontrol.notify.restaccess.composite.RestAccessInfoListComposite;
import com.clustercontrol.notify.restaccess.dialog.RestAccessInfoCreateDialog;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * [RESTアクセス情報]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 */
public class RestAccessInfoDoubleClickListener implements IDoubleClickListener {
	/** [RESTアクセス情報]ビュー用のコンポジット */
	private RestAccessInfoListComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite [RESTアクセス情報]ビュー用のコンポジット
	 */
	public RestAccessInfoDoubleClickListener(RestAccessInfoListComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * [RESTアクセス情報]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からRESTアクセス情報IDを取得します。</li>
	 * <li>RESTアクセスIDからRESTアクセス情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.notify.restaccess.dialog.RestAccessInfoCreateDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String RestAccessInfoId = null;

		//RESTアクセス情報IDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
					.getSelection()).getFirstElement();
			managerName = (String) info.get(GetRestAccessInfoListTableDefine.MANAGER_NAME);
			RestAccessInfoId = (String) info.get(GetRestAccessInfoListTableDefine.REST_ACCESS_ID);
		}

		if(RestAccessInfoId != null){
			// ダイアログを生成
			RestAccessInfoCreateDialog dialog = new RestAccessInfoCreateDialog(
					m_composite.getShell(), managerName, RestAccessInfoId,
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
