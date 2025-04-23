/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.JobObjectGroupInfoResponse;
import org.openapitools.client.model.JobObjectInfoResponse;

import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.jobmanagement.action.GetWaitRuleTableDefine;
import com.clustercontrol.jobmanagement.dialog.WaitRuleDialog;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableNotSortViewer;

/**
 * 対象条件一覧テーブルコンポジットクラス
 *
 */
public class JobWaitTableComposite extends Composite {
	private Table m_table = null;
	/** テーブルビューア */
	private CommonTableNotSortViewer m_viewer = null;
	/** 追加用ボタン */
	private Button m_createButton = null;
	/** 変更用ボタン */
	private Button m_modifyButton = null;
	/** コピー用ボタン */
	private Button m_copyButton = null;
	/** 削除用ボタン */
	private Button m_deleteButton = null;
	/** シェル */
	private Shell m_shell = null;
	/** 選択アイテム */
	private ArrayList<Object> m_selectItem = null;
	/** ジョブ情報 */
	private JobTreeItemWrapper m_jobTreeItem = null;
	/** ジョブ待ち条件情報 */
	private List<JobObjectGroupInfoResponse> m_objectGroupList = null;

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(JobWaitTableComposite.class);

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 * @param jobType
	 *            ジョブタイプ
	 */
	public JobWaitTableComposite(Composite parent, int style, JobInfoWrapper.TypeEnum jobType) {
		super(parent, style);
		if(jobType == null){
			throw new InternalError("JobType is null");
		}
		if (parent instanceof WaitRuleComposite) {
			this.initJobDialog(parent, jobType);
		} else {
			this.initWaitRuleListDialog(parent, jobType);
		}
		m_shell = parent.getShell();
	}

	/**
	 * ジョブ設定パースペクティブ用のコンポジットを構築します。
	 * 
	 * @param jobType
	 *            ジョブタイプ
	 */
	private void initJobDialog(Composite parent, JobInfoWrapper.TypeEnum jobType) {

		this.setLayout(JobDialogUtil.getParentLayout());

		// 判定対象一覧（ラベル）
		Label tableTitle = new Label(parent, SWT.NONE);
		tableTitle.setText(Messages.getString("object.list"));

		// 判定対象一覧（テーブル）
		createWaitTable(parent, jobType);

		// ボタン（Composite）
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new RowLayout());

		// dummy
		new Label(buttonComposite, SWT.NONE).setLayoutData(
				new RowData(120 + JobWaitTableComposite.getTableWidth(jobType), SizeConstant.SIZE_LABEL_HEIGHT));
		// ボタン：追加（ボタン）
		createButton(buttonComposite);
		// ボタン：変更（ボタン）
		modifyButton(buttonComposite);
		// ボタン：コピー（ボタン）
		copyButton(buttonComposite);
		// ボタン：削除（ボタン）
		deleteButton(buttonComposite);

