/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.port.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.AddServiceportMonitorRequest;
import org.openapitools.client.model.ModifyServiceportMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.PortCheckInfoResponse;
import org.openapitools.client.model.PortCheckInfoResponse.ServiceIdEnum;
import org.openapitools.client.model.MonitorNumericValueInfoRequest;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorIdInvalid;
import com.clustercontrol.monitor.run.dialog.CommonMonitorNumericDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.port.bean.PortRunCountConstant;
import com.clustercontrol.port.bean.PortRunIntervalConstant;
import com.clustercontrol.port.bean.ProtocolConstant;
import com.clustercontrol.port.bean.ProtocolMessage;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * サービス･ポート監視作成・変更ダイアログクラスです。
 *
 * @version 4.0.0
 * @since 2.4.0
 */
public class PortCreateDialog extends CommonMonitorNumericDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( PortCreateDialog.class );

	// ----- instance フィールド ----- //
	/** タイムアウト用テキストボックス */
	private Text m_textTimeout = null;

	/** TCP接続のみ */
	private Button m_radioTCP = null;

	/** サービス */
	private Button m_radioService = null;

	/** サービスプロトコル */
	private Combo m_comboService = null;

	/** ポート番号 */
	private Text m_textPortNo = null;

	/** 実行回数 */
	private Text m_textRunCount = null;

	/** 実行間隔（秒） */
	private Text m_textRunInterval = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public PortCreateDialog(Shell parent) {
		super(parent, null);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param notifyId
	 *            変更する通知ID
	 * @param updateFlg
	 *            更新するか否か（true:変更、false:新規登録）
	 *
	 */
	public PortCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);

		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		// 項目名に「応答時間（ミリ秒）」を設定
		item1 = Messages.getString("response.time.milli.sec");
		item2 = Messages.getString("response.time.milli.sec");

		super.customizeDialog(parent);
		m_numericValueInfo.setInfoWarnText("0", "1000", "1000", "3000");

		// タイトル
		shell.setText(Messages.getString("dialog.port.create.modify"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		/*
		 * チェック設定グループ（条件グループの子グループ）
		 */
		Group groupCheckRule = new Group(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "checkrule", groupCheckRule);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		groupCheckRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCheckRule.setLayoutData(gridData);
		groupCheckRule.setText(Messages.getString("check.rule"));

		/*
		 * 監視プロトコル
		 */
		// TCP接続のみ
		this.m_radioTCP = new Button(groupCheckRule, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "tcp", m_radioTCP);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioTCP.setLayoutData(gridData);
		this.m_radioTCP.setText(Messages.getString("tcp.connect.only"));
		this.m_radioTCP.setSelection(true);

		// サービス
		this.m_radioService = new Button(groupCheckRule, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "service", m_radioService);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioService.setLayoutData(gridData);
		this.m_radioService.setText(Messages.getString("service.protocol"));
		this.m_radioService.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabledComboService(m_radioService.getSelection());
			}
		});

		//コンボボックス
		this.m_comboService = new Combo(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "service", m_comboService);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboService.setLayoutData(gridData);
		this.m_comboService.add(ProtocolMessage.STRING_PROTOCOL_FTP);
		this.m_comboService.add(ProtocolMessage.STRING_PROTOCOL_SMTP);
		this.m_comboService.add(ProtocolMessage.STRING_PROTOCOL_SMTPS);
		this.m_comboService.add(ProtocolMessage.STRING_PROTOCOL_POP3);
		this.m_comboService.add(ProtocolMessage.STRING_PROTOCOL_POP3S);
		this.m_comboService.add(ProtocolMessage.STRING_PROTOCOL_IMAP);
		this.m_comboService.add(ProtocolMessage.STRING_PROTOCOL_IMAPS);
		this.m_comboService.add(ProtocolMessage.STRING_PROTOCOL_NTP);
		this.m_comboService.add(ProtocolMessage.STRING_PROTOCOL_DNS);
		//this.m_comboService.setEnabled(false);

		//空白
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * ポート番号
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "portnumber", label);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("port.number") + " : ");
		// テキスト
		this.m_textPortNo = new Text(groupCheckRule, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "protno", m_textPortNo);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textPortNo.setLayoutData(gridData);
		this.m_textPortNo.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 実行回数
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "labelruncount", label);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("trial.run.count") + " : ");

		// テキスト
		this.m_textRunCount = new Text(groupCheckRule, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "textruncount", m_textRunCount);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textRunCount.setLayoutData(gridData);
		this.m_textRunCount.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 単位
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "labelcount", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("count"));

		/*
		 * 実行間隔
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "trialruninterval", label);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("trial.run.interval") + " : ");

		// テキスト
		this.m_textRunInterval = new Text(groupCheckRule, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "runinterval", m_textRunInterval);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textRunInterval.setLayoutData(gridData);
		this.m_textRunInterval.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 単位
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "labelmillisec1", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("milli.sec"));

		/*
		 * タイムアウト
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "labeltimeout", label);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("time.out") + " : ");

		// テキスト
		this.m_textTimeout = new Text(groupCheckRule, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "timeout", m_textTimeout);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textTimeout.setLayoutData(gridData);
		this.m_textTimeout.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ラベル（単位）
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "labelmillisec2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("milli.sec"));

		// 収集値表示名のデフォルト値を設定
		this.itemName.setText(Messages.getString("response.time"));

		// 収集値単位のデフォルト値を設定
		this.measure.setText(Messages.getString("time.msec"));

		// ダイアログを調整
		this.adjustDialog();

		// 初期表示
		MonitorInfoResponse info = null;
		if(this.monitorId == null){
			// 作成の場合
			info = new MonitorInfoResponse();
			this.setInfoInitialValue(info);
		} else {
			// 変更の場合、情報取得
			try {
				MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(managerName);
				info = wrapper.getMonitor(monitorId);
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				throw new InternalError(e.getMessage());
			} catch (Exception e) {
				// Managerとの通信で予期せぬ内部エラーが発生したことを通知する
				m_log.warn("customizeDialog(), " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				throw new InternalError(e.getMessage());
			}
		}
		this.setInputData(info);

	}

	protected void setEnabledComboService(boolean enable) {
		this.m_comboService.setEnabled(enable);
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		super.update();

		// 必須項目を明示
		if(this.m_textPortNo.getEnabled() && "".equals(this.m_textPortNo.getText())){
			this.m_textPortNo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textPortNo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_textTimeout.getEnabled() && "".equals(this.m_textTimeout.getText())){
			this.m_textTimeout.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textTimeout.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor
	 *            設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(MonitorInfoResponse monitor) {
		super.setInputData(monitor);

		this.inputData = monitor;

		// 監視条件 port監視情報
		PortCheckInfoResponse portInfo = monitor.getPortCheckInfo();
		if (portInfo.getServiceId() == null || portInfo.getServiceId().equals(ServiceIdEnum.TCP)) {
			this.m_radioTCP.setSelection(true);
			this.m_radioService.setSelection(false);
			setEnabledComboService(m_radioService.getSelection());
		} else {
			this.m_radioTCP.setSelection(false);
			this.m_radioService.setSelection(true);
			setEnabledComboService(m_radioService.getSelection());
			this.m_comboService.setText(ProtocolMessage.typeToString(portInfo.getServiceId()));
		}
		if (portInfo.getPortNo() != null) {
			this.m_textPortNo.setText(portInfo.getPortNo().toString());
		}
		if (portInfo.getRunCount() != null) {
			this.m_textRunCount.setText(portInfo.getRunCount().toString());
		}
		if (portInfo.getRunInterval() != null) {
			this.m_textRunInterval.setText(portInfo.getRunInterval().toString());
		}
		// タイムアウト
		if (portInfo.getTimeout() != null) {
			this.m_textTimeout.setText(portInfo.getTimeout().toString());
		}

		m_numericValueInfo.setInputData(monitor);

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

		// 監視条件 port監視情報
		PortCheckInfoResponse portInfo = new PortCheckInfoResponse();

		/** 監視サービスの選択 */
		if (this.m_radioTCP.getSelection()) {
			portInfo.setServiceId(ServiceIdEnum.TCP);
		} else if (this.m_radioService.getSelection()) {/* サービスプロトコル選択時の処理 */
			if (this.m_comboService.getText() != null && !"".equals((this.m_comboService.getText()).trim())) {
				portInfo.setServiceId(ProtocolMessage.stringToType(this.m_comboService.getText()));
			}
		}
		/** ポート番号 */
		if (this.m_textPortNo.getText() != null && !"".equals((this.m_textPortNo.getText()).trim())) {
			try{
				Integer portNo = Integer.valueOf(this.m_textPortNo.getText().trim());
				portInfo.setPortNo(portNo);
			}
			catch(NumberFormatException e){
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.port.8"));
				return null;
			}
		}
		/** 試行回数 */

		if (!"".equals((this.m_textRunCount.getText()).trim())) {

			try{
				Integer runcount = Integer.parseInt(this.m_textRunCount.getText().trim());
				portInfo.setRunCount(runcount);
			}
			catch(NumberFormatException e){
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.port.1"));
				return null;
			}
		}
		/** 試行間隔 */

		if (!"".equals((this.m_textRunInterval.getText()).trim())) {

			try{
				Integer runinterval = Integer.parseInt(this.m_textRunInterval.getText().trim());
				portInfo.setRunInterval(runinterval);
			}
			catch(NumberFormatException e){
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.port.2"));
				return null;
			}
		}
		/** タイムアウト */
		if (!"".equals((this.m_textTimeout.getText()).trim())) {

			try{
				Integer timeout = Integer.parseInt(this.m_textTimeout.getText().trim());
				portInfo.setTimeout(timeout);
			}
			catch(NumberFormatException e){
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.port.3"));
				return null;
			}
		}
		monitorInfo.setPortCheckInfo(portInfo);


		// 結果判定の定義
		validateResult = m_numericValueInfo.createInputData(monitorInfo);
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
					AddServiceportMonitorRequest info = new AddServiceportMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddServiceportMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getPortCheckInfo() != null
							&& info.getPortCheckInfo().getServiceId() != null 
							&& this.inputData.getPortCheckInfo() != null) {
						info.getPortCheckInfo()
								.setServiceId(org.openapitools.client.model.PortCheckInfoRequest.ServiceIdEnum
										.fromValue(this.inputData.getPortCheckInfo().getServiceId().getValue()));
					}
					if (info.getNumericValueInfo() != null
							&& this.inputData.getNumericValueInfo() != null) {
						for (int i = 0; i < info.getNumericValueInfo().size(); i++) {
							info.getNumericValueInfo().get(i).setPriority(MonitorNumericValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getNumericValueInfo().get(i).getPriority().getValue()));
						}
					}
					info.setPredictionMethod(AddServiceportMonitorRequest.PredictionMethodEnum.fromValue(
							this.inputData.getPredictionMethod().getValue()));
					wrapper.addServiceportMonitor(info);
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.33", args));
					result = true;
				} catch (MonitorIdInvalid e) {
					// 監視項目IDが不適切な場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.monitor.97", args));
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
					ModifyServiceportMonitorRequest info = new ModifyServiceportMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifyServiceportMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getPortCheckInfo() != null
							&& info.getPortCheckInfo().getServiceId() != null 
							&& this.inputData.getPortCheckInfo() != null) {
						info.getPortCheckInfo()
								.setServiceId(org.openapitools.client.model.PortCheckInfoRequest.ServiceIdEnum
										.fromValue(this.inputData.getPortCheckInfo().getServiceId().getValue()));
					}
					if (info.getNumericValueInfo() != null
							&& this.inputData.getNumericValueInfo() != null) {
						for (int i = 0; i < info.getNumericValueInfo().size(); i++) {
							info.getNumericValueInfo().get(i).setPriority(MonitorNumericValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getNumericValueInfo().get(i).getPriority().getValue()));
						}
					}
					info.setPredictionMethod(ModifyServiceportMonitorRequest.PredictionMethodEnum.fromValue(
							this.inputData.getPredictionMethod().getValue()));
					wrapper.modifyServiceportMonitor(this.inputData.getMonitorId(), info);
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

	/**
	 * MonitorInfoに初期値を設定します
	 *
	 * @see com.clustercontrol.dialog.CommonMonitorDialog#setInfoInitialValue()
	 */
	@Override
	protected void setInfoInitialValue(MonitorInfoResponse monitor) {

		super.setInfoInitialValue(monitor);

		PortCheckInfoResponse portCheckInfo = new PortCheckInfoResponse();
		// ポート番号
		portCheckInfo.setPortNo(ProtocolConstant.DEFAULT_PORT_NUM_PROTOCOL_TCP);
		// リトライ回数（回）
		portCheckInfo.setRunCount(PortRunCountConstant.TYPE_COUNT_01);
		// リトライ間隔（ミリ秒）
		portCheckInfo.setRunInterval(PortRunIntervalConstant.TYPE_SEC_01);
		// タイムアウト（ミリ秒）
		portCheckInfo.setTimeout(TIMEOUT_SEC);
		monitor.setPortCheckInfo(portCheckInfo);
	}

}
