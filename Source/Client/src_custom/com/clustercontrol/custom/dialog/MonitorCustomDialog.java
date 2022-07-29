/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.custom.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.AddCustomNumericMonitorRequest;
import org.openapitools.client.model.CustomCheckInfoRequest;
import org.openapitools.client.model.CustomCheckInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoRequest;
import org.openapitools.client.model.ModifyCustomNumericMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.TextWithParameterComposite;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.bean.ConvertValueMessage;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorIdInvalid;
import com.clustercontrol.monitor.run.dialog.CommonMonitorNumericDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.notify.bean.PriChangeFailSelectTypeConstant;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * カスタム監視の設定ダイアログクラス<br/>
 *
 * @version 4.0.0
 * @since 2.4.0
 */
public class MonitorCustomDialog extends CommonMonitorNumericDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( MonitorCustomDialog.class );

	// ----- instance フィールド ----- //
	/** タイムアウト用テキストボックス */
	private Text m_textTimeout = null;
	private Button checkSelected = null;	// checkbox(指定したノードでまとめてコマンド実行)
	private Text textNode = null;			// コマンド実行ノードのスコープ文字列を表示するテキストボックス
	private Button buttonNode = null;		// 実行種別ボタン(特定のノードでまとめてコマンド実行)
	private Button buttonAgentUser = null; //実効ユーザ種別（エージェント起動ユーザ）
	private Button buttonSpecifyUser = null; //実効ユーザ種別（ユーザを指定する）
	private Text textEffectiveUser = null;	// 実効ユーザを入力するテキストボックス
	private TextWithParameterComposite textCommand = null;		//  コマンド文字列を入力するテキストボックス

	/** 取得値の加工 */
	private Combo m_comboConvertValue = null;

	private String nodeFacilityId = null;	// コマンド実行ノードのファシリティID

	// command用タイムアウト時間（ミリ秒）
	public static final int TIMEOUT_SEC_COMMAND = 15000;
	/**
	 * コンストラクタ(作成時)<br/>
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public MonitorCustomDialog(Shell parent) {
		super(parent, null);
		this.priorityChangeFailSelect = PriChangeFailSelectTypeConstant.TYPE_GET;
	}

	/**
	 * コンストラクタ(変更時)<br/>
	 *
	 * @param parent 親となるシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param monitorId 変更対象となるコマンド監視の監視項目ID
	 * @param updateFlg 更新するか否か（true:変更、false:新規登録）
	 */
	public MonitorCustomDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);
		this.managerName = managerName;
		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
		this.priorityChangeFailSelect = PriChangeFailSelectTypeConstant.TYPE_GET;
	}

	/**
	 * コマンド監視の入力項目を構成する。<br/>
	 *
	 * @param parent 親となるコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		// Local Variables
		Label label = null;		// 変数として利用されるラベル
		GridData gridData = null;	// 変数として利用されるグリッドデータ

		// MAIN

		// 閾値の単位を設定
		item1 = Messages.getString("select.value");
		item2 = Messages.getString("select.value");

		super.customizeDialog(parent);

		// タイトルの設定
		shell.setText(Messages.getString("dialog.monitor.custom.edit"));

		// チェック設定のグループを設定
		Group groupCheckRule = new Group(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "checkrule", groupCheckRule);
		groupCheckRule.setText(Messages.getString("check.rule"));
		GridLayout layout = new GridLayout(15, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		groupCheckRule.setLayout(layout);

		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = BASIC_UNIT;
		groupCheckRule.setLayoutData(gridData);

		// checkboxの設定(指定したノード上でコマンド実行)
		this.checkSelected = new Button(groupCheckRule, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "selectedCheck", checkSelected);
		this.checkSelected.setText(Messages.getString("monitor.custom.type.selected"));
		this.checkSelected.setToolTipText(Messages.getString("monitor.custom.type.selected.tips"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 7;
		checkSelected.setLayoutData(gridData);
		checkSelected.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.getSource();
				if (button.getSelection()) {
					textNode.setEnabled(true);
					buttonNode.setEnabled(true);
				} else {
					textNode.setEnabled(false);
					buttonNode.setEnabled(false);
				}
				update();
			}
		});

		// ノードを指定するテキストボックスとボタンの設定
		this.textNode = new Text(groupCheckRule, SWT.BORDER | SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "nodetext", textNode);
		this.textNode.setText("");
		this.textNode.setMessage(Messages.getString("monitor.custom.node.selected"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 6;
		this.textNode.setLayoutData(gridData);
		this.textNode.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});;

		this.buttonNode = new Button(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "node", buttonNode);
		this.buttonNode.setText(Messages.getString("refer"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		this.buttonNode.setLayoutData(gridData);
		this.buttonNode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				String managerName = getMonitorBasicScope().getManagerListComposite().getText();
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell, managerName, getMonitorBasicScope().getOwnerRoleId(), false, false);
				dialog.setSelectNodeOnly(true);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse item = dialog.getSelectItem();
					FacilityInfoResponse info = item.getData();
					nodeFacilityId = info.getFacilityId();
					if (info.getFacilityType() == FacilityInfoResponse.FacilityTypeEnum.NODE) {
						textNode.setText(info.getFacilityName());
					} else {
						FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
						textNode.setText(path.getPath(item));
					}
				}
			}
		});

		// チェック設定のグループを設定
		Group groupEffectiveUser = new Group(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "effectiveuser", groupEffectiveUser);
		groupEffectiveUser.setText(Messages.getString("effective.user"));
		layout = new GridLayout(15, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		groupEffectiveUser.setLayout(layout);

		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 15;
		groupEffectiveUser.setLayoutData(gridData);

		this.buttonAgentUser = new Button(groupEffectiveUser, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "agentuser", buttonAgentUser);
		this.buttonAgentUser.setText(Messages.getString("agent.user"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = SMALL_UNIT;
		this.buttonAgentUser.setLayoutData(gridData);
		this.buttonAgentUser.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					buttonSpecifyUser.setSelection(false);
					textEffectiveUser.setEnabled(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		this.buttonSpecifyUser = new Button(groupEffectiveUser, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "specifyuser", buttonSpecifyUser);
		this.buttonSpecifyUser.setText(Messages.getString("specified.user"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = WIDTH_TEXT_SHORT;
		this.buttonSpecifyUser.setLayoutData(gridData);
		this.buttonSpecifyUser.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					buttonAgentUser.setSelection(false);
					textEffectiveUser.setEnabled(true);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		this.textEffectiveUser = new Text(groupEffectiveUser, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "effectiveuser", textEffectiveUser);
		this.textEffectiveUser.setText("");
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = HALF_UNIT - (SMALL_UNIT + WIDTH_TEXT_SHORT);
		this.textEffectiveUser.setLayoutData(gridData);
		this.textEffectiveUser.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "customcommand", label);
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		label.setText(Messages.getString("monitor.custom.command") + " : ");
		gridData.horizontalSpan = WIDTH_TITLE;
		label.setLayoutData(gridData);

		this.textCommand = new TextWithParameterComposite(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "commnad", textCommand);
		this.textCommand.setText("");
		String tooltipText = Messages.getString("monitor.custom.commandline.tips") + Messages.getString("replace.parameter.node");
		this.textCommand.setToolTipText(tooltipText);
		this.textCommand.setColor(new Color(parent.getDisplay(), new RGB(0, 0, 255)));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 11;
		this.textCommand.setLayoutData(gridData);
		this.textCommand.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});;

		/*
		 * タイムアウト
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "timeout", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("time.out") + " : ");

		// テキスト
		this.m_textTimeout = new Text(groupCheckRule, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "timeout", m_textTimeout);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
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
		WidgetTestUtil.setTestId(this, "millisec", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("milli.sec"));

		// 空白
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank", label);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 値取得の加工
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("convert.value") + " : ");
		// コンボボックス
		this.m_comboConvertValue = new Combo(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "convertvalue", m_comboConvertValue);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_SHORT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboConvertValue.setLayoutData(gridData);
		this.m_comboConvertValue.add(ConvertValueMessage.STRING_NO);
		this.m_comboConvertValue.add(ConvertValueMessage.STRING_DELTA);

		// ダイアログの構成を調整する
		this.adjustDialog();

		// 初期表示
		MonitorInfoResponse info = null;
		if (this.monitorId == null) {
			// 新規作成の場合
			info = new MonitorInfoResponse();
			this.setInfoInitialValue(info);
		} else {
			// 変更の場合
			try {
				MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(managerName);
				info = wrapper.getMonitor(this.monitorId);
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				return;
			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("customizeDialog() getMonitor, " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				return;
			}
		}
		this.setInputData(info);
		update();
	}

	/**
	 * 更新処理
	 */
	@Override
	public void update(){
		super.update();

		// コマンド実行ノードが必須項目であることを明示
		if (checkSelected.getSelection() && "".equals(this.textNode.getText())) {
			this.textNode.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.textNode.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// コマンド実効ユーザが必須項目であることを明示
		if (buttonSpecifyUser.getSelection() && "".equals(this.textEffectiveUser.getText())) {
			this.textEffectiveUser.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.textEffectiveUser.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// コマンド文字列が必須項目であることを明示
		if ("".equals(this.textCommand.getText())) {
			this.textCommand.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.textCommand.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// タイムアウトが必須項目であることを明示
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
	 *			設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(MonitorInfoResponse monitor) {
		super.setInputData(monitor);

		this.inputData = monitor;

		// 監視条件コマンド監視情報
		CustomCheckInfoResponse customInfo = monitor.getCustomCheckInfo();
		if (customInfo == null) {
			customInfo = new CustomCheckInfoResponse();
			customInfo.setTimeout(TIMEOUT_SEC_COMMAND);
			this.checkSelected.setSelection(false);
			this.textNode.setEnabled(false);
			this.buttonNode.setEnabled(false);
			this.buttonAgentUser.setSelection(true);
			this.buttonSpecifyUser.setSelection(false);
			this.textEffectiveUser.setEnabled(false);
			this.m_comboConvertValue.setText(ConvertValueMessage.typeToString(ConvertValueConstant.TYPE_NO));
		} else {
			if (customInfo.getCommandExecTypeCode() == CustomCheckInfoResponse.CommandExecTypeCodeEnum.INDIVIDUAL) {
				this.checkSelected.setSelection(false);
			} else {
				this.checkSelected.setSelection(true);
				this.nodeFacilityId = customInfo.getSelectedFacilityId();

				String facilityPath = null;
				String managerName = this.getManagerName();
				try {
					RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
					facilityPath = wrapper.getFacilityPath(this.nodeFacilityId, null).getFacilityPath();
				} catch (InvalidRole e) {
					// アクセス権が付与されていないことを通知する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} catch (Exception e) {
					// 上記以外の例外
					m_log.warn("setInputData() getFacilityPath, " + HinemosMessage.replace(e.getMessage()), e);
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				}
				this.textNode.setText(facilityPath);
			}
			if (customInfo.getSpecifyUser().booleanValue()) {
				this.buttonAgentUser.setSelection(false);
				this.buttonSpecifyUser.setSelection(true);
				this.textEffectiveUser.setEnabled(true);
			} else {
				this.buttonAgentUser.setSelection(true);
				this.buttonSpecifyUser.setSelection(false);
				this.textEffectiveUser.setEnabled(false);
			}
			this.textEffectiveUser.setText(customInfo.getEffectiveUser());
			this.textCommand.setText(customInfo.getCommand());
			this.m_comboConvertValue.setText(ConvertValueMessage.codeToString(customInfo.getConvertFlg().toString()));
		}
		this.m_textTimeout.setText(Integer.toString(customInfo.getTimeout()));

		m_numericValueInfo.setInputData(monitor);
	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfoResponse createInputData() {
		// Local Variables
		CustomCheckInfoResponse customInfo = null;

		// MAIN
		super.createInputData();
		if(validateResult != null){
			return null;
		}

		// 監視条件コマンド監視情報
		customInfo = new CustomCheckInfoResponse();
		customInfo.setTimeout(TIMEOUT_SEC_COMMAND);
		customInfo.setConvertFlg(CustomCheckInfoResponse.ConvertFlgEnum.NONE);
		monitorInfo.setCustomCheckInfo(customInfo);

		// コマンド実行種別の格納
		if (! this.checkSelected.getSelection()) {
			customInfo.setCommandExecTypeCode(CustomCheckInfoResponse.CommandExecTypeCodeEnum.INDIVIDUAL);
		} else {
			customInfo.setCommandExecTypeCode(CustomCheckInfoResponse.CommandExecTypeCodeEnum.SELECTED);
			customInfo.setSelectedFacilityId(nodeFacilityId);
		}

		// 実効ユーザの格納
		if (this.buttonSpecifyUser.getSelection()) {
			customInfo.setSpecifyUser(true);
		} else {
			customInfo.setSpecifyUser(false);
		}
		customInfo.setEffectiveUser(this.textEffectiveUser.getText());

		// コマンド文字列の格納
		customInfo.setCommand(this.textCommand.getText());

		// タイムアウトの格納
		try {
			customInfo.setTimeout(Integer.parseInt(this.m_textTimeout.getText()));
		} catch (NumberFormatException e) {
			this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.custom.msg.timeout.invalid"));
			return null;
		}

		// 計算方法の格納
		if (!"".equals(this.m_comboConvertValue.getText().trim())) {
			int convertFlgType = ConvertValueMessage.stringToType(this.m_comboConvertValue.getText());
			if (convertFlgType == ConvertValueConstant.TYPE_NO) {
				customInfo.setConvertFlg(CustomCheckInfoResponse.ConvertFlgEnum.NONE);
			} else if (convertFlgType == ConvertValueConstant.TYPE_DELTA) {
				customInfo.setConvertFlg(CustomCheckInfoResponse.ConvertFlgEnum.DELTA);
			}
		}
		// 閾値判定の格納
		validateResult = m_numericValueInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			return null;
		}

		// 通知設定の格納
		validateResult = m_notifyInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			if (validateResult.getID() == null) {
				if (! displayQuestion(validateResult)) {	// 通知IDが選択されていない場合
					validateResult = null;
					return null;
				}
			} else {
				return null;	// アプリケーションが未入力の場合
			}
		}

		return monitorInfo;
	}

	/**
	 * 入力値をマネージャに反映する。<br/>
	 *
	 * @return 反映できた場合はtrue, その他はfalse
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		if (this.inputData != null) {
			String[] args = { this.inputData.getMonitorId(), getManagerName() };
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(getManagerName());
			if (!this.updateFlg) {
				// 新規作成の場合
				try {
					AddCustomNumericMonitorRequest info = new AddCustomNumericMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddCustomNumericMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getCustomCheckInfo() != null && this.inputData.getCustomCheckInfo() != null) {
						info.getCustomCheckInfo().setCommandExecTypeCode(
								CustomCheckInfoRequest.CommandExecTypeCodeEnum.fromValue(
										this.inputData.getCustomCheckInfo().getCommandExecTypeCode().getValue()));
						info.getCustomCheckInfo().setConvertFlg(
								CustomCheckInfoRequest.ConvertFlgEnum.fromValue(
										this.inputData.getCustomCheckInfo().getConvertFlg().getValue()));
					}
					if (info.getNumericValueInfo() != null
							&& this.inputData.getNumericValueInfo() != null) {
						for (int i = 0; i < info.getNumericValueInfo().size(); i++) {
							info.getNumericValueInfo().get(i).setPriority(MonitorNumericValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getNumericValueInfo().get(i).getPriority().getValue()));
						}
					}
					info.setPredictionMethod(AddCustomNumericMonitorRequest.PredictionMethodEnum.fromValue(
							this.inputData.getPredictionMethod().getValue()));
					wrapper.addCustomNumericMonitor(info);
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
					// 重複する監視項目IDが存在することを通知する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.monitor.53", args));
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
						// アクセス権が付与されていないことを通知する
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
					ModifyCustomNumericMonitorRequest info = new ModifyCustomNumericMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifyCustomNumericMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getCustomCheckInfo() != null && this.inputData.getCustomCheckInfo() != null) {
						info.getCustomCheckInfo().setCommandExecTypeCode(
								CustomCheckInfoRequest.CommandExecTypeCodeEnum.fromValue(
										this.inputData.getCustomCheckInfo().getCommandExecTypeCode().getValue()));
						info.getCustomCheckInfo().setConvertFlg(
								CustomCheckInfoRequest.ConvertFlgEnum.fromValue(
										this.inputData.getCustomCheckInfo().getConvertFlg().getValue()));
					}
					if (info.getNumericValueInfo() != null
							&& this.inputData.getNumericValueInfo() != null) {
						for (int i = 0; i < info.getNumericValueInfo().size(); i++) {
							info.getNumericValueInfo().get(i).setPriority(MonitorNumericValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getNumericValueInfo().get(i).getPriority().getValue()));
						}
					}
					info.setPredictionMethod(ModifyCustomNumericMonitorRequest.PredictionMethodEnum.fromValue(
							this.inputData.getPredictionMethod().getValue()));
					wrapper.modifyCustomNumericMonitor(this.inputData.getMonitorId(), info);
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.35", args));
					result = true;
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
						// アクセス権が付与されていないことを通知する
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
