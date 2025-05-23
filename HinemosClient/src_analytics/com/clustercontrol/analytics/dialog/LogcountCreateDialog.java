/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.dialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.openapitools.client.model.AddLogcountMonitorRequest;
import org.openapitools.client.model.GetMonitorStringTagListResponse;
import org.openapitools.client.model.LogcountCheckInfoResponse;
import org.openapitools.client.model.ModifyLogcountMonitorRequest;
import org.openapitools.client.model.MonitorInfoBeanResponse;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorInfoResponseP1;
import org.openapitools.client.model.MonitorNumericValueInfoRequest;

import com.clustercontrol.analytics.util.AnalyticsUtil;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorIdInvalid;
import com.clustercontrol.monitor.run.dialog.CommonMonitorNumericDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ログ件数監視作成・変更ダイアログクラスです。
 *
 * @version 6.1.0
 */
public class LogcountCreateDialog extends CommonMonitorNumericDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( LogcountCreateDialog.class );

	// ----- instance フィールド ----- //
	/** 対象監視設定ID */
	private Combo m_comboTargetMonitorId = null;
	
	/** キーワード */
	private Text m_textKeyword = null;
	
	/** ANDラジオボタン */
	private Button m_radioAnd = null;

	/** ORラジオボタン */
	private Button m_radioOr = null;
	
	/** カウント方法：全てラジオボタン */
	private Button m_radioAll = null;

	/** カウント方法：タグで集計ラジオボタン */
	private Button m_radioTag = null;

	/** カウント方法：タグコンボボックス */
	private Combo m_comboTag = null;

	/** 対象監視設定マップ（監視設定ラベル, 監視設定) */
	private Map<String, MonitorInfoResponseP1> m_targetMonitorInfoMap = new HashMap<>();

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public LogcountCreateDialog(Shell parent) {
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
	public LogcountCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);
		this.managerName = managerName;
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
		item1 = Messages.getString("response.logcount.range");
		item2 = Messages.getString("response.logcount.range");

		super.customizeDialog(parent);
		m_numericValueInfo.setInfoWarnText("0", "100", "100", "200");

		// タイトル
		shell.setText(Messages.getString("dialog.logcount.create.modify"));

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
		 * チェック設定
		 */
		// 対象監視設定ID（ラベル）
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "monitor.id", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.id") + " : ");

		// 対象監視設定ID（コンボボックス）
		this.m_comboTargetMonitorId = new Combo(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "monitor.id", this.m_comboTargetMonitorId);
		gridData = new GridData();
		gridData.horizontalSpan = 22;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboTargetMonitorId.setLayoutData(gridData);
		this.m_comboTargetMonitorId.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// タグコンボを切り替える
				setComboTag();
				update();
			}
		});

		// キーワード（ラベル）
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "keyword", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("keyword") + " : ");

		// キーワード（テキスト）
		this.m_textKeyword = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, null, m_textKeyword);
		gridData = new GridData();
		gridData.horizontalSpan = 22;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textKeyword.setMessage(Messages.getString("pattern.placeholder.tag"));
		this.m_textKeyword.setLayoutData(gridData);
		this.m_textKeyword.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//dummy
		label = new Label(groupCheckRule, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		label.setLayoutData(gridData);

		// キーワード（Composite）
		Composite compKeyword = new Composite(groupCheckRule, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		compKeyword.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 20;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		compKeyword.setLayoutData(gridData);

		// AND（ラジオ）
		this.m_radioAnd = new Button(compKeyword, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "and", m_radioAnd);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioAnd.setLayoutData(gridData);
		this.m_radioAnd.setText(Messages.getString("and"));
		this.m_radioAnd.setSelection(true);

		// OR（ラジオ）
		this.m_radioOr = new Button(compKeyword, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "or", m_radioOr);
		gridData = new GridData();
		gridData.horizontalSpan = 11;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioOr.setLayoutData(gridData);
		this.m_radioOr.setText(Messages.getString("or"));

		// カウント方法（ラベル）
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "countMethod", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("count.method") + " : ");

		// カウント方法（Composite）
		Composite compTag = new Composite(groupCheckRule, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		compTag.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		compTag.setLayoutData(gridData);

		// すべて（ラジオ）
		this.m_radioAll = new Button(compTag, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "all", m_radioAll);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioAll.setLayoutData(gridData);
		this.m_radioAll.setText(Messages.getString("all"));
		this.m_radioAll.setSelection(true);

		// タグで集計（ラジオ）
		this.m_radioTag = new Button(compTag, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "aggregate", m_radioOr);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioTag.setLayoutData(gridData);
		this.m_radioTag.setText(Messages.getString("aggregate.tag"));
		this.m_radioTag.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_comboTag.setEnabled(m_radioTag.getSelection());
				update();
			}
		});

		// タグで集計（コンボボックス）
		this.m_comboTag = new Combo(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "aggregate", this.m_comboTag);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboTag.setLayoutData(gridData);
		this.m_comboTag.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		if(!updateFlg) {
			//マネージャを変更した場合
			this.getMonitorBasicScope().getManagerListComposite()
			.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// 監視設定IDコンボを切り替える
					setComboTargetMonitorId();
					setComboTag();
					update();
				}
			});
		}

		// 目的変数：監視設定IDコンボを切り替える
		m_monitorBasic.getButtonScope().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setComboTargetMonitorId();
				setComboTag();
				update();
			}
		});

		// 収集値単位のデフォルト値を設定
		this.measure.setText(Messages.getString("record"));

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

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		super.update();

		// 必須項目を明示
		// 監視項目ID
		if("".equals(this.m_comboTargetMonitorId.getText())){
			this.m_comboTargetMonitorId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboTargetMonitorId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		
		// タグで集計
		if(this.m_radioTag.getSelection() && "".equals(this.m_comboTag.getText())){
			this.m_comboTag.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboTag.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
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

		// コンボボックス設定
		setComboTargetMonitorId();

		// チェック項目
		LogcountCheckInfoResponse collectLogCountCheckInfo = monitor.getLogcountCheckInfo();
		if (collectLogCountCheckInfo != null) {
			if(collectLogCountCheckInfo.getTargetMonitorId() != null
				&& !collectLogCountCheckInfo.getTargetMonitorId().isEmpty()) {
				MonitorInfoResponseP1 targetMonitorInfo = null;
	
				// 対象監視設定取得
				try {
					MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(managerName);
					MonitorInfoResponse info = wrapper.getMonitor(collectLogCountCheckInfo.getTargetMonitorId());
					targetMonitorInfo = new MonitorInfoResponseP1();
					RestClientBeanUtil.convertBean(info, targetMonitorInfo);
					targetMonitorInfo.setMonitorType(MonitorInfoResponseP1.MonitorTypeEnum.fromValue(info.getMonitorType().getValue()));
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
				this.m_comboTargetMonitorId.setText(AnalyticsUtil.getMonitorIdLabel(targetMonitorInfo));
				setComboTag();
			}
			// キーワード
			if (collectLogCountCheckInfo.getKeyword() != null) {
				this.m_textKeyword.setText(collectLogCountCheckInfo.getKeyword());
			}

			// AND/OR
			if (collectLogCountCheckInfo.getIsAnd()) {
				this.m_radioAnd.setSelection(true);
				this.m_radioOr.setSelection(false);
			} else {
				this.m_radioAnd.setSelection(false);
				this.m_radioOr.setSelection(true);
			}
			if (collectLogCountCheckInfo.getTag() == null
					|| collectLogCountCheckInfo.getTag().isEmpty()) {
				this.m_comboTag.setEnabled(false);
				this.m_radioAll.setSelection(true);
				this.m_radioTag.setSelection(false);
			} else {
				this.m_comboTag.setEnabled(true);
				this.m_comboTag.setText(collectLogCountCheckInfo.getTag());
				this.m_radioAll.setSelection(false);
				this.m_radioTag.setSelection(true);
			}
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

		// 監視条件 監視情報
		LogcountCheckInfoResponse collectLogCountCheckInfo = new LogcountCheckInfoResponse();
		if (this.m_comboTargetMonitorId.getText() != null) {
			collectLogCountCheckInfo.setTargetMonitorId(getMonitorId(this.m_comboTargetMonitorId.getText()));
		}
		if (this.m_textKeyword.getText() != null) {
			collectLogCountCheckInfo.setKeyword(this.m_textKeyword.getText());
		}
		collectLogCountCheckInfo.setIsAnd(this.m_radioAnd.getSelection());
		if (this.m_radioAll.getSelection()) {
			collectLogCountCheckInfo.setTag(null);
		} else {
			if (this.m_comboTag.getText() == null || this.m_comboTag.getText().isEmpty()) {
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.monitor.94"));
				return null;
			}
			collectLogCountCheckInfo.setTag(this.m_comboTag.getText());
		}

		monitorInfo.setLogcountCheckInfo(collectLogCountCheckInfo);

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
					AddLogcountMonitorRequest info = new AddLogcountMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddLogcountMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getNumericValueInfo() != null
							&& this.inputData.getNumericValueInfo() != null) {
						for (int i = 0; i < info.getNumericValueInfo().size(); i++) {
							info.getNumericValueInfo().get(i).setPriority(MonitorNumericValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getNumericValueInfo().get(i).getPriority().getValue()));
						}
					}
					info.setPredictionMethod(AddLogcountMonitorRequest.PredictionMethodEnum.fromValue(
							this.inputData.getPredictionMethod().getValue()));
					wrapper.addLogcountMonitor(info);
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
					ModifyLogcountMonitorRequest info = new ModifyLogcountMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifyLogcountMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getNumericValueInfo() != null
							&& this.inputData.getNumericValueInfo() != null) {
						for (int i = 0; i < info.getNumericValueInfo().size(); i++) {
							info.getNumericValueInfo().get(i).setPriority(MonitorNumericValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getNumericValueInfo().get(i).getPriority().getValue()));
						}
					}
					info.setPredictionMethod(ModifyLogcountMonitorRequest.PredictionMethodEnum.fromValue(
							this.inputData.getPredictionMethod().getValue()));
					wrapper.modifyLogcountMonitor(this.inputData.getMonitorId(), info);
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

	private void setComboTargetMonitorId() {
		this.m_comboTargetMonitorId.removeAll();
		this.m_targetMonitorInfoMap.clear();
		if (m_monitorBasic.getFacilityId() == null || m_monitorBasic.getFacilityId().isEmpty()) {
			return;
		}
		try {
			MonitorsettingRestClientWrapper monitorSettingWrapper = MonitorsettingRestClientWrapper.getWrapper(this.getManagerName());
			List<MonitorInfoBeanResponse> targetMonitorInfoList = monitorSettingWrapper.getStringAndTrapMonitorInfoList(
					m_monitorBasic.getFacilityId(), this.m_monitorBasic.getOwnerRoleId());
			if (targetMonitorInfoList == null) {
				return;
			}
			for (MonitorInfoBeanResponse monitorBeanInfo : targetMonitorInfoList) {
				MonitorInfoResponseP1 monitorInfo = new MonitorInfoResponseP1();
				RestClientBeanUtil.convertBean(monitorBeanInfo, monitorInfo);
				// FIXME v.7.0不具合#5761
				// 上記対応のため、あえてクラウドログ監視は監視対象外としている
				if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CLOUD_LOG)) {
					continue;
				}
				switch (monitorBeanInfo.getMonitorType()) {
				case TRUTH:
					monitorInfo.setMonitorType(MonitorInfoResponseP1.MonitorTypeEnum.TRUTH);
					break;
				case NUMERIC:
					monitorInfo.setMonitorType(MonitorInfoResponseP1.MonitorTypeEnum.NUMERIC);
					break;
				case STRING:
					monitorInfo.setMonitorType(MonitorInfoResponseP1.MonitorTypeEnum.STRING);
					break;
				case TRAP:
					monitorInfo.setMonitorType(MonitorInfoResponseP1.MonitorTypeEnum.TRAP);
					break;
				case SCENARIO:
					monitorInfo.setMonitorType(MonitorInfoResponseP1.MonitorTypeEnum.SCENARIO);
					break;
				case BINARY:
					monitorInfo.setMonitorType(MonitorInfoResponseP1.MonitorTypeEnum.BINARY);
					break;
				}
				String targetMonitorIdLabel = AnalyticsUtil.getMonitorIdLabel(monitorInfo);
				m_targetMonitorInfoMap.put(targetMonitorIdLabel, monitorInfo);
				m_comboTargetMonitorId.add(targetMonitorIdLabel);
			}
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

	private void setComboTag() {
		this.m_comboTag.removeAll();
		if (this.m_comboTargetMonitorId.getText() == null 
				|| this.m_comboTargetMonitorId.getText().isEmpty()) {
			return;
		}
		try {
			String monitorId = getMonitorId(this.m_comboTargetMonitorId.getText());
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(this.getManagerName());
			
			List<GetMonitorStringTagListResponse> tagList = wrapper.getMonitorStringTagList(monitorId, this.m_monitorBasic.getOwnerRoleId());
			if (tagList != null && tagList.size() > 0) {
				List<String> aggregateList = tagList
					.stream()
					.map(response -> response.getKey())
					.collect(Collectors.toList());
				for (String item : aggregateList) {
					m_comboTag.add(item);
				}
			}
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

	/**
	 * MonitorInfoに初期値を設定します
	 *
	 * @see com.clustercontrol.dialog.CommonMonitorDialog#setInfoInitialValue()
	 */
	@Override
	protected void setInfoInitialValue(MonitorInfoResponse monitor) {

		super.setInfoInitialValue(monitor);

		// 対象監視設定を取得する
		
		LogcountCheckInfoResponse logcountCheckInfo = new LogcountCheckInfoResponse();
		logcountCheckInfo.setIsAnd(true);
		monitor.setLogcountCheckInfo(logcountCheckInfo);
	}


	/**
	 * 監視項目IDを取得する
	 * 
	 * @param monitorIdLabel 監視設定文字列
	 * @return 監視項目ID
	 */
	private String getMonitorId(String monitorIdLabel) {
		MonitorInfoResponseP1 monitorInfo = this.m_targetMonitorInfoMap.get(monitorIdLabel);
		if (monitorInfo == null) {
			return null;
		} else {
			return monitorInfo.getMonitorId();
		}
	}

	/**
	 * オーナーロールを設定する
	 * @return
	 */
	@Override
	public void updateOwnerRole(String ownerRoleId) {
		super.updateOwnerRole(ownerRoleId);
		setComboTargetMonitorId();
		setComboTag();
	}
}
