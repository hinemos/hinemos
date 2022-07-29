/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.JobCommandInfoResponse;
import org.openapitools.client.model.JobFileCheckInfoResponse;
import org.openapitools.client.model.JobLinkRcvInfoResponse;
import org.openapitools.client.model.JobMonitorInfoResponse;
import org.openapitools.client.model.JobResourceInfoResponse;
import org.openapitools.client.model.JobTreeItem;
import org.openapitools.client.model.JobRpaInfoResponse;
import org.openapitools.client.model.JobWaitRuleInfoResponse;

import com.clustercontrol.jobmanagement.action.GetJobTableDefine;
import com.clustercontrol.jobmanagement.composite.action.JobDoubleClickListener;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/* ジョブ[一覧]ビュー用のコンポジットクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobListComposite extends Composite {
	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** パス用ラベル */
	private Label m_path = null;
	/** ジョブツリーアイテム */
	private JobTreeItemWrapper m_jobTreeItem = null;
	/** 選択ジョブツリーアイテムリスト */
	private List<JobTreeItemWrapper> m_selectJobTreeItemList = new ArrayList<JobTreeItemWrapper>();

	/**
	 * コンストラクタ
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public JobListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		m_path = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "path", m_path);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_path.setLayoutData(gridData);

		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		m_viewer = new CommonTableViewer(table);
		m_viewer.createTableColumn(GetJobTableDefine.get(),
				GetJobTableDefine.SORT_COLUMN_INDEX1,
				GetJobTableDefine.SORT_COLUMN_INDEX2,
				GetJobTableDefine.SORT_ORDER);
		// 列移動が可能に設定
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}

		// ダブルクリックリスナの追加
		m_viewer.addDoubleClickListener(new JobDoubleClickListener(this));
	}

	/**
	 * このコンポジットが利用するテーブルビューアを返します。
	 *
	 * @return テーブルビューア
	 */
	public TableViewer getTableViewer() {
		return m_viewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return m_viewer.getTable();
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * 引数で指定されたジョブツリー情報からジョブ一覧情報を取得し、
	 * 共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたジョブツリー情報からジョブ一覧情報を取得します。</li>
	 * <li>共通テーブルビューアーにジョブ一覧情報をセットします。</li>
	 * </ol>
	 *
	 * @param item ジョブツリー情報
	 *
	 * @see com.clustercontrol.jobmanagement.action.GetJobList#getJobList(JobTreeItem)
	 */
	public void update(JobTreeItemWrapper item) {
		m_selectJobTreeItemList.clear();

		// Set path label
		m_path.setText(Messages.getString("job") + " : " + (( null != item ) ? JobTreeItemUtil.getPath(item) : ""));

		//ジョブ一覧情報取得
		m_viewer.setInput(getJobList(item));

		m_jobTreeItem = item;
	}

	/**
	 * 選択ジョブツリーアイテムリストを返します。
	 *
	 * @return ジョブツリーアイテムリスト
	 */
	public List<JobTreeItemWrapper> getSelectJobTreeItemList() {
		return m_selectJobTreeItemList;
	}

	/**
	 * 選択ジョブツリーアイテムリストを設定します。
	 *
	 * @param jobTreeItemList ジョブツリーアイテムリスト
	 */
	public void setSelectJobTreeItemList(List<JobTreeItemWrapper> jobTreeItemList) {
		m_selectJobTreeItemList = jobTreeItemList;
	}

	/**
	 * ジョブツリー情報を返します。
	 *
	 * @return ジョブツリー情報
	 */
	public JobTreeItemWrapper getJobTreeItem() {
		return m_jobTreeItem;
	}

	/**
	 * ジョブツリー情報を設定します。
	 *
	 * @param jobTreeItem ジョブツリー情報
	 */
	public void setJobTreeItem(JobTreeItemWrapper jobTreeItem) {
		m_jobTreeItem = jobTreeItem;
	}

	/**
	 * 引数にて指定されたジョブツリーアイテムからジョブ一覧情報を作成する
	 *
	 * @param item ジョブツリー情報
	 * @return ジョブ一覧情報（Objectの2次元配列）
	 */
	private ArrayList<?> getJobList(JobTreeItemWrapper item) {
		ArrayList<Object> jobList = new ArrayList<Object>();

		if (item == null) {
			return null;
		}

		if(item.getData().getType() == JobInfoWrapper.TypeEnum.COMPOSITE) {
			//トップ("ジョブ")を選択した場合
			for(JobTreeItemWrapper t : item.getChildren()) {
				ArrayList<Object> list = new ArrayList<Object>();
				JobInfoWrapper data = t.getData();
				list.add(data.getName());
				int size = GetJobTableDefine.get().size();
				for(int i = 0; i<size; i++) {
					list.add("");
				}
				jobList.add(list);
			}
			return jobList;
		} else {
			JobTreeItemWrapper managerTree = JobTreeItemUtil.getManager(item);
			String managerName = managerTree.getData().getName();
			List<JobTreeItemWrapper> items = item.getChildren();

			// FullJob
			List<JobInfoWrapper> list = new ArrayList<JobInfoWrapper>();
			for (JobTreeItemWrapper info : items) {
				list.add(info.getData());
			}
			JobPropertyUtil.setJobFullList(managerName, list);

			for (int i = 0; i < items.size(); i++) {
				ArrayList<Object> line = new ArrayList<Object>();
				line.add(managerName);
				line.add(items.get(i).getData().getId());
				line.add(items.get(i).getData().getName());
				line.add(items.get(i).getData().getType());

				if (items.get(i).getData().getType() == JobInfoWrapper.TypeEnum.JOB) {
					JobCommandInfoResponse info = items.get(i).getData().getCommand();
					line.add(info.getFacilityID());
					line.add(HinemosMessage.replace(info.getScope()));
				} else if (items.get(i).getData().getType() ==  JobInfoWrapper.TypeEnum.MONITORJOB) {
					JobMonitorInfoResponse info = items.get(i).getData().getMonitor();
					line.add(info.getFacilityID());
					line.add(HinemosMessage.replace(info.getScope()));
				} else if (items.get(i).getData().getType() ==  JobInfoWrapper.TypeEnum.FILECHECKJOB) {
					JobFileCheckInfoResponse info = items.get(i).getData().getJobFileCheck();
					line.add(info.getFacilityID());
					line.add(HinemosMessage.replace(info.getScope()));
				} else if (items.get(i).getData().getType() ==  JobInfoWrapper.TypeEnum.RESOURCEJOB) {
					JobResourceInfoResponse info = items.get(i).getData().getResource();
					line.add(info.getResourceNotifyScope());
					line.add(HinemosMessage.replace(info.getResourceNotifyScopePath()));
				} else if (items.get(i).getData().getType() ==  JobInfoWrapper.TypeEnum.JOBLINKRCVJOB) {
					JobLinkRcvInfoResponse info = items.get(i).getData().getJobLinkRcv();
					line.add(info.getFacilityID());
					line.add(HinemosMessage.replace(info.getScope()));
				} else if (items.get(i).getData().getType() ==  JobInfoWrapper.TypeEnum.RPAJOB) {
					JobRpaInfoResponse info = items.get(i).getData().getRpa();
					line.add(info.getFacilityID());
					line.add(HinemosMessage.replace(info.getScope()));
				} else {
					line.add(null);
					line.add(null);
				}
				JobWaitRuleInfoResponse waitRule = items.get(i).getData().getWaitRule();
				if (waitRule != null) {
					if (waitRule.getObjectGroup() != null
							&& waitRule.getObjectGroup().size() > 0) {
						line.add(true);
					} else {
						line.add(false);
					}
				} else {
					line.add(false);
				}
				line.add(items.get(i).getData().getOwnerRoleId());
				line.add(items.get(i).getData().getCreateUser());
//				Long createTime = JobTreeItemWrapper.convertDtStringtoLong(items.get(i).getData().getCreateTime());
//				line.add(createTime == null ? null:new Date(createTime));
				line.add(items.get(i).getData().getCreateTime());
				line.add(items.get(i).getData().getUpdateUser());
//				Long updateTime =  JobTreeItemWrapper.convertDtStringtoLong(items.get(i).getData().getUpdateTime());
//				line.add(updateTime == null ? null:new Date(updateTime));
				line.add(items.get(i).getData().getUpdateTime());
				line.add(null);
				jobList.add(line);
			}
		}
		return jobList;
	}

	/**
	 * @return 選択されているアイテムリストを返します。
	 */
	public List<JobTreeItemWrapper> getSelectItemList() {
		return this.m_selectJobTreeItemList;
	}
}
