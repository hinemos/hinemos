/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.analytics.util.OperatorChangeUtil;
import com.clustercontrol.analytics.util.OperatorCommonUtil;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.ReturnValue;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosArithmeticException;
import com.clustercontrol.fault.HinemosIllegalArgumentException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.ConditionTypeConstant;
import com.clustercontrol.jobmanagement.bean.DecisionObjectConstant;
import com.clustercontrol.jobmanagement.bean.DelayNotifyConstant;
import com.clustercontrol.jobmanagement.bean.EndStatusCheckConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobLinkSendReturnValue;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.jobmanagement.bean.RetryWaitStatusConstant;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobNextJobOrderInfoEntity;
import com.clustercontrol.jobmanagement.model.JobOutputInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobWaitGroupInfoEntity;
import com.clustercontrol.jobmanagement.model.JobWaitInfoEntity;
import com.clustercontrol.jobmanagement.queue.JobQueue;
import com.clustercontrol.jobmanagement.queue.JobQueueContainer;
import com.clustercontrol.jobmanagement.queue.JobQueueLimitExceededException;
import com.clustercontrol.jobmanagement.queue.JobQueueNotFoundException;
import com.clustercontrol.jobmanagement.rpa.bean.RpaScreenshotTriggerTypeConstant;
import com.clustercontrol.jobmanagement.util.JobSessionChangeDataCache;
import com.clustercontrol.jobmanagement.util.JobSessionJobUtil;
import com.clustercontrol.jobmanagement.util.ParameterUtil;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.jobmanagement.util.RefreshRunningQueueAfterCommitCallback;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Singletons;
import com.clustercontrol.util.apllog.AplLogger;

public class JobSessionJobImpl {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( JobSessionJobImpl.class );

	/** タイムゾーン*/
	private static final long TIMEZONE = HinemosTime.getTimeZoneOffset();

	private static final ILock _lock;

	private static final String DELAY_SKIP_RESULT = "DelaySkip";

	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(JobSessionJobImpl.class.getName());
		
