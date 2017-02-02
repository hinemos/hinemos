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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.accesscontrol.action.GetRoleListTableDefine;
import com.clustercontrol.accesscontrol.composite.action.RoleDoubleClickListener;
import com.clustercontrol.accesscontrol.util.AccessEndpointWrapper;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.RoleInfo;

/**
 * アカウント[ロール]ビュー用のコンポジットクラスです。
 *
 * クライアントのビューを構成します。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class RoleListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( RoleListComposite.class );

	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** ヘッダ用ラベル */
	private Label m_labelType = null;
	/** 件数用ラベル */
	private Label m_labelCount = null;

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
	public RoleListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		m_labelType = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "type", m_labelType);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelType.setLayoutData(gridData);

		// テーブルの作成
		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// 合計ラベルの作成
		m_labelCount = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "count", m_labelCount);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelCount.setLayoutData(gridData);

		// テーブルビューアの作成
		m_viewer = new CommonTableViewer(table);
		m_viewer.createTableColumn(GetRoleListTableDefine.get(),
				GetRoleListTableDefine.SORT_COLUMN_INDEX1,
				GetRoleListTableDefine.SORT_COLUMN_INDEX2,
				GetRoleListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}
		// ダブルクリックリスナの追加
		m_viewer.addDoubleClickListener(new RoleDoubleClickListener(this));
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * ユーザ一覧情報を取得し、共通テーブルビューアーにセットします。
	 *
	 * @see com.clustercontrol.accesscontrol.action.GetUserList#getAll()
	 */
	@Override
	public void update() {

		//ロール情報取得
		List<RoleInfo> infoList = null;
		Map<String, List<RoleInfo>> dispDataMap= new ConcurrentHashMap<String, List<RoleInfo>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		//実行契機情報取得
		for(String managerName : EndpointManager.getActiveManagerSet()) {
			try {
				AccessEndpointWrapper wrapper = AccessEndpointWrapper.getWrapper(managerName);
				infoList = wrapper.getRoleInfoList();
			} catch (InvalidRole_Exception e) {
				// 権限なし
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );

			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("update(), " + e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}

			if (infoList == null) {
				infoList = new ArrayList<RoleInfo>();
			}

			dispDataMap.put(managerName, infoList);
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		for(Map.Entry<String, List<RoleInfo>> map : dispDataMap.entrySet()) {
			for (RoleInfo info : map.getValue()) {
				ArrayList<Object> obj = new ArrayList<Object>();
				obj.add(map.getKey());
				obj.add(info.getRoleId());
				obj.add(info.getRoleName());
				obj.add(info.getDescription());
				obj.add(info.getCreateUserId());
				obj.add(new Date(info.getCreateDate()));
				obj.add(info.getModifyUserId());
				obj.add(new Date(info.getModifyDate()));
				obj.add(null);
				listInput.add(obj);
			}
		}

		m_viewer.setInput(listInput);

		Object[] args = { listInput.size() };
		m_labelType.setText("");
		m_labelCount.setText(Messages.getString("records", args));
	}

	/**
	 * このコンポジットが利用するテーブルビューアを返します。
	 *
	 * @return テーブルビューア
	 */
	public CommonTableViewer getTableViewer() {
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
