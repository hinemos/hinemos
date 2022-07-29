/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import org.openapitools.client.model.JobRuntimeParamDetailResponse;
import org.openapitools.client.model.JobRuntimeParamResponse;

import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.jobmanagement.action.GetJobKickParameterTableDefine;
import com.clustercontrol.jobmanagement.dialog.RuntimeParameterDialog;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ランタイムジョブ変数タブ用のコンポジットクラスです。
 *
 * @version 5.1.0
 */
public class JobKickParamComposite extends Composite {

	/** シェル */
	private Shell m_shell = null;

	/** ジョブ変数用テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** ジョブ変数 追加用ボタン */
	private Button m_btnAdd = null;
	/** ジョブ変数 変更用ボタン */
	private Button m_btnModify = null;
	/** ジョブ変数 削除用ボタン */
	private Button m_btnDelete = null;

	/** ランタイムジョブ変数情報 */
	private Map<String, JobRuntimeParamResponse> m_jobRuntimeParamMap = new HashMap<>();

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
	public JobKickParamComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを構築します。
	 */
	private void initialize() {

		m_shell = this.getShell();

		this.setLayout(JobDialogUtil.getParentLayout());

		// Composite
		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));
		composite.setLayoutData(new RowData());
		((RowData)composite.getLayoutData()).width = 525;

		// ジョブ変数（テーブル）
		Table table = new Table(composite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, "table", table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(465, 150));
		((GridData)table.getLayoutData()).horizontalSpan = 4;
		
		// dummy
		new Label(composite, SWT.NONE)
			.setLayoutData(new GridData(325, SizeConstant.SIZE_LABEL_HEIGHT));

		// ジョブ変数：追加（ボタン）
		this.m_btnAdd = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnAdd", this.m_btnAdd);
		this.m_btnAdd.setText(Messages.getString("add"));
		this.m_btnAdd.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RuntimeParameterDialog dialog 
					= new RuntimeParameterDialog(m_shell, m_jobRuntimeParamMap);
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_jobRuntimeParamMap.put(dialog.getInputData().getParamId(), dialog.getInputData());
					reflectParamInfo();
				}
			}
		});

		// ジョブ変数：変更（ボタン）
		this.m_btnModify = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnModify", this.m_btnModify);
		this.m_btnModify.setText(Messages.getString("modify"));
		this.m_btnModify.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnModify.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = m_viewer.getTable().getSelectionIndex();
				if (order >= 0) {
					String paramId = (String)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(0);
					RuntimeParameterDialog dialog 
						= new RuntimeParameterDialog(m_shell, m_jobRuntimeParamMap,
								copyJobRuntimeParam(m_jobRuntimeParamMap.get(paramId)));
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_jobRuntimeParamMap.remove(paramId);
						m_jobRuntimeParamMap.put(
							dialog.getInputData().getParamId(), dialog.getInputData());
						reflectParamInfo();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});

		// ジョブ変数：削除（ボタン）
		this.m_btnDelete = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnDelete", this.m_btnDelete);
		this.m_btnDelete.setText(Messages.getString("delete"));
		this.m_btnDelete.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = m_viewer.getTable().getSelectionIndex();
				if (order >= 0) {
					String paramId = (String)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(0);
					if (paramId == null) {
						paramId = "";
					}

					String[] args = { paramId };
					if (MessageDialog.openConfirm(
							null,
							Messages.getString("confirmed"),
							Messages.getString("message.job.130", args))) {
						m_jobRuntimeParamMap.remove(paramId);
						reflectParamInfo();
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

		this.m_viewer = new CommonTableViewer(table);
		this.m_viewer.createTableColumn(GetJobKickParameterTableDefine.get(),
				GetJobKickParameterTableDefine.SORT_COLUMN_INDEX,
				GetJobKickParameterTableDefine.SORT_ORDER);
		this.m_viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				int order = m_viewer.getTable().getSelectionIndex();
				if (order >= 0) {
					String paramId = (String)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(0);
					RuntimeParameterDialog dialog 
						= new RuntimeParameterDialog(m_shell, m_jobRuntimeParamMap,
								copyJobRuntimeParam(m_jobRuntimeParamMap.get(paramId)));
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_jobRuntimeParamMap.remove(paramId);
						m_jobRuntimeParamMap.put(
								dialog.getInputData().getParamId(), dialog.getInputData());
						reflectParamInfo();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});
	}

	/**
	 * ランタイムジョブ変数情報をコンポジットに反映します。
	 *
	 */
	public void reflectParamInfo() {
		if (this.m_jobRuntimeParamMap != null) {
			// ランタイムジョブ変数
			ArrayList<ArrayList<?>> tableData = new ArrayList<ArrayList<?>>();
			for (JobRuntimeParamResponse jobRuntimeParam : this.m_jobRuntimeParamMap.values()) {
				ArrayList<Object> tableLineData = new ArrayList<Object>();
				tableLineData.add(jobRuntimeParam.getParamId());
				tableLineData.add(jobRuntimeParam.getParamType());
				tableLineData.add(jobRuntimeParam.getValue());
				tableLineData.add(jobRuntimeParam.getDescription());
				tableData.add(tableLineData);
			}
			this.m_viewer.setInput(tableData);
		}
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 処理なし
	}

	/**
	 * ランタイムジョブ変数情報を戻します。
	 * @return ランタイムジョブ変数情報
	 */
	public List<JobRuntimeParamResponse> getJobRuntimeParamList() {
		return new ArrayList<>(this.m_jobRuntimeParamMap.values());
	}

	/**
	 * ランタイムジョブ変数情報を設定します。
	 * @param jobRuntimeParamList ランタイムジョブ変数情報
	 */
	public void setJobRuntimeParamList(List<JobRuntimeParamResponse> jobRuntimeParamList) {
		if (jobRuntimeParamList == null) {
			this.m_jobRuntimeParamMap = new HashMap<>();
		} else {
			for (JobRuntimeParamResponse jobRuntimeParam : jobRuntimeParamList) {
				this.m_jobRuntimeParamMap.put(jobRuntimeParam.getParamId(), copyJobRuntimeParam(jobRuntimeParam));
			}
			reflectParamInfo();
		}
	}

	/**
	 * ランタイムジョブ変数情報をコピーします。
	 * @param jobRuntimeParam ランタイムジョブ変数情報
	 */
	private JobRuntimeParamResponse copyJobRuntimeParam(JobRuntimeParamResponse fromParam) {

		JobRuntimeParamResponse param = new JobRuntimeParamResponse();
		if (fromParam != null) {
			param.setDescription(fromParam.getDescription());
			param.setParamId(fromParam.getParamId());
			param.setParamType(fromParam.getParamType());
			param.setRequiredFlg(fromParam.getRequiredFlg());
			param.setValue(fromParam.getValue());
			for (JobRuntimeParamDetailResponse jobRuntimeParamDetail : fromParam.getJobRuntimeParamDetailList()) {
				JobRuntimeParamDetailResponse detailParam = new JobRuntimeParamDetailResponse();
				detailParam.setDescription(jobRuntimeParamDetail.getDescription());
				detailParam.setParamValue(jobRuntimeParamDetail.getParamValue());
				param.getJobRuntimeParamDetailList().add(detailParam);
			}
		}
		return param;
	}
}
