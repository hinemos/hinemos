/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.QueryDivergence;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RpaScenarioOperationResultCreateSettingNotFound;
import com.clustercontrol.hub.model.CollectStringData;
import com.clustercontrol.hub.model.CollectStringKeyInfoPK;
import com.clustercontrol.hub.util.StringDataIdGenerator;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageId;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rpa.model.RpaToolEnvMst;
import com.clustercontrol.rpa.scenario.factory.RpaLogParseException;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.rpa.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

import jakarta.persistence.TypedQuery;

public class RpaAnalyzeExecutor {
	private static Log m_log = LogFactory.getLog(RpaAnalyzeExecutor.class);
	
	// シナリオ実績作成設定ID単位でシナリオログ解析・実績登録処理の排他制御を行うためのSet
	private static Set<String> processing = new CopyOnWriteArraySet<>();

	// シナリオ実績作成設定ID
	private String settingId;
	// シナリオ実績作成設定
	private RpaScenarioOperationResultCreateSetting m_createSetting;
	// RPAログファイル監視のRPAツール設定一覧(キー:監視項目ID, 値:RPAツールID)
	private Map<String, String> m_rpaToolSettingMap;
	// collectIdをキーにしたCollectStringKeyInfo(PK)のマップ
	private Map<Long, CollectStringKeyInfoPK> m_collectStringKeyInfoMap;
	// 文字列収集データの最終解析位置
	private long m_lastPosition;
	// 文字列収集データの最新カウンタ
	private long m_currentStringDataId;

	// --　通知用パラメータ --
	// 成功したログ件数
	private long successed;
	// 失敗したログ件数
	private long failed;
	
	public RpaAnalyzeExecutor() {
		this.successed = 0;
		this.failed = 0;
	}

	// シナリオ実績作成設定単位でシナリオログ解析・実績登録を行う。
	// Quartzより実行される。
	public void start(String scenarioOperationResultCreateSettingId) throws HinemosUnknown {
		// シナリオ実績作成設定ID単位で排他制御
		if (processing.contains(scenarioOperationResultCreateSettingId)) {
			m_log.debug(String.format("analyzeLog() analyze is running. scenarioOperationResultCreateSettingId=%s", scenarioOperationResultCreateSettingId));
			return;
		}

		try {
			processing.add(scenarioOperationResultCreateSettingId);
			// ThreadLocalを設定
			// ユーザIDは空
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, "");
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, true);

