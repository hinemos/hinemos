/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.JobCommandParam;
import com.clustercontrol.ws.monitor.Variable;

/**
 * フィルタ[作成・変更]ダイアログクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class JobCommandParameterDialog extends CommonDialog {

	/** カラム数（タイトル） **/
	public static final int WIDTH_TITLE = 4;

	/** カラム数（値） **/
	public static final int WIDTH_VALUE = 2;

	/** 入力値を保持するオブジェクト **/
	private JobCommandParam m_inputData = null;

	/** 入力値の正当性を保持するオブジェクト **/
	private ValidateResult m_validateResult = null;

	/** 説明 **/
	private Text m_textName = null;

	/** パターンマッチ表現 **/
	private Text m_textValue = null;

	/** この設定を有効にする **/
	private Button m_buttonMatchingWithResponse = null;

	/**
	 * 作成：MODE_ADD = 0;
	 * 変更：MODE_MODIFY = 1;
	 * */
	private int m_mode = PropertyDefineConstant.MODE_ADD;

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public JobCommandParameterDialog(Shell parent) {
		super(parent);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param identifier 変更する文字列監視の判定情報の識別キー
	 */
	public JobCommandParameterDialog(Shell parent, JobCommandParam variable) {
		super(parent);

		this.m_inputData = variable;
		this.m_mode = PropertyDefineConstant.MODE_MODIFY;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see #setInputData(Variable)
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.job.add.modify.command.param"));

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
		 * 名前
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "variablename", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("name") + " : ");
		// テキスト
		this.m_textName = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "name", m_textName);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textName.setLayoutData(gridData);
		this.m_textName.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		this.m_textName.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		if (this.m_mode == PropertyDefineConstant.MODE_MODIFY) {
			this.m_textName.setEnabled(false);
		}

		/*
		 * 値
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "value", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("value") + " : ");
		// テキスト
		this.m_textValue = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "value", m_textValue);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textValue.setLayoutData(gridData);
		this.m_textValue.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_256));
		this.m_textValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 標準出力から取得
		 */
		this.m_buttonMatchingWithResponse = new Button(parent, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "machingwithresponse", m_buttonMatchingWithResponse);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_buttonMatchingWithResponse.setLayoutData(gridData);
		this.m_buttonMatchingWithResponse.setText(Messages.getString("job.standard.output"));

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
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

		// 識別子が指定されている場合、その情報を初期表示する。
		JobCommandParam variable = this.m_inputData;
		if (variable == null) {
			variable = new JobCommandParam();
			variable.setJobStandardOutputFlg(false);
		}

		this.setInputData(variable);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を可視化
		if("".equals(this.m_textName.getText())){
			this.m_textName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_textValue.getText())){
			this.m_textValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}



	/**
	 * 入力値を保持した文字列監視の判定情報を返します。
	 *
	 * @return 判定情報
	 */
	public JobCommandParam getInputData() {
		return this.m_inputData;
	}

	/**
	 * 引数で指定された判定情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる判定情報
	 */
	protected void setInputData(JobCommandParam variable) {

		this.m_inputData = variable;

		if(this.m_inputData.getParamId() != null){
			this.m_textName.setText(this.m_inputData.getParamId());
		}

		if(this.m_inputData.getValue() != null){
			this.m_textValue.setText(this.m_inputData.getValue());
		}

		this.m_buttonMatchingWithResponse.setSelection(this.m_inputData.isJobStandardOutputFlg());

		// 必須項目を可視化
		this.update();

	}

	/**
	 * 引数で指定された判定情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 判定情報
	 *
	 * @see #setValidateResult(String, String)
	 */
	private JobCommandParam createInputData() {
		JobCommandParam info = new JobCommandParam();

		// 名前
		if (this.m_textName.getText() != null
				&& !"".equals((this.m_textName.getText()).trim())) {
			info.setParamId(this.m_textName.getText());
		} else {
			this.setValidateResult(Messages.getString("message.hinemos.1"),	Messages.getString("message.job.19"));
			return null;
		}

		// 値
		if (this.m_textValue.getText() != null
				&& !"".equals((this.m_textValue.getText()).trim())) {
			info.setValue(this.m_textValue.getText());
		} else {
			this.setValidateResult(Messages.getString("message.hinemos.1"),	Messages.getString("message.job.17"));
			return null;
		}

		// 標準出力から取得
		if (this.m_buttonMatchingWithResponse.getSelection()) {
			info.setJobStandardOutputFlg(true);
		} else {
			info.setJobStandardOutputFlg(false);
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
	 * @param id ID
	 * @param message メッセージ
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

		JobCommandParam info = this.m_inputData;
		if(info != null){
			result = true;
		}

		return result;
	}
}
