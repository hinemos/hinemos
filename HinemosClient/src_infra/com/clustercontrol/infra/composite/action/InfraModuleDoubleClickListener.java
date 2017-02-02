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

package com.clustercontrol.infra.composite.action;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.infra.action.GetInfraModuleTableDefine;
import com.clustercontrol.infra.composite.InfraModuleComposite;
import com.clustercontrol.infra.dialog.CommandModuleDialog;
import com.clustercontrol.infra.dialog.FileTransferModuleDialog;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.ws.infra.CommandModuleInfo;
import com.clustercontrol.ws.infra.FileTransferModuleInfo;
import com.clustercontrol.ws.infra.HinemosUnknown_Exception;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.clustercontrol.ws.infra.InfraManagementNotFound_Exception;
import com.clustercontrol.ws.infra.InfraModuleInfo;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;
import com.clustercontrol.ws.infra.NotifyNotFound_Exception;

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
			InfraManagementInfo info = null;
			try {
				InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
				info = wrapper.getInfraManagement(managementId);
			} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception | InfraManagementNotFound_Exception e) {
				m_log.debug("doubleClick getInfraManagement, " + e.getMessage());
			}

			InfraModuleInfo module = null;

			if(info != null && info.getModuleList() != null){
				for(InfraModuleInfo tmpModule: info.getModuleList()){
					if(tmpModule.getModuleId().equals(moduleId)){
						module = tmpModule;
						break;
					}
				}

				CommonDialog dialog = null;
				if(module instanceof CommandModuleInfo){
					dialog = new CommandModuleDialog(m_composite.getShell(),
							managerName, managementId, moduleId,
							PropertyDefineConstant.MODE_MODIFY);
				} else if (module instanceof FileTransferModuleInfo) {
					dialog = new FileTransferModuleDialog(
							m_composite.getShell(), managerName,
							managementId, moduleId,
							PropertyDefineConstant.MODE_MODIFY);
				} else {
					throw new InternalError("dialog is null");
				}
				dialog.open();

				m_composite.update(m_composite.getManagerName(), m_composite.getManagementId());
			}
		}
	}
}