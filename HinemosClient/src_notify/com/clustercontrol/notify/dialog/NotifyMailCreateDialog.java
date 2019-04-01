/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.dialog;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.action.AddNotify;
import com.clustercontrol.notify.action.GetNotify;
import com.clustercontrol.notify.action.ModifyNotify;
import com.clustercontrol.notify.mail.composite.MailTemplateIdListComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.notify.NotifyInfo;
import com.clustercontrol.ws.notify.NotifyMailInfo;

/**
 * 通知（メール）作成・変更ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 3.0.0
 */
public class NotifyMailCreateDialog extends NotifyBasicCreateDialog {

	private static Log m_log = LogFactory.getLog( NotifyMailCreateDialog.class );

	/** カラム数（重要度）。 */
	private static final int WIDTH_PRIORITY = 2;

	/** カラム数（チェックボックス）。 */
	private static final int WIDTH_CHECK = 2;

	/** カラム数（メッセールアドレス）。 */
	private static final int WIDTH_MAIL_ADDRESS = 11;

	// ----- instance フィールド ----- //

	/** 通知タイプ
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	private final int TYPE_MAIL = 2;

	/** 入力値の正当性を保持するオブジェクト。 */
	protected ValidateResult validateResult = null;

	/** メールテンプレートID情報コンポジット */
	private MailTemplateIdListComposite m_compositeMailTemplateIdList = null;

	/** メール（重要度：通知） チェックボックス。 */
	private Button m_checkMailNormalInfo = null;
	/** メール（重要度：警告） チェックボックス。 */
	private Button m_checkMailNormalWarning = null;
	/** メール（重要度：危険） チェックボックス。 */
	private Button m_checkMailNormalCritical = null;
	/** メール（重要度：不明） チェックボックス。 */
	private Button m_checkMailNormalUnknown = null;

