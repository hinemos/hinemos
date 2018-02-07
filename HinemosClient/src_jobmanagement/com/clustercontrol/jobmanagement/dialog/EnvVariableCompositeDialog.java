/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.composite.EnvVariableComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.JobEnvVariableInfo;

/**
 * 環境変数のコンポジットを表示するダイアログクラスです。
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class EnvVariableCompositeDialog extends CommonDialog {
	
	private EnvVariableComposite m_envVariableComposite;
	
	private List<JobEnvVariableInfo> m_jobEnvVariableInfo = new ArrayList<JobEnvVariableInfo>();
	
	private boolean m_readOnly = false;
	
	public List<JobEnvVariableInfo> getJobEnvVariableInfo() {
		return m_jobEnvVariableInfo;
	}

	public void setJobEnvVariableInfo(List<JobEnvVariableInfo> m_jobEnvVariableInfo) {
		this.m_jobEnvVariableInfo = m_jobEnvVariableInfo;
	}

	/** コンストラクタ
	 * 
	 * @param parent
	 * @param readOnly
	 */
	public EnvVariableCompositeDialog(Shell parent, boolean readOnly) {
		super(parent);
		this.m_readOnly = readOnly;
	}
	
	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 *
	 * @see com.clustercontrol.monitor.action.GetEventFilterProperty#getProperty()
	 * @see com.clustercontrol.bean.JobParamTypeConstant
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		parent.getShell().setText(Messages.getString("job.environment.variable.list"));
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		this.m_envVariableComposite = new EnvVariableComposite(parent, SWT.NONE, false);
		WidgetTestUtil.setTestId(this, "envVariableComposite", this.m_envVariableComposite);
		this.m_envVariableComposite.setLayoutData(new GridData());
		
		reflectEnvVariableInfo();
		
		m_envVariableComposite.setEnabled(!m_readOnly);
	}

	/**
	 * 環境変数情報をコンポジットに反映します。
	 */
	private void reflectEnvVariableInfo() {
		m_envVariableComposite.setEnvVariableList(m_jobEnvVariableInfo);
		m_envVariableComposite.reflectEnvVariableInfo();
	}
	
	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 * @since 2.1.0
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 * @since 2.1.0
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}
	
	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		result = m_envVariableComposite.createEnvVariableInfo();
		if(result != null) {
			return result;
		}
		setJobEnvVariableInfo(m_envVariableComposite.getEnvVariableList());

		return null;
	}
}
