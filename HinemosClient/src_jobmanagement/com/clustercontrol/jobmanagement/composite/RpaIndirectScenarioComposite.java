/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.composite;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobRpaInfoResponse;
import org.openapitools.client.model.JobRpaInfoResponse.RpaStopTypeEnum;
import org.openapitools.client.model.RpaManagementToolAccountResponse;
import org.openapitools.client.model.RpaManagementToolResponse;
import org.openapitools.client.model.RpaManagementToolRunParamResponse;
import org.openapitools.client.model.RpaManagementToolRunTypeResponse;
import org.openapitools.client.model.RpaManagementToolStopModeResponse;

import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.action.RpaIndirectParameterTableViewer;
import com.clustercontrol.jobmanagement.dialog.RpaIndirectScenarioParameterDialog;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobTypeConstant;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * RPAシナリオ 間接実行 シナリオ実行タブ用のコンポジットクラスです
 */
public class RpaIndirectScenarioComposite extends Composite {

	/** ロガー */
	private static final Log m_log = LogFactory.getLog(RpaIndirectScenarioComposite.class);

	/** RPA管理ツールアカウント コンボボックス */
	private ComboViewer m_rpaAccountComboViewer = null;

	/** RPA管理ツール（テキスト） */
	private Text m_rpaManagementToolText = null;

	/** 実行種別 コンボボックス */
	private ComboViewer m_runTypeComboViewer = null;

	/** 起動パラメータテーブルビューア */
	private RpaIndirectParameterTableViewer m_runParamTableViewer = null;

	/** シナリオ入力パラメータボタン */
	private Button m_scenarioParamButton = null;

	/** シナリオ入力パラメータ */
	private String m_scenarioParam = null;

	/** シナリオを終了する ラジオボタン */
	private Button m_stopScenarioButton = null;

	/** 停止方法 コンボボックス */
	private ComboViewer m_stopModeComboViewer = null;

	/** ジョブのみ終了する ラジオボタン */
	private Button m_stopJobButton = null;

	/** RPAシナリオジョブ情報 */
	private JobRpaInfoResponse m_rpa = null;

	/** マネージャ名 */
	private String m_managerName = null;

	/** RPAツールアカウント */
	private RpaManagementToolAccountResponse m_rpaAccount;

	/** RPAツールアカウントとRPA管理ツールのMap */
	private Map<RpaManagementToolAccountResponse, RpaManagementToolResponse> m_rpaAccountToolMap = new LinkedHashMap<>();

	/** RPAシナリオジョブ種別 */
	private Integer m_rpaJobType = null;

	/**
	 * 終了値タブ コンポジット<BR>
	 * 選択されたRPA管理ツールアカウントのRPA管理ツールの終了状態の表示に使用します。
	 */
	private RpaIndirectEndValueComposite m_rpaIndirectEndValueComposite = null;

	/** シェル */
	private Shell m_shell = null;

	/** 読み取り専用フラグ */
	private boolean m_readOnly = false;

