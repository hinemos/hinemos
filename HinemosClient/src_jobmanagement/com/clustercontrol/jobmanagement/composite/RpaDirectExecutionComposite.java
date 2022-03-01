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
 * RPAシナリオ 直接実行タブ用のコンポジットクラスです
 */
public class RpaDirectExecutionComposite extends Composite {

	/** タブフォルダー */
	private TabFolder m_tabFolder;

	/** シナリオ実行タブコンポジット */
	private RpaDirectScenarioComposite m_directScenarioComposite = null;

	/** 制御タブコンポジット */
	private RpaDirectControlComposite m_directControlComposite = null;

	/** 終了値タブコンポジット */
	private RpaDirectEndValueComposite m_directEndValueComposite = null;

	/** RPAシナリオジョブ情報 */
	private JobRpaInfoResponse m_rpa = null;

	/** マネージャ名 */
	private String m_managerName = null;

	public RpaDirectExecutionComposite(Composite parent, int style, String managerName) {
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
		this.m_directScenarioComposite = new RpaDirectScenarioComposite(this.m_tabFolder, SWT.NONE, m_managerName);
		scenarioExecTabItem.setControl(this.m_directScenarioComposite);

		// 制御タブ
		TabItem controlTabItem = new TabItem(this.m_tabFolder, SWT.NONE);
		controlTabItem.setText(Messages.getString("control"));
		this.m_directControlComposite = new RpaDirectControlComposite(this.m_tabFolder, SWT.NONE, m_managerName);
		controlTabItem.setControl(this.m_directControlComposite);

		// 終了値タブ
		TabItem endValueTabItem = new TabItem(this.m_tabFolder, SWT.NONE);
		endValueTabItem.setText(Messages.getString("end.value"));
		this.m_directEndValueComposite = new RpaDirectEndValueComposite(this.m_tabFolder, SWT.NONE);
		endValueTabItem.setControl(this.m_directEndValueComposite);
	}

	public void reflectRpaJobInfo() {
		// シナリオ実行タブ
		this.m_directScenarioComposite.reflectRpaJobInfo();
		// 制御タブ
		this.m_directControlComposite.reflectRpaJobInfo();
		// 終了値タブ
		this.m_directEndValueComposite.reflectRpaJobInfo();
	}

	public ValidateResult validateRpaJobInfo() {
		ValidateResult result = null;
		// シナリオ実行タブ
		if ((result = this.m_directScenarioComposite.validateRpaJobInfo()) != null) {
			return result;
		}
		// 制御タブ
		if ((result = this.m_directControlComposite.validateRpaJobInfo()) != null) {
			return result;
		}
		// 終了値タブ
		if ((result = this.m_directEndValueComposite.validateRpaJobInfo()) != null) {
			return result;
		}
		return result;
	}
	
	public void createRpaJobInfo() {
		// シナリオ実行タブ
		this.m_directScenarioComposite.createRpaJobInfo();
		// 制御タブ
		this.m_directControlComposite.createRpaJobInfo();
		// 終了値タブ
		this.m_directEndValueComposite.createRpaJobInfo();
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		// シナリオ実行タブ
		this.m_directScenarioComposite.setEnabled(enabled);
		// 制御タブ
		this.m_directControlComposite.setEnabled(enabled);
		// 終了値タブ
		this.m_directEndValueComposite.setEnabled(enabled);
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.m_directScenarioComposite.setOwnerRoleId(ownerRoleId);
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
		this.m_directScenarioComposite.setRpaJobInfo(rpa);
		// 制御タブ
		this.m_directControlComposite.setRpaJobInfo(rpa);
		// 終了値タブ
		this.m_directEndValueComposite.setRpaJobInfo(rpa);
	}

	/**
	 * RPAシナリオジョブ種別を設定します。<br>
	 * 必須項目のチェック有無を判断するために使用します。
	 * @param rpaJobType
	 */
	public void setRpaJobType(int rpaJobType) {
		m_directScenarioComposite.setRpaJobType(rpaJobType);
		m_directControlComposite.setRpaJobType(rpaJobType);
		m_directEndValueComposite.setRpaJobType(rpaJobType);
	}
}
