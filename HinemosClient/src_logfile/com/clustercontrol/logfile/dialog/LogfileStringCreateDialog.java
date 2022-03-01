/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logfile.dialog;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.AddLogfileMonitorRequest;
import org.openapitools.client.model.LogfileCheckInfoResponse;
import org.openapitools.client.model.ModifyLogfileMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorStringValueInfoRequest;
import org.openapitools.client.model.NotifyRelationInfoResponse;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.monitor.run.dialog.CommonMonitorStringDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.notify.bean.PriChangeJudgeSelectTypeConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ログファイル監視の設定ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class LogfileStringCreateDialog extends CommonMonitorStringDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( LogfileStringCreateDialog.class );

	// ----- instance フィールド ----- //
	/** ログファイル */
	//private Text m_textLogfile = null;

	/** ディレクトリ */
	private Text m_directory = null;

	/** ファイル名 */
	private Text m_fileName = null;

	/** エンコード */
	private Text m_fileEncoding = null;

	/** 改行コード */
	private Combo m_fileReturnCode = null;

	/** マネージャ名 */
	private String managerName = null;
	
	/** 先頭パターン */
	private Text txtPatternHead;
	
	/** 終端パターン */
	private Text txtPatternTail;

	/** 最大読み取りバイト長 */
	private Text txtMaxBytes;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public LogfileStringCreateDialog(Shell parent) {
		super(parent, null);
		logLineFlag = true;
		this.priorityChangeJudgeSelect = PriChangeJudgeSelectTypeConstant.TYPE_PATTERN;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param monitorId 変更する監視項目ID
	 * @param updateFlg 更新するか否か（true:変更、false:新規登録）
	 */
	public LogfileStringCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);

		logLineFlag = true;
		this.managerName = managerName;
		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
		this.priorityChangeJudgeSelect = PriChangeJudgeSelectTypeConstant.TYPE_PATTERN;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		super.customizeDialog(parent);

		// タイトル
		shell.setText(Messages.getString("dialog.logfile.create.modify"));


		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;
		
		// ファイル情報および区切り条件をタブにまとめる
		TabFolder tabFolder = new TabFolder(groupRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		tabFolder.setLayoutData(gridData);
		TabItem tabCheckRule = new TabItem(tabFolder, SWT.NONE);
		tabCheckRule.setText(Messages.getString("file.info"));
		TabItem tabDelimiter = new TabItem(tabFolder, SWT.NONE);
		tabDelimiter.setText(Messages.getString("file.delimiter"));
		
		/*
		 * チェック設定グループ（条件グループの子グループ）
		 */
		Composite compositeCheckRule = new Composite(tabFolder, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, compositeCheckRule);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = BASIC_UNIT;
		compositeCheckRule.setLayout(layout);
		
		//ディレクトリ
		// ラベル
		label = new Label(compositeCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "directory", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("directory") + " : ");
		// テキスト
		this.m_directory = new Text(compositeCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "directory", m_directory);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		String tooltipText = Messages.getString("monitor.logfile.directory.tool.tip") + Messages.getString("replace.parameter.node");
		this.m_directory.setToolTipText(tooltipText);
		this.m_directory.setLayoutData(gridData);
		this.m_directory.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		//ファイル名
		// ラベル
		label = new Label(compositeCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "filename", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("file.name") + "(" + Messages.getString("regex") + ") : ");
		// テキスト
		this.m_fileName = new Text(compositeCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "filename", m_fileName);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_fileName.setLayoutData(gridData);
		this.m_fileName.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		this.m_fileName.setToolTipText(Messages.getString("dialog.logfile.pattern"));

		//ファイルエンコーディング
		// ラベル
		label = new Label(compositeCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "fileencoding", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("file.encoding") + " : ");
		// テキスト
		this.m_fileEncoding = new Text(compositeCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "fileencoding", m_fileEncoding);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_fileEncoding.setLayoutData(gridData);
		this.m_fileEncoding.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		tabCheckRule.setControl(compositeCheckRule);
		
		
		// 区切り条件
		Composite delimiter = new Composite(tabFolder, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, delimiter);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = BASIC_UNIT;
		delimiter.setLayout(layout);
		
		//先頭パターン（正規表現）
		// ラベル
		Label lblPrePattern = new Label(delimiter, SWT.NONE);
		WidgetTestUtil.setTestId(this, "txtPatternHead", lblPrePattern);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG + WIDTH_TITLE_MIDDLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblPrePattern.setLayoutData(gridData);
		lblPrePattern.setText(Messages.getString("file.delimiter.pattern.head") + " : ");
		// テキスト
		txtPatternHead = new Text(delimiter, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "txtPatternHead", txtPatternHead);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT - WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		txtPatternHead.setLayoutData(gridData);
		txtPatternHead.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//終端パターン（正規表現）
		// ラベル
		Label lblSufPattern = new Label(delimiter, SWT.NONE);
		WidgetTestUtil.setTestId(this, "txtPatternTail", lblSufPattern);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG + WIDTH_TITLE_MIDDLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblSufPattern.setLayoutData(gridData);
		lblSufPattern.setText(Messages.getString("file.delimiter.pattern.tail") + " : ");
		// テキスト
		txtPatternTail = new Text(delimiter, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "txtPatternTail", txtPatternTail);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT - WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		txtPatternTail.setLayoutData(gridData);
		txtPatternTail.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		VerifyListener verifier = new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				String text = e.text;
				if (e.character == SWT.BS || e.character == SWT.DEL){
					return;
				}
				if (e.text.equals("")) {
					return;
				}
				if (!text.matches("^[0-9]+$")){
					e.doit = false;
				}
			}
		};
		
		//ファイル改行コード
		// ラベル
		label = new Label(delimiter, SWT.NONE);
		WidgetTestUtil.setTestId(this, "filereturncode", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG + WIDTH_TITLE_MIDDLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("file.returncode") + " : ");
		// コンボボックス
		this.m_fileReturnCode = new Combo(delimiter, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "filereturncode", m_fileReturnCode);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT - WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_fileReturnCode.setLayoutData(gridData);

		m_fileReturnCode.add("LF");
		m_fileReturnCode.add("CR");
		m_fileReturnCode.add("CRLF");
		m_fileReturnCode.setText("LF");//デフォルト
		
		tabDelimiter.setControl(delimiter);
		
		//最大読み取りバイト長（Byte)
		// ラベル
		Label lblReadByte = new Label(delimiter, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG + WIDTH_TITLE_MIDDLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblReadByte.setLayoutData(gridData);
		lblReadByte.setText(Messages.getString("file.delimiter.chars") + " : ");
		// テキスト
		txtMaxBytes = new Text(delimiter, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT - WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		txtMaxBytes.setLayoutData(gridData);
		txtMaxBytes.addVerifyListener(verifier);
		txtMaxBytes.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		tabFolder.setSelection(new TabItem[]{tabCheckRule});

		// dummy
		label = new Label(delimiter, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 監視間隔の設定を利用不可とする
		this.m_monitorRule.setRunIntervalEnabled(false);

		// ダイアログを調整
		this.adjustDialog();

		// 初期表示
		MonitorInfoResponse info = null;
		if(this.monitorId == null){
			// 作成の場合
			info = new MonitorInfoResponse();
			this.setInfoInitialValue(info);
			this.setInputData(info);
		} else {
			// 変更の場合、情報取得
			try {
				MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(managerName);
				info = wrapper.getMonitor(monitorId);
				this.setInputData(info);
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));

			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("customizeDialog() getMonitor, " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));

			}
		}
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		super.update();

		// 必須項目を明示
		Text[] texts = {m_directory, m_fileName, m_fileEncoding};
		for (Text text : texts) {
			if("".equals(text.getText())){
				text.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			}else{
				text.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}
		
		// 必須項目を明示
		if (!txtPatternHead.getText().isEmpty()) {
			txtPatternHead.setEnabled(true);
			txtPatternTail.setEnabled(false);
			m_fileReturnCode.setEnabled(false);
		} else if (!txtPatternTail.getText().isEmpty()) {
			txtPatternHead.setEnabled(false);
			txtPatternTail.setEnabled(true);
			m_fileReturnCode.setEnabled(false);
		} else {
			txtPatternHead.setEnabled(true);
			txtPatternTail.setEnabled(true);
			m_fileReturnCode.setEnabled(true);
		}
	}

	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor 設定値として用いる監視情報
	 */
	@Override
	protected void setInputData(MonitorInfoResponse monitor) {

		super.setInputData(monitor);
		this.inputData = monitor;

		List<NotifyRelationInfoResponse> c = monitor.getNotifyRelationList();
		if (m_log.isDebugEnabled()) {
			if (c != null ) {
				for (NotifyRelationInfoResponse i : c) {
					m_log.debug("notifyId : " + i.getNotifyId());
				}
			}
		}

		// 監視条件 ログファイル監視情報
		LogfileCheckInfoResponse logfileInfo = monitor.getLogfileCheckInfo();
		if(logfileInfo == null){
			logfileInfo = new LogfileCheckInfoResponse();
		}
		if(logfileInfo != null){
			if (logfileInfo.getDirectory() != null){
				this.m_directory.setText(logfileInfo.getDirectory());
			}
			if (logfileInfo.getFileName() != null){
				this.m_fileName.setText(logfileInfo.getFileName());
			}
			if (logfileInfo.getFileEncoding() != null){
				this.m_fileEncoding.setText(logfileInfo.getFileEncoding());
			} else {
				this.m_fileEncoding.setText("UTF-8");
			}
			if (logfileInfo.getFileReturnCode() != null){
				this.m_fileReturnCode.setText(logfileInfo.getFileReturnCode());
			}
			if (logfileInfo.getPatternHead() != null){
				this.txtPatternHead.setText(logfileInfo.getPatternHead());
			}
			if (logfileInfo.getPatternTail() != null){
				this.txtPatternTail.setText(logfileInfo.getPatternTail());
			}
			if (logfileInfo.getMaxBytes() != null){
				this.txtMaxBytes.setText(logfileInfo.getMaxBytes().toString());
			}
		}

		// ログファイル名が必須項目であることを明示
		this.update();

		m_stringValueInfo.setInputData(monitor);
	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfoResponse createInputData() {
		super.createInputData();
		if(validateResult != null){
			return null;
		}

		// 監視条件 ログファイル監視情報
		LogfileCheckInfoResponse logfileInfo = new LogfileCheckInfoResponse();

		//テキストボックスから文字列を取得
		if (this.m_directory.getText() != null
				&& !"".equals(this.m_directory.getText())) {
			logfileInfo.setDirectory(this.m_directory.getText());
		}
		if (this.m_fileName.getText() != null
				&& !"".equals(this.m_fileName.getText())) {
			logfileInfo.setFileName(this.m_fileName.getText());
		}
		if (this.m_fileEncoding.getText() != null
				&& !"".equals(this.m_fileEncoding.getText())) {
			logfileInfo.setFileEncoding(this.m_fileEncoding.getText());
		}
		if (this.m_fileReturnCode.getText() != null
				&& !"".equals(this.m_fileReturnCode.getText())) {
			logfileInfo.setFileReturnCode(this.m_fileReturnCode.getText());
		}
		
		logfileInfo.setPatternHead(this.txtPatternHead.getText());
		logfileInfo.setPatternTail(this.txtPatternTail.getText());
		try {
				Integer maxBytes = this.txtMaxBytes.getText() == null || this.txtMaxBytes.getText().isEmpty() ? null: Integer.valueOf(this.txtMaxBytes.getText());
				logfileInfo.setMaxBytes(maxBytes);
		} catch (NumberFormatException e) {
				this.setValidateResult(Messages.getString("message.hinemos.1"), 
								Messages.getString("message.logfile.6", new String[]{ Integer.toString(Integer.MAX_VALUE) }));
				return null;
		}
		
		monitorInfo.setLogfileCheckInfo(logfileInfo);

		// 結果判定の定義
		validateResult = m_stringValueInfo.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
		}

		// 通知関連情報とアプリケーションの設定
		validateResult = m_notifyInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			if(validateResult.getID() == null){	// 通知ID警告用出力
				if(!displayQuestion(validateResult)){
					validateResult = null;
					return null;
				}
			}
			else{	// アプリケーション未入力チェック
				return null;
			}
		}

		return monitorInfo;
	}

	/**
	 * 入力値をマネージャに登録します。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		if(this.inputData != null){
			String[] args = { this.inputData.getMonitorId(), getManagerName() };
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(getManagerName());
			if(!this.updateFlg){
				// 作成の場合
				try {
					AddLogfileMonitorRequest info = new AddLogfileMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddLogfileMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getStringValueInfo() != null
							&& this.inputData.getStringValueInfo() != null) {
						for (int i = 0; i < info.getStringValueInfo().size(); i++) {
							info.getStringValueInfo().get(i).setPriority(MonitorStringValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getStringValueInfo().get(i).getPriority().getValue()));
						}
					}
					wrapper.addLogfileMonitor(info);
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.33", args));
					result = true;
				} catch (MonitorDuplicate e) {
					// 監視項目IDが重複している場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.monitor.53", args));

				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
						// アクセス権なしの場合、エラーダイアログを表示する
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}

					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.34", args) + errMessage);
				}
			} else {
				// 変更の場合
				try {
					ModifyLogfileMonitorRequest info = new ModifyLogfileMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifyLogfileMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getStringValueInfo() != null
							&& this.inputData.getStringValueInfo() != null) {
						for (int i = 0; i < info.getStringValueInfo().size(); i++) {
							info.getStringValueInfo().get(i).setPriority(MonitorStringValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getStringValueInfo().get(i).getPriority().getValue()));
						}
					}
					wrapper.modifyLogfileMonitor(this.inputData.getMonitorId(), info);
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.35", args));
					result = true;
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
						// アクセス権なしの場合、エラーダイアログを表示する
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.36", args) + errMessage);
				}
			}
		}

		return result;
	}
}
