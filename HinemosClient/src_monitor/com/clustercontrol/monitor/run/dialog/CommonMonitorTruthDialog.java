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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.composite.TruthValueInfoComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.MonitorInfo;
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
	protected MonitorInfo createInputData() {
		super.createInputData();
		if(validateResult != null){
			return null;
		}

		// 監視種別を真偽値に設定する
		monitorInfo.setMonitorType(MonitorTypeConstant.TYPE_TRUTH);

		return monitorInfo;
	}
}
