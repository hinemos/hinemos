/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.util;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.RpaManagementRestCheckFailed;
import com.clustercontrol.fault.RpaManagementRestRunFailed;
import com.clustercontrol.fault.RpaManagementToolMasterNotFound;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;
import com.clustercontrol.jobmanagement.bean.RpaJobConditionConstant;
import com.clustercontrol.jobmanagement.bean.RpaStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.RunInfo;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunResultInfo;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.factory.JobSessionNodeImpl;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobRpaCheckEndValueInfoEntity;
import com.clustercontrol.jobmanagement.model.JobRpaRunConditionEntity;
import com.clustercontrol.jobmanagement.model.JobRpaRunConditionEntityPK;
import com.clustercontrol.jobmanagement.model.JobRpaRunParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.rpa.bean.RpaManagementToolRunParamTypeConstant;
import com.clustercontrol.rpa.factory.RpaManagementRestDefine;
import com.clustercontrol.rpa.factory.RpaManagementRestTokenCache;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.rpa.util.RpaUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * HinemosManager上でRPAシナリオジョブ（間接実行）を実行するクラス<BR>
 */
public class RpaJobWorker {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(RpaJobWorker.class);
	/** スレッドプール */
	private static MonitoredThreadPoolExecutor service;
	/** ワーカースレッド名 */
	private static String workerName = "RpaJobWorker";
	/** スレッドプールサイズ */
	private static int maxThreadPoolSize = HinemosPropertyCommon.job_rpa_thread_pool_size.getIntegerValue();
	/** 処理を中断する際に使用するFutureオブジェクトのキャッシュ */
	private static Map<String, Future<Boolean>> futureCache = new ConcurrentHashMap<>();

