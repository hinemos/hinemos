/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.CollectKeyInfoResponseP1;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.collect.action.RecordDataWriter;
import com.clustercontrol.collect.bean.SummaryTypeMessage;
import com.clustercontrol.collect.util.CollectGraphUtil.CollectFacilityDataInfo;
import com.clustercontrol.util.DateTimeStringConverter;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * 収集した実績データをエクスポートするためのダイアログクラス
 *
 * @version 4.0.0
 * @since 1.0.0
 */
public class ExportDialog extends Dialog {

	// ログ
	private static Log m_log = LogFactory.getLog( ExportDialog.class );

	// Dialog Composite
	private Button headerCheckbox = null; // ヘッダ出力有無用チェックボックス

	// Export Setting
	private RecordDataWriter writer;
	
	private TreeMap<String, CollectFacilityDataInfo> m_managerFacilityDataInfoMap = null;
	private Integer m_summaryType = null;
	private List<CollectKeyInfoResponseP1> m_collectKeyInfoPkList = null;	
	private TreeMap<String, List<String>> m_targetManagerFacilityMap = null;
	private static final String SQUARE_SEPARATOR = "\u2029";
	/**
	 * コンストラクタ
	 */
	public ExportDialog(Shell parent, TreeMap<String, CollectFacilityDataInfo> managerFacilityDataInfoMap,
			Integer summaryType,
			List<CollectKeyInfoResponseP1> targetCollectKeyInfoList,
			TreeMap<String, List<String>> managerFacilityIdMap){
		super(parent);
		this.m_managerFacilityDataInfoMap = managerFacilityDataInfoMap;
		this.m_summaryType = summaryType;
		this.m_collectKeyInfoPkList = targetCollectKeyInfoList;
		this.m_targetManagerFacilityMap =managerFacilityIdMap;
	}

	/**
	 * 初期サイズの設定
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(400, 500);
	}

	/**
	 * タイトルの設定
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("performance.export.dialog.title")); // "性能[エクスポート]"
	}

	/**
	 * ダイアログの設定
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		// コンポジット全体
		Composite allComposite = (Composite) super.createDialogArea(parent);
		WidgetTestUtil.setTestId(this, "all", allComposite);
		allComposite.setLayout(new FillLayout());

		Composite scopeComposite = new Composite(allComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "scope", scopeComposite);
		scopeComposite.setLayout(new GridLayout());
		
		// スコープ表示
		Composite topComposite = new Composite(scopeComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "top", topComposite);
		topComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		topComposite.setLayout(new FormLayout());

		// Test
		// ヘッダ出力有無チェックボックス
		headerCheckbox = new Button(topComposite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "headercheck", headerCheckbox);
		headerCheckbox.setText(Messages.getString("performance.output.header")); // "ヘッダを出力"

		FormData formData = new FormData();
		formData.top = new FormAttachment(0, 0); // ウィンドウの上側にはりつく
		formData.right = new FormAttachment(100, 0); // ラベルの右側にはりつく
		headerCheckbox.setLayoutData(formData);

		// 対象の情報を表示する
		createTree(scopeComposite);

		// セパレータ
		createSeparator(scopeComposite);

		return allComposite;
	}

	/**
	 * FacilityTreeのコンポジット。
	 * ノード指定時のみOKボタンを
	 */
	private void createTree(Composite composite) {
		
		// マネージャ名
		String managerName = "";
		for (Map.Entry<String, CollectFacilityDataInfo> entry : m_managerFacilityDataInfoMap.entrySet()) {
			managerName = entry.getKey().substring(0, entry.getKey().lastIndexOf(SQUARE_SEPARATOR));
			break;// マネージャは1件しかありえないため、即break
		}
		Label managerLabel = new Label(composite, SWT.RIGHT | SWT.WRAP);
		managerLabel.setText(Messages.getString("facility.managername") + " : " + managerName);
		managerLabel.setToolTipText(managerName);
		
		// サマリータイプ
		Label summaryLabel = new Label(composite, SWT.RIGHT | SWT.WRAP);
		summaryLabel.setText(Messages.getString("collection.summary.type") + " : " + SummaryTypeMessage.typeToString(m_summaryType));

		// 期間
		Label termLabel = new Label(composite, SWT.RIGHT | SWT.WRAP);
		termLabel.setText(Messages.getString("collection.export.period") + " : " + Messages.getString("collection.export.entries.period"));
		
		// ファシリティ名とファシリティID
		Label facilityLabel = new Label(composite, SWT.RIGHT | SWT.WRAP);
		facilityLabel.setText(Messages.getString("facility.name") + " : ");
		org.eclipse.swt.widgets.List facilityList = new org.eclipse.swt.widgets.List(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.LEFT | SWT.BORDER);
		for (Map.Entry<String, CollectFacilityDataInfo> entry : m_managerFacilityDataInfoMap.entrySet()) {
			String facilityId = entry.getKey().split(SQUARE_SEPARATOR)[entry.getKey().split(SQUARE_SEPARATOR).length - 1];
			String facilityName = entry.getValue().getName();
			facilityList.add(facilityName + "(" + facilityId + ")");
		}
		GridData gridData_facility = new GridData(GridData.FILL_BOTH);
		gridData_facility.heightHint = facilityList.getItemHeight() * 5;
		gridData_facility.widthHint =300;
		facilityList.setLayoutData(gridData_facility);
		
		// 監視項目ID
		Label itemLabel = new Label(composite, SWT.RIGHT | SWT.WRAP);
		itemLabel.setText(Messages.getString("collection.display.name") + " : ");
		org.eclipse.swt.widgets.List itemList = new org.eclipse.swt.widgets.List(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.LEFT | SWT.BORDER);
		for (CollectKeyInfoResponseP1 collectInfo : m_collectKeyInfoPkList) {
			String itemName = HinemosMessage.replace(collectInfo.getItemName());
			if (!collectInfo.getDisplayName().equals("") && !itemName.endsWith("[" + collectInfo.getDisplayName() + "]")) {
				itemName += "[" + collectInfo.getDisplayName() + "]";
			}
			itemList.add(itemName + "(" + collectInfo.getMonitorId() + ")");
		}
		GridData gridData = new GridData(GridData.FILL_BOTH);	
		gridData.heightHint = itemList.getItemHeight() * 3;
		itemList.setLayoutData(gridData);
	}

