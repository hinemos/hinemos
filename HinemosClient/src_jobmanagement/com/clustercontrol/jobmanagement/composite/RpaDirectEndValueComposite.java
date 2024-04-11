/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobRpaInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.dialog.RpaDirectEndValueConditionDialog;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobTypeConstant;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;

/**
 * RPAシナリオ 直接実行 終了値タブ用のコンポジットクラスです
 */
public class RpaDirectEndValueComposite extends Composite {
	/** ログ */
	private static Log m_log = LogFactory.getLog(RpaDirectEndValueComposite.class);
	/** ディレクトリ */
	private Text m_directory = null;
	/** ファイル名 */
	private Text m_fileName = null;
	/** エンコード */
	private Text m_fileEncoding = null;
	/** 改行コード */
	private Combo m_fileReturnCode = null;
	/** 先頭パターン */
	private Text m_txtPatternHead = null;
	/** 終端パターン */
	private Text m_txtPatternTail = null;
	/** 最大読み取りバイト長 */
	private Text m_txtMaxBytes = null;
	/** 終了値判定条件テーブルビューア */
	private RpaDirectEndValueConditionComposite m_endValueConditionComposite = null;
	/** いずれの判定にも一致しない場合の終了値 */
	private Text m_defaultEndValueText = null;
	/** 判定条件追加ボタン */
	private Button m_conditionAddButton = null;
	/** 判定条件変更ボタン */
	private Button m_conditionModifyButton = null;
	/** 判定条件削除ボタン */
	private Button m_conditionDeleteButton = null;
	/** 判定条件コピーボタン */
	private Button m_conditionCopyButton = null;
	/** 判定条件上へボタン */
	private Button m_conditionUpButton = null;
	/** 判定条件下へボタン */
	private Button m_conditionDownButton = null;
	/** RPAシナリオジョブ実行情報 */
	private JobRpaInfoResponse m_rpa = null;
	/** シェル */
	private Shell m_shell = null;
	/** 読み取り専用モードのフラグ */
	private boolean m_enabled = false;
	/** RPAシナリオジョブ種別 */
	private Integer m_rpaJobType = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 */
	public RpaDirectEndValueComposite(Composite parent, int style) {
		super(parent, style);
		this.m_shell = this.getShell();
		initialize();
	}

