/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.composite;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.CommandModuleInfoResponse;
import org.openapitools.client.model.FileTransferModuleInfoResponse;
import org.openapitools.client.model.FileTransferModuleInfoResponse.SendMethodTypeEnum;
import org.openapitools.client.model.InfraCheckResultResponse;
import org.openapitools.client.model.InfraCheckResultResponse.ResultEnum;
import org.openapitools.client.model.InfraManagementInfoResponse;
import org.openapitools.client.model.ReferManagementModuleInfoResponse;

import com.clustercontrol.bean.ValidMessage;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.infra.action.GetInfraModuleTableDefine;
import com.clustercontrol.infra.composite.action.InfraModuleDoubleClickListener;
import com.clustercontrol.infra.composite.action.InfraModuleSelectionChangedListener;
import com.clustercontrol.infra.util.InfraRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * 環境構築[モジュール]ビュー用のコンポジットクラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraModuleComposite extends Composite {
	// ログ
	private static Log m_log = LogFactory.getLog( InfraModuleComposite.class );

	/** テーブルビューアー */
	private CommonTableViewer m_viewer = null;
	/** 構築ID用ラベル */
	private Label m_HeaderLabel = null;
	/** 件数用ラベル */
	private Label m_labelCount = null;
	/** 構築ＩＤ */
	private String m_managementId = null;
	/** マネージャ名 */
	private String m_managerName = null;

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
	public InfraModuleComposite(Composite parent, int style) {
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

		//構築IDラベル作成
		m_HeaderLabel = new Label(this, SWT.LEFT);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		m_HeaderLabel.setLayoutData(gridData);

		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId( this, null, table );

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		m_labelCount = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelCount.setLayoutData(gridData);

		m_viewer = new CommonTableViewer(table);
		m_viewer.createTableColumn(GetInfraModuleTableDefine.get(),
				GetInfraModuleTableDefine.SORT_COLUMN_INDEX,
				GetInfraModuleTableDefine.SORT_ORDER);
		// 列移動が可能に設定
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}

		m_viewer.addSelectionChangedListener(
				new InfraModuleSelectionChangedListener());

		m_viewer.addDoubleClickListener(
				new InfraModuleDoubleClickListener(this));

		update(null, null);
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * 引数で指定された構築IDのモジュール一覧情報を取得し、
	 * 共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>引数で指定された構築IDのモジュール一覧情報を取得します。</li>
	 * <li>共通テーブルビューアーにモジュール一覧情報をセットします。</li>
	 * </ol>
	 *
	 * @param managementId 構築ID
	 *
	 */
	public void update(String managerName, String managementId) {
		if(managerName == null || managementId == null){
			m_managerName = null;
			m_managementId = null;
			m_HeaderLabel.setText("");
			m_viewer.setInput(null);
			return;
		}

		//環境構築設定情報取得
		InfraManagementInfoResponse info = null;
		InfraRestClientWrapper wrapper = InfraRestClientWrapper.getWrapper(managerName);
		try {
			info = wrapper.getInfraManagement(managementId);
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InfraManagementNotFound | InvalidSetting e) {
			m_log.warn("update() getInfraManagement, " + e.getMessage());
		}

		if (info == null) {
			return;
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		m_managerName = managerName;
		m_managementId = managementId;
		m_HeaderLabel.setText(info.getManagementId());

		HashMap<String, String> map = getStatusString(managerName, managementId);

		for(CommandModuleInfoResponse moduleInfo : info.getCommandModuleInfoList()) {
			ArrayList<Object> a = new ArrayList<Object>();
			a.add(moduleInfo.getOrderNo() + 1);
			a.add(moduleInfo.getModuleId());
			a.add(moduleInfo.getName());
			a.add(Messages.getString("infra.module.command"));
			a.add(ValidMessage.typeToString(moduleInfo.getValidFlg()));
			a.add(moduleInfo.getExecCommand());
			a.add(map.get(moduleInfo.getModuleId()));
			a.add("");
			listInput.add(a);
		}
		
		for(FileTransferModuleInfoResponse moduleInfo : info.getFileTransferModuleInfoList()) {
			ArrayList<Object> a = new ArrayList<Object>();
			a.add(moduleInfo.getOrderNo() + 1);
			a.add(moduleInfo.getModuleId());
			a.add(moduleInfo.getName());
			a.add(Messages.getString("infra.module.transfer"));
			a.add(ValidMessage.typeToString(moduleInfo.getValidFlg()));
			String str = moduleInfo.getFileId();
			if (moduleInfo.getSendMethodType() == SendMethodTypeEnum.SCP) {
				str += " " + (moduleInfo.getDestOwner() == null ? "" : moduleInfo.getDestOwner());
				str += "," + (moduleInfo.getDestAttribute() == null ? "" : moduleInfo.getDestAttribute());
			}
			a.add(str);
			a.add(map.get(moduleInfo.getModuleId()));
			a.add("");
			listInput.add(a);
		}
		
		for(ReferManagementModuleInfoResponse moduleInfo : info.getReferManagementModuleInfoList()) {
			ArrayList<Object> a = new ArrayList<Object>();
			a.add(moduleInfo.getOrderNo() + 1);
			a.add(moduleInfo.getModuleId());
			a.add(moduleInfo.getName());
			a.add(Messages.getString("infra.module.refer.management"));
			a.add(ValidMessage.typeToString(moduleInfo.getValidFlg()));
			a.add(moduleInfo.getReferManagementId());
			a.add(map.get(moduleInfo.getModuleId()));
			a.add("");
			listInput.add(a);
		}

		m_viewer.setInput(listInput);

		Object[] args = null;
		args = new Object[]{ listInput.size() };
		m_labelCount.setText(Messages.getString("records", args));
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
	 * ビューに表示されているモジュール一覧の
	 * 親となる構築情報の構築ＩＤを返します。
	 *
	 */
	public String getManagementId(){
		return m_managementId;
	}

	/** 選択されているマネージャ名を返します。
	 * @return the m_managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}

	private HashMap<String, String> getStatusString(String managerName, String managementId) {
		HashMap<String, String> ret = new HashMap<>();
		List<InfraCheckResultResponse> resultList = null;
		try {
			InfraRestClientWrapper wrapper = InfraRestClientWrapper.getWrapper(managerName);
			resultList = wrapper.getCheckResultList(managementId);
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting e) {
			m_log.error("getStatusString() getCheckResultList, " + e.getMessage());
		}
		if(resultList == null){
			return ret;
		}

		HashMap<String, List<InfraCheckResultResponse>> checkResultMap = new HashMap<>();
		for (InfraCheckResultResponse result : resultList) {
			String moduleId = result.getModuleId();
			List<InfraCheckResultResponse> list = checkResultMap.get(moduleId);
			if (list == null) {
				list = new ArrayList<InfraCheckResultResponse>();
				checkResultMap.put(moduleId, list);
			}
			m_log.debug("moduleId=" + moduleId + ", facilityId=" + result.getNodeId() + ", " + result.getResult());
			list.add(result);
		}
		for (Entry<String, List<InfraCheckResultResponse>> resultEntry : checkResultMap.entrySet()) {
			List<InfraCheckResultResponse> list = resultEntry.getValue();
			List<String> okList = new ArrayList<>();
			List<String> ngList = new ArrayList<>();
			for (InfraCheckResultResponse result : list) {
				if(result.getResult() == ResultEnum.OK){
					okList.add(result.getNodeId());
				} else if (result.getResult() == ResultEnum.NG){
					ngList.add(result.getNodeId());
				} else {
					m_log.warn("getStatusString : " + result.getNodeId() + ", " + result.getResult()); // ここには到達しないはず。
				}
			}
			Collections.sort(okList);
			Collections.sort(ngList);
			StringBuilder message = new StringBuilder();
			StringBuilder str = new StringBuilder();
			for (String ng : ngList) {
				if (str.length() > 0) {
					str.append(", ");
				}
				str.append(ng);
			}
			message.append("NG(" + str + "), \n");
			str = new StringBuilder();
			for (String ok : okList) {
				if (str.length() > 0) {
					str.append(", ");
				}
				str.append(ok);
			}
			message.append("OK(" + str + ")");
			
			int maxMessage = 1024;
			if (message.length() > maxMessage) {
				message.substring(1, maxMessage);
				message.append("...");
			}
			ret.put(resultEntry.getKey(), message.toString());
		}

		return ret;
	}
}