	public RpaIndirectScenarioComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.m_managerName = managerName;
		this.m_shell = this.getShell();
		initialize();
	}

	private void initialize() {
		this.setLayout(JobDialogUtil.getParentLayout());

		// RPA管理ツール（グループ）
		Group rpaManagementToolGroup = new Group(this, SWT.NONE);
		rpaManagementToolGroup.setText(Messages.getString("rpa.management.tool"));
		rpaManagementToolGroup.setLayout(new GridLayout(2, false));

		// 対象アカウント（Composite）
		Composite rpaAccountComposite = new Composite(rpaManagementToolGroup, SWT.NONE);
		rpaAccountComposite.setLayout(new GridLayout(2, false));

		// 対象アカウント（ラベル）
		Label rpaAccountLabel = new Label(rpaAccountComposite, SWT.LEFT);
		rpaAccountLabel.setText(Messages.getString("rpa.select.account") + " : ");
		rpaAccountLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// 対象アカウント（コンボボックス）
		this.m_rpaAccountComboViewer = new ComboViewer(rpaAccountComposite, SWT.CENTER | SWT.READ_ONLY);
		this.m_rpaAccountComboViewer.getCombo().setLayoutData(new GridData(100, SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_rpaAccountComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		this.m_rpaAccountComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof RpaManagementToolAccountResponse) {
					RpaManagementToolAccountResponse account = (RpaManagementToolAccountResponse) element;
					return String.format("%s(%s)", account.getRpaScopeName(), account.getRpaScopeId());
				}
				return super.getText(element);
			}
		});

		// RPA管理ツール（Composite）
		Composite rpaManagementToolComposite = new Composite(rpaManagementToolGroup, SWT.NONE);
		rpaManagementToolComposite.setLayout(new GridLayout(2, false));

		// RPA管理ツール（ラベル）
		Label rpaManagementToolLabel = new Label(rpaManagementToolComposite, SWT.LEFT);
		rpaManagementToolLabel.setText(Messages.getString("rpa.management.tool") + " : ");
		rpaManagementToolLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// RPA管理ツール（テキスト）
		m_rpaManagementToolText = new Text(rpaManagementToolComposite, SWT.BORDER);
		m_rpaManagementToolText.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		// 直接入力は行わない
		m_rpaManagementToolText.setEditable(false);
		m_rpaManagementToolText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));

		// シナリオ実行（グループ）
		Group runScenarioGroup = new Group(this, SWT.NONE);
		runScenarioGroup.setText(Messages.getString("rpa.run.scenario"));
		runScenarioGroup.setLayout(new GridLayout(1, false));

		// 起動パラメータ（グループ）
		Group runParamGroup = new Group(runScenarioGroup, SWT.NONE);
		runParamGroup.setText(Messages.getString("rpa.run.parameter"));
		runParamGroup.setLayout(new GridLayout(1, false));

		// 実行種別（Composite）
		Composite runTypeComposite = new Composite(runParamGroup, SWT.NONE);
		runTypeComposite.setLayout(new GridLayout(2, false));

		// 実行種別（ラベル）
		Label runTypeLabel = new Label(runTypeComposite, SWT.LEFT);
		runTypeLabel.setText(Messages.getString("rpa.run.type") + " : ");
		runTypeLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// 実行種別（コンボボックス）
		this.m_runTypeComboViewer = new ComboViewer(runTypeComposite, SWT.CENTER | SWT.READ_ONLY);
		this.m_runTypeComboViewer.getCombo().setLayoutData(new GridData(100, SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_runTypeComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		this.m_runTypeComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof RpaManagementToolRunTypeResponse) {
					RpaManagementToolRunTypeResponse runType = (RpaManagementToolRunTypeResponse) element;
					return HinemosMessage.replace(runType.getRunTypeName());
				}
				return super.getText(element);
			}
		});

		// 起動パラメータ（テーブル）
		Table table = new Table(runParamGroup,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(450, 100));
		m_runParamTableViewer = new RpaIndirectParameterTableViewer(table);

		// シナリオ入力パラメータ（Composite）
		Composite scenarioParamComposite = new Composite(this, SWT.NONE);
		scenarioParamComposite.setLayout(new GridLayout(2, false));

		// シナリオ入力パラメータ（ラベル）
		Label scenarioParamLabel = new Label(scenarioParamComposite, SWT.LEFT);
		scenarioParamLabel.setText(Messages.getString("rpa.scenario.parameter") + " : ");
		scenarioParamLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// シナリオ入力パラメータ（ボタン）
		this.m_scenarioParamButton = new Button(scenarioParamComposite, SWT.NONE);
		this.m_scenarioParamButton.setText(Messages.getString("rpa.scenario.parameter"));
		this.m_scenarioParamButton.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scenarioParamButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RpaIndirectScenarioParameterDialog dialog = new RpaIndirectScenarioParameterDialog(m_shell, m_readOnly);
				dialog.setScenarioParam(m_scenarioParam);
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_scenarioParam = dialog.getScenarioParam();
				}
			}
		});

		// 停止（グループ）
		Group stopGroup = new Group(this, SWT.NONE);
		stopGroup.setText(Messages.getString("stop"));
		stopGroup.setLayout(new GridLayout(1, false));

		// シナリオを終了する（ラジオボタン）
		this.m_stopScenarioButton = new Button(stopGroup, SWT.RADIO);
		this.m_stopScenarioButton.setText(Messages.getString("rpa.stop.scenario"));
		this.m_stopScenarioButton.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_stopScenarioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_stopModeComboViewer.getCombo().setEnabled(m_stopModeComboViewer.getCombo().getItems().length > 1);
			}
		});

		// 停止方法（Composite）
		Composite stopModeComposite = new Composite(stopGroup, SWT.NONE);
		GridLayout stopModeLayout = new GridLayout(2, false);
		stopModeLayout.marginWidth = 0;
		stopModeComposite.setLayout(stopModeLayout);
		stopModeComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

		// 停止方法（ラベル）
		Label stopModeLabel = new Label(stopModeComposite, SWT.LEFT);
		stopModeLabel.setText(Messages.getString("rpa.stop.mode") + " : ");
		stopModeLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// 停止方法（コンボボックス）
		this.m_stopModeComboViewer = new ComboViewer(stopModeComposite, SWT.CENTER | SWT.READ_ONLY);
		this.m_stopModeComboViewer.getCombo().setLayoutData(new GridData(50, SizeConstant.SIZE_COMBO_HEIGHT));
		// プルダウン項目を設定
		this.m_stopModeComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		this.m_stopModeComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		this.m_stopModeComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof RpaManagementToolStopModeResponse) {
					RpaManagementToolStopModeResponse runType = (RpaManagementToolStopModeResponse) element;
					return HinemosMessage.replace(runType.getStopModeName());
				}
				return super.getText(element);
			}
		});

		// ジョブのみ終了する（ラジオボタン）
		this.m_stopJobButton = new Button(stopGroup, SWT.RADIO);
		this.m_stopJobButton.setText(Messages.getString("rpa.stop.only.job"));
		this.m_stopJobButton.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_stopJobButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_stopModeComboViewer.getCombo().setEnabled(false);
			}
		});

		// イベントリスナ
		// 対象アカウントの切り替え
		// RPA管理ツール名、実行種別、起動パラメータの表示
		// 終了値タブに終了状態を表示する
		this.m_rpaAccountComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				m_rpaAccount = (RpaManagementToolAccountResponse) selection.getFirstElement();
				// RPA管理ツール名
				m_rpaManagementToolText.setText(m_rpaAccountToolMap.get(m_rpaAccount).getRpaManagementToolName());
				// 実行種別プルダウン
				updateRunTypeCombo(m_rpaAccount.getRpaManagementToolId(), 1); // 実行種別の初期表示は1
				// 終了値タブの終了状態テーブルをRPA管理ツールに合わせて更新
				m_rpaIndirectEndValueComposite.refresh(m_rpaAccount.getRpaManagementToolId());
				// 停止方法
				updateStopModeCombo(m_rpaAccount.getRpaManagementToolId(), 1); // 停止方法の初期表示は1
				// 停止方法を選択できない場合はボタンを非活性にする
				disableStopTypeButton();
			}
		});

		// 実行種別の切り替え
		// 起動パラメータの表示
		this.m_runTypeComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				RpaManagementToolRunTypeResponse runType = (RpaManagementToolRunTypeResponse) selection
						.getFirstElement();
				// 選択中のRPAアカウントを取得
				IStructuredSelection rpaAccountSelection = (StructuredSelection) m_rpaAccountComboViewer.getSelection();
				RpaManagementToolAccountResponse rpaAccount = (RpaManagementToolAccountResponse) rpaAccountSelection
						.getFirstElement();
				// 起動パラメータ
				updateRunParamTable(rpaAccount.getRpaManagementToolId(), runType.getRunType());
			}
		});

		// RPAアカウントのプルダウンを生成
		List<RpaManagementToolAccountResponse> rpaAccountList;
		List<RpaManagementToolResponse> rpaManagementToolList;
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(this.m_managerName);
			rpaAccountList = wrapper.getRpaManagementToolAccountList();
			rpaManagementToolList = wrapper.getRpaManagementTool();
			// プルダウン項目を設定
			m_rpaAccountComboViewer.setInput(rpaAccountList);
			// RPA管理ツール名表示のためのMapを作成
			for (RpaManagementToolAccountResponse rpaAccount : rpaAccountList) {
				for (RpaManagementToolResponse rpaManagementTool : rpaManagementToolList) {
					if (rpaAccount.getRpaManagementToolId().equals(rpaManagementTool.getRpaManagementToolId())) {
						m_rpaAccountToolMap.put(rpaAccount, rpaManagementTool);
						break;
					}

				}
			}
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("initialize() : " + e.getMessage(), e);
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", "
							+ HinemosMessage.replace(e.getMessage()));
		}
	}

	private void updateRunTypeCombo(String rpaManagementToolId, Integer runType) {
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(this.m_managerName);
			// RPA管理ツールの実行種別リストを取得
			List<RpaManagementToolRunTypeResponse> runTypeResList = wrapper
					.getRpaManagementToolRunType(rpaManagementToolId);
			m_runTypeComboViewer.setInput(runTypeResList);
			// 実行種別を選択
			for (RpaManagementToolRunTypeResponse runTypeRes : runTypeResList) {
				if (runTypeRes.getRunType().equals(runType)) {
					m_runTypeComboViewer.setSelection(new StructuredSelection(runTypeRes));
					break;
				}
			}
			if (runTypeResList.size() > 1) {
				m_runTypeComboViewer.getCombo().setEnabled(true);
			} else {
				// 実行種別が1つだけの場合はプルダウンを無効にする
				m_runTypeComboViewer.getCombo().setEnabled(false);
			}
			// 起動パラメータを表示
			updateRunParamTable(rpaManagementToolId, runType);
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("updateRunTypeCombo() : " + e.getMessage(), e);
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", "
							+ HinemosMessage.replace(e.getMessage()));
		}
	}

	private void updateStopModeCombo(String rpaManagementToolId, Integer stopMode) {
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(this.m_managerName);
			// RPA管理ツールの停止方法リストを取得
			List<RpaManagementToolStopModeResponse> stopModeResList = wrapper
					.getRpaManagementToolStopMode(rpaManagementToolId);
			m_stopModeComboViewer.setInput(stopModeResList);
			// 停止方法を選択
			for (RpaManagementToolStopModeResponse stopModeRes : stopModeResList) {
				if (stopModeRes.getStopMode().equals(stopMode)) {
					m_stopModeComboViewer.setSelection(new StructuredSelection(stopModeRes));
					break;
				}
			}
			if (stopModeResList.size() > 1) {
				// シナリオを終了する場合はプルダウンを有効にする
				m_stopModeComboViewer.getCombo().setEnabled(m_stopScenarioButton.getSelection());
			} else {
				// 停止方法が1つだけの場合はプルダウンを無効にする
				m_stopModeComboViewer.getCombo().setEnabled(false);
			}
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("updateStopModeCombo() : " + e.getMessage(), e);
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", "
							+ HinemosMessage.replace(e.getMessage()));
		}

	}

	private void updateRunParamTable(String rpaManagementToolId, Integer runType) {
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(this.m_managerName);
			// RPA管理ツールの起動パラメータリストを取得
			List<RpaManagementToolRunParamResponse> runParamResList = wrapper
					.getRpaManagementToolRunParam(rpaManagementToolId, runType);
			m_runParamTableViewer.setInput(runParamResList);
			// 設定値を反映
			if (m_rpa != null) {
				m_runParamTableViewer.setRunParamInfos(m_rpa.getRpaJobRunParamInfos());
			}
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("updateRunParamTable() : " + e.getMessage(), e);
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", "
							+ HinemosMessage.replace(e.getMessage()));
		}

	}

	public void reflectRpaJobInfo() {
		if (this.m_rpa != null) {
			if (m_rpa.getRpaScopeId() != null && !m_rpa.getRpaScopeId().isEmpty()) {
				// 対象アカウント
				for (RpaManagementToolAccountResponse rpaAccount : m_rpaAccountToolMap.keySet()) {
					if (rpaAccount.getRpaScopeId().equals(m_rpa.getRpaScopeId())) {
						// 選択中のRPAアカウント
						m_rpaAccount = rpaAccount;
						// 設定項目をプルダウンから選択
						m_rpaAccountComboViewer.setSelection(new StructuredSelection(rpaAccount));
						// 実行種別、起動パラメータの反映
						updateRunTypeCombo(rpaAccount.getRpaManagementToolId(), m_rpa.getRpaRunType());
						m_runParamTableViewer.setRunParamInfos(m_rpa.getRpaJobRunParamInfos());
						// 停止方法の選択
						if (m_rpa.getRpaStopType() == RpaStopTypeEnum.SCENARIO) {
							updateStopModeCombo(m_rpaAccount.getRpaManagementToolId(), m_rpa.getRpaStopMode());
						}
						// 停止種別を選択できない場合はボタンを非活性にする
						disableStopTypeButton();
						break;
					}
				}
			}
			// シナリオ入力パラメータ
			m_scenarioParam = m_rpa.getRpaScenarioParam();
			// 停止種別
			if (m_rpa.getRpaStopType() != null) {
				if (m_rpa.getRpaStopType() == RpaStopTypeEnum.SCENARIO) {
					// シナリオを終了する
					m_stopScenarioButton.setSelection(true);
					m_stopJobButton.setSelection(false);
				} else {
					// シナリオは終了せず、ジョブのみ終了する
					m_stopScenarioButton.setSelection(false);
					m_stopJobButton.setSelection(true);
				}
			}
		} else {
			// 新規作成の場合はデフォルト値を表示
			m_stopScenarioButton.setSelection(true);
			m_stopJobButton.setSelection(false);
		}
	}

	public ValidateResult validateRpaJobInfo() {
		ValidateResult result = null;
		if (m_rpaJobType == RpaJobTypeConstant.DIRECT) {
			return result;
		}

		// 対象アカウント
		IStructuredSelection rpaAccountSelection = (StructuredSelection) m_rpaAccountComboViewer.getSelection();
		RpaManagementToolAccountResponse rpaAccount = (RpaManagementToolAccountResponse) rpaAccountSelection
				.getFirstElement();
		if (rpaAccount == null) {
			return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.job.rpa.27"));
		}

		// 起動パラメータ
		if ((result = m_runParamTableViewer.validateRunParamInfos()) != null) {
			return result;
		}

		return result;
	}

	public void createRpaJobInfo() {
		// 対象アカウント
		IStructuredSelection rpaAccountSelection = (StructuredSelection) m_rpaAccountComboViewer.getSelection();
		RpaManagementToolAccountResponse rpaAccount = (RpaManagementToolAccountResponse) rpaAccountSelection
				.getFirstElement();
		if (rpaAccount != null) {
			m_rpa.setRpaScopeId(rpaAccount.getRpaScopeId());
		}

		// 実行種別
		IStructuredSelection runTypeSelection = (StructuredSelection) m_runTypeComboViewer.getSelection();
		RpaManagementToolRunTypeResponse runType = (RpaManagementToolRunTypeResponse) runTypeSelection
				.getFirstElement();
		if (runType != null) {
			m_rpa.setRpaRunType(runType.getRunType());
		}

		// 起動パラメータ
		m_rpa.setRpaJobRunParamInfos(m_runParamTableViewer.createRunParamInfos());

		// シナリオ入力パラメータ
		m_rpa.setRpaScenarioParam(m_scenarioParam);

		// 停止種別
		if (m_stopScenarioButton.getSelection()) {
			m_rpa.setRpaStopType(RpaStopTypeEnum.SCENARIO);
		} else {
			m_rpa.setRpaStopType(RpaStopTypeEnum.JOB);
		}

		// 停止方法
		IStructuredSelection stopModeSelection = (StructuredSelection) m_stopModeComboViewer.getSelection();
		RpaManagementToolStopModeResponse stopMode = (RpaManagementToolStopModeResponse) stopModeSelection
				.getFirstElement();
		if (stopMode != null) {
			m_rpa.setRpaStopMode(stopMode.getStopMode());
		}
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		// 対象アカウント
		m_rpaAccountComboViewer.getCombo().setEnabled(enabled);
		// 実行種別
		// 実行種別が2つ以上ある場合のみ有効にする
		m_runTypeComboViewer.getCombo().setEnabled(m_runTypeComboViewer.getCombo().getItems().length > 1 && enabled);
		// 起動パラメータ
		m_runParamTableViewer.setEditable(enabled);
		// シナリオを終了する ラジオボタン
		m_stopScenarioButton.setEnabled(enabled);
		// 停止方法 コンボボックス
		// 停止方法が2つ以上ある場合のみ有効にする
		m_stopModeComboViewer.getCombo().setEnabled(m_stopModeComboViewer.getCombo().getItems().length > 1 && enabled);
		// ジョブのみ終了する ラジオボタン
		m_stopJobButton.setEnabled(enabled);
		m_readOnly = !enabled;
		// 停止方法を選択できない場合はボタンを非活性にする
		disableStopTypeButton();
	}

	/**
	 * @return the m_rpa
	 */
	public JobRpaInfoResponse getRpaJobInfo() {
		return this.m_rpa;
	}

	/**
	 * @param m_rpa
	 *            the m_rpa to set
	 */
	public void setRpaJobInfo(JobRpaInfoResponse rpa) {
		this.m_rpa = rpa;
	}

	public void setRpaIndirectEndValueComposite(RpaIndirectEndValueComposite composite) {
		this.m_rpaIndirectEndValueComposite = composite;
	}

	private void disableStopTypeButton() {
		// WinDirectorでapiVersion=1の場合はジョブのみ終了する以外選択不可
		if (m_rpaAccount != null
				&& m_rpaAccountToolMap.get(m_rpaAccount).getRpaManagementToolType().equals("WIN_DIRECTOR")
				&& m_rpaAccountToolMap.get(m_rpaAccount).getApiVersion() == 1) {
			m_stopScenarioButton.setEnabled(false);
			m_stopScenarioButton.setSelection(false);
			m_stopJobButton.setEnabled(false);
			m_stopJobButton.setSelection(true);
		}
	}

	/**
	 * RPAシナリオジョブ種別を設定します。<br>
	 * 必須項目のチェック有無を判断するために使用します。
	 * 
	 * @param rpaJobType
	 */
	public void setRpaJobType(Integer rpaJobType) {
		this.m_rpaJobType = rpaJobType;
	}
}
