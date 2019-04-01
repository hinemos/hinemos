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
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.jobmanagement.dialog.JobDialog;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.view.action.StartJobDetailAction;
import com.clustercontrol.jobmanagement.view.action.StopJobDetailAction;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.LogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.ViewUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.jobmanagement.JobDetailInfo;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobQueueContentsViewInfo;
import com.clustercontrol.ws.jobmanagement.JobQueueContentsViewInfoListItem;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブ履歴[同時実行制御状況]ビュークラスです。
 *
 * @since 6.2.0
 */
public class JobQueueContentsView extends CommonViewPart {
	public static final String ID = JobQueueContentsView.class.getName();

	private static final Log log = LogFactory.getLog(JobQueueContentsView.class);

	// テーブル列インデックスと項目の対応
	private static final int COLUMN_SESSION_ID = 1;
	private static final int COLUMN_JOBUNIT_ID = 4;
	private static final int COLUMN_JOB_ID = 2;
	private static final int COLUMN_REG_DATE = 8;

	// テーブルのソート基準
	private static final int SORT_COLUMN_INDEX1 = COLUMN_REG_DATE;
	private static final int SORT_COLUMN_INDEX2 = COLUMN_REG_DATE;
	private static final int SORT_ORDER = 1;

	// キュー情報ラベルの書式
	//   1:リテラル"キューID"(String)
	//   2:キューID(String)
	//   3:リテラル"キュー名"(String)
	//   4:キュー名(String)
	private static final String QUEUE_LABEL_FORMAT = "%1$s:%2$s %3$s:%4$s";
	
	// ステータス情報ラベルの書式
	//   1:リテラル"同時実行可能数"(String)
	//   2:同時実行可能数(int)
	//   3:リテラル"現在の同時実行数"(String)
	//   4:現在の同時実行数(int)
	//   5:リテラル"現在のキュー待機数"(String)
	//   6:現在のキュー待機数(int)
	private static final String STATUS_LABEL_FORMAT = "%1$s:%2$d %3$s:%4$d %5$s:%6$d";
	
	private Label queueLabel;
	private CommonTableViewer tableViewer;
	private Label statusLabel;

	private String managerName;
	private String queueId;
	private JobQueueContentsViewInfo viewInfo;
	
	private int selectedCount;

	private String selectedSessionId;
	private String selectedJobunitId;
	private String selectedJobId;

	public JobQueueContentsView() {
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

		// ジョブ詳細一覧テーブル
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
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
	}
	
	private ArrayList<TableColumnInfo> createColumnsDefinition() {
		return new ArrayList<>(Arrays.asList(
				new TableColumnInfo(Messages.get("run.status"), TableColumnInfo.STATE, 140, SWT.LEFT),
				new TableColumnInfo(Messages.get("session.id"), TableColumnInfo.NONE, 140, SWT.LEFT),
				new TableColumnInfo(Messages.get("job.id"), TableColumnInfo.NONE, 100, SWT.LEFT),
				new TableColumnInfo(Messages.get("job.name"), TableColumnInfo.NONE, 150, SWT.LEFT),
				new TableColumnInfo(Messages.get("jobunit.id"), TableColumnInfo.NONE, 100, SWT.LEFT),
				new TableColumnInfo(Messages.get("type"), TableColumnInfo.JOB, 110, SWT.LEFT),
				new TableColumnInfo(Messages.get("facility.id"), TableColumnInfo.NONE, 100, SWT.LEFT),
				new TableColumnInfo(Messages.get("scope"), TableColumnInfo.FACILITY, 150, SWT.LEFT),
				new TableColumnInfo(Messages.get("jobqueue.reg_time"), TableColumnInfo.NONE, 140, SWT.LEFT)));
	}

