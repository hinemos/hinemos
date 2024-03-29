/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.customtrap.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.AddCustomtrapNumericMonitorRequest;
import org.openapitools.client.model.CustomTrapCheckInfoRequest;
import org.openapitools.client.model.CustomTrapCheckInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoRequest;
import org.openapitools.client.model.ModifyCustomtrapNumericMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorIdInvalid;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.bean.ConvertValueMessage;
import com.clustercontrol.monitor.run.dialog.CommonMonitorNumericDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * カスタムトラップ監視の設定ダイアログクラス<br/>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class MonitorCustomTrapDialog extends CommonMonitorNumericDialog {

	// ログ
	private static Log m_log = LogFactory.getLog(MonitorCustomTrapDialog.class);

	// ----- instance フィールド ----- //
	private Text textKeypattern = null; // キーパターンを表示するテキストボックス
	/** 取得値の加工 */
	private Combo m_comboConvertValue = null;
	/**
	 * コンストラクタ(作成時)<br/>
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public MonitorCustomTrapDialog(Shell parent) {
		super(parent, null);
	}

	/**
	 * コンストラクタ(変更時)<br/>
	 *
	 * @param parent
	 *            親となるシェルオブジェクト
	 * @param managerName
	 *            マネージャ名
	 * @param monitorId
	 *            変更対象となるカスタムトラップ監視の監視項目ID
	 * @param updateFlg
	 *            更新するか否か（true:変更、false:新規登録）
	 */
	public MonitorCustomTrapDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);
		this.managerName = managerName;
		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
	}

	/**
	 * カスタムトラップ監視の入力項目を構成する。<br/>
	 *
	 * @param parent
	 *            親となるコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		// Local Variables
		Label label = null; // 変数として利用されるラベル
		GridData gridData = null; // 変数として利用されるグリッドデータ

		// 閾値の単位を設定
		item1 = Messages.getString("select.value");
		item2 = Messages.getString("select.value");
		
		// 未登録ノード スコープを表示する
		this.m_unregistered = true;

		super.customizeDialog(parent);

		// タイトルの設定
		shell.setText(Messages.getString("dialog.monitor.customtrap.edit"));

		// 監視間隔の設定を利用不可とする
		this.m_monitorRule.setRunIntervalEnabled(false);

		Composite compositeKey = new Composite(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "compositeKey", compositeKey);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 0;
		layout.marginHeight = BASIC_MARGIN;
		layout.numColumns = 15;
		compositeKey.setLayout(layout);

		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = BASIC_UNIT;
		compositeKey.setLayoutData(gridData);

		// キーパターン(ラベル)
		label = new Label(compositeKey, SWT.NONE);
		WidgetTestUtil.setTestId(this, "keypatterntest", label);
		gridData = new GridData();

		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.customtrap.target.key") + " : ");

		// キーパターン(テキスト)
		this.textKeypattern = new Text(compositeKey, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "textKeypattern", textKeypattern);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textKeypattern.setLayoutData(gridData);
		this.textKeypattern.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 値取得の加工 ラベル
		label = new Label(compositeKey, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("convert.value") + " : ");
		// 値取得の加工 コンボボックス
		this.m_comboConvertValue = new Combo(compositeKey, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "convertvalue", m_comboConvertValue);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
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
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				return;
			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("customizeDialog() getMonitor, " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openError(null, Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", "
								+ HinemosMessage.replace(e.getMessage()));
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
	public void update() {
		// 必須入力チェック
		super.update();

		// キーパターンが必須項目であることを明示
		if (this.textKeypattern.getEnabled() && "".equals(this.textKeypattern.getText())) {
			this.textKeypattern.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.textKeypattern.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
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

		// 監視条件カスタムトラップ監視情報

		CustomTrapCheckInfoResponse customtrapInfo = monitor.getCustomTrapCheckInfo();
		
		if(customtrapInfo == null){
			this.m_comboConvertValue.setText(ConvertValueMessage.typeToString(ConvertValueConstant.TYPE_NO));
		}else{
			this.textKeypattern.setText(customtrapInfo.getTargetKey());
			this.m_comboConvertValue.setText(ConvertValueMessage.codeToString(customtrapInfo.getConvertFlg().toString()));
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
		// Local Variables
		CustomTrapCheckInfoResponse customtrapInfo = null;

		// MAIN
		super.createInputData();
		if (validateResult != null) {
			return null;
		}

		// 監視条件コマンド監視情報
		customtrapInfo = new CustomTrapCheckInfoResponse();
		customtrapInfo.setConvertFlg(CustomTrapCheckInfoResponse.ConvertFlgEnum.NONE);
		customtrapInfo.setTargetKey(this.textKeypattern.getText());
		monitorInfo.setCustomTrapCheckInfo(customtrapInfo);

		// 閾値判定の格納
		validateResult = m_numericValueInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			return null;
		}
		
		// 計算方法の格納
		if (this.m_comboConvertValue.getText() != null
				&& !"".equals((this.m_comboConvertValue.getText()).trim())) {

			int convertFlgType = ConvertValueMessage.stringToType(this.m_comboConvertValue.getText());
			if (convertFlgType == ConvertValueConstant.TYPE_NO) {
				customtrapInfo.setConvertFlg(CustomTrapCheckInfoResponse.ConvertFlgEnum.NONE);
			} else if (convertFlgType == ConvertValueConstant.TYPE_DELTA) {
				customtrapInfo.setConvertFlg(CustomTrapCheckInfoResponse.ConvertFlgEnum.DELTA);
			}
		}
		
		// 通知設定の格納
		validateResult = m_notifyInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			if (validateResult.getID() == null) {
				if (!displayQuestion(validateResult)) { // 通知IDが選択されていない場合
					validateResult = null;
					return null;
				}
			} else {
				return null; // アプリケーションが未入力の場合
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
					AddCustomtrapNumericMonitorRequest info = new AddCustomtrapNumericMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddCustomtrapNumericMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getCustomTrapCheckInfo() != null && this.inputData.getCustomTrapCheckInfo() != null) {
						info.getCustomTrapCheckInfo().setConvertFlg(
								CustomTrapCheckInfoRequest.ConvertFlgEnum.fromValue(
										this.inputData.getCustomTrapCheckInfo().getConvertFlg().getValue()));
					}
					if (info.getNumericValueInfo() != null
							&& this.inputData.getNumericValueInfo() != null) {
						for (int i = 0; i < info.getNumericValueInfo().size(); i++) {
							info.getNumericValueInfo().get(i).setPriority(MonitorNumericValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getNumericValueInfo().get(i).getPriority().getValue()));
						}
					}
					info.setPredictionMethod(AddCustomtrapNumericMonitorRequest.PredictionMethodEnum.fromValue(
							this.inputData.getPredictionMethod().getValue()));
					wrapper.addCustomtrapNumericMonitor(info);
					MessageDialog.openInformation(null, Messages.getString("successful"),
							Messages.getString("message.monitor.33", args));
					result = true;
				} catch (MonitorIdInvalid e) {
					// 監視項目IDが不適切な場合、エラーダイアログを表示する
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.monitor.97", args));
				} catch (MonitorDuplicate e) {
					// 重複する監視項目IDが存在することを通知する
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.monitor.53", args));
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
						// アクセス権が付与されていないことを通知する
						MessageDialog.openInformation(null, Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}

					// 登録が失敗したことを通知する
					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("message.monitor.34", args) + errMessage);
				}
			} else {
				// 変更の場合
				try {
					ModifyCustomtrapNumericMonitorRequest info = new ModifyCustomtrapNumericMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifyCustomtrapNumericMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getCustomTrapCheckInfo() != null && this.inputData.getCustomTrapCheckInfo() != null) {
						info.getCustomTrapCheckInfo().setConvertFlg(
								CustomTrapCheckInfoRequest.ConvertFlgEnum.fromValue(
										this.inputData.getCustomTrapCheckInfo().getConvertFlg().getValue()));
					}
					if (info.getNumericValueInfo() != null
							&& this.inputData.getNumericValueInfo() != null) {
						for (int i = 0; i < info.getNumericValueInfo().size(); i++) {
							info.getNumericValueInfo().get(i).setPriority(MonitorNumericValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getNumericValueInfo().get(i).getPriority().getValue()));
						}
					}
					info.setPredictionMethod(ModifyCustomtrapNumericMonitorRequest.PredictionMethodEnum.fromValue(
							this.inputData.getPredictionMethod().getValue()));
					wrapper.modifyCustomtrapNumericMonitor(this.inputData.getMonitorId(), info);
					MessageDialog.openInformation(null, Messages.getString("successful"),
							Messages.getString("message.monitor.35", args));
					result = true;
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
						// アクセス権が付与されていないことを通知する
						MessageDialog.openInformation(null, Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}
					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("message.monitor.36", args) + errMessage);
				}
			}
		}

		return result;
	}
}
