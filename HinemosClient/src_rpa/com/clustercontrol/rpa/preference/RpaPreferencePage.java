/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.preference;

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

/**
 * RPA管理機能の設定ページクラス
 *
 */
public class RpaPreferencePage extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	private static Log log = LogFactory.getLog(RpaPreferencePage.class);

	/** シナリオ表示数 */
	public static final String P_MAX_DISPLAY_SCENARIOS = "maxDisplayScenarios";
	private static final String MSG_R_MAX_DISPLAY_SCENARIOS = Messages.getString("rpa.scenario.summary.graph.max.display.scenarios");

	/** ノード表示数 */
	public static final String P_MAX_DISPLAY_NODES = "maxDisplayNodes";
	private static final String MSG_R_MAX_DISPLAY_NODES = Messages.getString("rpa.scenario.summary.graph.max.display.nodes");


	/**
	 * デフォルトコンストラクタ
	 */
	public RpaPreferencePage() {
		super(GRID);
		log.debug("RpaPreferencePage()");

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

		// 集計グラフビュー関連
		Group rpaGroup = new Group(parent, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;
		rpaGroup.setLayoutData(gridData);
		rpaGroup.setText(Messages.getString("view.rpa.scenario.summary.graph"));

		// シナリオ表示数
		IntegerFieldEditor maxDisplayScenario = new IntegerFieldEditor(P_MAX_DISPLAY_SCENARIOS,
				MSG_R_MAX_DISPLAY_SCENARIOS, rpaGroup);
		maxDisplayScenario.setValidRange(1, 200);
		String[] args1 = { Integer.toString(1),
				Integer.toString(200) };
		maxDisplayScenario.setErrorMessage(Messages
				.getString("message.hinemos.8", args1));
		this.addField(maxDisplayScenario);

		// ノード表示数
		IntegerFieldEditor maxDisplayNode = new IntegerFieldEditor(P_MAX_DISPLAY_NODES,
				MSG_R_MAX_DISPLAY_NODES, rpaGroup);
		maxDisplayNode.setValidRange(1, 200);
		String[] args2 = { Integer.toString(1),
				Integer.toString(200) };
		maxDisplayNode.setErrorMessage(Messages
				.getString("message.hinemos.8", args2));
		this.addField(maxDisplayNode);
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
