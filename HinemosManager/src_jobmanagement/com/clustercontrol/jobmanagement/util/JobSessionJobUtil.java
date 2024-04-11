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
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.bean.DecisionObjectConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.RetryWaitStatusConstant;
import com.clustercontrol.jobmanagement.factory.JobSessionNodeImpl;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobWaitInfoEntity;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

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
		return JobCommonUtil.checkEndStatus(endValue, jobInfo.getNormalEndValueFrom(), jobInfo.getNormalEndValueTo(),
				jobInfo.getWarnEndValueFrom(), jobInfo.getWarnEndValueTo());
	}

	/**
	 * 先行ジョブの終了状態、または終了値、ジョブの戻り値が後続ジョブの待ち条件を満たしているかを返します。
	 * 待ち条件「ジョブ（戻り値）」については、先行ジョブの実行対象が1ノードである場合のみ、判定処理を行う
	 * （0ノードの場合: false、複数ノードの場合: none）。
	 * 
	 * @param targetSessionJob
	 * @param wait
	 * @return true: 満たしている false: 満たしていない none: 未設定
	 */
	public static ReturnValue checkStartCondition(JobSessionJobEntity targetSessionJob, JobWaitInfoEntity wait) {
		if (m_log.isDebugEnabled()) {
			m_log.debug("checkStartCondition() : targetSessionJob=" + targetSessionJob + ", wait=" + wait);
		}
		boolean typeIsEndStatus = wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS
				|| wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS;
		boolean typeIsEndValue = wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE
				|| wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE;
		boolean typeIsReturnValue = wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE;

		// 対象セッションジョブ(先行ジョブ)の実行状態をチェック
		if (!StatusConstant.isEndGroup(targetSessionJob.getStatus())) {
			return ReturnValue.NONE;
		}

		if (typeIsEndStatus) {
			// 終了状態での比較
			Integer endStatus = targetSessionJob.getEndStatus();
			if (endStatus == null) {
				return ReturnValue.FALSE;
			}
			// 対象セッションジョブの実行状態と待ち条件の終了状態を比較
			if ((wait.getStatus() == EndStatusConstant.TYPE_ANY) || (endStatus.equals(wait.getStatus()))) {
				return ReturnValue.TRUE;
			}
		}

		if (typeIsEndValue) {
			// 終了値での比較
			Integer endValue = targetSessionJob.getEndValue();
			if (endValue == null) {
				return ReturnValue.FALSE;
			}

			boolean result = false;
			switch (wait.getDecisionCondition()) {
			case DecisionObjectConstant.EQUAL_NUMERIC:
				result = endValue.compareTo(wait.getIntValueList().get(0)) == 0;
				break;
			case DecisionObjectConstant.NOT_EQUAL_NUMERIC:
				result = endValue.compareTo(wait.getIntValueList().get(0)) != 0;
				break;
			case DecisionObjectConstant.IN_NUMERIC:
				result = checkAsInteger(endValue, wait.getIntValueRangeList(), wait.getIntValueList(), true);
				break;
			case DecisionObjectConstant.NOT_IN_NUMERIC:
				result = checkAsInteger(endValue, wait.getIntValueRangeList(), wait.getIntValueList(), false);
				break;
			default:
				m_log.warn("Outside of DecisionCondition : " + wait.getDecisionCondition());
				return ReturnValue.FALSE;
			}
			if(result){
				return ReturnValue.TRUE;
			}
		}

		if (typeIsReturnValue){
			// 待ち条件「ジョブ（戻り値）」での比較
			if (m_log.isDebugEnabled()) {
				m_log.debug("checkStartCondition() : targetSessionJob.getJobSessionNodeEntities().size()=" + targetSessionJob.getJobSessionNodeEntities().size());
			}
			if (targetSessionJob.getJobSessionNodeEntities().size() == 0) {
				// 「ジョブ開始時に実行対象ノードを決定する」が有効の場合は0となるため、FALSEを返す。
				m_log.info("checkStartCondition() : No job session node, so start condition is false.");
				return ReturnValue.FALSE;
			}
			if (targetSessionJob.getJobSessionNodeEntities().size() > 1) {
				// 複数設定されていた場合はNONEを返す。
				m_log.warn("checkStartCondition() : multiple job session nodes, so start condition is none. targetSessionJob=" + targetSessionJob);
				return ReturnValue.NONE;
			}
			JobSessionNodeEntity targetSessionNode = targetSessionJob.getJobSessionNodeEntities().get(0);
			Integer returnValue = targetSessionNode.getEndValue();
			if (returnValue == null) {
				return ReturnValue.FALSE;
			}

			boolean result = false;
			switch (wait.getDecisionCondition()) {
			case DecisionObjectConstant.EQUAL_NUMERIC:
				result = returnValue.compareTo(wait.getIntValueList().get(0)) == 0;
				break;
			case DecisionObjectConstant.NOT_EQUAL_NUMERIC:
				result = returnValue.compareTo(wait.getIntValueList().get(0)) != 0;
				break;
			case DecisionObjectConstant.GREATER_THAN:
				result = returnValue.compareTo(wait.getIntValueList().get(0)) > 0;
				break;
			case DecisionObjectConstant.LESS_THAN:
				result = returnValue.compareTo(wait.getIntValueList().get(0)) < 0;
				break;
			case DecisionObjectConstant.GREATER_THAN_OR_EQUAL_TO:
				result = returnValue.compareTo(wait.getIntValueList().get(0)) >= 0;
				break;
			case DecisionObjectConstant.LESS_THAN_OR_EQUAL_TO:
				result = returnValue.compareTo(wait.getIntValueList().get(0)) <= 0 ;
				break;
			case DecisionObjectConstant.IN_NUMERIC:
				result = checkAsInteger(returnValue, wait.getIntValueRangeList(), wait.getIntValueList(),
						true);
				break;
			case DecisionObjectConstant.NOT_IN_NUMERIC:
				result = checkAsInteger(returnValue, wait.getIntValueRangeList(), wait.getIntValueList(),
						false);
				break;
			default:
				m_log.warn("Outside of DecisionCondition : " + wait.getDecisionCondition());
				return ReturnValue.FALSE;
			}
			if(result){
				return ReturnValue.TRUE;
			}
		}
		return ReturnValue.FALSE;
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
	 * @throws HinemosUnknown 
	 */
	public static ReturnValue checkStartCrossSessionCondition(List<JobSessionJobEntity> targetCrossSessionJobList,
			JobWaitInfoEntity startJobInfo) throws HinemosUnknown {
		ReturnValue ok;
		if (targetCrossSessionJobList.isEmpty()) {
			// 終了済みのジョブ履歴が無い場合
			if (m_log.isDebugEnabled()) {
				m_log.debug("CrossSessionJob no ended target jobs: sessionId=" + startJobInfo.getId().getSessionId()
						+ ", jobunitId=" + startJobInfo.getId().getJobunitId() + ", jobId="
						+ startJobInfo.getId().getJobId());
			}
			ok = ReturnValue.NONE;
		} else {
			// セッション横断待ち条件の場合、ジョブ履歴中に複数の先行ジョブ終了履歴がヒットする場合がある
			// その場合、1つでも待ち条件を満たしているジョブ履歴があれば条件を満たしていると判定する
			ok = ReturnValue.FALSE;
			for (JobSessionJobEntity crossJob : targetCrossSessionJobList) {
				if (checkStartCondition(crossJob, startJobInfo).equals(ReturnValue.TRUE)) { // 終了済みのジョブのみを渡すためnullは入らない
					ok = ReturnValue.TRUE;
					if (m_log.isDebugEnabled()) {
						m_log.debug("CrossSessionJob matched target job found: sessionId="
								+ crossJob.getId().getSessionId() + ", jobunitId=" + crossJob.getId().getJobunitId()
								+ ", jobId=" + crossJob.getId().getJobId());
					}
					break;
				}
			}
		}
		return ok;
	}

	/**
	 * ジョブ変数の変数置換後の判定値1、判定値2が判定条件を満たしているかを返します
	 * 
	 * @param decisionValue01
	 * @param decisionValue02
	 * @param wait
	 * @return true: 満たしている false: 満たしていない
	 * @throws HinemosUnknown 
	 */
	public static boolean checkJobParamCondition(String replacementValue01, String replacementValue02, JobWaitInfoEntity wait)
			throws HinemosUnknown {
		boolean result = true;
		if (replacementValue02 == null) {
			m_log.warn("checkJobParamCondition() : Value is null. decisionValue01=" + replacementValue01
					+ ", decisionValue02=null" );
			return false;
		}
		try {
			switch (wait.getDecisionCondition()) {
			case DecisionObjectConstant.EQUAL_NUMERIC:
				result = compareAsDouble(replacementValue01, replacementValue02) == 0;
				break;
			case DecisionObjectConstant.NOT_EQUAL_NUMERIC:
				result = compareAsDouble(replacementValue01, replacementValue02) != 0;
				break;
			case DecisionObjectConstant.GREATER_THAN:
				result = compareAsDouble(replacementValue01, replacementValue02) > 0;
				break;
			case DecisionObjectConstant.GREATER_THAN_OR_EQUAL_TO:
				result = compareAsDouble(replacementValue01, replacementValue02) >= 0;
				break;
			case DecisionObjectConstant.LESS_THAN:
				result = compareAsDouble(replacementValue01, replacementValue02) < 0;
				break;
			case DecisionObjectConstant.LESS_THAN_OR_EQUAL_TO:
				result = compareAsDouble(replacementValue01, replacementValue02) <= 0;
				break;
			case DecisionObjectConstant.EQUAL_STRING:
				result = replacementValue01.equals(replacementValue02);
				break;
			case DecisionObjectConstant.NOT_EQUAL_STRING:
				result = !replacementValue01.equals(replacementValue02);
				break;
			default:
				m_log.info("checkJobParamCondition() : DecisionCondition is unknown. decisionValue01=" + replacementValue01
						+ ", decisionValue02=" + replacementValue02);
				return false;
			}
		} catch (NumberFormatException e) {
			m_log.info("checkJobParamCondition() : Conversion to Double failed. decisionValue01=" + replacementValue01
					+ ", decisionValue02=" + replacementValue02);
			return false;
		}
		return result;
	}

	/**
	 * ジョブ変数のIN、NOT IN条件で変数置換後のカンマ、コロンで複数選択可能な判定値2が判定条件を満たしているかを返します
	 * 
	 * @param decisionValue01
	 * @param decisionValue02
	 * @param wait
	 * @return true: 満たしている false: 満たしていない
	 * @throws HinemosUnknown 
	 */
	public static boolean checkJobMultiParamCondition(String replacementValue01, List<String> replacementValueList,
			List<String[]> replacementValueRangeList, JobWaitInfoEntity wait) throws HinemosUnknown {
		boolean result = true;
		if ((replacementValueList == null || replacementValueList.isEmpty())
				&& (replacementValueRangeList == null || replacementValueRangeList.isEmpty())) {
			m_log.info("checkJobMultiParamCondition() : ValueList is null. decisionValue01=" + replacementValue01
					+ ", replacementValueList=" + replacementValueList + ", replacementValueList="
					+ replacementValueRangeList);
			return false;
		}
		try {
			switch (wait.getDecisionCondition()) {
			case DecisionObjectConstant.IN_NUMERIC:
				result = checkAsDouble(replacementValue01, replacementValueRangeList, replacementValueList, true);
				break;
			case DecisionObjectConstant.NOT_IN_NUMERIC:
				result = checkAsDouble(replacementValue01, replacementValueRangeList, replacementValueList, false);
				break;
			default:
				m_log.info("checkJobMultiParamCondition() : DecisionCondition is unknown. decisionValue01="
						+ replacementValue01 + ", replacementValueList=" + replacementValueList
						+ ", replacementValueList=" + replacementValueRangeList);
				return false;
			}
		} catch (NumberFormatException e) {
			m_log.info("checkJobMultiParamCondition() : Conversion to Double failed. decisionValue01="
					+ replacementValue01 + ", replacementValueList=" + replacementValueList + ", replacementValueList="
					+ replacementValueRangeList);
			return false;
		}
		return result;
	}

	/**
	* 入力された判定値の比較結果を返却する。
	* 
	* @param value1 比較対象の数値
	* @param valueRangeList 対象範囲のリスト
	* @param valueList 対象数値のリスト
	* @param isEquals true:IN(数値)の場合、false: NOT IN(数値)の場合
	* @return true:条件を満たす、false:条件を満たさない
	*/
	private static boolean checkAsInteger(Integer value1, List<Integer[]> valueRangeList, List<Integer> valueList, boolean isEquals) {
		if(valueList == null || valueRangeList == null){
			return false;
		}

		for (Integer value : valueList) {
			if (value1.equals(value)) {
				return isEquals;
			}
		}

		for (Integer[] valueRange : valueRangeList) {
			if (value1 >= valueRange[0]
					&& value1 <= valueRange[1]) {
				return isEquals;
			}
		}
		return !isEquals;
	}

	/**
	 * ジョブ変数の比較評価にて文字数制限128という制約のためInteger以上の数値がありうる
	 * 入力された判定値をDoubleに変換し、比較結果を返却する。
	 * 
	 * @param value1 判定値1
	 * @param value2 判定値2
	 * @return 比較結果
	 */
	private static int compareAsDouble(String value1, String value2) {
		Double dValue01 = Double.parseDouble(value1);
		Double dValue02 = Double.parseDouble(value2);

		return dValue01.compareTo(dValue02);
	}

	/**
	* ジョブ変数の比較評価にて文字数制限128という制約のためInteger以上の数値がありうる
	* 入力された判定値をDoubleに変換し、比較結果を返却する。
	* 
	* @param value1 比較対象の数値
	* @param valueRangeList 対象範囲のリスト
	* @param valueList 対象数値のリスト
	* @param isEquals true: IN(数値)の場合、false: NOT IN(数値)の場合
	* @return true:条件を満たす、false:条件を満たさない
	* @throws HinemosUnknown 
	*/
	private static boolean checkAsDouble(String value1, List<String[]> valueRangeList, List<String> valueList,
			boolean isEquals) throws HinemosUnknown {
		Double valueDouble = null;
		try {
			valueDouble = Double.parseDouble(value1);
			for (String value : valueList) {
				if (valueDouble.equals(Double.parseDouble(value))) {
					return isEquals;
				}
			}
			for (String[] valueRange : valueRangeList) {
				Double min = Double.parseDouble(valueRange[0]);
				Double max = Double.parseDouble(valueRange[1]);
				if (valueDouble >= min && valueDouble <= max) {
					return isEquals;
				}
			}
		} catch (NumberFormatException e) {
			m_log.warn(e.getClass().getSimpleName() + ", " + e.getMessage() + " value=" + value1 + ", valueList="
					+ valueList + ", replacementValueList=" + valueRangeList);
		} catch (NullPointerException e){
			m_log.error(e.getClass().getSimpleName() + ", " + e.getMessage() + " value=" + value1 + ", valueList="
					+ valueList + ", replacementValueList=" + valueRangeList);
		}
		return !isEquals;
	}

	/**
	 * セッション横断待ち条件において範囲時間内のジョブ履歴を検索します。
	 * 
	 * @param startJob
	 * @return 待ち条件ジョブリスト（ジョブが見つからない場合はnullを返す）
	 */
	public static List<JobSessionJobEntity> searchCrossSessionJob(JobWaitInfoEntity wait) {
		Long currentTime = HinemosTime.currentTimeMillis();
		Calendar cal = HinemosTime.getCalendarInstance();
		cal.setTimeInMillis(currentTime);
		int before = - wait.getCrossSessionRange();
		cal.add(Calendar.MINUTE, before);  //endDateがこの時間より後のジョブが対象

		List<JobSessionJobEntity> targetCrossSessionJobList = QueryUtil.getJobSessionJobByJobunitIdJobIdEndDate(
				wait.getTargetJobunitId(),
				wait.getTargetJobId(),
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
				|| (runCount < retryEndCount && endStatus == null)
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

	/**
	 * ジョブをリトライ待ち中の状態にする。ジョブネットの場合は再帰的に変更する。
	 * 
	 * @param sessionJob セッションジョブ
	 * @param setMessage セッションノードにリトライ待ちメッセージをセットするかのフラグ
	 */
	public static void toRetryWaitingStatusRecursive(JobSessionJobEntity sessionJob, boolean setMessage, Integer interval) {
		// セッションジョブを実行中にする
		sessionJob.setStatus(StatusConstant.TYPE_RUNNING);

		// セッションノードを実行中にしてメッセージをセットする
		List<JobSessionNodeEntity> sessionNodeList = sessionJob.getJobSessionNodeEntities();
		for (JobSessionNodeEntity sessionNode : sessionNodeList) {
			sessionNode.setStatus(StatusConstant.TYPE_RUNNING);
			if (setMessage) {
				new JobSessionNodeImpl().setMessage(sessionNode,
						MessageConstant.WAIT_RETRY_INTERVAL.getMessage(interval.toString()));
			}
		}

		// ジョブネットの場合は配下のジョブのステータスを再帰的に変更
		String sessionId = sessionJob.getId().getSessionId();
		String jobunitId = sessionJob.getId().getJobunitId();
		String jobId = sessionJob.getId().getJobId();
		if (sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOBNET) {
			List<JobSessionJobEntity> childSessionJobList = QueryUtil.getChildJobSessionJob(sessionId, jobunitId,
					jobId);
			for (JobSessionJobEntity childSessionJob : childSessionJobList) {
				// 配下のジョブをリトライ待ち状態にする
				toRetryWaitingStatusRecursive(childSessionJob, setMessage, interval);
			}
		}
	}

	/**
	 * ジョブセッションのリトライ待ち状態に、指定ステータスをセットする。<BR>
	 * ジョブネットの場合は再帰的に子ジョブセッションにもセットする。
	 * 
	 * @param sessionJob
	 * @param waitStatus
	 * @see com.clustercontrol.jobmanagement.bean.RetryWaitStatusConstant
	 */
	public static void setRetryJobStatusRecursive(JobSessionJobEntity sessionJob, int waitStatus) {

		sessionJob.setRetryWaitStatus(waitStatus);

		// ジョブネットの場合は配下のジョブのステータスを再帰的に変更
		if (sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOBNET) {
			String sessionId = sessionJob.getId().getSessionId();
			String jobunitId = sessionJob.getId().getJobunitId();
			String jobId = sessionJob.getId().getJobId();
			List<JobSessionJobEntity> childSessionJobList = QueryUtil.getChildJobSessionJob(sessionId, jobunitId,
					jobId);

			// 親が「WAIT」の場合、子は「PARENT_WAIT」にしておく（※セッションノードのタイムアウトによる再実行を防ぐため）
			int childStatus = waitStatus;
			if (waitStatus == RetryWaitStatusConstant.WAIT) {
				childStatus = RetryWaitStatusConstant.PARENT_WAIT;
			}
			for (JobSessionJobEntity childSessionJob : childSessionJobList) {
				setRetryJobStatusRecursive(childSessionJob, childStatus);
			}
		}
	}
}
