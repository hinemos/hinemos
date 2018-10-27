/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.preference;

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

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 性能管理機能の設定ページクラス
 *
 * @version 4.0.0
 * @since 2.0.0
 *
 */
public class PerformancePreferencePage extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	private static Log log = LogFactory.getLog(PerformancePreferencePage.class);

	/** グラフデータ取得数の最大値 */
	public static final String P_GRAPH_MAX = "graphMax";
	private static final String MSG_GRAPH_MAX = Messages.getString("collection.graph.max.line");
	/** グラフデータ取得数の最大値の最大値 */
	private static final int COLLECT_GRAPH_DATA_MAX_SIZE = 1000;
	/** グラフデータ取得数の最大値の最小値 */
	private static final int COLLECT_GRAPH_DATA_MIN_SIZE = 1;
	

	/** 性能データダウンロード待ち時間 */
	public static final String P_DL_MAX_WAIT = "downloadMaxWait";
	private static final String MSG_DL_MAX_WAIT = Messages.getString("collection.export.max.wait");
	/** 性能データダウンロード待ち時間の最大値 */
	private static final int COLLECT_GRAPH_DOWNLOAD_MAX_WAIT_MINUTE = 60;
	/** 性能データダウンロード待ち時間の最小値 */
	private static final int COLLECT_GRAPH_DOWNLOAD_MIN_WAIT_MINUTE = 1;


	/**
	 * デフォルトコンストラクタ
	 */
	public PerformancePreferencePage() {
		super(GRID);
		log.debug("PerformancePreferencePage()");

		this.setPreferenceStore(ClusterControlPlugin.getDefault()
				.getPreferenceStore());
	}

	/**
	 * フィールドの作成
	 */
	@Override
	protected void createFieldEditors() {
		log.debug("createFieldEditors()");

		Composite parent = this.getFieldEditorParent();
		GridData gridData = null;

		// 性能[グラフ]ビュー関連
		Group perfGroup = new Group(parent, SWT.SHADOW_NONE);
		WidgetTestUtil.setTestId(this, null, perfGroup);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;
		perfGroup.setLayoutData(gridData);
		perfGroup.setText(Messages.getString("view.performance.graph.default"));

		// グラフ表示数の最大値
		IntegerFieldEditor graphMax = new IntegerFieldEditor(P_GRAPH_MAX,
				MSG_GRAPH_MAX, perfGroup);
		graphMax.setValidRange(COLLECT_GRAPH_DATA_MIN_SIZE, COLLECT_GRAPH_DATA_MAX_SIZE);
		String[] args1 = { Integer.toString(COLLECT_GRAPH_DATA_MIN_SIZE),
				Integer.toString(COLLECT_GRAPH_DATA_MAX_SIZE) };
		graphMax.setErrorMessage(Messages
				.getString("message.hinemos.8", args1));
		this.addField(graphMax);

		// 性能データダウンロード待ち時間
		IntegerFieldEditor downloadMaxWait = new IntegerFieldEditor(P_DL_MAX_WAIT,
				MSG_DL_MAX_WAIT, perfGroup);
		downloadMaxWait.setValidRange(COLLECT_GRAPH_DOWNLOAD_MIN_WAIT_MINUTE, 
				COLLECT_GRAPH_DOWNLOAD_MAX_WAIT_MINUTE);
		String[] args2 = { Integer.toString(COLLECT_GRAPH_DOWNLOAD_MIN_WAIT_MINUTE),
				Integer.toString(COLLECT_GRAPH_DOWNLOAD_MAX_WAIT_MINUTE) };
		downloadMaxWait.setErrorMessage(Messages
				.getString("message.hinemos.8", args2));
		this.addField(downloadMaxWait);
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