	/** メールアドレス（重要度：通知） テキスト。 */
	private Text m_textMailAddressInfo = null;
	/** メールアドレス（重要度：警告） テキスト。 */
	private Text m_textMailAddressWarning = null;
	/** メールアドレス（重要度：危険） テキスト。 */
	private Text m_textMailAddressCritical = null;
	/** メールアドレス（重要度：不明） テキスト。 */
	private Text m_textMailAddressUnknown = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public NotifyMailCreateDialog(Shell parent) {
		super(parent);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param managerName マネージャ名
	 * @param parent 親のシェルオブジェクト
	 * @param notifyId 変更する通知情報の通知ID
	 * @param updateFlg 更新フラグ（true:更新する）
	 */
	public NotifyMailCreateDialog(Shell parent, String managerName, String notifyId, boolean updateFlg) {
		super(parent, managerName, notifyId, updateFlg);
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 * @see com.clustercontrol.notify.action.GetNotify#getNotify(String)
	 * @see #setInputData(NotifyInfo)
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		super.customizeDialog(parent);

		m_log.debug("addSelectionListener");
		if (this.m_notifyBasic.m_managerComposite.getComboManagerName() != null) {
			this.m_notifyBasic.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {	
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_log.debug("widgetSelected(managerComposite)");
								String managerName = m_notifyBasic.m_managerComposite.getText();
					m_compositeMailTemplateIdList.setManagerName(managerName);
					m_compositeMailTemplateIdList.update(m_notifyBasic.m_ownerRoleId.getText());
				}
			});
		}
		if (this.m_notifyBasic.m_ownerRoleId.getComboRoleId() != null) {
			this.m_notifyBasic.m_ownerRoleId.getComboRoleId().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_log.debug("widgetSelected(ownerRoleId)");
					m_compositeMailTemplateIdList.update(m_notifyBasic.m_ownerRoleId.getText());
				}
			});
		}

		// 通知IDが指定されている場合、その情報を初期表示する。
		NotifyInfo info = null;
		if(this.notifyId != null){
			info = new GetNotify().getNotify(this.managerName, this.notifyId);
		}
		else{
			info = new NotifyInfo();
		}
		this.setInputData(info);
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
		shell.setText(Messages.getString("dialog.notify.mail.create.modify"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);

		/*
		 * メール
		 */
		// メールグループ
		Group groupMail = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "goupmail", groupMail);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupMail.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupMail.setLayoutData(gridData);
		groupMail.setText(Messages.getString("notifies.mail"));

		/*
		 * メールテンプレートIDの設定
		 */
		this.m_compositeMailTemplateIdList = new MailTemplateIdListComposite(
				groupMail, SWT.NONE, this.m_notifyBasic.getManagerListComposite().getText(), true);
		WidgetTestUtil.setTestId(this, "templateidlist", m_compositeMailTemplateIdList);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_compositeMailTemplateIdList.setLayoutData(gridData);

		// 空行
		label = new Label(groupMail, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 重要度 ごとの設定
		 */
		// ラベル（重要度）
		label = new Label(groupMail, SWT.NONE);
		WidgetTestUtil.setTestId(this, "priority", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority"));

		// ラベル（メール）
		label = new Label(groupMail, SWT.NONE);
		WidgetTestUtil.setTestId(this, "mail", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.attribute"));

		// ラベル（メールアドレス）
		label = new Label(groupMail, SWT.NONE);
		WidgetTestUtil.setTestId(this, "mailaddress", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_MAIL_ADDRESS;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("email.address.ssv"));

		//　メール　重要度：情報
		label = this.getLabelPriority(groupMail, Messages.getString("info"),PriorityColorConstant.COLOR_INFO);
		this.m_checkMailNormalInfo = this.getCheckMailNormal(groupMail);
		WidgetTestUtil.setTestId(this, "mailnormalinfo", m_checkMailNormalInfo);
		this.m_checkMailNormalInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_textMailAddressInfo.setEnabled(m_checkMailNormalInfo.getSelection());
				update();
			}
		});
		this.m_textMailAddressInfo = this.getTextMailAddress(groupMail);
		WidgetTestUtil.setTestId(this, "mailaddressinfo", m_textMailAddressInfo);
		String tooltipText = Messages.getString("notify.parameter.tooltip") + Messages.getString("replace.parameter.notify") + Messages.getString("replace.parameter.node");
		this.m_textMailAddressInfo.setToolTipText(tooltipText);
		this.m_textMailAddressInfo.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//　メール　重要度：警告
		label = this.getLabelPriority(groupMail, Messages.getString("warning"),PriorityColorConstant.COLOR_WARNING);
		this.m_checkMailNormalWarning = this.getCheckMailNormal(groupMail);
		WidgetTestUtil.setTestId(this, "mailnormalwarning", m_checkMailNormalWarning);
		this.m_checkMailNormalWarning.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_textMailAddressWarning.setEnabled(m_checkMailNormalWarning.getSelection());
				update();
			}
		});
		this.m_textMailAddressWarning = this.getTextMailAddress(groupMail);
		WidgetTestUtil.setTestId(this, "mailaddresswarning", m_textMailAddressWarning);
		this.m_textMailAddressWarning.setToolTipText(tooltipText);
		this.m_textMailAddressWarning.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//　メール　重要度：危険
		label = this.getLabelPriority(groupMail, Messages.getString("critical"),PriorityColorConstant.COLOR_CRITICAL);
		this.m_checkMailNormalCritical = this.getCheckMailNormal(groupMail);
		WidgetTestUtil.setTestId(this, "mailnormalcritical", m_checkMailNormalCritical);
		this.m_checkMailNormalCritical.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_textMailAddressCritical.setEnabled(m_checkMailNormalCritical.getSelection());
				update();
			}
		});
		this.m_textMailAddressCritical = this.getTextMailAddress(groupMail);
		WidgetTestUtil.setTestId(this, "mailaddresscritical", m_textMailAddressCritical);
		this.m_textMailAddressCritical.setToolTipText(tooltipText);
		this.m_textMailAddressCritical.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//　メール　重要度：不明
		label = this.getLabelPriority(groupMail, Messages.getString("unknown"),PriorityColorConstant.COLOR_UNKNOWN);
		this.m_checkMailNormalUnknown = this.getCheckMailNormal(groupMail);
		WidgetTestUtil.setTestId(this, "mailnormalunknown", m_checkMailNormalUnknown);
		this.m_checkMailNormalUnknown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_textMailAddressUnknown.setEnabled(m_checkMailNormalUnknown.getSelection());
				update();
			}
		});
		this.m_textMailAddressUnknown = this.getTextMailAddress(groupMail);
		WidgetTestUtil.setTestId(this, "mailaddressunknown", m_textMailAddressUnknown);
		this.m_textMailAddressUnknown.setToolTipText(tooltipText);
		this.m_textMailAddressUnknown.addModifyListener(new ModifyListener(){
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
	public void update(){
		// テキストボックスの有効/無効を初期化
		this.m_textMailAddressInfo.setEnabled(this.m_checkMailNormalInfo.getSelection());
		this.m_textMailAddressWarning.setEnabled(this.m_checkMailNormalWarning.getSelection());
		this.m_textMailAddressCritical.setEnabled(this.m_checkMailNormalCritical.getSelection());
		this.m_textMailAddressUnknown.setEnabled(this.m_checkMailNormalUnknown.getSelection());

		// 必須項目を明示

		// 情報
		if(this.m_checkMailNormalInfo.getSelection() && "".equals(this.m_textMailAddressInfo.getText())){
			this.m_textMailAddressInfo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textMailAddressInfo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 警告
		if(this.m_checkMailNormalWarning.getSelection() && "".equals(this.m_textMailAddressWarning.getText())){
			this.m_textMailAddressWarning.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textMailAddressWarning.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 危険
		if(this.m_checkMailNormalCritical.getSelection() && "".equals(this.m_textMailAddressCritical.getText())){
			this.m_textMailAddressCritical.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textMailAddressCritical.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 不明
		if(this.m_checkMailNormalUnknown.getSelection() && "".equals(this.m_textMailAddressUnknown.getText())){
			this.m_textMailAddressUnknown.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textMailAddressUnknown.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 入力値を保持した通知情報を返します。
	 *
	 * @return 通知情報
	 */
	@Override
	public NotifyInfo getInputData() {
		return this.inputData;
	}

	/**
	 * 引数で指定された通知情報の値を、各項目に設定します。
	 *
	 * @param notify 設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(NotifyInfo notify) {
		// Add listener at first
		this.setInputListeners();
		super.setInputData(notify);

		// List mail template ID
		m_compositeMailTemplateIdList.update(m_notifyBasic.getOwnerRoleId());

		// コマンド情報
		NotifyMailInfo info = notify.getNotifyMailInfo();
		if (info != null) {
			this.setInputData(info);
		}

		// 必須入力項目を可視化
		this.update();
	}

	private void setInputData(NotifyMailInfo mail) {
		if (mail.getMailTemplateId() != null) {
			this.m_compositeMailTemplateIdList.setText(mail.getMailTemplateId());
		}

		Button[] checkMailNormals = new Button[] {
				this.m_checkMailNormalInfo,
				this.m_checkMailNormalWarning,
				this.m_checkMailNormalCritical,
				this.m_checkMailNormalUnknown
		};
		String[] mailAddresses = new String[] {
				mail.getInfoMailAddress(),
				mail.getWarnMailAddress(),
				mail.getCriticalMailAddress(),
				mail.getUnknownMailAddress()
		};
		Text[] textMailAddresses = new Text[] {
				this.m_textMailAddressInfo,
				this.m_textMailAddressWarning,
				this.m_textMailAddressCritical,
				this.m_textMailAddressUnknown
		};

		Boolean[] validFlgs = getValidFlgs(mail);
		for (int i = 0; i < validFlgs.length; i++) {
			boolean valid = validFlgs[i].booleanValue();
			checkMailNormals[i].setSelection(valid);
			WidgetTestUtil.setTestId(this, "checkMailNormals" + i, checkMailNormals[i]);
			if (mailAddresses[i] != null) {
				textMailAddresses[i].setText(mailAddresses[i]);
				WidgetTestUtil.setTestId(this, "addreses" + i, textMailAddresses[i]);
			}
		}
	}

	/**
	 * 入力値を設定した通知情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 通知情報
	 *
	 * @see #createInputDataForMail(ArrayList, int, Button, Text, Button)
	 */
	@Override
	protected NotifyInfo createInputData() {
		NotifyInfo info = super.createInputData();

		// 通知タイプの設定
		info.setNotifyType(TYPE_MAIL);

		// イベント情報
		NotifyMailInfo mail = createNotifyInfoDetail();
		info.setNotifyMailInfo(mail);

		return info;
	}

	private NotifyMailInfo createNotifyInfoDetail() {
		NotifyMailInfo mail = new NotifyMailInfo();

		// メール通知
		mail.setInfoValidFlg(m_checkMailNormalInfo.getSelection());
		mail.setWarnValidFlg(m_checkMailNormalWarning.getSelection());
		mail.setCriticalValidFlg(m_checkMailNormalCritical.getSelection());
		mail.setUnknownValidFlg(m_checkMailNormalUnknown.getSelection());

		// メールアドレス
		if (isNotNullAndBlank(m_textMailAddressInfo.getText())) {
			mail.setInfoMailAddress(m_textMailAddressInfo.getText());
		}
		if (isNotNullAndBlank(m_textMailAddressWarning.getText())) {
			mail.setWarnMailAddress(m_textMailAddressWarning.getText());
		}
		if (isNotNullAndBlank(m_textMailAddressCritical.getText())) {
			mail.setCriticalMailAddress(m_textMailAddressCritical.getText());
		}
		if (isNotNullAndBlank(m_textMailAddressUnknown.getText())) {
			mail.setUnknownMailAddress(m_textMailAddressUnknown.getText());
		}

		// メールテンプレートＩＤ
		String mailTemplateId = this.m_compositeMailTemplateIdList.getText();
		// メールテンプレートID
		if (isNotNullAndBlank(mailTemplateId)) {
			mail.setMailTemplateId(mailTemplateId);
		}

		return mail;
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

		NotifyInfo info = this.getInputData();
		if(info != null){
			if (!this.updateFlg) {
				// 作成の場合
				result = new AddNotify().add(this.getInputManagerName(), info);
			}
			else{
				// 変更の場合
				result = new ModifyNotify().modify(this.getInputManagerName(), info);
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

	/**
	 * 重要度のラベルを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param text ラベルに表示するテキスト
	 * @param background ラベルの背景色
	 * @return 生成されたラベル
	 */
	private Label getLabelPriority(Composite parent,
			String text,
			Color background
			) {

		// ラベル（重要度）
		Label label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "labelpriority", label);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(text + " : ");
		label.setBackground(background);

		return label;
	}

	/**
	 * メールの実行チェックボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Button getCheckMailNormal(Composite parent) {
		// チェックボックス（実行）
		Button button = new Button(parent, SWT.CHECK);
		WidgetTestUtil.setTestId(this, null, button);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		return button;
	}

	/**
	 * メールのメールアドレステキストボックスを返します。
	 *
	 * @param parent 親のインスタンス
	 * @return 生成されたテキストボックス
	 */
	private Text getTextMailAddress(Composite parent) {
		// テキスト（送信先（セミコロン区切り））
		Text text = new Text(parent, SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, text);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_MAIL_ADDRESS;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		text.setLayoutData(gridData);

		return text;
	}

	private void setInputListeners(){
		if( !this.updateFlg ){
			m_notifyBasic.getManagerListComposite().addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent e) {
					// TODO Reload role ID list -> reload calendar list, mail template list
				}
			});
		}
	}
}
