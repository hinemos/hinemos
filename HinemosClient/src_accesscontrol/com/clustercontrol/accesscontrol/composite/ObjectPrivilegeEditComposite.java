/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.composite;

import java.util.ArrayList;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.clustercontrol.accesscontrol.action.GetObjectPrivilegeEditTableDefine;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeBean;
import com.clustercontrol.util.CheckBoxSelectionAdapter;
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
		/** チェックボックスの選択を制御するリスナー */
		SelectionAdapter adapter = 
				new ObjectPrivilegeSelectionAdapter(this, this.m_viewer, 
				GetObjectPrivilegeEditTableDefine.ALLOW_CHECKBOX
						);
		table.addSelectionListener(adapter);
	}

	/**
	 * テーブル選択とチェックボックスの状態、権限の値を同期するためのAdapter
	 *
	 */
	public static class ObjectPrivilegeSelectionAdapter extends CheckBoxSelectionAdapter {
		
		private ObjectPrivilegeEditComposite composite;
		
		public ObjectPrivilegeSelectionAdapter(
				ObjectPrivilegeEditComposite parent, CommonTableViewer tableViewer, int checkBoxColIndex) {
			super(parent, tableViewer, checkBoxColIndex);
			this.composite = parent;
			
		}

		@Override
		protected boolean isIgnoreRow(TableItem item) {
			if (ObjectPrivilegeMode.READ.equals(getPrivilege(item))) {
				//Readは操作しない（常にON）
				return true;
			}
			return false;
		}
			
		
		@Override
		protected void setCheckBoxValue(TableItem item, boolean check) {
			super.setCheckBoxValue(item, check);
			//チェックに変更があった時、権限も変更する
			
			ObjectPrivilegeBean objPriv = this.composite.getObjectPrivilege();
			if (objPriv == null) {
				return;
			}
			
			if (ObjectPrivilegeMode.MODIFY.equals(getPrivilege(item))) {
				objPriv.setWritePrivilege(check);
			} else if (ObjectPrivilegeMode.EXEC.equals(getPrivilege(item))) {
				objPriv.setExecPrivilege(check);
			}
		}
		
		private ObjectPrivilegeMode getPrivilege(TableItem item) {
			return (ObjectPrivilegeMode) toRowValues(item).get(GetObjectPrivilegeEditTableDefine.PRIVILEGE);
		}
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
	
	public ObjectPrivilegeBean getObjectPrivilege() {
		return m_objPriv;
	}

}
