/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.run.dialog.CommonMonitorStringDialog;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorDuplicate_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.CustomTrapCheckInfo;

/**
 * カスタムトラップ監視(文字列)の設定ダイアログクラス<br/>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class MonitorCustomTrapStringDialog extends CommonMonitorStringDialog {

	// ログ
	private static Log m_log = LogFactory.getLog(MonitorCustomTrapStringDialog.class);

	// ----- instance フィールド ----- //
	private Text textKeypattern = null; // キーパターンを表示するテキストボックス

	/**
	 * コンストラクタ(作成時)<br/>
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public MonitorCustomTrapStringDialog(Shell parent) {
		super(parent, null);
		logLineFlag = true;
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
	public MonitorCustomTrapStringDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);
		logLineFlag = true;
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

		// ダイアログの構成を調整する
		this.adjustDialog();

		// 初期表示
		MonitorInfo info = null;
		if (this.monitorId == null) {
			// 新規作成の場合
			info = new MonitorInfo();
			this.setInfoInitialValue(info);
		} else {
			// 変更の場合
			try {
				MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
				info = wrapper.getMonitor(this.monitorId);
			} catch (InvalidRole_Exception e) {
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
		// 必須項目チェック
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
	protected void setInputData(MonitorInfo monitor) {
		super.setInputData(monitor);

		this.inputData = monitor;

		// 監視条件カスタムトラップ監視情報

		CustomTrapCheckInfo customtrapInfo = monitor.getCustomTrapCheckInfo();

		if (null != customtrapInfo) {
			this.textKeypattern.setText(customtrapInfo.getTargetKey());
		}

		m_stringValueInfo.setInputData(monitor);
	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfo createInputData() {
		// Local Variables
		CustomTrapCheckInfo customtrapInfo = null;

		// MAIN
		super.createInputData();
		if (validateResult != null) {
			return null;
		}

		// カスタムトラップ監視設定
		monitorInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S);

		// 監視条件カスタムトラップ監視情報
		customtrapInfo = new CustomTrapCheckInfo();
		customtrapInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S);
		customtrapInfo.setConvertFlg(ConvertValueConstant.TYPE_NO);
		customtrapInfo.setMonitorId(monitorInfo.getMonitorId());
		customtrapInfo.setTargetKey(this.textKeypattern.getText());
		monitorInfo.setCustomTrapCheckInfo(customtrapInfo);

		// カスタムトラップ監視（文字列）固有情報を設定
		monitorInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S);

		// 結果判定の定義
		validateResult = m_stringValueInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			return null;
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

		MonitorInfo info = this.inputData;
		String managerName = this.getManagerName();
		if (info != null) {
			String[] args = { info.getMonitorId(), managerName };
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
			if (!this.updateFlg) {
				// 新規作成の場合
				try {
					result = wrapper.addMonitor(info);
					if (result) {
						// 登録が成功したことを通知する
						MessageDialog.openInformation(null, Messages.getString("successful"),
								Messages.getString("message.monitor.33", args));
					} else {
						// 登録が失敗したことを通知する
						MessageDialog.openError(null, Messages.getString("failed"),
								Messages.getString("message.monitor.34", args));
					}
				} catch (MonitorDuplicate_Exception e) {
					// 重複する監視項目IDが存在することを通知する
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.monitor.53", args));
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole_Exception) {
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
				String errMessage = "";
				try {
					result = wrapper.modifyMonitor(info);
				} catch (InvalidRole_Exception e) {
					// アクセス権が付与されていないことを通知する
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} catch (Exception e) {
					errMessage = ", " + HinemosMessage.replace(e.getMessage());
				}

				if (result) {
					// 更新が成功したことを通知する
					MessageDialog.openInformation(null, Messages.getString("successful"),
							Messages.getString("message.monitor.35", args));
				} else {
					// 更新が失敗したことを通知する
					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("message.monitor.36", args) + errMessage);
				}
			}
		}

		return result;
	}
}
