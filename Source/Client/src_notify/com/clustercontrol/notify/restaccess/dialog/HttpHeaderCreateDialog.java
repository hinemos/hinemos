/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.dialog;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.restaccess.viewer.HttpHeaderTableRecord;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * httpヘッダー情報（RESTアクセス情報向け）作成・変更 ダイアログクラス<BR>
 *
 */
public class HttpHeaderCreateDialog  extends CommonDialog {

	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = 4;

	/** カラム数（値）。 */
	public static final int WIDTH_VALUE = 2;

	/** valueでのtooltip表示要否 */
	private boolean m_isTooltipDispray = false;

	/** 入力値を保持するオブジェクト。 */
	private HttpHeaderTableRecord m_inputData = null;

	/** 入力値の正当性を保持するオブジェクト。 */
	private ValidateResult m_validateResult = null;

	/** key */
	private Text m_textKey = null;

	/** value */
	private Text m_textValue = null;

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public HttpHeaderCreateDialog(Shell parent) {
		super(parent);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param identifier 変更する文字列監視の判定情報の識別キー
	 */
	public HttpHeaderCreateDialog(Shell parent, HttpHeaderTableRecord variable , boolean isTooltipDispray ) {
		super(parent);
		this.m_inputData = variable;
		m_isTooltipDispray = isTooltipDispray;
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
		shell.setText(Messages.getString("dialog.restaccess.httpheader.create.modify"));

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
		 * key 
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("key") + " : ");
		// テキスト
		this.m_textKey = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textKey.setLayoutData(gridData);
		this.m_textKey.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 値
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("value") + " : ");
		// テキスト
		this.m_textValue = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textValue.setLayoutData(gridData);
		this.m_textValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		if(m_isTooltipDispray){
			//変数置換向けのツールチップメッセージ
			String tooletipTextForRep = Messages.getString("notify.parameter.tooltip")
					+ Messages.getString("replace.parameter.restaccess")
					+ Messages.getString("replace.parameter.notify")
					+ Messages.getString("replace.parameter.node");
			this.m_textValue.setToolTipText(tooletipTextForRep);
		}
			


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

		// 識別子が指定されている場合、その情報を初期表示する。
		HttpHeaderTableRecord variable = this.m_inputData;
		if (variable == null) {
			variable = new HttpHeaderTableRecord();
		}

		this.setInputData(variable);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を可視化
		if("".equals(this.m_textKey.getText())){
			this.m_textKey.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textKey.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
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
	public HttpHeaderTableRecord getInputData() {
		return this.m_inputData;
	}

	/**
	 * 引数で指定された判定情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる判定情報
	 */
	public void setInputData(HttpHeaderTableRecord variable) {

		this.m_inputData = variable;

		if(this.m_inputData.getKey()!= null){
			this.m_textKey.setText(this.m_inputData.getKey());
		}

		if(this.m_inputData.getValue() != null){
			this.m_textValue.setText(this.m_inputData.getValue());
		}

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
	private HttpHeaderTableRecord createInputData() {
		HttpHeaderTableRecord info = new HttpHeaderTableRecord();

		// key
		if (this.m_textKey.getText() != null
				&& !"".equals((this.m_textKey.getText()).trim())) {
			info.setKey(this.m_textKey.getText());
		} else {
			this.setValidateResult(Messages.getString("message.hinemos.1"),	Messages.getString("validation.required_input.message", new Object[]{Messages.getString("key")}));
			return null;
		}

		// 値
		if (this.m_textValue.getText() != null
				&& !"".equals((this.m_textValue.getText()).trim())) {
			info.setValue(this.m_textValue.getText());
		} else {
			this.setValidateResult(Messages.getString("message.hinemos.1"),	Messages.getString("validation.required_input.message", new Object[]{Messages.getString("value")}));
			return null;
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

		HttpHeaderTableRecord info = this.m_inputData;
		if(info != null){
			result = true;
		}

		return result;
	}
}
