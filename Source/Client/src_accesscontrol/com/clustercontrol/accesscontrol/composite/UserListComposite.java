/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.composite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import org.openapitools.client.model.UserInfoResponse;

import com.clustercontrol.accesscontrol.action.GetUserListTableDefine;
import com.clustercontrol.accesscontrol.composite.action.UserDoubleClickListener;
import com.clustercontrol.accesscontrol.util.AccessRestClientWrapper;
import com.clustercontrol.bean.Property;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * アクセス[ユーザ]ビュー用のコンポジットクラスです。
 *
 * クライアントのビューを構成します。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class UserListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( UserListComposite.class );

	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** ヘッダ用ラベル */
	private Label m_labelType = null;
	/** 件数用ラベル */
	private Label m_labelCount = null;
	/** ユーザID */
	private String m_uid = null;

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
	public UserListComposite(Composite parent, int style) {
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
		m_viewer.createTableColumn(GetUserListTableDefine.get(),
				GetUserListTableDefine.SORT_COLUMN_INDEX1,
				GetUserListTableDefine.SORT_COLUMN_INDEX2,
				GetUserListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}
		// ダブルクリックリスナの追加
		m_viewer.addDoubleClickListener(new UserDoubleClickListener(this));
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * ユーザ一覧情報を取得し、共通テーブルビューアーにセットします。
	 *
	 * @see com.clustercontrol.accesscontrol.action.GetUserList#getAll()
	 */
	@Override
	public void update() {

		//ジョブ履歴情報取得
		List<UserInfoResponse> infoList = null;
		Map<String, List<UserInfoResponse>> dispDataMap= new ConcurrentHashMap<String, List<UserInfoResponse>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		//実行契機情報取得
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			try {
				AccessRestClientWrapper wrapper = AccessRestClientWrapper.getWrapper(managerName);
				infoList = wrapper.getUserInfoList();
			} catch (InvalidRole e) {
				// 権限なし
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );

				// 一覧の参照権限がない場合、ユーザ自身の情報を表示する
				infoList = getOwnUserList(managerName);

			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("update(), " + e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " +e.getMessage());
			}
			
			if (infoList == null) {
				infoList = new ArrayList<UserInfoResponse>();
			}
			
			dispDataMap.put(managerName, infoList);
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		for(Map.Entry<String, List<UserInfoResponse>> map : dispDataMap.entrySet()) {
			List<UserInfoResponse> list = map.getValue();
			for (UserInfoResponse info : list) {
				ArrayList<Object> obj = new ArrayList<Object>();
				obj.add(map.getKey());
				obj.add(info.getUserId());
				obj.add(info.getUserName());
				obj.add(info.getDescription());
				obj.add(info.getMailAddress());
				obj.add(info.getCreateUserId());
				obj.add(info.getCreateDate());
				obj.add(info.getModifyUserId());
				obj.add(info.getModifyDate());
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
	 * テーブルビューアーを更新します。<BR>
	 * フィルタ条件に一致するユーザ一覧情報を取得し、<BR>
	 * 共通テーブルビューアーにセットします。
	 *
	 * @param condition フィルタ条件
	 *
	 * @see com.clustercontrol.accesscontrol.action.GetUserList#getJobEditState(Property)
	 */
	public void update(Property condition) {
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
	 * ユーザIDを返します。
	 *
	 * @return ユーザID
	 */
	public String getUid() {
		return m_uid;
	}

	/**
	 * ユーザIDを設定します。
	 *
	 * @param uid ユーザID
	 */
	public void setUid(String uid) {
		m_uid = uid;
	}


	/**
	 * ユーザ自身の情報をリストとして取得します。<BR>
	 *
	 * @return ユーザ情報一覧
	 */
	public List<UserInfoResponse> getOwnUserList(String managerName) {

		//ユーザ情報一覧
		List<UserInfoResponse> infoList = new ArrayList<UserInfoResponse>();
		UserInfoResponse info = null;
		try {
			AccessRestClientWrapper wrapper = AccessRestClientWrapper.getWrapper(managerName);
			info = wrapper.getOwnUserInfo();
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));

		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("getOwnUserList(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage());
		}
		if(info == null){
			info = new UserInfoResponse();
			info.setCreateDate(TimezoneUtil.getSimpleDateFormat().format(new Date(0L)));
			info.setModifyDate(TimezoneUtil.getSimpleDateFormat().format(new Date(0L)));
		}
		infoList.add(info);

		return infoList;
	}
}
