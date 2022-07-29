/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import org.openapitools.client.model.JobRuntimeParamDetailResponse;
import org.openapitools.client.model.JobRuntimeParamResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PatternConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetRuntimeParameterTableDefine;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParamTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParamTypeMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ジョブ実行契機 ランタイムジョブ変数ダイアログクラスです。
 *
 * @version 5.1.0
 */
public class RuntimeParameterDialog extends CommonDialog {

	/** シェル */
	private Shell m_shell = null;

	/** 変数名用テキスト */
	private Text m_txtParamId = null;
	/** 説明用テキスト */
	private Text m_txtDescription = null;
	/** 種別用コンボボックス */
	private Combo m_cmbType = null;
	/** デフォルト値用テキスト */
	private Text m_txtDefaultValue = null;
	/** 選択候補用テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** 選択候補 追加用ボタン */
	private Button m_btnSelectAdd = null;
	/** 選択候補 変更用ボタン */
	private Button m_btnSelectModify = null;
	/** 選択候補 削除用ボタン */
	private Button m_btnSelectDelete = null;
	/** 選択候補 コピー用ボタン */
	private Button m_btnSelectCopy = null;
	/** 選択候補 上へ用ボタン */
	private Button m_btnSelectUp = null;
	/** 選択候補 下へ用ボタン */
	private Button m_btnSelectDown = null;
	/** 必須有無用チェックボックス */
	private Button m_chkRequiredFlg = null;

	/** ランタイムジョブ変数情報 */
	private JobRuntimeParamResponse m_jobRuntimeParam = null;

	/** ランタイムジョブ変数リスト */
	private Map<String, JobRuntimeParamResponse> m_parentJobRuntimeParamMap = new HashMap<>();
	
	/**
	 * コンストラクタ
	 * 変更時
	 * @param parent
	 * @param paramInfo
	 * @param mode
	 */
	public RuntimeParameterDialog(Shell parent, Map<String, JobRuntimeParamResponse> parentJobRuntimeParamMap,
		JobRuntimeParamResponse jobRuntimeParam){
		super(parent);
		this.m_jobRuntimeParam = jobRuntimeParam;
		this.m_parentJobRuntimeParamMap = parentJobRuntimeParamMap;
	}

