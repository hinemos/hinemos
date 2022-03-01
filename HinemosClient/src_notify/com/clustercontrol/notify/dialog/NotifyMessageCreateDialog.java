/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.dialog;

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
import org.openapitools.client.model.MessageNotifyDetailInfoResponse;

import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.action.AddNotify;
import com.clustercontrol.notify.action.GetNotify;
import com.clustercontrol.notify.action.ModifyNotify;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.util.Messages;

/**
 * 通知（メッセージ）作成・変更ダイアログクラス<BR>
 *
 */
public class NotifyMessageCreateDialog extends NotifyBasicCreateDialog {

	private static Log m_log = LogFactory.getLog(NotifyMessageCreateDialog.class);

	/** カラム数（重要度）。 */
	private static final int WIDTH_PRIORITY = 2;

	/** カラム数（チェックボックス）。 */
	private static final int WIDTH_CHECK = 2;

	/** カラム数（ルールベース）。 */
	private static final int WIDTH_RULEBASE_ADDRESS = 11;

	// ----- instance フィールド ----- //

	/**
	 * 通知タイプ
	 * 
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	private final int TYPE_MESSAGE = NotifyTypeConstant.TYPE_MESSAGE;

	/** 入力値の正当性を保持するオブジェクト。 */
	protected ValidateResult validateResult = null;

	/** ルールベース（重要度：通知） チェックボックス。 */
	private Button m_checkRuleBaseInfo = null;
	/** ルールベース（重要度：警告） チェックボックス。 */
	private Button m_checkRuleBaseWarning = null;
	/** ルールベース（重要度：危険） チェックボックス。 */
	private Button m_checkRuleBaseCritical = null;
	/** ルールベース（重要度：不明） チェックボックス。 */
	private Button m_checkRuleBaseUnknown = null;

