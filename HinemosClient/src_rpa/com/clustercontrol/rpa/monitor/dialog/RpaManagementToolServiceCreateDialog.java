/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.monitor.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.AddRpaManagementToolMonitorRequest;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.ModifyRpaManagementToolMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorTruthValueInfoRequest;
import org.openapitools.client.model.RpaManagementToolServiceCheckInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.client.swt.SWT;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.monitor.run.dialog.CommonMonitorTruthDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;

/**
 * RPA管理ツール監視作成・変更ダイアログクラス<BR>
 *
 */
public class RpaManagementToolServiceCreateDialog extends CommonMonitorTruthDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( RpaManagementToolServiceCreateDialog.class );

	/** コネクションタイムアウト用テキストボックス */
	private Text m_textConnectionTimeout = null;

	/** リクエストタイムアウト用テキストボックス */
	private Text m_textRequestTimeout = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public RpaManagementToolServiceCreateDialog(Shell parent) {
		super(parent);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param monitorId
	 *            変更する監視項目ID
	 * @param updateFlg
	 *            更新するか否か（true:変更、false:新規登録）
	 *
	 */
	public RpaManagementToolServiceCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName, monitorId);
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
		
		// スコープ選択ダイアログをRPAスコープ向けに置き換える。
		Listener listener = m_monitorBasic.getButtonScope().getListeners(SWT.Selection)[0];
		m_monitorBasic.getButtonScope().removeListener(SWT.Selection, listener);
		m_monitorBasic.getButtonScope().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				String managerName = m_monitorBasic.getManagerListComposite().getText();
				// RPAスコープ向けのダイアログを表示する。
				ScopeTreeDialog dialog = new RpaScopeTreeDialog(shell, managerName, m_monitorBasic.getOwnerRoleId());
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse item = dialog.getSelectItem();
					FacilityInfoResponse info = item.getData();
					m_monitorBasic.setFacilityId(info.getFacilityId());
					FacilityPath path = new FacilityPath(
							ClusterControlPlugin.getDefault()
							.getSeparator());
					m_monitorBasic.getTextScope().setText(path.getPath(item));
				}
			}
		});

		
		// チェック設定グループ(条件グループの子グループ)
		Group groupCheckRule = new Group(groupRule, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);	
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 16;
		groupCheckRule.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCheckRule.setLayoutData(gridData);
		groupCheckRule.setText(Messages.getString("check.rule"));

		/*
		 * コネクションタイムアウト
		 */
		// ラベル
		Label label = new Label(groupCheckRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("CONNECTION_TIMEOUT") + " : ");

		// テキスト
		this.m_textConnectionTimeout = new Text(groupCheckRule, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textConnectionTimeout.setLayoutData(gridData);
		this.m_textConnectionTimeout.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ラベル（単位）
		label = new Label(groupCheckRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("milli.sec"));

		/*
		 * リクエストタイムアウト
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("REQUEST_TIMEOUT") + " : ");

		// テキスト
		this.m_textRequestTimeout = new Text(groupCheckRule, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "requesttimeout", m_textRequestTimeout);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textRequestTimeout.setLayoutData(gridData);
		this.m_textRequestTimeout.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ラベル（単位）
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "millisec", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("milli.sec"));

		
		// タイトル
		shell.setText(Messages.getString("dialog.rpaservice.create.modify"));

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
				info = wrapper.getMonitor(this.monitorId);
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
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));

			}
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

		m_truthValueInfo.setInputData(monitor);
		// コネクションタイムアウト
		m_textConnectionTimeout.setText(String.valueOf(monitor.getRpaManagementToolServiceCheckInfo().getConnectTimeout()));
		// リクエストタイムアウト
		m_textRequestTimeout.setText(String.valueOf(monitor.getRpaManagementToolServiceCheckInfo().getRequestTimeout()));

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

		validateResult = m_truthValueInfo.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
		}

		// RPA管理ツールサービス監視チェック設定
		RpaManagementToolServiceCheckInfoResponse checkInfo = new RpaManagementToolServiceCheckInfoResponse();
		if (m_textConnectionTimeout.getText() != null && !m_textConnectionTimeout.getText().isEmpty()) {
			checkInfo.setConnectTimeout(Integer.parseInt(m_textConnectionTimeout.getText()));
		}
		if (m_textRequestTimeout.getText() != null && !m_textRequestTimeout.getText().isEmpty()) {
			checkInfo.setRequestTimeout(Integer.parseInt(m_textRequestTimeout.getText()));
		}
		monitorInfo.setRpaManagementToolServiceCheckInfo(checkInfo);

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
	 * 更新処理
	 */
	@Override
	protected void update(){
		super.update();

		// 必須項目を明示
		if(this.m_textConnectionTimeout.getEnabled() && "".equals(this.m_textConnectionTimeout.getText())){
			this.m_textConnectionTimeout.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textConnectionTimeout.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_textRequestTimeout.getEnabled() && "".equals(this.m_textRequestTimeout.getText())){
			this.m_textRequestTimeout.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textRequestTimeout.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
	/**
	 * MonitorInfoに初期値を設定します
	 *
	 */
	@Override
	protected void setInfoInitialValue(MonitorInfoResponse monitor) {
		super.setInfoInitialValue(monitor);
		// コネクションタイムアウト、リクエストタイムアウトの初期値設定
		RpaManagementToolServiceCheckInfoResponse checkInfo = new RpaManagementToolServiceCheckInfoResponse();
		checkInfo.setConnectTimeout(10 * 1000);
		checkInfo.setRequestTimeout(10 * 1000);
		monitor.setRpaManagementToolServiceCheckInfo(checkInfo);
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
					AddRpaManagementToolMonitorRequest info = new AddRpaManagementToolMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddRpaManagementToolMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getTruthValueInfo() != null
							&& this.inputData.getTruthValueInfo() != null) {
						for (int i = 0; i < info.getTruthValueInfo().size(); i++) {
							info.getTruthValueInfo().get(i).setPriority(MonitorTruthValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getTruthValueInfo().get(i).getPriority().getValue()));
							info.getTruthValueInfo().get(i).setTruthValue(MonitorTruthValueInfoRequest.TruthValueEnum.fromValue(
									this.inputData.getTruthValueInfo().get(i).getTruthValue().getValue()));
						}
					}
					wrapper.addRpaManagementToolMonitor(info);
					result = true;
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.33", args));
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
					ModifyRpaManagementToolMonitorRequest info = new ModifyRpaManagementToolMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifyRpaManagementToolMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getTruthValueInfo() != null
							&& this.inputData.getTruthValueInfo() != null) {
						for (int i = 0; i < info.getTruthValueInfo().size(); i++) {
							info.getTruthValueInfo().get(i).setPriority(MonitorTruthValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getTruthValueInfo().get(i).getPriority().getValue()));
							info.getTruthValueInfo().get(i).setTruthValue(MonitorTruthValueInfoRequest.TruthValueEnum.fromValue(
									this.inputData.getTruthValueInfo().get(i).getTruthValue().getValue()));
						}
					}
					wrapper.modifyRpaManagementToolMonitorInfo(this.inputData.getMonitorId(), info);
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

	@Override
	public ICheckPublishRestClientWrapper getCheckPublishWrapper(String managerName) {
		// RpaRestEndpointsにはcheckPublishが存在しない
		// どのEndpointでも内容は同じなのでUtilityを使用する
		return UtilityRestClientWrapper.getWrapper(managerName);
	}

}