	private void initialize() {
		this.setLayout(JobDialogUtil.getParentLayout());

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		Composite gridComposite = new Composite(this, SWT.NONE);
		gridComposite.setLayout(new GridLayout(1, true));

		// ファイル情報および区切り条件をタブにまとめる
		TabFolder tabFolder = new TabFolder(gridComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		tabFolder.setLayoutData(gridData);
		TabItem tabCheckRule = new TabItem(tabFolder, SWT.NONE);
		tabCheckRule.setText(Messages.getString("file.info"));
		TabItem tabDelimiter = new TabItem(tabFolder, SWT.NONE);
		tabDelimiter.setText(Messages.getString("file.delimiter"));

		/*
		 * チェック設定グループ（条件グループの子グループ）
		 */
		Composite compositeCheckRule = new Composite(tabFolder, SWT.NONE);
		GridLayout layout = new GridLayout(2, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		compositeCheckRule.setLayout(layout);

		// ディレクトリ
		// ラベル
		label = new Label(compositeCheckRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("directory") + " : ");
		// テキスト
		this.m_directory = new Text(compositeCheckRule, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		String tooltipText = Messages.getString("monitor.logfile.directory.tool.tip")
				+ Messages.getString("replace.parameter.node");
		this.m_directory.setToolTipText(tooltipText);
		this.m_directory.setLayoutData(gridData);
		this.m_directory.addModifyListener(e -> update());
		// ファイル名
		// ラベル
		label = new Label(compositeCheckRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("file.name") + "(" + Messages.getString("regex") + ") : ");
		// テキスト
		this.m_fileName = new Text(compositeCheckRule, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_fileName.setLayoutData(gridData);
		this.m_fileName.addModifyListener(e -> update());
		this.m_fileName.setToolTipText(Messages.getString("dialog.logfile.pattern"));

		// ファイルエンコーディング
		// ラベル
		label = new Label(compositeCheckRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("file.encoding") + " : ");
		// テキスト
		this.m_fileEncoding = new Text(compositeCheckRule, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_fileEncoding.setLayoutData(gridData);
		this.m_fileEncoding.addModifyListener(e -> update());

		tabCheckRule.setControl(compositeCheckRule);

		// 区切り条件
		Composite delimiter = new Composite(tabFolder, SWT.NONE);
		layout = new GridLayout(2, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		delimiter.setLayout(layout);

		// 先頭パターン（正規表現）
		// ラベル
		Label lblPrePattern = new Label(delimiter, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblPrePattern.setLayoutData(gridData);
		lblPrePattern.setText(Messages.getString("file.delimiter.pattern.head") + " : ");
		// テキスト
		m_txtPatternHead = new Text(delimiter, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_txtPatternHead.setLayoutData(gridData);
		m_txtPatternHead.addModifyListener(e -> update());

		// 終端パターン（正規表現）
		// ラベル
		Label lblSufPattern = new Label(delimiter, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblSufPattern.setLayoutData(gridData);
		lblSufPattern.setText(Messages.getString("file.delimiter.pattern.tail") + " : ");
		// テキスト
		m_txtPatternTail = new Text(delimiter, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_txtPatternTail.setLayoutData(gridData);
		m_txtPatternTail.addModifyListener(e -> update());

		VerifyListener verifier = new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				String text = e.text;
				if (e.character == SWT.BS || e.character == SWT.DEL) {
					return;
				}
				if (e.text.equals("")) {
					return;
				}
				if (!text.matches("^[0-9]+$")) {
					e.doit = false;
				}
			}
		};

		// ファイル改行コード
		// ラベル
		label = new Label(delimiter, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("file.returncode") + " : ");
		// コンボボックス
		this.m_fileReturnCode = new Combo(delimiter, SWT.DROP_DOWN | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_fileReturnCode.setLayoutData(gridData);

		m_fileReturnCode.add("LF");
		m_fileReturnCode.add("CR");
		m_fileReturnCode.add("CRLF");

		tabDelimiter.setControl(delimiter);

		// 最大読み取りバイト長（Byte)
		// ラベル
		Label lblReadByte = new Label(delimiter, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblReadByte.setLayoutData(gridData);
		lblReadByte.setText(Messages.getString("file.delimiter.chars") + " : ");
		// テキスト
		m_txtMaxBytes = new Text(delimiter, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_txtMaxBytes.setLayoutData(gridData);
		m_txtMaxBytes.addVerifyListener(verifier);
		m_txtMaxBytes.addModifyListener(e -> update());

		tabFolder.setSelection(new TabItem[] { tabCheckRule });

		// dummy
		label = new Label(delimiter, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// レイアウトのためのComposite
		Composite endValueConditionComposite = new Composite(this, SWT.NONE);
		endValueConditionComposite.setLayout(new GridLayout(2, false));

		// 終了値判定条件（テーブル）
		Composite endValueConditionTableComposite = new Composite(endValueConditionComposite, SWT.NONE);
		endValueConditionTableComposite.setLayout(new GridLayout(1, false));
		// テーブルとボタンの上部を揃える
		endValueConditionTableComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		this.m_endValueConditionComposite = new RpaDirectEndValueConditionComposite(endValueConditionTableComposite,
				SWT.NONE);

		// いずれの判定にも一致しない場合の終了値（ラベル）
		Composite defaultEndValueComposite = new Composite(endValueConditionTableComposite, SWT.NONE);
		defaultEndValueComposite.setLayout(new RowLayout());
		Label defaultEndValueLabel = new Label(defaultEndValueComposite, SWT.NONE);
		defaultEndValueLabel.setText(Messages.getString("rpa.default.end.value") + " : ");
		defaultEndValueLabel.setLayoutData(new RowData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// いずれの判定にも一致しない場合の終了値（テキスト）
		m_defaultEndValueText = new Text(defaultEndValueComposite, SWT.BORDER);
		m_defaultEndValueText.setLayoutData(new RowData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		m_defaultEndValueText.addModifyListener(e -> update());
		this.m_defaultEndValueText.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));

		// ボタンレイアウトのためのComposite
		Composite buttonComposite = new Composite(endValueConditionComposite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1, false));
		// 追加ボタン
		m_conditionAddButton = new Button(buttonComposite, SWT.NONE);
		m_conditionAddButton.setText(Messages.getString("add"));
		m_conditionAddButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		m_conditionAddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RpaDirectEndValueConditionDialog dialog = new RpaDirectEndValueConditionDialog(m_shell);
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_endValueConditionComposite.getInputData().add(dialog.getInputData());
					m_endValueConditionComposite.update();
					m_endValueConditionComposite.setSelection(m_endValueConditionComposite.getInputData().size() - 1); // 末尾の行を選択
					update();
				}
			}
		});

		// 変更ボタン
		m_conditionModifyButton = new Button(buttonComposite, SWT.NONE);
		m_conditionModifyButton.setText(Messages.getString("modify"));
		m_conditionModifyButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		this.m_conditionModifyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = (Integer) ((ArrayList<?>) m_endValueConditionComposite.getTableViewer().getTable()
						.getSelection()[0].getData()).get(0) - 1;
				if (index >= 0 && index <= m_endValueConditionComposite.getInputData().size()) {
					RpaDirectEndValueConditionDialog dialog = new RpaDirectEndValueConditionDialog(m_shell,
							m_endValueConditionComposite.getInputData().get(index));
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_endValueConditionComposite.getInputData().remove(index);
						m_endValueConditionComposite.getInputData().add(index, dialog.getInputData());
						m_endValueConditionComposite.update();
						m_endValueConditionComposite.setSelection(index);
						update();
					}
				} else {
					m_log.warn("widgetSelected() : modify index is invalid, index=" + index);
				}
			}
		});

		// 削除ボタン
		m_conditionDeleteButton = new Button(buttonComposite, SWT.NONE);
		m_conditionDeleteButton.setText(Messages.getString("delete"));
		m_conditionDeleteButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		m_conditionDeleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = (Integer) ((ArrayList<?>) m_endValueConditionComposite.getTableViewer().getTable()
						.getSelection()[0].getData()).get(0) - 1;
				if (index >= 0 && index <= m_endValueConditionComposite.getInputData().size()) {
					m_endValueConditionComposite.getInputData().remove(index);
					m_endValueConditionComposite.update();
					update();
				} else {
					m_log.warn("widgetSelected() : delete index is invalid, index=" + index);
				}
			}
		});

		// コピーボタン
		m_conditionCopyButton = new Button(buttonComposite, SWT.NONE);
		m_conditionCopyButton.setText(Messages.getString("copy"));
		m_conditionCopyButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		this.m_conditionCopyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = (Integer) ((ArrayList<?>) m_endValueConditionComposite.getTableViewer().getTable()
						.getSelection()[0].getData()).get(0) - 1;
				if (index >= 0 && index <= m_endValueConditionComposite.getInputData().size()) {
					RpaDirectEndValueConditionDialog dialog = new RpaDirectEndValueConditionDialog(m_shell,
							m_endValueConditionComposite.getInputData().get(index));
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_endValueConditionComposite.getInputData().add(dialog.getInputData());
						m_endValueConditionComposite.update();
						m_endValueConditionComposite.setSelection(index);
						update();
					}
				} else {
					m_log.warn("widgetSelected() : copy index is invalid, index=" + index);
				}
			}
		});

		// 上へボタン
		m_conditionUpButton = new Button(buttonComposite, SWT.NONE);
		m_conditionUpButton.setText(Messages.getString("up"));
		m_conditionUpButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		m_conditionUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = (Integer) ((ArrayList<?>) m_endValueConditionComposite.getTableViewer().getTable()
						.getSelection()[0].getData()).get(0) - 1;
				if (index > 0) {
					Collections.swap(m_endValueConditionComposite.getInputData(), index, index - 1);
					m_endValueConditionComposite.update();
					m_endValueConditionComposite.setSelection(index - 1);
				}
			}
		});

		// 下へボタン
		m_conditionDownButton = new Button(buttonComposite, SWT.NONE);
		m_conditionDownButton.setText(Messages.getString("down"));
		m_conditionDownButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		m_conditionDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = (Integer) ((ArrayList<?>) m_endValueConditionComposite.getTableViewer().getTable()
						.getSelection()[0].getData()).get(0) - 1;
				if (index < m_endValueConditionComposite.getInputData().size() - 1) {
					Collections.swap(m_endValueConditionComposite.getInputData(), index, index + 1);
					m_endValueConditionComposite.update();
					m_endValueConditionComposite.setSelection(index + 1);
				}
			}
		});
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update() {
		// 必須項目を明示
		Text[] texts = { m_directory, m_fileName, m_fileEncoding };
		for (Text text : texts) {
			// ログファイルによる判定条件が含まれている場合のみ必須
			if (m_enabled && "".equals(text.getText()) && m_endValueConditionComposite.isLogConditionExisting()) {
				text.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				text.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}

		if (m_enabled && "".equals(m_defaultEndValueText.getText())) {
			m_defaultEndValueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_defaultEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (m_enabled) {
			if (!m_txtPatternHead.getText().isEmpty()) {
				m_txtPatternHead.setEnabled(true);
				m_txtPatternTail.setEnabled(false);
				m_fileReturnCode.setEnabled(false);
			} else if (!m_txtPatternTail.getText().isEmpty()) {
				m_txtPatternHead.setEnabled(false);
				m_txtPatternTail.setEnabled(true);
				m_fileReturnCode.setEnabled(false);
			} else {
				m_txtPatternHead.setEnabled(true);
				m_txtPatternTail.setEnabled(true);
				m_fileReturnCode.setEnabled(true);
			}
		}
	}

	public void reflectRpaJobInfo() {
		if (m_rpa != null) {
			if (m_rpa.getRpaLogDirectory() != null) {
				m_directory.setText(m_rpa.getRpaLogDirectory());
			}
			if (m_rpa.getRpaLogFileName() != null) {
				m_fileName.setText(m_rpa.getRpaLogFileName());
			}
			if (m_rpa.getRpaLogEncoding() != null) {
				m_fileEncoding.setText(m_rpa.getRpaLogEncoding());
			}
			if (m_rpa.getRpaLogReturnCode() != null) {
				m_fileReturnCode.setText(m_rpa.getRpaLogReturnCode());
			}
			if (m_rpa.getRpaLogPatternHead() != null) {
				m_txtPatternHead.setText(m_rpa.getRpaLogPatternHead());
			}
			if (m_rpa.getRpaLogPatternTail() != null) {
				m_txtPatternTail.setText(m_rpa.getRpaLogPatternTail());
			}
			if (m_rpa.getRpaLogMaxBytes() != null) {
				m_txtMaxBytes.setText(String.valueOf(m_rpa.getRpaLogMaxBytes()));
			}
			if (m_rpa.getRpaDefaultEndValue() != null) {
				m_defaultEndValueText.setText(String.valueOf(m_rpa.getRpaDefaultEndValue()));
			}
			if (m_rpa.getRpaJobEndValueConditionInfos() != null) {
				m_endValueConditionComposite.setInputData(m_rpa.getRpaJobEndValueConditionInfos());
			}
		} else {
			// 新規作成の場合はデフォルト値を表示
			m_directory.setText("");
			m_fileName.setText("");
			m_fileEncoding.setText("UTF-8"); // WinActorのログのデフォルト値と合わせる
			m_fileReturnCode.setText("CRLF"); // Windows環境のためデフォルト値はCRLFにする
			m_txtPatternHead.setText("");
			m_txtPatternTail.setText("");
			m_txtMaxBytes.setText("");
			m_defaultEndValueText.setText("-1");
		}
	}

	public ValidateResult validateRpaJobInfo() {
		ValidateResult result = null;
		if (m_rpaJobType == RpaJobTypeConstant.INDIRECT) {
			return result;
		}

		// ログファイルによる判定条件が含まれている場合のみ必須項目のチェックを行う
		if (m_endValueConditionComposite.isLogConditionExisting()) {
			// ログファイルディレクトリ
			if (!JobDialogUtil.validateText(m_directory)) {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.14"));
			}
			// ログファイル名
			if (!JobDialogUtil.validateText(m_fileName)) {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.15"));
			}
			// ログファイルエンコーディング
			if (!JobDialogUtil.validateText(m_fileEncoding)) {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.16"));
			}
		}

		// 最大読み取り文字数
		if (JobDialogUtil.validateText(m_txtMaxBytes)) {
			// 入力されている場合のみ数値であることをチェック
			if (!JobDialogUtil.validateNumberText(m_txtMaxBytes)) {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.17", new String[] { Integer.toString(Integer.MAX_VALUE) }));
			}
		}
		// いずれの判定条件にも一致しなかった場合の終了値
		if (!JobDialogUtil.validateNumberText(m_defaultEndValueText)) {
			return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.job.rpa.18"));
		}

		return result;
	}

	public void createRpaJobInfo() {
		// ログファイルディレクトリ
		m_rpa.setRpaLogDirectory(m_directory.getText());
		// ログファイル名
		m_rpa.setRpaLogFileName(m_fileName.getText());
		// ログファイルエンコーディング
		m_rpa.setRpaLogEncoding(m_fileEncoding.getText());
		// ログファイル改行コード
		m_rpa.setRpaLogReturnCode(m_fileReturnCode.getText());
		// ログファイル先頭パターン
		m_rpa.setRpaLogPatternHead(m_txtPatternHead.getText());
		// ログファイル終端パターン
		m_rpa.setRpaLogPatternTail(m_txtPatternTail.getText());
		// ログファイル最大読み取り文字数
		try {
			m_rpa.setRpaLogMaxBytes(Integer.valueOf(m_txtMaxBytes.getText()));
		} catch (NumberFormatException e) {
			m_rpa.setRpaLogMaxBytes(null);
		}
		// いずれの判定条件にも一致しなかった場合の終了値
		try {
			m_rpa.setRpaDefaultEndValue(Integer.valueOf(m_defaultEndValueText.getText()));
		} catch (NumberFormatException e) {
			m_rpa.setRpaDefaultEndValue(null);
		}
		// 終了値判定条件
		m_rpa.setRpaJobEndValueConditionInfos(m_endValueConditionComposite.getInputData());

	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_directory.setEnabled(enabled);
		m_fileName.setEnabled(enabled);
		m_fileEncoding.setEnabled(enabled);
		m_fileReturnCode.setEnabled(enabled);
		m_txtPatternHead.setEnabled(enabled);
		m_txtPatternTail.setEnabled(enabled);
		m_txtMaxBytes.setEnabled(enabled);
		m_defaultEndValueText.setEnabled(enabled);
		m_conditionAddButton.setEnabled(enabled);
		m_conditionModifyButton.setEnabled(enabled);
		m_conditionDeleteButton.setEnabled(enabled);
		m_conditionCopyButton.setEnabled(enabled);
		m_conditionUpButton.setEnabled(enabled);
		m_conditionDownButton.setEnabled(enabled);
		this.m_enabled = enabled;
		update(); // 読み込み専用時は必須項目を明示しない
	}

	/**
	 * @return the m_rpa
	 */
	public JobRpaInfoResponse getRpaJobInfo() {
		return m_rpa;
	}

	/**
	 * @param m_rpa
	 *            the m_rpa to set
	 */
	public void setRpaJobInfo(JobRpaInfoResponse rpa) {
		this.m_rpa = rpa;
	}

	/**
	 * RPAシナリオジョブ種別を設定します。<br>
	 * 必須項目のチェック有無を判断するために使用します。
	 * 
	 * @param rpaJobType
	 */
	public void setRpaJobType(Integer rpaJobType) {
		this.m_rpaJobType = rpaJobType;
	}
}
