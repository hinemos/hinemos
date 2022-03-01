/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.openapitools.client.model.JobRuntimeParamDetailResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ジョブ実行契機 ランタイムジョブ変数選択候補ダイアログクラスです。
 *
 * @version 5.1.0
 */
public class RuntimeParameterSelectionDialog extends CommonDialog {
	/** 値用テキスト */
	private Text m_txtValue = null;
	/** 説明用テキスト */
	private Text m_txtDescription = null;
	/** デフォルト値用チェックボックス */
	private Button m_chkDefaultValue = null;

	/** ランタイムジョブ変数詳細情報 */
	private JobRuntimeParamDetailResponse m_jobRuntimeParamDetail = null;

	/** ランタイムジョブ変数詳細情報リスト */
	private List<JobRuntimeParamDetailResponse> m_parentJobRuntimeParamDetailList = null;

	/** デフォルト値選択有無 */
	private Boolean m_defaultValueSelect = false;

	/**
	 * コンストラクタ
	 * 変更時
	 * @param parent
	 * @param paramDetailInfo
	 */
	public RuntimeParameterSelectionDialog(
			Shell parent,
			List<JobRuntimeParamDetailResponse> parentJobRuntimeParamDetailList,
			JobRuntimeParamDetailResponse jobRuntimeParamDetail,
			Boolean defaultValueSelect){
		super(parent);
		this.m_jobRuntimeParamDetail = jobRuntimeParamDetail;
		this.m_parentJobRuntimeParamDetailList = parentJobRuntimeParamDetailList;
		this.m_defaultValueSelect = defaultValueSelect;
	}


	/**
	 * コンストラクタ
	 * 新規作成時、コピー時
	 * @param parent
	 * @param paramDetailInfo
	 */
	public RuntimeParameterSelectionDialog(
			Shell parent,
			List<JobRuntimeParamDetailResponse> parentJobRuntimeParamDetailList,
			JobRuntimeParamDetailResponse jobRuntimeParamDetail){
		super(parent);
		this.m_jobRuntimeParamDetail = jobRuntimeParamDetail;
		this.m_parentJobRuntimeParamDetailList = parentJobRuntimeParamDetailList;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		parent.getShell().setText(Messages.getString("dialog.job.add.modify.manual.param.detail"));

		Label label = null;
		
		/**
		 * レイアウト設定
		 * ダイアログ内のベースとなるレイアウトが全てを変更
		 */
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 0;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.marginBottom = 0;
		layout.fill = true;
		parent.setLayout(layout);

		// Composite
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		// 値（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("job.manual.param.detail.value") + " : ");
		label.setLayoutData(new GridData(90, SizeConstant.SIZE_LABEL_HEIGHT));

		// 値（テキスト）
		this.m_txtValue = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtValue", this.m_txtValue);
		this.m_txtValue.setLayoutData(new GridData(250, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtValue.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_1024));
		this.m_txtValue.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 説明（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("description") + " : ");
		label.setLayoutData(new GridData(90, SizeConstant.SIZE_LABEL_HEIGHT));

		// 説明（テキスト）
		this.m_txtDescription = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtDescription", this.m_txtDescription);
		this.m_txtDescription.setLayoutData(new GridData(250, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtDescription.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_1024));
		this.m_txtDescription.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// デフォルト値（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("default.value") + " : ");
		label.setLayoutData(new GridData(90, SizeConstant.SIZE_LABEL_HEIGHT));

		// デフォルト値（チェックボックス）
		this.m_chkDefaultValue = new Button(composite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_chkDefaultValue", this.m_chkDefaultValue);
		this.m_chkDefaultValue.setLayoutData(new GridData(60,
				SizeConstant.SIZE_BUTTON_HEIGHT));

		// 値反映
		reflectParamDetailInfo();

		// 更新処理
		update();
	}


	/**
	 * ランタイムジョブ変数情報をコンポジットに反映します。
	 *
	 */
	public void reflectParamDetailInfo() {

		if (this.m_jobRuntimeParamDetail != null) {
			// 値
			if (this.m_jobRuntimeParamDetail.getParamValue() != null) {
				this.m_txtValue.setText(this.m_jobRuntimeParamDetail.getParamValue());
			}
			// 説明
			if (this.m_jobRuntimeParamDetail.getDescription() != null) {
				this.m_txtDescription.setText(this.m_jobRuntimeParamDetail.getDescription());
			}
			// デフォルト値
			this.m_chkDefaultValue.setSelection(this.m_defaultValueSelect);
		}
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示
		if("".equals(this.m_txtValue.getText())){
			this.m_txtValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_txtDescription.getText())){
			this.m_txtDescription.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtDescription.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
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
	 * 入力値を保持したプロパティを返します。<BR>
	 * プロパティオブジェクトのコピーを返します。
	 *
	 * @return プロパティ
	 *
	 * @see com.clustercontrol.util.PropertyUtil#copy(Property)
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		if (this.m_jobRuntimeParamDetail == null) {
			// 新規作成
			this.m_jobRuntimeParamDetail = new JobRuntimeParamDetailResponse();
		}

		// 値
		if (this.m_txtValue.getText().length() > 0) {
			// 重複チェック
			if (isParameterDetailDuplicate(this.m_txtValue.getText(), this.m_jobRuntimeParamDetail.getParamValue())) {
				// 変数名の重複エラー
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.134"));
				return result;
			}
			this.m_jobRuntimeParamDetail.setParamValue(this.m_txtValue.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.17"));
			return result;
		}

		// 説明
		if (this.m_txtDescription.getText().length() > 0) {
			this.m_jobRuntimeParamDetail.setDescription(this.m_txtDescription.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.128"));
			return result;
		}

		// デフォルト値選択
		if (this.m_chkDefaultValue.getSelection()) {
			this.m_defaultValueSelect = true;
		} else {
			this.m_defaultValueSelect = false;
		}
		
		return null;
	}

	/**
	 * ジョブパラメータ詳細情報を返します。
	 *
	 * @return ジョブ変数情報
	 */
	public JobRuntimeParamDetailResponse getInputData() {
		return this.m_jobRuntimeParamDetail;
	}

	/**
	 * デフォルト値を返します。
	 *
	 * @return デフォルト値
	 */
	public Boolean getDefaultValueSelection() {
		return this.m_defaultValueSelect;
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(400, 170);
	}

	/**
	 * ランタイムジョブ変数詳細情報に重複した値が設定されているか
	 * 
	 * @param newValue 変更後ランタイムジョブ変数詳細情報
	 * @param oldValue 変更前ランタイムジョブ変数詳細情報
	 * @return true:重複あり, false:重複なし
	 */
	private boolean isParameterDetailDuplicate(String newValue, String oldValue) {
		boolean result = false;
		if (this.m_parentJobRuntimeParamDetailList == null) {
			// データがない場合は処理終了
			return result;
		}
		if (oldValue != null && oldValue.equals(newValue)) {
			// キーに変更がない場合は処理終了
		}
		for (JobRuntimeParamDetailResponse jobRuntimeParamDetail 
				: this.m_parentJobRuntimeParamDetailList) {
			if (oldValue != null && jobRuntimeParamDetail.getParamValue().equals(oldValue)) {
				continue;
			}
			if (jobRuntimeParamDetail.getParamValue().equals(newValue)) {
				result = true;
				break;
			}
		}
		return result;
	}
}
