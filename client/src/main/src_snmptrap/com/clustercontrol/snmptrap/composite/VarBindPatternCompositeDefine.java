package com.clustercontrol.snmptrap.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.util.TableItemManager;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;
import com.clustercontrol.snmptrap.action.GetVarBindPatternTableDefine;
import com.clustercontrol.snmptrap.dialog.CreateVarBindPatternDialog;
import com.clustercontrol.ws.monitor.VarBindPattern;

public class VarBindPatternCompositeDefine implements ITableItemCompositeDefine<VarBindPattern> {

	/** 現在有効なダイアログ */
	private CreateVarBindPatternDialog dialog = null;

	/** テーブルアイテムの管理クラス */
	private TableItemManager<VarBindPattern> manager = null;

	@Override
	public VarBindPattern getCurrentCreatedItem() {
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
	public CommonDialog createDialog(Shell shell, VarBindPattern item) {
		dialog = new CreateVarBindPatternDialog(shell, item);
		return dialog;
	}

	@Override
	public ArrayList<?> getTableDefine() {
		return GetVarBindPatternTableDefine.get();
	}

	@Override
	public TableItemManager<VarBindPattern> getTableItemInfoManager() {
		return manager;
	}

	@Override
	public void initTableItemInfoManager() {
		manager = new TableItemManager<>();
	}

	@Override
	public void initTableItemInfoManager(List<VarBindPattern> items) {
		manager = new TableItemManager<>(items);
	}

	@Override
	public int getButtonOptions() {
		return ADD | MODIFY | DELETE | COPY | SPACE | UP | DOWN;
	}

	@Override
	public String getItemsIdentifier(VarBindPattern item) {
		return item.getDescription();
	}

	@Override
	public CommonTableLabelProvider<VarBindPattern> getLabelProvider() {
		return new VarBindPatternTableLabelProvider(this);
	}

	@Override
	public int indexOf(VarBindPattern item) {
		if(this.manager != null){
			return this.manager.indexOf(item);
		}
		return -1;
	}

}
