/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.CommandTemplateResponse;

import com.clustercontrol.notify.action.GetCommandTemplate;
import com.clustercontrol.notify.action.GetCommandTemplateTableDefine;
import com.clustercontrol.notify.composite.action.CommandTemplateDoubleClickListener;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * コマンド通知テンプレート一覧コンポジットクラス<BR>
 *
 */
public class CommandTemplateListComposite extends Composite {
	/** テーブルビューアー */
	private CommonTableViewer tableViewer = null;

	/** 合計ラベル */
	private Label totalLabel = null;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 */
	public CommandTemplateListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 *
	 * @see com.clustercontrol.notify.action.GetCommandTemplateTableDefine#get()
	 * @see #update()
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		tableViewer = new CommonTableViewer(table);
		tableViewer.createTableColumn(GetCommandTemplateTableDefine.get(),
				GetCommandTemplateTableDefine.SORT_COLUMN_INDEX1,
				GetCommandTemplateTableDefine.SORT_COLUMN_INDEX2,
				GetCommandTemplateTableDefine.SORT_ORDER);
		// 列移動が可能に設定
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}

		// ダブルクリックリスナの追加
		tableViewer.addDoubleClickListener(new CommandTemplateDoubleClickListener(this));
		// 合計ラベルの作成
		totalLabel = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		totalLabel.setLayoutData(gridData);
	}

	/**
	 * このコンポジットが利用するテーブルビューアーを返します。
	 *
	 * @return テーブルビューアー
	 */
	public CommonTableViewer getTableViewer() {
		return tableViewer;
	}

	/**
	 * コンポジットを更新します。<BR>
	 * 一覧情報を取得し、テーブルビューアーにセットします。
	 *
	 * @see com.clustercontrol.notify.action.GetCommandTemplate#getCommandTemplateList()
	 */
	@Override
	public void update() {
		List<ArrayList<Object>> inputItemList = new ArrayList<>();

		// コマンド通知テンプレート一覧を各マネージャから取得
		for (String managerName : RestConnectManager.getActiveManagerSet()) {
			for(CommandTemplateResponse info : new GetCommandTemplate().getCommandTemplateList(managerName)) {
				ArrayList<Object> item = new ArrayList<>();
				item.add(managerName);
				item.add(info.getCommandTemplateId());
				item.add(info.getDescription());
				item.add(info.getOwnerRoleId());
				item.add(info.getCreateUser());
				item.add(info.getCreateDate());
				item.add(info.getModifyUser());
				item.add(info.getModifyDate());
				item.add(null);
				inputItemList.add(item);
			}
		}
		// 合計欄更新
		String message = Messages.getString("records", new String[]{ String.valueOf(inputItemList.size()) });
		totalLabel.setText(message);

		// テーブル更新
		tableViewer.setInput(inputItemList);
	}
}
