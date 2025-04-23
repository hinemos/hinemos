/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.dialog;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobRpaEndValueConditionInfoResponse;
import org.openapitools.client.model.JobRpaEndValueConditionInfoResponse.ConditionTypeEnum;
import org.openapitools.client.model.JobRpaEndValueConditionInfoResponse.ReturnCodeConditionEnum;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.JobRpaReturnCodeConditionMessage;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.rpa.util.ReturnCodeConditionChecker;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * RPAシナリオ 直接実行 終了値判定条件設定ダイアログ用のコンポジットクラスです
 */
public class RpaDirectEndValueConditionDialog extends CommonDialog {
	/** 説明テキスト */
	private Text m_descriptionText = null;
	/** ファイルの内容で判定する ラジオボタン */
	private Button m_fileContentButton = null;
	/** RPAツールのリターンコードで判定する ラジオボタン */
	private Button m_returnCodeButton = null;
	/** パターンマッチ表現テキスト */
	private Text m_patternMatchText = null;
	/** 条件に一致する場合ボタン */
	private Button m_conditionMatchedButton = null;
	/** 条件に一致しない場合ボタン */
	private Button m_conditionNotMatchedButton = null;
	/** 大文字・小文字を区別しないボタン */
	private Button m_ignoreCaseButton = null;
	/** リターンコードテキスト */
	private Text m_returnCodeText = null;
	/** リターンコード判定条件コンボボックス */
	private ComboViewer m_returnCodeConditionComboViewer = null;
	/** コマンドのリターンコードをそのまま終了値とする チェックボタン */
	private Button m_useCommandReturnCodeButton = null;
	/** ファイルの内容で判定する 終了値テキスト */
	private Text m_fileContentEndValueText = null;
	/** RPAツールのリターンコードで判定する 終了値テキスト */
	private Text m_returnCodeEndValueText = null;
	/** 終了値判定条件 */
	private JobRpaEndValueConditionInfoResponse m_condition = null;

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *            親コンポジット
	 */
	public RpaDirectEndValueConditionDialog(Shell parent) {
		super(parent);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *            親コンポジット
	 * @param condition
	 *            終了値判定条件
	 */
	public RpaDirectEndValueConditionDialog(Shell parent, JobRpaEndValueConditionInfoResponse condition) {
		super(parent);
		m_condition = condition;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		// ダイアログタイトル
		parent.getShell().setText(Messages.getString("dialog.job.create.modify.rpa.job.end.value.condition"));

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		// 説明（ラベル）
		Composite descriptionComposite = new Composite(parent, SWT.NONE);
		descriptionComposite.setLayout(new RowLayout());
		Label descriptionLabel = new Label(descriptionComposite, SWT.NONE);
		descriptionLabel.setText(Messages.getString("description") + " : ");
		descriptionLabel.setLayoutData(new RowData(120, SizeConstant.SIZE_LABEL_HEIGHT));

		// 説明（テキスト）
		m_descriptionText = new Text(descriptionComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "descriptionText", m_descriptionText);
		m_descriptionText.setLayoutData(new RowData(200, SizeConstant.SIZE_TEXT_HEIGHT));

		// ファイルの内容で判定する(ボタン)
		m_fileContentButton = new Button(parent, SWT.RADIO);
		m_fileContentButton.setText(Messages.getString("rpa.judgment.by.file.content"));
		m_fileContentButton.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));
		m_fileContentButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				switchEnabled();
				update();
			}
		});

		// レイアウトのためのComposite
		Composite patternMatchComposite = new Composite(parent, SWT.NONE);
		GridLayout patternMatchLayout = new GridLayout(1, true);
		patternMatchLayout.marginLeft = 10; // 条件の部分を右に寄せる
		patternMatchComposite.setLayout(patternMatchLayout);

		// 条件グループ
		Group patternMatchGroup = new Group(patternMatchComposite, SWT.NONE);
		patternMatchGroup.setLayout(new GridLayout(2, false));
		patternMatchGroup.setText(Messages.get("rpa.condition"));

		// パタンマッチ表現
		Label patternMatchLabel = new Label(patternMatchGroup, SWT.NONE);
		patternMatchLabel.setText(Messages.getString("pattern.matching.expression") + " : ");
		patternMatchLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		m_patternMatchText = new Text(patternMatchGroup, SWT.BORDER);
		m_patternMatchText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		m_patternMatchText.addModifyListener(e -> update());

		// 条件に一致する場合・しない場合
		Composite conditionButtonComposite = new Composite(patternMatchGroup, SWT.NONE);
		conditionButtonComposite.setLayout(new GridLayout(1, true));

		m_conditionMatchedButton = new Button(conditionButtonComposite, SWT.RADIO);
		m_conditionMatchedButton.setText(Messages.getString("rpa.condition.matched"));

		m_conditionNotMatchedButton = new Button(conditionButtonComposite, SWT.RADIO);
		m_conditionNotMatchedButton.setText(Messages.getString("rpa.condition.not.matched"));

		// 大文字・小文字を区別しないボタン
		m_ignoreCaseButton = new Button(patternMatchGroup, SWT.CHECK);
		m_ignoreCaseButton.setText(Messages.getString("case.sensitive"));
		m_ignoreCaseButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		// 終了値
		Composite fileContentEndValueComposite = new Composite(patternMatchComposite, SWT.NONE);
		fileContentEndValueComposite.setLayout(new RowLayout());

		Label fileContentEndValueLabel = new Label(fileContentEndValueComposite, SWT.NONE);
		fileContentEndValueLabel.setText(Messages.getString("end.value") + " : ");
		fileContentEndValueLabel.setLayoutData(new RowData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		m_fileContentEndValueText = new Text(fileContentEndValueComposite, SWT.BORDER);
		m_fileContentEndValueText.setLayoutData(new RowData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		m_fileContentEndValueText.addModifyListener(e -> update());
		m_fileContentEndValueText.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));

		// リターンコードで判定する(ボタン)
		m_returnCodeButton = new Button(parent, SWT.RADIO);
		m_returnCodeButton.setText(Messages.getString("rpa.judgment.by.return.code"));
		m_returnCodeButton.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));
		m_returnCodeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				switchEnabled();
				update();
			}
		});

		// レイアウトのためのComposite
		Composite conditionComposite = new Composite(parent, SWT.NONE);
		GridLayout conditionLayout = new GridLayout(1, true);
		conditionLayout.marginLeft = 10; // 条件の部分を右に寄せる
		conditionComposite.setLayout(conditionLayout);

		// 条件グループ
		Group returnCodeGroup = new Group(conditionComposite, SWT.NONE);
		returnCodeGroup.setLayout(new GridLayout(2, false));
		returnCodeGroup.setText(Messages.get("rpa.condition"));

		// リターンコード
		Label returnCodeLabel = new Label(returnCodeGroup, SWT.NONE);
		returnCodeLabel.setText(Messages.getString("rpa.return.code") + " : ");
		returnCodeLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		m_returnCodeText = new Text(returnCodeGroup, SWT.BORDER);
		m_returnCodeText.setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		m_returnCodeText.addModifyListener(e -> update());

		// 判定条件
		Label conditionLabel = new Label(returnCodeGroup, SWT.NONE);
		conditionLabel.setText(Messages.getString("judgment.condition") + " : ");
		conditionLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		m_returnCodeConditionComboViewer = new ComboViewer(returnCodeGroup, SWT.READ_ONLY);
		m_returnCodeConditionComboViewer.getCombo().setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		// プルダウン項目を設定
		m_returnCodeConditionComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		m_returnCodeConditionComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReturnCodeConditionEnum) {
					ReturnCodeConditionEnum condition = (ReturnCodeConditionEnum) element;
					return JobRpaReturnCodeConditionMessage.typeToString(condition.getValue());
				}
				return super.getText(element);
			}
		});
		m_returnCodeConditionComboViewer.setInput(ReturnCodeConditionEnum.values());

		// 終了値
		Composite returnCodeEndValueComposite = new Composite(conditionComposite, SWT.NONE);
		returnCodeEndValueComposite.setLayout(new RowLayout());

		Label returnCodeEndValueLabel = new Label(returnCodeEndValueComposite, SWT.NONE);
		returnCodeEndValueLabel.setText(Messages.getString("end.value") + " : ");
		returnCodeEndValueLabel.setLayoutData(new RowData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		m_returnCodeEndValueText = new Text(returnCodeEndValueComposite, SWT.BORDER);
		m_returnCodeEndValueText.setLayoutData(new RowData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		m_returnCodeEndValueText.addModifyListener(e -> update());
		m_returnCodeEndValueText.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));

		// コマンドのリターンコードをそのまま終了値にする
		m_useCommandReturnCodeButton = new Button(conditionComposite, SWT.CHECK);
		m_useCommandReturnCodeButton.setText(Messages.getString("rpa.use.return.code.as.end.value"));
		m_useCommandReturnCodeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				switchEnabled();
				update();
			}
		});

		// 変更の場合に値を反映
		if (m_condition != null) {
			if (m_condition.getConditionType() == ConditionTypeEnum.LOG) {
				m_fileContentButton.setSelection(true);
				m_returnCodeButton.setSelection(false);
				m_patternMatchText.setText(m_condition.getPattern());
				m_conditionMatchedButton.setSelection(m_condition.getProcessType());
				m_conditionNotMatchedButton.setSelection(!m_condition.getProcessType());
				m_ignoreCaseButton.setSelection(m_condition.getCaseSensitivityFlg());
				m_fileContentEndValueText.setText(String.valueOf(m_condition.getEndValue()));
			} else if (m_condition.getConditionType() == ConditionTypeEnum.RETURN_CODE) {
				m_fileContentButton.setSelection(false);
				m_returnCodeButton.setSelection(true);
				m_returnCodeText.setText(m_condition.getReturnCode());
				m_returnCodeConditionComboViewer
						.setSelection(new StructuredSelection(m_condition.getReturnCodeCondition()));
				m_returnCodeEndValueText.setText(String.valueOf(m_condition.getEndValue()));
				m_useCommandReturnCodeButton.setSelection(m_condition.getUseCommandReturnCodeFlg());
			}
			m_descriptionText.setText(m_condition.getDescription());
		} else {
			// 新規作成の場合はデフォルト値を表示
			m_fileContentButton.setSelection(true);
			m_conditionMatchedButton.setSelection(true);
			m_ignoreCaseButton.setSelection(true);
			m_returnCodeConditionComboViewer.setSelection(new StructuredSelection(ReturnCodeConditionEnum.EQUAL_NUMERIC));
			m_useCommandReturnCodeButton.setSelection(true);
		}

		if (m_fileContentButton.getSelection()) {

		}
		// 有効・無効を切り替え
		switchEnabled();

		// 必須項目を明示
		update();
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
	 * 更新処理
	 *
	 */
	private void update() {
		// 必須項目を明示
		if (m_fileContentButton.getSelection()) {
			if ("".equals(this.m_patternMatchText.getText())) {
				this.m_patternMatchText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_patternMatchText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
			if ("".equals(this.m_fileContentEndValueText.getText())) {
				this.m_fileContentEndValueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_fileContentEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		} else {
			this.m_patternMatchText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.m_fileContentEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (m_returnCodeButton.getSelection()) {
			if ("".equals(this.m_returnCodeText.getText())) {
				this.m_returnCodeText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_returnCodeText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
			if ("".equals(this.m_returnCodeEndValueText.getText()) && !m_useCommandReturnCodeButton.getSelection()) {
				this.m_returnCodeEndValueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_returnCodeEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		} else {
			this.m_returnCodeText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.m_returnCodeEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//  ファイルの内容で判定を選択時、条件に一致する/しないが未選択なら デフォルトとして一致するをセット
		if (m_fileContentButton.getSelection()) {
			if( !(m_conditionMatchedButton.getSelection()) && !(m_conditionNotMatchedButton.getSelection()) ){
				m_conditionMatchedButton.setSelection(true);
			}
		}
	}

	/**
	 * 有効・無効の切り替え
	 *
	 */
	private void switchEnabled() {
		boolean isFileContent = m_fileContentButton.getSelection();
		m_patternMatchText.setEditable(isFileContent);
		m_conditionMatchedButton.setEnabled(isFileContent);
		m_conditionNotMatchedButton.setEnabled(isFileContent);
		m_ignoreCaseButton.setEnabled(isFileContent);
		m_fileContentEndValueText.setEditable(isFileContent);
		m_returnCodeText.setEditable(!isFileContent);
		m_returnCodeConditionComboViewer.getCombo().setEnabled(!isFileContent);
		// コマンドのリターンードをそのまま終了値にする場合、終了値は入力不要
		m_returnCodeEndValueText.setEditable(!isFileContent && !m_useCommandReturnCodeButton.getSelection());
		m_useCommandReturnCodeButton.setEnabled(!isFileContent);
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
		m_condition = new JobRpaEndValueConditionInfoResponse();
		if (m_fileContentButton.getSelection()) {
			m_condition.setConditionType(ConditionTypeEnum.LOG);

			if (JobDialogUtil.validateText(m_patternMatchText)) {
				m_condition.setPattern(m_patternMatchText.getText());
			} else {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.19"));
			}

			m_condition.setCaseSensitivityFlg(m_ignoreCaseButton.getSelection());
			m_condition.setProcessType(m_conditionMatchedButton.getSelection());

			if (JobDialogUtil.validateNumberText(m_fileContentEndValueText)) {
				m_condition.setEndValue(Integer.valueOf(m_fileContentEndValueText.getText()));
			} else {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.20"));
			}
		} else {
			m_condition.setConditionType(ConditionTypeEnum.RETURN_CODE);

			// 入力の形式が正しいか確認
			if (JobDialogUtil.validateReturnCodeText(m_returnCodeText)) {
				m_condition.setReturnCode(m_returnCodeText.getText());
			} else {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.21"));
			}
			
			// ジョブ変数かどうか
			boolean isParam = SystemParameterConstant.isParamFormat(m_returnCodeText.getText());
			// ジョブ変数でなければ、区切り文字として分割し、範囲チェックを行う
			if(!isParam){
				try{
					ReturnCodeConditionChecker.comfirmReturnCodeNumberRange(
							MessageConstant.RPAJOB_END_VALUE_CONDITION_RETURN_CODE.getMessage(), m_returnCodeText.getText());
				} catch(InvalidSetting e) {
					return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),HinemosMessage.replace(e.getMessage()));
				}
			}
			
			IStructuredSelection returnCodeConditionSelection = (StructuredSelection) m_returnCodeConditionComboViewer
					.getSelection();
			ReturnCodeConditionEnum selectedCondition = (ReturnCodeConditionEnum) returnCodeConditionSelection
					.getFirstElement();
			if(selectedCondition == null){
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.38"));
			}
			// 終了値と判定条件の組み合わせチェック
			if (!isParam) {
				if (selectedCondition == ReturnCodeConditionEnum.EQUAL_NUMERIC
						|| selectedCondition == ReturnCodeConditionEnum.NOT_EQUAL_NUMERIC) {
					// 判定条件が"="か"!="の場合は、複数指定、範囲指定の書式でチェックする。
					if (!m_returnCodeText.getText().matches(ReturnCodeConditionChecker.MULTI_RANGE_CONDITION_REGEX)) {
						return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
								Messages.getString("message.job.rpa.25"));
					}
				} else {
					// 既に書式チェック済みではあるが、それ以外の場合は、念のために数値書式であることを確認する。
					if (!m_returnCodeText.getText().matches(ReturnCodeConditionChecker.NUMBER_REGEX)) {
						return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
								Messages.getString("message.job.rpa.25"));
					}
				}
			}
			m_condition.setReturnCodeCondition(selectedCondition);
			m_condition.setUseCommandReturnCodeFlg(m_useCommandReturnCodeButton.getSelection());

			// コマンドのリターンコードをそのまま終了値とする場合、終了値の入力は不要
			if (!m_useCommandReturnCodeButton.getSelection()) {
				if (JobDialogUtil.validateNumberText(m_returnCodeEndValueText)) {
					m_condition.setEndValue(Integer.valueOf(m_returnCodeEndValueText.getText()));
				} else {
					return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.job.rpa.22"));
				}
			}
		}
		m_condition.setDescription(m_descriptionText.getText());

		return null;
	}

	/**
	 * 入力値を返します。
	 *
	 * @return 判定対象情報
	 */
	public JobRpaEndValueConditionInfoResponse getInputData() {
		return m_condition;
	}

	/**
	 * 入力値を設定します。
	 *
	 * @return 判定対象情報
	 */
	public void setInputData(JobRpaEndValueConditionInfoResponse condition) {
		m_condition = condition;
	}
}
