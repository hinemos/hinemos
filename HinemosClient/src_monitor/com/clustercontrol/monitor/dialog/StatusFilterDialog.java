/*
 * Copyright (c) 2021 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.dialog;

import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.FilterSettingSummaryResponse;

import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.filtersetting.bean.StatusFilterContext;
import com.clustercontrol.filtersetting.composite.FilterSettingSelectorComposite;
import com.clustercontrol.filtersetting.composite.FilterSettingSelectorComposite.CustomEventListener;
import com.clustercontrol.filtersetting.composite.StatusFilterComposite;
import com.clustercontrol.filtersetting.dialog.FilterDialog;
import com.clustercontrol.filtersetting.util.GenericFilterSettingResponse;
import com.clustercontrol.filtersetting.util.FilterSettingHelper;
import com.clustercontrol.filtersetting.util.StatusFilterHelper;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.util.ManagerTag;
import com.clustercontrol.util.Messages;

/**
 * 監視[ステータスのフィルタ処理]ダイアログ
 */
public class StatusFilterDialog extends FilterDialog {

	private StatusFilterContext context;

	private StatusFilterComposite cmpFilter;

	public StatusFilterDialog(
			Shell parent,
			StatusFilterContext context) {
		super(parent);
		Objects.requireNonNull(context, "context");

		this.context = context;

		// 初期状態ではスコープは選択なし
		context.getFilter().setFacilityId(null);
	}

	@Override
	protected FilterCategoryEnum getFilterCategory() {
		return FilterCategoryEnum.STATUS;
	}

	@Override
	protected String getTitle() {
		return Messages.getString("dialog.monitor.filter.status");
	}

	@Override
	protected Composite createFilterComposite(Composite parent, FilterSettingSelectorComposite cmpSelector) {
		cmpFilter = new StatusFilterComposite(parent, context);
		cmpFilter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		cmpFilter.readContext();

		cmpSelector.addCustomEventListener(new CustomEventListener() {
			@Override
			public void onFilterSettingSelected(ManagerTag<FilterSettingSummaryResponse> selected) {
				if (selected == null) return;

				// フィルタ設定の詳細をマネージャから取得
				GenericFilterSettingResponse fs;
				try {
					fs = FilterSettingHelper.fetchFilterSetting(selected);
				} catch (Exception e) {
					new ApiResultDialog().addFailure(selected.managerName, e).show();
					return;
				}

				// マネージャから取得した設定値をコンテキストと入力欄へ反映
				context.setFilter(StatusFilterHelper.convertToRequest(fs.getStatusFilter()), selected.managerName);
				cmpFilter.readContext();
			}
		});

		return cmpFilter;
	}

	@Override
	protected void resetFilterComposite() {
		context.setFilter(StatusFilterHelper.createDefaultFilter(null), null);
		cmpFilter.readContext();
	}

	@Override
	protected final boolean action() {
		// 結果をコンテキストへ
		cmpFilter.writeContext();

		return super.action();
	}

}
