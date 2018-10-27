/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.accesscontrol.composite.SystemPrivilegeListComposite;
import com.clustercontrol.bean.Property;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;

/**
 * アカウント[システム権限]ビュークラス<BR>
 *
 * クライアントの画面を構成します。
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class SystemPrivilegeListView extends CommonViewPart {
	/** ビューID */
	public static final String ID = SystemPrivilegeListView.class.getName();

	/** アクセス[システム権限]ビュー用のコンポジット */
	private SystemPrivilegeListComposite m_systemPrivilegeList = null;

	/** 表示対象となるロール・ユーザのRoleInfo/UserInfo */
	private Object m_selectedInfo = null;

	/** マネージャ名 */
	private String m_managerName = null;

	/**
	 * コンストラクタ
	 */
	public SystemPrivilegeListView() {
		super();
	}

	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * ビューを構築します。
	 *
	 * @param parent 親コンポジット
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 * @see #createContextMenu()
	 * @see #update()
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		m_systemPrivilegeList = new SystemPrivilegeListComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_systemPrivilegeList);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_systemPrivilegeList.setLayoutData(gridData);

		//ビューを更新
		this.update();

	}

	/**
	 * ビューを更新します。
	 *
	 * @see com.clustercontrol.accesscontrol.composite.UserListComposite#update()
	 * @see com.clustercontrol.accesscontrol.composite.UserListComposite#update(Property)
	 */
	public void update() {
		if (m_selectedInfo == null) {
			m_systemPrivilegeList.update();
		} else {
			m_systemPrivilegeList.update(this.m_managerName, m_selectedInfo);
		}
	}

	/**
	 * ビューを更新します。
	 *
	 * @param selectedInfo 表示対象となるロール・ユーザのRoleInfo/UserInfo<br>
	 * 但し何も表示しない場合にはnullを指定する
	 *
	 * @see com.clustercontrol.accesscontrol.composite.UserListComposite#update(Property)
	 */
	public void update(String managerName, Object selectedInfo) {
		this.m_selectedInfo = selectedInfo;
		this.m_managerName = managerName;

		this.update();
	}

	/**
	 * アカウント[システム権限]ビュー用のコンポジットを返します。
	 *
	 * @return アカウント[システム権限]ビュー用のコンポジット
	 */
	public SystemPrivilegeListComposite getComposite() {
		return m_systemPrivilegeList;
	}
}
