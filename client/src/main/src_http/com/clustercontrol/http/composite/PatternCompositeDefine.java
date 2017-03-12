package com.clustercontrol.http.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.http.dialog.PatternCreateDialog;
import com.clustercontrol.http.viewer.GetPatternTableDefine;
import com.clustercontrol.http.viewer.PatternTableLabelProvider;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.util.TableItemManager;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;
import com.clustercontrol.ws.monitor.Pattern;

public class PatternCompositeDefine implements ITableItemCompositeDefine<Pattern> {

	/** 現在有効なダイアログ */
	private PatternCreateDialog dialog = null;

	/** テーブルアイテムの管理クラス */
	private TableItemManager<Pattern> manager = null;

	@Override
	public Pattern getCurrentCreatedItem() {
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
	public CommonDialog createDialog(Shell shell, Pattern item) {
		dialog = new PatternCreateDialog(shell, item);
		return dialog;
	}

	@Override
	public ArrayList<?> getTableDefine() {
		return GetPatternTableDefine.get();
	}

	@Override
	public TableItemManager<Pattern> getTableItemInfoManager() {
		return manager;
	}

	@Override
	public void initTableItemInfoManager() {
		manager = new TableItemManager<>();
	}

	@Override
	public void initTableItemInfoManager(List<Pattern> items) {
		manager = new TableItemManager<>(items);
	}

	@Override
	public int getButtonOptions() {
		return ADD | MODIFY | DELETE | COPY | SPACE | UP | DOWN;
	}

	@Override
	public String getItemsIdentifier(Pattern item) {
		return item.getDescription();
	}

	@Override
	public CommonTableLabelProvider<Pattern> getLabelProvider() {
		return new PatternTableLabelProvider(this);
	}

	@Override
	public int indexOf(Pattern item) {
		if(this.manager != null){
			return this.manager.indexOf(item);
		}
		return -1;
	}

}
