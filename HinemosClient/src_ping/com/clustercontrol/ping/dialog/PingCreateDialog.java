/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ping.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.AddPingMonitorRequest;
import org.openapitools.client.model.ModifyPingMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.PingCheckInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoRequest;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorIdInvalid;
import com.clustercontrol.monitor.run.dialog.CommonMonitorNumericDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.ping.bean.PingRunIntervalConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ping監視作成・変更ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class PingCreateDialog extends CommonMonitorNumericDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( PingCreateDialog.class );

	// ----- instance フィールド ----- //
	/** タイムアウト用テキストボックス */
	private Text m_textTimeout = null;

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
	public PingCreateDialog(Shell parent) {
		super(parent, null);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param monitorId 監視ID
	 * @param updateFlg 更新するか否か（true:変更、false:新規登録）
	 */
	public PingCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
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
		// 項目名に「パケット紛失(%)と応答時間（ミリ秒）」を設定
		item1 = Messages.getString("response.time.milli.sec");
		item2 = Messages.getString("ping.reach");

		// 数値監視共通コンポジットをping監視に合わせて変更する
		criterion1 = Messages.getString("less");
		criterion2 = Messages.getString("less");

		super.customizeDialog(parent);
		m_numericValueInfo.setInfoWarnText("1000", "1", "3000", "51");

		// タイトル
		shell.setText(Messages.getString("dialog.ping.create.modify"));

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
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = BASIC_UNIT;
		groupCheckRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCheckRule.setLayoutData(gridData);
		groupCheckRule.setText(Messages.getString("check.rule"));


		/*
		 * 実行回数
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "runcount", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("run.count") + " : ");

		// テキスト
		this.m_textRunCount= new Text(groupCheckRule, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "runcount", m_textRunCount);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE_LONG;
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
		WidgetTestUtil.setTestId(this, "count", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setText(Messages.getString("count"));
		label.setLayoutData(gridData);

		/*
		 * 実行間隔
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "runinterval", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("run.interval") + " : ");

		// テキスト
		this.m_textRunInterval= new Text(groupCheckRule, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "runinterval", m_textRunInterval);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE_LONG;
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
		WidgetTestUtil.setTestId(this, "millisec", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setText(Messages.getString("milli.sec"));
		label.setLayoutData(gridData);

		/*
		 * タイムアウト
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "timeout", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("time.out") + " : ");

		// テキスト
		this.m_textTimeout = new Text(groupCheckRule, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "timeout", m_textTimeout);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE_LONG;
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
		WidgetTestUtil.setTestId(this, "labelmillisec", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("milli.sec"));

		// 空白
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "responsetime", label);
		gridData = new GridData();
		gridData.horizontalSpan = 14;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

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
				MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(getManagerName());
				info = wrapper.getMonitor(this.monitorId);
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				throw new InternalError(e.getMessage());
			} catch (Exception e) {
				// 上記以外の例外
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


	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		super.update();

		// 必須項目を明示
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

		// 監視条件 ping監視情報
		PingCheckInfoResponse pingInfo = monitor.getPingCheckInfo();
		if (pingInfo.getRunCount() != null) {
			this.m_textRunCount.setText(pingInfo.getRunCount().toString());
		}
		if (pingInfo.getRunInterval() != null) {
			this.m_textRunInterval.setText(pingInfo.getRunInterval().toString());
		}
		if (pingInfo.getTimeout() != null) {
			this.m_textTimeout.setText(pingInfo.getTimeout().toString());
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

		// 監視条件 ping監視情報
		PingCheckInfoResponse pingInfo = new PingCheckInfoResponse();

		if (!"".equals((this.m_textRunCount.getText()).trim())) {

			try{
				Integer runcount = Integer.valueOf(this.m_textRunCount.getText().trim());
				pingInfo.setRunCount(runcount);
			}
			catch(NumberFormatException e){
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.ping.1"));
				return null;
			}
		}
		if (!"".equals((this.m_textRunInterval.getText()).trim())) {

			try{
				Integer runinterval = Integer.valueOf(this.m_textRunInterval.getText().trim());
				pingInfo.setRunInterval(runinterval);
			}
			catch(NumberFormatException e){
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.ping.2"));
				return null;
			}
		}
		if (!"".equals((this.m_textTimeout.getText()).trim())) {

			try{
				Integer timeout = Integer.parseInt(this.m_textTimeout.getText().trim());
				pingInfo.setTimeout(timeout);
			}
			catch(NumberFormatException e){
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.ping.3"));
				return null;
			}
		}
		monitorInfo.setPingCheckInfo(pingInfo);

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
					AddPingMonitorRequest info = new AddPingMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddPingMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getNumericValueInfo() != null
							&& this.inputData.getNumericValueInfo() != null) {
						for (int i = 0; i < info.getNumericValueInfo().size(); i++) {
							info.getNumericValueInfo().get(i).setPriority(MonitorNumericValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getNumericValueInfo().get(i).getPriority().getValue()));
						}
					}
					info.setPredictionMethod(AddPingMonitorRequest.PredictionMethodEnum.fromValue(
							this.inputData.getPredictionMethod().getValue()));
					wrapper.addPingMonitor(info);
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
			} else{
				// 変更の場合
				try {
					ModifyPingMonitorRequest info = new ModifyPingMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifyPingMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getNumericValueInfo() != null
							&& this.inputData.getNumericValueInfo() != null) {
						for (int i = 0; i < info.getNumericValueInfo().size(); i++) {
							info.getNumericValueInfo().get(i).setPriority(MonitorNumericValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getNumericValueInfo().get(i).getPriority().getValue()));
						}
					}
					info.setPredictionMethod(ModifyPingMonitorRequest.PredictionMethodEnum.fromValue(
							this.inputData.getPredictionMethod().getValue()));
					wrapper.modifyPingMonitor(this.inputData.getMonitorId(), info);
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

		PingCheckInfoResponse pingCheckInfo = new PingCheckInfoResponse();
		// 実行回数
		pingCheckInfo.setRunCount(RUNCOUNT_COUNT);
		// 実行間隔（秒）
		pingCheckInfo.setRunInterval(PingRunIntervalConstant.TYPE_SEC_01);
		// タイムアウト（ミリ秒）
		pingCheckInfo.setTimeout(TIMEOUT_SEC);
		monitor.setPingCheckInfo(pingCheckInfo);
	}

}
