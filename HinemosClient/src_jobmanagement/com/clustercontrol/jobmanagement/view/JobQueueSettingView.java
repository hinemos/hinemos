/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.GetJobQueueListSearchRequest;
import org.openapitools.client.model.JobQueueResponse;
import org.openapitools.client.model.JobQueueSettingViewInfoListItemResponse;
import org.openapitools.client.model.JobQueueSettingViewInfoResponse;

import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.jobmanagement.bean.JobQueueSettingViewFilter;
import com.clustercontrol.jobmanagement.util.JobQueueFilterBeanUtil;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.view.action.CopyJobQueueAction;
import com.clustercontrol.jobmanagement.view.action.DeleteJobQueueAction;
import com.clustercontrol.jobmanagement.view.action.JobQueueEditor;
import com.clustercontrol.jobmanagement.view.action.ModifyJobQueueAction;
import com.clustercontrol.jobmanagement.view.action.ObjectPrivilegeJobQueueAction;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.ViewUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;
import com.clustercontrol.viewer.CommonTableViewer;


/**
 * ジョブ設定[同時実行制御]ビュークラスです。
 *
 * @since 6.2.0
 */
public class JobQueueSettingView extends CommonViewPart implements ObjectPrivilegeTargetListView, JobQueueEditor {
	public static final String ID = JobQueueSettingView.class.getName();

	private static final Log log = LogFactory.getLog(JobQueueSettingView.class);

	// テーブル列インデックスと項目の対応
	private static final int COLUMN_MANAGER_NAME = 0;
	private static final int COLUMN_QUEUE_ID = 1;
	private static final int COLUMN_OWNER_ROLE_ID = 4;

	// テーブルのソート基準
	private static final int SORT_COLUMN_INDEX1 = COLUMN_MANAGER_NAME;
	private static final int SORT_COLUMN_INDEX2 = COLUMN_QUEUE_ID;
	private static final int SORT_ORDER = 1;

	private CommonTableViewer tableViewer;
	private Label statusLabel;

	private boolean filtering;
	private String managerFilter;
	private JobQueueSettingViewFilter queueFilter;