	/**
	 * コンストラクタ
	 * 新規作成時
	 * @param parent
	 */
	public RuntimeParameterDialog(Shell parent,
			Map<String, JobRuntimeParamResponse> parentJobRuntimeParamMap){
		super(parent);
		this.m_parentJobRuntimeParamMap = parentJobRuntimeParamMap;
		this.m_jobRuntimeParam = new JobRuntimeParamResponse();
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		m_shell = this.getShell();
		parent.getShell().setText(Messages.getString("dialog.job.add.modify.manual.param"));

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
		composite.setLayout(new GridLayout(3, false));

		// 名前（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("name") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 名前（テキスト）
		this.m_txtParamId = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtParamId", this.m_txtParamId);
		this.m_txtParamId.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtParamId.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		this.m_txtParamId.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		// dummy
		new Label(composite, SWT.NONE);

		// 説明（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("description") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 説明（テキスト）
		this.m_txtDescription = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtDescription", this.m_txtDescription);
		this.m_txtDescription.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtDescription.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_256));
		this.m_txtDescription.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// dummy
		new Label(composite, SWT.NONE);

		// 種別（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("type") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 種別（コンボボックス）
		this.m_cmbType = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_cmbType", this.m_cmbType);
		this.m_cmbType.setLayoutData(new GridData(200,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_cmbType.add(Messages.getString("job.manual.type.input"));
		this.m_cmbType.add(Messages.getString("job.manual.type.radio"));
		this.m_cmbType.add(Messages.getString("job.manual.type.combo"));
		this.m_cmbType.add(Messages.getString("job.manual.type.fixed"));
		this.m_cmbType.select(0);
		this.m_cmbType.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// dummy
		new Label(composite, SWT.NONE);

		// デフォルト値（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("default.value") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// デフォルト値（テキスト）
		this.m_txtDefaultValue = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtDefaultValue", this.m_txtDefaultValue);
		this.m_txtDefaultValue.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtDefaultValue.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_1024));
		this.m_txtDefaultValue.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// dummy
		new Label(composite, SWT.NONE);

		// 選択候補（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("job.manual.select.item") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)label.getLayoutData()).verticalSpan = 6;
		((GridData)label.getLayoutData()).verticalAlignment = SWT.BEGINNING;

		// 選択候補（テーブル）
		Table table = new Table(composite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, "table", table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(210, 135));
		((GridData)table.getLayoutData()).verticalSpan = 6;

		// 選択候補：追加（ボタン）
		this.m_btnSelectAdd = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnSelectAdd", this.m_btnSelectAdd);
		this.m_btnSelectAdd.setText(Messages.getString("add"));
		this.m_btnSelectAdd.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnSelectAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RuntimeParameterSelectionDialog dialog 
					= new RuntimeParameterSelectionDialog(
							m_shell,
							m_jobRuntimeParam.getJobRuntimeParamDetailList(),
							new JobRuntimeParamDetailResponse());
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_jobRuntimeParam.getJobRuntimeParamDetailList().add(dialog.getInputData());
					if (dialog.getDefaultValueSelection()) {
						m_jobRuntimeParam.setValue(
							dialog.getInputData().getParamValue());
					}
					reflectParamDetailInfo();
				}
			}
		});

		// 選択候補：変更（ボタン）
		this.m_btnSelectModify = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnSelectModify", this.m_btnSelectModify);
		this.m_btnSelectModify.setText(Messages.getString("modify"));
		this.m_btnSelectModify.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnSelectModify.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_viewer.getTable().getSelectionIndex() >= 0) {
					int orderNo = (Integer)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(1) - 1;
					RuntimeParameterSelectionDialog dialog 
						= new RuntimeParameterSelectionDialog(
							m_shell,
							m_jobRuntimeParam.getJobRuntimeParamDetailList(),
							m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo),
							(m_jobRuntimeParam.getValue() == null ? false : 
								m_jobRuntimeParam.getValue().equals(
								m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo).getParamValue())));
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_jobRuntimeParam.getJobRuntimeParamDetailList().remove(orderNo);
						m_jobRuntimeParam.getJobRuntimeParamDetailList().add(orderNo, dialog.getInputData());
						if (dialog.getDefaultValueSelection()) {
							m_jobRuntimeParam.setValue(
								dialog.getInputData().getParamValue());
						} else {
							if (m_jobRuntimeParam.getValue() != null
									&& m_jobRuntimeParam.getValue().equals(
									dialog.getInputData().getParamValue())) {
								m_jobRuntimeParam.setValue(null);
							}
						}
						reflectParamDetailInfo();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});

		// 選択候補：削除（ボタン）
		this.m_btnSelectDelete = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnSelectDelete", this.m_btnSelectDelete);
		this.m_btnSelectDelete.setText(Messages.getString("delete"));
		this.m_btnSelectDelete.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnSelectDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_viewer.getTable().getSelectionIndex() >= 0) {
					int orderNo = (Integer)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(1) - 1;
					String detail = m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo).getDescription();
					if (detail == null) {
						detail = "";
					}

					String[] args = { detail };
					if (MessageDialog.openConfirm(
							null,
							Messages.getString("confirmed"),
							Messages.getString("message.job.130", args))) {
						if (m_jobRuntimeParam.getValue() != null
								&& m_jobRuntimeParam.getValue().equals(
										m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo).getParamValue())) {
							m_jobRuntimeParam.setValue(null);
						}
						m_jobRuntimeParam.getJobRuntimeParamDetailList().remove(orderNo);
						reflectParamDetailInfo();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});

		// 選択候補：コピー（ボタン）
		this.m_btnSelectCopy = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnSelectCopy", this.m_btnSelectCopy);
		this.m_btnSelectCopy.setText(Messages.getString("copy"));
		this.m_btnSelectCopy.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnSelectCopy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_viewer.getTable().getSelectionIndex() >= 0) {
					int orderNo = (Integer)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(1) - 1;
					// シェルを取得
					JobRuntimeParamDetailResponse paramDetail = new JobRuntimeParamDetailResponse();
					paramDetail.setDescription(m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo).getDescription());
					paramDetail.setParamValue(m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo).getParamValue());
					RuntimeParameterSelectionDialog dialog 
						= new RuntimeParameterSelectionDialog(
								m_shell,
								m_jobRuntimeParam.getJobRuntimeParamDetailList(),
								paramDetail);
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_jobRuntimeParam.getJobRuntimeParamDetailList().add(dialog.getInputData());
						if (dialog.getDefaultValueSelection()) {
							m_jobRuntimeParam.setValue(
								dialog.getInputData().getParamValue());
						}
						reflectParamDetailInfo();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});

		// 選択候補：上へ（ボタン）
		this.m_btnSelectUp = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnSelectUp", this.m_btnSelectUp);
		this.m_btnSelectUp.setText(Messages.getString("up"));
		this.m_btnSelectUp.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnSelectUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_viewer.getTable().getSelectionIndex() >= 0) {
					int orderNo = (Integer)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(1) - 1;
					if (orderNo > 0) {
						JobRuntimeParamDetailResponse jobRuntimeParamDetail 
							= m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo);
						m_jobRuntimeParam.getJobRuntimeParamDetailList().remove(orderNo);
						m_jobRuntimeParam.getJobRuntimeParamDetailList().add(orderNo-1, jobRuntimeParamDetail);
						reflectParamDetailInfo();
						m_viewer.getTable().setSelection(orderNo-1);
					}
				}
				else{
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});

		// 選択候補：下へ（ボタン）
		this.m_btnSelectDown = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnSelectDown", this.m_btnSelectDown);
		this.m_btnSelectDown.setText(Messages.getString("down"));
		this.m_btnSelectDown.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnSelectDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_viewer.getTable().getSelectionIndex() >= 0) {
					int orderNo = (Integer)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(1) - 1;
					if (orderNo < m_jobRuntimeParam.getJobRuntimeParamDetailList().size() - 1) {
						JobRuntimeParamDetailResponse jobRuntimeParamDetail 
							= m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo);
						m_jobRuntimeParam.getJobRuntimeParamDetailList().remove(orderNo);
						m_jobRuntimeParam.getJobRuntimeParamDetailList().add(orderNo+1, jobRuntimeParamDetail);
						reflectParamDetailInfo();
						m_viewer.getTable().setSelection(orderNo+1);
					}
				}
				else{
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});
		
		// 必須有無（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("job.manual.required") + " : ");
		label.setLayoutData(new GridData(60, SizeConstant.SIZE_LABEL_HEIGHT));

		// 必須有無（チェックボックス）
		this.m_chkRequiredFlg = new Button(composite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_chkRequiredFlg", this.m_chkRequiredFlg);
		this.m_btnSelectDown.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));

		// dummy
		new Label(composite, SWT.NONE);

		this.m_viewer = new CommonTableViewer(table);
		this.m_viewer.createTableColumn(GetRuntimeParameterTableDefine.get(),
				GetRuntimeParameterTableDefine.SORT_COLUMN_INDEX,
				GetRuntimeParameterTableDefine.SORT_ORDER);
		this.m_viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (m_viewer.getTable().getSelectionIndex() >= 0) {
					int orderNo = (Integer)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(1) - 1;
					RuntimeParameterSelectionDialog dialog
						= new RuntimeParameterSelectionDialog(
							m_shell,
							m_jobRuntimeParam.getJobRuntimeParamDetailList(),
							m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo),
							(m_jobRuntimeParam.getValue() == null ? false : 
								m_jobRuntimeParam.getValue().equals(
								m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo).getParamValue())));
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_jobRuntimeParam.getJobRuntimeParamDetailList().remove(orderNo);
						m_jobRuntimeParam.getJobRuntimeParamDetailList().add(orderNo, dialog.getInputData());
						if (dialog.getDefaultValueSelection()) {
							m_jobRuntimeParam.setValue(
									dialog.getInputData().getParamValue());
						} else {
							if (m_jobRuntimeParam.getValue() == null || m_jobRuntimeParam.getValue().equals(
									dialog.getInputData().getParamValue())) {
								m_jobRuntimeParam.setValue(null);
							}
						}
						reflectParamDetailInfo();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});

		// ランタイム変数情報反映
		reflectParamInfo();

		// 更新処理
		update();
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 400);
	}


	/**
	 * ランタイムジョブ変数情報をコンポジットに反映します。
	 *
	 */
	public void reflectParamInfo() {
		if (this.m_jobRuntimeParam != null) {
			// 名前
			if (this.m_jobRuntimeParam.getParamId() != null) {
				this.m_txtParamId.setText(this.m_jobRuntimeParam.getParamId());
			}
			// 説明
			if (this.m_jobRuntimeParam.getDescription() != null) {
				this.m_txtDescription.setText(this.m_jobRuntimeParam.getDescription());
			}
			// 種別
			if (this.m_jobRuntimeParam.getParamType() != null) {
				this.m_cmbType.setText(JobRuntimeParamTypeMessage.typeEnumToString(this.m_jobRuntimeParam.getParamType()));
			}
			// デフォルト値
			if (this.m_jobRuntimeParam.getParamType() != null
					&& this.m_jobRuntimeParam.getParamType() != JobRuntimeParamResponse.ParamTypeEnum.RADIO
					&& this.m_jobRuntimeParam.getParamType() != JobRuntimeParamResponse.ParamTypeEnum.COMBO
					&& this.m_jobRuntimeParam.getValue() != null) {
				this.m_txtDefaultValue.setText(this.m_jobRuntimeParam.getValue());
			}
			// 必須有無
			if (this.m_jobRuntimeParam.getParamType() != null
					&& (this.m_jobRuntimeParam.getParamType() == JobRuntimeParamResponse.ParamTypeEnum.INPUT
					|| this.m_jobRuntimeParam.getParamType() == JobRuntimeParamResponse.ParamTypeEnum.COMBO)
				&& this.m_jobRuntimeParam.getRequiredFlg() != null) {
				this.m_chkRequiredFlg.setSelection(this.m_jobRuntimeParam.getRequiredFlg());
			}
			// 選択候補
			reflectParamDetailInfo();
		}
	}

	/**
	 * ランタイムジョブ変数詳細情報をコンポジットに反映します。
	 *
	 */
	public void reflectParamDetailInfo() {

		// 選択候補
		ArrayList<ArrayList<?>> tableData = new ArrayList<ArrayList<?>>();
		if ((this.m_cmbType.getText().equals(JobRuntimeParamTypeMessage.STRING_RADIO)
				|| this.m_cmbType.getText().equals(JobRuntimeParamTypeMessage.STRING_COMBO))
				&& this.m_jobRuntimeParam.getJobRuntimeParamDetailList() != null
				&& this.m_jobRuntimeParam.getJobRuntimeParamDetailList().size() > 0) {
			int detailIdx = 1;
			if (this.m_cmbType.getText().equals(JobRuntimeParamTypeMessage.STRING_RADIO)) {
				// ラジオボタンではデフォルト値未設定の場合先頭の項目をデフォルト値に設定する
				if (this.m_jobRuntimeParam.getValue() == null
						|| "".equals(this.m_jobRuntimeParam.getValue())) {
					this.m_jobRuntimeParam.setValue(
						this.m_jobRuntimeParam.getJobRuntimeParamDetailList()
						.get(0).getParamValue());
				}
			}
			for (JobRuntimeParamDetailResponse jobRuntimeParamDetail
					: this.m_jobRuntimeParam.getJobRuntimeParamDetailList()) {
				ArrayList<Object> tableLineData = new ArrayList<Object>();
				if (this.m_jobRuntimeParam.getValue() != null
						&& this.m_jobRuntimeParam.getValue().equals(
								jobRuntimeParamDetail.getParamValue())) {
					tableLineData.add("*");
				} else {
					tableLineData.add("");
				}
				tableLineData.add(detailIdx);
				tableLineData.add(jobRuntimeParamDetail.getParamValue());
				tableLineData.add(jobRuntimeParamDetail.getDescription());
				tableData.add(tableLineData);
				detailIdx++;
			}
		}
		this.m_viewer.setInput(tableData);
	}

	/**
	 * ジョブ実行契機情報 更新処理
	 *
	 */
	public void update(){
		// 種別
		Integer type = JobRuntimeParamTypeMessage.stringToType(this.m_cmbType.getText());

		// 必須項目を明示
		if("".equals(this.m_txtParamId.getText())){
			this.m_txtParamId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtParamId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_txtDescription.getText())){
			this.m_txtDescription.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtDescription.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (type == JobRuntimeParamTypeConstant.TYPE_FIXED 
				&& "".equals(this.m_txtDefaultValue.getText())){
			this.m_txtDefaultValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_txtDefaultValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// ボタン等制御
		boolean selectFlg = type == JobRuntimeParamTypeConstant.TYPE_RADIO 
				|| type == JobRuntimeParamTypeConstant.TYPE_COMBO;
		this.m_txtDefaultValue.setEnabled((type == JobRuntimeParamTypeConstant.TYPE_INPUT
				|| type == JobRuntimeParamTypeConstant.TYPE_FIXED));
		this.m_btnSelectAdd.setEnabled(selectFlg);
		this.m_btnSelectModify.setEnabled(selectFlg);
		this.m_btnSelectDelete.setEnabled(selectFlg);
		this.m_btnSelectCopy.setEnabled(selectFlg);
		this.m_btnSelectUp.setEnabled(selectFlg);
		this.m_btnSelectDown.setEnabled(selectFlg);
		this.m_chkRequiredFlg.setEnabled((type == JobRuntimeParamTypeConstant.TYPE_INPUT
				|| type == JobRuntimeParamTypeConstant.TYPE_COMBO));
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 * @since 2.1.0
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 * @since 2.1.0
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
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
		ValidateResult result = null;

		if (this.m_jobRuntimeParam == null) {
			// 新規作成
			this.m_jobRuntimeParam = new JobRuntimeParamResponse();
		}

		// 変数名
		if (this.m_txtParamId.getText() != null
				&& !this.m_txtParamId.getText().equals("")) {
			//アルファベット・数字・'_'・'-'以外は許容しない
			if(!m_txtParamId.getText().matches(PatternConstant.HINEMOS_ID_PATTERN)){
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.60"));
				return result;
			}
			// 重複チェック
			if (isParameterDuplicate(this.m_txtParamId.getText(), this.m_jobRuntimeParam.getParamId())) {
				// 変数名の重複エラー
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.134"));
				return result;
			}
			this.m_jobRuntimeParam.setParamId(this.m_txtParamId.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.19"));
			return result;
		}
		// 説明
		if (this.m_txtDescription.getText() != null
				&& !this.m_txtDescription.getText().equals("")) {
			this.m_jobRuntimeParam.setDescription(this.m_txtDescription.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.128"));
			return result;
		}
		// 種別
		JobRuntimeParamResponse.ParamTypeEnum type = JobRuntimeParamTypeMessage.stringToTypeEnum(this.m_cmbType.getText());
		this.m_jobRuntimeParam.setParamType(type);
		// デフォルト値
		if (type == JobRuntimeParamResponse.ParamTypeEnum.FIXED
				|| type == JobRuntimeParamResponse.ParamTypeEnum.INPUT) {
			if (this.m_txtDefaultValue.getText() != null
					&& !this.m_txtDefaultValue.getText().equals("")) {
				this.m_jobRuntimeParam.setValue(this.m_txtDefaultValue.getText());
			} else {
				if (type == JobRuntimeParamResponse.ParamTypeEnum.FIXED) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					result.setMessage(Messages.getString("message.job.132"));
					return result;
				} else {
					this.m_jobRuntimeParam.setValue(null);
				}
			}
		} else {
			if (this.m_jobRuntimeParam.getJobRuntimeParamDetailList() == null
				|| this.m_jobRuntimeParam.getJobRuntimeParamDetailList().size() == 0) {
				// 選択候補が存在しない場合は初期化
				this.m_jobRuntimeParam.setValue(null);
			} else {
				if (this.m_jobRuntimeParam.getValue() != null
						&& !this.m_jobRuntimeParam.getValue().equals("")) {
					boolean isMatched = false;
					for (JobRuntimeParamDetailResponse paramDetail : this.m_jobRuntimeParam.getJobRuntimeParamDetailList()) {
						if (paramDetail.getParamValue().equals(this.m_jobRuntimeParam.getValue())) {
							isMatched = true;
							break;
						}
					}
					if (!isMatched) {
						// 選択候補に対応するものが存在しない場合は初期化
						this.m_jobRuntimeParam.setValue(null);
					}
				}
				if (type == JobRuntimeParamResponse.ParamTypeEnum.RADIO) {
					// ラジオボタンではデフォルト値未設定の場合先頭の項目をデフォルト値に設定する
					if (this.m_jobRuntimeParam.getValue() == null
							|| this.m_jobRuntimeParam.getValue().equals("")) {
						this.m_jobRuntimeParam.setValue(
							this.m_jobRuntimeParam.getJobRuntimeParamDetailList()
							.get(0).getParamValue());
					}
				}
			}
		}
		// 選択候補
		if (type == JobRuntimeParamResponse.ParamTypeEnum.RADIO
				|| type == JobRuntimeParamResponse.ParamTypeEnum.COMBO) {
			if (this.m_jobRuntimeParam.getJobRuntimeParamDetailList() == null
				|| this.m_jobRuntimeParam.getJobRuntimeParamDetailList().size() == 0) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.133"));
				return result;
			}
		} else {
			// 「入力」「固定」の場合はクリア
			if (this.m_jobRuntimeParam.getJobRuntimeParamDetailList() != null
				&& this.m_jobRuntimeParam.getJobRuntimeParamDetailList().size() != 0) {
				this.m_jobRuntimeParam.getJobRuntimeParamDetailList().clear();
			}
		}

		// 必須有無
		this.m_jobRuntimeParam.setRequiredFlg(this.m_chkRequiredFlg.getSelection());

		return null;
	}

	/**
	 * ランタイムジョブ変数情報を返します。
	 *
	 * @return ジョブ変数情報
	 */
	public JobRuntimeParamResponse getInputData() {
		return this.m_jobRuntimeParam;
	}

	/**
	 * パラメータ情報に重複した値が設定されているか
	 * 
	 * @param newParamId 変更後ランタイムジョブ変数
	 * @param oldParamId 変更前ランタイムジョブ変数
	 * @return true:重複あり, false:重複なし
	 */
	private boolean isParameterDuplicate(String newParamId, String oldParamId) {
		boolean result = false;
		if (m_parentJobRuntimeParamMap == null) {
			// データがない場合は処理終了
			return result;
		}
		if (oldParamId != null && oldParamId.equals(newParamId)) {
			// キーに変更がない場合は処理終了
			return result;
		}
		for (Map.Entry<String, JobRuntimeParamResponse> entry : m_parentJobRuntimeParamMap.entrySet()) {
			if (oldParamId != null && entry.getKey().equals(oldParamId)) {
				continue;
			}
			if (entry.getKey().equals(newParamId)) {
				result = true;
				break;
			}
		}
		return result;
	}
}
