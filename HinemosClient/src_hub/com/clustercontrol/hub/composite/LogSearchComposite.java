/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.composite;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
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

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.binary.util.BinaryEndpointWrapper;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.hub.action.DownloadCollectedData;
import com.clustercontrol.hub.commons.LogSearchPeriodConstants;
import com.clustercontrol.hub.dialog.RecordInfoDialog;
import com.clustercontrol.hub.preference.HubPreferencePage;
import com.clustercontrol.hub.util.HubEndpointWrapper;
import com.clustercontrol.hub.util.TableViewerSorter;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.viewer.CommonTableViewerSorter;
import com.clustercontrol.ws.hub.BinaryQueryInfo;
import com.clustercontrol.ws.hub.InvalidRole_Exception;
import com.clustercontrol.ws.hub.InvalidSetting_Exception;
import com.clustercontrol.ws.hub.Operator;
import com.clustercontrol.ws.hub.StringData;
import com.clustercontrol.ws.hub.StringQueryInfo;
import com.clustercontrol.ws.hub.StringQueryResult;
import com.clustercontrol.ws.hub.Tag;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.MonitorInfoBean;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * ログ検索画面クラス<br/>
 *
 * @version 6.1.0 バイナリ検索機能追加.
 * @since 6.0.0
 */
public class LogSearchComposite extends Composite {

	private static Logger logger  = Logger.getLogger(LogSearchDateTimeComposite.class);