	static {
		service = new MonitoredThreadPoolExecutor(maxThreadPoolSize, maxThreadPoolSize, 0L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
					private volatile int _count = 0;

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, workerName + "-" + _count++);
					}

				}, new ThreadPoolExecutor.AbortPolicy());
	}

	/**
	 * RPAシナリオジョブ実行
	 * 
	 * @param runInstructionInfo
	 *            実行指示情報
	 * @throws JobRpaTooManyRunning
	 */
	public static void runJob(RunInstructionInfo runInstructionInfo) {
		m_log.info("runJob() SessionID=" + runInstructionInfo.getSessionId() + ", JobunitID="
				+ runInstructionInfo.getJobunitId() + ", JobID=" + runInstructionInfo.getJobId() + ", FacilityID="
				+ runInstructionInfo.getFacilityId() + ", CommandType=" + runInstructionInfo.getCommandType());
		try {
			// スレッドプールのサイズ以上に同時実行を行うと待たされるため、メッセージを出力する。
			if (service.getQueueRunningThreadMap().size() >= maxThreadPoolSize) {
				m_log.warn("run() : too many concurrent execution, current=" + service.getQueueRunningThreadMap().size()
						+ ", max=" + maxThreadPoolSize);
				JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(runInstructionInfo.getSessionId(),
						runInstructionInfo.getJobunitId(), runInstructionInfo.getJobId(),
						runInstructionInfo.getFacilityId());
				new JobSessionNodeImpl().setMessage(sessionNode,
						MessageConstant.MESSAGE_JOB_RPA_TOO_MANY_RUNNING.getMessage(String.valueOf(maxThreadPoolSize)));
			}
			Future<Boolean> future = service.submit(new RpaJobTask(runInstructionInfo));
			// スレッドを停止できるようMapに保持しておく
			futureCache.put(getKey(runInstructionInfo), future);
		} catch (Throwable e) {
			m_log.warn("runJob() Error : " + e.getMessage());
		}
	}

	/**
	 * 実行途中のままとなってるRPAシナリオジョブを再実行する<BR>
	 * マネージャ起動時に実行する
	 */
	public static void restartRunningJob() {
		// 実行途中となっているRPAシナリオジョブ情報を取得
		List<JobRpaRunConditionEntity> jobRunConditionList = null;
		try {
			jobRunConditionList = QueryUtil.getJobRpaRunCondition();
		} catch (Exception e) {
			m_log.warn("restartRunningJob() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
		if (jobRunConditionList == null || jobRunConditionList.isEmpty()) {
			return;
		}

		m_log.info("restartRunningJob() : restart running RpaJob");
		for (JobRpaRunConditionEntity condition : jobRunConditionList) {
			// ジョブ実行情報作成
			RunInstructionInfo runInstructionInfo = new RunInstructionInfo();
			runInstructionInfo.setSessionId(condition.getId().getSessionId());
			runInstructionInfo.setJobunitId(condition.getId().getJobunitId());
			runInstructionInfo.setJobId(condition.getId().getJobId());
			runInstructionInfo.setFacilityId(condition.getId().getFacilityId());
			runInstructionInfo.setCommand(CommandConstant.RPA);
			runInstructionInfo.setCommandType(CommandTypeConstant.NORMAL);
			m_log.debug("restartRunningJob() : sessionId=" + runInstructionInfo.getSessionId() + ", jobunitId="
					+ runInstructionInfo.getJobunitId() + ", jobId=" + runInstructionInfo.getJobId() + ", facilityId="
					+ runInstructionInfo.getFacilityId());

			// ジョブの実行状態を確認
			try {
				JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(condition.getId().getSessionId(),
						condition.getId().getJobunitId(), condition.getId().getJobId());
				if (sessionJob.getStatus() != StatusConstant.TYPE_RUNNING) {
					// ジョブが実行中でない場合は実行せず実行状態情報を削除
					m_log.warn("restartRunningJob() : status is not running, skip. sessionId="
							+ condition.getId().getSessionId() + ", jobunitId=" + condition.getId().getJobunitId()
							+ ", jobId=" + condition.getId().getJobId() + ", facilityId="
							+ condition.getId().getFacilityId() + ", status=" + sessionJob.getStatus());
					saveEndJob(runInstructionInfo);
					continue;
				}
			} catch (JobInfoNotFound | InvalidRole e) {
				m_log.warn("restartRunningJob() : " + e.getMessage(), e);
				saveEndJob(runInstructionInfo);
				continue;
			}

			// ジョブ実行
			try {
				service.submit(new RpaJobTask(runInstructionInfo, condition));
			} catch (Throwable e) {
				m_log.warn("restartRunningJob() Error : " + e.getMessage());
			}
		}
	}

	/**
	 * RPAシナリオジョブを実行するスレッドクラス
	 */
	private static class RpaJobTask implements Callable<Boolean> {
		/** ロガー */
		private static Log tasklog = LogFactory.getLog(RpaJobTask.class);
		/** リトライ間隔 */
		private static final long retryInterval = HinemosPropertyCommon.job_rpa_request_retry_interval
				.getIntegerValue();
		/** 実行結果確認間隔 */
		private static final long checkInterval = HinemosPropertyCommon.job_rpa_request_check_interval
				.getIntegerValue();
		/** 実行指示情報 */
		private RunInstructionInfo runInstructionInfo = null;
		/** ジョブ情報 */
		private JobInfoEntity jobInfo = null;
		/** RPAスコープ */
		private RpaManagementToolAccount account = null;
		/** API定義 */
		private RpaManagementRestDefine define = null;

		/**
		 * ジョブの進行状態（※nullは未実行扱い）
		 * 
		 * @see com.clustercontrol.jobmanagement.bean.RpaJobConditionConstant
		 */
		private Integer runCondition = null;
		/** RPA管理ツールで実行した処理の識別子となる文字列 */
		private String runIdentifier = null;

		/**
		 * コンストラクタ
		 * 
		 * @param runInstructionInfo
		 *            実行指示情報
		 */
		public RpaJobTask(RunInstructionInfo runInstructionInfo) {
			this.runInstructionInfo = runInstructionInfo;
		}

		/**
		 * コンストラクタ
		 * 
		 * @param runInstructionInfo
		 *            実行指情報
		 * @param runCondition
		 *            RPAシナリオジョブの進行状態
		 */
		public RpaJobTask(RunInstructionInfo runInstructionInfo, JobRpaRunConditionEntity runCondition) {
			this.runInstructionInfo = runInstructionInfo;
			this.runCondition = runCondition.getRunCondition();
			this.runIdentifier = runCondition.getRunIdentifier();
		}

		@Override
		public Boolean call() {
			tasklog.info("call() SessionID=" + this.runInstructionInfo.getSessionId() + ", JobunitID="
					+ this.runInstructionInfo.getJobunitId() + ", JobID=" + this.runInstructionInfo.getJobId()
					+ ", FacilityID=" + this.runInstructionInfo.getFacilityId() + ", CommandType="
					+ this.runInstructionInfo.getCommandType() + ", Command=" + this.runInstructionInfo.getCommand());

			try {
				// ジョブセッション情報からJobInfoEntityの取得
				// RPAスコープ、API定義の取得
				// ここで例外が発生した場合、一時的なエラーではないためリトライは行わない
				this.jobInfo = QueryUtil
						.getJobSessionJobPK(this.runInstructionInfo.getSessionId(),
								this.runInstructionInfo.getJobunitId(), this.runInstructionInfo.getJobId())
						.getJobInfoEntity();
				this.account = this.jobInfo.getRpaManagementToolAccount();
				this.define = RpaUtil.getRestDefine(this.account.getRpaManagementToolId());

				// ジョブが未開始状態の場合
				// ジョブの開始状態を記録（コミット）
				if (this.runCondition == null) {
					saveStartJob(this.runInstructionInfo);
					this.runCondition = RpaJobConditionConstant.START_JOB;
				}

				// ジョブが開始状態の場合
				// ジョブ実行を開始して記録（コミット）
				if (this.runCondition == RpaJobConditionConstant.START_JOB) {
					this.runIdentifier = execRun(createRunRequestData(this.jobInfo));
					tasklog.debug("call() : runIdentifier=" + this.runIdentifier);
					saveExecRun(this.runInstructionInfo, this.runIdentifier);
					this.runCondition = RpaJobConditionConstant.EXEC_RUN;
				}

				// ジョブが実行開始済み状態の場合
				// RPA管理ツールの実行結果を指定時間待機しながら確認、その後結果をもとにジョブ終了
				if (this.runCondition == RpaJobConditionConstant.EXEC_RUN) {
					String endStatus = execCheck();
					tasklog.debug("call() : endStatus=" + endStatus);
					// RPA管理ツールの終了状態からジョブの終了値にマッピング
					execJobEndNode(runInstructionInfo, RunStatusConstant.END, endStatusToEndValue(endStatus));
					return true;
				}
			} catch (JobInfoNotFound | InvalidRole | RpaManagementToolMasterNotFound | InvalidSetting
					| HinemosUnknown e) {
				// 設定が不正
				tasklog.warn("call() : invalid setting " + e.getMessage(), e);
				execJobEndNode(runInstructionInfo, "", e.getMessage(), RunStatusConstant.ERROR, -1);
			} catch (RpaManagementRestRunFailed e) {
				// シナリオ実行に失敗し終了
				execJobEndNode(runInstructionInfo, "", e.getMessage(), RunStatusConstant.ERROR,
						this.jobInfo.getRpaRunEndValue());
			} catch (RpaManagementRestCheckFailed e) {
				// シナリオ実行結果確認に失敗し終了
				execJobEndNode(runInstructionInfo, "", e.getMessage(), RunStatusConstant.ERROR,
						this.jobInfo.getRpaCheckEndValue());
			} catch (InterruptedException e) {
				// ジョブが停止された場合
				if (this.jobInfo.getRpaStopType() == RpaStopTypeConstant.STOP_SCENARIO) {
					// シナリオを終了する
					execCancel();
				}
				execJobEndNode(runInstructionInfo, "", "", RunStatusConstant.END, -1, CommandTypeConstant.STOP);
			} catch (Exception e) {
				// 上記以外
				tasklog.warn("call() : " + e.getMessage(), e);
				execJobEndNode(runInstructionInfo, "", e.getMessage(), RunStatusConstant.ERROR, -1);
			} finally {
				// 処理終了時にキャッシュから削除する
				futureCache.remove(getKey(runInstructionInfo));
			}
			return false;
		}

		/**
		 * RPA管理ツールの処理実行APIを呼び出します。<br>
		 * APIの呼び出しに失敗した場合、リトライを行います。
		 * 
		 * @return RPA管理ツール実行処理識別子
		 * @throws RpaManagementRestRunFailed
		 * @throws InterruptedException
		 */
		private String execRun(Map<String, Object> requestData)
				throws RpaManagementRestRunFailed, InterruptedException {
			long loopCount = 0;
			long retryCount = 0;
			while (true) {
				loopCount++;
				try (CloseableHttpClient client = RpaUtil.createHttpClient(this.account,
						this.jobInfo.getRpaRunConnectTimeout() * 1000, this.jobInfo.getRpaRunRequestTimeout() * 1000)) {
					String token = RpaManagementRestTokenCache.getInstance().getToken(this.account, this.define,
							client);
					return this.define.run(this.account.getUrl(), token, requestData, this.jobInfo.getRpaRunType(),
							client);
				} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | NullPointerException
						| IOException e) {
					tasklog.warn("execRun() : request failed, " + e.getMessage(), e);
					// メッセージを出力
					setMessage(MessageConstant.MESSAGE_JOB_RPA_RUN_SCENARIO_FAILED
							.getMessage(String.valueOf(retryCount), e.getMessage()));
					// 実行できない場合に終了する
					if (this.jobInfo.getRpaRunEndFlg() && retryCount >= this.jobInfo.getRpaRunRetry()) {
						throw new RpaManagementRestRunFailed(e);
					}
					retryCount++;
					// 例外が発生した場合、指定された間隔スリープ
					try {
						tasklog.debug("execRun() : sleep " + retryInterval + "ms, loopCount=" + loopCount);
						Thread.sleep(retryInterval);
					} catch (InterruptedException e1) {
						tasklog.warn("execRun() : thread interrupted");
						throw e1;
					}
				}
			}
		}

		/**
		 * ジョブの設定に基づいてRPA管理ツールの処理実行APIへのリクエストパラメータを生成します。
		 * 
		 * @param jobInfo
		 *            ジョブ設定情報
		 * @return リクエストパラメータのMap&lt;String, Object&gt;
		 *         <ul>
		 *         <li>key: パラメータ名</li>
		 *         <li>value:</li>
		 *         <ul>
		 *         <li>単一の値: String/Integer/Boolean</li>
		 *         <li>複数指定する項目: List&lt;String/Integer/Boolean&gt;</li>
		 *         <li>シナリオ入力パラメータ: List&lt;Map&lt;String, String&gt;&gt;</li>
		 *         </ul>
		 *         </ul>
		 * @throws InvalidSetting
		 */
		private Map<String, Object> createRunRequestData(JobInfoEntity jobInfo) throws InvalidSetting {
			Map<String, Object> requestData = new HashMap<>();
			// 起動パラメータ
			for (JobRpaRunParamInfoEntity paramInfo : jobInfo.getJobRpaRunParamInfoEntities()) {
				Object paramValue = convertParam(paramInfo);
				tasklog.debug(
						"createRequestData() : paramName=" + paramInfo.getParamName() + ", paramValue=" + paramValue);
				requestData.put(paramInfo.getParamName(), paramValue);
			}
			// シナリオ入力パラメータ
			if (jobInfo.getRpaScenarioParam() != null && !jobInfo.getRpaScenarioParam().isEmpty()) {
				// シナリオ入力パラメータJSONテキストの読み込み
				String scenarioParamName = com.clustercontrol.rpa.util.QueryUtil
						.getScenarioParamNameByRpaManagementToolId(this.account.getRpaManagementToolId());
				List<Map<String, String>> scenarioParams;
				try {
					// ジョブ変数があれば置換してからパースする
					scenarioParams = parseScenarioParamJson(ParameterUtil.replaceAllSessionParameterValue(
							runInstructionInfo.getSessionId(), runInstructionInfo.getJobId(),
							runInstructionInfo.getFacilityId(), jobInfo.getRpaScenarioParam()));
					tasklog.debug("createRequestData() : scenarioParamName=" + scenarioParamName + ", scenarioParams="
							+ scenarioParams);
					requestData.put(scenarioParamName, scenarioParams);
				} catch (JsonProcessingException | JobInfoNotFound | FacilityNotFound | InvalidRole
						| HinemosUnknown e) {
					m_log.warn("createRunRequestData() : " + e.getMessage(), e);
					throw new InvalidSetting(e);
				}
			}
			return requestData;
		}

		/**
		 * パラメータのデータ型に応じて値を変換します。
		 * 
		 * @param paramInfo
		 * @return
		 * @throws InvalidSetting
		 */
		private Object convertParam(JobRpaRunParamInfoEntity paramInfo) throws InvalidSetting {
			tasklog.debug("convertParam() : paramType=" + paramInfo.getParamType() + ", paramValue="
					+ paramInfo.getParamValue() + ", arrayFlg=" + paramInfo.getArrayFlg());
			Object paramValue = null;

			// データ型のコンバート
			Function<String, ?> convertDataType;
			switch (paramInfo.getParamType()) {
			case (RpaManagementToolRunParamTypeConstant.TYPE_STRING):
				convertDataType = String::valueOf;
				break;
			case (RpaManagementToolRunParamTypeConstant.TYPE_NUMERIC):
				convertDataType = Long::valueOf;
				break;
			case (RpaManagementToolRunParamTypeConstant.TYPE_BOOLEAN):
				convertDataType = Boolean::valueOf;
				break;
			default:
				tasklog.warn("convertParam() : unknown paramType=" + paramInfo.getParamType());
				convertDataType = String::valueOf;
			}

			// ジョブ変数を置換
			Function<String, String> replaceJobParam = (source) -> {
				try {
					return ParameterUtil.replaceAllSessionParameterValue(runInstructionInfo.getSessionId(),
							runInstructionInfo.getJobId(), runInstructionInfo.getFacilityId(), source);
				} catch (FacilityNotFound | InvalidRole | HinemosUnknown | JobInfoNotFound e) {
					tasklog.warn("convertParam() : replace job param failed, " + e.getMessage(), e);
				}
				return source;
			};

			// 端末IDを置換
			Function<String, String> replaceExecEnvId = (source) -> {
				if (ParameterUtil.isParamFormat(source)) {
					try {
						return ParameterUtil.replaceNodeRpaEnvIdParameter(source);
					} catch (FacilityNotFound | InvalidRole | HinemosUnknown e) {
						tasklog.warn("convertParam() : replace execEnvId failed, " + e.getMessage(), e);
					}
				}
				return source;
			};

			// ジョブ変数の置換を行った後にデータ型の変換を行う
			Function<String, ?> converter = replaceJobParam.andThen(replaceExecEnvId).andThen(convertDataType);
			try {
				if (paramInfo.getArrayFlg()) {
					// カンマ区切りで複数指定する項目はリストに変換する
					paramValue = Arrays.stream(paramInfo.getParamValue().split(",")).map(String::trim)
							.map(p -> converter.apply(p)).collect(Collectors.toList());
				} else {
					paramValue = converter.apply(paramInfo.getParamValue());
				}
			} catch (NumberFormatException e) {
				// 数値型のデータが不正な場合
				tasklog.warn("convertParam() : " + e.getMessage(), e);
				throw new InvalidSetting(e);
			}
			tasklog.debug("convertParam() : converted paramValue=" + paramValue);
			return paramValue;
		}

		/**
		 * RPA管理ツールの実行結果確認APIを呼び出し、処理が終了しているかどうか確認します。<br>
		 * 処理が終了していない場合、終了を待ちます。<br>
		 * 処理が終了した場合、RPA管理ツールの終了状態を返します。<br>
		 * APIの呼び出しに失敗した場合、リトライを行います。
		 * 
		 * @return RPA管理ツールの終了状態
		 * @throws RpaManagementRestCheckFailed
		 * @throws InterruptedException
		 */
		private String execCheck() throws RpaManagementRestCheckFailed, InterruptedException {
			// RPA管理ツールの実行終了を表すステータスを取得
			List<String> endStatusList = com.clustercontrol.rpa.util.QueryUtil
					.getEndStatusListByRpaManagementToolId(this.account.getRpaManagementToolId());
			tasklog.debug("execCheck() : endStatusList=" + endStatusList);
			long loopCount = 0;
			long retryCount = 0;
			while (true) {
				loopCount++;
				try (CloseableHttpClient client = RpaUtil.createHttpClient(this.account,
						this.jobInfo.getRpaCheckConnectTimeout() * 1000,
						this.jobInfo.getRpaCheckRequestTimeout() * 1000)) {
					String token = RpaManagementRestTokenCache.getInstance().getToken(this.account, this.define,
							client);
					String status = this.define.check(this.account.getUrl(), token, this.runIdentifier, client);
					// ステータスの取得に成功したらリトライ回数を0に戻す
					retryCount = 0;
					tasklog.debug("execCheck() : status=" + status);
					if (endStatusList.contains(status)) {
						// 終了状態を返す
						return status;
					}
				} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | NullPointerException
						| IOException e) {
					tasklog.warn("execCheck() : request failed, " + e.getMessage(), e);
					// メッセージを出力
					setMessage(MessageConstant.MESSAGE_JOB_RPA_CHECK_SCENARIO_RESULT_FAILED
							.getMessage(String.valueOf(retryCount), e.getMessage()));
					// 結果が確認できない場合に終了する
					if (this.jobInfo.getRpaCheckEndFlg() && retryCount >= this.jobInfo.getRpaCheckRetry()) {
						throw new RpaManagementRestCheckFailed(e);
					}
					retryCount++;
				}
				// まだ実行が終了していないまたは例外が発生した場合、指定された間隔スリープ
				try {
					tasklog.debug("execCheck() : sleep " + checkInterval + "ms, loopCount=" + loopCount);
					Thread.sleep(checkInterval);
				} catch (InterruptedException e1) {
					tasklog.warn("execCheck() : thread interrupted");
					throw e1;
				}
			}
		}

		/**
		 * ジョブの設定に基づいてRPA管理ツールの終了状態をジョブの終了値にマッピングします。
		 * 
		 * @param endStatus
		 *            RPA管理ツールの終了状態
		 * @return ジョブの終了値
		 */
		private int endStatusToEndValue(String endStatus) {
			for (JobRpaCheckEndValueInfoEntity endValueInfo : this.jobInfo.getJobRpaCheckEndValueInfoEntities()) {
				tasklog.debug("endStatusToEndValue() : endStatusId=" + endValueInfo.getId().getEndStatusId()
						+ ", endStatus=" + endValueInfo.getEndStatus() + ", endValue=" + endValueInfo.getEndValue());
				if (endStatus.equals(endValueInfo.getEndStatus())) {
					return endValueInfo.getEndValue();
				}
			}
			// 一致する条件が無かった場合（ここには来ないはず）
			tasklog.warn("endStatusToEndValue() : end value not found");
			return -1;
		}

		/**
		 * RPA管理ツールのシナリオ実行キャンセルAPIを呼び出し、シナリオを終了します。
		 */
		private void execCancel() {
			try (CloseableHttpClient client = RpaUtil.createHttpClient(this.account,
					this.jobInfo.getRpaCheckConnectTimeout() * 1000, this.jobInfo.getRpaCheckRequestTimeout() * 1000)) {
				String token = RpaManagementRestTokenCache.getInstance().getToken(this.account, this.define, client);
				this.define.cancel(this.account.getUrl(), token, this.runIdentifier, this.jobInfo.getRpaStopMode(),
						client);
			} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | NullPointerException
					| IOException e) {
				tasklog.warn("execCancel() : request failed, " + e.getMessage(), e);
				// メッセージを出力
				setMessage(MessageConstant.MESSAGE_JOB_RPA_CANCEL_SCENARIO_FAILED.getMessage(e.getMessage()));
			}
		}

		/**
		 * endNode()を実行する（メッセージ送信）
		 * 
		 * @param runInstructionInfo
		 * @param status
		 * @param endValue
		 * @return
		 */
		private static boolean execJobEndNode(RunInstructionInfo runInstructionInfo, Integer status, Integer endValue) {
			return execJobEndNode(runInstructionInfo, "", "", status, endValue, runInstructionInfo.getCommandType());
		}

		/**
		 * endNode()を実行する（メッセージ送信）
		 * 
		 * @param runInstructionInfo
		 * @param message
		 * @param errorMessage
		 * @param status
		 * @param endValue
		 * @return
		 */
		private static boolean execJobEndNode(RunInstructionInfo runInstructionInfo, String message,
				String errorMessage, Integer status, Integer endValue) {
			return execJobEndNode(runInstructionInfo, message, errorMessage, status, endValue,
					runInstructionInfo.getCommandType());
		}

		/**
		 * endNode()を実行する（メッセージ送信）
		 * 
		 * @param runInstructionInfo
		 * @param status
		 * @param message
		 * @param errorMessage
		 * @param endValue
		 * @param commandType
		 * @return
		 */
		private static boolean execJobEndNode(RunInstructionInfo runInstructionInfo, String message,
				String errorMessage, Integer status, Integer endValue, Integer commandType) {
			tasklog.debug("execJobEndNode() : sessionId=" + runInstructionInfo.getSessionId() + ", jobunitId="
					+ runInstructionInfo.getJobunitId() + ", jobId=" + runInstructionInfo.getJobId() + ", facilityId="
					+ runInstructionInfo.getFacilityId() + ", message=" + message + ", errorMessage=" + errorMessage
					+ ", status=" + status + ", endValue=" + endValue + ", commandType=" + commandType);

			boolean result = false;

			// メッセージ作成
			RunResultInfo resultInfo = new RunResultInfo();
			resultInfo.setSessionId(runInstructionInfo.getSessionId());
			resultInfo.setJobunitId(runInstructionInfo.getJobunitId());
			resultInfo.setJobId(runInstructionInfo.getJobId());
			resultInfo.setFacilityId(runInstructionInfo.getFacilityId());
			resultInfo.setCommand(runInstructionInfo.getCommand());
			resultInfo.setCommandType(commandType);
			resultInfo.setStopType(runInstructionInfo.getStopType());
			resultInfo.setStatus(status);
			resultInfo.setMessage(message);
			resultInfo.setErrorMessage(errorMessage);
			resultInfo.setTime(HinemosTime.getDateInstance().getTime());
			resultInfo.setEndValue(endValue);

			try {
				// メッセージ送信
				result = new JobRunManagementBean().endNode(resultInfo);
			} catch (HinemosUnknown | JobInfoNotFound | InvalidRole e) {
				tasklog.error("execJobEndNode() is error : " + ", SessionID=" + runInstructionInfo.getSessionId()
						+ ", JobunitID=" + runInstructionInfo.getJobunitId() + ", JobID="
						+ runInstructionInfo.getJobId() + ", FacilityID=" + runInstructionInfo.getFacilityId());
			}

			// ジョブ状態保持レコード削除（コミット）
			saveEndJob(runInstructionInfo);

			return result;
		}

		/**
		 * メッセージを出力します。
		 * 
		 * @param message
		 */
		private void setMessage(String message) {
			// メッセージを出力
			new JobControllerBean().setMessage(runInstructionInfo.getSessionId(), runInstructionInfo.getJobunitId(),
					runInstructionInfo.getJobId(), runInstructionInfo.getFacilityId(), message);
		}
	}

	/**
	 * RPAシナリオジョブが開始したことをDBに記録する
	 * 
	 * @param runInstructionInfo
	 *            ジョブ実行情報
	 */
	private static void saveStartJob(RunInstructionInfo runInstructionInfo) {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			JobRpaRunConditionEntity jobRpaRunConditionEntity = new JobRpaRunConditionEntity();
			jobRpaRunConditionEntity.setId(
					new JobRpaRunConditionEntityPK(runInstructionInfo.getSessionId(), runInstructionInfo.getJobunitId(),
							runInstructionInfo.getJobId(), runInstructionInfo.getFacilityId()));
			jobRpaRunConditionEntity.setRunCondition(RpaJobConditionConstant.START_JOB);
			jtm.checkEntityExists(JobRpaRunConditionEntity.class, jobRpaRunConditionEntity.getId());
			jtm.getEntityManager().persist(jobRpaRunConditionEntity);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("saveStartJob() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * RPAシナリオジョブで処理実行したことをDBに記録する<BR>
	 * 
	 * @param runInstructionInfo
	 *            ジョブ実行情報
	 * @param runIdentifier
	 *            RPA管理ツールで実行した処理の識別子となる文字列
	 */
	private static void saveExecRun(RunInstructionInfo runInstructionInfo, String runIdentifier) {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			// クエリ実行
			JobRpaRunConditionEntity entity = null;
			entity = QueryUtil.getJobRpaRunConditionPK(runInstructionInfo.getSessionId(),
					runInstructionInfo.getJobunitId(), runInstructionInfo.getJobId(),
					runInstructionInfo.getFacilityId());
			// RPAシナリオジョブの進行状態を「実行済」に更新
			if (entity != null) {
				entity.setRunCondition(RpaJobConditionConstant.EXEC_RUN);
				entity.setRunIdentifier(runIdentifier);
			}
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("saveExecRun() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * RPAシナリオジョブが終了したことをDBに記録する（該当レコード物理削除）<BR>
	 * 
	 * @param runInstructionInfo
	 *            ジョブ実行情報
	 */
	private static void saveEndJob(RunInstructionInfo runInstructionInfo) {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			// クエリ実行
			HinemosEntityManager em = jtm.getEntityManager();
			JobRpaRunConditionEntity entity = null;
			entity = QueryUtil.getJobRpaRunConditionPK(runInstructionInfo.getSessionId(),
					runInstructionInfo.getJobunitId(), runInstructionInfo.getJobId(),
					runInstructionInfo.getFacilityId());
			// レコード削除
			if (entity != null) {
				em.remove(entity);
			}
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("saveEndJob() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * 指示情報を元にキー文字列を返します。
	 * 
	 * @param runInstructionInfo
	 *            指示情報
	 * @return キー文字列
	 */
	private static String getKey(RunInfo runInfo) {
		return runInfo.getSessionId() + "#" + runInfo.getJobunitId() + "#" + runInfo.getJobId() + "#"
				+ runInfo.getFacilityId();
	}

	/**
	 * シナリオ入力パラメータのJSON文字列をパースします。
	 * 
	 * @param scenarioParamStr
	 * @return パース結果のオブジェクト List&lt;Map&lt;String, String&gt;&gt;
	 * @throws JsonProcessingException
	 */
	public static List<Map<String, String>> parseScenarioParamJson(String scenarioParamStr)
			throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		List<Map<String, String>> scenarioParams = objectMapper.readValue(scenarioParamStr,
				new TypeReference<List<Map<String, String>>>() {
				});
		return scenarioParams;
	}

	/**
	 * ジョブを停止します。
	 * 
	 * @param runInfo
	 */
	public static void stopJob(RunInfo runInfo) {
		m_log.info("stopJob()");
		String key = getKey(runInfo);
		m_log.debug("stopJob() : key=" + key);
		Future<Boolean> future = futureCache.get(key);
		if (future != null) {
			future.cancel(true);
		}
		futureCache.remove(key);
	}
}
