/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.VariableResponse;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.http.dialog.VariableCreateDialog;
import com.clustercontrol.http.viewer.GetVariableTableDefine;
import com.clustercontrol.http.viewer.VariableTableLabelProvider;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.util.TableItemManager;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;

public class VariableCompositeDefine implements ITableItemCompositeDefine<VariableResponse> {

	/** 現在有効なダイアログ */
	private VariableCreateDialog dialog = null;

	/** テーブルアイテムの管理クラス */
	private TableItemManager<VariableResponse> manager = null;

	@Override
	public VariableResponse getCurrentCreatedItem() {
		if(dialog != null){
			return dialog.getInputData();
		}

		return null;
	}

	@Override
	public CommonDialog createDialog(Shell shell) {
		dialog = new VariableCreateDialog(shell);
		return dialog;
	}

	@Override
	public CommonDialog createDialog(Shell shell, VariableResponse item) {
		dialog = new VariableCreateDialog(shell, item);
		return dialog;
	}

	@Override
	public ArrayList<?> getTableDefine() {
		return GetVariableTableDefine.get();
	}

	@Override
	public TableItemManager<VariableResponse> getTableItemInfoManager() {
		return manager;
	}

	@Override
	public void initTableItemInfoManager() {
		manager = new TableItemManager<>();
	}

	@Override
	public void initTableItemInfoManager(List<VariableResponse> items) {
		manager = new TableItemManager<>(items);
	}

	@Override
	public int getButtonOptions() {
		return ADD | MODIFY | DELETE;
	}

	@Override
	public String getItemsIdentifier(VariableResponse item) {
		return item.getName();
	}

	@Override
	public CommonTableLabelProvider<VariableResponse> getLabelProvider() {
		return new VariableTableLabelProvider(this);
	}

	@Override
	public int indexOf(VariableResponse item) {
		if(this.manager != null){
			return this.manager.indexOf(item);
		}
		return -1;
	}

}
