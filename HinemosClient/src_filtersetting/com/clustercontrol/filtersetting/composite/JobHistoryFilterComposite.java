/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.openapitools.client.model.JobHistoryFilterBaseRequest;
import org.openapitools.client.model.JobHistoryFilterConditionRequest;

import com.clustercontrol.filtersetting.bean.JobHistoryFilterContext;
import com.clustercontrol.filtersetting.composite.FilterConditionsComposite.Diversity;
import com.clustercontrol.filtersetting.composite.FilterConditionsComposite.FilterCondition;
import com.clustercontrol.filtersetting.util.JobHistoryFilterHelper;

/**
 * ジョブ実行履歴のフィルタ情報の表示と入力を行うことができるコンポジットです。
 */
public class JobHistoryFilterComposite extends Composite {

	private JobHistoryFilterContext context;

	private FilterConditionsComposite cmpCondition;

	public JobHistoryFilterComposite(
			Composite parent,
			JobHistoryFilterContext context) {
		super(parent, SWT.NONE);
		this.context = context;

		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		// 条件n
		cmpCondition = new FilterConditionsComposite(this, new Diversity() {
			@Override
			public String getNotesMessage() {
				return "";
			}

			@Override
			public FilterCondition createNewConditionProperty() {
				return new FilterCondition(
						"",
						false,
						JobHistoryFilterHelper.convertConditionToProperty(JobHistoryFilterHelper.createDefaultCondition()));
			}
		});
		cmpCondition.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	}

	/**
	 * コンテキストの内容を入力コントロールへ反映します。
	 */
	public void readContext() {
		JobHistoryFilterBaseRequest filter = context.getFilter();

		List<FilterCondition> fcs = new ArrayList<>();
		for (JobHistoryFilterConditionRequest cnd : filter.getConditions()) {
			fcs.add(new FilterCondition(
					cnd.getDescription(),
					cnd.getNegative() != null && cnd.getNegative().booleanValue(),
					JobHistoryFilterHelper.convertConditionToProperty(cnd)));
		}
		cmpCondition.setConditions(fcs);
	}

	/**
	 * 入力コントロールの内容をコンテキストへ反映します。
	 */
	public void writeContext() {
		JobHistoryFilterBaseRequest filter = new JobHistoryFilterBaseRequest();
		for (FilterCondition fc : cmpCondition.getConditions()) {
			JobHistoryFilterConditionRequest cnd = JobHistoryFilterHelper.convertPropertyToCondition(fc.property);
			cnd.setDescription(fc.description);
			cnd.setNegative(fc.negative);
			filter.addConditionsItem(cnd);
		}

		context.setFilter(filter);
	}

}
