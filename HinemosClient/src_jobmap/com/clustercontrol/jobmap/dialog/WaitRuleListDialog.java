/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmap.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.JobObjectGroupInfoResponse;
import org.openapitools.client.model.JobObjectInfoResponse;
import org.openapitools.client.model.JobWaitRuleInfoResponse;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetWaitRuleTableDefine;
import com.clustercontrol.jobmanagement.composite.JobWaitTableComposite;
import com.clustercontrol.jobmanagement.dialog.WaitRuleDialog;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.util.JobWaitRuleUtil;
import com.clustercontrol.util.Messages;

/**
 * ジョブマップ用の待ち条件の追加先を選択するための待ち条件一覧ダイアログクラス
 */
public class WaitRuleListDialog extends CommonDialog {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(WaitRuleListDialog.class);
	/** 対象条件一覧テーブルコンポジット */
	private JobWaitTableComposite m_jobWaitTable = null;
	/** ダイアログのサイズの初期値 */
	private final int sizeY = 300;
	/** シェル */
	private Shell m_shell = null;
	/** 選択アイテム */
	private ArrayList<Object> m_selectItem = null;
	/** 先行ジョブ */
	private JobTreeItemWrapper m_firstItem = null;
	/** 後続ジョブ */
	private JobTreeItemWrapper m_secondItem = null;

	/**
	 * @param parent
	 */
	public WaitRuleListDialog(Shell parent, JobTreeItemWrapper firstItem, JobTreeItemWrapper secondItem) {
		super(parent);
		m_firstItem = firstItem;
		m_secondItem = secondItem;
	}

	@Override
	protected void customizeDialog(Composite parent) {
		m_log.debug("customizeDialog");

		parent.setLayout(JobDialogUtil.getParentLayout());
		m_shell = this.getShell();
		Button selectButton = this.getButton(IDialogConstants.OK_ID);

		// ダイアログタイトル
		m_shell.setText(Messages.getString("wait.rule.list"));

		JobWaitRuleInfoResponse jobWaitRuleInfo = m_secondItem.getData().getWaitRule();

		JobInfoWrapper info = m_secondItem.getData();
		JobInfoWrapper.TypeEnum jobType = info.getType();

		// 待ち条件一覧（テーブル）
		this.m_jobWaitTable = new JobWaitTableComposite(parent, SWT.NONE, jobType);

		m_jobWaitTable.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
					// 選択行を取得
					@SuppressWarnings("unchecked")
					ArrayList<Object> info = (ArrayList<Object>) ((StructuredSelection) event.getSelection())
							.getFirstElement();
					setSelectItem(info);
					if (m_selectItem != null && (Integer) m_selectItem.get(GetWaitRuleTableDefine.ORDER_NO_SUB) == 0) {
						selectButton.setEnabled(true);
						return;
					} else {
						selectButton.setEnabled(false);
						return;
					}
				}
			}
		});

		// 画面中央に
		Display display = m_shell.getDisplay();
		m_shell.setLocation((display.getBounds().width - m_shell.getSize().x) / 2,
				(display.getBounds().height - m_shell.getSize().y) / 2);

		// ダイアログのサイズ調整（pack:resize to be its preferred size）
		m_shell.pack();
		m_shell.setSize(new Point(m_shell.getSize().x, sizeY));

		m_jobWaitTable.setObjectGroupList(jobWaitRuleInfo.getObjectGroup());
		m_jobWaitTable.reflectObjectGroup();
	}

	/**
	 * OK ボタンの表示テキスト設定
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("select");
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
	 * OK ボタン押下<BR>
	 */
	@Override
	protected void okPressed() {
		ValidateResult result = null;

		// 選択項目のnullチェック
		if (m_selectItem == null) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("wait.rule.list.select"));
			displayError(result);
			return;
		} else {
			Integer orderNo = (Integer) m_selectItem.get(GetWaitRuleTableDefine.ORDER_NO);
			Integer orderNoSub = (Integer) m_selectItem.get(GetWaitRuleTableDefine.ORDER_NO_SUB);
			if (orderNoSub == 0) {
				List<JobObjectGroupInfoResponse> objectGroupList = m_jobWaitTable.getObjectGroupList();
				JobObjectGroupInfoResponse objectGroupInfo = objectGroupList.get(orderNo);
				List<JobObjectInfoResponse> clone = new ArrayList<JobObjectInfoResponse>(
						objectGroupInfo.getJobObjectList());
				objectGroupInfo.addJobObjectListItem(getFirstDefWaitInfo());

				WaitRuleDialog dialog = new WaitRuleDialog(m_shell, m_secondItem);
				dialog.setInputData(objectGroupInfo);
				if (dialog.open() == IDialogConstants.OK_ID) {
					JobObjectGroupInfoResponse groupInfo = dialog.getInputData();
					objectGroupList.set(orderNo, groupInfo);
					result = JobWaitRuleUtil.validateWaitGroup(objectGroupList);
					if (result != null) {
						objectGroupList.get(orderNo).setJobObjectList(clone);
						displayError(result);
						return;
					}
					super.okPressed();
				} else {
					objectGroupList.get(orderNo).setJobObjectList(clone);
				}
			}
		}
	}

	/**
	 * 選択アイテムを設定します。
	 *
	 * @param selectItem
	 *            選択アイテム
	 */
	public void setSelectItem(ArrayList<Object> selectItem) {
		m_selectItem = selectItem;
	}

	/**
	 * 追加する待ち条件の初期値を作成します。
	 *
	 */
	public JobObjectInfoResponse getFirstDefWaitInfo() {
		JobObjectInfoResponse objectInfo = new JobObjectInfoResponse();
		objectInfo.setType(JobObjectInfoResponse.TypeEnum.JOB_END_STATUS);
		objectInfo.setJobId(m_firstItem.getData().getId());
		objectInfo.setStatus(JobObjectInfoResponse.StatusEnum.NORMAL);
		objectInfo.setDescription("");
		return objectInfo;
	}

	/**
	 * ジョブ待ち条件群情報を返します。
	 *
	 * @return 判定対象情報
	 */
	public List<JobObjectGroupInfoResponse> getListData() {
		return m_jobWaitTable.getObjectGroupList();
	}
}
