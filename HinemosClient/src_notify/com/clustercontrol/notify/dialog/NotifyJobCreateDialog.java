/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.dialog;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobNotifyDetailInfoResponse;

import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.action.AddNotify;
import com.clustercontrol.notify.action.GetNotify;
import com.clustercontrol.notify.action.ModifyNotify;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.composite.NotifyJobDirectComposite;
import com.clustercontrol.notify.composite.NotifyJobLinkSendComposite;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.util.Messages;

/**
 * 通知（ジョブ実行）作成・変更ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 3.0.0
 */
public class NotifyJobCreateDialog extends NotifyBasicCreateDialog {


	// ----- instance フィールド ----- //
	/** 入力値の正当性を保持するオブジェクト。 */
	protected ValidateResult validateResult = null;

	// 実行モードグループ
	private Group m_execGroup = null;

	// 実行モード (直接実行)
	private Button m_notifyJobType_Direct = null;

	// 実行モード (ジョブ連携メッセージ送信)
	private Button m_notifyJobType_JobLinkSend = null;

	// Composite
	private Composite m_execParentComposite = null;
	private Composite m_execComposite = null;
	
	
	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public NotifyJobCreateDialog(Shell parent) {
		super(parent);
		parentDialog = this;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param notifyId 変更する通知情報の通知ID
	 * @param updateFlg 更新フラグ（true:更新する）
	 */
	public NotifyJobCreateDialog(Shell parent, String managerName, String notifyId, boolean updateFlg) {
		super(parent, managerName, notifyId, updateFlg);
		parentDialog = this;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 * @see com.clustercontrol.notify.action.GetNotify#getNotify(String)
	 * @see #setInputData(NotifyInfoInputData)
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		super.customizeDialog(parent);

		// 通知IDが指定されている場合、その情報を初期表示する。
		NotifyInfoInputData inputData;
		if(this.notifyId != null){
			inputData = new GetNotify().getJobNotify(this.managerName, this.notifyId);
		} else {
			inputData = new NotifyInfoInputData();
		}
		this.setInputData(inputData);

	}

	/**
	 * 親のクラスから呼ばれ、各通知用のダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 */
	@Override
	protected void customizeSettingDialog(Composite parent) {
		final Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.notify.job.create.modify"));

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 5;
		parent.setLayout(layout);

		// 実行モード
		m_execGroup = new Group(parent, SWT.NONE);
		m_execGroup.setText(Messages.getString("notify.job.exec.mode"));
		layout = new GridLayout(2, false);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		m_execGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_execGroup.setLayoutData(gridData);

		// 直接実行 ラジオボタン
		this.m_notifyJobType_Direct = new Button(m_execGroup, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.m_notifyJobType_Direct.setLayoutData(gridData);
		this.m_notifyJobType_Direct.setText(Messages.getString("notifies.job.direct") + " : ");
		this.m_notifyJobType_Direct.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				createExecComposite(check.getSelection());
			}
		});
		this.m_notifyJobType_Direct.setSelection(true);

