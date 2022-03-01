/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.customtrap.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.calendar.factory.SelectCalendar;
import com.clustercontrol.collect.bean.PerfData;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.customtrap.bean.CustomTrap;
import com.clustercontrol.customtrap.bean.CustomTrap.Type;
import com.clustercontrol.customtrap.bean.CustomTraps;
import com.clustercontrol.customtrap.util.CustomTrapNotifier;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.hub.bean.CollectStringTag;
import com.clustercontrol.hub.bean.StringSample;
import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.hub.util.CollectStringDataUtil;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.run.bean.MonitorNumericType;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil;
import com.clustercontrol.monitor.run.util.MonitorJudgementInfoCache;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil.CollectMonitorDataInfo;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * フィルタリング処理を実装したクラス
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class ReceivedCustomTrapFilter {

	private Logger logger = Logger.getLogger(this.getClass());
	private static Map<String, Double> resentDataMap = new ConcurrentHashMap<String, Double>();

	CustomTraps receivedCustomTraps;
	private CustomTrapNotifier notifier;
	private long notifiedCount = 0;
	private int resentDataMapMaxSize = 0;

	/**
	 * コンストラクタ
	 * 
	 * @param receivedCustomTraps	受信データ
	 * @param notifier				CustomTrapNotifier
	 * @param defaultCharset		キャラクタセット
	 */
	public ReceivedCustomTrapFilter(CustomTraps receivedCustomTraps, CustomTrapNotifier notifier,
			Charset defaultCharset) {
		this.receivedCustomTraps = receivedCustomTraps;
		this.notifier = notifier;
		resentDataMapMaxSize = HinemosPropertyCommon.monitor_Customtrap_RecentData_Map_size.getIntegerValue();
		logger.info("monitor.Customtrap.RecentData.Map.initialCapacity=" + resentDataMapMaxSize);
	}

	/**
	 * フィルタリング処理します。
	 */
	public void work() {
		logger.info("ReceivedCustomTrapFilter work");
		JpaTransactionManager tm = null;
		List<OutputBasicInfo> notifyInfoList = new ArrayList<>();

		try {
			tm = new JpaTransactionManager();
			tm.begin();

			RepositoryControllerBean repositoryCtrl = new RepositoryControllerBean();
			List<String> matchedFacilityIdList = new ArrayList<String>();
			String agentAddr = receivedCustomTraps.getAgentAddr();
			String facilityId = receivedCustomTraps.getFacilityId();

			// 該当ファシリティを取得
			if ((null == facilityId) || (facilityId.isEmpty())) {
				// IPアドレスからファシリティIDリストを取得する
				matchedFacilityIdList = repositoryCtrl.getFacilityIdByIpAddress(InetAddress.getByName(agentAddr));
				if (matchedFacilityIdList.size() == 0) {
					// FacilityNotFound
					matchedFacilityIdList.add(FacilityTreeAttributeConstant.UNREGISTERED_SCOPE);
					logger.info("work() : UNREGISTERED_SCOPE agentAddr =" + agentAddr);
				}
			} else {
				try {
					if (repositoryCtrl.isNode(facilityId)) {
						matchedFacilityIdList.add(facilityId);
					} else {
						// スコープなので無視
						logger.warn("work() : Scope is set to FacilityID [FacilityID=" + facilityId + "]");
						return;
					}
				} catch (FacilityNotFound e) {
					matchedFacilityIdList.add(FacilityTreeAttributeConstant.UNREGISTERED_SCOPE);
				}
			}

			List<Sample> collectedSamples = new ArrayList<>();
			/* 監視ジョブ以外 */
			// 受信データ分処理を行う
			for (CustomTrap receivedCustomTrap : receivedCustomTraps.getCustomTraps()) {
				// 監視リスト取得
				List<MonitorInfo> monitorList = null;
				switch (receivedCustomTrap.getType()) {
				case STRING: {
						monitorList = QueryUtil.getMonitorInfoByMonitorTypeId(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S);
					}
					break;
				case NUM: {
						monitorList = QueryUtil.getMonitorInfoByMonitorTypeId(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N);
					}
					break;
				}

				if (monitorList == null) {
					// 該当モニタがない場合
					if (logger.isDebugEnabled()) {
						logger.info("work() : customtrap monitor not found. skip filtering. [" + receivedCustomTrap.toString()
								+ "]");
					}
					continue;
				}
				double value = 0;// 数値用値
				// 数値用値（ファシリティID単位）
				Map<String, Double> valueMap = null;
				String key = "";
				String valueKey = "";
				List<StringSample> collectedStringSamples = new ArrayList<>();
				StringSample stringSample = null;
				for (MonitorInfo monitor : monitorList) {
					// カレンダーチェック
					if (isNotInCalendar(monitor, receivedCustomTrap)) {
						logger.debug("work() : NotInCalender");
						continue;
					}

					// キーパターン
					Pattern keyPattern = Pattern.compile(monitor.getCustomTrapCheckInfo().getTargetKey(),
							Pattern.DOTALL);
					Matcher matcherKeyPattern = keyPattern.matcher(receivedCustomTrap.getKey());
					if (!matcherKeyPattern.matches()) {
						logger.info("work() : KeyPattern Unmatched");
						logger.debug("work() : MonitorId =" + monitor.getMonitorId());
						continue;
					}
					
					List<String> validFacilityIdList = getValidFacilityIdList(matchedFacilityIdList, monitor);
					logger.debug("work() : validFacilityIdList =" + validFacilityIdList);

					// 収集処理
					// 数値で差分取得の場合、Value値をsample/notify前に計算する
					if (receivedCustomTrap.getType() == Type.NUM) {
						value = Double.parseDouble(receivedCustomTrap.getMsg());
						key = monitor.getMonitorId() + ":" + receivedCustomTrap.getKey();
						valueMap = new HashMap<>();

						if (monitor.getCustomTrapCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_DELTA) {
							for (String facilityIdElement : validFacilityIdList) {
								// ファシリティID単位
								valueKey = key + ":" + facilityIdElement;

								Double oldData = null;
								logger.debug("work() : monitor.Customtrap.RecentData.Map.size=" + resentDataMap.size());
								//findbugs対応 synchronized用オブジェクトを staticメンバーからクラスに変更
								synchronized (ReceivedCustomTrapFilter.class) {
									// 取得した値と前回情報の差分をとり、閾値判定を行う。
									oldData = resentDataMap.putIfAbsent(valueKey, value);
									if (null == oldData) {
										// 前回情報なし
										// 差分処理の初回取得処理のため、処理終了[収集も通知もしない（Custom数値と同じ）]
										logger.info("work() : No previous information No Monitoring and sampling!!");
										continue;
									}
									// 前回値情報を今回の取得値に更新
									resentDataMap.replace(valueKey, value);
								}
								if (resentDataMapMaxSize < resentDataMap.size()) {
									logger.warn("work() : CustomTrap Specified max size(" + resentDataMapMaxSize
											+ ") < cache size(" + resentDataMap.size() + ")  ");
									// Internal Event
									String[] args = { String.valueOf(resentDataMap.size()),
											String.valueOf(resentDataMapMaxSize) };
									AplLogger.put(InternalIdCommon.MON_CUSTOMTRAP_N_SYS_022, args);
								}
								// 前回値を取得
								double prevValue = oldData.doubleValue();
								logger.info("work() : CustomTrapNum prev=" + prevValue + " value = " + value
										+ " new value=" + (value - prevValue));
								
								valueMap.put(facilityIdElement, value - prevValue);
							}
							logger.debug("work() : valueMap =" + valueMap);
							if (valueMap.size() == 0) {
								continue;
							}
						} else {
							for (String facilityIdElement : validFacilityIdList) {
								valueMap.put(facilityIdElement, value);
							}
						}
					}

					// 管理対象フラグが無効であれば、次の設定の処理へスキップする。
					if (monitor.getCollectorFlg()
							|| monitor.getPredictionFlg() 
							|| monitor.getChangeFlg()) {
						List<CustomTrap> customtrapListBuffer = new ArrayList<CustomTrap>();
						List<MonitorRunResultInfo> collectResultBuffer = new ArrayList<>();
						for (String facilityIdElement : validFacilityIdList) {
							switch (receivedCustomTrap.getType()) {
							case STRING: {
								stringSample = new StringSample(new Date(receivedCustomTrap.getSampledTime()), monitor.getMonitorId());

								// 抽出したタグ
								List<StringSampleTag> tags = new ArrayList<>();
								if (receivedCustomTrap.getDate() != null) {
									StringSampleTag tagDate = new StringSampleTag(
											CollectStringTag.TIMESTAMP_IN_LOG, Long.toString(receivedCustomTrap.getSampledTime()));
									tags.add(tagDate);
								}
								StringSampleTag tagType = new StringSampleTag(CollectStringTag.TYPE, receivedCustomTrap.getType().name());
								tags.add(tagType);
								StringSampleTag tagKey = new StringSampleTag(CollectStringTag.KEY, receivedCustomTrap.getKey());
								tags.add(tagKey);
								StringSampleTag tagMsg = new StringSampleTag(CollectStringTag.MSG, receivedCustomTrap.getMsg());
								tags.add(tagMsg);
								StringSampleTag tagFacility = new StringSampleTag(CollectStringTag.FacilityID, facilityIdElement);
								tags.add(tagFacility);

								// ログメッセージ
								stringSample.set(facilityIdElement, "customtrap", receivedCustomTrap.getOrgMsg(), tags);

								collectedStringSamples.add(stringSample);
								break;
							}
							case NUM: {
								Sample sample = new Sample(new Date(receivedCustomTrap.getSampledTime()), monitor.getMonitorId());
								if (!valueMap.containsKey(facilityIdElement)) {
									// 初回通知の場合
									continue;
								}
								boolean overlapCheck = false;
								// keyの重複チェック
								for (Sample cSample : collectedSamples){
									// カスタムトラップ監視ではcollectedSamplesの1要素に対してperfDataは複数になる
									for (PerfData pd : cSample.getPerfDataList()) {
										if (cSample.getMonitorId().equals(monitor.getMonitorId())
												&& cSample.getDateTime().getTime() == receivedCustomTrap
														.getSampledTime()
												&& pd.getFacilityId().equals(facilityIdElement)
												&& pd.getDisplayName().equals(key)
												&& pd.getItemName().equals(monitor.getItemName())) {
											overlapCheck = true;
											break;
										}
									}
								}
								if (!overlapCheck) {
									String displayName = null;
									if (receivedCustomTrap.getKey() != null) {
										displayName = receivedCustomTrap.getKey();
									}

									// 将来予測監視、変化量監視の処理を行う
									CollectMonitorDataInfo collectMonitorDataInfo 
										= CollectMonitorManagerUtil.calculateChangePredict(
											null, monitor, facilityIdElement, displayName, monitor.getItemName(), 
											receivedCustomTrap.getSampledTime(), valueMap.get(facilityIdElement));

									// 将来予測もしくは変更点監視が有効な場合、通知を行う
									Double average = null;
									Double standardDeviation = null;
									if (collectMonitorDataInfo != null) {
										if (collectMonitorDataInfo.getChangeMonitorRunResultInfo() != null) {
											// 変化量監視の通知
											MonitorRunResultInfo collectResult = collectMonitorDataInfo.getChangeMonitorRunResultInfo();
											customtrapListBuffer.add(receivedCustomTrap);
											collectResultBuffer.add(collectResult);
											countupNotified();
										}
										if (collectMonitorDataInfo.getPredictionMonitorRunResultInfo() != null) {
											// 将来予測監視の通知
											MonitorRunResultInfo collectResult = collectMonitorDataInfo.getPredictionMonitorRunResultInfo();
											customtrapListBuffer.add(receivedCustomTrap);
											collectResultBuffer.add(collectResult);
											countupNotified();
										}
										average = collectMonitorDataInfo.getAverage();
										standardDeviation = collectMonitorDataInfo.getStandardDeviation();
									}
									notifyInfoList.addAll(notifier.createPredictionOutputBasicInfoList(
											customtrapListBuffer, monitor, agentAddr, collectResultBuffer));
									
									if (monitor.getCollectorFlg()) {
										logger.debug("work() : facilityIdElement =" + facilityIdElement);
										if (receivedCustomTrap.getKey() != null) {
											sample.set(facilityIdElement, monitor.getItemName(), valueMap.get(facilityIdElement),
													average, standardDeviation,
													CollectedDataErrorTypeConstant.NOT_ERROR, receivedCustomTrap.getKey());
										} else {
											sample.set(facilityIdElement, monitor.getItemName(), valueMap.get(facilityIdElement),
													average, standardDeviation,
													CollectedDataErrorTypeConstant.NOT_ERROR);
										}
									}
								}
								collectedSamples.add(sample);
								break;
							}
							}
						}
					} else {
						logger.debug("work() : CustomTrap CollectorFlg==false");
					}

					// 通知処理
					if (monitor.getMonitorFlg()) {
						// 関連の通知設定がなければ、スキップ
						List<NotifyRelationInfo> notifyRelationList 
							= NotifyRelationCache.getNotifyList(monitor.getNotifyGroupId());
						if (notifyRelationList == null || notifyRelationList.size() == 0) {
							logger.info("work() : notifyRelationList.size() == 0");
							continue;
						}

						List<CustomTrap> customtrapListBuffer = new ArrayList<CustomTrap>();
						List<String> facilityIdListBuffer = new ArrayList<String>();

						List<MonitorStringValueInfo> ruleListBuffer = new ArrayList<MonitorStringValueInfo>();
						List<Integer> priorityBuffer = new ArrayList<Integer>();
						int orderNo = 0;

						switch (receivedCustomTrap.getType()) {
						case STRING: {
							// 文字列データの場合
							for (MonitorStringValueInfo rule : monitor.getStringValueInfo()) {
								++orderNo;
								if (logger.isDebugEnabled()) {
									logger.info(String.format(
											"work() : monitoring (monitorId = %s, orderNo = %d, patten = %s, enabled = %s, casesensitive = %s)",
											monitor.getMonitorId(), orderNo, rule.getPattern(), rule.getValidFlg(),
											rule.getCaseSensitivityFlg()));
								}
								if (!rule.getValidFlg()) {
									// 無効化されているルールはスキップする
									logger.debug("work() : CustomTrap !rule.getValidFlg()");
									continue;
								}
								// パターンマッチを実施
								if (logger.isDebugEnabled()) {
									logger.debug(String.format("work() : filtering customtrap (regex = %s, customtrap = %s",
											rule.getPattern(), receivedCustomTrap));
								}
								try {
									Pattern pattern = null;
									if (rule.getCaseSensitivityFlg()) {
										// 大文字・小文字を区別しない場合
										pattern = Pattern.compile(rule.getPattern(),
												Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
									} else {
										// 大文字・小文字を区別する場合
										pattern = Pattern.compile(rule.getPattern(), Pattern.DOTALL);
									}

									Matcher matcher = pattern.matcher(receivedCustomTrap.getMsg());
									if (matcher.matches()) {
										if (rule.getProcessType()) {
											logger.debug(String.format("work() : matched (regex = %s, CustomTrap = %s",
													rule.getPattern(), receivedCustomTrap));
											for (String facilityIdElement : validFacilityIdList) {
												customtrapListBuffer.add(receivedCustomTrap);
												ruleListBuffer.add(rule);
												priorityBuffer.add(rule.getPriority());
												facilityIdListBuffer.add(facilityIdElement);
												countupNotified();
											}
										} else {
											logger.debug(String.format("work() : CustomTrap not ProcessType (regex = %s, CustomTrap = %s",
													rule.getPattern(), receivedCustomTrap));
										}
										break;
									} else {
										logger.debug("work() : CustomTrap rule not match rule = " + rule.getPattern());
									}
								} catch (Exception e) {
									logger.warn("work() : filtering failure. (regex = " + rule.getPattern() + ") . "
											+ e.getMessage(), e);
								}
							}

							logger.info("work() : CustomTrap Notify ValueType.string " + customtrapListBuffer.size() + "data");
							notifyInfoList.addAll(notifier.createStringOutputBasicInfoList(
									customtrapListBuffer, monitor, priorityBuffer, ruleListBuffer,
									facilityIdListBuffer, agentAddr, null));
						}
							break;
						case NUM: {
							// 数値データの場合
							List<Double> valueBuffer = new ArrayList<Double>();
							TreeMap<Integer, MonitorJudgementInfo> thresholds = 
									MonitorJudgementInfoCache.getMonitorJudgementMap(
											monitor.getMonitorId(), MonitorTypeConstant.TYPE_NUMERIC, MonitorNumericType.TYPE_BASIC.getType());
							for (String facilityIdElement : validFacilityIdList) {
								if (!valueMap.containsKey(facilityIdElement)) {
									// 初回通知の場合
									continue;
								}
								customtrapListBuffer.add(receivedCustomTrap);
								facilityIdListBuffer.add(facilityIdElement);
								int priority = judgePriority(valueMap.get(facilityIdElement), thresholds, receivedCustomTrap);
								priorityBuffer.add(priority);
								valueBuffer.add(valueMap.get(facilityIdElement));
								countupNotified();
							}
							logger.info("work() : CustomTrap Notify ValueType.num " + customtrapListBuffer.size() + "data");
							notifyInfoList.addAll(notifier.createNumOutputBasicInfoList(customtrapListBuffer, monitor, priorityBuffer, facilityIdListBuffer,
									agentAddr, valueBuffer, null));
						}
							break;
						}
					}
				}
				// DB登録
				if (!collectedStringSamples.isEmpty()) {
					logger.debug("work() : CustomTrap collectedStringSamples " + collectedStringSamples.size() + "data");
					CollectStringDataUtil.store(collectedStringSamples);
				}
			}

			// DB登録(数値)
			if (!collectedSamples.isEmpty()) {
				logger.debug("work() : CustomTrap collectedSamples " + collectedSamples.size() + "data");
				CollectDataUtil.put(collectedSamples);
			}

			/* 監視ジョブ */
			// 受信データ分処理を行う
			for (CustomTrap receivedCustomTrap : receivedCustomTraps.getCustomTraps()) {
				// 監視リスト取得
				Map<RunInstructionInfo, MonitorInfo> monitorJobMap = null;
				switch (receivedCustomTrap.getType()) {
				case STRING: {
					monitorJobMap = MonitorJobWorker.getMonitorJobMap(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S);
				}
					break;
				case NUM: {
					monitorJobMap = MonitorJobWorker.getMonitorJobMap(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N);
				}
					break;
				}

				if (monitorJobMap == null) {
					// 該当モニタがない場合
					if (logger.isDebugEnabled()) {
						logger.info("customtrap job monitor not found. skip filtering. [" + receivedCustomTrap.toString()
								+ "]");
					}
					continue;
				}
				double value = 0;// 数値用値
				for (Map.Entry<RunInstructionInfo, MonitorInfo> entry : monitorJobMap.entrySet()) {
					if (!isMatchFacilityIdList(matchedFacilityIdList, entry.getKey().getFacilityId())) {
						// 監視対象ノードでない場合
						continue;
					}
					// キーパターン
					Pattern keyPattern = Pattern.compile(entry.getValue().getCustomTrapCheckInfo().getTargetKey(),
							Pattern.DOTALL);
					Matcher matcherKeyPattern = keyPattern.matcher(receivedCustomTrap.getKey());
					if (!matcherKeyPattern.matches()) {
						logger.info("KeyPattern Unmatched");
						continue;
					}

					// 収集処理
					// 数値で差分取得の場合、Value値をsample/notify前に計算する
					if (receivedCustomTrap.getType() == Type.NUM) {
						value = Double.parseDouble(receivedCustomTrap.getMsg());
						String key = receivedCustomTrap.getKey();
						if (entry.getValue().getCustomTrapCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_DELTA) {
							// 取得した値と前回情報の差分をとり、閾値判定を行う。
							// 前回値の取得
							Object oldData = MonitorJobWorker.getPrevMonitorValue(entry.getKey());
							if (oldData == null) {
								// 前回情報なし
								Map<String, Double> map = new ConcurrentHashMap<>();
								map.put(key, value);
								MonitorJobWorker.addPrevMonitorValue(entry.getKey(), map);
								// 差分処理の初回取得処理のため、処理終了
								logger.info("No previous information No Monitoring and sampling!!");
								continue;
							} else {
								@SuppressWarnings("unchecked")
								Map<String, Double> map = (Map<String, Double>)oldData;
								if (map.get(key) == null) {
									// 前回情報はあるが、キーに対応した値がない
									map.put(key, value);
									MonitorJobWorker.addPrevMonitorValue(entry.getKey(), map);
									// 差分処理の初回取得処理のため、処理終了
									logger.info("No previous information No Monitoring and sampling!!");
									continue;
								} else {
									// 前回値を取得
									double prevValue = ((Double)map.get(key)).doubleValue();
									logger.info("CustomTrapNum prev=" + prevValue + " value = " + value + " new value="
											+ (value - prevValue));
									value -= prevValue;
								}
							}
						}
					}

					// 通知処理
					List<CustomTrap> customtrapListBuffer = new ArrayList<CustomTrap>();
					List<String> facilityIdListBuffer = new ArrayList<String>();

					List<MonitorStringValueInfo> ruleListBuffer = new ArrayList<MonitorStringValueInfo>();
					List<Integer> priorityBuffer = new ArrayList<Integer>();

					switch (receivedCustomTrap.getType()) {
					case STRING: {
						// 文字列データの場合
						for (MonitorStringValueInfo rule : entry.getValue().getStringValueInfo()) {
							if (!rule.getValidFlg()) {
								// 無効化されているルールはスキップする
								logger.debug("CustomTrap !rule.getValidFlg()");
								continue;
							}
							// パターンマッチを実施
							if (logger.isDebugEnabled()) {
								logger.debug(String.format("filtering customtrap (regex = %s, customtrap = %s",
										rule.getPattern(), receivedCustomTrap));
							}
							try {
								Pattern pattern = null;
								if (rule.getCaseSensitivityFlg()) {
									// 大文字・小文字を区別しない場合
									pattern = Pattern.compile(rule.getPattern(),
											Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
								} else {
									// 大文字・小文字を区別する場合
									pattern = Pattern.compile(rule.getPattern(), Pattern.DOTALL);
								}

								Matcher matcher = pattern.matcher(receivedCustomTrap.getMsg());
								if (matcher.matches()) {
									if (rule.getProcessType()) {
										logger.debug(String.format("matched (regex = %s, CustomTrap = %s",
												rule.getPattern(), receivedCustomTrap));
										customtrapListBuffer.add(receivedCustomTrap);
										ruleListBuffer.add(rule);
										priorityBuffer.add(rule.getPriority());
										facilityIdListBuffer.add(entry.getKey().getFacilityId());
										countupNotified();
									} else {
										logger.debug(String.format("CustomTrap not ProcessType (regex = %s, CustomTrap = %s",
												rule.getPattern(), receivedCustomTrap));
									}
									break;
								} else {
									logger.debug("CustomTrap rule not match rule = " + rule.getPattern());
								}
							} catch (Exception e) {
								logger.warn("filtering failure. (regex = " + rule.getPattern() + ") . "
										+ e.getMessage(), e);
							}
						}

						logger.info("CustomTrap Notify ValueType.string " + customtrapListBuffer.size() + "data");
						notifyInfoList.addAll(notifier.createStringOutputBasicInfoList(
								customtrapListBuffer, entry.getValue(), priorityBuffer, ruleListBuffer,
								facilityIdListBuffer, agentAddr, entry.getKey()));
					}
						break;
					case NUM: {
						// 数値データの場合
						List<Double> valueBuffer = new ArrayList<Double>();
						TreeMap<Integer, MonitorJudgementInfo> thresholds 
							 = MonitorJudgementInfoCache.getMonitorJudgementMap(
							 entry.getValue().getMonitorId(), MonitorTypeConstant.TYPE_NUMERIC, MonitorNumericType.TYPE_BASIC.getType());
						int priority = judgePriority(value, thresholds, receivedCustomTrap);
						customtrapListBuffer.add(receivedCustomTrap);
						facilityIdListBuffer.add(entry.getKey().getFacilityId());
						priorityBuffer.add(priority);
						valueBuffer.add(value);

						logger.info("CustomTrap Notify ValueType.num " + customtrapListBuffer.size() + "data");
						notifyInfoList.addAll(notifier.createNumOutputBasicInfoList(
								customtrapListBuffer, entry.getValue(), priorityBuffer, facilityIdListBuffer,
								agentAddr, valueBuffer, entry.getKey()));
					}
						break;
					}
				}
			}

			// 通知設定
			tm.addCallback(new NotifyCallback(notifyInfoList));

			tm.commit();
		} catch (HinemosUnknown | UnknownHostException e) {
			e.printStackTrace();
			logger.warn("work() : unexpected internal error. : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// HA構成のため、例外を握りつぶしてはいけない
			throw new RuntimeException(
					"unexpected internal error. : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("work() : CustomTrap data error " + e.getMessage());
			// HA構成のため、例外を握りつぶしてはいけない
			throw new RuntimeException(
					"unexpected internal error. : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			if (tm != null) {
				tm.close();
			}
		}
	}

	/**
	 * 閾値からプライオリティ（危険・警告・情報）を判定します。
	 * 
	 * @param value
	 *            測定値
	 * @param thresholds
	 *            閾値テーブル
	 * @param customTrap
	 *            監視情報ファイル
	 * @return プライオリティ
	 * @throws CustomInvalid
	 */
	private int judgePriority(Double value, TreeMap<Integer, MonitorJudgementInfo> thresholds, CustomTrap customTrap)
			throws CustomInvalid {
		// Local Variables
		int priority = PriorityConstant.TYPE_UNKNOWN;

		// MAIN
		if (Double.isNaN(value)) {
			// if user defined not a number
			priority = PriorityConstant.TYPE_UNKNOWN;
		} else {
			// if numeric value is defined
			if (thresholds.containsKey(PriorityConstant.TYPE_INFO)
					&& thresholds.containsKey(PriorityConstant.TYPE_WARNING)) {
				if (value >= thresholds.get(PriorityConstant.TYPE_INFO).getThresholdLowerLimit()
						&& value < thresholds.get(PriorityConstant.TYPE_INFO).getThresholdUpperLimit()) {
					return PriorityConstant.TYPE_INFO;
				} else if (value >= thresholds.get(PriorityConstant.TYPE_WARNING).getThresholdLowerLimit()
						&& value < thresholds.get(PriorityConstant.TYPE_WARNING).getThresholdUpperLimit()) {
					return PriorityConstant.TYPE_WARNING;
				} else {
					priority = PriorityConstant.TYPE_CRITICAL;
				}
			} else {
				// if threshold is not defined
				CustomInvalid e = new CustomInvalid(
						"configuration of CustomTrap monitor is not valid. [" + customTrap + "]");
				logger.info("judgePriority() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		return priority;
	}

	/**
	 * カスタム監視情報に対応するファシリティ一覧を取得する
	 * 
	 * @param facilityIdList
	 *            ファシリティ一覧
	 * @param monitor
	 *            カスタム監視情報
	 * @return カスタム監視情報に対応するファシリティ一覧
	 */
	private List<String> getValidFacilityIdList(List<String> facilityIdList, MonitorInfo monitor) {
		List<String> validFacilityIdList = new ArrayList<String>();
		for (String facilityId : facilityIdList) {

			if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)) {
				if (!FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(monitor.getFacilityId())) {
					// 未登録ノードから送信されたCustomTrapだが、未登録ノードに対する設定でない場合はスキップする
					continue;
				}
			} else {
				if (!new RepositoryControllerBean().containsFaciliyId(monitor.getFacilityId(), facilityId,
						monitor.getOwnerRoleId())) {
					// CustomTrapの送信元ノードが、設定のスコープ内に含まれない場合はスキップする
					continue;
				}
			}

			validFacilityIdList.add(facilityId);

		}
		return validFacilityIdList;
	}


	/**
	 * 監視情報に対応するファシリティを取得する（監視ジョブで使用）
	 * 
	 * @param facilityIdList
	 *            ファシリティ一覧
	 * @param facilityId 監視対象のファシリティID
	 *            カスタム監視情報
	 * @return true：一致、false：不一致
	 */
	private boolean isMatchFacilityIdList(List<String> facilityIdList, String monitorJobFacilityId) {
		for (String facilityId : facilityIdList) {
			if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)) {
				if (!FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(monitorJobFacilityId)) {
					// 未登録ノードから送信されたCustomTrapだが、未登録ノードに対する設定でない場合はスキップする
					continue;
				}
			} else {
				if (facilityId.equals(monitorJobFacilityId)) {
					// 監視対象
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 受信したカスタム監視情報が設定されたカレンダー内のものかを判定する
	 * 
	 * @param monitor
	 *            カスタム監視設定
	 * @param recievedCustomTrap
	 *            受信したカスタム監視情報
	 * @return 設定カレンダー内である場合はtrue
	 */
	private boolean isNotInCalendar(MonitorInfo monitor, CustomTrap recievedCustomTrap) {
		boolean notInCalendar = false;

		// カレンダが割り当てられている場合
		if (monitor.getCalendarId() != null && monitor.getCalendarId().length() > 0) {
			try {
				boolean run = new SelectCalendar().isRun(monitor.getCalendarId(), recievedCustomTrap.getSampledTime());
				notInCalendar = !run;
			} catch (CalendarNotFound e) {
				logger.warn("calendar not found (calendarId = " + monitor.getCalendarId() + ")");
			} catch (InvalidRole e) {
				logger.warn("calendar not found (calendarId = " + monitor.getCalendarId() + ") ," + e.getMessage());
			}

			// カレンダの有効期間外の場合
			if (notInCalendar) {
				if (logger.isDebugEnabled()) {
					logger.debug("skip monitoring because of calendar. (monitorId = " + monitor.getMonitorId()
							+ ", calendarId = " + monitor.getCalendarId() + ")");
				}
			}
		}
		return notInCalendar;
	}

	private synchronized void countupNotified() {
		notifiedCount = notifiedCount >= Long.MAX_VALUE ? 0 : notifiedCount + 1;
		int _statsInterval = HinemosPropertyCommon.monitor_customtrap_stats_interval.getIntegerValue();
		logger.info("monitor.customtrap.stats.interval = " + _statsInterval);
		if (notifiedCount % _statsInterval == 0) {
			logger.info("The number of CustomTrap (notified) : " + notifiedCount);
		}
	}
}