	private ISelectionChangedListener createTableSelectionChangedListener() {
		return new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// 選択対象の情報を退避
				selectedCount = 0;
				selectedSessionId = null;
				selectedJobunitId = null;
				selectedJobId = null;

				StructuredSelection selection = (StructuredSelection) event.getSelection();
				if (selection != null) {
					List<?> selectedRow = (List<?>) selection.getFirstElement();
					if (selectedRow != null) {
						selectedCount = selection.size();
						selectedSessionId = (String) selectedRow.get(COLUMN_SESSION_ID);
						selectedJobunitId = (String) selectedRow.get(COLUMN_JOBUNIT_ID);
						selectedJobId = (String) selectedRow.get(COLUMN_JOB_ID);
					}
				}
				log.debug("SelectionChangedListener: Selected. " + getSelectedIdString());
				
				// アクションの使用可/不可を設定
				refreshCommands(StartJobDetailAction.ID, StopJobDetailAction.ID);

				// ノード詳細・ファイル転送ビューを更新
				ViewUtil.executeWith(JobNodeDetailView.class, view -> {
					view.update(managerName, selectedSessionId, selectedJobunitId, selectedJobId);
				});
				ViewUtil.executeWith(ForwardFileView.class, view -> {
					view.update(managerName, selectedSessionId, selectedJobunitId, selectedJobId);
				});
			}
		};
	}
	
	private IDoubleClickListener createTableDoubleClickListener() {
		return new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				// 選択されているジョブのJobTreeItemを取得してダイアログを表示する
				JobQueueContentsViewInfoListItem selectedItem = null;
				for (JobQueueContentsViewInfoListItem item : viewInfo.getItems()) {
					JobInfo jobInfo = item.getJobTreeItem().getData();
					if (item.getSessionId().equals(selectedSessionId)
							&& jobInfo.getJobunitId().equals(selectedJobunitId)
							&& jobInfo.getId().equals(selectedJobId)) {
						selectedItem = item;
						break;
					}
				}
				if (selectedItem == null) {
					// 対応する情報が見つからないことは通常は考えられないが…
					log.info("DoubleClickListener: JobTreeItem not found. " + getSelectedIdString());
					return;
				}
				
				// viewInfoが持っているJobTreeItemは、ジョブダイアログを表示するには情報不足であるため、
				// 改めて詳しい情報を取得する。
				ApiResultDialog errorDialog = new ApiResultDialog();
				JobTreeItem fullInfo = null;
				try {
					JobEndpointWrapper ep = JobEndpointWrapper.getWrapper(managerName);
					fullInfo = ep.getSessionJobInfo(selectedItem.getSessionId(),
							selectedItem.getJobTreeItem().getData().getJobunitId(),
							selectedItem.getJobTreeItem().getData().getId());
				} catch (Throwable t) {
					log.warn(LogUtil.filterWebFault("DoubleClickListener: ", t));
					errorDialog.addFailure(managerName, t, "");
				}
				errorDialog.show(); // エラーメッセージ表示(エラーがあれば)

				if (fullInfo == null) return;

				// 本ビューでは参照ジョブは表示対象にならないため、JobTreeCompsiteは渡さなくて良いはず。
				JobDialog dialog = new JobDialog(getSite().getShell(), managerName, true);
				dialog.setJobTreeItem(fullInfo);
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
			JobEndpointWrapper ep = JobEndpointWrapper.getWrapper(managerName);
			viewInfo = ep.getJobQueueContentsViewInfo(queueId);
		} catch (Throwable t) {
			log.warn(LogUtil.filterWebFault("refresh: ", t));
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
		// - JobTableTreeLabelProviderを参考にした。
		List<List<Object>> table = new ArrayList<>();
		for (JobQueueContentsViewInfoListItem item : viewInfo.getItems()) {
			JobInfo job = item.getJobTreeItem().getData();
			JobDetailInfo detail = item.getJobTreeItem().getDetail();

			List<Object> row = new ArrayList<>();
			row.add(detail.getStatus());
			row.add(item.getSessionId());
			row.add(job.getId());
			row.add(job.getName());
			row.add(job.getJobunitId());
			row.add(job.getType());
			row.add(detail.getFacilityId());
			row.add(HinemosMessage.replace(detail.getScope()));
			row.add(item.getRegDate() == null ? null : new Date(item.getRegDate()));
			table.add(row);
		}
		tableViewer.setInput(table);

		// ステータスバー更新
		statusLabel.setText(String.format(STATUS_LABEL_FORMAT,
				Messages.get("jobqueue.concurrency"), unboxSafely(viewInfo.getConcurrency(), 0),
				Messages.get("jobqueue.current.count"), unboxSafely(viewInfo.getActiveCount(), 0),
				Messages.get("jobqueue.current.waiting_count"),
				unboxSafely(viewInfo.getCount(), 0) - unboxSafely(viewInfo.getActiveCount(), 0)));
	}

	/**
	 * このビューで表示しているキューのマネージャ名を返します。
	 */
	public String getManagerName() {
		return managerName;
	}

	/**
	 * このビュー上で選択状態にあるジョブ詳細の件数を返します。
	 * (複数選択不可のため、0または1を返します。)
	 */
	public int getSelectedCount() {
		return selectedCount;
	}

	/**
	 * このビュー上で選択状態にあるジョブ詳細のセッションIDを返します。
	 */
	public String getSelectedSessionId() {
		return selectedSessionId;
	}

	/**
	 * このビュー上で選択状態にあるジョブ詳細のジョブユニットIDを返します。
	 */
	public String getSelectedJobunitId() {
		return selectedJobunitId;
	}

	/**
	 * このビュー上で選択状態にあるジョブ詳細のジョブIDを返します。
	 */
	public String getSelectedJobId() {
		return selectedJobId;
	}

	// こういう汎用ユーティリティは一箇所にまとめるべきだが、全体に影響するので控える。
	private int unboxSafely(Integer i, int alt) {
		if (i == null) return alt;
		return i.intValue();
	}
	
	private String join(CharSequence delimiter, Object... objectsToString) {
		StringBuilder sb = new StringBuilder();
		for (Object o : objectsToString) {
			if (sb.length() > 0) sb.append(delimiter);
			sb.append(o);
		}
		return sb.toString();
	}
	
	private String getSelectedIdString() {
		return join(",", selectedSessionId, selectedJobunitId, selectedJobId);
	}
}
