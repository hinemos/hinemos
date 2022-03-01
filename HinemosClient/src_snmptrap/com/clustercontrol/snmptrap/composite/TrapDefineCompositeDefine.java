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
import org.openapitools.client.model.TrapValueInfoResponse;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.util.TableItemManager;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;
import com.clustercontrol.snmptrap.action.GetTrapDefineTableDefine;
import com.clustercontrol.snmptrap.dialog.CreateTrapDefineDialog;

public class TrapDefineCompositeDefine implements ITableItemCompositeDefine<TrapValueInfoResponse> {

	/** 現在有効なダイアログ */
	private CreateTrapDefineDialog dialog = null;

	/** テーブルアイテムの管理クラス */
	private TableItemManager<TrapValueInfoResponse> manager = null;

	@Override
	public TrapValueInfoResponse getCurrentCreatedItem() {
		if(dialog != null){
			return dialog.getInputData();
		}

		return null;
	}

	@Override
	public CommonDialog createDialog(Shell shell) {
		dialog = new CreateTrapDefineDialog(shell);
		return dialog;
	}

	@Override
	public CommonDialog createDialog(Shell shell, TrapValueInfoResponse item) {
		dialog = new CreateTrapDefineDialog(shell, item);
		return dialog;
	}

	@Override
	public ArrayList<?> getTableDefine() {
		return GetTrapDefineTableDefine.get();
	}

	@Override
	public TableItemManager<TrapValueInfoResponse> getTableItemInfoManager() {
		return manager;
	}

	@Override
	public void initTableItemInfoManager() {
		manager = new TableItemManager<>();
	}

	@Override
	public void initTableItemInfoManager(List<TrapValueInfoResponse> items) {
		manager = new TableItemManager<>(items);
	}

	@Override
	public int getButtonOptions() {
		return ADD | MODIFY | DELETE | COPY;
	}

	@Override
	public String getItemsIdentifier(TrapValueInfoResponse item) {
		return item.getUei();
	}

	@Override
	public CommonTableLabelProvider<TrapValueInfoResponse> getLabelProvider() {
		return new TrapDefineTableLabelProvider(this);
	}

	@Override
	public int indexOf(TrapValueInfoResponse item) {
		if(this.manager != null){
			return this.manager.indexOf(item);
		}
		return -1;
	}

}
