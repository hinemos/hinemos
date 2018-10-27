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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import com.clustercontrol.jobmanagement.action.GetEnvVariableTableDefine;
import com.clustercontrol.jobmanagement.composite.action.EnvVariableSelectionChangedListener;
import com.clustercontrol.jobmanagement.dialog.EnvVariableDialog;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.jobmanagement.JobEnvVariableInfo;

/**
 * 環境変数のコンポジットクラスです。
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class EnvVariableComposite extends Composite {
	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** 追加ボタン */
	private Button m_createCondition = null;
	/** 変更ボタン */
	private Button m_modifyCondition = null;
	/** 削除ボタン */
	private Button m_deleteCondition = null;
	/** 環境変数のリスト */
	private List<JobEnvVariableInfo> m_envVariableList = null;
	/** シェル */
	private Shell m_shell = null;
	/** 選択アイテム */
	private List<?> m_selectItem = new ArrayList<Object>();
	/** 初期時フラグ(初期時はデフォルトパラメータを投入する。) */
	private boolean initFlag;

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
	public EnvVariableComposite(Composite parent, int style, boolean initFlag) {
		super(parent, style);
		initialize();
		m_shell = this.getShell();
		this.initFlag = initFlag;
	}

	/**
	 * コンポジットを構築します。
	 */
	private void initialize() {

		this.setLayout(JobDialogUtil.getParentLayout());

		// 一覧（テーブル）
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
		new Label(buttonComposite, SWT.NONE)
			.setLayoutData(new RowData(190, SizeConstant.SIZE_LABEL_HEIGHT));

		// ボタン：追加（ボタン）
		this.m_createCondition = new Button(buttonComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_createCondition", this.m_createCondition);
		this.m_createCondition.setText(Messages.getString("add"));
		this.m_createCondition.setLayoutData(new RowData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_createCondition.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EnvVariableDialog dialog = new EnvVariableDialog(m_shell);
				if (dialog.open() == IDialogConstants.OK_ID) {
					ArrayList<?> info = dialog.getInputData();
					@SuppressWarnings("unchecked")
					ArrayList<Object> list = (ArrayList<Object>) m_viewer.getInput();
					if (list == null) {
						list = new ArrayList<Object>();
					}else{
						// Check if environment variable name already exists
						for( Object one : list ){
							@SuppressWarnings("unchecked")
							String name = (String)((ArrayList<Object>)one).get(0);
							if( name.equals( info.get(0) ) ){
								MessageDialog.openError( null, Messages.getString("message.hinemos.1"), Messages.getString("message.hinemos.10", new Object[]{Messages.getString("job.environment.variable"), name}) );
								return;
							}
						}
					}
					// Finally, add it
					list.add(info);
					m_viewer.setInput(list);
				}
			}
		});

		// ボタン：変更（ボタン）
		this.m_modifyCondition = new Button(buttonComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_modifyCondition", this.m_modifyCondition);
		this.m_modifyCondition.setText(Messages.getString("modify"));
		this.m_modifyCondition.setLayoutData(new RowData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_modifyCondition.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EnvVariableDialog dialog = new EnvVariableDialog(m_shell);
				@SuppressWarnings("unchecked")
				ArrayList<Object> objList = m_selectItem.isEmpty() ? null : (ArrayList<Object>)m_selectItem.get(0);
				if (objList != null) {
					dialog.setInputData(objList);
					if (dialog.open() == IDialogConstants.OK_ID) {
						ArrayList<?> info = dialog.getInputData();
						@SuppressWarnings("unchecked")
						ArrayList<Object> list = (ArrayList<Object>) m_viewer.getInput();

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

		// ボタン：削除（ボタン）
		this.m_deleteCondition = new Button(buttonComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_deleteCondition", this.m_deleteCondition);
		this.m_deleteCondition.setText(Messages.getString("delete"));
		this.m_deleteCondition.setLayoutData(new RowData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_deleteCondition.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ArrayList<?> list = (ArrayList<?>) m_viewer.getInput();
				if (m_selectItem != null && m_selectItem.size() > 0) {
					for(Object obj : m_selectItem) {
						if(obj instanceof ArrayList) {
							@SuppressWarnings("unchecked")
							ArrayList<Object> objList =  (ArrayList<Object>)obj;
							list.remove(objList);
							m_selectItem = new ArrayList<Object>();
							m_viewer.setInput(list);
						}
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});

		this.m_viewer = new CommonTableViewer(table);
		this.m_viewer.createTableColumn(GetEnvVariableTableDefine.get(),
				GetEnvVariableTableDefine.SORT_COLUMN_INDEX,
				GetEnvVariableTableDefine.SORT_ORDER);

		this.m_viewer.addSelectionChangedListener(
				new EnvVariableSelectionChangedListener(this));
	}

	/**
	 * 環境変数をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobParameterInfo
	 */
	public void reflectEnvVariableInfo() {
		if (!initFlag) {
			//パラメータ設定
			ArrayList<ArrayList<?>> tableData = new ArrayList<ArrayList<?>>();
			for (int i = 0; i < m_envVariableList.size(); i++) {
				JobEnvVariableInfo info = m_envVariableList.get(i);
				ArrayList<Object> tableLineData = new ArrayList<Object>();
				tableLineData.add(info.getEnvVariableId());
				tableLineData.add(info.getValue());
				tableLineData.add(info.getDescription());
				tableData.add(tableLineData);
			}
			m_viewer.setInput(tableData);
		}
	}

	/**
	 * 環境変数を設定します。
	 *
	 * @param paramList 環境変数情報のリスト
	 */
	public void setEnvVariableList(List<JobEnvVariableInfo> envVariableList) {
		m_envVariableList = envVariableList;
	}

	/**
	 * 環境変数のリストを返します。
	 *
	 * @return 環境変数情報のリスト
	 */
	public List<JobEnvVariableInfo> getEnvVariableList() {
		return m_envVariableList;
	}

	/**
	 * コンポジットの情報から、環境変数を作成します。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobParameterInfo
	 */
	public ValidateResult createEnvVariableInfo() {

		//パラメータ情報のインスタンスを作成・取得
		m_envVariableList = new ArrayList<JobEnvVariableInfo>();

		//パラメータ取得
		ArrayList<?> tableData = (ArrayList<?>) m_viewer.getInput();
		HashMap<String, Object> map = new HashMap<String, Object>();
		if (tableData != null) {
			for (int i = 0; i < tableData.size(); i++) {
				ArrayList<?> tableLineData = (ArrayList<?>) tableData.get(i);
				JobEnvVariableInfo info = new JobEnvVariableInfo();
				info.setEnvVariableId((String)tableLineData.get(
						GetEnvVariableTableDefine.ENV_VARIABLE_ID));
				info.setValue((String)tableLineData.get(
						GetEnvVariableTableDefine.VALUE));
				info.setDescription((String)tableLineData.get(
						GetEnvVariableTableDefine.DESCRIPTION));

				//重複チェック
				Integer checkValue = (Integer) map.get(info.getEnvVariableId());
				if (checkValue == null) {
					m_envVariableList.add(info);
					map.put(info.getEnvVariableId(), 0);
				}
			}
		}

		return null;
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
		m_modifyCondition.setEnabled(enabled);
		m_createCondition.setEnabled(enabled);
		m_deleteCondition.setEnabled(enabled);
	}
}
