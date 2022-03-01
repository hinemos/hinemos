/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.dialog;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.AddIntegrationMonitorRequest;
import org.openapitools.client.model.IntegrationCheckInfoResponse;
import org.openapitools.client.model.IntegrationConditionInfoRequest;
import org.openapitools.client.model.IntegrationConditionInfoResponse;
import org.openapitools.client.model.ModifyIntegrationMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorTruthValueInfoRequest;

import com.clustercontrol.analytics.action.GetIntegrationConditionTableDefine;
import com.clustercontrol.analytics.composite.IntegrationConditionListComposite;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorIdInvalid;
import com.clustercontrol.monitor.run.dialog.CommonMonitorTruthDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * 収集値統合監視作成・変更ダイアログクラスです。
 *
 * @version 6.1.0
 */
public class IntegrationCreateDialog extends CommonMonitorTruthDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( IntegrationCreateDialog.class );

	// ----- instance フィールド ----- //
	/** タイムアウト */
	private Text m_textTimeout = null;
	/** 判定条件一覧 コンポジット。 */
	private IntegrationConditionListComposite m_compConditionList = null;
	/** 追加 ボタン。 */
	private Button m_buttonAdd = null;
	/** 変更 ボタン。 */
	private Button m_buttonModify = null;
	/** 削除 ボタン。 */
	private Button m_buttonDelete = null;
	/** コピー ボタン。 */
	private Button m_buttonCopy = null;
	/** 上へ ボタン。 */
	private Button m_buttonUp = null;
	/** 下へ ボタン。 */
	private Button m_buttonDown = null;
	/** 収集の順序を考慮しないチェックボックス */
	private Button m_checkNotOrder = null;
	/** OKメッセージ */
	private Text m_textMessageOk = null;
	/** NGメッセージ */
	private Text m_textMessageNg = null;

	/** オーナーロールID */
	private String m_ownerRoleId = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public IntegrationCreateDialog(Shell parent) {
		super(parent);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param managerName　マネージャ名
	 * @param monitorId 監視設定ID
	 * @param updateFlg 更新するか否か（true:変更、false:新規登録）
	 */
	public IntegrationCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
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

		// タイトル
		shell.setText(Messages.getString("dialog.integration.create.modify"));

		// スコープのラベル変更
		m_monitorBasic.setScopeLabel(Messages.getString("notification.scope"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		/*
		 * チェック設定グループ（条件グループの子グループ）
		 */
		Group groupCheckRule = new Group(groupRule, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		groupCheckRule.setLayout(layout);
		groupCheckRule.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, BASIC_UNIT, 1));
		groupCheckRule.setText(Messages.getString("check.rule"));

		// タイムアウト（ラベル）
		label = new Label(groupCheckRule, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 8, 1));
		label.setText(Messages.getString("time.out.minute") + " : ");

		// タイムアウト（テキスト）
		this.m_textTimeout = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT);
		this.m_textTimeout.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 10, 1));
		this.m_textTimeout.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//dummy
		label = new Label(groupCheckRule, SWT.LEFT);
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 12, 1));

		// 判定条件情報(Group)
		Group groupIntegrationCondition = new Group(groupCheckRule, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		groupIntegrationCondition.setLayout(layout);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false, BASIC_UNIT, 1);
		gridData.heightHint = 130;
		groupIntegrationCondition.setLayoutData(gridData);
		groupIntegrationCondition.setText(Messages.getString("judgment.condition"));

		/*
		 * 判定条件情報一覧
		 */
		this.m_compConditionList = new IntegrationConditionListComposite(
				groupIntegrationCondition, SWT.BORDER, GetIntegrationConditionTableDefine.get());
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 24, 1);
		this.m_compConditionList.setLayoutData(gridData);

		/*
		 * 操作ボタン
		 */
		Composite buttonComposite = new Composite(groupIntegrationCondition, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 6, 1));

		// 追加ボタン
		this.m_buttonAdd = this.createButton(buttonComposite, Messages.getString("add"));
		this.m_buttonAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				IntegrationConditionCreateDialog dialog = new IntegrationConditionCreateDialog(
						shell, getManagerName(), m_ownerRoleId, 
						m_monitorBasic.getFacilityId(), new IntegrationConditionInfoResponse());
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_compConditionList.getIntegrationConditionList().add(dialog.getInputData());
					m_compConditionList.update();
				}
			}
		});

		// 変更ボタン
		this.m_buttonModify = this.createButton(buttonComposite, Messages.getString("modify"));
		this.m_buttonModify.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = (Integer) ((ArrayList<?>) m_compConditionList.getTableViewer().getTable()
						.getSelection()[0].getData()).get(0) - 1;
				if (order >= 0) {
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					IntegrationConditionCreateDialog dialog = new IntegrationConditionCreateDialog(
							shell, getManagerName(), m_ownerRoleId, 
							m_monitorBasic.getFacilityId(), m_compConditionList.getIntegrationConditionList().get(order));
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_compConditionList.getIntegrationConditionList().remove(order);
						m_compConditionList.getIntegrationConditionList().add(order, dialog.getInputData());
						m_compConditionList.setSelection();
						m_compConditionList.update();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 削除ボタン
		this.m_buttonDelete = this.createButton(buttonComposite, Messages.getString("delete"));
		this.m_buttonDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = (Integer) ((ArrayList<?>) m_compConditionList.getTableViewer().getTable()
						.getSelection()[0].getData()).get(0) - 1;
				if (order >= 0) {
					String detail = m_compConditionList.getItem().getDescription();
					if (detail == null) {
						detail = "";
					}
					String[] args = { detail };
					if (MessageDialog.openConfirm(
							null,
							Messages.getString("confirmed"),
							Messages.getString("message.monitor.31", args))) {
						m_compConditionList.getIntegrationConditionList().remove(order);
						m_compConditionList.update();
					}
				}
				else{
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// コピーボタン
		this.m_buttonCopy = this.createButton(buttonComposite, Messages.getString("copy"));
		this.m_buttonCopy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = (Integer) ((ArrayList<?>) m_compConditionList.getTableViewer().getTable()
						.getSelection()[0].getData()).get(0) - 1;
				if (order >= 0) {
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					IntegrationConditionCreateDialog dialog = new IntegrationConditionCreateDialog(
							shell, getManagerName(), m_ownerRoleId, 
							m_monitorBasic.getFacilityId(), m_compConditionList.getIntegrationConditionList().get(order));
					if (dialog.open() == IDialogConstants.OK_ID) {
						Table table = m_compConditionList.getTableViewer().getTable();
						int selectIndex = table.getSelectionIndex();
						m_compConditionList.getIntegrationConditionList().add(dialog.getInputData());
						m_compConditionList.update();
						table.setSelection(selectIndex);
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 上へボタン
		this.m_buttonUp = this.createButton(buttonComposite, Messages.getString("up"));
		this.m_buttonUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = m_compConditionList.getTableViewer().getTable().getSelectionIndex();
				if (order >= 0) {
					m_compConditionList.up();
					m_compConditionList.update();
				}
				else{
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 下へボタン
		this.m_buttonDown = this.createButton(buttonComposite, Messages.getString("down"));
		this.m_buttonDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = m_compConditionList.getTableViewer().getTable().getSelectionIndex();
				if (order >= 0) {
					m_compConditionList.down();
					m_compConditionList.update();
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 収集の順序を考慮しない(チェックボックス)
		this.m_checkNotOrder = new Button(groupIntegrationCondition, SWT.CHECK);
		this.m_checkNotOrder.setText(Messages.getString("integration.monitor.notorder"));
		this.m_checkNotOrder.setLayoutData(new GridData(SWT.BEGINNING, GridData.CENTER, true, false, 30, 1));

		// メッセージのグループを設定
		Group groupMessage = new Group(groupCheckRule, SWT.NONE);
		groupMessage.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, BASIC_UNIT, 1));
		layout = new GridLayout(15, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		groupMessage.setLayout(layout);
		groupMessage.setText(Messages.getString("message"));

		// メッセージ_OK（ラベル）
		label = new Label(groupMessage, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		label.setText(Messages.getString("message.ok") + " : ");
		// メッセージ_OK（テキスト）
		this.m_textMessageOk = new Text(groupMessage, SWT.BORDER | SWT.LEFT);
		this.m_textMessageOk.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 13, 1));
		this.m_textMessageOk.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// メッセージ_NG（ラベル）
		label = new Label(groupMessage, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		label.setText(Messages.getString("message.ng") + " : ");
		// メッセージ_NG（テキスト）
		this.m_textMessageNg = new Text(groupMessage, SWT.BORDER | SWT.LEFT);
		this.m_textMessageNg.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 13, 1));
		this.m_textMessageNg.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// マネージャを変更した場合
		if(!updateFlg) {
			this.getMonitorBasicScope().getManagerListComposite()
			.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_compConditionList.setManagerName(getManagerName());
				}
			});
		}

		// 目的変数：監視設定IDコンボを切り替える
		m_monitorBasic.getButtonScope().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_compConditionList.setMonitorFacilityId(m_monitorBasic.getFacilityId());
			}
		});

		// ダイアログを調整
		this.adjustDialog();

		// マネージャ名設定
		m_compConditionList.setManagerName(getManagerName());

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

		// タイムアウト
		if("".equals(this.m_textTimeout.getText())){
			this.m_textTimeout.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textTimeout.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// OKメッセージ
		if("".equals(this.m_textMessageOk.getText())){
			this.m_textMessageOk.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textMessageOk.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// NGメッセージ
		if("".equals(this.m_textMessageNg.getText())){
			this.m_textMessageNg.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textMessageNg.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
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

		// 監視設定ファシリティID設定
		m_compConditionList.setMonitorFacilityId(m_monitorBasic.getFacilityId());

		// チェック項目
		IntegrationCheckInfoResponse checkInfo = monitor.getIntegrationCheckInfo();
		if (checkInfo == null) {
			checkInfo = new IntegrationCheckInfoResponse();
		}

		// タイムアウト
		if (checkInfo.getTimeout() != null) {
			this.m_textTimeout.setText(checkInfo.getTimeout().toString());
		}
		// メッセージ(OK)
		if (checkInfo.getMessageOk() != null) {
			this.m_textMessageOk.setText(checkInfo.getMessageOk());
		}
		// メッセージ(NG)
		if (checkInfo.getMessageNg() != null) {
			this.m_textMessageNg.setText(checkInfo.getMessageNg());
		}

		// 判定条件
		this.m_compConditionList.setInputData(checkInfo);

		// 収集の順序を考慮しない
		if (checkInfo.getNotOrder() != null) {
			this.m_checkNotOrder.setSelection(checkInfo.getNotOrder());
		}

		// 必須項目を明示
		this.update();
		m_truthValueInfo.setInputData(monitor);
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
		IntegrationCheckInfoResponse checkInfo = new IntegrationCheckInfoResponse();

		// タイムアウト
		if (this.m_textTimeout.getText() != null) {
			try {
				checkInfo.setTimeout(Integer.valueOf(this.m_textTimeout.getText()));
			} catch (Exception e) {
				// 入力の詳細チェックはマネージャで行う。
			}
		}

		// メッセージ（OK）
		if (this.m_textMessageOk.getText() != null) {
			checkInfo.setMessageOk(this.m_textMessageOk.getText());
		}
		// メッセージ（NG）
		if (this.m_textMessageNg.getText() != null) {
			checkInfo.setMessageNg(this.m_textMessageNg.getText());
		}
		monitorInfo.setIntegrationCheckInfo(checkInfo);

		// 判定条件を設定する
		m_compConditionList.createInputData(checkInfo);

		// 収集の順序を考慮しない
		checkInfo.setNotOrder(this.m_checkNotOrder.getSelection());

		// 結果判定の定義
		validateResult = m_truthValueInfo.createInputData(monitorInfo);
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
					AddIntegrationMonitorRequest info = new AddIntegrationMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddIntegrationMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getTruthValueInfo() != null
							&& this.inputData.getTruthValueInfo() != null) {
						for (int i = 0; i < info.getTruthValueInfo().size(); i++) {
							info.getTruthValueInfo().get(i).setPriority(MonitorTruthValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getTruthValueInfo().get(i).getPriority().getValue()));
							info.getTruthValueInfo().get(i).setTruthValue(MonitorTruthValueInfoRequest.TruthValueEnum.fromValue(
									this.inputData.getTruthValueInfo().get(i).getTruthValue().getValue()));
						}
					}
					if (this.inputData.getIntegrationCheckInfo() != null
							&& this.inputData.getIntegrationCheckInfo().getConditionList() != null) {
						for (int i = 0; i < this.inputData.getIntegrationCheckInfo().getConditionList().size(); i++) {
							info.getIntegrationCheckInfo().getConditionList().get(i).setTargetMonitorType(
									IntegrationConditionInfoRequest.TargetMonitorTypeEnum.fromValue(
											this.inputData.getIntegrationCheckInfo().getConditionList().get(i).getTargetMonitorType().getValue()));
						}
					}
					wrapper.addIntegrationMonitor(info);
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
					ModifyIntegrationMonitorRequest info = new ModifyIntegrationMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifyIntegrationMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getTruthValueInfo() != null
							&& this.inputData.getTruthValueInfo() != null) {
						for (int i = 0; i < info.getTruthValueInfo().size(); i++) {
							info.getTruthValueInfo().get(i).setPriority(MonitorTruthValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getTruthValueInfo().get(i).getPriority().getValue()));
							info.getTruthValueInfo().get(i).setTruthValue(MonitorTruthValueInfoRequest.TruthValueEnum.fromValue(
									this.inputData.getTruthValueInfo().get(i).getTruthValue().getValue()));
						}
					}
					if (this.inputData.getIntegrationCheckInfo() != null
							&& this.inputData.getIntegrationCheckInfo().getConditionList() != null) {
						for (int i = 0; i < this.inputData.getIntegrationCheckInfo().getConditionList().size(); i++) {
							info.getIntegrationCheckInfo().getConditionList().get(i).setTargetMonitorType(
									IntegrationConditionInfoRequest.TargetMonitorTypeEnum.fromValue(
											this.inputData.getIntegrationCheckInfo().getConditionList().get(i).getTargetMonitorType().getValue()));
						}
					}
					wrapper.modifyIntegrationMonitor(this.inputData.getMonitorId(), info);
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.35", args));
					result = true;
				} catch (RuntimeException e) {
					// findbus対応 RuntimeExceptionのcatchを明示化
					String errMessage = ", " + HinemosMessage.replace(e.getMessage());
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.36", args) + errMessage);
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
		// 対象監視設定を取得する
		IntegrationCheckInfoResponse checkInfo = new IntegrationCheckInfoResponse();
		checkInfo.setTimeout(60);
		monitor.setIntegrationCheckInfo(checkInfo);
	}

	/**
	 * オーナーロールを設定する
	 * @return
	 */
	@Override
	public void updateOwnerRole(String ownerRoleId) {
		super.updateOwnerRole(ownerRoleId);
		m_ownerRoleId = ownerRoleId;
		m_compConditionList.setOwnerRoleId(ownerRoleId);
	}

	/**
	 * ボタンを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param label ボタンに表示するテキスト
	 * @return ボタン
	 */
	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.NONE);
		button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		button.setText(label);
		return button;
	}
}