		// ジョブ連携メッセージ送信 ラジオボタン
		this.m_notifyJobType_JobLinkSend = new Button(m_execGroup, SWT.RADIO);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		this.m_notifyJobType_JobLinkSend.setLayoutData(gridData);
		this.m_notifyJobType_JobLinkSend.setText(Messages.getString("notifies.job.joblinksend") + " : ");
		this.m_notifyJobType_JobLinkSend.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				createExecComposite(!check.getSelection());
			}
		});
		this.m_notifyJobType_JobLinkSend.setSelection(false);

		// 実行モード親Composite
		this.m_execParentComposite = new Composite(parent, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		m_execParentComposite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_execParentComposite.setLayoutData(gridData);

		// 実行モードComposite
		this.m_execComposite = new NotifyJobDirectComposite(m_execParentComposite, SWT.NONE);
		setExecCompositeLayout(this.m_execComposite);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示
		m_execComposite.update();
	}
	/**
	 * 入力値を保持した通知情報を返します。
	 *
	 * @return 通知情報
	 */
	@Override
	public NotifyInfoInputData getInputData() {
		return this.inputData;
	}

	/**
	 * 引数で指定された通知情報の値を、各項目に設定します。
	 *
	 * @param notify 設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(NotifyInfoInputData notify) {
		super.setInputData(notify);

		// コマンド情報
		JobNotifyDetailInfoResponse info = notify.getNotifyJobInfo();
		if (info != null) {
			setInputDatail(info);
		} else {
			// 新規追加の場合
			m_notifyJobType_Direct.setSelection(true);
			m_notifyJobType_JobLinkSend.setSelection(false);
		}
		update();
	}

	private void setInputDatail(JobNotifyDetailInfoResponse job) {
		createExecComposite(
				job.getNotifyJobType() == JobNotifyDetailInfoResponse.NotifyJobTypeEnum.DIRECT);

		if (m_execComposite instanceof NotifyJobDirectComposite) {
			((NotifyJobDirectComposite)m_execComposite).setInputData(job);
		} else if (m_execComposite instanceof NotifyJobLinkSendComposite) {
			((NotifyJobLinkSendComposite)m_execComposite).setInputData(job);
		}
	}

	/**
	 * 入力値を設定した通知情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 通知情報
	 *
	 * @see #createInputDataForJob(ArrayList, int, Button, Text, Button, Combo)
	 */
	@Override
	protected NotifyInfoInputData createInputData() {
		NotifyInfoInputData info = super.createInputData();

		// 通知タイプの設定
		info.setNotifyType(NotifyTypeConstant.TYPE_JOB);

		// コマンド情報
		if (m_execComposite instanceof NotifyJobDirectComposite) {
			((NotifyJobDirectComposite)m_execComposite).createInputData(info);
		} else if (m_execComposite instanceof NotifyJobLinkSendComposite) {
			((NotifyJobLinkSendComposite)m_execComposite).createInputData(info);
		}
		return info;
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		// 入力値生成
		this.inputData = this.createInputData();
		return super.validate();
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

		NotifyInfoInputData info = this.getInputData();
		if(info != null){
			if (!this.updateFlg) {
				// 作成の場合
				result = new AddNotify().addJobNotify(managerName, info);
			}
			else{
				// 変更の場合
				result = new ModifyNotify().modifyJobNotify(managerName, info);
			}
		}

		return result;
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 */
	@Override
	protected void setValidateResult(String id, String message) {

		this.validateResult = new ValidateResult();
		this.validateResult.setValid(false);
		this.validateResult.setID(id);
		this.validateResult.setMessage(message);
	}

	/**
	 * ボタンを生成します。<BR>
	 * 参照フラグが<code> true </code>の場合は閉じるボタンを生成し、<code> false </code>の場合は、デフォルトのボタンを生成します。
	 *
	 * @param parent ボタンバーコンポジット
	 *
	 * @see #createButtonsForButtonBar(Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		if(!this.referenceFlg){
			super.createButtonsForButtonBar(parent);
		}
		else{
			// 閉じるボタン
			this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("close"), false);
		}
	}

	/**
	 * コンポジットの選択可/不可を設定します。
	 *
	 * @param enable 選択可の場合、<code> true </code>
	 */
	@Override
	protected void setEnabled(boolean enable) {
		super.m_notifyBasic.setEnabled(enable);
		super.m_notifyInhibition.setEnabled(enable);
	}

	@Override
	public void updateManagerName(String managerName) {
		super.updateManagerName(managerName);
		// コマンド情報
		if (m_execComposite instanceof NotifyJobDirectComposite) {
			((NotifyJobDirectComposite)m_execComposite).setManagerName(this.managerName);
		} else if (m_execComposite instanceof NotifyJobLinkSendComposite) {
				((NotifyJobLinkSendComposite)m_execComposite).setManagerName(this.managerName);
		}
	}

	@Override
	public void updateOwnerRole(String ownerRoleId) {
		super.updateOwnerRole(ownerRoleId);
		// コマンド情報
		if (m_execComposite instanceof NotifyJobDirectComposite) {
			((NotifyJobDirectComposite)m_execComposite).setOwnerRoleId(this.ownerRoleId);
		} else if (m_execComposite instanceof NotifyJobLinkSendComposite) {
				((NotifyJobLinkSendComposite)m_execComposite).setOwnerRoleId(this.ownerRoleId);
		}
	}

	/**
	 * Compositeの切り替え
	 * 
	 * @param isDirect true:直接実行
	 */
	private void createExecComposite(boolean isDirect) {

		m_notifyJobType_Direct.setSelection(isDirect);
		m_notifyJobType_JobLinkSend.setSelection(!isDirect);

		m_execComposite.dispose();
		if (isDirect) {
			m_execComposite = new NotifyJobDirectComposite(m_execParentComposite, SWT.NONE);
		} else {
			m_execComposite = new NotifyJobLinkSendComposite(m_execParentComposite, SWT.NONE);
		}
		setExecCompositeLayout(m_execComposite);
		m_execComposite.layout();
		m_execParentComposite.layout();
		if (isDirect) {
			((NotifyJobDirectComposite)m_execComposite).setManagerName(managerName);
			((NotifyJobDirectComposite)m_execComposite).setOwnerRoleId(ownerRoleId);
		} else {
			((NotifyJobLinkSendComposite)m_execComposite).setManagerName(managerName);
			((NotifyJobLinkSendComposite)m_execComposite).setOwnerRoleId(ownerRoleId);
		}
	}

	private void setExecCompositeLayout(Composite execComposite) {
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		execComposite.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 240;
		execComposite.setLayoutData(gridData);
	}
}
