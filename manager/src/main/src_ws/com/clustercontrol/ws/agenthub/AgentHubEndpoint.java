/*
Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
 */
package com.clustercontrol.ws.agenthub;

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
import com.clustercontrol.custom.bean.CommandResultDTO;
import com.clustercontrol.custom.session.MonitorCustomControllerBean;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.logfile.bean.LogfileResultDTO;
import com.clustercontrol.logfile.session.MonitorLogfileControllerBean;
import com.clustercontrol.repository.factory.SearchNodeBySNMP;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.winevent.bean.WinEventResultDTO;
import com.clustercontrol.winevent.session.MonitorWinEventControllerBean;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * ジョブ操作用のWebAPIエンドポイント
 */
@MTOM
@javax.jws.WebService(targetNamespace = "http://agenthub.ws.clustercontrol.com")
public class AgentHubEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( AgentHubEndpoint.class );

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
	 * [Custom Monitor] コマンド監視において、コマンドの実行結果をまとめてマネージャに通知する。
	 * 1 HTTP Requestで多数のコマンド実行結果を送信できるため、リソース観点から効率的に処理できる。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param resultList コマンドの実行結果のリスト
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public void forwardCustomResult(List<CommandResultDTO> resultList)  throws InvalidUserPass, InvalidRole, HinemosUnknown
	{
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// MAIN
		try {
			new MonitorCustomControllerBean().evalCommandResult(resultList);
		} catch (MonitorNotFound e) {
			m_log.warn("monitor not found... " + e.getMessage());
		} catch (HinemosUnknown e) {
			m_log.warn("unexpected internal failure occurred...", e);
			throw e;
		} catch (CustomInvalid e) {
			m_log.warn("configuration of command monitor is not valid...", e);
		}
	}
	

	/**
	 * [Logfile] ログファイル監視でマッチしたものに対して通知をマネージャに依頼する。
	 * 1 HTTP Requestで多数の監視実行結果を送信できるため、リソース観点から効率的に処理できる。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void forwardLogfileResult(List<LogfileResultDTO> resultList, AgentInfo agentInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		for (String facilityId : getFacilityId(agentInfo)) {
			new MonitorLogfileControllerBean().run(facilityId, resultList);
		}
	}
	
	/**
	 * [WinEvent] Windowsイベント監視でマッチしたものに対して通知をマネージャに依頼する。
	 * 1 HTTP Requestで多数の監視実行結果を送信できるため、リソース観点から効率的に処理できる。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void forwardWinEventResult(List<WinEventResultDTO> resultList, AgentInfo agentInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		for (String facilityId : getFacilityId(agentInfo)) {
			new MonitorWinEventControllerBean().run(facilityId, resultList);
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
			 * agentInfoにfacilityIdが入っていない場合。
			 * この場合は、ホスト名とIPアドレスからファシリティIDを決める。
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
				m_log.warn(e,e);
				throw new HinemosUnknown("getFacilityId " + e.getMessage());
			}
		}
		return facilityIdList;
	}
}
