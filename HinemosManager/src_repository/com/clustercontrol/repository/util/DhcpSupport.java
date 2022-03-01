/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.UserIdConstant;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.bean.IpAddressInfo;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.repository.factory.SearchNodeBySNMP;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.NetworkInterfaceUtil;
import com.clustercontrol.util.SubnetAddress;

/**
 * DHCPサポート機能を提供します。
 */
public class DhcpSupport {
	private static final Log m_log = LogFactory.getLog(DhcpSupport.class);

	/**
	 * DHCPサポート機能によりエージェントの情報を用いてノードを更新する。
	 * Agentプロパティ"dhcp.update.mode"によって更新方法が決まる。
	 * @throws HinemosUnknown 
	 */
	public static void updateNodes(AgentInfo agentInfo, String remoteIp) throws HinemosUnknown {
		m_log.debug(String.format("updateNodes(): DHCP update mode=%s, remote IP Address=%s", agentInfo.getDhcpUpdateMode().name(), remoteIp));

		switch (agentInfo.getDhcpUpdateMode()) {
		case ip:
			// Hinemosプロパティdhcp.autoupdate.ipaddress.enableをチェック
			if (!HinemosPropertyCommon.dhcp_autoupdate_ipaddress_enable.getBooleanValue()) {
				return;
			}

			// 送信元IPがHinemosプロパティdhcp.autoupdate.ipaddress.cidrの範囲内かチェック
			if (!checkIpAddressBelongsToCidrProp(remoteIp, HinemosPropertyCommon.dhcp_autoupdate_ipaddress_cidr)) {
				return;
			}
			
			// 同一ホスト名のノードにエージェントのIPアドレスを設定する。
			updateNodesIpAddress(agentInfo, remoteIp);
			return;
		case host:
			// Hinemosプロパティdhcp.autoupdate.hostname.enableをチェック
			if (!HinemosPropertyCommon.dhcp_autoupdate_hostname_enable.getBooleanValue()) {
				return;
			}
			
			// 送信元IPがHinemosプロパティdhcp.autoupdate.ipaddress.cidrの範囲内かチェック
			if (!checkIpAddressBelongsToCidrProp(remoteIp, HinemosPropertyCommon.dhcp_autoupdate_hostname_cidr)) {
				return;
			}
			
			// 同一IPアドレスのノードにエージェントのホスト名を設定する。
			updateNodesHostName(agentInfo, remoteIp);
			return;
		case disable:
		default:
			// エージェントでDHCPサポートが無効なため、更新を行わない。
			return;
		}
	}

