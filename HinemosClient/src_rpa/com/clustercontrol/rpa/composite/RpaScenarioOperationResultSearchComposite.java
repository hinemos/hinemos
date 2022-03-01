/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.RpaScenarioTagResponse;
import org.openapitools.client.model.SearchRpaScenarioOperationResultDataResponse;
import org.openapitools.client.model.SearchRpaScenarioOperationResultRequest;
import org.openapitools.client.model.SearchRpaScenarioOperationResultRequest.StatusListEnum;
import org.openapitools.client.model.SearchRpaScenarioOperationResultResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.RpaScenarioEndStatusMessage;
import com.clustercontrol.hub.preference.HubPreferencePage;
import com.clustercontrol.hub.util.TableViewerSorter;
import com.clustercontrol.rpa.action.DownloadRpaScenarioOperationResultData;
import com.clustercontrol.rpa.commons.RpaScenarioOperationResultSearchPeriodConstants;
import com.clustercontrol.rpa.dialog.RpaScenarioOperationResultDetailDialog;
import com.clustercontrol.rpa.dialog.RpaScenarioOperationResultDownloadDialog;
import com.clustercontrol.rpa.dialog.RpaScenarioTagListDialog;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.rpa.util.RpaScenarioTagUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.viewer.CommonTableViewerSorter;

/**
 * シナリオ実績検索画面クラス<br/>
 */
public class RpaScenarioOperationResultSearchComposite extends Composite {

