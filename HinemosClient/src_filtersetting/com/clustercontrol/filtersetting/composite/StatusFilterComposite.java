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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openapitools.client.model.StatusFilterBaseRequest;
import org.openapitools.client.model.StatusFilterBaseRequest.FacilityTargetEnum;
import org.openapitools.client.model.StatusFilterConditionRequest;

import com.clustercontrol.composite.FacilitySelectorComposite;
import com.clustercontrol.filtersetting.bean.StatusFilterContext;
import com.clustercontrol.filtersetting.composite.FilterConditionsComposite.Diversity;
import com.clustercontrol.filtersetting.composite.FilterConditionsComposite.FilterCondition;
import com.clustercontrol.filtersetting.util.StatusFilterHelper;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.Messages;

/**
 * ステータス通知結果のフィルタ情報の表示と入力を行うことができるコンポジットです。
 */
public class StatusFilterComposite extends Composite {

	private StatusFilterContext context;

	private Button rdoFacilityAll;
	private Button rdoFacilityOneLevel;
	private FacilitySelectorComposite cmpScope;
	private FilterConditionsComposite cmpCondition;

	public StatusFilterComposite(
			Composite parent,
			StatusFilterContext context) {
		super(parent, SWT.NONE);
		this.context = context;

		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		// スコープ
		Label lblScope = new Label(this, SWT.NONE);
		lblScope.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		lblScope.setText(Messages.getString("scope") + " : ");

		cmpScope = new FacilitySelectorComposite(this);
		cmpScope.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		cmpScope.setHintMessage(context.getScopeHintMessage());
		cmpScope.enable(false, true);

		// ファシリティターゲット
		Label lblFcTarget = new Label(this, SWT.NONE);
		lblFcTarget.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		lblFcTarget.setText("");

		Composite cmpoFcTarget = new Composite(this, SWT.NONE);
		cmpoFcTarget.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		GridLayout loFcTarget = new GridLayout(2, true);
		loFcTarget.marginWidth = 0;
		loFcTarget.marginHeight = 0;
		cmpoFcTarget.setLayout(loFcTarget);

		rdoFacilityAll = new Button(cmpoFcTarget, SWT.RADIO);
		rdoFacilityAll.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		rdoFacilityAll.setText(Messages.getString("facility.target.all"));

		rdoFacilityOneLevel = new Button(cmpoFcTarget, SWT.RADIO);
		rdoFacilityOneLevel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		rdoFacilityOneLevel.setText(Messages.getString("facility.target.beneath"));

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
						StatusFilterHelper.convertConditionToProperty(
								StatusFilterHelper.createDefaultCondition()));
			}
		});
		cmpCondition.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
	}

	/**
	 * コンテキストの内容を入力コントロールへ反映します。
	 */
	public void readContext() {
		StatusFilterBaseRequest filter = context.getFilter();

		// スコープ
		if (filter.getFacilityId() == null) {
			cmpScope.unselect();
		} else {
			cmpScope.select(context.getManagerName(), filter.getFacilityId());
		}

		// ファシリティターゲット
		if (filter.getFacilityTarget() == FacilityTargetEnum.ALL) {
			rdoFacilityAll.setSelection(true);
			rdoFacilityOneLevel.setSelection(false); // 明示的に無効化しないと二重選択が起こる
		} else {
			rdoFacilityAll.setSelection(false); // 明示的に無効化しないと二重選択が起こる
			rdoFacilityOneLevel.setSelection(true);
		}

		// 条件n
		List<FilterCondition> fcs = new ArrayList<>();
		for (StatusFilterConditionRequest cnd : filter.getConditions()) {
			fcs.add(new FilterCondition(
					cnd.getDescription(),
					cnd.getNegative() != null && cnd.getNegative().booleanValue(),
					StatusFilterHelper.convertConditionToProperty(cnd)));
		}
		cmpCondition.setConditions(fcs);
	}

	/**
	 * 入力コントロールの内容をコンテキストへ反映します。
	 */
	public void writeContext() {
		StatusFilterBaseRequest filter = new StatusFilterBaseRequest();

		// スコープ
		FacilityTreeItemResponse scope = cmpScope.getSelectedItem().orElse(null);
		String managerName = cmpScope.getSelecedManager().orElse(null);
		if (scope != null && managerName != null) {
			filter.setFacilityId(scope.getData().getFacilityId());
		}

		// ファシリティターゲット
		filter.setFacilityTarget(rdoFacilityAll.getSelection() ? FacilityTargetEnum.ALL : FacilityTargetEnum.ONE_LEVEL);

		// 条件n
		for (FilterCondition cond : cmpCondition.getConditions()) {
			StatusFilterConditionRequest cnd = StatusFilterHelper.convertPropertyToCondition(cond.property);
			cnd.setDescription(cond.description);
			cnd.setNegative(cond.negative);
			filter.addConditionsItem(cnd);
		}

		context.setFilter(filter, managerName);
	}

	/**
	 * フィルタ条件の所有者を設定します。
	 * 
	 * @param managerName マネージャ名。
	 * @param common 共通フィルタ設定ならtrue、ユーザフィルタ設定ならfalse。
	 * @param ownerRoleId オーナーロールID。共通フィルタ設定の場合のみ有効。
	 */
	public void setOwner(String managerName, boolean common, String ownerRoleId) {
		// スコープ選択へ情報連携
		cmpScope.enable(managerName, (common ? ownerRoleId : null), false, true);
	}

}
