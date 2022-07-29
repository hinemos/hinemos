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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobEndStatusInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusColorConstant;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 終了状態タブ用のコンポジットクラスです。
 *
 * @version 4.0.0
 * @since 1.0.0
 */
public class EndStatusComposite extends Composite {
	/** 正常終了値 */
	private Text m_normalValue = null;
	/** 正常終了値範囲(開始) */
	private Text m_normalStartRange = null;
	/** 正常終了値範囲(終了) */
	private Text m_normalEndRange = null;
	/** 警告終了値 */
	private Text m_warningValue = null;
	/** 警告終了値範囲(開始) */
	private Text m_warningStartRange = null;
	/** 警告終了値範囲(終了) */
	private Text m_warningEndRange = null;
	/** 異常終了値 */
	private Text m_abnormalValue = null;
	/** ジョブ終了値情報のリスト */
	private List<JobEndStatusInfoResponse> m_end = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public EndStatusComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		this.setLayout(JobDialogUtil.getParentLayout());

		// Composite
		Composite endStatusComposite = new Composite(this, SWT.NONE);
		endStatusComposite.setLayout(new GridLayout(5, false));

		// dummy
		new Label(endStatusComposite, SWT.NONE);

		// 終了値（ラベル）
		Label endValueTitle = new Label(endStatusComposite, SWT.CENTER);
		endValueTitle.setText(Messages.getString("end.value"));
		endValueTitle.setLayoutData(new GridData(100,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// 終了値の範囲（ラベル）
		Label rangeTitle = new Label(endStatusComposite, SWT.CENTER);
		rangeTitle.setText(Messages.getString("range.end.value"));
		rangeTitle.setLayoutData(new GridData(240,
				SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)rangeTitle.getLayoutData()).horizontalSpan = 3;

		// 正常（ラベル）
		Label normalTitle = new Label(endStatusComposite, SWT.CENTER);
		normalTitle.setText(EndStatusMessage.STRING_NORMAL + " : ");
		normalTitle.setLayoutData(new GridData(60,
				SizeConstant.SIZE_LABEL_HEIGHT));
		normalTitle.setBackground(EndStatusColorConstant.COLOR_NORMAL);

		// 正常：終了値（テキスト）
		this.m_normalValue = new Text(endStatusComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_normalValue", this.m_normalValue);
		this.m_normalValue.setLayoutData(new GridData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_normalValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH));
		this.m_normalValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 正常：終了値の範囲FROM（テキスト）
		this.m_normalStartRange = new Text(endStatusComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_normalStartRange", this.m_normalStartRange);
		this.m_normalStartRange.setLayoutData(new GridData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_normalStartRange.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH));
		this.m_normalStartRange.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 正常：終了値の範囲-（ラベル）
		Label normalTo = new Label(endStatusComposite, SWT.CENTER);
		normalTo.setText("-");
		normalTo.setLayoutData(new GridData(20, SizeConstant.SIZE_LABEL_HEIGHT));

		// 正常：終了値の範囲TO（テキスト）
		this.m_normalEndRange = new Text(endStatusComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_normalEndRange", this.m_normalEndRange);
		this.m_normalEndRange.setLayoutData(new GridData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_normalEndRange.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH));
		this.m_normalEndRange.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 警告（ラベル）
		Label warningTitle = new Label(endStatusComposite, SWT.CENTER);
		warningTitle.setText(EndStatusMessage.STRING_WARNING + " : ");
		warningTitle.setLayoutData(new GridData(60,
				SizeConstant.SIZE_LABEL_HEIGHT));
		warningTitle.setBackground(EndStatusColorConstant.COLOR_WARNING);

		// 警告：終了値（テキスト）
		this.m_warningValue = new Text(endStatusComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_warningValue", this.m_warningValue);
		this.m_warningValue.setLayoutData(new GridData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_warningValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH));
		this.m_warningValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 警告：終了値の範囲FROM（テキスト）
		this.m_warningStartRange = new Text(endStatusComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_warningStartRange", this.m_warningStartRange);
		this.m_warningStartRange.setLayoutData(new GridData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_warningStartRange.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH));
		this.m_warningStartRange.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 警告：終了値の範囲-（ラベル）
		Label warningTo = new Label(endStatusComposite, SWT.CENTER);
		warningTo.setText("-");
		warningTo
		.setLayoutData(new GridData(20, SizeConstant.SIZE_LABEL_HEIGHT));

		// 警告：終了値の範囲TO（テキスト）
		this.m_warningEndRange = new Text(endStatusComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_warningEndRange", this.m_warningEndRange);
		this.m_warningEndRange.setLayoutData(new GridData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_warningEndRange.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH));
		this.m_warningEndRange.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 異常（ラベル）
		Label abnormalTitle = new Label(endStatusComposite, SWT.CENTER);
		abnormalTitle.setText(EndStatusMessage.STRING_ABNORMAL + " : ");
		abnormalTitle.setLayoutData(new GridData(60,
				SizeConstant.SIZE_LABEL_HEIGHT));
		abnormalTitle.setBackground(EndStatusColorConstant.COLOR_ABNORMAL);

		// 異常：終了値（テキスト）
		this.m_abnormalValue = new Text(endStatusComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_abnormalValue", this.m_abnormalValue);
		this.m_abnormalValue.setLayoutData(new GridData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_abnormalValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH));
		this.m_abnormalValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 異常：終了値の範囲（ラベル）
		Label abnormalMessage = new Label(endStatusComposite, SWT.CENTER);
		abnormalMessage.setText(Messages.getString("other"));
		abnormalMessage.setLayoutData(new GridData(240,
				SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)abnormalMessage.getLayoutData()).horizontalSpan = 3;
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須項目を明示
		if("".equals(this.m_normalValue.getText())){
			this.m_normalValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_normalValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_normalStartRange.getText())){
			this.m_normalStartRange.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_normalStartRange.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_normalEndRange.getText())){
			this.m_normalEndRange.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_normalEndRange.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_warningValue.getText())){
			this.m_warningValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_warningValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_warningStartRange.getText())){
			this.m_warningStartRange.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_warningStartRange.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_warningEndRange.getText())){
			this.m_warningEndRange.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_warningEndRange.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_abnormalValue.getText())){
			this.m_abnormalValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_abnormalValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ジョブ終了状態情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobEndStatusInfo
	 */
	public void reflectEndInfo() {

		// 初期値
		m_normalValue.setText(String
				.valueOf(EndStatusConstant.INITIAL_VALUE_NORMAL));
		m_normalStartRange.setText("0");
		m_normalEndRange.setText("0");
		m_warningValue.setText(String
				.valueOf(EndStatusConstant.INITIAL_VALUE_WARNING));
		m_warningStartRange.setText("1");
		m_warningEndRange.setText("1");
		m_abnormalValue.setText(String
				.valueOf(EndStatusConstant.INITIAL_VALUE_ABNORMAL));

		if (m_end != null) {
			JobEndStatusInfoResponse infoNormal = null;
			JobEndStatusInfoResponse infoWarning = null;
			JobEndStatusInfoResponse infoAbnormal = null;

			for (int i = 0; i < m_end.size(); i++) {
				if (m_end.get(i).getType() ==  JobEndStatusInfoResponse.TypeEnum.NORMAL) {
					infoNormal = m_end.get(i);
				} else if (m_end.get(i).getType() ==  JobEndStatusInfoResponse.TypeEnum.WARNING) {
					infoWarning = m_end.get(i);
				} else if (m_end.get(i).getType() ==  JobEndStatusInfoResponse.TypeEnum.ABNORMAL) {
					infoAbnormal = m_end.get(i);
				}
			}

			//正常
			if (infoNormal != null) {
				//終了値設定
				m_normalValue.setText(String.valueOf(infoNormal.getValue()));
				//終了値範囲の開始値設定
				m_normalStartRange.setText(String.valueOf(infoNormal
						.getStartRangeValue()));
				//終了値範囲の終了値設定
				m_normalEndRange.setText(String.valueOf(infoNormal
						.getEndRangeValue()));
			}

			//警告
			if (infoWarning != null) {
				//終了値設定
				m_warningValue.setText(String.valueOf(infoWarning.getValue()));
				//終了値範囲の開始値設定
				m_warningStartRange.setText(String.valueOf(infoWarning
						.getStartRangeValue()));
				//終了値範囲の終了値設定
				m_warningEndRange.setText(String.valueOf(infoWarning
						.getEndRangeValue()));
			}

			//異常
			if (infoAbnormal != null) {
				//終了値設定
				m_abnormalValue
				.setText(String.valueOf(infoAbnormal.getValue()));
			}
		}
	}

	/**
	 * ジョブ終了状態情報を設定します。
	 *
	 * @param end ジョブ終了状態情報のリスト
	 */
	public void setEndInfo(List<JobEndStatusInfoResponse> end) {
		m_end = end;
	}

	/**
	 * ジョブ終了状態情報を返します。
	 *
	 * @return ジョブ終了状態情報のリスト
	 */
	public List<JobEndStatusInfoResponse> getEndInfo() {
		return m_end;
	}

	/**
	 * コンポジットの情報から、ジョブ終了状態情報を作成する。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobEndStatusInfo
	 */
	public ValidateResult createEndInfo() {
		ValidateResult result = null;

		JobEndStatusInfoResponse infoNormal = null;
		JobEndStatusInfoResponse infoWarning = null;
		JobEndStatusInfoResponse infoAbnormal = null;

		//終了状態定義情報クラスのインスタンスを作成・取得
		m_end = new ArrayList<JobEndStatusInfoResponse>();
		infoNormal = new JobEndStatusInfoResponse();
		infoNormal.setType(JobEndStatusInfoResponse.TypeEnum.NORMAL);
		m_end.add(infoNormal);
		infoWarning = new JobEndStatusInfoResponse();
		infoWarning.setType(JobEndStatusInfoResponse.TypeEnum.WARNING);
		m_end.add(infoWarning);
		infoAbnormal = new JobEndStatusInfoResponse();
		infoAbnormal.setType(JobEndStatusInfoResponse.TypeEnum.ABNORMAL);
		m_end.add(infoAbnormal);

		try {
			//正常時の終了値取得
			infoNormal.setValue(Integer.parseInt(m_normalValue.getText()));
		} catch (NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.7"));
			return result;
		}
		try {
			//正常時の終了値範囲の開始値取得
			infoNormal.setStartRangeValue(Integer.parseInt(m_normalStartRange
					.getText()));
		} catch (NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.8"));
			return result;
		}
		try {
			//正常時の終了値範囲の終了値取得
			infoNormal.setEndRangeValue(Integer.parseInt(m_normalEndRange
					.getText()));
		} catch (NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.9"));
			return result;
		}

		try {
			//警告時の終了値取得
			infoWarning.setValue(Integer.parseInt(m_warningValue.getText()));
		} catch (NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.10"));
			return result;
		}
		try {
			//警告時の終了値範囲の開始値取得
			infoWarning.setStartRangeValue(Integer.parseInt(m_warningStartRange
					.getText()));
		} catch (NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.11"));
			return result;
		}
		try {
			//警告時の終了値範囲の終了値取得
			infoWarning.setEndRangeValue(Integer.parseInt(m_warningEndRange
					.getText()));
		} catch (NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.12"));
			return result;
		}

		try {
			//異常時の終了値取得
			infoAbnormal.setValue(Integer.parseInt(m_abnormalValue.getText()));
		} catch (NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.13"));
			return result;
		}

		//正常時の終了値範囲チェック
		if (infoNormal.getStartRangeValue() > infoNormal.getEndRangeValue()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.14"));
			return result;
		}

		//警告時の終了値範囲チェック
		if (infoWarning.getStartRangeValue() > infoWarning.getEndRangeValue()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.15"));
			return result;
		}

		return null;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_normalValue.setEditable(enabled);
		m_normalStartRange.setEditable(enabled);
		m_normalEndRange.setEditable(enabled);
		m_warningValue.setEditable(enabled);
		m_warningStartRange.setEditable(enabled);
		m_warningEndRange.setEditable(enabled);
		m_abnormalValue.setEditable(enabled);
	}
}
