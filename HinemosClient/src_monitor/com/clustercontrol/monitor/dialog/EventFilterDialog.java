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

import com.clustercontrol.bean.Property;
import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.filtersetting.bean.EventFilterContext;
import com.clustercontrol.filtersetting.composite.EventFilterComposite;
import com.clustercontrol.filtersetting.composite.EventFilterComposite.DefaultPropertyConverter;
import com.clustercontrol.filtersetting.composite.EventFilterComposite.PropertyConverter;
import com.clustercontrol.filtersetting.composite.FilterSettingSelectorComposite;
import com.clustercontrol.filtersetting.composite.FilterSettingSelectorComposite.CustomEventListener;
import com.clustercontrol.filtersetting.dialog.FilterDialog;
import com.clustercontrol.filtersetting.util.GenericFilterSettingResponse;
import com.clustercontrol.filtersetting.util.EventFilterHelper;
import com.clustercontrol.filtersetting.util.FilterSettingHelper;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.util.ManagerTag;
import com.clustercontrol.util.Messages;

/**
 * 監視[イベントのフィルタ処理]ダイアログ
 */
public class EventFilterDialog extends FilterDialog {

	private EventFilterContext context;

	private EventFilterComposite cmpFilter;

	public EventFilterDialog(
			Shell parent,
			EventFilterContext context) {
		super(parent);
		Objects.requireNonNull(context, "context");

		this.context = context;

		// 初期状態ではスコープは選択なし
		context.getFilter().setFacilityId(null);
	}

	@Override
	protected FilterCategoryEnum getFilterCategory() {
		return FilterCategoryEnum.EVENT;
	}

	@Override
	protected String getTitle() {
		return Messages.getString("dialog.monitor.filter.events");
	}

	@Override
	protected Composite createFilterComposite(Composite parent, FilterSettingSelectorComposite cmpSelector) {
		cmpFilter = new EventFilterComposite(parent, context, createPropertyConverter());
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
				context.setFilter(EventFilterHelper.convertToRequest(fs.getEventFilter()), selected.managerName);
				cmpFilter.readContext();
			}
		});

		return cmpFilter;
	}

	@Override
	protected void resetFilterComposite() {
		context.setFilter(EventFilterHelper.createDefaultFilter(null), null);
		cmpFilter.readContext();
	}

	// サブクラス側でsuper.actionを「呼ぶべきか」「その順番は」などの点を意識させないように final で継承禁止にして、
	// サブクラスには別メソッド actionWithUpdatedContext を継承してもらう。
	@Override
	protected final boolean action() {
		// 結果をコンテキストへ
		cmpFilter.writeContext();

		if (!actionWithUpdatedContext()) {
			return false;
		}

		return super.action();
	}

	/**
	 * サブクラスで条件入力の {@link Property} 変換処理をカスタマイズしたい場合は、本メソッドをオーバーライドしてください。
	 */
	protected PropertyConverter createPropertyConverter() {
		return new DefaultPropertyConverter();
	}

	/**
	 * フィルタ条件の結果が確定したあと、サブクラスで行いたい処理がある場合は、本メソッドを実装してください。
	 */
	protected boolean actionWithUpdatedContext() {
		// サブクラスの拡張ポイント
		return true;
	}

}
