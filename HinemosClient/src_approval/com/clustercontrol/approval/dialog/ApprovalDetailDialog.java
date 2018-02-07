/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.approval.dialog;

import java.util.Date;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.JobApprovalResultConstant;
import com.clustercontrol.bean.JobApprovalStatusConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.OperationMessage;
import com.clustercontrol.jobmanagement.action.OperationJob;
import com.clustercontrol.jobmanagement.bean.JobApprovalResultMessage;
import com.clustercontrol.jobmanagement.bean.JobApprovalStatusMessage;
import com.clustercontrol.jobmanagement.bean.JobOperationConstant;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.ws.jobmanagement.InvalidApprovalStatus_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobApprovalInfo;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * 承認[詳細]を表示するダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class ApprovalDetailDialog extends CommonDialog {
	
	// ログ
	private static Log m_log = LogFactory.getLog( ApprovalDetailDialog.class );
	
	private JobApprovalInfo approvalInfo;

	/** 承認状態 テキストボックス */
	private Text approvalStatusText = null;

	/** 承認結果 テキストボックス */
	private Text approvalResultText = null;

	/** ジョブセッションID テキストボックス */
	private Text jobSessionIdText = null;

	/** ジョブユニットID テキストボックス */
	private Text jobunitIdText = null;
	
	/** ジョブID テキストボックス */
	private Text jobIdText = null;

	/** ジョブ名 テキストボックス */
	private Text jobNameText = null;

	/** 実行ユーザ テキストボックス */
	private Text requestUserText = null;

	/** 承認ユーザ テキストボックス */
	private Text approvalUserText = null;

	/** 承認依頼日時 テキストボックス */
	private Text approvalRequestTimeText = null;

	/** 承認完了日時 テキストボックス */
	private Text approvalCompletionTimeText = null;
	
	/** 承認依頼文 テキストボックス */
	private Text requestSentenceText = null;
	
	/** コメント テキストボックス */
	private Text approvalCommentText = null;
	
	/** 承認(true)/否認(false)結果 */
	private Boolean isApprove = null;
	
	/** シェル */
	private Shell m_shell = null;

	/**
	 * コンストラクタ
	 * 作成時
	 * @param parent 親シェル
	 */
	public ApprovalDetailDialog(Shell parent, JobApprovalInfo info) {
		super(parent);
		this.approvalInfo = info;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_shell = this.getShell();

		parent.getShell().setText(Messages.getString("dialog.approval.detail"));
		/**
		 * レイアウト設定
		 * ダイアログ内のベースとなるレイアウトが全てを変更
		 */
		GridLayout baseLayout = new GridLayout(2, true);
		baseLayout.marginWidth = 5;
		baseLayout.marginHeight = 5;
		baseLayout.numColumns = 2;
		//一番下のレイヤー
		parent.setLayout(baseLayout);

		Composite composite = null;
		GridData gridData= null;

		/*
		 * ダイアログ
		 */
		composite = new Composite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, composite);
		GridLayout layout = new GridLayout(2, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns =2;
		composite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);
		
		//承認状態（ラベル）
		Label approvalStatusLabel = new Label(composite, SWT.NONE);
		approvalStatusLabel.setText(Messages.getString("approval.status"));
		approvalStatusLabel.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));
		
		//承認状態（テキスト）
		this.approvalStatusText =new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "approvalStatusText", this.approvalStatusText);
		this.approvalStatusText.setLayoutData(new GridData(160, SizeConstant.SIZE_TEXT_HEIGHT));
		this.approvalStatusText.setEditable(false);
		
		//承認結果（ラベル）
		Label approvalResultLabel = new Label(composite, SWT.NONE);
		approvalResultLabel.setText(Messages.getString("approval.result"));
		approvalResultLabel.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));
		
		//承認結果（テキスト）
		this.approvalResultText =new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "approvalResultText", this.approvalResultText);
		this.approvalResultText.setLayoutData(new GridData(160, SizeConstant.SIZE_TEXT_HEIGHT));
		this.approvalResultText.setEditable(false);

		//ジョブセッションID（ラベル）
		Label jobSessionIdLabel = new Label(composite, SWT.NONE);
		jobSessionIdLabel.setText(Messages.getString("session.id"));
		jobSessionIdLabel.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));
		
		//ジョブセッションID（テキスト）
		this.jobSessionIdText =new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "jobSessionIdText", this.jobSessionIdText);
		this.jobSessionIdText.setLayoutData(new GridData(160, SizeConstant.SIZE_TEXT_HEIGHT));
		this.jobSessionIdText.setEditable(false);

		//ジョブユニットID（ラベル）
		Label jobunitIdLabel = new Label(composite, SWT.NONE);
		jobunitIdLabel.setText(Messages.getString("jobunit.id"));
		jobunitIdLabel.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));
		
		//ジョブユニットID（テキスト）
		this.jobunitIdText =new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "jobunitIdText", this.jobunitIdText);
		this.jobunitIdText.setLayoutData(new GridData(160, SizeConstant.SIZE_TEXT_HEIGHT));
		this.jobunitIdText.setEditable(false);

		//ジョブID（ラベル）
		Label jobIdLabel = new Label(composite, SWT.NONE);
		jobIdLabel.setText(Messages.getString("job.id"));
		jobIdLabel.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));
		
		//ジョブID（テキスト）
		this.jobIdText =new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "jobIdText", this.jobIdText);
		this.jobIdText.setLayoutData(new GridData(160, SizeConstant.SIZE_TEXT_HEIGHT));
		this.jobIdText.setEditable(false);

		//ジョブ名（ラベル）
		Label jobNameLabel = new Label(composite, SWT.NONE);
		jobNameLabel.setText(Messages.getString("job.name"));
		jobNameLabel.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));
		
		//ジョブ名（テキスト）
		this.jobNameText =new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "jobNameText", this.jobNameText);
		this.jobNameText.setLayoutData(new GridData(160, SizeConstant.SIZE_TEXT_HEIGHT));
		this.jobNameText.setEditable(false);

		//実行ユーザ（ラベル）
		Label exeUserLabel = new Label(composite, SWT.NONE);
		exeUserLabel.setText(Messages.getString("approval.request.user"));
		exeUserLabel.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));
		
		//実行ユーザ（テキスト）
		this.requestUserText =new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "requestUserText", this.requestUserText);
		this.requestUserText.setLayoutData(new GridData(160, SizeConstant.SIZE_TEXT_HEIGHT));
		this.requestUserText.setEditable(false);

		//承認ユーザ（ラベル）
		Label approvalUserLabel = new Label(composite, SWT.NONE);
		approvalUserLabel.setText(Messages.getString("approval.user"));
		approvalUserLabel.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));
		
		//承認ユーザ（テキスト）
		this.approvalUserText =new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "approvalUserText", this.approvalUserText);
		this.approvalUserText.setLayoutData(new GridData(160, SizeConstant.SIZE_TEXT_HEIGHT));
		this.approvalUserText.setEditable(false);

		//承認依頼日時（ラベル）
		Label approvalRequestTimeLabel = new Label(composite, SWT.NONE);
		approvalRequestTimeLabel.setText(Messages.getString("approval.request.time"));
		approvalRequestTimeLabel.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));
		
		//承認依頼日時（テキスト）
		this.approvalRequestTimeText =new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "approvalRequestTimeText", this.approvalRequestTimeText);
		this.approvalRequestTimeText.setLayoutData(new GridData(160, SizeConstant.SIZE_TEXT_HEIGHT));
		this.approvalRequestTimeText.setEditable(false);

		//承認依頼日時（ラベル）
		Label approvalCompletionTimeLabel = new Label(composite, SWT.NONE);
		approvalCompletionTimeLabel.setText(Messages.getString("approval.completion.time"));
		approvalCompletionTimeLabel.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));
		
		//承認依頼日時（テキスト）
		this.approvalCompletionTimeText =new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "approvalCompletionTimeText", this.approvalCompletionTimeText);
		this.approvalCompletionTimeText.setLayoutData(new GridData(160, SizeConstant.SIZE_TEXT_HEIGHT));
		this.approvalCompletionTimeText.setEditable(false);

		//承認依頼文（ラベル）
		Label requestSentenceLabel = new Label(composite, SWT.NONE);
		requestSentenceLabel.setText(Messages.getString("approval.request.sentence"));
		requestSentenceLabel.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));
		
		//dummy
		new Label(composite, SWT.NONE);
		
		//承認依頼文（テキスト）
		this.requestSentenceText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "requestSentenceText", this.requestSentenceText);
		gridData = new GridData(240, 200);
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.requestSentenceText.setLayoutData(gridData);
		this.requestSentenceText.setEditable(false);
		
		//コメント（ラベル）
		Label approvalCommentLabel = new Label(composite, SWT.NONE);
		approvalCommentLabel.setText(Messages.getString("comment"));
		approvalCommentLabel.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));
		
		//dummy
		new Label(composite, SWT.NONE);
		
		//コメント（テキスト）
		this.approvalCommentText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "approvalCommentText", this.approvalCommentText);
		gridData = new GridData(240, SizeConstant.SIZE_TEXTFIELD_HEIGHT);
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.approvalCommentText.setLayoutData(gridData);
		this.approvalCommentText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		//承認/否認/コメント登録/キャンセルのボタン生成は、親クラスからのcreateButtonBar()呼び出しで行う
