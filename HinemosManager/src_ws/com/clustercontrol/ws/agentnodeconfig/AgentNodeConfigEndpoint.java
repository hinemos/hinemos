/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.agentnodeconfig;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NodeConfigSettingDuplicate;
import com.clustercontrol.fault.NodeConfigSettingNotFound;
import com.clustercontrol.fault.NodeHistoryRegistered;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.repository.bean.AutoRegisterResult;
import com.clustercontrol.repository.bean.NodeConfigRunCollectInfo;
import com.clustercontrol.repository.bean.NodeConfigSetting;
import com.clustercontrol.repository.factory.SearchNodeBySNMP;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.session.AutoRegisterNodeControllerBean;
import com.clustercontrol.repository.session.NodeConfigSettingControllerBean;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.util.HttpAuthenticator;
import com.sun.net.httpserver.HttpExchange;

/**
 * CMDB用のWebAPIエンドポイント<br>
 * <br>
 * 
 * @since 6.2.0
 * @version 6.2.0
 */
@MTOM
@javax.jws.WebService(targetNamespace = "http://agentnodeconfig.ws.clustercontrol.com")
public class AgentNodeConfigEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog(AgentNodeConfigEndpoint.class);

	/**
	 * [Node Config] ノード自動登録.
	 * 
	 * 
	 * @param platform
	 *            AgentのOS(OS別スコープ登録用).
	 * @param nodeNifList
	 *            AgentのNIF情報リスト(MACアドレスと主キー必須)
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @throws HinemosDbTimeout
	 */
	public AutoRegisterResult registerNode(String platform, List<NodeNetworkInterfaceInfo> nodeNifList)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, HinemosDbTimeout {
		// 引数で渡されたMACアドレスをデバッグログ出力・性能測定用に時間測定.
		long startTime = 0;
		String forLogAddress = "";
		if (nodeNifList != null && !nodeNifList.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (NodeNetworkInterfaceInfo nif : nodeNifList) {
				if (nif.getDeviceIndex() == null) {
					sb.append("Index.null[");
				} else {
					sb.append("Index." + nif.getDeviceIndex().toString() + "[");
				}
				if (nif.getNicMacAddress() == null) {
					sb.append("null");
				} else {
					sb.append(nif.getNicMacAddress());
				}
				sb.append("]");
			}
			forLogAddress = sb.toString();
		} else {
			forLogAddress = "empty";
		}
		if (m_log.isDebugEnabled()) {
			startTime = HinemosTime.currentTimeMillis();
			m_log.debug(String.format("registerNode() : start. MAC addresses=[%s]", forLogAddress));
		}

		// 権限チェック.
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// ソースIPを取得する.
		MessageContext messageContext = wsctx.getMessageContext();
		InetAddress sourceIpAddress = null;
		HttpExchange exchange = (HttpExchange) messageContext.get("com.sun.xml.internal.ws.http.exchange");
		if (exchange != null && exchange.getRemoteAddress() != null
				&& exchange.getRemoteAddress().getAddress() != null) {
			sourceIpAddress = exchange.getRemoteAddress().getAddress();
		}

		// 自動登録処理.
		AutoRegisterResult result = AutoRegisterNodeControllerBean.autoRegister(platform, nodeNifList, forLogAddress,
				sourceIpAddress);

		// 性能測定用にManager処理時間を出力.
		if (m_log.isDebugEnabled()) {
			long endTime = HinemosTime.currentTimeMillis();
			long millis = endTime - startTime;
			m_log.debug(String.format("registerNode() : end. MAC addresses=[%s], processing-time=%dms", forLogAddress,
					millis));
		}

		return result;
	}

	/**
	 * [Node Config] 対象構成情報取得
	 *
	 * HinemosAgentAccess(READ)権限が必要
	 *
	 * @param agentInfo
	 *            エージェント情報
	 * @return 対象構成情報リスト
	 * @throws NodeConfigSettingNotFound
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public List<NodeConfigSetting> getNodeConfigSetting(AgentInfo agentInfo)
			throws NodeConfigSettingNotFound, FacilityNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// ファシリティIDを取得(Agentから送られた場合は1件、Agentで設定されていない場合はMangerから全件取得).
		ArrayList<String> facilityIdList = getFacilityId(agentInfo);

		// ログ出力.
		StringBuilder facilityIdString = new StringBuilder();
		facilityIdString.append("[");
		boolean isTop = true;
		for (String facilityID : facilityIdList) {
			if (!isTop) {
				facilityIdString.append(", ");
			}
			facilityIdString.append(facilityID);
			isTop = false;
		}
		facilityIdString.append("]");

		m_log.debug("getNodeConfigSetting() : prepared list of facilityId to get setting."
				+ String.format(" facilityId count=%d, facilityId=%s", facilityIdList.size(), facilityIdString));

		return new NodeConfigSettingControllerBean().getNodeConfigSettingListByFacilityIds(facilityIdList);
	}

	/**
	 * [Node Config] 構成情報登録
	 *
	 * HinemosAgentAccess(READ)権限が必要
	 *
	 * @param registerDatetime
	 *            構成情報取得日時
	 * @param nodeInfo
	 *            登録対象の構成情報
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public void registerNodeConfigInfo(Long registerDatetime, NodeInfo nodeInfo)
			throws FacilityNotFound, NodeConfigSettingNotFound, NodeConfigSettingDuplicate,
			HinemosUnknown, InvalidSetting, InvalidUserPass, InvalidRole, NodeHistoryRegistered {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		String settingId = "";
		String facilityId = "";
		if (nodeInfo != null) {
			if (nodeInfo.getNodeConfigSettingId() != null) {
				settingId = nodeInfo.getNodeConfigSettingId();
			}
			if (nodeInfo.getFacilityId() != null) {
				facilityId = nodeInfo.getFacilityId();
			}
		}
		m_log.info("registerNodeConfigInfo : " 
				+ String.format("registerDatetime=%d, settingId=%s, facilityId=%s", registerDatetime, settingId, facilityId));
		new NodeConfigSettingControllerBean().registerNodeConfigInfo(registerDatetime, nodeInfo);
	}

	/**
	 * [Agent Base] AgentInfoからFacilityIdのリストを取得する。
	 *
	 * @param agentInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 */
	private ArrayList<String> getFacilityId(AgentInfo agentInfo) throws HinemosUnknown, FacilityNotFound {
		ArrayList<String> facilityIdList = new ArrayList<String>();

		if (agentInfo.getFacilityId() != null && !agentInfo.getFacilityId().equals("")) {
			/*
			 * agentInfoにfacilityIdが入っている場合。
			 */
			// 複数facilityId対応。
			// agentInfoの内容をカンマ(,)で分割する。
			StringTokenizer st = new StringTokenizer(agentInfo.getFacilityId(), ",");
			while (st.hasMoreTokens()) {
				String facilityId = st.nextToken();
				facilityIdList.add(facilityId);
				m_log.debug("add facilityId=" + facilityId);
			}

		} else {
			/*
			 * agentInfoにfacilityIdが入っていない場合。 この場合は、ホスト名とIPアドレスからファシリティIDを決める。
			 */
			StringBuilder ipAddressLog = new StringBuilder();
			boolean isTop = true;
			try {
				for (String ipAddress : agentInfo.getIpAddress()) {
					String hostname = agentInfo.getHostname();
					hostname = SearchNodeBySNMP.getShortName(hostname);
					ArrayList<String> list = new RepositoryControllerBean().getFacilityIdList(hostname, ipAddress);
					if (list != null && list.size() != 0) {
						for (String facilityId : list) {
							m_log.debug("facilityId=" + facilityId + ", " + agentInfo.toString());
						}
						facilityIdList.addAll(list);
					}
					if(!isTop){
						ipAddressLog.append(", ");
					}
					ipAddressLog.append(ipAddress);
					isTop=false;
				}
			} catch (Exception e) {
				m_log.warn(e, e);
				throw new HinemosUnknown("getFacilityId " + e.getMessage());
			}
			if(facilityIdList.isEmpty()){
				String hostname = agentInfo.getHostname();
				m_log.warn("getFacilityId() : Not found list of facility-ID of node for a Connected Agent." // 
						+ " hostname=[" + hostname + "]" // ホスト名.
						+ ", IP-Address=[" + ipAddressLog.toString() + "]"); // IPアドレス
				FacilityNotFound e = new FacilityNotFound("Not found list of facility-ID of node for a Connected Agent.");
				throw e;
			}
		}
		return facilityIdList;
	}

	/**
	 * [Node Config] 即時実行情報の取得
	 *
	 * HinemosAgentAccess(READ)権限が必要
	 *
	 * @param agentInfo
	 *            エージェント情報
	 * @return 対象構成情報リスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws FacilityNotFound 
	 */
	public NodeConfigRunCollectInfo getNodeConfigRunCollectInfo(AgentInfo agentInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, FacilityNotFound {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// ファシリティIDを取得(Agentから送られた場合は1件、Agentで設定されていない場合はMangerから全件取得).
		ArrayList<String> facilityIdList = getFacilityId(agentInfo);

		// ログ出力.
		StringBuilder facilityIdString = new StringBuilder();
		facilityIdString.append("[");
		boolean isTop = true;
		for (String facilityID : facilityIdList) {
			if (!isTop) {
				facilityIdString.append(", ");
			}
			facilityIdString.append(facilityID);
			isTop = false;
		}
		facilityIdString.append("]");

		m_log.debug("getNodeConfigRunCollectInfo() : prepared list of facilityId to get setting."
				+ String.format(" facilityId count=%d, facilityId=%s", facilityIdList.size(), facilityIdString));

		return new NodeConfigSettingControllerBean().getNodeConfigRunCollectInfo(facilityIdList);
	}

	/**
	 * [Node Config] 即時実行の中止.
	 *
	 * @param agentInfo
	 *            エージェント情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws FacilityNotFound 
	 */
	public void stopNodeConfigRunCollect(AgentInfo agentInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown, FacilityNotFound {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// ファシリティIDを取得(Agentから送られた場合は1件、Agentで設定されていない場合はMangerから全件取得).
		ArrayList<String> facilityIdList = getFacilityId(agentInfo);

		// ログ出力.
		StringBuilder facilityIdString = new StringBuilder();
		facilityIdString.append("[");
		boolean isTop = true;
		for (String facilityID : facilityIdList) {
			if (!isTop) {
				facilityIdString.append(", ");
			}
			facilityIdString.append(facilityID);
			isTop = false;
		}
		facilityIdString.append("]");

		m_log.debug("stopNodeConfigRunCollect() : prepared list of facilityId to stop aquireing."
				+ String.format(" facilityId count=%d, facilityId=%s", facilityIdList.size(), facilityIdString));
		new NodeConfigSettingControllerBean().stopNodeConfigRunCollect(facilityIdList);
	}
	/**
	 * [Node Config] NodeInfoの取得.
	 * 
	 * 構成情報取得スクリプト実行時に、エージェントにノードのSNMP情報を渡す
	 *
	 * @param agentInfo
	 *            エージェント情報
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 * @return NodeInfo
	 * @throws InvalidRole 
	 * @throws InvalidUserPass 
	 */
	public List<NodeInfo> getNodeInfoList(AgentInfo agentInfo) throws HinemosUnknown, FacilityNotFound, InvalidUserPass, InvalidRole{
		//権限チェック
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		ArrayList<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
		//Agentに紐づいているFacilityIDを取得;
		ArrayList<String> facilityIdList = getFacilityId(agentInfo);
		RepositoryControllerBean rb = new RepositoryControllerBean();
		//FacilityIDごとのNodeInfoを取得
		for (String facilityID : facilityIdList){
			nodeInfoList.add(rb.getNode(facilityID));
		}
		// ログ出力.
		StringBuilder facilityIdString = new StringBuilder();
		facilityIdString.append("[");
		boolean isTop = true;
		for (String facilityID : facilityIdList) {
			if (!isTop) {
				facilityIdString.append(", ");
			}
			facilityIdString.append(facilityID);
			isTop = false;
		}
		facilityIdString.append("]");

		m_log.debug("getNodeInfoList() : prepared list of NodeInfo to send to agent."
				+ String.format(" facilityId count=%d, facilityId=%s", facilityIdList.size(), facilityIdString));
		
		return nodeInfoList;
	}
}
