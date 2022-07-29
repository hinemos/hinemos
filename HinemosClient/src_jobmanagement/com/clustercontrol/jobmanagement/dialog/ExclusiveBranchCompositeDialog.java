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
import org.openapitools.client.model.JobNextJobOrderInfoResponse;
import org.openapitools.client.model.JobWaitRuleInfoResponse;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.composite.ExclusiveBranchComposite;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

public class ExclusiveBranchCompositeDialog extends CommonDialog{
	
	private ExclusiveBranchComposite m_exclusiveBranchComposite;
	private boolean m_readOnly = false;
	private JobTreeItemWrapper m_jobTreeItem;

	/** ジョブ待ち条件情報（後続ジョブ実行設定） */
	/** 排他分岐 */
	private boolean m_exclusiveBranch;
	/** 排他分岐の終了状態 */
	private  JobWaitRuleInfoResponse.ExclusiveBranchEndStatusEnum m_exclusiveBranchEndStatus = null;
	/** 排他分岐の終了値 */
	private Integer m_exclusiveBranchEndValue = null;
	/** 排他分岐の優先度リスト */
	private List<JobNextJobOrderInfoResponse> m_exclusiveBranchNextJobOrderList = new ArrayList<>();

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
		m_exclusiveBranchComposite.setExclusiveBranchRtn(m_exclusiveBranch);
		m_exclusiveBranchComposite.setExclusiveBranchEndStatusRtn(m_exclusiveBranchEndStatus);
		m_exclusiveBranchComposite.setExclusiveBranchEndValueRtn(m_exclusiveBranchEndValue);
		m_exclusiveBranchComposite.setExclusiveBranchNextJobOrderListRtn(m_exclusiveBranchNextJobOrderList);
		m_exclusiveBranchComposite.setJobTreeItem(m_jobTreeItem);
		m_exclusiveBranchComposite.reflectExclusiveBranchInfo();
	}

	/**
	 * 後続ジョブ表示用JobTreeItemを設定します。
	 *
	 * @return ジョブ待ち条件情報
	 */
	public void setJobTreeItem(JobTreeItemWrapper jobTreeItem) {
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
	 * 排他分岐を設定します。
	 *
	 * @param m_exclusiveBranch 排他分岐
	 */
	public boolean isExclusiveBranch() {
		return m_exclusiveBranch;
	}

	/**
	 * 排他分岐を返します。
	 *
	 * @return 排他分岐
	 */
	public void setExclusiveBranch(boolean m_exclusiveBranch) {
		this.m_exclusiveBranch = m_exclusiveBranch;
	}

	/**
	 * 排他分岐の終了状態を返します。
	 *
	 * @return 排他分岐の終了状態
	 */
	public JobWaitRuleInfoResponse.ExclusiveBranchEndStatusEnum getExclusiveBranchEndStatus() {
		return m_exclusiveBranchEndStatus;
	}

	/**
	 * 排他分岐の終了状態を設定します。
	 *
	 * @param m_exclusiveBranchEndStatus 排他分岐の終了状態
	 */
	public void setExclusiveBranchEndStatus( JobWaitRuleInfoResponse.ExclusiveBranchEndStatusEnum m_exclusiveBranchEndStatus) {
		this.m_exclusiveBranchEndStatus = m_exclusiveBranchEndStatus;
	}

	/**
	 * 排他分岐の終了値を返します。
	 *
	 * @return 排他分岐の終了値
	 */
	public Integer getExclusiveBranchEndValue() {
		return m_exclusiveBranchEndValue;
	}

	/**
	 *  排他分岐の終了値を設定します。
	 *
	 * @param m_exclusiveBranchEndValue  排他分岐の終了値
	 */
	public void setExclusiveBranchEndValue(Integer m_exclusiveBranchEndValue) {
		this.m_exclusiveBranchEndValue = m_exclusiveBranchEndValue;
	}

	/**
	 * 排他分岐の優先度リストを返します。
	 *
	 * @return 排他分岐の優先度リスト
	 */
	public List<JobNextJobOrderInfoResponse> getExclusiveBranchNextJobOrderList() {
		return m_exclusiveBranchNextJobOrderList;
	}

	/**
	 * 排他分岐の優先度リストを設定します。
	 *
	 * @param m_exclusiveBranchNextJobOrderList 排他分岐の優先度リスト
	 */
	public void setExclusiveBranchNextJobOrderList(List<JobNextJobOrderInfoResponse> m_exclusiveBranchNextJobOrderList) {
		this.m_exclusiveBranchNextJobOrderList = m_exclusiveBranchNextJobOrderList;
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
		setExclusiveBranch(m_exclusiveBranchComposite.isExclusiveBranchRtn());
		setExclusiveBranchEndStatus(m_exclusiveBranchComposite.getExclusiveBranchEndStatusRtn());
		setExclusiveBranchEndValue(m_exclusiveBranchComposite.getExclusiveBranchEndValueRtn());
		setExclusiveBranchNextJobOrderList(m_exclusiveBranchComposite.getExclusiveBranchNextJobOrderListRtn());
		
		return null;
	}
}