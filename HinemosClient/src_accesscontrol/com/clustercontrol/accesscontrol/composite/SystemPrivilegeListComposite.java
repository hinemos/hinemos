/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.composite;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.accesscontrol.action.GetSystemPrivilegeListTableDefine;
import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.accesscontrol.util.AccessEndpointWrapper;
import com.clustercontrol.accesscontrol.util.SystemPrivilegePropertyUtil;
import com.clustercontrol.bean.Property;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.RoleInfo;
import com.clustercontrol.ws.access.SystemPrivilegeInfo;
import com.clustercontrol.ws.access.UserInfo;

/**
 * アカウント[システム権限]ビュー用のコンポジットクラスです。
 *
 * クライアントのビューを構成します。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class SystemPrivilegeListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( SystemPrivilegeListComposite.class );

	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** ヘッダ用ラベル */
	private Label m_labelType = null;
	/** 件数用ラベル */
	private Label m_labelCount = null;
	/** 表示対象 */
	private Object m_data = null;

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
	public SystemPrivilegeListComposite(Composite parent, int style) {
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
				| SWT.FULL_SELECTION | SWT.SINGLE);
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
		m_viewer.createTableColumn(GetSystemPrivilegeListTableDefine.get(),
				GetSystemPrivilegeListTableDefine.SORT_COLUMN_INDEX,
				GetSystemPrivilegeListTableDefine.SORT_ORDER);
		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * ユーザ一覧情報を取得し、共通テーブルビューアーにセットします。
	 *
	 * @see com.clustercontrol.accesscontrol.action.GetUserList#getAll()
	 */
	@Override
	public void update() {
		update(null, null);
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * フィルタ条件に一致するユーザ一覧情報を取得し、<BR>
	 * 共通テーブルビューアーにセットします。
	 *
	 * @param selectedInfo 表示対象となるロール・ユーザのRoleInfo/UserInfo<br>
	 * 但し何も表示しない場合にはnullを指定する
	 *
	 * @see com.clustercontrol.accesscontrol.action.GetUserList#getJobEditState(Property)
	 */
	public void update(String managerName, Object selectedInfo) {

		//システム権限情報取得
		List<SystemPrivilegeInfo> infoList = null;
		try {
			if (selectedInfo != null) {
				AccessEndpointWrapper wrapper = AccessEndpointWrapper.getWrapper(managerName);
				if (selectedInfo instanceof RoleInfo) {
					if (!((RoleInfo)selectedInfo).getRoleId().equals(RoleSettingTreeConstant.ROOT_ID) &&
						!((RoleInfo)selectedInfo).getRoleId().equals(RoleSettingTreeConstant.MANAGER)) {
						infoList = wrapper.getSystemPrivilegeInfoListByRoleId(((RoleInfo)selectedInfo).getRoleId());
					}
				} else if (selectedInfo instanceof UserInfo) {
					infoList = wrapper.getSystemPrivilegeInfoListByUserId(((UserInfo)selectedInfo).getUserId());
				}
			}
		} catch (InvalidRole_Exception e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));

			// TODO：あとで何とか考える
			//			// 一覧の参照権限がない場合、ユーザ自身の情報を表示する
			//			infoList = getOwnUserList();

		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		if (selectedInfo != null && infoList != null) {
			for (SystemPrivilegeInfo info : infoList) {
				ArrayList<Object> obj = new ArrayList<Object>();
				obj.add(SystemPrivilegePropertyUtil.getSystemPrivilegeName(managerName, info));
				obj.add(null);
				listInput.add(obj);
			}
		}

		m_viewer.setInput(listInput);

		Object[] args = { listInput.size() };
		if (selectedInfo == null) {
			m_labelType.setText("");
			m_labelCount.setText(Messages.getString("records", args));
		} else {
			if (selectedInfo instanceof RoleInfo) {
				if (((RoleInfo)selectedInfo).getRoleId().equals(RoleSettingTreeConstant.ROOT_ID)) {
					m_labelType.setText("");
					m_labelCount.setText(Messages.getString("records", args));
				} else {
					RoleInfo roleInfo = (RoleInfo)selectedInfo;
					m_labelType.setText(Messages.getString("role.name") + " : " + roleInfo.getRoleName() + "(" + roleInfo.getRoleId() + ")");
					m_labelCount.setText(Messages.getString("filtered.records", args));
				}
			} else if (selectedInfo instanceof UserInfo) {
				UserInfo userInfo = (UserInfo)selectedInfo;
				m_labelType.setText(Messages.getString("user.name") + " : " + userInfo.getUserName() + "(" + userInfo.getUserId() + ")");
				m_labelCount.setText(Messages.getString("filtered.records", args));
			}
		}
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
