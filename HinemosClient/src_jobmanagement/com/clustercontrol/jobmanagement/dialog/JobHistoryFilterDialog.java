/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.FilterSettingSummaryResponse;

import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.filtersetting.bean.JobHistoryFilterContext;
import com.clustercontrol.filtersetting.composite.FilterSettingSelectorComposite;
import com.clustercontrol.filtersetting.composite.FilterSettingSelectorComposite.CustomEventListener;
import com.clustercontrol.filtersetting.composite.JobHistoryFilterComposite;
import com.clustercontrol.filtersetting.dialog.FilterDialog;
import com.clustercontrol.filtersetting.util.GenericFilterSettingResponse;
import com.clustercontrol.filtersetting.util.FilterSettingHelper;
import com.clustercontrol.filtersetting.util.JobHistoryFilterHelper;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.util.ManagerTag;
import com.clustercontrol.util.Messages;

/**
 * ジョブ[履歴フィルタ処理]ダイアログ
 */
public class JobHistoryFilterDialog extends FilterDialog {

	private JobHistoryFilterContext context;

	private ManagerListComposite cmpManager;
	private JobHistoryFilterComposite cmpFilter;

	public JobHistoryFilterDialog(
			Shell parent,
			JobHistoryFilterContext context) {
		super(parent);
		Objects.requireNonNull(context, "context");

		this.context = context;
	}

	@Override
	protected FilterCategoryEnum getFilterCategory() {
		return FilterCategoryEnum.JOB_HISTORY;
	}

	@Override
	protected String getTitle() {
		return Messages.getString("dialog.job.filter.histories");
	}

	@Override
	protected void createMiddleComposite(Composite parent) {
		Group grpManager = new Group(parent, SWT.NONE);
		grpManager.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpManager.setText(Messages.getString("facility.manager"));
		grpManager.setLayout(new GridLayout(1, true));

		cmpManager = new ManagerListComposite(grpManager, SWT.NONE, true);
		cmpManager.add("", 0); // 先頭に空欄の選択肢を追加
		cmpManager.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	}

	@Override
	protected Composite createFilterComposite(Composite parent, FilterSettingSelectorComposite cmpSelector) {
		cmpFilter = new JobHistoryFilterComposite(parent, context);
		cmpFilter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

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
				context.setManagerName(selected.managerName);
				context.setFilter(JobHistoryFilterHelper.convertToRequest(fs.getJobHistoryFilter()));
				showContext();
			}
		});

		return cmpFilter;
	}

	@Override
	protected void initializeAfterCustomizeDialog() {
		showContext();
	}

	@Override
	protected void resetFilterComposite() {
		context.setManagerName(null);
		context.setFilter(JobHistoryFilterHelper.createDefaultFilter());
		showContext();
	}

	@Override
	protected boolean action() {
		// 結果をコンテキストへ
		String manager = cmpManager.getText();
		if (manager.length() == 0) {
			manager = null;
		}
		context.setManagerName(manager);

		cmpFilter.writeContext();

		return super.action();
	}

	/**
	 * コンテキストの内容を表示します。
	 */
	private void showContext() {
		String manager = context.getManagerName();
		if (manager == null) {
			manager = "";
		}
		cmpManager.setText(manager);

		cmpFilter.readContext();
	}

}