	/** ルールベース（重要度：通知） テキスト。 */
	private Text m_textRuleBaseInfo = null;
	/** ルールベース（重要度：警告） テキスト。 */
	private Text m_textRuleBaseWarning = null;
	/** ルールベース（重要度：危険） テキスト。 */
	private Text m_textRuleBaseCritical = null;
	/** ルールベース（重要度：不明） テキスト。 */
	private Text m_textRuleBaseUnknown = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public NotifyMessageCreateDialog(Shell parent) {
		super(parent);
		parentDialog = this;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param managerName
	 *            マネージャ名
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param notifyId
	 *            変更する通知情報の通知ID
	 * @param updateFlg
	 *            更新フラグ（true:更新する）
	 */
	public NotifyMessageCreateDialog(Shell parent, String managerName, String notifyId, boolean updateFlg) {
		super(parent, managerName, notifyId, updateFlg);
		parentDialog = this;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
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
		if (this.notifyId != null) {
			inputData = new GetNotify().getMessageNotify(this.managerName, this.notifyId);
		} else {
			inputData = new NotifyInfoInputData();
		}
		this.setInputData(inputData);
	}

	/**
	 * 親のクラスから呼ばれ、各通知用のダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 */
	@Override
	protected void customizeSettingDialog(Composite parent) {
		final Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.notify.message.create.modify"));

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
		 * メッセージ
		 */
		// メッセージグループ
		Group groupMessage = new Group(parent, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupMessage.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupMessage.setLayoutData(gridData);
		groupMessage.setText(Messages.getString("notifies.message"));

		// 空行
		label = new Label(groupMessage, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 重要度 ごとの設定
		 */
		// ラベル（重要度）
		label = new Label(groupMessage, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority"));

		// ラベル（通知）
		label = new Label(groupMessage, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.attribute"));

		// ラベル（ルールベース）
		label = new Label(groupMessage, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_RULEBASE_ADDRESS;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("rulebase"));

		// メッセージ 重要度：情報
		label = this.getLabelPriority(groupMessage, Messages.getString("info"), PriorityColorConstant.COLOR_INFO);
		this.m_checkRuleBaseInfo = this.getCheckRuleBase(groupMessage);
		this.m_checkRuleBaseInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_textRuleBaseInfo.setEnabled(m_checkRuleBaseInfo.getSelection());
				update();
			}
		});
		this.m_textRuleBaseInfo = this.getTextRuleBase(groupMessage);
		this.m_textRuleBaseInfo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// メッセージ 重要度：警告
		label = this.getLabelPriority(groupMessage, Messages.getString("warning"), PriorityColorConstant.COLOR_WARNING);
		this.m_checkRuleBaseWarning = this.getCheckRuleBase(groupMessage);
		this.m_checkRuleBaseWarning.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_textRuleBaseWarning.setEnabled(m_checkRuleBaseWarning.getSelection());
				update();
			}
		});
		this.m_textRuleBaseWarning = this.getTextRuleBase(groupMessage);
		this.m_textRuleBaseWarning.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// メッセージ 重要度：危険
		label = this.getLabelPriority(groupMessage, Messages.getString("critical"),
				PriorityColorConstant.COLOR_CRITICAL);
		this.m_checkRuleBaseCritical = this.getCheckRuleBase(groupMessage);
		this.m_checkRuleBaseCritical.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_textRuleBaseCritical.setEnabled(m_checkRuleBaseCritical.getSelection());
				update();
			}
		});
		this.m_textRuleBaseCritical = this.getTextRuleBase(groupMessage);
		this.m_textRuleBaseCritical.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// メッセージ 重要度：不明
		label = this.getLabelPriority(groupMessage, Messages.getString("unknown"), PriorityColorConstant.COLOR_UNKNOWN);
		this.m_checkRuleBaseUnknown = this.getCheckRuleBase(groupMessage);
		this.m_checkRuleBaseUnknown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_textRuleBaseUnknown.setEnabled(m_checkRuleBaseUnknown.getSelection());
				update();
			}
		});
		this.m_textRuleBaseUnknown = this.getTextRuleBase(groupMessage);
		this.m_textRuleBaseUnknown.addModifyListener(new ModifyListener() {
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
	public void update() {
		// テキストボックスの有効/無効を初期化
		this.m_textRuleBaseInfo.setEnabled(this.m_checkRuleBaseInfo.getSelection());
		this.m_textRuleBaseWarning.setEnabled(this.m_checkRuleBaseWarning.getSelection());
		this.m_textRuleBaseCritical.setEnabled(this.m_checkRuleBaseCritical.getSelection());
		this.m_textRuleBaseUnknown.setEnabled(this.m_checkRuleBaseUnknown.getSelection());

		// 必須項目を明示

		// 情報
		if (this.m_checkRuleBaseInfo.getSelection() && "".equals(this.m_textRuleBaseInfo.getText())) {
			this.m_textRuleBaseInfo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_textRuleBaseInfo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 警告
		if (this.m_checkRuleBaseWarning.getSelection() && "".equals(this.m_textRuleBaseWarning.getText())) {
			this.m_textRuleBaseWarning.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_textRuleBaseWarning.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 危険
		if (this.m_checkRuleBaseCritical.getSelection() && "".equals(this.m_textRuleBaseCritical.getText())) {
			this.m_textRuleBaseCritical.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_textRuleBaseCritical.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 不明
		if (this.m_checkRuleBaseUnknown.getSelection() && "".equals(this.m_textRuleBaseUnknown.getText())) {
			this.m_textRuleBaseUnknown.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_textRuleBaseUnknown.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
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
	 * @param notify
	 *            設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(NotifyInfoInputData notify) {
		super.setInputData(notify);

		// コマンド情報
		MessageNotifyDetailInfoResponse info = notify.getNotifyMessageInfo();
		if (info != null) {
			this.setInputData(info);
		}

		// 必須入力項目を可視化
		this.update();
	}

	private void setInputData(MessageNotifyDetailInfoResponse message) {

		Button[] checkRuleBases = new Button[] { this.m_checkRuleBaseInfo, this.m_checkRuleBaseWarning,
				this.m_checkRuleBaseCritical, this.m_checkRuleBaseUnknown };
		String[] ruleBases = new String[] { message.getInfoRulebaseId(), message.getWarnRulebaseId(),
				message.getCriticalRulebaseId(), message.getUnknownRulebaseId() };
		Text[] textRuleBases = new Text[] { this.m_textRuleBaseInfo, this.m_textRuleBaseWarning,
				this.m_textRuleBaseCritical, this.m_textRuleBaseUnknown };

		Boolean[] validFlgs = getValidFlgs(message);
		for (int i = 0; i < validFlgs.length; i++) {
			boolean valid = validFlgs[i].booleanValue();
			checkRuleBases[i].setSelection(valid);
			if (ruleBases[i] != null) {
				textRuleBases[i].setText(ruleBases[i]);
			}
		}
	}

	/**
	 * 入力値を設定した通知情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 通知情報
	 *
	 */
	@Override
	protected NotifyInfoInputData createInputData() {
		NotifyInfoInputData info = super.createInputData();

		// 通知タイプの設定
		info.setNotifyType(TYPE_MESSAGE);

		// イベント情報
		MessageNotifyDetailInfoResponse message = createNotifyInfoDetail();
		if (message == null) {
			return null;
		}
		info.setNotifyMessageInfo(message);

		return info;
	}

	private MessageNotifyDetailInfoResponse createNotifyInfoDetail() {
		MessageNotifyDetailInfoResponse message = new MessageNotifyDetailInfoResponse();

		// メッセージ通知
		message.setInfoValidFlg(m_checkRuleBaseInfo.getSelection());
		message.setWarnValidFlg(m_checkRuleBaseWarning.getSelection());
		message.setCriticalValidFlg(m_checkRuleBaseCritical.getSelection());
		message.setUnknownValidFlg(m_checkRuleBaseUnknown.getSelection());

		// ルールベース
		if (isNotNullAndBlank(m_textRuleBaseInfo.getText())) {
			validateRuleBase(m_textRuleBaseInfo.getText());
			if (this.validateResult != null) {
				return null;
			}
			message.setInfoRulebaseId(m_textRuleBaseInfo.getText());
		}
		if (isNotNullAndBlank(m_textRuleBaseWarning.getText())) {
			validateRuleBase(m_textRuleBaseWarning.getText());
			if (this.validateResult != null) {
				return null;
			}
			message.setWarnRulebaseId(m_textRuleBaseWarning.getText());
		}
		if (isNotNullAndBlank(m_textRuleBaseCritical.getText())) {
			validateRuleBase(m_textRuleBaseCritical.getText());
			if (this.validateResult != null) {
				return null;
			}
			message.setCriticalRulebaseId(m_textRuleBaseCritical.getText());
		}
		if (isNotNullAndBlank(m_textRuleBaseUnknown.getText())) {
			validateRuleBase(m_textRuleBaseUnknown.getText());
			if (this.validateResult != null) {
				return null;
			}
			message.setUnknownRulebaseId(m_textRuleBaseUnknown.getText());
		}

		return message;
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

		if (this.inputData != null) {
			return super.validate();
		} else {
			return validateResult;
		}
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
		if (info != null) {
			if (!this.updateFlg) {
				// 作成の場合
				result = new AddNotify().addMessageNotify(managerName, info);
			} else {
				// 変更の場合
				result = new ModifyNotify().modifyMessageNotify(managerName, info);
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
	 * @param id
	 *            ID
	 * @param message
	 *            メッセージ
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
	 * 参照フラグが<code> true </code>の場合は閉じるボタンを生成し、<code> false </code>
	 * の場合は、デフォルトのボタンを生成します。
	 *
	 * @param parent
	 *            ボタンバーコンポジット
	 *
	 * @see #createButtonsForButtonBar(Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		if (!this.referenceFlg) {
			super.createButtonsForButtonBar(parent);
		} else {
			// 閉じるボタン
			this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("close"), false);
		}
	}

	/**
	 * コンポジットの選択可/不可を設定します。
	 *
	 * @param enable
	 *            選択可の場合、<code> true </code>
	 */
	@Override
	protected void setEnabled(boolean enable) {
		super.m_notifyBasic.setEnabled(enable);
		super.m_notifyInhibition.setEnabled(enable);
	}

	/**
	 * 重要度のラベルを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param text
	 *            ラベルに表示するテキスト
	 * @param background
	 *            ラベルの背景色
	 * @return 生成されたラベル
	 */
	private Label getLabelPriority(Composite parent, String text, Color background) {

		// ラベル（重要度）
		Label label = new Label(parent, SWT.NONE);
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
	 * ルールベースの実行チェックボックスを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Button getCheckRuleBase(Composite parent) {
		// チェックボックス（実行）
		Button button = new Button(parent, SWT.CHECK);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		return button;
	}

	/**
	 * ルールベースのテキストボックスを返します。
	 *
	 * @param parent
	 *            親のインスタンス
	 * @return 生成されたテキストボックス
	 */
	private Text getTextRuleBase(Composite parent) {
		// テキスト（送信先（セミコロン区切り））
		Text text = new Text(parent, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_RULEBASE_ADDRESS;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		text.setLayoutData(gridData);

		return text;
	}

	private Boolean[] getValidFlgs(MessageNotifyDetailInfoResponse info) {
		Boolean[] validFlgs = new Boolean[] { info.getInfoValidFlg(), info.getWarnValidFlg(),
				info.getCriticalValidFlg(), info.getUnknownValidFlg() };
		return validFlgs;
	}

	private void validateRuleBase(String ruleBase) {
		// 入力最大文字数
		Integer limit = 200;
		if (ruleBase.length() > limit) {
			String[] args = { Messages.getString("rulebase"), limit.toString() };
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.common.2", args));
		}
	}
}
