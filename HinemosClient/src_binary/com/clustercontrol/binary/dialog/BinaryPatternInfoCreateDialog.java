/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.dialog;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyListener;
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
import org.openapitools.client.model.BinaryPatternInfoResponse;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.binary.bean.BinarySearchBean;
import com.clustercontrol.binary.util.BinaryBeanUtil;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.CommonVerifyListener;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * バイナリ検索条件ダイアログ<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class BinaryPatternInfoCreateDialog extends CommonDialog {

	// static フィールド
	/** パターンマッチ表現入力チェックキー */
	private static String PATTERN_VERIFY_LISTNER = "pattern.verify.listner";
	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = 4;
	/** カラム数（値）。 */
	public static final int WIDTH_VALUE = 2;

	// 画面項目.
	/** 説明 */
	private Text m_textDescription = null;
	/** 検索文字列 */
	private Text m_textPattern = null;
	/** 条件に一致したら処理しない */
	private Button m_radioNotProcess = null;
	/** 条件に一致したら処理する */
	private Button m_radioProcess = null;
	/** エンコード */
	private Text m_encoding = null;
	/** 重要度 */
	private Combo m_comboPriority = null;
	/** メッセージ */
	private Text m_textMessage = null;
	/** この設定を有効にする */
	private Button m_buttonValid = null;

	// そのほかフィールド
	/** バイナリ検索条件の入力値保持オブジェクト */
	private BinaryPatternInfoResponse m_binaryInputData = null;
	/** 入力値チェック結果 */
	private ValidateResult m_binaryValidateResult = null;
	/** メッセージにデフォルト値を入れるフラグ */
	private boolean logLineFlag = false;

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public BinaryPatternInfoCreateDialog(Shell parent, boolean logLineFlag) {
		super(parent);
		this.logLineFlag = logLineFlag;

		// バイナリ検索条件の初期化.
		BinaryPatternInfoResponse info = new BinaryPatternInfoResponse();
		info.setProcessType(true);
		info.setValidFlg(true);
		info.setPriority(BinaryPatternInfoResponse.PriorityEnum.CRITICAL);
		info.setEncoding("UTF-8");

		m_binaryInputData = info;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param identifier
	 *            変更する文字列監視の判定情報の識別キー
	 */
	public BinaryPatternInfoCreateDialog(Shell parent, BinaryPatternInfoResponse binaryInfo) {
		super(parent);
		this.logLineFlag = false;
		m_binaryInputData = binaryInfo;
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

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);

		// タイトル
		shell.setText(Messages.getString("dialog.monitor.run.create.modify.binary"));

		// 画面項目の設定.
		this.setInputFields(parent, layout, shell);

		// 各項目に前回入力値等を反映.
		this.setInputBinaryData();
	}

	/**
	 * 画面項目の設定.
	 *
	 */
	private void setInputFields(Composite parent, GridLayout layout, Shell shell) {
		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// --説明
		// ラベル
		label = new Label(parent, SWT.NONE);
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
		this.m_textDescription.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ----監視条件グループ
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

		// --検索文字列
		// ラベル
		label = new Label(monitorRuleGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("binary.search.text") + " : ");
		// テキスト
		this.m_textPattern = new Text(monitorRuleGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "pattern", m_textPattern);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textPattern.setMessage(Messages.getString("pattern.placeholder.binary"));
		this.m_textPattern.setLayoutData(gridData);
		this.m_textPattern.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				checkHexOrText();
			}
		});
		this.m_textPattern.setToolTipText(Messages.getString("tooltip.input.search.hex"));

		// --条件に一致したら処理する

		// ラジオボタン
		this.m_radioProcess = new Button(monitorRuleGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "process", m_radioProcess);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioProcess.setLayoutData(gridData);
		this.m_radioProcess.setText(Messages.getString("process.if.matched"));

		// 変更時のイベント(処理しないと共通)
		SelectionAdapter processSelection = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabledByProcess();
				updateByProcess();
			}
		};
		this.m_radioProcess.addSelectionListener(processSelection);

		// 空白
		label = new Label(monitorRuleGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// --条件に一致したら処理しない
		this.m_radioNotProcess = new Button(monitorRuleGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "notprocess", m_radioNotProcess);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioNotProcess.setLayoutData(gridData);
		this.m_radioNotProcess.setText(Messages.getString("don't.process.if.matched"));
		this.m_radioNotProcess.addSelectionListener(processSelection);

		// 空白
		label = new Label(monitorRuleGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// --エンコーディング
		// ラベル
		label = new Label(monitorRuleGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("job.script.encoding") + " : ");
		// テキスト
		this.m_encoding = new Text(monitorRuleGroup, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_encoding.setLayoutData(gridData);
		this.m_encoding.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				BinaryConstant.SearchType searchType = BinaryBeanUtil.getSearchType(m_textPattern.getText());
				updateBySearchType(searchType);
			}
		});
		this.m_encoding.setToolTipText(Messages.getString("tooltip.input.java.charset"));

		// ----処理グループ
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

		// --重要度
		// ラベル
		label = new Label(executeGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority") + " : ");
		// コンボボックス
		this.m_comboPriority = new Combo(executeGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "priority", m_comboPriority);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE;
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
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// --メッセージ
		// ラベル
		label = new Label(executeGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
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
		this.m_textMessage.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				updateByProcess();
			}
		});
		// デフォルト #[BINARY_LINE]
		if (logLineFlag) {
			this.m_textMessage.setText(BinaryConstant.BINARY_LINE);
		}

		// --有効/無効
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

	}

	/**
	 * 16進数検索/文字列検索判定の上処理.
	 */
	private void checkHexOrText() {
		BinaryConstant.SearchType searchType = BinaryBeanUtil.getSearchType(this.m_textPattern.getText());
		this.setPatternVeryfy(searchType);
		this.setEnabledBySearchType(searchType);
		this.updateBySearchType(searchType);
	}

	/**
	 * パターンマッチ表現の入力チェックセット.
	 */
	private void setPatternVeryfy(BinaryConstant.SearchType searchType) {

		// 前に設定した入力チェック取得.
		Object veryfyListener = this.m_textPattern.getData(PATTERN_VERIFY_LISTNER);
		if (veryfyListener != null && veryfyListener instanceof VerifyListener) {
			// 前に設定した入力チェック除去.
			this.m_textPattern.removeVerifyListener((VerifyListener) veryfyListener);
			this.m_textPattern.setData(PATTERN_VERIFY_LISTNER, null);
		}

		if (searchType == BinaryConstant.SearchType.HEX) {
			// 16進数検索の場合は0xはじまりの数値+A～Fのみ入力可能なテキストフィールド.
			this.m_textPattern.addVerifyListener(CommonVerifyListener.onlyHexStrListener);
			this.m_textPattern.setData(PATTERN_VERIFY_LISTNER, CommonVerifyListener.onlyHexStrListener);
		}

	}

	/**
	 * 項目の活性・非活性制御を設定.
	 */
	protected void setEnabled() {
		this.setEnabledByProcess();
		BinaryConstant.SearchType searchType = BinaryBeanUtil.getSearchType(this.m_textPattern.getText());
		this.setEnabledBySearchType(searchType);

	}

	/**
	 * 条件に一致したら処理する/しないによる活性制御.
	 */
	protected void setEnabledByProcess() {
		// 条件に一致したら処理する/しないラジオボタンによる活性制御.
		if (this.m_radioProcess.getSelection()) {
			this.m_comboPriority.setEnabled(true);
			this.m_textMessage.setEnabled(true);
		} else {
			this.m_comboPriority.setEnabled(false);
			this.m_textMessage.setEnabled(false);
		}
	}

	/**
	 * 検索タイプによる活性制御.
	 */
	protected void setEnabledBySearchType(BinaryConstant.SearchType searchType) {
		// 文字列検索(空文字含む)の場合の活性制御.
		if (searchType == BinaryConstant.SearchType.STRING || searchType == BinaryConstant.SearchType.EMPTY) {
			this.m_encoding.setEnabled(true);
			Object data = this.m_encoding.getData();
			if (data != null && data instanceof String) {
				this.m_encoding.setText((String) data);
				this.m_encoding.setData(null);
			}
		} else {
			if (!this.m_encoding.getText().isEmpty()) {
				this.m_encoding.setData(this.m_encoding.getText());
			}
			this.m_encoding.setText("");
			this.m_encoding.setEnabled(false);
		}
	}

	/**
	 * 更新処理
	 */
	public void update() {
		this.updateByProcess();
		BinaryConstant.SearchType searchType = BinaryBeanUtil.getSearchType(this.m_textPattern.getText());
		this.updateBySearchType(searchType);
	}

	/**
	 * ラジオボタンによる更新処理
	 */
	public void updateByProcess() {
		// 必須項目を可視化
		if ("".equals(this.m_textPattern.getText())) {
			this.m_textPattern.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_textPattern.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_radioProcess.getSelection() && "".equals(this.m_textMessage.getText())) {
			this.m_textMessage.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_textMessage.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 検索タイプによる更新処理
	 */
	public void updateBySearchType(BinaryConstant.SearchType searchType) {
		// 必須項目を可視化
		if ("".equals(this.m_textPattern.getText())) {
			this.m_textPattern.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_textPattern.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_radioProcess.getSelection() && "".equals(this.m_textMessage.getText())) {
			this.m_textMessage.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_textMessage.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 文字列検索の場合必須.
		if (searchType == BinaryConstant.SearchType.STRING) {
			if ("".equals(this.m_encoding.getText())) {
				this.m_encoding.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_encoding.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		} else {
			this.m_encoding.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 引数で指定されたバイナリ検索条件を画面項目に反映
	 *
	 */
	protected void setInputBinaryData() {
		// 入力値の反映.
		if (m_binaryInputData != null) {

			// 説明
			if (m_binaryInputData.getDescription() != null) {
				this.m_textDescription.setText(m_binaryInputData.getDescription());
			}

			// 検索文字列.
			if (m_binaryInputData.getGrepString() != null) {
				this.m_textPattern.setText(m_binaryInputData.getGrepString());
			}

			// 処理する／しない
			if (m_binaryInputData.getProcessType()) {
				this.m_radioProcess.setSelection(true);
			} else {
				this.m_radioNotProcess.setSelection(true);
			}

			// エンコード.
			if (m_binaryInputData.getEncoding() != null) {
				this.m_encoding.setText(m_binaryInputData.getEncoding());
			}

			// 重要度
			this.m_comboPriority.setText(PriorityMessage.codeToString(m_binaryInputData.getPriority().toString()));

			// メッセージ
			if (m_binaryInputData.getMessage() != null) {
				this.m_textMessage.setText(m_binaryInputData.getMessage());
			}

			// 有効／無効
			if (m_binaryInputData.getValidFlg()) {
				this.m_buttonValid.setSelection(true);
			}
		}

		// 入力値を反映したので入力値による画面制御を実施.
		this.updateAll();
	}

	/**
	 * 入力値が変更された場合に画面項目を一通り更新.
	 */
	private void updateAll() {
		this.updateByProcess();
		this.setEnabledByProcess();
		this.checkHexOrText();
	}

	/**
	 * 入力値を保持したバイナリ検索条件を取得
	 *
	 * @return 判定情報
	 */
	public BinaryPatternInfoResponse getBinaryInputData() {
		return this.m_binaryInputData;
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

		// 検索文字列から"0x"を除外する処理のため先に親クラスのバリデータを走らせる.
		ValidateResult superResult = super.validate();
		this.m_binaryInputData = this.createInputData();

		if (this.m_binaryInputData != null) {
			return superResult;
		} else {
			return this.m_binaryValidateResult;
		}
	}

	/**
	 * 引数で指定された判定情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。<br>
	 * ※ダイアログ閉じるタイミングでマネージャー通信を行わないため、クライアントでチェック実施する.
	 *
	 * @return 判定情報
	 *
	 * @see #setValidateResult(String, String)
	 */
	private BinaryPatternInfoResponse createInputData() {

		// 変数初期化.
		BinaryPatternInfoResponse info = new BinaryPatternInfoResponse();
		String[] args = null;

		// 説明
		if (this.m_textDescription.getText() != null && !"".equals((this.m_textDescription.getText()).trim())) {
			info.setDescription(this.m_textDescription.getText());
		}

		// 検索文字列.
		BinarySearchBean searchBean = BinaryBeanUtil.getSearchBean(this.m_textPattern.getText());
		switch (searchBean.getSearchType()) {

		case HEX:
		case STRING:
			// 入力値をそのまま検索文字列として設定する.
			info.setGrepString(this.m_textPattern.getText());
			break;

		case EMPTY:
			// 入力されていない場合.
			args = new String[] { Messages.getString("binary.search.text") };
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.common.1", args));
			return null;

		case ERROR:
			if (searchBean.getSearchError() == BinaryConstant.SearchError.ONLY_OX) {
				// "0x"のみが入力されている場合.
				args = new String[] { Messages.getString("binary.pattern.hex") };
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.common.1", args));
				return null;
			}
			if (searchBean.getSearchError() == BinaryConstant.SearchError.INVALID_HEX) {
				// 16進数の形式にマッチしない場合.
				this.setValidateResult(Messages.getString("message.hinemos.1"), Messages.getString("message.binary.7"));
				return null;
			}
		default:
			// 想定外のエラー.
			args = new String[] { Messages.getString("binary.search.text") };
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.common.14", args));
			return null;

		}

		// 処理する／しない
		if (this.m_radioProcess.getSelection()) {
			info.setProcessType(true);
		} else {
			info.setProcessType(false);
		}

		// エンコード.
		if (this.m_encoding.getText() != null && !"".equals(this.m_encoding.getText())) {
			// 妥当なエンコーディング方式かチェック.
			try {
				Charset.forName(this.m_encoding.getText());
			} catch (IllegalCharsetNameException exception) {
				// 不正なエンコーディング方式の場合.
				args = new String[] { this.m_encoding.getText() };
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.binary.2", args));
				return null;
			} catch (UnsupportedCharsetException exception) {
				// サポートされていないエンコーディング方式の場合.
				args = new String[] { this.m_encoding.getText() };
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.binary.3", args));
				return null;
			}
			// OKなのでエンティティにセット.
			info.setEncoding(this.m_encoding.getText());
		} else {
			if (searchBean.getSearchType() == BinaryConstant.SearchType.STRING) {
				// 文字列検索パターンなのにエンコードが入力されていない場合.
				args = new String[] { Messages.getString("job.script.encoding") };
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.common.1", args));
				return null;
			}
		}

		// 重要度
		info.setPriority(PriorityMessage.stringToEnum(this.m_comboPriority.getText(), BinaryPatternInfoResponse.PriorityEnum.class));

		// メッセージ
		if (this.m_textMessage.getText() != null && !"".equals((this.m_textMessage.getText()).trim())) {
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
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id
	 *            ID
	 * @param message
	 *            メッセージ
	 */
	protected void setValidateResult(String id, String message) {

		this.m_binaryValidateResult = new ValidateResult();
		this.m_binaryValidateResult.setValid(false);
		this.m_binaryValidateResult.setID(id);
		this.m_binaryValidateResult.setMessage(message);
	}

}
