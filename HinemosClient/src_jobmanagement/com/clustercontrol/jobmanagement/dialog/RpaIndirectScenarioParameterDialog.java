/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;

public class RpaIndirectScenarioParameterDialog extends CommonDialog {

	/** シナリオ入力パラメータテキストボックス */
	private Text m_scenarioParamText = null;

	/** シナリオ入力パラメータ */
	private String m_scenarioParam = null;

	/** 読み取り専用フラグ */
	private boolean m_readOnly = false;

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *            親コンポジット
	 */
	public RpaIndirectScenarioParameterDialog(Shell parent, boolean readOnly) {
		super(parent);
		this.m_readOnly = readOnly;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		// ダイアログタイトル
		parent.getShell().setText(Messages.getString("dialog.job.create.modify.rpa.scenario.parameter"));

		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		// シナリオ入力パラメータ（グループ）
		Group scenarioParamGroup = new Group(parent, SWT.NONE);
		scenarioParamGroup.setText(Messages.getString("rpa.scenario.parameter"));
		scenarioParamGroup.setLayout(new GridLayout(1, false));

		// シナリオ入力パラメータ（テキスト）
		m_scenarioParamText = new Text(scenarioParamGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		GridData scenarioParamGrid = new GridData(500, 350);
		m_scenarioParamText.setLayoutData(scenarioParamGrid);
		// 変更の場合に値を反映
		if (m_scenarioParam != null) {
			m_scenarioParamText.setText(m_scenarioParam);
		}

		// コンポーネントの無効/有効
		if (m_readOnly) {
			m_scenarioParamText.setEditable(false);
		} else {
			m_scenarioParamText.setEditable(true);
		}
	}

	/**
	 * 入力値チェック
	 *
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		// JSON形式のチェックはマネージャ側で行う
		m_scenarioParam = m_scenarioParamText.getText();
		return null;
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * @return the m_scenarioParam
	 */
	public String getScenarioParam() {
		return m_scenarioParam;
	}

	/**
	 * @param m_scenarioParam
	 *            the m_scenarioParam to set
	 */
	public void setScenarioParam(String m_scenarioParam) {
		this.m_scenarioParam = m_scenarioParam;
	}
}
