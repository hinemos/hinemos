/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

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
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.JobFileCheckInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.bean.JobInfoParameterConstant;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * ファイルチェックタブ用のコンポジットクラスです。
 *
 */
public class FileCheckComposite extends Composite {

	/** スコープ用テキスト */
	private Text m_scopeFixedValueText = null;
	/** スコープ（ジョブ変数）用テキスト */
	private Text m_scopeJobParamText = null;
	/** ジョブ変数用ラジオボタン */
	private Button m_scopeJobParamRadio = null;
	/** 固定値用ラジオボタン */
	private Button m_scopeFixedValueRadio = null;
	/** スコープ参照用ボタン */
	private Button m_scopeFixedValueSelect = null;
	/** ディレクトリ用テキスト */
	private Text m_directory = null;
	/** ファイル名用テキスト */
	private Text m_fileName = null;

	/** 全てのノードで用ラジオボタン */
	private Button m_allNode = null;
	/** いずれかのノードで用ラジオボタン */
	private Button m_eitherNode = null;

	/** ファイルチェック種別 - 作成用チェックボックス */
	private Button m_typeCreate = null;
	/** ジョブ開始前に作成されたファイルも対象にする用チェックボックス */
	private Button m_createBeforeJobStart = null;
	/** ファイルチェック種別 - 削除用チェックボックス */
	private Button m_typeDelete = null;
	/** ファイルチェック種別 - 変更用チェックボックス */
	private Button m_typeModify = null;
	/** 変更種別 - タイムスタンプ用ラジオボタン */
	private Button m_typeTimeStamp = null;
	/** 変更種別 - ファイルサイズ用ラジオボタン */
	private Button m_typeFileSize = null;
	/** ファイルの使用中は判定しない用チェックボックス */
	private Button m_notJudgeFileInUse = null;
	/** 条件を満たした場合の終了値(テキスト) */
	private Text m_successEndValue = null;

	/** 条件を満たさなければ終了する用チェックボタン */
	private Button m_failureEnd = null;
	/** 条件を満たさない時のタイムアウト用テキスト */
	private Text m_failureWaitTime = null;
	/** 条件を満たさない時の終了値用テキスト */
	private Text m_failureEndValue = null;

	/** シェル */
	private Shell m_shell = null;
	/** オーナーロールID */
	private String m_ownerRoleId = null;
	/** マネージャ名 */
	private String m_managerName = null;
	/** ファシリティID */
	private String m_facilityId = null;
	/** スコープ */
	private String m_facilityPath = null;
	/** ファシリティID（固定値用） */
	private String m_facilityIdFixed = null;

	/** ファイルチェック情報 */
	private JobFileCheckInfoResponse m_filecheck;

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
	public FileCheckComposite(Composite parent, int style) {
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
		this.m_scopeJobParamRadio = new Button(cmdScopeGroup, SWT.RADIO);
		this.m_scopeJobParamRadio.setText(Messages.getString("job.parameter") + " : ");
		this.m_scopeJobParamRadio.setLayoutData(
				new GridData(120, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeJobParamRadio.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					m_scopeJobParamText.setEditable(true);
					m_scopeFixedValueRadio.setSelection(false);
					m_scopeFixedValueSelect.setEnabled(false);
					m_facilityId = m_scopeJobParamText.getText();
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// スコープ：ジョブ変数（テキスト）
		this.m_scopeJobParamText = new Text(cmdScopeGroup, SWT.BORDER);
		this.m_scopeJobParamText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_scopeJobParamText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
				if (m_scopeJobParamRadio.getSelection()) {
					m_facilityId = m_scopeJobParamText.getText();
				}
			}
		});

		//dummy
		new Label(cmdScopeGroup, SWT.LEFT);