	/**
	 * Customize button bar
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// 出力ボタン
		Button exportButton = this.createButton(parent,
				IDialogConstants.OPEN_ID, Messages.getString("export"), true);
		WidgetTestUtil.setTestId(this, "export", exportButton);

		this.getButton(IDialogConstants.OPEN_ID).addSelectionListener(
			new SelectionAdapter() {
				private FileDialog saveDialog;
				@Override
				public void widgetSelected(SelectionEvent e) {
					// 出力先ファイルを選択するダイアログを開く
					this.saveDialog = new FileDialog(getShell(), SWT.SAVE);
					boolean headerFlag = ExportDialog.this.headerCheckbox.getSelection();
					
					//名前([summaryType]_日時.zip、summaryTypeは英語)
					// 名前に日本語を含めると文字化けするため、英語にする
					
					// 対象ファイル名に含めるID(日付)を生成
					String defaultDateStr = DateTimeStringConverter.formatLongDate(System.currentTimeMillis(), "yyyyMMddHHmmss");
					String defaultFileName = SummaryTypeMessage.typeToStringEN(m_summaryType)+ '_' + defaultDateStr;
					this.saveDialog.setFilterExtensions(new String[] { "*.zip" });
					defaultFileName += ".zip";
					// ファイル名に空白があると+に置き換わってしまうため、空白を削除
					defaultFileName = defaultFileName.replaceAll(" ", "");
					this.saveDialog.setFileName(defaultFileName);

					String filePath = this.saveDialog.open();
					if( filePath != null ){
						m_log.debug("filePath = " + filePath + ", defaultFileName = " + defaultFileName);
						output(m_managerFacilityDataInfoMap, m_summaryType, m_collectKeyInfoPkList, m_targetManagerFacilityMap, 
								headerFlag, filePath, defaultFileName, defaultDateStr);
					}
				}

				/**
				 * Output
				 */
				protected void output(TreeMap<String,CollectFacilityDataInfo> managerFacilityDataInfoMap,
						Integer summaryType,
						List<CollectKeyInfoResponseP1> targetCollectKeyInfoList,
						TreeMap<String, List<String>> targetManagerFacilityMap,
						boolean headerFlag,
						String filePath,
						String fileName,
						String defaultDateStr) {

					// DataWriterへの入力
					// 書き込み準備
					writer = new RecordDataWriter(
							managerFacilityDataInfoMap,
							summaryType,
							targetCollectKeyInfoList,
							targetManagerFacilityMap,
							headerFlag,
							filePath,
							defaultDateStr);

					// Download & 書き込み
					try {
						IRunnableWithProgress op = new IRunnableWithProgress() {
							@Override
							public void run(IProgressMonitor monitor)
									throws InvocationTargetException, InterruptedException {
								// エクスポートを開始
								ServiceContext context = ContextProvider.getContext();
								writer.setContext(context);
								Thread exportThread = new Thread(writer);
								exportThread.start();
								Thread.sleep(3000);
								monitor.beginTask(Messages.getString("export"), 100); // "エクスポート"

								int progress = 0;
								int buff = 0;
								while (progress < 100) {
									progress = writer.getProgress();

									if (monitor.isCanceled()) {
										throw new InterruptedException("");
									}
									if (writer.isCanceled()) {
										throw new InterruptedException(writer.getCancelMessage());
									}
									Thread.sleep(50);
									monitor.worked(progress - buff);
									buff = progress;
								}
								monitor.done();
							}
						};

						// ダイアログの表示
						new ProgressMonitorDialog(getShell()).run(true, true, op);

						// Start download file
						if( ClusterControlPlugin.isRAP() ){
							FileDownloader.openBrowser(
									PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
									filePath, 
									fileName);
						}else{
							MessageDialog.openInformation(getShell(),
									Messages.getString("confirmed"),
									Messages.getString("performance.export.success"));
						}
					} catch (InterruptedException e) {
						// キャンセルされた場合の処理
						MessageDialog.openInformation(getShell(),
								Messages.getString("confirmed"),
								Messages.getString("performance.export.cancel") + " : " + e.getMessage());
					} catch (Exception e) {
						// 異常終了
						m_log.warn("output() : " + e.getMessage(), e);
						MessageDialog.openInformation(getShell(),
								Messages.getString("confirmed"),
								Messages.getString("performance.export.cancel") + " : " + e.getMessage() +
								"(" + e.getClass().getName() + ")");
					} finally {
						writer.setCanceled(true);
						if (ClusterControlPlugin.isRAP()) {
							FileDownloader.cleanup( filePath );
						}
					}
				}
			});
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("close"), false);
	}
	/**
	 * セパレータの作成
	 */
	private void createSeparator(Composite composite) {
		// セパレータ(水平線)を作成
		Label h_separator = new Label(composite, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "separator", h_separator);
		GridData gridDataLabel = new GridData(GridData.FILL_HORIZONTAL);
		h_separator.setLayoutData(gridDataLabel);
	}
}
