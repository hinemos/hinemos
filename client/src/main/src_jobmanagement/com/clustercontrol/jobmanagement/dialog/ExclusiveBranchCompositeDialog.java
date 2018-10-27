/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.composite.ExclusiveBranchComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.JobWaitRuleInfo;

public class ExclusiveBranchCompositeDialog extends CommonDialog{
	
	private ExclusiveBranchComposite m_exclusiveBranchComposite;
	private boolean m_readOnly = false;
	private JobWaitRuleInfo m_waitRule;
	private JobTreeItem m_jobTreeItem;

	/** コンストラクタ
	 * 
	 * @param parent
	 * @param readOnly
	 */
	public ExclusiveBranchCompositeDialog(Shell parent, boolean readOnly) {
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
		parent.getShell().setText(Messages.getString("job.exclusive.branch"));
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		this.m_exclusiveBranchComposite = new ExclusiveBranchComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "jobExclusiveBranchComposite", this.m_exclusiveBranchComposite);
		this.m_exclusiveBranchComposite.setLayoutData(new GridData());
		
		reflectExclusiveBranchInfo();
		
		m_exclusiveBranchComposite.setEnabled(!m_readOnly);
	}

	/**
	 * ジョブ変数情報をコンポジットに反映します。
	 */
	private void reflectExclusiveBranchInfo() {
		m_exclusiveBranchComposite.setWaitRuleInfo(m_waitRule);
		m_exclusiveBranchComposite.setJobTreeItem(m_jobTreeItem);
		m_exclusiveBranchComposite.reflectExclusiveBranchInfo();
	}

	/**
	 * ジョブ待ち条件情報を設定します。
	 *
	 * @param waitRule ジョブ待ち条件情報
	 */
	public void setWaitRuleInfo(JobWaitRuleInfo waitRule) {
		m_waitRule = waitRule;
	}

	/**
	 * ジョブ待ち条件情報を返します。
	 *
	 * @return ジョブ待ち条件情報
	 */
	public JobWaitRuleInfo getWaitRuleInfo() {
		return m_waitRule;
	}

	/**
	 * 後続ジョブ表示用JobTreeItemを設定します。
	 *
	 * @return ジョブ待ち条件情報
	 */
	public void setJobTreeItem(JobTreeItem jobTreeItem) {
		m_jobTreeItem = jobTreeItem;
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
		
		result = m_exclusiveBranchComposite.createExclusiveBranchInfo();
		if (result != null) {
			return result;
		}
		setWaitRuleInfo(m_exclusiveBranchComposite.getWaitRuleInfo());
		return null;
	}
}