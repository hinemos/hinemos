/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.composite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.ValidMessage;
import com.clustercontrol.infra.action.GetInfraManagementTableDefine;
import com.clustercontrol.infra.composite.action.InfraManagementDoubleClickListener;
import com.clustercontrol.infra.composite.action.InfraManagementSelectionChangedListener;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.infra.view.InfraManagementView;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.sun.xml.internal.ws.client.ClientTransportException;

/**
 * 環境構築[構築・チェック]ビュー用のコンポジットクラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraManagementComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( InfraManagementComposite.class );

	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** ヘッダ用ラベル */
	private Label m_labelType = null;
	/** 件数用ラベル */
	private Label m_labelCount = null;

	private InfraManagementView m_view = null;

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
	public InfraManagementComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	public void setView(InfraManagementView view) {
		m_view = view;
	}

	public InfraManagementView getView() {
		return m_view;
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
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelType.setLayoutData(gridData);

		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId( this, null, table );
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		m_labelCount = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelCount.setLayoutData(gridData);

		m_viewer = new CommonTableViewer(table);
		m_viewer.createTableColumn(GetInfraManagementTableDefine.get(),
				GetInfraManagementTableDefine.SORT_COLUMN_INDEX1,
				GetInfraManagementTableDefine.SORT_COLUMN_INDEX2,
				GetInfraManagementTableDefine.SORT_ORDER);
		// 列移動が可能に設定
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}
        
		m_viewer.addSelectionChangedListener(
				new InfraManagementSelectionChangedListener(this));

		m_viewer.addDoubleClickListener(
				new InfraManagementDoubleClickListener(this));
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * 環境構築設定一覧情報を取得し、共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>監視管理のプレファレンスページより、環境構築[構築・チェック]ビューの表示件数を取得します。</li>
	 * <li>環境構築設定一覧情報を、表示件数分取得します。</li>
	 * <li>表示件数を超える場合、メッセージダイアログを表示します。</li>
	 * <li>共通テーブルビューアーに環境構築設定一覧情報をセットします。</li>
	 * </ol>
	 *
	 * @see com.clustercontrol.infra.action.GetHistory#getHistory(int)
	 * @see #selectItem(ArrayList)
	 */
	@Override
	public void update() {
		update(null);
	}

	/**
	 * テーブルビューアを更新します。<BR>
	 * 引数で指定された条件に一致する履歴一覧情報を取得し、共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>監視管理のプレファレンスページより、環境構築[構築・チェック]ビューの表示件数を取得します。</li>
	 * <li>引数で指定された条件に一致する履歴一覧情報を、表示件数分取得します。</li>
	 * <li>表示件数を超える場合、メッセージダイアログを表示します。</li>
	 * <li>共通テーブルビューアーにジ環境構築一覧情報をセットします。</li>
	 * </ol>
	 *
	 * @param condition 検索条件
	 *
	 * @see com.clustercontrol.infra.action.GetHistory#getHistory(Property, int)
	 * @see #selectItem(ArrayList)
	 */
	public void update(Property condition) {
		Map<String, List<InfraManagementInfo>> dispDataMap= new ConcurrentHashMap<String, List<InfraManagementInfo>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		//環境構築設定情報取得
		for(String managerName : EndpointManager.getActiveManagerSet()) {
			List<InfraManagementInfo> infraManagementList = null;
			InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
			try {
				if (condition == null) {
					infraManagementList = wrapper.getInfraManagementList();
				}
			} catch (Exception e) {
				if(ClientSession.isDialogFree()){
					ClientSession.occupyDialog();
					if (e instanceof ClientTransportException) {
						m_log.warn("update() : " + e.getMessage()); //TODO
						errorMsgs.put(managerName, Messages.getString("message.hinemos.failure.transfer") + ", " + HinemosMessage.replace(e.getMessage()));
					} else {
						m_log.warn("update() : " + e.getMessage(), e); //TODO
						errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
					}
					ClientSession.freeDialog();
				}
			}

			if (infraManagementList == null) {
				infraManagementList = new ArrayList<InfraManagementInfo>();
			}

			dispDataMap.put(managerName, infraManagementList);
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		for(Map.Entry<String, List<InfraManagementInfo>> map : dispDataMap.entrySet()) {
			for (InfraManagementInfo setting : map.getValue()) {
				ArrayList<Object> a = new ArrayList<Object>();
				a.add(map.getKey());
				a.add(setting.getManagementId());
				a.add(setting.getName());
				a.add(setting.getDescription());
				a.add(ValidMessage.typeToString(setting.isValidFlg()));
				a.add(setting.getFacilityId() != null ? setting.getFacilityId() : "#[FACILITY_ID]");
				a.add(HinemosMessage.replace(setting.getScope()));
				a.add(setting.getOwnerRoleId());
				a.add(setting.getRegUser());
				a.add(new Date(setting.getRegDate()));
				a.add(setting.getUpdateUser());
				a.add(new Date(setting.getUpdateDate()));
				a.add(null);
				listInput.add(a);
			}
		}

		m_viewer.setInput(listInput);

//		if (condition != null) {
//			m_labelType.setText(Messages.getString("filtered.list"));
//			Object[] args = { new Integer(historyInfo.getTotal()) };
//			m_labelCount.setText(Messages.getString("filtered.records", args));
//		} else {
			// 表示件数をセット(最大件数以上に達しているか否かの分岐)
			m_labelType.setText("");
			Object[] args = null;
			args = new Object[]{ listInput.size() };
			m_labelCount.setText(Messages.getString("records", args));
//		}
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
