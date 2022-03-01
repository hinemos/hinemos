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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.JobInfoReferrerQueueResponse;
import org.openapitools.client.model.JobQueueReferrerViewInfoListItemResponse;
import org.openapitools.client.model.JobWaitRuleInfoResponse;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.jobmanagement.dialog.JobDialog;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.ViewUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ジョブ設定[同時実行制御ジョブ一覧]ビュークラスです。
 *
 * @since 6.2.0
 */
public class JobQueueReferrerView extends CommonViewPart {
	public static final String ID = JobQueueReferrerView.class.getName();

	private static final Log log = LogFactory.getLog(JobQueueReferrerView.class);

	// テーブル列インデックスと項目の対応
	private static final int COLUMN_JOBUNIT_ID = 1;
	private static final int COLUMN_JOB_ID = 2;

	// テーブルのソート基準
	private static final int SORT_COLUMN_INDEX1 = COLUMN_JOBUNIT_ID;
	private static final int SORT_COLUMN_INDEX2 = COLUMN_JOB_ID;
	private static final int SORT_ORDER = 1;

	// キュー情報ラベルの書式
	// (1:リテラル"キューID", 2:キューID, 3:リテラル"キュー名", 4:キュー名)
	private static final String QUEUE_LABEL_FORMAT = "%1$s:%2$s %3$s:%4$s";
	
	private Label queueLabel;
	private CommonTableViewer tableViewer;
	private Label statusLabel;

	private String managerName;
	private String queueId;
	private JobInfoReferrerQueueResponse viewInfo;

	public JobQueueReferrerView() {
		super();
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

		// キュー情報ラベル
		queueLabel = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "queueLabel", queueLabel);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		queueLabel.setLayoutData(gridData);

		// ジョブ一覧テーブル
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(parent, "table", table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		tableViewer = new CommonTableViewer(table);
		tableViewer.createTableColumn(createColumnsDefinition(), SORT_COLUMN_INDEX1, SORT_COLUMN_INDEX2, SORT_ORDER);
		tableViewer.setAllColumnsMovable();
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
	}
	
	private ArrayList<TableColumnInfo> createColumnsDefinition() {
		return new ArrayList<>(Arrays.asList(
				new TableColumnInfo(Messages.get("facility.manager"), TableColumnInfo.NONE, 100, SWT.LEFT),
				new TableColumnInfo(Messages.get("jobunit.id"), TableColumnInfo.NONE, 100, SWT.LEFT),
				new TableColumnInfo(Messages.get("job.id"), TableColumnInfo.NONE, 100, SWT.LEFT),
				new TableColumnInfo(Messages.get("job.name"), TableColumnInfo.NONE, 150, SWT.LEFT),
				new TableColumnInfo(Messages.get("type"), TableColumnInfo.JOB, 110, SWT.LEFT),
				new TableColumnInfo(Messages.get("facility.id"), TableColumnInfo.NONE, 100, SWT.LEFT),
				new TableColumnInfo(Messages.get("scope"), TableColumnInfo.FACILITY, 150, SWT.LEFT),
				new TableColumnInfo(Messages.get("wait.rule"), TableColumnInfo.WAIT_RULE, 80, SWT.LEFT),
				new TableColumnInfo(Messages.get("owner.role.id"), TableColumnInfo.NONE, 130, SWT.LEFT),
				new TableColumnInfo(Messages.get("creator.name"), TableColumnInfo.NONE, 80, SWT.LEFT),
				new TableColumnInfo(Messages.get("create.time"), TableColumnInfo.NONE, 140, SWT.LEFT),
				new TableColumnInfo(Messages.get("modifier.name"), TableColumnInfo.NONE, 80, SWT.LEFT),
				new TableColumnInfo(Messages.get("update.time"), TableColumnInfo.NONE, 140, SWT.LEFT)));
	}

