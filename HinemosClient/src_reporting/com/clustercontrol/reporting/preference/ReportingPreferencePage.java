/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.preference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.util.Messages;

/**
 * レポーティング機能の設定ページクラス
 *
 * @version 5.0.a
 * @since 5.0.a
 *
 */
public class ReportingPreferencePage extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	private static Log log = LogFactory.getLog(ReportingPreferencePage.class);

	/** レポートダウンロード待ち時間 */
	public static final String P_DL_MAX_WAIT = "downloadMaxWait";
	private static final String MSG_R_MAX_WAIT = Messages.getString("report.download.max.wait");

	/** レポートダウンロード確認間隔 */
	public static final String P_DL_CHECK_INTREVAL = "downloadCheckInterval";
	private static final String MSG_DL_R_DL_CHECK_INTREVAL = Messages.getString("report.download.check.interval");


	/**
	 * デフォルトコンストラクタ
	 */
	public ReportingPreferencePage() {
		super(GRID);
		log.debug("ReportingPreferencePage()");

		this.setPreferenceStore(ClusterControlPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * フィールドの作成
	 */
	@Override
	protected void createFieldEditors() {
		log.debug("createFieldEditors()");

		Composite parent = this.getFieldEditorParent();
		GridData gridData = null;

		// レポーティング[スケジュール]ビュー関連
		Group reportingGroup = new Group(parent, SWT.SHADOW_NONE);
		WidgetTestUtil.setTestId(this, null, reportingGroup);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;
		reportingGroup.setLayoutData(gridData);
		reportingGroup.setText(Messages.getString("view.reporting.schedule"));

		// レポートダウンロード待ち時間
		IntegerFieldEditor downloadMaxWait = new IntegerFieldEditor(P_DL_MAX_WAIT,
				MSG_R_MAX_WAIT, reportingGroup);
		downloadMaxWait.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		String[] args1 = { Integer.toString(1),
				Integer.toString(DataRangeConstant.SMALLINT_HIGH) };
		downloadMaxWait.setErrorMessage(Messages
				.getString("message.hinemos.8", args1));
		this.addField(downloadMaxWait);

		// レポートダウンロード確認間隔
		IntegerFieldEditor downloadCheckInterval = new IntegerFieldEditor(P_DL_CHECK_INTREVAL,
				MSG_DL_R_DL_CHECK_INTREVAL, reportingGroup);
		downloadCheckInterval.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		String[] args2 = { Integer.toString(1),
				Integer.toString(DataRangeConstant.SMALLINT_HIGH) };
		downloadCheckInterval.setErrorMessage(Messages
				.getString("message.hinemos.8", args2));
		this.addField(downloadCheckInterval);
	}

	/**
	 * ボタン押下時に設定反映
	 */
	@Override
	public boolean performOk() {
		log.debug("performOk()");
		applySetting();
		return super.performOk();
	}

	/**
	 * 設定内容を反映します。
	 */
	private void applySetting() {
	}

	/**
	 * 初期化
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

}
