/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobRpaInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobTypeConstant;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;

public class RpaIndirectControlComposite extends Composite {

	/** シナリオ実行 コネクションタイムアウト（テキスト） */
	private Text m_runConnectTimeoutText = null;

	/** シナリオ実行 リクエストタイムアウト（テキスト） */
	private Text m_runRequestTimeoutText = null;

	/** シナリオ実行 実行できない場合に終了するチェックボタン */
	private Button m_runEndButton = null;

	/** シナリオ実行 リトライ回数（テキスト） */
	private Text m_runRetryText = null;

	/** シナリオ実行 終了値（テキスト） */
	private Text m_runEndValueText = null;

	/** シナリオ実行結果確認 コネクションタイムアウト（テキスト） */
	private Text m_checkConnectTimeoutText = null;

	/** シナリオ実行結果確認 リクエストタイムアウト（テキスト） */
	private Text m_checkRequestTimeoutText = null;

	/** シナリオ実行結果確認 実行できない場合に終了するチェックボタン */
	private Button m_checkEndButton = null;

	/** シナリオ実行結果確認 リトライ回数（テキスト） */
	private Text m_checkRetryText = null;

	/** シナリオ実行結果確認 終了値（テキスト） */
	private Text m_checkEndValueText = null;

	/** RPAシナリオジョブ実行情報 */
	private JobRpaInfoResponse m_rpa = null;

	/** 読み取り専用モードのフラグ */
	private boolean m_enabled = false;

	/** RPAシナリオジョブ種別 */
	private Integer m_rpaJobType = null;

