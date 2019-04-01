/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.composite;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.infra.action.GetInfraFileReplaceSettingTableDefine;
import com.clustercontrol.infra.dialog.ReplaceTextDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.infra.FileTransferVariableInfo;

/**
 * ファイル配布モジュール作成時の置換設定用コンポジットクラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class FileReplaceSettingComposite extends Composite {
	
	// ログ
	private static Log m_log = LogFactory.getLog(FileReplaceSettingComposite.class);

	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** 追加ボタン */
	private Button m_createCondition = null;
	/** 変更ボタン */
	private Button m_modifyCondition = null;
	/** 削除ボタン */
	private Button m_deleteCondition = null;
	/** シェル */
	private Shell m_shell = null;
	/** 選択アイテム */
	private List<Object> m_selectItem = null;

	private List<FileTransferVariableInfo> m_inputData = new ArrayList<FileTransferVariableInfo>();

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
	public FileReplaceSettingComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
		m_shell = this.getShell();
	}

	/**
	 * コンポジットを構築します。
	 */
	private void initialize() {
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 12;
		this.setLayout(layout);
		
		Table table = new Table(this, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 100;
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(gridData);
		
		// 空白
		Label label = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		
		m_createCondition = new Button(this, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_createCondition.setText(Messages.getString("add"));
		m_createCondition.setLayoutData(gridData);
		m_createCondition.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				ReplaceTextDialog dialog = new ReplaceTextDialog(m_shell);
				if (dialog.open() == IDialogConstants.OK_ID) {
					List<?> info = dialog.getInputData();
					List<Object> list = (ArrayList<Object>) m_viewer.getInput();
					if (list == null) {
						list = new ArrayList<Object>();
					}
					list.add(info);
					m_viewer.setInput(list);
				}
			}
		});

		//パラメータの変更
		m_modifyCondition = new Button(this, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		WidgetTestUtil.setTestId(this, "modifycondition", m_modifyCondition);
		m_modifyCondition.setText(Messages.getString("modify"));
		m_modifyCondition.setLayoutData(gridData);
		m_modifyCondition.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredSelection selection = (StructuredSelection) m_viewer.getSelection();
				if(selection != null && selection.size() > 0){
					m_selectItem = (List<Object>) selection.getFirstElement();
				} else {
					m_selectItem = null;
				}
				
				if (m_selectItem != null) {
					ReplaceTextDialog dialog = new ReplaceTextDialog(m_shell, m_selectItem);
					if (dialog.open() == IDialogConstants.OK_ID) {
						List<?> info = dialog.getInputData();
						List<Object> list = (ArrayList<Object>) m_viewer.getInput();
						list.remove(m_selectItem);
						list.add(info);
						m_selectItem = null;
						m_viewer.setInput(list);
					}
				} else {

				}
			}
		});

		//パラメータの削除
		m_deleteCondition = new Button(this, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_deleteCondition.setText(Messages.getString("delete"));
		m_deleteCondition.setLayoutData(gridData);
		m_deleteCondition.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredSelection selection = (StructuredSelection) m_viewer.getSelection();
				if(selection != null && selection.size() > 0){
					m_selectItem = (List<Object>) selection.getFirstElement();
				} else {
					m_selectItem = null;
				}
				if (m_selectItem != null) {
					List<Object> list = (ArrayList<Object>) m_viewer.getInput();
					boolean b = list.remove(m_selectItem);
					m_log.debug("remove " + b);
					m_selectItem = null;
					m_viewer.setInput(list);
				} else {
					m_log.debug("m_selectItem is null.");
				}
			}
		});

		m_viewer = new CommonTableViewer(table);
		m_viewer.createTableColumn(GetInfraFileReplaceSettingTableDefine.get(),
				GetInfraFileReplaceSettingTableDefine.SORT_COLUMN_INDEX,
				GetInfraFileReplaceSettingTableDefine.SORT_ORDER);

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
	public void setSelectItem(List<Object> selectItem) {
		m_selectItem = selectItem;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		// super.setEnabled(enabled); // スクロールバーを動かせるように、ここはコメントアウト
		m_createCondition.setEnabled(enabled);
		m_modifyCondition.setEnabled(enabled);
		m_deleteCondition.setEnabled(enabled);
	}

	public List<FileTransferVariableInfo> getInputData() {
		@SuppressWarnings("unchecked")
		List<List<Object>> list = (List<List<Object>>) m_viewer.getInput();
		m_inputData = new ArrayList<>();
		// ここは不要なはず。暫定的な実装を用意しておく。
		if (list == null) {
			list = new ArrayList<>();
		}
		for(List<Object> item: list){
			FileTransferVariableInfo info = new FileTransferVariableInfo();
			info.setName((String)item.get(GetInfraFileReplaceSettingTableDefine.SEARCH_WORD));
			info.setValue((String)item.get(GetInfraFileReplaceSettingTableDefine.REPLACEMENT_WORD));
			m_inputData.add(info);
		}
		return m_inputData;
	}

	public void setInputData(List<FileTransferVariableInfo> inputData) {
		this.m_inputData = inputData;
		List<Object> list = new ArrayList<Object>();
		for(FileTransferVariableInfo info: inputData){
			List<Object> item = new ArrayList<Object>();
			item.add(info.getName());
			item.add(info.getValue());
			item.add(""); // dummy
			list.add(item);
		}
		
		m_viewer.setInput(list);
	}
}
