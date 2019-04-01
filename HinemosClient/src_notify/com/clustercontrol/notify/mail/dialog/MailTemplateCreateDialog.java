/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.mail.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.composite.TextWithParameterComposite;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.mail.util.MailTemplateEndpointWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.mailtemplate.InvalidRole_Exception;
import com.clustercontrol.ws.mailtemplate.MailTemplateDuplicate_Exception;
import com.clustercontrol.ws.mailtemplate.MailTemplateInfo;

/**
 * メールテンプレートID作成・変更ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 2.4.0
 */
public class MailTemplateCreateDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( MailTemplateCreateDialog.class );

	/** カラム数 */
	public static final int WIDTH	 = 15;

	/** カラム数（ラベル）。 */
	public static final int WIDTH_LABEL = 4;

	/** カラム数（テキスト）。 */
	public static final int WIDTH_TEXT = 10;

	/** 入力値を保持するオブジェクト。 */
	private MailTemplateInfo inputData = null;

	/** 入力値の正当性を保持するオブジェクト。 */
	protected ValidateResult validateResult = null;

	/** ダイアログ表示時の処理タイプ */
	private int mode;

	/** 変更対象のメールテンプレートID。 */
	private String mailTemplateId = null;

	/** マネージャ名 */
	private String managerName = null;

	/** メールテンプレートID テキストボックス。 */
	private Text textMailTemplateId = null;

	/** 説明 テキストボックス。 */
	private Text textDescription = null;

	/** オーナーロールID用テキスト */
	private RoleIdListComposite m_ownerRoleId = null;

	/** 件名 スタイルテキストコンポジット。 */
	private TextWithParameterComposite textSubject = null;

	/** 本文 スタイルテキストコンポジット。 */
	private TextWithParameterComposite textBody = null;

	/** マネージャリスト用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param mailTemplateId 変更するメールテンプレート情報のメールテンプレートID
	 * @param dataOperationType データ処理タイプ
	 */
	public MailTemplateCreateDialog(Shell parent, String managerName, String mailTemplateId, int mode) {
		super(parent);

		this.managerName = managerName;
		this.mailTemplateId = mailTemplateId;
		this.mode = mode;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.mail.action.GetMailTemplate#getMailTemplate(String)
	 * @see #setInputData(MailTemplateInfoData)
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.mail.template.create.modify"));

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
		 * マネージャ
		 */
		label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("facility.manager") + " : ");
		if (this.mode == PropertyDefineConstant.MODE_MODIFY || this.mode == PropertyDefineConstant.MODE_SHOW) {
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, true);
		}
		WidgetTestUtil.setTestId(this, "managerComposite", m_managerComposite);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);
		this.m_managerComposite.setText(this.managerName);
		if(this.mode != PropertyDefineConstant.MODE_MODIFY && this.mode != PropertyDefineConstant.MODE_SHOW) {
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerName = m_managerComposite.getText();
					m_ownerRoleId.createRoleIdList(managerName);
				}
			});
		}

		/*
		 * メールテンプレートID
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "mailtempateid", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("mail.template.id") + " : ");
		// テキスト
		this.textMailTemplateId = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "id", textMailTemplateId);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textMailTemplateId.setLayoutData(gridData);
		if(this.mode == PropertyDefineConstant.MODE_SHOW
				|| this.mode == PropertyDefineConstant.MODE_MODIFY){
			this.textMailTemplateId.setEnabled(false);
		}
		this.textMailTemplateId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		/*
		 * 説明
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "description", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("description") + " : ");
		// テキスト
		this.textDescription = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "description", textDescription);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textDescription.setLayoutData(gridData);

		/*
		 * オーナーロールID
		 */
		Label labelRoleId = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "roleid", labelRoleId);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		if (this.mode == PropertyDefineConstant.MODE_ADD) {
			this.m_ownerRoleId = new RoleIdListComposite(parent, SWT.NONE, managerName, true, Mode.OWNER_ROLE);
		} else {
			this.m_ownerRoleId = new RoleIdListComposite(parent, SWT.NONE, managerName, false, Mode.OWNER_ROLE);
		}
		WidgetTestUtil.setTestId(this, "ownerroleid", m_ownerRoleId);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_ownerRoleId.setLayoutData(gridData);

		/*
		 * メールテンプレート設定
		 */
		/*
		 * メールテンプレート設定グループ
		 */
		Group groupMailTemplate = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "mailtemplate", groupMailTemplate);
		layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		groupMailTemplate.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupMailTemplate.setLayoutData(gridData);
		groupMailTemplate.setText(Messages.getString("mail.template.setting"));

		/*
		 * 件名
		 */
		// ラベル
		label = new Label(groupMailTemplate, SWT.NONE);
		WidgetTestUtil.setTestId(this, "subject", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("subject") + " : ");
		// テキスト
		this.textSubject = new TextWithParameterComposite(groupMailTemplate, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "subject", textSubject);
		gridData = new GridData();
		gridData.horizontalSpan = 13;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textSubject.setLayoutData(gridData);
		String tooltipText = Messages.getString("notify.parameter.tooltip") + Messages.getString("replace.parameter.notify") + Messages.getString("replace.parameter.node");
		this.textSubject.setToolTipText(tooltipText);
		this.textSubject.setColor(new Color(groupMailTemplate.getDisplay(), new RGB(0, 0, 255)));
		this.textSubject.setInputUpper(DataRangeConstant.VARCHAR_256);
		this.textSubject.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 本文
		 */
		// ラベル
		label = new Label(groupMailTemplate, SWT.NONE);
		WidgetTestUtil.setTestId(this, "repositorybody", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("response.body") + " : ");
		// テキスト
		this.textBody = new TextWithParameterComposite(groupMailTemplate, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "body", textBody);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalSpan = 50;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.textBody.setLayoutData(gridData);
		String tooletipText = Messages.getString("notify.parameter.tooltip") + Messages.getString("replace.parameter.notify") + Messages.getString("replace.parameter.node");
		this.textBody.setToolTipText(tooletipText);
		this.textBody.setColor(new Color(groupMailTemplate.getDisplay(), new RGB(0, 0, 255)));

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		line.setLayoutData(gridData);

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(550, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		// メールテンプレートIDが指定されている場合、その情報を初期表示する。
		MailTemplateInfo info = null;
		if(this.mailTemplateId != null){
			try {
				MailTemplateEndpointWrapper wrapper = MailTemplateEndpointWrapper.getWrapper(this.managerName);
				info = wrapper.getMailTemplateInfo(this.mailTemplateId);
			} catch (InvalidRole_Exception e) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				return;
			} catch (Exception e) {
				m_log.warn("customizeDialog() getMailTemplateInfo, " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				return;
			}
		}
		else{
			info = new MailTemplateInfo();
		}
		this.setInputData(info);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		if("".equals(this.textMailTemplateId.getText())){
			this.textMailTemplateId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textMailTemplateId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.textSubject.getText())){
			this.textSubject.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textSubject.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 入力値を保持したメールテンプレート情報を返します。
	 *
	 * @return メールテンプレート情報
	 */
	public MailTemplateInfo getInputData() {
		return this.inputData;
	}

	/**
	 * 引数で指定されたメールテンプレート情報の値を、各項目に設定します。
	 *
	 * @param mailTemplate 設定値として用いるメールテンプレート情報
	 */
	protected void setInputData(MailTemplateInfo mailTemplate) {

		this.inputData = mailTemplate;

		// 各項目に反映
		// メールテンプレートID
		if (mailTemplate.getMailTemplateId() != null) {
			this.textMailTemplateId.setText(mailTemplate.getMailTemplateId());
		}
		// 説明
		if (mailTemplate.getDescription() != null) {
			this.textDescription.setText(mailTemplate.getDescription());
		}
		// 件名
		if (mailTemplate.getSubject() != null) {
			this.textSubject.setText(mailTemplate.getSubject());
		}
		// 本文
		if (mailTemplate.getBody() != null) {
			this.textBody.setText(mailTemplate.getBody());
		}

		// オーナーロールID取得
		if (mailTemplate.getOwnerRoleId() != null) {
			this.m_ownerRoleId.setText(mailTemplate.getOwnerRoleId());
		}

		// 入力制御
		if(this.mode == PropertyDefineConstant.MODE_SHOW){
			this.textMailTemplateId.setEnabled(false);
			this.textDescription.setEnabled(false);
			this.textSubject.setEnabled(false);
			this.textBody.setEnabled(false);
		}

		// 必須入力項目を可視化
		this.update();
	}

	/**
	 * 入力値を設定したメールテンプレート情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return メールテンプレート情報
	 */
	private MailTemplateInfo createInputData() {
		MailTemplateInfo info = new MailTemplateInfo();

		if (this.textMailTemplateId.getText() != null
				&& !"".equals((this.textMailTemplateId.getText()).trim())) {
			info.setMailTemplateId(this.textMailTemplateId.getText());
		}
		if (this.textDescription.getText() != null
				&& !"".equals((this.textDescription.getText()).trim())) {
			info.setDescription(this.textDescription.getText());
		}
		if (this.textSubject.getText() != null
				&& !"".equals((this.textSubject.getText()).trim())) {
			info.setSubject(this.textSubject.getText());
		}
		if (this.textBody.getText() != null
				&& !"".equals((this.textBody.getText()).trim())) {
			info.setBody(this.textBody.getText());
		}

		//オーナーロールID
		if (this.m_ownerRoleId.getText().length() > 0) {
			info.setOwnerRoleId(this.m_ownerRoleId.getText());
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

		MailTemplateInfo info = this.getInputData();
		if(info != null){
			String errMessage = "";
			String managerName = m_managerComposite.getText();
			String[] args = {info.getMailTemplateId(), managerName};
			MailTemplateEndpointWrapper wrapper = MailTemplateEndpointWrapper.getWrapper(managerName);
			if(this.mode == PropertyDefineConstant.MODE_ADD){
				// 作成の場合
				try {
					result = wrapper.addMailTemplate(info);

					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.notify.mail.1", args));

				} catch (MailTemplateDuplicate_Exception e) {
					// メールテンプレートIDが重複している場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.notify.mail.10", args));
				} catch (Exception e) {
					if (e instanceof InvalidRole_Exception) {
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
							Messages.getString("message.notify.mail.2", args) + errMessage);
				}
			}
			else if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
				// 変更の場合
				try {
					result = wrapper.modifyMailTemplate(info);

					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.notify.mail.3", args));

				} catch (Exception e) {
					if (e instanceof InvalidRole_Exception) {
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
							Messages.getString("message.notify.mail.4", args) + errMessage);
				}
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
	 * ボタンを生成します。<BR>
	 * 参照フラグが<code> true </code>の場合は閉じるボタンを生成し、<code> false </code>の場合は、デフォルトのボタンを生成します。
	 *
	 * @param parent ボタンバーコンポジット
	 *
	 * @see #createButtonsForButtonBar(Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		if(this.mode != PropertyDefineConstant.MODE_SHOW){
			super.createButtonsForButtonBar(parent);
		}else{
			// 閉じるボタン
			this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("close"), false);
		}
	}
}
