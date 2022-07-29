/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.VarBindPatternResponse;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.util.TableItemManager;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;
import com.clustercontrol.snmptrap.action.GetVarBindPatternTableDefine;
import com.clustercontrol.snmptrap.dialog.CreateVarBindPatternDialog;

public class VarBindPatternCompositeDefine implements ITableItemCompositeDefine<VarBindPatternResponse> {

	/** 現在有効なダイアログ */
	private CreateVarBindPatternDialog dialog = null;

	/** テーブルアイテムの管理クラス */
	private TableItemManager<VarBindPatternResponse> manager = null;

	@Override
	public VarBindPatternResponse getCurrentCreatedItem() {
		if(dialog != null){
			return dialog.getInputData();
		}

		return null;
	}

	@Override
	public CommonDialog createDialog(Shell shell) {
		dialog = new CreateVarBindPatternDialog(shell);
		return dialog;
	}

	@Override
	public CommonDialog createDialog(Shell shell, VarBindPatternResponse item) {
		dialog = new CreateVarBindPatternDialog(shell, item);
		return dialog;
	}

	@Override
	public ArrayList<?> getTableDefine() {
		return GetVarBindPatternTableDefine.get();
	}

	@Override
	public TableItemManager<VarBindPatternResponse> getTableItemInfoManager() {
		return manager;
	}

	@Override
	public void initTableItemInfoManager() {
		manager = new TableItemManager<>();
	}

	@Override
	public void initTableItemInfoManager(List<VarBindPatternResponse> items) {
		manager = new TableItemManager<>(items);
	}

	@Override
	public int getButtonOptions() {
		return ADD | MODIFY | DELETE | COPY | SPACE | UP | DOWN;
	}

	@Override
	public String getItemsIdentifier(VarBindPatternResponse item) {
		return item.getPattern();
	}

	@Override
	public CommonTableLabelProvider<VarBindPatternResponse> getLabelProvider() {
		return new VarBindPatternTableLabelProvider(this);
	}

	@Override
	public int indexOf(VarBindPatternResponse item) {
		if(this.manager != null){
			return this.manager.indexOf(item);
		}
		return -1;
	}

}
