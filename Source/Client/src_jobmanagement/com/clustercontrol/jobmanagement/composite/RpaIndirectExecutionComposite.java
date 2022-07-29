/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.openapitools.client.model.JobRpaInfoResponse;

import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;

/**
 * RPAシナリオ 間接実行タブ用のコンポジットクラスです
 */
public class RpaIndirectExecutionComposite extends Composite {
	/** タブフォルダー */
	private TabFolder m_tabFolder;

	/** シナリオ実行タブコンポジット */
	private RpaIndirectScenarioComposite m_indirectScenarioComposite = null;

	/** 制御タブコンポジット */
	private RpaIndirectControlComposite m_indirectControlComposite = null;

	/** 終了値タブコンポジット */
	private RpaIndirectEndValueComposite m_indirectEndValueComposite = null;

	/** RPAシナリオジョブ情報 */
	private JobRpaInfoResponse m_rpa = null;

	/** マネージャ名 */
	private String m_managerName = null;

	public RpaIndirectExecutionComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.m_managerName = managerName;
		initialize();
	}

	private void initialize() {
		this.setLayout(JobDialogUtil.getParentLayout());
		this.m_tabFolder = new TabFolder(this, SWT.NONE);
		// シナリオ実行タブ
		TabItem scenarioExecTabItem = new TabItem(this.m_tabFolder, SWT.NONE);
		scenarioExecTabItem.setText(Messages.getString("rpa.scenario.execution"));
		this.m_indirectScenarioComposite = new RpaIndirectScenarioComposite(this.m_tabFolder, SWT.NONE, m_managerName);
		scenarioExecTabItem.setControl(this.m_indirectScenarioComposite);

		// 制御タブ
		TabItem controlTabItem = new TabItem(this.m_tabFolder, SWT.NONE);
		controlTabItem.setText(Messages.getString("control"));
		this.m_indirectControlComposite = new RpaIndirectControlComposite(this.m_tabFolder, SWT.NONE);
		controlTabItem.setControl(this.m_indirectControlComposite);

		// 終了値タブ
		TabItem endValueTabItem = new TabItem(this.m_tabFolder, SWT.NONE);
		endValueTabItem.setText(Messages.getString("end.value"));
		this.m_indirectEndValueComposite = new RpaIndirectEndValueComposite(this.m_tabFolder, SWT.NONE, m_managerName);
		endValueTabItem.setControl(this.m_indirectEndValueComposite);
		// シナリオ実行タブに終了値タブのオブジェクトをセット
		this.m_indirectScenarioComposite.setRpaIndirectEndValueComposite(m_indirectEndValueComposite);
	}

	public void reflectRpaJobInfo() {
		// シナリオ実行タブ
		this.m_indirectScenarioComposite.reflectRpaJobInfo();
		// 制御タブ
		this.m_indirectControlComposite.reflectRpaJobInfo();
		// 終了値タブ
		this.m_indirectEndValueComposite.reflectRpaJobInfo();
	}

	public ValidateResult validateRpaJobInfo() {
		ValidateResult result = null;
		// シナリオ実行タブ
		if ((result = this.m_indirectScenarioComposite.validateRpaJobInfo()) != null) {
			return result;
		}
		// 制御タブ
		if ((result = this.m_indirectControlComposite.validateRpaJobInfo()) != null) {
			return result;
		}
		// 終了値タブ
		// RpaIndirectEndValueTableViewerでバリデーションを行っているためこでは不要
		return result;
	}

	public void createRpaJobInfo() {
		// シナリオ実行タブ
		this.m_indirectScenarioComposite.createRpaJobInfo();
		// 制御タブ
		this.m_indirectControlComposite.createRpaJobInfo();
		// 終了値タブ
		this.m_indirectEndValueComposite.createRpaJobInfo();
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		// シナリオ実行タブ
		m_indirectScenarioComposite.setEnabled(enabled);
		// 制御タブ
		m_indirectControlComposite.setEnabled(enabled);
		// 終了値タブ
		m_indirectEndValueComposite.setEnabled(enabled);
	}

	/**
	 * @return the m_rpa
	 */
	public JobRpaInfoResponse getRpaJobInfo() {
		return this.m_rpa;
	}

	/**
	 * @param m_rpa
	 *            the m_rpa to set
	 */
	public void setRpaJobInfo(JobRpaInfoResponse rpa) {
		this.m_rpa = rpa;
		// シナリオ実行タブ
		this.m_indirectScenarioComposite.setRpaJobInfo(rpa);
		// 制御タブ
		this.m_indirectControlComposite.setRpaJobInfo(rpa);
		// 終了値タブ
		this.m_indirectEndValueComposite.setRpaJobInfo(rpa);
	}

	/**
	 * RPAシナリオジョブ種別を設定します。<br>
	 * 必須項目のチェック有無を判断するために使用します。
	 * 
	 * @param rpaJobType
	 */
	public void setRpaJobType(int rpaJobType) {
		m_indirectScenarioComposite.setRpaJobType(rpaJobType);
		m_indirectControlComposite.setRpaJobType(rpaJobType);
	}
}
