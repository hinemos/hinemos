/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.systemlog.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.AddSystemlogMonitorRequest;
import org.openapitools.client.model.ModifySystemlogMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorStringValueInfoRequest;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.monitor.run.dialog.CommonMonitorStringDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.notify.bean.PriChangeFailSelectTypeConstant;
import com.clustercontrol.notify.bean.PriChangeJudgeSelectTypeConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * システムログ監視の設定ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class SystemlogStringCreateDialog extends CommonMonitorStringDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( SystemlogStringCreateDialog.class );

	// ----- instance フィールド ----- //

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public SystemlogStringCreateDialog(Shell parent) {
		super(parent, null);
		logLineFlag = true;
		this.priorityChangeJudgeSelect = PriChangeJudgeSelectTypeConstant.TYPE_PATTERN;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param monitorId 変更する監視項目ID
	 * @param updateFlg 更新するか否か（true:変更、false:新規登録）
	 */
	public SystemlogStringCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);

		logLineFlag = true;
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
		// 未登録ノード スコープを表示する
		this.m_unregistered = true;

		super.customizeDialog(parent);

		// タイトル
		shell.setText(Messages.getString("dialog.systemlog.create.modify"));

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
		} else {
			// 変更の場合、情報取得
			try {
				MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(managerName);
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
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor 設定値として用いる監視情報
	 */
	@Override
	protected void setInputData(MonitorInfoResponse monitor) {

		super.setInputData(monitor);
		this.inputData = monitor;

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
					AddSystemlogMonitorRequest info = new AddSystemlogMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddSystemlogMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getStringValueInfo() != null
							&& this.inputData.getStringValueInfo() != null) {
						for (int i = 0; i < info.getStringValueInfo().size(); i++) {
							info.getStringValueInfo().get(i).setPriority(MonitorStringValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getStringValueInfo().get(i).getPriority().getValue()));
						}
					}
					wrapper.addSystemlogMonitor(info);
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
						MessageDialog.openInformation(null, Messages.getString("message"),
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
					ModifySystemlogMonitorRequest info = new ModifySystemlogMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifySystemlogMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getStringValueInfo() != null
							&& this.inputData.getStringValueInfo() != null) {
						for (int i = 0; i < info.getStringValueInfo().size(); i++) {
							info.getStringValueInfo().get(i).setPriority(MonitorStringValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getStringValueInfo().get(i).getPriority().getValue()));
						}
					}
					wrapper.modifySystemlogMonitor(this.inputData.getMonitorId(), info);
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
