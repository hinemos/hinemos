package com.clustercontrol.http.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.http.dialog.VariableCreateDialog;
import com.clustercontrol.http.viewer.GetVariableTableDefine;
import com.clustercontrol.http.viewer.VariableTableLabelProvider;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.util.TableItemManager;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;
import com.clustercontrol.ws.monitor.Variable;

public class VariableCompositeDefine implements ITableItemCompositeDefine<Variable> {

	/** 現在有効なダイアログ */
	private VariableCreateDialog dialog = null;

	/** テーブルアイテムの管理クラス */
	private TableItemManager<Variable> manager = null;

	@Override
	public Variable getCurrentCreatedItem() {
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
	public CommonDialog createDialog(Shell shell, Variable item) {
		dialog = new VariableCreateDialog(shell, item);
		return dialog;
	}

	@Override
	public ArrayList<?> getTableDefine() {
		return GetVariableTableDefine.get();
	}

	@Override
	public TableItemManager<Variable> getTableItemInfoManager() {
		return manager;
	}

	@Override
	public void initTableItemInfoManager() {
		manager = new TableItemManager<>();
	}

	@Override
	public void initTableItemInfoManager(List<Variable> items) {
		manager = new TableItemManager<>(items);
	}

	@Override
	public int getButtonOptions() {
		return ADD | MODIFY | DELETE;
	}

	@Override
	public String getItemsIdentifier(Variable item) {
		return item.getName();
	}

	@Override
	public CommonTableLabelProvider<Variable> getLabelProvider() {
		return new VariableTableLabelProvider(this);
	}

	@Override
	public int indexOf(Variable item) {
		if(this.manager != null){
			return this.manager.indexOf(item);
		}
		return -1;
	}

}
