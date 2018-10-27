/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.dialog;

import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.analytics.util.AnalyticsUtil;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.monitor.run.dialog.CommonMonitorNumericDialog;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.collect.CollectKeyInfo;
import com.clustercontrol.ws.monitor.CorrelationCheckInfo;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorDuplicate_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * 相関係数監視作成・変更ダイアログクラスです。
 *
 * @version 6.1.0
 */
public class CorrelationCreateDialog extends CommonMonitorNumericDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( CorrelationCreateDialog.class );

	// ----- instance フィールド ----- //
	/** 対象収集値表示名 */
	private Combo m_comboTargetItemName = null;

	/** 対象収集値表示名マップ（表示名, キー) */
	private Map<String, CollectKeyInfo> m_targetItemNameMap = new HashMap<>();

	/** 対象収集期間 */
	private Text m_analysysRange = null;

	/** 参照収集値表示名 */
	private Combo m_comboReferItemName = null;

	/** 参照収集値表示名マップ（表示名, キー) */
	private Map<String, CollectKeyInfo> m_referItemNameMap = new HashMap<>();

	/** 参照スコープ */
	private String m_referFacilityId = null;

	/** 参照スコープ */
	private Text m_textReferScope = null;
	
	/** 参照スコープ用参照ボタン */
	private Button m_buttonReferScope = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public CorrelationCreateDialog(Shell parent) {
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
	public CorrelationCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
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
		item1 = Messages.getString("response.correlation.range");
		item2 = Messages.getString("response.correlation.range");

		super.customizeDialog(parent);
		m_numericValueInfo.setInfoWarnText("0.8", "1.0", "0.6", "0.8");

		// タイトル
		shell.setText(Messages.getString("dialog.correlation.create.modify"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		/*
		 * チェック設定
		 */

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

		// 対象収集値表示名ID（ラベル）
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "collection.display.name", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("collection.display.name") + " : ");

		// 対象収集値表示名（コンボボックス）
		this.m_comboTargetItemName = new Combo(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "collection.display.name", this.m_comboTargetItemName);
		gridData = new GridData();
		gridData.horizontalSpan = 22;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboTargetItemName.setLayoutData(gridData);
		this.m_comboTargetItemName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// ラベル（対象収集期間）
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "label", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("analysys.range") + " : ");

		// テキスト（対象収集期間）
		this.m_analysysRange = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "analysysRange", m_analysysRange);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_analysysRange.setLayoutData(gridData);
		this.m_analysysRange.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//dummy
		label = new Label(groupCheckRule, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		label.setLayoutData(gridData);

		// 参照スコープ（ラベル）
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "monitor.refer.scope", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.refer.scope") + " : ");

		// 参照スコープ（テキスト）
		this.m_textReferScope = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, m_textReferScope);
		gridData = new GridData();
		gridData.horizontalSpan = 18;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textReferScope.setLayoutData(gridData);
		this.m_textReferScope.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 参照スコープ用参照ボタン（ボタン）
		this.m_buttonReferScope = new Button(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_buttonReferScope);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_buttonReferScope.setLayoutData(gridData);
		m_buttonReferScope.setText(Messages.getString("refer"));
		m_buttonReferScope.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				String managerName = getMonitorBasicScope().getManagerListComposite().getText();
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell, managerName, m_monitorBasic.getOwnerRoleId(), false, m_unregistered);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItem item = dialog.getSelectItem();
					FacilityInfo info = item.getData();
					if (info.getFacilityType() == FacilityConstant.TYPE_NODE) {
						m_textReferScope.setText(info.getFacilityName());
					} else {
						FacilityPath path = new FacilityPath(
								ClusterControlPlugin.getDefault()
								.getSeparator());
						m_textReferScope.setText(path.getPath(item));
					}
					m_referFacilityId = info.getFacilityId();
					// 参照収集値表示名コンボを切り替える
					AnalyticsUtil.setComboItemNameForNumeric(m_comboReferItemName, m_referItemNameMap, m_referFacilityId, 
							getManagerName(), m_monitorBasic.getOwnerRoleId());
					update();
				}
			}
		});

		// 参照収集値表示名（ラベル）
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "monitor.refer.collection.display.name", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.refer.collection.display.name") + " : ");

		// 参照収集値表示名（コンボボックス）
		this.m_comboReferItemName = new Combo(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "monitor.refer.collection.display.name", this.m_comboReferItemName);
		gridData = new GridData();
		gridData.horizontalSpan = 22;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboReferItemName.setLayoutData(gridData);
		this.m_comboReferItemName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// マネージャを変更した場合
		if(!updateFlg) {
			this.getMonitorBasicScope().getManagerListComposite()
			.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// 対象収集値表示名コンボを切り替える
					AnalyticsUtil.setComboItemNameForNumeric(m_comboTargetItemName, m_targetItemNameMap, m_monitorBasic.getFacilityId(),
							getManagerName(), m_monitorBasic.getOwnerRoleId());
					// 参照情報も切り替える
					m_textReferScope.setText("");
					m_referFacilityId = "";
					AnalyticsUtil.setComboItemNameForNumeric(m_comboReferItemName, m_referItemNameMap, m_referFacilityId,
							getManagerName(), m_monitorBasic.getOwnerRoleId());
					update();
				}
			});
		}

		// スコープを変更した場合
		m_monitorBasic.getButtonScope().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 対象収集値表示名コンボを切り替える
				AnalyticsUtil.setComboItemNameForNumeric(m_comboTargetItemName, m_targetItemNameMap, m_monitorBasic.getFacilityId(),
						getManagerName(), m_monitorBasic.getOwnerRoleId());
				update();
			}
		});

		// ダイアログを調整
		this.adjustDialog();

		// 初期表示
		MonitorInfo info = null;
		if(this.monitorId == null){
			// 作成の場合
			info = new MonitorInfo();
			this.setInfoInitialValue(info);
		} else {
			// 変更の場合、情報取得
			try {
				MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(getManagerName());
				info = wrapper.getMonitor(monitorId);
			} catch (InvalidRole_Exception e) {
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
		// 収集値表示名
		if("".equals(this.m_comboTargetItemName.getText())){
			this.m_comboTargetItemName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboTargetItemName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 対象収集期間
		if("".equals(this.m_analysysRange.getText())){
			this.m_analysysRange.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_analysysRange.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 参照先スコープ
		if("".equals(this.m_textReferScope.getText())){
			this.m_textReferScope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textReferScope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 参照先収集値表示名
		if("".equals(this.m_comboReferItemName.getText())){
			this.m_comboReferItemName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboReferItemName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
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

		// チェック項目
		CorrelationCheckInfo checkInfo = monitor.getCorrelationCheckInfo();
		if (checkInfo != null) {
			// 対象収集値表示名設定
			AnalyticsUtil.setComboItemNameForNumeric(m_comboTargetItemName, m_targetItemNameMap, monitor.getFacilityId(),
					getManagerName(), m_monitorBasic.getOwnerRoleId());
			m_comboTargetItemName.setText(AnalyticsUtil.getComboItemNameForNumeric(m_targetItemNameMap,
					checkInfo.getTargetMonitorId(), checkInfo.getTargetDisplayName(), checkInfo.getTargetItemName()));

			// 参照ノード設定
			this.m_referFacilityId = checkInfo.getReferFacilityId();
			try {
				RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(getManagerName());
				m_textReferScope.setText(HinemosMessage.replace(wrapper.getFacilityPath(m_referFacilityId, null)));
			} catch (com.clustercontrol.ws.repository.InvalidRole_Exception e) {
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

			// 参照収集値表示名設定
			AnalyticsUtil.setComboItemNameForNumeric(m_comboReferItemName, m_referItemNameMap, checkInfo.getReferFacilityId(),
					getManagerName(), m_monitorBasic.getOwnerRoleId());
			m_comboReferItemName.setText(AnalyticsUtil.getComboItemNameForNumeric(m_referItemNameMap,
					checkInfo.getReferMonitorId(), checkInfo.getReferDisplayName(), checkInfo.getReferItemName()));

			// 収集期間
			m_analysysRange.setText(checkInfo.getAnalysysRange().toString());
		}
		m_numericValueInfo.setInputData(monitor);
	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfo createInputData() {
		super.createInputData();
		if(validateResult != null){
			return null;
		}
		// 監視固有情報を設定
		monitorInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_CORRELATION);

		// 監視条件 監視情報
		CorrelationCheckInfo correlationCheckInfo = new CorrelationCheckInfo();
		if (this.m_comboTargetItemName.getText() != null) {
			CollectKeyInfo collectKeyInfo = m_targetItemNameMap.get(this.m_comboTargetItemName.getText());
			if (collectKeyInfo != null) {
				correlationCheckInfo.setTargetMonitorId(collectKeyInfo.getMonitorId());
				correlationCheckInfo.setTargetDisplayName(collectKeyInfo.getDisplayName());
				correlationCheckInfo.setTargetItemName(collectKeyInfo.getItemName());
			}
		}
		if (this.m_comboReferItemName.getText() != null) {
			CollectKeyInfo collectKeyInfo = m_referItemNameMap.get(this.m_comboReferItemName.getText());
			if (collectKeyInfo != null) {
				correlationCheckInfo.setReferMonitorId(collectKeyInfo.getMonitorId());
				correlationCheckInfo.setReferDisplayName(collectKeyInfo.getDisplayName());
				correlationCheckInfo.setReferItemName(collectKeyInfo.getItemName());
			}
		}
		correlationCheckInfo.setReferFacilityId(this.m_referFacilityId);
		if (m_analysysRange.getText() != null) {
			try {
				correlationCheckInfo.setAnalysysRange(Integer.valueOf(m_analysysRange.getText()));
			} catch (Exception e) {
				// 入力の詳細チェックはマネージャで行う。
			}
		}
		monitorInfo.setCorrelationCheckInfo(correlationCheckInfo);

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

		MonitorInfo info = this.inputData;
		if(info != null){
			String[] args = { info.getMonitorId(), getManagerName() };
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(getManagerName());
			if(!this.updateFlg){
				// 作成の場合
				try {
					result = wrapper.addMonitor(info);

					if(result){
						MessageDialog.openInformation(
								null,
								Messages.getString("successful"),
								Messages.getString("message.monitor.33", args));
					} else {
						MessageDialog.openError(
								null,
								Messages.getString("failed"),
								Messages.getString("message.monitor.34", args));
					}
				} catch (MonitorDuplicate_Exception e) {
					// 監視項目IDが重複している場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.monitor.53", args));
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole_Exception) {
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
				String errMessage = "";
				try {
					result = wrapper.modifyMonitor(info);
				} catch (InvalidRole_Exception e) {
					// アクセス権なしの場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} catch (Exception e) {
					errMessage = ", " + HinemosMessage.replace(e.getMessage());
				}

				if(result){
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.35", args));
				} else {
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
	protected void setInfoInitialValue(MonitorInfo monitor) {

		super.setInfoInitialValue(monitor);

		// 対象監視設定を取得する
		CorrelationCheckInfo correlationCheckInfo = new CorrelationCheckInfo();
		correlationCheckInfo.setAnalysysRange(60);
		monitor.setCorrelationCheckInfo(correlationCheckInfo);
	}

	/**
	 * オーナーロールを設定する
	 * @return
	 */
	@Override
	public void updateOwnerRole(String ownerRoleId) {
		super.updateOwnerRole(ownerRoleId);
		AnalyticsUtil.setComboItemNameForNumeric(m_comboTargetItemName, m_targetItemNameMap, 
				m_monitorBasic.getFacilityId(),	getManagerName(), m_monitorBasic.getOwnerRoleId());
		m_textReferScope.setText("");
		m_referFacilityId = "";
		AnalyticsUtil.setComboItemNameForNumeric(m_comboReferItemName, m_referItemNameMap, m_referFacilityId,
				getManagerName(), m_monitorBasic.getOwnerRoleId());
	}
}
