/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.dialog;

import java.util.List;

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
import org.openapitools.client.model.TrapValueInfoResponse;
import org.openapitools.client.model.VarBindPatternResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.composite.action.NumberKeyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.run.composite.TableItemInfoComposite;
import com.clustercontrol.snmptrap.composite.VarBindPatternCompositeDefine;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * SNMPTRAP監視 マスタ選択ダイアログクラス<BR>
 *
 * @version 5.0.0
 * @since 2.1.0
 */
public class CreateTrapDefineDialog extends CommonDialog {

	public static final int WIDTH_TITLE = 3;
	public static final int WIDTH_VALUE = 2;
	public static final int MAX_COLUMN = 15;

	public static final String DEFAULT_TARGET_STRING = "parm1=%parm[#1]%&parm2=%parm[#2]%&parm3=%parm[#3]%";

	public static final String NUMERIC_PATTERN = "^[0-9]+$";

	// ----- instance フィールド ----- //

	/** 入力値を保持するオブジェクト */
	private TrapValueInfoResponse inputData = null;

	/** MIB */
	private Text txtMib;

	/** トラップ名 */
	private Text txtName;

	/** バージョン */
	private Button btnV1;
	private Button btnV2c;

	/** OID */
	private Text txtOid;

	/** Generic ID */
	private Text txtGenericId;

	/** Specific ID */
	private Text txtSpecificId;

	/** メッセージ */
	private Text txtMessage;

	/** 詳細メッセージ */
	private Text txtMessageDetail;

	/** 変数に関わらず通知する */
	private Button btnNotifyIgnoreVariable;

	/** 重要度 */
	private Combo cmbPriority;

	/** 変数で判定する */
	private Button btnNotifyUseVariable;

	/** 判定用文字列 */
	private Text txtTargetString;

	/** 文字列テーブルコンポジット */
	private TableItemInfoComposite<VarBindPatternResponse> cmpPatternList = null;

	/** 有効・無効 */
	private Button btnValid;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param monitorType 監視判定タイプ
	 */
	public CreateTrapDefineDialog(Shell parent) {
		this(parent, null);
	}

	public CreateTrapDefineDialog(Shell parent, TrapValueInfoResponse info) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		this.inputData = info;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(750, 750);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.snmptrap.add.definition"));

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// 変数として利用されるラベル
		Label label;

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = MAX_COLUMN;
		parent.setLayout(layout);


		// Mib ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "mib", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("mib"));


