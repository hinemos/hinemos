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
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.JobCommandParamResponse;

import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetCommandParameterTableDefine;
import com.clustercontrol.jobmanagement.dialog.JobCommandParameterDialog;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ジョブコマンド変数のコンポジットクラスです。
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class JobCommandParameterComposite extends Composite {
	/** ジョブ変数用テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** ジョブ変数 追加用ボタン */
	private Button m_btnAdd = null;
	/** ジョブ変数 変更用ボタン */
	private Button m_btnModify = null;
	/** ジョブ変数 削除用ボタン */
	private Button m_btnDelete = null;
	/** ジョブ変数情報 */
	private Map<String, JobCommandParamResponse> m_jobCommandParamMap = new HashMap<>();
	/** シェル */
	private Shell m_shell = null;
	/** 選択アイテム */
	private List<?> m_selectItem = new ArrayList<Object>();

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
	public JobCommandParameterComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
		m_shell = this.getShell();
	}

	/**
	 * コンポジットを構築します。
	 */
	private void initialize() {

		this.setLayout(JobDialogUtil.getParentLayout());

		// ジョブ変数（テーブル）
		Table table = new Table(this, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, "table", table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new RowData(430, 75));

		// ボタン（Composite）
		Composite buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayout(new RowLayout());

		// dummy
		new Label(buttonComposite, SWT.NONE).setLayoutData(new RowData(190, SizeConstant.SIZE_LABEL_HEIGHT));

		// ジョブ変数：追加（ボタン）
		this.m_btnAdd = new Button(buttonComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnAdd", this.m_btnAdd);
		this.m_btnAdd.setText(Messages.getString("add"));
		this.m_btnAdd.setLayoutData(new RowData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JobCommandParameterDialog dialog 
					= new JobCommandParameterDialog(m_shell);
				if (dialog.open() == IDialogConstants.OK_ID) {
					// ジョブ変数
					JobCommandParamResponse info = dialog.getInputData();
					@SuppressWarnings("unchecked")
					ArrayList<Object> list = (ArrayList<Object>) m_viewer.getInput();
					
					if (list == null) {
						list = new ArrayList<Object>();
					} else {
						String paramId = info.getParamId();
						for (Object one : list) {
							@SuppressWarnings("unchecked")
							String name = (String) ((ArrayList<Object>) one).get(0);
							if (paramId.equals(name)) {
								// 変数名の重複エラー
								MessageDialog.openWarning(null, Messages.getString("warning"),
										Messages.getString("message.job.134"));
								return;
							}
						}
						
						ArrayList<Object> tableLineData = converJobCommandParam(info);
						list.add(tableLineData);
					}
					m_viewer.setInput(list);
				}
			}
		});

		// ジョブ変数：変更（ボタン）
		this.m_btnModify = new Button(buttonComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnModify", this.m_btnModify);
		this.m_btnModify.setText(Messages.getString("modify"));
		this.m_btnModify.setLayoutData(new RowData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnModify.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = m_viewer.getTable().getSelectionIndex();
				if (order >= 0) {
					JobCommandParameterDialog dialog 
						= new JobCommandParameterDialog(m_shell, createJobCommandParam());
					@SuppressWarnings("unchecked")
					ArrayList<Object> objList = (ArrayList<Object>) m_viewer.getTable().getSelection()[0].getData();
					if (dialog.open() == IDialogConstants.OK_ID) {
							@SuppressWarnings("unchecked")
							ArrayList<Object> list = (ArrayList<Object>) m_viewer.getInput();
							ArrayList<Object> info = converJobCommandParam(dialog.getInputData());
							list.remove(objList);
							list.add(info);
	
							m_selectItem = null;
							m_viewer.setInput(list);
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
		this.m_btnDelete = new Button(buttonComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnDelete", this.m_btnDelete);
		this.m_btnDelete.setText(Messages.getString("delete"));
		this.m_btnDelete.setLayoutData(new RowData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = m_viewer.getTable().getSelectionIndex();
				if (order >= 0) {
					@SuppressWarnings("unchecked")
					ArrayList<Object> objList = (ArrayList<Object>) m_viewer.getTable().getSelection()[0].getData();
					@SuppressWarnings("unchecked")
					ArrayList<Object> list = (ArrayList<Object>) m_viewer.getInput();
					list.remove(objList);

					m_selectItem = null;
					m_viewer.setInput(list);
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
				
			}
		});

		this.m_viewer = new CommonTableViewer(table);
		this.m_viewer.createTableColumn(GetCommandParameterTableDefine.get(),
				GetCommandParameterTableDefine.SORT_COLUMN_INDEX,
				GetCommandParameterTableDefine.SORT_ORDER);
		this.m_viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (m_btnAdd.isEnabled()) {
					int order = m_viewer.getTable().getSelectionIndex();
					if (order >= 0) {
						
						JobCommandParameterDialog dialog 
							= new JobCommandParameterDialog(m_shell, createJobCommandParam());
						@SuppressWarnings("unchecked")
						ArrayList<Object> objList = (ArrayList<Object>) m_viewer.getTable().getSelection()[0].getData();
						if (dialog.open() == IDialogConstants.OK_ID) {
								@SuppressWarnings("unchecked")
								ArrayList<Object> list = (ArrayList<Object>) m_viewer.getInput();
								ArrayList<Object> info = converJobCommandParam(dialog.getInputData());
								list.remove(objList);
								list.add(info);
		
								m_selectItem = null;
								m_viewer.setInput(list);
						}
					} else {
						MessageDialog.openWarning(
								null,
								Messages.getString("warning"),
								Messages.getString("message.job.129"));
					}
				}
			}
		});
	}

	/**
	 * ジョブ変数をコンポジットに反映します。
	 *
	 */
	public void reflectParamInfo() {
		if (this.m_jobCommandParamMap != null) {
			// ジョブ変数
			ArrayList<ArrayList<?>> tableData = new ArrayList<ArrayList<?>>();
			for (JobCommandParamResponse jobCommandParam : this.m_jobCommandParamMap.values()) {
				ArrayList<Object> tableLineData = new ArrayList<Object>();
				tableLineData.add(jobCommandParam.getParamId());
				tableLineData.add(jobCommandParam.getValue());
				if (jobCommandParam.getJobStandardOutputFlg()) {
					tableLineData.add(Messages.getString("monitor.http.scenario.page.obtain.from.current.page.valid"));
				} else {
					tableLineData.add(Messages.getString("monitor.http.scenario.page.obtain.from.current.page.invalid"));
				}
				tableData.add(tableLineData);
			}
			this.m_viewer.setInput(tableData);
		}
	}

	/**
	 * コンポジットの情報から、入力内容をチェックする。
	 *
	 * @return 入力値の検証結果
	 */
	public ValidateResult validateJobCommandParam() {

		reflectJobParamMap();

		return null;
	}

	/**
	 * コンポジットの情報から、ジョブ変数情報を作成します。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobParameterInfo
	 */
	public void reflectJobParamMap() {

		m_jobCommandParamMap =  new HashMap<>();
		
		//パラメータ取得
		ArrayList<?> tableData = (ArrayList<?>) m_viewer.getInput();
		if (tableData != null) {
			for (int i = 0; i < tableData.size(); i++) {
				ArrayList<?> tableLineData = (ArrayList<?>) tableData.get(i);
				JobCommandParamResponse info = new JobCommandParamResponse();
				info.setParamId((String)tableLineData.get(
						GetCommandParameterTableDefine.PARAM_ID));
				info.setValue((String)tableLineData.get(
						GetCommandParameterTableDefine.VALUE));
				info.setJobStandardOutputFlg(Messages.getString("monitor.http.scenario.page.obtain.from.current.page.valid").equals((String)tableLineData.get(
						GetCommandParameterTableDefine.JOB_STANDARD_OUTPUT)));
				this.m_jobCommandParamMap.put(info.getParamId(), info);
			}
		}
	}

	/**
	 * ジョブ変数を設定します。
	 *
	 * @param paramList ジョブ変数情報のリスト
	 */
	public void setJobCommandParamMap(Map<String, JobCommandParamResponse> jobCommandParamMap) {
		m_jobCommandParamMap = jobCommandParamMap;
	}

	/**
	 * ジョブ変数のリストを返します。
	 *
	 * @return ジョブ変数情報のリスト
	 */
	public Map<String, JobCommandParamResponse> getJobCommandParamMap() {
		return m_jobCommandParamMap;
	}

	/**
	 * 選択アイテムを返します。
	 *
	 * @return 選択アイテム
	 */
	public List<?> getSelectItem() {
		return m_selectItem;
	}

	/**
	 * 選択アイテムを設定します。
	 *
	 * @param selectItem 選択アイテム
	 */
	public void setSelectItem(List<?> selectItem) {
		m_selectItem = selectItem;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_btnAdd.setEnabled(enabled);
		m_btnModify.setEnabled(enabled);
		m_btnDelete.setEnabled(enabled);
	}

	/**
	 * ジョブ変数情報をコンポジット向けの情報に変換する。
	 * 
	 * @param info ジョブ変数情報
	 * @return コンポジット向けデータ
	 */
	private ArrayList<Object> converJobCommandParam(JobCommandParamResponse info) {
		ArrayList<Object> tableLineData = new ArrayList<Object>();
		tableLineData.add(info.getParamId());
		tableLineData.add(info.getValue());
		if (info.getJobStandardOutputFlg()) {
			tableLineData.add(
					Messages.getString("monitor.http.scenario.page.obtain.from.current.page.valid"));
		} else {
			tableLineData.add(
					Messages.getString("monitor.http.scenario.page.obtain.from.current.page.invalid"));
		}
		return tableLineData;
	}

	
	/**
	 * コンポジットの情報から、ジョブ変数情報を作成します。
	 *
	 * @return ジョブ変数情報
	 */
	private JobCommandParamResponse createJobCommandParam() {

		//パラメータ取得
		ArrayList<?> tableLineData = ((ArrayList<?>)m_viewer.getTable()
				.getSelection()[0].getData());
		
		JobCommandParamResponse info = new JobCommandParamResponse();
		if (tableLineData != null) {
			info.setParamId((String) tableLineData.get(GetCommandParameterTableDefine.PARAM_ID));
			info.setValue((String) tableLineData.get(GetCommandParameterTableDefine.VALUE));
			info.setJobStandardOutputFlg(Messages.getString("monitor.http.scenario.page.obtain.from.current.page.valid")
					.equals((String) tableLineData.get(GetCommandParameterTableDefine.JOB_STANDARD_OUTPUT)));
		}

		return info;
	}
}
