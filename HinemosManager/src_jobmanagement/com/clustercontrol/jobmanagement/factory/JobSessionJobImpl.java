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
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
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
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
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
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobNextJobOrderInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobStartJobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobStartParamInfoEntity;
import com.clustercontrol.jobmanagement.util.JobSessionChangeDataCache;
import com.clustercontrol.jobmanagement.util.JobSessionJobUtil;
import com.clustercontrol.jobmanagement.util.ParameterUtil;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

public class JobSessionJobImpl {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( JobSessionJobImpl.class );

	/** タイムゾーン*/
	private static final long TIMEZONE = HinemosTime.getTimeZoneOffset();

	private static final ILock _lock;
	
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


	/**
	 * チェック時刻を取得する
	 * 
	 * @param sessionId セッションID
	 * @return チェック時刻
	 */
	public static Long getCheckDate(String sessionId) {
		return checkTimeMap.get(sessionId);
	}

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
	 * 待ち合わせチェックジョブを空にします。
	 */
	public void clearWaitCheckMap(String sessionId) {
		List<String[]> waitCheckJobIdList = waitCheckJobMap.get(sessionId);
		if (waitCheckJobIdList != null) {
			m_log.info("clearWaitCheckJob " + sessionId);
			waitCheckJobMap.put(sessionId, new ArrayList<String[]>());
		}
	}

