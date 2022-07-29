/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.SdmlControlSettingInfoResponse;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.sdml.action.GetSdmlControlSettingListTableDefine;
import com.clustercontrol.sdml.action.GetSdmlControlSettingV1;
import com.clustercontrol.sdml.dialog.SdmlControlSettingCreateDialog;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;

public class SdmlControlSettingListComposite extends Composite {

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	/** 表示内容ラベル */
	private Label statuslabel = null;

	/** 合計ラベル */
	private Label totalLabel = null;

	/** 検索条件 */
	private Property condition = null;

	/** 自分自身のComposite */
	private Composite composite = null;

	public SdmlControlSettingListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
		this.composite = this;
	}

	/**
	 * コンポジットを生成・構築します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		this.statuslabel = new Label(this, SWT.LEFT);
		this.statuslabel.setText("");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.statuslabel.setLayoutData(gridData);

		Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(GetSdmlControlSettingListTableDefine.get(),
				GetSdmlControlSettingListTableDefine.SORT_COLUMN_INDEX1,
				GetSdmlControlSettingListTableDefine.SORT_COLUMN_INDEX2,
				GetSdmlControlSettingListTableDefine.SORT_ORDER);
		// 列移動が可能に設定
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}

		// ダブルクリックリスナの追加
		this.tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				String managerName = null;
				String applicationId = null;

				if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
					ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event.getSelection()).getFirstElement();

					managerName = (String) info.get(GetSdmlControlSettingListTableDefine.MANAGER_NAME);
					applicationId = (String) info.get(GetSdmlControlSettingListTableDefine.APPLICATION_ID);
				}

				if (applicationId != null) {
					// ダイアログを生成
					SdmlControlSettingCreateDialog dialog = new SdmlControlSettingCreateDialog(composite.getShell(),
							managerName, applicationId, PropertyDefineConstant.MODE_MODIFY);
					// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
					if (dialog.open() == IDialogConstants.OK_ID) {
						composite.update();
					}
				}
			}
		});

		this.totalLabel = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.totalLabel.setLayoutData(gridData);

	}

	/**
	 * このコンポジットが利用するテーブルビューアを返します。
	 *
	 * @return テーブルビューア
	 */
	public CommonTableViewer getTableViewer() {
		return this.tableViewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return this.tableViewer.getTable();
	}

	/**
	 * コンポジットを更新します。
	 * <p>
	 *
	 * 検索条件が事前に設定されている場合、その条件にヒットする構成情報収集設定一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全構成情報収集設定を表示します。
	 */
	@Override
	public void update() {
		// データ取得
		Map<String, List<SdmlControlSettingInfoResponse>> dispDataMap = null;
		ArrayList<Object> listInput = new ArrayList<Object>();

		if (this.condition == null) {
			this.statuslabel.setText("");
			dispDataMap = new GetSdmlControlSettingV1().getList();

		} else {
			this.statuslabel.setText(Messages.getString("filtered.list"));
			dispDataMap = new GetSdmlControlSettingV1().getListWithCondition(this.condition);
		}

		for (Map.Entry<String, List<SdmlControlSettingInfoResponse>> entrySet : dispDataMap.entrySet()) {
			List<SdmlControlSettingInfoResponse> list = entrySet.getValue();
			if (list == null) {
				list = new ArrayList<SdmlControlSettingInfoResponse>();
			}
			for (SdmlControlSettingInfoResponse info : list) {
				ArrayList<Object> a = new ArrayList<Object>();
				a.add(entrySet.getKey());
				a.add(info.getApplicationId());
				a.add(info.getDescription());
				a.add(info.getFacilityId());
				a.add(HinemosMessage.replace(info.getScope()));
				a.add(info.getValidFlg());
				a.add(info.getControlLogCollectFlg());
				a.add(info.getOwnerRoleId());
				a.add(info.getRegUser());
				a.add(info.getRegDate());
				a.add(info.getUpdateUser());
				a.add(info.getUpdateDate());
				a.add(null);
				listInput.add(a);
			}
		}

		// テーブル更新
		this.tableViewer.setInput(listInput);

		// 合計欄更新
		String[] args = { String.valueOf(listInput.size()) };
		String message = null;
		if (this.condition == null) {
			message = Messages.getString("records", args);
		} else {
			message = Messages.getString("filtered.records", args);
		}
		this.totalLabel.setText(message);
	}

	/**
	 * コンポジットを更新します。
	 * <p>
	 *
	 * 検索条件が事前に設定されている場合、その条件にヒットする監視設定の一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全監視設定を表示します。
	 */
	public void update(Property condition) {
		this.condition = condition;

		this.update();
	}
}
