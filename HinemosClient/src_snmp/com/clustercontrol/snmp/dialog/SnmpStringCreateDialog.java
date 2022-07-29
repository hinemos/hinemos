/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmp.dialog;

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
import org.openapitools.client.model.AddSnmpStringMonitorRequest;
import org.openapitools.client.model.ModifySnmpStringMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.SnmpCheckInfoRequest;
import org.openapitools.client.model.SnmpCheckInfoResponse;
import org.openapitools.client.model.MonitorStringValueInfoRequest;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.monitor.run.dialog.CommonMonitorStringDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.notify.bean.PriChangeFailSelectTypeConstant;
import com.clustercontrol.notify.bean.PriChangeJudgeSelectTypeConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * SNMP監視（文字列）作成・変更ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class SnmpStringCreateDialog extends CommonMonitorStringDialog {

	// ----- instance フィールド ----- //

	/** OID */
	private Text m_textOid = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public SnmpStringCreateDialog(Shell parent) {
		super(parent, null);
		this.priorityChangeJudgeSelect = PriChangeJudgeSelectTypeConstant.TYPE_PATTERN;
		this.priorityChangeFailSelect = PriChangeFailSelectTypeConstant.TYPE_GET;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param マネージャ名
	 * @param monitorId 変更する監視項目ID
	 * @param updateFlg 更新するか否か（true:変更、false:新規登録）
	 */
	public SnmpStringCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);

		this.managerName = managerName;
		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
		this.priorityChangeJudgeSelect = PriChangeJudgeSelectTypeConstant.TYPE_PATTERN;
		this.priorityChangeFailSelect = PriChangeFailSelectTypeConstant.TYPE_GET;
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

		super.customizeDialog(parent);

		// タイトル
		shell.setText(Messages.getString("dialog.snmp.create.modify"));

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
		 * OID
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "oid", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("oid") + " : ");
		// テキスト
		this.m_textOid = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "oid", m_textOid);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textOid.setLayoutData(gridData);
		this.m_textOid.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// 空白
		label = new Label(groupCheckRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

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

		// OIDが必須項目であることを明示
		if("".equals(this.m_textOid.getText())){
			this.m_textOid.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textOid.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor
	 *            設定値として用いる監視情報
	 */
	@Override
	protected void setInputData(MonitorInfoResponse monitor) {
		super.setInputData(monitor);

		this.inputData = monitor;

		// 監視条件 SNMP監視情報
		SnmpCheckInfoResponse snmpInfo = monitor.getSnmpCheckInfo();
		if(snmpInfo == null){
			snmpInfo = new SnmpCheckInfoResponse();
			snmpInfo.setConvertFlg(SnmpCheckInfoResponse.ConvertFlgEnum.DELTA);
		}
		if(snmpInfo != null){
			if (snmpInfo.getSnmpOid() != null) {
				this.m_textOid.setText(snmpInfo.getSnmpOid());
			}

			// OIDが必須項目であることを明示
			this.update();
		}

		// 文字列監視情報
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

		// 監視条件 SNMP監視情報
		SnmpCheckInfoResponse snmpInfo = new SnmpCheckInfoResponse();
		snmpInfo.setConvertFlg(SnmpCheckInfoResponse.ConvertFlgEnum.DELTA);

		if (this.m_textOid.getText() != null
				&& !"".equals((this.m_textOid.getText()).trim())) {

			snmpInfo.setSnmpOid(this.m_textOid.getText());
		}

		monitorInfo.setSnmpCheckInfo(snmpInfo);

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

		// 重要度の定義
		validateResult = m_stringValueInfo.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
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
					AddSnmpStringMonitorRequest info = new AddSnmpStringMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddSnmpStringMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));

					if (info.getSnmpCheckInfo() != null && this.inputData.getSnmpCheckInfo() != null) {
						info.getSnmpCheckInfo().setConvertFlg(
								SnmpCheckInfoRequest.ConvertFlgEnum.fromValue(
										this.inputData.getSnmpCheckInfo().getConvertFlg().getValue()));
					}
					if (info.getStringValueInfo() != null
							&& this.inputData.getStringValueInfo() != null) {
						for (int i = 0; i < info.getStringValueInfo().size(); i++) {
							info.getStringValueInfo().get(i).setPriority(MonitorStringValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getStringValueInfo().get(i).getPriority().getValue()));
						}
					}
					wrapper.addSnmpStringMonitor(info);
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
			} else{
				// 変更の場合
				try {
					ModifySnmpStringMonitorRequest info = new ModifySnmpStringMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifySnmpStringMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getSnmpCheckInfo() != null && this.inputData.getSnmpCheckInfo() != null) {
						info.getSnmpCheckInfo().setConvertFlg(
								SnmpCheckInfoRequest.ConvertFlgEnum.fromValue(
										this.inputData.getSnmpCheckInfo().getConvertFlg().getValue()));
					}
					if (info.getStringValueInfo() != null
							&& this.inputData.getStringValueInfo() != null) {
						for (int i = 0; i < info.getStringValueInfo().size(); i++) {
							info.getStringValueInfo().get(i).setPriority(MonitorStringValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getStringValueInfo().get(i).getPriority().getValue()));
						}
					}
					wrapper.modifySnmpStringMonitor(this.inputData.getMonitorId(), info);
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

		SnmpCheckInfoResponse snmpCheckInfo = new SnmpCheckInfoResponse();
		snmpCheckInfo.setConvertFlg(SnmpCheckInfoResponse.ConvertFlgEnum.DELTA);
		monitor.setSnmpCheckInfo(snmpCheckInfo);
	}
}
