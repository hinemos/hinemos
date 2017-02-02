/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.repository.factory;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.repository.NodeSearchTask;
import com.clustercontrol.repository.bean.NodeInfoDeviceSearch;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.FacilityIdCacheInitCallback;
import com.clustercontrol.repository.util.FacilityTreeCacheRefreshCallback;
import com.clustercontrol.repository.util.RepositoryChangedNotificationCallback;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;


/**
 * ノートサーチ機能の実行管理を行う Session Bean クラス<BR>
 *
 */
public class NodeSearcher {
	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( NodeSearcher.class );

	// 二重起動を防ぐためのセマフォ
	private static final Semaphore duplicateExec = new Semaphore(1);

	public static final String MaxSearchNodeKey = "repository.node.search.max.node"; 

	private final ExecutorService _executorService = Executors.newCachedThreadPool(
			new ThreadFactory() {
		private volatile int _count = 0;
		@Override
		public Thread newThread(Runnable r) {
			String threadName = "NodeSearchWorker-" + _count++;
			m_log.debug("new thread=" + threadName);
			return new Thread(r, threadName);
		}
	});

	public List<NodeInfoDeviceSearch> searchNode(String ownerRoleId,
			String ipAddrFrom, String ipAddrTo, int port, String community,
			int version, String facilityID, String securityLevel,
			String user, String authPass, String privPass, String authProtocol,
			String privProtocol) throws HinemosUnknown, FacilityDuplicate, InvalidSetting {

		long startTime = HinemosTime.currentTimeMillis();
		// クライアントのタイムアウト(デフォルト60秒)よりも短くしておく。
		int maxMsec = HinemosPropertyUtil.getHinemosPropertyNum("repository.node.search.timeout", Long.valueOf(50 * 1000)).intValue();
		List<NodeInfoDeviceSearch> nodeList = new ArrayList<NodeInfoDeviceSearch>();

		if (duplicateExec.tryAcquire()) {
			try {
				String errMsg = MessageConstant.MESSAGE_PLEASE_SET_IPADDR_CORRECT_FORMAT.getMessage();

				//入力チェック
				if (ipAddrFrom == null || ipAddrFrom.equals("") || ipAddrTo == null || ipAddrTo.equals("")) {
					throw new HinemosUnknown(MessageConstant.MESSAGE_PLEASE_SET_SEARCH_IPADDR.getMessage());
				} else if (version == SnmpVersionConstant.TYPE_V3
						&& securityLevel.equals(SnmpSecurityLevelConstant.NOAUTH_NOPRIV) == false) {
					if(user == null || user.equals("")) {
						throw new HinemosUnknown(MessageConstant.MESSAGE_PLEASE_SET_USER_NAME.getMessage());
					} else if(authPass == null || authPass.equals("")) {
						throw new HinemosUnknown(MessageConstant.MESSAGE_PLEASE_SET_AUTHPASS_8CHARA_MINIMUM.getMessage());
					} else if (securityLevel.equals(SnmpSecurityLevelConstant.AUTH_PRIV)) {
						if(privPass == null || privPass.equals("")) {
							throw new HinemosUnknown(MessageConstant.MESSAGE_PLEASE_SET_PRIVPASS_8CHARA_MINIMUM.getMessage());
						}
					}
				}

				List<String> ipAddressList = null;

				// IPアドレスチェック
				InetAddress addressFrom;
				InetAddress addressTo;
				try {
					addressFrom = InetAddress.getByName(ipAddrFrom);
					addressTo = InetAddress.getByName(ipAddrTo);

					if (addressFrom instanceof Inet4Address && addressTo instanceof Inet4Address){
						//IPv4の場合はさらにStringをチェック
						if (!ipAddrFrom.matches(".{1,3}?\\..{1,3}?\\..{1,3}?\\..{1,3}?")){
							m_log.info(errMsg);
							throw new HinemosUnknown(errMsg);
						}
						ipAddressList = RepositoryUtil.getIpList(ipAddrFrom, ipAddrTo, 4);
					} else if (addressFrom instanceof Inet6Address && addressTo instanceof Inet6Address){
						//IPv6の場合は特にStringチェックは無し
						ipAddressList = RepositoryUtil.getIpList(ipAddrFrom, ipAddrTo, 6);
					} else {
						m_log.info(errMsg);
						throw new HinemosUnknown(errMsg);
					}
				} catch (UnknownHostException e) {
					m_log.warn(errMsg);
					throw new HinemosUnknown(errMsg);
				}

				if (m_log.isDebugEnabled()) {
					StringBuilder str = new StringBuilder();
					for (String ipAddress : ipAddressList) {
						if (str.length() != 0) {
							str.append(", ");
						}
						str.append(ipAddress);
					}
					m_log.debug("ipAddress=" + str);
				}

				List<NodeInfoDeviceSearch> searchList = new ArrayList<>();
				try {
					//ノード一覧を取得
					//256ノードより多い場合はエラーとする。
					if (ipAddressList.size() > HinemosPropertyUtil.getHinemosPropertyNum(MaxSearchNodeKey, Long.valueOf(256))) {
						m_log.info(MessageConstant.MESSAGE_EXCEED_LIMIT_NUMBER_256NODES.getMessage());
						throw new HinemosUnknown(MessageConstant.MESSAGE_EXCEED_LIMIT_NUMBER_256NODES.getMessage());
					}

					//ノードの数だけ多重起動
					//delayさせながらスレッドを立ち上げる。終わったスレッドは再利用される。(60秒間利用されないとスレッドは消える)
					List<Future<NodeInfoDeviceSearch>> list = new ArrayList<>();
					for (String ipAddress : ipAddressList) {
						if (list.size() > 0) {
							Thread.sleep(HinemosPropertyUtil.getHinemosPropertyNum("repository.node.search.delay", Long.valueOf(10)));
						}
						try {
							InetAddress address = InetAddress.getByName(ipAddress);
							List<String> facilityList = new RepositoryControllerBean().getFacilityIdByIpAddress(address);
							if(facilityList != null && 0 < facilityList.size()) {
								//ノード一覧に既にIPアドレスが存在する場合は終了
								m_log.info("ipAddress " + address + " is already registered.");
								continue;
							}
						} catch (UnknownHostException e) {
							m_log.warn("UnknownHostException : " + e.getMessage()); // ここは通らないはず
							continue;
						}
						NodeSearchTask task = new NodeSearchTask(ipAddress, port, community, version, facilityID, securityLevel,
								user, authPass, privPass, authProtocol, privProtocol);
						list.add(_executorService.submit(task));
					}

					for (Future<NodeInfoDeviceSearch> future : list) {
						if (future != null) {
							try {
								// SNMPのタイムアウトは5秒にしているが念のため、getのときにタイムアウトを指定する。
								searchList.add(future.get(maxMsec, TimeUnit.MILLISECONDS));
							} catch (TimeoutException e) {
								m_log.warn("searchNode : " + e.getClass().getName() + ", " + e.getMessage());
							}
						}
					}
				} catch (InterruptedException e) {
					m_log.warn("searchNode : " + e.getClass().getName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				} catch (ExecutionException e) {
					m_log.warn("searchNode : " + e.getClass().getName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}

				//ノード登録
				RepositoryControllerBean controller = new RepositoryControllerBean();

				boolean commitFlag = false;
				for(NodeInfoDeviceSearch searchInfo : searchList) {
					try {
						if (searchInfo == null) {
							m_log.warn("searchInfo is null");
							continue;
						}
						nodeList.add(searchInfo);
						NodeInfo nodeInfo = searchInfo.getNodeInfo();
						nodeInfo.setOwnerRoleId(ownerRoleId);
						m_log.info("nodeInfo " + nodeInfo);
						if (searchInfo.getErrorMessage() != null) {
							continue;
						}

						//タイムアウトチェック
						long msec = HinemosTime.currentTimeMillis() - startTime;
						if (msec > maxMsec) {
							m_log.info(MessageConstant.MESSAGE_TIME_OUT.getMessage() + " msec=" + msec);
							throw new HinemosUnknown(MessageConstant.MESSAGE_TIME_OUT.getMessage());
						}
						try {
							//性能改善のためリフレッシュは全ノード登録後に一度のみ行う
							controller.addNodeWithoutRefresh(nodeInfo);
							commitFlag = true;
						} catch (FacilityDuplicate | InvalidSetting | HinemosUnknown e) {
							String errorMessage = "" + e.getMessage();
							searchInfo.setErrorMessage(errorMessage);
						} finally {
						}
					} catch (Exception e) {
						m_log.warn("searchNode : " + e.getClass().getName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown(e.getMessage());
					}
				}
				if (commitFlag) {
					new FacilityIdCacheInitCallback().postCommit();

					//FacilityTreeCache更新のためにトランザクションが必要
					JpaTransactionManager jtm = new JpaTransactionManager();
					try {
						jtm.begin();
						new FacilityTreeCacheRefreshCallback().postCommit();
						jtm.commit();
					} catch (Exception e1) {
						jtm.rollback();
						throw e1;
					} finally {
						jtm.close();
					}

					new RepositoryChangedNotificationCallback().postCommit();
				}
			} catch(HinemosUnknown e) {
				throw e;
			} finally {
				m_log.info("node search : " +
						"ipAddrFrom=" + ipAddrFrom + ", ipAddrTo=" + ipAddrTo + 
						", time=" + (HinemosTime.currentTimeMillis() - startTime) + "ms");
				duplicateExec.release();
			}
		} else {
			m_log.warn("runningCheck is busy !!");
		}

		return nodeList;
	}
}