	public JobQueueSettingView() {
		super();
		clearFilter();
	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		// 全体レイアウト
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// ジョブキューテーブル
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(parent, "table", table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		tableViewer = new CommonTableViewer(table);
		tableViewer.createTableColumn(createColumnsDefinition(), SORT_COLUMN_INDEX1, SORT_COLUMN_INDEX2, SORT_ORDER);
		tableViewer.setAllColumnsMovable();
		tableViewer.addSelectionChangedListener(createTableSelectionChangedListener());
		tableViewer.addDoubleClickListener(createTableDoubleClickListener());

		// ステータス表示
		statusLabel = new Label(parent, SWT.RIGHT);
		WidgetTestUtil.setTestId(parent, "statusLabel", statusLabel);
		statusLabel.setText("");

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.statusLabel.setLayoutData(gridData);

		createContextMenu();
		update();
	}

	private ArrayList<TableColumnInfo> createColumnsDefinition() {
		return new ArrayList<>(Arrays.asList(
				new TableColumnInfo(Messages.getString("facility.manager"), TableColumnInfo.NONE, 100, SWT.LEFT),
				new TableColumnInfo(Messages.getString("jobqueue.id"), TableColumnInfo.NONE, 120, SWT.LEFT),
				new TableColumnInfo(Messages.getString("jobqueue.name"), TableColumnInfo.NONE, 150, SWT.LEFT),
				new TableColumnInfo(Messages.getString("jobqueue.concurrency"), TableColumnInfo.NONE, 100, SWT.LEFT),
				new TableColumnInfo(Messages.getString("owner.role.id"), TableColumnInfo.NONE, 130, SWT.LEFT),
				new TableColumnInfo(Messages.getString("creator.name"), TableColumnInfo.NONE, 100, SWT.LEFT),
				new TableColumnInfo(Messages.getString("create.time"), TableColumnInfo.NONE, 140, SWT.LEFT),
				new TableColumnInfo(Messages.getString("modifier.name"), TableColumnInfo.NONE, 100, SWT.LEFT),
				new TableColumnInfo(Messages.getString("update.time"), TableColumnInfo.NONE, 140, SWT.LEFT)));
	}

	private ISelectionChangedListener createTableSelectionChangedListener() {
		return new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// アクションの使用可/不可を設定
				refreshCommands(ModifyJobQueueAction.ID, DeleteJobQueueAction.ID, CopyJobQueueAction.ID,
						ObjectPrivilegeJobQueueAction.ID);

				// "選択されたジョブキューを参照しているジョブ"の一覧を更新
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				if (selection == null) return;
				List<?> selectedRow = (List<?>) selection.getFirstElement();
				if (selectedRow == null) return;
				
				ViewUtil.executeWith(JobQueueReferrerView.class, view -> {
					String managerName = (String) selectedRow.get(COLUMN_MANAGER_NAME);
					String queueId = (String) selectedRow.get(COLUMN_QUEUE_ID);
					view.update(managerName, queueId);
				});
			}
		};
	}

	private IDoubleClickListener createTableDoubleClickListener() {
		return new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (!event.getSelection().isEmpty()) {
					// 変更コマンドを実行
					executeCommand(ModifyJobQueueAction.ID);
				}
			}
		};
	}

	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(tableViewer.getTable());
		WidgetTestUtil.setTestId(this, null, menu);
		tableViewer.getTable().setMenu(menu);
		getSite().registerContextMenu(menuManager, tableViewer);
	}

	@Override
	public void update() {
		try {
			// 一覧情報を取得
			Map<String, JobQueueSettingViewInfoResponse> dispDataMap = new HashMap<>();
			ApiResultDialog errorDialog = new ApiResultDialog();

			Collection<String> managerNames;
			Collection<String> activemanagerNames = RestConnectManager.getActiveManagerSet();
			if (!activemanagerNames.contains(managerFilter)) {
				managerFilter = "";
			}
			if (!filtering || StringUtils.isEmpty(managerFilter)) {
				managerNames = activemanagerNames;
			} else {
				managerNames = Arrays.asList(managerFilter);
			}

			for (String managerName : managerNames) {
				try {
					JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
					GetJobQueueListSearchRequest request = JobQueueFilterBeanUtil.convertToRequest(filtering ? queueFilter : new JobQueueSettingViewFilter());
					JobQueueSettingViewInfoResponse jobQueueSettingViewInfoResponse = wrapper.getJobQueueListSearch(request);
					dispDataMap.put(managerName, jobQueueSettingViewInfoResponse);
				} catch (Throwable t) {
					log.warn("update: " + t.getClass().getName() + ", " + t.getMessage());
					errorDialog.addFailure(managerName, t, "");
				}
			}

			// エラーメッセージ表示(エラーがあれば)
			errorDialog.show();

			// テーブル更新
			List<List<Object>> table = new ArrayList<>();
			for (Entry<String, JobQueueSettingViewInfoResponse> entry : dispDataMap.entrySet()) {
				JobQueueSettingViewInfoResponse list = entry.getValue();
				for (JobQueueSettingViewInfoListItemResponse it : list.getItems()) {
					List<Object> row = new ArrayList<>();
					row.add(entry.getKey());
					row.add(it.getQueueId());
					row.add(it.getName());
					row.add(it.getConcurrency());
					row.add(it.getOwnerRoleId());
					row.add(it.getRegUser());
					row.add(it.getRegDate());
					row.add(it.getUpdateUser());
					row.add(it.getUpdateDate());
					table.add(row);
				}
			}
			tableViewer.setInput(table);

			// ステータスバー更新
			String status;
			Object[] args = { String.valueOf(table.size()) };
			if (filtering) {
				status = Messages.getString("filtered.records", args);
			} else {
				status = Messages.getString("records", args);
			}
			statusLabel.setText(status);
		} catch (RuntimeException e) {
			log.warn("update(), " + e.getMessage(), e);
		}
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object elem : selection.toList()) {
			List<?> row = (List<?>) elem;
			objectBeans.add(new ObjectBean(row.get(COLUMN_MANAGER_NAME).toString(), HinemosModuleConstant.JOB_QUEUE,
					row.get(COLUMN_QUEUE_ID).toString()));
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		List<?> row = (List<?>) selection.getFirstElement();
		if (row == null)
			return null;
		return row.get(COLUMN_OWNER_ROLE_ID).toString();
	}

	@Override
	public JobQueueEditTarget getJobQueueEditTarget() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		List<?> row = (List<?>) selection.getFirstElement();
		if (row == null)
			return JobQueueEditTarget.empty;
		return new JobQueueEditTarget(row.get(COLUMN_MANAGER_NAME).toString(), row.get(COLUMN_QUEUE_ID).toString());
	}

	@Override
	public List<JobQueueEditTarget> getJobQueueEditTargets() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		List<JobQueueEditTarget> targets = new ArrayList<>();
		for (Object elem : selection.toList()) {
			List<?> row = (List<?>) elem;
			targets.add(new JobQueueEditTarget(row.get(COLUMN_MANAGER_NAME).toString(),
					row.get(COLUMN_QUEUE_ID).toString()));
		}
		return targets;
	}

	@Override
	public int getSelectedJobQueueCount() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		return selection.size();
	}

	@Override
	public void onJobQueueEdited() {
		update();
	}

	public void clearFilter() {
		filtering = false;
		managerFilter = "";
		queueFilter = new JobQueueSettingViewFilter();
	}
	
	public void disableFilter() {
		filtering = false;
	}

	public void enableFilter(String managerName, JobQueueSettingViewFilter queueFilter) {
		this.managerFilter = managerName;
		this.queueFilter = queueFilter;
		filtering = true;
	}
	
	public String getManagerFilter() {
		return managerFilter;
	}
	
	public JobQueueSettingViewFilter getQueueFilter() {
		return queueFilter;
	}
}
