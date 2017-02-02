/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.dialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.Property;
import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.action.GetEventReportProperty;
import com.clustercontrol.monitor.bean.OutputFormConstant;
import com.clustercontrol.monitor.util.EventFilterPropertyUtil;
import com.clustercontrol.monitor.util.MonitorEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.viewer.PropertySheet;
import com.clustercontrol.ws.monitor.EventFilterInfo;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 監視[イベントのダウンロード]ダイアログクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class EventReportDialog extends CommonDialog {
	// ログ
	private static Log m_log = LogFactory.getLog(EventReportDialog.class);

	// 後でpackするためsizeXはダミーの値。
	private final int sizeX = 500;
	private final int sizeY = 700;

	/** プロパティシート。 */
	private PropertySheet propertySheet = null;

	/** Cache map of filter properties for each UI session */
	private static Map<UISession, Property> filterPropertyCache = new ConcurrentHashMap<>();

	/**
	 * 出力形式。
	 *
	 * @see com.clustercontrol.bean.OutputFormConstant
	 */
	private int m_outputForm = OutputFormConstant.TYPE_PDF;

	/** Scope */
	private String facilityId = null;

	/** Filename */
	private String fileName;

	/** File path */
	private String filePath;

	/** マネージャ名 */
	private String managerName = null;

	/**
	 * インスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param マネージャ名
	 */
	public EventReportDialog(Shell parent, String managerName, String facilityId) {
		super(parent);
		this.managerName = managerName;
		this.facilityId = facilityId;
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(sizeX, sizeY);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 *
	 * @see com.clustercontrol.monitor.action.GetEventReportProperty#getProperty()
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// タイトル
		shell.setText(Messages.getString("dialog.monitor.events.download"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);

		/*
		 * 属性プロパティシート
		 */
		// ラベル
		label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "attribute", label);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 15;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("attribute") + " : ");
		// プロパティシート
		Tree table = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, table);
		// table.setData(ClusterControlPlugin.CUSTOM_WIDGET_ID,
		// Debug "eventReportDialogTable");
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 15;
		table.setLayoutData(gridData);

		this.createPropertySheet(table);

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 15;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		// ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(shell.getSize().x, sizeY));
	}

	/**
	 * 既存のボタンに加え、クリアボタンを追加します。<BR>
	 * クリアボタンがクリックされた場合、 プロパティを再取得します。
	 *
	 * @param parent
	 *            親のコンポジット（ボタンバー）
	 *
	 * @see org.eclipse.swt.widgets.Button#addSelectionListener(SelectionListener)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// Add clear button
		Button clearButton = this.createButton(parent,
				IDialogConstants.OPEN_ID, Messages.getString("clear"), false);
		WidgetTestUtil.setTestId(this, "clear", clearButton);

		this.getButton(IDialogConstants.OPEN_ID).addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						resetPropertySheet();
					}
				});

		super.createButtonsForButtonBar(parent);
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("output");
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("close");
	}

	/**
	 * 出力形式を返します。
	 *
	 * @return 出力形式
	 */
	public int getOutputForm() {
		return m_outputForm;
	}

	/**
	 * 入力値チェックをして、ファイルに出力します。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult validateResult = null;
		if ( !this.output() ) {
			validateResult = new ValidateResult();
			validateResult.setValid(false);
			validateResult.setID(Messages.getString("message.hinemos.1"));
			validateResult.setMessage(Messages.getString("message.monitor.44"));
		}
		return validateResult;
	}

	@Override
	protected void okPressed() {
		// ファイルダイアログを開く
		FileDialog  fileDialog = new FileDialog(this.getShell(), SWT.SAVE);
		String extension = ".csv";
		fileDialog.setFilterExtensions(new String[] { "*" + extension });

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		// クライアントで出力するファイル名の日時情報はクライアントのタイムゾーンの現在時刻とする(マネージャのタイムゾーン時刻に補正しない)
		this.fileName = "EventReport-" + sdf.format(new Date()) + extension;
		fileDialog.setFileName(this.fileName);

		this.filePath = fileDialog.open();
		if (this.filePath != null && !"".equals(this.filePath.trim())) {
			// Get specified new filename on RCP
			if(! ClusterControlPlugin.isRAP()){
				this.fileName = fileDialog.getFileName();
			}
			// Validate
			super.okPressed();
		}
	}

	protected boolean output() {
		boolean flag = false;
		// Write event file
		File file = new File( this.filePath );
		FileOutputStream fOut = null;
		try {
			if (!file.createNewFile()) {
				m_log.warn("file is already exist.");
			}

			fOut = new FileOutputStream(file);

			DataHandler handler = null;

			Property condition = this.getInputData();

			// イベント一覧取得
			PropertyUtil.deletePropertyDefine(condition);
			EventFilterInfo filter = EventFilterPropertyUtil.property2dto(condition);
			MonitorEndpointWrapper wrapper = MonitorEndpointWrapper.getWrapper(this.managerName);
			String language = Locale.getDefault().getLanguage();
			handler = wrapper.downloadEventFile(this.facilityId, filter, this.fileName, language);
			handler.writeTo(fOut);

			// Start download file
			if( ClusterControlPlugin.isRAP() ){
				FileDownloader.openBrowser(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					this.filePath,
					this.fileName
				);
			}

			flag = true;
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (MonitorNotFound_Exception e) {
			MessageDialog.openError(
					null,
					Messages.getString("message"),
					Messages.getString("message.monitor.66") + ", "
							+ HinemosMessage.replace(e.getMessage()));
		} catch (HinemosUnknown_Exception e) {
			MessageDialog.openError(
					null,
					Messages.getString("message"),
					Messages.getString("message.monitor.66") + ", "
							+ HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("run() downloadEventFile, " + e.getMessage(), e);
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected")
							+ ", " + HinemosMessage.replace(e.getMessage()));
		} finally {
			try {
				if (fOut != null) {
					fOut.close();
				}
				if( ClusterControlPlugin.isRAP() ){
					// Clean up temporary file
					FileDownloader.cleanup( this.filePath );
				}
			} catch (IOException e) {
				m_log.warn(
						"output() fileOutputStream.close(), " + e.getMessage(),
						e);
			}
			try {
				MonitorEndpointWrapper wrapper = MonitorEndpointWrapper.getWrapper(this.managerName);
				wrapper.deleteEventFile(this.fileName);
			} catch (Exception e) {
				m_log.warn("output() deleteEventFile, " + e.getMessage(), e);
			}
		}
		return flag;
	}

	/**
	 * 出力先を返します。
	 *
	 * @return 出力先
	 */
	public String getFilePath() {
		return this.filePath;
	}
	public String getFileName() {
		return this.fileName;
	}

	/**
	 * 入力値を保持したプロパティを返します。<BR>
	 * プロパティオブジェクトのコピーを返します。
	 *
	 * @return プロパティ
	 *
	 * @see com.clustercontrol.util.PropertyUtil#copy(Property)
	 */
	public Property getInputData() {
		Property property = getOrInitFilterProperty();
		return PropertyUtil.copy( property );
	}

	/**
	 * 検索条件用プロパティ設定
	 * 前回ダイアログオープン時の情報を保持するために使用します。
	 */
	private void createPropertySheet( Tree table ){
		propertySheet = new PropertySheet(table);
		Property filterProperty = getOrInitFilterProperty();
		propertySheet.setInput(filterProperty);
		propertySheet.expandAll();
	}

	/**
	 * Reset property sheet
	 * The filter properties will be cleared
	 */
	private void resetPropertySheet(){
		Property filterProperty = initFilterProperty();
		propertySheet.setInput(filterProperty);
		propertySheet.expandAll();
	}

	/**
	 * Initialize a filter property
	 */
	private Property initFilterProperty() {
		Property property = new GetEventReportProperty().getProperty();
		filterPropertyCache.put(RWT.getUISession(), property);
		return property;
	}

	/**
	 * Get the cached filter property if existed,
	 * or initialize one while not.
	 */
	private Property getOrInitFilterProperty() {
		Property property = filterPropertyCache.get(RWT.getUISession());
		if( null == property ){
			property = initFilterProperty();
		}
		return property;
	}
}