		// スコープ：固定値（ラジオ）
		this.m_scopeFixedValueRadio = new Button(cmdScopeGroup, SWT.RADIO);
		this.m_scopeFixedValueRadio.setText(Messages.getString("fixed.value") + " : ");
		this.m_scopeFixedValueRadio.setLayoutData(new GridData(120,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeFixedValueRadio.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					m_scopeFixedValueSelect.setEnabled(true);
					m_scopeJobParamRadio.setSelection(false);
					m_scopeJobParamText.setEditable(false);
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// スコープ：固定値（テキスト）
		this.m_scopeFixedValueText = new Text(cmdScopeGroup, SWT.BORDER | SWT.READ_ONLY);
		this.m_scopeFixedValueText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_scopeFixedValueText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// スコープ：参照
		this.m_scopeFixedValueSelect = new Button(cmdScopeGroup, SWT.NONE);
		this.m_scopeFixedValueSelect.setText(Messages.getString("refer"));
		this.m_scopeFixedValueSelect.setLayoutData(new GridData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeFixedValueSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(m_shell, m_managerName, m_ownerRoleId);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse selectItem = dialog.getSelectItem();
					FacilityInfoResponse info = selectItem.getData();
					FacilityPath path = new FacilityPath(
							ClusterControlPlugin.getDefault()
							.getSeparator());
					m_facilityPath = path.getPath(selectItem);
					m_facilityIdFixed = info.getFacilityId();
					m_scopeFixedValueText.setText(m_facilityPath);
					update();
				}
			}
		});

		// ディレクトリ（Composite）
		Composite directoryComposite = new Composite(this, SWT.NONE);
		directoryComposite.setLayout(new GridLayout(2, false));

		// ディレクトリ(ラベル)
		Label directoryLabel = new Label(directoryComposite, SWT.NONE);
		directoryLabel.setText(Messages.getString("directory") + " : ");
		directoryLabel.setLayoutData(new GridData(130, SizeConstant.SIZE_LABEL_HEIGHT));

		// ディレクトリ
		this.m_directory = new Text(directoryComposite, SWT.BORDER);
		this.m_directory.setLayoutData(new GridData(300, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_directory.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ファイル名(ラベル)
		Label fileNameLabel = new Label(directoryComposite, SWT.NONE);
		fileNameLabel.setText(Messages.getString("file.name") + "(" + Messages.getString("regex") + ") : ");
		fileNameLabel.setLayoutData(new GridData(130, SizeConstant.SIZE_LABEL_HEIGHT));

		// ファイル名
		this.m_fileName = new Text(directoryComposite, SWT.BORDER);
		this.m_fileName.setLayoutData(new GridData(300, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_fileName.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// スコープ処理（グループ）
		Group scopeProcGroup = new Group(this, SWT.NONE);
		scopeProcGroup.setText(Messages.getString("scope.process"));
		scopeProcGroup.setLayout(new GridLayout(2, false));

		// スコープ処理(ラベル)
		Label scopeProcLabel = new Label(scopeProcGroup, SWT.NONE);
		scopeProcLabel.setText(Messages.getString("job.file.check.target.node"));
		scopeProcLabel.setLayoutData(new GridData(300, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)scopeProcLabel.getLayoutData()).horizontalSpan = 2;

		// スコープ処理：全てのノード
		this.m_allNode = new Button(scopeProcGroup, SWT.RADIO);
		this.m_allNode.setText(Messages.getString("scope.all.nodes"));
		this.m_allNode.setLayoutData(
				new GridData(150,SizeConstant.SIZE_BUTTON_HEIGHT));
		
		// スコープ処理：いずれかのノード
		this.m_eitherNode = new Button(scopeProcGroup, SWT.RADIO);
		this.m_eitherNode.setText(Messages.getString("scope.either.nodes"));
		this.m_eitherNode.setLayoutData(
				new GridData(250, SizeConstant.SIZE_BUTTON_HEIGHT));

		// チェック種別（グループ）
		Group checkGroup = new Group(this, SWT.NONE);
		checkGroup.setLayout(new GridLayout(2, false));
		checkGroup.setText(Messages.getString("file.check.type")+ " : ");

		// チェック種別：作成
		this.m_typeCreate = new Button(checkGroup, SWT.CHECK);
		this.m_typeCreate.setText(Messages.getString("create"));
		this.m_typeCreate.setLayoutData(new GridData(70, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_typeCreate.getLayoutData()).verticalAlignment = SWT.BEGINNING;
		this.m_typeCreate.setSelection(true);
		this.m_typeCreate.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_createBeforeJobStart.setEnabled(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// ジョブ開始前に作成されたファイルも対象にする
		this.m_createBeforeJobStart = new Button(checkGroup, SWT.CHECK);
		this.m_createBeforeJobStart.setText(Messages.getString("file.check.target.before.jobrun"));
		this.m_createBeforeJobStart.setLayoutData(new GridData(300, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_createBeforeJobStart.getLayoutData()).horizontalAlignment = GridData.CENTER;

		// チェック種別：削除
		this.m_typeDelete = new Button(checkGroup, SWT.CHECK);
		this.m_typeDelete.setText(Messages.getString("delete"));
		this.m_typeDelete.setLayoutData(new GridData(70, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_typeDelete.getLayoutData()).verticalAlignment = SWT.BEGINNING; 

		// dummy
		new Label(checkGroup, SWT.NONE);

		// チェック種別：変更
		this.m_typeModify = new Button(checkGroup, SWT.CHECK);
		this.m_typeModify.setText(Messages.getString("modify"));
		this.m_typeModify.setLayoutData(new GridData(70, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_typeModify.getLayoutData()).verticalAlignment = SWT.BEGINNING; 
		this.m_typeModify.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_typeTimeStamp.setEnabled(check.getSelection());
				m_typeFileSize.setEnabled(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 変更：種別（Composite）
		Composite modifyTypeComposite = new Composite(checkGroup, SWT.BORDER);
		modifyTypeComposite.setLayout(new GridLayout(2, false));

		// 変更：タイムスタンプ変更（ラジオボタン）
		this.m_typeTimeStamp = new Button(modifyTypeComposite, SWT.RADIO);
		this.m_typeTimeStamp.setText(Messages.getString("file.check.type.modify.timestamp"));
		this.m_typeTimeStamp.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_typeTimeStamp.getLayoutData()).horizontalAlignment = GridData.CENTER;
		this.m_typeTimeStamp.setSelection(true);
		this.m_typeTimeStamp.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_typeFileSize.setSelection(!check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 変更：ファイルサイズ変更（ラジオボタン）
		this.m_typeFileSize = new Button(modifyTypeComposite, SWT.RADIO);
		this.m_typeFileSize.setText(Messages.getString("file.check.type.modify.file.size"));
		this.m_typeFileSize.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_typeFileSize.getLayoutData()).horizontalAlignment = GridData.CENTER;
		this.m_typeFileSize.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_typeTimeStamp.setSelection(!check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// ファイルの使用中は判定しない（チェックボックス）
		this.m_notJudgeFileInUse = new Button(this, SWT.CHECK);
		this.m_notJudgeFileInUse.setText(Messages.getString("file.check.not.judge.inuse"));
		this.m_notJudgeFileInUse.setLayoutData(new RowData(300, SizeConstant.SIZE_BUTTON_HEIGHT));

		// separator
		JobDialogUtil.getSeparator(this);

		// 条件を満たした場合の終了値（Composite）
		Composite successEndGroup = new Composite(JobDialogUtil.getComposite_MarginZero(this), SWT.NONE);
		successEndGroup.setLayout(new RowLayout());

		// 条件を満たした場合の終了値（ラベル）
		Label successEndValueLabel = new Label(JobDialogUtil.getComposite_MarginZero(successEndGroup), SWT.NONE);
		successEndValueLabel.setText(Messages.getString("job.file.check.match.end.value") + " : ");
		successEndValueLabel.setLayoutData(new RowData(200, SizeConstant.SIZE_LABEL_HEIGHT));

		// 条件を満たした場合の終了値
		this.m_successEndValue = new Text(JobDialogUtil.getComposite_MarginZero(successEndGroup), SWT.BORDER);
		this.m_successEndValue.setLayoutData(new RowData(120, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_successEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_successEndValue.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
			);

		// separator
		JobDialogUtil.getSeparator(this);

		// 条件を満たさなければ終了（チェックボックス）
		this.m_failureEnd = new Button(this, SWT.CHECK);
		this.m_failureEnd.setText(Messages.getString("end.if.condition.unmatched"));
		this.m_failureEnd.setLayoutData(new RowData(300, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_failureEnd.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_failureWaitTime.setEditable(check.getSelection());
				m_failureEndValue.setEditable(check.getSelection());
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 条件を満たさなければ終了（Composite）
		Composite failureEndGroup = new Composite(this, SWT.BORDER);
		failureEndGroup.setLayout(new GridLayout(5, false));

		// 条件を満たさなければ終了：タイムアウト（ラベル）
		Label timeoutLabel = new Label(failureEndGroup, SWT.LEFT);
		timeoutLabel.setText(Messages.getString("job.timeout") + " : ");
		timeoutLabel.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 条件を満たさなければ終了：タイムアウト（テキスト）
		this.m_failureWaitTime = new Text(failureEndGroup, SWT.BORDER);
		this.m_failureWaitTime.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_failureWaitTime
				.addVerifyListener(new NumberVerifyListener(JobInfoParameterConstant.FILECHECK_TIMEOUT_LEN_MIN,
						JobInfoParameterConstant.FILECHECK_TIMEOUT_LEN_MAX));
		this.m_failureWaitTime.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 監視結果が得られない場合：時間単位（ラベル）
		Label unitLabel = new Label(failureEndGroup, SWT.LEFT);
		unitLabel.setText(Messages.getString("min"));
		unitLabel.setLayoutData(new GridData(30, SizeConstant.SIZE_LABEL_HEIGHT));

		// 条件を満たさなければ終了：終了値（ラベル）
		Label endValueLabel = new Label(failureEndGroup, SWT.RIGHT);
		endValueLabel.setText(Messages.getString("end.value") + " : ");
		endValueLabel.setLayoutData(new GridData(80, SizeConstant.SIZE_LABEL_HEIGHT));

		// 条件を満たさなければ終了：終了値（テキスト）
		this.m_failureEndValue = new Text(failureEndGroup, SWT.BORDER);
		this.m_failureEndValue.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_failureEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_failureEndValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
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
		if(m_scopeFixedValueRadio.getSelection() && "".equals(this.m_scopeFixedValueText.getText())){
			this.m_scopeFixedValueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_scopeFixedValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_scopeJobParamRadio.getSelection() && "".equals(this.m_scopeJobParamText.getText())){
			this.m_scopeJobParamText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_scopeJobParamText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (this.m_failureWaitTime.getEditable() && "".equals(this.m_failureWaitTime.getText())) {
			this.m_failureWaitTime.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_failureWaitTime.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_failureEndValue.getEditable() && "".equals(this.m_failureEndValue.getText())) {
			this.m_failureEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_failureEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_directory.getEditable() && "".equals(this.m_directory.getText())) {
			this.m_directory.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_directory.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_fileName.getEditable() && "".equals(this.m_fileName.getText())) {
			this.m_fileName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_fileName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_successEndValue.getEditable() && "".equals(this.m_successEndValue.getText())) {
			this.m_successEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_successEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}


	/**
	 * ファイル出力情報をコンポジットに反映します。
	 */
	public void reflectFileCheckInfo() {

		// 初期値
		//スコープ（ジョブ変数）の初期値は"#[FACILITY_ID]"とする
		m_scopeJobParamRadio.setSelection(false);
		m_scopeJobParamText.setText(SystemParameterConstant.getParamText(SystemParameterConstant.FACILITY_ID));
		m_scopeFixedValueRadio.setSelection(true);
		m_scopeFixedValueText.setText("");
		m_directory.setText("");
		m_fileName.setText("");
		m_allNode.setSelection(true);
		m_eitherNode.setSelection(false);
		m_typeCreate.setSelection(false);
		m_createBeforeJobStart.setSelection(false);
		m_typeDelete.setSelection(false);
		m_typeModify.setSelection(false);
		m_typeTimeStamp.setSelection(true);
		m_typeFileSize.setSelection(false);
		m_notJudgeFileInUse.setSelection(false);
		m_successEndValue.setText(String.valueOf(0));
		m_failureEnd.setSelection(false);
		m_failureWaitTime.setText(String.valueOf(1));
		m_failureEndValue.setText(String.valueOf(-1));

		if (m_filecheck != null) {
			//スコープ設定
			m_facilityPath = HinemosMessage.replace(m_filecheck.getScope());
			m_facilityId = m_filecheck.getFacilityID();
			if(isParamFormat(m_facilityId)){
				//ファシリティIDがジョブ変数の場合
				m_facilityPath = "";
				m_scopeJobParamRadio.setSelection(true);
				m_scopeJobParamText.setText(m_facilityId);
				m_scopeFixedValueRadio.setSelection(false);
				m_scopeFixedValueText.setText("");
			} else{
				if (m_facilityPath != null && m_facilityPath.length() > 0) {
					m_scopeFixedValueText.setText(m_facilityPath);
					m_facilityIdFixed = m_facilityId;
				}
				m_scopeJobParamRadio.setSelection(false);
				m_scopeFixedValueRadio.setSelection(true);
			}
			//処理方法設定
			if (m_filecheck.getProcessingMethod() ==  JobFileCheckInfoResponse.ProcessingMethodEnum.ALL_NODE) {
				m_allNode.setSelection(true);
				m_eitherNode.setSelection(false);
			} else {
				m_allNode.setSelection(false);
				m_eitherNode.setSelection(true);
			}

			if (m_filecheck.getDirectory() != null) {
				m_directory.setText(m_filecheck.getDirectory());
			} 
			if (m_filecheck.getFileName() != null) {
				m_fileName.setText(m_filecheck.getFileName());
			}
			m_typeCreate.setSelection(m_filecheck.getCreateValidFlg());
			m_createBeforeJobStart.setSelection(m_filecheck.getCreateBeforeJobStartFlg());
			m_typeDelete.setSelection(m_filecheck.getDeleteValidFlg());
			m_typeModify.setSelection(m_filecheck.getModifyValidFlg());
			if (m_filecheck.getModifyType() == JobFileCheckInfoResponse.ModifyTypeEnum.TIMESTAMP) {
				m_typeTimeStamp.setSelection(true);
				m_typeFileSize.setSelection(false);
			} else {
				m_typeTimeStamp.setSelection(false);
				m_typeFileSize.setSelection(true);
			}
			m_notJudgeFileInUse.setSelection(m_filecheck.getNotJudgeFileInUseFlg());
			m_successEndValue.setText(String.valueOf(m_filecheck.getSuccessEndValue()));
			m_failureEnd.setSelection(m_filecheck.getFailureEndFlg());
			m_failureWaitTime.setText(String.valueOf(m_filecheck.getFailureWaitTime()));
			m_failureEndValue.setText(String.valueOf(m_filecheck.getFailureEndValue()));
		}

		// オブジェクトの有効/無効設定
		if (m_scopeJobParamRadio.getSelection()) {
			m_scopeFixedValueSelect.setEnabled(false);
		} else {
			m_scopeFixedValueSelect.setEnabled(true);
		}
		if (m_typeCreate.getSelection()) {
			m_createBeforeJobStart.setEnabled(true);
		} else {
			m_createBeforeJobStart.setEnabled(false);
		}
		if (m_typeModify.getSelection()) {
			m_typeTimeStamp.setEnabled(true);
			m_typeFileSize.setEnabled(true);
		} else {
			m_typeTimeStamp.setEnabled(false);
			m_typeFileSize.setEnabled(false);
		}
		m_failureWaitTime.setEditable(m_failureEnd.getSelection());
		m_failureEndValue.setEditable(m_failureEnd.getSelection());
	}

	/**
	 * ファイルチェック情報を設定します。
	 *
	 * @param filecheck ファイルチェック情報
	 */
	public void setFileCheckInfo(JobFileCheckInfoResponse filecheck) {
		m_filecheck = filecheck;
	}

	/**
	 * ファイルチェック情報を返します。
	 *
	 * @return ファイルチェック情報
	 */
	public JobFileCheckInfoResponse getFileCheckInfo() {
		return m_filecheck;
	}

	/**
	 * コンポジットの情報から、ファイルチェック情報を作成する。
	 *
	 * @return 入力値の検証結果
	 */
	public ValidateResult createFileCheckInfo() {
		ValidateResult result = null;

		// ファイルチェック情報のインスタンスを作成・取得
		m_filecheck = new JobFileCheckInfoResponse();

		//スコープ取得
		if(m_scopeJobParamRadio.getSelection()){
			if (isParamFormat(m_facilityId)) {
				//ジョブ変数の場合
				m_filecheck.setFacilityID(m_facilityId);
				m_filecheck.setScope("");
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.hinemos.4"));
				return result;
			}
		}
		else{
			//固定値の場合
			if (m_facilityIdFixed != null && m_facilityIdFixed.length() > 0){
				m_filecheck.setFacilityID(m_facilityIdFixed);
				m_filecheck.setScope(m_facilityPath);
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.hinemos.3"));
				return result;
			}
		}

		// ディレクトリ
		if (m_directory.getText().trim().isEmpty()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.common.1", new String[] { Messages.getString("directory") }));
			return result;
		}
		m_filecheck.setDirectory(m_directory.getText());

		// ファイル名
		if (m_fileName.getText().trim().isEmpty()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.common.1", new String[] { Messages.getString("file.name") }));
			return result;
		}
		m_filecheck.setFileName(m_fileName.getText());

		//処理方法取得
		if (m_allNode.getSelection()) {
			m_filecheck.setProcessingMethod(JobFileCheckInfoResponse.ProcessingMethodEnum.ALL_NODE);
		} else {
			m_filecheck.setProcessingMethod(JobFileCheckInfoResponse.ProcessingMethodEnum.ANY_NODE);
		}

		// 条件を満たした場合の終了値
		try {
			m_filecheck.setSuccessEndValue(
					Integer.parseInt(m_successEndValue.getText()));
		} catch (NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.common.1", 
					new String[]{Messages.getString("job.file.check.match.end.value")}));
			result.setMessage(Messages.getString("message.job.21"));
			return result;
		}

		// 条件を満たさない場合に終了する
		m_filecheck.setFailureEndFlg(m_failureEnd.getSelection());

		// 条件を満たさない場合のタイムアウト値
		try {
			m_filecheck.setFailureWaitTime(
					Integer.parseInt(m_failureWaitTime.getText()));
		} catch (NumberFormatException e) {
			if (m_filecheck.getFailureEndFlg()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.171"));
				return result;
			}
		}

		// 条件を満たさない場合の終了値
		try {
			m_filecheck.setFailureEndValue(
					Integer.parseInt(m_failureEndValue.getText()));
		} catch (NumberFormatException e) {
			if (m_filecheck.getFailureEndFlg()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.172"));
				return result;
			}
		}

		// チェック種別がいずれもチェックされていない場合はNG
		if (!m_typeCreate.getSelection() && !m_typeDelete.getSelection() && !m_typeModify.getSelection()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(
					Messages.getString("message.common.1", new String[] { Messages.getString("file.check.type") }));
			return result;
		}

		// チェック種別 - 作成
		m_filecheck.setCreateValidFlg(m_typeCreate.getSelection());

		// ジョブ開始前に作成されたファイルも対象とする
		m_filecheck.setCreateBeforeJobStartFlg(m_createBeforeJobStart.getSelection());

		// チェック種別 - 削除
		m_filecheck.setDeleteValidFlg(m_typeDelete.getSelection());

		// チェック種別 - 変更
		m_filecheck.setModifyValidFlg(m_typeModify.getSelection());

		// 変更判定（タイムスタンプ変更/ファイルサイズ変更）
		if (m_typeTimeStamp.getSelection()) {
			m_filecheck.setModifyType(JobFileCheckInfoResponse.ModifyTypeEnum.TIMESTAMP);
		} else {
			m_filecheck.setModifyType(JobFileCheckInfoResponse.ModifyTypeEnum.FILESIZE);
		}

		// ファイルの使用中は判定しないか
		m_filecheck.setNotJudgeFileInUseFlg(m_notJudgeFileInUse.getSelection());

		return null;
	}


	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_scopeFixedValueText.setEditable(false);
		m_scopeJobParamText.setEditable(m_scopeJobParamRadio.getSelection() && enabled);
		m_scopeJobParamRadio.setEnabled(enabled);
		m_scopeFixedValueRadio.setEnabled(enabled);
		m_scopeFixedValueSelect.setEnabled(enabled);
		m_directory.setEditable(enabled);
		m_fileName.setEditable(enabled);
		m_allNode.setEnabled(enabled);
		m_eitherNode.setEnabled(enabled);
		m_typeCreate.setEnabled(enabled);
		m_createBeforeJobStart.setEnabled(enabled && m_typeCreate.getSelection());
		m_typeDelete.setEnabled(enabled);
		m_typeModify.setEnabled(enabled);
		m_typeTimeStamp.setEnabled(enabled && m_typeModify.getSelection());
		m_typeFileSize.setEnabled(enabled && m_typeModify.getSelection());
		m_notJudgeFileInUse.setEnabled(enabled);
		m_successEndValue.setEditable(enabled);
		m_failureEnd.setEnabled(enabled);
		m_failureWaitTime.setEditable(enabled && m_failureEnd.getSelection());
		m_failureEndValue.setEditable(enabled && m_failureEnd.getSelection());
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
		this.m_scopeFixedValueText.setText("");
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
	 * strがジョブ変数の書式(#[xxx])かどうかを判定する
	 * 
	 * @param str
	 * @return
	 */
	private boolean isParamFormat(String str) {
		if (str == null) {
			return false;
		}
		return str.startsWith(SystemParameterConstant.PREFIX)
				&& str.endsWith(SystemParameterConstant.SUFFIX);
	}
}
