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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobLinkExpInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;

/**
 * 拡張情報[作成・変更]ダイアログクラス<BR>
 *
 */
public class JobLinkExpAddDialog extends CommonDialog {

	/** 入力値を保持するオブジェクト **/
	private JobLinkExpInfoResponse m_inputData = null;

	/** キー **/
	private Text m_key = null;

	/** 値 **/
	private Text m_value = null;

	/**
	 * インスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public JobLinkExpAddDialog(Shell parent) {
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
		Label label = null;

		// タイトル
		shell.setText(Messages.getString("dialog.job.add.joblink.exp"));

		// レイアウト
		GridLayout layout = new GridLayout(4, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		// キー(ラベル)
		label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(40, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("key") + " : ");
		// キー
		this.m_key = new Text(parent, SWT.BORDER | SWT.LEFT);
		m_key.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)m_key.getLayoutData()).horizontalAlignment = GridData.FILL;
		this.m_key.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_128));
		this.m_key.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 値(ラベル)
		label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(40, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)label.getLayoutData()).horizontalAlignment = SWT.RIGHT;
		label.setText(Messages.getString("value") + " : ");
		// 値
		this.m_value = new Text(parent, SWT.BORDER | SWT.LEFT);
		m_value.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)m_value.getLayoutData()).horizontalAlignment = GridData.FILL;
		this.m_value.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(400, shell.getSize().y));

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
		if("".equals(this.m_key.getText())){
			this.m_key.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_key.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 入力値を保持した文字列監視の判定情報を返します。
	 *
	 * @return 拡張情報
	 */
	public JobLinkExpInfoResponse getInputData() {
		return this.m_inputData;
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
		ValidateResult result = null;

		// 入力値生成
		this.m_inputData = new JobLinkExpInfoResponse();

		// キー
		if (this.m_key.getText() != null
				&& !"".equals((this.m_key.getText()).trim())) {
			this.m_inputData.setKey(this.m_key.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.common.1", 
					new String[]{Messages.getString("key")}));
			return result;
		}

		// 値
		if (this.m_value.getText() != null
				&& !"".equals((this.m_value.getText()).trim())) {
			this.m_inputData.setValue(this.m_value.getText());
		}
		return null;
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
	 * 入力値の判定を行います。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		JobLinkExpInfoResponse info = this.m_inputData;
		if(info != null){
			result = true;
		}
		return result;
	}
}
