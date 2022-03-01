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
import org.openapitools.client.model.PatternResponse;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.http.dialog.PatternCreateDialog;
import com.clustercontrol.http.viewer.GetPatternTableDefine;
import com.clustercontrol.http.viewer.PatternTableLabelProvider;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.util.TableItemManager;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;

public class PatternCompositeDefine implements ITableItemCompositeDefine<PatternResponse> {

	/** 現在有効なダイアログ */
	private PatternCreateDialog dialog = null;

	/** テーブルアイテムの管理クラス */
	private TableItemManager<PatternResponse> manager = null;

	@Override
	public PatternResponse getCurrentCreatedItem() {
		if(dialog != null){
			return dialog.getInputData();
		}

		return null;
	}

	@Override
	public CommonDialog createDialog(Shell shell) {
		dialog = new PatternCreateDialog(shell);
		return dialog;
	}

	@Override
	public CommonDialog createDialog(Shell shell, PatternResponse item) {
		dialog = new PatternCreateDialog(shell, item);
		return dialog;
	}

	@Override
	public ArrayList<?> getTableDefine() {
		return GetPatternTableDefine.get();
	}

	@Override
	public TableItemManager<PatternResponse> getTableItemInfoManager() {
		return manager;
	}

	@Override
	public void initTableItemInfoManager() {
		manager = new TableItemManager<>();
	}

	@Override
	public void initTableItemInfoManager(List<PatternResponse> items) {
		manager = new TableItemManager<>(items);
	}

	@Override
	public int getButtonOptions() {
		return ADD | MODIFY | DELETE | COPY | SPACE | UP | DOWN;
	}

	@Override
	public String getItemsIdentifier(PatternResponse item) {
		return item.getPattern();
	}

	@Override
	public CommonTableLabelProvider<PatternResponse> getLabelProvider() {
		return new PatternTableLabelProvider(this);
	}

	@Override
	public int indexOf(PatternResponse item) {
		if(this.manager != null){
			return this.manager.indexOf(item);
		}
		return -1;
	}

}
