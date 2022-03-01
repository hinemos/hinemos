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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.openapitools.client.model.FilterSettingSummariesResponse;
import org.openapitools.client.model.FilterSettingSummaryResponse;

import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.filtersetting.util.FilterSettingRestClientWrapper;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.util.ManagerTag;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;

/**
 * 既存のフィルタ設定を選択するためのコンポジットです。
 */
public class FilterSettingSelectorComposite extends Composite {

	/** カスタムイベント番号 */
	private static final int EVENT_ID_FILTER_SETTING_SELECTED = 1024; // 採番は適当、SWTクラスで定義されているイベント番号と被らなければOK。

	private FilterCategoryEnum filterCategory;

	private Button rdoCommonFilter;
	private Button rdoUserFilter;
	private Combo cboFilter;

	/** 独自イベントのリスナーです。 */
	public static abstract class CustomEventListener implements Listener {
		@Override
		public final void handleEvent(Event event) {
			FilterSettingSelectorComposite self = (FilterSettingSelectorComposite) event.widget;
			if (event.type == EVENT_ID_FILTER_SETTING_SELECTED) {
				onFilterSettingSelected(self.getSelected());
			}
		}

		/**
		 * フィルタ設定が選択されたときに呼ばれます。<br/>
		 * 実際に起こりうる状況かは分かりませんが、もし未選択状態でイベントが発火された場合はnullが渡されますので、
		 * その場合の対応を念のため行ってください。
		 */
		public abstract void onFilterSettingSelected(ManagerTag<FilterSettingSummaryResponse> selected);
	}

	public FilterSettingSelectorComposite(Composite parent, FilterCategoryEnum filterCategory) {
		super(parent, SWT.NONE);
		this.filterCategory = filterCategory;

		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		// 共通/ユーザフィルタの選択
		rdoCommonFilter = new Button(this, SWT.RADIO);
		rdoCommonFilter.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		rdoCommonFilter.setText(Messages.getString("fltset.owner.common"));

		rdoUserFilter = new Button(this, SWT.RADIO);
		rdoUserFilter.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		rdoUserFilter.setText(Messages.getString("fltset.owner.user"));

		rdoCommonFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				reload();
			}
		});

		rdoUserFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				reload();
			}
		});

		// フィルタ選択ドロップダウンリスト
		cboFilter = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		cboFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		cboFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				notifyListeners(EVENT_ID_FILTER_SETTING_SELECTED, null);
			}
		});

		// 初期選択
		rdoCommonFilter.setSelection(true);
	}

	/**
	 * 独自イベントのリスナーを追加します。
	 */
	public void addCustomEventListener(CustomEventListener listener) {
		addListener(EVENT_ID_FILTER_SETTING_SELECTED, listener);
	}

	/**
	 * 独自イベントのリスナーを削除します。
	 */
	public void removeCustomEventListener(CustomEventListener listener) {
		removeListener(EVENT_ID_FILTER_SETTING_SELECTED, listener);
	}

	/**
	 * フィルタ設定の選択肢を、マネージャから再取得します。
	 */
	public void reload() {
		List<ManagerTag<FilterSettingSummaryResponse>> summaries = new ArrayList<>();

		// マネージャから一覧情報を取得
		ApiResultDialog resultDialog = new ApiResultDialog();
		for (String managerName : RestConnectManager.getActiveManagerNameList()) {
			try {
				FilterSettingRestClientWrapper rest = FilterSettingRestClientWrapper.getWrapper(managerName);
				FilterSettingSummariesResponse res;
				if (isCommonSelected()) {
					try {
						res = rest.getCommonFilterSettingSummaries(filterCategory, "%");
					} catch (InvalidRole ignored) {
						// ユーザフィルタに関してはシステム権限なしで使えるので、
						// 共通フィルタ設定の読み取りで権限エラーが起きたとしてもエラーは出さずに無視する
						continue;
					}
				} else {
					res = rest.getUserFilterSettingSummaries(filterCategory, "%",
							RestConnectManager.getLoginUserId(managerName));
				}
				summaries.addAll(ManagerTag.listFrom(managerName, res.getSummaries()));
			} catch (Exception e) {
				resultDialog.addFailure(managerName, e);
			}
		}
		resultDialog.show();

		// ドロップダウンへ選択肢を追加
		cboFilter.removeAll();
		cboFilter.add("");
		for (ManagerTag<FilterSettingSummaryResponse> summ : summaries) {
			String text = String.format("%s : %s (%s)",
					summ.managerName,
					summ.data.getFilterName(),
					summ.data.getFilterId());

			cboFilter.add(text);
			cboFilter.setData(text, summ);
		}
	}

	/**
	 * 共通フィルタ設定が選択されている場合は true を、ユーザフィルタ設定の場合は false を返します。
	 */
	public boolean isCommonSelected() {
		return rdoCommonFilter.getSelection();
	}

	/**
	 * 選択されているフィルタ設定と、そのフィルタ設定が所属しているマネージャを返します。
	 */
	@SuppressWarnings("unchecked")
	public ManagerTag<FilterSettingSummaryResponse> getSelected() {
		int i = cboFilter.getSelectionIndex();
		if (i == -1) return null;
		return (ManagerTag<FilterSettingSummaryResponse>) cboFilter.getData(cboFilter.getItem(i));
	}

	/**
	 * 何も選択されていない状態にします。
	 */
	public void unselect() {
		cboFilter.select(0);
	}

}
