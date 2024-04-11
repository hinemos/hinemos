/* Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.xcloud.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;
import com.clustercontrol.jobmanagement.bean.ResourceJobConditionConstant;
import com.clustercontrol.jobmanagement.bean.ResourceJobConstant;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunResultInfo;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.factory.JobSessionNodeImpl;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rest.endpoint.cloud.RestSessionScope;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ResourceJobActionEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ResourceJobTypeEnum;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.HinemosCredential;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.bean.InstanceStatus;
import com.clustercontrol.xcloud.bean.Option;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.ActionMode;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.IInstances;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.InstanceEntity;
import com.clustercontrol.xcloud.model.ResourceJobRunConditionEntity;

import jakarta.persistence.TypedQuery;

/**
 * HinemosManager上でリソース制御ジョブを実行するクラス
 */
public class ResourceJobWorker {

	private static Log log = LogFactory.getLog(ResourceJobWorker.class);

	private static ExecutorService service;

	private static String workerName = "ResourceControlJobWorker";

	// 実行中ジョブを中断するためのFutureを保持するMap
	private static ConcurrentHashMap<String, Future<?>> execFutureMap = new ConcurrentHashMap<>();

	// ジョブノードメッセージ用のアクション名
	private static final String ACTION_POWERON = "POWERON";
	private static final String ACTION_POWEROFF = "POWEROFF";
	private static final String ACTION_REBOOT = "REBOOT";
	private static final String ACTION_SUSPEND = "SUSPEND";
	private static final String ACTION_SNAPSHOT = "SNAPSHOT";
	private static final String ACTION_ATTACH = "ATTACH";
	private static final String ACTION_DETACH = "DETACH";

