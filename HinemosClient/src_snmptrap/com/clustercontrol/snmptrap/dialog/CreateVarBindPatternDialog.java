/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.dialog;

import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.VarBindPatternResponse;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * フィルタ[作成・変更]ダイアログクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class CreateVarBindPatternDialog extends CommonDialog {

	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = 4;

	/** 入力値を保持するオブジェクト。 */
	private VarBindPatternResponse m_inputData = null;

	/** 入力値の正当性を保持するオブジェクト。 */
	private ValidateResult m_validateResult = null;

	/** 説明。 */
	private Text m_textDescription = null;

	/** パターンマッチ表現。 */
	private Text m_textPattern = null;

	/** 重要度。 */
	private Combo m_comboPriority = null;

	/** 条件に一致したら処理しない。 */
	private Button m_radioNotProcess = null;

	/** 条件に一致したら処理する。 */
	private Button m_radioProcess = null;

	/** 大文字・小文字を区別しない */
	private Button m_checkCaseSensitive = null;

	/** この設定を有効にする。 */
	private Button m_buttonValid = null;

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public CreateVarBindPatternDialog(Shell parent) {
		super(parent);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param identifier 変更する文字列監視の判定情報の識別キー
	 */
	public CreateVarBindPatternDialog(Shell parent, VarBindPatternResponse pattern) {
		super(parent);

		this.m_inputData = pattern;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.composite.NotifyIdListComposite#NotifyIdListComposite(Composite, int, boolean)
	 * @see com.clustercontrol.monitor.run.composite.StringRunJobComposite#StringRunJobComposite(Composite, int)
	 * @see com.clustercontrol.monitor.run.util.StringValueInfoManager#getJobEditState(String)
	 * @see com.clustercontrol.monitor.run.bean.MonitorStringValueInfo
	 * @see #setInputData(Pattern)
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.monitor.snmptrap.create.modify.pattern"));

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
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("description") + " : ");
		// テキスト
		this.m_textDescription = new Text(parent, SWT.BORDER | SWT.LEFT);
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
		 * 監視条件グループ
		 */
		// グループ
		Group monitorRuleGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "monitorrule", monitorRuleGroup);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		monitorRuleGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		monitorRuleGroup.setLayoutData(gridData);
		monitorRuleGroup.setText(Messages.getString("monitor.rule"));

		/*
		 * パターンマッチ表現
		 */
		// ラベル
		label = new Label(monitorRuleGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "matching", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("pattern.matching.expression") + " : ");
		// テキスト
		this.m_textPattern = new Text(monitorRuleGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "pattern", m_textPattern);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textPattern.setLayoutData(gridData);
		this.m_textPattern.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(monitorRuleGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 大文字・小文字をチェックしない
		this.m_checkCaseSensitive = new Button(monitorRuleGroup, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "casesensitive", m_checkCaseSensitive);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_checkCaseSensitive.setLayoutData(gridData);
		this.m_checkCaseSensitive.setText(Messages.getString("case.sensitive"));

		/*
		 * 出力の条件
		 */

		// 条件に一致したら処理する
		this.m_radioProcess = new Button(monitorRuleGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "process", m_radioProcess);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioProcess.setLayoutData(gridData);
		this.m_radioProcess.setText(Messages.getString("process.if.matched"));
		this.m_radioProcess.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// 空白
		label = new Label(monitorRuleGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		this.m_comboPriority = new Combo(monitorRuleGroup, SWT.BORDER | SWT.LEFT | SWT.SINGLE | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "priority", m_comboPriority);

		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboPriority.setLayoutData(gridData);
		this.m_comboPriority.add(PriorityMessage.STRING_CRITICAL);
		this.m_comboPriority.add(PriorityMessage.STRING_WARNING);
		this.m_comboPriority.add(PriorityMessage.STRING_INFO);
		this.m_comboPriority.add(PriorityMessage.STRING_UNKNOWN);
		this.m_comboPriority.setText(PriorityMessage.STRING_CRITICAL);
		this.m_comboPriority.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(monitorRuleGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space3", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 条件に一致したら処理しない
		this.m_radioNotProcess = new Button(monitorRuleGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "notprocess", m_radioNotProcess);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioNotProcess.setLayoutData(gridData);
		this.m_radioNotProcess.setText(Messages.getString("don't.process.if.matched"));
		this.m_radioNotProcess.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// 空白
		label = new Label(monitorRuleGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space4", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		/*
		 * 有効／無効
		 */
		this.m_buttonValid = new Button(parent, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "valid", m_buttonValid);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.m_buttonValid.setLayoutData(gridData);
		this.m_buttonValid.setText(Messages.getString("setting.valid.confirmed"));

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
		VarBindPatternResponse info = this.m_inputData;
		if (info == null) {
			info = new VarBindPatternResponse();
			info.setProcessType(true);
			info.setValidFlg(true);
		}

		this.setInputData(info);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を可視化
		if("".equals(this.m_textPattern.getText())){
			this.m_textPattern.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textPattern.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		this.m_comboPriority.setEnabled(this.m_radioProcess.getSelection());
	}


	/**
	 * 入力値を保持した文字列監視の判定情報を返します。
	 *
	 * @return 判定情報
	 */
	public VarBindPatternResponse getInputData() {
		return this.m_inputData;
	}

	/**
	 * 引数で指定された判定情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる判定情報
	 */
	protected void setInputData(VarBindPatternResponse info) {

		this.m_inputData = info;

		// 説明
		if (info.getDescription() != null) {
			this.m_textDescription.setText(info.getDescription());
		}

		// パターンマッチ表現
		if (info.getPattern() != null) {
			this.m_textPattern.setText(info.getPattern());
		}

		// 処理する(true)->異常／処理しない(false)->正常
		if (info.getProcessType()) {
			this.m_radioProcess.setSelection(true);
		} else {
			this.m_radioNotProcess.setSelection(true);
		}

		// 重要度
		if (info.getPriority() != null) {
			this.m_comboPriority.setText(PriorityMessage.codeToString(info.getPriority().toString()));
		}

		// 大文字・小文字を区別しない
		if (info.getCaseSensitivityFlg() != null && info.getCaseSensitivityFlg()){
			this.m_checkCaseSensitive.setSelection(true);
		}

		// 有効／無効
		if (info.getValidFlg() != null && info.getValidFlg()) {
			this.m_buttonValid.setSelection(true);
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
	private VarBindPatternResponse createInputData() {
		VarBindPatternResponse info = new VarBindPatternResponse();

		// 説明
		if (this.m_textDescription.getText() != null
				&& !"".equals((this.m_textDescription.getText()).trim())) {
			info.setDescription(this.m_textDescription.getText());
		}

		// パターンマッチ表現
		if (this.m_textPattern.getText() != null
				&& !"".equals((this.m_textPattern.getText()).trim())) {

			try{
				java.util.regex.Pattern.compile(this.m_textPattern.getText());
				info.setPattern(this.m_textPattern.getText());
			}
			catch(PatternSyntaxException e){
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.monitor.27"));
				return null;
			}

		} else {
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.monitor.27"));
			return null;
		}

		// 処理する／しない
		if (this.m_radioProcess.getSelection()) {
			info.setProcessType(true);
		} else {
			info.setProcessType(false);
		}

		// 重要度
		info.setPriority(PriorityMessage.stringToEnum(
				this.m_comboPriority.getText(), VarBindPatternResponse.PriorityEnum.class));

		// 大文字・小文字を区別する/しない
		if (this.m_checkCaseSensitive.getSelection()){
			info.setCaseSensitivityFlg(true);
		}else{
			info.setCaseSensitivityFlg(false);
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

		VarBindPatternResponse info = this.m_inputData;
		if(info != null){
			result = true;
		}

		return result;
	}
}
