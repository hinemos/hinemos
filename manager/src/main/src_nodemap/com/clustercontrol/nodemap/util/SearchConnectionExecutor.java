/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.nodemap.bean.Association;
import com.clustercontrol.poller.impl.Snmp4jPollerImpl;
import com.clustercontrol.poller.util.DataTable;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;


public class SearchConnectionExecutor {
	// ログ
	private static Log m_log = LogFactory.getLog( SearchConnectionExecutor.class );

	private long now;
	private long start;
	//
	private final ExecutorService _executor;

	//
	private final boolean isL3;
	private final Set<String> oidSet;
	private final List<String> facilityIdList;
	private final RepositoryControllerBean bean = new RepositoryControllerBean();
	private ConcurrentHashMap<String, List<String>>macFacilityMap;

	/**
	 * 接続状況を取得するExecutorService<BR>
	 *
	 * @param scopeId スコープのファシリティID
	 * @param isL3 L3の接続の取得ならtrue L2の接続の取得ならfalseが入る
	 * @throws HinemosUnknown
	 */
	public SearchConnectionExecutor(String scopeId, boolean isL3) throws HinemosUnknown {
		start = HinemosTime.currentTimeMillis();
		this.isL3 = isL3;

		// 同時にSNMPリクエストするスレッド数
		int threadSize = HinemosPropertyCommon.nodemap_search_connection_thread.getIntegerValue();
		m_log.info("static() : Thread Size = " + threadSize);
		m_log.info("SearchConnectionExecutor() : scopeId="+scopeId+",L3="+isL3);

		// 取得する接続状況に応じてOIDを設定する
		String oid = isL3 ? SearchConnectionProperties.DEFAULT_OID_ARP : SearchConnectionProperties.DEFAULT_OID_FDB;
		oidSet = new HashSet<String>();
		oidSet.add(oid);

		facilityIdList = bean.getFacilityIdList(scopeId, RepositoryControllerBean.ONE_LEVEL);

		_executor = new MonitoredThreadPoolExecutor(threadSize, threadSize,
				0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(),
				new ThreadFactory() {
			private volatile int _count = 0;
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "SearchConnectionExecutor-" + _count++);
			}
		}, new ThreadPoolExecutor.AbortPolicy());

		now = HinemosTime.currentTimeMillis();
		m_log.debug("Constructer : " + (now - start) + "ms");
	}

	public List<Association> execute() throws Exception {
		List<Association> result = new ArrayList<Association>();
		List<Future<List<Association>>> futures = new ArrayList<Future<List<Association>>>();
		// スコープに含まれるノードとMACアドレスのマップを作成する
		// ひとつのノードが複数のファシリティIDで登録される場合があるので、ファシリティIDのリストを保持する
		macFacilityMap = new ConcurrentHashMap<String, List<String>>();
		for (String id : facilityIdList) {
			if (!bean.isNode(id)) {
				continue;
			}

			NodeInfo node = bean.getNode(id);
			for (NodeNetworkInterfaceInfo info :node.getNodeNetworkInterfaceInfo()) {
				// NICのMACアドレスを大文字に変換して取得する
				String nicMacAddress = info.getNicMacAddress().toUpperCase();
				if (!macFacilityMap.containsKey(nicMacAddress)) {
					// まだマップに登録されていないMACアドレスの場合
					macFacilityMap.putIfAbsent(nicMacAddress, new ArrayList<String>());
				}
				macFacilityMap.get(nicMacAddress).add(id);
			}
		}
		now = HinemosTime.currentTimeMillis();
		m_log.debug("get mac map : " + (now - start) + "ms");

		for (String facilityId : facilityIdList) {
			if (!bean.isNode(facilityId)) {
				// スコープだったら何もしない
				continue;
			}

			NodeInfo node = bean.getNode(facilityId);

			if (!node.getPlatformFamily().equals("NW_EQUIPMENT")) {
				// ネットワーク機器でない場合も何もしない
				continue;
			}

			if (isL3 && !node.getHardwareType().equals("L3")) {
				// L3の接続状況を取得する場合はハードウェアタイプに「L3」と入っていないものも対象外
				continue;
			}

			futures.add(_executor.submit(new SearchConnection(node)));
		}

		// スイッチへの問い合わせが完了するまで待つ
		_executor.shutdown();

		// 得られた関連を全結合して返す
		for (Future<List<Association>> future : futures) {
			result.addAll(future.get());
		}
		now = HinemosTime.currentTimeMillis();
		m_log.debug("end : " + (now - start) + "ms");
		return result;
	}

	public class SearchConnection implements Callable<List<Association>> {
		private NodeInfo node;

		public SearchConnection(NodeInfo node) {
			this.node = node;
		}

		@Override
		public List<Association> call() throws Exception {
			List<Association> result = new ArrayList<Association>();
			DataTable tmpTable = null;

			m_log.info("SearchConnection() : polling to " + node.getFacilityId());

			tmpTable = Snmp4jPollerImpl.getInstance().polling(
					node.getAvailableIpAddress(),
					node.getSnmpPort(),
					node.getSnmpVersion(),
					node.getSnmpCommunity(),
					node.getSnmpRetryCount(),
					node.getSnmpTimeout(),
					oidSet,
					node.getSnmpSecurityLevel(),
					node.getSnmpUser(),
					node.getSnmpAuthPassword(),
					node.getSnmpPrivPassword(),
					node.getSnmpAuthProtocol(),
					node.getSnmpPrivProtocol());

			now = HinemosTime.currentTimeMillis();
			m_log.debug("polling done (" + node.getFacilityId() +") : " + (now - start) + "ms");

			for (String key : tmpTable.keySet()) {
				// NW機器から取得したMACアドレスを大文字に変換したものを格納する
				String macAddress = tmpTable.getValue(key).getValue().toString().toUpperCase();
				if (!macFacilityMap.containsKey(macAddress)) {
					continue;
				}
				for (String target : macFacilityMap.get(macAddress)) {
					Association asso = new Association(node.getFacilityId(), target);
					m_log.debug("call() : " + asso.getSource() + " -> " +asso.getTarget());
					result.add(asso);
				}
			}

			now = HinemosTime.currentTimeMillis();
			m_log.debug("create association (" + node.getFacilityId() +") : " + (now - start) + "ms");

			return result;
		}
	}
}
