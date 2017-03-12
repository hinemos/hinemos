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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;
import com.clustercontrol.maintenance.HinemosPropertyTypeMessage;
import com.clustercontrol.maintenance.action.GetHinemosPropertyTableDefine;
import com.clustercontrol.maintenance.composite.HinemosPropertyComposite;
import com.clustercontrol.maintenance.dialog.HinemosPropertyDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.maintenance.HinemosPropertyInfo;

/**
 * メンテナンス[共通設定]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 * @version 5.0.0
 */
public class HinemosPropertyDoubleClickListener implements IDoubleClickListener {
	/** メンテナンス[共通設定]ビュー用のコンポジット */
	private final HinemosPropertyComposite m_composite;

	// ログ
	private static Log m_log = LogFactory.getLog( HinemosPropertyDoubleClickListener.class );

	/**
	 * コンストラクタ
	 *
	 * @param composite メンテナンス[共通設定]ビュー用のコンポジット
	 */
	public HinemosPropertyDoubleClickListener(HinemosPropertyComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * メンテナンス[共通設定]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行から共通設定情報を取得します。</li>
	 * <li>取得した共通設定情報をもとに、ダイアログ表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.maintenance.dialog.HinemosPropertyDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void doubleClick(DoubleClickEvent event) {

		ArrayList list;
		HinemosPropertyInfo info = new HinemosPropertyInfo();
		String managerName = null;
		int valueType = 0;

		//共通設定情報を取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			list = (ArrayList) ((StructuredSelection) event.getSelection()).getFirstElement();
		} else {
			return;
		}

		info.setKey((String)list.get(GetHinemosPropertyTableDefine.KEY));
		managerName = (String)list.get(GetHinemosPropertyTableDefine.MANAGER_NAME);
		valueType = HinemosPropertyTypeMessage.stringToType((String)list.get(GetHinemosPropertyTableDefine.VALUE_TYPE));
		info.setValueType(valueType);

		if (valueType == HinemosPropertyTypeConstant.TYPE_STRING) {
			String value = (String)list.get(GetHinemosPropertyTableDefine.VALUE);
			info.setValueString(value);
		} else if (valueType == HinemosPropertyTypeConstant.TYPE_NUMERIC) {
			Object val = list.get(GetHinemosPropertyTableDefine.VALUE);
			try {
				if (val != null) {
					info.setValueNumeric(Long.parseLong(val.toString()));
				} else {
					info.setValueNumeric(null);
				}
			} catch (NumberFormatException e) {
				m_log.info("run() setValueNumeric(), " + e.getMessage());
				Object[] args = {Messages.getString("hinemos.property.key"), Long.MIN_VALUE, Long.MAX_VALUE};
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.common.4", args));
			}
		} else {
			boolean value = Boolean.parseBoolean((String)list.get(GetHinemosPropertyTableDefine.VALUE));
			info.setValueBoolean(value);
		}
		info.setDescription((String)list.get(GetHinemosPropertyTableDefine.DESCRIPTION));

		// ダイアログを生成
		HinemosPropertyDialog dialog = new HinemosPropertyDialog(
				m_composite.getShell(), managerName, valueType,
				PropertyDefineConstant.MODE_MODIFY, info);
		// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
		if (dialog.open() == IDialogConstants.OK_ID) {
			Table table = m_composite.getTableViewer().getTable();
			WidgetTestUtil.setTestId(this, null, table);
			int selectIndex = table.getSelectionIndex();
			m_composite.update();
			table.setSelection(selectIndex);
		}
	}

}
