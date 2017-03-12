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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.accesscontrol.action.GetObjectPrivilegeListTableDefine;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeBean;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * オブジェクト権限一覧ダイアログ用のコンポジットクラスです。
 *
 * クライアントのビューを構成します。
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class ObjectPrivilegeListComposite extends Composite {

	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** 表示対象 */
	private Object m_data = null;
	/** オブジェクト権限マップ */
	private  HashMap<String, ObjectPrivilegeBean> m_objPrivMap = null;


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
	public ObjectPrivilegeListComposite(Composite parent, int style) {
		super(parent, style);
		initialize(false);
	}
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
	public ObjectPrivilegeListComposite(Composite parent, int style, HashMap<String, ObjectPrivilegeBean> objPrivMap) {
		super(parent, style);
		this.m_objPrivMap = objPrivMap;

		initialize(false);
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize(boolean flg) {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

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
		this.m_viewer.createTableColumn(GetObjectPrivilegeListTableDefine.get(),
				GetObjectPrivilegeListTableDefine.SORT_COLUMN_INDEX,
				GetObjectPrivilegeListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}
		this.update();
	}

	/**
	 * コンポジットを更新します。<BR>
	 * オブジェクト権限情報を取得し、テーブルビューアーにセットします。
	 *
	 */
	@Override
	public void update() {

		// 表示結果格納するリスト
		List<ArrayList<Object>> inputList = new ArrayList<ArrayList<Object>>();

		ObjectPrivilegeBean bean = null;

		if(m_objPrivMap == null){
			return;
		}

		for(Map.Entry<String, ObjectPrivilegeBean> keyValue : m_objPrivMap.entrySet()) {

			bean = keyValue.getValue();
			ArrayList<Object> a = new ArrayList<Object>();

			//ロールID
			a.add(bean.getRoleId());

			// 参照権限が存在する場合
			if(bean.getReadPrivilege())
				a.add(true);
			else
				a.add(false);
			// 更新権限が存在する場合
			if(bean.getWritePrivilege())
				a.add(true);
			else
				a.add(false);
			// 実行権限が存在する場合
			if(bean.getExecPrivilege())
				a.add(true);
			else
				a.add(false);

			a.add(null);
			inputList.add(a);
		}

		// テーブル更新
		this.m_viewer.setInput(inputList);
	}

	public void objectPrivilegeRefresh(HashMap<String, ObjectPrivilegeBean> map) {
		this.m_objPrivMap = map;
		this.update();
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

	/**
	 * 表示対象データを返します。
	 *
	 * @return ユーザ情報もしくはロール情報
	 */
	@Override
	public Object getData() {
		return m_data;
	}

	/**
	 * 表示対象データを設定します。
	 *
	 * @param data ユーザ情報もしくはロール情報
	 */
	@Override
	public void setData(Object data) {
		m_data = data;
	}
}