	public RpaIndirectControlComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	private void initialize() {
		this.setLayout(JobDialogUtil.getParentLayout());

		// シナリオ実行時の制御（グループ）
		Group runControlGroup = new Group(this, SWT.NONE);
		runControlGroup.setText(Messages.getString("rpa.control.run.scenario"));
		runControlGroup.setLayout(new GridLayout(2, false));

		// コネクションタイムアウト（Composite）
		Composite runConnectTimeoutComposite = new Composite(runControlGroup, SWT.NONE);
		runConnectTimeoutComposite.setLayout(new GridLayout(3, false));

		// コネクションタイムアウト（ラベル）
		Label runConnectTimeoutLabel = new Label(runConnectTimeoutComposite, SWT.LEFT);
		runConnectTimeoutLabel.setText(Messages.getString("rpa.connect.timeout") + " : ");
		runConnectTimeoutLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// コネクションタイムアウト（テキスト）
		m_runConnectTimeoutText = new Text(runConnectTimeoutComposite, SWT.BORDER);
		m_runConnectTimeoutText.setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		m_runConnectTimeoutText.addModifyListener(e -> update());
		m_runConnectTimeoutText.addVerifyListener(new NumberVerifyListener(0, DataRangeConstant.SMALLINT_HIGH));

		// 秒（ラベル）
		Label runConnectTimeoutSecLabel = new Label(runConnectTimeoutComposite, SWT.LEFT);
		runConnectTimeoutSecLabel.setText(Messages.getString("second"));
		runConnectTimeoutSecLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// リクエストタイムアウト（Composite）
		Composite runRequestTimeoutComposite = new Composite(runControlGroup, SWT.NONE);
		runRequestTimeoutComposite.setLayout(new GridLayout(3, false));

		// リクエストタイムアウト（ラベル）
		Label runRequestTimeoutLabel = new Label(runRequestTimeoutComposite, SWT.LEFT);
		runRequestTimeoutLabel.setText(Messages.getString("rpa.request.timeout") + " : ");
		runRequestTimeoutLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// リクエストタイムアウト（テキスト）
		m_runRequestTimeoutText = new Text(runRequestTimeoutComposite, SWT.BORDER);
		m_runRequestTimeoutText.setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		m_runRequestTimeoutText.addModifyListener(e -> update());
		m_runRequestTimeoutText.addVerifyListener(new NumberVerifyListener(0, DataRangeConstant.SMALLINT_HIGH));

		// 秒（ラベル）
		Label runRequestTimeoutSecLabel = new Label(runRequestTimeoutComposite, SWT.LEFT);
		runRequestTimeoutSecLabel.setText(Messages.getString("second"));
		runRequestTimeoutSecLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// 実行できない場合に終了する（チェックボックス）
		this.m_runEndButton = new Button(runControlGroup, SWT.CHECK);
		this.m_runEndButton.setText(Messages.getString("rpa.run.fail.end") + " : ");
		GridData runEndButtonGrid = new GridData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT);
		runEndButtonGrid.horizontalSpan = 2;
		this.m_runEndButton.setLayoutData(runEndButtonGrid);
		this.m_runEndButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_runRetryText.setEditable(check.getSelection());
				m_runEndValueText.setEditable(check.getSelection());
				update();
			}
		});

		// リトライ回数（Composite）
		Composite runRetryComposite = new Composite(runControlGroup, SWT.NONE);
		runRetryComposite.setLayout(new GridLayout(2, false));
		runRetryComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

		// リトライ回数（ラベル）
		Label runRetryLabel = new Label(runRetryComposite, SWT.LEFT);
		runRetryLabel.setText(Messages.getString("retry.count") + " : ");
		runRetryLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// リトライ回数（テキスト）
		m_runRetryText = new Text(runRetryComposite, SWT.BORDER);
		m_runRetryText.setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		m_runRetryText.addModifyListener(e -> update());
		m_runRetryText.addVerifyListener(new NumberVerifyListener(0, DataRangeConstant.SMALLINT_HIGH));

		// 終了値（Composite）
		Composite runEndValueComposite = new Composite(runControlGroup, SWT.NONE);
		runEndValueComposite.setLayout(new GridLayout(2, false));

		// 終了値（ラベル）
		Label runEndValueLabel = new Label(runEndValueComposite, SWT.LEFT);
		runEndValueLabel.setText(Messages.getString("end.value") + " : ");
		runEndValueLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// 終了値（テキスト）
		m_runEndValueText = new Text(runEndValueComposite, SWT.BORDER);
		m_runEndValueText.setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		m_runEndValueText.addModifyListener(e -> update());
		m_runEndValueText.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));

		// シナリオ実行結果確認時の制御（グループ）
		Group checkControlGroup = new Group(this, SWT.NONE);
		checkControlGroup.setText(Messages.getString("rpa.control.check.scenario"));
		checkControlGroup.setLayout(new GridLayout(2, false));

		// コネクションタイムアウト（Composite）
		Composite checkConnectTimeoutComposite = new Composite(checkControlGroup, SWT.NONE);
		checkConnectTimeoutComposite.setLayout(new GridLayout(3, false));

		// コネクションタイムアウト（ラベル）
		Label checkConnectTimeoutLabel = new Label(checkConnectTimeoutComposite, SWT.LEFT);
		checkConnectTimeoutLabel.setText(Messages.getString("rpa.connect.timeout") + " : ");
		checkConnectTimeoutLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// コネクションタイムアウト（テキスト）
		m_checkConnectTimeoutText = new Text(checkConnectTimeoutComposite, SWT.BORDER);
		m_checkConnectTimeoutText.setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		m_checkConnectTimeoutText.addModifyListener(e -> update());
		m_checkConnectTimeoutText.addVerifyListener(new NumberVerifyListener(0, DataRangeConstant.SMALLINT_HIGH));

		// 秒（ラベル）
		Label checkConnectTimeoutSecLabel = new Label(checkConnectTimeoutComposite, SWT.LEFT);
		checkConnectTimeoutSecLabel.setText(Messages.getString("second"));
		checkConnectTimeoutSecLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// リクエストタイムアウト（Composite）
		Composite checkRequestTimeoutComposite = new Composite(checkControlGroup, SWT.NONE);
		checkRequestTimeoutComposite.setLayout(new GridLayout(3, false));

		// リクエストタイムアウト（ラベル）
		Label checkRequestTimeoutLabel = new Label(checkRequestTimeoutComposite, SWT.LEFT);
		checkRequestTimeoutLabel.setText(Messages.getString("rpa.request.timeout") + " : ");
		checkRequestTimeoutLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// リクエストタイムアウト（テキスト）
		m_checkRequestTimeoutText = new Text(checkRequestTimeoutComposite, SWT.BORDER);
		m_checkRequestTimeoutText.setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		m_checkRequestTimeoutText.addModifyListener(e -> update());
		m_checkRequestTimeoutText.addVerifyListener(new NumberVerifyListener(0, DataRangeConstant.SMALLINT_HIGH));

		// 秒（ラベル）
		Label checkRequestTimeoutSecLabel = new Label(checkRequestTimeoutComposite, SWT.LEFT);
		checkRequestTimeoutSecLabel.setText(Messages.getString("second"));
		checkRequestTimeoutSecLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// 実行できない場合に終了する（チェックボックス）
		this.m_checkEndButton = new Button(checkControlGroup, SWT.CHECK);
		this.m_checkEndButton.setText(Messages.getString("rpa.check.fail.end") + " : ");
		GridData checkEndButtonGrid = new GridData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT);
		checkEndButtonGrid.horizontalSpan = 2;
		this.m_checkEndButton.setLayoutData(checkEndButtonGrid);
		this.m_checkEndButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_checkRetryText.setEditable(check.getSelection());
				m_checkEndValueText.setEditable(check.getSelection());
				update();
			}
		});

		// リトライ回数（Composite）
		Composite checkRetryComposite = new Composite(checkControlGroup, SWT.NONE);
		checkRetryComposite.setLayout(new GridLayout(2, false));
		checkRetryComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

		// リトライ回数（ラベル）
		Label checkRetryLabel = new Label(checkRetryComposite, SWT.LEFT);
		checkRetryLabel.setText(Messages.getString("retry.count") + " : ");
		checkRetryLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// リトライ回数（テキスト）
		m_checkRetryText = new Text(checkRetryComposite, SWT.BORDER);
		m_checkRetryText.setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		m_checkRetryText.addModifyListener(e -> update());
		m_checkRetryText.addVerifyListener(new NumberVerifyListener(0, DataRangeConstant.SMALLINT_HIGH));

		// 終了値（Composite）
		Composite checkEndValueComposite = new Composite(checkControlGroup, SWT.NONE);
		checkEndValueComposite.setLayout(new GridLayout(2, false));

		// 終了値（ラベル）
		Label checkEndValueLabel = new Label(checkEndValueComposite, SWT.LEFT);
		checkEndValueLabel.setText(Messages.getString("end.value") + " : ");
		checkEndValueLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// 終了値（テキスト）
		m_checkEndValueText = new Text(checkEndValueComposite, SWT.BORDER);
		m_checkEndValueText.setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		m_checkEndValueText.addModifyListener(e -> update());
		m_checkEndValueText.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		// シナリオ実行 コネクションタイムアウト
		m_runConnectTimeoutText.setEditable(enabled);
		// シナリオ実行 リクエストタイムアウト
		m_runRequestTimeoutText.setEditable(enabled);
		// シナリオ実行 実行できない場合に終了する
		m_runEndButton.setEnabled(enabled);
		// シナリオ実行 リトライ回数
		m_runRetryText.setEditable(m_runEndButton.getSelection() && enabled);
		// シナリオ実行 終了値
		m_runEndValueText.setEditable(m_runEndButton.getSelection() && enabled);
		// シナリオ実行結果確認 コネクションタイムアウト
		m_checkConnectTimeoutText.setEditable(enabled);
		// シナリオ実行結果確認 リクエストタイムアウト
		m_checkRequestTimeoutText.setEditable(enabled);
		// シナリオ実行結果確認 実行できない場合に終了する
		m_checkEndButton.setEnabled(enabled);
		// シナリオ実行結果確認 リトライ回数
		m_checkRetryText.setEditable(m_checkEndButton.getSelection() && enabled);
		// シナリオ実行結果確認 終了値
		m_checkEndValueText.setEditable(m_checkEndButton.getSelection() && enabled);
		this.m_enabled = enabled;
		update(); // 読み込み専用時は必須項目を明示しない
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update() {
		// 必須項目を明示
		if (m_enabled && "".equals(this.m_runConnectTimeoutText.getText())) {
			this.m_runConnectTimeoutText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_runConnectTimeoutText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_enabled && "".equals(this.m_runRequestTimeoutText.getText())) {
			this.m_runRequestTimeoutText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_runRequestTimeoutText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_runEndButton.getSelection()) {
			if (m_enabled && "".equals(this.m_runRetryText.getText())) {
				this.m_runRetryText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_runRetryText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
			if (m_enabled && "".equals(this.m_runEndValueText.getText())) {
				this.m_runEndValueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_runEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		} else {
			this.m_runRetryText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.m_runEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (m_enabled && "".equals(this.m_checkConnectTimeoutText.getText())) {
			this.m_checkConnectTimeoutText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_checkConnectTimeoutText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_enabled && "".equals(this.m_checkRequestTimeoutText.getText())) {
			this.m_checkRequestTimeoutText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_checkRequestTimeoutText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_checkEndButton.getSelection()) {
			if (m_enabled && "".equals(this.m_checkRetryText.getText())) {
				this.m_checkRetryText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_checkRetryText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
			if (m_enabled && "".equals(this.m_checkEndValueText.getText())) {
				this.m_checkEndValueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_checkEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		} else {
			this.m_checkRetryText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.m_checkEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	public void reflectRpaJobInfo() {
		if (m_rpa != null) {
			if (m_rpa.getRpaRunConnectTimeout() != null) {
				m_runConnectTimeoutText.setText(String.valueOf(m_rpa.getRpaRunConnectTimeout()));
			}
			if (m_rpa.getRpaRunRequestTimeout() != null) {
				m_runRequestTimeoutText.setText(String.valueOf(m_rpa.getRpaRunRequestTimeout()));
			}
			if (m_rpa.getRpaRunEndFlg() != null) {
				m_runEndButton.setSelection(m_rpa.getRpaRunEndFlg());
			}
			if (m_rpa.getRpaRunRetry() != null) {
				m_runRetryText.setText(String.valueOf(m_rpa.getRpaRunRetry()));
			}
			if (m_rpa.getRpaRunEndValue() != null) {
				m_runEndValueText.setText(String.valueOf(m_rpa.getRpaRunEndValue()));
			}
			if (m_rpa.getRpaCheckConnectTimeout() != null) {
				m_checkConnectTimeoutText.setText(String.valueOf(m_rpa.getRpaCheckConnectTimeout()));
			}
			if (m_rpa.getRpaCheckRequestTimeout() != null) {
				m_checkRequestTimeoutText.setText(String.valueOf(m_rpa.getRpaCheckRequestTimeout()));
			}
			if (m_rpa.getRpaCheckEndFlg() != null) {
				m_checkEndButton.setSelection(m_rpa.getRpaCheckEndFlg());
			}
			if (m_rpa.getRpaCheckRetry() != null) {
				m_checkRetryText.setText(String.valueOf(m_rpa.getRpaCheckRetry()));
			}
			if (m_rpa.getRpaCheckEndValue() != null) {
				m_checkEndValueText.setText(String.valueOf(m_rpa.getRpaCheckEndValue()));
			}
		} else {
			// 新規作成の場合はデフォルト値を表示
			m_runConnectTimeoutText.setText("10");
			m_runRequestTimeoutText.setText("10");
			m_runEndButton.setSelection(true);
			m_runRetryText.setText("10");
			m_runEndValueText.setText("-1");
			m_checkConnectTimeoutText.setText("10");
			m_checkRequestTimeoutText.setText("10");
			m_checkEndButton.setSelection(true);
			m_checkRetryText.setText("10");
			m_checkEndValueText.setText("-1");
		}
	}

	public ValidateResult validateRpaJobInfo() {
		ValidateResult result = null;
		if (m_rpaJobType == RpaJobTypeConstant.DIRECT) {
			return result;
		}

		// シナリオ実行時のコネクションタイムアウト
		if (!JobDialogUtil.validateNumberText(m_runConnectTimeoutText)) {
			return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.job.rpa.29"));
		}

		// シナリオ実行時のリクエストタイムアウト
		if (!JobDialogUtil.validateNumberText(m_runRequestTimeoutText)) {
			return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.job.rpa.30"));
		}

		if (m_runEndButton.getSelection()) {
			// 実行できない場合に終了する場合のみ必須項目のチェックを行う
			// 実行できない場合のリトライ回数
			if (!JobDialogUtil.validateNumberText(m_runRetryText)) {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.31"));
			}
			// 実行できない場合の終了値
			if (!JobDialogUtil.validateNumberText(m_runEndValueText)) {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.32"));
			}
		}

		// シナリオ実行結果確認時のコネクションタイムアウト
		if (!JobDialogUtil.validateNumberText(m_checkConnectTimeoutText)) {
			return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.job.rpa.33"));
		}

		// シナリオ実行結果確認時のリクエストタイムアウト
		if (!JobDialogUtil.validateNumberText(m_checkRequestTimeoutText)) {
			return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.job.rpa.34"));
		}

		if (m_checkEndButton.getSelection()) {
			// 実行結果が確認できない場合に終了する場合のみ必須項目のチェックを行う
			// 実行結果が確認できない場合のリトライ回数
			if (!JobDialogUtil.validateNumberText(m_checkRetryText)) {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.35"));
			}
			// 実行結果が確認できない場合の終了値
			if (!JobDialogUtil.validateNumberText(m_checkEndValueText)) {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.36"));
			}
		}

		return result;
	}

	public void createRpaJobInfo() {
		// シナリオ実行時のコネクションタイムアウト
		m_rpa.setRpaRunConnectTimeout(getIntegerValue(m_runConnectTimeoutText));
		// シナリオ実行時のリクエストタイムアウト
		m_rpa.setRpaRunRequestTimeout(getIntegerValue(m_runRequestTimeoutText));
		// シナリオが実行できない場合に終了する
		m_rpa.setRpaRunEndFlg(m_runEndButton.getSelection());
		// 実行できない場合のリトライ回数
		m_rpa.setRpaRunRetry(getIntegerValue(m_runRetryText));
		// 実行できない場合の終了値
		m_rpa.setRpaRunEndValue(getIntegerValue(m_runEndValueText));
		// シナリオ実行結果確認時のコネクションタイムアウト
		m_rpa.setRpaCheckConnectTimeout(getIntegerValue(m_checkConnectTimeoutText));
		// シナリオ実行結果確認時のリクエストタイムアウト
		m_rpa.setRpaCheckRequestTimeout(getIntegerValue(m_checkRequestTimeoutText));
		// シナリオが実行結果が確認できない場合に終了する
		m_rpa.setRpaCheckEndFlg(m_checkEndButton.getSelection());
		// 実行結果が確認できない場合のリトライ回数
		m_rpa.setRpaCheckRetry(getIntegerValue(m_checkRetryText));
		// 実行結果が確認できない場合の終了値
		m_rpa.setRpaCheckEndValue(getIntegerValue(m_checkEndValueText));
	}

	private Integer getIntegerValue(Text text) {
		try {
			return Integer.valueOf(text.getText());
		} catch (NumberFormatException e) {
			return null;
		}
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
