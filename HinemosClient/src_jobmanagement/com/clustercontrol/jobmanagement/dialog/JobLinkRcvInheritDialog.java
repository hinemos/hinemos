/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobLinkInheritInfoResponse;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.bean.JobLinkInheritKeyInfoConstant;
import com.clustercontrol.util.Messages;

/**
 * メッセージ情報の追加ダイアログクラス<BR>
 *
 */
public class JobLinkRcvInheritDialog extends CommonDialog {


	/** 入力値を保持するオブジェクト **/
	private JobLinkInheritInfoResponse m_inputData = null;

	/** 入力値の正当性を保持するオブジェクト **/
	private ValidateResult m_validateResult = null;

	/** ジョブ変数 **/
	private Text m_paramId = null;

	/** メッセージ情報 **/
	private Combo m_keyInfo;

	/** 拡張情報キー **/
	private Text m_expKey = null;

	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public JobLinkRcvInheritDialog(Shell parent) {
		super(parent);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see #setInputData(Variable)
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.job.add.joblink.inherit"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// レイアウト
		GridLayout layout = new GridLayout(12, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);


		// ジョブ変数(ラベル)
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("job.parameter.name"));

		new Label(parent, SWT.NONE);

		// メッセージ情報(ラベル)
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("job.joblink.message.info"));

		// ジョブ変数(テキスト)
		this.m_paramId = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_paramId.setLayoutData(gridData);
		this.m_paramId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(" : ");

		// メッセージ情報(コンボボックス)
		this.m_keyInfo = new Combo(parent, SWT.CENTER | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_keyInfo.setLayoutData(gridData);
		this.m_keyInfo.add(JobLinkInheritKeyInfoConstant.STRING_SOURCE_FACILITY_ID);
		this.m_keyInfo.add(JobLinkInheritKeyInfoConstant.STRING_SOURCE_IP_ADDRESS);
		this.m_keyInfo.add(JobLinkInheritKeyInfoConstant.STRING_JOBLINK_MESSAGE_ID);
		this.m_keyInfo.add(JobLinkInheritKeyInfoConstant.STRING_MONITOR_DETAIL_ID);
		this.m_keyInfo.add(JobLinkInheritKeyInfoConstant.STRING_PRIORITY);
		this.m_keyInfo.add(JobLinkInheritKeyInfoConstant.STRING_APPLICATION);
		this.m_keyInfo.add(JobLinkInheritKeyInfoConstant.STRING_MESSAGE);
		this.m_keyInfo.add(JobLinkInheritKeyInfoConstant.STRING_MESSAGE_ORG);
		this.m_keyInfo.add(JobLinkInheritKeyInfoConstant.STRING_EXP_INFO);
		this.m_keyInfo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo combo = (Combo) e.getSource();
				m_expKey.setEditable(combo.getText().equals(JobLinkInheritKeyInfoConstant.STRING_EXP_INFO));
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		this.m_keyInfo.setText(JobLinkInheritKeyInfoConstant.STRING_SOURCE_FACILITY_ID);

		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		label.setLayoutData(gridData);

		// 拡張情報キー(ラベル)
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("extended.info.key"));

		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		label.setLayoutData(gridData);

		// 拡張情報キー(テキスト)
		this.m_expKey = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_expKey.setLayoutData(gridData);
		this.m_expKey.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		m_expKey.setEditable(false);

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(350, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		update();
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を可視化
		if("".equals(this.m_paramId.getText())){
			this.m_paramId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_paramId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_keyInfo.getText())){
			this.m_keyInfo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_keyInfo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_expKey.getEditable() && "".equals(this.m_expKey.getText())){
			this.m_expKey.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_expKey.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 入力値を返します。
	 *
	 * @return 判定情報
	 */
	public JobLinkInheritInfoResponse getInputData() {
		return this.m_inputData;
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
	private JobLinkInheritInfoResponse createInputData() {
		JobLinkInheritInfoResponse info = new JobLinkInheritInfoResponse();

		// ジョブ変数
		if (this.m_paramId.getText() != null && !"".equals((this.m_paramId.getText()).trim())) {
			info.setParamId(this.m_paramId.getText());
		} else {
			this.setValidateResult(Messages.getString("message.hinemos.1"),	
					Messages.getString("message.common.1", new String[]{Messages.getString("job.parameter.name")}));
			return null;
		}

		// メッセージ情報
		if (this.m_keyInfo.getText() != null) {
			if (m_keyInfo.getText().equals(JobLinkInheritKeyInfoConstant.STRING_SOURCE_FACILITY_ID)) {
				info.setKeyInfo(JobLinkInheritInfoResponse.KeyInfoEnum.SOURCE_FACILITY_ID);

			} else if (m_keyInfo.getText().equals(JobLinkInheritKeyInfoConstant.STRING_SOURCE_IP_ADDRESS)) {
				info.setKeyInfo(JobLinkInheritInfoResponse.KeyInfoEnum.SOURCE_IP_ADDRESS);

			} else if (m_keyInfo.getText().equals(JobLinkInheritKeyInfoConstant.STRING_JOBLINK_MESSAGE_ID)) {
				info.setKeyInfo(JobLinkInheritInfoResponse.KeyInfoEnum.JOBLINK_MESSAGE_ID);

			} else if (m_keyInfo.getText().equals(JobLinkInheritKeyInfoConstant.STRING_MONITOR_DETAIL_ID)) {
				info.setKeyInfo(JobLinkInheritInfoResponse.KeyInfoEnum.MONITOR_DETAIL_ID);

			} else if (m_keyInfo.getText().equals(JobLinkInheritKeyInfoConstant.STRING_PRIORITY)) {
				info.setKeyInfo(JobLinkInheritInfoResponse.KeyInfoEnum.PRIORITY);

			} else if (m_keyInfo.getText().equals(JobLinkInheritKeyInfoConstant.STRING_APPLICATION)) {
				info.setKeyInfo(JobLinkInheritInfoResponse.KeyInfoEnum.APPLICATION);

			} else if (m_keyInfo.getText().equals(JobLinkInheritKeyInfoConstant.STRING_MESSAGE)) {
				info.setKeyInfo(JobLinkInheritInfoResponse.KeyInfoEnum.MESSAGE);

			} else if (m_keyInfo.getText().equals(JobLinkInheritKeyInfoConstant.STRING_MESSAGE_ORG)) {
				info.setKeyInfo(JobLinkInheritInfoResponse.KeyInfoEnum.MESSAGE_ORG);

			} else if (m_keyInfo.getText().equals(JobLinkInheritKeyInfoConstant.STRING_EXP_INFO)) {
				info.setKeyInfo(JobLinkInheritInfoResponse.KeyInfoEnum.EXP_INFO);
			}
		} else {
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.common.1", new String[]{Messages.getString("job.joblink.message.info")}));
			return null;
		}

		// 拡張情報キー
		if (this.m_expKey.getEditable()) {
			if (this.m_expKey.getText() != null && !"".equals((this.m_expKey.getText()).trim())) {
				info.setExpKey(this.m_expKey.getText());
			} else {
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.common.1", new String[]{Messages.getString("extended.info.key")}));
				return null;
			}
		} else {
			info.setExpKey("");
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

		JobLinkInheritInfoResponse info = this.m_inputData;
		if(info != null){
			result = true;
		}

		return result;
	}
}
