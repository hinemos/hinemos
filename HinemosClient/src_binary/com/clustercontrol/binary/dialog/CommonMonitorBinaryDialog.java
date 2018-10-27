/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.dialog;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.binary.action.GetBinaryFilterTableDefine;
import com.clustercontrol.binary.composite.BinaryPatternInfoComposite;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * バイナリ監視設定共通ダイアログクラス<BR>
 *
 * @version 6.1.0 バイナリ監視用フィルタ追加対応
 * @since 6.1.0
 */
public class CommonMonitorBinaryDialog extends CommonMonitorDialog {

	/** バイナリ監視判定情報 */
	protected BinaryPatternInfoComposite m_binaryPatternInfo = null;

	/** メッセージにデフォルトを入れるフラグ */
	protected boolean logLineFlag = false;

	/** 収集グループ */
	private Group groupCollect = null;

	/** 収集を有効にする */
	public Button confirmCollectValid = null;

	public SelectionAdapter collectSelectedListner = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName
	 *            マネージャ名
	 */
	public CommonMonitorBinaryDialog(Shell parent, String managerName) {
		super(parent, managerName);
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		super.customizeDialog(parent);

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// バイナリフィルタ定義情報
		ArrayList<TableColumnInfo> tableDefine = GetBinaryFilterTableDefine.get();
		this.m_binaryPatternInfo = new BinaryPatternInfoComposite(groupDetermine, SWT.NONE, tableDefine, logLineFlag);
		WidgetTestUtil.setTestId(this, null, m_binaryPatternInfo);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 220;
		m_binaryPatternInfo.setLayoutData(gridData);

		/*
		 * 収集グループ
		 */
		groupCollect = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "collect", groupCollect);
		GridLayout layout = new GridLayout(1, true);
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
		this.collectSelectedListner = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 収集エリアを有効/無効化
				if (confirmCollectValid.getSelection()) {
					setCollectorEnabled(true);
				} else {
					setCollectorEnabled(false);
				}
			}
		};
		this.confirmCollectValid.addSelectionListener(collectSelectedListner);
	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfo createInputData() {
		super.createInputData();

		if (validateResult != null) {
			return null;
		}

		// 収集
		monitorInfo.setCollectorFlg(this.confirmCollectValid.getSelection());

		// 監視種別を文字列に設定する
		monitorInfo.setMonitorType(MonitorTypeConstant.TYPE_STRING);

		return monitorInfo;
	}

	/**
	 * 収集エリアを有効/無効化します。
	 *
	 */
	private void setCollectorEnabled(boolean enabled) {

		update();
	}

	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor
	 *            設定値として用いる監視情報
	 */
	@Override
	protected void setInputData(MonitorInfo monitor) {
		super.setInputData(monitor);

		// 収集
		if (monitor.isCollectorFlg()) {
			this.confirmCollectValid.setSelection(true);
		} else {
			this.setCollectorEnabled(false);
		}
	}

}