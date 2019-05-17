/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.JobFileInfo;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * ファイル転送タブ用のコンポジットクラスです。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class FileComposite extends Composite {
	/** 転送スコープ */
	private Text m_srcScope = null;
	/** 転送ファイル */
	private Text m_srcFile = null;

	/** 受信スコープ */
	private Text m_destScope = null;
	/** 受信ディレクトリ */
	private Text m_destDirectory = null;

	/** エージェント実行ユーザ用ラジオボタン */
	private Button m_agentUser = null;
	/** ユーザを指定する用ラジオボタン  */
	private Button m_specifyUser = null;
	/** 実効ユーザ */
	private Text m_user = null;
	/** 転送スコープ参照ボタン */
	private Button m_srcScopeSelect = null;
	/** 受信スコープ参照ボタン */
	private Button m_destScopeSelect = null;
	/** 全てのノードで受信用ラジオボタン */
	private Button m_allNode = null;
	/** 1ノードで受信用ラジオボタン */
	private Button m_oneNode = null;
	/** ファイル転送時圧縮用チェックボタン */
	private Button m_compressionCondition = null;
	/** 転送ファイルチェック用チェックボタン */
	private Button m_checkFileCondition = null;
	/** 転送ファシリティID */
	private String m_srcFacilityId = null;
	/** 転送ファシリティパス */
	private String m_srcFacilityPath = null;
	/** 受信ファシリティID */
	private String m_destFacilityId = null;
	/** 受信ファシリティパス */
	private String m_destFacilityPath = null;
	/** ジョブファイル転送情報 */
	private JobFileInfo m_jobFileInfo = null;
	/** シェル */
	private Shell m_shell = null;
	/** オーナーロールID */
	private String m_ownerRoleId = null;
	/** マネージャ名 */
	private String m_managerName = null;

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
	public FileComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
		m_shell = this.getShell();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		this.setLayout(JobDialogUtil.getParentLayout());

		// 転送（グループ）
		Group fileTransferFromgroup = new Group(this, SWT.NONE);
		fileTransferFromgroup.setText(Messages.getString("forward.source"));
		fileTransferFromgroup.setLayout(new GridLayout(3, false));

		// 転送：スコープ（ラベル）
		Label srcScopeTitle = new Label(fileTransferFromgroup, SWT.NONE);
		srcScopeTitle.setText(Messages.getString("scope") + " : ");
		srcScopeTitle.setLayoutData(new GridData(100,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// 転送：スコープ（テキスト）
		this.m_srcScope = new Text(fileTransferFromgroup, SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_srcScope", this.m_srcScope);
		this.m_srcScope.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_srcScope.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 転送：スコープ参照（ボタン）
		this.m_srcScopeSelect = new Button(fileTransferFromgroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_srcScopeSelect", this.m_srcScopeSelect);
		this.m_srcScopeSelect.setText(Messages.getString("refer"));
		this.m_srcScopeSelect.setLayoutData(new GridData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_srcScopeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(m_shell, m_managerName, m_ownerRoleId);
				// ノードのみ選択可能とする。
				dialog.setSelectNodeOnly(true);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItem selectItem = dialog.getSelectItem();
					FacilityInfo info = selectItem.getData();
					FacilityPath path = new FacilityPath(
							ClusterControlPlugin.getDefault()
							.getSeparator());
					m_srcFacilityPath = path.getPath(selectItem);
					m_srcFacilityId = info.getFacilityId();
					m_srcScope.setText(m_srcFacilityPath);
				}
			}
		});

		// 転送：ファイル（ラベル）
		Label fileTitle = new Label(fileTransferFromgroup, SWT.NONE);
		fileTitle.setText(Messages.getString("file") + " : ");
		fileTitle.setLayoutData(new GridData(100,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// 転送：ファイル（テキスト）
		this.m_srcFile = new Text(fileTransferFromgroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_srcFile", this.m_srcFile);
		this.m_srcFile.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_srcFile.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_4096));
		this.m_srcFile.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 受信（グループ）
		Group fileTransferTogroup = new Group(this, SWT.NONE);
		fileTransferTogroup.setText(Messages.getString("forward.destination"));
		fileTransferTogroup.setLayout(new GridLayout(3, false));

		// 受信：スコープ（ラベル）
		Label destScopeTitle = new Label(fileTransferTogroup, SWT.NONE);
		destScopeTitle.setText(Messages.getString("scope") + " : ");
		destScopeTitle.setLayoutData(new GridData(100,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// 受信：スコープ（テキスト）
		this.m_destScope = new Text(fileTransferTogroup, SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_destScope", m_destScope);
		this.m_destScope.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_destScope.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 受信：スコープ参照（ボタン）
		m_destScopeSelect = new Button(fileTransferTogroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_destScopeSelect", m_destScopeSelect);
		m_destScopeSelect.setText(Messages.getString("refer"));
		m_destScopeSelect.setLayoutData(new GridData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		m_destScopeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(m_shell, m_managerName, m_ownerRoleId);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItem selectItem = dialog.getSelectItem();
					FacilityInfo info = selectItem.getData();
					FacilityPath path = new FacilityPath(
							ClusterControlPlugin.getDefault()
							.getSeparator());
					m_destFacilityPath = path.getPath(selectItem);
					m_destFacilityId = info.getFacilityId();
					m_destScope.setText(m_destFacilityPath);
				}
			}
		});

		// 受信：受信ノード（グループ）
		Group methodGroup = new Group(fileTransferTogroup, SWT.NONE);
		methodGroup.setText(Messages.getString("process.method"));
		methodGroup.setLayout(new RowLayout());
		methodGroup.setLayoutData(new GridData());
		((GridData)methodGroup.getLayoutData()).horizontalSpan = 3;

		// 受信：受信ノード：全てのノード（ラジオ）
		m_allNode = new Button(methodGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_allNode", m_allNode);
		m_allNode.setText(Messages.getString("forward.all.nodes"));
		m_allNode.setLayoutData(
				new RowData(150, SizeConstant.SIZE_BUTTON_HEIGHT));

		// ラジオボタン配置調整用の空Composite
		JobDialogUtil.getComposite_Space(methodGroup, 70, SizeConstant.SIZE_BUTTON_HEIGHT);

		// 受信：受信ノード：１ノード（ラジオ）
		m_oneNode = new Button(methodGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_oneNode", m_oneNode);
		m_oneNode.setText(Messages.getString("forward.one.node"));
		m_oneNode.setLayoutData(
				new RowData(120, SizeConstant.SIZE_BUTTON_HEIGHT));

		// ラジオボタン配置調整用の空Composite
		JobDialogUtil.getComposite_Space(methodGroup, 130, SizeConstant.SIZE_BUTTON_HEIGHT);

		// 受信：ディレクトリ（ラベル）
		Label forwardDirTitle = new Label(fileTransferTogroup, SWT.NONE);
		forwardDirTitle.setText(Messages.getString("directory") + " : ");
		forwardDirTitle.setLayoutData(new GridData(100,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// 受信：ディレクトリ（テキスト）
		this.m_destDirectory = new Text(fileTransferTogroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_destDirectory", m_destDirectory);
		this.m_destDirectory.setLayoutData(new GridData(200,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_destDirectory.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_4096));
		this.m_destDirectory.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// dummy
		new Label(fileTransferTogroup, SWT.NONE);

		// ファイル転送時（Composite）
		Composite fileTransfarComposite = new Composite(this, SWT.NONE);
		fileTransfarComposite.setLayout(new RowLayout());

		// ファイル転送時：ファイル転送時に圧縮（チェック）
		this.m_compressionCondition = new Button(fileTransfarComposite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_compressionCondition", this.m_compressionCondition);
		this.m_compressionCondition.setText(Messages.getString("forward.compression.file"));
		this.m_compressionCondition.setLayoutData(new RowData(180,
				SizeConstant.SIZE_BUTTON_HEIGHT));

		// ラジオボタン配置調整用の空Composite
		JobDialogUtil.getComposite_Space(fileTransfarComposite, 40, SizeConstant.SIZE_BUTTON_HEIGHT);

		// ファイル転送時：転送ファイルのチェック（チェック）
		this.m_checkFileCondition = new Button(fileTransfarComposite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_checkFileCondition", this.m_checkFileCondition);
		this.m_checkFileCondition.setText(Messages.getString("forward.file.check"));
		this.m_checkFileCondition.setLayoutData(new RowData(220,
				SizeConstant.SIZE_BUTTON_HEIGHT));

		// 実効ユーザ（グループ）
		Group fileEffectiveUserGroup = new Group(this, SWT.NONE);
		fileEffectiveUserGroup.setText(Messages.getString("effective.user"));
		fileEffectiveUserGroup.setLayout(new GridLayout(2, false));

		// 実効ユーザ：エージェント起動ユーザ（ラジオ）
		this.m_agentUser = new Button(fileEffectiveUserGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_agentUser", this.m_agentUser);
		this.m_agentUser.setText(Messages.getString("agent.user"));
		this.m_agentUser.setLayoutData(
				new GridData(200, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_agentUser.getLayoutData()).horizontalSpan = 2;
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
		this.m_specifyUser = new Button(fileEffectiveUserGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_specifyUser", this.m_specifyUser);
		this.m_specifyUser.setText(Messages.getString("specified.user"));
		this.m_specifyUser.setLayoutData(
				new GridData(200, SizeConstant.SIZE_BUTTON_HEIGHT));
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
		this.m_user = new Text(fileEffectiveUserGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_user", this.m_user);
		this.m_user.setLayoutData(new GridData(250, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_user.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		this.m_user.addModifyListener(new ModifyListener(){
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
		if("".equals(this.m_srcScope.getText())){
			this.m_srcScope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_srcScope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_srcFile.getText())){
			this.m_srcFile.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_srcFile.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_specifyUser.getSelection() && "".equals(this.m_user.getText())){
			this.m_user.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_user.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_destScope.getText())){
			this.m_destScope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_destScope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_destDirectory.getText())){
			this.m_destDirectory.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_destDirectory.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ジョブファイル転送情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobFileInfo
	 */
	public void reflectFileInfo() {

		// 初期値
		m_srcScope.setText("");
		m_srcFile.setText("");
		m_destScope.setText("");
		m_destDirectory.setText("");
		m_allNode.setSelection(true);
		m_compressionCondition.setSelection(false);
		m_checkFileCondition.setSelection(false);
		m_agentUser.setSelection(true);
		m_specifyUser.setSelection(false);
		m_user.setText("");
		m_user.setEditable(false);

		if (m_jobFileInfo != null) {
			//転送元スコープ設定
			m_srcFacilityPath = m_jobFileInfo.getSrcScope();
			m_srcFacilityId = m_jobFileInfo.getSrcFacilityID();
			if (m_srcFacilityPath != null && m_srcFacilityPath.length() > 0) {
				m_srcScope.setText(m_srcFacilityPath);
			}
			//転送元ファイル設定
			if (m_jobFileInfo.getSrcFile() != null
					&& m_jobFileInfo.getSrcFile().length() > 0) {
				m_srcFile.setText(m_jobFileInfo.getSrcFile());
			}
			//転送先スコープ設定
			m_destFacilityPath = HinemosMessage.replace(m_jobFileInfo.getDestScope());
			m_destFacilityId = m_jobFileInfo.getDestFacilityID();
			if (m_destFacilityPath != null && m_destFacilityPath.length() > 0) {
				m_destScope.setText(m_destFacilityPath);
			}
			//転送先ファイル設定
			if (m_jobFileInfo.getDestDirectory() != null
					&& m_jobFileInfo.getDestDirectory().length() > 0) {
				m_destDirectory.setText(m_jobFileInfo.getDestDirectory());
			}
			//処理方法設定
			if (m_jobFileInfo.getProcessingMethod() == ProcessingMethodConstant.TYPE_ALL_NODE) {
				m_allNode.setSelection(true);
				m_oneNode.setSelection(false);
			} else {
				m_allNode.setSelection(false);
				m_oneNode.setSelection(true);
			}
			//ファイル圧縮
			m_compressionCondition.setSelection(m_jobFileInfo.isCompressionFlg());
			//整合性チェック
			m_checkFileCondition.setSelection(m_jobFileInfo.isCheckFlg());
			//ユーザー設定
			if (m_jobFileInfo.isSpecifyUser().booleanValue()) {
				m_specifyUser.setSelection(true);
				m_agentUser.setSelection(false);
				m_user.setEditable(true);
			} else {
				m_specifyUser.setSelection(false);
				m_agentUser.setSelection(true);
				m_user.setEditable(false);
			}
			if (m_jobFileInfo.getUser() != null && m_jobFileInfo.getUser().length() > 0) {
				m_user.setText(m_jobFileInfo.getUser());
			}
		}
	}

	/**
	 * ジョブファイル転送情報を設定します。
	 *
	 * @param jobFileInfo ジョブファイル転送情報
	 */
	public void setFileInfo(JobFileInfo jobFileInfo) {
		m_jobFileInfo = jobFileInfo;
	}

	/**
	 * ジョブファイル転送情報を返します。
	 *
	 * @return ジョブファイル転送情報
	 */
	public JobFileInfo getFileInfo() {
		return m_jobFileInfo;
	}

	/**
	 * コンポジットの情報から、ジョブファイル転送情報を作成する。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobFileInfo
	 */
	public ValidateResult createFileInfo() {
		ValidateResult result = null;

		//ファイル転送情報クラスのインスタンスを作成・取得
		m_jobFileInfo = new JobFileInfo();

		//転送元スコープ取得
		if (m_srcFacilityId != null && m_srcFacilityId.length() > 0) {
			m_jobFileInfo.setSrcFacilityID(m_srcFacilityId);
			m_jobFileInfo.setSrcScope(m_srcFacilityPath);
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("forward.source") +
					Messages.getString("message.hinemos.3"));
			return result;
		}

		//転送元ファイル
		if (m_srcFile.getText().length() > 0) {
			m_jobFileInfo.setSrcFile(m_srcFile.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("forward.source") +
					Messages.getString("message.job.45"));
			return result;
		}

		//転送元作業ディレクトリ
		m_jobFileInfo.setSrcWorkDir("");

		//転送先スコープ取得
		if (m_destFacilityId != null && m_destFacilityId.length() > 0) {
			m_jobFileInfo.setDestFacilityID(m_destFacilityId);
			m_jobFileInfo.setDestScope(m_destFacilityPath);
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("forward.destination") +
					Messages.getString("message.hinemos.3"));
			return result;
		}

		//転送先ディレクトリ
		if (m_destDirectory.getText().length() > 0) {
			m_jobFileInfo.setDestDirectory(m_destDirectory.getText());

			// 転送先ディレクトリが指定されていない場合
		} else if (m_destDirectory.getText().length() <= 0) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("forward.destination") +
					Messages.getString("message.job.46"));
			return result;

		}


		//転送先作業ディレクトリ
		m_jobFileInfo.setDestWorkDir("");

		//処理方法取得
		if (m_allNode.getSelection()) {
			m_jobFileInfo.setProcessingMethod(
					ProcessingMethodConstant.TYPE_ALL_NODE);
		} else {
			m_jobFileInfo.setProcessingMethod(
					ProcessingMethodConstant.TYPE_RETRY);
		}

		//ファイル圧縮
		m_jobFileInfo.setCompressionFlg(m_compressionCondition.getSelection());

		//整合性チェック
		m_jobFileInfo.setCheckFlg(m_checkFileCondition.getSelection());

		//ユーザー取得
		if (m_agentUser.getSelection()) {
			m_jobFileInfo.setSpecifyUser(false);
		} else {
			if (m_user.getText().length() > 0) {
				m_jobFileInfo.setSpecifyUser(true);
				m_jobFileInfo.setUser(m_user.getText());
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.5"));
				return result;
			}
		}

		return null;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
		this.m_destScope.setText("");
		this.m_destFacilityId = null;
		this.m_srcScope.setText("");
		this.m_srcFacilityId = null;
	}

	public void setManagerName(String managerName) {
		this.m_managerName = managerName;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_srcScope.setEditable(false);
		m_srcFile.setEditable(enabled);
		m_destScope.setEditable(false);
		m_destDirectory.setEditable(enabled);
		m_agentUser.setEnabled(enabled);
		m_specifyUser.setEnabled(enabled);
		m_user.setEditable(m_specifyUser.getSelection() && enabled);
		m_srcScopeSelect.setEnabled(enabled);
		m_destScopeSelect.setEnabled(enabled);
		m_allNode.setEnabled(enabled);
		m_oneNode.setEnabled(enabled);
		m_compressionCondition.setEnabled(enabled);
		m_checkFileCondition.setEnabled(enabled);
	}
}