	private IDoubleClickListener createTableDoubleClickListener() {
		return new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				if (selection == null) return;
				List<?> selectedRow = (List<?>) selection.getFirstElement();
				if (selectedRow == null) return;

				String jobunitId = (String) selectedRow.get(COLUMN_JOBUNIT_ID);
				String jobId = (String) selectedRow.get(COLUMN_JOB_ID);

				ViewUtil.executeWith(JobListView.class, view -> {
					view.setFocus(managerName, jobunitId, jobId);
				});
			}
		};
	}
	
	// ジョブダイアログを参照オンリーモードで表示する場合のリスナ。
	// スケジュール予定ビューと足並みをそろえるために未使用としたが、
	// 将来の仕様変更により復活の可能性もあるため残しておく。
	@SuppressWarnings("unused")
	private IDoubleClickListener createTableDoubleClickListener_showDialogVersion() {
		return new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				if (selection == null) return;
				List<?> selectedRow = (List<?>) selection.getFirstElement();
				if (selectedRow == null) return;

				// 選択されているジョブのJobInfoを取得してダイアログを表示する
				String selectedJobunitId = (String) selectedRow.get(COLUMN_JOBUNIT_ID);
				String selectedJobId = (String) selectedRow.get(COLUMN_JOB_ID);
				JobInfoWrapper selectedJobInfo = null;
				for (JobQueueReferrerViewInfoListItemResponse item : viewInfo.getItems()) {
					if (item.getJobunitId().equals(selectedJobunitId) && item.getJobId().equals(selectedJobId)) {
						selectedJobInfo = JobTreeItemUtil.getInfoFromDto(item.getJobInfoWithOwnerRoleId());
						break;
					}
				}
				if (selectedJobInfo == null) {
					// 対応するJobInfoが見つからないことは通常は考えられないが…
					log.info("DoubleClickListener: JobInfo not found. [" + selectedJobunitId + "," + selectedJobId
							+ "]");
					return;
				}

				// parentItemをnullに設定したJobTreeItemをダイアログに渡すと、編集不可モードとなる。
				JobTreeItemWrapper jobTreeItem = new JobTreeItemWrapper();
				jobTreeItem.setData(selectedJobInfo);

				// 本ビューでは参照ジョブは表示対象にならないため、JobTreeCompsiteは渡さなくて良いはず。
				JobDialog dialog = new JobDialog(getSite().getShell(), managerName, true);
				dialog.setJobTreeItem(jobTreeItem);
				dialog.open();
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

	/**
	 * 指定されたマネージャとジョブキューIDをもとに、表示を更新します。
	 * 
	 * @param managerName
	 * @param queueId
	 */
	public void update(String managerName, String queueId) {
		this.managerName = managerName;
		this.queueId = queueId;
		refresh();
	}

	/**
	 * 現在のマネージャとジョブキューの指定を保ったまま、表示を更新します。
	 */
	public void refresh() {
		viewInfo = null;
		if (managerName == null || queueId == null) return;

		// 一覧情報を取得
		ApiResultDialog errorDialog = new ApiResultDialog();
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			viewInfo = wrapper.getJobInfoReferrerQueue(queueId);
		} catch (Throwable t) {
			log.warn("refresh: " + t.getClass().getName() + ", " + t.getMessage());
			errorDialog.addFailure(managerName, t, "");
		}
		errorDialog.show(); // エラーメッセージ表示(エラーがあれば)

		// データを取得できなかった場合、各種表示はクリアする
		if (viewInfo == null) {
			queueLabel.setText("");
			tableViewer.setInput(new ArrayList<>());
			statusLabel.setText("");
			return;
		}

		// キュー情報ラベル設定
		queueLabel.setText(String.format(QUEUE_LABEL_FORMAT, Messages.get("jobqueue.id"), viewInfo.getQueueId(),
				Messages.get("jobqueue.name"), viewInfo.getQueueName()));

		// テーブル更新
		// - 表示内容の加工に関しては、JobListCompositeに合わせてある。
		// - ただし、オーナーロールIDのみ、サーバ側で再設定している。
		List<List<Object>> table = new ArrayList<>();
		for (JobQueueReferrerViewInfoListItemResponse item : viewInfo.getItems()) {
			JobInfoWrapper jobInfo = JobTreeItemUtil.getInfoFromDto(item.getJobInfoWithOwnerRoleId());

			List<Object> row = new ArrayList<>();
			row.add(managerName);
			row.add(jobInfo.getJobunitId());
			row.add(jobInfo.getId());
			row.add(jobInfo.getName());
			row.add(jobInfo.getType());

			switch (jobInfo.getType()) {
			case JOB:
				row.add(jobInfo.getCommand().getFacilityID());
				row.add(HinemosMessage.replace(jobInfo.getCommand().getScope()));
				break;
			case MONITORJOB:
				row.add(jobInfo.getMonitor().getFacilityID());
				row.add(HinemosMessage.replace(jobInfo.getMonitor().getScope()));
				break;
			case RESOURCEJOB:
				row.add(jobInfo.getResource().getResourceNotifyScope());
				row.add(HinemosMessage.replace(jobInfo.getResource().getResourceNotifyScopePath()));
				break;
			case RPAJOB:
				row.add(jobInfo.getRpa().getFacilityID());
				row.add(HinemosMessage.replace(jobInfo.getRpa().getScope()));
				break;
			default:
				row.add(null);
				row.add(null);
			}

			JobWaitRuleInfoResponse waitRule = jobInfo.getWaitRule();
			if (waitRule != null) {
				if (waitRule.getObjectGroup() != null && waitRule.getObjectGroup().size() > 0) {
					row.add(true);
				} else {
					row.add(false);
				}
			} else {
				row.add(false);
			}

			row.add(jobInfo.getOwnerRoleId());
			row.add(jobInfo.getCreateUser());
			row.add(jobInfo.getCreateTime() == null ? null : jobInfo.getCreateTime());
			row.add(jobInfo.getUpdateUser());
			row.add(jobInfo.getUpdateTime() == null ? null : jobInfo.getUpdateTime());

			table.add(row);
		}
		tableViewer.setInput(table);

		// ステータスバー更新
		Object[] args = { String.valueOf(table.size()) };
		statusLabel.setText(Messages.getString("records", args));
	}
}
