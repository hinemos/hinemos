/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.SnmpResponseError;
import com.clustercontrol.monitor.bean.EventConfirmConstant;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.bean.DeviceSearchMessageInfo;
import com.clustercontrol.repository.bean.NodeInfoDeviceSearch;
import com.clustercontrol.repository.bean.NodeRegisterFlagConstant;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * 自動デバイスサーチ処理の実装クラス
 */
public class DeviceSearchTask implements Callable<Boolean> {

	private static Log m_log = LogFactory.getLog(DeviceSearchTask.class);
	private String facilityId;

	/**
	 * コンストラクタ
	 * @param facilityId 処理対象のファシリティID
	 */
	public DeviceSearchTask(String facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * 自動デバイスサーチ処理の実行
	 */
	@Override
	public Boolean call() {
		m_log.debug("run() start");
		boolean isExceptionOccur = false;
		boolean isOutPutLog = false;
		NodeInfoDeviceSearch nodeDeviceSearch = null;

		try {
			String user = HinemosPropertyCommon.repository_auto_device_user.getStringValue();
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, user);
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR,
					new AccessControllerBean().isAdministrator());
			RepositoryControllerBean controller = new RepositoryControllerBean();

			//ノードの数だけSNMPでノード情報を再取得
			NodeInfo node = controller.getNode(facilityId);

			if (node.getAutoDeviceSearch() == false) {
				return isOutPutLog;
			}

			nodeDeviceSearch = controller.getNodePropertyBySNMP(
					node.getAvailableIpAddress(),
					node.getSnmpPort(),
					node.getSnmpCommunity(),
					node.getSnmpVersion(),
					facilityId,
					node.getSnmpSecurityLevel(),
					node.getSnmpUser(),
					node.getSnmpAuthPassword(),
					node.getSnmpPrivPassword(),
					node.getSnmpAuthProtocol(),
					node.getSnmpPrivProtocol()
					);

			if (nodeDeviceSearch.getDeviceSearchMessageInfo() != null
					&& nodeDeviceSearch.getDeviceSearchMessageInfo().size() > 0) {
				// 変更ありの場合はDB更新
				// 自動デバイスサーチでは更新しないものはフラグを設定する
				NodeInfo newNodeInfo = nodeDeviceSearch.getNewNodeInfo();
				newNodeInfo.setNodeMemoryRegisterFlag(NodeRegisterFlagConstant.NOT_GET);
				newNodeInfo.setNodeNetstatRegisterFlag(NodeRegisterFlagConstant.NOT_GET);
				newNodeInfo.setNodeProcessRegisterFlag(NodeRegisterFlagConstant.NOT_GET);
				newNodeInfo.setNodePackageRegisterFlag(NodeRegisterFlagConstant.NOT_GET);
				newNodeInfo.setNodeProductRegisterFlag(NodeRegisterFlagConstant.NOT_GET);
				newNodeInfo.setNodeLicenseRegisterFlag(NodeRegisterFlagConstant.NOT_GET);
				controller.modifyNode(newNodeInfo);
				//変更ありの場合はイベント登録
				isOutPutLog = true;
			}
		} catch (SnmpResponseError e) {
			//SNMPの応答エラー時にイベント登録有無を取得
			isOutPutLog = HinemosPropertyCommon.repository_auto_device_find_log.getBooleanValue();
		} catch (Exception e) {
			m_log.warn("run() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			isExceptionOccur = true;
			//SNMPの応答エラー時にイベント登録有無を取得
			isOutPutLog = HinemosPropertyCommon.repository_auto_device_find_log.getBooleanValue();
		} finally {
			//イベントログの登録
			if (isOutPutLog) {
				String details = "";
				ArrayList<DeviceSearchMessageInfo> list;
				if (nodeDeviceSearch != null) {
					list = nodeDeviceSearch.getDeviceSearchMessageInfo();
				} else {
					return isOutPutLog;
				}

				for (DeviceSearchMessageInfo msgInfo : list) {
					details = details.length() > 0 ? details + ", " : details;
					details = details + msgInfo.getItemName() + " "
							+ MessageConstant.LASTTIME.getMessage() + ":" + msgInfo.getLastVal() + " "
							+ MessageConstant.THISTIME.getMessage() + ":" + msgInfo.getThisVal();
				}
				putEvent(isExceptionOccur, facilityId, details);
			}
		}
		return isOutPutLog;
	}

	private void putEvent(boolean isExceptionOccur, String facilityID, String details) {
		//現在日時取得
		Date nowDate = HinemosTime.getDateInstance();
		String msg = null;
		int priority = 0;

		if (!isExceptionOccur) {
			msg = MessageConstant.MESSAGE_EXECUTED_AUTO_SEARCH_DEVICES.getMessage() + " " + details;
			priority = PriorityConstant.TYPE_INFO;
		} else {
			msg = MessageConstant.MESSAGE_FAILED_AUTO_SEARCH_DEVICES.getMessage();
			priority = PriorityConstant.TYPE_WARNING;
		}

		//メッセージ情報作成
		OutputBasicInfo output = new OutputBasicInfo();
		output.setPluginId(HinemosModuleConstant.REPOSITORY_DEVICE_SEARCH);
		output.setMonitorId(HinemosModuleConstant.SYSYTEM);
		output.setFacilityId(facilityID);
		output.setScopeText(facilityID);
		output.setApplication("");
		output.setMessage(msg);
		output.setMessageOrg(msg);
		output.setPriority(priority);
		output.setGenerationDate(nowDate.getTime());

		try {
			new NotifyControllerBean().insertEventLog(output, EventConfirmConstant.TYPE_UNCONFIRMED);
		} catch (Exception e ) {
			m_log.error(e.getMessage(), e);
		}
	}
}
