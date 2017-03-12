package com.clustercontrol.http.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.http.dialog.PageCreateDialog;
import com.clustercontrol.http.viewer.GetPageTableDefine;
import com.clustercontrol.http.viewer.PageTableLabelProvider;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.util.TableItemManager;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;
import com.clustercontrol.ws.monitor.Page;

public class PageCompositeDefine implements ITableItemCompositeDefine<Page> {

	/** 現在有効なダイアログ */
	private PageCreateDialog dialog = null;

	/** テーブルアイテムの管理クラス */
	private TableItemManager<Page> manager = null;

	@Override
	public Page getCurrentCreatedItem() {
		if(dialog != null){
			return dialog.getInputData();
		}

		return null;
	}

	@Override
	public CommonDialog createDialog(Shell shell) {
		dialog = new PageCreateDialog(shell);
		return dialog;
	}

	@Override
	public CommonDialog createDialog(Shell shell, Page item) {
		dialog = new PageCreateDialog(shell, item);
		return dialog;
	}

	@Override
	public ArrayList<?> getTableDefine() {
		return GetPageTableDefine.get();
	}

	@Override
	public TableItemManager<Page> getTableItemInfoManager() {
		return manager;
	}

	@Override
	public void initTableItemInfoManager() {
		manager = new TableItemManager<>();
	}

	@Override
	public void initTableItemInfoManager(List<Page> items) {
		manager = new TableItemManager<>(items);
	}

	@Override
	public int getButtonOptions() {
		return ADD | MODIFY | DELETE | COPY | UP | DOWN | MULTI;
	}

	@Override
	public String getItemsIdentifier(Page item) {
		return item.getDescription();
	}

	@Override
	public CommonTableLabelProvider<Page> getLabelProvider() {
		return new PageTableLabelProvider(this);
	}

	@Override
	public int indexOf(Page item) {
		if(this.manager != null){
			return this.manager.indexOf(item);
		}
		return -1;
	}

}