	/**
	 * 検索結果:一覧ビュー カラム定義enum
	 */
	public enum ViewColumn {
		time(Messages.getString("view.hub.log.search.result.culumn.timestamp"), new ColumnPixelData(110, true, true),
			new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					StringData stringData = (StringData) element;
					Date date=new Date(stringData.getTime());
					SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					return df.format(date);
				}
			}), 
		facility(Messages.getString("view.hub.log.search.result.culumn.facility"),new ColumnPixelData(70, true, true),
			new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					StringData stringData = (StringData) element;
					return stringData.getFacilityId();
				}
			}), 
		monitor(Messages.getString("view.hub.log.search.result.culumn.monitor"),new ColumnPixelData(70, true, true),
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						StringData stringData = (StringData) element;
						return stringData.getMonitorId();
					}
				}), 
		original_message(Messages.getString("view.hub.log.search.result.culumn.original.message"),
				new ColumnPixelData(370, true, true), new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					StringData stringData = (StringData) element;
					return HinemosMessage.replace(stringData.getData());
				}

				@Override
				public String getToolTipText(Object element) {
					return decode((String) getText(element));
				}

				@Override
				public Point getToolTipShift(Object object) {
					return new Point(100, 5);
				}

				@Override
				public int getToolTipDisplayDelayTime(Object object) {
					return 500;
				}

				@Override
				public int getToolTipTimeDisplayed(Object object) {
					return 500000;
				}

				@Override
				public int getToolTipStyle(Object object) {
					return SWT.LEFT;
				}
			}), 
		etc(Messages.getString("view.hub.log.search.result.culumn.etc"),
			new ColumnPixelData(370, true, true), new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					StringData stringData = (StringData) element;
					final StringBuffer label=new StringBuffer();
					List<Tag> tags = stringData.getTagList();
					for (Tag tag : tags){
						if (label.length()>0){
							label.append(",");
						}
						label.append(tag.getKey()+"="+tag.getValue());
					}
					return label.toString();
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

	private String manager;
	private String facilityId;
	private int dataCount;
	private int dispOffset;
	private int dispSize;
	
	private Long from;
	private Long to;
	private String monitorId;
	private String keywords;
	private Operator ope;

	private Text txtKeywords;
	private Combo cmbMonitorId;
	private Button btnAnd;
	private Button btnOr;
	private Combo cmbPeriod;
	
	private Button btnPerv;
	private Button btnNext;
	private Button btnTop;
	private Button btnEnd;
	
	private LogSearchDateTimeComposite dateTimeCompositeFrom;
	private LogSearchDateTimeComposite dateTimeCompositeTo;
	private Label lbltotal;
	private Text browser;
	
	// バイナリ検索向けに追加した部品.
	private Button btnText;
	private Button btnBinary;
	private Text txtEncoding;
	private Button btnDownload;
	private Shell m_shell = null;

	/** 検索結果の出力範囲 */
	private Label lblPage;
	private TableViewer tableViewer;
	private Table table;

	/**
	 * コンストラクタ.
	 */
	public LogSearchComposite(Composite parent, int style) {
		super(parent, style);
		this.m_shell = this.getShell();
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

		// 検索UI - 検索条件 テキスト検索/バイナリ検索.
		Composite textOrBinary = new Composite(searchComposite, SWT.NONE);
		GridLayout gl_textOrBinary = new GridLayout(2, false);
		gl_textOrBinary.verticalSpacing = 1;
		textOrBinary.setLayout(gl_textOrBinary);
		textOrBinary.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1));

		btnText = new Button(textOrBinary, SWT.RADIO);
		btnText.setText(Messages.getString("search.text"));
		btnText.setSelection(true);

		btnBinary = new Button(textOrBinary, SWT.RADIO);
		btnBinary.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnBinary.setText(Messages.getString("search.binary"));

		SelectionAdapter textOrBinarySelection = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnBinary.getSelection()) {
					txtEncoding.setEnabled(true);
					txtEncoding.setText("UTF-8");
				} else {
					txtEncoding.setEnabled(false);
					txtEncoding.setText("");
				}
				updateMonitorCombo();
				clearResultComposite();
			}
		};
		btnText.addSelectionListener(textOrBinarySelection);
		btnBinary.addSelectionListener(textOrBinarySelection);

		// 検索UI - 期間指定.		
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
		lblPeriod.setText(Messages.getString("view.hub.log.search.condition.period.select"));
		
		cmbPeriod = new Combo(compositePeriod, SWT.READ_ONLY);
		GridData gd_cmbPeriod = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_cmbPeriod.widthHint = 111;
		cmbPeriod.setLayoutData(gd_cmbPeriod);
		cmbPeriod.setText(LogSearchPeriodConstants.ALL);
		cmbPeriod.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo select = (Combo) e.getSource();
				List<Calendar> list = LogSearchPeriodConstants.getPeriod(select.getText());
				if (list == null) {
					cmbPeriod.setText(LogSearchPeriodConstants.ALL);
				} else {
					dateTimeCompositeFrom.setPeriod(list.get(0));
					dateTimeCompositeTo.setPeriod(list.get(1));
				}
				if (LogSearchPeriodConstants.ALL.equals(select.getText())){
					enableDateTime(false);
				}else{
					enableDateTime(true);
				}
				update();
			}
		});

		Label label = new Label(compositePeriod, SWT.NONE);
		GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_label.widthHint = 10;
		label.setLayoutData(gd_label);

		dateTimeCompositeFrom = new LogSearchDateTimeComposite(compositePeriod, SWT.NONE);
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

		dateTimeCompositeTo = new LogSearchDateTimeComposite(compositePeriod, SWT.NONE);
		gd_dateTimeComposite = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_dateTimeComposite.widthHint = 250;
		dateTimeCompositeTo.setLayoutData(gd_dateTimeComposite);
		gridLayout = (GridLayout) dateTimeCompositeTo.getLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		enableDateTime(false);
		
		// 検索UI - 監視ID
		Composite compositeMonitor = new Composite(searchComposite, SWT.NONE);
		GridLayout gl_compositeMonitor = new GridLayout(4, false);
		gl_compositeMonitor.marginHeight = 0;
		compositeMonitor.setLayout(gl_compositeMonitor);
		GridData gd_compositeMonitor = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeMonitor.setLayoutData(gd_compositeMonitor);

		Label lblMonitorId = new Label(compositeMonitor, SWT.NONE);
		GridData gd_lblMonitorId = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblMonitorId.widthHint = 98;
		lblMonitorId.setLayoutData(gd_lblMonitorId);
		lblMonitorId.setText(Messages.getString("monitor.id"));

		cmbMonitorId = new Combo(compositeMonitor, SWT.READ_ONLY);
		GridData gd_monitorId = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_monitorId.heightHint = 18;
		gd_monitorId.widthHint = 481;
		cmbMonitorId.setLayoutData(gd_monitorId);
		cmbMonitorId.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				update();
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}
		});
		for (String str : LogSearchPeriodConstants.getPeriodStrList()) {
			cmbPeriod.add(str);
		}
		cmbPeriod.select(0);

		// 検索UI - キーワード
		Composite compositeKeyword = new Composite(searchComposite, SWT.NONE);
		GridLayout gl_compositeKeyword = new GridLayout(4, false);
		gl_compositeKeyword.marginHeight = 0;
		compositeKeyword.setLayout(gl_compositeKeyword);
		GridData gd_compositeKeyword = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeKeyword.setLayoutData(gd_compositeKeyword);
		
		Label lblKeyword = new Label(compositeKeyword, SWT.NONE);
		GridData gd_lblKeyword = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblKeyword.widthHint = 98;
		lblKeyword.setLayoutData(gd_lblKeyword);
		lblKeyword.setText(Messages.getString("view.hub.log.search.condition.keyword"));

		txtKeywords = new Text(compositeKeyword, SWT.BORDER);
		GridData gd_txtKeywords = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtKeywords.widthHint = 500;
		txtKeywords.setMessage(Messages.getString("pattern.placeholder.like"));
		txtKeywords.setLayoutData(gd_txtKeywords);

		// 検索UI - 検索条件 AND/OR
		Composite compositeConditionAndOr = new Composite(compositeKeyword, SWT.NONE);
		GridLayout gl_compositeConditionAndOr = new GridLayout(2, false);
		gl_compositeConditionAndOr.verticalSpacing = 1;
		compositeConditionAndOr.setLayout(gl_compositeConditionAndOr);
		compositeConditionAndOr.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));

		btnAnd = new Button(compositeConditionAndOr, SWT.RADIO);
		btnAnd.setText(Messages.getString("and"));
		btnAnd.setSelection(true);

		btnOr = new Button(compositeConditionAndOr, SWT.RADIO);
		btnOr.setText(Messages.getString("or"));

		// 検索UI - 検索条件 エンコーディング(バイナリ検索向け)
		Composite compositeEncoding = new Composite(searchComposite, SWT.NONE);
		GridLayout gl_compositeEncoding = new GridLayout(4, false);
		gl_compositeEncoding.marginHeight = 0;
		compositeEncoding.setLayout(gl_compositeEncoding);
		GridData gd_compositeEncoding = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeEncoding.setLayoutData(gd_compositeEncoding);

		Label lblEncoding = new Label(compositeEncoding, SWT.NONE);
		GridData gd_lblEncoding = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblEncoding.widthHint = 98;
		lblEncoding.setLayoutData(gd_lblEncoding);
		lblEncoding.setText(Messages.getString("job.script.encoding"));

		txtEncoding = new Text(compositeEncoding, SWT.BORDER);
		GridData gd_txtEncoding = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_txtEncoding.heightHint = 18;
		gd_txtEncoding.widthHint = 500;
		txtEncoding.setLayoutData(gd_txtEncoding);
		txtEncoding.setEnabled(false);
		txtEncoding.setToolTipText(Messages.getString("tooltip.input.java.charset"));

		// 検索UI - 検索ボタン
		Button btnSearchSimply = new Button(compositeEncoding, SWT.NONE);
		GridData gd_btnSearchSimply = new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1);
		gd_btnSearchSimply.widthHint = 82;
		btnSearchSimply.setLayoutData(gd_btnSearchSimply);
		btnSearchSimply.setSize(300, 30);
		btnSearchSimply.setText(Messages.getString("search"));
		btnSearchSimply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 入力値チェック.
				ValidateResult result = checkInputForSearch();
				if (result == null || result.isValid()) {
					StringQueryInfo query = null;
					BinaryQueryInfo binaryQuery = null;
					Boolean isNeedCount = null;
					if (btnText.getSelection()) {
						query = makeNewQuery();
						isNeedCount = query.isNeedCount();
					} else {
						binaryQuery = makeNewBinaryQuery();
						isNeedCount = binaryQuery.isNeedCount();
					}
					StringQueryResult stringQueryResult = getQueryExecute(manager, query, binaryQuery);
					updateResultComposite(isNeedCount, stringQueryResult);
				} else {
					// クライアントにエラーダイアログ表示.
					MessageDialog.openWarning(null, result.getID(), result.getMessage());
				}
			}
		});

		// 検索UI - ダウンロードボタン
		btnDownload = new Button(compositeEncoding, SWT.NONE);
		GridData gd_btnDownload = new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1);
		gd_btnDownload.widthHint = 82;
		btnDownload.setLayoutData(gd_btnDownload);
		btnDownload.setText(Messages.getString("view.hub.binary.download"));
		btnDownload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 表示されているレコードを全件取得(ページ外除く).
				@SuppressWarnings("unchecked")
				List<StringData> dataList = (List<StringData>) tableViewer.getInput();
				DownloadCollectedData downloadLog = new DownloadCollectedData();
				if (btnBinary.getSelection()) {
					// バイナリデータを複数ダウンロード.
					downloadLog.executeBinaryRecords(m_shell, manager, dataList);
				}
				if (btnText.getSelection()) {
					// オリジナルメッセージをファイルとして出力.
					downloadLog.executeTextRecordsToOne(browser.getText(), dataList, m_shell);
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
		tbtmTable.setText(Messages.getString("view.hub.log.search.result.tab.list"));

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
				StringData selectStringData = (StringData) ((StructuredSelection) event.getSelection())
						.getFirstElement();
				RecordInfoDialog dialog = new RecordInfoDialog(m_shell, manager, selectStringData,
						btnBinary.getSelection());
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
		tableViewer.setSorter(new CommonTableViewerSorter(ViewColumn.time.ordinal()));

		TabItem tbtmSource = new TabItem(tabFolder, SWT.NONE);
		tbtmSource.setText(Messages.getString("view.hub.log.search.result.tab.original.message"));

		Composite compositeSource = new Composite(tabFolder, SWT.NONE);
		tbtmSource.setControl(compositeSource);
		compositeSource.setLayout(new GridLayout(1, false));

		GridData gd_Source = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_Source.heightHint = 634;
		compositeSource.setLayoutData(gd_Source);

		browser = new Text(compositeSource, SWT.READ_ONLY | SWT.MULTI);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		// Not working End(WindowBuilder)

		Composite compositePageNumber = new Composite(resultComposite, SWT.NONE);
		GridLayout gl_compositePageNumber = new GridLayout(6, false);
		gl_compositePageNumber.marginHeight = 0;
		compositePageNumber.setLayout(gl_compositePageNumber);
		GridData gd_pagerComposite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
//		gd_pagerComposite.heightHint = 24;
//		gd_pagerComposite.widthHint = 411;
		compositePageNumber.setLayoutData(gd_pagerComposite);

		lbltotal = new Label(compositePageNumber, SWT.RIGHT);
		GridData gd_lbltotal = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		lbltotal.setLayoutData(gd_lbltotal);
		lbltotal.setText(Messages.getString("view.hub.log.search.result.page.number", new Object[] { "0", "0", "0", "0.0"}));

		btnTop = new Button(compositePageNumber, SWT.NONE);
		btnTop.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnTop.setText("<<");
		btnTop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringQueryInfo query = null;
				BinaryQueryInfo binaryQuery = null;
				Boolean isNeedCount = null;
				if (btnText.getSelection()) {
					query = makeContinuousQuery(0, getDataCountPerPage(getTable()));
					isNeedCount = query.isNeedCount();
				} else {
					binaryQuery = makeContinuousBinaryQuery(0, getDataCountPerPage(getTable()));
					isNeedCount = binaryQuery.isNeedCount();
				}
				StringQueryResult stringQueryResult = getQueryExecute(manager, query, binaryQuery);
				updateResultComposite(isNeedCount, stringQueryResult);
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
				StringQueryInfo query = null;
				BinaryQueryInfo binaryQuery = null;
				Boolean isNeedCount = null;
				if (btnText.getSelection()) {
					query = makeContinuousQuery(offset, getDataCountPerPage(getTable()));
					isNeedCount = query.isNeedCount();
				} else {
					binaryQuery = makeContinuousBinaryQuery(offset, getDataCountPerPage(getTable()));
					isNeedCount = binaryQuery.isNeedCount();
				}
				StringQueryResult stringQueryResult = getQueryExecute(manager, query, binaryQuery);
				updateResultComposite(isNeedCount, stringQueryResult);
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
				StringQueryInfo query = null;
				BinaryQueryInfo binaryQuery = null;
				Boolean isNeedCount = null;
				if (btnText.getSelection()) {
					query = makeContinuousQuery(offset, getDataCountPerPage(getTable()));
					isNeedCount = query.isNeedCount();
				} else {
					binaryQuery = makeContinuousBinaryQuery(offset, getDataCountPerPage(getTable()));
					isNeedCount = binaryQuery.isNeedCount();
				}
				StringQueryResult stringQueryResult = getQueryExecute(manager, query, binaryQuery);
				// 追加データがなかった
				if (0 == stringQueryResult.getSize()){
					return;
				}
				updateResultComposite(isNeedCount, stringQueryResult);
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
				StringQueryInfo query = null;
				BinaryQueryInfo binaryQuery = null;
				Boolean isNeedCount = null;
				if (btnText.getSelection()) {
					query = makeContinuousQuery(offset, getDataCountPerPage(getTable()));
					isNeedCount = query.isNeedCount();
				} else {
					binaryQuery = makeContinuousBinaryQuery(offset, getDataCountPerPage(getTable()));
					isNeedCount = binaryQuery.isNeedCount();
				}
				StringQueryResult stringQueryResult = getQueryExecute(manager, query, binaryQuery);
				// 追加データがなかった
				if (0 == stringQueryResult.getSize()){
					return;
				}
				updateResultComposite(isNeedCount, stringQueryResult);
			}
		});
		btnEnd.setEnabled(false);
	}

	private void enableDateTime(boolean enable){
		dateTimeCompositeFrom.setEnabled(enable);
		dateTimeCompositeTo.setEnabled(enable);
	}

	/**
	 * 検索ボタン押下時入力値チェック.
	 */
	private ValidateResult checkInputForSearch() {

		// エラーメッセージ用引数.
		String[] args = null;
		ValidateResult result = null;

		// エンコード.
		if (this.txtEncoding.getText() != null && !"".equals(this.txtEncoding.getText())) {
			// 妥当なエンコーディング方式かチェック.
			try {
				Charset.forName(this.txtEncoding.getText());
			} catch (IllegalCharsetNameException exception) {
				// 不正なエンコーディング方式の場合.
				args = new String[] { this.txtEncoding.getText() };
				result = this.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.binary.2", args));
			} catch (UnsupportedCharsetException exception) {
				// サポートされていないエンコーディング方式の場合.
				args = new String[] { this.txtEncoding.getText() };
				result = this.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.binary.3", args));
			}
		}
		return result;
	}

	/**
	 * エラー内容をセット.
	 *
	 * @param id
	 *            メッセージID
	 * @param message
	 *            メッセージ内容
	 */
	private ValidateResult getValidateResult(String id, String message) {

		ValidateResult result = new ValidateResult();
		result.setValid(false);
		result.setID(id);
		result.setMessage(message);

		return result;
	}

	/**
	 * 検索条件の作成.
	 */
	private StringQueryInfo makeNewQuery() {
		ope = btnAnd.getSelection()? Operator.AND: Operator.OR;
		if (cmbMonitorId.getText().isEmpty()){
			monitorId = null;
		} else {
			monitorId = cmbMonitorId.getText();
		}
		
		if (LogSearchPeriodConstants.ALL.equals(cmbPeriod.getText())){
			from = null;
			to = null;
		}else{
			from = dateTimeCompositeFrom.getEpochMillis();
			to = dateTimeCompositeTo.getEpochMillis();
		}
		keywords = txtKeywords.getText();
		
		return makeQuery(
				from,
				to,
				monitorId,
				keywords,
				ope,
				0,
				getDataCountPerPage(getTable()),
				true
				);
	}

	/**
	 * バイナリ検索条件の作成.
	 */
	private BinaryQueryInfo makeNewBinaryQuery() {
		StringQueryInfo stringQueryInfo = makeNewQuery();
		BinaryQueryInfo binaryQueryInfo = this.getBinaryQueryInfo(stringQueryInfo);
		return binaryQueryInfo;
	}

	/**
	 * バイナリ検索条件を生成して取得.
	 * 
	 * @param stringQueryInfo
	 *            生成済の親の検索条件.
	 */
	private BinaryQueryInfo getBinaryQueryInfo(StringQueryInfo stringQueryInfo) {

		BinaryQueryInfo binaryQueryInfo = new BinaryQueryInfo();
		// 親クラスの検索条件設定.
		binaryQueryInfo.setFrom(stringQueryInfo.getFrom());
		binaryQueryInfo.setTo(stringQueryInfo.getTo());
		binaryQueryInfo.setMonitorId(stringQueryInfo.getMonitorId());
		binaryQueryInfo.setFacilityId(stringQueryInfo.getFacilityId());
		binaryQueryInfo.setKeywords(stringQueryInfo.getKeywords());
		binaryQueryInfo.setOperator(stringQueryInfo.getOperator());
		binaryQueryInfo.setOffset(stringQueryInfo.getOffset());
		binaryQueryInfo.setSize(stringQueryInfo.getSize());
		binaryQueryInfo.setNeedCount(stringQueryInfo.isNeedCount());

		// バイナリの検索条件設定.
		if (txtEncoding.getText() != null && !txtEncoding.getText().isEmpty()) {
			binaryQueryInfo.setTextEncoding(txtEncoding.getText());
		}

		return binaryQueryInfo;
	}

	/**
	 * 表示範囲指定して検索条件生成.
	 */
	private StringQueryInfo makeContinuousQuery(int dispOffset, int dispSize) {
		return makeQuery(
				from,
				to,
				monitorId,
				keywords,
				ope,
				dispOffset,
				dispSize,
				true
				);
	}
	
	/**
	 * 表示範囲指定してバイナリ検索条件生成.
	 */
	private BinaryQueryInfo makeContinuousBinaryQuery(int dispOffset, int dispSize) {
		StringQueryInfo stringQueryInfo = makeContinuousQuery(dispOffset, dispSize);
		BinaryQueryInfo binaryQueryInfo = this.getBinaryQueryInfo(stringQueryInfo);
		return binaryQueryInfo;
	}

	/**
	 * クエリの生成.
	 */
	private StringQueryInfo makeQuery(Long from, Long to, String monitorId, String keywords, Operator ope, int dispOffset, int dispSize, boolean firstQuery){
		StringQueryInfo query = new StringQueryInfo();
		query.setFacilityId(facilityId);
		query.setOperator(ope);
		
		query.setMonitorId(monitorId);
		
		query.setOffset(dispOffset);
		query.setSize(dispSize);
		
		query.setFrom(from);
		query.setTo(to);
		
		query.setKeywords(keywords);
		query.setNeedCount(firstQuery);
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

	/**
	 * 転送データ種別一覧の取得
	 * @param managerName マネージャ名
	 */
	private StringQueryResult getQueryExecute(String managerName, StringQueryInfo query, BinaryQueryInfo binaryQuery) {
		Map<String, String> errorMsgs = new HashMap<>();

		StringQueryResult stringQueryResult=null;
		try {
			HubEndpointWrapper wrapper = HubEndpointWrapper.getWrapper(managerName);
			BinaryEndpointWrapper binaryWrapper = BinaryEndpointWrapper.getWrapper(managerName);
			if (this.btnText.getSelection()) {
				stringQueryResult = wrapper.queryCollectStringData(query);
			} else {
				stringQueryResult = binaryWrapper.queryCollectBinaryData(binaryQuery);
			}
		} catch (InvalidSetting_Exception e){
			errorMsgs.put(managerName, Messages.getString(HinemosMessage.replace(e.getMessage())));
		} catch (InvalidRole_Exception e) {
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).warn("getLogTransferDestTypeMstList(), " + e.getMessage(), e);
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		return stringQueryResult;
	}
	
	private void updateResultComposite(Boolean isNeedCount, StringQueryResult result) {
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
			// データ存在する場合はダウンロードボタンを活性化.
			this.btnDownload.setEnabled(true);
		} else {
			// 0件の場合はダウンロードボタン非活性.
			this.btnDownload.setEnabled(false);
		}
		lbltotal.setText(Messages.getString("view.hub.log.search.result.page.number", 
				new Object[] { dataCount, dataFrom, dispOffset + dispSize, new DecimalFormat("#.##").format(((double)result.getTime()) / 1000)}));
		lblPage.setText(currentPage + " / "+ allPage);
		
		tableViewer.setInput(result.getDataList());
		updateSourceView(result.getDataList());
		
		btnPerv.setEnabled(currentPage > 1);
		btnNext.setEnabled(currentPage < allPage);
		
		btnTop.setEnabled(currentPage > 1);
		btnEnd.setEnabled(currentPage < allPage);
	}

	/**
	 * 検索結果の表示クリア.
	 */
	private void clearResultComposite() {

		// データ件数・表示オフセット・表示行数更新
		dataCount = 0;

		// 総ページ数
		Table table = tableViewer.getTable();
		long dateCntPerPage = getDataCountPerPage(table);
		if (dateCntPerPage == 0) {
			return;
		}
		
		long allPage = 0;
		dispOffset = 0;
		dispSize = 0;
		
		long currentPage = 0;
		long dataFrom = dispOffset;
		this.btnDownload.setEnabled(false);
		lbltotal.setText(Messages.getString("view.hub.log.search.result.page.number", 
				new Object[] { dataCount, dataFrom, dispOffset + dispSize, new DecimalFormat("#.##").format((0) / 1000)}));
		lblPage.setText(currentPage + " / "+ allPage);
		
		tableViewer.setInput(new ArrayList<StringData>());
		updateSourceView(new ArrayList<StringData>());
		
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
	 * オリジナルメッセージ欄の表示内容更新.
	 */
	private void updateSourceView(List<StringData> stringDataList){
		if (null == stringDataList){
			return;
		}
		StringBuffer sb = new StringBuffer();
		for (StringData stringData: stringDataList){
			sb.append(stringData.getData()).append("\n");
		}
		
		browser.setText(sb.toString());
		browser.redraw();
	}

	public static String decode(String text) {
		text = text.replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"")
				.replaceAll("&yen;", "\\").replaceAll("&nbsp;", " ").replaceAll("&nbsp;&nbsp;&nbsp;&nbsp;", "\t")
				.replaceAll("<br/>", "\r\n");

		return text;
	}
	
	public void setManager(String manager) {
		this.manager = manager;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * 監視IDコンボボックス更新.
	 */
	public void updateMonitorCombo() {
		// Message collecting
		Map<String, String> errMsgs = new ConcurrentHashMap<>();

		// データ取得
		Map<String, List<MonitorInfoBean>> dispDataMap= new ConcurrentHashMap<>();

		for(String managerName : EndpointManager.getActiveManagerSet()) {
			getMonitorList(managerName, dispDataMap, errMsgs);
		}

		// Show message box
		if( 0 < errMsgs.size() ){
			UIManager.showMessageBox(errMsgs, true);
		}

		this.cmbMonitorId.removeAll();
		this.cmbMonitorId.add("");
		for( Map.Entry<String, List<MonitorInfoBean>> e: dispDataMap.entrySet() ){
			if (e.getKey().equals(this.manager)) {
				for (MonitorInfoBean monitorBean : e.getValue()) {
					if (isCollectableMonitor(monitorBean.getMonitorType())){
						this.cmbMonitorId.add(monitorBean.getMonitorId());
					}
				}
			}
		}
		this.cmbMonitorId.select(0);
	}
	
	/**
	 * 収集向け監視種別か判定.
	 * 
	 * @param monitorType
	 *            判定対象の監視種別
	 * @return true:収集向け監視対象,false:対象外
	 */
	private boolean isCollectableMonitor(Integer monitorType){
		boolean ret = false;
		if (this.btnText.getSelection()) {
			// 文字列検索の場合は文字列・トラップ.
			if (MonitorTypeConstant.TYPE_STRING == monitorType || MonitorTypeConstant.TYPE_TRAP == monitorType) {
				ret = true;
			}
		} else {
			// バイナリ検索の場合.
			if (MonitorTypeConstant.TYPE_BINARY == monitorType) {
				ret = true;
			}
		}
		return ret;
	}
	
	private void getMonitorList(String managerName,
			Map<String, List<MonitorInfoBean>> dispDataMap,
			Map<String, String> errorMsgs) {
		try {
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
			List<MonitorInfoBean> list = wrapper.getMonitorBeanList();
			if( null != list ){
				dispDataMap.put(managerName, list);
			}
		} catch (MonitorNotFound_Exception | HinemosUnknown_Exception e) {
			errorMsgs.put( managerName, Messages.getString("message.monitor.67") + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			logger.warn("update() getMonitorList, " + HinemosMessage.replace(e.getMessage()), e);
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
	}
}