		// MIB
		this.txtMib = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, null, txtMib);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - WIDTH_TITLE * 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.txtMib.setLayoutData(gridData);
		this.txtMib.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		// トラップ名 ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "trapname", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("trap.name"));

		// トラップ名
		this.txtName = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "name", txtName);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - WIDTH_TITLE * 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.txtName.setLayoutData(gridData);
		this.txtName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space2", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);



		// バージョン ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "snmptrapversion", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.snmptrap.version"));


		// バージョン(1)
		this.btnV1 = new Button(parent, SWT.RADIO | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.btnV1.setLayoutData(gridData);
		this.btnV1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				update();
			}
		});
		this.btnV1.setText(SnmpVersionConstant.STRING_V1);

		// バージョン(2c/3)
		this.btnV2c = new Button(parent, SWT.RADIO | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.btnV2c.setLayoutData(gridData);
		this.btnV2c.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				update();
			}
		});
		this.btnV2c.setText(SnmpVersionConstant.STRING_V2 + "/" + SnmpVersionConstant.STRING_V3);

		// 空白
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - WIDTH_TITLE * 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		// OID ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "oid", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("oid"));


		// OID
		this.txtOid = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "oid", txtOid);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - WIDTH_TITLE * 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.txtOid.setLayoutData(gridData);
		this.txtOid.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space4", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		// generic id ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "genericid", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("generic.id"));


		// generic id
		this.txtGenericId = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "genericid", txtGenericId);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - WIDTH_TITLE * 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.txtGenericId.setLayoutData(gridData);
		this.txtGenericId.addKeyListener(new NumberKeyListener());
		this.txtGenericId.setTextLimit(5);
		this.txtGenericId.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space5", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		// specific id ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "specificid", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("specific.id"));


		// specific id
		this.txtSpecificId = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "specificid", txtSpecificId);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - WIDTH_TITLE * 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.txtSpecificId.setLayoutData(gridData);
		this.txtSpecificId.addKeyListener(new NumberKeyListener());
		this.txtSpecificId.setTextLimit(10);
		this.txtSpecificId.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space6", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		/*
		 * メッセージ
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "message", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.TOP;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("message"));

		// テキスト
		this.txtMessage = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.MULTI | SWT.WRAP);
		WidgetTestUtil.setTestId(this, "requestpost", txtMessage);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - WIDTH_TITLE * 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 50;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		this.txtMessage.setLayoutData(gridData);
		this.txtMessage.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space7", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		/*
		 * 詳細メッセージ
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "messagedetail", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.TOP;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.snmptrap.message.detail"));

		// テキスト
		this.txtMessageDetail = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.MULTI | SWT.WRAP);
		WidgetTestUtil.setTestId(this, "messagedetail", txtMessageDetail);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - WIDTH_TITLE * 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 50;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		this.txtMessageDetail.setLayoutData(gridData);
		this.txtMessageDetail.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space8", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		/*
		 * 判定グループ
		 */
		// グループ
		Group groupDetermine = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "checkrule", groupDetermine);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = MAX_COLUMN;
		groupDetermine.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		groupDetermine.setLayoutData(gridData);
		groupDetermine.setText(Messages.getString("determine"));

		// 変数に限らず通知する
		this.btnNotifyIgnoreVariable = new Button(groupDetermine, SWT.RADIO | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "notifyignorevariable", btnNotifyIgnoreVariable);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE * 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.btnNotifyIgnoreVariable.setLayoutData(gridData);
		this.btnNotifyIgnoreVariable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				update();
			}
		});
		this.btnNotifyIgnoreVariable.setText(Messages.getString("monitor.snmptrap.notify.regardless.of.the.variable"));
		// コンボ
		this.cmbPriority = new Combo(groupDetermine, SWT.BORDER | SWT.LEFT | SWT.SINGLE | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "priority", cmbPriority);

		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.cmbPriority.setLayoutData(gridData);
		this.cmbPriority.add(PriorityMessage.STRING_CRITICAL);
		this.cmbPriority.add(PriorityMessage.STRING_WARNING);
		this.cmbPriority.add(PriorityMessage.STRING_INFO);
		this.cmbPriority.add(PriorityMessage.STRING_UNKNOWN);
		this.cmbPriority.setText(PriorityMessage.STRING_CRITICAL);
		this.cmbPriority.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupDetermine, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space9", label);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - WIDTH_TITLE * 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 変数に限らず通知する
		this.btnNotifyUseVariable = new Button(groupDetermine, SWT.RADIO | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "notifyusevariable", btnNotifyUseVariable);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE * 2;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.btnNotifyUseVariable.setLayoutData(gridData);
		this.btnNotifyUseVariable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				update();
			}
		});
		this.btnNotifyUseVariable.setText(Messages.getString("monitor.snmptrap.determine.by.specified.variable"));

		// 空白
		label = new Label(groupDetermine, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space10", label);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - WIDTH_TITLE * 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 判定対象文字列 ラベル
		label = new Label(groupDetermine, SWT.NONE);
		WidgetTestUtil.setTestId(this, "tagetstring", label);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalIndent = 20;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.snmptrap.target.string"));

		// 判定対象文字列
		this.txtTargetString = new Text(groupDetermine, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "targetstring", txtTargetString);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.txtTargetString.setLayoutData(gridData);
		this.txtTargetString.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//label
		label = new Label(groupDetermine, SWT.NONE);
		WidgetTestUtil.setTestId(this, "determinerule", label);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 20;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.snmptrap.determine.rule"));

		/*
		 * パターン文字列テーブル
		 */
		this.cmpPatternList = new TableItemInfoComposite<>(groupDetermine, SWT.NONE , new VarBindPatternCompositeDefine());
		WidgetTestUtil.setTestId(this, "patternlist", cmpPatternList);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.cmpPatternList.setLayoutData(gridData);

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 15;
		line.setLayoutData(gridData);

		// バージョン
		this.btnValid = new Button(parent, SWT.CHECK | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "valid", btnValid);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.btnValid.setLayoutData(gridData);
		this.btnValid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				update();
			}
		});
		this.btnValid.setText(Messages.getString("setting.valid.confirmed"));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		// 初期表示
		this.setInputData();

	}

	protected void update(){
		if("".equals(txtMib.getText().trim())){
			txtMib.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			txtMib.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(txtName.getText().trim())){
			txtName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			txtName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(txtOid.getText().trim())){
			txtOid.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			txtOid.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		txtGenericId.setEnabled(btnV1.getSelection());
		txtSpecificId.setEnabled(btnV1.getSelection());

		if(txtGenericId.getEnabled() && !java.util.regex.Pattern.matches(NUMERIC_PATTERN, txtGenericId.getText().trim())){
			txtGenericId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			txtGenericId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(txtSpecificId.getEnabled() && !java.util.regex.Pattern.matches(NUMERIC_PATTERN, txtSpecificId.getText().trim())){
			txtSpecificId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			txtSpecificId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(txtMessage.getText().trim())){
			txtMessage.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			txtMessage.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		cmbPriority.setEnabled(btnNotifyIgnoreVariable.getSelection());

		txtTargetString.setEnabled(btnNotifyUseVariable.getSelection());

		if(txtTargetString.getEnabled() && "".equals(txtTargetString.getText().trim())){
			txtTargetString.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			txtTargetString.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		cmpPatternList.setEnabled(btnNotifyUseVariable.getSelection());
	}

	/**
	 * 入力内容を返します。
	 *
	 * @return 入力内容を保持した通知情報
	 */
	public TrapValueInfoResponse getInputData() {
		return this.inputData;
	}

	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor
	 *            設定値として用いる監視情報
	 */
	protected void setInputData() {

		if(inputData == null){
			btnV1.setSelection(true);
			btnNotifyIgnoreVariable.setSelection(true);
			txtTargetString.setText(DEFAULT_TARGET_STRING);
			btnValid.setSelection(true);
		} else {
			if(inputData.getMib() != null){
				txtMib.setText(inputData.getMib());
			}
			if(inputData.getUei() != null){
				txtName.setText(inputData.getUei());
			}
			if(inputData.getVersion() == TrapValueInfoResponse.VersionEnum.V1){
				btnV1.setSelection(true);
				if(inputData.getGenericId() != null){
					txtGenericId.setText(inputData.getGenericId().toString());
				}
				if(inputData.getSpecificId() != null){
					txtSpecificId.setText(inputData.getSpecificId().toString());
				}
			} else {
				btnV2c.setSelection(true);
			}
			if(inputData.getTrapOid() != null){
				txtOid.setText(inputData.getTrapOid());
			}
			if(inputData.getLogmsg() != null){
				txtMessage.setText(inputData.getLogmsg());
			}
			if(inputData.getDescription() != null){
				txtMessageDetail.setText(inputData.getDescription());
			}
			if (inputData.getPriorityAnyVarBind() != null) {
				cmbPriority.select(cmbPriority.indexOf(PriorityMessage.codeToString(inputData.getPriorityAnyVarBind().toString())));
			}
			if(!inputData.getProcVarbindSpecified()){
				btnNotifyIgnoreVariable.setSelection(true);
				txtTargetString.setText(DEFAULT_TARGET_STRING);
			} else {
				btnNotifyUseVariable.setSelection(true);
				if(inputData.getFormatVarBinds() != null){
					txtTargetString.setText(inputData.getFormatVarBinds());
				}
				cmpPatternList.setInputData(inputData.getVarBindPatterns());
			}
			btnValid.setSelection(inputData.getValidFlg());
		}

		cmpPatternList.update();
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		if ("".equals((txtMib.getText()).trim())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("mib")}));
		}
		if ("".equals((txtName.getText()).trim())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("trap.name")}));
		}
		if ("".equals((txtOid.getText()).trim())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("oid")}));
		}

		if(btnV1.getSelection()){
			if (!java.util.regex.Pattern.matches(NUMERIC_PATTERN, txtGenericId.getText().trim())) {
				return createValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required.numeric", new Object[]{Messages.getString("generic.id")}));
			}
			if (!java.util.regex.Pattern.matches(NUMERIC_PATTERN, txtSpecificId.getText().trim())) {
				return createValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required.numeric", new Object[]{Messages.getString("specific.id")}));
			}
			if (Integer.parseInt(txtGenericId.getText()) > DataRangeConstant.SMALLINT_HIGH
					|| Integer.parseInt(txtGenericId.getText()) < DataRangeConstant.GENERIC_ID_LOW) {
				String[] args = {Messages.getString("generic.id"),
						String.valueOf(DataRangeConstant.GENERIC_ID_LOW), String.valueOf(DataRangeConstant.SMALLINT_HIGH)};
				return createValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.calendar.52",args));
			}
			if (Long.parseLong(txtSpecificId.getText()) > DataRangeConstant.INTEGER_HIGH
					|| Long.parseLong(txtSpecificId.getText()) < DataRangeConstant.SPECIFIC_ID_LOW) {
				String[] args = {Messages.getString("specific.id"),
						String.valueOf(DataRangeConstant.SPECIFIC_ID_LOW), String.valueOf(DataRangeConstant.INTEGER_HIGH)};
				return createValidateResult(
						Messages.getString("message.hinemos.1"), 
						Messages.getString("message.calendar.52",args));
			}
		}

		if ("".equals(txtMessage.getText().trim())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("message")}));
		}

		if(btnNotifyUseVariable.getSelection()){
			if ("".equals((txtTargetString.getText()).trim())) {
				return createValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("monitor.snmptrap.target.string")}));
			}
			if(cmpPatternList.getItems().isEmpty()){
				return createValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("monitor.snmptrap.determine.rule")}));
			}
		}

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
		inputData = createInputData();

		return true;
	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	protected TrapValueInfoResponse createInputData() {
		TrapValueInfoResponse info = new TrapValueInfoResponse();

		info.setMib(txtMib.getText());
		info.setUei(txtName.getText());
		info.setTrapOid(txtOid.getText());
		if(btnV1.getSelection()){
			info.setVersion(TrapValueInfoResponse.VersionEnum.V1);
			info.setGenericId(Integer.valueOf(txtGenericId.getText()));
			info.setSpecificId(Integer.valueOf(txtSpecificId.getText()));
		} else {
			info.setVersion(TrapValueInfoResponse.VersionEnum.V2C_V3);
		}
		info.setLogmsg(txtMessage.getText());
		info.setDescription(txtMessageDetail.getText());

		info.setPriorityAnyVarBind(PriorityMessage.stringToEnum(
				cmbPriority.getText(), TrapValueInfoResponse.PriorityAnyVarBindEnum.class));
		if(btnNotifyIgnoreVariable.getSelection()){
			info.setProcVarbindSpecified(false);
		} else {
			info.setProcVarbindSpecified(true);
			info.setFormatVarBinds(txtTargetString.getText());
			List<VarBindPatternResponse> patterns = info.getVarBindPatterns();
			patterns.clear();
			patterns.addAll(cmpPatternList.getItems());
		}

		info.setValidFlg(btnValid.getSelection());

		return info;
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("add");
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
	 * 無効な入力値の情報を設定します
	 *
	 */
	protected ValidateResult createValidateResult(String id, String message) {

		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}
}
