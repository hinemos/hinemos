/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.AddPacketcaptureMonitorRequest;
import org.openapitools.client.model.BinaryPatternInfoRequest;
import org.openapitools.client.model.ModifyPacketcaptureMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.PacketCheckInfoResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * パケットキャプチャーの監視設定ダイアログクラス<BR>
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class MonitorPacketCaptureDialog extends CommonMonitorBinaryDialog {

	// ログ
	private static Log m_log = LogFactory.getLog(MonitorPacketCaptureDialog.class);

	// 画面入力項目.
	/** BPFフィルタ */
	private Text m_bpfFilter = null;
	/** プロミスキャスモード */
	private Button m_promiscuousMode = null;

	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public MonitorPacketCaptureDialog(Shell parent) {
		super(parent, null);
		super.logLineFlag = true;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName
	 *            マネージャ名
	 * @param monitorId
	 *            変更する監視項目ID
	 * @param updateFlg
	 *            更新するか否か（true:変更、false:新規登録）
	 */
	public MonitorPacketCaptureDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);

		super.logLineFlag = true;

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

		super.customizeDialog(parent);

		// タイトル
		shell.setText(Messages.getString("dialog.pcap.create.modify"));

		// 独自の画面項目の設定.
		this.setInputFields();

		// ダイアログを調整
		super.adjustDialog();

		// 非活性項目の設定.
		super.m_monitorRule.setRunIntervalEnabled(false);
		super.confirmCollectValid.removeSelectionListener(super.collectSelectedListner);

		// 初期表示値の設定.
		MonitorInfoResponse info = null;
		if (this.monitorId == null) {
			// 作成の場合
			info = new MonitorInfoResponse();
			super.setInfoInitialValue(info);
			super.m_monitorRule.setInputData(info);
			this.setInputData(info);
		} else {
			// 変更の場合、情報取得
			try {
				MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(super.managerName);
				info = wrapper.getMonitor(monitorId);
				this.setInputData(info);
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("customizeDialog() getMonitor, " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.hinemos.failure.unexpected") + ", "
								+ HinemosMessage.replace(e.getMessage()));
			}
		}
	}

	/**
	 * 画面項目の設定.
	 *
	 */
	private void setInputFields() {
		// 共通で利用する変数設定.
		Label label = null;
		GridData gridData = null;

		// チェック設定グループ（条件グループの子グループ）
		Group groupCapture = new Group(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "capture", groupCapture);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = BASIC_UNIT;
		groupCapture.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCapture.setLayoutData(gridData);
		groupCapture.setText(Messages.getString("packet.capture"));

		// BPFフィルタ
		// ラベル
		label = new Label(groupCapture, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("bpf.filter") + " : ");
		// テキスト
		this.m_bpfFilter = new Text(groupCapture, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_bpfFilter.setLayoutData(gridData);
		this.m_bpfFilter.setToolTipText(Messages.getString("tooltip.input.bpf.syntax"));

		// プロミスキャスモード
		// ラベル
		label = new Label(groupCapture, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("promiscuous.mode") + " : ");
		// チェックボックス
		this.m_promiscuousMode = new Button(groupCapture, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.m_promiscuousMode.setLayoutData(gridData);
	}

	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor
	 *            設定値として用いる監視情報
	 */
	@Override
	protected void setInputData(MonitorInfoResponse monitor) {

		// 親の入力値を設定.
		super.setInputData(monitor);

		// バイナリ監視情報の設定.
		PacketCheckInfoResponse packetCaptureInfo = monitor.getPacketCheckInfo();
		if (packetCaptureInfo == null) {
			packetCaptureInfo = new PacketCheckInfoResponse();
		}

		// 独自項目のセット.
		if (packetCaptureInfo != null) {
			if (packetCaptureInfo.getPromiscuousMode() != null && packetCaptureInfo.getPromiscuousMode()) {
				this.m_promiscuousMode.setSelection(true);
			} else {
				this.m_promiscuousMode.setSelection(false);
			}
			if (packetCaptureInfo.getFilterStr() != null) {
				this.m_bpfFilter.setText(packetCaptureInfo.getFilterStr());
			}
		}

		// 必須項目の赤反転処理.
		this.update();

		super.m_binaryPatternInfo.setInputData(monitor);

	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfoResponse createInputData() {
		super.createInputData();
		if (super.validateResult != null) {
			return null;
		}

		// パケットキャプチャ監視（バイナリ）固有情報を設定
		PacketCheckInfoResponse packetCaptureInfo = new PacketCheckInfoResponse();

		if (this.m_promiscuousMode.getSelection()) {
			packetCaptureInfo.setPromiscuousMode(true);
		} else {
			packetCaptureInfo.setPromiscuousMode(false);
		}
		if (this.m_bpfFilter.getText() != null && !this.m_bpfFilter.getText().isEmpty()) {
			packetCaptureInfo.setFilterStr(this.m_bpfFilter.getText());
		}

		// 設定した項目を監視情報としてセット.
		super.monitorInfo.setPacketCheckInfo(packetCaptureInfo);

		// 結果判定の定義
		super.validateResult = super.m_binaryPatternInfo.createInputData(super.monitorInfo);
		if (validateResult != null) {
			return null;
		}

		// 通知関連情報とアプリケーションの設定
		super.validateResult = super.m_notifyInfo.createInputData(super.monitorInfo);
		if (super.validateResult != null) {
			if (super.validateResult.getID() == null) { // 通知ID警告用出力
				if (!displayQuestion(super.validateResult)) {
					super.validateResult = null;
					return null;
				}
			} else { // アプリケーション未入力チェック
				return null;
			}
		}

		return super.monitorInfo;
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

		if (super.inputData != null) {
			String[] args = { this.inputData.getMonitorId(), getManagerName() };
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(getManagerName());
			if (!this.updateFlg) {
				// 作成の場合
				try {
					AddPacketcaptureMonitorRequest info = new AddPacketcaptureMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddPacketcaptureMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getBinaryPatternInfo() != null) {
						for (int i = 0; i < this.inputData.getBinaryPatternInfo().size(); i++) {
							info.getBinaryPatternInfo().get(i).setPriority(
									BinaryPatternInfoRequest.PriorityEnum.fromValue(
											this.inputData.getBinaryPatternInfo().get(i).getPriority().getValue()));
						}
					}
					wrapper.addPacketcaptureMonitor(info);
					MessageDialog.openInformation(null, Messages.getString("successful"),
							Messages.getString("message.monitor.33", args));
					result = true;
				} catch (MonitorDuplicate e) {
					// 監視項目IDが重複している場合、エラーダイアログを表示する
					MessageDialog.openInformation(null, Messages.getString("message"),
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

					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("message.monitor.34", args) + errMessage);
				}
			} else {
				// 変更の場合
				try {
					ModifyPacketcaptureMonitorRequest info = new ModifyPacketcaptureMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifyPacketcaptureMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getBinaryPatternInfo() != null) {
						for (int i = 0; i < this.inputData.getBinaryPatternInfo().size(); i++) {
							info.getBinaryPatternInfo().get(i).setPriority(
									BinaryPatternInfoRequest.PriorityEnum.fromValue(
											this.inputData.getBinaryPatternInfo().get(i).getPriority().getValue()));
						}
					}
					wrapper.modifyPacketcaptureMonitor(this.inputData.getMonitorId(), info);
					MessageDialog.openInformation(null, Messages.getString("successful"),
							Messages.getString("message.monitor.35", args));
					result = true;
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
						// アクセス権なしの場合、エラーダイアログを表示する
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
