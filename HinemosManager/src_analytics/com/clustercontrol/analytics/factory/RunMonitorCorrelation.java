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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.analytics.model.CorrelationCheckInfo;
import com.clustercontrol.analytics.util.AnalyticsUtil;
import com.clustercontrol.analytics.util.OperatorAnalyticsUtil;
import com.clustercontrol.analytics.util.OperatorCommonUtil;
import com.clustercontrol.analytics.util.QueryUtil;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosArithmeticException;
import com.clustercontrol.fault.HinemosIllegalArgumentException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil.CollectMonitorDataInfo;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCache;
import com.clustercontrol.monitor.run.util.MonitorMultipleExecuteTask;
import com.clustercontrol.monitor.run.util.ParallelExecution;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCache.MonitorCollectData;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCache.MonitorCollectDataPK;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * 相関係数監視 数値監視設定を実行するファクトリークラス<BR>
 *
 * @version 6.1.0
 */
public class RunMonitorCorrelation extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorCorrelation.class );

	/** 相関係数監視情報 */
	private CorrelationCheckInfo m_correlation = null;

	/** ファシリティID(参照収集値) */
	private List<String> m_referFacilityIdList = null;

	/** 不明メッセージ */
	private String m_unKnownMessage = null;

	/** メッセージ **/
	private String m_message = null;

	/** 収集データリスト文字列 */
	private String m_collectDataListMessage = null;

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorCorrelation() {
		super();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.MonitorExecuteTask
	 */
	@Override
	protected RunMonitorCorrelation createMonitorInstance() {
		return new RunMonitorCorrelation();
	}

	/**
	 * 収集するを取得
	 * 
	 * @param facilityId ファシリティID（処理では使用しない）
	 * @return 監視結果のリスト
	 */
	@Override
	public List<MonitorRunResultInfo> collectMultiple(String facilityId) {

		List<MonitorRunResultInfo> list = new ArrayList<>();
		// set Generation Date
		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}

		m_message = "";
		// 対象監視設定情報の取得
		MonitorCollectDataPK targetPK 
			= new MonitorCollectDataPK(m_correlation.getTargetMonitorId(), facilityId, 
					m_correlation.getTargetDisplayName(), m_correlation.getTargetItemName());
		for (String referFacilityId : m_referFacilityIdList) {
			// 参照監視設定情報の取得
			MonitorCollectDataPK referPK 
			= new MonitorCollectDataPK(m_correlation.getReferMonitorId(), referFacilityId, 
					m_correlation.getReferDisplayName(), m_correlation.getReferItemName());
			TreeMap<Long, Double[]> dataMap = getCollectDataAverageList(
					targetPK, referPK, m_correlation.getAnalysysRange().doubleValue(), m_nodeDate, m_monitor.getRunInterval());

			// オリジナルメッセージに設定する値リストを作成する 
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
			sdf.setTimeZone(HinemosTime.getTimeZone());
			StringBuilder sbMonitorCollectDataList = new StringBuilder();
			if (dataMap != null) {
				for (Map.Entry<Long, Double[]> entry : dataMap.entrySet()) {
					String strTargetValue = "-";
					String strReferValue = "-";
					if (entry.getValue()[0] != null && !entry.getValue()[0].isNaN()) {
						strTargetValue = entry.getValue()[0].toString();
					}
					if (entry.getValue()[1] != null && !entry.getValue()[1].isNaN()) {
						strReferValue = entry.getValue()[1].toString();
					}
					sbMonitorCollectDataList.append(String.format("(%s, %s, %s)%n", 
					sdf.format(new Date(entry.getKey())), strTargetValue, strReferValue));
				}
			}
			m_collectDataListMessage = sbMonitorCollectDataList.toString();

			if (m_log.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				sb.append("\n");
				sb.append("dataList = " + sbMonitorCollectDataList.toString());
				sb.toString();
				m_log.info("collectMultiple(): dataOutputput"
					+ " monitorId=" + m_monitor.getMonitorId()
					+ ", facilityId=" + facilityId
					+ ", displayName=" + referFacilityId
					+ ", itemName=" + m_monitor.getItemName()
					+ ", targetDate=" + new Date(m_nodeDate)
					+ "\n" + m_collectDataListMessage
					+ "\n" + sb.toString());
			}
			// 解析処理
			Double tmpValue = null;
			// 最小データ数
			Long dataCount = HinemosPropertyCommon.monitor_correlation_lower_limit.getNumericValue();
			try {
				tmpValue = OperatorAnalyticsUtil.getCorrelationCoefficient(dataMap, dataCount.intValue());
			} catch (HinemosArithmeticException e) {
				m_log.warn("collectMultiple():"
						+ " monitorId=" + m_monitor.getMonitorId()
						+ ", facilityId=" + facilityId
						+ ", displayName=" + referFacilityId
						+ ", itemName=" + m_monitor.getItemName()
						+ ", targetDate=" + new Date(m_nodeDate)
						+ "\n" + e.getMessage());
			} catch (HinemosIllegalArgumentException e) {
				m_log.info("collectMultiple():"
						+ " monitorId=" + m_monitor.getMonitorId()
						+ ", facilityId=" + facilityId
						+ ", displayName=" + referFacilityId
						+ ", itemName=" + m_monitor.getItemName()
						+ ", targetDate=" + new Date(m_nodeDate)
						+ "\n" + e.getMessage());
				// データが不足している場合は処理対象外
				continue;
			}
			boolean ret = false;
			MonitorRunResultInfo info = new MonitorRunResultInfo();
			if (tmpValue != null && !tmpValue.isNaN()) {
				// 正常にデータが取得された場合
				ret = true;
				m_value = tmpValue;
				m_curData = tmpValue;
				info.setValue(getValue());
				info.setCurData(getCurData());
			} else {
				// データが取得されない場合
				info.setValue(null);
				info.setCurData(null);
			}
			String valueStr = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_ANALYTICS.getMessage();
			if (m_value != null && !m_value.isNaN()) {
				valueStr = m_value.toString();
			}
			m_message = String.format("%s : %s", m_monitor.getItemName(), valueStr);
			Integer checkResult = getCheckResult(ret);
			info.setFacilityId(facilityId);
			info.setMonitorFlg(ret);
			info.setCollectorResult(ret);
			info.setCheckResult(checkResult);
			info.setMessage(getMessage(checkResult));
			info.setMessageOrg(getMessageOrg(checkResult));
			if (checkResult == -2) {
				info.setPriority(PriorityConstant.TYPE_NONE);
				info.setProcessType(false);
			} else {
				info.setPriority(getPriority(checkResult));
				info.setProcessType(true);
			}
			info.setNodeDate(m_nodeDate);
			info.setItemName(m_monitor.getItemName());
			info.setDisplayName(referFacilityId);
			info.setCollectorFlg(m_monitor.getCollectorFlg());
			info.setNotifyGroupId(getNotifyGroupId());
			info.setApplication(m_monitor.getApplication());
			list.add(info);
		}
		return list;
	}

	/**
	 * 解析対象データを取得する
	 * 解析対象データは、を[収集期間]の間の[間隔]ごとの平均値を返す
	 * 
	 * @param targetPk 収集データキー
	 * @param referPk 収集データキー（参照側）
	 * @param analysysRange 収集期間
	 * @param targetDate 取得日時
	 * @param runInterval 間隔
	 * @return 解析対象データリスト
	 */
	private TreeMap<Long, Double[]> getCollectDataAverageList(
			MonitorCollectDataPK targetPk,
			MonitorCollectDataPK referPk,
			Double analysysRange,
			Long targetDate,
			Integer runInterval) {
		TreeMap<Long, Double[]> map = new TreeMap<>(new Comparator<Long>() {
			public int compare(Long m, Long n) {
				return ((Long)m).compareTo(n) * -1;
			}
		});

		// 対象収集値表示名
		// キャッシュを更新する(キャッシュ更新可否はupdate()メソッドにて判定)
		MonitorCollectDataCache.update(
				targetPk.getMonitorId(), 
				targetPk.getFacilityId(), 
				targetPk.getDisplayName(), 
				targetPk.getItemName(), 
				targetDate);
		List<MonitorCollectData> targetMonitorCollectDataList = MonitorCollectDataCache.getMonitorCollectDataList(
				targetPk.getMonitorId(), targetPk.getFacilityId(), targetPk.getDisplayName(), targetPk.getItemName(), targetDate, analysysRange);
		int targetIdx = 0;

		// 参照収集値表示名
		// キャッシュを更新する(キャッシュ更新可否はupdate()メソッドにて判定)
		MonitorCollectDataCache.update(
				referPk.getMonitorId(), 
				referPk.getFacilityId(), 
				referPk.getDisplayName(), 
				referPk.getItemName(), 
				targetDate);
		List<MonitorCollectData> referMonitorCollectDataList = MonitorCollectDataCache.getMonitorCollectDataList(
				referPk.getMonitorId(), referPk.getFacilityId(), referPk.getDisplayName(), referPk.getItemName(), targetDate, analysysRange);
		int referIdx = 0;

		for (long toTime = targetDate; toTime >= targetDate - analysysRange * 60D * 1000D; toTime -= (runInterval * 1000)) {
			long fromTime = toTime - runInterval * 1000;
			if (targetMonitorCollectDataList.size() < targetIdx + 1
					&& referMonitorCollectDataList.size() < referIdx + 1) {
				break;
			}
			if ((targetMonitorCollectDataList.size() < targetIdx + 1 
					|| targetMonitorCollectDataList.get(targetIdx).getTime().longValue() <= fromTime)
					&& (referMonitorCollectDataList.size() < referIdx + 1 
					|| referMonitorCollectDataList.get(referIdx).getTime().longValue() <= fromTime)) {
				// 対象収集値表示名、もしくは参照収集値表示名が対象外
				continue;
			}
			Double targetAverage = null;
			Double referAverage = null;
			List<Double> targetValueList = new ArrayList<>();
			List<Double> referValueList = new ArrayList<>();
			if (targetMonitorCollectDataList.size() > targetIdx
					&& targetMonitorCollectDataList.get(targetIdx).getTime().longValue() > fromTime) {
				// 対象収集値表示名
				// 対象範囲（fromTime < 対象データの日時 <= toTime）
				while (targetMonitorCollectDataList.size() > targetIdx
						&& targetMonitorCollectDataList.get(targetIdx).getTime().longValue() > fromTime
						&& targetMonitorCollectDataList.get(targetIdx).getTime().longValue() <= toTime) {
					// 時間間隔範囲内の場合はListに設定
					if (targetMonitorCollectDataList.get(targetIdx).getValue() != null
							&& !targetMonitorCollectDataList.get(targetIdx).getValue().isNaN()) {
						targetValueList.add(targetMonitorCollectDataList.get(targetIdx).getValue());
					}
					targetIdx++;
				}
				try {
					targetAverage = OperatorCommonUtil.getAverage(targetValueList);
					m_log.debug("getCollectDataList(): average target monitorId=" + targetPk.getMonitorId() + ", facilityId=" + targetPk.getFacilityId() + ", displayName=" + targetPk.getDisplayName() 
					+ ", itemName=" + targetPk.getItemName() + ", targetDate=" + new Date(targetDate) + ", from=" + new Date(fromTime) + ", to=" + new Date(toTime) + ", list=" + Arrays.toString(targetValueList.toArray()) 
					+ ", average=" + targetAverage);
				} catch (HinemosArithmeticException e) {
					m_log.warn("getCollectDataList(): monitorId=" + targetPk.getMonitorId() + ", facilityId=" + targetPk.getFacilityId() + ", displayName=" + targetPk.getDisplayName() 
						+ ", itemName=" + targetPk.getItemName() + ", targetDate=" + new Date(targetDate) + "\n" + e.getMessage());
				} catch (HinemosIllegalArgumentException e) {
					m_log.debug("getCollectDataList(): monitorId=" + targetPk.getMonitorId() + ", facilityId=" + targetPk.getFacilityId() + ", displayName=" + targetPk.getDisplayName()
						+ ", itemName=" + targetPk.getItemName() + ", targetDate=" + new Date(targetDate) + "\n" + e.getMessage());
				}
			}
			if (referMonitorCollectDataList.size() > referIdx
					&& referMonitorCollectDataList.get(referIdx).getTime().longValue() > fromTime) {
				// 参照収集値表示名
				// 対象範囲（fromTime < 対象データの日時 <= toTime）
				while (referMonitorCollectDataList.size() > referIdx
						&& referMonitorCollectDataList.get(referIdx).getTime().longValue() > fromTime
						&& referMonitorCollectDataList.get(referIdx).getTime().longValue() <= toTime) {
					// 時間間隔範囲内の場合はListに設定
					if (referMonitorCollectDataList.get(referIdx).getValue() != null
							&& !referMonitorCollectDataList.get(referIdx).getValue().isNaN()) {
						referValueList.add(referMonitorCollectDataList.get(referIdx).getValue());
					}
					referIdx++;
				}
				try {
					referAverage = OperatorCommonUtil.getAverage(referValueList);
					m_log.debug("getCollectDataList(): average target monitorId=" + referPk.getMonitorId() + ", facilityId=" + referPk.getFacilityId() + ", displayName=" + referPk.getDisplayName() 
					+ ", itemName=" + referPk.getItemName() + ", targetDate=" + new Date(targetDate) + ", from=" + new Date(fromTime) + ", to=" + new Date(toTime) + ", list=" + Arrays.toString(referValueList.toArray()) 
					+ ", average=" + referAverage);
				} catch (HinemosArithmeticException e) {
					m_log.warn("getCollectDataList(): monitorId=" + referPk.getMonitorId() + ", facilityId=" + referPk.getFacilityId() + ", displayName=" + referPk.getDisplayName() 
						+ ", itemName=" + referPk.getItemName() + ", targetDate=" + new Date(targetDate) + "\n" + e.getMessage());
				} catch (HinemosIllegalArgumentException e) {
					m_log.debug("getCollectDataList(): monitorId=" + referPk.getMonitorId() + ", facilityId=" + referPk.getFacilityId() + ", displayName=" + referPk.getDisplayName()
						+ ", itemName=" + referPk.getItemName() + ", targetDate=" + new Date(targetDate) + "\n" + e.getMessage());
				}
			}
			if (targetAverage != null || referAverage != null) {
				map.put(toTime, new Double[]{targetAverage, referAverage});
			}
		}

		return map;
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
				com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(m_correlation.getTargetMonitorId(), m_monitor.getOwnerRoleId());
			} catch (InvalidRole | MonitorNotFound e) {
				throw new HinemosUnknown("It does not have access authority to target monitor info. : monitorId=" + m_correlation.getTargetMonitorId());
			}
			try {
				com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(m_correlation.getReferMonitorId(), m_monitor.getOwnerRoleId());
			} catch (InvalidRole | MonitorNotFound e) {
				throw new HinemosUnknown("It does not have access authority to target monitor info. : monitorId=" + m_correlation.getReferMonitorId());
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
						RunMonitorCorrelation runMonitor = this.createMonitorInstance();

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
				RunMonitorCorrelation runMonitor = this.createMonitorInstance();

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
			Date sampleTime = HinemosTime.getDateInstance();
			
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
							if (m_monitor.getCollectorFlg()
									|| m_monitor.getPredictionFlg()
									|| m_monitor.getChangeFlg()) {

								// 将来予測監視、変化量監視の処理を行う
								CollectMonitorDataInfo collectMonitorDataInfo 
								= CollectMonitorManagerUtil.calculateChangePredict(
									this, 
									m_monitor, 
									facilityId, 
									result.getDisplayName(),
									m_monitor.getItemName(),
									sampleTime.getTime(),
									result.getValue());

								// 将来予測もしくは変更点監視が有効な場合、通知を行う
								Double average = null;
								Double standardDeviation = null;
								if (collectMonitorDataInfo != null) {
									if (collectMonitorDataInfo.getChangeMonitorRunResultInfo() != null) {
										// 変化量監視の通知
										MonitorRunResultInfo collectResult = collectMonitorDataInfo.getChangeMonitorRunResultInfo();
										ret.add(createOutputBasicInfo(true, facilityId, collectResult.getCheckResult(), 
												new Date(collectResult.getNodeDate()), collectResult, m_monitor));
									}
									if (collectMonitorDataInfo.getPredictionMonitorRunResultInfo() != null) {
										// 将来予測監視の通知
										MonitorRunResultInfo collectResult = collectMonitorDataInfo.getPredictionMonitorRunResultInfo();
										ret.add(createOutputBasicInfo(true, facilityId, collectResult.getCheckResult(), 
												new Date(collectResult.getNodeDate()), collectResult, m_monitor));
									}
									average = collectMonitorDataInfo.getAverage();
									standardDeviation = collectMonitorDataInfo.getStandardDeviation();
								}

								if (m_monitor.getCollectorFlg().booleanValue()) {
									sample = new Sample(sampleTime, m_monitor.getMonitorId());
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
	 * 相関係数監視情報を設定
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {

		// 相関係数監視情報を取得
		if (!m_isMonitorJob) {
			// 監視ジョブ以外の場合
			m_correlation = QueryUtil.getMonitorCorrelationInfoPK(m_monitorId);
		} else {
			// 監視ジョブの場合
			m_correlation = QueryUtil.getMonitorCorrelationInfoPK(m_monitor.getMonitorId());
		}
		try {
			m_referFacilityIdList = new RepositoryControllerBean().getExecTargetFacilityIdList(
					m_correlation.getReferFacilityId(), m_monitor.getOwnerRoleId());
		} catch (HinemosUnknown e) {
			// 何もしない
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
		if (m_monitor == null || m_monitor.getCorrelationCheckInfo() == null) {
			return "";
		}
		String msgTargetItemName = AnalyticsUtil.getMsgItemName(
				m_monitor.getCorrelationCheckInfo().getTargetItemName(), 
				m_monitor.getCorrelationCheckInfo().getTargetDisplayName(),
				m_monitor.getCorrelationCheckInfo().getTargetMonitorId());
		String msgReferItemName = AnalyticsUtil.getMsgItemName(
				m_monitor.getCorrelationCheckInfo().getReferItemName(), 
				m_monitor.getCorrelationCheckInfo().getReferDisplayName(),
				m_monitor.getCorrelationCheckInfo().getReferMonitorId());
		String[] args = {
				msgTargetItemName,
				m_monitor.getCorrelationCheckInfo().getAnalysysRange().toString(),
				m_monitor.getCorrelationCheckInfo().getReferFacilityId(),
				msgReferItemName};
		/** メッセージ出力 */
		return m_message + "\n" + MessageConstant.MESSAGE_MONITOR_ORGMSG_CORRELATION.getMessage(args) + "\n" + m_collectDataListMessage;
	}

	/* (非 Javadoc)
	 * ノード用オリジナルメッセージを取得(監視ジョブ)
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#makeJobOrgMessage(java.lang.String, java.lang.String)
	 */
	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		String rtn = "";
		if (m_monitor == null || m_monitor.getCorrelationCheckInfo() == null) {
			return rtn;
		}
		String msgTargetItemName = AnalyticsUtil.getMsgItemName(
				m_monitor.getCorrelationCheckInfo().getTargetItemName(), 
				m_monitor.getCorrelationCheckInfo().getTargetDisplayName(),
				m_monitor.getCorrelationCheckInfo().getTargetMonitorId());
		String msgReferItemName = AnalyticsUtil.getMsgItemName(
				m_monitor.getCorrelationCheckInfo().getReferItemName(), 
				m_monitor.getCorrelationCheckInfo().getReferDisplayName(),
				m_monitor.getCorrelationCheckInfo().getReferMonitorId());
		String[] args = {
				msgTargetItemName,
				m_monitor.getCorrelationCheckInfo().getAnalysysRange().toString(),
				m_monitor.getCorrelationCheckInfo().getReferFacilityId(),
				msgReferItemName};
		/** メッセージ出力 */
		if (msg != null) {
			rtn = msg + "\n";
		}
		rtn += MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_CORRELATION.getMessage(args);
		return rtn;
	}
}
