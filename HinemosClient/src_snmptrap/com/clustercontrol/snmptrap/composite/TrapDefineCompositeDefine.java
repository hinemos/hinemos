package com.clustercontrol.snmptrap.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.util.TableItemManager;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;
import com.clustercontrol.snmptrap.action.GetTrapDefineTableDefine;
import com.clustercontrol.snmptrap.dialog.CreateTrapDefineDialog;
import com.clustercontrol.ws.monitor.TrapValueInfo;

public class TrapDefineCompositeDefine implements ITableItemCompositeDefine<TrapValueInfo> {

	/** 現在有効なダイアログ */
	private CreateTrapDefineDialog dialog = null;

	/** テーブルアイテムの管理クラス */
	private TableItemManager<TrapValueInfo> manager = null;

	@Override
	public TrapValueInfo getCurrentCreatedItem() {
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
	public CommonDialog createDialog(Shell shell, TrapValueInfo item) {
		dialog = new CreateTrapDefineDialog(shell, item);
		return dialog;
	}

	@Override
	public ArrayList<?> getTableDefine() {
		return GetTrapDefineTableDefine.get();
	}

	@Override
	public TableItemManager<TrapValueInfo> getTableItemInfoManager() {
		return manager;
	}

	@Override
	public void initTableItemInfoManager() {
		manager = new TableItemManager<>();
	}

	@Override
	public void initTableItemInfoManager(List<TrapValueInfo> items) {
		manager = new TableItemManager<>(items);
	}

	@Override
	public int getButtonOptions() {
		return ADD | MODIFY | DELETE | COPY;
	}

	@Override
	public String getItemsIdentifier(TrapValueInfo item) {
		return item.getUei();
	}

	@Override
	public CommonTableLabelProvider<TrapValueInfo> getLabelProvider() {
		return new TrapDefineTableLabelProvider(this);
	}

	@Override
	public int indexOf(TrapValueInfo item) {
		if(this.manager != null){
			return this.manager.indexOf(item);
		}
		return -1;
	}

}
