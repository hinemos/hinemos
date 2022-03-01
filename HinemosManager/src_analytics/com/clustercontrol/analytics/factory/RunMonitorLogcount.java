/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.factory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import jakarta.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.analytics.model.LogcountCheckInfo;
import com.clustercontrol.analytics.util.AnalyticsUtil;
import com.clustercontrol.analytics.util.QueryUtil;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.QueryExecutor;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hub.bean.StringQueryInfo;
import com.clustercontrol.hub.bean.StringQueryInfo.Operator;
import com.clustercontrol.hub.model.CollectStringKeyInfo;
import com.clustercontrol.hub.bean.StringQueryResult;
import com.clustercontrol.hub.session.HubControllerBean;
import com.clustercontrol.hub.session.HubControllerBean.Token;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil.CollectMonitorDataInfo;
import com.clustercontrol.monitor.run.util.MonitorMultipleExecuteTask;
import com.clustercontrol.monitor.run.util.ParallelExecution;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * ログ件数監視 数値監視設定を実行するファクトリークラス<BR>
 *
 * @version 6.1.0
 */
public class RunMonitorLogcount extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorLogcount.class );

	/** ログ件数監視情報 */
	private LogcountCheckInfo m_logCount = null;

	/** 不明メッセージ */
	private String m_unKnownMessage = null;

	/** メッセージ **/
	private String m_message = null;

	/** オリジナルメッセージ **/
	private String m_messageorg = null;
	
	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorLogcount() {
		super();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.MonitorExecuteTask
	 */
	@Override
	protected RunMonitorLogcount createMonitorInstance() {
		return new RunMonitorLogcount();
	}

	/**
	 * 監視に必要な収集データ、および、収集値を取得する
	 * 
	 * @param facilityId ファシリティID（処理では使用しない）
	 * @return 監視結果のリスト
	 */
	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#collectMultiple(java.lang.String)
	 */
	@Override
	public List<MonitorRunResultInfo> collectMultiple(String facilityId) throws InvalidSetting, HinemosDbTimeout {
		List<MonitorRunResultInfo> list = new ArrayList<>();
		
		Double collectCount = null;
		Double maxCount = null;
		String maxCountPeriod = "";
		
		// set Generation Date
		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}
		
		m_message = "";
		// 日付情報
		int interval = m_monitor.getRunInterval() * 1000;	//監視間隔
		Long to = m_nodeDate - m_nodeDate % interval;	//監視実行時刻
		Long lastMonitorTime = to - interval;	//前回の監視実行時刻
		Long from = to - interval * 2;	//前々回の監視実行時刻
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
		sdf.setTimeZone(HinemosTime.getTimeZone());
		String collectPeriod = MessageConstant.COLLECTION_DATETIME.getMessage(new String[]{
				sdf.format(new Date(lastMonitorTime)) + " - " + sdf.format(new Date(to))});
		
		//値取得
		StringQueryResult stringQueryResult = summaryLogcount(m_monitor, facilityId, from, to, true);
		if (stringQueryResult == null) {
			// データが取得されない場合
			MonitorRunResultInfo info = new MonitorRunResultInfo();
			m_message =  String.format("%s : %s", MessageConstant.DATA_COUNT.getMessage(), null);
			m_messageorg = String.format("%s%n%s%n%s%n%s", 
					m_message, 
					String.format("%s : %s", MessageConstant.COUNT_DATETIME.getMessage(),
							sdf.format(new Date(from)) + " - " + sdf.format(new Date(to))),
					String.format("%s : %s", m_monitor.getItemName(), null), 
					collectPeriod);
			boolean ret = false;
			Integer checkResult = getCheckResult(ret);
			info.setFacilityId(facilityId);
			info.setMonitorFlg(ret);
			info.setCollectorResult(ret);
			info.setCheckResult(checkResult);
			info.setMessage(getMessage(checkResult));
			info.setMessageOrg(getMessageOrg(checkResult));
			info.setPriority(getPriority(checkResult));
			info.setProcessType(true);
			info.setNodeDate(to);
			info.setValue(null);
			info.setCurData(null);
			info.setItemName(m_monitor.getItemName());
			info.setDisplayName("");
			info.setCollectorFlg(m_monitor.getCollectorFlg());
			info.setNotifyGroupId(getNotifyGroupId());
			info.setApplication(m_monitor.getApplication());
			list.add(info);
		} else {
			if(m_logCount.getTag() == null || m_logCount.getTag().isEmpty()) {
				// 全て集計
				List<Long> stringDataList = stringQueryResult.getCollectDataList();
				if (stringDataList == null || stringDataList.size() == 0) {
					//収集データの件数が0件
					MonitorRunResultInfo info = new MonitorRunResultInfo();
					m_value = Double.valueOf(0);
					m_message = String.format("%s : %s", MessageConstant.DATA_COUNT.getMessage(), Double.toString(m_value));
					m_messageorg = String.format("%s%n%s%n%s%n%s", 
							m_message, 
							String.format("%s : %s", MessageConstant.COUNT_DATETIME.getMessage(),
									sdf.format(new Date(from)) + " - " + sdf.format(new Date(to))),
							String.format("%s : %s", m_monitor.getItemName(), Double.toString(m_value)), 
							collectPeriod);
					boolean ret = true;
					Integer checkResult = getCheckResult(ret);
					info.setFacilityId(facilityId);
					info.setMonitorFlg(ret);
					info.setCollectorResult(ret);
					info.setCheckResult(checkResult);
					info.setMessage(getMessage(checkResult));
					info.setMessageOrg(getMessageOrg(checkResult));
					info.setPriority(getPriority(checkResult));
					info.setProcessType(true);
					info.setNodeDate(to);
					info.setValue(getValue());
					info.setCurData(getCurData());
					info.setItemName(m_monitor.getItemName());
					info.setDisplayName("");
					info.setCollectorFlg(m_monitor.getCollectorFlg());
					info.setNotifyGroupId(getNotifyGroupId());
					info.setApplication(m_monitor.getApplication());
					list.add(info);
				} else {
					//収集データの件数が1件以上
					//カウント結果のリストを取得
					List<Entry<String, Double>> countForJudgementList = getCountList(stringDataList, from, to, interval, sdf);
					
					//ログ件数監視の収集値として登録する値を取得(前回の監視実行から監視実行までのデータ件数)
					collectCount = Double.valueOf(stringDataList.stream()
							.filter(time -> time > lastMonitorTime)
							.filter(time -> time <= to)
							.collect(Collectors.toList())
							.size());
					
					boolean ret = true;
					
					//カウント数の最大値をもとに閾値判定
					maxCountPeriod = countForJudgementList.get(0).getKey();
					m_value = countForJudgementList.get(0).getValue();
					Integer checkResult = getCheckResult(ret);
					if (checkResult == PriorityConstant.TYPE_INFO || checkResult == PriorityConstant.TYPE_WARNING) {
						//情報か警告の場合は、最大値のみで通知
						m_message =  String.format("%s : %s", MessageConstant.DATA_COUNT.getMessage(), Double.toString(m_value));
						m_messageorg = String.format("%s%n%s%n%s%n%s", 
								m_message, 
								String.format("%s : %s", MessageConstant.COUNT_DATETIME.getMessage(), maxCountPeriod),
								String.format("%s : %s", m_monitor.getItemName(), Double.toString(collectCount)), 
								collectPeriod);
					} else if (checkResult == PriorityConstant.TYPE_CRITICAL) {
						//危険の場合は、危険で判定される最大値、最小値で通知
						
						//危険で判定される最小値と集計期間を取得
						maxCount = m_value;
						String[] minData = getMinDataForNotify(countForJudgementList);
						
						m_message = String.format("%s : %s", MessageConstant.DATA_COUNT.getMessage(), 
								MessageConstant.MAX_MIN_VALUE.getMessage(Double.toString(maxCount), minData[0]));
						m_messageorg = String.format("%s%n%s%n%s%n%s", 
								m_message, 
								String.format("%s : %s", MessageConstant.COUNT_DATETIME.getMessage(), 
										MessageConstant.MAX_MIN_VALUE.getMessage(maxCountPeriod, minData[1])),
								String.format("%s : %s", m_monitor.getItemName(), Double.toString(collectCount)), 
								collectPeriod);
					}
					MonitorRunResultInfo info = new MonitorRunResultInfo();
					info.setFacilityId(facilityId);
					info.setMonitorFlg(ret);
					info.setCollectorResult(ret);
					info.setCheckResult(checkResult);
					info.setMessage(getMessage(checkResult));
					info.setMessageOrg(getMessageOrg(checkResult));
					info.setPriority(getPriority(checkResult));
					info.setProcessType(true);
					info.setNodeDate(to);
					info.setValue(collectCount);
					info.setCurData(getCurData());
					info.setItemName(m_monitor.getItemName());
					info.setDisplayName("");
					info.setCollectorFlg(m_monitor.getCollectorFlg());
					info.setNotifyGroupId(getNotifyGroupId());
					info.setApplication(m_monitor.getApplication());
					list.add(info);
				}
			} else {
				// タグ集計
				if (stringQueryResult.getCollectDataMap() != null) {
					for (Map.Entry<String, List<Long>> entry : stringQueryResult.getCollectDataMap().entrySet()) {
						List<Long> collectTimeList = entry.getValue();
						
						//カウント結果のリストを取得
						List<Entry<String, Double>> countForJudgementList = getCountList(collectTimeList, from, to, interval, sdf);
						
						//ログ件数監視の収集値として登録する値を取得(前回の監視実行から監視実行までのデータ件数)
						collectCount = Double.valueOf(collectTimeList.stream()
								.filter(time -> time > lastMonitorTime)
								.filter(time -> time <= to)
								.collect(Collectors.toList())
								.size());
						
						boolean ret = true;
						
						//カウント数の最大値をもとに閾値判定
						maxCountPeriod = countForJudgementList.get(0).getKey();
						m_value = countForJudgementList.get(0).getValue();
						Integer checkResult = getCheckResult(ret);
						if (checkResult == PriorityConstant.TYPE_INFO || checkResult == PriorityConstant.TYPE_WARNING) {
							//情報か警告の場合は、最大値のみで通知
							m_message =  String.format("%s : %s", MessageConstant.DATA_COUNT.getMessage(), Double.toString(m_value));
							m_messageorg = String.format("%s%n%s%n%s%n%s", 
									m_message, 
									String.format("%s : %s", MessageConstant.COUNT_DATETIME.getMessage(), maxCountPeriod),
									String.format("%s : %s", m_monitor.getItemName(), Double.toString(collectCount)), 
									collectPeriod);
						} else if (checkResult == PriorityConstant.TYPE_CRITICAL) {
							//危険の場合は、危険で判定される最大値、最小値で通知
							
							//危険で判定される最小値と集計期間を取得
							maxCount = m_value;
							String[] minData = getMinDataForNotify(countForJudgementList);
							
							m_message = String.format("%s : %s", MessageConstant.DATA_COUNT.getMessage(), 
									MessageConstant.MAX_MIN_VALUE.getMessage(Double.toString(maxCount), minData[0]));
							m_messageorg = String.format("%s%n%s%n%s%n%s", 
									m_message, 
									String.format("%s : %s", MessageConstant.COUNT_DATETIME.getMessage(), 
											MessageConstant.MAX_MIN_VALUE.getMessage(maxCountPeriod, minData[1])),
									String.format("%s : %s", m_monitor.getItemName(), Double.toString(collectCount)), 
									collectPeriod);
						}
						MonitorRunResultInfo info = new MonitorRunResultInfo();
						info.setFacilityId(facilityId);
						info.setMonitorFlg(ret);
						info.setCollectorResult(ret);
						info.setCheckResult(checkResult);
						info.setMessage(getMessage(checkResult));
						info.setMessageOrg(getMessageOrg(checkResult));
						info.setPriority(getPriority(checkResult));
						info.setProcessType(true);
						info.setNodeDate(to);
						info.setValue(collectCount);
						info.setCurData(getCurData());
						info.setItemName(m_monitor.getItemName());
						info.setDisplayName(entry.getKey());
						info.setCollectorFlg(m_monitor.getCollectorFlg());
						info.setNotifyGroupId(getNotifyGroupId());
						info.setApplication(m_monitor.getApplication());
						list.add(info);
					}
				}
			}
		}
		return list;
	}
	
	/**
	 * データ件数のカウント結果を取得する <br>
	 * 前回の監視以降の各収集データを対象に監視間隔分過去に遡り、件数をカウントする
	 * 
	 * @param collectTimeList カウント対象の収集データのリスト(収集日時の昇順でソート済)
	 * @param countFrom カウント対象期間の起点日時(前々回の監視実行日時)
	 * @param countTo カウント対象期間の終点日時(監視実行日時)
	 * @param interval 監視の実行間隔
	 * @param sdf 文字列フォーマット用のSimpleDateFormatクラスのインスタンス
	 * @return カウント結果のリスト (EntryのKey：カウント期間、Value：件数)
	 */
	private List<Entry<String, Double>> getCountList(List<Long> collectTimeList, Long countFrom, Long countTo, int interval, SimpleDateFormat sdf) {
		int nextIdx = 0;
		boolean isCheckedZeroPeriod = false;
		boolean isExistDataAfterLastMonitor = false;
		
		//前回の監視実行時間
		Long lastMonitorTime = countTo - interval;
		
		Map<String, Double> countMap = new HashMap<String, Double>();
		
		for (Long collectTime : collectTimeList) {
			if (collectTime > lastMonitorTime) {
				//収集日時が前回の監視実行時間よりも後のデータを対象に処理
				isExistDataAfterLastMonitor = true;
				
				//件数のカウント
				//収集日時から監視間隔分過去に遡り、その期間のデータ件数を取得
				Double count = (double)collectTimeList.stream()
						.filter(time -> time > (collectTime - interval))
						.filter(time -> time <= collectTime)
						.collect(Collectors.toList())
						.size();
				String countPeriod = sdf.format(new Date(collectTime - interval)) + " - " + sdf.format(new Date(collectTime));
				//集計期間と件数をMapに追加
				countMap.put(countPeriod, count);
				
				//件数が0件だった期間の有無の確認
				if (!isCheckedZeroPeriod) {
					nextIdx++;
					
					//収集日時が1つ前のデータとの時間差が、監視間隔以上空いているかを確認
					if (nextIdx < collectTimeList.size()) {
						Long previousCollectTime = collectTimeList.get(nextIdx);
						if ((collectTime - previousCollectTime) > interval) {
							//監視間隔以上空いている場合
							String countZeroPeriod = sdf.format(new Date(previousCollectTime)) + " - " + sdf.format(new Date(collectTime));
							countMap.put(countZeroPeriod, Double.valueOf(0));
							
							isCheckedZeroPeriod = true;
						}
					} else {
						//収集データがこれ以上存在しない場合
						String strMinZeroPeriod = sdf.format(new Date(countFrom)) + " - " + sdf.format(new Date(collectTime));
						countMap.put(strMinZeroPeriod, Double.valueOf(0));
						
						isCheckedZeroPeriod = true;
					}
				}
			} else {
				if(!isExistDataAfterLastMonitor) {
					//収集日時が前回の監視実行時間より前、かつ、前回の監視実行時間より後の収集データがない場合
					String strCountPeriod = sdf.format(new Date(collectTime)) + " - " + sdf.format(new Date(countTo));
					countMap.put(strCountPeriod, Double.valueOf(0));
				} 
				break;
			}
		}
		
		List<Entry<String, Double>> sortedCountList = new ArrayList<Entry<String, Double>>(countMap.entrySet());
		Collections.sort(sortedCountList, new Comparator<Entry<String, Double>>() {
			//収集日時の降順でソート
			public int compare(Entry<String, Double> collectData1, Entry<String, Double> collectData2) {
				return collectData2.getValue().compareTo(collectData1.getValue());
			}
		});
		
		return sortedCountList;
	}

	/**
	 * 重要度が危険で判定される最小値、カウント期間を取得する
	 * 
	 * @param countForJudgementList 閾値判定対象の収集データのリスト(収集日時の昇順でソート済)
	 * @return 危険で判定される最小値、カウント期間
	 */
	private String[] getMinDataForNotify(List<Entry<String, Double>> countForJudgementList) {
		String[] minData = new String[2];
		
		//リストの最後の要素から順に処理
		for (int i = countForJudgementList.size() - 1; i >= 0; i--) {
			m_value = countForJudgementList.get(i).getValue();
			
			//重要度が危険で判定される値、その集計期間を取得
			if (getCheckResult(true) == PriorityConstant.TYPE_CRITICAL) {
				minData[0] = Double.toString(countForJudgementList.get(i).getValue());
				minData[1] = countForJudgementList.get(i).getKey();
				
				break;
			}
		}
		
		return minData;
	}

	public StringQueryResult summaryLogcount(MonitorInfo monitorInfo, String facilityId, Long fromDate, Long toDate, boolean isMonitor) 
		throws InvalidSetting, HinemosDbTimeout {
		StringQueryResult rtn = null;

		// 検索条件作成
		Operator ope = null;
		LogcountCheckInfo logCount = monitorInfo.getLogcountCheckInfo();
		if (logCount == null) {
			return rtn;
		}
		if (logCount.getIsAnd() == null || logCount.getIsAnd()) {
			ope = Operator.AND;
		} else {
			ope = Operator.OR;
		}
		StringQueryInfo stringQueryInfo = AnalyticsUtil.makeQuery(fromDate, toDate, facilityId,
			logCount.getTargetMonitorId(), logCount.getKeyword(), logCount.getTag(), ope);
		// 値取得
		int logSearchTimeout = HinemosPropertyCommon.monitor_logcount_search_timeout.getIntegerValue();
		if (isMonitor) {
			//ログ件数監視
			rtn = queryCollectStringDataForMonitorLogcount(stringQueryInfo, logSearchTimeout);
		} else {
			//ログ件数集計
			rtn = queryCollectStringDataForSummaryLogcount(stringQueryInfo, logSearchTimeout);
		}
		return rtn;
	}

	/**
	 * 文字列収集情報を検索する。
	 * ログ件数集計で使用する。
	 * 
	 * @param queryInfo 検索条件
	 * @param logSearchTimeout 検索タイムアウト値
	 * @return 検索結果
	 * @throws HinemosDbTimeout
	 * @throws InvalidSetting
	 */
	private StringQueryResult queryCollectStringDataForSummaryLogcount(StringQueryInfo queryInfo, Integer logSearchTimeout) 
			throws HinemosDbTimeout, InvalidSetting {
		m_log.debug(String.format("queryCollectStringDataForSummaryLogcount() : start query. query=%s", queryInfo));

		// 入力値判定
		if (queryInfo.getFrom() == null 
				|| queryInfo.getTo() == null
				|| queryInfo.getFacilityId() == null
				|| queryInfo.getMonitorId() == null) {
			m_log.warn("queryCollectStringDataForSummaryLogcount() : "
					+ "parameter is null query=" + queryInfo);
			throw new InvalidSetting("parameter is null query=" + queryInfo);
		}

		if (queryInfo.getFrom() > queryInfo.getTo()){
			m_log.warn("queryCollectStringDataForSummaryLogcount() : "
					+ MessageConstant.MESSAGE_HUB_SEARCH_DATE_INVALID.getMessage());
			throw new InvalidSetting(MessageConstant.MESSAGE_HUB_SEARCH_DATE_INVALID.getMessage());
		}

		long start = System.currentTimeMillis();
		StringQueryResult result = new StringQueryResult();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			// キーのクエリ
			StringBuilder keyQueryStr = new StringBuilder("SELECT DISTINCT k FROM CollectStringKeyInfo k");
			keyQueryStr.append(" WHERE k.id.facilityId = :facilityId");
			keyQueryStr.append(" AND k.id.monitorId = :monitorId");
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("facilityId", queryInfo.getFacilityId());
			parameters.put("monitorId", queryInfo.getMonitorId());
			List<CollectStringKeyInfo> ketResults = QueryExecutor.getListByJpqlWithTimeout(
					keyQueryStr.toString(), 
					CollectStringKeyInfo.class, 
					parameters, 
					logSearchTimeout);
			Map<Long, CollectStringKeyInfo> keys = new HashMap<>();
			for (CollectStringKeyInfo r: ketResults) {
				keys.put(r.getCollectId(), r);
			}
			m_log.debug(String.format("queryCollectStringDataForSummaryLogcount() : target data key. keys=%s, query=%s", keys.values(), queryInfo));
			
			if (keys.isEmpty()) {
				// アクセスできるキーがないので終了。
				result = new StringQueryResult();
				result.setOffset(queryInfo.getOffset());
				result.setSize(0);
				result.setCount((queryInfo.isNeedCount() != null && queryInfo.isNeedCount()) ? 0: null);
				result.setTime(System.currentTimeMillis() - start);
				m_log.debug(String.format("queryCollectStringDataForSummaryLogcount() : end query. result=%s, query=%s", result, queryInfo));
				return result;
			}

			// データのクエリ
			StringBuilder dataQueryStr;
			if (queryInfo.getTag() == null || queryInfo.getTag().isEmpty()) {
				// 全件
				dataQueryStr = new StringBuilder("FROM CollectStringData d");
			} else {
				// タグごとの集計
				dataQueryStr = new StringBuilder("FROM CollectDataTag t JOIN t.collectStringData d");
			}
			dataQueryStr.append(" WHERE d.id.collectId IN :collectIds");
			dataQueryStr.append(" AND d.time >= :from");
			dataQueryStr.append(" AND d.time < :to");
			StringBuilder whereStr = new StringBuilder();
			if (queryInfo.getKeywords() != null && !queryInfo.getKeywords().isEmpty()) {
				StringBuffer conditionValueBuffer=new StringBuffer();
				String operator="";
				if (com.clustercontrol.hub.bean.StringQueryInfo.Operator.AND ==  queryInfo.getOperator() ){
					operator = " AND ";
				}else{
					operator = " OR ";
				}
				String keywords = queryInfo.getKeywords();
				List<Token> tokens = HubControllerBean.parseKeywords(keywords);
				for (Token token: tokens) {
					if (conditionValueBuffer.length() != 0){
						conditionValueBuffer.append(operator);
					} else {
						conditionValueBuffer.append("(");
					}

					if (token.key == null) {
						// タグ指定ではない場合
						if (token.negate){
							if (token.word.length() == 1){
								m_log.warn("queryCollectStringDataForSummaryLogcount() : "
										+ MessageConstant.MESSAGE_HUB_SEARCH_KEYWORD_INVALID.getMessage());
								throw new InvalidSetting(MessageConstant.MESSAGE_HUB_SEARCH_KEYWORD_INVALID.getMessage());
							}
							conditionValueBuffer
								.append(String.format("NOT EXISTS(SELECT t FROM d.tagList t WHERE t.value = '%s')", token.word));
						} else {
							conditionValueBuffer
								.append(String.format("EXISTS(SELECT t FROM d.tagList t WHERE t.value = '%s')", token.word));
						}
					} else {
						// タグ指定の場合
						if (token.negate){
							conditionValueBuffer.append(String.format("EXISTS(SELECT t FROM d.tagList t WHERE (t.key = '%s' AND t.value <> '%s'))", token.key, token.word));
						}else{
							conditionValueBuffer.append(String.format("EXISTS(SELECT t FROM d.tagList t WHERE (t.key = '%s' AND t.value = '%s'))", token.key, token.word));
						}
					}
				}
				if (conditionValueBuffer.length() != 0){
					whereStr.append(" AND " + conditionValueBuffer.toString()).append(")");
				}
				if (whereStr.length() != 0) {
					dataQueryStr.append(whereStr);
				}
			}

			result.setOffset(queryInfo.getOffset());
			
			// データ件数を取得する。
			if (queryInfo.isNeedCount() != null && queryInfo.isNeedCount()) {
				String queryStr = "";
				if (queryInfo.getTag() == null || queryInfo.getTag().isEmpty()) {
					// 全件
					queryStr = "SELECT COUNT(DISTINCT d) " + dataQueryStr.toString();
					m_log.debug(String.format("queryCollectStringDataForSummaryLogcount() : query count. queryStr=%s, query=%s", queryStr, queryInfo));
					parameters = new HashMap<String, Object>();
					parameters.put("collectIds", keys.keySet());
					parameters.put("from", queryInfo.getFrom());
					parameters.put("to", queryInfo.getTo());
					Long count = QueryExecutor.getDataByJpqlWithTimeout(
							queryStr.toString(), 
							Long.class,
							parameters, 
							logSearchTimeout);
					if (count == null || count == 0) {
						result.setSize(0);
						result.setCount(0);
						result.setTime(System.currentTimeMillis() - start);
						m_log.debug(String.format("queryCollectStringDataForSummaryLogcount() : end query. result=%s, query=%s", result, queryInfo));
						return result;
					}
					result.setCount(Integer.valueOf(count.toString()));
				} else {
					// タグごとの集計
					queryStr = "SELECT t.value, COUNT(DISTINCT d) " 
							+ dataQueryStr.toString()
							+ " AND t.id.key = :tag"
							+ " GROUP BY t.value";
					m_log.debug(String.format("queryCollectStringDataForSummaryLogcount() : query count. queryStr=%s, query=%s", queryStr, queryInfo));
					parameters = new HashMap<String, Object>();
					parameters.put("collectIds", keys.keySet());
					parameters.put("from", queryInfo.getFrom());
					parameters.put("to", queryInfo.getTo());
					parameters.put("tag", queryInfo.getTag());
					List<Object[]> tagCountList = QueryExecutor.getListByJpqlWithTimeout(
							queryStr.toString(), 
							Object[].class,
							parameters, 
							logSearchTimeout);
					if (tagCountList == null) {
						result.setSize(0);
						result.setCount(0);
						result.setTime(System.currentTimeMillis() - start);
						m_log.debug(String.format("queryCollectStringDataForSummaryLogcount() : end query. result=%s, query=%s", result, queryInfo));
						return result;
					}
					Map<String, Integer> tagCountMap = new HashMap<>();
					int count = 0;
					for (Object[] logcountData : tagCountList) {
						tagCountMap.put((String)logcountData[0], ((Long)logcountData[1]).intValue());
						count += ((Long)logcountData[1]).intValue();
					}
					result.setTagCountMap(tagCountMap);
					result.setCount(count);
				}
			}
			
			result.setTime(System.currentTimeMillis() - start);
			
			m_log.debug(String.format("queryCollectStringDataForSummaryLogcount() : end query. result=%s, query=%s", result.toResultString(), queryInfo));
		}
		return result;
	}

	/**
	 * 文字列収集情報を検索する。
	 * ログ件数監視で使用する。
	 * 
	 * @param queryInfo 検索条件
	 * @param logSearchTimeout 検索タイムアウト値
	 * @return 検索結果
	 * @throws HinemosDbTimeout
	 * @throws InvalidSetting
	 */
	private StringQueryResult queryCollectStringDataForMonitorLogcount(StringQueryInfo queryInfo, Integer logSearchTimeout) 
			throws HinemosDbTimeout, InvalidSetting {
		m_log.debug(String.format("queryCollectStringDataForMonitorLogcount() : start query. query=%s", queryInfo));

		// 入力値判定
		if (queryInfo.getFrom() == null 
				|| queryInfo.getTo() == null
				|| queryInfo.getFacilityId() == null
				|| queryInfo.getMonitorId() == null) {
			m_log.warn("queryCollectStringDataForMonitorLogcount() : "
					+ "parameter is null query=" + queryInfo);
			throw new InvalidSetting("parameter is null query=" + queryInfo);
		}

		if (queryInfo.getFrom() > queryInfo.getTo()){
			m_log.warn("queryCollectStringDataForMonitorLogcount() : "
					+ MessageConstant.MESSAGE_HUB_SEARCH_DATE_INVALID.getMessage());
			throw new InvalidSetting(MessageConstant.MESSAGE_HUB_SEARCH_DATE_INVALID.getMessage());
		}

		long start = System.currentTimeMillis();
		StringQueryResult result = new StringQueryResult();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			// キーのクエリ
			StringBuilder keyQueryStr = new StringBuilder("SELECT DISTINCT k FROM CollectStringKeyInfo k");
			keyQueryStr.append(" WHERE k.id.facilityId = :facilityId");
			keyQueryStr.append(" AND k.id.monitorId = :monitorId");
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("facilityId", queryInfo.getFacilityId());
			parameters.put("monitorId", queryInfo.getMonitorId());
			List<CollectStringKeyInfo> ketResults = QueryExecutor.getListByJpqlWithTimeout(
					keyQueryStr.toString(), 
					CollectStringKeyInfo.class, 
					parameters, 
					logSearchTimeout);
			Map<Long, CollectStringKeyInfo> keys = new HashMap<>();
			for (CollectStringKeyInfo r: ketResults) {
				keys.put(r.getCollectId(), r);
			}
			m_log.debug(String.format("queryCollectStringDataForMonitorLogcount() : target data key. keys=%s, query=%s", keys.values(), queryInfo));
			
			if (keys.isEmpty()) {
				// アクセスできるキーがないので終了。
				result.setOffset(queryInfo.getOffset());
				result.setSize(0);
				result.setCount((queryInfo.isNeedCount() != null && queryInfo.isNeedCount()) ? 0: null);
				result.setTime(System.currentTimeMillis() - start);
				m_log.debug(String.format("queryCollectStringDataForMonitorLogcount() : end query. result=%s, query=%s", result, queryInfo));
				return result;
			}

			// データのクエリ
			StringBuilder dataQueryStr;
			if (queryInfo.getTag() == null || queryInfo.getTag().isEmpty()) {
				// 全件
				dataQueryStr = new StringBuilder("FROM CollectStringData d");
			} else {
				// タグごとの集計
				dataQueryStr = new StringBuilder("FROM CollectDataTag t JOIN t.collectStringData d");
			}
			dataQueryStr.append(" WHERE d.id.collectId IN :collectIds");
			dataQueryStr.append(" AND d.time >= :from");
			dataQueryStr.append(" AND d.time < :to");
			StringBuilder whereStr = new StringBuilder();
			if (queryInfo.getKeywords() != null && !queryInfo.getKeywords().isEmpty()) {
				StringBuffer conditionValueBuffer = new StringBuffer();
				String operator="";
				if (com.clustercontrol.hub.bean.StringQueryInfo.Operator.AND ==  queryInfo.getOperator() ){
					operator = " AND ";
				}else{
					operator = " OR ";
				}
				String keywords = queryInfo.getKeywords();
				List<Token> tokens = HubControllerBean.parseKeywords(keywords);
				for (Token token: tokens) {
					if (conditionValueBuffer.length() != 0){
						conditionValueBuffer.append(operator);
					} else {
						conditionValueBuffer.append("(");
					}
					
					if (token.key == null) {
						// タグ指定ではない場合
						if (token.negate){
							if (token.word.length() == 1){
								m_log.warn("queryCollectStringDataForMonitorLogcount() : "
										+ MessageConstant.MESSAGE_HUB_SEARCH_KEYWORD_INVALID.getMessage());
								throw new InvalidSetting(MessageConstant.MESSAGE_HUB_SEARCH_KEYWORD_INVALID.getMessage());
							}
							conditionValueBuffer
								.append(String.format("NOT EXISTS(SELECT t FROM d.tagList t WHERE t.value = '%s')", token.word));
						} else {
							conditionValueBuffer
								.append(String.format("EXISTS(SELECT t FROM d.tagList t WHERE t.value = '%s')", token.word));
						}
					} else {
						// タグ指定の場合
						if (token.negate){
							conditionValueBuffer.append(String.format("EXISTS(SELECT t FROM d.tagList t WHERE (t.key = '%s' AND t.value <> '%s'))", token.key, token.word));
						}else{
							conditionValueBuffer.append(String.format("EXISTS(SELECT t FROM d.tagList t WHERE (t.key = '%s' AND t.value = '%s'))", token.key, token.word));
						}
					}
				}
				if (conditionValueBuffer.length() != 0){
					whereStr.append(" AND " + conditionValueBuffer.toString()).append(")");
				}
				if (whereStr.length() != 0) {
					dataQueryStr.append(whereStr);
				}
			}

			result.setOffset(queryInfo.getOffset());
			
			// カウント対象の収集データを取得する。
			if (queryInfo.isNeedCount() != null && queryInfo.isNeedCount()) {
				String queryStr = "";
				if (queryInfo.getTag() == null || queryInfo.getTag().isEmpty()) {
					// 全件
					queryStr = "SELECT d.time " + dataQueryStr.toString() + " ORDER BY d.time DESC";
					m_log.debug(String.format("queryCollectStringDataForMonitorLogcount() : query collect data. queryStr=%s, query=%s", queryStr, queryInfo));
					parameters = new HashMap<String, Object>();
					parameters.put("collectIds", keys.keySet());
					parameters.put("from", queryInfo.getFrom());
					parameters.put("to", queryInfo.getTo());
					List<Long> collectDataList = QueryExecutor.getListByJpqlWithTimeout(
							queryStr.toString(), 
							Long.class,
							parameters, 
							logSearchTimeout);
					if (collectDataList == null || collectDataList.isEmpty()) {
						result.setSize(0);
						result.setCount(0);
						result.setTime(System.currentTimeMillis() - start);
						m_log.debug(String.format("queryCollectStringDataForMonitorLogcount() : end query. result=%s, query=%s", result, queryInfo));
						return result;
					}
					result.setCount(collectDataList.size());
					result.setCollectDataList(collectDataList);
					
				} else {
					// タグごとの集計
					queryStr = "SELECT t.value, d.time " 
							+ dataQueryStr.toString()
							+ " AND t.id.key = :tag"
							+ " ORDER BY t.value, d.time DESC";
					m_log.debug(String.format("queryCollectStringDataForMonitorLogcount() : query collect data. queryStr=%s, query=%s", queryStr, queryInfo));
					parameters = new HashMap<String, Object>();
					parameters.put("collectIds", keys.keySet());
					parameters.put("from", queryInfo.getFrom());
					parameters.put("to", queryInfo.getTo());
					parameters.put("tag", queryInfo.getTag());
					List<Object[]> collectDataList = QueryExecutor.getListByJpqlWithTimeout(
							queryStr.toString(), 
							Object[].class,
							parameters, 
							logSearchTimeout);
					if (collectDataList == null || collectDataList.isEmpty()) {
						result.setSize(0);
						result.setCount(0);
						result.setTime(System.currentTimeMillis() - start);
						m_log.debug(String.format("queryCollectStringDataForMonitorLogcount() : end query. result=%s, query=%s", result, queryInfo));
						return result;
					}
					
					//重複排除のためにタグの値をSetで保持
					Set<String> tagValueSet = new HashSet<String>();
					for (Object[] collectData : collectDataList) {
						tagValueSet.add((String)collectData[0]);
					}
					
					//タグの値ごとに収集データをリストしMapで保持
					Map<String, List<Long>> collectDataMap = new HashMap<String, List<Long>>();
					for (String tagValue : tagValueSet) {
						List<Object[]> filterCollectDataList = collectDataList.stream()
								.filter(collectData -> collectData[0].equals(tagValue))
								.collect(Collectors.toList());
						
						List<Long> collectTimeList = new ArrayList<Long>();
						
						for (Object[] collectData : filterCollectDataList) {
							collectTimeList.add((Long)collectData[1]);
						}
						collectDataMap.put(tagValue, collectTimeList);
					}
					result.setCollectDataMap(collectDataMap);
					result.setCount(collectDataList.size());
				}
			}
			
			result.setTime(System.currentTimeMillis() - start);
			
			m_log.debug(String.format("queryCollectStringDataForMonitorLogcount() : end query. result=%s, query=%s", result.toResultString(), queryInfo));
		}
		return result;
	}

	/**
	 * ログ件数監視の場合はcollectを呼ばず、collectListで処理を行なう
	 */
	@Override
	public boolean collect(String facilityId) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 監視を実行します。（並列処理）
	 * <p>
	 * <ol>
	 * <li>監視情報を取得し、保持します（{@link #setMonitorInfo(String, String)}）。</li>
	 * <li>判定情報を取得し、判定情報マップに保持します（{@link #setJudgementInfo()}）。</li>
	 * <li>チェック条件情報を取得し、保持します（{@link #setCheckInfo()}）。</li>
	 * <li>ファシリティ毎に並列に監視を実行し、値を収集します。 （{@link #collect(String)}）。</li>
	 * <li>監視結果から、判定結果を取得します。 （{@link #getCheckResult(boolean)}）。</li>
	 * <li>監視結果から、重要度を取得します（{@link #getPriority(int)}）。</li>
	 * <li>監視結果を通知します（{@link #notify(boolean, String, int, Date)}）。</li>
	 * </ol>
	 *
	 * @return 実行に成功した場合、</code> true </code>
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 *
	 * @see #setMonitorInfo(String, String)
	 * @see #setJudgementInfo()
	 * @see #setCheckInfo()
	 * @see #collect(String)
	 * @see #getCheckResult(boolean)
	 * @see #getPriority(int)
	 * @see #notify(boolean, String, int, Date)
	 */
	@Override
	protected List<OutputBasicInfo> runMonitorInfo() throws FacilityNotFound, MonitorNotFound, InvalidRole, EntityExistsException, HinemosUnknown {

		List<OutputBasicInfo> ret = new ArrayList<>();
		m_now = HinemosTime.getDateInstance();

		m_priorityMap = new HashMap<Integer, ArrayList<String>>();
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_INFO),		new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_WARNING),	new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_CRITICAL),	new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_UNKNOWN),	new ArrayList<String>());
		List<Sample> sampleList = new ArrayList<Sample>();
		
		try
		{
			// 監視基本情報を設定
			boolean run = this.setMonitorInfo(m_monitorTypeId, m_monitorId);
			if(!run){
				// 処理終了
				return ret;
			}

			// 判定情報を設定
			setJudgementInfo();

			// チェック条件情報を設定
			setCheckInfo();

			// 対象監視設定のオブジェクト権限（Read）がない場合は実行されないこと
			try {
				com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(m_logCount.getTargetMonitorId(), m_monitor.getOwnerRoleId());
			} catch (InvalidRole | MonitorNotFound e) {
				throw new HinemosUnknown("It does not have access authority to target monitor info. : monitorId=" + m_logCount.getTargetMonitorId());
			}

			ArrayList<String> facilityList = null;
			ExecutorCompletionService<List<MonitorRunResultInfo>> ecs 
				= new ExecutorCompletionService<List<MonitorRunResultInfo>>(ParallelExecution.instance().getExecutorService());
			int taskCount = 0;

			if (!m_isMonitorJob) {
				// 監視ジョブ以外の場合
				// ファシリティIDの配下全ての一覧を取得
				// 有効/無効フラグがtrueとなっているファシリティIDを取得する
				facilityList = new RepositoryControllerBean().getExecTargetFacilityIdList(m_facilityId, m_monitor.getOwnerRoleId());
				if (facilityList.size() == 0) {
					return ret;
				}

				m_isNode = new RepositoryControllerBean().isNode(m_facilityId);

				// 監視対象となっているノードの変数を取得
				nodeInfo = new HashMap<String, NodeInfo>();
				for (String facilityId : facilityList) {
					try {
						synchronized (this) {
							nodeInfo.put(facilityId, new RepositoryControllerBean().getNode(facilityId));
						}
					} catch (FacilityNotFound e) {
						// 何もしない
					}
				}

				m_log.debug("monitor start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

				/**
				 * 監視の実行
				 */
				// ファシリティIDの数だけ、各監視処理を実行する
				Iterator<String> itr = facilityList.iterator();
				while(itr.hasNext()){
					String facilityId = itr.next();
					if(facilityId != null && !"".equals(facilityId)){

						// マルチスレッド実行用に、RunMonitorのインスタンスを新規作成する
						// インスタンスを新規作成するのは、共通クラス部分に監視結果を保持するため
						RunMonitorLogcount runMonitor = this.createMonitorInstance();

						// 監視実行に必要な情報を再度セットする
						runMonitor.m_monitorTypeId = this.m_monitorTypeId;
						runMonitor.m_monitorId = this.m_monitorId;
						runMonitor.m_now = this.m_now;
						runMonitor.m_priorityMap = this.m_priorityMap;
						runMonitor.setMonitorInfo(runMonitor.m_monitorTypeId, runMonitor.m_monitorId);
						runMonitor.setJudgementInfo();
						runMonitor.setCheckInfo();
						runMonitor.nodeInfo = this.nodeInfo;

						ecs.submit(new MonitorMultipleExecuteTask(runMonitor, facilityId));
						taskCount++;
						
						if (m_log.isDebugEnabled()) {
							m_log.debug("starting monitor result : monitorId = " + m_monitorId + ", facilityId = " + facilityId);
						}
					}
					else {
						facilityList.remove(facilityId);
					}
				}

			} else {
				// 監視ジョブの場合
				// ファシリティIDの配下全ての一覧を取得
				// 有効/無効フラグがtrueとなっているファシリティIDを取得する
				facilityList = new RepositoryControllerBean().getExecTargetFacilityIdList(m_facilityId, m_monitor.getOwnerRoleId());
				if (facilityList.size() != 1
						|| !facilityList.get(0).equals(m_facilityId) ) {
					return ret;
				}

				m_isNode = true;

				// 監視対象となっているノードの変数を取得
				nodeInfo = new HashMap<String, NodeInfo>();
				try {
					synchronized (this) {
						nodeInfo.put(m_facilityId, new RepositoryControllerBean().getNode(m_facilityId));
					}
				} catch (FacilityNotFound e) {
					// 何もしない
				}
				m_log.debug("monitor start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

				/**
				 * 監視の実行
				 */
				// マルチスレッド実行用に、RunMonitorのインスタンスを新規作成する
				// インスタンスを新規作成するのは、共通クラス部分に監視結果を保持するため
				RunMonitorLogcount runMonitor = this.createMonitorInstance();

				// 監視実行に必要な情報を再度セットする
				runMonitor.m_isMonitorJob = this.m_isMonitorJob;
				runMonitor.m_monitorTypeId = this.m_monitorTypeId;
				runMonitor.m_monitorId = this.m_monitorId;
				runMonitor.m_now = this.m_now;
				runMonitor.m_priorityMap = this.m_priorityMap;
				runMonitor.setMonitorInfo(runMonitor.m_monitorTypeId, runMonitor.m_monitorId);
				runMonitor.setJudgementInfo();
				runMonitor.setCheckInfo();
				runMonitor.nodeInfo = this.nodeInfo;
				runMonitor.m_prvData = this.m_prvData;

				ecs.submit(new MonitorMultipleExecuteTask(runMonitor, m_facilityId));
				taskCount++;

				if (m_log.isDebugEnabled()) {
					m_log.debug("starting monitor result : monitorId = " + m_monitorId + ", facilityId = " + m_facilityId);
				}
			}

			/**
			 * 監視結果の集計
			 */
			List<MonitorRunResultInfo> resultList = new ArrayList<>();	// 監視結果を格納

			m_log.debug("total start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

			// 収集値の入れ物を作成
			Sample sample = null;
			
			for (int i = 0; i < taskCount; i++) {
				Future<List<MonitorRunResultInfo>> future = ecs.take();
				resultList = future.get();	// 監視結果を取得

				if (resultList == null || resultList.size() <= 0) {
					continue;
				}
				if (!m_isMonitorJob) {
					for (MonitorRunResultInfo result : resultList) {
						String facilityId = result.getFacilityId();
						m_nodeDate = result.getNodeDate();
						
						if (m_log.isDebugEnabled()) {
							m_log.debug("finished monitor : monitorId = " + m_monitorId + ", facilityId = " + facilityId);
						}
					
						// 処理する場合
						if(result.getProcessType().booleanValue()){
							if (m_monitor.getMonitorFlg()) {
								// 監視結果を通知
								ret.add(createOutputBasicInfo(true, facilityId, result.getCheckResult(), new Date(m_nodeDate), result, m_monitor));
							}
							// 個々の収集値の登録
							if (m_monitor.getCollectorFlg().booleanValue()
									|| m_monitor.getPredictionFlg().booleanValue()
									|| m_monitor.getChangeFlg().booleanValue()) {

								// 将来予測監視、変化量監視の処理を行う
								CollectMonitorDataInfo collectMonitorDataInfo 
								= CollectMonitorManagerUtil.calculateChangePredict(
									this, 
									m_monitor, 
									facilityId, 
									result.getDisplayName(),
									m_monitor.getItemName(),
									result.getNodeDate(),
									result.getValue());

								// 将来予測もしくは変更点監視が有効な場合、通知を行う
								Double average = null;
								Double standardDeviation = null;
								if (collectMonitorDataInfo != null) {
									if (collectMonitorDataInfo.getChangeMonitorRunResultInfo() != null) {
										// 変化量監視の通知
										MonitorRunResultInfo collectResult = collectMonitorDataInfo.getChangeMonitorRunResultInfo();
										ret.add(createOutputBasicInfo(true, facilityId, collectResult.getCheckResult(), 
												new Date(collectResult.getNodeDate()),  
												collectResult, m_monitor));
									}
									if (collectMonitorDataInfo.getPredictionMonitorRunResultInfo() != null) {
										// 将来予測監視の通知
										MonitorRunResultInfo collectResult = collectMonitorDataInfo.getPredictionMonitorRunResultInfo();
										ret.add(createOutputBasicInfo(true, facilityId, collectResult.getCheckResult(), 
												new Date(collectResult.getNodeDate()),  
												collectResult, m_monitor));
									}
									average = collectMonitorDataInfo.getAverage();
									standardDeviation = collectMonitorDataInfo.getStandardDeviation();
								}

								if (m_monitor.getCollectorFlg().booleanValue()
										&& !((m_logCount.getTag() != null && !m_logCount.getTag().isEmpty()) 
												&& result.getDisplayName().isEmpty())) {
									sample = new Sample(new Date(result.getNodeDate()), m_monitor.getMonitorId());
									int errorType = -1;
									if(result.isCollectorResult()){
										errorType = CollectedDataErrorTypeConstant.NOT_ERROR;
									}else{
										errorType = CollectedDataErrorTypeConstant.UNKNOWN;
									}
									sample.set(facilityId, m_monitor.getItemName(), result.getValue(), 
											average, standardDeviation, errorType, result.getDisplayName());
									sampleList.add(sample);
								}
							}
						}
					}
				} else {
					m_monitorRunResultInfo = new MonitorRunResultInfo();
					m_monitorRunResultInfo.setPriority(resultList.get(0).getPriority());
					m_monitorRunResultInfo.setCheckResult(resultList.get(0).getCheckResult());
					m_monitorRunResultInfo.setNodeDate(m_nodeDate);
					m_monitorRunResultInfo.setMessageOrg(makeJobOrgMessage(resultList.get(0).getMessageOrg(), resultList.get(0).getMessage()));
					m_monitorRunResultInfo.setCurData(resultList.get(0).getCurData());
				}
			}

			// 収集値をまとめて登録
			if(!sampleList.isEmpty()){
				CollectDataUtil.put(sampleList);
			}
			
			m_log.debug("monitor end : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

			return ret;

		} catch (FacilityNotFound e) {
			throw e;
		} catch (InterruptedException e) {
			m_log.info("runMonitorInfo() monitorTypeId = " + m_monitorTypeId + ", monitorId  = " + m_monitorId + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new HinemosUnknown(e);
		} catch (ExecutionException e) {
			m_log.info("runMonitorInfo() monitorTypeId = " + m_monitorTypeId + ", monitorId  = " + m_monitorId + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new HinemosUnknown(e);
		}
	}

	/* (non-Javadoc)
	 * ログ件数監視情報を設定
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {

		// ログ件数監視情報を取得
		if (!m_isMonitorJob) {
			// 監視ジョブ以外の場合
			m_logCount = QueryUtil.getMonitorLogcountInfoPK(m_monitorId);
		} else {
			// 監視ジョブの場合
			m_logCount = QueryUtil.getMonitorLogcountInfoPK(m_monitor.getMonitorId());
		}
	}

	/* (非 Javadoc)
	 * ノード用メッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {
		if(m_message == null || "".equals(m_message)){
			return m_unKnownMessage;
		}
		return m_message;
	}

	/* (非 Javadoc)
	 * ノード用オリジナルメッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		if (m_monitor == null || m_monitor.getLogcountCheckInfo() == null) {
			return "";
		}
		String strAndOr = "";
		if (m_monitor.getLogcountCheckInfo().getIsAnd()) {
			strAndOr = "AND";
		} else {
			strAndOr = "OR";
		}
		String strCountMethod = "";
		String strTag = "";
		if (m_monitor.getLogcountCheckInfo().getTag() == null 
				|| m_monitor.getLogcountCheckInfo().getTag().isEmpty()) {
			strCountMethod = MessageConstant.ALL.getMessage();
		} else {
			strCountMethod = MessageConstant.TAG.getMessage();
			strTag = m_monitor.getLogcountCheckInfo().getTag();
		}
		String[] args = {
				m_monitor.getLogcountCheckInfo().getTargetMonitorId(),
				m_monitor.getLogcountCheckInfo().getKeyword(),
				strAndOr,
				strCountMethod,
				strTag};
		/** メッセージ出力 */
		return m_messageorg + "\n" + MessageConstant.MESSAGE_MONITOR_ORGMSG_LOGCOUNT.getMessage(args);
	}

	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		return orgMsg;
	}
}
