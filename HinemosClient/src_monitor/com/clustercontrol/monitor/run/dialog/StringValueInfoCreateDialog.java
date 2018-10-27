/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.dialog;

import java.util.regex.Pattern;
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

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.monitor.MonitorStringValueInfo;

/**
 * フィルタ[作成・変更]ダイアログクラス<BR>
 *
 * @version 6.1.0 バイナリ監視用フィルタ追加対応
 * @since 2.1.0
 */
public class StringValueInfoCreateDialog extends CommonDialog {


	/** 入力値を保持するオブジェクト。 */
	private MonitorStringValueInfo m_inputData = null;

	/** 入力値の正当性を保持するオブジェクト。 */
	private ValidateResult m_validateResult = null;

	/** 説明。 */
	private Text m_textDescription = null;

	/** パターンマッチ表現。 */
	private Text m_textPattern = null;

	/** 条件に一致したら処理しない。 */
	private Button m_radioNotProcess = null;

	/** 条件に一致したら処理する。 */
	private Button m_radioProcess = null;

	/** 大文字・小文字を区別しない */
	private Button m_checkCaseSensitive = null;

	/** 重要度。 */
	private Combo m_comboPriority = null;

	/** メッセージ。 */
	private Text m_textMessage = null;

	/** この設定を有効にする。 */
	private Button m_buttonValid = null;