	static {
		int maxThreadPoolSize = HinemosPropertyCommon.job_resource_thread_pool_size.getIntegerValue();

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
	 * リソース制御ジョブ実行
	 * @param runInstructionInfo 実行指示情報
	 */
	public static void runJob(RunInstructionInfo runInstructionInfo) {
		log.info("runJob() SessionID=" + runInstructionInfo.getSessionId()
				+ ", JobunitID=" + runInstructionInfo.getJobunitId()
				+ ", JobID=" + runInstructionInfo.getJobId()
				+ ", FacilityID=" + runInstructionInfo.getFacilityId()
				+ ", CommandType=" + runInstructionInfo.getCommandType());

		try {
			Future<?> future = service.submit(new ResourceControlJobTask(runInstructionInfo));
			execFutureMap.put(createFutureMapKey(runInstructionInfo), future);
		} catch(Throwable e) {
			log.warn("runJob() Error : " + e.getMessage());
		}
	}

	/**
	 * 実行途中のままとなってるリソース制御ジョブを再実行する<BR>
	 * マネージャ起動時に実行する
	 */
	public static void restartRunningJob() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 実行途中となっているリソース制御ジョブ情報を取得
			TypedQuery<ResourceJobRunConditionEntity> query = em.createNamedQuery(
					ResourceJobRunConditionEntity.findAllResourceJobRunCondition, ResourceJobRunConditionEntity.class);
			List<ResourceJobRunConditionEntity> jobRunConditionList = query.getResultList();

			if (jobRunConditionList == null || jobRunConditionList.size() == 0) {
				return;
			}

			// 再実行するジョブの総数をログ出力
			log.info("Restart Job Number : " + jobRunConditionList.size());

			for (ResourceJobRunConditionEntity condition : jobRunConditionList) {
				// ジョブセッションからジョブ情報の取得
				JobInfoEntity jobInfo = QueryUtil.getJobSessionJobPK(
						condition.getSessionId(),
						condition.getJobunit(),
						condition.getJobId()).getJobInfoEntity();

				// ジョブ実行情報作成
				RunInstructionInfo runInstructionInfo = new RunInstructionInfo();
				runInstructionInfo.setSessionId(condition.getSessionId());
				runInstructionInfo.setJobunitId(condition.getJobunit());
				runInstructionInfo.setJobId(condition.getJobId());
				runInstructionInfo.setFacilityId(jobInfo.getFacilityId());
				runInstructionInfo.setUser(jobInfo.getRegUser());
				runInstructionInfo.setCommand(CommandConstant.RESOURCE);
				runInstructionInfo.setCommandType(CommandTypeConstant.NORMAL);

				// 再実行するジョブ情報をログ出力
				log.info("Restart Job Info : "
						+ "SessionID=" + runInstructionInfo.getSessionId()
						+ ", JobunitID=" + runInstructionInfo.getJobunitId()
						+ ", JobID=" + runInstructionInfo.getJobId());

				// ジョブ実行
				try {
					Future<?> future = service.submit(new ResourceControlJobTask(runInstructionInfo, condition.getRunCondition()));
					execFutureMap.put(createFutureMapKey(runInstructionInfo), future);
				} catch(Throwable e) {
					log.warn("restartRunningJob() Error : " + e.getMessage());
				}
			}
		} catch (RuntimeException e) {
			//findbugs対応 RuntimeException のキャッチを明示化
			log.warn("runningJobRestart() Error : " + e.getMessage());
		} catch (Exception e) {
			log.warn("runningJobRestart() Error : " + e.getMessage());
		}
	}

	/**
	 * リソース制御ジョブを終了する
	 * @param runInstructionInfo 実行指示情報
	 * @param message メッセージ
	 * @param errorMessage エラーメッセージ
	 * @param status ステータス
	 * @param endValue 終了値
	 * @param forceStop ジョブの強制停止フラグ
	 */
	public static void endResourceControlJob(RunInstructionInfo runInstructionInfo,
			String message, String errorMessage, Integer status, Integer endValue, boolean forceStop) {

		log.debug("endResourceControlJob() : sessionId=" + runInstructionInfo.getSessionId()
				+ ", JobunitId=" + runInstructionInfo.getJobunitId()
				+ ", JobId=" + runInstructionInfo.getJobId());

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// ジョブ終了状態を記録（DB上の状態保持レコード削除）
			saveEndJob(runInstructionInfo);

			// メッセージ送信
			execJobEndNode(runInstructionInfo, message, errorMessage, status, endValue);

			// Mapから実行中ジョブの停止＋削除
			String futureKey = createFutureMapKey(runInstructionInfo);
			if (execFutureMap.containsKey(futureKey)) {
				// ジョブがリソース制御ジョブの処理終了以外で停止した場合、
				// スレッドをInterrupt(ユーザ操作、終了遅延など)
				if (forceStop) {
					boolean hasInterrupted = execFutureMap.get(futureKey).cancel(true);
					log.info("endResourceControlJob(): terminated RunningTask for " + futureKey + " Interrupted: "
							+ hasInterrupted);
				}
				execFutureMap.remove(futureKey);
			}

			jtm.commit();

		} catch(Exception e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * endNode()を実行する（メッセージ送信）
	 * @param runInstructionInfo
	 * @param status
	 * @param message
	 * @param errorMessage
	 * @param endValue
	 * @return
	 */
	private static boolean execJobEndNode(RunInstructionInfo runInstructionInfo,
			String message, String errorMessage, Integer status, Integer endValue) {

		boolean result = false;

		// メッセージ作成
		RunResultInfo resultInfo = new RunResultInfo();
		resultInfo.setSessionId(runInstructionInfo.getSessionId());
		resultInfo.setJobunitId(runInstructionInfo.getJobunitId());
		resultInfo.setJobId(runInstructionInfo.getJobId());
		resultInfo.setFacilityId(runInstructionInfo.getFacilityId());
		resultInfo.setCommand(runInstructionInfo.getCommand());
		resultInfo.setCommandType(runInstructionInfo.getCommandType());
		resultInfo.setStopType(runInstructionInfo.getStopType());
		resultInfo.setStatus(status);
		resultInfo.setMessage(message);
		resultInfo.setErrorMessage(errorMessage);
		resultInfo.setTime(HinemosTime.getDateInstance().getTime());
		resultInfo.setEndValue(endValue);

		try {
			JpaTransactionManager jtm = new JpaTransactionManager();
			boolean isNestedEm = jtm.isNestedEm();

			// メッセージ送信のため、一旦トランザクションを終了
			if (isNestedEm) {
				try {
					log.debug("execJobEndNode() jtm.commit");
					jtm.commit(true);
				} catch (Throwable e) {
					log.error("execJobEndNode() jtm.commit ", e);
				} finally {
					if (jtm != null) {
						jtm.close();
					}
				}
			}

			// メッセージ送信
			result = new JobRunManagementBean().endNode(resultInfo);

			//トランザクションを終了していた場合、開始する
			if (isNestedEm) {
				jtm = new JpaTransactionManager();
				jtm.begin();
			}
		} catch (HinemosUnknown | JobInfoNotFound | InvalidRole e) {
			log.error("endNode() is error : "
				+ ", SessionID=" + runInstructionInfo.getSessionId() 
				+ ", JobunitID=" + runInstructionInfo.getJobunitId()
				+ ", JobID=" + runInstructionInfo.getJobId()
				+ ", FacilityID=" + runInstructionInfo.getFacilityId());
		}

		return result;
	}

	/**
	 * 本クラスフィールドexecFutureMapのキーを生成
	 */
	private static String createFutureMapKey(RunInstructionInfo runInstructionInfo) {
		return runInstructionInfo.getSessionId() + runInstructionInfo.getJobunitId() + runInstructionInfo.getJobId();
	}

	/**
	 * ジョブノードにメッセージを追加する
	 * @param runInstructionInfo
	 * @param message
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	private static void setResourceJobNodeMessage(RunInstructionInfo runInstructionInfo, String message)
			throws JobInfoNotFound, InvalidRole {

		// ジョブセッション情報からJobSessionNodeEntityの取得（セッションとノード1:1）
		JobSessionNodeEntity jobSessionNode = QueryUtil.getJobSessionJobPK(
				runInstructionInfo.getSessionId(),
				runInstructionInfo.getJobunitId(),
				runInstructionInfo.getJobId()).getJobSessionNodeEntities().get(0);

		new JobSessionNodeImpl().setMessage(jobSessionNode, message);
	}

	private static class ResourceResult {
		/** API実行後ステータスチェック対象IDリスト **/
		public List<String> checkIdList = new ArrayList<>();
		/** API実行、もしくはステータスチェック成功対象IDリスト **/
		public List<String> successIdList = new ArrayList<>();
		/** API実行の失敗対象IDリスト **/
		public List<String> failedIdList = new ArrayList<>();
		/** API実行後ステータスチェック対象ロケーションIDごとの対象IDマップ **/
		public Map<String, List<String>> checkLocationIdMap = new ConcurrentHashMap<>();
		/** API実行および実行後のステータスチェックすべてに成功したかどうか **/
		public boolean isSuccess() {
			if (checkIdList.isEmpty() && failedIdList.isEmpty()) {
				return true;
			}
			return false;
		}
	}

	/**
	 * リソース制御ジョブを実行するスレッドクラス
	 */
	private static class ResourceControlJobTask extends Thread {

		/** ロガー */
		private static Log tasklog = LogFactory.getLog(ResourceControlJobTask.class);

		/** 実行指示情報 */
		private RunInstructionInfo runInstructionInfo = null;

		/** 
		 * ジョブの進行状態（※nullは未実行扱い）
		 * @see com.clustercontrol.jobmanagement.bean.ResourceJobConditionConstant
		 */
		private Integer runCondition = null;

		/**
		 * コンストラクタ
		 * @param runInstructionInfo 実行指示情報
		 */
		public ResourceControlJobTask(RunInstructionInfo runInstructionInfo) {
			this.runInstructionInfo = runInstructionInfo;
		}

		/**
		 * コンストラクタ
		 * @param runInstructionInfo 実行指情報
		 * @param runCondition リソース制御ジョブの進行状態
		 */
		public ResourceControlJobTask(RunInstructionInfo runInstructionInfo, Integer runCondition) {
			this.runInstructionInfo = runInstructionInfo;
			this.runCondition = runCondition;
		}

		@Override
		public void run() {

			// クラウドセッション生成
			try (SessionScope sessionScope = SessionScope.open()) {
				String userId = this.runInstructionInfo.getUser();
				HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, userId);
				Session.current().setHinemosCredential(new HinemosCredential(userId));
				HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, new AccessControllerBean().isAdministrator());
			} catch (Exception e) {
				throw new InternalManagerError(e);
			}

			tasklog.info("run() SessionID=" + this.runInstructionInfo.getSessionId()
			+ ", JobunitID=" + this.runInstructionInfo.getJobunitId()
			+ ", JobID=" + this.runInstructionInfo.getJobId()
			+ ", FacilityID=" + this.runInstructionInfo.getFacilityId()
			+ ", CommandType=" + this.runInstructionInfo.getCommandType()
			+ ", Command=" + this.runInstructionInfo.getCommand());

			JobInfoEntity jobInfo = null;
			JpaTransactionManager jtm = null;
			//実行結果
			ResourceResult result = new ResourceResult();
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				// ジョブセッション情報からJobInfoEntity、JobSessionNodeEntityの取得
				JobSessionJobEntity jobSession = QueryUtil.getJobSessionJobPK(
						this.runInstructionInfo.getSessionId(),
						this.runInstructionInfo.getJobunitId(),
						this.runInstructionInfo.getJobId());
				jobInfo = jobSession.getJobInfoEntity();

				// ジョブが未開始状態の場合
				// ジョブの開始状態を記録（コミット）
				if (this.runCondition == null) {
					saveStartJob(this.runInstructionInfo);
					this.runCondition = ResourceJobConditionConstant.START_JOB;

					// 処理状況（開始）を記録するため一旦コミット
					jtm.commit();
					jtm.close();
					jtm = new JpaTransactionManager();
					jtm.begin();
				}

				// ジョブが開始状態の場合
				// クラウドリソース制御APIを実行して記録（コミット）
				if (this.runCondition == ResourceJobConditionConstant.START_JOB) {
					// Enum名をジョブノードメッセージにセットし、アクションを判別できるようにする
					Integer actionNumber = jobInfo.getResourceAction();
					ResourceJobActionEnum actionEnum = Arrays.asList(ResourceJobActionEnum.values())
							.stream()
							.filter(p -> p.getCode().equals(actionNumber))
							.findFirst().get();
					String actionName = toActionString(actionEnum);
					setResourceJobNodeMessage(this.runInstructionInfo, MessageConstant.RESOURCEJOB_API_START.getMessage(actionName));

					// ジョブノードメッセ―ジ（API開始）を記録するため一旦コミット
					jtm.commit();
					jtm.close();
					jtm = new JpaTransactionManager();
					jtm.begin();

					result = executeCloudAPI(jobInfo);

					setResourceJobNodeMessage(this.runInstructionInfo, MessageConstant.RESOURCEJOB_API_END.getMessage());

					saveCalledAPI(this.runInstructionInfo);

					this.runCondition = ResourceJobConditionConstant.EXEC_API;

					// 処理状況とジョブノードメッセージ（API完了）を記録するため一旦コミット
					jtm.commit();
					jtm.close();
					jtm = new JpaTransactionManager();
					jtm.begin();
				}

				// ジョブがAPI実行済み状態の場合
				// クラウドリソースAPIの実行結果を、指定時間待機しながら確認、その後結果をもとにジョブ終了
				if (this.runCondition == ResourceJobConditionConstant.EXEC_API) {
					setResourceJobNodeMessage(this.runInstructionInfo, MessageConstant.RESOURCEJOB_CONFIRM_START.getMessage());

					// ジョブノードメッセージ（状態確認開始）を記録するため一旦コミット
					jtm.commit();
					jtm.close();
					jtm = new JpaTransactionManager();
					jtm.begin();

					// ジョブの成否確認
					result = checkCloudAPISuccess(jobInfo, result);

					if (!result.checkIdList.isEmpty()) {
						// 状態確認期間、状態確認間隔の指定時間をもとにジョブの成否確認の繰り返し
						if (jobInfo.getResourceStatusConfirmInterval() > 0) {
							int roopCount = jobInfo.getResourceStatusConfirmTime() / jobInfo.getResourceStatusConfirmInterval();
							for (int i = 0; i < roopCount; i++) {
								if (result.checkIdList.isEmpty()) {
									break;
								} else {
									Thread.sleep(jobInfo.getResourceStatusConfirmInterval() * 1000);
									result = checkCloudAPISuccess(jobInfo, result);
								}
							}
						}
					}

					// ジョブ終了＋ジョブ状態保持レコード削除
					if (result.isSuccess()) {
						setResourceJobNodeMessage(this.runInstructionInfo, MessageConstant.RESOURCEJOB_CONFIRM_END.getMessage());
						endResourceControlJob(this.runInstructionInfo, String.format("success=[%s]", String.join(",", result.successIdList)), "", RunStatusConstant.END,
								jobInfo.getResourceSuccessValue(), false);
					} else {
						setResourceJobNodeMessage(this.runInstructionInfo, MessageConstant.RESOURCEJOB_CONFIRM_TIMEOUT.getMessage());
						endResourceControlJob(this.runInstructionInfo,
								String.format("success=[%s], failure=[%s] ", String.join(",", result.successIdList), String.join(",", result.failedIdList)),
								"Internal Error : Ex. Job timed out.",
								RunStatusConstant.ERROR,
								jobInfo.getResourceFailureValue(),
								false);
					}
				}
				jtm.commit();
			} catch (InterruptedException e) {
				// ジョブ停止、中断などでスレッド処理キャンセルされた場合にここに入る想定
				tasklog.info("ResourceJob cancel : "+ e.getClass().getSimpleName() + ", " + e.getMessage());

				// ジョブノードにジョブ停止メッセージ追加
				try {
					setResourceJobNodeMessage(this.runInstructionInfo, MessageConstant.RESOURCEJOB_INTERRUPTED.getMessage());
					jtm.commit();
				} catch (Exception e1) {
					tasklog.error("setResourceJobNodeMessage() : "+ e1.getClass().getSimpleName() + ", " + e1.getMessage(), e1);
				}

			} catch (Exception e) {
				Integer endValue = ResourceJobConstant.FAILURE_VALUE;
				if (jobInfo != null) {
					endValue = jobInfo.getResourceFailureValue();
				}
				if (jtm != null && jtm.isNestedEm()) {
					jtm.rollback();
				}
				tasklog.error("run() : "+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);

				// ジョブノードにエラー失敗メッセージ追加
				try {
					jtm = new JpaTransactionManager();
					jtm.begin();
					if (this.runCondition == ResourceJobConditionConstant.EXEC_API) {
						// 状態確認失敗（API実行完了している状態）
						setResourceJobNodeMessage(this.runInstructionInfo, MessageConstant.RESOURCEJOB_FAILURE_CONFIRM.getMessage(e.getMessage()));
					} else {
						// 上記以外はAPI実行失敗（状態確認まで進めていないため）
						setResourceJobNodeMessage(this.runInstructionInfo, MessageConstant.RESOURCEJOB_FAILURE_API.getMessage(e.getMessage()));
					}
					// ジョブ終了＋ジョブ状態保持レコード削除
					endResourceControlJob(this.runInstructionInfo, "", e.getMessage(), RunStatusConstant.ERROR, endValue, false);

					jtm.commit();
				} catch (Exception e1) {
					tasklog.error("setResourceJobNodeMessage() : "+ e1.getClass().getSimpleName() + ", " + e1.getMessage(), e1);
				}
			} finally {
				if (jtm != null) {
					jtm.close();
				}
			}
		}
	}

	/**
	 * クラウドリソース制御API実行
	 */
	private static ResourceResult executeCloudAPI(JobInfoEntity jobInfo) throws FacilityNotFound, CloudManagerException, HinemosUnknown, InvalidUserPass, InvalidRole {

		ResourceResult result = null;
		try(RestSessionScope sessionScope = RestSessionScope.open()) {

			if (jobInfo.getResourceType().equals(ResourceJobTypeEnum.STORAGE.getCode())) {
				// ストレージ対するジョブ
				if (jobInfo.getResourceAction().equals(ResourceJobActionEnum.TYPE_ATTACH.getCode())) {
					result = attachStorage(jobInfo);
				} else if (jobInfo.getResourceAction().equals(ResourceJobActionEnum.TYPE_DETACH.getCode())){
					result = detachStorage(jobInfo);
				} else if (jobInfo.getResourceAction().equals(ResourceJobActionEnum.TYPE_SNAPSHOT.getCode())){
					result = snapshotStorage(jobInfo);
				}
			} else {
				// コンピュートに対するジョブ
				if (jobInfo.getResourceAction().equals(ResourceJobActionEnum.TYPE_POWERON.getCode())) {
					result = powerOnCompute(jobInfo);
				} else if (jobInfo.getResourceAction().equals(ResourceJobActionEnum.TYPE_POWEROFF.getCode())){
					result = powerOffCompute(jobInfo);
				} else if (jobInfo.getResourceAction().equals(ResourceJobActionEnum.TYPE_SUSPEND.getCode())){
					result = suspendCompute(jobInfo);
				} else if (jobInfo.getResourceAction().equals(ResourceJobActionEnum.TYPE_REBOOT.getCode())){
					result = rebootCompute(jobInfo);
				} else if (jobInfo.getResourceAction().equals(ResourceJobActionEnum.TYPE_SNAPSHOT.getCode())){
					result = snapshotCompute(jobInfo);
				}
			}
		}
		return result;
	}

	/**
	 * クラウドリソース制御APIを実行した結果、成功しているかを確認<BR>
	 * コンピュートノードの起動、停止、再起動、サスペンドのみ確認可能<BR>
	 */
	private static ResourceResult checkCloudAPISuccess(JobInfoEntity jobInfo, ResourceResult result)
			throws CloudManagerException {

		try(RestSessionScope sessionScope = RestSessionScope.open()) {

			if (!jobInfo.getResourceType().equals(ResourceJobTypeEnum.STORAGE.getCode())) {
				if (jobInfo.getResourceAction().equals(ResourceJobActionEnum.TYPE_POWERON.getCode())) {
					result = isSpecifiedStatus(jobInfo, InstanceStatus.running, result);
				} else if (jobInfo.getResourceAction().equals(ResourceJobActionEnum.TYPE_POWEROFF.getCode())) {
					result = isSpecifiedStatus(jobInfo, InstanceStatus.stopped, result);
				} else if (jobInfo.getResourceAction().equals(ResourceJobActionEnum.TYPE_SUSPEND.getCode())) {
					result = isSpecifiedStatus(jobInfo, InstanceStatus.suspend, result);
				} else if (jobInfo.getResourceAction().equals(ResourceJobActionEnum.TYPE_REBOOT.getCode())) {
					result = isSpecifiedStatus(jobInfo, InstanceStatus.running, result);
				}
			}
		}

		return result;
	}

	/**
	 * リソース制御ジョブが開始したことをDBに記録する
	 * @param runInstructionInfo ジョブ実行情報
	 */
	private static void saveStartJob(RunInstructionInfo runInstructionInfo) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			ResourceJobRunConditionEntity resourceJobRunConditionEntity = new ResourceJobRunConditionEntity();
			resourceJobRunConditionEntity.setSessionId(runInstructionInfo.getSessionId());
			resourceJobRunConditionEntity.setJobunit(runInstructionInfo.getJobunitId());
			resourceJobRunConditionEntity.setJobId(runInstructionInfo.getJobId());
			resourceJobRunConditionEntity.setRunCondition(ResourceJobConditionConstant.START_JOB);
			jtm.checkEntityExists(ResourceJobRunConditionEntity.class, resourceJobRunConditionEntity.getId());
			jtm.getEntityManager().persist(resourceJobRunConditionEntity);
		}
	}

	/**
	 * リソース制御ジョブがAPI実行したことをDBに記録する<BR>
	 * @param runInstructionInfo ジョブ実行情報
	 */
	private static void saveCalledAPI(RunInstructionInfo runInstructionInfo) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			// クエリ作成
			HinemosEntityManager em = jtm.getEntityManager();
			TypedQuery<ResourceJobRunConditionEntity> query = em.createNamedQuery(
					ResourceJobRunConditionEntity.findResourceJobRunCondition, ResourceJobRunConditionEntity.class);
			query.setParameter("sessionId", runInstructionInfo.getSessionId());
			query.setParameter("jobunit", runInstructionInfo.getJobunitId());
			query.setParameter("jobId", runInstructionInfo.getJobId());

			// クエリ実行
			ResourceJobRunConditionEntity entity = null;
			entity = query.getSingleResult();

			// リソースジョブの進行状態を「API実行済」に更新
			if (entity != null) {
				entity.setRunCondition(ResourceJobConditionConstant.EXEC_API);
			}
		}
	}

	/**
	 * リソース制御ジョブが終了したことをDBに記録する（該当レコード物理削除）<BR>
	 * @param runInstructionInfo ジョブ実行情報
	 */
	private static void saveEndJob(RunInstructionInfo runInstructionInfo) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			// クエリ作成
			HinemosEntityManager em = jtm.getEntityManager();
			TypedQuery<ResourceJobRunConditionEntity> query = em.createNamedQuery(
					ResourceJobRunConditionEntity.findResourceJobRunCondition, ResourceJobRunConditionEntity.class);
			query.setParameter("sessionId", runInstructionInfo.getSessionId());
			query.setParameter("jobunit", runInstructionInfo.getJobunitId());
			query.setParameter("jobId", runInstructionInfo.getJobId());

			// クエリ実行（対象レコードがない場合は例外が発生する）
			ResourceJobRunConditionEntity entity = null;
			try {
				entity = query.getSingleResult();
			} catch (Exception e) {
				log.warn("saveEndJob() : "+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}

			// レコード削除
			if (entity != null) {
				em.remove(entity);
			}
		}
	}

	/**
	 * スコープ指定でクラウドリソース制御API実行用のインタフェース
	 */
	private interface InstancesExecutor {
		void execute(IInstances instances, List<String> instanceIds) throws CloudManagerException;
		void throwException(List<String> failedLocations) throws CloudManagerException;
	}

	/**
	 * スコープ指定でクラウドリソース制御API実行
	 */
	private static void executeInstancesOperation(String cloudScopeId, String facilityId, String ownerRoleId,
			InstancesExecutor executor) throws CloudManagerException {

		Map<String, List<String>> locationMap = getTargetInstanceMap(cloudScopeId, facilityId);

		List<String> failedLocations = new ArrayList<>();
		CloudLoginUserEntity user = getCloudLoginUser(cloudScopeId, ownerRoleId);
		for (Map.Entry<String, List<String>> entry : locationMap.entrySet()) {
			try {
				IInstances instances = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(entry.getKey()));
				executor.execute(instances, entry.getValue());
			} catch (CloudManagerException e) {
				log.warn(e.getMessage(), e);
				failedLocations.add(entry.getKey());
			}
		}
		if (!failedLocations.isEmpty()) {
			executor.throwException(failedLocations);
		}
	}

	/**
	 * ジョブ対象となるロケーション・インスタンスのMapを取得
	 * @param cloudScopeId
	 * @param facilityId
	 * @return Key:ロケーションID、Value:インスタンスIDリスト
	 * @throws CloudManagerException
	 */
	private static Map<String, List<String>> getTargetInstanceMap(String cloudScopeId, String facilityId) throws CloudManagerException {
		Map<String, List<String>> locationMap = new HashMap<>();
		List<InstanceEntity> queryResults;
		try {
			boolean isNode = false;
			try {
				isNode = RepositoryControllerBeanWrapper.bean().isNode(facilityId);
			} catch (FacilityNotFound e) {
				throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
			}
			List<String> facilityIds = new ArrayList<>();
			if (isNode) {
				facilityIds.add(facilityId);
			} else {
				facilityIds.addAll(RepositoryControllerBeanWrapper.bean().getFacilityIdList(facilityId, RepositoryControllerBean.ALL, false));
			}
			if (facilityIds.isEmpty()) {
				log.info("There are no nodes in the specified scope. scopeID="+facilityId);
				return locationMap;
			}

			HinemosEntityManager em = Session.current().getEntityManager();
			TypedQuery<InstanceEntity> query = em.createNamedQuery(InstanceEntity.findInstancesByFacilityIds, InstanceEntity.class);
			query.setParameter("cloudScopeId", cloudScopeId);
			query.setParameter("facilityIds", facilityIds);
			queryResults = query.getResultList();

			for (InstanceEntity instanceEntiy : queryResults) {
				List<String> list = locationMap.get(instanceEntiy.getLocationId());
				if (list == null) {
					list = new ArrayList<>();
					locationMap.put(instanceEntiy.getLocationId(), list);
				}
				list.add(instanceEntiy.getResourceId());
			}
		} catch (HinemosUnknown e) {
			throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
		}
		return locationMap;
	}

	/**
	 * コンピュートノード：起動
	 */
	private static ResourceResult powerOnCompute(JobInfoEntity jobInfo)
			throws CloudManagerException {

		ResourceResult result = new ResourceResult();
		if (jobInfo.getResourceType().equals(ResourceJobTypeEnum.COMPUTE_COMPUTE_ID.getCode())) {
			//インスタンスＩＤを指定
			List<String> targetInstanceIds = Arrays.asList(jobInfo.getResourceTargetId());
			CloudLoginUserEntity user = getCloudLoginUser(jobInfo.getResourceCloudScopeId(), jobInfo.getJobSessionJobEntity().getOwnerRoleId());
			IInstances instances = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(jobInfo.getResourceLocationId()));
			// コンピュートノードの存在チェック
			instances.getInstance(jobInfo.getResourceTargetId());

			instances.powerOnInstances(targetInstanceIds);
			result.checkIdList.add(jobInfo.getResourceTargetId());
		} else {
			//スコープ、ノードのファシリティＩＤを指定
			InstancesExecutor powerOnExcecutor = new InstancesExecutor() {
				@Override
				public void execute(IInstances instances, List<String> instanceIds) throws CloudManagerException {
					instances.powerOnInstances(instanceIds);
					result.checkIdList.addAll(instanceIds);
				}

				@Override
				public void throwException(List<String> failedLocations) throws CloudManagerException {
					throw ErrorCode.CLOUDINSTANCE_FIAL_TO_POWERON_INSTANCE_BY_FACILITY.cloudManagerFault(jobInfo.getResourceCloudScopeId(),
							jobInfo.getResourceTargetId(), failedLocations.toString());
				}
			};
			executeInstancesOperation(jobInfo.getResourceCloudScopeId(), jobInfo.getResourceTargetId(), 
					jobInfo.getJobSessionJobEntity().getOwnerRoleId(), powerOnExcecutor);
		}
		return result;
	}

	/**
	 * コンピュートノード：停止
	 */
	private static ResourceResult powerOffCompute(JobInfoEntity jobInfo)
			throws CloudManagerException {

		ResourceResult result = new ResourceResult();
		if (jobInfo.getResourceType().equals(ResourceJobTypeEnum.COMPUTE_COMPUTE_ID.getCode())) {
			//インスタンスＩＤを指定
			List<String> targetInstanceIds = Arrays.asList(jobInfo.getResourceTargetId());
			CloudLoginUserEntity user = getCloudLoginUser(jobInfo.getResourceCloudScopeId(), jobInfo.getJobSessionJobEntity().getOwnerRoleId());
			IInstances instances = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(jobInfo.getResourceLocationId()));
			// コンピュートノードの存在チェック
			instances.getInstance(jobInfo.getResourceTargetId());

			instances.powerOffInstances(targetInstanceIds);
			result.checkIdList.add(jobInfo.getResourceTargetId());
		} else {
			//スコープ、ノードのファシリティＩＤを指定
			InstancesExecutor powerOffExcecutor = new InstancesExecutor() {
				@Override
				public void execute(IInstances instances, List<String> instanceIds) throws CloudManagerException {
					instances.powerOffInstances(instanceIds);
					result.checkIdList.addAll(instanceIds);
				}

				@Override
				public void throwException(List<String> failedLocations) throws CloudManagerException {
					throw ErrorCode.CLOUDINSTANCE_FIAL_TO_POWEROFF_INSTANCE_BY_FACILITY.cloudManagerFault(jobInfo.getResourceCloudScopeId(),
							jobInfo.getResourceTargetId(), failedLocations.toString());
				}
			};
			executeInstancesOperation(jobInfo.getResourceCloudScopeId(), jobInfo.getResourceTargetId(),
					jobInfo.getJobSessionJobEntity().getOwnerRoleId(), powerOffExcecutor);

		}
		return result;
	}

	/**
	 * コンピュートノード：一時停止
	 */
	private static ResourceResult suspendCompute(JobInfoEntity jobInfo)
			throws CloudManagerException {

		ResourceResult result = new ResourceResult();
		if (jobInfo.getResourceType().equals(ResourceJobTypeEnum.COMPUTE_COMPUTE_ID.getCode())) {
			//インスタンスＩＤを指定
			List<String> targetInstanceIds = Arrays.asList(jobInfo.getResourceTargetId());
			CloudLoginUserEntity user = getCloudLoginUser(jobInfo.getResourceCloudScopeId(), jobInfo.getJobSessionJobEntity().getOwnerRoleId());
			IInstances instances = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(jobInfo.getResourceLocationId()));
			// コンピュートノードの存在チェック
			instances.getInstance(jobInfo.getResourceTargetId());
			
			instances.suspendInstances(targetInstanceIds);
			result.checkIdList.add(jobInfo.getResourceTargetId());
		} else {
			//スコープ、ノードのファシリティＩＤを指定
			InstancesExecutor suspendExcecutor = new InstancesExecutor() {
				@Override
				public void execute(IInstances instances, List<String> instanceIds) throws CloudManagerException {
					instances.suspendInstances(instanceIds);
					result.checkIdList.addAll(instanceIds);
				}

				@Override
				public void throwException(List<String> failedLocations) throws CloudManagerException {
					throw ErrorCode.CLOUDINSTANCE_FIAL_TO_SUSPEND_INSTANCE_BY_FACILITY.cloudManagerFault(jobInfo.getResourceCloudScopeId(),
							jobInfo.getResourceTargetId(), failedLocations.toString());
				}
			};
			executeInstancesOperation(jobInfo.getResourceCloudScopeId(), jobInfo.getResourceTargetId(),
					jobInfo.getJobSessionJobEntity().getOwnerRoleId(), suspendExcecutor);
		}
		return result;
	}

	/**
	 * コンピュートノード：再起動
	 */
	private static ResourceResult rebootCompute(JobInfoEntity jobInfo)
			throws CloudManagerException {

		ResourceResult result = new ResourceResult();
		if (jobInfo.getResourceType().equals(ResourceJobTypeEnum.COMPUTE_COMPUTE_ID.getCode())) {
			//インスタンスＩＤを指定
			List<String> targetInstanceIds = Arrays.asList(jobInfo.getResourceTargetId());
			CloudLoginUserEntity user = getCloudLoginUser(jobInfo.getResourceCloudScopeId(), jobInfo.getJobSessionJobEntity().getOwnerRoleId());
			IInstances instances = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(jobInfo.getResourceLocationId()));
			// コンピュートノードの存在チェック
			instances.getInstance(jobInfo.getResourceTargetId());

			instances.rebootInstances(targetInstanceIds);
			result.checkIdList.add(jobInfo.getResourceTargetId());
		} else {
			//スコープ、ノードのファシリティＩＤを指定
			InstancesExecutor rebootExcecutor = new InstancesExecutor() {
				@Override
				public void execute(IInstances instances, List<String> instanceIds) throws CloudManagerException {
					instances.rebootInstances(instanceIds);
					result.checkIdList.addAll(instanceIds);
				}

				@Override
				public void throwException(List<String> failedLocations) throws CloudManagerException {
					throw ErrorCode.CLOUDINSTANCE_FIAL_TO_REBOOT_INSTANCE_BY_FACILITY.cloudManagerFault(jobInfo.getResourceCloudScopeId(),
							jobInfo.getResourceTargetId(), failedLocations.toString());
				}
			};
			executeInstancesOperation(jobInfo.getResourceCloudScopeId(), jobInfo.getResourceTargetId(),
					jobInfo.getJobSessionJobEntity().getOwnerRoleId(), rebootExcecutor);
		}
		return result;
	}

	/**
	 * コンピュートノード：スナップショット作成
	 * スナップショット作成後、状態チェックしないため、スナップショット作成APIが実施できた時点で成功対象とみなす
	 */
	private static ResourceResult snapshotCompute(JobInfoEntity jobInfo)
			throws CloudManagerException {

		ResourceResult result = new ResourceResult();
		String cloudScopeId = jobInfo.getResourceCloudScopeId();
		String locationId = jobInfo.getResourceLocationId();
		String instanceId = jobInfo.getResourceTargetId();

		Date now = HinemosTime.getDateInstance();
		String snapshotName = instanceId + "-" + new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		String snapshotDescription = "Create by Hinemos at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);

		if (jobInfo.getResourceType().equals(ResourceJobTypeEnum.COMPUTE_COMPUTE_ID.getCode())) {
			//インスタンスＩＤを指定
			CloudLoginUserEntity user = getCloudLoginUser(cloudScopeId, jobInfo.getJobSessionJobEntity().getOwnerRoleId());
			IInstances instances = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId));
			// コンピュートノードの存在チェック
			instances.getInstance(instanceId);

			instances.takeInstanceSnapshot(instanceId, snapshotName, snapshotDescription, null);
			result.successIdList.add(jobInfo.getResourceTargetId());
		} else {
			//ノードのファシリティＩＤを指定
			InstancesExecutor snapshotExcecutor = new InstancesExecutor() {
				@Override
				public void execute(IInstances instances, List<String> instanceIds) throws CloudManagerException {
					instances.takeInstanceSnapshot(instanceIds.get(0), snapshotName, snapshotDescription, null);
					result.successIdList.add(instanceIds.get(0));
				}

				@Override
				public void throwException(List<String> failedLocations) throws CloudManagerException {
					throw ErrorCode.CLOUDINSTANCE_FIAL_TO_SNAPSHOT_INSTANCE_BY_FACILITY.cloudManagerFault(jobInfo.getResourceCloudScopeId(),
							jobInfo.getResourceTargetId(), failedLocations.toString());
				}
			};
			executeInstancesOperation(cloudScopeId, instanceId,
					jobInfo.getJobSessionJobEntity().getOwnerRoleId(), snapshotExcecutor);
		}
		return result;

	}

	/**
	 * ストレージ：アタッチ
	 * アタッチ操作後、状態チェックしないため、アタッチ操作APIが実施できた時点で成功対象とみなす
	 */
	private static ResourceResult attachStorage(JobInfoEntity jobInfo) throws CloudManagerException {

		ResourceResult result = new ResourceResult();
		String storageId = jobInfo.getResourceTargetId();
		String instanceId = jobInfo.getResourceAttachNode();
		String cloudScopeId = jobInfo.getResourceCloudScopeId();
		String locationId = jobInfo.getResourceLocationId();

		List<Option> options = new ArrayList<>();
		Option op = new Option();
		op.setName("deviceName");
		op.setValue(jobInfo.getResourceAttachDevice());
		options.add(op);

		CloudLoginUserEntity user = getCloudLoginUser(cloudScopeId, jobInfo.getJobSessionJobEntity().getOwnerRoleId());
		CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId))
				.attachStorage(instanceId, storageId, options);
		result.successIdList.add(storageId);
		return result;
	}

	/**
	 * ストレージ：デタッチ
	 * デタッチ操作後、状態チェックしないため、デタッチ操作APIが実施できた時点で成功対象とみなす
	 */
	private static ResourceResult detachStorage(JobInfoEntity jobInfo) throws CloudManagerException {

		ResourceResult result = new ResourceResult();
		List<String> storageIds = Arrays.asList(jobInfo.getResourceTargetId());
		String cloudScopeId = jobInfo.getResourceCloudScopeId();
		String locationId = jobInfo.getResourceLocationId();

		CloudLoginUserEntity user = getCloudLoginUser(cloudScopeId, jobInfo.getJobSessionJobEntity().getOwnerRoleId());
		CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId))
				.detachStorage(storageIds);
		result.successIdList.add(jobInfo.getResourceTargetId());
		return result;
	}

	/**
	 * ストレージ：スナップショット作成
	 * スナップショット作成後、状態チェックしないため、スナップショット作成APIが実施できた時点で成功対象とみなす
	 */
	private static ResourceResult snapshotStorage(JobInfoEntity jobInfo) throws CloudManagerException {

		ResourceResult result = new ResourceResult();
		Date now = HinemosTime.getDateInstance();
		String storageId = jobInfo.getResourceTargetId();
		String cloudScopeId = jobInfo.getResourceCloudScopeId();
		String locationId = jobInfo.getResourceLocationId();
		String name = storageId + "-" + new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		String description = "Create by Hinemos at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);

		CloudLoginUserEntity user = getCloudLoginUser(cloudScopeId, jobInfo.getJobSessionJobEntity().getOwnerRoleId());
		CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId))
				.takeStorageSnapshot(storageId, name, description, null);
		result.successIdList.add(storageId);
		return result;
	}

	/**
	 * コンピュートノードが指定された状態か確認する<BR>
	 * クラウドリソース制御API実行後の確認に使用する
	 */
	private static ResourceResult isSpecifiedStatus(JobInfoEntity jobInfo, InstanceStatus status, ResourceResult result)
			throws CloudManagerException {

		String cloudScopeId = jobInfo.getResourceCloudScopeId();
		String targetId = jobInfo.getResourceTargetId();

		if (jobInfo.getResourceType().equals(ResourceJobTypeEnum.COMPUTE_COMPUTE_ID.getCode())) {
			// インスタンス単体の場合
			try {
				List<String> instanceIds;
				if (result.checkLocationIdMap.isEmpty()) {
					instanceIds = Arrays.asList(targetId);
				} else {
					instanceIds = result.checkLocationIdMap.get(jobInfo.getResourceLocationId());
				}
				ActionMode.enterAutoDetection();
				CloudLoginUserEntity user = getCloudLoginUser(cloudScopeId, jobInfo.getJobSessionJobEntity().getOwnerRoleId());
				List<InstanceEntity> instanceEntities = CloudManager.singleton()
						.getInstances(user, user.getCloudScope().getLocation(jobInfo.getResourceLocationId()))
						.updateInstances(instanceIds);
				for (InstanceEntity instance : instanceEntities) {
					if (instance.getInstanceStatus() != status) {
						if (result.checkLocationIdMap.get(instance.getLocationId()) == null) {
							result.checkLocationIdMap.put(instance.getLocationId(), new ArrayList<>());
						}
						result.checkLocationIdMap.get(instance.getLocationId()).add(instance.getResourceId());
						if (!result.failedIdList.contains(instance.getResourceId())) {
							result.failedIdList.add(instance.getResourceId());
						}
					} else {
						if (result.checkLocationIdMap.get(instance.getLocationId()) != null) {
							result.checkLocationIdMap.get(instance.getLocationId()).remove(instance.getResourceId());
						}
						if (result.failedIdList.contains(instance.getResourceId())) {
							result.failedIdList.remove(instance.getResourceId());
						}
						result.checkIdList.remove(instance.getResourceId());
						result.successIdList.add(instance.getResourceId());
					}
					log.debug(String.format("instanceId=%s status=%s", instance.getResourceId(), instance.getInstanceStatus()));
				}
			} finally {
				ActionMode.leaveAutoDetection();
			}
		} else {
			Map<String, List<String>> locationMap;
			if (result.checkLocationIdMap.isEmpty()) {
				locationMap = getTargetInstanceMap(cloudScopeId, targetId);
			} else {
				locationMap = result.checkLocationIdMap;
			}
			for (Map.Entry<String, List<String>> entry : locationMap.entrySet()) {
				try {
					ActionMode.enterAutoDetection();
					CloudLoginUserEntity user = getCloudLoginUser(cloudScopeId, jobInfo.getJobSessionJobEntity().getOwnerRoleId());
					List<InstanceEntity> instanceEntities = CloudManager.singleton()
							.getInstances(user, user.getCloudScope().getLocation(entry.getKey()))
							.updateInstances(entry.getValue());
					for (InstanceEntity instance : instanceEntities) {
						if (instance.getInstanceStatus() != status) {
							if (result.checkLocationIdMap.get(instance.getLocationId()) == null) {
								result.checkLocationIdMap.put(instance.getLocationId(), new ArrayList<>());
							}
							result.checkLocationIdMap.get(instance.getLocationId()).add(instance.getResourceId());
							if (!result.failedIdList.contains(instance.getResourceId())) {
								result.failedIdList.add(instance.getResourceId());
							}
						} else {
							if (result.checkLocationIdMap.get(instance.getLocationId()) != null) {
								result.checkLocationIdMap.get(instance.getLocationId()).remove(instance.getResourceId());
							}
							if (result.failedIdList.contains(instance.getResourceId())) {
								result.failedIdList.remove(instance.getResourceId());
							}
							result.checkIdList.remove(instance.getResourceId());
							result.successIdList.add(instance.getResourceId());
						}
						log.debug(String.format("instanceId=%s status=%s", instance.getResourceId(), instance.getInstanceStatus()));
					}
				} finally {
					ActionMode.leaveAutoDetection();
				}
			}
		}
		return result;
	}

	/**
	 * ResourceJobActionEnumをジョブノードメッセージ用の文字列に変換する
	 */
	private static String toActionString(ResourceJobActionEnum action) {
		switch (action) {
		case TYPE_POWERON:
			return ACTION_POWERON;
		case TYPE_POWEROFF:
			return ACTION_POWEROFF;
		case TYPE_REBOOT:
			return ACTION_REBOOT;
		case TYPE_SUSPEND:
			return ACTION_SUSPEND;
		case TYPE_SNAPSHOT:
			return ACTION_SNAPSHOT;
		case TYPE_ATTACH:
			return ACTION_ATTACH;
		case TYPE_DETACH:
			return ACTION_DETACH;
		}
		return null;
	}
	
	private static CloudLoginUserEntity getCloudLoginUser(String cloudScopeId, String ownerRoleId)
			throws CloudManagerException{
		CloudLoginUserEntity ret = null;
		List<CloudLoginUserEntity> users = CloudManager.singleton().getLoginUsers()
				.getCloudLoginUserByRole(ownerRoleId);

		for(CloudLoginUserEntity user : users){
			if (cloudScopeId.equals(user.getCloudScopeId())) {
				ret = user;
				break;
			}
		}
		
		if(ret == null){
			throw ErrorCode.LOGINUSER_NOT_ASSIGNED_TO_ROLE.cloudManagerFault(cloudScopeId, ownerRoleId);
		}
		
		return ret;
	}
}
