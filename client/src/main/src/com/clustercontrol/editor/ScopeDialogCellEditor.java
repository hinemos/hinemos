/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.editor;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Control;

import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * スコープツリーダイアログセルエディタークラス<BR>
 *
 * @version 4.0.0
 * @since 1.0.0
 */
public class ScopeDialogCellEditor extends DialogCellEditor {

	private boolean selectNodeOnly = false;

	/**
	 *
	 * @param selectNodeOnly
	 */
	public ScopeDialogCellEditor(boolean selectNodeOnly) {
		super();
		this.selectNodeOnly = selectNodeOnly;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.swt.widgets.Control)
	 */
	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		//スコープツリーダイアログを表示する
		ScopeTreeDialog dialog = new ScopeTreeDialog(cellEditorWindow.getShell(), null, null);
		dialog.setSelectNodeOnly(selectNodeOnly);
		dialog.open();
		//選択したファシリティツリーアイテムを取得する
		FacilityTreeItem item = null;
		if (dialog.getReturnCode() == ScopeTreeDialog.OK) {
			item = dialog.getSelectItem();
			if (item.getData().isNotReferFlg()
					|| item.getData().getFacilityType() == FacilityConstant.TYPE_COMPOSITE) {
				item = null;
			}
		}
		return item;
	}
}