	/**
	 * 検索結果:一覧ビュー カラム定義
	 */
	public enum ViewColumn {
		start_date(Messages.getString("view.rpa.scenario.operation.result.search.column.date.start"), new ColumnPixelData(150, true, true),
			new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					SearchRpaScenarioOperationResultDataResponse operationResult = (SearchRpaScenarioOperationResultDataResponse) element;
					Date date=new Date(operationResult.getStartDate());
					SimpleDateFormat df = TimezoneUtil.getSimpleDateFormat();
					return df.format(date);
				}
			}), 
		end_date(Messages.getString("view.rpa.scenario.operation.result.search.column.date.end"), new ColumnPixelData(150, true, true),
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						SearchRpaScenarioOperationResultDataResponse operationResult = (SearchRpaScenarioOperationResultDataResponse) element;
						if (operationResult.getEndDate() != null){
							Date date=new Date(operationResult.getEndDate());
							SimpleDateFormat df = TimezoneUtil.getSimpleDateFormat();
							return df.format(date);
						}
						return "";
					}
				}),
		scenario_id(Messages.getString("view.rpa.scenario.operation.result.search.column.scenario.id"),new ColumnPixelData(150, true, true),
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						SearchRpaScenarioOperationResultDataResponse operationResult = (SearchRpaScenarioOperationResultDataResponse) element;
						return operationResult.getScenarioId();
					}
				}),
		scenario_name(Messages.getString("view.rpa.scenario.operation.result.search.column.scenario.name"),new ColumnPixelData(150, true, true),
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						SearchRpaScenarioOperationResultDataResponse operationResult = (SearchRpaScenarioOperationResultDataResponse) element;
						return operationResult.getScenarioName();
					}
				}),
		facility_id(Messages.getString("view.rpa.scenario.operation.result.search.column.facility.id"),new ColumnPixelData(150, true, true),
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						SearchRpaScenarioOperationResultDataResponse operationResult = (SearchRpaScenarioOperationResultDataResponse) element;
						return operationResult.getFacilityId();
					}
				}),
		facility_name(Messages.getString("view.rpa.scenario.operation.result.search.column.facility.name"),new ColumnPixelData(150, true, true),
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						SearchRpaScenarioOperationResultDataResponse operationResult = (SearchRpaScenarioOperationResultDataResponse) element;
						return operationResult.getFacilityName();
					}
				}),
		runtime(Messages.getString("view.rpa.scenario.operation.result.search.column.runtime"),new ColumnPixelData(70, true, true),
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						SearchRpaScenarioOperationResultDataResponse operationResult = (SearchRpaScenarioOperationResultDataResponse) element;
						if (operationResult.getRunTime() != null){
							return convertTimeToHMS(operationResult.getRunTime());
						}
						return "";
					}
				}),
		status(Messages.getString("view.rpa.scenario.operation.result.search.column.status"),new ColumnPixelData(70, true, true),
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						SearchRpaScenarioOperationResultDataResponse operationResult = (SearchRpaScenarioOperationResultDataResponse) element;
						if (operationResult.getStatus() != null){
							return RpaScenarioEndStatusMessage.typeEnumValueToString(operationResult.getStatus().getValue());
						}
						return "";
					}
				}),
		step(Messages.getString("view.rpa.scenario.operation.result.search.column.step"),new ColumnPixelData(70, true, true),
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						SearchRpaScenarioOperationResultDataResponse operationResult = (SearchRpaScenarioOperationResultDataResponse) element;
						if (operationResult.getStep() != null){
							return operationResult.getStep().toString();
						}
						return "";
					}
				});

		private String label;
		private transient  ColumnLabelProvider provider;
		private transient  ColumnPixelData pixelData;
		
		ViewColumn(String label, ColumnPixelData pixelData, ColumnLabelProvider provider) {
			this.label = label;
			this.pixelData = pixelData;
			this.provider = provider;
		}

		public String getLabel() {
			return label;
		}

		public ColumnPixelData getPixelData() {
			return pixelData;
		}

		public ColumnLabelProvider getProvider() {
			return provider;
		}
		
	}

	private Shell shell = null;
	private String managerName;
	private String facilityId;
	
	private int dataCount;
	private int dispOffset;
	private int dispSize;
	
	private Long from;
	private Long to;
	private Combo cmbPeriod;

	private Text txtScenarioId;
	private String scenarioId;
	
	private Text txtTagId;
	private List<RpaScenarioTagResponse> tagList;
	
	private List<StatusListEnum> statusList;
	
	private Button btnPerv;
	private Button btnNext;
	private Button btnTop;
	private Button btnEnd;
	
	private RpaScenarioOperationResultSearchDateTimeComposite dateTimeCompositeFrom;
	private RpaScenarioOperationResultSearchDateTimeComposite dateTimeCompositeTo;
	private Label lbltotal;
	
	private Button btnNormalEnd;
	private Button btnErrorEnd;
	private Button btnNormalRunning;
	private Button btnErrorRunning;
	private Button btnUnknown;
	private Button btnDownload;
	
	private SearchRpaScenarioOperationResultRequest lastExecQuery;

	/** 検索結果の出力範囲 */
	private Label lblPage;
	private TableViewer tableViewer;
	private Table table;

	/**
	 * コンストラクタ.
	 */
	public RpaScenarioOperationResultSearchComposite(Composite parent, int style) {
		super(parent, style);
		this.shell = this.getShell();
		initialize();
	}
	
	/**
	 * 入力項目等の初期化.
	 */
	private void initialize() {
		setLayout(new GridLayout(1, false));
		
		// 検索UI
		Composite searchComposite = new Composite(this, SWT.NONE);
		searchComposite.setLayout(new GridLayout(1, false));

		// 検索UI - 実行日時
		Composite compositePeriod = new Composite(searchComposite, SWT.NONE);
		GridLayout gl_groupPeriod = new GridLayout(12, false);
		gl_groupPeriod.verticalSpacing = 0;
		compositePeriod.setLayout(gl_groupPeriod);
		GridData gd_groupPeriod = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_groupPeriod.widthHint = 50;
		compositePeriod.setLayoutData(gd_groupPeriod);
		
		Label lblPeriod = new Label(compositePeriod, SWT.NONE);
		GridData gd_lblPeriod = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblPeriod.widthHint = 98;
		lblPeriod.setLayoutData(gd_lblPeriod);
		lblPeriod.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.date.run"));
		
		cmbPeriod = new Combo(compositePeriod, SWT.READ_ONLY);
		GridData gd_cmbPeriod = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_cmbPeriod.widthHint = 111;
		cmbPeriod.setLayoutData(gd_cmbPeriod);
		cmbPeriod.setText(RpaScenarioOperationResultSearchPeriodConstants.ALL);
		cmbPeriod.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo select = (Combo) e.getSource();
				List<Calendar> list = RpaScenarioOperationResultSearchPeriodConstants.getPeriod(select.getText());
				if (list == null) {
					cmbPeriod.setText(RpaScenarioOperationResultSearchPeriodConstants.ALL);
				} else {
					dateTimeCompositeFrom.setPeriod(list.get(0));
					dateTimeCompositeTo.setPeriod(list.get(1));
				}
				if (RpaScenarioOperationResultSearchPeriodConstants.ALL.equals(select.getText())){
					enableDateTime(false);
				}else{
					enableDateTime(true);
				}
				update();
			}
		});
		for (String str : RpaScenarioOperationResultSearchPeriodConstants.getPeriodStrList()) {
			cmbPeriod.add(str);
		}
		cmbPeriod.select(0);

		Label label = new Label(compositePeriod, SWT.NONE);
		GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_label.widthHint = 10;
		label.setLayoutData(gd_label);

		dateTimeCompositeFrom = new RpaScenarioOperationResultSearchDateTimeComposite(compositePeriod, SWT.NONE);
		GridData gd_dateTimeComposite = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_dateTimeComposite.widthHint = 250;
		dateTimeCompositeFrom.setLayoutData(gd_dateTimeComposite);
		GridLayout gridLayout = (GridLayout) dateTimeCompositeFrom.getLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;

		Label lblFromTo = new Label(compositePeriod, SWT.NONE);
		lblFromTo.setAlignment(SWT.CENTER);
		GridData gd_lblFromTo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblFromTo.widthHint = 39;
		lblFromTo.setLayoutData(gd_lblFromTo);
		lblFromTo.setText(Messages.getString("wave"));

		dateTimeCompositeTo = new RpaScenarioOperationResultSearchDateTimeComposite(compositePeriod, SWT.NONE);
		gd_dateTimeComposite = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_dateTimeComposite.widthHint = 250;
		dateTimeCompositeTo.setLayoutData(gd_dateTimeComposite);
		gridLayout = (GridLayout) dateTimeCompositeTo.getLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		enableDateTime(false);
		
		// 検索UI - シナリオID
		Composite compositeScenarioId = new Composite(searchComposite, SWT.NONE);
		GridLayout gl_compositeScenarioId = new GridLayout(4, false);
		gl_compositeScenarioId.marginHeight = 0;
		compositeScenarioId.setLayout(gl_compositeScenarioId);
		GridData gd_compositeScenarioId = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeScenarioId.setLayoutData(gd_compositeScenarioId);
		
		Label lblScenarioId = new Label(compositeScenarioId, SWT.NONE);
		GridData gd_lblScenarioId = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblScenarioId.widthHint = 98;
		lblScenarioId.setLayoutData(gd_lblScenarioId);
		lblScenarioId.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.scenario.id"));

		txtScenarioId = new Text(compositeScenarioId, SWT.BORDER);
		GridData gd_txtScenarioId = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtScenarioId.widthHint = 500;
		txtScenarioId.setLayoutData(gd_txtScenarioId);
		
		// 検索UI - タグID
		Composite compositeTagId = new Composite(searchComposite, SWT.NONE);
		GridLayout gl_compositeTagId = new GridLayout(4, false);
		gl_compositeTagId.marginHeight = 0;
		compositeTagId.setLayout(gl_compositeTagId);
		GridData gd_compositeTagId = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeTagId.setLayoutData(gd_compositeTagId);
		
		Label lblTagId = new Label(compositeTagId, SWT.NONE);
		GridData gd_lblTagId = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblTagId.widthHint = 98;
		lblTagId.setLayoutData(gd_lblTagId);
		lblTagId.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.tag"));
		if(tagList == null){
			tagList = new ArrayList<>();
		}

		txtTagId = new Text(compositeTagId, SWT.BORDER);
		GridData gd_txtTagId = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtTagId.widthHint = 500;
		txtTagId.setLayoutData(gd_txtTagId);
		txtTagId.setEnabled(false);
		
		Button btnGetTagSimply = new Button(compositeTagId, SWT.NONE);
		GridData gd_btnGetTagSimply = new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1);
		gd_btnGetTagSimply.widthHint = 82;
		btnGetTagSimply.setLayoutData(gd_btnGetTagSimply);
		btnGetTagSimply.setSize(300, 30);
		btnGetTagSimply.setText(Messages.getString("select"));
		btnGetTagSimply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				// ダイアログ表示及び終了処理
				RpaScenarioTagListDialog dialog = new RpaScenarioTagListDialog(shell, managerName);
				if (txtTagId.getText() != null || "".equals(txtTagId.getText())) {
					dialog.setSelectTag(tagList);
				}
				dialog.open();
				
				// ダイアログからデータを取得してタグテキストを設定する
				setTag(dialog.getSelectTag(),dialog.getTagNameMap(),dialog.getTagPathMap());

				// コンポジットを更新する
				update();
			}
		});
		
		// 検索UI - ステータス
		Composite compositeStatus = new Composite(searchComposite, SWT.NONE);
		GridLayout gl_compositeStatus = new GridLayout(8, false);
		gl_compositeStatus.verticalSpacing = 1;
		compositeStatus.setLayout(gl_compositeStatus);
		compositeStatus.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1));
		
		Label lblStatus = new Label(compositeStatus, SWT.NONE);
		GridData gd_lblStatus = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblStatus.widthHint = 98;
		lblStatus.setLayoutData(gd_lblStatus);
		lblStatus.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.status"));
		
		btnNormalEnd = new Button(compositeStatus, SWT.CHECK);
		btnNormalEnd.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnNormalEnd.setText(Messages.getString("view.rpa.scenario.operation.result.search.button.normal.end"));
		btnNormalEnd.setSelection(true);

		btnErrorEnd = new Button(compositeStatus, SWT.CHECK);
		btnErrorEnd.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnErrorEnd.setText(Messages.getString("view.rpa.scenario.operation.result.search.button.error.end"));
		btnErrorEnd.setSelection(true);
		
		btnNormalRunning = new Button(compositeStatus, SWT.CHECK);
		btnNormalRunning.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnNormalRunning.setText(Messages.getString("view.rpa.scenario.operation.result.search.button.normal.running"));
		btnNormalRunning.setSelection(true);
		
		btnErrorRunning = new Button(compositeStatus, SWT.CHECK);
		btnErrorRunning.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnErrorRunning.setText(Messages.getString("view.rpa.scenario.operation.result.search.button.error.running"));
		btnErrorRunning.setSelection(true);
		
		btnUnknown = new Button(compositeStatus, SWT.CHECK);
		btnUnknown.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnUnknown.setText(Messages.getString("view.rpa.scenario.operation.result.search.button.unknown"));
		btnUnknown.setSelection(true);
		
		// 検索UI - 検索ボタン
		Button btnSearchSimply = new Button(compositeStatus, SWT.NONE);
		GridData gd_btnSearchSimply = new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1);
		gd_btnSearchSimply.widthHint = 82;
		btnSearchSimply.setLayoutData(gd_btnSearchSimply);
		btnSearchSimply.setSize(300, 30);
		btnSearchSimply.setText(Messages.getString("search"));
		btnSearchSimply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SearchRpaScenarioOperationResultRequest query = null;
				query = makeNewQuery();
				if (query == null){
					return;
				}
				SearchRpaScenarioOperationResultResponse resultList = getQueryExecute(managerName, query);
				updateResultComposite(query.getNeedCount(), resultList);
			}
		});

		// 検索UI - ダウンロードボタン
		btnDownload = new Button(compositeStatus, SWT.NONE);
		GridData gd_btnDownload = new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1);
		gd_btnDownload.widthHint = 82;
		btnDownload.setLayoutData(gd_btnDownload);
		btnDownload.setText(Messages.getString("view.rpa.scenario.operation.result.search.download"));
		btnDownload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RpaScenarioOperationResultDownloadDialog dialog = new RpaScenarioOperationResultDownloadDialog(shell);
				
				if (dialog.open() == IDialogConstants.OK_ID) {
					// ダウンロード処理
					DownloadRpaScenarioOperationResultData downloadData = new DownloadRpaScenarioOperationResultData();
					dialog.getDownloadRecordsRequest().setSearchRpaScenarioOperationResultRequest(lastExecQuery);
					try {
						downloadData.executeRecords(shell, managerName, dialog.getDownloadRecordsRequest());
					} catch (UnknownHostException e1) {
					}
				}
			}
		});
		btnDownload.setEnabled(false);

		// 検索結果UI
		Composite resultComposite = new Composite(this, SWT.NONE);
		resultComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		resultComposite.setLayout(new GridLayout(2, false));
		TabFolder tabFolder = new TabFolder(resultComposite, SWT.NONE);
		GridData gd_tabFolder = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_tabFolder.heightHint = 676;
		tabFolder.setLayoutData(gd_tabFolder);

		// Not working Start(WindowBuilder)
		TabItem tbtmTable = new TabItem(tabFolder, SWT.NONE);
		tbtmTable.setText(Messages.getString("view.rpa.scenario.operation.result.search.tab.list"));

		Composite compositeTable = new Composite(tabFolder, SWT.NONE);
		tbtmTable.setControl(compositeTable);
		compositeTable.setLayout(new GridLayout(1, false));

		TableColumnLayout tcl_composite_1 = new TableColumnLayout();
		compositeTable.setLayout(tcl_composite_1);

		tableViewer = new TableViewer(compositeTable, SWT.FULL_SELECTION | SWT.MULTI);
		ColumnViewerToolTipSupport.enableFor(tableViewer);
		// ダブルクリックにて選択レコードの詳細をダイアログ表示.
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				// 選択アイテムを取得する
				SearchRpaScenarioOperationResultDataResponse selectResultData = 
						(SearchRpaScenarioOperationResultDataResponse) ((StructuredSelection) event.getSelection()).getFirstElement();
				RpaScenarioOperationResultDetailDialog dialog = 
						new RpaScenarioOperationResultDetailDialog(shell, managerName, selectResultData.getResultId());
				dialog.open();
			}
		});
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		for (final ViewColumn column : ViewColumn.values()) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			TableColumn tableColumn = tableViewerColumn.getColumn();
			tcl_composite_1.setColumnData(tableColumn, column.getPixelData());
			tableColumn.setText(column.getLabel());
			tableViewerColumn.setLabelProvider(column.getProvider());
			tableColumn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					tableViewer.setSorter(new TableViewerSorter(tableViewer, column.getProvider()));
				}
			});
		}
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
			}
		});
		tableViewer.setSorter(new CommonTableViewerSorter(ViewColumn.start_date.ordinal()));

		Composite compositePageNumber = new Composite(resultComposite, SWT.NONE);
		GridLayout gl_compositePageNumber = new GridLayout(6, false);
		gl_compositePageNumber.marginHeight = 0;
		compositePageNumber.setLayout(gl_compositePageNumber);
		GridData gd_pagerComposite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		compositePageNumber.setLayoutData(gd_pagerComposite);

		lbltotal = new Label(compositePageNumber, SWT.RIGHT);
		GridData gd_lbltotal = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		lbltotal.setLayoutData(gd_lbltotal);
		lbltotal.setText(Messages.getString("view.rpa.scenario.operation.result.search.page.number", new Object[] { "0", "0", "0", "0.0"}));

		btnTop = new Button(compositePageNumber, SWT.NONE);
		btnTop.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnTop.setText("<<");
		btnTop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SearchRpaScenarioOperationResultRequest query = null;
				query = makeContinuousQuery(0, getDataCountPerPage(getTable()));
				Boolean isNeedCount = true;
				SearchRpaScenarioOperationResultResponse result = getQueryExecute(managerName, query);
				updateResultComposite(isNeedCount, result);
			}
		});
		btnTop.setEnabled(false);

		btnPerv = new Button(compositePageNumber, SWT.NONE);
		btnPerv.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnPerv.setText("<");
		btnPerv.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int offset = Math.max(0,dispOffset-getDataCountPerPage(getTable()));
				SearchRpaScenarioOperationResultRequest query = null;
				query = makeContinuousQuery(offset, getDataCountPerPage(getTable()));
				Boolean isNeedCount = true;
				SearchRpaScenarioOperationResultResponse result = getQueryExecute(managerName, query);
				updateResultComposite(isNeedCount, result);
			}
		});
		btnPerv.setEnabled(false);

		Composite composite = new Composite(compositePageNumber, SWT.NONE);
		GridData gd_composite = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_composite.widthHint = 133;
		composite.setLayoutData(gd_composite);
		GridData gd_compositePageNumber = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_compositePageNumber.widthHint = 76;
		composite.setLayoutData(gd_compositePageNumber);
		composite.setLayout(new GridLayout(1, false));
		lblPage = new Label(composite, SWT.CENTER);
		lblPage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblPage.setText("0 / 0");

		btnNext = new Button(compositePageNumber, SWT.NONE);
		btnNext.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnNext.setText(">");
		btnNext.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int offset = dispOffset+dispSize;
				SearchRpaScenarioOperationResultRequest query = null;
				query = makeContinuousQuery(offset, getDataCountPerPage(getTable()));
				Boolean isNeedCount = true;
				SearchRpaScenarioOperationResultResponse result = getQueryExecute(managerName, query);
				// 追加データがなかった
				if (0 == result.getSize()){
					return;
				}
				updateResultComposite(isNeedCount, result);
			}
		});
		btnNext.setEnabled(false);

		btnEnd = new Button(compositePageNumber, SWT.NONE);
		btnEnd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnEnd.setText(">>");
		btnEnd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int dataCountPerPage = getDataCountPerPage(getTable());
				int pageCount = dataCount / dataCountPerPage;
				pageCount = (dataCount % dataCountPerPage) ==0 ? pageCount-1:pageCount;
				int offset = pageCount * dataCountPerPage;
				SearchRpaScenarioOperationResultRequest query = null;
				query = makeContinuousQuery(offset, getDataCountPerPage(getTable()));
				Boolean isNeedCount = true;
				SearchRpaScenarioOperationResultResponse result = getQueryExecute(managerName, query);
				// 追加データがなかった
				if (0 == result.getSize()){
					return;
				}
				updateResultComposite(isNeedCount, result);
			}
		});
		btnEnd.setEnabled(false);
	}

	private void enableDateTime(boolean enable){
		dateTimeCompositeFrom.setEnabled(enable);
		dateTimeCompositeTo.setEnabled(enable);
	}

	/**
	 * 検索条件の作成.
	 */
	private SearchRpaScenarioOperationResultRequest makeNewQuery() {
		// 実行日時をセット
		if (RpaScenarioOperationResultSearchPeriodConstants.ALL.equals(cmbPeriod.getText())){
			from = null;
			to = null;
		}else{
			from = dateTimeCompositeFrom.getEpochMillis();
			to = dateTimeCompositeTo.getEpochMillis();
		}
		
		// シナリオIDをセット
		scenarioId = txtScenarioId.getText();
		
		// タグIDをセット
		List<String> tagIdList = new ArrayList<>();
		for (RpaScenarioTagResponse tag : tagList){
			tagIdList.add(tag.getTagId());
		}
		
		// ステータスをセット
		statusList = new ArrayList<>();
		if (btnNormalEnd.getSelection()){
			statusList.add(StatusListEnum.NORMAL_END);
		}
		if (btnErrorEnd.getSelection()){
			statusList.add(StatusListEnum.ERROR_END);
		}
		if (btnNormalRunning.getSelection()){
			statusList.add(StatusListEnum.NORMAL_RUNNING);
		}
		if (btnErrorRunning.getSelection()){
			statusList.add(StatusListEnum.ERROR_RUNNING);
		}
		if (btnUnknown.getSelection()){
			statusList.add(StatusListEnum.UNKNOWN);
		}
		
		if (statusList.isEmpty()){
			Map<String, String> errorMsgs = new HashMap<>();
			errorMsgs.put(Messages.getString("rpa.scenario.operation.result.search.condition"), 
					Messages.getString("message.rpa.scenario.operation.result.status.nochecked"));
			UIManager.showMessageBox(errorMsgs, true);
			
			return null;
		}
		
		return makeQuery(
				from,
				to,
				scenarioId,
				tagIdList,
				statusList,
				0,
				getDataCountPerPage(getTable()),
				true
				);
	}

	/**
	 * 表示範囲指定して検索条件生成.
	 */
	private SearchRpaScenarioOperationResultRequest makeContinuousQuery(int dispOffset, int dispSize) {
		// タグIDをセット
		List<String> tagIdList = new ArrayList<>();
		for (RpaScenarioTagResponse tag : tagList){
			tagIdList.add(tag.getTagId());
		}
		
		return makeQuery(
				from,
				to,
				scenarioId,
				tagIdList,
				statusList,
				dispOffset,
				dispSize,
				true
				);
	}

	/**
	 * クエリの生成.
	 */
	private SearchRpaScenarioOperationResultRequest makeQuery
			(Long from, Long to, String scenarioId, List<String> tagList, List<StatusListEnum> statusList, int dispOffset, int dispSize, boolean firstQuery){
		SearchRpaScenarioOperationResultRequest query = new SearchRpaScenarioOperationResultRequest();
		query.setStartDateFrom(from);
		query.setStartDateTo(to);
		query.setScenarioId(scenarioId);
		query.setTagIdList(tagList);
		query.setStatusList(statusList);
		query.setOffset(dispOffset);
		query.setSize(dispSize);
		query.setNeedCount(firstQuery);
		query.setFacilityId(facilityId);
		
		// ダウンロード用にリクエストクラスを保持
		lastExecQuery = query;
		
		return query;
	}
	
	/**
	 * このコンポジットが利用するテーブルを取得します。<BR>
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return tableViewer.getTable();
	}
	
	/**
	 * このコンポジットが利用するテーブルビューアを取得します。<BR>
	 *
	 * @return テーブルビューア
	 */
	public TableViewer getTableViewer() {
		return tableViewer;
	}
	
	public void setManager(String managerName) {
		this.managerName = managerName;
	}
	
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * シナリオ実績情報の取得
	 */
	private SearchRpaScenarioOperationResultResponse getQueryExecute(String managerName, SearchRpaScenarioOperationResultRequest query) {
		Map<String, String> errorMsgs = new HashMap<>();
		SearchRpaScenarioOperationResultResponse resultResponseList = null;

		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			resultResponseList = wrapper.searchRpaScenarioOperationResultList(query);
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).warn("getLogTransferDestTypeMstList(), " + e.getMessage(), e);
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		return resultResponseList;
	}
	
	private void updateResultComposite(Boolean isNeedCount, SearchRpaScenarioOperationResultResponse result) {
		if (result == null) {
			return;
		}

		// データ件数・表示オフセット・表示行数更新
		if (isNeedCount) {
			dataCount = result.getCount();
		}

		// 総ページ数
		Table table = tableViewer.getTable();
		long dateCntPerPage = getDataCountPerPage(table);
		if (dateCntPerPage == 0) {
			return;
		}
		
		long allPage = dataCount / dateCntPerPage + ((dataCount % dateCntPerPage) != 0 ? 1: 0);
		dispOffset = result.getOffset();
		dispSize = result.getSize();
		
		long currentPage = 0;
		long dataFrom = dispOffset;
		if (dataCount != 0){
			currentPage = result.getOffset()/dateCntPerPage +1;
			dataFrom +=1;
			// データが存在する場合はダウンロードボタンを活性化
			this.btnDownload.setEnabled(true);
		} else {
			// 0件の場合はダウンロードボタン非活性
			this.btnDownload.setEnabled(false);
		}
		lbltotal.setText(Messages.getString("view.rpa.scenario.operation.result.search.page.number", 
				new Object[] { dataCount, dataFrom, dispOffset + dispSize, new DecimalFormat("#.##").format(((double)result.getTime()) / 1000)}));
		lblPage.setText(currentPage + " / "+ allPage);
		
		tableViewer.setInput(result.getResultList());
		
		btnPerv.setEnabled(currentPage > 1);
		btnNext.setEnabled(currentPage < allPage);
		
		btnTop.setEnabled(currentPage > 1);
		btnEnd.setEnabled(currentPage < allPage);
	}
	
	private int getDataCountPerPage(Table table){
		if (null ==table) {
			return 0;
		}
		return ClusterControlPlugin.getDefault().getPreferenceStore().getInt(HubPreferencePage.P_SIZE_POS);
	}
	
	/**
	 * タグを設定します。
	 */
	public void setTag(List<RpaScenarioTagResponse> selectTagList, Map<String,String> tagNameMap, Map<String,String> tagPathMap) {
		this.txtTagId.setText(new RpaScenarioTagUtil().getJoinTagLayer(selectTagList, tagNameMap, tagPathMap));
		// ダイアログ用に選択したタグを保存しておく
		this.tagList = selectTagList;
	}
	
	/**
	 * ミリ秒Longを文字列「時:分:秒」に変換する。
	 */
	public static String convertTimeToHMS(Long time) {
		if (time == null) {
			return "";
		}
		
		String sign = "";
		if (time < 0) {
			time *= -1;
			sign = "-";
		}
		long hour = time / (60 * 60 * 1000);
		long minute = (time % (60 * 60 * 1000)) / (60 * 1000);
		long second = Math.round((double)(time % (60 * 1000)) / 1000);
		// 丸めて60秒になったら繰り上げ
		if (second == 60) {
			minute += 1;
			second = 0;
			// 60分になったら繰り上げ
			if (minute == 60) {
				hour += 1;
				minute = 0;
			}
		}
		
		return String.format("%s%d:%02d:%02d", sign, hour, minute, second);
	}
}