	/**
	 * ジョブ開始処理メイン1を行います。
	 *
	 * @param sessionId
	 * @param jobId
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws FacilityNotFound 
	 */
	public void startJob(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, HinemosUnknown, InvalidRole, FacilityNotFound {
		m_log.debug("startJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		//実行状態チェック
		if(sessionJob.getStatus() == StatusConstant.TYPE_WAIT){
			//待機中の場合
			//開始条件とカレンダをチェックする
			if(checkWaitCondition(sessionId, jobunitId, jobId) &&
					checkCalendar(sessionId, jobunitId, jobId) &&
					sessionJob.getStatus() == StatusConstant.TYPE_WAIT){
				//実行状態に遷移した場合は、1分後に終了遅延のチェックをする必要がある。
				addForceCheck(sessionId);
				//実行状態を実行中にする
				sessionJob.setStatus(StatusConstant.TYPE_RUNNING);
				//開始・再実行日時を設定
				sessionJob.setStartDate(HinemosTime.currentTimeMillis());
				//実行回数をインクリメント
				sessionJob.setRunCount(sessionJob.getRunCount() + 1);
				//通知処理
				new Notice().notify(sessionId, jobunitId, jobId, EndStatusConstant.TYPE_BEGINNING);
				if(sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOB
						|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_APPROVALJOB
						|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_MONITORJOB){
					//ノードへの実行指示
					new JobSessionNodeImpl().startNode(sessionId, jobunitId, jobId);
				}else{
					//配下のジョブ開始処理（再帰呼び出し）
					startJob(sessionId, jobunitId, jobId);
				}
			} else {
				//実行できなかった場合は開始遅延チェック
				Boolean delayCheck = checkStartDelayRecursive(sessionId, jobunitId, jobId);
				//待機中ジョブがジョブ変数、又はセッション横断待ち条件を持つ場合、
				//定期チェックで待ち合わせ解除を確認する
				if(!delayCheck){
					//開始遅延の操作が行われなかった場合のみ定期チェックに登録する
					if (sessionJob.getWaitCheckFlg() != null && sessionJob.getWaitCheckFlg()) {
						addWaitCheckJob(sessionId, new String[] {sessionJob.getId().getJobunitId(), sessionJob.getId().getJobId()});
					}
				}
			}
		} else if(sessionJob.getStatus() == StatusConstant.TYPE_RUNNING){
			//実行中の場合
			if(!checkEndDelay(sessionId, jobunitId, jobId)) {
				//遅延操作されない場合は、下位のジョブツリーを見に行く。
				Collection<JobSessionJobEntity> collection =
						QueryUtil.getChildJobSessionJob(sessionId, jobunitId, jobId);
				for (JobSessionJobEntity execJob : collection){
					String childJobId = execJob.getId().getJobId();
					//ジョブ開始処理
					startJob(sessionId, jobunitId, childJobId);
				}
			}
			if(sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOB
					|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_APPROVALJOB
					|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_MONITORJOB){
				//ノードへの実行指示(終了していたらendJobを実行)
				// ここはジョブを中断にして、ノード詳細で終了した後に、ジョブの中断解除をしたら、
				// RUNNINGのままで止まってしまう。
				// それを回避するために下記の実装を加える。
				if (new JobSessionNodeImpl().startNode(sessionId, jobunitId, jobId)) {
					endJob(sessionId, jobunitId, jobId, "", true);
				}
			}
		} else if(sessionJob.getStatus() == StatusConstant.TYPE_SKIP){
			//スキップの場合
			//開始条件をチェックする
			Integer endStatus = 0;
			Integer endValue = 0;
			Integer status = 0;
			JobInfoEntity job = sessionJob.getJobInfoEntity();
			if (job.getStartDelay().booleanValue() &&
					job.getStartDelayOperation().booleanValue() &&
					job.getStartDelayOperationType() == OperationConstant.TYPE_STOP_SKIP) {
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
			if(checkWaitCondition(sessionId, jobunitId, jobId)){
				//実行状態、終了状態、終了値、終了日時を設定
				setEndStatus(sessionId, jobunitId, jobId, status, endStatus, endValue, null);
				//ジョブ終了時関連処理
				endJob(sessionId, jobunitId, jobId, null, false);
			}
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

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		//待機中の場合
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		//待ち条件ジョブを取得
		Collection<JobStartJobInfoEntity> startJobs = job.getJobStartJobInfoEntities();
		ArrayList<Boolean> jobResult = new ArrayList<Boolean>();

		//statusCheck 待ち条件判定を行うかを表すフラグ
		//AND条件の場合　実行中の先行ジョブがある場合 :false　全ての先行ジョブが終了している場合 :true
		//OR条件の場合 :true
		//先行ジョブが存在しない場合（時刻のみの制御）：true
		boolean statusCheck = true;

		//startCheck 待ち条件チェックの結果、ジョブを実行するかを表すフラグ
		//AND条件の場合　全ての待ち条件が満たされている場合：ture
		//OR条件の場合　待ち条件のうちどれか一つでも満たされている場合：true
		boolean startCheck = true;

		//allEndCheck 先行ジョブが全て終了しているかを表すフラグ
		boolean allEndCheck = true;

		//possibilityCheck 実行可能性を表すフラグ
		//以下の場合、実行可能性なし
		//ANDの場合：待ち条件ジョブの実行結果に1つでもNGがある場合
		//ORの場合：待ち条件がジョブのみで、全てのジョブが終了し、実行結果がNGである場合
		boolean possibilityCheck = true;

		//待ち条件変数判定を取得
		List<JobStartParamInfoEntity> jobStartParamInfonList = job.getJobStartParamInfoEntities();

		//replaceCheck ジョブ変数置換チェックの結果、ジョブを実行するかを表すフラグ
		boolean replaceCheck = true;

		//待ち条件が設定されていない場合
		if((startJobs == null || startJobs.size() == 0)
				&& job.getStartTime() == null
				&& job.getStartMinute() == null
				&& (jobStartParamInfonList ==null || jobStartParamInfonList.size() == 0)){
			return true;
		}

		//待ち条件ジョブ判定
		boolean crossSessionExists = false;
		for(JobStartJobInfoEntity startJob: startJobs) {
			//待ち条件ジョブを取得
			JobSessionJobEntity targetSessionJob;
			ReturnValue ok;  //待ち条件を満たしているかどうか
			if (startJob.getId().getTargetJobType() != JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS 
				&& startJob.getId().getTargetJobType() != JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE){
				//通常の待ち条件の場合
				targetSessionJob = QueryUtil.getJobSessionJobPK(
						sessionJob.getId().getSessionId(),
						startJob.getId().getTargetJobunitId(),
						startJob.getId().getTargetJobId());
				ok = JobSessionJobUtil.checkStartCondition(targetSessionJob, startJob);
			} else {
				//セッション横断待ち条件の場合
				m_log.info("CrossSessionJob exists : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);
				crossSessionExists = true;
				//ジョブユニットID、ジョブID、ジョブ終了日時から、対象セッションジョブを取得
				List<JobSessionJobEntity> targetCrossSessionJobList = JobSessionJobUtil.searchCrossSessionJob(startJob);
				ok = JobSessionJobUtil.checkStartCrossSessionCondition(targetCrossSessionJobList, startJob);
			}
			if (!ok.equals(ReturnValue.NONE)) {
				//先行ジョブが終了済み
				if (ok.equals(ReturnValue.TRUE)) {
					//待ち条件を満たしている
					jobResult.add(true);
					//OR条件の場合、これ以上ループを回す必要はない
					if(job.getConditionType() == ConditionTypeConstant.TYPE_OR){
						break;
					}
				} else {
					jobResult.add(false);
				}
			} else {
				// 終了していないジョブが存在するため、allEndCheckフラグをfalseに変更
				allEndCheck = false;
				if(job.getConditionType() == ConditionTypeConstant.TYPE_AND) {
					// 待ち条件が「AND」の場合、待ち条件ジョブの判定を終了
					statusCheck = false;
					startCheck = false;
					break;
				}else{
					// 待ち条件が「OR」の場合
					jobResult.add(false);
				}
			}
		}
		if (crossSessionExists) {
			//セッション横断待ち条件の場合、将来別のセッションで実行されたジョブによって
			//待ち条件が満たされる可能性があるため常にfalseにする
			allEndCheck = false;
		}

		// 待ち条件変数判定
		for (JobStartParamInfoEntity jobStartParamInfoEntity : jobStartParamInfonList) {

			// 判定値1の置換後の文字列
			String decisionValue01 = "";
			// 判定値2の置換後の文字列
			String decisionValue02 = "";
			String regex = "#\\[[a-zA-Z0-9-_:]+\\]";
			Pattern pattern = Pattern.compile(regex);
			// ファシリティIDの文字列
			String decisionFacilityId = job.getFacilityId();
			// ファシリティIDが正規表現にマッチするか検証する
			if (pattern.matcher(decisionFacilityId).find()) {
				// ファシリティIDの置換
				decisionFacilityId = ParameterUtil.replaceSessionParameterValue(
						sessionId, decisionFacilityId, decisionFacilityId);
				
				// ファシリティIDが置換されているかどうか再度検証し、置換されていなければ次の判定処理まで待機
				if (pattern.matcher(decisionFacilityId).find()) {
					replaceCheck = false;
					jobResult.add(false);
					continue;
				}
			}
			// 判定値1が正規表現にマッチするか検証する
			if(pattern.matcher(jobStartParamInfoEntity.getId().getStartDecisionValue01()).find()) {
				// 判定値1の置換
				decisionValue01 = ParameterUtil.replaceSessionParameterValue(
						sessionId, decisionFacilityId, jobStartParamInfoEntity.getId().getStartDecisionValue01());
				decisionValue01 = ParameterUtil.replaceReturnCodeParameter(sessionId, jobunitId, decisionValue01);
				
				// 判定値1が置換されているかどうか再度検証し、置換されていなければ次の判定処理まで待機
				if (pattern.matcher(decisionValue01).find()) {
					replaceCheck = false;
					jobResult.add(false);
					continue;
				}
			}
			else {
				decisionValue01 = jobStartParamInfoEntity.getId().getStartDecisionValue01();
			}

			// 判定値2が正規表現にマッチするか検証する
			if(pattern.matcher(jobStartParamInfoEntity.getId().getStartDecisionValue02()).find()) {
				// 判定値2の置換
				decisionValue02 = ParameterUtil.replaceSessionParameterValue(
						sessionId, decisionFacilityId, jobStartParamInfoEntity.getId().getStartDecisionValue02());
				decisionValue02 = ParameterUtil.replaceReturnCodeParameter(sessionId, jobunitId, decisionValue02);
				
				// 判定値2が置換されているかどうか再度検証し、置換されていなければ次の判定処理まで待機
				if (pattern.matcher(decisionValue02).find()) {
					replaceCheck = false;
					jobResult.add(false);
					continue;
				}
			}
			else {
				decisionValue02 = jobStartParamInfoEntity.getId().getStartDecisionValue02();
			}

			if (jobStartParamInfoEntity.getId().getStartDecisionCondition() == DecisionObjectConstant.EQUAL_NUMERIC) {
				try {
					if (checkDecisionValue(decisionValue01, decisionValue02) == 0) {
						jobResult.add(true);
						//OR条件の場合、これ以上ループを回す必要はない
						if(job.getConditionType() == ConditionTypeConstant.TYPE_OR){
							break;
						}
					} else {
						jobResult.add(false);
					}
				} catch (NumberFormatException e) {
					jobResult.add(false);
				}
			} else if (jobStartParamInfoEntity.getId().getStartDecisionCondition() == DecisionObjectConstant.NOT_EQUAL_NUMERIC) {
				try {
					if (checkDecisionValue(decisionValue01, decisionValue02) != 0) {
						jobResult.add(true);
						//OR条件の場合、これ以上ループを回す必要はない
						if(job.getConditionType() == ConditionTypeConstant.TYPE_OR){
							break;
						}
					} else {
						jobResult.add(false);
					}
				} catch (NumberFormatException e) {
					jobResult.add(false);
				}
			} else if (jobStartParamInfoEntity.getId().getStartDecisionCondition() ==  DecisionObjectConstant.GREATER_THAN) {
				try {
					if (checkDecisionValue(decisionValue01, decisionValue02) > 0) {
						jobResult.add(true);
						//OR条件の場合、これ以上ループを回す必要はない
						if(job.getConditionType() == ConditionTypeConstant.TYPE_OR){
							break;
						}
					} else {
						jobResult.add(false);
					}
				} catch (NumberFormatException e) {
					jobResult.add(false);
				}
			} else if (jobStartParamInfoEntity.getId().getStartDecisionCondition() ==  DecisionObjectConstant.GREATER_THAN_OR_EQUAL_TO) {
				try {
					if (checkDecisionValue(decisionValue01, decisionValue02) >= 0) {
						jobResult.add(true);
						//OR条件の場合、これ以上ループを回す必要はない
						if(job.getConditionType() == ConditionTypeConstant.TYPE_OR){
							break;
						}
					} else {
						jobResult.add(false);
					}
				} catch (NumberFormatException e) {
					jobResult.add(false);
				}
			} else if (jobStartParamInfoEntity.getId().getStartDecisionCondition() ==  DecisionObjectConstant.LESS_THAN) {
				try {
					if (checkDecisionValue(decisionValue01, decisionValue02) < 0) {
						jobResult.add(true);
						//OR条件の場合、これ以上ループを回す必要はない
						if(job.getConditionType() == ConditionTypeConstant.TYPE_OR){
							break;
						}
					} else {
						jobResult.add(false);
					}
				} catch (NumberFormatException e) {
					jobResult.add(false);
				}
			} else if (jobStartParamInfoEntity.getId().getStartDecisionCondition() ==  DecisionObjectConstant.LESS_THAN_OR_EQUAL_TO) {
				try {
					if (checkDecisionValue(decisionValue01, decisionValue02) <= 0) {
						jobResult.add(true);
						//OR条件の場合、これ以上ループを回す必要はない
						if(job.getConditionType() == ConditionTypeConstant.TYPE_OR){
							break;
						}
					} else {
						jobResult.add(false);
					}
				} catch (NumberFormatException e) {
					jobResult.add(false);
				}
			} else if (jobStartParamInfoEntity.getId().getStartDecisionCondition() ==  DecisionObjectConstant.EQUAL_STRING) {
				if (decisionValue01.equals(decisionValue02)) {
					jobResult.add(true);
					//OR条件の場合、これ以上ループを回す必要はない
					if(job.getConditionType() == ConditionTypeConstant.TYPE_OR){
						break;
					}
				}
				else {
					jobResult.add(false);
				}
			} else if (jobStartParamInfoEntity.getId().getStartDecisionCondition() ==  DecisionObjectConstant.NOT_EQUAL_STRING) {
				if (!decisionValue01.equals(decisionValue02)) {
					jobResult.add(true);
					//OR条件の場合、これ以上ループを回す必要はない
					if(job.getConditionType() == ConditionTypeConstant.TYPE_OR){
						break;
					} 
				}
				else {
					jobResult.add(false);
				}
			} else {
				m_log.warn("Outside of DecisionCondition : " + jobStartParamInfoEntity.getId().getStartDecisionCondition());
			}
			m_log.debug("DecisionInfo " + jobStartParamInfoEntity.getId().getStartDecisionCondition()
					+ ", value01 : "+ decisionValue01
					+ ", value02 : "+ decisionValue02);
		}
		
		// 待ち条件判定開始
		if(statusCheck){
			//ANDまたはOR条件に一致するかチェック
			if(job.getConditionType() == ConditionTypeConstant.TYPE_AND){
				//AND条件の場合
				startCheck = true;
				for (Boolean flag : jobResult) {
					if(!flag){
						startCheck = false;
						break;
					}
				}
			}else{
				//OR条件の場合
				startCheck = false;
				for (Boolean flag : jobResult) {
					if(flag){
						startCheck = true;
						break;
					}
				}
			}

			//実行可能性のチェック
			//ANDまたはOR条件により、開始できる可能性を取得する
			if(!startCheck){
				if(job.getConditionType() == ConditionTypeConstant.TYPE_AND){
					//ANDの場合：待ち条件ジョブの実行結果に1つでもNGがある場合(startCheckがfalseの時点で条件を満たす)
					possibilityCheck = false;
				}else{
					//ORの場合：待ち条件がジョブのみで、全てのジョブが終了し、実行結果がNGである場合
					if(job.getStartTime() == null && allEndCheck && job.getStartMinute() == null){
						possibilityCheck = false;
					}
				}
			}

			if (!skipTimeCondition) {
				// 待ち条件時間のチェック
				if(job.getConditionType() == ConditionTypeConstant.TYPE_AND){
					// 待ち条件が「AND」の場合
					//待ち条件ジョブ判定を満たしていたら、待ち条件時間をチェック
					if(startCheck &&job.getStartTime() != null){
						startCheck = checkWaitTime(startCheck,sessionId, job.getStartTime());
					}
				}else {
					// 待ち条件が「OR」の場合
					// 待ち条件ジョブ判定を満たしていない場合、待ち条件時間をチェック
					if(!startCheck && job.getStartTime() != null){
						startCheck = checkWaitTime(startCheck,sessionId, job.getStartTime());
					}
				}

				// 待ち条件－セッション開始時の時間（分）のチェック
				if(job.getConditionType() == ConditionTypeConstant.TYPE_AND){
					// 待ち条件が「AND」の場合
					//待ち条件ジョブ判定を満たしていたら、待ち条件－セッション開始時の時間（分）をチェック
					if(startCheck &&job.getStartMinute() != null){
						startCheck = checkStartMinute(startCheck,sessionId, job.getStartMinute());
					}
				}else {
					// 待ち条件が「OR」の場合
					//待ち条件ジョブ判定を満たしていたら、待ち条件－セッション開始時の時間（分）をチェック
					if(!startCheck && job.getStartMinute() != null){
						startCheck = checkStartMinute(startCheck,sessionId, job.getStartMinute());
					}
				}
			}
		}

		//開始チェック結果が開始NGの場合
		//開始できる可能性なしの場合
		//条件を満たさなければ終了する場合
		if(!startCheck && !possibilityCheck && job.getUnmatchEndFlg().booleanValue() && replaceCheck){
			m_log.debug("checkStartCondition() : unmatch end flg is true. end job : " +
					" jobid : " + jobId +" : status :" + sessionJob.getStatus());
			//条件を満たさず終了の場合は、終了状態を異常とする。
			Integer endStatus = job.getUnmatchEndStatus();
			// 終了値を設定
			Integer endValue = job.getUnmatchEndValue();

			//実行状態、終了状態、終了値、終了日時を設定
			setEndStatus(sessionId, jobunitId, jobId, StatusConstant.TYPE_END_UNMATCH,
					endStatus, endValue, null);
			//ジョブ終了時関連処理
			endJob(sessionId, jobunitId, jobId, null, false);
		}
		return startCheck;
	}

	/**
	 * 待ち条件（セッション開始時の時間（分））のチェック
	 * @param startCheck
	 * @param sessionId
	 * @param startMinute
	 * @return true or false
	 * @throws JobInfoNotFound
	 */
	private Boolean checkStartMinute(Boolean startCheck,
			String sessionId, Integer startMinute) throws JobInfoNotFound {

		m_log.trace("startCheck : " + startCheck);

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
		Boolean startMinuteCheck = check <= HinemosTime.currentTimeMillis();
		/*
		 * セッション開始時の時間（分）が実行されない場合は、
		 * checkDateMapに追加する。
		 */
		if (!startMinuteCheck) {
			addCheckDate(sessionId, check);
		}

		return startMinuteCheck;
	}

	/**
	 * 待ち条件（時刻）のチェック
	 * @param startCheck
	 * @param sessionId
	 * @param startTime
	 * @return true or false (ジョブセッション開始日 + 待ち条件(時刻) + TIMEZONE <= 現在日時)
	 * @throws JobInfoNotFound
	 */
	private Boolean checkWaitTime(Boolean startCheck,
			String sessionId, Long startTime) throws JobInfoNotFound {

		m_log.trace("startCheck : " + startCheck);

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
		startCheck = overMidnight <= HinemosTime.currentTimeMillis();
		m_log.trace("startCheck2 : " + startCheck);

		/*
		 * 待ち条件時刻が原因で実行できない場合は、checkDateMapに追加する。
		 */
		if (!startCheck) {
			addCheckDate(sessionId, overMidnight);
		}
		return startCheck;
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
	private Boolean checkStartDelayRecursive(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		m_log.debug("checkStartDelayMain() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		Boolean delayCheck = false;

		//実行状態チェック
		if(sessionJob.getStatus() == StatusConstant.TYPE_WAIT){
			//開始遅延チェック
			delayCheck = checkStartDelaySub(sessionId, jobunitId, jobId);
		}

		//セッションIDとジョブIDから、直下のジョブを取得
		Collection<JobSessionJobEntity> collection = null;
		collection = QueryUtil.getChildJobSessionJob(sessionId, jobunitId, jobId);

		if (collection == null) {
			m_log.trace("collection is null. " + sessionId + "," + jobunitId + "," + jobId);
			return delayCheck;
		}
		for (JobSessionJobEntity childSessionJob : collection) {
			String childSessionId = childSessionJob.getId().getSessionId();
			String childJobUnitId = childSessionJob.getId().getJobunitId();
			String childJobId = childSessionJob.getId().getJobId();

			//開始遅延チェックメイン処理を行う（再帰呼び出し）
			checkStartDelayRecursive(childSessionId, childJobUnitId, childJobId);
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
	private Boolean checkStartDelaySub(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		m_log.debug("checkStartDelay() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

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

			//操作
			if(job.getStartDelayOperation().booleanValue()){
				int type = job.getStartDelayOperationType();

				if(type == OperationConstant.TYPE_STOP_SKIP){
					//実行状態が待機の場合、実行状態をスキップにする
					if(sessionJob.getStatus() == StatusConstant.TYPE_WAIT){
						sessionJob.setStatus(StatusConstant.TYPE_SKIP);
					}
				}else if(type == OperationConstant.TYPE_STOP_WAIT){
					//実行状態が待機の場合、実行状態を保留中にする
					if(sessionJob.getStatus() == StatusConstant.TYPE_WAIT){
						sessionJob.setStatus(StatusConstant.TYPE_RESERVING);
					}
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

		if(job.getEndDelayJob().booleanValue()){
			//ジョブ開始後の時間が設定されている場合

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

		if(job.getEndDelayChangeMount().booleanValue()){
			//実行履歴からの変化量が設定されている場合
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
			}
			operation = true;
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

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			m_log.debug("checkEndStatus() : sessionId=" + sessionId + ", jobId=" + jobId);

			//セッションIDとジョブIDから、セッションジョブを取得
			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
			// ジョブ情報を取得
			JobInfoEntity job = sessionJob.getJobInfoEntity();

			ArrayList<Integer> statusList = new ArrayList<Integer>();

			if(sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOB
					|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_APPROVALJOB
					|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_MONITORJOB){
				//ジョブの場合

				//セッションジョブからセッションノードを取得
				Collection<JobSessionNodeEntity> collection = sessionJob.getJobSessionNodeEntities();
				for (JobSessionNodeEntity sessionNode : sessionJob.getJobSessionNodeEntities()) {

					Integer endValue = sessionNode.getEndValue();
					if(endValue == null){
						continue;
					}
					Integer status = JobSessionJobUtil.checkEndStatus(sessionJob, endValue);
					statusList.add(status);

					//コマンドの実行が正常終了するまで順次リトライの場合
					if(job.getProcessMode() == ProcessingMethodConstant.TYPE_RETRY &&
							status == EndStatusConstant.TYPE_NORMAL){
						statusList.clear();
						statusList.add(EndStatusConstant.TYPE_NORMAL);
						break;
					}
				}
				//配下にセッションノードが存在しない場合
				if(collection.size() == 0){
					statusList.clear();
					statusList.add(EndStatusConstant.TYPE_ABNORMAL);
				}
			}else{
				//ジョブ以外の場合

				Integer endStatusCheck = sessionJob.getEndStausCheckFlg();
				if(endStatusCheck == null ||
						endStatusCheck == EndStatusCheckConstant.NO_WAIT_JOB){
					//待ち条件に指定されていないジョブのみで判定

					//セッションIDとジョブIDの直下のジョブを取得
					Collection<JobSessionJobEntity> collection = QueryUtil.getChildJobSessionJob(sessionId, jobunitId, jobId);

					for (JobSessionJobEntity childSessionJob : collection) {

						//待ち条件に指定されているかチェック
						Collection<JobStartJobInfoEntity> targetJobList = null;
						targetJobList = em.createNamedQuery("JobStartJobInfoEntity.findByTargetJobId", JobStartJobInfoEntity.class)
								.setParameter("sessionId", sessionId)
								.setParameter("targetJobId", childSessionJob.getId().getJobId())
								.getResultList();

						if(targetJobList.size() > 0){
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
		if (endValue != null) {
			sessionJob.setEndValue(endValue);
		} else {
			//終了値を設定
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
	protected void endJob(String sessionId, String jobunitId, String jobId, String result, boolean normalEndFlag)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			m_log.info("endJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);
			//セッションIDとジョブIDから、セッションジョブを取得
			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

			////////// 状態遷移、通知 //////////
			if (normalEndFlag){
				//終了状態を判定し、終了状態と終了値を設定
				Integer endStatus = checkEndStatus(sessionId, jobunitId, jobId);
				//実行状態、終了状態、終了値、終了日時を設定
				setEndStatus(sessionId, jobunitId, jobId, StatusConstant.TYPE_END, endStatus, null, result);
			}

			////////// 繰り返し実行を判定 //////////
			Boolean jobRetryFlg = sessionJob.getJobInfoEntity().getJobRetryFlg();
			if (jobRetryFlg != null && jobRetryFlg && JobSessionJobUtil.checkRetryContinueCondition(sessionJob)) {
				//ジョブの状態等をリセットする
				JobSessionJobUtil.resetJobStatusRecursive(sessionJob, false /* resetRunCount=false */);
				//自身を再実行する
				startJob(sessionJob.getId().getSessionId(), sessionJob.getId().getJobunitId(), sessionJob.getId().getJobId());
				m_log.info("Retry job : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", runCount=" + sessionJob.getRunCount());
				return;  //繰り返し実行する場合はここで終了し後続ジョブを実行しない
			}

			////////// 待ち条件の処理 //////////
			// 終了ジョブ(endJobメソッドの引数)を待ち条件に指定しているジョブの処理
			Collection<JobStartJobInfoEntity> collection;
			collection = em.createNamedQuery("JobStartJobInfoEntity.findByTargetJobId", JobStartJobInfoEntity.class)
					.setParameter("sessionId", sessionId)
					.setParameter("targetJobId", jobId)
					.getResultList();
			ArrayList<JobSessionJobEntity> targetJobList = new ArrayList<JobSessionJobEntity>();
			for (JobStartJobInfoEntity startJob : collection) {
				//セッションIDとジョブIDから、セッションジョブを取得
				JobSessionJobEntity targetSessionJob = QueryUtil.getJobSessionJobPK(
						startJob.getId().getSessionId(),
						startJob.getId().getJobunitId(),
						startJob.getId().getJobId());
				if (targetJobList.contains(targetSessionJob)) {
					m_log.debug("duplicate " + targetSessionJob.getId().toString());
				} else {
					targetJobList.add(targetSessionJob);
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
				//実行状態が終了または変更済以外の場合、同一階層のジョブは未完了
				if(!StatusConstant.isEndGroup(sessionJob1.getStatus())){
					endAll = false;
					break;
				}
			}
			if(!endAll){
				return;
			}
			//同一階層のジョブが全て完了の場合
			if(!CreateJobSession.TOP_JOB_ID.equals(parentJobId)){
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
	 * 入力された判定値をDoubleに変換し、比較結果を返却する。
	 * @param value1 判定値1
	 * @param value2 判定値2
	 * @return 比較結果
	 */
	private int checkDecisionValue(String value1, String value2) {
		Double dValue01 = Double.parseDouble(value1);
		Double dValue02 = Double.parseDouble(value2);
		
		return dValue01.compareTo(dValue02);
	}
}
