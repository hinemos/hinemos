/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.dialog.JobTreeDialog;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 参照タブ用のコンポジットクラスです。
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class ReferComposite extends Composite {

	/** 参照先ジョブユニットID テキストボックス */
	private Text m_textJobunitId = null;

	/** 参照先ジョブID テキストボックス */
	private Text m_textJobId = null;

	/** 参照ボタン */
	private Button m_buttonRefer = null;

	/** シェル */
	private Shell m_shell = null;

	/** 参照先ジョブユニットID*/
	private String m_referJobUnitId = null;

	/** 参照先ジョブID*/
	private String m_referJobId = null;

	/** ジョブツリー情報*/
	private JobTreeItemWrapper m_jobTreeItem = null;

	/** ジョブツリー用コンポジット */
	private JobTreeComposite m_jobTreeComposite = null;

	/** オーナーロールID */
	private String m_ownerRoleId = null;

	/** 参照ジョブ種別 */
	private JobInfoWrapper.TypeEnum m_referJobType = JobInfoWrapper.TypeEnum.REFERJOB;
	

	/*
	 * 参照ジョブ選択種別
	 * 0 : ジョブツリーからの選択
	 * 1 : 登録済みモジュールからの選択
	 */
	private JobInfoWrapper.ReferJobSelectTypeEnum  m_selectType = null;
	
	/** 「ジョブツリーからの選択」のラジオボタン */
	private Button m_selectFromJobTreeRadio = null;

	/** 「登録済みモジュールからの選択」のラジオボタン */
	private Button m_selectFromRegisteredModuleRadio = null;
	
	/** 登録済みモジュールのコンボボックス */
	private Combo m_ComboRegisteredModuleCombo = null;
	
	/** 登録済みモジュールのジョブリスト */
	List<JobInfoWrapper> registeredJobList= null;
	
	
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
	public ReferComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
		m_shell = this.getShell();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		this.setLayout(JobDialogUtil.getParentLayout());

		// Composite
		Composite referComposite = new Composite(this, SWT.NONE);
		referComposite.setLayout(new GridLayout(3, false));

		//「ジョブツリーからの選択」のラジオボタン
		this.m_selectFromJobTreeRadio = new Button(referComposite, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_selectFromJobTreeRadio", this.m_selectFromJobTreeRadio);
		this.m_selectFromJobTreeRadio.setText(Messages.getString("job.select.jobtree"));
		GridData gridDataRadio = new GridData();
		gridDataRadio.horizontalSpan = 3;
		gridDataRadio.horizontalAlignment = SWT.BEGINNING;
		gridDataRadio.grabExcessHorizontalSpace = true;
		this.m_selectFromJobTreeRadio.setLayoutData(gridDataRadio);
		// ラジオボタンのイベント
		this.m_selectFromJobTreeRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		
		// ジョブユニットID（ラベル）
		Label jobunitLabel = new Label(referComposite, SWT.NONE);
		jobunitLabel.setText(Messages.getString("jobunit.id"));

		// ジョブID（ラベル）
		Label jobLabel = new Label(referComposite, SWT.NONE);
		jobLabel.setText(Messages.getString("job.id"));

		// dummy
		new Label(referComposite, SWT.NONE);

		// ジョブユニットID（テキスト）
		this.m_textJobunitId = new Text(referComposite, SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_textJobunitId", this.m_textJobunitId);
		this.m_textJobunitId.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_textJobunitId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ジョブID（テキスト）
		this.m_textJobId = new Text(referComposite, SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_textJobId", this.m_textJobId);
		this.m_textJobId.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));

		// 参照（ボタン参照）
		this.m_buttonRefer = new Button(referComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_buttonRefer", this.m_buttonRefer);
		this.m_buttonRefer.setLayoutData(new GridData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_buttonRefer.setText(Messages.getString("refer"));
		this.m_buttonRefer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// ジョブツリーダイアログ表示
				JobTreeDialog dialog = new JobTreeDialog(m_shell, m_ownerRoleId, m_jobTreeItem, JobInfoWrapper.TypeEnum.REFERJOB, null);
				if (dialog.open() == IDialogConstants.OK_ID) {
					JobTreeItemWrapper selectItem = dialog.getSelectItem().isEmpty() ? null : dialog.getSelectItem().get(0);
					if (selectItem != null && selectItem.getData().getType() != JobInfoWrapper.TypeEnum.COMPOSITE) {
						m_textJobId.setText(selectItem.getData().getId());
						m_textJobunitId.setText(selectItem.getData().getJobunitId());
						if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
							m_referJobType = JobInfoWrapper.TypeEnum.REFERJOBNET;
						} else {
							m_referJobType = JobInfoWrapper.TypeEnum.REFERJOB;
						}
					} else {
						m_textJobId.setText("");
						m_textJobunitId.setText("");
					}
				}
			}
		});
		
		// dummy
		new Label(referComposite, SWT.NONE);
		
		//「登録済みモジュールからの選択」のラジオボタン
		this.m_selectFromRegisteredModuleRadio = new Button(referComposite, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_selectFromRegisteredModuleRadio", this.m_selectFromRegisteredModuleRadio);
		this.m_selectFromRegisteredModuleRadio.setText(Messages.getString("job.select.module"));
		gridDataRadio = new GridData();
		gridDataRadio.horizontalSpan = 3;
		gridDataRadio.horizontalAlignment = SWT.BEGINNING;
		gridDataRadio.grabExcessHorizontalSpace = true;
		this.m_selectFromRegisteredModuleRadio.setLayoutData(gridDataRadio);
		// ラジオボタンのイベント
		this.m_selectFromRegisteredModuleRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		
		//「登録済みモジュール」の選択コンボボックス
		this.m_ComboRegisteredModuleCombo = new Combo(referComposite, SWT.RIGHT | SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_ComboRegisteredModuleCombo", m_ComboRegisteredModuleCombo);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint = 385;
		this.m_ComboRegisteredModuleCombo.setLayoutData(gridData);
		//コンボ内のリスト情報は、参照ジョブ情報取得後のreflectReferInfo()内で設定
		this.m_ComboRegisteredModuleCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
				if (m_ComboRegisteredModuleCombo.getText() != null
						&& !"".equals(m_ComboRegisteredModuleCombo.getText().trim())) {
					JobInfoWrapper info = registeredJobList.get(m_ComboRegisteredModuleCombo.getSelectionIndex());
					if(info.getType() == JobInfoWrapper.TypeEnum.JOBNET){
						m_referJobType = JobInfoWrapper.TypeEnum.REFERJOBNET;
					} else {
						m_referJobType = JobInfoWrapper.TypeEnum.REFERJOB;
					}
				}
			}
		});
		
		reflectReferInfo();
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		//「ジョブツリーから選択」の場合
		if(m_selectFromJobTreeRadio.getSelection()){
			m_selectType = JobInfoWrapper.ReferJobSelectTypeEnum.JOB_TREE;
			m_textJobunitId.setEnabled(true);
			m_textJobId.setEnabled(true);
			m_buttonRefer.setEnabled(true);
			m_ComboRegisteredModuleCombo.setEnabled(false);
			
			// 必須項目を明示
			if("".equals(this.m_textJobunitId.getText())){
				this.m_textJobunitId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			}else{
				this.m_textJobunitId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
			if("".equals(this.m_textJobId.getText())){
				this.m_textJobId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			}else{
				this.m_textJobId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
			m_ComboRegisteredModuleCombo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			
		//「登録済みモジュールから選択」の場合
		} else if(m_selectFromRegisteredModuleRadio.getSelection()){
			m_selectType = JobInfoWrapper.ReferJobSelectTypeEnum .REGISTERED_MODULE;
			m_textJobunitId.setEnabled(false);
			m_textJobId.setEnabled(false);
			m_buttonRefer.setEnabled(false);
			m_ComboRegisteredModuleCombo.setEnabled(true);
			
			// 必須項目を明示
			if(("").equals(m_ComboRegisteredModuleCombo.getText())){
				m_ComboRegisteredModuleCombo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			}else {
				m_ComboRegisteredModuleCombo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
			this.m_textJobunitId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.m_textJobId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

	}

	/**
	 * 参照先ジョブユニットIDを返す。<BR>
	 * @return 参照先ジョブユニットID
	 */
	public String getReferJobUnitId() {
		return m_referJobUnitId;
	}
	/**
	 * 参照先ジョブユニットIDを設定する。<BR>
	 * @param referJobUnitId 参照先ジョブユニットID
	 */
	public void setReferJobUnitId(String referJobUnitId) {
		this.m_referJobUnitId = referJobUnitId;
	}
	/**
	 * 参照先ジョブIDを返す。<BR>
	 * @return 参照先ジョブID
	 */
	public String getReferJobId() {
		return m_referJobId;
	}
	/**
	 * 参照先ジョブIDを設定する。<BR>
	 * @param referJobId 参照先ジョブID
	 */
	public void setReferJobId(String referJobId) {
		this.m_referJobId = referJobId;
	}
	/**
	 * 参照ジョブ種別を返す。<BR>
	 * @return ジョブ種別(参照ジョブまたは参照ジョブネット)
	 */
	public JobInfoWrapper.TypeEnum getReferJobType() {
		return m_referJobType;
	}
	/**
	 * 参照ジョブ種別を設定する。<BR>
	 * @param referJobType ジョブ種別(参照ジョブまたは参照ジョブネット)
	 */
	public void setReferJobType(JobInfoWrapper.TypeEnum referJobType) {
		m_referJobType = referJobType;
	}
	/**
	 * 参照ジョブ選択種別を返す。<BR>
	 * @return 参照ジョブ選択種別
	 */
	public JobInfoWrapper.ReferJobSelectTypeEnum  getReferJobSelectType() {
		return m_selectType;
	}
	/**
	 * 参照ジョブ選択種別を設定する。<BR>
	 * @param selectType 参照ジョブ選択種別
	 */
	public void setReferJobSelectType(JobInfoWrapper.ReferJobSelectTypeEnum selectType) {
		m_selectType = selectType;
	}
	/**
	 * ジョブツリーアイテムを設定する。<BR>
	 * @param jobTreeItem
	 */
	public void setJobTreeItem(JobTreeItemWrapper jobTreeItem) {
		m_jobTreeItem = jobTreeItem;
	}

	/**
	 * ジョブツリーコンポジットを設定する。<BR>
	 *
	 * @param ジョブツリー用のコンポジット
	 */
	public void setJobTreeComposite(JobTreeComposite composite) {
		m_jobTreeComposite = composite;
	}
	
	/**
	 * 参照ジョブ情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobFileInfo
	 */
	public void reflectReferInfo() {
		//ラジオボタン設定
		if(m_selectType != null && m_selectType == JobInfoWrapper.ReferJobSelectTypeEnum .REGISTERED_MODULE){
			this.m_selectFromJobTreeRadio.setSelection(false);
			this.m_selectFromRegisteredModuleRadio.setSelection(true);
		}else{
			this.m_selectFromJobTreeRadio.setSelection(true);
			this.m_selectFromRegisteredModuleRadio.setSelection(false);
		}
		//「登録済みモジュールから選択」のコンボリスト設定
		if(m_jobTreeItem != null){
			registeredJobList = getRegisteredModule(m_jobTreeItem);
			if(registeredJobList != null && !registeredJobList.isEmpty()){
				for(JobInfoWrapper info : registeredJobList){
					//コンボリストにジョブID設定(コンボリスト順とジョブリスト順は同期させる)
					this.m_ComboRegisteredModuleCombo.add(info.getName() + "(" + info.getId() + ")");
				}
			}
		}
		//「ジョブツリーから選択」の場合
		if(m_selectFromJobTreeRadio.getSelection()){
			if(m_referJobUnitId != null && m_referJobUnitId.length() > 0){
				m_textJobunitId.setText(m_referJobUnitId);
			}
			if(m_referJobId != null && m_referJobId.length() > 0){
				m_textJobId.setText(m_referJobId);
			}
		//「登録済みモジュールから選択」の場合
		} else if(m_selectFromRegisteredModuleRadio.getSelection()){
			if(m_referJobId != null && m_referJobId.length() > 0 && registeredJobList != null){
				//ジョブリスト情報から該当するジョブIDを検索
				for(int i = 0; i < registeredJobList.size(); i++){
					if(m_referJobId.equals(registeredJobList.get(i).getId())){
						this.m_ComboRegisteredModuleCombo.select(i);
					}
				}
			}
		}
		update();
	}

	/**
	 * コンポジットの情報から、参照ジョブ情報を作成する。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobFileInfo
	 */
	public ValidateResult createReferInfo() {
		ValidateResult result = null;
		//「ジョブツリーから選択」の場合
		if(m_selectFromJobTreeRadio.getSelection()){
			// 参照先jobId
			if (m_textJobId.getText() != null
					&& !"".equals(m_textJobId.getText().trim())) {
				this.setReferJobId(m_textJobId.getText());
			}else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.100"));
				return result;
			}
			// 参照先jobunitId
			if (m_textJobunitId.getText() != null
					&& !"".equals(m_textJobunitId.getText().trim())) {
				this.setReferJobUnitId(m_textJobunitId.getText());
			}else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.99"));
				return result;
			}
		}
		//「登録済みモジュールから選択」の場合
		else if(m_selectFromRegisteredModuleRadio.getSelection()){
			if (m_ComboRegisteredModuleCombo.getText() != null
					&& !"".equals(m_ComboRegisteredModuleCombo.getText().trim())) {
				JobInfoWrapper info = registeredJobList.get(m_ComboRegisteredModuleCombo.getSelectionIndex());
				this.setReferJobId(info.getId());
				this.setReferJobUnitId(info.getJobunitId());
			}else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.100"));
				return result;
			}
		}
		return null;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
		this.m_textJobunitId.setText("");
		this.m_textJobId.setText("");
		this.m_referJobUnitId = null;
		this.m_referJobId = null;
		this.m_ComboRegisteredModuleCombo = null;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_textJobunitId.setEditable(false);
		m_textJobId.setEditable(false);
		m_selectFromJobTreeRadio.setEnabled(enabled);
		m_selectFromRegisteredModuleRadio.setEnabled(enabled);
		if(m_selectFromJobTreeRadio.getSelection()){
			m_buttonRefer.setEnabled(enabled);
		}else if(m_selectFromRegisteredModuleRadio.getSelection()){
			m_ComboRegisteredModuleCombo.setEnabled(enabled);
		}
	}
	
	/**
	 * 登録済みモジュール情報取得
	 *
	 * @param JobTreeItem ジョブ情報
	 * @return
	 */
	private List<JobInfoWrapper> getRegisteredModule(JobTreeItemWrapper item){
		List<JobInfoWrapper> jobList = new ArrayList<JobInfoWrapper>();
		
		//自身が所属するジョブユニット配下のJobTreeItemを取得する。
		JobTreeItemWrapper tree = m_jobTreeComposite.getJobTreeOneUnit(item);
		//ジョブユニット配下のJobTreeからモジュール登録済みのジョブ情報を取得する。
		JobUtil.getRegisteredJob(tree, jobList);
		
		return jobList;
	}
}
