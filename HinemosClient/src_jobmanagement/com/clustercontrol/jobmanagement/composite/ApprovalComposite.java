/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.TextWithParameterComposite;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;


/**
 *承認タブ用のコンポジットクラスです。
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class ApprovalComposite extends Composite {

	/**  承認依頼先ロール コンボボックス */
	private Combo m_approvalReqRoleCombo = null;
	
	/**  承認依頼先ユーザ コンボボックス */
	private Combo m_approvalReqUserCombo = null;
	
	/** 承認依頼文 テキストボックス */
	private TextWithParameterComposite m_requestSentenceText = null;
	
	/** 承認依頼メール件名 テキストボックス */
	private TextWithParameterComposite m_approvalReqMailTitleText = null;
	
	/** 承認依頼文利用有無 チェックボタン */
	private Button m_approvalReqMailBodyCondition = null;
	
	/** 承認依頼メール本文 テキストボックス */
	private TextWithParameterComposite m_approvalReqMailBodyText = null;
	
	/** マネージャ名 */
	private String m_managerName = null;
	
	/** 承認依頼先ロールID */
	private String m_approvalReqRoleId = null;
	
	/** 承認依頼先ユーザID */
	private String m_approvalReqUserId = null;
	
	/** 承認依頼文 */
	private String m_approvalReqSentence = null;
	
	/** 承認依頼メール件名 */
	private String m_approvalReqMailTitle = null;
	
	/** 承認依頼メール本文 */
	private String m_approvalReqMailBody = null;
	
	/** 承認依頼文の利用有無 */
	private Boolean m_isUseApprovalReqSentence = null;
	
	/** ジョブツリー情報*/
	private JobTreeItem m_jobTreeItem = null;
	
	/**  承認画面へのリンク先アドレス */
	private String m_approvalPageLink = null;
	
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
	public ApprovalComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.m_managerName = managerName;
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		
		this.setLayout(JobDialogUtil.getParentLayout());
		
		// Composite
		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(2,false));
		
		//承認依頼先ロール（ラベル）
		Label approvalReqRoleLabel = new Label(composite, SWT.NONE);
		approvalReqRoleLabel.setText(Messages.getString("approval.request.destination.role")+" :");
		approvalReqRoleLabel.setLayoutData(new GridData(250, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)approvalReqRoleLabel.getLayoutData()).horizontalSpan = 1;
		
		//承認依頼先ロール（コンボ）
		this.m_approvalReqRoleCombo = new Combo(composite, SWT.RIGHT | SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_approvalReqRoleCombo", this.m_approvalReqRoleCombo);
		this.m_approvalReqRoleCombo.setLayoutData(new GridData(250, SizeConstant.SIZE_COMBO_HEIGHT));
		((GridData)m_approvalReqRoleCombo.getLayoutData()).horizontalSpan = 1;
		this.m_approvalReqRoleCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(m_approvalReqRoleCombo.getText() != null){
					getApprovalReqUserIdList(m_approvalReqRoleCombo.getText());
					//初期値はユーザ指定無しとして"*"を設定
					m_approvalReqUserCombo.setText("*");
				}
				update();
			}
		});
		
		//承認依頼先ユーザ（ラベル）
		Label approvalReqUserLabel = new Label(composite, SWT.NONE);
		approvalReqUserLabel.setText(Messages.getString("approval.request.destination.user")+" :");
		approvalReqUserLabel.setLayoutData(new GridData(250, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)approvalReqUserLabel.getLayoutData()).horizontalSpan = 1;
		
		//承認依頼先ユーザ（コンボ）
		this.m_approvalReqUserCombo = new Combo(composite, SWT.RIGHT | SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_approvalReqUserCombo", this.m_approvalReqUserCombo);
		this.m_approvalReqUserCombo.setLayoutData(new GridData(250, SizeConstant.SIZE_COMBO_HEIGHT));
		((GridData)m_approvalReqUserCombo.getLayoutData()).horizontalSpan = 1;
		this.m_approvalReqUserCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		
		JobDialogUtil.getGridSeparator(composite, 3);
		
		//承認依頼文（ラベル）
		Label approvalReqSentenceLabel = new Label(composite, SWT.NONE);
		approvalReqSentenceLabel.setText(Messages.getString("approval.request.sentence"));
		approvalReqSentenceLabel.setLayoutData(new GridData(250, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)approvalReqSentenceLabel.getLayoutData()).horizontalSpan = 2;
		
		//承認依頼文（テキスト）
		this.m_requestSentenceText = new TextWithParameterComposite(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "m_requestSentenceText", this.m_requestSentenceText);
		this.m_requestSentenceText.setLayoutData(new GridData(500, SizeConstant.SIZE_TEXTFIELD_HEIGHT));
		((GridData)m_requestSentenceText.getLayoutData()).horizontalSpan = 2;
		this.m_requestSentenceText.setColor(new Color(this.getDisplay(), new RGB(0, 0, 255)));
		this.m_requestSentenceText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				updateRequestSentence();
			}
		});
		
		JobDialogUtil.getGridSeparator(composite, 3);
		
		//承認依頼メール件名（ラベル）
		Label approvalReqMailTitleLabel = new Label(composite, SWT.NONE);
		approvalReqMailTitleLabel.setText(Messages.getString("approval.request.mail.title"));
		approvalReqMailTitleLabel.setLayoutData(new GridData(250, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)approvalReqMailTitleLabel.getLayoutData()).horizontalSpan = 2;
		
		//承認依頼メール件名（テキスト）
		this.m_approvalReqMailTitleText = new TextWithParameterComposite(composite, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "m_approvalReqMailTitleText", this.m_approvalReqMailTitleText);
		this.m_approvalReqMailTitleText.setLayoutData(new GridData(500, 22));
		((GridData)m_approvalReqMailTitleText.getLayoutData()).horizontalSpan = 2;
		this.m_approvalReqMailTitleText.setColor(new Color(this.getDisplay(), new RGB(0, 0, 255)));
		this.m_approvalReqMailTitleText.setInputUpper(DataRangeConstant.VARCHAR_256);
		this.m_approvalReqMailTitleText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		JobDialogUtil.getGridSeparator(composite, 3);
		
		//承認依頼メール本文（ラベル）
		Label approvalReqMailBodyLabel = new Label(composite, SWT.NONE);
		approvalReqMailBodyLabel.setText(Messages.getString("approval.request.mail.body"));
		approvalReqMailBodyLabel.setLayoutData(new GridData(250, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)approvalReqMailBodyLabel.getLayoutData()).horizontalSpan = 1;
		
		//承認依頼文の利用有無（チェック）
		this.m_approvalReqMailBodyCondition = new Button(composite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_approvalReqMailBodyCondition", this.m_approvalReqMailBodyCondition);
		this.m_approvalReqMailBodyCondition.setText(Messages.getString("approval.request.sentence.use"));
		this.m_approvalReqMailBodyCondition.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		this.m_approvalReqMailBodyCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateMailBody();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				updateMailBody();
			}
		});
		
		//承認依頼メール本文（テキスト）
		this.m_approvalReqMailBodyText= new TextWithParameterComposite(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "m_approvalReqMailBodyText", this.m_approvalReqMailBodyText);
		this.m_approvalReqMailBodyText.setLayoutData(new GridData(500, SizeConstant.SIZE_TEXTFIELD_HEIGHT));
		((GridData)m_approvalReqMailBodyText.getLayoutData()).horizontalSpan = 2;
		this.m_approvalReqMailBodyText.setColor(new Color(this.getDisplay(), new RGB(0, 0, 255)));
		this.m_approvalReqMailBodyText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				updateMailBody();
			}
		});
		
		m_approvalPageLink = getApprovalPageLink();
		
		reflectApprovalInfo();
	}

	@Override
	public void update() {
		// 必須項目を明示
		if("".equals(this.m_approvalReqRoleCombo.getText())){
			this.m_approvalReqRoleCombo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_approvalReqRoleCombo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_approvalReqUserCombo.getText())){
			this.m_approvalReqUserCombo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_approvalReqUserCombo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_approvalReqMailTitleText.getText())){
			this.m_approvalReqMailTitleText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_approvalReqMailTitleText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
	
	// スクロールが有効な、承認依頼文(m_requestSentenceText)、承認依頼メール本文(m_approvalReqMailBodyText)で
	// TextWithParameterCompositeクラスのsetEnabled()/setBackground()を呼ぶと
	// 連続する文字入力でテキストボックスの枠が点滅することがあるため、
	// 他の項目の文字入力で、承認依頼文/承認依頼メール本文の項目を点滅させないよう、他の項目のupdate()処理とは別にする
	public void updateRequestSentence() {
		if("".equals(m_requestSentenceText.getText())){
			m_requestSentenceText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			m_requestSentenceText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
	public void updateMailBody() {
		if(m_approvalReqMailBodyCondition.getSelection()){
			m_approvalReqMailBodyText.setEnabled(false);
		} else {
			m_approvalReqMailBodyText.setEnabled(true);
			if("".equals(m_approvalReqMailBodyText.getText())){
				m_approvalReqMailBodyText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			}else{
				m_approvalReqMailBodyText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}
	}

	/**
	 * 承認情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobCommandInfo
	 */
	public void reflectApprovalInfo() {
		
		//承認依頼先ロールIDコンボリスト設定
		if(m_jobTreeItem != null){
			getApprovalReqRoleIdList();
		}
		// 設定情報反映
		if(m_approvalReqRoleId != null && m_approvalReqRoleId.length() > 0){
			m_approvalReqRoleCombo.setText(m_approvalReqRoleId);
		}else if(m_jobTreeItem != null){
			//デフォルトではオーナーロールを設定
			m_approvalReqRoleCombo.setText(JobUtil.getTopJobUnitTreeItem(m_jobTreeItem).getData().getOwnerRoleId());
		}
		//承認依頼先ユーザIDコンボリスト設定
		if(m_approvalReqRoleCombo.getText() != null){
			getApprovalReqUserIdList(m_approvalReqRoleCombo.getText());
		}
		if(m_approvalReqUserId != null && m_approvalReqUserId.length() > 0){
			m_approvalReqUserCombo.setText(m_approvalReqUserId);
		}else{
			//デフォルトではユーザ指定無しとして"*"を設定
			m_approvalReqUserCombo.setText("*");
		}
		if(m_approvalReqSentence != null && m_approvalReqSentence.length() > 0){
			m_requestSentenceText.setText(m_approvalReqSentence);
		}
		if(m_approvalReqMailTitle != null && m_approvalReqMailTitle.length() > 0){
			m_approvalReqMailTitleText.setText(m_approvalReqMailTitle);
		}
		
		if(m_approvalReqRoleCombo.getText().length() <= 0 &&
			m_requestSentenceText.getText().length() <= 0 &&
			m_approvalReqMailTitleText.getText().length() <= 0){
			// 必須項目が未設定の場合、初回作成時とみなしhinemosプロパティでの設定値を取得
			// (承認依頼先ユーザはデフォルトで"*"が設定されているためチェック対象外)
			m_approvalReqMailBodyText.setText(m_approvalPageLink);
		}else
		if(m_approvalReqMailBody != null){
			// 必須項目が設定済みの場合、変更時とみなしDBの設定値を取得
			// メール本文は必須項目ではないため、空白の場合は空白を設定
			m_approvalReqMailBodyText.setText(m_approvalReqMailBody);
		}
		
		if(m_isUseApprovalReqSentence != null){
			m_approvalReqMailBodyCondition.setSelection(m_isUseApprovalReqSentence);
		}
		update();
		updateRequestSentence();
		updateMailBody();
	}
	
	
	/**
	 * コンポジットの情報から、ジョブコマンド情報を作成する。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobCommandInfo
	 */
	public ValidateResult createApprovalInfo() {
		
		ValidateResult result = null;
		
		//承認依頼先ロールID
		if (m_approvalReqRoleCombo.getText().length() > 0) {
			this.m_approvalReqRoleId = m_approvalReqRoleCombo.getText();
		}else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.149"));
			return result;
		}
		
		//承認依頼先ユーザID
		if (m_approvalReqUserCombo.getText().length() > 0) {
			this.m_approvalReqUserId = m_approvalReqUserCombo.getText();
		}else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.150"));
			return result;
		}
		
		//承認依頼文
		if (m_requestSentenceText.getText() != null
				&& !"".equals(m_requestSentenceText.getText().trim())) {
			this.m_approvalReqSentence = m_requestSentenceText.getText();
		}else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.165"));
			return result;
		}
		
		//承認依頼メール件名
		if (m_approvalReqMailTitleText.getText() != null
				&& !"".equals(m_approvalReqMailTitleText.getText().trim())) {
			this.m_approvalReqMailTitle = m_approvalReqMailTitleText.getText();
		}else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.151"));
			return result;
		}
		
		//承認依頼メール本文
		this.m_approvalReqMailBody = m_approvalReqMailBodyText.getText();
		//承認依頼文を利用しない場合は本文をチェックする
		if (!m_approvalReqMailBodyCondition.getSelection()){
			if(m_approvalReqMailBodyText.getText() == null
					|| "".equals(m_approvalReqMailBodyText.getText().trim())) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.152"));
				return result;
			}
		}
		//承認依頼文の利用有無
		m_isUseApprovalReqSentence = m_approvalReqMailBodyCondition.getSelection();
		
		return null;
	}


	/**
	 * @return the m_approvalReqRoleId
	 */
	public String getApprovalReqRoleId() {
		return m_approvalReqRoleId;
	}
	
	/**
	 * @param approvalReqRoleId
	 */
	public void setApprovalReqRoleId(String approvalReqRoleId) {
		this.m_approvalReqRoleId = approvalReqRoleId;
	}
	
	/**
	 * @return the m_approvalReqUserId
	 */
	public String getApprovalReqUserId() {
		return m_approvalReqUserId;
	}
	
	/**
	 * @param approvalReqUserId
	 */
	public void setApprovalReqUserId(String approvalReqUserId) {
		this.m_approvalReqUserId = approvalReqUserId;
	}
	
	/**
	 * @return the m_approvalReqSentence
	 */
	public String getApprovalReqSentence() {
		return m_approvalReqSentence;
	}
	
	/**
	 * @param approvalReqSentence
	 */
	public void setApprovalReqSentence(String approvalReqSentence) {
		this.m_approvalReqSentence = approvalReqSentence;
	}


	/**
	 * @return the m_approvalReqMailTitle
	 */
	public String getApprovalReqMailTitle() {
		return m_approvalReqMailTitle;
	}
	
	/**
	 * @param approvalReqMailTitle
	 */
	public void setApprovalReqMailTitle(String approvalReqMailTitle) {
		this.m_approvalReqMailTitle = approvalReqMailTitle;
	}


	/**
	 * @return the m_approvalReqMailBody
	 */
	public String getApprovalReqMailBody() {
		return m_approvalReqMailBody;
	}
	
	/**
	 * @param approvalReqMailBody
	 */
	public void setApprovalReqMailBody(String approvalReqMailBody) {
		this.m_approvalReqMailBody = approvalReqMailBody;
	}


	/**
	 * @return the m_isUseRequestSentence
	 */
	public Boolean isUseApprovalReqSentence() {
		return m_isUseApprovalReqSentence;
	}
	
	/**
	 * @param isUseApprovalReqSentence
	 */
	public void setUseApprovalReqSentence(Boolean isUseApprovalReqSentence) {
		this.m_isUseApprovalReqSentence = isUseApprovalReqSentence;
	}

	/**
	 * ジョブツリーを設定する。<BR>
	 * @param jobTreeItem
	 */
	public void setJobTreeItem(JobTreeItem jobTreeItem) {
		m_jobTreeItem = jobTreeItem;
	}
	
	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_approvalReqRoleCombo.setEnabled(enabled);
		m_approvalReqUserCombo.setEnabled(enabled);
		m_requestSentenceText.setEnabled(enabled);
		m_approvalReqMailTitleText.setEnabled(enabled);
		m_approvalReqMailBodyCondition.setEnabled(enabled);
		m_approvalReqMailBodyText.setEnabled(enabled);
		
		// TextWithParameterCompositeのテキストボックスは↑のsetEnabled()で
		// 背景色が白またはグレーアウトになるため、↓で必須項目のチェックと再描画を行う
		if(enabled){
			update();
			updateRequestSentence();
			updateMailBody();
		}
	}
	
	private String getApprovalPageLink() {
		
		String link = null;
		
		//モジュールとして登録されたジョブリスト情報を取得
		try {
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(m_managerName);
			link = wrapper.getApprovalPageLink();
		} catch (Exception e) {
			// 上記以外の例外
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			return null;
		}
		return link;
	}
	
	private void getApprovalReqRoleIdList() {
		
		List<String> list = null;
		
		JobTreeItem jobunitItem = JobUtil.getTopJobUnitTreeItem(m_jobTreeItem);
		String jobunitId = jobunitItem.getData().getJobunitId();
		
		//参照のオブジェクト権限を持つロールIDのリストを取得
		try {
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(m_managerName);
			list = wrapper.getRoleIdListWithReadObjectPrivilege(jobunitId);
		} catch (Exception e) {
			// 上記以外の例外
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			return;
		}
		//リストの先頭にはオーナーロールIDを設定
		this.m_approvalReqRoleCombo.add(jobunitItem.getData().getOwnerRoleId(), 0);
		
		if(list != null && !list.isEmpty()){
			for(String roleId : list){
				this.m_approvalReqRoleCombo.add(roleId);
			}
		}
		return;
	}
	
	private void getApprovalReqUserIdList(String roleId) {
		
		List<String> list = null;
		
		//指定のロールIDに属するユーザIDのリストを取得
		try {
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(m_managerName);
			list = wrapper.getUserIdListBelongToRoleId(roleId);
		} catch (Exception e) {
			// 上記以外の例外
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			return;
		}
		//指定のロールが切り替わるためリセット
		m_approvalReqUserCombo.removeAll();
		//リストの先頭にはユーザ指定無しとして"*"を設定
		this.m_approvalReqUserCombo.add("*", 0);
		
		if(list != null && !list.isEmpty()){
			for(String userId : list){
				this.m_approvalReqUserCombo.add(userId);
			}
		}
		return;
	}

}