//		createButtonBar(composite);
		
		// ダイアログを調整
		this.adjustDialog();
		
		if(approvalInfo != null){
			this.setInputData();
			// 承認可能な状態でなければコメント欄は更新させないようにする
			//(承認権限がなければコメント更新もさせないため)
			if(approvalInfo.getStatus() != JobApprovalStatusConstant.TYPE_PENDING &&
				approvalInfo.getStatus() != JobApprovalStatusConstant.TYPE_SUSPEND){
				approvalCommentText.setEnabled(false);
			}
		}
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		// create a layout with spacing and margins appropriate for the font size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 0; // this is incremented by createButton
		layout.makeColumnsEqualWidth = true;
		layout.marginTop = 0;
		layout.marginBottom = 5;
		layout.marginWidth = convertHorizontalDLUsToPixels(2);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(2);
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END
				| GridData.VERTICAL_ALIGN_CENTER);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		
		if(approvalInfo != null){
			if(approvalInfo.getStatus() == JobApprovalStatusConstant.TYPE_PENDING ||
				approvalInfo.getStatus() == JobApprovalStatusConstant.TYPE_SUSPEND){
				createButtonsForApprovalButtonBar(composite);
			}else if(approvalInfo.getStatus() == JobApprovalStatusConstant.TYPE_STILL){
				createButtonsForStopButtonBar(composite);
			}else{
				createButtonsForCancelButtonBar(composite);
			}
		}
		
		return composite;
	}
	
	private void createButtonsForApprovalButtonBar(Composite parent) {
		//承認
		createButton(parent, IDialogConstants.YES_ID, Messages.getString("approval.approve"), false);
		//否認
		createButton(parent, IDialogConstants.NO_ID, Messages.getString("approval.deny"), false);
		//コメント登録
		createButton(parent, IDialogConstants.OK_ID, Messages.getString("approval.comment.registration"), false);
		//キャンセル
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("cancel"), false);
	}
	
	private void createButtonsForStopButtonBar(Composite parent) {
		//取り下げボタン
		createButton(parent, IDialogConstants.STOP_ID, Messages.getString("approval.stop"), false);
		//キャンセル
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("cancel"), false);
	}
	
	private void createButtonsForCancelButtonBar(Composite parent) {
		//キャンセル
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("cancel"), false);
	}
	
	@Override
	protected void setButtonLayoutData(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = convertHorizontalDLUsToPixels(32);
		Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(widthHint, minSize.x);
		button.setLayoutData(data);
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.YES_ID == buttonId) {
			OperationApprove(true);
		} else if (IDialogConstants.NO_ID == buttonId) {
			OperationApprove(false);
		} else if (IDialogConstants.OK_ID == buttonId) {
			okPressed();
		} else if (IDialogConstants.CANCEL_ID == buttonId) {
			cancelPressed();
		} else if (IDialogConstants.STOP_ID == buttonId) {
			OperationStop(approvalInfo);
		}
	}
	
	private void setInputData(){
		
		if(approvalInfo.getStatus() != null){
			approvalStatusText.setText(JobApprovalStatusMessage.typeToString(approvalInfo.getStatus()));
		}
		if(approvalInfo.getResult() != null){
			approvalResultText.setText(JobApprovalResultMessage.typeToString(approvalInfo.getResult()));
		}
		if(approvalInfo.getSessionId() != null){
			jobSessionIdText.setText(approvalInfo.getSessionId());
		}
		if(approvalInfo.getJobunitId() != null){
			jobunitIdText.setText(approvalInfo.getJobunitId());
		}
		if(approvalInfo.getJobId() != null){
			jobIdText.setText(approvalInfo.getJobId());
		}
		if(approvalInfo.getJobName() != null){
			jobNameText.setText(approvalInfo.getJobName());
		}
		if(approvalInfo.getRequestUser() != null){
			requestUserText.setText(approvalInfo.getRequestUser());
		}
		if(approvalInfo.getApprovalUser() != null){
			approvalUserText.setText(approvalInfo.getApprovalUser());
		}
		if(approvalInfo.getStartDate() != null){
			approvalRequestTimeText.setText(TimezoneUtil.getSimpleDateFormat().format(new Date(approvalInfo.getStartDate())));
		}
		if(approvalInfo.getEndDate() != null){
			approvalCompletionTimeText.setText(TimezoneUtil.getSimpleDateFormat().format(new Date(approvalInfo.getEndDate())));
		}
		if(approvalInfo.getRequestSentence() != null){
			requestSentenceText.setText(approvalInfo.getRequestSentence());
		}
		if(approvalInfo.getComment() != null){
			approvalCommentText.setText(approvalInfo.getComment());
		}
	}
	
	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		m_shell.pack();
		m_shell.setSize(new Point(400, m_shell.getSize().y));

		// 画面中央に配置
		Display display = m_shell.getDisplay();
		m_shell.setLocation((display.getBounds().width - m_shell.getSize().x) / 2,
				(display.getBounds().height - m_shell.getSize().y) / 2);
	}
	/**
	 * 更新処理
	 *
	 */
	private void update(){
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("approval.comment.registration");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;
		
		Integer limit = DataRangeConstant.VARCHAR_1024;
		if (approvalCommentText.getText().length() > limit) {
			String[] args = { limit.toString() };
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.hinemos.7", args ));
		} else {
			approvalInfo.setComment(approvalCommentText.getText());
		}
		return result;
	}

	/**
	 * 停止用プロパティ取得
	 *
	 * @param managerName マネージャ名
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @return ジョブ停止操作用プロパティ
	 *
	 */
	private Property getStopProperty(JobApprovalInfo info) {
		Locale locale = Locale.getDefault();

		//セッションID
		Property session =
				new Property(JobOperationConstant.SESSION, Messages.getString("session.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		session.setValue(info.getSessionId());

		//ジョブユニットID
		Property jobUnit =
				new Property(JobOperationConstant.JOB_UNIT, Messages.getString("jobunit.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		jobUnit.setValue(info.getJobunitId());

		//ジョブID
		Property job =
				new Property(JobOperationConstant.JOB, Messages.getString("job.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		job.setValue(info.getJobId());

		//制御
		Property control =
				new Property(JobOperationConstant.CONTROL, Messages.getString("control", locale), PropertyDefineConstant.EDITOR_TEXT);
		control.setValue(OperationMessage.STRING_STOP_AT_ONCE);
		
		//変更の可/不可を設定
		session.setModify(PropertyDefineConstant.MODIFY_NG);
		jobUnit.setModify(PropertyDefineConstant.MODIFY_NG);
		job.setModify(PropertyDefineConstant.MODIFY_NG);
		control.setModify(PropertyDefineConstant.MODIFY_NG);

		// 初期表示ツリーを構成。
		Property property = new Property(null, null, "");
		property.removeChildren();
		property.addChildren(session);
		property.addChildren(jobUnit);
		property.addChildren(job);
		property.addChildren(control);

		return property;
	}
	
	private void OperationStop(JobApprovalInfo info) {
		
		if (MessageDialog.openQuestion(null, Messages.getString("confirmed"), Messages.getString("message.approval.5"))) {
			if (info.getSessionId() != null && info.getSessionId().length() > 0 && info.getJobunitId() != null
					&& info.getJobunitId().length() > 0 && info.getJobId() != null&& info.getJobId().length() > 0) {
				
				//プロパティ設定
				Property prop = getStopProperty(info);
				
				//ジョブ停止
				OperationJob operation = new OperationJob();
				boolean result = operation.operationJob(info.getMangerName(), prop);
				
				if(result){
					MessageDialog.openInformation(null, Messages.getString("confirmed"),Messages.getString("message.approval.6"));
				}
				close();
			}
		}
	}
	
	private void OperationApprove(boolean bool) {
		if(bool){
			//承認
			if (MessageDialog.openQuestion(null, Messages.getString("confirmed"), Messages.getString("message.approval.1"))) {
				isApprove = true;
				approvalInfo.setResult(JobApprovalResultConstant.TYPE_APPROVAL);
				okPressed();
			}
		}else{
			//却下
			if (MessageDialog.openQuestion(null, Messages.getString("confirmed"), Messages.getString("message.approval.3"))) {
				isApprove = false;
				approvalInfo.setResult(JobApprovalResultConstant.TYPE_DENIAL);
				okPressed();
			}
		}
	}

	@Override
	protected boolean action() {
		boolean result = false;
		
		String errMsg = null;
		JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(approvalInfo.getMangerName());
		try {
			wrapper.modifyApprovalInfo(approvalInfo, isApprove);
			result = true;
		} catch (InvalidRole_Exception e) {
			m_log.warn("action() modifyApprovalInfo, " + e.getMessage());
			errMsg = Messages.getString("message.accesscontrol.16");
		} catch (InvalidApprovalStatus_Exception e) {
			m_log.warn("action() modifyApprovalInfo, " + e.getMessage());
			errMsg = e.getMessage();
		} catch (Exception e) {
			m_log.error("action() modifyApprovalInfo, " + e.getMessage());
			errMsg = Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage());
		}
		if (result) {
			if(isApprove != null){
				String msg;
				if(isApprove == true){
					msg = Messages.getString("message.approval.2") + "("+ approvalInfo.getMangerName() +")";
				} else {
					msg = Messages.getString("message.approval.4") + "("+ approvalInfo.getMangerName() +")";
				}
				MessageDialog.openInformation(null,
						Messages.getString("confirmed"),msg);
			}
		} else {
			MessageDialog.openInformation(null,
					Messages.getString("failed"),
					errMsg);
		}
		isApprove = null;
		return result;
	}
}
