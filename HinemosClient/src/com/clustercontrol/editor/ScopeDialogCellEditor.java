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
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;

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
		FacilityTreeItemResponse item = null;
		if (dialog.getReturnCode() == ScopeTreeDialog.OK) {
			item = dialog.getSelectItem();
			if (item.getData().getNotReferFlg()
					|| item.getData().getFacilityType() == FacilityTypeEnum.COMPOSITE) {
				item = null;
			}
		}
		return item;
	}
}