		try {
			_lock.writeLock();
			
			ArrayList<String> cache = getForceCheckCache();
			if (cache == null) {	// not null when clustered
				storeForceCheckCache(new ArrayList<String>());
			}
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * JobSessionJobEntity について、子を取得するためのキャッシュ。
	 * ジョブ開始から終了までで使用する想定で、始めにキャッシュを生成するため競合することはない。
	 * ジョブ実行中に別スレッドから同時に読み込むことある。
	 * 
	 * キー：セッションID、ジョブユニットID、親のジョブID
	 * 値：子のジョブIDのリスト
	 */
	static private Map<String, Map<String, Map<String, List<String>>>> jobSessionJobChildrenCache = new ConcurrentHashMap<>();


	/**
	 * JobSessionJobEntity について、子を取得するためのキャッシュ作成
	 * 
	 * @param sessionId
	 * @param jobunitId
	 */
	private void makeJobSessionJobChildrenCache(String sessionId, String jobunitId) {
		m_log.debug("makeJobSessionJobChildrenCache(sessionId=" + sessionId + ", jobunitId=" + jobunitId + ")");

		if (jobSessionJobChildrenCache.containsKey(sessionId)) {
			m_log.debug("makeJobSessionJobChildrenCache(), has cache.");
			return;
		}

		// キャッシュがない場合、キャッシュ作成
		// sessionId, jobunitIdの全てを取得
		Collection<JobSessionJobEntity> jobSessionJobList = QueryUtil.getAllChildJobSessionJob(sessionId, jobunitId);
		m_log.debug("makeJobSessionJobChildrenCache(), make cache from jobSessionJobList=" + Arrays.toString(jobSessionJobList.toArray()));

		Map<String, List<String>> jobunitCache = new ConcurrentHashMap<String, List<String>>();
		for (JobSessionJobEntity job : jobSessionJobList) {
			m_log.debug("makeJobSessionJobChildrenCache(), job=" + job);
			String parentId = job.getParentJobId();
			if (!jobunitCache.containsKey(parentId)) {
				jobunitCache.put(parentId, new CopyOnWriteArrayList<>());
			}
			jobunitCache.get(parentId).add(job.getId().getJobId());
		}

		Map<String, Map<String, List<String>>> sessionCache = new ConcurrentHashMap<String, Map<String,List<String>>>();
		sessionCache.put(jobunitId, jobunitCache);
		jobSessionJobChildrenCache.put(sessionId, sessionCache);
		m_log.debug("makeJobSessionJobChildrenCache(), end. jobSessionJobChildrenCache=" + jobSessionJobChildrenCache);
	}

	/**
	 * JobSessionJobEntity について、子を取得するためのキャッシュを削除
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 */
	private void removeJobSessionJobChildrenCache(String sessionId, String jobunitId, String jobId) {
		m_log.debug("removeJobSessionJobChildrenCache(sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ")");

		if (!jobSessionJobChildrenCache.containsKey(sessionId)) {
			m_log.debug("removeJobSessionJobChildrenCache(), already cache removed sessionId=" + sessionId);
			return;
		}

		// TOP_JOB_IDの場合、キャッシュを削除する
		List<String> jobIdList = jobSessionJobChildrenCache.get(sessionId).get(jobunitId).get(CreateJobSession.TOP_JOB_ID);
		if (jobIdList == null) {
			// 想定外
			m_log.warn("removeJobSessionJobChildrenCache(), list of TOP_JOB_ID is null");
			return;
		}
		if (jobIdList.size() != 1) {
			// 想定外
			m_log.warn("removeJobSessionJobChildrenCache(), jobIdList=" + Arrays.toString(jobIdList.toArray()));
			return;
		}
		if (!jobIdList.get(0).equals(jobId)) {
			// TOPでないので、削除しない
			m_log.debug("removeJobSessionJobChildrenCache(), is not TOP, NOT removed. jobId=" + jobId);
			return;
		}
		// キャッシュ削除
		m_log.debug("removeJobSessionJobChildrenCache(), cache removed. sessionId=" + sessionId);
		jobSessionJobChildrenCache.remove(sessionId);
	}

	/**
	 * JobSessionJobEntity について、子を取得するためのキャッシュ削除。
	 * セッションIDリスト（実行中のジョブセッション）以外のキャッシュを削除する。
	 * 通常は removeJobSessionJobChildrenCache() で削除されるはずであるが、クリーニング用に用意している。
	 * 
	 * @param sessionIdList セッションIDリスト（実行中のジョブセッション）
	 */
	public static void removeJobSessionJobChildrenCacheExceptSessionList(List<String> sessionIdList) {
		m_log.debug("removeJobSessionJobChildrenCacheExceptSessionList(sessionIdList=" + Arrays.toString(sessionIdList.toArray()) + ")");	
	
		for (String cacheSessionId : jobSessionJobChildrenCache.keySet()) {
			if (sessionIdList.contains(cacheSessionId)) {
				// sessionIdListにあるものは削除しない
				m_log.debug("removeJobSessionJobChildrenCacheExceptSessionList(), skip cacheSessionId=" + cacheSessionId);	
				continue;
			}

			m_log.debug("removeJobSessionJobChildrenCacheExceptSessionList(), remove cacheSessionId=" + cacheSessionId);	
			jobSessionJobChildrenCache.remove(cacheSessionId);
		}
	}

	// 次回のrunningCheckを強制的に動作させるためのリスト。
	// このリストがないと、次回時刻(待ち条件、遅延監視)になるまではrunnincCheckは動作しない。
	@SuppressWarnings("unchecked")
	private static ArrayList<String> getForceCheckCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_FORCE_CHECK);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_JOB_FORCE_CHECK + " : " + cache);
		return cache == null ? null : (ArrayList<String>)cache;
	}
	
	private static void storeForceCheckCache(ArrayList<String> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_JOB_FORCE_CHECK + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_FORCE_CHECK, newCache);
	}

	public static void addForceCheck(String sessionId) {
		try {
			_lock.writeLock();
			
			ArrayList<String> cache = getForceCheckCache();
			if (cache.contains(sessionId)) {
				return;
			}
			
			cache.add(sessionId);
			storeForceCheckCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public static boolean checkRemoveForceCheck(String sessionId) {
		try {
			_lock.writeLock();
			
			ArrayList<String> cache = getForceCheckCache();
			boolean flag = cache.remove(sessionId);
			if (flag) {
				m_log.debug("checkRemoveForceCheck " + sessionId);
			}
			return flag; // forceCheckに含まれている場合はtrueを返す。
		} finally {
			_lock.writeUnlock();
		}
	}

	private static ConcurrentHashMap <String, Long> checkTimeMap = new ConcurrentHashMap<String, Long>();

	public static boolean isSkipCheck(String sessionId) {
		Long time = checkTimeMap.get(sessionId);
		if (time == null) {
			return false;
		}
		if (time <= HinemosTime.currentTimeMillis()) {
			return false;
		}
		return true;
	}

	/**
	 * inputDateが原因でジョブが実行されなかった場合は、inputDateをマップに追加する。
	 * isCheckDateでは、inputDateを過ぎていたらtrueを返し、ジョブの実行チェックをする。
	 * @param sessionId
	 * @param inputTime
	 */
	private static void addCheckDate(String sessionId, Long inputTime) {
		Long time = checkTimeMap.get(sessionId);
		if (time == null || inputTime < time) {
			Date date = null;
			if (time != null) {
				date = new Date(time);
			}
			m_log.info("addCheckDate " + sessionId +
					", input=" + new Date(inputTime) + ", date=" + date);
			checkTimeMap.put(sessionId, inputTime);
		}
	}

	/**
	 * ジョブセッションを実行するときには、最初にこのメソッドを呼ぶ。
	 * 次回の定期チェックはずっと未来にしてもらう。
	 * (待ち条件や開始遅延等に時刻が含まれている場合は、addCheckDateが呼ばれ、
	 *  次回の定期チェック時刻が設定される。)
	 * @param sessionId
	 */
	public static void maxCheckDate(String sessionId) {
		m_log.debug("maxCheckDate " + sessionId);
		checkTimeMap.put(sessionId, Long.MAX_VALUE);
	}

	/**
	 * 次回の定期チェックを削除する。
	 * (待ち条件や開始遅延等に時刻が含まれている場合は、addCheckDateが呼ばれ、
	 *  次回の定期チェック時刻が設定される。)
	 * @param sessionId
	 */
	public static void removeCheckDate(String sessionId) {
		Long time = checkTimeMap.get(sessionId);
		if (time != null) {
			checkTimeMap.remove(sessionId);
			if (m_log.isDebugEnabled()) {
				m_log.debug("removeCheckDate " + sessionId);
			}
		}
	}
	
	/**
	 * 定期的に待ち合わせ解除をチェックするジョブを記憶しておきます。
	 * key: sessionId
	 * value: List<{jobunitId, jobId}>
	 */
	private static ConcurrentHashMap <String, List<String[]>> waitCheckJobMap = new ConcurrentHashMap<>();
	
	/**
	 * 定期待ち条件判定チェックジョブを登録します。
	 * 異なるスレッドが同一のジョブセッションについてこの処理を呼び出すことはないので排他制御は不要です。
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 */
	private void addWaitCheckJob(String sessionId, String[] jobunitIdJobId) {
		List<String[]> waitCheckJobIdList = waitCheckJobMap.get(sessionId);
		if (waitCheckJobIdList == null) {
			waitCheckJobIdList = new ArrayList<>();
			waitCheckJobMap.put(sessionId, waitCheckJobIdList);
		}
		//定期チェックの対象となるJobunitId, JobIdを配列に格納
		//既に登録されている場合は登録しない
		for(String[] exists: waitCheckJobIdList) {
			if (exists[0].equals(jobunitIdJobId[0]) &&
					exists[1].equals(jobunitIdJobId[1])) {
				return;
			}
		}
		waitCheckJobIdList.add(jobunitIdJobId);
		m_log.info("addWaitCheckJob " + sessionId + ","
					+ " jobunitId=" + jobunitIdJobId[0] + ", jobId=" + jobunitIdJobId[1]);
	}

	/**
	 * 待ち合わせチェックジョブのリストを返します。
	 * @param sessionId
	 * @return List<{jobunitId, jobId}の配列>  対象のジョブが無い場合は空のリストを返します。
	 */
	public List<String[]> getWaitCheckJob(String sessionId) {
		List<String[]> waitCheckJobIdList = waitCheckJobMap.getOrDefault(sessionId, new ArrayList<String[]>());
		return waitCheckJobIdList;
	}
	
	/**
	 * 待ち合わせチェックジョブをクリアします。
	 */
	public void clearWaitCheckMap(String sessionId) {
		List<String[]> waitCheckJobIdList = waitCheckJobMap.get(sessionId);
		if (waitCheckJobIdList != null) {
			m_log.info("clearWaitCheckJob " + sessionId);
			waitCheckJobMap.remove(sessionId);
		}
	}

	/**
	 * ジョブ開始処理メイン1を行います。
	 *
	 * "start"という名前ですが、実際は単純に開始していないジョブを開始するだけでなく、
	 * 実行中ジョブの終了遅延チェックなどの検査的な処理も行います。
	 * 
	 * @param sessionId
	 * @param jobId
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws FacilityNotFound
	 */
	public void startJob(String sessionId, String jobunitId, String jobId)
			throws JobInfoNotFound, HinemosUnknown, InvalidRole, FacilityNotFound {
		m_log.debug("startJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		// キャッシュを作成する
		makeJobSessionJobChildrenCache(sessionId, jobunitId);

		// 実行状態によって処理分岐する
		switch (sessionJob.getStatus()) {
		case StatusConstant.TYPE_WAIT:
			startJobInWait(sessionJob);
			break;
		case StatusConstant.TYPE_RUNNING_QUEUE:
			startJobInRunningQueue(sessionJob);
			break;
		case StatusConstant.TYPE_RUNNING:
			startJobInRunning(sessionJob);
			break;
		case StatusConstant.TYPE_SKIP:
			startJobInSkip(sessionJob);
			break;
		default:
			// NOP
			break;
		}
	}

	// 待機中の場合
	private void startJobInWait(JobSessionJobEntity sessionJob)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		m_log.debug("startJobInWait(" + sessionJob + ")");

		String sessionId = sessionJob.getId().getSessionId();
		String jobunitId = sessionJob.getId().getJobunitId();
		String jobId = sessionJob.getId().getJobId();

		// 開始条件とカレンダをチェックする
		if (checkWaitCondition(sessionId, jobunitId, jobId) && checkCalendar(sessionId, jobunitId, jobId)) {
			String queueId = sessionJob.getJobInfoEntity().getQueueIdIfEnabled();
			// ジョブキューが設定されている場合は、「実行中(キュー待機)」へ遷移
			if (queueId != null) {
				try {
					sessionJob.setStatus(StatusConstant.TYPE_RUNNING_QUEUE);
					JobQueue queue = Singletons.get(JobQueueContainer.class).get(queueId);
					queue.add(sessionId, jobunitId, jobId);
				} catch (JobQueueNotFoundException e) {
					// キューが削除されている場合、設定されていないものとして、即時に実行中へ遷移させる。
					m_log.info("startJobInWait: Skip the absent JobQueue. ["
							+ queueId + "," + sessionId + "," + jobId + "]");
					changeToRunning(sessionJob);
				} catch (JobQueueLimitExceededException e) {
					// キューサイズ超過
					endByJobQueueLimit(sessionJob, queueId);
				}
			}
			// ジョブキューが設定されていない場合は、「実行中」へ遷移
			else {
				changeToRunning(sessionJob);
			}
		} else {
			// 実行できなかった場合は開始遅延チェック
			if (!checkStartDelayRecursive(sessionId, jobunitId, jobId)) {
				// 開始遅延の操作が行われなかった場合のみ、
				// 待機中ジョブがジョブ変数、又はセッション横断待ち条件を持つ場合、
				// 定期チェックで待ち合わせ解除を確認する
				if (sessionJob.getWaitCheckFlg() != null && sessionJob.getWaitCheckFlg()) {
					addWaitCheckJob(sessionId, new String[] { jobunitId, jobId });
				}
			}
		}
	}

	// 実行中(キュー待機)の場合
	private void startJobInRunningQueue(JobSessionJobEntity sessionJob) throws JobInfoNotFound, InvalidRole {
		m_log.debug("startJobInRunningQueue(" + sessionJob + ")");

		String sessionId = sessionJob.getId().getSessionId();
		String jobunitId = sessionJob.getId().getJobunitId();
		String jobId = sessionJob.getId().getJobId();

		// 開始遅延チェックを行い、ステータス遷移した場合はキューから除去する。
		if (checkStartDelayRecursive(sessionId, jobunitId, jobId)
				&& sessionJob.getStatus() != StatusConstant.TYPE_RUNNING_QUEUE) {
			String queueId = sessionJob.getJobInfoEntity().getQueueId(); // 状況的にqueueIdは!null
			try {
				JobQueue queue = Singletons.get(JobQueueContainer.class).get(queueId);
				queue.remove(sessionId, jobunitId, jobId);
			} catch (JobQueueNotFoundException e) {
				// キューが存在しないなら、結果として"キューからの除去"という目的は果たせているので、ログだけ出して無視する。
				m_log.info("startJobInRunningQueue: " + e);
			}
			// キュー待機していたということは、既に待ち条件を満たした状態にある。
			// 開始遅延によりスキップへ遷移した場合は次の定期チェックで終了するように、チェック対象に加える。
			if (sessionJob.getStatus() == StatusConstant.TYPE_SKIP) {
				m_log.info("startJobInRunningQueue: Reserve next running check. sessionId=" + sessionId);
				addForceCheck(sessionId);
			}
		} else {
			// 開始遅延の操作が行われなかった場合のみ、
			// 待機中ジョブがジョブ変数、又はセッション横断待ち条件を持つ場合、
			// 定期チェックで待ち合わせ解除を確認する
			if (sessionJob.getWaitCheckFlg() != null && sessionJob.getWaitCheckFlg()) {
				addWaitCheckJob(sessionId, new String[] { jobunitId, jobId });
			}
		}
	}

	// 実行中の場合
	private void startJobInRunning(JobSessionJobEntity sessionJob)
			throws HinemosUnknown, JobInfoNotFound, InvalidRole, FacilityNotFound {
		m_log.debug("startJobInRunning(" + sessionJob + ")");

		String sessionId = sessionJob.getId().getSessionId();
		String jobunitId = sessionJob.getId().getJobunitId();
		String jobId = sessionJob.getId().getJobId();

		// 終了遅延チェック
		if (!checkEndDelay(sessionId, jobunitId, jobId)) {
			// 遅延操作されない場合は、下位のジョブツリーを見に行く。
			Collection<JobSessionJobEntity> children = QueryUtil.getChildJobSessionJob(sessionId, jobunitId, jobId);
			for (JobSessionJobEntity child : children) {
				startJob(sessionId, jobunitId, child.getId().getJobId());
			}
		}

		// ノードへの実行指示(終了していたらendJobを実行)
		// ここはジョブを中断にして、ノード詳細で終了した後に、ジョブの中断解除をしたら、
		// RUNNINGのままで止まってしまう。
		// それを回避するために下記の実装を加える。
		if (sessionJob.hasSessionNode()) {
			if (new JobSessionNodeImpl().startNode(sessionId, jobunitId, jobId, false)) {
				// いずれかのノードで条件を満たしたら終了の場合
				if (sessionJob.getJobInfoEntity().getProcessMode() == ProcessingMethodConstant.TYPE_ANY_NODE) {
					// 中断中にいずれかのノードが条件を満たしていた場合、
					// それ以外のノードを停止する必要があるため他のノードの停止処理を実行する
					new JobSessionNodeImpl().endNodeByOtherNode(sessionId, jobunitId, jobId, null);
				}
				endJob(sessionId, jobunitId, jobId, "", true);
			}
		}
	}

	// スキップの場合
	private void startJobInSkip(JobSessionJobEntity sessionJob)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		String sessionId = sessionJob.getId().getSessionId();
		String jobunitId = sessionJob.getId().getJobunitId();
		String jobId = sessionJob.getId().getJobId();

		// 開始条件をチェックする
		Integer endStatus = 0;
		Integer endValue = 0;
		Integer status = 0;
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		if (job.getStartDelay().booleanValue() && job.getStartDelayOperation().booleanValue()
				&& job.getStartDelayOperationType() == OperationConstant.TYPE_STOP_SKIP
				&& DELAY_SKIP_RESULT.equals(sessionJob.getResult())) {
			// 開始遅延によるスキップの場合
			endStatus = job.getStartDelayOperationEndStatus();
			endValue = job.getStartDelayOperationEndValue();
			status = StatusConstant.TYPE_END_START_DELAY;
		} else {
			// 制御によるスキップor停止[スキップ]の場合
			endStatus = job.getSkipEndStatus();
			endValue = job.getSkipEndValue();
			status = StatusConstant.TYPE_END_SKIP;
		}
		if (checkWaitCondition(sessionId, jobunitId, jobId)) {
			// 実行状態、終了状態、終了値、終了日時を設定
			setEndStatus(sessionId, jobunitId, jobId, status, endStatus, endValue, null);
			// ジョブ終了時関連処理
			endJob(sessionId, jobunitId, jobId, null, false);
		} else {
			// 待機中ジョブがジョブ変数、又はセッション横断待ち条件を持つ場合、
			// 定期チェックで待ち合わせ解除を確認する
			if (sessionJob.getWaitCheckFlg() != null && sessionJob.getWaitCheckFlg()) {
				addWaitCheckJob(sessionId, new String[] { jobunitId, jobId });
			}
		}
	}

	/**
	 * キュー待機状態のジョブを実行開始します。
	 * <p>
	 * 本メソッドは、ジョブキュー {@link JobQueue} が使用します。
	 * ジョブキュー以外が本メソッドを呼び出した場合、ジョブキューが管理しているジョブ実行状況と
	 * ジョブセッションの実行状態に矛盾が生じます。
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 */
	public void startQueuedJob(String sessionId, String jobunitId, String jobId)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		m_log.debug("startQueuedJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		// 実行状態チェック
		if (sessionJob.getStatus() != StatusConstant.TYPE_RUNNING_QUEUE) return;

		changeToRunning(sessionJob);
	}
	
	// 実行状態へ遷移
	private void changeToRunning(JobSessionJobEntity sessionJob)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		String sessionId = sessionJob.getId().getSessionId();
		String jobunitId = sessionJob.getId().getJobunitId();
		String jobId = sessionJob.getId().getJobId();
		boolean isExpNode = false;

		// 実行状態に遷移した場合は、1分後に終了遅延のチェックをする必要がある。
		addForceCheck(sessionId);
		// 実行状態が待機→実行中に遷移する場合ノードを再展開の対象になる
		if (sessionJob.getStatus() == StatusConstant.TYPE_WAIT) {
			isExpNode = true;
		}
		// 実行状態を実行中にする
		sessionJob.setStatus(StatusConstant.TYPE_RUNNING);
		// 開始・再実行日時を設定
		sessionJob.setStartDate(HinemosTime.currentTimeMillis());
		// 実行回数をインクリメント
		sessionJob.setRunCount(sessionJob.getRunCount() + 1);
		// 通知処理
		new Notice().notify(sessionId, jobunitId, jobId, EndStatusConstant.TYPE_BEGINNING);
		if (sessionJob.hasSessionNode()) {
			// ノードへの実行指示
			new JobSessionNodeImpl().startNode(sessionId, jobunitId, jobId, isExpNode);
		} else {
			// 配下のジョブ開始処理 (状態がTYPE_RUNNINGへ変更されている)
			startJob(sessionId, jobunitId, jobId);
		}
	}

	// ジョブキュー超過のため異常終了させる。
	private void endByJobQueueLimit(JobSessionJobEntity sessionJob, String queueId)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		String sessionId = sessionJob.getId().getSessionId();
		String jobunitId = sessionJob.getId().getJobunitId();
		String jobId = sessionJob.getId().getJobId();

		// ジョブ異常終了
		setEndStatus(sessionId, jobunitId, jobId, StatusConstant.TYPE_END_QUEUE_LIMIT, EndStatusConstant.TYPE_ABNORMAL,
				sessionJob.getJobInfoEntity().getAbnormalEndValue(), null);
		endJob(sessionId, jobunitId, jobId, null, false);

		// INTERNALイベント通知
		try {
			AplLogger.put(InternalIdCommon.JOB_QUEUE_SYS_001, new String[] { queueId, sessionId, jobId });
		} catch (Exception e) {
			// 通知に失敗したとしても終了処理を中止しないように、例外はここで抑える。
			m_log.warn("endByJobQueueLimit: Failed to notify InternalEvent.", e);
		}
	}

	/**
	 * ジョブの待ち条件のチェックを行います。
	 * @see checkWaitCondition(String, String, String, boolean)
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @return
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 */
	private boolean checkWaitCondition(String sessionId, String jobunitId, String jobId) 
			throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		return checkWaitCondition(sessionId, jobunitId, jobId, false /* skipTimeCondition=false */);
	}

	/**
	 * ジョブの待ち条件のチェックを行います。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId ジョブユニットID
	 * @param jobId ジョブID
	 * @param skipTimeCondition 時刻、セッション開始後の時間の判定をスキップする場合にtrueを渡す(排他分岐で使用)
	 * @return true：実行可、false：実行不可
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 */
	private boolean checkWaitCondition(String sessionId, String jobunitId, String jobId, boolean skipTimeCondition)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		m_log.debug("checkWaitCondition() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		//<名称の定義例>
		// オブジェクト(waitGroupsでAND,OR[jobConditionType])
		// --待ち条件群1(waitGroupでAND,OR[waitConditionType]) 
		// ----待ち条件1-1(wait)
		// ----待ち条件1-2(wait)
		// --待ち条件群2(waitGroupでAND,OR[waitConditionType])
		// ----待ち条件2-1(wait)
		// ----待ち条件2-2(wait)

		//startCheck 待ち条件チェックの結果、ジョブを実行するかを表すフラグ
		//AND条件の場合　全ての待ち条件が満たされている場合：ture
		//OR条件の場合　待ち条件のうちどれか一つでも満たされている場合：true
		boolean startCheck = true;

		//ジョブの終了処理を行う
		// true 終了処理が可能なので正常か異常
		// false 終了処理が不可能なので待機
		boolean isEnd = true;

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		//待機中の場合
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		//待ち条件群を取得
		Collection<JobWaitGroupInfoEntity> waitGroups = job.getJobWaitGroupInfoEntities();

		//待ち条件が設定されていない場合
		if(waitGroups == null || waitGroups.size() == 0){
			return true;
		}

		if(job.getConditionType() == null){
			HinemosUnknown e = new HinemosUnknown("checkWaitCondition() : ConditionType is null.");
			m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage() + " sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);
			throw e;
		}

		// 待ち条件群の判定を行う
		List<ReturnValue> groupReturnValueList = new ArrayList<>();
		ReturnValue groupReturnValue = null;
		for(JobWaitGroupInfoEntity waitGroup : waitGroups) {
			if(waitGroup.getConditionType() == null){
				HinemosUnknown e = new HinemosUnknown("checkWaitCondition() : WaitGroup ConditionType is null.");
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage() + " sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);
				throw e;
			}

			// 待ち条件群内のジョブ待ち条件の判定を行う。
			List<ReturnValue> infoReturnValueList = new ArrayList<>();
			ReturnValue infoReturnValue = null;
			for (JobWaitInfoEntity wait : waitGroup.getJobWaitInfoEntities()) {
				m_log.debug("checkWaitCondition() : id=" + wait.getId().toString());
				if (wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS
						|| wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE
						|| wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS
						|| wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE) {
					// 待ち条件群内のジョブを指定する待ち条件についての判定
					infoReturnValue = checkWaitJobEnd(sessionId, jobunitId, jobId, sessionJob, wait);
				} else if (wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE) {
					JobSessionJobEntity targetSessionJob = QueryUtil.getJobSessionJobPK(sessionJob.getId().getSessionId(),
							wait.getId().getTargetJobunitId(), wait.getId().getTargetJobId());
					// 待ち条件「ジョブ（戻り値）」については、先行ジョブの実行対象が１ノードである場合のみ使用可能とする
					if(targetSessionJob.getJobSessionNodeEntities().size() > 1){
						// 後の判定は行わず「待機状態」のままとする
						infoReturnValue = ReturnValue.NONE;
					} else {
						// 待ち条件群内のジョブ終了判定
						infoReturnValue = checkWaitJobEnd(sessionId, jobunitId, jobId, sessionJob, wait);
					}
				} else if (wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
					// 待ち条件群内のジョブ変数判定
					infoReturnValue = checkWaitJobParam(sessionId, jobunitId, job, wait);
				} else if (wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_TIME && !skipTimeCondition) {
					// 待ち条件群内の時刻判定
					// 時間の判定を行う、時間については正常か待機となる
					// 補足：待ち条件群内では時間の設定は1件しか設定できない
					if (checkWaitTime(sessionId, wait.getTime())) {
						infoReturnValue = ReturnValue.TRUE;
					} else {
						infoReturnValue = ReturnValue.NONE;
					}
				} else if (wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_START_MINUTE && !skipTimeCondition) {
					// 待ち条件群内のセッション開始後の時間判定
					// 時間の判定を行う、時間については正常か待機となる
					// 補足：待ち条件群内では時間の設定は1件しか設定できない
					if (checkStartMinute(sessionId, wait.getStartMinute())) {
						infoReturnValue = ReturnValue.TRUE;
					} else {
						infoReturnValue = ReturnValue.NONE;
					}
				} else {
					HinemosUnknown e = new HinemosUnknown("checkWaitCondition() : JudgmentObject is unknown.");
					m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage() + " sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);
					throw e;
				}
				infoReturnValueList.add(infoReturnValue);
				if (waitGroup.getConditionType() == ConditionTypeConstant.TYPE_OR
						&& infoReturnValue == ReturnValue.TRUE) {
					break;
				}
				if (waitGroup.getConditionType() == ConditionTypeConstant.TYPE_AND
						&& infoReturnValue == ReturnValue.FALSE) {
					break;
				}
			}

			// 結果が0件の場合はエラーとする
			if (infoReturnValueList.size() == 0) {
				HinemosUnknown e = new HinemosUnknown("checkWaitCondition() : infoReturnValue is null.");
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage() + " sessionId=" + sessionId
						+ ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", waitGroupId=" + waitGroup.getId().toString());
				throw e;
			}

			// ここから待ち条件群内の評価を行う
			if (waitGroup.getConditionType() == ConditionTypeConstant.TYPE_AND) {
				// AND条件は時間に関係なく、1件でもNGが含まれる時点で待ち条件群内の判定を終了する
				// 待ち条件群内のジョブ対象条件の判定を終了
				if(infoReturnValueList.contains(ReturnValue.FALSE)) {
					groupReturnValue = ReturnValue.FALSE;
				} else if (infoReturnValueList.contains(ReturnValue.NONE)) {
					// 時間を満たしていない
					// 先行ジョブが終了していない
					// セッション横断ジョブについてジョブ履歴範囲に存在しない場合
					// 待ち条件群の判定が終了していない場合、待ち条件群のチェック状態はNONEにする
					groupReturnValue = ReturnValue.NONE;
				} else {
					groupReturnValue = ReturnValue.TRUE;
				}
			} else if (waitGroup.getConditionType() == ConditionTypeConstant.TYPE_OR) {
				// OR条件は時間を満たす、または1件でもOKが含まれる時点で待ち条件群内の判定を終了する
				// 待ち条件群内のジョブ対象条件の判定を終了
				if (infoReturnValueList.contains(ReturnValue.TRUE)) {
					groupReturnValue = ReturnValue.TRUE;
				} else if (infoReturnValueList.contains(ReturnValue.NONE)) {
					// 時間を満たしていない
					// 先行ジョブが終了していない
					// セッション横断ジョブについてジョブ履歴範囲に存在しない場合
					// 待ち条件群の判定が終了していない場合、待ち条件群のチェック状態はNONEにする
					groupReturnValue = ReturnValue.NONE;
				} else {
					groupReturnValue = ReturnValue.FALSE;
				}
			}
			groupReturnValueList.add(groupReturnValue);
			if (job.getConditionType() == ConditionTypeConstant.TYPE_OR
					&& groupReturnValue == ReturnValue.TRUE) {
				break;
			}
			if (job.getConditionType() == ConditionTypeConstant.TYPE_AND
					&& groupReturnValue == ReturnValue.FALSE) {
				break;
			}
		}

		// 結果が0件の場合はエラーとする
		if (groupReturnValueList.size() == 0) {
			HinemosUnknown e = new HinemosUnknown("checkWaitCondition() : groupReturnValue is null.");
			m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage() + " sessionId=" + sessionId
					+ ", jobunitId=" + jobunitId + ", jobId=" + jobId);
			throw e;
		}

		if (job.getConditionType() == ConditionTypeConstant.TYPE_AND) {
			// 待ち条件の判定条件がANDかつ、ジョブの実行が不可能の場合
			if (groupReturnValueList.contains(ReturnValue.FALSE)) {
				startCheck = false;
				// 判定が終了していないものがあっても条件が確定するので終了処理を行う
				isEnd = true;
			} else if (groupReturnValueList.contains(ReturnValue.NONE)) {
				// 時間を満たしていない
				// 先行ジョブが終了していない
				// セッション横断ジョブについてジョブ履歴範囲に存在しない場合
				// 終了処理は行わず待機するようにフラグを設定する
				isEnd = false;
			} else {
				startCheck = true;
				isEnd = true;
			}
		} else if (job.getConditionType() == ConditionTypeConstant.TYPE_OR) {
			if (groupReturnValueList.contains(ReturnValue.TRUE)) {
				// OR条件は時間を満たす、または1件でもOKが含まれる時点で待ち条件群の判定を終了する
				startCheck = true;
				// 判定が終了していないものがあっても条件が確定するので終了処理を行う
				isEnd = true;
			} else if (groupReturnValueList.contains(ReturnValue.NONE)) {
				// 時間を満たしていない
				// 先行ジョブが終了していない
				// セッション横断ジョブについてジョブ履歴範囲に存在しない場合
				// 終了処理は行わず待機するようにフラグを設定する
				isEnd = false;
			} else {
				startCheck = false;
				isEnd = true;
			}
			if (m_log.isTraceEnabled()) {
				m_log.trace(
						"checkStartCondition() : possibilityCheck is false . Reason : All job wait has not been achieved ."
								+ "sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);
			}
		}


		
		
		// 終了処理を行わない場合、待機する
		if (!isEnd) {
			return false;
		}

		// ジョブの終了処理が可能かつ
		// ジョブの実行が不可能かつ
		// 条件を満たさなければ終了する場合
		// 終了状態を異常で終了する
		if (isEnd && !startCheck && job.getUnmatchEndFlg().booleanValue()) {
			m_log.debug("checkStartCondition() : unmatch end flg is true. end job : " + " jobid : " + jobId
					+ " : status :" + sessionJob.getStatus());
			// 条件を満たさず終了の場合は、終了状態を異常とする。
			Integer endStatus = job.getUnmatchEndStatus();
			// 終了値を設定
			Integer endValue = job.getUnmatchEndValue();

			// 実行状態、終了状態、終了値、終了日時を設定
			setEndStatus(sessionId, jobunitId, jobId, StatusConstant.TYPE_END_UNMATCH, endStatus, endValue, null);
			// ジョブ終了時関連処理
			endJob(sessionId, jobunitId, jobId, null, false);
		}
		return startCheck;
	}

	/**
	 * 待ち条件群内（ジョブ終了）のチェック
	 * ジョブを指定する待ち条件についての判定を行う
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @param sessionJob
	 * @param job
	 * @param state
	 * @param wait
	 * @return state
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private ReturnValue checkWaitJobEnd(String sessionId, String jobunitId, String jobId,
			JobSessionJobEntity sessionJob, JobWaitInfoEntity wait)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		boolean typeIsJob = wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS
				|| wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE
				|| wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE;
		boolean typeIsCrossJob = wait.getId()
				.getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS
				|| wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE;

		// 待ち条件ジョブを取得
		JobSessionJobEntity targetSessionJob;
		// 待ち条件を満たしているかどうか
		ReturnValue returnValue = null;

		// 通常のジョブが待ち条件の場合
		if (typeIsJob) {
			targetSessionJob = QueryUtil.getJobSessionJobPK(sessionJob.getId().getSessionId(),
					wait.getId().getTargetJobunitId(), wait.getId().getTargetJobId());
			returnValue = JobSessionJobUtil.checkStartCondition(targetSessionJob, wait);
		}
		// セッション横断待ち条件の場合
		if (typeIsCrossJob) {
			m_log.info("CrossSessionJob exists : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId="
					+ jobId);
			// ジョブユニットID、ジョブID、ジョブ終了日時から、対象セッションジョブを取得
			List<JobSessionJobEntity> targetCrossSessionJobList = JobSessionJobUtil.searchCrossSessionJob(wait);
			returnValue = JobSessionJobUtil.checkStartCrossSessionCondition(targetCrossSessionJobList, wait);
		}

		if (returnValue == null) {
			HinemosUnknown e = new HinemosUnknown("checkWaitJobEnd() : returnValue is null.");
			m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage() + " sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);
			throw e;
		}

		return returnValue;
	}

	/**
	 * 待ち条件群内（ジョブ変数）のチェック
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param job
	 * @param state
	 * @param wait
	 * @return state
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 */
	static final String regex = "#\\[[a-zA-Z0-9-_:]+\\]";
	static final Pattern pattern = Pattern.compile(regex);
	private ReturnValue checkWaitJobParam(String sessionId, String jobunitId, JobInfoEntity job,
			JobWaitInfoEntity wait)
			throws JobInfoNotFound, HinemosUnknown, FacilityNotFound, InvalidRole {

		// ファシリティIDの文字列
		String decisionFacilityId = job.getFacilityId();
		// ファシリティIDが正規表現にマッチするか検証する
		if (pattern.matcher(decisionFacilityId).find()) {
			// ファシリティIDの置換
			decisionFacilityId = ParameterUtil.replaceSessionParameterValue(sessionId, decisionFacilityId,
					decisionFacilityId);

			// ファシリティIDが置換されているかどうか再度検証し、置換されていなければ次の判定処理まで待機
			if (pattern.matcher(decisionFacilityId).find()) {
				return ReturnValue.NONE;
			}
		}
		// 判定値1の置換後の文字列
		String replacementValue01 = "";
		// 判定値1を置換する
		replacementValue01 = replaceValue(sessionId, jobunitId, wait.getDecisionValue01(), decisionFacilityId);
		if(replacementValue01 == null){
			return ReturnValue.NONE;
		}

		boolean result;
		// 判定値2を置換する
		// 判定値2は条件がIN, NOT INの場合カンマ、コロンで複数指定が可能
		// それ以外の条件については置換後、一対一で比較する
		if (wait.getDecisionCondition() == DecisionObjectConstant.IN_NUMERIC
				|| wait.getDecisionCondition() == DecisionObjectConstant.NOT_IN_NUMERIC) {
			// 判定値2ジョブ変数用[置換前])
			List<String> stringValueList = wait.getStringValueList();
			// 判定値2(FromTo)(ジョブ変数用[置換前])
			List<String[]> stringValueRangeList = wait.getStringValueRangeList();
			// 判定値2(ジョブ変数用[置換後])
			List<String> replacementValueList = new ArrayList<String>();
			// 判定値2(FromTo)(ジョブ変数用[置換後])
			List<String[]> replacementValueRangeList = new ArrayList<String[]>();

			// カンマ区切りの値を置換する
			if (stringValueList.size() > 0) {
				for (String value : stringValueList) {
					String replacementValue = replaceValue(sessionId, jobunitId, value, decisionFacilityId);
					if (replacementValue == null) {
						return ReturnValue.NONE;
					}
					replacementValueList.add(replacementValue);
				}
			}
			// コロン区切りの値を置換する
			if (stringValueRangeList.size() > 0) {
				for (String[] value : stringValueRangeList) {
					String replacementValueMin = replaceValue(sessionId, jobunitId, value[0], decisionFacilityId);
					if (replacementValueMin == null) {
						return ReturnValue.NONE;
					}
					String replacementValueMax = replaceValue(sessionId, jobunitId, value[1], decisionFacilityId);
					if (replacementValueMax == null) {
						return ReturnValue.NONE;
					}
					String[] replacementValues = { replacementValueMin, replacementValueMax };
					replacementValueRangeList.add(replacementValues);
				}
			}
			// 置換後の値を比較する
			result = JobSessionJobUtil.checkJobMultiParamCondition(replacementValue01,
					replacementValueList, replacementValueRangeList, wait);
			m_log.debug("DecisionInfo " + wait.getDecisionCondition() + ", value01 : " + replacementValue01
					+ ", replacementValueList=" + replacementValueList + ", replacementValueList="
					+ replacementValueRangeList);
		} else {
			// 判定値2の置換後の文字列
			String replacementValue02 = "";
			replacementValue02 = replaceValue(sessionId, jobunitId, wait.getDecisionValue02(), decisionFacilityId);
			if (replacementValue02 == null) {
				return ReturnValue.NONE;
			}
			// 置換後の値を比較する
			result = JobSessionJobUtil.checkJobParamCondition(replacementValue01, replacementValue02,
					wait);
			m_log.debug("DecisionInfo " + wait.getDecisionCondition() + ", value01 : " + replacementValue01
					+ ", value02 : " + replacementValue02);
		}
		if(result){
			return ReturnValue.TRUE;
		}else{
			return ReturnValue.FALSE;
		}
	}

	/**
	 * 判定値のジョブ変数の置換を行う
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param wait
	 * @param decisionFacilityId
	 * @return
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 */
	private String replaceValue(String sessionId, String jobunitId, String value,
			String decisionFacilityId) throws JobInfoNotFound, HinemosUnknown, FacilityNotFound, InvalidRole {
		String replacementValue;
		// 判定値が正規表現にマッチするか検証する
		if (pattern.matcher(value).find()) {
			/*
			 * 判定値の置換
			 *  変数#[END_NUM:jobId]、変数#[RETURN:jobId:facilityId]ではジョブが終了していない場合に
			 *  文字列"null"で変換されるため、個々に判定する。
			 */
			// ジョブ変数
			replacementValue = ParameterUtil.replaceSessionParameterValue(sessionId, decisionFacilityId, value);
			// 変数#[END_NUM:jobId]
			try {
				replacementValue = ParameterUtil.replaceEndNumParameter(sessionId, jobunitId, replacementValue, true);
			} catch (JobInfoNotFound e) {
				// 置換しない
			}
			// 変数#[RETURN:jobId:facilityId]
			try {
				replacementValue = ParameterUtil.replaceReturnCodeParameter(sessionId, jobunitId, replacementValue, true);
			} catch (JobInfoNotFound e) {
				// 置換しない
			}
			// 判定値が置換されているかどうか再度検証し、置換されていなければ次の判定処理まで待機
			if (pattern.matcher(replacementValue).find()) {
				return null;
			}
		} else {
			replacementValue = value;
		}
		return replacementValue;
	}

	
	/**
	 * 待ち条件（セッション開始時の時間（分））のチェック
	 * @param sessionId
	 * @param startMinute
	 * @return true or false
	 * @throws JobInfoNotFound
	 */
	private boolean checkStartMinute(String sessionId, Integer startMinute) throws JobInfoNotFound {

		boolean timeCheck = false;

		//セッションIDから、セッションを取得
		JobSessionEntity session = QueryUtil.getJobSessionPK(sessionId);
		//セッションの開始時刻
		Long sessionDate = session.getScheduleDate();
		m_log.trace("sessionDate : " + sessionDate);

		Calendar work = HinemosTime.getCalendarInstance();
		work.setTimeInMillis(sessionDate);
		work.getTime();
		work.add(Calendar.MINUTE, startMinute);
		Long check = work.getTimeInMillis();
		timeCheck = check <= HinemosTime.currentTimeMillis();
		/*
		 * セッション開始時の時間（分）が実行されない場合は、
		 * checkDateMapに追加する。
		 */
		if (!timeCheck) {
			addCheckDate(sessionId, check);
		}

		return timeCheck;
	}

	/**
	 * 待ち条件（時刻）のチェック
	 * @param sessionId
	 * @param startTime
	 * @return true or false (ジョブセッション開始日 + 待ち条件(時刻) + TIMEZONE <= 現在日時)
	 * @throws JobInfoNotFound
	 */
	private boolean checkWaitTime(String sessionId, Long startTime) throws JobInfoNotFound {


		boolean timeCheck = false;

		//セッションIDから、セッションを取得
		JobSessionEntity session = QueryUtil.getJobSessionPK(sessionId);
		//セッションの開始時刻
		Long sessionDate = session.getScheduleDate();
		m_log.trace("sessionDate : " + sessionDate);

		//セッションの開始日時の00:00:00取得
		Calendar sessionCal = HinemosTime.getCalendarInstance();
		sessionCal.setTimeInMillis(sessionDate);
		sessionCal.set(Calendar.HOUR_OF_DAY, 0);
		sessionCal.set(Calendar.MINUTE, 0);
		sessionCal.set(Calendar.SECOND, 0);
		sessionCal.set(Calendar.MILLISECOND, 0);
		Date sessionDate0h = sessionCal.getTime();
		m_log.trace("sessionDate0h : " + sessionDate0h);

		/*
		 * ジョブセッション開始日 + 待ち条件(時刻) + TIMEZONE
		 * 例
		 * セッション開始日：2013/01/09 00:00:00
		 * 待ち条件（時刻）：40:00:00(1970/01/02 16:00:00)
		 *
		 * セッション開始日 +待ち条件(時刻) + TIMEZONE = 2013/01/10 16:00:00
		 */
		long overMidnight = sessionDate0h.getTime() + startTime + TIMEZONE;
		m_log.trace("overMidnight : " + new Date(overMidnight));

		//現在日時
		timeCheck = overMidnight <= HinemosTime.currentTimeMillis();
		m_log.trace("timeCheck : " + timeCheck);

		/*
		 * 待ち条件時刻が原因で実行できない場合は、checkDateMapに追加する。
		 */
		if (!timeCheck) {
			addCheckDate(sessionId, overMidnight);
		}
		return timeCheck;
	}

	/**
	 * 開始遅延処理を行います。
	 *
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @return true=操作あり, false=操作なし
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @return true: 遅延あり、false: 遅延なし
	 */
	private boolean checkStartDelayRecursive(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		m_log.debug("checkStartDelayRecursive() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		boolean delayCheck = false;

		//実行状態チェック
		if (sessionJob.getStatus() == StatusConstant.TYPE_WAIT
				|| sessionJob.getStatus() == StatusConstant.TYPE_RUNNING_QUEUE) {
			//開始遅延チェック
			delayCheck = checkStartDelaySub(sessionId, jobunitId, jobId);
		}

		//セッションIDとジョブIDから、直下のジョブを取得
		if (!jobSessionJobChildrenCache.containsKey(sessionId)) {
			makeJobSessionJobChildrenCache(sessionId, jobunitId);
		}
		List<String> jobSessionJobIdList = jobSessionJobChildrenCache.get(sessionId).get(jobunitId).get(jobId);
		if (jobSessionJobIdList == null) {
			m_log.trace("child job is null. " + sessionId + "," + jobunitId + "," + jobId);
			return delayCheck;
		}
		for (String childJobId : jobSessionJobIdList) {
			//開始遅延チェックメイン処理を行う（再帰呼び出し）
			checkStartDelayRecursive(sessionId, jobunitId, childJobId);
		}

		return delayCheck;
	}

	/**
	 * 開始遅延をチェックします。
	 *
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @return true=操作あり, false=操作なし
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	private boolean checkStartDelaySub(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		m_log.debug("checkStartDelaySub() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		ArrayList<Boolean> result = new ArrayList<Boolean>();
		boolean delayCheck = true;

		if(!job.getStartDelay().booleanValue()){
			return false;
		}
		//開始遅延が設定されている場合
		Long sessionDate = null;
		
		StringBuilder reason = new StringBuilder();

		if(job.getStartDelaySession().booleanValue()){
			//セッション開始後の時間が設定されている場合
			//セッション開始日時を取得
			JobSessionEntity session = QueryUtil.getJobSessionPK(sessionId);
			sessionDate = session.getScheduleDate();
			Calendar work = HinemosTime.getCalendarInstance();
			work.setTimeInMillis(sessionDate);
			work.getTime();
			work.add(Calendar.MINUTE, job.getStartDelaySessionValue());
			Long check = work.getTimeInMillis();
			Boolean startDelayCheck = check <= HinemosTime.currentTimeMillis();
			/*
			 * 開始遅延(セッション開始後の時間)が実行されない場合は、
			 * checkDateMapに追加する。
			 */
			if (!startDelayCheck) {
				addCheckDate(sessionId, check);
			}
			if (startDelayCheck) {
				reason.append(MessageConstant.TIME_AFTER_SESSION_START.getMessage()).append(" > ").append(job.getStartDelaySessionValue()).append("\n");
			}
			result.add(startDelayCheck);
		}

		if(job.getStartDelayTime().booleanValue()){
			//時刻が設定されている場合
			if(job.getStartDelayTimeValue() != null){
				//セッション開始日時を取得
				if(sessionDate == null){
					JobSessionEntity session = QueryUtil.getJobSessionPK(sessionId);
					sessionDate = session.getScheduleDate();
				}
				//セッションの開始日時の00:00:00取得
				Calendar sessionCal = HinemosTime.getCalendarInstance();
				sessionCal.setTimeInMillis(sessionDate);
				sessionCal.set(Calendar.HOUR_OF_DAY, 0);
				sessionCal.set(Calendar.MINUTE, 0);
				sessionCal.set(Calendar.SECOND, 0);
				sessionCal.set(Calendar.MILLISECOND, 0);
				Date sessionDate0h = sessionCal.getTime();
				m_log.trace("sessionDate0h : " + sessionDate0h);

				// ジョブセッション開始日 + 開始遅延(時刻) + TIMEZONE
				long startDelay = sessionDate0h.getTime() + job.getStartDelayTimeValue() + TIMEZONE;
				m_log.trace("himatagiDate : " + new Date(startDelay));

				//現在日時取得
				boolean startDelayCheck = startDelay <= HinemosTime.currentTimeMillis();
				m_log.trace("startDelayCheck : " + startDelayCheck);
				/*
				 * 開始遅延(時刻)が実行されない場合は、
				 * checkDateMapに追加する。
				 */
				if (!startDelayCheck) {
					addCheckDate(sessionId, startDelay);
				}
				if (startDelayCheck) {
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
					sdf.setTimeZone(HinemosTime.getTimeZone());
					reason.append(MessageConstant.TIMESTAMP.getMessage()).append(" > ").append(sdf.format(startDelay)).append("\n");
				}
				result.add(startDelayCheck);
			}else{
				result.add(false);
			}
		}

		//ANDまたはOR条件に一致するかチェック
		if(result.size() > 0){
			if(job.getStartDelayConditionType() == ConditionTypeConstant.TYPE_AND){
				//AND条件の場合
				delayCheck = true;
				for (Boolean flag : result) {
					if(!flag){
						delayCheck = false;
						break;
					}
				}
			}else{
				//OR条件の場合
				delayCheck = false;
				for (Boolean flag : result) {
					if(flag){
						delayCheck = true;
						break;
					}
				}
			}
		}else{
			delayCheck = false;
		}

		//開始遅延チェック結果が遅延の場合
		if(delayCheck){
			m_log.info("checkStartDelaySub: Detected a start delay."
						+ " job=[" + sessionId + ", " + jobunitId + ", " + jobId + "]"
						+ ", notify=" + job.getStartDelayNotify()
						+ ", operation=" + job.getStartDelayOperation()
						+ "(" + job.getStartDelayOperationType() + ")");

			//通知
			if(job.getStartDelayNotify().booleanValue()){
				//遅延通知状態を取得
				int flg = sessionJob.getDelayNotifyFlg();
				//遅延通知状態から通知済みフラグを取得
				int notifyFlg = DelayNotifyConstant.getNotify(flg);

				if(notifyFlg == DelayNotifyConstant.NONE || notifyFlg == DelayNotifyConstant.END){
					//通知済みフラグが「通知・操作なし」又は「終了遅延通知済み」の場合

					//通知処理
					new Notice().delayNotify(sessionId, jobunitId, jobId, true, reason.toString());

					if(notifyFlg == DelayNotifyConstant.NONE) {
						sessionJob.setDelayNotifyFlg(DelayNotifyConstant.START);
					} else if(notifyFlg == DelayNotifyConstant.END) {
						sessionJob.setDelayNotifyFlg(DelayNotifyConstant.START_AND_END);
					}
				}
			}

			// 操作
			if (job.getStartDelayOperation().booleanValue()) {
				int type = job.getStartDelayOperationType();
				if (type == OperationConstant.TYPE_STOP_SKIP) {
					sessionJob.setStatus(StatusConstant.TYPE_SKIP);
					sessionJob.setResult(DELAY_SKIP_RESULT);
				} else if (type == OperationConstant.TYPE_STOP_WAIT) {
					sessionJob.setStatus(StatusConstant.TYPE_RESERVING);
				}
			} else {
				// 開始遅延かつ、操作しない場合、次の定期チェックで待ち合わせ解除を行う
				if (sessionJob.getStatus() == StatusConstant.TYPE_WAIT &&
						sessionJob.getWaitCheckFlg() != null && sessionJob.getWaitCheckFlg()) {
					addWaitCheckJob(sessionId, new String[] { jobunitId, jobId });
				}
			}
		}
		return delayCheck;
	}

	/**
	 * 終了遅延処理を行います。
	 *
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @return true=操作あり, false=操作なし
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws FacilityNotFound 
	 */
	private boolean checkEndDelay(String sessionId, String jobunitId, String jobId)
			throws HinemosUnknown, JobInfoNotFound, InvalidRole, FacilityNotFound {
		m_log.debug("checkEndDelay() : sessionId=" + sessionId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		JobInfoEntity job = sessionJob.getJobInfoEntity();

		ArrayList<Boolean> result = new ArrayList<Boolean>();
		boolean delayCheck = true;
		
		// 遅延と判定するのに満たされた条件
		StringBuilder reason = new StringBuilder();

		//終了遅延が設定されていない場合
		if(!job.getEndDelay().booleanValue()){
			return false;
		}

		long sessionDate = -1;

		if(job.getEndDelaySession().booleanValue()){
			//セッション開始後の時間が設定されている場合

			//セッション開始日時を取得
			JobSessionEntity session = QueryUtil.getJobSessionPK(sessionId);
			sessionDate = session.getScheduleDate();
			Calendar work = HinemosTime.getCalendarInstance();
			work.setTimeInMillis(sessionDate);
			work.getTime();
			work.add(Calendar.MINUTE, job.getEndDelaySessionValue());
			Long check = work.getTimeInMillis();
			Boolean endDelayCheck = check <= HinemosTime.currentTimeMillis();
			/*
			 * 終了遅延(セッション開始後の時間)が実行されない場合は、
			 * checkDateMapに追加する。
			 */
			if (!endDelayCheck) {
				addCheckDate(sessionId, check);
			}
			if (endDelayCheck) {
				reason.append(MessageConstant.TIME_AFTER_SESSION_START.getMessage()).append(" > ").append(job.getEndDelaySessionValue()).append("\n");
			}
			result.add(endDelayCheck);
		}

		if(job.getEndDelayJob().booleanValue()
				&& sessionJob.getStartDate() != null){
			// ジョブ開始後の時間が設定されている場合(ジョブ繰り返し実行待機中など、ジョブが開始されていない場合は対象外)

			//ジョブ開始日時を取得
			Long startDate = sessionJob.getStartDate();
			Calendar work = HinemosTime.getCalendarInstance();
			work.setTimeInMillis(startDate);
			work.getTime();
			work.add(Calendar.MINUTE, job.getEndDelayJobValue());
			Long check = work.getTimeInMillis();
			Boolean endDelayCheck = check <= HinemosTime.currentTimeMillis();
			/*
			 * 終了遅延(セッション開始後の時間)が実行されない場合は、
			 * checkDateMapに追加する。
			 */
			if (!endDelayCheck) {
				addCheckDate(sessionId, check);
			}
			if (endDelayCheck) {
				reason.append(MessageConstant.TIME_AFTER_JOB_START.getMessage()).append(" > ").append(job.getEndDelayJobValue()).append("\n");
			}
			result.add(endDelayCheck);
		}

		if(job.getEndDelayTime().booleanValue()){
			//時刻が設定されている場合

			if(job.getEndDelayTimeValue() != null){
				//セッション開始日時を取得
				if(sessionDate < 0){
					JobSessionEntity session = QueryUtil.getJobSessionPK(sessionId);
					sessionDate = session.getScheduleDate();
				}
				//セッションの開始日時の00:00:00取得
				Calendar sessionCal = HinemosTime.getCalendarInstance();
				sessionCal.setTimeInMillis(sessionDate);
				sessionCal.set(Calendar.HOUR_OF_DAY, 0);
				sessionCal.set(Calendar.MINUTE, 0);
				sessionCal.set(Calendar.SECOND, 0);
				sessionCal.set(Calendar.MILLISECOND, 0);
				Date sessionDate0h = sessionCal.getTime();
				m_log.trace("sessionDate0h : " + sessionDate0h);

				// ジョブセッション開始日 + 終了遅延(時刻) + TIMEZONE
				long endDelay = sessionDate0h.getTime() + job.getEndDelayTimeValue() + TIMEZONE;
				m_log.trace("endDelayDate : " + new Date(endDelay));

				//現在日時取得
				boolean endDelayCheck = endDelay <= HinemosTime.currentTimeMillis();
				m_log.trace("endDelayCheck : " + endDelayCheck);
				/*
				 * 終了遅延(時刻)が実行されない場合は、
				 * checkDateMapに追加する。
				 */
				if (!endDelayCheck) {
					addCheckDate(sessionId, endDelay);
				}
				if (endDelayCheck) {
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
					sdf.setTimeZone(HinemosTime.getTimeZone());
					reason.append(MessageConstant.TIMESTAMP.getMessage()).append(" > ").append(sdf.format(endDelay)).append("\n");
				}
				result.add(endDelayCheck);
			}else{
				result.add(false);
			}
		}

		if(job.getEndDelayChangeMount().booleanValue()
				&& sessionJob.getStartDate() != null){
			//実行履歴からの変化量が設定されている場合(ジョブ繰り返し実行待機中など、ジョブが開始されていない場合は対象外)

			// 次回確認する時間
			long check = HinemosTime.currentTimeMillis();
			// 経過時間
			Long sessionTime = check - sessionJob.getStartDate();
			boolean endDelayCheck = false;
			if(job.getEndDelayChangeMountValue() != null){
				// 判定用データ取得
				List<Double> jobSessionChangeList = JobSessionChangeDataCache.getJobSessionChangeDataDoubleList(sessionJob);
				// 判定用データの最小件数
				Long dataCount = HinemosPropertyCommon.job_end_delay_change_mount_lower_limit.getNumericValue();
				if (jobSessionChangeList.size() < dataCount) {
					// 判定対象のデータが最小件数より下回る場合は、終了遅延としない
					m_log.debug("checkEndDelay():" + " sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId 
							+ ": The number of data is insufficient. : dataCount=" + jobSessionChangeList.size());
				} else {
					// 判定情報の取得
					Double average = null;
					Double standardDeviation = null;
					try {
						average = OperatorCommonUtil.getAverage(jobSessionChangeList);
						standardDeviation = OperatorCommonUtil.getStandardDeviation(jobSessionChangeList);
					} catch (HinemosArithmeticException e) {
						// 正常処理とする
						m_log.warn("checkEndDelay():" + " sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + "\n" + e.getMessage());
					} catch (HinemosIllegalArgumentException e) {
						// 正常処理とする
						m_log.info("checkEndDelay():" + " sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + "\n" + e.getMessage());					
					}
					m_log.debug("checkEndDelay():" + " sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId
							+ "\n" + ", jobSessionChangeList=" + jobSessionChangeList + ", average=" + average + ", standardDeviation=" + standardDeviation
							+ ", sigma=" + job.getEndDelayChangeMountValue());
					if (average == null || standardDeviation == null) {
						// 標準偏差がnullの場合は正常
					} else if (standardDeviation.doubleValue() == 0D
							&& sessionTime > average.doubleValue()) {
						// 標準偏差=0の場合
						// 値 > 平均値は遅延
							endDelayCheck = true;
							if (endDelayCheck) {
								reason.append(MessageConstant.JOB_CHANGE_MOUNT.getMessage()).append(" > ");
								reason.append((int)(average.doubleValue() / 1000 / 60));
								reason.append("\n");
							}
					} else {
						// 比較対象時間
						Double checkTime = OperatorChangeUtil.getStandardDeviation(average, standardDeviation, job.getEndDelayChangeMountValue());
						m_log.debug("checkEndDelay():" + " sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + "\n" 
								+ ", average=" + average + ", standardDeviation=" + standardDeviation + ", sigma=" + job.getEndDelayChangeMountValue()
								+ ", checkTime=" + checkTime + ", sessionTime=" + sessionTime);
						if (checkTime != null) {
							if (sessionTime.doubleValue() > checkTime) {
								endDelayCheck = true;
							} else {
								check = sessionJob.getStartDate() + checkTime.longValue();
							}
						}
						if (endDelayCheck) {
							reason.append(MessageConstant.JOB_CHANGE_MOUNT.getMessage()).append(" > ");
							reason.append((int)(checkTime / 1000 / 60));
							reason.append("\n");
						}
					}
				}
				/*
				 * 終了遅延(変化量)が実行されない場合は、
				 * checkDateMapに追加する。
				 */
				m_log.debug("checkEndDelay():"
						+ " sessionId=" + sessionId
						+ ", jobunitId=" + jobunitId
						+ ", jobId=" + jobId
						+ "\n" 
						+ ", endDelayCheck=" + endDelayCheck);
				if (!endDelayCheck) {
					addCheckDate(sessionId, check);
				}
				result.add(endDelayCheck);
			}else{
				result.add(false);
			}
		}

		//ANDまたはOR条件に一致するかチェック
		if(result.size() > 0){
			if(job.getEndDelayConditionType() == ConditionTypeConstant.TYPE_AND){
				//AND条件の場合
				delayCheck = true;
				for (Boolean flag : result) {
					if (!flag) {
						delayCheck = false;
						break;
					}
				}
			}else{
				//OR条件の場合
				delayCheck = false;
				for (Boolean flag : result) {
					if (flag) {
						delayCheck = true;
						break;
					}
				}
			}
		}else{
			delayCheck = false;
		}
		if (!delayCheck) {
			return false;
		}

		boolean operation = false;

		//通知
		if(job.getEndDelayNotify().booleanValue()){
			//遅延通知状態を取得
			int flg = sessionJob.getDelayNotifyFlg();
			//遅延通知状態から通知済みフラグを取得
			int notifyFlg = DelayNotifyConstant.getNotify(flg);
			if(notifyFlg == DelayNotifyConstant.NONE || notifyFlg == DelayNotifyConstant.START){
				//通知済みフラグが「通知・操作なし」又は「開始遅延通知済み」の場合
				//通知処理
				new Notice().delayNotify(sessionId, jobunitId, jobId, false, reason.toString());
				if(notifyFlg == DelayNotifyConstant.NONE) {
					sessionJob.setDelayNotifyFlg(DelayNotifyConstant.END);
				} else if(notifyFlg == DelayNotifyConstant.START) {
					sessionJob.setDelayNotifyFlg(DelayNotifyConstant.START_AND_END);
				}
			}
		}

		//操作
		if(job.getEndDelayOperation().booleanValue()){
			int type = job.getEndDelayOperationType();
			//遅延通知状態を取得
			int flg = sessionJob.getDelayNotifyFlg();
			if(type == OperationConstant.TYPE_STOP_AT_ONCE){
				//停止[コマンド]
				//遅延通知状態に操作済みフラグを設定
				int notifyFlg = DelayNotifyConstant.addOperation(
						flg, DelayNotifyConstant.STOP_AT_ONCE);
				sessionJob.setDelayNotifyFlg(notifyFlg);
				new OperateStopOfJob().stopJob(sessionId, jobunitId, jobId);
			}else if(type == OperationConstant.TYPE_STOP_SUSPEND){
				//停止[中断]
				//遅延通知状態から操作済みフラグを取得
				int operationFlg = DelayNotifyConstant.getOperation(flg);
				if(operationFlg != DelayNotifyConstant.STOP_SUSPEND ){
					//操作済みフラグが停止[中断]以外の場合
					//遅延通知状態に操作済みフラグを設定
					int notifyFlg = DelayNotifyConstant.addOperation(
							flg, DelayNotifyConstant.STOP_SUSPEND);
					sessionJob.setDelayNotifyFlg(notifyFlg);
					new OperateSuspendOfJob().suspendJob(sessionId, jobunitId, jobId);
				}
			}else if(type == OperationConstant.TYPE_STOP_SET_END_VALUE){
				//停止[状態指定]
				//遅延通知状態に操作済みフラグを設定
				int notifyFlg = DelayNotifyConstant.addOperation(
						flg, DelayNotifyConstant.STOP_SET_END_VALUE);
				sessionJob.setDelayNotifyFlg(notifyFlg);
				new OperateStopOfJob().stopJob(sessionId, jobunitId, jobId);
			}else if(type == OperationConstant.TYPE_STOP_SET_END_VALUE_FORCE){
				//停止[状態指定](強制)
				//遅延通知状態に操作済みフラグを設定
				int notifyFlg = DelayNotifyConstant.addOperation(
						flg, DelayNotifyConstant.STOP_SET_END_VALUE_FORCE);
				sessionJob.setDelayNotifyFlg(notifyFlg);
				new OperateStopOfJob().stopJob(sessionId, jobunitId, jobId);
			}
			operation = true;
		}
		
		//RPAシナリオジョブのスクリーンショット取得
		if (job.getJobType() == JobConstant.TYPE_RPAJOB
				&& job.getRpaScreenshotEndDelayFlg()) {
			new OperateRpaScreenshotOfJob().takeScreenshot(sessionId, jobunitId, jobId,
					RpaScreenshotTriggerTypeConstant.END_DELAY);
		}

		return operation;
	}

	/**
	 * 終了状態をチェックします
	 *
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @return 終了状態
	 * @throws JobInfoNotFound
	 */
	protected Integer checkEndStatus(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		m_log.debug("checkEndStatus() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			m_log.debug("checkEndStatus() : sessionId=" + sessionId + ", jobId=" + jobId);

			//セッションIDとジョブIDから、セッションジョブを取得
			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
			// ジョブ情報を取得
			JobInfoEntity job = sessionJob.getJobInfoEntity();

			ArrayList<Integer> statusList = new ArrayList<Integer>();

			if(sessionJob.hasSessionNode()){
				// ---- ノードが配下にあるジョブ

				boolean isNodeExists = false;

				//セッションジョブからセッションノードを取得
				for (JobSessionNodeEntity sessionNode : sessionJob.getJobSessionNodeEntities()) {
					if (sessionNode.getStatus() != StatusConstant.TYPE_NOT_MANAGED) {
						isNodeExists = true;
					}
					Integer endValue = sessionNode.getEndValue();
					if(endValue == null){
						continue;
					}
					Integer status = null;
					if (job.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB) {
						if (endValue == JobLinkSendReturnValue.SUCCESS.value()) {
							status = EndStatusConstant.TYPE_NORMAL;
						} else {
							status = EndStatusConstant.TYPE_ABNORMAL;
						}
					} else {
						status = JobSessionJobUtil.checkEndStatus(sessionJob, endValue);
					}
					statusList.add(status);

					//コマンドの実行が正常終了するまで順次リトライ、またはいずれかのノードの場合
					if ((job.getProcessMode() == ProcessingMethodConstant.TYPE_RETRY ||
							job.getProcessMode() == ProcessingMethodConstant.TYPE_ANY_NODE) &&
							status == EndStatusConstant.TYPE_NORMAL) {
						statusList.clear();
						statusList.add(EndStatusConstant.TYPE_NORMAL);
						break;
					}
				}
				//配下にセッションノードが存在しない場合
				if(!isNodeExists){
					statusList.clear();
					statusList.add(EndStatusConstant.TYPE_ABNORMAL);
				}
			}else{
				// ---- ジョブが配下にあるジョブ

				Integer endStatusCheck = sessionJob.getEndStausCheckFlg();
				if(endStatusCheck == null ||
						endStatusCheck == EndStatusCheckConstant.NO_WAIT_JOB){
					//待ち条件に指定されていないジョブのみで判定
					//セッションIDとジョブIDの直下のジョブを取得
					Collection<JobSessionJobEntity> collection = QueryUtil.getChildJobSessionJob(sessionId, jobunitId, jobId);

					for (JobSessionJobEntity childSessionJob : collection) {

						//待ち条件に指定されているかチェック
						Collection<JobWaitGroupInfoEntity> targetJobGroupList = null;
						targetJobGroupList = QueryUtil.getJobWaitGroupInfoByTargetJobId(
								sessionId, childSessionJob.getId().getJobId());
						if(targetJobGroupList.size() > 0){
							continue;
						}

						//待ち条件に指定されていないジョブ及びジョブネットを対象にする
						Integer endValue = childSessionJob.getEndValue();
						Integer status = JobSessionJobUtil.checkEndStatus(sessionJob, endValue);
						statusList.add(status);
					}
					//配下にセッションジョブが存在しない場合
					if(collection.size() == 0){
						statusList.clear();
						statusList.add(EndStatusConstant.TYPE_ABNORMAL);
					}
				}else{
					//全ジョブで判定

					//セッションIDとジョブIDの直下のジョブを取得
					Collection<JobSessionJobEntity> collection = QueryUtil.getChildJobSessionJob(sessionId, jobunitId, jobId);

					for (JobSessionJobEntity childSessionJob : collection) {

						Integer endValue = childSessionJob.getEndValue();
						Integer status = JobSessionJobUtil.checkEndStatus(sessionJob, endValue);
						statusList.add(status);
					}
					//配下にセッションジョブが存在しない場合
					if(collection.size() == 0){
						statusList.clear();
						statusList.add(EndStatusConstant.TYPE_ABNORMAL);
					}
				}
			}

			//終了判定を行う。
			Integer endStatus = EndJudgment.judgment(statusList);

			return endStatus;
		}
	}

	/**
	 * カレンダチェックを行います。
	 *
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @return true:実行可 false:実行不可
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 */
	private boolean checkCalendar(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		m_log.debug("checkCalendar() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		boolean check = false;

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		JobSessionEntity session = sessionJob.getJobSessionEntity();
		JobInfoEntity job = sessionJob.getJobInfoEntity();

		//カレンダをチェック
		if(job.getCalendar().booleanValue()){
			try {
				//カレンダによる実行可/不可のチェック
				if(new CalendarControllerBean().isRun(
						job.getCalendarId(),
						session.getScheduleDate())){
					check = true;
				}
			} catch (CalendarNotFound e) {
				// 何もしない
			} catch (HinemosUnknown e) {
				// 何もしない
			}
		}else{
			check = true;
		}

		//実行不可の場合
		if(!check){
			// 終了状態を設定
			Integer endStatus = job.getCalendarEndStatus();
			// 終了値を設定
			Integer endValue = job.getCalendarEndValue();
			//実行状態、終了状態、終了値、終了日時を設定
			setEndStatus(sessionId, jobunitId, jobId, StatusConstant.TYPE_END_CALENDAR,
					endStatus, endValue, null);
			//ジョブ終了時関連処理
			endJob(sessionId, jobunitId, jobId, null, false);
		}
		return check;
	}

	/**
	 * 終了状態を設定します。
	 *
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @param status 実行状態
	 * @param endStatus 終了状態
	 * @param result 結果
	 * @throws JobInfoNotFound
	 */
	private void setEndStatus(
			String sessionId,
			String jobunitId,
			String jobId,
			Integer status,
			Integer endStatus,
			Integer endValue,
			String result) throws JobInfoNotFound, InvalidRole {
		m_log.debug("setEndStaus() : sessionId=" + sessionId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		// ジョブ情報の取得
		JobInfoEntity jobInfo = sessionJob.getJobInfoEntity();
		//実行状態を設定
		sessionJob.setStatus(status);
		//終了状態を設定
		Integer preEndStatus = sessionJob.getEndStatus();
		sessionJob.setEndStatus(endStatus);
		//終了値を設定
		if (endValue != null) {
			sessionJob.setEndValue(endValue);
		} else {
			if(Integer.valueOf(EndStatusConstant.TYPE_NORMAL).equals(endStatus)){
				sessionJob.setEndValue(jobInfo.getNormalEndValue());
			}else if(Integer.valueOf(EndStatusConstant.TYPE_WARNING).equals(endStatus)){
				sessionJob.setEndValue(jobInfo.getWarnEndValue());
			}else if(Integer.valueOf(EndStatusConstant.TYPE_ABNORMAL).equals(endStatus)){
				sessionJob.setEndValue(jobInfo.getAbnormalEndValue());
			}
		}
		//終了日時を設定
		sessionJob.setEndDate(HinemosTime.currentTimeMillis());
		// ジョブ履歴用キャッシュ更新
		JobSessionChangeDataCache.add(sessionJob);
		// 収集データ更新
		CollectDataUtil.put(sessionJob);
		//結果を設定
		sessionJob.setResult(result);

		//通知処理
		//状態が変わったときのみ通知する
		if (preEndStatus == null || !preEndStatus.equals(endStatus)) {
			new Notice().notify(sessionId, jobunitId, jobId, endStatus);
		}
	}

	/**
	 * ジョブ終了時関連処理を行います。
	 *
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 */
	public void endJob(String sessionId, String jobunitId, String jobId, String result, boolean normalEndFlag)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		m_log.debug("endJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", result=" + result + ", normalEndFlag=" + normalEndFlag);

		// キャッシュを削除
		removeJobSessionJobChildrenCache(sessionId, jobunitId, jobId);

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			m_log.info("endJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);
			//セッションIDとジョブIDから、セッションジョブを取得
			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
			JobInfoEntity jobInfo = sessionJob.getJobInfoEntity();

			////////// 状態遷移、通知 //////////
			if (normalEndFlag){
				//終了状態を判定し、終了状態と終了値を設定
				Integer endStatus = checkEndStatus(sessionId, jobunitId, jobId);
				if (jobInfo.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB) {

					if (endStatus == EndStatusConstant.TYPE_NORMAL) {
						// 終了状態は[送信成功]-[終了値]と[終了状態]タブから求める
						endStatus = JobSessionJobUtil.checkEndStatus(sessionJob, jobInfo.getSuccessEndValue());
						//　実行状態、終了状態、終了値、終了日時を設定
						setEndStatus(sessionId, jobunitId, jobId, StatusConstant.TYPE_END,
								endStatus, jobInfo.getSuccessEndValue(), result);
					} else {
						if (jobInfo.getFailureOperation() == OperationConstant.TYPE_STOP_SUSPEND) {
							// 中断
							new OperateSuspendOfJob().suspendJob(sessionId, jobunitId, jobId);
						} else if (jobInfo.getFailureOperation() == OperationConstant.TYPE_STOP_SET_END_VALUE ||
									jobInfo.getFailureOperation() == OperationConstant.TYPE_STOP_SET_END_VALUE_FORCE) {
							// 状態指定
							setEndStatus(sessionId, jobunitId, jobId, StatusConstant.TYPE_END, 
									jobInfo.getFailureEndStatus(), jobInfo.getFailureEndValue(), result);
						}
					}
				} else if (jobInfo.getJobType() == JobConstant.TYPE_JOB) {
					boolean isFailureOperation = false;
					for (JobSessionNodeEntity sessionNode : sessionJob.getJobSessionNodeEntities()) {
						if (sessionNode.getStatus().equals(StatusConstant.TYPE_END_FAILED_OUTPUT)) {
							isFailureOperation = true;
						}
					}

					//ファイル出力に失敗時かつ状態変更を設定していた場合は終了(ファイル出力)に遷移
					if (isFailureOperation) {
						for (JobOutputInfoEntity outputEnt : jobInfo.getJobOutputInfoEntities()) {
							if (outputEnt.getFailureOperationFlg() &&
									(outputEnt.getFailureOperationType().equals(OperationConstant.TYPE_STOP_SET_END_VALUE) ||
									outputEnt.getFailureOperationType().equals(OperationConstant.TYPE_STOP_SET_END_VALUE_FORCE))) {
								setEndStatus(sessionId, jobunitId, jobId, StatusConstant.TYPE_END_FAILED_OUTPUT, outputEnt.getFailureOperationEndStatus(), outputEnt.getFailureOperationEndValue(), result);
								break;
							}
						}
					} else {
						//実行状態、終了状態、終了値、終了日時を設定
						setEndStatus(sessionId, jobunitId, jobId, StatusConstant.TYPE_END, endStatus, null, result);
					}
				} else {
					//実行状態、終了状態、終了値、終了日時を設定
					setEndStatus(sessionId, jobunitId, jobId, StatusConstant.TYPE_END, endStatus, null, result);
				}
			}

			////////// 繰り返し実行を判定 //////////
			Boolean jobRetryFlg = sessionJob.getJobInfoEntity().getJobRetryFlg();
			if (jobRetryFlg != null && jobRetryFlg && JobSessionJobUtil.checkRetryContinueCondition(sessionJob)
					&& (sessionJob.getStatus() == StatusConstant.TYPE_END
					|| sessionJob.getRetryWaitStatus().equals(RetryWaitStatusConstant.WAIT))) {

				// ジョブ定期実行により、一定間隔で以下待ちチェック処理を呼ばれる想定の実装としている

				// 自身と配下を待機状態に戻して(すぐに実行中に遷移させるわけだが、既存コードを尊重してそのままとする)、
				// 各種日時フラグをリセットする
				JobSessionJobUtil.resetJobStatusRecursive(sessionJob, false);

				// 初回のみリトライ待ち開始時間として、現在時刻を記録しておく
				long currentTime = HinemosTime.currentTimeMillis();
				if (sessionJob.getRetryWaitStatus().equals(RetryWaitStatusConstant.NONE)) {
					JobSessionJobUtil.setRetryJobStatusRecursive(sessionJob, RetryWaitStatusConstant.WAIT);
					sessionJob.setRetryWaitStartTime(currentTime);
				}

				// リトライ待ち開始 から 待ち時間分経過していれば、ジョブを再実行する
				long waitStartTime = sessionJob.getRetryWaitStartTime();
				long intervalMillis = 60L * 1000 * sessionJob.getJobInfoEntity().getJobRetryInterval();
				if ((waitStartTime + intervalMillis) <= currentTime) {
					// 自身を再実行する
					m_log.info("Retry job : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId
							+ ", runCount=" + sessionJob.getRunCount());
					JobSessionJobUtil.setRetryJobStatusRecursive(sessionJob, RetryWaitStatusConstant.NONE);
					sessionJob.setRetryWaitStartTime(null);
					changeToRunning(sessionJob);
				} else {
					// ジョブをリトライ待ち中の状態に変更
					// 初回（waitStartTime == currentTime）はメッセージをセットする
					Integer interval = sessionJob.getJobInfoEntity().getJobRetryInterval();
					JobSessionJobUtil.toRetryWaitingStatusRecursive(sessionJob, (waitStartTime == currentTime), interval);
				}

				return;  // ここで終了し、後続ジョブを実行しない
			}

			////////// 待ち条件の処理 //////////
			// 終了ジョブ(endJobメソッドの引数)を待ち条件に指定しているジョブの処理
			Collection<JobWaitGroupInfoEntity> collection;
			collection = QueryUtil.getJobWaitGroupInfoByTargetJobId(sessionId, jobId);
			ArrayList<JobSessionJobEntity> targetJobList = new ArrayList<JobSessionJobEntity>();
			for (JobWaitGroupInfoEntity waitGroup : collection) {
				if (waitGroup.getJobWaitInfoEntities() == null || waitGroup.getJobWaitInfoEntities().size() == 0) {
					continue;
				}
				for (JobWaitInfoEntity wait : waitGroup.getJobWaitInfoEntities()) {
					if (wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE
							&& wait.getId().getTargetJobId().equals(jobId)) {
						// 待ち条件「ジョブ（戻り値）」については、先行ジョブの実行対象が１ノードである場合のみ使用可能とする
						if (sessionJob.getJobSessionNodeEntities().size() > 1) {
							// FIXME 不具合#6713 INTERNALイベントで出力する必要がある
							// INTERNALイベント通知
							// AplLogger.put(InternalIdCommon.JOB_SYS_032,
							//		new String[] { wait.getId().getJobId(), jobId, sessionId });
							m_log.warn("A job whose execution target is a scope to which multiple nodes are assigned is set in the wait condition \"Job (return code)\"."
									+ ": sessionId=" + sessionId
									+ ", jobId=" + wait.getId().getJobId()
									+ ", target jobId=" + jobId);
						}
					}

					// セッションIDとジョブIDから、セッションジョブを取得
					JobSessionJobEntity targetSessionJob = QueryUtil.getJobSessionJobPK(wait.getId().getSessionId(),
							wait.getId().getJobunitId(), wait.getId().getJobId());

					if (targetJobList.contains(targetSessionJob)) {
						m_log.debug("duplicate " + targetSessionJob.getId().toString());
					} else {
						targetJobList.add(targetSessionJob);
					}
				}
			}
			// 「後続ジョブは１つだけ実行する」フラグをチェック
			JobInfoEntity currentJobInfo = sessionJob.getJobInfoEntity();
			Boolean exclusiveBranchFlg = currentJobInfo.getExclusiveBranchFlg();
			if (exclusiveBranchFlg == null || exclusiveBranchFlg == false) {
				for (JobSessionJobEntity targetSessionJob : targetJobList) {
					String startSessionId = targetSessionJob.getId().getSessionId();
					String startJobUnitId = targetSessionJob.getId().getJobunitId();
					String startJobId = targetSessionJob.getId().getJobId();
					int status = targetSessionJob.getStatus();
					if(status == StatusConstant.TYPE_WAIT || status == StatusConstant.TYPE_SKIP) {
						// 先行ジョブの終了が確定しているので、runningQueueに反映させるため更新
						jtm.addCallback(new RefreshRunningQueueAfterCommitCallback());
						//実行状態が待機の場合
						//ジョブ開始処理を行う
						startJob(startSessionId, startJobUnitId, startJobId);
					}
				}
			} else {
				//後続ジョブを１つだけ実行する
				m_log.info("ExclusiveBranch job: sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);
				// 先行ジョブに設定されている後続ジョブ実行優先度を取得する
				List<JobNextJobOrderInfoEntity> jobNextJobOrderInfoEntities = QueryUtil.getJobNextJobOrderInfoEntityFindBySessionIdJobunitIdJobId(sessionId, jobunitId, jobId);
				JobSessionJobEntity startJobSessionJob = selectExclusiveBranchJob(targetJobList, jobNextJobOrderInfoEntities);
				Integer endStatus = currentJobInfo.getExclusiveBranchEndStatus(); 
				Integer endValue = currentJobInfo.getExclusiveBranchEndValue(); 
				doExclusiveBranch(startJobSessionJob, targetJobList, endStatus, endValue);
			}

			////////// ジョブキューが設定されたジョブの場合は、キューから自身を取り除く。 //////////
			// キュー制限超過で終了した場合は、キューに入っていないことは確実なのでスキップ
			if (sessionJob.getStatus() != StatusConstant.TYPE_END_QUEUE_LIMIT) {
				String queueId = sessionJob.getJobInfoEntity().getQueueIdIfEnabled();
				if (queueId != null) {
					try {
						JobQueue queue = Singletons.get(JobQueueContainer.class).get(queueId);
						queue.remove(sessionId, jobunitId, jobId);
					} catch (JobQueueNotFoundException e) {
						// キューが存在しないとしても、結果として"キューからの除去"という目的は果たせているので、ログだけ出して無視する。
						m_log.info("endJob: " + e);
					}
				}
			}
			
			////////// 親ジョブに対してendJob()を実行する。 //////////
			//親ジョブのジョブIDを取得
			String parentJobunitId = null;
			String parentJobId = null;
			QueryUtil.getJobSessionJobPK(sessionId, sessionJob.getParentJobunitId(), sessionJob.getParentJobId());
			parentJobunitId = sessionJob.getParentJobunitId();
			parentJobId = sessionJob.getParentJobId();
			//同一階層のジョブが全て完了したかチェック
			boolean endAll = true;
			for (JobSessionJobEntity sessionJob1 : QueryUtil.getChildJobSessionJob(sessionId, parentJobunitId, parentJobId)) {
				// 待ち条件を設定していないジョブのみを対象とする
				if (HinemosPropertyCommon.job_end_criteria.getBooleanValue()) {
					//待ち条件に指定されているかチェック
					Collection<JobWaitGroupInfoEntity> targetJobWaitGroupList = null;
					targetJobWaitGroupList = QueryUtil.getJobWaitGroupInfoByTargetJobId(
							sessionId, sessionJob1.getId().getJobId());
					if(targetJobWaitGroupList.size() > 0){
						continue;
					}
				}
				//実行状態が終了または変更済以外の場合、同一階層のジョブは未完了
				if(!StatusConstant.isEndGroup(sessionJob1.getStatus())){
					endAll = false;
					break;
				}
			}
			if(!endAll){
				return;
			}
			if(CreateJobSession.TOP_JOB_ID.equals(parentJobId)){
				// セッションジョブ完了時
				// JOB_FORCE_CHECKクリアする
				if (checkRemoveForceCheck(sessionId)) {
					if (m_log.isDebugEnabled()) {
						m_log.debug("clear cache " + AbstractCacheManager.KEY_JOB_FORCE_CHECK + " : sessionId="
								+ sessionId);
					}
				}
				// 待ち合わせチェックマップをクリアする
				clearWaitCheckMap(sessionId);
				// 定期ジョブチェックマップをクリアする
				removeCheckDate(sessionId);
			} else {
				//同一階層のジョブが全て完了の場合
				//セッションIDとジョブIDから、セッションジョブを取得
				//ジョブ終了時関連処理（再帰呼び出し）
				endJob(sessionId, parentJobunitId, parentJobId, null, true);
			}
		}
	}
	
	/**
	 * 待機中の後続ジョブの中から優先度に従って排他分岐で実行すべきジョブを調べて返します。
	 *
	 * @param nextJobList 後続ジョブのJobSessionJobEntityのリスト
	 * @param jobNextJobOrderInfoEntities 後続ジョブ優先度設定のリスト
	 * @return 実行すべき待機中の後続ジョブ(対象のジョブが無い場合はnullを返す)
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 */
	private JobSessionJobEntity selectExclusiveBranchJob(List<JobSessionJobEntity> nextJobList, List<JobNextJobOrderInfoEntity> jobNextJobOrderInfoEntities)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		//後続ジョブが存在しない場合はnullを返す
		if (nextJobList.size() == 0) {
			return null;
		}
		//ジョブユニット内の後続ジョブとその優先度のHashMap
		HashMap<String, Integer> jobSessionJobPriorityMap = new HashMap<>();
		for (JobNextJobOrderInfoEntity order: jobNextJobOrderInfoEntities) {
			jobSessionJobPriorityMap.put(order.getId().getNextJobId(), order.getOrder());
		}

		//優先度が設定されていないジョブは優先度を最も低くする(MAX_VALUE)
		Comparator<JobSessionJobEntity> tmpComparator = Comparator.comparing(
			targetSessionJob -> jobSessionJobPriorityMap.getOrDefault(targetSessionJob.getId().getJobId(), Integer.MAX_VALUE));
		//優先度が設定されていないジョブはJOB_IDの昇順の優先度とする
		Comparator<JobSessionJobEntity> orderComparator = tmpComparator.thenComparing(
			targetSessionJob -> targetSessionJob.getId().getJobId());
		//後続ジョブを優先度順にソート
		nextJobList.sort(orderComparator);

		for (JobSessionJobEntity targetSessionJob : nextJobList) {
			String startSessionId = targetSessionJob.getId().getSessionId();
			String startJobunitId = targetSessionJob.getId().getJobunitId();
			String startJobId = targetSessionJob.getId().getJobId();
			
			//既に後続ジョブが終了している場合は待ち条件判定を行わない
			if (targetSessionJob.getStatus() == StatusConstant.TYPE_END) {
				return targetSessionJob;
			}

			//排他分岐の条件判定では時刻とセッション開始後の時間の待ち条件は除外して判定を行う
			if(checkWaitCondition(startSessionId, startJobunitId, startJobId, true /* skipTimeCondition=true */) &&
				checkCalendar(startSessionId, startJobunitId, startJobId) &&
				targetSessionJob.getStatus() == StatusConstant.TYPE_WAIT) {
				//実行する後続ジョブを返す
				return targetSessionJob;
			}
		}
		//待ち条件を満たす後続ジョブがない場合、優先度の最も高いジョブを返す
		//(優先度の最も高いジョブだけが将来実行されるようにする)
		return nextJobList.get(0);
	}
	
	/**
	 * 排他分岐において後続ジョブから１つのジョブを実行し、その他のジョブは終了させます。
	 *
	 * @param startJobSessionJob 実行する後続ジョブ
	 * @param nextJobList 後続ジョブのJobSessionJobEntityのリスト
	 * @param endStatus 実行されなかった後続ジョブの終了状態
	 * @param endValue 実行されなかった後続ジョブの終了値
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 */
	private void doExclusiveBranch(JobSessionJobEntity startJobSessionJob, List<JobSessionJobEntity> nextJobList, Integer endStatus, Integer endValue) 
			throws JobInfoNotFound, HinemosUnknown, InvalidRole, FacilityNotFound {
		//実行できる後続ジョブが無いときは何も行わない
		if (startJobSessionJob == null) {
			return;
		}
		String startSessionId = startJobSessionJob.getId().getSessionId();
		String startJobunitId = startJobSessionJob.getId().getJobunitId();
		String startJobId = startJobSessionJob.getId().getJobId();
		//ジョブ開始処理を行う
		startJob(startSessionId, startJobunitId, startJobId);
		m_log.info("ExclusiveBranch next job started : sessionId=" + startSessionId + ", jobunitId=" + startJobunitId + ", jobId=" + startJobId);
		//後続ジョブを１つ実行したらそれ以外のジョブを先行ジョブの設定基づいて終了させる
		//実行しなかったジョブを終了させる
		for(JobSessionJobEntity abort: nextJobList){
			String abortSessionId = abort.getId().getSessionId();
			String abortJobunitId = abort.getId().getJobunitId();
			String abortJobId = abort.getId().getJobId();
			if (abortSessionId.equals(startSessionId) &&
				abortJobunitId.equals(startJobunitId) &&
				abortJobId.equals(startJobId)) {
				continue;
			}
			//カレンダによって終了状態が設定されたもの等は処理しない
			if(abort.getStatus() != StatusConstant.TYPE_WAIT) {
				continue;
			}
			setEndStatus(
				abortSessionId, abortJobunitId, abortJobId,
				StatusConstant.TYPE_END_EXCLUSIVE_BRANCH, endStatus, endValue, null
			);
			m_log.info("ExclusiveBranch next job aborted : sessionId=" + abortSessionId + ", jobunitId=" + abortJobunitId + ", jobId=" + abortJobId);
		}
	}

	/**
	 * 終了していないジョブのタイムアウトをチェックします。
	 * 
	 * @param pk
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 */
	public void checkJobTimeout(JobSessionJobEntityPK pk)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		m_log.debug("checkJobTimeout() : " + pk.toString());
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(pk.getSessionId(), pk.getJobunitId(), pk.getJobId());

		if (sessionJob.getStatus() != StatusConstant.TYPE_RUNNING) {
			// ジョブの実行状態が実行中でなければ実行しない（タイミング次第で到達する）
			m_log.info("checkJobTimeout() : skip. " + sessionJob.getId().toString() + ", status="
					+ StatusConstant.typeToMessageCode(sessionJob.getStatus()));
			return;
		}

		// ジョブ情報
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		if (!job.getFailureEndFlg().booleanValue()) {
			m_log.debug("checkJobTimeout() : Timeout is invalid.");
			return;
		}
		if (job.getFailureWaitTime() == null || job.getFailureWaitTime() < 1) {
			// 通常は到達しない
			m_log.warn("checkJobTimeout() : WaitTime is null or illegal.");
			return;
		}

		// ジョブの開始日時を取得
		long startDate = sessionJob.getStartDate();
		Calendar work = HinemosTime.getCalendarInstance();
		work.setTimeInMillis(startDate);
		work.add(Calendar.MINUTE, job.getFailureWaitTime());
		Long check = work.getTimeInMillis();
		// タイムアウトチェック
		if (check <= HinemosTime.currentTimeMillis()) {
			m_log.info("checkJobTimeout() : Job is timed out. " + sessionJob.getId().toString());

			if (job.getJobType() == JobConstant.TYPE_FILECHECKJOB) {
				// ノードの停止処理を実行する
				boolean allNodeEnded = new JobSessionNodeImpl().endAllNodeByJobTimeout(sessionJob);

				if (allNodeEnded) {
					// 全てのノードが停止している場合はジョブ終了時関連処理を行う
					Integer endStatus = JobSessionJobUtil.checkEndStatus(sessionJob, job.getFailureEndValue());
					setEndStatus(sessionJob.getId().getSessionId(), sessionJob.getId().getJobunitId(),
							sessionJob.getId().getJobId(), StatusConstant.TYPE_END, endStatus, null, null);
					endJob(sessionJob.getId().getSessionId(), sessionJob.getId().getJobunitId(),
							sessionJob.getId().getJobId(), null, false);
				}
			} else {
				// 取得時にジョブ種別を指定するので通常到達しない
				m_log.error("checkJobTimeout() : Invalid JobType. jobType=" + job.getJobType());
			}
		}
	}
}
