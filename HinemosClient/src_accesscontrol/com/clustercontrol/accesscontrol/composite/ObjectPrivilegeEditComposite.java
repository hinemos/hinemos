/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.accesscontrol.composite;

import java.util.ArrayList;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.clustercontrol.accesscontrol.action.GetObjectPrivilegeEditTableDefine;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeBean;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * オブジェクト権限編集ダイアログ用のコンポジットクラスです。
 *
 * クライアントのビューを構成します。
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class ObjectPrivilegeEditComposite extends Composite {

	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** オブジェクト権限 */
	private ObjectPrivilegeBean m_objPriv = null;


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
	public ObjectPrivilegeEditComposite(Composite parent, int style) {
		super(parent, style);
		initialize(false);
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize(boolean flg) {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 10;
		layout.marginWidth = 10;

		final Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.m_viewer = new CommonTableViewer(table);
		this.m_viewer.createTableColumn(GetObjectPrivilegeEditTableDefine.get(),
				GetObjectPrivilegeEditTableDefine.SORT_COLUMN_INDEX,
				GetObjectPrivilegeEditTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}

		// テーブル情報の初期表示 TODO: 外だししたほうがよい
		ArrayList<ArrayList<Object>> listInput = new ArrayList<ArrayList<Object>>();
		ArrayList<Object> a = new ArrayList<Object>();
		a.add(Messages.getString("refer"));
		a.add(false);
		a.add(ObjectPrivilegeMode.READ);
		a.add(0);
		listInput.add(a);
		a = new ArrayList<Object>();
		a.add(Messages.getString("modify"));
		a.add(false);
		a.add(ObjectPrivilegeMode.MODIFY);
		a.add(1);
		listInput.add(a);
		a = new ArrayList<Object>();
		a.add(Messages.getString("run"));
		a.add(false);
		a.add(ObjectPrivilegeMode.EXEC);
		a.add(2);
		listInput.add(a);
		this.m_viewer.setInput(listInput);

		// table 選択時のチェックボックスの挙動
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// 選択されたTableColumnを取得します。
				TableItem[] ti = table.getSelection();
				for (int i = 0; i < ti.length; i++) {
					@SuppressWarnings("unchecked")
					ArrayList<Object> al = (ArrayList<Object>)ti[i].getData();
					WidgetTestUtil.setTestId(this, "tableitem" + i, ti[i]);
					if(!al.get(GetObjectPrivilegeEditTableDefine.PRIVILEGE).equals(ObjectPrivilegeMode.READ)){
						// チェックボックスの状況を確認
						if((Boolean)al.get(GetObjectPrivilegeEditTableDefine.ALLOW_CHECKBOX)){
							// YESならNO
							al.set(GetObjectPrivilegeEditTableDefine.ALLOW_CHECKBOX, false);

							// オブジェクト権限設定の更新
							if(al.get(GetObjectPrivilegeEditTableDefine.PRIVILEGE).equals(ObjectPrivilegeMode.MODIFY)){
								m_objPriv.setWritePrivilege(false);
							}
							else if(al.get(GetObjectPrivilegeEditTableDefine.PRIVILEGE).equals(ObjectPrivilegeMode.EXEC)){
								m_objPriv.setExecPrivilege(false);
							}
						} else {
							// NOならYES
							al.set(GetObjectPrivilegeEditTableDefine.ALLOW_CHECKBOX, true);

							// オブジェクト権限設定の更新
							if(al.get(GetObjectPrivilegeEditTableDefine.PRIVILEGE).equals(ObjectPrivilegeMode.MODIFY)){
								m_objPriv.setWritePrivilege(true);
							}
							else if(al.get(GetObjectPrivilegeEditTableDefine.PRIVILEGE).equals(ObjectPrivilegeMode.EXEC)){
								m_objPriv.setExecPrivilege(true);
							}
						}
					}
				}
				// チェックが入るので、再描画
				m_viewer.refresh();
			}
		});

	}

	/**
	 * 与えられたオブジェクト権限情報で再描画します。
	 *
	 * @param objPriv
	 */
	public void setObjectPrivilege(ObjectPrivilegeBean objPriv) {
		this.m_objPriv = objPriv;
		this.update();
	}

	/**
	 * コンポジットを更新します。<BR>
	 * オブジェクト権限情報を取得し、テーブルビューアーにセットします。
	 *
	 */
	@Override
	public void update() {

		ArrayList<ArrayList<Object>> listInput = new ArrayList<ArrayList<Object>>();
		ArrayList<Object> a = new ArrayList<Object>();
		a.add(Messages.getString("refer"));
		a.add(m_objPriv.getReadPrivilege());
		a.add(ObjectPrivilegeMode.READ);
		a.add(0);
		listInput.add(a);
		a = new ArrayList<Object>();
		a.add(Messages.getString("modify"));
		a.add(m_objPriv.getWritePrivilege());
		a.add(ObjectPrivilegeMode.MODIFY);
		a.add(1);
		listInput.add(a);
		a = new ArrayList<Object>();
		a.add(Messages.getString("run"));
		a.add(m_objPriv.getExecPrivilege());
		a.add(ObjectPrivilegeMode.EXEC);
		a.add(2);
		a.add(null);
		listInput.add(a);
		this.m_viewer.setInput(listInput);

	}

	/**
	 * このコンポジットが利用するテーブルビューアを返します。
	 *
	 * @return テーブルビューア
	 */
	public TableViewer getTableViewer() {
		return m_viewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return m_viewer.getTable();
	}

}