			// 解析実行
			this.settingId = scenarioOperationResultCreateSettingId;
			analyze();
		} finally {
			processing.remove(scenarioOperationResultCreateSettingId);
		}
	}
	
	private void analyze() throws HinemosUnknown {
		m_log.debug(String.format("analyze(): start. monitorId=%s", settingId));
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();
			
			// 各設定情報取得
			// 情報が空の場合は終了。
			if (!initialize()) {
				return;
			}
			
			// 収集ログを取得、解析
			int analyzed = 0;
			int fetchSize = HinemosPropertyCommon.rpa_collect_analyze_fetch_size.getIntegerValue();
			int maxSize = HinemosPropertyCommon.rpa_collect_analyze_maxsize.getIntegerValue();
			List<Long> collectIds = new ArrayList<>(m_collectStringKeyInfoMap.keySet());
			
			// collectIds格納用の一時テーブルの作成(JPQLのクエリパラメータ数制限への対応)
			m_log.debug("analyze(): CREATE TEMPORARY TABLE");
			QueryUtil.createTargetCollectorIdsTable();
			// 一時テーブルにcollectIdsを格納
			m_log.debug("analyze(): INSERT collectIds");
			insertTargetCollectIds(collectIds);
			
			while (analyzed < maxSize) {
				// 次のフェッチで最大解析サイズを超えそうな場合は、フェッチサイズを減らす。
				if (analyzed + fetchSize > maxSize) {
					fetchSize = maxSize - analyzed;
				}

				m_log.debug(String.format("analyze(): start fetch string data. maxSize=%d, fetchSize=%d, analyzed=%d", maxSize, fetchSize, analyzed));
				// フェッチサイズを指定して文字列収集データを取得
				List<CollectStringData> rpaLogDataList = getRpaLogDataList(m_lastPosition, fetchSize);
				m_log.debug(String.format("analyze(): %d string data are fetched. start analyze.", rpaLogDataList.size()));
				if (rpaLogDataList.isEmpty()) {
					// 解析対象のデータが無い場合は、最終解析位置を更新して終了。
					m_createSetting.setLastPosition(m_currentStringDataId);
					break;
				}
				if (m_log.isDebugEnabled()) {
					m_log.debug("analyze(): original data=" + Arrays.toString(rpaLogDataList.toArray()));
				}

				// 重複行があると解析がおかしくなるため、重複を取り除く。
				// ただし、シナリオ実行ログがRPAシナリオ実績作成のタイミングで分割された場合には対応していない。
				List<CollectStringData> newRpaLogDataList = new ArrayList<>();
				for (CollectStringData data : rpaLogDataList) {
					boolean isRedundant = false;
					for (CollectStringData newData : newRpaLogDataList) {
						// value、collectIdが一致している場合、重複データと判断する
						if (data.getValue().equals(newData.getValue())
								&& data.getId().getCollectId().equals(newData.getId().getCollectId())) {
							m_log.warn("analyze(): skip, found redundant data=" + data);
							isRedundant = true;
							break;
						}
					}
					if (isRedundant) {
						failed++;
						continue;
					}
					newRpaLogDataList.add(data);
				}

				// 収集データ解析
				if (m_log.isDebugEnabled()) {
					m_log.debug("analyze(): analyzing data=" + Arrays.toString(newRpaLogDataList.toArray()));
				}
				analyzeStringDataList(newRpaLogDataList);

				// 解析済カウンタを更新。
				m_lastPosition = rpaLogDataList.get(rpaLogDataList.size() - 1).getDataId();
				m_createSetting.setLastPosition(m_lastPosition);

				analyzed += rpaLogDataList.size();
			}
			
			// 一時テーブルを削除
			m_log.debug("analyze(): DROP TEMPORARY TABLE");
			QueryUtil.dropTargetCollectorIdsTable();

			if (analyzed != 0 && this.m_createSetting.getNotifyGroupId() != null) {
				// 通知が設定されている場合、完了通知を行う。
				OutputBasicInfo output = new OutputBasicInfo();
				// メッセージ「シナリオ実績作成が完了しました。対象ログ=I件、処理件数=M件, スキップ件数=N件」
				String message = MessageConstant.MESSAGE_RPA_SCENARIO_OPERATION_RESULT_CREATE_FINISH.getMessage(
						String.valueOf(successed + failed), String.valueOf(successed), String.valueOf(failed));
				int priority = PriorityConstant.TYPE_INFO;

				output.setGenerationDate(HinemosTime.currentTimeMillis());
				output.setPriority(priority);
				output.setPluginId(HinemosModuleConstant.RPA_SCENARIO_CREATE);
				output.setMonitorId(m_createSetting.getScenarioOperationResultCreateSettingId());
				output.setSubKey("");
				output.setApplication(m_createSetting.getNotifyApplication());
				output.setJoblinkMessageId(JobLinkMessageId.getId(
						NotifyTriggerType.RPA_SCENARIO_CREATE, 
						HinemosModuleConstant.RPA_SCENARIO_CREATE, 
						m_createSetting.getScenarioOperationResultCreateSettingId()));

				output.setScopeText(m_createSetting.getScope());
				output.setFacilityId(m_createSetting.getFacilityId());
				output.setMessage(message);
				output.setMessageOrg(message);

				// 通知設定
				output.setNotifyGroupId(this.m_createSetting.getNotifyGroupId());
				jtm.addCallback(new NotifyCallback(output));
			}
			jtm.commit();
		}
	}

	// 各設定情報取得
	// 情報が空の場合はfalseを返す(解析が終了する)。
	private boolean initialize() throws HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			try {
				// シナリオ実績作成設定を取得する。
				m_createSetting = QueryUtil.getRpaScenarioCreateSettingPK(settingId);
			} catch (RpaScenarioOperationResultCreateSettingNotFound e) {
				// 設定が無い場合は終了する。
				m_log.warn("analyze(): RpaScenarioOperationResultCreateSetting was not found. scenarioOperationResultCreateSettingId=" + settingId);
				return false;
			} catch (Exception e) {
				// 想定外エラー
				m_log.warn(e);
				throw new HinemosUnknown(e);
			}
			
			// カレンダチェック
			// 実行期間で無い場合は終了
			if (!isRunCalendar()) {
				return false;
			}
	
			// RPAログファイル監視設定一覧を取得する。
			List<MonitorInfo> monitorList = getRpaLogfileMonitorList();
			if (monitorList.isEmpty()) {
				// 監視設定が無い場合は終了。
				m_log.info("analyze(): RPA Logfile Monitor settings don't exist. scenarioOperationResultCreateSettingId=" + settingId);
				return false;
			} else {
				// 監視項目IDとRPAツールIDをマッピング
				m_rpaToolSettingMap = monitorList.stream()
						.collect(Collectors.toMap(
								MonitorInfo::getMonitorId, 
								m -> m.getRpaLogFileCheckInfo().getRpaToolEnvId()));
			}
	
			// 対象ノード一覧を取得する。
			List<String> facilityIdList =  new RepositoryControllerBean().getExecTargetFacilityIdList(
					m_createSetting.getFacilityId(),
					m_createSetting.getOwnerRoleId());
			if (facilityIdList.isEmpty()) {
				// 対象となるノードが無い場合は終了。
				m_log.info(String.format("analyze(): target nodes don't exist. scenarioOperationResultCreateSettingId=%s, facilityId=%s", 
						m_createSetting.getScenarioOperationResultCreateSettingId(), 
						m_createSetting.getFacilityId()));
				return false;
			}
			
			// 監視項目ID、ファシリティIDから対象となる文字列データ収集キーを取得する。
			Map<Long, CollectStringKeyInfoPK> collectStringKeyInfoMap
			= getCollectStringKeyInfoMap(
					new ArrayList<>(m_rpaToolSettingMap.keySet()),
					facilityIdList);
	
			if (collectStringKeyInfoMap.isEmpty()) {
				// 対象となるキーが無い場合は終了
				m_log.info("analyze(): collectStringKeyInfo not found. scenarioOperationResultCreateSettingId=" + settingId);
				return false;
			} else {
				m_collectStringKeyInfoMap = collectStringKeyInfoMap;
			}
			
			// 作成設定から文字列収集データの最終解析位置を取得
			m_lastPosition = m_createSetting.getLastPosition();
			// 全文字列収集データの最新カウンタを取得(解析完了後に解析済カウンタとして登録)
			m_currentStringDataId = StringDataIdGenerator.getCurrent();
	
			// lastPositionが0(初回の解析)の場合は、作成対象日から解析位置を取得する。
			if (m_lastPosition == 0) {
				m_lastPosition = QueryUtil.getStartPosition(m_createSetting.getCreateFromDate());
			}
			m_log.debug("settingId=" + m_createSetting.getScenarioOperationResultCreateSettingId() + ", lastPosition=" + m_lastPosition + ", m_currentStringDataId=" + m_currentStringDataId);
			
			if (m_lastPosition == m_currentStringDataId) {
				// 最終解析位置=最新カウンタになる場合、最終解析位置を更新して終了
				m_createSetting.setLastPosition(m_currentStringDataId);
				return false;				
			}
			
			return true;
		}
	}
	
	/**
	 * 文字列収集データをRPAログとして解析する。
	 */
	private void analyzeStringDataList(List<CollectStringData> rpaLogDataList) throws HinemosUnknown {
		// 収集ログを1件ずつ解析する。
		for (CollectStringData rpaLogData : rpaLogDataList) {
			CollectStringKeyInfoPK keyInfo = m_collectStringKeyInfoMap.get(rpaLogData.getCollectId());
			String facilityId = keyInfo.getFacilityId();
			String rpaToolEnvId = m_rpaToolSettingMap.get(keyInfo.getMonitorId());
			try {
				RpaToolEnvMst rpaToolMst = QueryUtil.getRpaToolEnvMstPK(rpaToolEnvId);
				
				// 解析実行
				RpaLogAnalyzer.analyzeLog(facilityId, rpaLogData, m_createSetting, rpaToolMst);
				successed += 1;
			} catch (RpaLogParseException e) {
				// パースに失敗した場合はスキップし、次のログを解析する。
				m_log.info("analyze(): log parse failed. skip and parse next log. "
						+ "RpaScenarioCreateSettingId=" + settingId
						+ ", rpaToolEnvId=" + rpaToolEnvId
						+ ", monitorId=" + keyInfo.getMonitorId()
						+ ", facilityId=" + keyInfo.getFacilityId()
						+ ", time=" + rpaLogData.getTime()
						);
				m_log.debug(e.getMessage(), e);
				failed += 1;
			} catch (Exception e) {
				// 想定外例外
				throw new HinemosUnknown(e);
			}
		}

	}
	
	// カレンダチェック
	private boolean isRunCalendar() throws HinemosUnknown {
		try {
			return new CalendarControllerBean().isRun(m_createSetting.getCalendarId(), HinemosTime.getDateInstance().getTime()).booleanValue();
		} catch (CalendarNotFound | InvalidRole e) {
			throw new HinemosUnknown(e);
		}
	}
	
	private List<MonitorInfo> getRpaLogfileMonitorList() throws HinemosUnknown {
		try {
			// RPAログファイル監視設定一覧を取得
			return com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoByMonitorTypeId_OR(HinemosModuleConstant.MONITOR_RPA_LOGFILE, m_createSetting.getOwnerRoleId());			
		} catch (Exception e) {
			// 想定外エラー
			m_log.warn("analyze(): ", e);
			throw new HinemosUnknown(e);
		}
	}
	
	/**
	 * クエリパラメータ数制限に抵触しないようにgetCollectStringKeyInfoMapFindByMonitorIdsAndFacilityIdsを実行する。
	 */
	private Map<Long, CollectStringKeyInfoPK> getCollectStringKeyInfoMap(List<String> monitorIds, List<String> facilityIds) {
		Map<Long, CollectStringKeyInfoPK> ret = new HashMap<>();
		
		// クエリパラメータ数制限
		// Listが2種類あるので1/2に設定する
		int threshold = QueryDivergence.getQueryWhereInParamThreashold() / 2;
		
		// 制限内でListを分割してクエリを実行
		int fromMonIdx = 0;
		int fromFacIdx = 0;
		while(fromMonIdx < monitorIds.size()) {
			int toMonIdx;
			if (fromMonIdx + threshold >= monitorIds.size()) {
				toMonIdx = monitorIds.size();
			} else {
				toMonIdx = fromMonIdx + threshold; 
			}
			while(fromFacIdx < facilityIds.size()) {
				int toFacIdx;
				if (fromFacIdx + threshold >= facilityIds.size()) {
					toFacIdx = facilityIds.size();
				} else {
					toFacIdx = fromFacIdx + threshold; 
				}
				
				ret.putAll(
						QueryUtil.getCollectStringKeyInfoMapFindByMonitorIdsAndFacilityIds(
								monitorIds.subList(fromMonIdx, toMonIdx), facilityIds.subList(fromFacIdx, toFacIdx)));
				fromFacIdx = toFacIdx;
			}
			fromMonIdx = toMonIdx;
		}
		return ret;
	}
	
	/**
	 * クエリパラメータ数制限に抵触しないようにinsertJobCompletedSessionsJobSessionJobを実行する。
	 */
	private void insertTargetCollectIds(List<Long> collectIds) {
		// クエリパラメータ数制限
		int threshold = QueryDivergence.getQueryWhereInParamThreashold();
		
		// 制限内でListを分割してクエリを実行
		int fromIdx = 0;
		while (fromIdx < collectIds.size()) {
			int toIdx;
			if (fromIdx + threshold >= collectIds.size()) {
				toIdx = collectIds.size();
			} else {
				toIdx = fromIdx + threshold; 
			}
			QueryUtil.insertTargetCollectIds(collectIds.subList(fromIdx, toIdx));
			fromIdx = toIdx;
		}
	}

	
	/**
	 * 解析対象となる文字列収集データを取得する。<BR>
	 * collectIdsを分割してクエリを実行すると、CollectStringDataの順序性が保持できないため、
	 * 一時テーブルからcollectIdsを取得する。
	 */
	private List<CollectStringData> getRpaLogDataList(long lastPosition, int maxSize) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			TypedQuery<CollectStringData> query;
			if (lastPosition == m_currentStringDataId) {
				return Collections.emptyList();
			} else if (lastPosition < m_currentStringDataId) {
				query = em.createNamedQuery("CollectStringData.rpa", CollectStringData.class); 
			} else {
				// 最終解析位置 > 文字列収集データ最新カウンタ の場合は、IDがサイクルしているため、考慮したクエリを実行
				query = em.createNamedQuery("CollectStringData.rpa.cycled", CollectStringData.class);
			}
			
			return query.setParameter("collectedDataId", lastPosition)
					.setParameter("currentDataId", m_currentStringDataId)
					.setParameter("createFromDate", m_createSetting.getCreateFromDate())
					.setMaxResults(maxSize)
					.getResultList();

		}
	}
}
