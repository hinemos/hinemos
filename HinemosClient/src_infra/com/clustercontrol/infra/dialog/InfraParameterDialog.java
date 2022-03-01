/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.dialog;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.InfraManagementParamInfoResponse;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 環境構築変数ダイアログクラスです。
 *
 * @version 6.1.0
 */
public class InfraParameterDialog extends CommonDialog {

	/** 変数名用テキスト */
	private Text m_txtParamId = null;
	/** 説明用テキスト */
	private Text m_txtDescription = null;
	/** 値用テキスト */
	private Text m_txtValue = null;

	/** パスワード表示用チェックボックス*/
	private Button m_chkPasswordFlg = null;

	/** 環境構築変数情報 */
	private InfraManagementParamInfoResponse m_infraManagementParamInfo = null;

	/** 環境構築変数リスト */
	private Map<String, InfraManagementParamInfoResponse> m_parentInfraManagementParamMap = new HashMap<>();
	
	/**
	 * コンストラクタ
	 * 変更時
	 * @param parent
	 * @param paramInfo
	 * @param mode
	 */
	public InfraParameterDialog(Shell parent, Map<String, InfraManagementParamInfoResponse> parentInfraManagementParamMap,
			InfraManagementParamInfoResponse infraManagementParamInfo){
		super(parent);
		this.m_infraManagementParamInfo = infraManagementParamInfo;
		this.m_parentInfraManagementParamMap = parentInfraManagementParamMap;
	}

	/**
	 * コンストラクタ
	 * 新規作成時
	 * @param parent
	 */
	public InfraParameterDialog(Shell parent,
			Map<String, InfraManagementParamInfoResponse> parentInfraManagementParamMap){
		super(parent);
		this.m_parentInfraManagementParamMap = parentInfraManagementParamMap;
		this.m_infraManagementParamInfo = new InfraManagementParamInfoResponse();
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 *
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		parent.getShell().setText(Messages.getString("infra.parameter"));

		Label label = null;

		/**
		 * レイアウト設定
		 * ダイアログ内のベースとなるレイアウトが全てを変更
		 */
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 0;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.marginBottom = 0;
		layout.fill = true;
		parent.setLayout(layout);

		// Composite
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		// 名前（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("name") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 名前（テキスト）
		this.m_txtParamId = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtParamId", this.m_txtParamId);
		this.m_txtParamId.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtParamId.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		// dummy
		new Label(composite, SWT.NONE);

		// 説明（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("description") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 説明（テキスト）
		this.m_txtDescription = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtDescription", this.m_txtDescription);
		this.m_txtDescription.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtDescription.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// dummy
		new Label(composite, SWT.NONE);

		// 値（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("value") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 値（テキスト）
		this.m_txtValue = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtValue", this.m_txtValue);
		this.m_txtValue.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtValue.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// dummy
		new Label(composite, SWT.NONE);

		// dummy
		new Label(composite, SWT.NONE);

		// パスワード表示にする
		this.m_chkPasswordFlg = new Button(composite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_chkPasswordFlg", this.m_chkPasswordFlg);
		this.m_chkPasswordFlg.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_chkPasswordFlg.setText(Messages.getString("show.password"));
		this.m_chkPasswordFlg.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.getSource();
				if (button.getSelection()) {
					m_txtValue.setEchoChar('*');
				} else {
					m_txtValue.setEchoChar('\0');
					m_txtValue.setText("");
				}
				update();
			}
		});

		// dummy
		new Label(composite, SWT.NONE);

		// 環境変数情報反映
		reflectParamInfo();

		// 更新処理
		update();
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 200);
	}


	/**
	 * 環境構築変数情報をコンポジットに反映します。
	 *
	 */
	public void reflectParamInfo() {
		if (this.m_infraManagementParamInfo != null) {
			// 名前
			if (this.m_infraManagementParamInfo.getParamId() != null) {
				this.m_txtParamId.setText(this.m_infraManagementParamInfo.getParamId());
			}
			// 説明
			if (this.m_infraManagementParamInfo.getDescription() != null) {
				this.m_txtDescription.setText(this.m_infraManagementParamInfo.getDescription());
			}
			// 値
			if (this.m_infraManagementParamInfo.getValue() != null) {
				this.m_txtValue.setText(this.m_infraManagementParamInfo.getValue());
			}
			// パスワード表示
			if (this.m_infraManagementParamInfo.getPasswordFlg() != null) {
				this.m_chkPasswordFlg.setSelection(this.m_infraManagementParamInfo.getPasswordFlg());
				if (this.m_infraManagementParamInfo.getPasswordFlg()) {
					this.m_txtValue.setEchoChar('*');
				}
			}
		}
	}

	/**
	 * 環境構築情報 更新処理
	 *
	 */
	public void update(){

		// 必須項目を明示
		if("".equals(this.m_txtParamId.getText())){
			this.m_txtParamId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtParamId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if ("".equals(this.m_txtValue.getText())){
			this.m_txtValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_txtValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
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

		if (this.m_infraManagementParamInfo == null) {
			// 新規作成
			this.m_infraManagementParamInfo = new InfraManagementParamInfoResponse();
		}

		// 変数名
		if (this.m_txtParamId.getText() != null
				&& !this.m_txtParamId.getText().equals("")) {
			// 重複チェック
			if (isParameterDuplicate(this.m_txtParamId.getText(), this.m_infraManagementParamInfo.getParamId())) {
				// 変数名の重複エラー
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.infra.param.duplicated"));
				return result;
			}
			this.m_infraManagementParamInfo.setParamId(this.m_txtParamId.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.infra.param.id.required"));
			return result;
		}

		// 説明
		if (this.m_txtDescription.getText() != null
				&& !this.m_txtDescription.getText().equals("")) {
			this.m_infraManagementParamInfo.setDescription(this.m_txtDescription.getText());
		}

		// 値
		if (this.m_txtValue.getText() != null
				&& !this.m_txtValue.getText().equals("")) {
			this.m_infraManagementParamInfo.setValue(this.m_txtValue.getText());
		}else{
			// ここはそのまま空欄にし、マネージャのValidatorでまとめて確認
			this.m_infraManagementParamInfo.setValue("");
		}

		// パスワード表示
		this.m_infraManagementParamInfo.setPasswordFlg(this.m_chkPasswordFlg.getSelection());

		return null;
	}

	/**
	 * 環境構築変数情報を返します。
	 *
	 * @return 環境構築変数情報
	 */
	public InfraManagementParamInfoResponse getInputData() {
		return this.m_infraManagementParamInfo;
	}


	/**
	 * 変数情報に重複した値が設定されているか
	 * 
	 * @param newParamId 変更後変数
	 * @param oldParamId 変更前変数
	 * @return true:重複あり, false:重複なし
	 */
	private boolean isParameterDuplicate(String newParamId, String oldParamId) {
		boolean result = false;
		if (m_parentInfraManagementParamMap == null) {
			// データがない場合は処理終了
			return result;
		}
		if (oldParamId != null && oldParamId.equals(newParamId)) {
			// キーに変更がない場合は処理終了
			return result;
		}
		for (Map.Entry<String, InfraManagementParamInfoResponse> entry : m_parentInfraManagementParamMap.entrySet()) {
			if (oldParamId != null && entry.getKey().equals(oldParamId)) {
				continue;
			}
			if (entry.getKey().equals(newParamId)) {
				result = true;
				break;
			}
		}
		return result;
	}
}
