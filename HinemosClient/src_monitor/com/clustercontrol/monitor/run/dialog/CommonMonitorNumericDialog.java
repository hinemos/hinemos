/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.run.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.composite.NumericValueInfoComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNumericValueInfo;

/**
 * 数値系監視設定共通ダイアログクラス<BR>
 *
 */
public class CommonMonitorNumericDialog extends CommonMonitorDialog {

	private Group groupCollect = null;			// 収集グループ

	/** 収集を有効にする */
	private Button confirmCollectValid = null;

	/** 収集値表示名 */
	protected Text itemName = null;

	/** 収集値単位 */
	protected Text measure = null;


	/** 数値監視判定情報 */
	protected NumericValueInfoComposite m_numericValueInfo = null;

	/** 閾値の上限・下限を示す文字列**/
	protected String item1 = null;
	protected String item2 = null;

	/** 判定基準を示す文字列 **/
	protected String criterion1 = Messages.getString("greater");
	protected String criterion2 = Messages.getString("less");

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName
	 *            マネージャ名
	 */
	public CommonMonitorNumericDialog(Shell parent, String managerName) {
		super(parent, managerName);
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		super.customizeDialog(parent);

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = BASIC_MARGIN;
		layout.marginHeight = BASIC_MARGIN;
		layout.numColumns = BASIC_UNIT;
		parent.setLayout(layout);

		// 変数として利用されるラベル
		Label label = null;

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// 数値判定定義情報
		m_numericValueInfo = new NumericValueInfoComposite(groupDetermine,
				SWT.NONE,
				true,
				item1,
				item2,
				criterion1,
				criterion2,
				NumericValueInfoComposite.INPUT_VERIFICATION_REAL_NUMBER);
		WidgetTestUtil.setTestId(this, "numericvalue", m_numericValueInfo);

		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_numericValueInfo.setLayoutData(gridData);

		/*
		 * 収集グループ
		 */
		groupCollect = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "collect", groupCollect);
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		groupCollect.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCollect.setLayoutData(gridData);
		groupCollect.setText(Messages.getString("collection.run"));

		// 収集（有効／無効）
		this.confirmCollectValid = new Button(groupCollect, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "confirmcollectvalid", confirmCollectValid);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.confirmCollectValid.setLayoutData(gridData);
		this.confirmCollectValid.setText(Messages.getString("collection.run"));
		this.confirmCollectValid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 収集エリアを有効/無効化
				if(confirmCollectValid.getSelection()){
					setCollectorEnabled(true);
				}else{
					setCollectorEnabled(false);
				}
			}
		});

		// ラベル（収集値表示名）
		label = new Label(groupCollect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "displayname", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("collection.display.name") + " : ");

		// テキスト（収集値表示名）
		this.itemName = new Text(groupCollect, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "itemName", itemName);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT - (WIDTH_TITLE * 2);
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.itemName.setLayoutData(gridData);
		this.itemName.setText(Messages.getString("select.value"));
		this.itemName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupCollect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		// ラベル（収集値単位）
		label = new Label(groupCollect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "collectionunit", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("collection.unit") + " : ");
		// テキスト（収集値単位）
		this.measure = new Text(groupCollect, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "measure", measure);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT - (WIDTH_TITLE * 2);
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.measure.setLayoutData(gridData);
		this.measure.setText(Messages.getString("collection.unit"));
		this.measure.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
	}

	/**
	 * 収集エリアを有効/無効化します。
	 *
	 */
	private void setCollectorEnabled(boolean enabled){
		itemName.setEnabled(enabled);
		measure.setEnabled(enabled);

		update();
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	protected void update(){
		super.update();

		// 収集値項目名
		if(this.itemName.getEnabled() && "".equals(this.itemName.getText())){
			this.itemName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.itemName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 収集値単位
		if(this.measure.getEnabled() && "".equals(this.measure.getText())){
			this.measure.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.measure.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}


	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor 設定値として用いる監視情報
	 */
	@Override
	protected void setInputData(MonitorInfo monitor) {
		super.setInputData(monitor);

		// 収集
		if (monitor.isCollectorFlg()) {
			this.confirmCollectValid.setSelection(true);
		}else{
			this.setCollectorEnabled(false);
		}

		// 収集値表示名
		if (monitor.getItemName() != null){
			this.itemName.setText(monitor.getItemName());
		}

		// 収集値単位
		if (monitor.getMeasure() != null){
			this.measure.setText(monitor.getMeasure());
		}
		
		// 閾値情報
		if (this.m_MonitorNumericValueInfo != null) {
			List<MonitorNumericValueInfo> monitorNumericValueInfo = monitor.getNumericValueInfo();
			for (MonitorNumericValueInfo valueInfo : monitorNumericValueInfo) {
				for (MonitorNumericValueInfo mValueInfo : m_MonitorNumericValueInfo) {
					if (valueInfo.getPriority().equals(mValueInfo.getPriority())) {
						if (monitor.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PING)) {
							valueInfo.setThresholdLowerLimit(mValueInfo.getThresholdUpperLimit());
						} else {
							valueInfo.setThresholdLowerLimit(mValueInfo.getThresholdLowerLimit());
							valueInfo.setThresholdUpperLimit(mValueInfo.getThresholdUpperLimit());
						}
					}
				}
			}
		}
	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfo createInputData() {
		super.createInputData();
		if(validateResult != null){
			return null;
		}

		// 収集
		monitorInfo.setCollectorFlg(this.confirmCollectValid.getSelection());

		if(this.itemName.getText() != null){
			monitorInfo.setItemName(this.itemName.getText());
		}

		if(this.measure.getText() != null){
			monitorInfo.setMeasure(this.measure.getText());
		}

		// 監視種別を数値に設定する
		monitorInfo.setMonitorType(MonitorTypeConstant.TYPE_NUMERIC);

		return monitorInfo;
	}

}
