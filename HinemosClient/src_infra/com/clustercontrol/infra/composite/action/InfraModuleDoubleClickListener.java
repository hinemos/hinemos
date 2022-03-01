/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.composite.action;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.openapitools.client.model.CommandModuleInfoResponse;
import org.openapitools.client.model.FileTransferModuleInfoResponse;
import org.openapitools.client.model.InfraManagementInfoResponse;
import org.openapitools.client.model.ReferManagementModuleInfoResponse;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.infra.action.GetInfraModuleTableDefine;
import com.clustercontrol.infra.composite.InfraModuleComposite;
import com.clustercontrol.infra.dialog.CommandModuleDialog;
import com.clustercontrol.infra.dialog.FileTransferModuleDialog;
import com.clustercontrol.infra.dialog.ReferManagementModuleDialog;
import com.clustercontrol.infra.util.InfraRestClientWrapper;

/**
 * 環境構築[構築・チェック]ビューまたは環境構築[モジュール]ビューのテーブルビューア用のDoubleClickListenerです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraModuleDoubleClickListener implements IDoubleClickListener {

	// ログ
	private static Log m_log = LogFactory.getLog( InfraModuleDoubleClickListener.class );

	/** 環境構築[構築・チェック]ビュー、環境構築[モジュール]ビュー、環境構築[スケジュール予定]用のコンポジット */
	private InfraModuleComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite 環境構築[構築・チェック]ビュー、環境構築[モジュール]ビュー、環境構築[スケジュール予定]用のコンポジット
	 */
	public InfraModuleDoubleClickListener(InfraModuleComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * 環境構築[構築・チェック]ビュー、
	 * 環境構築[モジュール]ビュー
	 * のテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行から環境構築IDを取得します。</li>
	 * <li>環境構築IDから環境構築情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		//セッションIDと環境構築IDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			String moduleId = (String) ((ArrayList<?>)((StructuredSelection) event.getSelection()).getFirstElement()).get(GetInfraModuleTableDefine.MODULE_ID);

			if (moduleId == null)
				throw new InternalError("select elemnt does not have moduleId");

			String managerName = m_composite.getManagerName();
			String managementId = m_composite.getManagementId();
			InfraManagementInfoResponse info = null;
			try {
				InfraRestClientWrapper wrapper = InfraRestClientWrapper.getWrapper(managerName);
				info = wrapper.getInfraManagement(managementId);
			} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InfraManagementNotFound
					| InvalidSetting e) {
				m_log.debug("doubleClick getInfraManagement, " + e.getMessage());
			}

			if(info != null && info.getCommandModuleInfoList() != null) {
				for(CommandModuleInfoResponse module: info.getCommandModuleInfoList()) {
					if(module.getModuleId().equals(moduleId)){
						CommonDialog dialog = new CommandModuleDialog(m_composite.getShell(),
								managerName, managementId, moduleId,
								PropertyDefineConstant.MODE_MODIFY);
						
						dialog.open();
						m_composite.update(m_composite.getManagerName(), m_composite.getManagementId());
						
						return;
					}
				}
			}
			
			if(info != null && info.getFileTransferModuleInfoList() != null) {
				for(FileTransferModuleInfoResponse module: info.getFileTransferModuleInfoList()) {
					if(module.getModuleId().equals(moduleId)){
						CommonDialog dialog = new FileTransferModuleDialog(m_composite.getShell(),
								managerName, managementId, moduleId,
								PropertyDefineConstant.MODE_MODIFY);
						
						dialog.open();
						m_composite.update(m_composite.getManagerName(), m_composite.getManagementId());
						
						return;
					}
				}
			}
			
			if(info != null && info.getReferManagementModuleInfoList() != null) {
				for(ReferManagementModuleInfoResponse module: info.getReferManagementModuleInfoList()) {
					if(module.getModuleId().equals(moduleId)){
						CommonDialog dialog = new ReferManagementModuleDialog(m_composite.getShell(),
								managerName, managementId, moduleId,
								PropertyDefineConstant.MODE_MODIFY);
						
						dialog.open();
						m_composite.update(m_composite.getManagerName(), m_composite.getManagementId());
						
						return;
					}
				}
			}
			
			throw new InternalError("dialog is null");
		}
	}
}