/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.composite.JobCommandParameterComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.JobCommandParam;

/**
 * ジョブ変数のコンポジットを表示するダイアログクラスです。
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class JobCommandParameterCompositeDialog extends CommonDialog {
	
	private JobCommandParameterComposite m_jobCommandParameterComposite;

	/** ジョブ変数情報 */
	private Map<String, JobCommandParam> m_jobCommandParamMap = new HashMap<>();
	
	private boolean m_readOnly = false;
	
	public Map<String, JobCommandParam> getJobCommandParamMap() {
		return m_jobCommandParamMap;
	}

	public void setJobCommandParamMap(Map<String, JobCommandParam> m_jobCommandParamMap) {
		this.m_jobCommandParamMap = m_jobCommandParamMap;
	}

	/** コンストラクタ
	 * 
	 * @param parent
	 * @param readOnly
	 */
	public JobCommandParameterCompositeDialog(Shell parent, boolean readOnly) {
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
		parent.getShell().setText(Messages.getString("job.command.result.parameter.list"));
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		this.m_jobCommandParameterComposite = new JobCommandParameterComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "jobCommandParameterComposite", this.m_jobCommandParameterComposite);
		this.m_jobCommandParameterComposite.setLayoutData(new GridData());
		
		reflectParamInfo();
		
		m_jobCommandParameterComposite.setEnabled(!m_readOnly);
	}

	/**
	 * ジョブ変数情報をコンポジットに反映します。
	 */
	private void reflectParamInfo() {
		m_jobCommandParameterComposite.setJobCommandParamMap(m_jobCommandParamMap);
		m_jobCommandParameterComposite.reflectParamInfo();
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

		result = m_jobCommandParameterComposite.validateJobCommandParam();
		if(result != null) {
			return result;
		}
		setJobCommandParamMap(m_jobCommandParameterComposite.getJobCommandParamMap());

		return null;
	}
}
