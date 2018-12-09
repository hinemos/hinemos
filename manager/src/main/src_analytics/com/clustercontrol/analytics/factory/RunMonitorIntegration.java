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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.analytics.model.IntegrationCheckInfo;
import com.clustercontrol.analytics.model.IntegrationConditionInfo;
import com.clustercontrol.analytics.util.AnalyticsUtil;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hub.bean.StringData;
import com.clustercontrol.hub.bean.StringQueryInfo;
import com.clustercontrol.hub.bean.StringQueryInfo.Operator;
import com.clustercontrol.hub.model.CollectDataTag;
import com.clustercontrol.hub.model.CollectStringData;
import com.clustercontrol.hub.model.CollectStringKeyInfo;
import com.clustercontrol.hub.bean.StringQueryResult;
import com.clustercontrol.hub.bean.Tag;
import com.clustercontrol.hub.session.HubControllerBean;
import com.clustercontrol.hub.session.HubControllerBean.Token;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.bean.TruthConstant;
import com.clustercontrol.monitor.run.factory.RunMonitorTruthValueType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.platform.QueryExecutor;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * 収集値統合監視 数値監視設定を実行するファクトリークラス<BR>
 *
 * @version 6.1.0
 */
public class RunMonitorIntegration extends RunMonitorTruthValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorIntegration.class );

	/** 収集値統合監視情報 */
	private IntegrationCheckInfo m_integration = null;

	/** メッセージ **/
	private String m_message = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/** 処理結果データ(メッセージ出力用) */
	private List<String[]> m_resultDataList = null;

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorIntegration() {
		super();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.MonitorExecuteTask
	 */
	@Override
	protected RunMonitorIntegration createMonitorInstance() {
		return new RunMonitorIntegration();
	}

	/**
	 * 収集するを取得
	 * 
	 * @param facilityId ファシリティID（処理では使用しない）
	 * @return 監視結果
	 */
	@Override
	public boolean collect(String facilityId) throws InvalidSetting, HinemosUnknown, HinemosDbTimeout {
		boolean rtn = true;
		m_value = false;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
		sdf.setTimeZone(HinemosTime.getTimeZone());

		// set Generation Date
		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}

		m_message = "";

		// 判定処理
		// 処理結果
		boolean tmpValue = true;
		// 収集間隔(監視間隔×2)
		long analysysRange =  m_monitor.getRunInterval().longValue() * 2 * 1000;
		// タイムアウト
		long timeout = m_integration.getTimeout().longValue() * 60 * 1000;
		// 収集日時(From)
		long startDate = m_nodeDate - analysysRange;
		// 収集日時(To)
		long endDate = m_nodeDate;
		// 「順序を考慮しない場合」の判定に使用
		List<Long> dateList = new ArrayList<>();
		//
		long minDate = 0L;
		// 結果格納（Time、Value）
		m_resultDataList = new ArrayList<>();

		for (int i = 0; i < m_integration.getConditionList().size(); i++) {
			IntegrationConditionInfo condition = m_integration.getConditionList().get(i);
			String targetFacilityId = null;
			String tmpDataValue = null;
			Long tmpDate = null;

			// 対象監視設定のオブジェクト権限（Read）がない場合は実行されないこと
			try {
				com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(condition.getTargetMonitorId(), m_monitor.getOwnerRoleId());
			} catch (InvalidRole | MonitorNotFound e) {
				throw new HinemosUnknown("It does not have access authority to target monitor info. : monitorId=" + condition.getTargetMonitorId());
			}

			if (condition.getMonitorNode()) {
				// 監視ノード対象
				targetFacilityId = facilityId;
			} else {
				// 設定ノード対象
				targetFacilityId = condition.getTargetFacilityId();
			}
			if (condition.getTargetMonitorType() == MonitorTypeConstant.TYPE_NUMERIC) {
				List<CollectData> collectDataList = null;
				try {
					collectDataList = new SelectMonitorIntegration()
							.getCollectDataForNumeric(condition, targetFacilityId, startDate, endDate);
				} catch (HinemosUnknown e) {
					// エラーの場合
					m_log.info("collect() : "
							+ "monitorTypeId = " + m_monitorTypeId 
							+ ", monitorId  = " + m_monitorId
							+ ", facilityId=" + facilityId
							+ ", targetMonitorType=" + condition.getTargetMonitorType()
							+ ", targetMonitorId=" + condition.getTargetMonitorId()
							+ ", targetDisplayName=" + condition.getTargetDisplayName()
							+ ", targetItemName=" + condition.getTargetItemName()
							+ ", targetFacilityId=" + targetFacilityId
							+ " : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				}
				if (collectDataList != null && collectDataList.size() > 0) {
					// データが存在する場合
					tmpDataValue = collectDataList.get(0).getValue().toString();
					tmpDate = collectDataList.get(0).getTime();
				}					
			} else if (condition.getTargetMonitorType() == MonitorTypeConstant.TYPE_STRING) {
				// 検索条件作成
				Operator ope = null;
				if (condition.getIsAnd() == null || condition.getIsAnd()) {
					ope = Operator.AND;
				} else {
					ope = Operator.OR;
				}
				StringQueryInfo stringQueryInfo = AnalyticsUtil.makeQuery(startDate, endDate, targetFacilityId,
					condition.getTargetMonitorId(), condition.getComparisonValue(), null, ope);
				StringQueryResult stringQueryResult = null;
				try {
					int logSearchTimeout = HinemosPropertyCommon.monitor_integration_search_timeout.getIntegerValue();
					stringQueryResult = queryCollectStringDataForMonitorIntegration(stringQueryInfo, logSearchTimeout);
				} catch (HinemosDbTimeout | InvalidSetting e) {
					// エラーの場合
					m_log.info("collect() : "
							+ "monitorTypeId = " + m_monitorTypeId 
							+ ", monitorId  = " + m_monitorId
							+ ", facilityId=" + facilityId
							+ ", targetMonitorType=" + condition.getTargetMonitorType()
							+ ", targetMonitorId=" + condition.getTargetMonitorId()
							+ ", targetFacilityId=" + targetFacilityId
							+ " : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				if (stringQueryResult != null && stringQueryResult.getCount() > 0) {
					// データが存在する場合
					if (m_log.isDebugEnabled()) {
						m_log.debug("collect() get collect data : "
								+ " order=" + i
								+ ", monitorId=" + m_monitor.getMonitorId()
								+ ", facilityId=" + facilityId
								+ ", targetMonitorType=" + condition.getTargetMonitorType()
								+ ", targetMonitorId=" + condition.getTargetMonitorId()
								+ ", targetFacilityId=" + targetFacilityId
								+ ", nodeDate=" + sdf.format(new Date(m_nodeDate)));
						for (int j = 0; j < stringQueryResult.getDataList().size(); j++) {
							m_log.debug("collect() get collect data : "
									+ " list_order=" + i
									+ " time=" + sdf.format(new Date(stringQueryResult.getDataList().get(i).getTime())) 
									+ ", value=" + stringQueryResult.getDataList().get(i).getData());
						}
					}
					tmpDataValue = stringQueryResult.getDataList().get(0).getData();
					tmpDate = stringQueryResult.getDataList().get(0).getTime();
				}
			}
			if (tmpDate == null) {
				// データが取得できない場合は異常
				tmpValue = false;
			} else {
				//　データが取得できた場合は値を設定する
				m_resultDataList.add(new String[]{sdf.format(new Date(tmpDate)), tmpDataValue});
			}
			if (!m_integration.getNotOrder()) {
				// 順序を考慮する場合
				if (tmpDate == null) {
					if (i == 0) {
						// 初回データが存在しない場合は通知しない
						rtn = false;
					}
					break;
				}
				// 初回取得したデータを基点とする
				startDate = tmpDate;
				if (i == 0) {
					minDate = startDate;
					if ((minDate + timeout) < endDate) {
						endDate = minDate + timeout;
					}
				}
			} else {
				// 順序を考慮しない場合
				if (tmpDate == null) {
					//データが取得できない場合は空の値を設定する
					m_resultDataList.add(new String[]{"", ""});
				} else {
					// データが1件でも取得できた場合は正常処理とする
					if (minDate == 0L) {
						//　初めてデータが取得できた場合
						minDate = tmpDate.longValue();
					} else if (minDate > tmpDate) {
						// 古い日時の収集データの場合
						minDate = tmpDate;
					}
				}
				dateList.add(tmpDate);					
			}
		}
		if (rtn && !tmpValue) { 
			if((m_nodeDate < minDate + timeout)) {
				// 順序を考慮かつ異常の場合、タイムアウトが現在日時より後なら通知しない
				// 順序を考慮しないかつ異常の場合、タイムアウトが現在日時より後ならば通知しない
				rtn = false;
			}
			if (m_integration.getNotOrder() && dateList != null) {
				// 順序を考慮しない場合、データが全て取得できない場合は通知しない
				boolean isEmpty = true;
				for (Long date : dateList) {
					if (date != null) {
						isEmpty = false;
						break;
					}
				}
				if (isEmpty) {
					rtn = false;
				}
			}
		}

		if (rtn 
			&& dateList != null 
			&& m_integration.getNotOrder()) {
			// 順序を考慮しない場合にデータの有効判定
			for (int i = 0; i < dateList.size(); i++) {
				if (dateList.get(i) != null && dateList.get(i) > minDate + timeout) {
					m_resultDataList.set(i, new String[]{"", ""});
					tmpValue = false;
				}
			}
		}
		
		// 結果を設定
		m_value = tmpValue;
		if (m_log.isDebugEnabled()) {
			m_log.debug("collect() : "
					+ "m_nodeDate = " + sdf.format(new Date(m_nodeDate)) 
					+ ", monitorTypeId = " + m_monitorTypeId 
					+ ", monitorId  = " + m_monitorId
					+ ", facilityId=" + facilityId
					+ ", m_value=" + m_value
					+ ", rtn=" + rtn);
		}
		return rtn;
	}


	/* (non-Javadoc)
	 * 収集値統合監視情報を設定
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {
		// 収集値統合監視情報を取得
		String monitorId = "";
		MonitorInfo monitorInfo = null;
		try {
			if (!m_isMonitorJob) {
				// 監視ジョブ以外の場合
				monitorInfo = new SelectMonitorIntegration().getMonitor(
						HinemosModuleConstant.MONITOR_INTEGRATION, m_monitorId);
				m_integration = monitorInfo.getIntegrationCheckInfo();
				monitorId = m_monitorId;
			} else {
				// 監視ジョブの場合
				monitorInfo = new SelectMonitorIntegration().getMonitor(
						HinemosModuleConstant.MONITOR_INTEGRATION, m_monitor.getMonitorId());
				m_integration = monitorInfo.getIntegrationCheckInfo();
				monitorId = m_monitor.getMonitorId();
			}
		} catch (HinemosUnknown | InvalidRole e) {
			String message = "MonitorIntegrationInfoEntity.getConditionList"
					+ ", monitorId = " + monitorId;
			MonitorNotFound ex = new MonitorNotFound(message);
			m_log.info("getMonitorIntegrationInfoPK() : "
					+ message
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw ex;
		}
	
		if (m_integration.getConditionList() == null 
				|| m_integration.getConditionList().size() == 0) {
			MonitorNotFound e = new MonitorNotFound("MonitorIntegrationInfoEntity.getConditionList"
					+ ", monitorId = " + monitorId);
			m_log.info("getMonitorIntegrationInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * 判定結果を返します。
	 * <p>
	 * 監視取得値の真偽値定数を返します。
	 * 
	 * @see com.clustercontrol.monitor.run.bean.TruthConstant
	 */
	@Override
	public int getCheckResult(boolean ret) {
		// processType=falseの場合は、通知をしない
		int result = -2;

		// 値取得の成功時
		if(ret){
			if(m_value){
				// 真
				result = TruthConstant.TYPE_TRUE;
			}
			else{
				// 偽
				result = TruthConstant.TYPE_FALSE;
			}
		}
		return result;
	}

	/* (非 Javadoc)
	 * ノード用メッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {
		if (m_value) {
			m_message = m_integration.getMessageOk();
		} else {
			m_message = m_integration.getMessageNg();
		}
		return m_message;
	}

	/* (非 Javadoc)
	 * ノード用オリジナルメッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		if (m_monitor == null 
				|| m_monitor.getIntegrationCheckInfo() == null
				|| m_monitor.getIntegrationCheckInfo().getConditionList() == null) {
			return "";
		}
		Integer analysysRange = m_monitor.getRunInterval() * 2 / 60;
		StringBuilder sb = new StringBuilder();
		sb.append(MessageConstant.MESSAGE_MONITOR_ORGMSG_INTEGRATION.getMessage(
				new String[]{
				analysysRange.toString(),
				m_integration.getTimeout().toString(),
				m_monitor.getFacilityId(),
				m_integration.getNotOrder().toString()}));
		sb.append("\n\n");
		for (int i = 0; i < m_integration.getConditionList().size(); i++) {
			IntegrationConditionInfo condition = m_integration.getConditionList().get(i);
			String nodeName = "";
			if (condition.getMonitorNode()) {
				nodeName = MessageConstant.MONITOR_INTEGRATION_MONITOR_SCOPE.getMessage();
			} else {
				nodeName = condition.getTargetFacilityId();
			}
			String judgment = condition.getComparisonMethod() + " " + condition.getComparisonValue();
			String collectTime = "";
			String collectValue = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_ANALYTICS.getMessage();
			if (m_resultDataList.size() > i) {
				collectTime = m_resultDataList.get(i)[0];
				if (m_resultDataList.get(i)[1] != null 
						&& !m_resultDataList.get(i)[1].isEmpty()) {
					collectValue = m_resultDataList.get(i)[1];
				}
			}
			if (condition.getTargetMonitorType() == MonitorTypeConstant.TYPE_NUMERIC) {
				sb.append(MessageConstant.MESSAGE_MONITOR_ORGMSG_INTEGRATION_NUMERIC.getMessage(
						new String[]{
							nodeName,
							AnalyticsUtil.getMsgItemName(
									condition.getTargetItemName(), 
									condition.getTargetDisplayName(),
									condition.getTargetMonitorId()),
							judgment,
							collectTime,
							collectValue}));
			} else if (condition.getTargetMonitorType() == MonitorTypeConstant.TYPE_STRING) {
				String andor = "";
				if (condition.getIsAnd()) {
					andor = "AND";
				} else {
					andor = "OR";
				}
				sb.append(MessageConstant.MESSAGE_MONITOR_ORGMSG_INTEGRATION_STRING.getMessage(
						new String[]{
							nodeName,
							condition.getTargetMonitorId(),
							judgment,
							andor,
							collectTime,
							collectValue}));
			}
			sb.append("\n\n");
		}
		/** メッセージ出力 */
		m_messageOrg = sb.toString();
		return m_messageOrg;
	}

	/* (非 Javadoc)
	 * ノード用オリジナルメッセージを取得(監視ジョブ)
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#makeJobOrgMessage(java.lang.String, java.lang.String)
	 */
	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		return m_messageOrg;
	}
	

	/**
	 * 文字列収集情報を検索する。
	 * 収集値統合監視で使用する。
	 * 
	 * @param format
	 * @param logSearchTimeout 検索タイムアウト値
	 * @param isDesc true:取得順desc、 false:取得順asc
	 * @return
	 * @throws HinemosDbTimeout
	 * @throws InvalidSetting
	 */
	private StringQueryResult queryCollectStringDataForMonitorIntegration(StringQueryInfo queryInfo, Integer logSearchTimeout)
			throws HinemosDbTimeout, InvalidSetting  {
		long start = System.currentTimeMillis();
		
		m_log.debug(String.format("queryCollectStringDataForMonitorIntegration() : start query. query=%s", queryInfo));

		// 入力値判定
		if (queryInfo.getFrom() == null 
				|| queryInfo.getTo() == null
				|| queryInfo.getFacilityId() == null
				|| queryInfo.getMonitorId() == null) {
			m_log.warn("queryCollectStringDataForMonitorIntegration() : "
					+ "parameter is null query=" + queryInfo);
			throw new InvalidSetting("parameter is null query=" + queryInfo);
		}

		if (queryInfo.getFrom() > queryInfo.getTo()){
			m_log.warn("queryCollectStringDataForMonitorIntegration() : "
					+ MessageConstant.MESSAGE_HUB_SEARCH_DATE_INVALID.getMessage());
			throw new InvalidSetting(MessageConstant.MESSAGE_HUB_SEARCH_DATE_INVALID.getMessage());
		}

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
			m_log.debug(String.format("queryCollectStringDataForMonitorIntegration() : target data key. keys=%s, query=%s", keys.values(), queryInfo));
			
			if (keys.isEmpty()) {
				// アクセスできるキーがないので終了。
				StringQueryResult result = new StringQueryResult();
				result.setOffset(queryInfo.getOffset());
				result.setSize(0);
				result.setCount((queryInfo.isNeedCount() != null && queryInfo.isNeedCount()) ? 0: null);
				result.setTime(System.currentTimeMillis() - start);
				m_log.debug(String.format("queryCollectStringDataForMonitorIntegration() : end query. result=%s, query=%s", result, queryInfo));
				return result;
			}

			// データのクエリ
			StringBuilder dataQueryStr = new StringBuilder("FROM CollectStringData d"); 
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
								m_log.warn("queryCollectStringDataForMonitorIntegration() : "
										+ MessageConstant.MESSAGE_HUB_SEARCH_KEYWORD_INVALID.getMessage());
								throw new InvalidSetting(MessageConstant.MESSAGE_HUB_SEARCH_KEYWORD_INVALID.getMessage());
							}
							conditionValueBuffer
								.append(String.format("EXISTS(SELECT t FROM d.tagList t WHERE t.value <> '%s')", token.word.substring(1)));
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
			
			StringQueryResult result = new StringQueryResult();
			result.setOffset(queryInfo.getOffset());
			
			// データの最大数を取得する。
			if (queryInfo.isNeedCount() != null && queryInfo.isNeedCount()) {
				String queryStr = "SELECT COUNT(DISTINCT d) " + dataQueryStr.toString();
				m_log.debug(String.format("queryCollectStringDataForMonitorIntegration() : query count. queryStr=%s, query=%s", queryStr, queryInfo));
				
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
					m_log.debug(String.format("queryCollectStringDataForMonitorIntegration() : end query. result=%s, query=%s", result, queryInfo));
					return result;
				}

				result.setCount(Integer.valueOf(count.toString()));
			}
			
			String queryStr = "SELECT DISTINCT d " + dataQueryStr.toString() + " ORDER BY d.time";
			m_log.debug(String.format("queryCollectStringDataForMonitorIntegration() : query data. queryStr=%s, query=%s", queryStr, queryInfo));
			
			parameters = new HashMap<String, Object>();
			parameters.put("collectIds", keys.keySet());
			parameters.put("from", queryInfo.getFrom());
			parameters.put("to", queryInfo.getTo());
			List<CollectStringData> dataResults = QueryExecutor.getListByJpqlWithTimeout(
					queryStr.toString(), 
					CollectStringData.class, 
					parameters,
					logSearchTimeout,
					queryInfo.getOffset(),
					queryInfo.getSize());

			if (dataResults == null || dataResults.isEmpty()) {
				result.setSize(0);
				result.setTime(System.currentTimeMillis() - start);
				m_log.debug(String.format("queryCollectStringDataForMonitorIntegration() : end query. result=%s, query=%s", result, queryInfo));
				return result;
			}
			
			result.setOffset(queryInfo.getOffset());
			result.setSize(dataResults.size());
			
			List<StringData> stringDataList = new ArrayList<StringData>();
			for (CollectStringData r: dataResults) {
				CollectStringKeyInfo key = keys.get(r.getCollectId());
				StringData data = new StringData();
				data.setFacilityId(key.getFacilityId());
				data.setMonitorId(key.getMonitorId());
				data.setTime(r.getTime());
				data.setData(r.getValue());
				List<Tag> tagList = new ArrayList<Tag>();
				Tag tag = null;
				for (CollectDataTag t: r.getTagList()) {
					tag = new Tag();
					tag.setKey(t.getKey());
					tag.setValue(t.getValue());
					tagList.add(tag);
				}
				data.setTagList(tagList);
				data.setPrimaryKey(r.getId());
				stringDataList.add(data);
			}
			result.setDataList(stringDataList);
			
			result.setTime(System.currentTimeMillis() - start);
			
			m_log.debug(String.format("queryCollectStringDataForMonitorIntegration() : end query. result=%s, query=%s", result.toResultString(), queryInfo));
			
			return result;
		}
	}
}
