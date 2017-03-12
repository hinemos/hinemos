/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.dialog.EnvVariableCompositeDialog;
import com.clustercontrol.jobmanagement.dialog.JobCommandParameterCompositeDialog;
import com.clustercontrol.jobmanagement.dialog.ManagerDistributionDialog;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.JobCommandInfo;
import com.clustercontrol.ws.jobmanagement.JobCommandParam;
import com.clustercontrol.ws.jobmanagement.JobEnvVariableInfo;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * コマンドタブ用のコンポジットクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class CommandComposite extends Composite {
	/** スコープ用テキスト */
	private Text m_scope = null;
	/** 起動コマンド用テキスト */
	private Text m_startCommand = null;
	/** 停止コマンド用ラジオボタン */
	private Button m_executeStopCommand;
	/** 停止コマンド用テキスト */
	private Text m_stopCommand = null;
	/** プロセス終了用ラジオボタン */
	private Button m_destroyProcess;
	/** エージェントと同じユーザ用ラジオボタン */
	private Button m_agentUser = null;
	/** ユーザを指定する用ラジオボタン */
	private Button m_specifyUser = null;
	/** 実効ユーザ用テキスト */
	private Text m_user = null;
	/** ジョブ変数用ラジオボタン */
	private Button m_scopeJobParam = null;
	/** 固定値用ラジオボタン */
	private Button m_scopeFixedValue = null;
	/** スコープ参照用ボタン */
	private Button m_scopeSelect = null;
	/** 全てのノードで実行用ラジオボタン */
	private Button m_allNode = null;
	/** 正常終了するまでノードを順次リトライ用ラジオボタン */
	private Button m_retry = null;
	/** ファシリティID */
	private String m_facilityId = null;
	/** スコープ */
	private String m_facilityPath = null;
	/** ジョブコマンド情報 */
	private JobCommandInfo m_execute = null;
	/** シェル */
	private Shell m_shell = null;
	/** オーナーロールID */
	private String m_ownerRoleId = null;
	/** マネージャ名 */
	private String m_managerName = null;
	/** スクリプト配布ボタン */
	private Button m_managerDistributionBtn = null;
	/** ジョブ終了時の変数設定ボタン */
	private Button m_jobCommandParamBtn = null;
	/** 環境変数ボタン */
	private Button m_envVariableBtn = null;
	
	/** スクリプト配布 */
	private boolean m_managerDistribution;
	private String m_scriptName = null;
	private String m_scriptEncoding = null;
	private String m_scriptContent = null;

	/** 環境変数 */
	private List<JobEnvVariableInfo> m_jobEnvVariableInfo = new ArrayList<JobEnvVariableInfo>();

	/** ジョブ変数情報 */
	private Map<String, JobCommandParam> m_jobCommandParamMap = new HashMap<>();

	/** 読み取り専用フラグ */
	private boolean m_readOnly = false;
	
	/**
	 * コンストラクタ
	 *
	 * @param parent 親コンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public CommandComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
		m_shell = this.getShell();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		this.setLayout(JobDialogUtil.getParentLayout());

		// スコープ（グループ）
		Group cmdScopeGroup = new Group(this, SWT.NONE);
		cmdScopeGroup.setText(Messages.getString("scope"));
		cmdScopeGroup.setLayout(new GridLayout(3, false));

		// スコープ：ジョブ変数（ラジオ）
		this.m_scopeJobParam = new Button(cmdScopeGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_scopeJobParam", this.m_scopeJobParam);
		this.m_scopeJobParam.setText(Messages.getString("job.parameter") + " : ");
		this.m_scopeJobParam.setLayoutData(
				new GridData(120, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeJobParam.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_scopeFixedValue.setSelection(false);
					m_scopeSelect.setEnabled(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		// スコープ：ジョブ変数（ラベル）
		Label scopeJobParamLabel = new Label(cmdScopeGroup, SWT.LEFT);
		scopeJobParamLabel.setText(
				SystemParameterConstant.getParamText(SystemParameterConstant.FACILITY_ID));
		scopeJobParamLabel.setLayoutData(
				new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		//dummy
		new Label(cmdScopeGroup, SWT.LEFT);

		// スコープ：固定値（ラジオ）
		this.m_scopeFixedValue = new Button(cmdScopeGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_scopeFixedValue", this.m_scopeFixedValue);
		this.m_scopeFixedValue.setText(Messages.getString("fixed.value") + " : ");
		this.m_scopeFixedValue.setLayoutData(new GridData(120,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeFixedValue.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_scopeJobParam.setSelection(false);
					m_scopeSelect.setEnabled(true);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		// スコープ：固定値（テキスト）
		this.m_scope = new Text(cmdScopeGroup, SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_scope", this.m_scope);
		this.m_scope.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_scope.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// スコープ：参照（ボタン）
		this.m_scopeSelect = new Button(cmdScopeGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_scopeSelect", this.m_scopeSelect);
		this.m_scopeSelect.setText(Messages.getString("refer"));
		this.m_scopeSelect.setLayoutData(new GridData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(m_shell, m_managerName, m_ownerRoleId);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItem selectItem = dialog.getSelectItem();
					FacilityInfo info = selectItem.getData();
					FacilityPath path = new FacilityPath(
							ClusterControlPlugin.getDefault()
							.getSeparator());
					m_facilityPath = path.getPath(selectItem);
					m_facilityId = info.getFacilityId();
					m_scope.setText(m_facilityPath);
					update();
				}
			}
		});

		// スコープ処理（グループ）
		Group cmdScopeProcGroup = new Group(this, SWT.NONE);
		cmdScopeProcGroup.setText(Messages.getString("scope.process"));
		cmdScopeProcGroup.setLayout(new RowLayout());

		// スコープ処理：全てのノード（ラジオ）
		this.m_allNode = new Button(cmdScopeProcGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_allNode", this.m_allNode);
		this.m_allNode.setText(Messages.getString("scope.process.all.nodes"));
		this.m_allNode.setLayoutData(
				new RowData(150,SizeConstant.SIZE_BUTTON_HEIGHT));
		
		// スコープ処理：正常終了するまでリトライ（ラジオ）
		this.m_retry = new Button(cmdScopeProcGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_retry", this.m_retry);
		this.m_retry.setText(Messages.getString("scope.process.retry.nodes"));
		this.m_retry.setLayoutData(
				new RowData(350, SizeConstant.SIZE_BUTTON_HEIGHT));
		
		// スクリプト配布
		Composite scriptDistributionComposite = new Composite(this, SWT.NONE);
		scriptDistributionComposite.setLayout(new GridLayout(2, false));
		
		// スクリプト配布：マネージャから配布（ラベル）
		Label scriptDistributionLabel = new Label(scriptDistributionComposite, SWT.NONE);
		scriptDistributionLabel.setText(Messages.getString("job.script.distribution") + ":");
		scriptDistributionLabel.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		
		//スクリプト配布：マネージャから配布（ボタン）
		this.m_managerDistributionBtn = new Button(scriptDistributionComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_managerDistributionBtn", this.m_managerDistributionBtn);
		this.m_managerDistributionBtn.setText(Messages.getString("job.script.distribution"));
		this.m_managerDistributionBtn.setLayoutData(new GridData(140,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_managerDistributionBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ManagerDistributionDialog dialog = new ManagerDistributionDialog(m_shell, m_readOnly);
				dialog.setManagerDistribution(m_managerDistribution);
				dialog.setScriptName(m_scriptName);
				dialog.setScriptEncoding(m_scriptEncoding);
				dialog.setScriptContent(m_scriptContent);
				dialog.setManager(m_managerName);
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_managerDistribution = dialog.getManagerDistribution();
					m_scriptName = dialog.getScriptName();
					m_scriptEncoding = dialog.getScriptEncoding();
					m_scriptContent = dialog.getScriptContent();
				}
			}
		});
		
		// 起動コマンド（Composite）
		Composite startCommandComposite = new Composite(this, SWT.NONE);
		startCommandComposite.setLayout(new RowLayout());

		// 起動コマンド（ラベル）
		Label startCommandLabel = new Label(startCommandComposite, SWT.NONE);
		startCommandLabel.setText(Messages.getString("start.command") + " : ");
		startCommandLabel.setLayoutData(new RowData(150,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// 起動コマンド（テキスト）
		this.m_startCommand = new Text(startCommandComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_startCommand", this.m_startCommand);
		this.m_startCommand.setLayoutData(new RowData(400,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_startCommand.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_1024));
		this.m_startCommand.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 停止（グループ）
		Group cmdStopGroup = new Group(this, SWT.NONE);
		cmdStopGroup.setText(Messages.getString("stop"));
		cmdStopGroup.setLayout(new RowLayout());

		// 停止：プロセスの終了（ラジオ）
		this.m_destroyProcess = new Button(cmdStopGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_destroyProcess", this.m_destroyProcess);
		this.m_destroyProcess.setText(Messages.getString("shutdown.process"));
		this.m_destroyProcess.setLayoutData(
				new RowData(190, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_destroyProcess.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_executeStopCommand.setSelection(false);
					m_stopCommand.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 停止：停止コマンド（ラジオ）
		this.m_executeStopCommand = new Button(cmdStopGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_executeStopCommand", this.m_executeStopCommand);
		this.m_executeStopCommand.setText(Messages.getString("stop.command"));
		this.m_executeStopCommand.setLayoutData(
				new RowData(120,SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_executeStopCommand.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_destroyProcess.setSelection(false);
					m_stopCommand.setEditable(true);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 停止：停止コマンド（テキスト）
		this.m_stopCommand = new Text(cmdStopGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_stopCommand", this.m_stopCommand);
		this.m_stopCommand.setLayoutData(new RowData(160,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_stopCommand.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_1024));
		this.m_stopCommand.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 実効ユーザ（グループ）
		Group cmdExeUserGroup = new Group(this, SWT.NONE);
		cmdExeUserGroup.setText(Messages.getString("effective.user"));
		cmdExeUserGroup.setLayout(new RowLayout());

		// 実効ユーザ：エージェント起動ユーザ（ラジオ）
		this.m_agentUser = new Button(cmdExeUserGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_agentUser", this.m_agentUser);
		this.m_agentUser.setText(Messages.getString("agent.user"));
		this.m_agentUser.setLayoutData(
				new RowData(190, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_agentUser.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_specifyUser.setSelection(false);
					m_user.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 実効ユーザ：ユーザを指定する（ラジオ）
		this.m_specifyUser = new Button(cmdExeUserGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_specifyUser", this.m_specifyUser);
		this.m_specifyUser.setText(Messages.getString("specified.user"));
		this.m_specifyUser.setLayoutData(
				new RowData(120, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_specifyUser.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_agentUser.setSelection(false);
					m_user.setEditable(true);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 実効ユーザ：ユーザを指定する（テキスト）
		this.m_user = new Text(cmdExeUserGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_user", this.m_user);
		this.m_user.setLayoutData(new RowData(160, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_user.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		this.m_user.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// Composite
		Composite cmdJobParameter = new Composite(this, SWT.NONE);
		cmdJobParameter.setLayout(new GridLayout(2, false));

		// ジョブ変数（ボタン）
		m_jobCommandParamBtn = new Button(cmdJobParameter, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_jobCommandParamBtn", this.m_jobCommandParamBtn);
		m_jobCommandParamBtn.setText(Messages.getString("job.command.result.parameter"));
		m_jobCommandParamBtn.setLayoutData(new GridData(180, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_jobCommandParamBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JobCommandParameterCompositeDialog dialog = new JobCommandParameterCompositeDialog(m_shell, m_readOnly);
				dialog.setJobCommandParamMap(m_jobCommandParamMap);
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_jobCommandParamMap = dialog.getJobCommandParamMap();
				}
			}
		});

		// 環境変数（ボタン）
		m_envVariableBtn = new Button(cmdJobParameter, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_envVariableBtn", this.m_envVariableBtn);
		m_envVariableBtn.setText(Messages.getString("job.environment.variable"));
		m_envVariableBtn.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_envVariableBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EnvVariableCompositeDialog dialog = new EnvVariableCompositeDialog(m_shell, m_readOnly);
				dialog.setJobEnvVariableInfo(m_jobEnvVariableInfo);
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_jobEnvVariableInfo = dialog.getJobEnvVariableInfo();
				}
			}
		});
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須項目を明示
		if(m_scopeFixedValue.getSelection() && "".equals(this.m_scope.getText())){
			this.m_scope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_scope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_startCommand.getText())){
			this.m_startCommand.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_startCommand.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_executeStopCommand.getSelection() && "".equals(this.m_stopCommand.getText())){
			this.m_stopCommand.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_stopCommand.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_specifyUser.getSelection() && "".equals(this.m_user.getText())){
			this.m_user.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_user.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ジョブコマンド情報をコンポジットに反映する。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobCommandInfo
	 */
	public void reflectCommandInfo() {

		// 初期値
		m_scopeJobParam.setSelection(false);
		m_scopeFixedValue.setSelection(true);
		m_scope.setText("");
		m_allNode.setSelection(true);
		m_startCommand.setText("");
		m_executeStopCommand.setSelection(false);
		m_destroyProcess.setSelection(true);
		m_stopCommand.setText("");
		m_stopCommand.setEditable(false);
		m_agentUser.setSelection(true);
		m_specifyUser.setSelection(false);
		m_user.setText("");
		m_user.setEditable(false);

		if (m_execute != null) {
			//スコープ設定
			m_facilityPath = HinemosMessage.replace(m_execute.getScope());
			m_facilityId = m_execute.getFacilityID();
			if(SystemParameterConstant.isParam(
					m_facilityId,
					SystemParameterConstant.FACILITY_ID)){
				//ファシリティIDがジョブ変数の場合
				m_facilityId = "";
				m_facilityPath = "";
				m_scope.setText(m_facilityPath);
				m_scopeJobParam.setSelection(true);
				m_scopeFixedValue.setSelection(false);
			}
			else{
				if (m_facilityPath != null && m_facilityPath.length() > 0) {
					m_scope.setText(m_facilityPath);
				}
				m_scopeJobParam.setSelection(false);
				m_scopeFixedValue.setSelection(true);
			}
			//処理方法設定
			if (m_execute.getProcessingMethod() == ProcessingMethodConstant.TYPE_ALL_NODE) {
				m_allNode.setSelection(true);
				m_retry.setSelection(false);
			} else {
				m_allNode.setSelection(false);
				m_retry.setSelection(true);
			}
			
			//スクリプト配布
			m_managerDistribution = m_execute.isManagerDistribution();
			m_scriptName = m_execute.getScriptName();
			m_scriptEncoding = m_execute.getScriptEncoding();
			m_scriptContent = m_execute.getScriptContent();
			
			//起動コマンド設定
			if (m_execute.getStartCommand() != null
					&& m_execute.getStartCommand().length() > 0) {
				m_startCommand.setText(m_execute.getStartCommand());
			}
			//停止コマンド設定
			if (m_execute.getStopType() == CommandStopTypeConstant.DESTROY_PROCESS) {
				m_destroyProcess.setSelection(true);
				m_executeStopCommand.setSelection(false);
			} else {
				m_destroyProcess.setSelection(false);
				m_executeStopCommand.setSelection(true);
				m_stopCommand.setEditable(true);
			}
			if (m_execute.getStopCommand() != null
					&& m_execute.getStopCommand().length() > 0) {
				m_stopCommand.setText(m_execute.getStopCommand());
			}
			//ユーザー設定
			if (m_execute.isSpecifyUser().booleanValue()) {
				m_specifyUser.setSelection(true);
				m_agentUser.setSelection(false);
				m_user.setEditable(true);
			} else {
				m_specifyUser.setSelection(false);
				m_agentUser.setSelection(true);
				m_user.setEditable(false);
			}
			if (m_execute.getUser() != null && m_execute.getUser().length() > 0) {
				m_user.setText(m_execute.getUser());
			}

			// コマンド終了時のジョブ変数
			if (m_execute.getJobCommandParamList() != null) {
				for (JobCommandParam jobCommandParam : m_execute.getJobCommandParamList()) {
					this.m_jobCommandParamMap.put(jobCommandParam.getParamId(), jobCommandParam);
				}
				reflectParamInfo();
			}
			//環境変数
			m_jobEnvVariableInfo.addAll(m_execute.getEnvVariableInfo());
		}

		//スコープ
		if (m_scopeJobParam.getSelection()) {
			m_scopeSelect.setEnabled(false);
		} else {
			m_scopeSelect.setEnabled(true);
		}
	}

	/**
	 * ジョブコマンド情報を設定する。
	 *
	 * @param execute ジョブコマンド情報
	 */
	public void setCommandInfo(JobCommandInfo execute) {
		m_execute = execute;
	}

	/**
	 * ジョブコマンド情報を返す。
	 *
	 * @return ジョブコマンド情報
	 */
	public JobCommandInfo getCommandInfo() {
		return m_execute;
	}

	/**
	 * コンポジットの情報から、ジョブコマンド情報を作成する。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobCommandInfo
	 */
	public ValidateResult createCommandInfo() {
		ValidateResult result = null;

		//実行内容情報クラスのインスタンスを作成・取得
		m_execute = new JobCommandInfo();

		//スコープ取得
		if(m_scopeJobParam.getSelection()){
			//ジョブ変数の場合
			m_execute.setFacilityID(
					SystemParameterConstant.getParamText(SystemParameterConstant.FACILITY_ID));
			m_execute.setScope("");
		}
		else{
			//固定値の場合
			if (m_facilityId != null && m_facilityId.length() > 0){
				m_execute.setFacilityID(m_facilityId);
				m_execute.setScope(m_facilityPath);
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.hinemos.3"));
				return result;
			}
		}

		//処理方法取得
		if (m_allNode.getSelection()) {
			m_execute
			.setProcessingMethod(ProcessingMethodConstant.TYPE_ALL_NODE);
		} else {
			m_execute.setProcessingMethod(ProcessingMethodConstant.TYPE_RETRY);
		}

		//スクリプト配布
		m_execute.setManagerDistribution(m_managerDistribution);
		m_execute.setScriptName(m_scriptName);
		m_execute.setScriptEncoding(m_scriptEncoding);
		m_execute.setScriptContent(m_scriptContent);
		
		//起動コマンド取得
		if (m_startCommand.getText().length() > 0) {
			m_execute.setStartCommand(m_startCommand.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.3"));
			return result;
		}

		//停止コマンド取得
		if (m_destroyProcess.getSelection()) {
			m_execute.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);
		} else {
			if (m_stopCommand.getText().length() > 0) {
				m_execute.setStopType(CommandStopTypeConstant.EXECUTE_COMMAND);
				m_execute.setStopCommand(m_stopCommand.getText());
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.4"));
				return result;
			}
		}

		//ユーザー取得
		if (m_agentUser.getSelection()) {
			m_execute.setSpecifyUser(false);
		} else {
			if (m_user.getText().length() > 0) {
				m_execute.setSpecifyUser(true);
				m_execute.setUser(m_user.getText());
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.5"));
				return result;
			}
		}
		
		//環境変数
		m_execute.getEnvVariableInfo().addAll(m_jobEnvVariableInfo);

		// ジョブ変数
		m_execute.getJobCommandParamList().addAll(m_jobCommandParamMap.values());
		
		return null;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
		this.m_scope.setText("");
		this.m_facilityId = null;
	}

	/**
	 * @return the m_managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}

	/**
	 * @param m_managerName the m_managerName to set
	 */
	public void setManagerName(String m_managerName) {
		this.m_managerName = m_managerName;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_scope.setEditable(false);
		m_startCommand.setEditable(enabled);
		m_executeStopCommand.setEnabled(enabled);
		m_stopCommand.setEditable(m_executeStopCommand.getSelection() && enabled);
		m_destroyProcess.setEnabled(enabled);
		m_agentUser.setEnabled(enabled);
		m_specifyUser.setEnabled(enabled);
		m_user.setEditable(m_specifyUser.getSelection() && enabled);
		m_scopeJobParam.setEnabled(enabled);
		m_scopeFixedValue.setEnabled(enabled);
		m_scopeSelect.setEnabled(enabled);
		m_allNode.setEnabled(enabled);
		m_retry.setEnabled(enabled);
		m_readOnly = !enabled;
	}

	/**
	 * ジョブ変数情報をコンポジットに反映します。
	 *
	 */
	public void reflectParamInfo() {
		m_managerDistributionBtn.setEnabled(isEnabled());
		m_envVariableBtn.setEnabled(isEnabled());
		m_jobCommandParamBtn.setEnabled(isEnabled());
	}
}
