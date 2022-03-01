/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.composite;

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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.InfraManagementParamInfoResponse;

import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.infra.action.GetInfraParameterTableDefine;
import com.clustercontrol.infra.dialog.InfraParameterDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * 環境変数タブ用のコンポジットクラスです。
 *
 * @version 6.1.0
 */
public class InfraParameterComposite extends Composite {

	/** シェル */
	private Shell m_shell = null;

	/** 変数用テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** 変数 追加用ボタン */
	private Button m_btnAdd = null;
	/** 変数 変更用ボタン */
	private Button m_btnModify = null;
	/** 変数 削除用ボタン */
	private Button m_btnDelete = null;
	/** 環境変数変数情報 */
	private Map<String, InfraManagementParamInfoResponse> m_infraManagementParamMap = new HashMap<>();

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
	public InfraParameterComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを構築します。
	 */
	private void initialize() {

		m_shell = this.getShell();

		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 1;
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.fill = true;
		this.setLayout(layout);

		// Composite
		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));
		composite.setLayoutData(new RowData());
		((RowData)composite.getLayoutData()).width = 450;

		// 変数（テーブル）
		Table table = new Table(composite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, "table", table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(420, 230));
		((GridData)table.getLayoutData()).horizontalSpan = 4;
		
		// dummy
		new Label(composite, SWT.NONE)
			.setLayoutData(new GridData(265, SizeConstant.SIZE_LABEL_HEIGHT));

		// 変数：追加（ボタン）
		this.m_btnAdd = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnAdd", this.m_btnAdd);
		this.m_btnAdd.setText(Messages.getString("add"));
		this.m_btnAdd.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InfraParameterDialog dialog 
					= new InfraParameterDialog(m_shell, m_infraManagementParamMap);
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_infraManagementParamMap.put(dialog.getInputData().getParamId(), dialog.getInputData());
					reflectParamInfo();
				}
			}
		});

		// 変数：変更（ボタン）
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
					InfraParameterDialog dialog 
						= new InfraParameterDialog(m_shell, m_infraManagementParamMap,
								m_infraManagementParamMap.get(paramId));
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_infraManagementParamMap.remove(paramId);
						m_infraManagementParamMap.put(
							dialog.getInputData().getParamId(), dialog.getInputData());
						reflectParamInfo();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.infra.param.choose"));
				}
			}
		});

		// 変数：削除（ボタン）
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
							Messages.getString("message.infra.param.delete", args))) {
						m_infraManagementParamMap.remove(paramId);
						reflectParamInfo();
					}
				}
				else{
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.infra.param.choose"));
				}
			}
		});

		this.m_viewer = new CommonTableViewer(table);
		this.m_viewer.createTableColumn(GetInfraParameterTableDefine.get(),
				GetInfraParameterTableDefine.SORT_COLUMN_INDEX,
				GetInfraParameterTableDefine.SORT_ORDER);
		this.m_viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				int order = m_viewer.getTable().getSelectionIndex();
				if (order >= 0) {
					String paramId = (String)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(0);
					InfraParameterDialog dialog 
						= new InfraParameterDialog(m_shell, m_infraManagementParamMap,
								m_infraManagementParamMap.get(paramId));
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_infraManagementParamMap.remove(paramId);
						m_infraManagementParamMap.put(
								dialog.getInputData().getParamId(), dialog.getInputData());
						reflectParamInfo();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.infra.param.choose"));
				}
			}
		});
	}

	/**
	 * 環境構築変数情報をコンポジットに反映します。
	 *
	 */
	public void reflectParamInfo() {
		if (this.m_infraManagementParamMap != null) {
			// 環境構築変数
			ArrayList<ArrayList<?>> tableData = new ArrayList<ArrayList<?>>();
			for (InfraManagementParamInfoResponse infraParam : this.m_infraManagementParamMap.values()) {
				ArrayList<Object> tableLineData = new ArrayList<Object>();
				tableLineData.add(infraParam.getParamId());
				String value = infraParam.getValue();
				if (infraParam.getPasswordFlg()) {
					value = value.replaceAll(".", "*");
				}
				tableLineData.add(value);
				tableLineData.add(infraParam.getPasswordFlg());
				tableLineData.add(infraParam.getDescription());
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
	 * 環境構築変数情報を戻します。
	 * @return 環境構築変数情報
	 */
	public List<InfraManagementParamInfoResponse> getInfraManagementParamList() {
		return new ArrayList<>(this.m_infraManagementParamMap.values());
	}

	/**
	 * 環境構築変数情報を設定します。
	 * @param infraManagementParamList ランタイムジョブ変数情報
	 */
	public void setInfraManagementParamList(List<InfraManagementParamInfoResponse> infraManagementParamList) {
		if (infraManagementParamList == null) {
			this.m_infraManagementParamMap = new HashMap<>();
		} else {
			for (InfraManagementParamInfoResponse infraManagementParam : infraManagementParamList) {
				this.m_infraManagementParamMap.put(infraManagementParam.getParamId(), infraManagementParam);
			}
			reflectParamInfo();
		}
	}
}
