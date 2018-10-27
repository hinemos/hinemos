/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.ReturnValue;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobStartJobInfoEntity;
import com.clustercontrol.util.HinemosTime;

public class JobSessionJobUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( JobSessionJobUtil.class );
	
	/**
	 * セッションジョブの終了値から終了状態を判定し返します。 
	 * 
	 * @param sessionJob
	 * @param endValue
	 * @return 終了状態
	 */
	public static Integer checkEndStatus(JobSessionJobEntity sessionJob, Integer endValue) {
		JobInfoEntity jobInfo = sessionJob.getJobInfoEntity();

		if(endValue >= jobInfo.getNormalEndValueFrom()
				&& endValue <= jobInfo.getNormalEndValueTo()){
			//終了状態（正常）の範囲内ならば、正常とする
			return EndStatusConstant.TYPE_NORMAL;
		}else if(endValue >= jobInfo.getWarnEndValueFrom()
				&& endValue <= jobInfo.getWarnEndValueTo()){
			//終了状態（警告）の範囲内ならば、警告とする
			return EndStatusConstant.TYPE_WARNING;
		}else{
			//終了状態（異常）の範囲内ならば、異常とする
			return EndStatusConstant.TYPE_ABNORMAL;
		}
	}

	/**
	 * 先行ジョブの終了状態、または終了値が後続ジョブの待ち条件を満たしているかを返します。
	 * 
	 * @param targetSessionJob
	 * @param startJob
	 * @return true:  満たしている
	 *          false: 満たしていない
	 *          null:  先行ジョブが終了していないため判定不能
	 */
	public static ReturnValue checkStartCondition(JobSessionJobEntity targetSessionJob, JobStartJobInfoEntity startJob) {
		//対象セッションジョブ(先行ジョブ)の実行状態をチェック
		if(StatusConstant.isEndGroup(targetSessionJob.getStatus())){
			if(startJob.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS
				|| startJob.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS){
				//終了状態での比較
				Integer endStatus = targetSessionJob.getEndStatus();
				if(endStatus != null){
					//対象セッションジョブの実行状態と待ち条件の終了状態を比較
					if((startJob.getId().getTargetJobEndValue() == EndStatusConstant.TYPE_ANY)
							|| (endStatus.equals(startJob.getId().getTargetJobEndValue()))){
						return ReturnValue.TRUE;
					}
				}
			}else if(startJob.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE
						|| startJob.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE){
				//終了値での比較
				Integer endValue = targetSessionJob.getEndValue();
				if(endValue != null){
					//対象セッションジョブの実行状態と待ち条件の終了値を比較
					if(endValue.equals(startJob.getId().getTargetJobEndValue())){
						return ReturnValue.TRUE;
					}
				}
			}
			return ReturnValue.FALSE;
		} else {
			return ReturnValue.NONE;
		}
	}

	/**
	 * セッション横断待ち条件の先行ジョブの終了状態、または終了値が後続ジョブの待ち条件を満たしているかを返します。
	 * 先行ジョブのうち1つでも待ち条件を満たすものがあればtrueを返します。
	 * 
	 * @param targetSessionJobList
	 * @param startJob
	 * @return true:  満たしている
	 *          false: 満たしていない
	 *          null:  先行ジョブが終了していないため判定不能
	 */
	public static ReturnValue checkStartCrossSessionCondition(List<JobSessionJobEntity> targetCrossSessionJobList, JobStartJobInfoEntity startJobInfo) {
		ReturnValue ok;
		if (targetCrossSessionJobList.isEmpty()) {
			//終了済みのジョブ履歴が無い場合
			m_log.info("CrossSessionJob no ended target jobs: sessionId=" + startJobInfo.getId().getSessionId() +
															", jobunitId=" + startJobInfo.getId().getJobunitId() +
															", jobId=" + startJobInfo.getId().getJobId());
			ok = ReturnValue.NONE;
		} else {
			//セッション横断待ち条件の場合、ジョブ履歴中に複数の先行ジョブ終了履歴がヒットする場合がある
			//その場合、1つでも待ち条件を満たしているジョブ履歴があれば条件を満たしていると判定する
			ok = ReturnValue.FALSE;
			for (JobSessionJobEntity crossJob: targetCrossSessionJobList) {
				if (checkStartCondition(crossJob, startJobInfo).equals(ReturnValue.TRUE)) {  //終了済みのジョブのみを渡すためnullは入らない
					ok = ReturnValue.TRUE;
					m_log.info("CrossSessionJob matched target job found: sessionId=" + crossJob.getId().getSessionId() +
																		", jobunitId=" + crossJob.getId().getJobunitId() +
																		", jobId=" + crossJob.getId().getJobId());
					break;
				} 
			}
		}
		return ok;
	}

	/**
	 * セッション横断待ち条件において範囲時間内のジョブ履歴を検索します。
	 * 
	 * @param startJob
	 * @return 待ち条件ジョブリスト（ジョブが見つからない場合はnullを返す）
	 */
	public static List<JobSessionJobEntity> searchCrossSessionJob(JobStartJobInfoEntity startJob) {
		Long currentTime = HinemosTime.currentTimeMillis();
		Calendar cal = HinemosTime.getCalendarInstance();
		cal.setTimeInMillis(currentTime);
		int before = - startJob.getTargetJobCrossSessionRange();
		cal.add(Calendar.MINUTE, before);  //endDateがこの時間より後のジョブが対象

		List<JobSessionJobEntity> targetCrossSessionJobList = QueryUtil.getJobSessionJobByJobunitIdJobIdEndDate(
				startJob.getId().getTargetJobunitId(),
				startJob.getId().getTargetJobId(),
				cal.getTimeInMillis());
		return targetCrossSessionJobList;
	}

	/**
	 * 繰り返し実行を設定されているジョブが繰り返し完了条件を満たしているかどうかを返します。 
	 * 
	 * @param sessionJob
	 * @return true: 繰り返し完了条件を満たしている、false: 満たしていない。
	 */
	public static boolean checkRetryContinueCondition(JobSessionJobEntity sessionJob) {
		Integer runCount = sessionJob.getRunCount();
		Integer endStatus = sessionJob.getEndStatus();
		Integer retryEndCount = sessionJob.getJobInfoEntity().getJobRetry();
		Integer retryEndStatus = sessionJob.getJobInfoEntity().getJobRetryEndStatus();
		
		return (runCount < retryEndCount && retryEndStatus == null)
				|| (runCount < retryEndCount && !endStatus.equals(retryEndStatus));
	}
	
	/**
	 * 繰り返し実行において対象のジョブの状態をリセットします。
	 * 
	 * @param sessionJob
	 * @param resetRunCount true: 実行回数をリセットする、false: 実行回数をリセットしない
	 */
	public static void resetJobStatus(JobSessionJobEntity sessionJob, boolean resetRunCount) {
		//状態を「待機」戻し、終了状態、終了値、開始・終了日時、実行回数をクリアしておく
		sessionJob.setStatus(StatusConstant.TYPE_WAIT);
		sessionJob.setEndStatus(null);
		sessionJob.setEndValue(null);
		sessionJob.setStartDate(null);
		sessionJob.setEndDate(null);
		if (resetRunCount) {
			//繰り返しの最も外側のジョブは実行回数をリセットしない
			sessionJob.setRunCount(0);
		}
		//セッションノードの状態も戻す
		List<JobSessionNodeEntity> sessionNodeList = sessionJob.getJobSessionNodeEntities();
		for(JobSessionNodeEntity sessionNode: sessionNodeList) {
			sessionNode.setStatus(StatusConstant.TYPE_WAIT);
			sessionNode.setEndValue(null);
			sessionNode.setStartDate(null);
			sessionNode.setEndDate(null);
			sessionNode.setRetryCount(0);
			sessionNode.setErrorRetryCount(0);
		}
	}
	
	/**
	 * 繰り返し実行において対象のジョブの状態を再帰的にリセットします。
	 * 
	 * @param sessionJob
	 */
	public static void resetJobStatusRecursive(JobSessionJobEntity sessionJob, boolean resetRunCount) {
		//繰り返す最上位のジョブは実行回数をリセットしないためfalseを渡す
		resetJobStatus(sessionJob, resetRunCount);

		//ジョブネットの場合は配下のジョブのステータスを再帰的に変更
		String sessionId = sessionJob.getId().getSessionId();
		String jobunitId = sessionJob.getId().getJobunitId();
		String jobId = sessionJob.getId().getJobId();
		if (sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOBNET) {
			List<JobSessionJobEntity> childSessionJobList =
				QueryUtil.getChildJobSessionJob(sessionId, jobunitId, jobId);
			for (JobSessionJobEntity childSessionJob: childSessionJobList) {
				//配下のジョブは実行回数をリセットする
				resetJobStatusRecursive(childSessionJob, true);
			}
		}
	}
	
}
