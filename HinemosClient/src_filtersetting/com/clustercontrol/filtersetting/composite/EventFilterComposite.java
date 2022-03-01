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
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openapitools.client.model.EventFilterBaseRequest;
import org.openapitools.client.model.EventFilterBaseRequest.FacilityTargetEnum;
import org.openapitools.client.model.EventFilterConditionRequest;

import com.clustercontrol.bean.Property;
import com.clustercontrol.composite.FacilitySelectorComposite;
import com.clustercontrol.filtersetting.bean.EventFilterContext;
import com.clustercontrol.filtersetting.composite.FilterConditionsComposite.Diversity;
import com.clustercontrol.filtersetting.composite.FilterConditionsComposite.FilterCondition;
import com.clustercontrol.filtersetting.util.EventFilterHelper;
import com.clustercontrol.filtersetting.util.EventFilterHelper.PropertyConversionType;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.Messages;

/**
 * イベント履歴のフィルタ情報の表示と入力を行うことができるコンポジットです。
 */
public class EventFilterComposite extends Composite {

	private PropertyConverter propConv;
	private EventFilterContext context;

	private Button chkEntire;
	private Button rdoFacilityAll;
	private Button rdoFacilityOneLevel;
	private FacilitySelectorComposite cmpScope;
	private FilterConditionsComposite cmpCondition;

	/**
	 * {@link EventFilterConditionRequest} と {@link Property} の相互変換についてのdelegateです。
	 * サブクラスで変換方法を変更する場合は、このインターフェイスをデフォルト実装以外にします。
	 */
	public interface PropertyConverter {
		public Property convertConditionToProperty(EventFilterConditionRequest cnd,
				MultiManagerEventDisplaySettingInfo eventDspSetting, String targetManagerName);

		public EventFilterConditionRequest convertPropertyToCondition(Property property);
	}

	/** {@link PropertyConverter} のデフォルト実装です。 */
	public static class DefaultPropertyConverter implements PropertyConverter {
		@Override
		public Property convertConditionToProperty(EventFilterConditionRequest cnd,
				MultiManagerEventDisplaySettingInfo eventDspSetting, String targetManagerName) {
			return EventFilterHelper.convertConditionToProperty(cnd, eventDspSetting, targetManagerName,
					PropertyConversionType.NORMAL);
		}

		@Override
		public EventFilterConditionRequest convertPropertyToCondition(Property property) {
			return EventFilterHelper.convertPropertyToCondition(property, PropertyConversionType.NORMAL);
		}
	}

	public EventFilterComposite(
			Composite parent,
			EventFilterContext context) {
		this(parent, context, new DefaultPropertyConverter());
	}

	public EventFilterComposite(
			Composite parent,
			EventFilterContext context,
			PropertyConverter propConv) {
		super(parent, SWT.NONE);
		this.context = context;
		this.propConv = propConv;

		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		// 全範囲検索
		chkEntire = new Button(this, SWT.CHECK);
		chkEntire.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		chkEntire.setText(Messages.getString("all.search"));

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
				if (context.hasHasMultiDisplayName()) {
					//マネジャー毎にユーザ項目の表示名が異なる場合の説明メッセージ
					return Messages.getString("dialog.monitor.filter.events.multiuseritemname");
				}
				return "";
			}

			@Override
			public FilterCondition createNewConditionProperty() {
				return new FilterCondition(
						"",
						false,
						propConv.convertConditionToProperty(
								EventFilterHelper.createDefaultCondition(),
								context.getDisplaySetting(),
								context.getManagerName()));
			}
		});
		cmpCondition.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
	}

	/**
	 * コンテキストの内容を入力コントロールへ反映します。
	 */
	public void readContext() {
		EventFilterBaseRequest filter = context.getFilter();

		// 全範囲検索
		chkEntire.setSelection(filter.getEntire() != null && filter.getEntire().booleanValue());

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
		for (EventFilterConditionRequest cnd : filter.getConditions()) {
			fcs.add(new FilterCondition(
					cnd.getDescription(),
					cnd.getNegative() != null && cnd.getNegative().booleanValue(),
					propConv.convertConditionToProperty(
							cnd,
							context.getDisplaySetting(),
							context.getManagerName())));
		}
		cmpCondition.setConditions(fcs);
	}

	/**
	 * 入力コントロールの内容をコンテキストへ反映します。
	 */
	public void writeContext() {
		EventFilterBaseRequest filter = new EventFilterBaseRequest();

		// 全範囲検索
		filter.setEntire(chkEntire.getSelection());

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
			EventFilterConditionRequest cnd = propConv.convertPropertyToCondition(cond.property);
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
		if (!common) {
			ownerRoleId = null;
		}

		// スコープ選択へ情報連携
		cmpScope.enable(managerName, ownerRoleId, false, true);

		// 対象マネージャが変わった場合、拡張項目に関する表示を変えるため、同じ内容でフィルタ条件を作り直す
		if (!Objects.equals(managerName, context.getManagerName())) {
			context.setManagerName(managerName);
			cmpCondition.setConditions(cmpCondition.getConditions());
		}
	}

}
