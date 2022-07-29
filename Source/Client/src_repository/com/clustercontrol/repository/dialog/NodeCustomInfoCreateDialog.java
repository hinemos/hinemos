/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.dialog;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.NodeConfigCustomInfoResponse;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;

/**
 * リポジトリ[ユーザ任意情報の作成・変更]ダイアログクラス<BR>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeCustomInfoCreateDialog extends CommonDialog {

	/** 入力値を保持するオブジェクト. */
	private NodeConfigCustomInfoResponse m_inputData = null;

	/** 入力値の正当性を保持するオブジェクト. */
	private ValidateResult m_validateResult = null;

	/** ユーザ任意情報ID. */
	private Text m_customId = null;

	/** ユーザ任意情報名. */
	private Text m_customName = null;

	/** 説明. */
	private Text m_textDescription = null;

	/** コマンド. */
	private Text m_command = null;

	/** 実効ユーザ種別（エージェント起動ユーザ）. */
	private Button m_userAgent = null;

	/** 実効ユーザ種別（ユーザを指定する）. */
	private Button m_userSpecify = null;

	/** 実効ユーザを入力するテキストボックス. */
	private Text m_effectiveUser = null;

	/** この設定を有効にする。 */
	private Button m_buttonValid = null;

	/** 変更フラグ(ID非活性制御). */
	private boolean toModify = false;

	/** 重複チェック用ユーザ任意情報IDリスト */
	private ArrayList<String> registeredCustomIdList = null;

	/** 重複チェック用変更前ID */
	private String beforeId = null;

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param registeredCustomIdList
	 *            入力済の他のユーザ任意情報IDリスト(重複チェック用).
	 */
	public NodeCustomInfoCreateDialog(Shell parent, ArrayList<String> registeredCustomIdList) {
		super(parent);

		// デフォルト値の設定
		NodeConfigCustomInfoResponse info = new NodeConfigCustomInfoResponse();
		info.setSettingCustomId("");
		info.setDisplayName("");
		info.setDescription("");
		info.setCommand("");
		info.setSpecifyUser(Boolean.FALSE);
		info.setEffectiveUser("");
		info.setValidFlg(Boolean.TRUE);
		this.m_inputData = info;
		this.registeredCustomIdList = registeredCustomIdList;
		this.beforeId = null;
		this.toModify = false;
	}

	/**
	 * 変更/コピー用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト.
	 * @param info
	 *            前回入力値/コピー元.
	 * @param isModyfyDb
	 *            DB登録済データか. * @param isModyfyDb DB登録済データか.
	 * @param registeredCustomIdList
	 *            入力済の他のユーザ任意情報IDリスト(重複チェック用).
	 * @param toCopy
	 *            true:コピー、false:変更.
	 * 
	 */
	public NodeCustomInfoCreateDialog(Shell parent, NodeConfigCustomInfoResponse info, ArrayList<String> registeredCustomIdList,
			boolean toCopy) {
		super(parent);
		this.toModify = !toCopy;
		this.registeredCustomIdList = registeredCustomIdList;
		if (toCopy) {
			if (info != null) {
				NodeConfigCustomInfoResponse copyItem = new NodeConfigCustomInfoResponse();
				copyItem.setSettingCustomId(info.getSettingCustomId());
				copyItem.setDisplayName(info.getDisplayName());
				copyItem.setDescription(info.getDescription());
				copyItem.setCommand(info.getCommand());
				copyItem.setSpecifyUser(info.getSpecifyUser());
				copyItem.setEffectiveUser(info.getEffectiveUser());
				copyItem.setValidFlg(info.getValidFlg());
				this.m_inputData = copyItem;
			} else {
				this.m_inputData = null;
			}
			this.beforeId = null;
		} else {
			this.m_inputData = info;
			this.beforeId = info.getSettingCustomId();
		}
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.repository.custom.create.modify"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);
		this.setInput(parent, layout, shell);

	}

	/**
	 * 画面項目のセット.
	 *
	 */
	protected void setInput(Composite parent, GridLayout layout, Shell shell) {

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// --ユーザ任意情報ID.
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("node.custom.id") + " : ");
		// テキスト
		this.m_customId = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_customId.setLayoutData(gridData);
		this.m_customId.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// --ユーザ任意情報名.
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("node.custom.name") + " : ");
		// テキスト
		this.m_customName = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_customName.setLayoutData(gridData);
		this.m_customName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// --説明.
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("description") + " : ");
		// テキスト
		this.m_textDescription = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textDescription.setLayoutData(gridData);

		// --コマンド.
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("command") + " : ");
		// テキスト
		this.m_command = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_command.setLayoutData(gridData);
		this.m_command.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// --実行ユーザ.
		// 実行ユーザグループ.
		Group groupEffectiveUser = new Group(parent, SWT.NONE);
		groupEffectiveUser.setText(Messages.getString("effective.user"));
		layout = new GridLayout(15, true);
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		groupEffectiveUser.setLayout(layout);

		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 15;
		groupEffectiveUser.setLayoutData(gridData);

		// ラジオボタン(エージェント起動ユーザ).
		this.m_userAgent = new Button(groupEffectiveUser, SWT.RADIO);
		this.m_userAgent.setText(Messages.getString("agent.user"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 5;
		this.m_userAgent.setLayoutData(gridData);
		this.m_userAgent.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabledInputs();
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// ラジオボタン(ユーザを指定する).
		this.m_userSpecify = new Button(groupEffectiveUser, SWT.RADIO);
		this.m_userSpecify.setText(Messages.getString("specified.user"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 4;
		this.m_userSpecify.setLayoutData(gridData);
		this.m_userSpecify.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabledInputs();
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// テキストボックス(実行ユーザ指定用).
		this.m_effectiveUser = new Text(groupEffectiveUser, SWT.BORDER | SWT.LEFT);
		this.m_effectiveUser.setText("");
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 15 - (5 + 4);
		this.m_effectiveUser.setLayoutData(gridData);
		this.m_effectiveUser.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// -- 有効／無効
		this.m_buttonValid = new Button(parent, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.m_buttonValid.setLayoutData(gridData);
		this.m_buttonValid.setText(Messages.getString("setting.valid.confirmed"));

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 15;
		line.setLayoutData(gridData);

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(550, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		setInputData();
	}

	/**
	 * 活性制御
	 */
	private void setEnabledInputs() {
		// DB登録済みデータはID変更させない.
		if (this.toModify) {
			this.m_customId.setEnabled(false);
		} else {
			this.m_customId.setEnabled(true);
		}

		// 活性/非活性の切り替え.
		if (this.m_userSpecify.getSelection()) {
			this.m_effectiveUser.setEnabled(true);
		} else {
			this.m_effectiveUser.setEnabled(false);
		}
	}

	/**
	 * 入力値による表示更新処理.
	 */
	public void update() {

		// 必須項目の背景色を更新.
		Text[] needTexts = { this.m_customId, this.m_customName, this.m_command };
		this.updateTexts(needTexts);

		// 条件付必須項目.
		if (this.m_userSpecify.getSelection() && "".equals(m_effectiveUser.getText())) {
			this.m_effectiveUser.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_effectiveUser.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

	}

	/**
	 * Textの必須チェックをして背景色をセット.<br>
	 * <br>
	 * 空文字もしくの場合に赤.
	 * 
	 * @param needs
	 *            必須項目
	 */
	private void updateTexts(Text[] needs) {
		// 数値(空文字もしくは0の場合に赤).
		if (needs != null) {
			for (Text text : needs) {
				if ("".equals(text.getText())) {
					text.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
				} else {
					text.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				}
			}
		}
	}

	/**
	 * 入力値を保持したオブジェクトを返却する.
	 *
	 * @return 判定情報
	 */
	public NodeConfigCustomInfoResponse getInputData() {
		return this.m_inputData;
	}

	/**
	 * 引数で指定された判定情報の値を、各項目に設定します。
	 *
	 * @param info
	 *            設定値として用いる判定情報
	 */
	protected void setInputData() {

		// ユーザ任意情報ID.
		if (this.m_inputData.getSettingCustomId() != null) {
			this.m_customId.setText(m_inputData.getSettingCustomId());
		}

		// ユーザ任意情報名.
		if (this.m_inputData.getDisplayName() != null) {
			this.m_customName.setText(m_inputData.getDisplayName());
		}

		// 説明
		if (this.m_inputData.getDescription() != null) {
			this.m_textDescription.setText(m_inputData.getDescription());
		}

		// コマンド.
		if (this.m_inputData.getCommand() != null) {
			this.m_command.setText(m_inputData.getCommand());
		}

		// ユーザ任意情報名.
		if (this.m_inputData.getSpecifyUser()) {
			this.m_userSpecify.setSelection(true);
			this.m_userAgent.setSelection(false);
		} else {
			this.m_userSpecify.setSelection(false);
			this.m_userAgent.setSelection(true);
		}

		// 指定ユーザ.
		if (this.m_inputData.getEffectiveUser() != null) {
			this.m_effectiveUser.setText(m_inputData.getEffectiveUser());
		}

		// 有効／無効
		if (this.m_inputData.getValidFlg()) {
			this.m_buttonValid.setSelection(true);
		} else {
			this.m_buttonValid.setSelection(false);
		}

		// 必須項目を可視化
		this.setEnabledInputs();
		this.update();

	}

	/**
	 * 引数で指定された判定情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。<br>
	 * ダイアログ閉じるタイミングでManager通信を行わないため、Managerのチェックと重複しているチェックも行います.
	 *
	 * @return 判定情報
	 *
	 * @see #setValidateResult(String, String)
	 */
	private NodeConfigCustomInfoResponse createInputData() {
		NodeConfigCustomInfoResponse info = new NodeConfigCustomInfoResponse();
		String[] args = null;

		// カスタムID.
		if (this.m_customId.getText() != null && !"".equals((this.m_customId.getText()).trim())) {
			// 登録済IDと重複かつ前回入力or登録IDとは異なる..
			if (this.registeredCustomIdList != null && this.registeredCustomIdList.contains(this.m_customId.getText())
					&& (this.beforeId == null || !this.beforeId.equals(this.m_customId.getText()))) {
				args = new String[] { "", Messages.getString("node.custom.id") };
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.repository.32", args));
				return null;
			}
			info.setSettingCustomId(this.m_customId.getText());
		} else {
			args = new String[] { Messages.getString("node.custom.id") };
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.common.1", args));
			return null;
		}

		// 収集結果の表示名.
		if (this.m_customName.getText() != null && !"".equals((this.m_customName.getText()).trim())) {
			info.setDisplayName(this.m_customName.getText());
		} else {
			args = new String[] { Messages.getString("node.custom.name") };
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.common.1", args));
			return null;
		}

		// 説明
		if (this.m_textDescription.getText() != null && !"".equals((this.m_textDescription.getText()).trim())) {
			info.setDescription(this.m_textDescription.getText());
		}

		// コマンド.
		if (this.m_command.getText() != null && !"".equals((this.m_command.getText()).trim())) {
			info.setCommand(this.m_command.getText());
		} else {
			args = new String[] { Messages.getString("command") };
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.common.1", args));
			return null;
		}

		// 実行ユーザ.
		if (this.m_userSpecify.getSelection()) {
			info.setSpecifyUser(true);
			// 指定ユーザの入力.
			if (this.m_effectiveUser.getText() != null && !"".equals((this.m_effectiveUser.getText()).trim())) {
				info.setEffectiveUser(this.m_effectiveUser.getText());
			} else {
				args = new String[] { Messages.getString("effective.user") };
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.common.1", args));
				return null;
			}
		} else {
			info.setSpecifyUser(false);
		}

		// 有効
		if (this.m_buttonValid.getSelection()) {
			info.setValidFlg(true);
		} else {
			info.setValidFlg(false);
		}

		return info;
	}

	/**
	 * 無効な入力値をチェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see #createInputData()
	 */
	@Override
	protected ValidateResult validate() {
		// 入力値生成
		this.m_inputData = this.createInputData();

		if (this.m_inputData != null) {
			return super.validate();
		} else {
			return m_validateResult;
		}
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
	protected void setValidateResult(String id, String message) {

		this.m_validateResult = new ValidateResult();
		this.m_validateResult.setValid(false);
		this.m_validateResult.setID(id);
		this.m_validateResult.setMessage(message);
	}

	/**
	 * 入力値の判定を行います。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		NodeConfigCustomInfoResponse info = this.m_inputData;
		if (info != null) {
			result = true;
		}

		return result;
	}

	public String getCustomId() {
		return this.m_inputData.getSettingCustomId();
	}
}
