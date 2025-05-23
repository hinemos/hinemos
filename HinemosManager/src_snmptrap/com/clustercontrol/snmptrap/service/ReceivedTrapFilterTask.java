/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.StringUtil;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.hub.bean.StringSample;
import com.clustercontrol.hub.util.CollectStringDataUtil;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.snmptrap.bean.SnmpTrap;
import com.clustercontrol.snmptrap.bean.SnmpVarBind;
import com.clustercontrol.snmptrap.bean.TrapId;
import com.clustercontrol.snmptrap.model.TrapCheckInfo;
import com.clustercontrol.snmptrap.model.TrapValueInfo;
import com.clustercontrol.snmptrap.model.VarBindPattern;
import com.clustercontrol.snmptrap.util.SnmpTrapConstants;
import com.clustercontrol.snmptrap.util.SnmpTrapNotifier;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.XMLUtil;
import com.clustercontrol.util.apllog.AplLogger;

import jakarta.persistence.Query;

/**
 * フィルタリング処理を実装したクラス
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class ReceivedTrapFilterTask implements Runnable {

	/*
	 * 受信したSNMPTRAPにマッチするSNMPTRAP監視設定を順番に処理するためのクラス
	 * 
	 * クエリの結果、受信したSNMPTRAPにマッチする(MonitorInfo, MonitorTrapValueInfoEntity)の配列のリストが得られるので、
	 * MonitorInfoごとに順番に処理する
	 * 
	 * 例：SNMPTRAP監視設定A,Bがあり、受信したSNMPTRAPが、
	 * 監視設定Aのトラップ定義A-1とトラップ定義A-2とトラップ定義A-3、
	 * 監視設定Bのトラップ定義B-1にマッチする場合
	 * resultsには、以下のように2要素の配列から成るリストが設定される
	 * [[監視設定A, トラップ定義A-1(v1)],
	 *  [監視設定A, トラップ定義A-2(v2)],
	 *  [監視設定A, トラップ定義A-3(v1)],
	 *  [監視設定B, トラップ定義B-1(v2)]]
	 *  
	 *  この場合next()を実行すると、監視設定A, 監視設定Bと順に得られる。
	 *  next()で監視設定Aが得られた後、次にnext()を実行するまでgetValueInfoList()を実行すると、
	 *  リスト[v1のトラップ定義リスト[トラップ定義A-1, トラップ定義A-3],v2のトラップ定義リスト[トラップ定義A-2]]が得られ、
	 *  next()で監視設定Bが得られた後、次にnext()を実行するまでgetValueInfoList()を実行すると、
	 *  リスト[v1のトラップ定義リスト[], v2のトラップ定義リスト[トラップ定義B-1]]が得られる。
	 */
	private static class QueryResultIterator<E> implements Iterator<E> {
		
		/* 受信したSNMPTRAPにマッチする(MonitorInfo, MonitorTrapValueInfoEntity)の配列のリスト
		 * 配列の0番目にはMonitorInfoが、1番目にはMonitorTrapValueInfoEntityが入る
		 */
		private List<Object[]> results;
		private static int MONITOR_INFO = 0;
		private static int MONITOR_TRAP_VALUE_INFO = 1;
		
		// 処理しているMonitorInfoを示すための、リストresultsのインデックス
		private int current;
		// currentに対応するMonitorInfo
		private MonitorInfo currentMonitorInfo;
		// リストresultsのインデックス(next()で次に得られるものを示す）
		private Integer nextIndex = null;
		
		public QueryResultIterator(List<Object[]> results) {
			this.results = results;
			this.current = 0;
			this.currentMonitorInfo = results.isEmpty() ? null: (MonitorInfo)results.get(current)[MONITOR_INFO];
			this.nextIndex = currentMonitorInfo == null ? -1: current;
		}

		@Override
		public boolean hasNext() {
			if (nextIndex == null) {
				for (int i = current; i < results.size(); ++i) {
					MonitorInfo o = (MonitorInfo)results.get(i)[MONITOR_INFO];
					if (o != null && o != currentMonitorInfo) {
						nextIndex = i;
						current = i;
						currentMonitorInfo = o;
						break;
					}
				}
				if (nextIndex == null) {
					nextIndex = -1;
				}
			}
			return nextIndex != -1;
		}

		// MonitorInfoに対応するMonitorTrapValueInfoEntity(受信したトラップにマッチするもの)のリストを返す
		private List<List<TrapValueInfo>> getValueInfoList() {
			List<List<TrapValueInfo>> list = new ArrayList<List<TrapValueInfo>>();
			// 2バージョン分けてリストに保持する
			// (MonitorTrapValueInfoEntityのバージョン情報は v1 or v2c/v3 の2値なため)
			list.add(new ArrayList<TrapValueInfo>());
			list.add(new ArrayList<TrapValueInfo>());
			for (int i = current; i < results.size(); ++i) {
				MonitorInfo o = (MonitorInfo)results.get(i)[MONITOR_INFO];
				if (o != null && o == currentMonitorInfo) {
					// MonitorInfoに対応するMonitorTrapValueInfoEntityが複数ある場合はすべてリストにつめる
					TrapValueInfo entity = (TrapValueInfo)results.get(i)[MONITOR_TRAP_VALUE_INFO];
					if (entity != null) {
						list.get(entity.getVersion()).add(entity);
					}
				} else {
					break;
				}
			}
			return list;
		}

		// MonitorInfoを順番に返す
		@SuppressWarnings("unchecked")
		@Override
		public E next() {
			if (hasNext()) {
				Object result = results.get(nextIndex)[MONITOR_INFO];
				nextIndex = null;
				return (E)result;
			}
			return null;
		}

		@Override
		public void remove() {
			if ((MonitorInfo)results.get(current)[MONITOR_INFO] == currentMonitorInfo) {
				results.remove(current);
				nextIndex = null;
			}
		}
	}

	private Logger logger = Logger.getLogger(this.getClass());

	private List<SnmpTrap> receivedTrapList;
	private SnmpTrapNotifier notifier;
	private TrapProcCounter counter;
	private Charset defaultCharset;

	public ReceivedTrapFilterTask(List<SnmpTrap> receivedTrapList, SnmpTrapNotifier notifier, TrapProcCounter counter, Charset defaultCharset) {
		this.receivedTrapList = receivedTrapList;
		this.notifier = notifier;
		this.counter = counter;
		this.defaultCharset = defaultCharset;
	}

	public ReceivedTrapFilterTask(SnmpTrap receivedTrap,
			SnmpTrapNotifier notifier, TrapProcCounter counter, Charset defaultCharset) {

		this(Arrays.asList(receivedTrap), notifier, counter, defaultCharset);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		JpaTransactionManager tm = null;
		boolean warn = true;
		List<OutputBasicInfo> notifyInfoList = new ArrayList<>();

		try {
			tm = new JpaTransactionManager();
			tm.begin();

			HinemosEntityManager em = tm.getEntityManager();

			List<MonitorInfo> monitorList = QueryUtil.getMonitorInfoByMonitorTypeId(HinemosModuleConstant.MONITOR_SNMPTRAP);
			if (monitorList == null) {
				if (logger.isDebugEnabled()) {
					for (SnmpTrap receivedTrap : receivedTrapList) {
						logger.debug("snmptrap monitor not found. skip filtering. [" + receivedTrap + "]");
					}
				}
				return;
			}

			// 以下の処理は、未指定トラップの検出のために追加。
			// 上記クエリは、MonitorTrapValueInfoEntity に焦点を置いている。トラップ情報にマッチしないと、MonitorInfo が残らない。
			// トラップ情報にマッチしなかった監視情報を抽出するために、以下の処理を行う。
			List<MonitorInfo> unspecifiedFlgMonitorInfoList = em
					.createNamedQuery(
							"MonitorInfo.findByNotifyofReceivingUnspecifiedFlg",
							MonitorInfo.class).getResultList();

			// 収集、通知処理実行
			List<SnmpTrap> receivedTrapBuffer = new ArrayList<SnmpTrap>();
			List<MonitorInfo> monitorBuffer = new ArrayList<MonitorInfo>();
			List<Integer> priorityBuffer = new ArrayList<Integer>();
			List<String> facilityIdBuffer = new ArrayList<String>();
			List<String[]> msgsBuffer = new ArrayList<String[]>();
			List<RunInstructionInfo> runInstructionBuffer = new ArrayList<>();
			Map<String, List<String>> matchedFacilityIdListMap = new HashMap<String, List<String>>();

			RepositoryControllerBean repositoryCtrl = new RepositoryControllerBean();
			for (SnmpTrap receivedTrap : receivedTrapList) {
				String ipAddr =receivedTrap.getAgentAddr();
				if (!matchedFacilityIdListMap.containsKey(ipAddr)) {
					List<String> matchedFacilityIdList = repositoryCtrl.getFacilityIdByIpAddress(InetAddress.getByName(ipAddr));
					matchedFacilityIdListMap.put(ipAddr, matchedFacilityIdList);
				}
			}

			/* 監視ジョブ以外 */
			// 収集項目がTrueのものを取得
			List<MonitorInfo> collectorFlgTrueMonitorFlgFlaseList = em
					.createNamedQuery(
							"MonitorInfo.findByCollectorFlg",
							MonitorInfo.class).getResultList();
			
			//監視True または、収集True AND 監視Falseを満たす監視項目のリスト
			unspecifiedFlgMonitorInfoList.addAll(collectorFlgTrueMonitorFlgFlaseList);

			
			// 収集値の入れ物を作成
			List<StringSample> collectedSamples = new ArrayList<>();
			
			for (SnmpTrap receivedTrap : receivedTrapList) {
				List<Object[]> results = getTargetMonitorTrapList(
						receivedTrap, em, unspecifiedFlgMonitorInfoList, false);

				QueryResultIterator<MonitorInfo> monitorIter = new QueryResultIterator<MonitorInfo>(results);
				while (monitorIter.hasNext()) {
					MonitorInfo currentMonitor = monitorIter.next();
					collectionAndNotifySnmptrap(
							em, 
							currentMonitor,
							null,
							receivedTrap,
							monitorIter.getValueInfoList(),
							matchedFacilityIdListMap,
							receivedTrapBuffer,
							monitorBuffer,
							priorityBuffer,
							facilityIdBuffer,
							msgsBuffer,
							runInstructionBuffer,
							collectedSamples);
				}//while monitorIter
			}//for

			//収集 文字列を蓄積
			if (!collectedSamples.isEmpty()) {
				CollectStringDataUtil.store(collectedSamples);
			}
			notifyInfoList.addAll(notifier.createOutputBasicInfoList(
					receivedTrapBuffer, monitorBuffer, priorityBuffer, facilityIdBuffer, msgsBuffer, null));

			/* 監視ジョブ */
			receivedTrapBuffer = new ArrayList<SnmpTrap>();
			monitorBuffer = new ArrayList<MonitorInfo>();
			priorityBuffer = new ArrayList<Integer>();
			facilityIdBuffer = new ArrayList<String>();
			msgsBuffer = new ArrayList<String[]>();
			runInstructionBuffer = new ArrayList<>();
			unspecifiedFlgMonitorInfoList = em
					.createNamedQuery(
					"MonitorInfo.findByNotifyofReceivingUnspecifiedFlgForMonitorJob",
					MonitorInfo.class).getResultList();
			for (SnmpTrap receivedTrap : receivedTrapList) {
				List<Object[]> results = getTargetMonitorTrapList(
						receivedTrap, em, unspecifiedFlgMonitorInfoList, true);

				for (Map.Entry<RunInstructionInfo, MonitorInfo> entry 
						: MonitorJobWorker.getMonitorJobMap(HinemosModuleConstant.MONITOR_SNMPTRAP).entrySet()) {

					boolean isCheck = false;
					List<List<TrapValueInfo>> trapValueList = null;
					QueryResultIterator<MonitorInfo> monitorIter = new QueryResultIterator<MonitorInfo>(results);
					while (monitorIter.hasNext()) {
						MonitorInfo currentMonitor = monitorIter.next();
						if (entry.getValue().getMonitorId().equals(currentMonitor.getMonitorId())) {
							trapValueList = monitorIter.getValueInfoList();
							isCheck = true;
							break;
						}
					}
					if (!isCheck) {
						// 監視対象外の監視の場合は次の処理を行う
						continue;
					}
					collectionAndNotifySnmptrap(
							em, 
							entry.getValue(),
							entry.getKey(),
							receivedTrap,
							trapValueList,
							matchedFacilityIdListMap,
							receivedTrapBuffer,
							monitorBuffer,
							priorityBuffer,
							facilityIdBuffer,
							msgsBuffer,
							runInstructionBuffer,
							collectedSamples);
				}//while monitorIter
			}//for

			notifyInfoList.addAll(notifier.createOutputBasicInfoList(
					receivedTrapBuffer, monitorBuffer, priorityBuffer, facilityIdBuffer, msgsBuffer, runInstructionBuffer));

			// 通知設定
			tm.addCallback(new NotifyCallback(notifyInfoList));

			tm.commit();
			warn = false;
		} catch (HinemosUnknown | UnknownHostException e) {
			logger.warn("unexpected internal error. : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// HA構成のため、例外を握りつぶしてはいけない
			throw new RuntimeException("unexpected internal error. : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			if (tm != null) {
				tm.close();
			}

			if (warn) {
				for (SnmpTrap receivedTrap : receivedTrapList) {
					TrapId trapV1 = receivedTrap.getTrapId().asTrapV1Id();
					
					// Internal Event
					String[] args = {
							trapV1.getEnterpriseId(),
							String.valueOf(trapV1.getGenericId()),
							String.valueOf(trapV1.getSpecificId())};
					AplLogger.put(InternalIdCommon.MON_SNMP_TRP_SYS_009, args);
				}
			}
		}
	}

	/**
	 * SNMPトラップの収集・通知情報の取得を行う
	 * 
	 * @param em エンティティマネージャ
	 * @param monitorInfo 監視情報
	 * @param receivedTrap SNMPTRAP情報
	 * @param monitorIter
	 * @param matchedFacilityIdListMap
	 * @param receivedTrapBuffer
	 * @param monitorBuffer
	 * @param priorityBuffer
	 * @param facilityIdBuffer
	 * @param msgsBuffer
	 * @param collectedSamples
	 * @throws HinemosUnknown
	 * @throws UnknownHostException
	 */
	private void collectionAndNotifySnmptrap(
		HinemosEntityManager em,
		MonitorInfo monitorInfo,
		RunInstructionInfo runInstructionInfo,
		SnmpTrap receivedTrap,
		List<List<TrapValueInfo>> valueInfoList,
		Map<String, List<String>> matchedFacilityIdListMap,
		List<SnmpTrap> receivedTrapBuffer,
		List<MonitorInfo> monitorBuffer,
		List<Integer> priorityBuffer,
		List<String> facilityIdBuffer,
		List<String[]> msgsBuffer,
		List<RunInstructionInfo> runInstructionBuffer,
		List<StringSample> collectedSamples) throws HinemosUnknown, UnknownHostException {

		// カレンダの有効期間外の場合、スキップする
		if (runInstructionInfo == null && isInDisabledCalendar(monitorInfo, receivedTrap)) {
			return;
		}

		TrapCheckInfo trapInfo = monitorInfo.getTrapCheckInfo();
		// コミュニティのチェック
		if (trapInfo.getCommunityCheck().booleanValue()) {
			if (!trapInfo.getCommunityName().equals(receivedTrap.getCommunity())) {
				if (logger.isDebugEnabled()) {
					logger.debug("community " + trapInfo.getCommunityName() + " is not matched. [" + receivedTrap + "]");
				}
				return;
			}
		}

		String ipAddr = receivedTrap.getAgentAddr();
		List<String> notifyFacilityIdList = new ArrayList<>();
		if (runInstructionInfo == null) {
			// 監視ジョブ以外
			notifyFacilityIdList = getNotifyFacilityIdList(
				monitorInfo, receivedTrap,
				matchedFacilityIdListMap.get(ipAddr));
			if (notifyFacilityIdList.isEmpty()) {
				if (logger.isDebugEnabled()) {
					logger.debug("notification facilities not found [" + receivedTrap + "]");
				}
				return;
			}
		} else {
			// 監視ジョブ
			if (matchedFacilityIdListMap.get(ipAddr) != null
					&& matchedFacilityIdListMap.get(ipAddr).contains(runInstructionInfo.getFacilityId())) {
				notifyFacilityIdList.add(runInstructionInfo.getFacilityId());
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("notification facilities not found [" + receivedTrap + "]");
				}
				return;
			}
		}

		// varbindの文字列変換
		Charset charset = defaultCharset;
		if (trapInfo.getCharsetConvert().booleanValue()) {
			if (trapInfo.getCharsetName() != null) {
				if (Charset.isSupported(trapInfo.getCharsetName())) {
					charset = Charset.forName(trapInfo.getCharsetName());
				} else {
					logger.warn("not supported charset : " + trapInfo.getCharsetName());
				}
			}
		}

		String conversion = HinemosPropertyCommon.monitor_snmptrap_org_message_text_conversion.getStringValue();
		boolean checkConversion = "CharacterCode".equals(conversion);
		List<SnmpVarBind> varBinds = receivedTrap.getVarBinds();
		String[] varBindStrs = new String[varBinds.size()];
		for (int i = 0; i < varBinds.size(); i++) {
			switch (varBinds.get(i).getType()) {
			case OctetString :
			case Opaque :
				// SNMPTRAP監視で受信した文字列を監視設定で、指定した文字コードに変換できるか
				// または、monitor.snmptrap.org.message.text.conversionにCharacterCodeが指定されているか
				if (StringUtil.checkEncode(varBinds.get(i).getObject(), charset) || checkConversion) {
					// 指定された文字コードに変換する
					varBindStrs[i] = new String(varBinds.get(i).getObject(), charset);
				} else {
					// 16進数文字列に変換する
					varBindStrs[i] = DatatypeConverter.printHexBinary(varBinds.get(i).getObject());
				}
				break;
			default :
				varBindStrs[i] = new String(varBinds.get(i).getObject());
			}
		}

		TrapValueInfo matchedTrapValueInfo = null;
		VarBindPattern matchedPattern = null;
		String matchedString = null;

		if (runInstructionInfo == null) {
			// 監視ジョブ以外
			//収集
			if (monitorInfo.getCollectorFlg() != null && monitorInfo.getCollectorFlg()) {

				String[] msgs = createMessages(trapInfo, varBindStrs, receivedTrap);

				Date date = new Date(HinemosTime.currentTimeMillis());
				// 収集値の入れ物を作成
				StringSample strSample = new StringSample(date, monitorInfo.getMonitorId());
				for (String facilityId : notifyFacilityIdList) {
					//msgs[1]はオリジナルメッセージ
					String replacedMsg = XMLUtil.ignoreInvalidString(msgs[1]);
					strSample.set(facilityId, "", replacedMsg);
				}
				if (strSample != null) {
					collectedSamples.add(strSample);
				}
			}
			if (!monitorInfo.getMonitorFlg()) {
				return;
			}
		}

		// 受信したSNMPTRAPのバージョンに合わせて、バージョンの一致するトラップ定義を優先する
		List<Integer> trapVersionOrder = new ArrayList<Integer>();
		if (SnmpVersionConstant.TYPE_V1 == receivedTrap.getTrapId().getVersion()) {
			trapVersionOrder.add(SnmpVersionConstant.TYPE_V1);
			trapVersionOrder.add(SnmpVersionConstant.TYPE_V2);
		} else {
			trapVersionOrder.add(SnmpVersionConstant.TYPE_V2);
			trapVersionOrder.add(SnmpVersionConstant.TYPE_V1);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("processing trapVersionOrder, receivedTrapVer " + receivedTrap.getTrapId().getVersion());
			logger.debug("processing trapVersionOrder : " + trapVersionOrder);
		}

		for (Integer trapDefVersion : trapVersionOrder) {
			if (logger.isDebugEnabled()) {
				logger.debug("processing valueInfoList, version " + SnmpVersionConstant.typeToString(trapDefVersion));
				logger.debug("processing valueInfoList : " + valueInfoList.get(trapDefVersion));
			}

			if (valueInfoList.get(trapDefVersion).isEmpty()) {
				// 受信したSNMPTRAPと同じバージョンのトラップ定義がないため、別バージョンの定義も確認する
				continue;
			}

			Iterator<TrapValueInfo> valueIterator = valueInfoList.get(trapDefVersion).iterator();

			while (valueIterator.hasNext()) {
				TrapValueInfo currentValueInfo = valueIterator.next();
				if (logger.isDebugEnabled()) {
					logger.debug("currentValueInfo : " + currentValueInfo);
				}

				if (!currentValueInfo.getProcessingVarbindSpecified()) {
					// varbindで判定をおこなわない場合
					matchedTrapValueInfo = currentValueInfo;
					if (!SnmpTrapConstants.genericTrapV2Set.contains(currentValueInfo.getId().getTrapOid())) {
						// GENERIC TRAPのOIDより、個別のOIDを優先する
						break;
					}
				} else {
					// varbindで判定をおこなう場合

					String varBindStr = getBindedString(currentValueInfo.getFormatVarBinds(), varBindStrs);
					List<VarBindPattern> patterns = new ArrayList<>(currentValueInfo.getVarBindPatterns());
					Collections.sort(patterns, new Comparator<VarBindPattern>() {
						@Override
						public int compare(VarBindPattern o1, VarBindPattern o2) {
							return o1.getId().getOrderNo().compareTo(o2.getId().getOrderNo());
						}
					});

					for (VarBindPattern currentPattern : patterns) {
						if (!currentPattern.getValidFlg()) // VarBindパターン[作成・変更]の「この設定を有効にする」がOFF
							continue;

						Pattern pattern = null;
						if (currentPattern.getCaseSensitivityFlg()) {
							// 大文字・小文字を区別しない場合
							pattern = Pattern.compile(currentPattern.getPattern(),
									Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
						} else {
							// 大文字・小文字を区別する場合
							pattern = Pattern.compile(currentPattern.getPattern(), Pattern.DOTALL);
						}

						// パターンマッチ表現でマッチング
						Matcher matcher = pattern.matcher(varBindStr);
						if (matcher.matches()) {
							matchedTrapValueInfo = currentValueInfo;
							matchedPattern = currentPattern;
							matchedString = varBindStr;
							break;
						}
					}

					if (matchedTrapValueInfo != null
							&& !SnmpTrapConstants.genericTrapV2Set.contains(currentValueInfo.getId().getTrapOid())) {
						// GENERIC TRAPのOIDより、個別のOIDを優先する
						break;
					}
				}
			} // while valueIterator

			// 受信したSNMPTRAPと同じバージョンのトラップ定義があった場合は、別バージョンの定義は確認しない
			break;
		} // while v1 and v2c/v3

		if (matchedTrapValueInfo == null && trapInfo.getNotifyofReceivingUnspecifiedFlg()) {
			// マッチするTRAPの設定が存在せず、存在しない場合に通知する場合
			String[] msgs = createMessages(trapInfo, varBindStrs, receivedTrap);

			for (String facilityId : notifyFacilityIdList) {
				receivedTrapBuffer.add(receivedTrap);
				monitorBuffer.add(monitorInfo);
				priorityBuffer.add(trapInfo.getPriorityUnspecified());
				facilityIdBuffer.add(facilityId);
				msgsBuffer.add(msgs);
				runInstructionBuffer.add(runInstructionInfo);
				counter.countupNotified();
			}
			return;
		}

		if (matchedTrapValueInfo == null) {
			return;
		}

		if (!matchedTrapValueInfo.getProcessingVarbindSpecified()) {
			// 変数にかかわらず常に通知する
			String[] msgs = createMessages(trapInfo, matchedTrapValueInfo, varBindStrs, receivedTrap);

			for (String facilityId : notifyFacilityIdList) {
				receivedTrapBuffer.add(receivedTrap);
				monitorBuffer.add(monitorInfo);
				priorityBuffer.add(matchedTrapValueInfo.getPriorityAnyVarbind());
				facilityIdBuffer.add(facilityId);
				msgsBuffer.add(msgs);
				runInstructionBuffer.add(runInstructionInfo);
				counter.countupNotified();
			}
		} else {
			// 変数で判定する
			if (matchedPattern.getProcessType().booleanValue()) {
				// 処理する
				String[] msgs = createPattenMatchedOrgMessage(
						trapInfo, matchedTrapValueInfo,
						matchedPattern, matchedString,
						varBindStrs, receivedTrap);

				for (String facilityId : notifyFacilityIdList) {
					receivedTrapBuffer.add(receivedTrap);
					monitorBuffer.add(monitorInfo);
					priorityBuffer.add(matchedPattern.getPriority());
					facilityIdBuffer.add(facilityId);
					msgsBuffer.add(msgs);
					runInstructionBuffer.add(runInstructionInfo);
					counter.countupNotified();
				}
			} else {
				//処理しない
			}
		}
	}

	private boolean isInDisabledCalendar(MonitorInfo monitorInfo,
			SnmpTrap trap) {
		
		String calendarId = monitorInfo.getCalendarId();
		boolean inDisabledCalendar = false;
		if (calendarId != null && !"".equals(calendarId)) {
			try {
				inDisabledCalendar = !new CalendarControllerBean().isRun(calendarId, trap.getReceivedTime());
				if (inDisabledCalendar) {
					if (logger.isDebugEnabled()) {
						logger.debug("calendar " + calendarId + " is not enabled term. [" + trap + "]");
					}
				}
			} catch (CalendarNotFound | InvalidRole | HinemosUnknown | RuntimeException e) {
				// カレンダが未定義の場合は、スキップせずに継続する（予期せぬロストの回避）
				logger.info("calendar " + calendarId
						+ " is not found, skip calendar check. [" + trap + "]");
			}
		}
		
		return inDisabledCalendar;
	}

	private List<Object[]> getTargetMonitorTrapList (
			SnmpTrap receivedTrap, HinemosEntityManager em,
			List<MonitorInfo> unspecifiedFlgMonitorInfoList,
			boolean isMonitorJob) {
		TrapId trapV1;
		List<TrapId> trapV2List;
		if (receivedTrap.getTrapId().getVersion() == SnmpVersionConstant.TYPE_V1) {
			trapV1 = (TrapId)receivedTrap.getTrapId();
			trapV2List = trapV1.asTrapV2Id();
		} else {
			TrapId trap2 = (TrapId)receivedTrap.getTrapId();
			trapV1 = trap2.asTrapV1Id();
			trapV2List = Arrays.asList(trap2);
		}

		Query query = null;
		if (isMonitorJob) {
			// 監視ジョブ
			query = em.createNamedQuery("MonitorTrapValueInfoEntity.findByReceivedTrapForMonitorJob");
		} else {
			// 監視ジョブ以外
			query = em.createNamedQuery("MonitorTrapValueInfoEntity.findByReceivedTrap");
		}
		query.setParameter("enterpriseId", trapV1.getEnterpriseId());
		query.setParameter("genericId", trapV1.getGenericId());
		query.setParameter("specificId", trapV1.getSpecificId());
		List<String> snmpTrapOids = new ArrayList<>();
		for (TrapId trapV2: trapV2List) {
			snmpTrapOids.add(trapV2.getSnmpTrapOid());
		}
		query.setParameter("v2TrapOids", snmpTrapOids);
		query.setParameter("enterpriseSpecific", SnmpTrapConstants.SNMP_GENERIC_enterpriseSpecific);
		query.setParameter("genericTrapOid", SnmpTrapConstants.genericTrapV1Map.get(trapV1.getGenericId()));

		long startTime = HinemosTime.currentTimeMillis();
		@SuppressWarnings("unchecked")
		List<Object[]> results = (List<Object[]>)query.getResultList();
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("finished searching traps. elapsed time = %d, matched count = %d", HinemosTime.currentTimeMillis() -startTime, results.size()));
		}

		Set<String> monitorIds = new HashSet<>();
		for (Object[] m: results) {
			monitorIds.add(((MonitorInfo)m[0]).getMonitorId());
		}
		for (MonitorInfo m: unspecifiedFlgMonitorInfoList) {
			if (!monitorIds.contains(m.getMonitorId()))
				results.add(new Object[]{m, null,null});
		}
		return results;
	}

	private List<String> getNotifyFacilityIdList(MonitorInfo monitorInfo, SnmpTrap receivedTrap, List<String>matchedFacilityIdList) throws HinemosUnknown, UnknownHostException {
		RepositoryControllerBean repositoryCtrl = new RepositoryControllerBean();

		if (matchedFacilityIdList == null || matchedFacilityIdList.isEmpty()) {
			matchedFacilityIdList = Arrays.asList(FacilityTreeAttributeConstant.UNREGISTERED_SCOPE);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("matched facilities : " + matchedFacilityIdList + " [" + receivedTrap + "]");
		}

		// 監視対象のファシリティID一覧を取得する
		List<String> targetFacilityIdList = Collections.emptyList();
		if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(monitorInfo.getFacilityId())) {
			targetFacilityIdList = Arrays.asList(FacilityTreeAttributeConstant.UNREGISTERED_SCOPE);
		}	else {
			targetFacilityIdList = repositoryCtrl.getExecTargetFacilityIdList(monitorInfo.getFacilityId(), monitorInfo.getOwnerRoleId());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("target facilities : " + targetFacilityIdList + " [" + receivedTrap + "]");
		}

		// 通知対象のファシリティID一覧を絞り込む
		List<String> notifyFacilityIdList = new ArrayList<String>(matchedFacilityIdList);
		notifyFacilityIdList.retainAll(targetFacilityIdList);

		return notifyFacilityIdList;
	}

	private String[] createMessages(TrapCheckInfo checkInfo, TrapValueInfo valueInfo, String[] varBindStrs, SnmpTrap receivedTrap) {
		StringBuilder orgMessage = new StringBuilder();

		if (receivedTrap.getTrapId().getVersion() == SnmpVersionConstant.TYPE_V1) {
			TrapId trapV1 = (TrapId)receivedTrap.getTrapId();
			orgMessage.append("OID=").append(trapV1.getEnterpriseId()).append("\nTrapName=").append(valueInfo.getUei()).append('\n');
		}
		else {
			TrapId trapV2 = (TrapId)receivedTrap.getTrapId();
			orgMessage.append("OID=").append(trapV2.getSnmpTrapOid()).append("\nTrapName=").append(valueInfo.getUei()).append('\n');
		}

		StringBuilder detail = new StringBuilder();
		if (HinemosPropertyCommon.monitor_snmptrap_org_message_community.getBooleanValue())
			detail.append(MessageConstant.COMMUNITY_NAME.getMessage()).append("=").append(receivedTrap.getCommunity()).append(" \n");

		if (HinemosPropertyCommon.monitor_snmptrap_org_message_varbind.getBooleanValue()) {
			boolean first = true;
			for (String value: varBindStrs) {
				if (first) {
					first = false;
					detail.append("VarBind=").append(value);
				}
				else
					detail.append(", ").append(value);
			}
			detail.append(" \n");
		}
		detail.append(MessageConstant.DESCRIPTION.getMessage()).append("=").append(valueInfo.getDescription());
		orgMessage.append(getBindedString(detail.toString(), varBindStrs));

		return new String[]{getBindedString(valueInfo.getLogmsg(), varBindStrs), orgMessage.toString()};
	}

	private String[] createMessages(TrapCheckInfo checkInfo, String[] varBindStrs, SnmpTrap receivedTrap) {
		StringBuilder orgMessage = new StringBuilder();
		// v3の場合、receivedTrap.communityにユーザ名が入る。
		// monitor.snmptrap.org.message.communityで表示を制御すると混乱を招きそう、またmonitor.snmptrap.v3.userに指定されたユーザ以外は認証されないため、ユーザ名は表示しない。
		if (HinemosPropertyCommon.monitor_snmptrap_org_message_community.getBooleanValue()
				&& receivedTrap.getTrapId().getVersion() != SnmpVersionConstant.TYPE_V3) {
			orgMessage.append(MessageConstant.COMMUNITY_NAME.getMessage()).append("=").append(receivedTrap.getCommunity()).append(" \n");
		}

		if (HinemosPropertyCommon.monitor_snmptrap_org_message_varbind.getBooleanValue()) {
			boolean first = true;
			for (String value: varBindStrs) {
				if (first) {
					first = false;
					orgMessage.append("VarBind=").append(value);
				}
				else
					orgMessage.append(", ").append(value);
			}
			orgMessage.append(" \n");
		}

		if (receivedTrap.getTrapId().getVersion() == SnmpVersionConstant.TYPE_V1) {
			TrapId trapV1 = (TrapId)receivedTrap.getTrapId();
			orgMessage.append("version : ").append(SnmpVersionConstant.typeToString(trapV1.getVersion()))
				.append(", oid : ").append(trapV1.getEnterpriseId())
				.append(", generic_id : ").append(trapV1.getGenericId())
				.append(", specificId : ").append(trapV1.getSpecificId());
		}
		else {
			TrapId trapV2 = (TrapId)receivedTrap.getTrapId();
			orgMessage.append("version : ").append(SnmpVersionConstant.typeToString(trapV2.getVersion()))
				.append(", oid : ").append(trapV2.getSnmpTrapOid());
		}

		String msg = orgMessage.toString();
		return new String[]{msg, msg};
	}

	private String[] createPattenMatchedOrgMessage(
			TrapCheckInfo checkInfo,
			TrapValueInfo valueInfo,
			VarBindPattern pattern, String matchedString,
			String[] varBindStrs, SnmpTrap receivedTrap) {
		
		String[] msgs = createMessages(checkInfo, valueInfo, varBindStrs, receivedTrap);
		StringBuilder orgMessage = new StringBuilder().append(msgs[1])
				.append('\n').append("Pattern=").append(pattern.getPattern())
				.append('\n').append("Matched string=").append(matchedString);
		return new String[]{msgs[0], orgMessage.toString()};
	}

	private String getBindedString(String str, String[] varbinds) {
		if (str == null) {
			return "";
		}
		if (varbinds == null) {
			return str;
		}

		for (int i = 0; i < varbinds.length; i++) {
			if (logger.isDebugEnabled()) {
				logger.debug("binding : " + str + "  " + "%parm[#" + (i + 1) + "]% to " + varbinds[i] + "]");
			}
			str = str.replace("%parm[#" + (i + 1) + "]%", varbinds[i]);
			if (logger.isDebugEnabled()) {
				logger.debug("binded : " + str);
			}
		}

		return str;
	}
}
