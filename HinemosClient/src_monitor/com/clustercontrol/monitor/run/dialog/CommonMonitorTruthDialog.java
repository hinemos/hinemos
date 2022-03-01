/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.monitor.run.composite.TruthValueInfoComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 真偽値系監視設定共通ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class CommonMonitorTruthDialog extends CommonMonitorDialog {

	// ----- instance フィールド ----- //

	/** 真偽値監視判定情報 */
	protected TruthValueInfoComposite m_truthValueInfo= null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public CommonMonitorTruthDialog(Shell parent) {
		super(parent, null);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName
	 *            マネージャ名
	 * @param monitorId
	 *            変更する監視項目ID
	 */
	public CommonMonitorTruthDialog(Shell parent, String managerName, String monitorId) {
		super(parent, managerName);

		this.monitorId = monitorId;
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

		// 真偽値判定定義情報
		m_truthValueInfo = new TruthValueInfoComposite(groupDetermine,
				SWT.NONE,
				true,
				Messages.getString("OK"),
				Messages.getString("NG"));
		WidgetTestUtil.setTestId(this, null, m_truthValueInfo);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_truthValueInfo.setLayoutData(gridData);

	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfoResponse createInputData() {
		super.createInputData();
		if(validateResult != null){
			return null;
		}

		// 監視種別を真偽値に設定する
		monitorInfo.setMonitorType(MonitorInfoResponse.MonitorTypeEnum.TRUTH);

		return monitorInfo;
	}
}