	/** メッセージにデフォルト値を入れるフラグ */
	private boolean logLineFlag = false;

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public StringValueInfoCreateDialog(Shell parent, boolean logLineFlag) {
		super(parent);
		this.logLineFlag = logLineFlag;

		// 識別子が指定されている場合、その情報を初期表示する。
		MonitorStringValueInfo info = new MonitorStringValueInfo();
		info.setProcessType(true);
		info.setValidFlg(true);
		info.setCaseSensitivityFlg(false);
		info.setPriority(PriorityConstant.TYPE_CRITICAL);

		m_inputData = info;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param identifier 変更する文字列監視の判定情報の識別キー
	 */
	public StringValueInfoCreateDialog(Shell parent, MonitorStringValueInfo info) {
		super(parent);
		m_inputData = info;
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
	 * @see #setInputData(MonitorStringValueInfo)
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.monitor.run.create.modify.string"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);
		this.setDescription(parent, layout);
		this.setOtherInput(parent, layout, shell);

	}

	/**
	 * 説明部分のセット.
	 *
	 */
	protected void setDescription(Composite parent, GridLayout layout) {

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;
		
		/*
		 * 説明
		 */
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
	}

	/**
	 * 画面項目残部分のセット.
	 *
	 */
	protected void setOtherInput(Composite parent, GridLayout layout, Shell shell) {

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

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
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("pattern.matching.expression") + " : ");
		// テキスト
		this.m_textPattern = new Text(monitorRuleGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "pattern", m_textPattern);
		gridData = new GridData();
		gridData.horizontalSpan = 9;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textPattern.setMessage(Messages.getString("pattern.placeholder.regex"));
		this.m_textPattern.setLayoutData(gridData);
		this.m_textPattern.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
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
				setEnabled(m_radioProcess.getSelection());
				update();
			}
		});

		// 空白
		label = new Label(monitorRuleGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
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
				setEnabled(!m_radioNotProcess.getSelection());
				update();
			}
		});

		// 空白
		label = new Label(monitorRuleGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 処理グループ
		 */
		Group executeGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "execute", executeGroup);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		executeGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		executeGroup.setLayoutData(gridData);
		executeGroup.setText(Messages.getString("process"));

		/*
		 * 重要度
		 */
		// ラベル
		label = new Label(executeGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority") + " : ");
		// コンボボックス
		this.m_comboPriority = new Combo(executeGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "priority", m_comboPriority);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboPriority.setLayoutData(gridData);
		this.m_comboPriority.add(PriorityMessage.STRING_CRITICAL);
		this.m_comboPriority.add(PriorityMessage.STRING_WARNING);
		this.m_comboPriority.add(PriorityMessage.STRING_INFO);
		this.m_comboPriority.add(PriorityMessage.STRING_UNKNOWN);
		this.m_comboPriority.setText(PriorityMessage.STRING_CRITICAL);
		// 空白
		label = new Label(executeGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * メッセージ
		 */
		// ラベル
		label = new Label(executeGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("message") + " : ");
		// テキスト
		this.m_textMessage = new Text(executeGroup, SWT.BORDER);
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
		// デフォルト #[LOG_LINE]
		if (logLineFlag) {
			this.m_textMessage.setText("#[LOG_LINE]");
		}

		/*
		 * 有効／無効
		 */
		this.m_buttonValid = new Button(parent, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "valid", m_buttonValid);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
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

		update();
		setInputData();
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
		if(this.m_radioProcess.getSelection() && "".equals(this.m_textMessage.getText())){
			this.m_textMessage.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textMessage.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}



	/**
	 * 入力値を保持した文字列監視の判定情報を返します。
	 *
	 * @return 判定情報
	 */
	public MonitorStringValueInfo getInputData() {
		return this.m_inputData;
	}

	/**
	 * 引数で指定された判定情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる判定情報
	 */
	protected void setInputData() {

		// 説明
		if (m_inputData.getDescription() != null) {
			this.m_textDescription.setText(m_inputData.getDescription());
		}

		// パターンマッチ表現
		if (m_inputData.getPattern() != null) {
			this.m_textPattern.setText(m_inputData.getPattern());
		}

		// 処理する／しない
		if (m_inputData.isProcessType()) {
			this.m_radioProcess.setSelection(true);
		} else {
			this.m_radioNotProcess.setSelection(true);
		}

		// 大文字・小文字を区別しない
		if (m_inputData.isCaseSensitivityFlg()){
			this.m_checkCaseSensitive.setSelection(true);
		}

		// 重要度
		this.m_comboPriority.setText(PriorityMessage.typeToString(m_inputData.getPriority()));

		// メッセージ
		if (m_inputData.getMessage() != null) {
			this.m_textMessage.setText(m_inputData.getMessage());
		}


		// 有効／無効
		if (m_inputData.isValidFlg()) {
			this.m_buttonValid.setSelection(true);
		}

		// 入力制御
		this.setEnabled(this.m_radioProcess.getSelection());

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
	private MonitorStringValueInfo createInputData() {
		MonitorStringValueInfo info = new MonitorStringValueInfo();


		// 説明
		if (this.m_textDescription.getText() != null
				&& !"".equals((this.m_textDescription.getText()).trim())) {
			info.setDescription(this.m_textDescription.getText());
		}

		// パターンマッチ表現
		if (this.m_textPattern.getText() != null
				&& !"".equals((this.m_textPattern.getText()).trim())) {

			try{
				Pattern.compile(this.m_textPattern.getText());
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

		// 大文字・小文字を区別する/しない
		if (this.m_checkCaseSensitive.getSelection()){
			info.setCaseSensitivityFlg(true);
		}else{
			info.setCaseSensitivityFlg(false);
		}

		// 重要度
		String priorityText = this.m_comboPriority.getText();
		info.setPriority(PriorityMessage.stringToType(priorityText));

		// メッセージ
		if (this.m_textMessage.getText() != null
				&& !"".equals((this.m_textMessage.getText()).trim())) {
			info.setMessage(this.m_textMessage.getText());
		} else {
			if (this.m_radioProcess.getSelection()) {
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.monitor.29"));
				return null;
			}
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
	 * コンポジットの選択可/不可を設定します。
	 *
	 * @param enable 選択可の場合、<code> true </code>
	 */
	protected void setEnabled(boolean enable) {
		this.m_comboPriority.setEnabled(enable);
		this.m_textMessage.setEnabled(enable);
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

		MonitorStringValueInfo info = this.m_inputData;
		if(info != null){
			result = true;
		}

		return result;
	}
}
