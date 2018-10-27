/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParamTypeConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.SizeConstantsWrapper;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.JobRuntimeParam;
import com.clustercontrol.ws.jobmanagement.JobRuntimeParamDetail;

/**
 * ジョブ実行契機のランタイム変数入力用コンポジットクラスです。
 *
 * @version 5.1.0
 */
public class JobKickInputParamComposite extends Composite {

	/** ランタイムジョブ変数情報 */
	private JobRuntimeParam m_jobRuntimeParam = null;

	/** 入力・固定値用テキスト */
	private Text m_valueText = null;

	/** 選択(ラジオボタン)用ラジオボタンリスト */
	private List<Button> m_valueRadioList = new ArrayList<>();

	/** 選択(コンボボックス) */
	private Combo m_valueCombo = null;

	/** 選択(コンボボックス)用値リスト */
	private List<String> m_valueComboValueList = new ArrayList<>();

	// フォント
	private final static Font jobKickFont = new Font(
			Display.getCurrent(), "MS Gothic", SizeConstantsWrapper.JOBKICK_FONT_SIZE, 0);

	/**
	 * コンストラクタ
	 *
	 * @param parent 親コンポジット
	 * @param style スタイル
	 * @param jobRuntimeParam ランタイムジョブ変数情報
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public JobKickInputParamComposite(
			Composite parent,
			int style,
			JobRuntimeParam jobRuntimeParam) {
		super(parent, style);
		this.m_jobRuntimeParam = jobRuntimeParam;
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.fill = true;
		this.setLayout(layout);

		// グループ（Composite）
		Group group = new Group(this, SWT.NONE);
		group.setText(this.m_jobRuntimeParam.getParamId()
				+ (this.m_jobRuntimeParam.isRequiredFlg()?" (*)":""));
		group.setLayout(new RowLayout(SWT.VERTICAL));
		group.setLayoutData(new RowData());
		((RowData)group.getLayoutData()).width = 570;

		// 説明（ラベル）
		Label label = new Label(group, SWT.LEFT | SWT.WRAP);
		label.setFont(jobKickFont);
		LabelSetting labelSetting = new LabelSetting(
				this.m_jobRuntimeParam.getDescription(),
				SizeConstantsWrapper.JOBKICK_RUN_HALFWIDTH_CHARACTER_WIDTH,
				SizeConstantsWrapper.JOBKICK_RUN_LABEL_LINE_WORD_COUNT);
		labelSetting.makeSetting();
		label.setText(labelSetting.getWrapLine());
		int labelWidth = (int)(SizeConstantsWrapper.JOBKICK_RUN_LABEL_LINE_INITIALHEIGHT
				+ (labelSetting.getLineCount() - 1)
				* SizeConstantsWrapper.JOBKICK_RUN_LABEL_LINE_HEIGHT);
		label.setLayoutData(new RowData(520, labelWidth));

		// コントロール
		if (this.m_jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_INPUT
				|| this.m_jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_FIXED) {
			// 「入力」「固定値」の場合
			this.m_valueText = new Text(group, SWT.BORDER);
			WidgetTestUtil.setTestId(this, "m_valueText", this.m_valueText);
			this.m_valueText.setFont(jobKickFont);
			if (this.m_jobRuntimeParam.getValue() != null) {
				this.m_valueText.setText(this.m_jobRuntimeParam.getValue());
			}
			this.m_valueText.setLayoutData(new RowData(350, SizeConstant.SIZE_TEXT_HEIGHT));
			if (this.m_jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_FIXED) {
				this.m_valueText.setEditable(false);
			}
			this.m_valueText.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent arg0) {
					update();
				}
			});

		} else if (this.m_jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_RADIO) {
			// 「選択（ラジオボタン）」の場合
			if (this.m_jobRuntimeParam.getJobRuntimeParamDetailList() != null) {
				for (JobRuntimeParamDetail jobRuntimeParamDetail 
						: this.m_jobRuntimeParam.getJobRuntimeParamDetailList()) {
					Button valueRadio = new Button(group, SWT.RADIO | SWT.WRAP);
					WidgetTestUtil.setTestId(this, "valueRadio", valueRadio);
					valueRadio.setFont(jobKickFont);
					String labelStr = String.format("%s(%s)", 
							jobRuntimeParamDetail.getDescription(), 
							jobRuntimeParamDetail.getParamValue());
					labelSetting = new LabelSetting(
							labelStr,
							SizeConstantsWrapper.JOBKICK_RUN_HALFWIDTH_CHARACTER_WIDTH,
							SizeConstantsWrapper.JOBKICK_RUN_RADIO_LINE_WORD_COUNT);
					labelSetting.makeSetting();
					int radioWidth  = (int)(SizeConstantsWrapper.JOBKICK_RUN_RADIO_LINE_INITIALHEIGHT
							+ (labelSetting.getLineCount() - 1)
							* SizeConstantsWrapper.JOBKICK_RUN_RADIO_LINE_HEIGHT);
					valueRadio.setText(labelSetting.getWrapLine());
					valueRadio.setLayoutData(new RowData(520, radioWidth));
					valueRadio.setData(jobRuntimeParamDetail.getParamValue());
					if (this.m_jobRuntimeParam.getValue() != null
							&& this.m_jobRuntimeParam.getValue().equals(jobRuntimeParamDetail.getParamValue())) {
						valueRadio.setSelection(true);
					}
					this.m_valueRadioList.add(valueRadio);
				}
			}

		} else if (this.m_jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_COMBO) {
			// 「選択（コンボボックス）」の場合
			this.m_valueComboValueList = new ArrayList<>();
			this.m_valueCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
			this.m_valueCombo.setLayoutData(new RowData(330, SizeConstant.SIZE_COMBO_HEIGHT));
			WidgetTestUtil.setTestId(this, "valueCombo", this.m_valueCombo);
			this.m_valueCombo.setFont(jobKickFont);
			int rowcnt = 0;
			String selectText = "";
			if (this.m_jobRuntimeParam.getJobRuntimeParamDetailList() != null) {
				if (this.m_jobRuntimeParam.getValue() == null
						|| this.m_jobRuntimeParam.getValue().equals("")) {
					// デフォルト値が設定されていない場合
					this.m_valueCombo.add("", rowcnt);
					this.m_valueComboValueList.add(rowcnt, "");
					rowcnt++;
				}
				for (JobRuntimeParamDetail jobRuntimeParamDetail 
						: this.m_jobRuntimeParam.getJobRuntimeParamDetailList()) {
					String text = String.format("%s(%s)", 
							jobRuntimeParamDetail.getDescription(), 
							jobRuntimeParamDetail.getParamValue());
					if (this.m_jobRuntimeParam.getValue() != null
							&& this.m_jobRuntimeParam.getValue().equals(jobRuntimeParamDetail.getParamValue())) {
						// デフォルト値の場合
						selectText = text;
					}
					this.m_valueCombo.add(text, rowcnt);
					this.m_valueComboValueList.add(rowcnt, jobRuntimeParamDetail.getParamValue());
					rowcnt++;
				}
				this.m_valueCombo.setText(selectText);
			}
		}

		// 更新処理
		update();
	}

	/**
	 * コンポジットの情報から、入力内容をチェックする。
	 *
	 * @return 入力値の検証結果
	 */
	public ValidateResult validateInputParam() {
		ValidateResult result = null;

		// 「入力」もしくは「選択（コンボボックス）」の場合
		if (this.m_jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_INPUT
				&& this.m_jobRuntimeParam.isRequiredFlg()
				&& "".equals(this.m_valueText.getText())) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.135"
					,new Object[]{this.m_jobRuntimeParam.getParamId()}));
			return result;
		} else if (this.m_jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_COMBO
				&& this.m_jobRuntimeParam.isRequiredFlg()
				&& "".equals(this.m_valueCombo.getText())) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.135"
					,new Object[]{this.m_jobRuntimeParam.getParamId()}));
			return result;
		}

		return null;
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		if (this.m_valueText != null) {
			if (this.m_jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_INPUT
				&& this.m_jobRuntimeParam.isRequiredFlg()
				&& "".equals(this.m_valueText.getText())) {
				// 必須項目を明示
				this.m_valueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			}else{
				this.m_valueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}
	}

	/**
	 * 値を戻します。
	 * @return 値
	 */
	public String getValue() {
		String result = null;
		if (this.m_jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_INPUT
				|| this.m_jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_FIXED) {
			result = this.m_valueText.getText();
		} else if (m_jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_RADIO) {
			for (Button button : this.m_valueRadioList) {
				if (button.getSelection()) {
					result = (String)button.getData();
					break;
				}
			}
		} else if (m_jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_COMBO) {
			result = this.m_valueComboValueList.get(this.m_valueCombo.getSelectionIndex());
		}
		return result;
	}

	/**
	 * パラメータIDを戻します。
	 * @return パラメータID
	 */
	public String getParamId() {
		return this.m_jobRuntimeParam.getParamId();
	}

	/**
	 * ラベルの設定情報を作成する
	 */
	private static class LabelSetting {
		// 行数
		private int m_lineCount = 1;
		// 改行を含む文字列
		private String m_wrapLine = "";
		
		// 処理対象文字列
		private String m_line;
		// 全角文字列を１とした場合の半角文字列幅
		private double m_halfWidth;
		// １行の全角文字数
		private int m_lineCharacterCount;

		/**
		 * コンストラクタ
		 * @param line 処理対象文字列
		 * @param halfWidth 全角文字列を１とした場合の半角文字列幅
		 * @param lineCharacterCount １行の全角文字数
		 */
		private LabelSetting (String line, double halfWidth, int lineCharacterCount) {
			m_line = line;
			m_halfWidth = halfWidth;
			m_lineCharacterCount = lineCharacterCount;
			
		}
		/**
		 * コンストラクタ
		 */
		private void makeSetting() {
			if (m_line != null && !m_line.isEmpty()	 && m_halfWidth > 0 && m_lineCharacterCount > 0) {
				char[] chrArray = m_line.toCharArray();
				double lineWidth = 0;
				for (int i = 0; i < chrArray.length; i++) {
					if (String.valueOf(chrArray[i]).getBytes().length < 2) {
						// 半角
						if (m_lineCharacterCount < (lineWidth + m_halfWidth)) {
							// 行数を超える場合は次の行として処理する
							m_wrapLine+= "\n";
							m_lineCount++;
							lineWidth = 0;
							i--;
							continue;
						}
						lineWidth+= m_halfWidth;
						m_wrapLine+= chrArray[i];
					} else {
						// 全角
						if (m_lineCharacterCount < (lineWidth + 1)) {
							// 行数を超える場合は次の行として処理する
							m_wrapLine += "\n";
							m_lineCount++;
							lineWidth = 0;
							i--;
							continue;
						}
						lineWidth++;
						m_wrapLine += chrArray[i];
					}
				}
			} else {
				m_wrapLine = m_line;
			}
		}

		/**
		 * 行数を戻す
		 * @return 行数
		 */
		private int getLineCount() {
			return m_lineCount;
		}

		/**
		 * 改行を含む文字列を戻す
		 * @return 改行を含む文字列
		 */
		private String getWrapLine() {
			return m_wrapLine;
		}
	}
}
