/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.filtersetting.bean.FilterSettingEditMode;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetUtil;

/**
 * フィルタ設定のヘッダ項目(マネージャ名、フィルタID、フィルタ名、共通/ユーザ、オーナーロールID)を
 * 入力するためのコンポジットです。
 */
public class FilterSettingHeaderComposite extends Composite {

	/** カスタムイベント番号 */
	// 採番は適当、SWTクラスで定義されているイベント番号と被らなければOK。
	private static final int EVENT_OWNER_CHANGED = 1024;

	private ManagerListComposite cmpManager;
	private RoleIdListComposite cmpOwnerRoleId;
	private Text txtFilterSettingId;
	private Text txtFilterSettingName;
	private Button btnUserPrivate;

	/** 独自イベントのリスナーです。 */
	public static abstract class CustomEventListener implements Listener {
		@Override
		public final void handleEvent(Event event) {
			FilterSettingHeaderComposite self = (FilterSettingHeaderComposite) event.widget;
			if (event.type == EVENT_OWNER_CHANGED) {
				onOwnerChanged(self.getManagerName(), self.isCommonFilter(), self.getOwnerRoleId());
			}
		}

		/** フィルタ設定の所有者(マネージャ、共通/ユーザ、オーナーロールID)が変更されたときに呼ばれます。 */
		public abstract void onOwnerChanged(String managerName, boolean commonOrUser, String ownerRoleId);
	}

	/**
	 * コンストラクタ。
	 */
	public FilterSettingHeaderComposite(
			Composite parent,
			FilterSettingEditMode mode,
			String managerName,
			boolean common,
			String ownerRoleId,
			String filterId,
			String filterName) {
		super(parent, SWT.NONE);

		if (filterId == null) filterId = "";
		if (filterName == null) filterName = "";

		// キー項目を編集可能なのは新規作成時のみ
		boolean keyEditable = (mode == FilterSettingEditMode.ADD);

		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		Label label; // ラベルは汎用変数で使い捨て

		// マネージャ
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		label.setText(Messages.getString("facility.manager") + " : ");

		cmpManager = new ManagerListComposite(this, SWT.NONE, keyEditable);
		cmpManager.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		cmpManager.setText(managerName);

		// フィルタ設定ID
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		label.setText(Messages.getString("fltset.filter_id") + " : ");

		txtFilterSettingId = new Text(this, SWT.BORDER | SWT.LEFT);
		txtFilterSettingId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		txtFilterSettingId.setText(filterId);
		txtFilterSettingId.setEnabled(keyEditable);
		WidgetUtil.applyRequiredStyleOnChange(txtFilterSettingId);

		// フィルタ設定名
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		label.setText(Messages.getString("fltset.filter_name") + " : ");

		txtFilterSettingName = new Text(this, SWT.BORDER | SWT.LEFT);
		txtFilterSettingName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		txtFilterSettingName.setText(filterName);
		WidgetUtil.applyRequiredStyleOnChange(txtFilterSettingName);

		// オーナーロールID
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		label.setText(Messages.getString("owner.role.id") + " : ");

		cmpOwnerRoleId = new RoleIdListComposite(this, SWT.NONE, cmpManager.getText(), keyEditable,
				com.clustercontrol.composite.RoleIdListComposite.Mode.OWNER_ROLE);
		cmpOwnerRoleId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (common) {
			if (ownerRoleId == null) {
				// compositeの初期値に任せる
			} else {
				cmpOwnerRoleId.setText(ownerRoleId);
			}
		} else {
			// ユーザフィルタ設定の場合は空欄にする
			cmpOwnerRoleId.add("");
			cmpOwnerRoleId.setText("");
			cmpOwnerRoleId.setEnabled(false);
		}

		// ユーザフィルタ設定チェックボックス
		btnUserPrivate = new Button(this, SWT.CHECK);
		btnUserPrivate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		btnUserPrivate.setText(Messages.getString("fltset.owner.user.caption"));
		btnUserPrivate.setSelection(!common);
		btnUserPrivate.setEnabled(keyEditable);

		// イベントリスナーの登録
		cmpManager.addComboSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 共通フィルタ設定ならオーナーロールIDをリセット
				if (!btnUserPrivate.getSelection()) {
					cmpOwnerRoleId.createRoleIdList(cmpManager.getText());
				}
				notifyListeners(EVENT_OWNER_CHANGED, null);
			}
		});

		btnUserPrivate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (btnUserPrivate.getSelection()) {
					// チェックOn: ユーザフィルタ設定
					cmpOwnerRoleId.add("");
					cmpOwnerRoleId.setText("");
					cmpOwnerRoleId.setEnabled(false);
					notifyListeners(EVENT_OWNER_CHANGED, null);
				} else {
					// チェックOff: 共通フィルタ設定
					cmpOwnerRoleId.setEnabled(true);
					cmpOwnerRoleId.createRoleIdList(cmpManager.getText());
					notifyListeners(EVENT_OWNER_CHANGED, null);
				}
			}
		});

		cmpOwnerRoleId.addComboSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				notifyListeners(EVENT_OWNER_CHANGED, null);
			}
		});
	}

	/**
	 * 独自イベントのリスナーを追加します。
	 */
	public void addCustomEventListener(CustomEventListener listener) {
		addListener(EVENT_OWNER_CHANGED, listener);
	}

	/**
	 * 独自イベントのリスナーを削除します。
	 */
	public void removeCustomEventListener(CustomEventListener listener) {
		removeListener(EVENT_OWNER_CHANGED, listener);
	}

	/**
	 * 選択されているマネージャの名前を返します。
	 */
	public String getManagerName() {
		return cmpManager.getText();
	}

	/**
	 * 入力されているフィルタIDを返します。
	 */
	public String getFilterId() {
		return txtFilterSettingId.getText();
	}

	/**
	 * 入力されているフィルタ名を返します。
	 */
	public String getFilterName() {
		return txtFilterSettingName.getText();
	}

	/**
	 * 共通フィルタ設定が選ばれている場合は true、ユーザフィルタ設定の場合は false を返します。
	 */
	public boolean isCommonFilter() {
		return !btnUserPrivate.getSelection();
	}

	/**
	 * 選択されているオーナーロールIDを返します。
	 */
	public String getOwnerRoleId() {
		return cmpOwnerRoleId.getText();
	}

}