	/**
	 * エージェントと同一ホスト名のノードにエージェントのIPアドレスを設定する。
	 * また、エージェントと同一IPアドレスのノードを無効化する。
	 * @throws HinemosUnknown 
	 */
	private static void updateNodesIpAddress(AgentInfo agentInfo, String remoteIp) throws HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			String execUser = HinemosSessionContext.getLoginUserId();
			// 管理者ユーザで変更
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, UserIdConstant.HINEMOS);
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, true);
			
			jtm.begin();

			RepositoryControllerBean bean = new RepositoryControllerBean();
			IpAddressInfo ipInfo = NetworkInterfaceUtil.getIpAddressInfo(remoteIp);
			
			String hostname = SearchNodeBySNMP.getShortName(agentInfo.getHostname());

			// エージェントと同一ホスト名(ノード名)のノードを検索
			Set<String> facilityIds = bean.getNodeListAllByNodename(hostname);
			if (facilityIds == null) {
				m_log.debug(String.format("no nodes exist having nodename=%s", hostname));

				facilityIds = Collections.emptySet();
			} else {
				m_log.debug(String.format("update target nodes:%s for IP Address=%s", facilityIds, remoteIp));
			}

			// ノード情報更新
			for (String facilityId: facilityIds) {
				boolean modified = false;
				NodeInfo node = bean.getNode(facilityId);
				
				// 管理対象フラグを有効にする。
				if (!node.getValid()) {
					node.setValid(true);
					modified = true;
				}
				
				switch (ipInfo.getVersion()) {
				case IPV4:
					if (!node.getIpAddressV4().equals(remoteIp)) {
						node.setIpAddressVersion(4);
						node.setIpAddressV4(remoteIp);
						node.setIpAddressV6("");
						modified = true;
					}
					break;
				case IPV6:
				default:
					if (!node.getIpAddressV6().equals(remoteIp)) {
						node.setIpAddressVersion(6);
						node.setIpAddressV6(remoteIp);
						node.setIpAddressV4("");
						modified = true;
					}
					break;
				}
				
				if (modified) {
					bean.modifyNode(node);
					m_log.info(String.format("updateNodesIpAddress(): set IP Address=%s to %s by DHCP support", remoteIp, node.getFacilityId()));
				}
			}
			
			// エージェントと同一IPを持ち、エージェントと異なるノードを無効化する。
			// エージェントと同一IPアドレスを持つノードを検索
			Set<String> sameIpFacilityIds = bean.getNodeListAllByIpAddress(ipInfo.getInetAddress());
			if (sameIpFacilityIds == null) {
				m_log.debug(String.format("no nodes have IP Address=%s", remoteIp));
				sameIpFacilityIds = Collections.emptySet();
			} else {
				m_log.debug(String.format("nullify nodes:%s having IP Address=%s", sameIpFacilityIds, remoteIp));
			}

			for (String facilityId: sameIpFacilityIds) {
				if (!facilityIds.contains(facilityId)) {
					// エージェントと同一IPを持ち、かつエージェントとノード名が異なるホストは、管理対象フラグをOFFにし、無効を表すIPアドレスに変更する。
					nullfyNode(facilityId);
				}
			}
			
			jtm.commit();
			
			// ThreadLocalを戻す
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, execUser);
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, new AccessControllerBean().isAdministrator());
		} catch (FacilityNotFound | InvalidRole | InvalidSetting e) {
			// 通常通らない想定
			m_log.warn(e.getMessage(), e);
		}
	}

	/**
	 * エージェントと同一IPアドレスのノードにエージェントのホスト名を設定する。
	 * @throws HinemosUnknown 
	 */
	private static void updateNodesHostName(AgentInfo agentInfo, String remoteIp) throws HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			String execUser = HinemosSessionContext.getLoginUserId();
			// 管理者ユーザで変更
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, UserIdConstant.HINEMOS);
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, true);

			jtm.begin();

			RepositoryControllerBean bean = new RepositoryControllerBean();
			IpAddressInfo ipInfo = NetworkInterfaceUtil.getIpAddressInfo(remoteIp);

			String hostname = SearchNodeBySNMP.getShortName(agentInfo.getHostname());

			// エージェントと同一IPアドレスのノードを検索
			Set<String> facilityIds = bean.getNodeListAllByIpAddress(ipInfo.getInetAddress());
			if (facilityIds == null) {
				m_log.debug(String.format("no nodes have IP Address=%s", remoteIp));
				facilityIds = Collections.emptySet();
			} else {
				m_log.debug(String.format("update target nodes:%s for hostname=%s", facilityIds, hostname));
			}
			
			// ノード情報更新
			for (String facilityId: facilityIds) {
				NodeInfo node = bean.getNode(facilityId);
				boolean modified = false;
				
				// 管理対象フラグを有効にする。
				if (!node.getValid()) {
					node.setValid(true);
					modified = true;
				}

				// ノード名更新
				if (!node.getNodeName().equals(hostname)) {
					node.setNodeName(hostname);
					modified = true;
				}

				if (modified) {
					bean.modifyNode(node);
					m_log.info(String.format("updateNodesIpAddress(): set hostname=%s to %s by DHCP Support", hostname, node.getFacilityId()));
				}
			}
			
			jtm.commit();
			
			// ThreadLocalを戻す
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, execUser);
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, new AccessControllerBean().isAdministrator());

		} catch (InvalidRole | InvalidSetting | FacilityNotFound e) {
			// 通常通らない想定
			m_log.warn(e.getMessage(), e);
		}
	}
	
	
	
	/**
	 * 対象ファシリティID一覧のノードの管理対象フラグをOFFにし、無効を表すIPアドレスに変更する。
	 * @throws HinemosUnknown 
	 * @throws FacilityNotFound
	 */
	public static void nullfyNodes(String...facilityIds) throws HinemosUnknown, FacilityNotFound {
		// Hinemosプロパティdhcp.autoupdate.ipaddress.enableをチェック
		if (!HinemosPropertyCommon.dhcp_autoupdate_ipaddress_enable.getBooleanValue()) {
			return;
		}

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			String execUser = HinemosSessionContext.getLoginUserId();
			// 管理者ユーザで変更
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, UserIdConstant.HINEMOS);
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, true);
			
			jtm.begin();

			for (String facilityId : facilityIds) {
				nullfyNode(facilityId);
			}
			
			jtm.commit();

			// ThreadLocalを戻す
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, execUser);
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, new AccessControllerBean().isAdministrator());
		} catch (InvalidRole | InvalidSetting e) {
			// 通常通らない想定
			m_log.warn(e.getMessage(), e);
		}
	}
	
	/**
	 * 対象ファシリティID一覧のノードの管理対象フラグをOFFにし、無効を表すIPアドレスに変更する。
	 * @throws HinemosUnknown 
	 * @throws FacilityNotFound
	 */
	public static void nullfyNodes(List<String> facilityIds) throws HinemosUnknown, FacilityNotFound {
		nullfyNodes(facilityIds.toArray(new String[facilityIds.size()]));
	}

	/**
	 * ノードの管理対象フラグをOFFにし、無効を表すIPアドレスに変更する。
	 * @throws HinemosUnknown 
	 * @throws InvalidRole 
	 * @throws InvalidSetting 
	 * @throws FacilityNotFound 
	 */
	private static void nullfyNode(String facilityId) throws HinemosUnknown, InvalidRole, InvalidSetting, FacilityNotFound {
		NodeInfo node = new RepositoryControllerBean().getNode(facilityId);

		node.setValid(false);
		node.setIpAddressVersion(4);
		node.setIpAddressV4(HinemosPropertyCommon.dhcp_autoupdate_ipaddress_notavailable.getStringValue());
		node.setIpAddressV6("");

		new RepositoryControllerBean().modifyNode(node);
		m_log.info(facilityId + " is nullified by DHCP Support.");
	}

	/**
	 * IPアドレスがHinemosプロパティで定義されたCIDR(カンマ区切り)の範囲内に含まれているかチェックする。
	 */
	private static boolean checkIpAddressBelongsToCidrProp(String ipAddress, HinemosPropertyCommon cidrProp) {
		String[] cidrs = cidrProp.getStringValue().replaceAll("\\s*", "").split(",");
		for (String cidr : cidrs) {
			try {
				SubnetAddress subnet = new SubnetAddress(cidr);
				if (subnet.contains(ipAddress)) {
					// IPアドレスがCIDRの範囲内に含まれていた場合
					m_log.debug(String.format("%s belongs to %s=%s", ipAddress, cidrProp.getKey(), cidrProp.getStringValue()));
					return true;
				}
			} catch (UnknownHostException e) {
				// 不正なCIDRが指定された場合
				m_log.warn(String.format("Invalid CIDR Expression: %s=%s", cidrProp.getKey(), cidrProp.getStringValue()));
				return false;
			}
		}

		m_log.debug(String.format("%s don't belongs to %s=%s", ipAddress, cidrProp.getKey(), cidrProp.getStringValue()));
		return false;
	}
}