		// ボタン制御
		m_modifyButton.setEnabled(false);
		m_copyButton.setEnabled(false);
		m_deleteButton.setEnabled(false);
	}

	/**
	 * @param buttonComposite
	 */
	private void createButton(Composite buttonComposite) {
		this.m_createButton = new Button(buttonComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_createCondition", this.m_createButton);
		this.m_createButton.setText(Messages.getString("add"));
		this.m_createButton.setLayoutData(new RowData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_createButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_log.debug("widgetSelected");
				WaitRuleDialog dialog = new WaitRuleDialog(m_shell, m_jobTreeItem);
				if (dialog.open() == IDialogConstants.OK_ID) {
					JobObjectGroupInfoResponse groupInfo = dialog.getInputData();
					if (groupInfo != null) {
						Integer maxOrderNo = getMaxOrderNo(m_objectGroupList);
						groupInfo.setOrderNo(maxOrderNo + 1);
						m_objectGroupList.add(groupInfo);
						reflectObjectGroup();
					}
				}
			}
		});
	}

	/**
	 * @param buttonComposite
	 */
	private void modifyButton(Composite buttonComposite) {
		this.m_modifyButton = new Button(buttonComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_modifyCondition", this.m_modifyButton);
		this.m_modifyButton.setText(Messages.getString("modify"));
		this.m_modifyButton.setLayoutData(new RowData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_modifyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_selectItem != null) {
					Integer orderNo = (Integer) m_selectItem.get(GetWaitRuleTableDefine.ORDER_NO);
					Integer orderNoSub = (Integer) m_selectItem.get(GetWaitRuleTableDefine.ORDER_NO_SUB);
					if (orderNoSub == 0) {
						WaitRuleDialog dialog = new WaitRuleDialog(m_shell, m_jobTreeItem);
						dialog.setInputData(m_objectGroupList.get(orderNo));
						if (dialog.open() == IDialogConstants.OK_ID) {
							JobObjectGroupInfoResponse groupInfo = dialog.getInputData();
							if (groupInfo == null) {
								m_objectGroupList.remove((int) orderNo);
							} else {
								JobObjectGroupInfoResponse orgInfo = m_objectGroupList.get(orderNo);
								if(orgInfo == null){
									//通常ここには来ない想定
									groupInfo.setOrderNo(orderNo);
								}else{
									//OrderNoが引き継がれるようにフォロー
									groupInfo.setOrderNo(orgInfo.getOrderNo());
								}
								m_objectGroupList.set(orderNo, groupInfo);
							}
							reflectObjectGroup();
							m_selectItem = null;
						}
					}
				}
			}
		});
	}

	/**
	 * @param buttonComposite
	 */
	private void copyButton(Composite buttonComposite) {
		this.m_copyButton = new Button(buttonComposite, SWT.NONE);
		this.m_copyButton.setText(Messages.getString("copy"));
		this.m_copyButton.setLayoutData(new RowData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_copyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_selectItem != null) {
					Integer orderNo = (Integer) m_selectItem.get(GetWaitRuleTableDefine.ORDER_NO);
					Integer orderNoSub = (Integer) m_selectItem.get(GetWaitRuleTableDefine.ORDER_NO_SUB);
					if (orderNoSub == 0) {
						WaitRuleDialog dialog = new WaitRuleDialog(m_shell, m_jobTreeItem);
						dialog.setInputData(m_objectGroupList.get(orderNo));
						if (dialog.open() == IDialogConstants.OK_ID) {
							JobObjectGroupInfoResponse groupInfo = dialog.getInputData();
							Integer maxOrderNo = getMaxOrderNo(m_objectGroupList);
							groupInfo.setOrderNo(maxOrderNo + 1);
							m_objectGroupList.add(groupInfo);
							reflectObjectGroup();
							m_selectItem = null;
						}
					}
				}
			}
		});
	}

	/**
	 * @param buttonComposite
	 */
	private void deleteButton(Composite buttonComposite) {
		this.m_deleteButton = new Button(buttonComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_deleteCondition", this.m_deleteButton);
		this.m_deleteButton.setText(Messages.getString("delete"));
		this.m_deleteButton.setLayoutData(new RowData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_selectItem != null) {
					Integer orderNo = (Integer) m_selectItem.get(GetWaitRuleTableDefine.ORDER_NO);
					Integer orderNoSub = (Integer) m_selectItem.get(GetWaitRuleTableDefine.ORDER_NO_SUB);
					if (orderNoSub == 0) {
						m_objectGroupList.remove((int) orderNo);
						reflectObjectGroup();
						m_selectItem = null;
					}
				}
			}
		});
	}

	public void setCreateButtonEnabled(boolean enabled) {
		m_createButton.setEnabled(enabled);
	}

	public void setModifyButtonEnabled(boolean enabled) {
		m_modifyButton.setEnabled(enabled);
	}

	public void setCopyButtonEnabled(boolean enabled) {
		m_copyButton.setEnabled(enabled);
	}

	public void setDeleteButtonEnabled(boolean enabled) {
		m_deleteButton.setEnabled(enabled);
	}

	/**
	 * ジョブマップエディタパースペクティブ用のコンポジットを構築します。
	 * 
	 * @param jobType
	 *            ジョブタイプ
	 */
	private void initWaitRuleListDialog(Composite parent, JobInfoWrapper.TypeEnum jobType) {

		this.setLayout(JobDialogUtil.getParentLayout());
		// 判定対象一覧（ラベル）
		Label tableTitle = new Label(parent, SWT.NONE);
		tableTitle.setText(Messages.getString("wait.rule.list.select"));
		// 判定対象一覧（テーブル）
		createWaitTable(parent, jobType);
	}

	private void createWaitTable(Composite composite, JobInfoWrapper.TypeEnum jobType) {
		m_table = new Table(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		WidgetTestUtil.setTestId(composite, "table", m_table);
		m_table.setHeaderVisible(true);
		m_table.setLinesVisible(true);
		m_table.setLayoutData(new RowData(430 + getTableWidth(jobType), 150));
		m_viewer = new CommonTableNotSortViewer(m_table);
		m_viewer.createTableColumn(GetWaitRuleTableDefine.get(), GetWaitRuleTableDefine.SORT_COLUMN_INDEX1,
				GetWaitRuleTableDefine.SORT_COLUMN_INDEX2, GetWaitRuleTableDefine.SORT_ORDER);

		// 先頭2列はキー情報のためサイズ変更不可
		m_viewer.getTable().getColumn(GetWaitRuleTableDefine.ORDER_NO).setResizable(false);
		m_viewer.getTable().getColumn(GetWaitRuleTableDefine.ORDER_NO_SUB).setResizable(false);
	}

	private static int getTableWidth(JobInfoWrapper.TypeEnum jobType) {
		int addTableWidth = 0;
		if (jobType == JobInfoWrapper.TypeEnum.JOBNET) {
			// ジョブネット
			addTableWidth = 0;
		} else if (jobType == JobInfoWrapper.TypeEnum.JOB) {
			// コマンドジョブ
			addTableWidth = 170;
		} else if (jobType == JobInfoWrapper.TypeEnum.FILEJOB) {
			// ファイルジョブ
			addTableWidth = 170;
		} else if (jobType == JobInfoWrapper.TypeEnum.REFERJOB || jobType == JobInfoWrapper.TypeEnum.REFERJOBNET) {
			// 参照ジョブもしくは参照ジョブネット
			addTableWidth = 0;
		} else if (jobType == JobInfoWrapper.TypeEnum.APPROVALJOB) {
			// 承認ジョブ
			addTableWidth = 170;
		} else if (jobType == JobInfoWrapper.TypeEnum.MONITORJOB) {
			// 監視ジョブ
			addTableWidth = 170;
		} else if (jobType == JobInfoWrapper.TypeEnum.FILECHECKJOB) {
			// ファイルチェックジョブ
			addTableWidth = 170;
		} else if (jobType == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB) {
			// ジョブ連携送信ジョブ
			addTableWidth = 170;
		} else if (jobType == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB) {
			// ジョブ連携待機ジョブ
			addTableWidth = 170;
		} else if (jobType == JobInfoWrapper.TypeEnum.RESOURCEJOB) {
			// リソース制御ジョブ
			addTableWidth = 170;
		}
		return addTableWidth;
	}

	/**
	 * 待ち条件群をテーブルビューに反映する
	 */
	public void reflectObjectGroup() {

		ArrayList<Object> tableData = new ArrayList<Object>();
		for (int i = 0; i < m_objectGroupList.size(); i++) {

			JobObjectGroupInfoResponse groupInfo = m_objectGroupList.get(i);
			if (groupInfo == null) {
				continue;
			}

			ArrayList<Object> tableLineData = new ArrayList<Object>();
			boolean isGroup = groupInfo.getJobObjectList().size() != 1;
			if (isGroup) {
				// 待ち条件群
				tableLineData.add(i);
				tableLineData.add(0);
				tableLineData.add(getGroupConditionMessage(groupInfo.getConditionType()));
				tableLineData.add("");
				tableLineData.add("");
				tableLineData.add("");
				tableLineData.add("");
				tableLineData.add("");
				tableLineData.add("");
				tableLineData.add("");
				tableData.add(tableLineData);
			}

			// 待ち条件
			List<JobObjectInfoResponse> list = groupInfo.getJobObjectList();
			if (list == null) {
				continue;
			}
			m_log.debug("reflectWaitRuleInfo_JobObjectInfo.size() = " + list.size());
			for (int j = 0; j < list.size(); j++) {
				JobObjectInfoResponse info = list.get(j);
				tableLineData = new ArrayList<Object>();
				// 順番
				tableLineData.add(i);
				if (isGroup) {
					// 待ち条件群の場合親が0(待ち条件群情報)子は1のインクリメント
					tableLineData.add(j + 1);
				} else {
					// 待ち条件のみの場合、単体なので0
					tableLineData.add(0);
				}

				// 種別
				tableLineData.add(info.getType());

				switch (info.getType()) {
				case JOB_END_STATUS:
					tableLineData.add(info.getJobId());
					tableLineData.add(EndStatusMessage
							.typeEnumValueToString(((JobObjectInfoResponse.StatusEnum) info.getStatus()).getValue()));
					tableLineData.add("");
					tableLineData.add("");
					tableLineData.add("");
					tableLineData.add("");
					tableLineData.add(info.getDescription());
					tableData.add(tableLineData);
					break;

				case JOB_END_VALUE:
					tableLineData.add(info.getJobId());
					tableLineData.add(info.getValue());
					tableLineData.add("");
					tableLineData.add(info.getDecisionCondition());
					tableLineData.add("");
					tableLineData.add("");
					tableLineData.add(info.getDescription());
					tableData.add(tableLineData);
					break;

				case TIME:
					tableLineData.add("");
					tableLineData.add(new Date(JobTreeItemUtil.convertTimeStringtoLong(info.getTime())));
					tableLineData.add("");
					tableLineData.add("");
					tableLineData.add("");
					tableLineData.add("");
					tableLineData.add(info.getDescription());
					tableData.add(tableLineData);
					break;

				case START_MINUTE:
					tableLineData.add("");
					tableLineData.add(info.getStartMinute());
					tableLineData.add("");
					tableLineData.add("");
					tableLineData.add("");
					tableLineData.add("");
					tableLineData.add(info.getDescription());
					tableData.add(tableLineData);
					break;

				case JOB_PARAMETER:
					tableLineData.add("");
					tableLineData.add("");
					tableLineData.add(info.getDecisionValue());
					tableLineData.add(info.getDecisionCondition());
					tableLineData.add(info.getValue());
					tableLineData.add("");
					tableLineData.add(info.getDescription());
					tableData.add(tableLineData);
					break;

				case CROSS_SESSION_JOB_END_STATUS:
					tableLineData.add(info.getJobId());
					tableLineData.add(EndStatusMessage
							.typeEnumValueToString(((JobObjectInfoResponse.StatusEnum) info.getStatus()).getValue()));
					tableLineData.add("");
					tableLineData.add("");
					tableLineData.add("");
					tableLineData.add(info.getCrossSessionRange());
					tableLineData.add(info.getDescription());
					tableData.add(tableLineData);
					break;

				case CROSS_SESSION_JOB_END_VALUE:
					tableLineData.add(info.getJobId());
					tableLineData.add(info.getValue());
					tableLineData.add("");
					tableLineData.add(info.getDecisionCondition());
					tableLineData.add("");
					tableLineData.add(info.getCrossSessionRange());
					tableLineData.add(info.getDescription());
					tableData.add(tableLineData);
					break;

				case JOB_RETURN_VALUE:
					tableLineData.add(info.getJobId());
					tableLineData.add(info.getValue());
					tableLineData.add("");
					tableLineData.add(info.getDecisionCondition());
					tableLineData.add("");
					tableLineData.add("");
					tableLineData.add(info.getDescription());
					tableData.add(tableLineData);
					break;

				}
			}
		}
		m_log.debug("reflectWaitRuleInfo_tableData.size() = " + tableData.size());
		m_viewer.setInput(tableData);
	}

	/**
	 * 選択アイテムをを返します。
	 *
	 * @return 選択アイテム
	 */
	public ArrayList<Object> getSelectItem() {
		return m_selectItem;
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
	 * ジョブ情報を設定します。
	 *
	 * @param jobTreeItem
	 *            ジョブ情報
	 */
	public void setJobTreeItem(JobTreeItemWrapper jobTreeItem) {
		m_jobTreeItem = jobTreeItem;
	}

	/**
	 * 判定対象(ジョブ待ち条件群のリスト)を返します。
	 *
	 * @return ジョブ待ち条件群のリスト
	 */
	public List<JobObjectGroupInfoResponse> getObjectGroupList() {
		return m_objectGroupList;
	}

	/**
	 * 判定対象(ジョブ待ち条件群のリスト)を設定します。
	 *
	 * @param groupList
	 *            ジョブ待ち条件群のリスト
	 */
	public void setObjectGroupList(List<JobObjectGroupInfoResponse> groupList) {
		m_objectGroupList = groupList;
	}

	/**
	 * テーブルビューアを返します。
	 *
	 * @return テーブルビューア
	 */
	public CommonTableNotSortViewer getTableViewer() {
		return m_viewer;
	}

	/**
	 * 条件関係に対応した文言を取得する
	 * 
	 * @param conditionType
	 *            条件関係
	 * @return 対応した文言
	 */
	private static String getGroupConditionMessage(JobObjectGroupInfoResponse.ConditionTypeEnum conditionType) {
		if (conditionType == JobObjectGroupInfoResponse.ConditionTypeEnum.AND) {
			return Messages.getString("job.wait.group.and");
		} else if (conditionType == JobObjectGroupInfoResponse.ConditionTypeEnum.OR) {
			return Messages.getString("job.wait.group.or");
		} else {
			return "";
		}
	}

	/**
	 * List<JobObjectGroupInfoResponse>からOrderNoの最大値を取得
	 * 
	 * @param targetList 対象リスト
	 * @return OrderNoの最大値(対象リストが空なら-1となる)
	 */
	private static Integer getMaxOrderNo(List<JobObjectGroupInfoResponse> targetList){
		Integer ret = -1;
		try{
			// 対象リストが空なら-1を返す
			if (targetList == null || targetList.size() == 0) {
				return ret;
			}
	
			//OrderNoが設定されていないレコードを取り除く
			List<JobObjectGroupInfoResponse> filterList = targetList.stream().filter(c -> c.getOrderNo() != null)
					.collect(Collectors.toList());
	
			// OrderNoが設定されたレコードが無い場合も-1を返す
			if (filterList == null || filterList.size() == 0) {
				return ret;
			}
		
			// OrderNoの最大値を取得する
			ret = filterList.stream().max(Comparator.comparingInt(JobObjectGroupInfoResponse::getOrderNo)).get()
					.getOrderNo();
		}catch(Exception e){
			//ここには来ない想定
			m_log.error("getMaxOrderNo . exception has occurred .message=" + e.getMessage(), e);
		}
		return ret;
	}
}
