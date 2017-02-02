/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.http.dialog;


import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.TextWithParameterComposite;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.http.composite.PatternCompositeDefine;
import com.clustercontrol.http.composite.VariableCompositeDefine;
import com.clustercontrol.monitor.run.composite.TableItemInfoComposite;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.monitor.Page;
import com.clustercontrol.ws.monitor.Pattern;
import com.clustercontrol.ws.monitor.Variable;

/**
 * HttpScenario監視（文字列）作成・変更ダイアログクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class PageCreateDialog extends CommonDialog {

	// ----- static フィールド ----- //
	private static final String HTTP_PREFIX = "http://";
	private static final String HTTPS_PREFIX = "https://";
	private static final String DEFAULT_STATUS_CODE = "200";
	private static final String STATUS_CODE_PATTERN = "^[0-9]{3}(,[0-9]{3})*";

	// ----- instance フィールド ----- //

	/** シェル */
	private Shell shell = null;

	/** 入力値を保持するオブジェクト。 */
	private Page m_inputData = null;

	/** 文字列監視判定情報 */
	protected TableItemInfoComposite<Pattern> m_pattern = null;

	/** URL */
	private TextWithParameterComposite m_textRequestUrl = null;

	/** 説明 */
	private Text m_textDescription = null;

	/** ステータスコード */
	private Text m_textStatusCode = null;

	/** POST変数 */
	private Text m_textPost = null;

	/** 通知 */
	private Combo m_comboNotify = null;

	/** メッセージ */
	private Text m_textMessage = null;

	/** 変数 */
	private TableItemInfoComposite<Variable> m_variable = null;

	protected Group groupDetermine = null;			// 判定グループ
	protected Group groupNotifyAttribute = null;	// 通知グループ

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public PageCreateDialog(Shell parent) {
		super(parent);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param monitorId 変更する監視項目ID
	 * @param updateFlg 更新するか否か（true:変更、false:新規登録）
	 */
	public PageCreateDialog(Shell parent, Page page) {
		super(parent);
		this.m_inputData = page;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		shell = this.getShell();

		super.customizeDialog(parent);

		// タイトル
		shell.setText(Messages.getString("dialog.monitor.http.scenario.create.modify.page"));

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
		 * 説明
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "description", label);
		gridData = new GridData();
		gridData.horizontalSpan = CommonMonitorDialog.WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("description") + " : ");
		// テキスト
		this.m_textDescription = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "description", m_textDescription);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textDescription.setLayoutData(gridData);
		this.m_textDescription.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});


		/*
		 * チェック設定グループ（条件グループの子グループ）
		 */
		Group groupCheckRule = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "checkrule", groupCheckRule);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupCheckRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCheckRule.setLayoutData(gridData);
		groupCheckRule.setText(Messages.getString("monitor.http.scenario.page.check.rule"));


		/*
		 * URL
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "url", label);
		gridData = new GridData();
		gridData.horizontalSpan = CommonMonitorDialog.WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("request.url") + " : ");
		// テキスト
		this.m_textRequestUrl = new TextWithParameterComposite(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "requesturl", m_textRequestUrl);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textRequestUrl.setLayoutData(gridData);
		this.m_textRequestUrl.setToolTipText(Messages.getString("monitor.http.scenario.page.request.url.tool.tip"));
		this.m_textRequestUrl.setColor(new Color(parent.getDisplay(), new RGB(0, 0, 255)));
		this.m_textRequestUrl.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * ステータスコード
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "statuscode", label);
		gridData = new GridData();
		gridData.horizontalSpan = CommonMonitorDialog.WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("status.code") + " : ");
		// テキスト
		this.m_textStatusCode = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "statuscode", m_textStatusCode);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textStatusCode.setLayoutData(gridData);
		this.m_textStatusCode.setToolTipText(Messages.getString("monitor.http.scenario.page.status.code.tooltip"));
		this.m_textStatusCode.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * POST
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "scenariopost", label);
		gridData = new GridData();
		gridData.horizontalSpan = CommonMonitorDialog.WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.http.scenario.post") + " : ");
		// テキスト
		this.m_textPost = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.MULTI | SWT.WRAP);
		WidgetTestUtil.setTestId(this, "post", m_textPost);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = 15;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		this.m_textPost.setLayoutData(gridData);
		this.m_textPost.setToolTipText(Messages.getString("monitor.http.scenario.page.post.tooltip"));
		this.m_textPost.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 判定グループ（チェック設定グループの子グループ）
		 */
		groupDetermine = new Group(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "determine", groupDetermine);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 1;
		groupDetermine.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupDetermine.setLayoutData(gridData);
		groupDetermine.setText(Messages.getString("monitor.http.scenario.page.content.determine"));

		// 文字列判定定義情報
		this.m_pattern = new TableItemInfoComposite<Pattern>(groupDetermine, SWT.NONE, new PatternCompositeDefine());
		WidgetTestUtil.setTestId(this, "pattern", m_pattern);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 220;
		m_pattern.setLayoutData(gridData);

		/*
		 * 通知グループ（監視グループの子グループ）
		 */
		groupNotifyAttribute = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notifyattribute", groupNotifyAttribute);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupNotifyAttribute.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupNotifyAttribute.setLayoutData(gridData);
		groupNotifyAttribute.setText(Messages.getString("notify.attribute"));

		/*
		 * 通知
		 */
		// ラベル
		label = new Label(groupNotifyAttribute, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notify", label);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.http.scenario.page.notify.priority.on.fail") + " : ");
		// コンボ
		this.m_comboNotify = new Combo(groupNotifyAttribute, SWT.BORDER | SWT.LEFT | SWT.SINGLE | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "notify", m_comboNotify);

		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboNotify.setLayoutData(gridData);
		this.m_comboNotify.add(PriorityMessage.STRING_CRITICAL);
		this.m_comboNotify.add(PriorityMessage.STRING_WARNING);
		this.m_comboNotify.add(PriorityMessage.STRING_INFO);
		this.m_comboNotify.add(PriorityMessage.STRING_UNKNOWN);
		this.m_comboNotify.add(PriorityMessage.STRING_NONE);
		this.m_comboNotify.setText(PriorityMessage.STRING_CRITICAL);
		this.m_comboNotify.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupNotifyAttribute, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank", label);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 説明
		 */
		// ラベル
		label = new Label(groupNotifyAttribute, SWT.NONE);
		WidgetTestUtil.setTestId(this, "message", label);
		gridData = new GridData();
		gridData.horizontalSpan = CommonMonitorDialog.WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("message") + " : ");
		// テキスト
		this.m_textMessage = new Text(groupNotifyAttribute, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "message", m_textMessage);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textMessage.setLayoutData(gridData);
		this.m_textMessage.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 変数設定グループ
		 */
		Group groupVariable = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "variable", groupVariable);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupVariable.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupVariable.setLayoutData(gridData);
		groupVariable.setText(Messages.getString("monitor.http.scenario.available.variable.on.next.page"));

		// テーブルコンポジット
		this.m_variable = new TableItemInfoComposite<Variable>(groupVariable, SWT.NONE, new VariableCompositeDefine());
		WidgetTestUtil.setTestId(this, "variable", m_variable);
		gridData = new GridData();
		gridData.horizontalSpan = CommonMonitorDialog.WIDTH_TITLE + 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_variable.setLayoutData(gridData);


		// ダイアログを調整
		this.adjustDialog();

		setInputData();
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示
		if(HTTP_PREFIX.equals(this.m_textRequestUrl.getText()) || !(this.m_textRequestUrl.getText().startsWith(HTTP_PREFIX) || this.m_textRequestUrl.getText().startsWith(HTTPS_PREFIX))){
			this.m_textRequestUrl.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textRequestUrl.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if ("".equals((this.m_textStatusCode.getText()).trim()) || !java.util.regex.Pattern.compile(STATUS_CODE_PATTERN).matcher(this.m_textStatusCode.getText()).matches()) {
			this.m_textStatusCode.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textStatusCode.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(!"".equals(this.m_comboNotify.getText().trim()) && "".equals(this.m_textMessage.getText().trim())){
			this.m_textMessage.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textMessage.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 各項目に入力値を設定します。
	 */
	protected void setInputData() {
		if(this.m_inputData == null){
			this.m_inputData = new Page();
		} else {
			this.m_comboNotify.select(this.m_comboNotify.indexOf(PriorityMessage.typeToString(this.m_inputData.getPriority())));
		}

		if(this.m_inputData.getMessage() != null){
			this.m_textMessage.setText(this.m_inputData.getMessage());
		}

		if(this.m_inputData.getDescription() != null){
			this.m_textDescription.setText(this.m_inputData.getDescription());
		}
		if (this.m_inputData.getUrl() != null) {
			this.m_textRequestUrl.setText(this.m_inputData.getUrl());
		} else {
			this.m_textRequestUrl.setText(HTTP_PREFIX);
		}
		if(this.m_inputData.getStatusCode() != null){
			this.m_textStatusCode.setText(this.m_inputData.getStatusCode());
		} else {
			this.m_textStatusCode.setText(DEFAULT_STATUS_CODE);
		}
		if(this.m_inputData.getPost() != null){
			this.m_textPost.setText(this.m_inputData.getPost());
		}
		if(this.m_inputData.getPatterns() != null){
			this.m_pattern.setInputData(this.m_inputData.getPatterns());
		}

		if(this.m_inputData.getVariables() != null){
			this.m_variable.setInputData(this.m_inputData.getVariables());
		}

		// 必須項目を明示
		this.update();
	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	protected Page createInputData() {
		Page page = new Page();

		page.setDescription(this.m_textDescription.getText());
		page.setUrl(this.m_textRequestUrl.getText());
		page.setStatusCode(this.m_textStatusCode.getText());
		page.setPost(this.m_textPost.getText());
		page.setPriority(PriorityMessage.stringToType(this.m_comboNotify.getText()));
		if(!"".equals(this.m_comboNotify.getText().trim())){
			page.setMessage(this.m_textMessage.getText());
		}
		List<Pattern> patterns = page.getPatterns();
		patterns.clear();
		patterns.addAll(this.m_pattern.getItems());
		List<Variable> variables = page.getVariables();
		variables.clear();
		variables.addAll(this.m_variable.getItems());

		return page;
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

		this.m_inputData = createInputData();

		if(this.m_inputData != null){
			result = true;
		}

		return result;
	}

	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	protected void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(600, shell.getSize().y));

		// 画面中央に配置
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}

	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		if ("".equals((this.m_textRequestUrl.getText()).trim())) {
			result = createValidateResult(Messages.getString("message.hinemos.1"), Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("request.url")}));
		}

		if ("".equals((this.m_textStatusCode.getText()).trim())) {
			result = createValidateResult(Messages.getString("message.hinemos.1"), Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("status.code")}));
		}

		if (!java.util.regex.Pattern.compile(STATUS_CODE_PATTERN).matcher(this.m_textStatusCode.getText()).matches()) {
			result = createValidateResult(Messages.getString("message.hinemos.1"), Messages.getString("message.monitor.http.scenario.invalid.input", new Object[]{Messages.getString("status.code")}));
		}

		if(!"".equals(this.m_comboNotify.getText().trim()) && "".equals(this.m_textMessage.getText().trim())){
			result = createValidateResult(Messages.getString("message.hinemos.1"), Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("message")}));
		}

		if(this.m_pattern.getItems().isEmpty()){
			result = createValidateResult(Messages.getString("message.hinemos.1"), Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("monitor.http.scenario.page.content.determine")}));
		}

		if(result == null){
			result = super.validate();
		}
		return result;
	}

	private ValidateResult createValidateResult(String id, String message){
		ValidateResult result = new ValidateResult();
		result.setID(id);
		result.setMessage(message);
		result.setValid(false);
		return result;
	}

	/**
	 * 入力値を保持した文字列監視の判定情報を返します。
	 *
	 * @return 判定情報
	 */
	public Page getInputData() {
		return this.m_inputData;
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
}
