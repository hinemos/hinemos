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

import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetCommandParameterTableDefine;
import com.clustercontrol.jobmanagement.dialog.JobCommandParameterDialog;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.jobmanagement.JobCommandParam;

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
	private Map<String, JobCommandParam> m_jobCommandParamMap = new HashMap<>();
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
					if (isParameterDuplicate(dialog.getInputData().getParamId())){
						// 変数名の重複エラー
						MessageDialog.openWarning(
								null,
								Messages.getString("warning"),
								Messages.getString("message.job.134"));
					} else {
						m_jobCommandParamMap.put(dialog.getInputData().getParamId(), dialog.getInputData());
						reflectParamInfo();
					}
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
					String paramId = (String)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(0);
					JobCommandParameterDialog dialog 
						= new JobCommandParameterDialog(m_shell, m_jobCommandParamMap.get(paramId));
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_jobCommandParamMap.put(paramId, (JobCommandParam)dialog.getInputData());
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
		this.m_btnDelete = new Button(buttonComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnDelete", this.m_btnDelete);
		this.m_btnDelete.setText(Messages.getString("delete"));
		this.m_btnDelete.setLayoutData(new RowData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
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
					m_jobCommandParamMap.remove(paramId);
					reflectParamInfo();
				} else{
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
						String paramId = (String)((ArrayList<?>)m_viewer.getTable()
								.getSelection()[0].getData()).get(0);
						JobCommandParameterDialog dialog 
							= new JobCommandParameterDialog(m_shell, m_jobCommandParamMap.get(paramId));
						if (dialog.open() == IDialogConstants.OK_ID) {
							m_jobCommandParamMap.put(paramId, dialog.getInputData());
							reflectParamInfo();
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
			for (JobCommandParam jobCommandParam : this.m_jobCommandParamMap.values()) {
				ArrayList<Object> tableLineData = new ArrayList<Object>();
				tableLineData.add(jobCommandParam.getParamId());
				tableLineData.add(jobCommandParam.getValue());
				if (jobCommandParam.isJobStandardOutputFlg()) {
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

		reflectParamInfo();

		return null;
	}

	/**
	 * ジョブ変数を設定します。
	 *
	 * @param paramList ジョブ変数情報のリスト
	 */
	public void setJobCommandParamMap(Map<String, JobCommandParam> jobCommandParamMap) {
		m_jobCommandParamMap = jobCommandParamMap;
	}

	/**
	 * ジョブ変数のリストを返します。
	 *
	 * @return ジョブ変数情報のリスト
	 */
	public Map<String, JobCommandParam> getJobCommandParamMap() {
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
	 * パラメータ情報に重複した値が設定されているか
	 * 
	 * @param paramId
	 * @return true:重複あり, false:重複なし
	 */
	private boolean isParameterDuplicate(String paramId) {
		boolean result = false;
		if (this.m_viewer != null
			&& this.m_jobCommandParamMap.containsKey(paramId)) {
				result = true;
		}
		return result;
	}

}
