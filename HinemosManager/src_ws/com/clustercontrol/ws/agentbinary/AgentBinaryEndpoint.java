/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.agentbinary;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.binary.bean.BinaryResultDTO;
import com.clustercontrol.binary.session.BinaryControllerBean;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.repository.factory.SearchNodeBySNMP;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * バイナリ監視用のWebAPIエンドポイント
 * 
 * @since 6.1.0
 * @version 6.1.0
 */
@MTOM
@javax.jws.WebService(targetNamespace = "http://agentbinary.ws.clustercontrol.com")
public class AgentBinaryEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog(AgentBinaryEndpoint.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/**
	 * [Binary] バイナリ監視の監視設定を取得
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param agentInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getMonitorBinary(AgentInfo agentInfo)
			throws MonitorNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getMonitorBinary : " + agentInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		ArrayList<String> facilityIdList = new ArrayList<String>();

		try {
			facilityIdList.addAll(getFacilityId(agentInfo));
		} catch (Exception e) {
			m_log.warn(e, e);
			return null;
		}

		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		BinaryControllerBean bean = new BinaryControllerBean();
		for (String facilityId : facilityIdList) {
			list.addAll(bean.getBinaryListForFacilityId(facilityId, true));
		}
		return list;
	}

	/**
	 * echo(WebサービスAPI疎通用)
	 *
	 * 権限必要なし（ユーザ名チェックのみ実施）
	 *
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String echo(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}

	/**
	 * [Binary] バイナリ監視でマッチしたものに対して通知をマネージャに依頼する<br>
	 * 1 HTTP Requestで多数の監視実行結果を送信できるため、リソース観点から効率的に処理できる<br>
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void forwardBinaryResult(List<BinaryResultDTO> resultList, AgentInfo agentInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// ファシリティIDを取得(Agentから送られた場合は1件、Agentで設定されていない場合はMangerから全件取得).
		ArrayList<String> facilityIdList = getFacilityId(agentInfo);
		if (facilityIdList == null || facilityIdList.isEmpty()) {
			m_log.warn(methodName + DELIMITER
					+ "skip to process after receiving binary result because facilityID is empty.");
			return;
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

		m_log.info(methodName + DELIMITER + String.format("received binary result. facilityId count=%d, facilityId=%s",
				facilityIdList.size(), facilityIdString));

		for (String facilityId : facilityIdList) {
			new BinaryControllerBean().run(facilityId, resultList);
		}
	}

	/**
	 * [Agent Base] AgentInfoからFacilityIdのリストを取得する。
	 *
	 * @param agentInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	private ArrayList<String> getFacilityId(AgentInfo agentInfo) throws HinemosUnknown {
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
				}
			} catch (Exception e) {
				m_log.warn(e, e);
				throw new HinemosUnknown("getFacilityId " + e.getMessage());
			}
		}
		return facilityIdList;
	}

}
