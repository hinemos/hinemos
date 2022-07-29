/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.util.HinemosTime;

/**
 * ジョブ履歴のキャッシュ
 * 実行履歴からの変化量で使用する
 *
 * @version 6.1.0
 */
public class JobSessionChangeDataCache {

	private static Log m_log = LogFactory.getLog( JobSessionChangeDataCache.class );

	private static final ILock _lock;

	// ジョブ収集対象のステータス
	private static List<Integer> m_statusList = Arrays.asList(new Integer[]{
			StatusConstant.TYPE_END,
			StatusConstant.TYPE_MODIFIED,
			StatusConstant.TYPE_END_END_DELAY});

	// 収集データ数
	private static Integer analysysCount;

	// データ有効期間
	private static Integer analysysRange;

	static {
		// 収集データ数を設定する
		analysysCount = HinemosPropertyCommon.job_session_change_data_analysys_count.getIntegerValue();
		// データ有効期間を設定する
		analysysRange = HinemosPropertyCommon.job_session_change_data_analysys_range.getIntegerValue();
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(JobSessionChangeDataCache.class.getName());

		try {
			_lock.writeLock();
			
			ConcurrentHashMap<JobSessionChangeDataPK, JobSessionChangeDataInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				storeCache(new ConcurrentHashMap<JobSessionChangeDataPK, JobSessionChangeDataInfo>());
			}
		} finally {
			_lock.writeUnlock();
		}
	}


	/**
	 * 収集データ情報
	 */
	public static class JobSessionChangeDataInfo {
		// キー情報
		private JobSessionChangeDataPK id;
		// 収集データ
		private List<JobSessionChangeData> jobSessionChangeDataList = new ArrayList<>();
		// 前回取得日時
		private Long accessDate;

		public JobSessionChangeDataInfo(JobSessionChangeDataPK pk) {
			this.setId(pk);
		}

		public JobSessionChangeDataPK getId() {
			return this.id;
		}
		public void setId(JobSessionChangeDataPK id) {
			this.id = id;
		}

		public List<JobSessionChangeData> getJobSessionChangeDataList() {
			return this.jobSessionChangeDataList;
		}

		public Long getAccessDate() {
			return this.accessDate;
		}
		public void setAccessDate(Long accessDate) {
			this.accessDate = accessDate;
		}
	}

	/**
	 * ジョブ履歴データBean
	 * 開始日時、実行時間は、計算時に解析時にデータ型を統一する必要があるため、
	 * Double型にして保持している。
	 */
	public static class JobSessionChangeData {
		// セッションID
		private String sessionId;
		// 実行時間
		private Double value;

		public JobSessionChangeData(String sessionId, Double value) {
			this.setSessionId(sessionId);
			this.setValue(value);
		}

		public String getSessionId() {
			return this.sessionId;
		}
		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
		}

		public Double getValue() {
			return this.value;
		}
		public void setValue(Double value) {
			this.value = value;
		}
		@Override
		public String toString(){
			return "JobSessionChangeData : sessionId = " + sessionId + ", value = " + value;
		}
	}

	/**
	 * 収集データキー情報
	 */
	public static class JobSessionChangeDataPK {
		// ジョブユニットID
		private String jobunitId;
		// ジョブID
		private String jobId;

		public JobSessionChangeDataPK(String jobunitId, String jobId) {
			this.setJobunitId(jobunitId);
			this.setJobId(jobId);
		}

		public String getJobunitId() {
			return this.jobunitId;
		}
		public void setJobunitId(String jobunitId) {
			this.jobunitId = jobunitId;
		}

		public String getJobId() {
			return this.jobId;
		}
		public void setJobId(String jobId) {
			this.jobId = jobId;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof JobSessionChangeDataPK)) {
				return false;
			}
			JobSessionChangeDataPK castOther = (JobSessionChangeDataPK)other;
			return
				this.jobunitId.equals(castOther.jobunitId)
				&& this.jobId.equals(castOther.jobId);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int hash = 17;
			hash = hash * prime + this.jobunitId.hashCode();
			hash = hash * prime + this.jobId.hashCode();
			return hash;
		}

		@Override
		public String toString() {
			String[] names = {
					"jobunitId",
					"jobId",
			};
			String[] values = {
					this.jobunitId,
					this.jobId
			};
			return Arrays.toString(names) + " = " + Arrays.toString(values);
		}
	}

	@SuppressWarnings("unchecked")
	private static ConcurrentHashMap<JobSessionChangeDataPK, JobSessionChangeDataInfo> getCache() {
		ConcurrentHashMap<JobSessionChangeDataPK, JobSessionChangeDataInfo> rtn = null;
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_SESSION_CHANGE_DATA);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_JOB_SESSION_CHANGE_DATA + " : " + cache);
		if (cache != null) {
			rtn = (ConcurrentHashMap<JobSessionChangeDataPK, JobSessionChangeDataInfo>)cache;
		}
		return rtn;
	}
	
	private static void storeCache(ConcurrentHashMap<JobSessionChangeDataPK, JobSessionChangeDataInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_JOB_SESSION_CHANGE_DATA + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_SESSION_CHANGE_DATA, newCache);
	}

	/**
	 * キャッシュの更新を行う。
	 * データが存在する場合は処理を終了する。
	 * 
	 * @param jobunitId ジョブユニットID
	 * @param jobId ジョブID
	 */
	private static boolean updateCache(JobSessionJobEntity jobSessionJobEntity) {

		boolean rtn = false;

		// 対象（ジョブネット、コマンドジョブ、ファイル転送ジョブ、承認ジョブ、監視ジョブ、ジョブ連携送信ジョブ、ジョブ連携待機ジョブ、ファイルチェックジョブ、リソース制御ジョブ）以外は処理終了
		if (jobSessionJobEntity.getJobInfoEntity() == null) {
			// JobInfoEntityが設定されていない場合は処理終了
			return rtn;
		}
		if (jobSessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_JOBNET
			&& jobSessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_JOB
			&& jobSessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_FILEJOB
			&& jobSessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_APPROVALJOB
			&& jobSessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_MONITORJOB
			&& jobSessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_JOBLINKSENDJOB
			&& jobSessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_JOBLINKRCVJOB
			&& jobSessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_FILECHECKJOB
			&& jobSessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_RESOURCEJOB
			&& jobSessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_RPAJOB) {
			// 対象外のジョブの場合は処理終了
			return rtn;
		}
		String jobunitId = jobSessionJobEntity.getId().getJobunitId();
		String jobId = jobSessionJobEntity.getId().getJobId();
		// キー
		JobSessionChangeDataPK jobSessionChangeDataPk = new JobSessionChangeDataPK(jobunitId, jobId);
		try {
			_lock.writeLock();

			ConcurrentHashMap<JobSessionChangeDataPK, JobSessionChangeDataInfo> cache = getCache();
			List<JobSessionChangeData> jobSessionChangeDataList = new ArrayList<>();

			// キャッシュ上にデータが存在しない場合にキャッシュ更新処理を行う。
			if (!cache.containsKey(jobSessionChangeDataPk)) {
				// DBからデータを取得する。
				JobSessionChangeDataInfo jobSessionChangeDataInfo = new JobSessionChangeDataInfo(jobSessionChangeDataPk);

				// データ取得
				try {
					List<JobSessionJobEntity> dbJobSessionJobList 
						= QueryUtil.getJobSessionJobByIdsDesc(jobunitId, jobId, analysysCount, m_statusList);
					if (dbJobSessionJobList != null) {
						for (JobSessionJobEntity dbJobSessionJob : dbJobSessionJobList) {
							jobSessionChangeDataList.add(
									new JobSessionChangeData(dbJobSessionJob.getId().getSessionId(),
											dbJobSessionJob.getEndDate().doubleValue() - dbJobSessionJob.getStartDate().doubleValue()));
						}
					}
				} catch (Exception e) {
					m_log.debug("jobSessionChangeData is not found. " + e.getClass().getName() 
							+ ", jobunitId=" + jobunitId 
							+ ", jobId=" + jobId
							+ ", analysysCount=" + analysysCount);
						// エラーにしない
				}
				jobSessionChangeDataInfo.getJobSessionChangeDataList().addAll(jobSessionChangeDataList);
				cache.putIfAbsent(jobSessionChangeDataPk, jobSessionChangeDataInfo);
				storeCache(cache);
			}
			rtn = true;
		} catch (Exception e) {
			m_log.warn("updateCache() : "
					+ ", jobunitId=" + jobunitId 
					+ ", jobId=" + jobId
					+ ", analysysCount=" + analysysCount
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
			m_log.debug("JobSessionChangeDataCache updateCache() : "
					+ ", jobunitId=" + jobunitId 
					+ ", jobId=" + jobId
					+ ", analysysCount=" + analysysCount
					+ ", cachesize=" + getCache().size());
		}
		return rtn;
	}

	/**
	 * キャッシュのデータの追加を行う。
	 * 既にデータが存在する場合は更新する。
	 * 
	 * @param jobSessionJobEntity ジョブセッション情報
	 */
	public static void add(JobSessionJobEntity jobSessionJobEntity) {

		// データが設定されていない場合は処理終了
		if (jobSessionJobEntity == null
				|| jobSessionJobEntity.getId() == null
				|| jobSessionJobEntity.getId().getJobunitId() == null
				|| jobSessionJobEntity.getId().getJobunitId().isEmpty()
				|| jobSessionJobEntity.getId().getJobId() == null
				|| jobSessionJobEntity.getId().getJobId().isEmpty()
				|| jobSessionJobEntity.getStartDate() == null
				|| jobSessionJobEntity.getEndDate() == null) {
			return;
		}

		// 対象ステータスでない場合は処理終了
		if (!m_statusList.contains(jobSessionJobEntity.getStatus())) {
			return;
		}

		// 終了遅延で「実行履歴からの変化量」を使用していない場合は、キャッシュを作成しない
		if (!jobSessionJobEntity.getJobInfoEntity().getEndDelay()
				|| !jobSessionJobEntity.getJobInfoEntity().getEndDelayChangeMount()) {
			m_log.debug(
					String.format("add(): Do not create cache [JobUnitID:%s, JobID:%s, endDelay:%s endDelayChangeMount:%s]",
							jobSessionJobEntity.getId().getJobunitId(), jobSessionJobEntity.getId().getJobId(),
							jobSessionJobEntity.getJobInfoEntity().getEndDelay(),
							jobSessionJobEntity.getJobInfoEntity().getEndDelayChangeMount()));
			removeSpecificCache(jobSessionJobEntity);
			return;
		}
		
		m_log.debug(String.format("add(): Create cache [JobUnitID:%s, JobID:%s, endDelay:%s endDelayChangeMount:%s]",
							jobSessionJobEntity.getId().getJobunitId(), jobSessionJobEntity.getId().getJobId(),
							jobSessionJobEntity.getJobInfoEntity().getEndDelay(),
							jobSessionJobEntity.getJobInfoEntity().getEndDelayChangeMount()));
		
		// キャッシュをUpdateする（データ存在チェックはupdate()で行っている。
		// 対象外の場合は処理終了
		// ジョブ作成後に「実行履歴からの変化量」を有効に変更した場合など、過去のキャッシュが存在しない場合は、
		// updateCacheでキャッシュの作成が行われる
		if (!updateCache(jobSessionJobEntity)) {
			return;
		}
		// キー
		JobSessionChangeDataPK jobSessionChangeDataPk = new JobSessionChangeDataPK(
				jobSessionJobEntity.getId().getJobunitId(), jobSessionJobEntity.getId().getJobId());
		try {
			_lock.writeLock();

			ConcurrentHashMap<JobSessionChangeDataPK, JobSessionChangeDataInfo> cache = getCache();
			if (cache.containsKey(jobSessionChangeDataPk)) {
				// データが存在する場合
				JobSessionChangeDataInfo jobSessionChangeDataInfo = cache.get(jobSessionChangeDataPk);
				// 追加データがキャッシュ上に存在するかどうか確認する
				boolean isExists = false;
				Long collectValue = jobSessionJobEntity.getEndDate().longValue() - jobSessionJobEntity.getStartDate().longValue();
				for (JobSessionChangeData jobSessionChangeData : jobSessionChangeDataInfo.getJobSessionChangeDataList()) {
					if (jobSessionChangeData.getSessionId().equals(jobSessionJobEntity.getId().getSessionId())) {
						// 更新処理を行う
						jobSessionChangeData.setValue(collectValue.doubleValue());
						isExists = true;
						break;
					}
				}
				if (!isExists) {
					// 最新のデータを保持していない場合に追加する
					jobSessionChangeDataInfo.getJobSessionChangeDataList().add(0,
							new JobSessionChangeData(jobSessionJobEntity.getId().getSessionId(), collectValue.doubleValue()));
					// 不要なデータを削除する
					if (jobSessionChangeDataInfo.getJobSessionChangeDataList().size() > analysysCount) {
						for (int i = jobSessionChangeDataInfo.getJobSessionChangeDataList().size() - 1; i >= analysysCount; i--) {
							jobSessionChangeDataInfo.getJobSessionChangeDataList().remove(i);
						}
					}
				}
				cache.put(jobSessionChangeDataPk, jobSessionChangeDataInfo);
				storeCache(cache);
				// 該当のジョブに対してキャッシュで保持している履歴の数を出力
				m_log.debug("add(): Current jobSessionChangeDataInfoList size: "+jobSessionChangeDataInfo.getJobSessionChangeDataList().size());
			}

		} catch (Exception e) {
			m_log.warn("add() : "
					+ ", jobunitId=" + jobSessionJobEntity.getId().getJobunitId()
					+ ", jobId=" + jobSessionJobEntity.getId().getJobId()
					+ ", sessionId=" + jobSessionJobEntity.getId().getSessionId()
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
			m_log.debug("JobSessionChangeDataCache add() : "
					+ ", jobunitId=" + jobSessionJobEntity.getId().getJobunitId()
					+ ", jobId=" + jobSessionJobEntity.getId().getJobId()
					+ ", sessionId=" + jobSessionJobEntity.getId().getSessionId()
					+ ", cachesize=" + getCache().size());
		}
	}

	/**
	 * ジョブ履歴データ（時間情報のみ）を取得する
	 *    
	 * @param jobunitId ジョブユニットID
	 * @param jobId　ジョブID
	 * @return　ジョブ履歴（時間情報のみ）
	 */
	private static List<JobSessionChangeData> getJobSessionChangeDataList(JobSessionJobEntity jobSessionJobEntity) {
		List<JobSessionChangeData> rtn = new ArrayList<>();
		Long targetDate = HinemosTime.currentTimeMillis();
		String jobunitId = null;
		String jobId = null;

		// キャッシュをUpdateする（データ存在チェックはupdate()で行っている。
		// 対象外の場合は処理終了
		if (!updateCache(jobSessionJobEntity)) {
			return rtn;
		}

		jobunitId = jobSessionJobEntity.getId().getJobunitId();
		jobId = jobSessionJobEntity.getId().getJobId();
		// キー
		JobSessionChangeDataPK jobSessionChangeDataPk = new JobSessionChangeDataPK(jobunitId, jobId);
		try {
			_lock.readLock();
			ConcurrentHashMap<JobSessionChangeDataPK, JobSessionChangeDataInfo> cache = getCache();
			if (cache.containsKey(jobSessionChangeDataPk)) {
				JobSessionChangeDataInfo jobSessionChangeDataInfo = cache.get(jobSessionChangeDataPk);
				// キャッシュのリストより必要な情報を抽出する
				for (JobSessionChangeData jobSessionChangeData : jobSessionChangeDataInfo.getJobSessionChangeDataList()) {
					rtn.add(new JobSessionChangeData(jobSessionChangeData.getSessionId(), 
							jobSessionChangeData.getValue()));
				}
			}
		} catch (Exception e) {
			m_log.warn("getJobSessionChangeDataList() : "
					+ ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId
					+ ", targetDate=" + targetDate
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.readUnlock();
			m_log.debug("JobSessionChangeDataCache getJobSessionChangeDataList() : "
					+ ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId
					+ ", targetDate=" + targetDate
					+ ", cachesize=" + getCache().size());
		}
		try {
			_lock.writeLock();
			ConcurrentHashMap<JobSessionChangeDataPK, JobSessionChangeDataInfo> cache = getCache();
			if (cache.containsKey(jobSessionChangeDataPk)) {
				// アクセス日時を更新する
				JobSessionChangeDataInfo jobSessionChangeDataInfo = cache.get(jobSessionChangeDataPk);
				if (jobSessionChangeDataInfo.getAccessDate() == null
						|| jobSessionChangeDataInfo.getAccessDate() < targetDate) {
					jobSessionChangeDataInfo.setAccessDate(targetDate);
					cache.put(jobSessionChangeDataPk, jobSessionChangeDataInfo);
					storeCache(cache);
				}
			}
		} catch (Exception e) {
			m_log.warn("getJobSessionChangeDataList() : "
					+ ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId
					+ ", targetDate=" + targetDate
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
			m_log.debug("JobSessionChangeDataCache getJobSessionChangeDataList() : "
					+ ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId
					+ ", targetDate=" + targetDate
					+ ", cachesize=" + getCache().size());
		}
		return rtn;
	}

	/**
	 * ジョブ履歴データ（時間情報のみ）を取得する
	 *    
	 * @param jobunitId ジョブユニットID
	 * @param jobId　ジョブID
	 * @return　ジョブ履歴（時間情報のみ）
	 */
	public static List<Double> getJobSessionChangeDataDoubleList(JobSessionJobEntity jobSessionJobEntity) {
		List<Double> rtn = new ArrayList<>();
		List<JobSessionChangeData> jobSessionChangeList	= getJobSessionChangeDataList(jobSessionJobEntity);
		if (jobSessionChangeList != null) {
			for (JobSessionChangeData changeData : jobSessionChangeList) {
				rtn.add(changeData.getValue());
			}
		}
		return rtn;
	}

	/**
	 * キャッシュより不要なデータを削除する。
	 * メンテナンス機能より呼び出されることを想定。
	 */
	public static void removeUnnecessaryData() {
		m_log.info("removeUnnecessaryData() cache is removed. start");
		int delCount = 0;
		// 削除対象日時
		Long targetDate = HinemosTime.currentTimeMillis() - analysysRange.longValue() * 60L * 1000L;

		try {
			_lock.writeLock();

			ConcurrentHashMap<JobSessionChangeDataPK, JobSessionChangeDataInfo> cache = getCache();
			for (Iterator<JobSessionChangeDataInfo> iter = cache.values().iterator(); iter.hasNext();) {
				// アクセス日時を参照し、削除するよう修正する
				JobSessionChangeDataInfo info = iter.next();
				if (info.getAccessDate() == null 
						|| info.getAccessDate().longValue() < targetDate) {
					iter.remove();
					m_log.debug("removeUnnecessaryData() : delete"
							+ " jobunitId=" + info.getId().getJobunitId()
							+ ", jobId=" + info.getId().getJobId());
					delCount++;
				}
			}
			storeCache(cache);
			m_log.info("removeUnnecessaryData() : delete count=" + delCount);
		} catch (Exception e) {
			// 何もしない
		} finally {
			_lock.writeUnlock();
		}
	}
	
	/**
	 * 特定のジョブに紐づくキャッシュを削除する
	 */
	private static void removeSpecificCache(JobSessionJobEntity jobSessionJobEntity){
		
		JobSessionChangeDataPK jobSessionChangeDataPk = new JobSessionChangeDataPK(
				jobSessionJobEntity.getId().getJobunitId(), jobSessionJobEntity.getId().getJobId());
		
		ConcurrentHashMap<JobSessionChangeDataPK, JobSessionChangeDataInfo> cache = getCache();

		// 削除対象のキャッシュがあるかを確認
		try {
			_lock.readLock();
			if (!cache.containsKey(jobSessionChangeDataPk)) {
				m_log.debug("removeSpecificCache(): No cache to delete " + jobSessionChangeDataPk);
				return;
			}
			
		} catch (Exception e) {
			m_log.warn("removeSpacificCache() readLock: "
					+ ", jobunitId=" + jobSessionJobEntity.getId().getJobunitId()
					+ ", jobId=" + jobSessionJobEntity.getId().getJobId()
					+ ", sessionId=" + jobSessionJobEntity.getId().getSessionId()
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.readUnlock();
		}

		// 削除対象のキャッシュがある場合、削除
		try {
			_lock.writeLock();
			cache.remove(jobSessionChangeDataPk);
			storeCache(cache);
			
		} catch (Exception e) {
			m_log.warn("removeSpecificCache() writeLock: "
					+ ", jobunitId=" + jobSessionJobEntity.getId().getJobunitId()
					+ ", jobId=" + jobSessionJobEntity.getId().getJobId()
					+ ", sessionId=" + jobSessionJobEntity.getId().getSessionId()
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
			m_log.debug("removeSpecificCache() removed: "
					+ ", jobunitId=" + jobSessionJobEntity.getId().getJobunitId()
					+ ", jobId=" + jobSessionJobEntity.getId().getJobId()
					+ ", sessionId=" + jobSessionJobEntity.getId().getSessionId()
					+ ", cachesize=" + getCache().size());
		}
		
	}
}
