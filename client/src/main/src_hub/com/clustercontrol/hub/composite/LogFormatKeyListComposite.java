/*

 Copyright (C) 2016 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.hub.dialog.LogKeyPatternDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.hub.LogFormatKey;

public class LogFormatKeyListComposite extends Composite {
	
	/**　キー　*/
	public static final int KEY = 0;
	
	/** 説明 */
	public static final int DESCRIPTION = 1;
	
	/** パターン */
	public static final int PATTERN = 2;

	/** ダミー**/
	public static final int DUMMY = 3;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = KEY;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;
	
	/**
	 * ログ[フォーマット]メッセージキーパターンテーブル定義を取得します。<BR>
	 *
	 * @return
	 */
	public static ArrayList<TableColumnInfo> getTableColumnInfoList() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(KEY,
				new TableColumnInfo(Messages.getString("hub.log.format.key", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(PATTERN,
				new TableColumnInfo(Messages.getString("hub.log.format.key.pattern.regex", locale), TableColumnInfo.NONE, 300, SWT.LEFT));

		return tableDefine;
	}
	
	
	/** テーブルビューアー。 */
	private CommonTableViewer m_tableViewer = null;
	/** ログ[フォーマット]キーの情報一覧 */
	private List<LogFormatKey> m_keyList = null;
	/** オーナーロールID */
	private String m_ownerRoleId = null;
	/** マネージャ名 */
	private String m_managerName = null;

	/**
	 * @return the m_managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}
	
	/**
	 * キーをもとに、LogFormatKey情報をコンポジットのリストから検索し、
	 * 一致したLogFormatKey情報を返す
	 * @param key
	 * @return
	 */
	public LogFormatKey getLogFormatKeyListByKey(String key){
		for (LogFormatKey logFormatKey : getLogFormatKeyList()) {
			if (logFormatKey.getKey().equals(key)) {
				return logFormatKey;
			}
		}
		return null;
	}
	
	/**
	 *
	 * @return
	 */
	public List<LogFormatKey> getLogFormatKeyList(){
		return this.m_keyList;
	}
	/**
	 * 引数で指定されたログ[ターゲット]情報をコンポジット内リストに反映させる
	 * @param keyList
	 */
	public void setLogFormatKeyList(List<LogFormatKey> keyList){
		if (keyList != null) {
			this.m_keyList = keyList;
			this.update();
		}
	}
	public void addLogFormatKeyList(LogFormatKey key){
		this.m_keyList.add(key);
		}
	/**
	 *
	 * @return
	 */
	public String getOwnerRoleId() {
		return m_ownerRoleId;
	}
	/**
	 *
	 * @param ownerRoleId
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
	}
	
	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param managerName マネージャ名
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public LogFormatKeyListComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.m_managerName = managerName;
		this.m_keyList = new ArrayList<LogFormatKey>();
		this.initialize();
	}
	
	/**
	 * 引数で指定された判定情報の行を選択状態にします。
	 *
	 * @param identifier 識別キー
	 */
	@SuppressWarnings("unused")
	private void selectItem(Integer order) {
		Table tblLogFormatKeyList = m_tableViewer.getTable();
		TableItem[] items = tblLogFormatKeyList.getItems();

		if (items == null || order == null) {
			return;
		}
		tblLogFormatKeyList.select(order);
		return;
	}
	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
	
		/*
		 * ログ[フォーマット]情報初期化
		 */
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Table tblLogFormatKeyList = new Table(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, tblLogFormatKeyList);
		tblLogFormatKeyList.setHeaderVisible(true);
		tblLogFormatKeyList.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		tblLogFormatKeyList.setLayoutData(gridData);

		// テーブルビューアの作成
		m_tableViewer = new CommonTableViewer(tblLogFormatKeyList);
		m_tableViewer.createTableColumn(getTableColumnInfoList(),
				SORT_COLUMN_INDEX,
				SORT_ORDER);
		m_tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				//選択したテーブルのキーを取得
				String order = getSelectionLogFormatKey();
				
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				//キーをもとに、LogFormatKey情報をコンポジットのリストから検索する
				LogFormatKey logFormatKey = getLogFormatKeyListByKey(order);
				
				if (logFormatKey != null) {
					LogKeyPatternDialog dialog = new LogKeyPatternDialog(shell, 
							PropertyDefineConstant.MODE_MODIFY, 
							m_keyList,
							logFormatKey);
					if (dialog.open() == IDialogConstants.OK_ID) {
						getLogFormatKeyList().remove(logFormatKey);
						addLogFormatKeyList(dialog.getLogFormatKey());
						update();
					}
				}else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});
	}
	
	/**
	 * コンポジットを更新します。<BR>
	 * ログ[フォーマット]キー情報一覧を取得し、テーブルビューアーにセットします。
	 */
	@Override
	public void update() {
		// テーブル更新
		ArrayList<Object> listAll = new ArrayList<Object>();
		for (LogFormatKey key : getLogFormatKeyList()) {
			ArrayList<Object> list = new ArrayList<Object>();
			//FIXME バリデート?
			list.add(key.getKey());
			list.add(key.getDescription());
			list.add(key.getPattern());
			//ダミー列
			list.add(null);
			listAll.add(list);
		}
		m_tableViewer.setInput(listAll);
	}
	
	/**
	 * 選択したテーブル行番号を返す。
	 *
	 */
	public Integer getSelection() {
		StructuredSelection selection = (StructuredSelection) m_tableViewer.getSelection();
		if (selection.getFirstElement() instanceof List) {
			List<?> list = (List<?>)selection.getFirstElement();
			if (list.get(0) instanceof Integer) {
				return (Integer) list.get(0);
			}
		}
		return null;
	}
	/**
	 * 選択したテーブルのLogFormatKeyのキーを返す。
	 *
	 */
	public String getSelectionLogFormatKey() {
		StructuredSelection selection = (StructuredSelection) m_tableViewer.getSelection();
		if (selection.getFirstElement() instanceof List) {
			List<?> list = (List<?>)selection.getFirstElement();
			if (list.get(0) instanceof String) {
				return (String) list.get(0);
			}
		}
		return null;
	}
	/**
	 * 
	 */
	public void setSelection() {
		Table setSelectionTable = m_tableViewer.getTable();
		WidgetTestUtil.setTestId(this, null, setSelectionTable);
		int selectIndex = setSelectionTable.getSelectionIndex();
		update();
		setSelectionTable.setSelection(selectIndex);
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_tableViewer.getTable().setEnabled(enabled);
	}
	
}
