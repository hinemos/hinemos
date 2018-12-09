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

import com.clustercontrol.bean.ValidMessage;
import com.clustercontrol.infra.action.GetInfraModuleTableDefine;
import com.clustercontrol.infra.bean.OkNgConstant;
import com.clustercontrol.infra.bean.SendMethodConstant;
import com.clustercontrol.infra.composite.action.InfraModuleDoubleClickListener;
import com.clustercontrol.infra.composite.action.InfraModuleSelectionChangedListener;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.infra.CommandModuleInfo;
import com.clustercontrol.ws.infra.FileTransferModuleInfo;
import com.clustercontrol.ws.infra.HinemosUnknown_Exception;
import com.clustercontrol.ws.infra.InfraCheckResult;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.clustercontrol.ws.infra.InfraManagementNotFound_Exception;
import com.clustercontrol.ws.infra.InfraModuleInfo;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;
import com.clustercontrol.ws.infra.NotifyNotFound_Exception;
import com.clustercontrol.ws.infra.ReferManagementModuleInfo;

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
		InfraManagementInfo info = null;
		InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
		try {
			info = wrapper.getInfraManagement(managementId);
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception | InfraManagementNotFound_Exception e) {
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

		int order = 0;
		for (InfraModuleInfo moduleInfo : info.getModuleList()) {
			order++;
			ArrayList<Object> a = new ArrayList<Object>();
			a.add(order);
			a.add(moduleInfo.getModuleId());
			a.add(moduleInfo.getName());
			if(moduleInfo instanceof FileTransferModuleInfo){
				a.add(Messages.getString("infra.module.transfer"));
			} else if(moduleInfo instanceof CommandModuleInfo){
				a.add(Messages.getString("infra.module.command"));
			} else if(moduleInfo instanceof ReferManagementModuleInfo){
				a.add(Messages.getString("infra.module.refer.management"));
			}
			a.add(ValidMessage.typeToString(moduleInfo.isValidFlg()));
			if(moduleInfo instanceof FileTransferModuleInfo){
				FileTransferModuleInfo fileModInfo = (FileTransferModuleInfo) moduleInfo;
				String str = fileModInfo.getFileId();
				if (fileModInfo.getSendMethodType() == SendMethodConstant.TYPE_SCP) {
					str += " " + (fileModInfo.getDestOwner() == null ? "" : fileModInfo.getDestOwner());
					str += "," + (fileModInfo.getDestAttribute() == null ? "" : fileModInfo.getDestAttribute());
				}
				a.add(str);
			} else if (moduleInfo instanceof CommandModuleInfo) {
				CommandModuleInfo commandModuleInfo = (CommandModuleInfo) moduleInfo;
				a.add(commandModuleInfo.getExecCommand());
			} else if (moduleInfo instanceof ReferManagementModuleInfo) {
				ReferManagementModuleInfo referManagementModuleInfo = (ReferManagementModuleInfo) moduleInfo;
				a.add(referManagementModuleInfo.getReferManagementId());
			} else {
				a.add("");
			}
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
		List<InfraCheckResult> resultList = null;
		try {
			InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
			resultList = wrapper.getCheckResultList(managementId);
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
			m_log.error("getStatusString() getCheckResultList, " + e.getMessage());
		}
		if(resultList == null){
			return ret;
		}

		HashMap<String, List<InfraCheckResult>> checkResultMap = new HashMap<>();
		for (InfraCheckResult result : resultList) {
			String moduleId = result.getModuleId();
			List<InfraCheckResult> list = checkResultMap.get(moduleId);
			if (list == null) {
				list = new ArrayList<InfraCheckResult>();
				checkResultMap.put(moduleId, list);
			}
			m_log.debug("moduleId=" + moduleId + ", facilityId=" + result.getNodeId() + ", " + result.getResult());
			list.add(result);
		}
		for (Entry<String, List<InfraCheckResult>> resultEntry : checkResultMap.entrySet()) {
			List<InfraCheckResult> list = resultEntry.getValue();
			List<String> okList = new ArrayList<>();
			List<String> ngList = new ArrayList<>();
			for (InfraCheckResult result : list) {
				if(result.getResult() == OkNgConstant.TYPE_OK){
					okList.add(result.getNodeId());
				} else if (result.getResult() == OkNgConstant.TYPE_NG){
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
