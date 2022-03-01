/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.composite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.openapitools.client.model.NodeConfigFilterInfoRequest;
import org.openapitools.client.model.NodeConfigFilterItemInfoRequest;

import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.DateTimeDialog;
import com.clustercontrol.repository.bean.NodeConfigFilterComparisonMethod;
import com.clustercontrol.repository.bean.NodeConfigFilterDataType;
import com.clustercontrol.repository.bean.NodeConfigFilterItem;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;

/**
 * 構成情報フィルタ条件用コンポジットクラスです。
 *
 * @version 6.2.0
 */
public class NodeListConfigFilterItemComposite extends Composite {

	/** shell */
	private Shell m_shell;

	/** 検索条件 */
	private NodeConfigFilterInfoRequest m_filterInfo = null;

	/** 親Composite */
	private Composite m_parentComposite;

	/** Coposite */
	private Composite m_composite;

	/** 各項目のCompositeマップ(項目名, NodeFilterCompositeItem) */
	private HashMap<String, NodeFilterCompositeItem> m_compositeItemMap = new HashMap<>();

	/**
	 * コンストラクタ
	 *
	 * @param parent 親コンポジット
	 * @param style スタイル
	 * @param filterInfo 検索条件
	 */
	public NodeListConfigFilterItemComposite(Composite parent, int style, NodeConfigFilterInfoRequest filterInfo) {
		super(parent, style);
		m_filterInfo = filterInfo;
		m_parentComposite = parent;
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		m_shell = this.getShell();

		// 入力欄
		m_composite = new Composite(m_parentComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 4;
		m_composite.setLayoutData(gridData);
		m_composite.setLayout(new GridLayout(4, false));

		m_shell.layout();
	}

	/**
	 * 
	 * 選択された構成情報種別に応じて入力欄を作成
	 * 
	 * @param filterInfo 検索条件
	 */
	public void createItemsComposite(NodeConfigFilterInfoRequest filterInfo) {

		m_compositeItemMap.clear();

		m_filterInfo = filterInfo;

		// 既存の入力欄を削除
		for (Widget widget : m_composite.getChildren()) {
			widget.dispose();
		}

		// 対象項目が設定されていない場合は処理終了
		if (m_filterInfo.getNodeConfigSettingItemName() == null) {
			return;
		}

		// 入力欄の生成
		List<NodeConfigFilterItem> itemList = NodeConfigFilterItem.getTargetItemList(
				NodeConfigSettingItem.valueOf(m_filterInfo.getNodeConfigSettingItemName().getValue()));

		for (NodeConfigFilterItem itemInfo : itemList) {
			// 項目名 (Label)
			Label displayNameLabel = new Label(m_composite, SWT.LEFT | SWT.WRAP);
			displayNameLabel.setText(itemInfo.displayName());
			displayNameLabel.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));

			// 比較演算子 (ComboBox)
			Combo methodCombo = new Combo(m_composite, SWT.DROP_DOWN | SWT.READ_ONLY);
			methodCombo.setLayoutData(new GridData(50, SizeConstant.SIZE_COMBO_HEIGHT));
			List<String> methodList = new ArrayList<>();
			methodList.add("");
			if (itemInfo.dataType().isOnlyEqual()) {
				methodList.addAll(NodeConfigFilterComparisonMethod.symbolsForString());
			} else {
				methodList.addAll(NodeConfigFilterComparisonMethod.symbols());
			}
			for (String methodStr : methodList) {
				methodCombo.add(methodStr);
			}

			// 値 (Text)
			Text valueText = new Text(m_composite, SWT.BORDER);
			valueText.setLayoutData(new GridData(250, SizeConstant.SIZE_TEXT_HEIGHT));
			if (itemInfo.dataType() == NodeConfigFilterDataType.DATETIME) {
				// DATE
				// 日時データの場合は、日時ダイアログからの入力しか受け付けない
				valueText.setEditable(false);
				valueText.addModifyListener(new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				});

				// 日時用ボタン
				Button valueDateButton = new Button(m_composite, SWT.NONE);
				valueDateButton.setText(Messages.getString("calendar.button"));
				valueDateButton.setData(itemInfo.name());
				valueDateButton.setLayoutData(new GridData(30, SizeConstant.SIZE_BUTTON_HEIGHT));
				valueDateButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Button button = (Button)e.getSource();

						DateTimeDialog dialog = new DateTimeDialog(m_shell);
						if (valueText.getText().length() > 0) {
							Date date = new Date(m_compositeItemMap.get((String)button.getData()).getDatetimeValue());
							dialog.setDate(date);
						}
						if (dialog.open() == IDialogConstants.OK_ID) {
							NodeFilterCompositeItem compositeItem = m_compositeItemMap.get((String)button.getData()); 
							//取得した日時をLong型で保持
							compositeItem.setDatetimeValue(dialog.getDate().getTime());
							//ダイアログより取得した日時を"yyyy/MM/dd HH:mm:ss"の形式に変換
							SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
							String tmp = sdf.format(dialog.getDate());
							compositeItem.getValueText().setText(tmp);
							update();
						}
					}
				});

			} else {
				// dummy
				Label dummy = new Label(m_composite, SWT.NONE);
				dummy.setLayoutData(new GridData());
			}
			m_compositeItemMap.put(itemInfo.name(), new NodeFilterCompositeItem(methodCombo, valueText));
		}

		// データ設定
		setInputData();

		m_composite.layout(); 
	}

	/**
	 * 
	 * 選択された検索条件を削除
	 * 
	 */
	public void dropItemsComposite() {

		// 既存の入力欄を削除
		for (Widget widget : m_composite.getChildren()) {
			widget.dispose();
		}
	}

	/**
	 * 検索条件を入力欄に設定します。
	 */
	public void setInputData() {

		if (m_filterInfo.getItemList() == null) {
			return;
		}

		for (NodeConfigFilterItemInfoRequest itemInfo : m_filterInfo.getItemList()) {
			NodeFilterCompositeItem compositeItem = m_compositeItemMap.get(itemInfo.getItemName().getValue());

			// 値
			String inputValue = "";
			NodeConfigFilterItem filterItem = NodeConfigFilterItem.valueOf(itemInfo.getItemName().getValue());
			if (filterItem.dataType().equals(NodeConfigFilterDataType.INTEGER_ONLYEQUAL)
					|| filterItem.dataType().equals(NodeConfigFilterDataType.INTEGER)) {
				// INTEGER
				inputValue = itemInfo.getItemIntegerValue().toString();
				
			} else if (filterItem.dataType().equals(NodeConfigFilterDataType.STRING)
					|| filterItem.dataType().equals(NodeConfigFilterDataType.STRING_ONLYEQUAL)
					|| filterItem.dataType().equals(NodeConfigFilterDataType.STRING_VERSION)) {
				// STRING
				inputValue = itemInfo.getItemStringValue();
			} else if (filterItem.dataType().equals(NodeConfigFilterDataType.DATETIME)) {
				// 日時
				SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
				inputValue = sdf.format(new Date(itemInfo.getItemLongValue()));
				compositeItem.setDatetimeValue(itemInfo.getItemLongValue());
			}
			compositeItem.getValueText().setText(inputValue);

			// 比較演算子
			compositeItem.getMethodCombo().setText(itemInfo.getMethod());
		}
	}

	/**
	 * 入力された値をNodeConfigFilterInfoに設定する。
	 * <p>
	 * 
	 */
	public void createInputData() {

		m_filterInfo.getItemList().clear();
		for (Map.Entry<String, NodeFilterCompositeItem> entry : m_compositeItemMap.entrySet()) {
			if (entry.getValue().getMethodCombo().getText() == null
					|| entry.getValue().getMethodCombo().getText().isEmpty()
					|| entry.getValue().getValueText().getText() == null
					|| entry.getValue().getValueText().getText().isEmpty()) {
				continue;
			}

			// 属性を取得
			NodeConfigFilterItem itemType = NodeConfigFilterItem.valueOf(entry.getKey());
			NodeConfigFilterItemInfoRequest itemInfo = new NodeConfigFilterItemInfoRequest();
			itemInfo.setItemName(NodeConfigFilterItemInfoRequest.ItemNameEnum.fromValue(itemType.name()));

			// 比較演算子
			itemInfo.setMethod(entry.getValue().getMethodCombo().getText());

			// 値
			if (itemType.dataType() == NodeConfigFilterDataType.INTEGER
					|| itemType.dataType() == NodeConfigFilterDataType.INTEGER_ONLYEQUAL) {
				try {
					itemInfo.setItemIntegerValue(Integer.valueOf(entry.getValue().getValueText().getText()));
				} catch (NumberFormatException e) {
					// 数値以外の場合は対象外とする
					continue;
				}
			} else if (itemType.dataType() == NodeConfigFilterDataType.STRING
					|| itemType.dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL
					|| itemType.dataType() == NodeConfigFilterDataType.STRING_VERSION) {
				itemInfo.setItemStringValue(entry.getValue().getValueText().getText());
			} else if (itemType.dataType() == NodeConfigFilterDataType.DATETIME) {
				itemInfo.setItemLongValue(entry.getValue().getDatetimeValue());
			}
			m_filterInfo.getItemList().add(itemInfo);
		}
	}

	/**
	 * NodeConfigFilterInfoを返す。
	 *
	 * @return NodeConfigFilterInfo
	 * 
	 */
	public NodeConfigFilterInfoRequest getNodeConfigFilterInfo() {
		return m_filterInfo;
	}

	@Override
	public void layout() {
		super.layout();
		m_composite.layout();
	}

	/**
	 * 入力項目情報
	 */
	static class NodeFilterCompositeItem {

		// 比較演算子Combo
		private Combo m_methodCombo;
		// 値Text
		private Text m_valueText;
		// 日時用値Text
		private Long m_datetimeValue;

		NodeFilterCompositeItem(Combo methodCombo, Text valueText) {
			this.m_methodCombo = methodCombo;
			this.m_valueText = valueText;
		}

		public Combo getMethodCombo() {
			return m_methodCombo;
		}

		public Text getValueText() {
			return m_valueText;
		}

		public Long getDatetimeValue() {
			return m_datetimeValue;
		}
		public void setDatetimeValue(Long datetimeValue) {
			this.m_datetimeValue = datetimeValue;
		}
	}
}
