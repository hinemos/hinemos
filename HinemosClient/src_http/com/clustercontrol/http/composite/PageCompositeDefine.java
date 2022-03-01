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
import org.openapitools.client.model.PageResponse;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.http.dialog.PageCreateDialog;
import com.clustercontrol.http.viewer.GetPageTableDefine;
import com.clustercontrol.http.viewer.PageTableLabelProvider;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.util.TableItemManager;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;

public class PageCompositeDefine implements ITableItemCompositeDefine<PageResponse> {

	/** 現在有効なダイアログ */
	private PageCreateDialog dialog = null;

	/** テーブルアイテムの管理クラス */
	private TableItemManager<PageResponse> manager = null;

	@Override
	public PageResponse getCurrentCreatedItem() {
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
	public CommonDialog createDialog(Shell shell, PageResponse item) {
		dialog = new PageCreateDialog(shell, item);
		return dialog;
	}

	@Override
	public ArrayList<?> getTableDefine() {
		return GetPageTableDefine.get();
	}

	@Override
	public TableItemManager<PageResponse> getTableItemInfoManager() {
		return manager;
	}

	@Override
	public void initTableItemInfoManager() {
		manager = new TableItemManager<>();
	}

	@Override
	public void initTableItemInfoManager(List<PageResponse> items) {
		manager = new TableItemManager<>(items);
	}

	@Override
	public int getButtonOptions() {
		return ADD | MODIFY | DELETE | COPY | UP | DOWN | MULTI;
	}

	@Override
	public String getItemsIdentifier(PageResponse item) {
		return item.getUrl();
	}

	@Override
	public CommonTableLabelProvider<PageResponse> getLabelProvider() {
		return new PageTableLabelProvider(this);
	}

	@Override
	public int indexOf(PageResponse item) {
		if(this.manager != null){
			return this.manager.indexOf(item);
		}
		return -1;
	}

}
