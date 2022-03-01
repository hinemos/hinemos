/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.repository;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtNodeNetworkInterfaceInfoRequest;
import org.openapitools.client.model.RegisterNodeRequest;
import org.openapitools.client.model.RegisterNodeResponse;
import org.openapitools.client.model.RegisterNodeResponse.ResultStatusEnum;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.AgentNodeConfigRestClientWrapper;
import com.clustercontrol.agent.repository.NodeConfigConstant.Function;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.repository.util.RepositoryBeanUtil;

/**
 * ノード自動登録<BR>
 * <br>
 * Agent起動時に実行するノード自動登録に関する処理クラス.
 * 
 * @since 6.2.0
 * @version 6.2.0
 */
public class NodeRegister {

	// ロガー
	private static Log m_log = LogFactory.getLog(NodeRegister.class);

	// ログ出力区切り文字
	private static final String DELIMITER = "() : ";

	/**
	 * 自動登録処理<br>
	 * <br>
	 * 登録済かどうかの識別用にMACアドレスを取得してManagerの自動登録処理を呼び出す.
	 * 
	 * @return 再試行が必要かどうか<br>
	 *		   true:Manager接続失敗(Manager接続以外のエラー含む)<br>
	 *		   false: 自動登録成功もしくは登録不要
	 */
	public static boolean callRegister() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start to get MAC addresses for automatically registering.");

		// Agent.propertiesで無効な場合は処理スキップ.
		String validKey = "automatically.register.node";
		String validValue = AgentProperties.getProperty(validKey, "false");
		if (validValue == null) {
			m_log.info(methodName + DELIMITER
					+ String.format(
							"skip to register this node, because the property doese not exist on Agent.properties. key=[%s]",
							validKey));
			return false;
		}
		if (!Boolean.parseBoolean(validValue)) {
			m_log.info(methodName + DELIMITER
					+ String.format(
							"skip to register this node, because it's invalid on Agent.properties. key=[%s], value=[%s]",
							validKey, validValue));
			return false;
		}
		m_log.debug(methodName + DELIMITER
				+ String.format("valid to register this node on Agent.properties. key=[%s]", validKey));

		// OSを識別(Solarisもいったん識別・Manager側で区別).
		String osName = System.getProperty("os.name");
		String platform = RepositoryBeanUtil.getPlatform(osName, true);

		// NICの主キー・MACアドレスを取得する.
		List<AgtNodeNetworkInterfaceInfoRequest> nodeNifList = new NodeConfigCollector(Function.NODE_REGISTER).getNICList();
		if (nodeNifList == null || nodeNifList.isEmpty()) {
			m_log.warn(methodName + DELIMITER
					+ "failed to get NIC by script to register node automatically. check out logs and settings.");
			return true;
		}

		// NICが取得できているか確認してログ出力.
		if (m_log.isDebugEnabled()) {
			String nifStr = "";
			if (!nodeNifList.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				boolean top = true;
				for (AgtNodeNetworkInterfaceInfoRequest nif : nodeNifList) {
					if (!top) {
						sb.append(",");
					}
					if (nif == null) {
						sb.append("null");
						continue;
					}
					if (nif.getDeviceIndex() == null) {
						sb.append("Index.null[");
					} else {
						sb.append("Index." + nif.getDeviceIndex().toString() + "[");
					}
					sb.append("Device Name:");
					if (nif.getDeviceName() == null) {
						sb.append("null");
					} else {
						sb.append(nif.getDeviceName());
					}
					sb.append(", ");
					sb.append("MAC Address:");
					if (nif.getNicMacAddress() == null) {
						sb.append("null");
					} else {
						sb.append(nif.getNicMacAddress());
					}
					sb.append(", ");
					sb.append("IP Address:");
					if (nif.getNicIpAddress() == null) {
						sb.append("null");
					} else {
						sb.append(nif.getNicIpAddress());
					}
					sb.append("]");
					top = false;
				}
				nifStr = sb.toString();
			}
			m_log.debug(methodName + DELIMITER
					+ String.format("complete NodeConfigCollector(dto).getNICList(). NICList=[%s]", nifStr));
		}

		// Managerの自動登録処理を呼び出す.
		RegisterNodeResponse registerResult;
		try {
			RegisterNodeRequest req = new RegisterNodeRequest();
			req.setPlatform(platform);
			req.setNodeNifList(nodeNifList);
			registerResult = AgentNodeConfigRestClientWrapper.registerNode(req);
		} catch (HinemosUnknown | InvalidRole | InvalidSetting | InvalidUserPass | HinemosDbTimeout e) {
			m_log.warn(methodName + DELIMITER + "failed to register this node automatically."
					+ " for more information, see 'hinemos_manager.log'.", e);
			return true;
		} catch (RestConnectFailed e) {
			m_log.info(methodName + DELIMITER + "failed to register this node automatically, to retry.");
			return true;
		}

		// Managerの処理結果を元にハンドリング.
		switch (registerResult.getResultStatus()) {
		case REGISTERED:
			// 正常登録(処理続行).
			m_log.info(methodName + DELIMITER + "registered automatically on hinemos-manager.");
			break;
		case INVALID:
			// 自動登録無効.
			m_log.info(methodName + DELIMITER
					+ "skip to register this node, because auto register is invalid on manager.");
			return false;
		case EXIST:
			// 登録済ノード(Agent.propertiesにfacilityID反映できてない場合もあるので処理続行).
			m_log.debug(methodName + DELIMITER + "skip to register this node, because it was registered.");
			break;
		default:
			// 想定外.
			m_log.warn(methodName + DELIMITER + "failed to register this node."
					+ " for more information, see 'hinemos_manager.log'.");
			return true;
		}

		// 正常登録後の処理.
		String registeredId = registerResult.getFacilityId();
		if (registeredId == null || registeredId.isEmpty()) {
			// 想定外.
			m_log.warn(methodName + DELIMITER + "failed to update facilityID on Agent.properties.");
			return true;
		}

		// Agent.propertiesファイルに登録されているファシリティIDを取得する.
		String agentId = Agent.getAgentInfoRequest().getFacilityId();
		String facilityIdKey = "facilityId";
		if (agentId != null && !agentId.isEmpty()) {
			if (registerResult.getResultStatus() == ResultStatusEnum.EXIST
					&& agentId.equals(registerResult.getFacilityId())) {
				// Manager登録済・かつFacilityIDがちゃんとAgent.propertiesに反映されている場合.
				m_log.debug(methodName + DELIMITER
						+ String.format("skip to update '%s' on Agent.properties. %s[old]=%s, %s[new]=%s",
								facilityIdKey, facilityIdKey, agentId, facilityIdKey, registeredId));
				return false;
			}
			// 上記以外の場合は既存のFacilityIDを上書きするのでログ出力.
			m_log.info(
					methodName + DELIMITER + String.format("to update '%s' on Agent.properties. %s[old]=%s, %s[new]=%s",
							facilityIdKey, facilityIdKey, agentId, facilityIdKey, registeredId));
		}

		// ファシリティIDをプロパティファイルに反映.
		AgentProperties.updateProperty(facilityIdKey, registeredId);
		m_log.info(methodName + DELIMITER + "succeded to register this node automatically.");
		// 更新されたプロパティをメモリ上にも反映.
		Agent.getAgentInfoRequest().setFacilityId(registeredId);
		return false;
	}

}
