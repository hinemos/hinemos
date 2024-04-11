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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.JobApprovalStatusConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidApprovalStatus;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RpaToolMasterNotFound;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.hinemosagent.util.AgentVersionManager;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;
import com.clustercontrol.jobmanagement.bean.DelayNotifyConstant;
import com.clustercontrol.jobmanagement.bean.FileCheckConstant;
import com.clustercontrol.jobmanagement.bean.JobApprovalInfo;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobEnvVariableInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkExpInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkInheritKeyInfo;
import com.clustercontrol.jobmanagement.bean.JobOutputInfo;
import com.clustercontrol.jobmanagement.bean.JobOutputType;
import com.clustercontrol.jobmanagement.bean.JobParamTypeConstant;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.jobmanagement.bean.RetryWaitStatusConstant;
import com.clustercontrol.jobmanagement.bean.RpaJobEndValueConditionInfo;
import com.clustercontrol.jobmanagement.bean.RpaStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.RunInstructionFileCheckInfo;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunOutputResultInfo;
import com.clustercontrol.jobmanagement.bean.RunResultInfo;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.model.JobCommandParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobEnvVariableInfoEntity;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobLinkInheritInfoEntity;
import com.clustercontrol.jobmanagement.model.JobOutputInfoEntity;
import com.clustercontrol.jobmanagement.model.JobParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobRpaEndValueConditionInfoEntity;
import com.clustercontrol.jobmanagement.model.JobRpaOptionInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntityPK;
import com.clustercontrol.jobmanagement.rpa.bean.LoginParameter;
import com.clustercontrol.jobmanagement.rpa.bean.RoboRunInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobEndValueConditionTypeConstant;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobErrorTypeConstant;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobReturnCodeConditionConstant;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobTypeConstant;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.jobmanagement.util.FromRunningAfterCommitCallback;
import com.clustercontrol.jobmanagement.util.JobLinkRcvJobWorker;
import com.clustercontrol.jobmanagement.util.JobLinkSendJobWorker;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.jobmanagement.util.JobSessionJobUtil;
import com.clustercontrol.jobmanagement.util.JobSessionNodeRetryController;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.jobmanagement.util.ParameterUtil;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.jobmanagement.util.RpaJobLoginWorker;
import com.clustercontrol.jobmanagement.util.RpaJobWorker;
import com.clustercontrol.jobmanagement.util.RunHistoryUtil;
import com.clustercontrol.jobmanagement.util.SendApprovalMail;
import com.clustercontrol.jobmanagement.util.SendTopic;
import com.clustercontrol.jobmanagement.util.ToRunningAfterCommitCallback;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rpa.model.RpaToolRunCommandMst;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.apllog.AplLogger;
import com.clustercontrol.xcloud.util.ResourceJobWorker;

import jakarta.persistence.EntityExistsException;

public class JobSessionNodeImpl {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( JobSessionNodeImpl.class );


	/**
	 * 指定されたジョブ配下のノードへの実行指示を行います。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId ジョブユニットID
	 * @param jobId ジョブID
	 * @param isExpNode true:ノードを展開する（スコープ内のノード情報を取得しなおす）、false:展開しない
	 * @return true：終了していた、false：実行された
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean startNode(String sessionId, String jobunitId, String jobId, boolean isExpNode) throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("startNode() : sessionId=" + sessionId + ", jobId=" + jobId + ", isExpNode=" + isExpNode);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		Collection<JobSessionNodeEntity> jobSessionNodeList = sessionJob.getJobSessionNodeEntities();

		// ノード／スコープを含めた情報を登録
		if((jobSessionNodeList == null || jobSessionNodeList.size() == 0)
			&& (isExpNode && sessionJob.getJobSessionEntity().getExpNodeRuntimeFlg())){
			CreateJobSession.createJobSessionNode(sessionJob.getJobInfoEntity());
			// 再度ジョブセッションノードを検索
			jobSessionNodeList = sessionJob.getJobSessionNodeEntities();
		}
		if(jobSessionNodeList == null || jobSessionNodeList.size() == 0){
			//ジョブ終了時関連処理（再帰呼び出し）
			try {
				new JobSessionJobImpl().endJob(sessionId, jobunitId, jobId, null, true);
			} catch (HinemosUnknown e) {
				m_log.warn("startNode() : no node. " + e.getMessage(), e);
			} catch (FacilityNotFound e) {
				m_log.warn("startNode() : no node. " + e.getMessage(), e);
			}
			return false;
		}

		//終了している場合はメソッドから抜ける。
		if (checkAllNodeEnd(sessionJob)) {
			return true;
		}

		//コマンドの実行が正常終了するまで順次リトライの場合
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		if(job.getProcessMode() == ProcessingMethodConstant.TYPE_RETRY){
			//実行中のノードが存在するかチェック
			for (JobSessionNodeEntity sessionNode : jobSessionNodeList) {
				// 停止処理中(終了遅延等)は後続のノードを実行させないため、「実行された」状態とする
				if (sessionNode.getStatus() == StatusConstant.TYPE_RUNNING 
						|| sessionNode.getStatus() == StatusConstant.TYPE_STOPPING) {
					return false;
				}
				// リトライ待機中の場合も実行中とみなす。
				// - 後の方で同様の判定を行っており、本処理は冗長に見えるが、もし本処理が無かった場合、
				// ノードの優先順位の変化により"リトライ待機中のノード"以外がorderedNodeListの先頭に来たケースにおいて、
				// TYPE_RETRY(1つのノードで実行)であるにも関わらず、2つのノードが実行中となる恐れがある。
				if (sessionNode.getStatus() == StatusConstant.TYPE_WAIT
						&& JobSessionNodeRetryController.isRegistered(sessionNode.getId())) {
					m_log.debug("startNode() : Waiting to retry. " + sessionNode.getId());
					return false;
				}
				// waitingキューに入っている場合も実行中とみなす (#11132)
				if (JobMultiplicityCache.existsInWaitingQueue(sessionNode.getId())) {
					m_log.debug("startNode() : Waiting in the queue. " + sessionNode.getId());
					return false;
				}
			}
		}

		ArrayList<JobSessionNodeEntity> orderedNodeList = new ArrayList<JobSessionNodeEntity>();
		ArrayList<String> validNodeList = AgentConnectUtil.getValidAgent();
		ArrayList<JobSessionNodeEntity> invalidNodeList = new ArrayList<JobSessionNodeEntity>();

		// 有効なノードがリストの前方にくるように並び替える
		for (JobSessionNodeEntity sessionNode : jobSessionNodeList) {
			if (validNodeList.contains(sessionNode.getId().getFacilityId())) {
				orderedNodeList.add(sessionNode);
			} else {
				invalidNodeList.add(sessionNode);
			}
		}

		// ノードの優先度順に並び替え
		Collections.sort(orderedNodeList, new JobPriorityComparator());
		Collections.sort(invalidNodeList, new JobPriorityComparator());

		// 有効なノードの末尾に、有効でないノードを挿入する
		orderedNodeList.addAll(invalidNodeList);

		if (m_log.isDebugEnabled()) {
			StringBuilder str = new StringBuilder();
			for (JobSessionNodeEntity sessionNode : orderedNodeList) {
				str.append(sessionNode.getNodeName()).append(" -> ");
			}
			m_log.debug("orderedNodeList: " + str);
		}

		for (JobSessionNodeEntity sessionNode : orderedNodeList) {
			// リトライ待機中の場合、実行指示は別タイミングで行うので、ここではスキップする
			if (sessionNode.getStatus() == StatusConstant.TYPE_WAIT
					&& JobSessionNodeRetryController.isRegistered(sessionNode.getId())) {
				m_log.debug("startNode() : Skip, waiting to retry " + sessionNode.getId());
				continue;
			}
			// ノード実行開始を試みる
			if (startNodeSub(sessionNode)) {
				// TYPE_RETRYの場合は、1つのノードで実行開始できれば良い
				if (job.getProcessMode() == ProcessingMethodConstant.TYPE_RETRY) {
					break;
				}
			}
		}
		return false;
	}

	/**
	 * 指定されたノードを実行開始する。
	 * <p>
	 * 本メソッドは"post-commitコールバック"を登録するが、自身ではトランザクションは制御しない。
	 * したがって、本メソッドの呼び出しを行うまでにトランザクションを開始しておく必要がある。
	 * 
	 * @param sessionNode
	 * @return true:実行開始(コールバックを設定)できた。false:実行開始できなかった。
	 */
	public boolean startNodeSub(JobSessionNodeEntity sessionNode) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			if (!jtm.getEntityManager().getTransaction().isActive()) {
				// 例外を投げても良いくらいだが、万が一バグが紛れてしまった場合に過剰な影響が出るのを防ぐため、
				// エラーログを記録してfalseを返すに留める。
				m_log.error("startNodeSub() : A transaction has not been begun. " + sessionNode.getId());
				return false;
			}
			// ノードが管理対象でない場合は実行しない
			if (sessionNode.getJobSessionJobEntity().getJobInfoEntity().getJobType() == JobConstant.TYPE_JOBLINKRCVJOB
					&& sessionNode.getStatus() == StatusConstant.TYPE_WAIT) {
				// ジョブ連携待機ジョブは、セッションノードのスコープ（もしくはノード）の存在確認
				boolean valid = false;
				try {
					valid = new RepositoryControllerBean().getFacilityEntityByPK(sessionNode.getId().getFacilityId()).getValid();
				} catch (FacilityNotFound | InvalidRole e) {
					m_log.warn("startNodeSub() : " + e.getMessage());
					valid = false;
				} catch (HinemosUnknown e) {
					m_log.warn("startNodeSub() : " + e.getMessage());
					valid = false;
				}
				if (!valid) {
					m_log.debug("startNodeSub() : node is not managed. " + sessionNode.getId());
					sessionNode.setStatus(StatusConstant.TYPE_NOT_MANAGED);
				}
			} else if (sessionNode.getJobSessionJobEntity().getJobInfoEntity().getJobType() != JobConstant.TYPE_APPROVALJOB
					&& sessionNode.getJobSessionJobEntity().getJobInfoEntity().getJobType() != JobConstant.TYPE_RESOURCEJOB
					&& !(sessionNode.getJobSessionJobEntity().getJobInfoEntity().getJobType() == JobConstant.TYPE_RPAJOB
						&& sessionNode.getJobSessionJobEntity().getJobInfoEntity().getRpaJobType() == RpaJobTypeConstant.INDIRECT)
					&& sessionNode.getStatus() == StatusConstant.TYPE_WAIT
					&& !(isMonitorRpaAccountJob(sessionNode))
					&& !checkManaged(sessionNode)) {
				m_log.debug("startNodeSub() : node is not managed. " + sessionNode.getId());
				sessionNode.setStatus(StatusConstant.TYPE_NOT_MANAGED);
			}
			if (sessionNode.getStatus() == StatusConstant.TYPE_NOT_MANAGED) {
				return false;
			}
			if (!checkMultiplicity(sessionNode)) {
				m_log.debug("runNode() : Multiplicity check was not passed. " + sessionNode.getId());
				return false;
			}
			jtm.addCallback(new ToRunningAfterCommitCallback(sessionNode.getId()));
			m_log.debug("runNode() : Set toRunning callback. " + sessionNode.getId());
		}
		return true;
	}

	/**
	 * ノードが管理対象外でないか確認する
	 * 
	 * @param sessionNode セッションノード
	 * @return true:管理対象、false:管理対象外、未存在
	 */
	private boolean checkManaged(JobSessionNodeEntity sessionNode) {
		boolean valid = false;
		try {
			valid = new RepositoryControllerBean().getNode(sessionNode.getId().getFacilityId()).getValid();
		} catch (FacilityNotFound e) {
			m_log.warn("checkManaged() : " + e.getMessage());
			valid = false;
		} catch (HinemosUnknown e) {
			m_log.warn("checkManaged() : " + e.getMessage());
			valid = false;
		}
		return valid;
	}
	
	private boolean checkMultiplicity(JobSessionNodeEntity sessionNode) {
		boolean startFlag = false;
		String sessionId = sessionNode.getId().getSessionId();
		String jobunitId = sessionNode.getId().getJobunitId();
		String jobId = sessionNode.getId().getJobId();
		if (sessionNode.getStatus() != StatusConstant.TYPE_WAIT) {
			return false;
		}
		try {
			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

			// 多重度判定の対象ジョブではない場合は検証をスキップ
			if (!JobMultiplicityCache.isMultiplicityJob(sessionJob.getJobInfoEntity().getJobType())) {
				return true;
			}

			 // 多重度の検証を行う
		 	if (JobMultiplicityCache.isRunNowWithSession(sessionJob, sessionNode.getId().getFacilityId())) {
		 		return true;
		 	}
			
			if(sessionJob.getJobInfoEntity().getMultiplicityNotify() == null){
				// ここは通らないはず
				m_log.info("multiplicity notify is null");
				return false;
			}

			// 上限を超えたときは通知する
			m_log.debug("checkMultiplicity " + sessionJob.getJobInfoEntity().getMultiplicityNotify());
			if (sessionJob.getJobInfoEntity().getMultiplicityNotify().booleanValue()) {
				//通知処理
				new Notice().multiplicityNotify(sessionId, jobunitId, jobId,
						sessionJob.getJobInfoEntity().getMultiplicityOperation());
			}

			// 上限を超えたときは、待機、実行、終了のいずれか。
			int status = sessionJob.getJobInfoEntity().getMultiplicityOperation();
			switch (status) {
			case StatusConstant.TYPE_WAIT:
				startFlag = true;
				break;
			case StatusConstant.TYPE_END:
				//実行状態を終了にする
				startFlag = false;
				sessionNode.setStatus(StatusConstant.TYPE_END);
				sessionNode.setEndValue(sessionJob.getJobInfoEntity().getMultiplicityEndValue());
				setMessage(sessionNode, MessageConstant.MESSAGE_EXCEEDED_MULTIPLICITY_OF_JOBS.getMessage());

				if (checkAllNodeEnd(sessionJob)) {
					try {
						new JobSessionJobImpl().endJob(sessionId, jobunitId, jobId, sessionNode.getResult(), true);
					} catch (HinemosUnknown e) {
						m_log.warn("wait2running END " + e.getMessage(), e);
					} catch (FacilityNotFound e) {
						m_log.warn("wait2running END " + e.getMessage(), e);
					}
				}
				break;
			default:
				m_log.warn("wait2running " + status + " is unknown status");
				startFlag = true; // 想定外の値の場合は実行する。
			}
		} catch (InvalidRole e) {
			m_log.warn("wait2running " + e.getMessage());
			startFlag = true; // 想定外の場合は実行する。
		} catch (JobInfoNotFound e) {
			m_log.warn("wait2running " + e.getMessage());
			startFlag = true; // 想定外の場合は実行する。
		}
		return startFlag;
	}

	/**
	 * ノード詳細の中で待機中のものを実行中に遷移させる。
	 * JobMultiplicityCache.kick()およびkickWithoutQueue()以外から呼ばないこと。
	 *
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @param facilityId
	 * @return 0:実行、1:ジョブ詳細が実行中ではない、-1:実行しない
	 * @throws JobInfoNotFound
	 */
	public int wait2running(JobSessionNodeEntityPK pk) {
		int startCommand = -1;

		String sessionId = pk.getSessionId();
		String jobunitId = pk.getJobunitId();
		String jobId = pk.getJobId();
		String facilityId = pk.getFacilityId();

		JobSessionNodeEntity sessionNode = null;
		try {
			sessionNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);
		} catch (JobInfoNotFound e) {
			m_log.warn("wait2running " + e.getMessage());
			return -1;
		}

		// ノード詳細が起動失敗の場合は、waitQueueから削除させるため、意図とは異なるが1を返す。
		if(sessionNode.getStatus() == StatusConstant.TYPE_ERROR) {
				return 1;
		}
		//ノード詳細の実行状態が待機ではない、かつ終了ではない場合
		if(sessionNode.getStatus() != StatusConstant.TYPE_WAIT
				&& sessionNode.getStatus() != StatusConstant.TYPE_END){
				return -1;
		}
		
		JobSessionJobEntity sessionJobEntity = null;
		try {
			sessionJobEntity = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		} catch (JobInfoNotFound e) {
			m_log.warn("wait2running " + e.getMessage());
			return -1;
		} catch (InvalidRole e) {
			m_log.warn("wait2running " + e.getMessage());
			return -1;
		}
		
		//ジョブ詳細の実行状態が実行中でない場合、
		//waitQueueから削除し、false を返す
		if(sessionJobEntity.getStatus() != StatusConstant.TYPE_RUNNING){
			m_log.info("wait2running job detail is not running " + sessionNode.getId().getFacilityId() +
					"," + sessionNode.getId().getSessionId() + "," + sessionNode.getId().getJobunitId() + "," + sessionNode.getId().getJobId() +
					", status is " + StatusConstant.typeToMessageCode(sessionJobEntity.getStatus()));
			
			return 1;
		}

		//実行状態を実行中にする
		m_log.info("wait2running " + sessionNode.getId().getFacilityId() +
				"," + sessionNode.getId().getSessionId());
		sessionNode.setStatus(StatusConstant.TYPE_RUNNING);
		if(sessionJobEntity.getJobInfoEntity().getJobType() == JobConstant.TYPE_APPROVALJOB){
			sessionNode.setApprovalStatus(JobApprovalStatusConstant.TYPE_PENDING);
		}
		if (sessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_MONITORJOB
			&& sessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_APPROVALJOB
			&& sessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_RESOURCEJOB
			&& sessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_JOBLINKSENDJOB
			&& sessionJobEntity.getJobInfoEntity().getJobType() != JobConstant.TYPE_JOBLINKRCVJOB
			&& ! (sessionJobEntity.getJobInfoEntity().getJobType() == JobConstant.TYPE_RPAJOB
					&& sessionJobEntity.getJobInfoEntity().getRpaJobType() == RpaJobTypeConstant.INDIRECT)) {
			setMessage(sessionNode, MessageConstant.WAIT_AGENT_RESPONSE.getMessage());
		}

		try {
			//Topicに送信
			m_log.debug("startNode() : send RunInstructionInfo() : " +
					"sessionId=" + sessionId + ", jobId=" + jobId + ", facilityId=" + sessionNode.getId().getFacilityId());
			runJobSessionNode(sessionId, jobunitId, jobId, sessionNode.getId().getFacilityId());
			startCommand = 0;
		} catch (Exception e) {
			m_log.warn("startNode() RunInstructionInfo() send error : " +
					"sessionId=" + sessionId + ", jobId=" + jobId + ", facilityId=" + sessionNode.getId().getFacilityId() + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
		return startCommand;
	}

	private void runJobSessionNode(String sessionId, String jobunitId, String jobId, String facilityId) throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound, RpaToolMasterNotFound {
		m_log.debug("runJobSessionNode() : sessionId=" + sessionId + ", jobunitid=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId);
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);

		//実行指示情報を作成
		RunInstructionInfo instructionInfo = new RunInstructionInfo();
		instructionInfo.setSessionId(sessionJob.getId().getSessionId());
		instructionInfo.setJobunitId(sessionJob.getId().getJobunitId());
		instructionInfo.setJobId(sessionJob.getId().getJobId());
		instructionInfo.setFacilityId(sessionNode.getId().getFacilityId());

		//ソート
		job.getJobOutputInfoEntities().sort(new Comparator<JobOutputInfoEntity>() {
			@Override
			public int compare(JobOutputInfoEntity info1, JobOutputInfoEntity info2) {
				return info1.getId().getOutputType().compareTo(info2.getId().getOutputType());
			}
		});

		//ファイル出力情報の設定
		for (JobOutputInfoEntity outputInfoEntity : job.getJobOutputInfoEntities()) {
			if (!outputInfoEntity.getValid()) {
				continue;
			}
			JobOutputInfo outputInfo = new JobOutputInfo();
			String directory;
			String fileName;
			if (outputInfoEntity.getSameNormalFlg() != null && outputInfoEntity.getSameNormalFlg()) {
				JobOutputInfoEntity normalInfo = job.getJobOutputInfoEntities().get(JobOutputType.STDOUT.getCode());
				directory = normalInfo.getDirectory();
				fileName = normalInfo.getFileName();
			} else {
				directory = outputInfoEntity.getDirectory();
				fileName = outputInfoEntity.getFileName();
			}
			directory = ParameterUtil.replaceSessionParameterValue(sessionId, facilityId, directory);
			outputInfo.setDirectory(directory);
			fileName = ParameterUtil.replaceSessionParameterValue(sessionId, facilityId, fileName);
			outputInfo.setFileName(fileName);
			outputInfo.setAppendFlg(outputInfoEntity.getAppendFlg());
			outputInfo.setValid(outputInfoEntity.getValid());
			if (JobOutputType.STDOUT.getCode().equals(outputInfoEntity.getId().getOutputType())) {
				instructionInfo.setNormalJobOutputInfo(outputInfo);
			} else if (JobOutputType.STDERR.getCode().equals(outputInfoEntity.getId().getOutputType())) {
				instructionInfo.setErrorJobOutputInfo(outputInfo);
			}
		}

		//環境変数情報の設定
		List<JobEnvVariableInfo> envInfoList = new ArrayList<JobEnvVariableInfo>();
		for(JobEnvVariableInfoEntity envEntity : job.getJobEnvVariableInfoEntities()) {
			JobEnvVariableInfo envInfo = new JobEnvVariableInfo();
			envInfo.setEnvVariableId(envEntity.getId().getEnvVariableId());
			String value = envEntity.getValue();
			String replacedValue = ParameterUtil.replaceSessionParameterValue(sessionId, facilityId, value);
			envInfo.setValue(replacedValue);
			envInfo.setDescription(envEntity.getDescription());
			envInfoList.add(envInfo);
		}
		instructionInfo.setJobEnvVariableInfoList(envInfoList);

		if (job.getJobType() == JobConstant.TYPE_MONITORJOB) {
			instructionInfo.setCommand(CommandConstant.MONITOR);
			instructionInfo.setCommandType(CommandTypeConstant.NORMAL);

			try {
				// HinemosManager上でジョブ実行
				MonitorJobWorker.runJob(instructionInfo);

			} catch (Exception e) {
				m_log.warn("runJobSessionNode() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		} else if (job.getJobType() == JobConstant.TYPE_RESOURCEJOB) {
			// リソース制御ジョブの実行ユーザはコマンドジョブ扱いだったときと同じく、ジョブ作成者
			instructionInfo.setUser(job.getRegUser());
			instructionInfo.setCommand(CommandConstant.RESOURCE);
			instructionInfo.setCommandType(CommandTypeConstant.NORMAL);

			// HinemosManager上でジョブ実行
			try {
				ResourceJobWorker.runJob(instructionInfo);
				// セッションノードの開始日時をセット
				sessionNode.setStartDate(HinemosTime.currentTimeMillis());
			} catch (Exception e) {
				m_log.warn("runJobSessionNode() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		} else if (job.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB) {
			instructionInfo.setCommand(CommandConstant.JOB_LINK_SEND);
			instructionInfo.setCommandType(CommandTypeConstant.NORMAL);

			try {
				// HinemosManager上でジョブ実行
				JobLinkSendJobWorker.runJob(instructionInfo);

			} catch (Exception e) {
				m_log.warn("runJobSessionNode() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		} else if (job.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB) {
			instructionInfo.setCommand(CommandConstant.JOB_LINK_RCV);
			instructionInfo.setCommandType(CommandTypeConstant.NORMAL);

			try {
				// HinemosManager上でジョブ実行
				JobLinkRcvJobWorker.runJob(instructionInfo);

			} catch (Exception e) {
				m_log.warn("runJobSessionNode() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		}else if (job.getJobType() == JobConstant.TYPE_APPROVALJOB) {
			// 承認ジョブの場合、Topic送信せずに承認待ち状態へ
			sessionNode.setStartDate(HinemosTime.currentTimeMillis());
			String jobFacilityId = job.getFacilityId();
			// ジョブ変数のパラメータを置き換える
			// 承認依頼文
			String reqSentence = job.getApprovalReqSentence();
			reqSentence = ParameterUtil.replaceAllSessionParameterValue(
					sessionId,
					jobunitId,
					jobFacilityId,
					reqSentence);
			job.setApprovalReqSentence(reqSentence);
			
			// 承認依頼メール件名
			String mailTitle = job.getApprovalReqMailTitle();
			mailTitle = ParameterUtil.replaceAllSessionParameterValue(
					sessionId,
					jobunitId,
					jobFacilityId,
					mailTitle);
			// 承認依頼メール件名が256文字を超えている場合は、先頭256文字のみとする
			if(mailTitle.length() > 256){
				mailTitle = mailTitle.substring(0, 256);
			}
			job.setApprovalReqMailTitle(mailTitle);
			
			// 承認依頼メール本文
			String mailBody = job.getApprovalReqMailBody();
			mailBody = ParameterUtil.replaceAllSessionParameterValue(
					sessionId,
					jobunitId,
					jobFacilityId,
					mailBody);
			job.setApprovalReqMailBody(mailBody);

			setMessage(sessionNode, MessageConstant.WAIT_APPROVAL.getMessage());
			//メール送信
			SendApprovalMail sendMail = new SendApprovalMail();
			sendMail.sendRequest(job, sessionNode.getApprovalRequestUser());
		} else if (job.getJobType() == JobConstant.TYPE_FILECHECKJOB) {
			// ファイルチェックジョブの場合
			instructionInfo.setCommand(CommandConstant.FILE_CHECK);
			instructionInfo.setCommandType(CommandTypeConstant.NORMAL);
			// ファイルチェックジョブの実行指示情報を作成
			RunInstructionFileCheckInfo runFileCheckInfo = new RunInstructionFileCheckInfo();
			runFileCheckInfo.setDirectory(job.getDirectory());
			runFileCheckInfo.setFileName(job.getFileName());
			runFileCheckInfo.setCreateValidFlg(job.getCreateValidFlg());
			runFileCheckInfo.setCreateBeforeJobStartFlg(job.getCreateBeforeJobStartFlg());
			runFileCheckInfo.setDeleteValidFlg(job.getDeleteValidFlg());
			runFileCheckInfo.setModifyValidFlg(job.getModifyValidFlg());
			runFileCheckInfo.setModifyType(job.getModifyType());
			runFileCheckInfo.setNotJudgeFileInUseFlg(job.getNotJudgeFileInUseFlg());
			runFileCheckInfo.setSuccessEndValue(job.getSuccessEndValue());
			instructionInfo.setRunInstructionFileCheckInfo(runFileCheckInfo);

			// ジョブ開始時のシステムジョブ変数を格納
			ParameterUtil.registerSystemJobParamInfo(sessionJob, ParameterUtil.createParamInfo(
					instructionInfo.getRunInstructionFileCheckInfo(), instructionInfo.getJobId()), false);

			try {
				// Topicに送信
				SendTopic.put(instructionInfo, AgentVersionManager.VERSION_7_0);
			} catch (Exception e) {
				m_log.warn("runJobSessionNode() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}

		} else if (job.getJobType() == JobConstant.TYPE_RPAJOB) {
			instructionInfo.setCommand(CommandConstant.RPA);
			instructionInfo.setCommandType(CommandTypeConstant.NORMAL);
			if (job.getRpaJobType() == RpaJobTypeConstant.DIRECT) {
				// RPAシナリオジョブ（直接実行）
				// ジョブ変数置換用
				Function<String, String> replaceParam = source -> {
					if (source != null) {
						try {
							return ParameterUtil.replaceAllSessionParameterValue(sessionId, jobunitId,
									sessionNode.getId().getFacilityId(), source);
						} catch (Exception e) {
							m_log.warn("runJobSessionNode() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						}
					}
					return source;
				};
				instructionInfo.setRpaLogDirectory(replaceParam.apply(job.getRpaLogDirectory()));
				instructionInfo.setRpaLogFileName(replaceParam.apply(job.getRpaLogFileName()));
				instructionInfo.setRpaLogFileEncoding(replaceParam.apply(job.getRpaLogEncoding()));
				instructionInfo.setRpaLogFileReturnCode(replaceParam.apply(job.getRpaLogReturnCode()));
				instructionInfo.setRpaLogPatternHead(replaceParam.apply(job.getRpaLogPatternHead()));
				instructionInfo.setRpaLogPatternTail(replaceParam.apply(job.getRpaLogPatternTail()));
				instructionInfo.setRpaLogMaxBytes(job.getRpaLogMaxBytes());
				instructionInfo.setRpaDefaultEndValue(job.getRpaDefaultEndValue());
				// 存在チェック用にシナリオファイルパスをセット
				instructionInfo.setFilePath(replaceParam.apply(job.getRpaScenarioFilepath()));
				// マネージャからログインを行う場合はログイン完了までエージェントを待機させる時間を指定
				Integer loginWaitMills = 0;
				if (job.getRpaLoginFlg()) {
					// ログインされるまで待機する時間[ms] = コマンドのタイムアウト時間 + リトライ間隔 * リトライ回数
					 loginWaitMills = HinemosPropertyCommon.job_rpa_login_connection_timeout.getIntegerValue() 
							+ HinemosPropertyCommon.job_rpa_login_retry_interval.getIntegerValue() * job.getRpaLoginRetry();
				}
				instructionInfo.setRpaLoginWaitMills(loginWaitMills);
				// スクリーンショット取得終了値条件
				if (job.getRpaScreenshotEndValueFlg()) {
					instructionInfo.setRpaScreenshotEndValue(job.getRpaScreenshotEndValue());
					instructionInfo.setRpaScreenshotEndValueCondition(job.getRpaScreenshotEndValueCondition());
				}
				// 終了状態判定条件を設定（ログアウトするかどうかの判定で使用する）
				instructionInfo.setRpaNormalEndValueFrom(job.getNormalEndValueFrom());
				instructionInfo.setRpaNormalEndValueTo(job.getNormalEndValueTo());
				instructionInfo.setRpaWarnEndValueFrom(job.getWarnEndValueFrom());
				instructionInfo.setRpaWarnEndValueTo(job.getWarnEndValueTo());
				// 実行ファイルパスから実行ファイル名を取得
				String rpaExeName = StringUtils.substringAfterLast(replaceParam.apply(job.getRpaExeFilepath()), "\\");
				// RPAシナリオ実行オプションをソートした上でひとつの文字列に結合する
				List<JobRpaOptionInfoEntity> optionList = job.getJobRpaOptionInfoEntities();
				optionList.sort(new Comparator<JobRpaOptionInfoEntity> () {
					@Override
					public int compare(JobRpaOptionInfoEntity o1, JobRpaOptionInfoEntity o2) {
						return o1.getId().getOrderNo().compareTo(o2.getId().getOrderNo());
					}
				});
				StringBuilder options = new StringBuilder();
				for (JobRpaOptionInfoEntity option : optionList) {
					options.append(option.getOption());
					options.append(' ');
				}
				m_log.debug("rpaScenarioOptions=" + options.toString());
				instructionInfo.setRpaExeName(rpaExeName);
				// RPAツールエグゼキュータで実行するコマンドを取得
				RpaToolRunCommandMst commandMst = com.clustercontrol.rpa.util.QueryUtil.getRpaToolRunCommandMstPK(job.getRpaToolId());
				// シナリオ実行コマンドのパラメータを置換
				String execCommand = ParameterUtil.replaceRpaToolExecCommandParameter(commandMst.getExecCommand(),
						replaceParam.apply(job.getRpaExeFilepath()),
						replaceParam.apply(job.getRpaScenarioFilepath()),
						replaceParam.apply(options.toString()));
				// プロセス終了コマンドのパラメータを置換
				String destroyCommand = ParameterUtil.replaceRpaToolDestroyCommandParameter(commandMst.getDestroyCommand(), rpaExeName);
				// RPAツールエグゼキューターシナリオ実行情報を設定
				// プロセス終了を行うかどうかのフラグをHinemosプロパティから取得
				RoboRunInfo roboRunInfo = new RoboRunInfo(HinemosTime.currentTimeMillis(),
						job.getId().getSessionId(), job.getId().getJobunitId(), job.getId().getJobId(), facilityId, job.getRpaLoginUserId(),
						execCommand, destroyCommand, job.getRpaLoginFlg(), job.getRpaLogoutFlg(), 
						HinemosPropertyCommon.job_rpa_destroy_process.getBooleanValue());
				m_log.debug("roboRunInfo=" + roboRunInfo);
				instructionInfo.setRpaRoboRunInfo(roboRunInfo);
				// RPAシナリオ終了値判定条件を設定
				// orderNoの昇順でDTOをリストに格納する
				List<JobRpaEndValueConditionInfoEntity> endValueConditionEntityList = job.getJobRpaEndValueConditionInfoEntities();
				List<RpaJobEndValueConditionInfo> endValueConditionList = new ArrayList<>();
				endValueConditionEntityList.sort(new Comparator<JobRpaEndValueConditionInfoEntity> () {
					@Override
					public int compare(JobRpaEndValueConditionInfoEntity o1, JobRpaEndValueConditionInfoEntity o2) {
						return o1.getId().getOrderNo().compareTo(o2.getId().getOrderNo());
					}
				});
				for (JobRpaEndValueConditionInfoEntity endValueConditionEntity : endValueConditionEntityList) {
					RpaJobEndValueConditionInfo endValueCondition = new RpaJobEndValueConditionInfo();
					endValueCondition.setOrderNo(endValueConditionEntity.getId().getOrderNo());
					endValueCondition.setConditionType(endValueConditionEntity.getConditionType());
					endValueCondition.setPattern(replaceParam.apply(endValueConditionEntity.getPattern()));
					endValueCondition.setCaseSensitivityFlg(endValueConditionEntity.getCaseSensitivityFlg());
					endValueCondition.setProcessType(endValueConditionEntity.getProcessType());
					endValueCondition.setReturnCode(replaceParam.apply(endValueConditionEntity.getReturnCode()));
					endValueCondition.setReturnCodeCondition(endValueConditionEntity.getReturnCodeCondition());
					endValueCondition.setUseCommandReturnCodeFlg(endValueConditionEntity.getUseCommandReturnCodeFlg());
					endValueCondition.setEndValue(endValueConditionEntity.getEndValue());
					endValueCondition.setDescription(endValueConditionEntity.getDescription());
					endValueConditionList.add(endValueCondition);
				}
				instructionInfo.setRpaEndValueConditionInfoList(endValueConditionList);
				
				// シナリオの実行前にOSへログインする
				// ログアウトはシナリオ実行後にエージェント側でRPAツールエグゼキューターが行う
				if (job.getRpaLoginFlg()) {
					// 重複してログインが実行されることを防ぐため、履歴が無い場合のみログインを実行する
					if (RunHistoryUtil.findRunHistory(instructionInfo) == null) {
						NodeInfo info = NodeProperty.getProperty(facilityId);
						String ipAddress = info.getAvailableIpAddress();
						m_log.debug("runJobSessionNode() : Rpa Login, sessionId=" + instructionInfo.getSessionId() +
								", jobunitId=" + instructionInfo.getJobunitId() +
								", jobId=" + instructionInfo.getJobId() +
								", facilityId=" + instructionInfo.getFacilityId() +
								", ipAddress=" + ipAddress +
								", userId=" + job.getRpaLoginUserId() +
								", resolution=" + job.getRpaLoginResolution() +
								", retry=" + job.getRpaLoginRetry()); 
						setMessage(sessionNode, MessageConstant.MESSAGE_JOB_RPA_EXEC_LOGIN.getMessage());
						RpaJobLoginWorker.run(instructionInfo,
								new LoginParameter(ipAddress,
										replaceParam.apply(job.getRpaLoginUserId()),
										replaceParam.apply(job.getRpaLoginPassword()),
										job.getRpaLoginResolution()),
										job.getRpaLoginRetry());
					}
				}
				RunHistoryUtil.addRunHistory(instructionInfo);
				try {
					//Topicに送信
					SendTopic.put(instructionInfo, AgentVersionManager.VERSION_7_0);
				} catch (Exception e) {
					m_log.warn("runJobSessionNode() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
			} else {
				// RPAシナリオジョブ（間接実行）
				// 間接実行の場合、Topic送信せずにシナリオ実行完了待ち状態へ
				RpaJobWorker.runJob(instructionInfo);
				sessionNode.setStartDate(HinemosTime.currentTimeMillis());
				setMessage(sessionNode, MessageConstant.WAIT_RPA_SCENARIO_END.getMessage());
			}
		} else {
			String startCommand = job.getStartCommand();
	
			// ジョブ変数のパラメータを置き換える
			startCommand = ParameterUtil.replaceAllSessionParameterValue(
					sessionId,
					jobunitId,
					sessionNode.getId().getFacilityId(),
					job.getStartCommand());
			instructionInfo.setCommand(startCommand);
			instructionInfo.setSpecifyUser(job.getSpecifyUser());
			instructionInfo.setUser(job.getEffectiveUser());
			instructionInfo.setCommandType(CommandTypeConstant.NORMAL);
	
			//特殊コマンド
			if(instructionInfo.getCommand().equals(CommandConstant.ADD_PUBLIC_KEY) ||
					instructionInfo.getCommand().equals(CommandConstant.DELETE_PUBLIC_KEY)){
				//公開鍵設定
	
				//セッションIDとジョブIDから、セッションジョブを取得
				JobSessionJobEntity argumentSessionJob = QueryUtil.getJobSessionJobPK(
						sessionId, jobunitId,
						job.getArgumentJobId());
				String result = argumentSessionJob.getResult();
	
				//設定したい公開鍵のファシリティIDを設定
				instructionInfo.setPublicKey(result);
			}else if(instructionInfo.getCommand().equals(CommandConstant.GET_FILE_LIST)){
				//ファイルリスト取得
	
				//取得したいファイルリストのパスを設定
				instructionInfo.setFilePath(job.getArgument());
			}else if(instructionInfo.getCommand().equals(CommandConstant.GET_CHECKSUM)){
				//チェックサム取得
	
				//チェックサムを取得するファイルパスを設定
				instructionInfo.setFilePath(job.getArgument());
			}else if(instructionInfo.getCommand().equals(CommandConstant.CHECK_CHECKSUM)){
				//整合性チェック
	
				//セッションIDとジョブIDから、セッションジョブを取得
				JobSessionJobEntity argumentSessionJob =
						QueryUtil.getJobSessionJobPK(sessionId, jobunitId, job.getArgumentJobId());
				String result = argumentSessionJob.getResult();
	
				//チェックサムを設定
				instructionInfo.setCheckSum(result);
				//整合性チェックするファイルパスを設定
				instructionInfo.setFilePath(job.getArgument());
			} else if (instructionInfo.getJobId().endsWith(CreateHulftJob.HULOPLCMD)) {
				//セッションIDとジョブIDから、セッションジョブを取得
				JobSessionJobEntity argumentSessionJob = QueryUtil.getJobSessionJobPK(
						sessionId, jobunitId,
						job.getArgumentJobId());
				String result = argumentSessionJob.getResult();
	
				// TRIDを指定
				instructionInfo.setCommand(instructionInfo.getCommand() + " " + result);
			}

			try {
				//Topicに送信
				SendTopic.put(instructionInfo);

			} catch (Exception e) {
				m_log.warn("runJobSessionNode() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		}
	}

	/**
	 * ノード終了処理を行います。
	 *
	 * @param info 実行結果情報
	 * @return コマンドを実行してよい場合はtrue、コマンドを実行しないでほしい場合はfalse
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws FacilityNotFound
	 * @throws EntityExistsException
	 * @throws InvalidRole
	 */
	public boolean endNode(RunResultInfo info) throws HinemosUnknown, JobInfoNotFound, EntityExistsException, FacilityNotFound, InvalidRole {
		return endNode(info, null);
	}
	
	public boolean endNode(RunResultInfo info, RunOutputResultInfo outputInfo) throws HinemosUnknown, JobInfoNotFound, EntityExistsException, FacilityNotFound, InvalidRole {
		m_log.info("endNode() : sessionId=" + info.getSessionId() + ", jobunitId=" + info.getJobunitId() +
				", jobId=" + info.getJobId() + ", facilityId=" + info.getFacilityId() + ", commandType=" + info.getCommandType());

		//コマンドタイプチェック
		//NORMALの結果が返ってくる前に、STOPコマンドを実行すると、
		//STOPコマンドの結果の後にNORMALコマンドの結果が返ってくる。
		//その場合は、NORMALコマンドの結果を無視する。(状態が上書きされてしまうので。)
		JobSessionNodeEntity jobNode = QueryUtil.getJobSessionNodePK(info.getSessionId(), info.getJobunitId(),
				info.getJobId(), info.getFacilityId());
		int status = jobNode.getStatus();
		int commandType = info.getCommandType();
		if ((commandType == CommandTypeConstant.NORMAL && status != StatusConstant.TYPE_RUNNING) ||
				(commandType == CommandTypeConstant.STOP && status != StatusConstant.TYPE_STOPPING)) {
			// 実行終了が返ってきたが、終了遅延等で状態が実行中以外になっていた場合等は、
			// このルートを通る。
			m_log.info("ignore command, commandType=" + commandType + ", status=" + status);
			return false;
		}

		if(commandType == CommandTypeConstant.NORMAL || commandType == CommandTypeConstant.STOP 
				|| commandType == CommandTypeConstant.SCREENSHOT){
			if (!endNodeNormalStop(info, outputInfo)) {
				// ジョブの多重実行の場合はエージェントでジョブを実行させないようにfalseを返す
				return false;
			}
		}

		endNodeFinish(info.getSessionId(), info.getJobunitId(), info.getJobId(), info.getFacilityId(),
				info.getCommand(), info.getFileList());
		return true;
	}

	// 	 * @return コマンドを実行してよい場合はtrue、コマンドを実行しないでほしい場合はfalseを返す
	private boolean endNodeNormalStop(RunResultInfo info, RunOutputResultInfo outputInfo) throws JobInfoNotFound, FacilityNotFound, InvalidRole, HinemosUnknown {
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			//セッションIDとジョブIDから、セッションジョブを取得
			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(info.getSessionId(), info.getJobunitId(), info.getJobId());
			JobInfoEntity job = sessionJob.getJobInfoEntity();
			JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(info.getSessionId(),
					info.getJobunitId(), info.getJobId(), info.getFacilityId());

			boolean isSessionNodeRunning = false;
			if (sessionNode.getStatus() == StatusConstant.TYPE_RUNNING) {
				isSessionNodeRunning = true;
			}

			//実行状態で分岐
			if(info.getStatus() == RunStatusConstant.START){

				// RPAシナリオジョブのスクリーンショット取得
				if(info.getCommandType() == CommandTypeConstant.SCREENSHOT) {
					setMessage(sessionNode, MessageConstant.MESSAGE_JOB_RPA_SCREENSHOT_START.getMessage());
					return false;
				}

				//開始の場合

				if(sessionNode.getStartDate() == null) {
					//開始・再実行日時を設定
					sessionNode.setStartDate(info.getTime());
					if (job.getJobType() == JobConstant.TYPE_MONITORJOB) {
						setMessage(sessionNode, MessageConstant.WAIT_MONITOR_RESPONSE.getMessage());
					} else if (job.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB) {
						setMessage(sessionNode, MessageConstant.WAIT_JOBLINKSEND_END.getMessage());
					} else if (job.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB) {
						setMessage(sessionNode, MessageConstant.WAIT_JOBLINKRCV_END.getMessage());
						// 開始時に確認済みメッセージ番号をクリアする
						sessionJob.setJoblinkRcvCheckedPosition(null);
					} else if (job.getJobType() == JobConstant.TYPE_FILECHECKJOB) {
						// ファイルチェックジョブの場合
						List<String> validList = new ArrayList<>();
						if (job.getCreateValidFlg()) {
							if (job.getCreateBeforeJobStartFlg()) {
								validList.add(MessageConstant.EXISTS.getMessage());
							} else {
								validList.add(MessageConstant.CREATE.getMessage());
							}
						}
						if (job.getDeleteValidFlg()) {
							validList.add(MessageConstant.DELETE.getMessage());
						}
						if (job.getModifyValidFlg()) {
							if (job.getModifyType() == FileCheckConstant.TYPE_MODIFY_TIMESTAMP) {
								validList.add(MessageConstant.TIMESTAMP_MODIFY.getMessage());
							} else if (job.getModifyType() == FileCheckConstant.TYPE_MODIFY_FILESIZE) {
								validList.add(MessageConstant.FILE_SIZE_MODIFY.getMessage());
							}
						}
						StringBuilder editMessageBuilder = new StringBuilder();
						editMessageBuilder.append(MessageConstant.FILE_CHECK_STATUS_CHECKING.getMessage() + " ( ");
						editMessageBuilder.append(MessageConstant.FILE_CHECK_TYPE.getMessage() + ":" + String.join(",", validList) + " ");
						editMessageBuilder.append(MessageConstant.FILE_CHECK_TARGET_DIR.getMessage() + ":" + job.getDirectory()  + " ");
						editMessageBuilder.append(MessageConstant.FILE_CHECK_TARGET_FILE_REGEX.getMessage() + ":" + job.getFileName() + " )");
						setMessage(sessionNode,editMessageBuilder.toString());

						// ファイルチェック開始時のシステムジョブ変数を格納
						ParameterUtil.registerSystemJobParamInfo(sessionJob,
								ParameterUtil.createParamInfoFcStart(info.getJobId()), true);
					} else if (job.getJobType() == JobConstant.TYPE_RPAJOB) {
						setMessage(sessionNode, MessageConstant.WAIT_RPA_SCENARIO_END.getMessage());
					} else {
						setMessage(sessionNode, MessageConstant.WAIT_COMMAND_END.getMessage());
					}
					
					AgentInfo agentInfo = AgentConnectUtil.getAgentInfo(info.getFacilityId());
					if (agentInfo != null) {
						sessionNode.setStartupTime(agentInfo.getStartupTime());
						String instanceId = agentInfo.getInstanceId();
						if (instanceId == null) {
							instanceId = "";
						}
						sessionNode.setInstanceId(instanceId);
					} else {
						m_log.info("agentInfo is null");
					}

					//チェック中の場合
					m_log.debug("agent check OK : status=" + info.getStatus() + ", sessionId=" + info.getSessionId() + ", jobId=" + info.getJobId() + ", facilityId=" + info.getFacilityId());

				}else{
					m_log.info("endNodeSetStatus() : this messsage is already received. drop message." +
							" sessionId=" + info.getSessionId() +
							", jobId=" + info.getJobId() +
							", facilityId=" + info.getFacilityId());
					// 同一のジョブが複数実行されてしまう場合は、後発を実行させないようにfalseを返す
					return false;
				}
			}else{
				boolean retryFlag = true;

				//再実行判定
				if (info.getStatus() == RunStatusConstant.END) {
					if (checkRetryFinish(sessionJob, info.getEndValue())) {
						retryFlag = false;
					}
				} else {
					//エージェントタイムアウト等のエラーの場合こちらを通る
					if (AgentVersionManager.isUnsupportedVersionError(info)) {
						// 対象外バージョンのエラーの場合は再実行しない
						retryFlag = false;
					} else if (!job.getCommandRetryFlg().booleanValue()) {
						retryFlag = false;
					}
				}

				if(info.getStatus() == RunStatusConstant.END){
					// RPAシナリオジョブのスクリーンショット取得
					if(info.getCommandType() == CommandTypeConstant.SCREENSHOT) {
						setMessage(sessionNode, MessageConstant.MESSAGE_JOB_RPA_SCREENSHOT_END.getMessage());
						return false;
					}

					//終了の場合
					if (retryFlag && retryJob(sessionNode, sessionJob, info, job.getCommandRetry())) {
						//再実行あり
						m_log.info("Retry command : sessionId=" + sessionNode.getId().getSessionId() + ", jobunitId=" + sessionNode.getId().getJobunitId()+ ", jobId=" + sessionNode.getId().getJobId() +
												", facilityId=" + sessionNode.getId().getFacilityId() + ", runCount=" + (sessionNode.getErrorRetryCount() + 1));
						return false;
					}

					//開始・再実行日時が設定済みならば、終了処理を行う。
					if(sessionJob.getStartDate() != null || sessionNode.getStartDate() != null){
						if(sessionJob.getEndDate() == null ||
								sessionJob.getStatus() == StatusConstant.TYPE_RUNNING ||
								sessionJob.getStatus() == StatusConstant.TYPE_SUSPEND ||
								sessionJob.getStatus() == StatusConstant.TYPE_STOPPING){

							//実行状態バッファを設定
							if(sessionNode.getStatus() == StatusConstant.TYPE_RUNNING){
								//実行状態が実行中の場合、終了を設定
								sessionNode.setStatus(StatusConstant.TYPE_END);
								//終了・中断日時を設定
								sessionNode.setEndDate(info.getTime());
								// 収集データ更新
								CollectDataUtil.put(sessionNode);
							}else if(sessionNode.getStatus() == StatusConstant.TYPE_STOPPING){
								if(info.getCommandType() == CommandTypeConstant.STOP){
									//実行状態が停止処理中の場合、コマンド停止を設定
									sessionNode.setStatus(StatusConstant.TYPE_STOP);
								}
							}
							//承認ジョブの用のメッセージを設定
							if (job.getJobType() == JobConstant.TYPE_APPROVALJOB) {
								setMessage(sessionNode, info.getMessage());
							//RPAシナリオジョブの場合
							} else if (job.getJobType() == JobConstant.TYPE_RPAJOB) {
								//直接実行の場合
								if (job.getRpaJobType() == RpaJobTypeConstant.DIRECT) {
									//メッセージを設定
									if(info.getCommandType() == CommandTypeConstant.STOP){
										//プロセス終了を行うかのフラグに応じてメッセージを設定
										if (HinemosPropertyCommon.job_rpa_destroy_process.getBooleanValue()) {
											// シナリオを終了
											setMessage(sessionNode, MessageConstant.MESSAGE_JOB_RPA_STOP_SCENARIO.getMessage());
										} else {
											// シナリオは終了せず、ジョブのみ終了
											setMessage(sessionNode, MessageConstant.MESSAGE_JOB_RPA_STOP_JOB.getMessage());
										}
									} else {
										// エージェントからのメッセージと共に終了値判定結果のメッセージを表示
										setMessage(sessionNode, createRpaJobEndMessage(info));
									}
									// 実行履歴を削除
									RunInstructionInfo history = RunHistoryUtil.findRunHistory(info.getSessionId(),
											info.getJobunitId(), info.getJobId(), info.getFacilityId());
									if (history != null) {
										RunHistoryUtil.delRunHistory(history);
										m_log.debug("endNodeNormalStop() : delRunHistory, jobType=" + job.getJobType() + 
												", sessionId=" + info.getSessionId() +
												", jobunitId=" + info.getJobunitId() +
												", jobId=" + info.getJobId() +
												", facilityId=" + info.getFacilityId());
									}
									if (job.getRpaLoginFlg()) {
										// ログイン処理が継続中の場合は停止しておく
										RpaJobLoginWorker.waitStopAndCancel(info);
									}
									
								//間接実行の場合
								} else if (job.getRpaJobType() == RpaJobTypeConstant.INDIRECT){
									//メッセージを設定
									if(info.getCommandType() == CommandTypeConstant.STOP){
										if (job.getRpaStopType() == RpaStopTypeConstant.STOP_SCENARIO) {
											// シナリオを終了
											setMessage(sessionNode, MessageConstant.MESSAGE_JOB_RPA_STOP_SCENARIO.getMessage());
										} else {
											// シナリオは終了せず、ジョブのみ終了
											setMessage(sessionNode, MessageConstant.MESSAGE_JOB_RPA_STOP_JOB.getMessage());
										}
									} else if (info.getEndValue() != 0) {
										// 終了値が0以外の場合は異常終了
										setMessage(sessionNode, MessageConstant.MESSAGE_JOB_RPA_RUN_SCENARIO_ERROR.getMessage());
									} else {
										setMessage(sessionNode, MessageConstant.MESSAGE_JOB_RPA_SCENARIO_COMPLETED.getMessage());
									}
								}
							}
							else if (info.getCommandType() == CommandTypeConstant.STOP &&
									info.getStopType() == CommandStopTypeConstant.DESTROY_PROCESS) {
								// プロセス終了の場合
								//メッセージを設定
								setMessage(sessionNode, MessageConstant.JOB_PROCESS_SHUTDOWN.getMessage());

							} else if (job.getJobType() == JobConstant.TYPE_FILECHECKJOB) {
								// ファイルチェックジョブの場合
								// システムジョブ変数を格納
								ParameterUtil.registerSystemJobParamInfo(sessionJob,
										ParameterUtil.createParamInfo(info.getRunResultFileCheckInfo(), info.getJobId(),
												info.getFacilityId()),
										false);
								// 格納されているメッセージをそのまま表示する
								if (info.getMessage() != null && !info.getMessage().isEmpty()) {
									setMessage(sessionNode, info.getMessage());
								}

							} else {
								// プロセス終了以外の場合
								// コマンド終了時のジョブ変数を格納する
								if (job.getJobCommandParamInfoEntities() != null
										&& job.getJobCommandParamInfoEntities().size() > 0) {
									List<JobCommandParamInfoEntity> jobCommandParamInfoEntityList
										= job.getJobCommandParamInfoEntities();
									// セッションIDとセッションジョブIDから、ジョブ変数のリストを取得
									JobSessionJobEntity sessionJobEntity = QueryUtil.getJobSessionJobPK(info.getSessionId(),
											info.getJobunitId(),
											job.getJobSessionJobEntity().getJobSessionEntity().getJobId());
									List<JobParamInfoEntity> jobParamInfoList = sessionJobEntity.getJobInfoEntity().getJobParamInfoEntities();
									for (JobCommandParamInfoEntity jobCommandParamInfoEntity : jobCommandParamInfoEntityList) {
										String value = "";
										String createParamId = jobCommandParamInfoEntity.getId().getParamId() + ":" + info.getFacilityId();
										// 標準出力から取得する
										if (jobCommandParamInfoEntity.getJobStandardOutputFlg()) {
											if (info.getMessage() != null) {
												Matcher m = Pattern.compile(jobCommandParamInfoEntity.getValue()).matcher(info.getMessage());
												if (m.find()) {
													try {
														value = m.group(1);
													}
													catch (IndexOutOfBoundsException e) {
														m_log.warn(String.format(
															"not contain group paragraph in pattern for message."
															+ " facilityId=%s, jobunitId=%s, jobId=%s, paramId=%s, message=%s",
															info.getFacilityId(),
															info.getJobunitId(),
															info.getJobId(),
															jobCommandParamInfoEntity.getId().getParamId(),
															info.getMessage()));
													}
												}
												else {
													// マッチしない。
													m_log.debug(String.format(
														"variable not match. facilityId=%s, jobunitId=%s, jobId=%s, paramId=%s, message=%s",
														info.getFacilityId(),
														info.getJobunitId(),
														info.getJobId(),
														jobCommandParamInfoEntity.getId().getParamId(),
														info.getMessage()));
												}
											}
											else {
												// メッセージがない。
												m_log.warn(String.format(
													"Not foudnd previous post. facilityId=%s, jobunitId=%s, jobId=%s, paramId=%s, message=%s",
													info.getFacilityId(),
													info.getJobunitId(),
													info.getJobId(),
													jobCommandParamInfoEntity.getId().getParamId(),
													info.getMessage()));
											}
										}
										else {
											// 固定値とした場合
											value = jobCommandParamInfoEntity.getValue();
										}
										// 値へのジョブ変数適用
										value = ParameterUtil.replaceAllSessionParameterValue(
												job.getId().getSessionId(),
												info.getJobunitId(),
												info.getFacilityId(),
												value);
										// 重複チェック
										boolean chkFlg = false;
										for (JobParamInfoEntity jobParamInfo : jobParamInfoList) {
											if (jobParamInfo.getId().getJobId().equals(job.getJobSessionJobEntity().getJobSessionEntity().getJobId())
													&& jobParamInfo.getId().getJobunitId().equals(job.getId().getJobunitId())
													&& jobParamInfo.getId().getSessionId().equals(job.getId().getSessionId())
													&& jobParamInfo.getId().getParamId().equals(createParamId)) {
												jobParamInfo.setValue(value);
												chkFlg = true;
												break;
											}
										}
										// 重複していない場合は追加
										if (!chkFlg) {
											JobParamInfoEntity jobParamInfoEntity = new JobParamInfoEntity(job, createParamId);
											// ジョブIDにセッションジョブIDを指定する。
											jobParamInfoEntity.getId().setJobId(job.getJobSessionJobEntity().getJobSessionEntity().getJobId());
											jobParamInfoEntity.setParamType(JobParamTypeConstant.TYPE_SYSTEM_JOB);
											jobParamInfoEntity.setValue(value);
											em.persist(jobParamInfoEntity);
											jobParamInfoEntity.relateToJobInfoEntity(job);
											jobParamInfoList.add(jobParamInfoEntity);
											sessionJobEntity.getJobInfoEntity().setJobParamInfoEntities(jobParamInfoList);
										}
									}
								}
								// ジョブ連携待機ジョブの引継ぎ情報を格納する
								if (job.getJobLinkInheritInfoEntities() != null
										&& job.getJobLinkInheritInfoEntities().size() > 0) {
									List<JobLinkInheritInfoEntity> jobLinkInheritInfoEntityList = job.getJobLinkInheritInfoEntities();
									// セッションIDとセッションジョブIDから、ジョブ変数のリストを取得
									JobSessionJobEntity sessionJobEntity = QueryUtil.getJobSessionJobPK(info.getSessionId(),
											info.getJobunitId(),
											job.getJobSessionJobEntity().getJobSessionEntity().getJobId());
									List<JobParamInfoEntity> jobParamInfoList = sessionJobEntity.getJobInfoEntity().getJobParamInfoEntities();
									
									for (JobLinkInheritInfoEntity jobLinkInheritInfoEntity : jobLinkInheritInfoEntityList) {
										String createParamId = jobLinkInheritInfoEntity.getId().getParamId();
										String value = "";
										if (info.getJobLinkMessageInfo() != null) {
											JobLinkInheritKeyInfo keyInfo = JobLinkInheritKeyInfo.valueOf(jobLinkInheritInfoEntity.getKeyInfo());
											if (keyInfo == JobLinkInheritKeyInfo.SOURCE_FACILITY_ID) {
												// 送信元ファシリティID
												value = info.getJobLinkMessageInfo().getFacilityId();
											} else if (keyInfo == JobLinkInheritKeyInfo.SOURCE_IP_ADDRESS) {
												// 送信元IPアドレス
												value = info.getJobLinkMessageInfo().getIpAddress();
											} else if (keyInfo == JobLinkInheritKeyInfo.JOBLINK_MESSAGE_ID) {
												// ジョブ連携メッセージID
												value = info.getJobLinkMessageInfo().getJoblinkMessageId();
											} else if (keyInfo == JobLinkInheritKeyInfo.MONITOR_DETAIL_ID) {
												// 監視詳細
												value = info.getJobLinkMessageInfo().getMonitorDetailId();
											} else if (keyInfo == JobLinkInheritKeyInfo.PRIORITY) {
												// 重要度
												value = String.valueOf(info.getJobLinkMessageInfo().getPriority());
											} else if (keyInfo == JobLinkInheritKeyInfo.APPLICATION) {
												// アプリケーション
												value = info.getJobLinkMessageInfo().getApplication();
											} else if (keyInfo == JobLinkInheritKeyInfo.MESSAGE) {
												// メッセージ
												value = info.getJobLinkMessageInfo().getMessage();
											} else if (keyInfo == JobLinkInheritKeyInfo.MESSAGE_ORG) {
												// オリジナルメッセージ
												value = info.getJobLinkMessageInfo().getMessageOrg();
											} else if (keyInfo == JobLinkInheritKeyInfo.EXP_INFO) {
												// 拡張情報
												if (info.getJobLinkMessageInfo().getJobLinkExpInfo() != null) {
													for (JobLinkExpInfo expInfo : info.getJobLinkMessageInfo().getJobLinkExpInfo()) {
														if (expInfo.getKey().equals(jobLinkInheritInfoEntity.getExpKey())) {
															value = expInfo.getValue();
															break;
														}
													}
												}
											}
										}

										// 値へのジョブ変数適用
										// 重複チェック
										boolean chkFlg = false;
										for (JobParamInfoEntity jobParamInfo : jobParamInfoList) {
											if (jobParamInfo.getId().getJobId().equals(job.getJobSessionJobEntity().getJobSessionEntity().getJobId())
													&& jobParamInfo.getId().getJobunitId().equals(job.getId().getJobunitId())
													&& jobParamInfo.getId().getSessionId().equals(job.getId().getSessionId())
													&& jobParamInfo.getId().getParamId().equals(createParamId)) {
												jobParamInfo.setValue(value);
												chkFlg = true;
												break;
											}
										}
										// 重複していない場合は追加
										if (!chkFlg) {
											JobParamInfoEntity jobParamInfoEntity = new JobParamInfoEntity(job, createParamId);
											// ジョブIDにセッションジョブIDを指定する。
											jobParamInfoEntity.getId().setJobId(job.getJobSessionJobEntity().getJobSessionEntity().getJobId());
											jobParamInfoEntity.setParamType(JobParamTypeConstant.TYPE_SYSTEM_JOB);
											jobParamInfoEntity.setValue(value);
											em.persist(jobParamInfoEntity);
											jobParamInfoEntity.relateToJobInfoEntity(job);
											jobParamInfoList.add(jobParamInfoEntity);
											sessionJobEntity.getJobInfoEntity().setJobParamInfoEntities(jobParamInfoList);
										}
									}
								}
								//メッセージを設定
								setMessage(sessionNode, "stdout=" + info.getMessage() + ", stderr=" + info.getErrorMessage());
							}

							//終了値を設定
							sessionNode.setEndValue(info.getEndValue());

							//特殊コマンド
							if(info.getCommand().equals(CommandConstant.GET_PUBLIC_KEY)){
								//公開鍵取得
								sessionNode.setResult(info.getPublicKey());
							}else if(info.getCommand().equals(CommandConstant.GET_CHECKSUM)){
								//チェックサム取得
								sessionNode.setResult(info.getCheckSum());
							} else if (info.getJobId().endsWith(CreateHulftJob.UTLSEND) && CreateHulftJob.isHulftMode()) {
								//TRID取得
								sessionNode.setResult(info.getMessage());
							}
						}else{
							m_log.debug("endNodeSetStatus() : this messsage is already received. drop message." +
									" sessionId=" + info.getSessionId() +
									", jobId=" + info.getJobId() +
									", facilityId=" + info.getFacilityId());
						}
					}else{
						// 起動時刻が無い状態で停止通知を受信した場合
						m_log.info("endNodeSetStatus() : this messsage does not have start time. drop message." +
								" sessionId=" + info.getSessionId() +
								", jobId=" + info.getJobId() +
								", facilityId=" + info.getFacilityId());
					}
				}else if(info.getStatus() == RunStatusConstant.ERROR){
					// RPAシナリオジョブのスクリーンショット取得でエラーが発生
					if(info.getCommandType() == CommandTypeConstant.SCREENSHOT) {
						setMessage(sessionNode, MessageConstant.MESSAGE_JOB_RPA_SCREENSHOT_FAIL.getMessage());
						return false;
					}
					//失敗の場合
					if (retryFlag && retryJob(sessionNode, sessionJob, info, job.getCommandRetry())) {
						//再実行あり
						return false;
					}

					// RPAシナリオジョブ（直接実行）での異常発生時
					if (job.getJobType() == JobConstant.TYPE_RPAJOB 
							&& job.getRpaJobType() == RpaJobTypeConstant.DIRECT
							&& info.getRpaJobErrorType() != null
							&& sessionNode.getStatus() == StatusConstant.TYPE_RUNNING) {
						endAbnormalRpaJob(info, job, sessionNode);
					// RPAシナリオジョブ（間接実行）での異常発生時
					} else if (job.getJobType() == JobConstant.TYPE_RPAJOB 
							&& job.getRpaJobType() == RpaJobTypeConstant.INDIRECT
							&& sessionNode.getStatus() == StatusConstant.TYPE_RUNNING) {
						// ジョブを終了
						sessionNode.setStatus(StatusConstant.TYPE_END);
						//終了日時を設定
						sessionNode.setEndDate(HinemosTime.currentTimeMillis());
						//終了値を設定
						sessionNode.setEndValue(info.getEndValue());
						// 収集データ更新
						CollectDataUtil.put(sessionNode);
					} else {
						//実行状態、終了値、終了・中断日時を設定
						if(sessionNode.getStatus() == StatusConstant.TYPE_RUNNING){
							//エラー時に終了にする
							if(job.getMessageRetryEndFlg().booleanValue()){
								//実行状態が実行中の場合、実行状態バッファに終了を設定
								sessionNode.setStatus(StatusConstant.TYPE_END);

								//終了・中断日時を設定
								sessionNode.setEndDate(HinemosTime.currentTimeMillis());
								//終了値を設定
								sessionNode.setEndValue(job.getMessageRetryEndValue());
								// 収集データ更新
								CollectDataUtil.put(sessionNode);
							}else{
								//実行状態が実行中の場合、実行状態バッファに実行失敗を設定
								sessionNode.setStatus(StatusConstant.TYPE_ERROR);
							}
							if (job.getJobType() == JobConstant.TYPE_RPAJOB && job.getRpaJobType() == RpaJobTypeConstant.DIRECT
									&& info.getRpaJobErrorType() == null && job.getRpaLoginFlg()) {
								//RPAシナリオジョブ（直接実行）でマネージャ側判断での異常（エージェント停止等）にてログイン処理継続中なら停止を試行
								RpaJobLoginWorker.cancel(info);
							}
						}else if(sessionNode.getStatus() == StatusConstant.TYPE_STOPPING){
							//実行状態が停止処理中の場合、実行状態バッファにコマンド停止を設定
							sessionNode.setStatus(StatusConstant.TYPE_STOP);
							sessionNode.setEndValue(info.getEndValue());
						}
						//メッセージを設定
						setMessage(sessionNode, info.getMessage() + info.getErrorMessage());
					}
				}

				if (sessionNode.getStartDate() == null) {
					// ジョブ実行命令がノードに届いていない場合
					m_log.debug("set status buffer : status=" + info.getStatus() +
							", sessionId=" + info.getSessionId() + ", jobId=" + info.getJobId() + ", facilityId=" + info.getFacilityId());
				}

				//コマンドジョブのファイル出力設定の判定
				if (outputInfo != null && !outputInfo.getErorrTargetTypeList().isEmpty()) {
					for (JobOutputInfoEntity outputEnt : job.getJobOutputInfoEntities()) {
						//ファイル出力設定が有効だった場合
						Integer outputType = outputEnt.getId().getOutputType();
						if (outputEnt.getValid() && outputInfo.getErorrTargetTypeList().contains(outputType)) {
							String ssesionId = sessionJob.getId().getSessionId();
							String jobId = job.getId().getJobId();
							
							String failureOperationType = "";
							//失敗時状態遷移する場合
							if (outputEnt.getFailureOperationFlg()) {
								sessionJob.setEndDate(HinemosTime.currentTimeMillis());
								failureOperationType = OperationConstant.typeToMessageCode(outputEnt.getFailureOperationType());
								if (outputEnt.getFailureOperationType().equals(OperationConstant.TYPE_STOP_SET_END_VALUE) ||
										outputEnt.getFailureOperationType().equals(OperationConstant.TYPE_STOP_SET_END_VALUE_FORCE)) {
									sessionNode.setStatus(StatusConstant.TYPE_END_FAILED_OUTPUT);
								} else if (outputEnt.getFailureOperationType().equals(OperationConstant.TYPE_STOP_SUSPEND)) {
									sessionJob.setStatus(StatusConstant.TYPE_SUSPEND);
								}
							}
							
							String jobMessage;
							if (JobOutputType.STDOUT.getCode().equals(outputType)) {
								jobMessage = MessageConstant.MESSAGE_JOB_COMMAND_STDOUT_OUTPUT_FAILURE.getMessage()
											+ System.lineSeparator() + outputInfo.getStdoutErrorMessage();
							} else {
								jobMessage = MessageConstant.MESSAGE_JOB_COMMAND_STDERR_OUTPUT_FAILURE.getMessage()
											+ System.lineSeparator() + outputInfo.getStderrErrorMessage();
							}
							setMessage(sessionNode, jobMessage);

							//失敗時通知する場合
							if (outputEnt.getFailureNotifyFlg()) {
								String message = MessageConstant.MESSAGE_JOB_COMMAND_OUTPUT_FAILURE.getMessage(
										job.getId().getJobId(),
										job.getJobName(),
										sessionJob.getId().getSessionId());
								String messageorg;
								if (failureOperationType.isEmpty()) {
									messageorg = MessageConstant.MESSAGE_JOB_COMMAND_OUTPUT_FAILURE_ORG.getMessage(
											job.getId().getJobId(),
											job.getJobName(),
											sessionJob.getId().getSessionId(),
											failureOperationType);
								} else {
									//失敗時状態遷移する場合、通知のメッセージとオリジナルメッセージは同じ内容
									messageorg = message;
								}
								new Notice().notify(ssesionId, job.getId().getJobunitId(), jobId, outputEnt.getFailureNotifyPriority(), message, messageorg, NotifyTriggerType.JOB_COMMAND_OUTPUT_FAILED);
							}
						}
					}
				}

				//他の状態に遷移した場合は、キャッシュを更新する。
				if (isSessionNodeRunning &&
						sessionNode.getStatus() != StatusConstant.TYPE_RUNNING) {
					jtm.addCallback(new FromRunningAfterCommitCallback(sessionNode.getId()));
				}

			}
			return true;
		}
	}

	protected void endNodeFinish(String sessionId, String jobunitId, String jobId, String facilityId, String command, List<String> fileList)
			throws JobInfoNotFound, HinemosUnknown, EntityExistsException, FacilityNotFound, InvalidRole {
		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);
		m_log.debug("endNodeFinish() : status=" + sessionNode.getStatus() + ", " + sessionNode.getId());

		//実行状態チェック
		if(sessionNode.getStatus() == StatusConstant.TYPE_STOP){
			//実行状態がコマンド停止の場合
			if(sessionJob.getStatus() == StatusConstant.TYPE_STOPPING &&
					checkAllNodeStop(sessionJob)){
				//全ノード停止の場合
				//実行状態にコマンド停止を設定
				sessionJob.setStatus(StatusConstant.TYPE_STOP);
				//ジョブ停止関連処理
				new OperateStopOfJob().stopJob2(sessionId, jobunitId, jobId);
				//遅延通知状態を取得
				int flg = sessionJob.getDelayNotifyFlg();
				//遅延通知状態から操作済みフラグを取得
				int operationFlg = DelayNotifyConstant.getOperation(flg);
				if(operationFlg == DelayNotifyConstant.STOP_SET_END_VALUE || operationFlg == DelayNotifyConstant.STOP_SET_END_VALUE_FORCE){
					//操作済みフラグが停止[状態指定]の場合、停止[状態変更]を行う
					new OperateMaintenanceOfJob().maintenanceJob(
							sessionId,
							jobunitId,
							jobId,
							StatusConstant.TYPE_END_END_DELAY,
							job.getEndDelayOperationEndStatus(),
							job.getEndDelayOperationEndValue());
				}
			}
		}else{
			if(sessionJob.getJobInfoEntity().getProcessMode() == ProcessingMethodConstant.TYPE_RETRY) {
				//実行状態がコマンド停止以外の場合
				if(sessionJob.getStatus() == StatusConstant.TYPE_SUSPEND) {
					return;
				}
				if(checkAllNodeEnd(sessionJob)){
					m_log.info("endNodeFinish() : all nodes end (type retry) " + facilityId);
					Collection<JobSessionNodeEntity> nodeList = sessionJob.getJobSessionNodeEntities();
					for (JobSessionNodeEntity node : nodeList) {
						// 状態が待機のノードは終了に遷移させる。
						if (node.getStatus() == StatusConstant.TYPE_WAIT) {
							JobMultiplicityCache.removeWait(node.getId());
							node.setStatus(StatusConstant.TYPE_END);
							setMessage(node, "didn't execute");
						}
					}
					//ジョブ終了時関連処理（再帰呼び出し）
					new JobSessionJobImpl().endJob(sessionId, jobunitId, jobId,
							sessionNode.getResult(), true);
				} else {
					m_log.info("endNodeFinish() : next node " + sessionJob.getId());
					// 次のノードを実行させる。
					startNode(sessionJob.getId().getSessionId(),
							sessionJob.getId().getJobunitId(),
							sessionJob.getId().getJobId(),
							false);
				}
			} else if (sessionJob.getJobInfoEntity().getProcessMode() == ProcessingMethodConstant.TYPE_ANY_NODE) {
				// 実行状態が中断以外の場合
				if (sessionJob.getStatus() == StatusConstant.TYPE_SUSPEND) {
					return;
				}
				if (checkAllNodeEnd(sessionJob)) {
					m_log.info("endNodeFinish() : all nodes end (type any node) " + facilityId);
					if (sessionNode.getStatus().equals(StatusConstant.TYPE_END)
							|| sessionNode.getStatus().equals(StatusConstant.TYPE_MODIFIED)) {
						// 他のノードの停止処理を実行する
						endNodeByOtherNode(sessionId, jobunitId, jobId, sessionNode);
					}
					// ジョブ終了時関連処理（再帰呼び出し）
					new JobSessionJobImpl().endJob(sessionId, jobunitId, jobId, sessionNode.getResult(), true);
				}
			} else {
				//実行状態がコマンド停止以外の場合
				if(sessionJob.getStatus() != StatusConstant.TYPE_SUSPEND && checkAllNodeEnd(sessionJob)){
					//ジョブ終了の場合
					
					final boolean isFileTransferJob = CommandConstant.GET_FILE_LIST.equals(command);
					final boolean isFileTransferJobHulft = sessionJob.getId().getJobId().endsWith(CreateHulftJob.UTILIUPDT_S); 
					
					//ファイル転送ジョブ(HULFT以外)
					if (isFileTransferJob) {
						new CreateFileJob().createFileJobNet(
								sessionJob,
								fileList);
					} else if (isFileTransferJobHulft) {
						JobInfoEntity parentJobInfo = QueryUtil.getJobInfoEntityPK(sessionId, jobunitId, sessionJob.getParentJobId());
						if (parentJobInfo.getJobType() == JobConstant.TYPE_FILEJOB
							&& sessionJob.getJobSessionEntity().getExpNodeRuntimeFlg()) {
							new CreateHulftJob().createHulftFileJobNet(sessionId, jobunitId, sessionJob.getParentJobId());
						}
					}
					
					if ((isFileTransferJob || isFileTransferJobHulft) && sessionNode.getEndValue() != null && sessionNode.getEndValue() == 0) {
						// ファイル転送ジョブかつファイルリストの取得が成功
						JobSessionJobEntity parentJobSessionJobEntity = QueryUtil.getJobSessionJobPK(
								sessionJob.getId().getSessionId(),
								sessionJob.getParentJobunitId(),
								sessionJob.getParentJobId());

						String destFasilityId = parentJobSessionJobEntity.getJobInfoEntity().getDestFacilityId();
						List<String> nodeIdList = new RepositoryControllerBean().getExecTargetFacilityIdList(destFasilityId, sessionJob.getOwnerRoleId());
						if (nodeIdList.isEmpty()) {
							// 受信先ノードが存在しない場合
							setMessage(sessionNode, MessageConstant.MESSAGE_JOBFILETRANSFER_RECEIVE_NODE_NOT_EXISTS.getMessage(destFasilityId));
							sessionNode.setEndValue(9);
						}
					}
					
					//ジョブ終了時関連処理（再帰呼び出し）
					new JobSessionJobImpl().endJob(sessionId, jobunitId, jobId, sessionNode.getResult(), true);
				}
			}
		}
	}

	/**
	 * 他のノードが条件を満たした場合にノードの停止処理を実行する
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @param endedNode
	 *            条件を満たしたノード、nullの場合はこの処理内で条件を満たしたノードを探す
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	public void endNodeByOtherNode(String sessionId, String jobunitId, String jobId, JobSessionNodeEntity endedNode)
			throws JobInfoNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			// セッションIDとジョブIDから、セッションジョブを取得
			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

			List<JobSessionNodeEntity> sessionNodeList = sessionJob.getJobSessionNodeEntities();
			if (endedNode == null) {
				// 引数がnullの場合は条件を満たしているノードを探す
				// 複数ノードが条件を満たしている場合、ノードの優先度順に並び替えた最初のノードとする
				Collections.sort(sessionNodeList, new JobPriorityComparator());
				for (JobSessionNodeEntity sessionNode : sessionNodeList) {
					if (sessionNode.getEndValue() == null) {
						continue;
					}
					if (!sessionNode.getStatus().equals(StatusConstant.TYPE_END)
							&& !sessionNode.getStatus().equals(StatusConstant.TYPE_MODIFIED)) {
						continue;
					}
					if (JobSessionJobUtil.checkEndStatus(sessionJob,
							sessionNode.getEndValue()) == EndStatusConstant.TYPE_NORMAL) {
						endedNode = sessionNode;
						break;
					}
				}
				if (endedNode == null) {
					// このメソッドを呼び出す前にcheckAllNodeEnd()を通過するはずなので通常到達しない
					m_log.warn("endNodeByOtherNode() : endedNode is null. sessionId=" + sessionId + ", jobunitId="
							+ jobunitId + ", jobId=" + jobId);
					return;
				}
			}

			m_log.debug("endNodeByOtherNode() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId
					+ ", endedNode=" + endedNode.getId().getFacilityId());
			for (JobSessionNodeEntity sessionNode : sessionNodeList) {
				if (endedNode.getId().equals(sessionNode.getId())) {
					continue;
				}

				// ノードの実行状態が｛実行中、待機、停止処理中｝でなければ、停止処理は実行しない
				if (sessionNode.getStatus() != StatusConstant.TYPE_RUNNING
						&& sessionNode.getStatus() != StatusConstant.TYPE_WAIT
						&& sessionNode.getStatus() != StatusConstant.TYPE_STOPPING) {
					m_log.debug("endNodeByOtherNode() : skip. " + sessionNode.getId().toString() + ", status="
							+ StatusConstant.typeToMessageCode(sessionNode.getStatus()));
					continue;
				}

				m_log.info("endNodeByOtherNode() : stop node. sessionId=" + sessionId + ", jobunitId=" + jobunitId
						+ ", jobId=" + jobId + "facilityId=" + sessionNode.getId().getFacilityId());
				JobInfoEntity job = sessionJob.getJobInfoEntity();
				if (job.getJobType() == JobConstant.TYPE_FILECHECKJOB) {
					// 実行中から他の状態に遷移する場合は、キャッシュを更新する。
					if (sessionNode.getStatus() == StatusConstant.TYPE_RUNNING) {
						jtm.addCallback(new FromRunningAfterCommitCallback(sessionNode.getId()));
					} else if (sessionNode.getStatus() == StatusConstant.TYPE_WAIT) {
						JobMultiplicityCache.removeWait(sessionNode.getId());
					}

					if (sessionNode.getStatus() == StatusConstant.TYPE_RUNNING && sessionNode.getStartDate() != null) {
						// ノードが実行中の場合はエージェントに停止指示を送る
						// 開始日時がない場合はエージェント応答待ちなので送らない

						// 実行指示情報を作成
						RunInstructionInfo instructionInfo = new RunInstructionInfo();
						instructionInfo.setSessionId(sessionId);
						instructionInfo.setJobunitId(jobunitId);
						instructionInfo.setJobId(jobId);
						instructionInfo.setFacilityId(sessionNode.getId().getFacilityId());
						instructionInfo.setSpecifyUser(job.getSpecifyUser());
						instructionInfo.setUser(job.getEffectiveUser());
						instructionInfo.setCommand(CommandConstant.FILE_CHECK);
						instructionInfo.setCommandType(CommandTypeConstant.STOP);
						instructionInfo.setStopType(job.getStopType()); // DESTROY_PROCESS固定の想定
						// 環境変数情報は不要
						instructionInfo.setJobEnvVariableInfoList(new ArrayList<>());

						try {
							// Topicに送信
							SendTopic.put(instructionInfo);
						} catch (Exception e) {
							m_log.warn("endNodeByOtherNode() : RunInstructionInfo send error : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						}
					}

					// ノードの実行状態を終了に変更する
					// エージェントの状態によってはジョブの終了が遅れる可能性があるため、
					// エージェントの応答を待たずに終了し、エージェントから返ってきた停止の結果は無視する
					// 停止処理中のノードに関しても何らかの理由で停止処理が遅延している可能性を考慮し終了とする
					sessionNode.setStatus(StatusConstant.TYPE_END);
					sessionNode.setEndDate(HinemosTime.currentTimeMillis());
					sessionNode.setEndValue(job.getSuccessEndValue()); // 成功時の終了値
					setMessage(sessionNode, MessageConstant.FILE_CHECK_FINISHED_OTHER_NODE
							.getMessage(endedNode.getId().getFacilityId()));
				}
			}
		}
	}

	/**
	 * ジョブのタイムアウトによるノードの停止処理を実行します<BR>
	 * 
	 * @param sessionJob
	 * @return true:全てのノードが終了済み、false:一つでも停止中のノードが存在する
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	public boolean endAllNodeByJobTimeout(JobSessionJobEntity sessionJob) throws JobInfoNotFound, InvalidRole {
		m_log.debug("endAllNodeTimeout() : " + sessionJob.getId().toString());
		boolean allNodeEnded = true;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			Collection<JobSessionNodeEntity> sessionNodeList = sessionJob.getJobSessionNodeEntities();
			for (JobSessionNodeEntity sessionNode : sessionNodeList) {
				// ノードの実行状態が｛実行中、待機、停止処理中｝でなければ、停止処理は実行しない
				if (sessionNode.getStatus() != StatusConstant.TYPE_RUNNING
						&& sessionNode.getStatus() != StatusConstant.TYPE_WAIT
						&& sessionNode.getStatus() != StatusConstant.TYPE_STOPPING) {
					m_log.debug("endAllNodeTimeout() : skip. " + sessionNode.getId().toString() + ", status="
							+ StatusConstant.typeToMessageCode(sessionNode.getStatus()));
					if(!StatusConstant.isEndGroup(sessionNode.getStatus())) {
						// 終了状態でなければフラグは折っておく
						allNodeEnded = false;
					}
					continue;
				}

				JobInfoEntity job = sessionNode.getJobSessionJobEntity().getJobInfoEntity();
				if (job.getJobType() == JobConstant.TYPE_FILECHECKJOB) {
					// 実行中から他の状態に遷移する場合は、キャッシュを更新する。
					if (sessionNode.getStatus() == StatusConstant.TYPE_RUNNING) {
						jtm.addCallback(new FromRunningAfterCommitCallback(sessionNode.getId()));
					} else if (sessionNode.getStatus() == StatusConstant.TYPE_WAIT) {
						JobMultiplicityCache.removeWait(sessionNode.getId());
					}

					if (sessionNode.getStatus() == StatusConstant.TYPE_RUNNING && sessionNode.getStartDate() != null) {
						// ノードが実行中の場合はエージェントに停止指示を送る
						// 開始日時がない場合はエージェント応答待ちなので送らない

						m_log.info("endAllNodeTimeout() : Stop node. " + sessionNode.getId().toString());
						// 停止の実行指示情報を作成
						RunInstructionInfo instructionInfo = new RunInstructionInfo();
						instructionInfo.setSessionId(sessionNode.getId().getSessionId());
						instructionInfo.setJobunitId(sessionNode.getId().getJobunitId());
						instructionInfo.setJobId(sessionNode.getId().getJobId());
						instructionInfo.setFacilityId(sessionNode.getId().getFacilityId());
						instructionInfo.setSpecifyUser(job.getSpecifyUser());
						instructionInfo.setUser(job.getEffectiveUser());
						instructionInfo.setCommand(CommandConstant.FILE_CHECK);
						instructionInfo.setCommandType(CommandTypeConstant.STOP);
						instructionInfo.setStopType(job.getStopType()); // DESTROY_PROCESS固定の想定
						// 環境変数情報は不要
						instructionInfo.setJobEnvVariableInfoList(new ArrayList<>());

						try {
							// Topicに送信
							SendTopic.put(instructionInfo);
						} catch (Exception e) {
							m_log.warn("endAllNodeTimeout() : RunInstructionInfo send error : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						}
					}

					// ノードの実行状態を終了に変更する
					// エージェントの応答は待たず、エージェントから返ってきた停止指示の結果は無視する
					// 停止処理中のノードに関しても何らかの理由で停止処理が遅延している可能性を考慮し終了とする
					sessionNode.setStatus(StatusConstant.TYPE_END);
					sessionNode.setEndDate(HinemosTime.currentTimeMillis());
					sessionNode.setEndValue(job.getFailureEndValue()); // タイムアウト時の終了値
					// メッセージを設定する
					setMessage(sessionNode,
							MessageConstant.FILE_CHECK_TIMEOUT.getMessage(job.getFailureWaitTime().toString()));
				}
			}
		}
		return allNodeEnded;
	}

	/**
	 * 全ノードの停止チェックを行います。
	 *
	 * @param sessionJob セッションジョブ
	 * @return true：停止、false：未停止あり
	 */
	protected boolean checkAllNodeStop(JobSessionJobEntity sessionJob){
		m_log.debug("checkAllNodeStop() : sessionId=" + sessionJob.getId().getSessionId() +
				", jobId=" + sessionJob.getId().getJobId());

		boolean stop = true;
		for (JobSessionNodeEntity sessionNode : sessionJob.getJobSessionNodeEntities()) {
			//実行状態が停止処理中かチェック
			if(sessionNode.getStatus() == StatusConstant.TYPE_STOPPING ||
					sessionNode.getStatus() == StatusConstant.TYPE_RUNNING){
				stop = false;
				break;
			}
		}

		return stop;
	}

	/**
	 * 全ノードの終了チェックを行います。
	 *
	 * @param sessionJob セッションジョブ
	 * @return true：終了、false：未終了
	 */
	private boolean checkAllNodeEnd(JobSessionJobEntity sessionJob) {
		m_log.debug("checkAllNodeEnd() : sessionId=" + sessionJob.getId().getSessionId() + ", jobId=" + sessionJob.getId().getJobId());

		//終了フラグをfalseにする
		boolean end = false;

		if(sessionJob.getJobInfoEntity().getProcessMode() == ProcessingMethodConstant.TYPE_RETRY ||
				sessionJob.getJobInfoEntity().getProcessMode() == ProcessingMethodConstant.TYPE_ANY_NODE){
			//順次リトライの場合は、実行状態が正常終了のものが一つあれば終了とみなす。
			//いずれかのノードの場合も同様
			Integer endStatus = null;
			try {
				endStatus = new JobSessionJobImpl().checkEndStatus(sessionJob.getId().getSessionId(),
						sessionJob.getId().getJobunitId(), sessionJob.getId().getJobId());
			} catch (JobInfoNotFound e) {
				// ここは通らない。
				m_log.info("checkAllNodeEnd " + e.getMessage(), e);
			} catch (InvalidRole e) {
				m_log.info("checkAllNodeEnd " + e.getMessage(), e);
			} catch (Exception e) {
				m_log.info("checkAllNodeEnd " + e.getMessage(), e);
			}
			if (endStatus != null && endStatus == EndStatusConstant.TYPE_NORMAL) {
				return true;
			}
		}
		end = true;
		for (JobSessionNodeEntity sessionNode : sessionJob.getJobSessionNodeEntities()) {
			//実行状態が終了または変更済以外がなければOK。
			if(! StatusConstant.isEndGroup(sessionNode.getStatus())){
				end = false;
				break;
			}
		}
		return end;
	}


	/**
	 * エージェントタイムアウトチェックを行います。
	 */
	public HashMap<String, List<JobSessionNodeEntityPK>> checkTimeoutAll() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Integer> statusList = Arrays.asList(new Integer[]{
					StatusConstant.TYPE_RUNNING,
					StatusConstant.TYPE_STOPPING});
			Collection<JobSessionNodeEntity> collection = null;
			collection = em.createNamedQuery("JobSessionNodeEntity.findByStatusStartIsNull", JobSessionNodeEntity.class)
					.setParameter("statusList", statusList)
					.getResultList();

			HashMap<String, List<JobSessionNodeEntityPK>> map = new HashMap<String, List<JobSessionNodeEntityPK>>();

			for (JobSessionNodeEntity node : collection) {
				if (node.getStartDate() == null) {
					ArrayList<JobSessionNodeEntityPK> list = (ArrayList<JobSessionNodeEntityPK>)map.get(node.getId().getSessionId());
					if (list == null) {
						list = new ArrayList<JobSessionNodeEntityPK>();
					}
					list.add(node.getId());
					map.put(node.getId().getSessionId(), list);
				}
			}

			return map;
		}
	}

	/**
	 * エージェントタイムアウトチェックを行います。
	 *
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole 
	 */
	public void checkTimeout(JobSessionNodeEntityPK pk) throws JobInfoNotFound, InvalidRole {
		String sessionId = pk.getSessionId();
		String jobunitId = pk.getJobunitId();
		String jobId = pk.getJobId();
		String facilityId = pk.getFacilityId();
		JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);
		m_log.debug("checkTimeout() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId);

		// 監視ジョブの場合はタイムアウトチェック対象外とする。
		if (sessionNode.getJobSessionJobEntity().getJobInfoEntity().getJobType() == JobConstant.TYPE_MONITORJOB) {
			m_log.debug("checkTimeout() : job_type is monitor_job");
			return;
		}
		// リソース制御ジョブの場合はタイムアウトチェック対象外とする。
		if (sessionNode.getJobSessionJobEntity().getJobInfoEntity().getJobType() == JobConstant.TYPE_RESOURCEJOB) {
			m_log.debug("checkTimeout() : job_type is resource_control_job");
			return;
		}
		// ジョブ連携送信ジョブの場合はタイムアウトチェック対象外とする。
		if (sessionNode.getJobSessionJobEntity().getJobInfoEntity().getJobType() == JobConstant.TYPE_JOBLINKSENDJOB) {
			m_log.debug("checkTimeout() : job_type is joblinksend_job");
			return;
		}
		// ジョブ連携待機ジョブの場合はタイムアウトチェック対象外とする。
		if (sessionNode.getJobSessionJobEntity().getJobInfoEntity().getJobType() == JobConstant.TYPE_JOBLINKRCVJOB) {
			m_log.debug("checkTimeout() : job_type is joblinkrcv_job");
			return;
		}
		//待ち条件ジョブ判定
		if(sessionNode.getStatus() != StatusConstant.TYPE_RUNNING &&
				sessionNode.getStatus() != StatusConstant.TYPE_STOPPING ){
			// 1分に1回のタイムアウトチェック中にエージェントの応答があると、
			// このルートを通る。
			m_log.info("checkTimeout() : status is not running");
			return;
		}

		if(sessionNode.getStartDate() != null){
			// エージェントにジョブ実行命令を出した直後に、
			// 1分に1回のcheckTimeoutが走った場合は、このルートを通る。
			m_log.info("checkTimeout() : startDate is not null");
			return;
		}

		JobSessionJobEntity sessionJob = sessionNode.getJobSessionJobEntity();
		if (sessionJob.getRetryWaitStatus().equals(RetryWaitStatusConstant.WAIT)
				|| sessionJob.getRetryWaitStatus().equals(RetryWaitStatusConstant.PARENT_WAIT)) {
			// セッションジョブがリトライ待ち中のとき、
			// 1分に1回のcheckTimeoutが走った場合は、このルートを通る。
			m_log.debug("checkTimeout() : sessionJob is retry waiting");
			return;
		}
		int retry = sessionNode.getRetryCount();
		int messageRetry = sessionNode.getJobSessionJobEntity().getJobInfoEntity().getMessageRetry();
		if(retry >= messageRetry){
			//リトライ上限を超えたとき
			// 停止[状態指定](強制)以外での停止処理中の場合、停止させず、一度のみAgentTimeoutErrorのメッセージを出力する
			if (sessionNode.getStatus() == StatusConstant.TYPE_STOPPING &&
					sessionNode.getJobSessionJobEntity().getDelayNotifyFlg() != DelayNotifyConstant.STOP_SET_END_VALUE_FORCE){
				if (!sessionNode.getMessage().contains(MessageConstant.AGENT_TIMEOUT_ERROR.getMessage())){
					new JobSessionNodeImpl().setMessage(sessionNode,MessageConstant.AGENT_TIMEOUT_ERROR.getMessage() + " (" + retry + ")");
				}
				return;
			}
			//その他は、AgentTimeoutErrorとする。

			m_log.info("checkTimeout() : Agent Check NG : sessionId=" + sessionId + ", jobId=" + jobId + ", facilityId=" + facilityId);

			//実行結果情報を作成
			RunResultInfo info = new AgentTimeoutRunResultInfo();
			info.setSessionId(sessionId);
			info.setJobunitId(jobunitId);
			info.setJobId(jobId);
			info.setFacilityId(facilityId);
			info.setCommand("");
			int commandType = CommandTypeConstant.NORMAL;
			if(sessionNode.getStatus() == StatusConstant.TYPE_STOPPING){
				commandType = CommandTypeConstant.STOP;
			}
			info.setCommandType(commandType);
			info.setStatus(RunStatusConstant.ERROR);
			info.setMessage(MessageConstant.AGENT_TIMEOUT_ERROR.getMessage() + " (" + retry + ")");
			info.setErrorMessage("");
			try {
				endNode(info);
			} catch (InvalidRole | JobInfoNotFound e) {
				m_log.warn("setting status failure. (sessionId = " + info.getSessionId() + ", facilityId = " + info.getFacilityId()
						+ ", status = " + info.getStatus() + ", commandType = " + info.getCommandType() + ")"  + " (" + e.getClass().getName() + ")");
			} catch (Exception e) {
				m_log.warn("checkTimeout() RunresultInfo send error : sessionId=" + sessionId + ", jobId=" + jobId + ", facilityId=" + facilityId + ",  : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage() + " (" + e.getClass().getName() + ")", e);
			}
		}else{
			int retryCount = sessionNode.getRetryCount();
			retryCount++;
			sessionNode.setRetryCount(retryCount);
			try {
				//Topicに送信
				m_log.debug("checkTimeout() : send RunInstructionInfo() : sessionId=" + sessionId +
						", jobId=" + jobId + ", facilityId=" + facilityId + ", retry=" + retryCount);
				if(sessionNode.getStatus() == StatusConstant.TYPE_STOPPING){
					new OperateStopOfJob().stopNode(sessionId, jobunitId, jobId, facilityId, true);
				}else{
					runJobSessionNode(sessionId, jobunitId, jobId, facilityId);
				}
			} catch (InvalidRole | HinemosUnknown | FacilityNotFound e) {
				m_log.warn("checkTimeout() RunInstructionInfo() send error : sessionId=" + sessionId + ", jobId=" + jobId + ", facilityId=" + facilityId + " : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
			} catch (Exception e) {
				m_log.warn("checkTimeout() RunInstructionInfo() send error : sessionId=" + sessionId + ", jobId=" + jobId + ", facilityId=" + facilityId + " : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		}
	}

	// エージェントタイムアウト専用の実行結果情報
	private static class AgentTimeoutRunResultInfo extends RunResultInfo {
		private static final long serialVersionUID = -28880960176647898L;

		// 「エージェントタイムアウトによって生成されたRunResultInfoを識別する」という目的は、
		// 専用クラスであるということだけで果たせるため、特別な実装はない。
	}
	
	/**
	 * 監視ジョブのタイムアウト可否を確認します。
	 *
	 * @param jobSessionNodeEntity ジョブセッションノード
	 * @param monitorTypeId 監視種別ID
	 * @return
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole 
	 */
	public void checkMonitorJobTimeout(JobSessionNodeEntity jobSessionNodeEntity, String monitorTypeId) {
		String sessionId = jobSessionNodeEntity.getId().getSessionId();
		String jobunitId = jobSessionNodeEntity.getId().getJobunitId();
		String jobId = jobSessionNodeEntity.getId().getJobId();
		String facilityId = jobSessionNodeEntity.getId().getFacilityId();
		m_log.debug("checkMonitorJobTimeout() : sessionId=" + sessionId
			+ ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId
			+ ", monitorTypeId=" + monitorTypeId);

		// トラップ系監視のみ対象
		if (monitorTypeId == null
				|| (!monitorTypeId.equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)
				&& !monitorTypeId.equals(HinemosModuleConstant.MONITOR_SNMPTRAP)
				&& !monitorTypeId.equals(HinemosModuleConstant.MONITOR_LOGFILE)
				&& !monitorTypeId.equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)
				&& !monitorTypeId.equals(HinemosModuleConstant.MONITOR_PCAP_BIN)
				&& !monitorTypeId.equals(HinemosModuleConstant.MONITOR_WINEVENT)
				&& !monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
				&& !monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)
				&& !monitorTypeId.equals(HinemosModuleConstant.MONITOR_CLOUD_LOG))) {
			return;
		}

		// ジョブ情報
		JobInfoEntity jobInfoEntity = jobSessionNodeEntity.getJobSessionJobEntity().getJobInfoEntity();

		if(jobInfoEntity.getMonitorWaitTime() == null 
				|| jobInfoEntity.getMonitorWaitTime() == 0){
			// タイムアウト時間がが指定されていない場合は処理終了
			return;
		}

		// 終了処理
		// ノードの開始日時を取得
		long startDate = jobSessionNodeEntity.getStartDate();
		Calendar work = HinemosTime.getCalendarInstance();
		work.setTimeInMillis(startDate);
		work.getTime();
		work.add(Calendar.MINUTE, jobInfoEntity.getMonitorWaitTime());
		Long check = work.getTimeInMillis();
		if (check <= HinemosTime.currentTimeMillis()) {
			// 処理終了
			RunInstructionInfo runInstructionInfo = RunHistoryUtil.findRunHistory(
					sessionId, jobunitId, jobId, jobSessionNodeEntity.getId().getFacilityId());
			if (runInstructionInfo == null) {
				//実行指示情報を作成
				runInstructionInfo = new RunInstructionInfo();
				runInstructionInfo.setSessionId(sessionId);
				runInstructionInfo.setJobunitId(jobunitId);
				runInstructionInfo.setJobId(jobId);
				runInstructionInfo.setFacilityId(jobSessionNodeEntity.getId().getFacilityId());
				runInstructionInfo.setSpecifyUser(jobInfoEntity.getSpecifyUser());
				runInstructionInfo.setUser(jobInfoEntity.getEffectiveUser());
				runInstructionInfo.setCommandType(CommandTypeConstant.NORMAL);
				runInstructionInfo.setCommand(CommandConstant.MONITOR);
				runInstructionInfo.setStopType(jobInfoEntity.getStopType());
			}
			MonitorJobWorker.endMonitorJob(
				runInstructionInfo,
				monitorTypeId,
				MessageConstant.MESSAGE_JOB_MONITOR_RESULT_NOT_FOUND.getMessage(),
				"",
				RunStatusConstant.END,
				jobInfoEntity.getMonitorWaitEndValue());
		}
	}
	
	/**
	 * ジョブ連携待機ジョブのタイムアウト可否を確認します。
	 *
	 * @param jobSessionNodeEntity ジョブセッションノード
	 * @return
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole 
	 */
	public void checkJobLinkRcvJobTimeout(JobSessionNodeEntity jobSessionNodeEntity) {
		String sessionId = jobSessionNodeEntity.getId().getSessionId();
		String jobunitId = jobSessionNodeEntity.getId().getJobunitId();
		String jobId = jobSessionNodeEntity.getId().getJobId();
		String facilityId = jobSessionNodeEntity.getId().getFacilityId();
		m_log.debug("checkJobLinkRcvJobTimeout() : sessionId=" + sessionId
			+ ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId);

		// ジョブ情報
		JobInfoEntity jobInfoEntity = jobSessionNodeEntity.getJobSessionJobEntity().getJobInfoEntity();

		if(!jobInfoEntity.getFailureEndFlg()) {
			// タイムアウト時間が指定されていない場合は処理終了
			return;
		}

		if (jobInfoEntity.getMonitorWaitTime() == null) {
			// タイムアウト時間がnullの場合は0を設定
			jobInfoEntity.setMonitorWaitTime(0);
		}

		// 終了処理
		// ノードの開始日時を取得
		long startDate = jobSessionNodeEntity.getStartDate();
		Calendar work = HinemosTime.getCalendarInstance();
		work.setTimeInMillis(startDate);
		work.getTime();
		work.add(Calendar.MINUTE, jobInfoEntity.getMonitorWaitTime());
		Long check = work.getTimeInMillis();
		if (check <= HinemosTime.currentTimeMillis()) {
			// 処理終了
			RunInstructionInfo runInstructionInfo = RunHistoryUtil.findRunHistory(
					sessionId, jobunitId, jobId, jobSessionNodeEntity.getId().getFacilityId());
			if (runInstructionInfo == null) {
				//実行指示情報を作成
				runInstructionInfo = new RunInstructionInfo();
				runInstructionInfo.setSessionId(sessionId);
				runInstructionInfo.setJobunitId(jobunitId);
				runInstructionInfo.setJobId(jobId);
				runInstructionInfo.setFacilityId(jobSessionNodeEntity.getId().getFacilityId());
				runInstructionInfo.setSpecifyUser(jobInfoEntity.getSpecifyUser());
				runInstructionInfo.setUser(jobInfoEntity.getEffectiveUser());
				runInstructionInfo.setCommandType(CommandTypeConstant.NORMAL);
				runInstructionInfo.setCommand(CommandConstant.JOB_LINK_RCV);
				runInstructionInfo.setStopType(jobInfoEntity.getStopType());
			}
			JobLinkRcvJobWorker.endJobLinkRcvJob(
				runInstructionInfo,
				MessageConstant.MESSAGE_JOB_MONITOR_RESULT_NOT_FOUND.getMessage(),
				RunStatusConstant.END,
				jobInfoEntity.getMonitorWaitEndValue(),
				null, false);
		}
	}

	/**
	 * コマンドを再実行するかどうかを終了状態から判定します。
	 * コマンド実行回数からの判定はretryJob内で行っています。 
	 * 
	 * @param sessionJob
	 * @param sessionNode
	 * @return true: 再実行しない、false: 再実行する
	 */
	protected boolean checkRetryFinish(JobSessionJobEntity sessionJob, Integer endValue) {
		Boolean retryFlg = sessionJob.getJobInfoEntity().getCommandRetryFlg();
		Integer retryEndStatus = sessionJob.getJobInfoEntity().getCommandRetryEndStatus();
		if (retryFlg == null || !retryFlg) {
			//繰り返しフラグが設定されていない場合は再実行しない
			return true;
		}
		if (retryEndStatus == null) {
			//繰り返し完了状態が設定されていない場合は再実行する
			return false;
		} else if (!retryEndStatus.equals(JobSessionJobUtil.checkEndStatus(sessionJob, endValue))) {
			// 再実行完了状態が設定されている場合、終了状態が一致していなければ再実行する
			return false;
		} else {
			return true;
		}
	}

	private static class JobPriorityComparator implements Comparator<JobSessionNodeEntity>, Serializable {
		/** ログ出力のインスタンス */
		private static Log m_log = LogFactory.getLog( JobPriorityComparator.class );

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		// 二つのJobSessionNodeEntityを受け取り、ノードのジョブ優先度を降順で比較する関数
		@Override
		public int compare(JobSessionNodeEntity s, JobSessionNodeEntity t) {
			m_log.debug("compare() s: " + s.getId() + ", t: " + t.getId());

			int ret = 0;
			try {
				String facilityId_s = s.getId().getFacilityId();
				String facilityId_t = t.getId().getFacilityId();
				
				/*
				 * ノードの優先度
				 * 優先度が高いほうが実行される。(tを実行したい場合は、「return 正の値」とする。)
				 */
				int priority_s = NodeProperty.getProperty(facilityId_s).getJobPriority();
				int priority_t = NodeProperty.getProperty(facilityId_t).getJobPriority();
				ret = priority_t - priority_s;
				if (ret != 0){
					m_log.debug("decided by node job priority, ret: " + ret);
					return ret;
				}

				// 多重度判定の対象ジョブではない場合はノードの優先度とファシリティIDのみで
				// 比較するためここは通らない
				if (JobMultiplicityCache
						.isMultiplicityJob(s.getJobSessionJobEntity().getJobInfoEntity().getJobType())) {
					
					/*
					 * 多重率（ジョブ実行数 / ジョブ多重度)
					 * 多重率が低いほうが実行される。
					 * 多重率が同じ場合は、多重度上限が高いほうで実行される。
					 */
					// 多重度
					int multi_s = NodeProperty.getProperty(facilityId_s).getJobMultiplicity();
					int multi_t = NodeProperty.getProperty(facilityId_t).getJobMultiplicity(); 
					m_log.debug("job multiplicity. multi_s: " + multi_s + ", multi_t: " + multi_t);

					// 実行数＋待ち数＋実行予定数のジョブ数
					int count_s = JobMultiplicityCache.getMultiplicity(facilityId_s);
					int count_t = JobMultiplicityCache.getMultiplicity(facilityId_t);
					m_log.debug("job count. count_s: " + count_s + ", count_t: " + count_t);

					// return (rs / ms - rt / mt) → return (rs * mt - rt * ms)
					ret = count_s * multi_t - count_t * multi_s;
					m_log.debug("calculate rate of multiplicity, ret: " + ret);
					if (ret != 0) {
						m_log.debug("decided by rate of multiplicity, ret: " + ret);
						return ret;
					}

					// 多重度順
					ret = multi_t - multi_s;
					if (ret != 0) {
						m_log.debug("decided by job multiplicity, ret: " + ret);
						return ret;
					}
				}

				// facilityId
				ret = facilityId_s.compareTo(facilityId_t);
				if (ret != 0) {
					m_log.debug("decided by facilityId, ret: " + ret);
					return ret;
				}
			} catch (FacilityNotFound e) {
				m_log.warn("NodeComparator " + e.getMessage());
			}
			return 0;
		}
	}

	/**
	 * ノード詳細に表示するメッセージをセットします。
	 * @param sessionNode
	 * @param newMsg
	 */
	public void setMessage(JobSessionNodeEntity sessionNode, String newMsg) {
		String msg;
		Date date = HinemosTime.getDateInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		df.setTimeZone(HinemosTime.getTimeZone());
		String dateStr = df.format(date);
		String header ="["+ dateStr + "] ";
		String oldMsg = sessionNode.getMessage();

		//行末に改行が含まれている場合は除く
		Pattern pattern = Pattern.compile("\r\n$");
		Matcher m = pattern.matcher(newMsg);
		if (m.find() == false) {
			pattern = Pattern.compile("\n$");
			m = pattern.matcher(newMsg);
		}
		newMsg = m.replaceAll("");

		if (oldMsg == null || oldMsg.equals("")) {
			msg = header + newMsg;
		} else {
			//既にメッセージがあれば追記して改行
			msg =  header + newMsg + "\r\n" + oldMsg;
		}

		//設定された文字数で切る
		int msgMaxLen = HinemosPropertyCommon.job_message_max_length.getIntegerValue();
		if (msg.length() > msgMaxLen) {
			msg = msg.substring(0, msgMaxLen);
		}
		sessionNode.setMessage(msg);
	}

	private boolean retryJob(JobSessionNodeEntity sessionNode, JobSessionJobEntity sessionJob, RunResultInfo info, int maxRetry) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			
			if (sessionNode.getStatus() != StatusConstant.TYPE_RUNNING) {
				return false;
			}

			if (sessionJob.getStatus() == StatusConstant.TYPE_SUSPEND) {
				setMessage(sessionNode, MessageConstant.SUSPEND.getMessage());
				sessionNode.setStatus(StatusConstant.TYPE_WAIT);
				sessionNode.setStartDate(null);
				return false;
			} else if (sessionJob.getStatus() != StatusConstant.TYPE_RUNNING) {
				return false;
			}

			// エージェントタイムアウト時はコマンドリトライしない
			if (info instanceof AgentTimeoutRunResultInfo) {
				m_log.debug("retryJob() : Agent timeout");
				return false;
			}

			//通算リトライ回数
			int errorCount = sessionNode.getErrorRetryCount();
			//通算実行回数
			int runCount = errorCount + 1;
			m_log.debug("maxRetry:" + maxRetry + "runCount:" + runCount +  " errorCount:" + errorCount + " " + (maxRetry > runCount) + ", " + sessionNode.getId());
			if (maxRetry > runCount) {
				//上限回数に達してない場合は再実行
				//通算回数を加算
				errorCount++;
				m_log.debug("errRtryCnt++=" + errorCount);

				//DB更新
				setMessage(sessionNode, "stdout=" + info.getMessage() + ", stderr=" + info.getErrorMessage());
				setMessage(sessionNode, MessageConstant.RETRYING.getMessage() + "(" + errorCount + ")");
				sessionNode.setErrorRetryCount(errorCount);
				sessionNode.setStatus(StatusConstant.TYPE_WAIT);
				sessionNode.setStartDate(null);

				jtm.addCallback(new FromRunningAfterCommitCallback(sessionNode.getId()));
				m_log.debug("retryJob() : jtm.addCallback() " + sessionNode.getId());

				//再実行登録
				JobSessionNodeRetryController.register(sessionNode.getId());
				return true;
			}
	
			return false;
		}
	}

	/**
	 * Hinemosエージェントが終了した際に実行中ノードを終了状態へ更新します。
	 * @param facilityId ファシリティID
	 * @param agentInfo エージェント情報
	 * @param isNormalEnd エージェントが正常終了時に呼ばれたかどうか
	 */
	public void endNodeByAgent(String facilityId, AgentInfo agentInfo, boolean isNormalEnd) {
		String queryName = isNormalEnd ? "JobSessionNodeEntity.findByFacilityIdStatus":
			"JobSessionNodeEntity.findByDifferentStartuptime";

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			jtm.begin();

			String instanceId = agentInfo.getInstanceId();
			if (instanceId == null) {
				instanceId = "";
			}
			
			//実行中のジョブ情報を取得
			List<JobSessionNodeEntity> list = null;
			List<Integer> statusList = Arrays.asList(new Integer[]{
					StatusConstant.TYPE_RUNNING,
					StatusConstant.TYPE_STOPPING});
			
			list = em.createNamedQuery(queryName, JobSessionNodeEntity.class)
					.setParameter("statusList", statusList)
					.setParameter("facilityId", facilityId)
					.setParameter("startupTime", agentInfo.getStartupTime())
					.setParameter("instanceId", instanceId)
					.getResultList();

			for (JobSessionNodeEntity entity : list) {
				// 監視ジョブ、ジョブ連携送信ジョブの場合、終了させない
				if (entity.getJobSessionJobEntity().getJobInfoEntity().getJobType() == JobConstant.TYPE_MONITORJOB
						|| entity.getJobSessionJobEntity().getJobInfoEntity().getJobType() == JobConstant.TYPE_JOBLINKSENDJOB
						|| entity.getJobSessionJobEntity().getJobInfoEntity().getJobType() == JobConstant.TYPE_JOBLINKRCVJOB) {
					m_log.info("endNodeByAgent() : Skip " + entity.getId().getSessionId());
					continue;
				}

				// 停止[状態指定](強制)以外での停止処理中の場合、停止させず、一度のみエージェント停止のメッセージを出力する
				if (entity.getStatus() == StatusConstant.TYPE_STOPPING && 
						entity.getJobSessionJobEntity().getDelayNotifyFlg() != DelayNotifyConstant.STOP_SET_END_VALUE_FORCE){
					if (!entity.getMessage().contains(MessageConstant.MESSAGE_AGENT_STOPPED.getMessage())){
						new JobSessionNodeImpl().setMessage(entity, MessageConstant.MESSAGE_AGENT_STOPPED.getMessage());
					}
					continue;
				}
				
				ILock lock = JobRunManagementBean.getLock(entity.getId().getSessionId());
				try {
					// ジョブの実行結果とエージェント停止を同時に受信した場合への対応
					// 実行結果の上書きを防止するため、ジョブのステータスが変化していなければ処理を続行する
					lock.writeLock();
					JobSessionNodeEntity jobSessionNodeEntity = QueryUtil.getJobSessionNodePK(
							entity.getId().getSessionId(), entity.getId().getJobunitId(), entity.getId().getJobId(),
							entity.getId().getFacilityId());
					em.refresh(jobSessionNodeEntity);
					if (jobSessionNodeEntity.getStatus() != StatusConstant.TYPE_RUNNING) {
						continue;
					}

					// 実行結果情報を作成
					RunResultInfo info = new RunResultInfo();
					info.setSessionId(entity.getId().getSessionId());
					info.setJobunitId(entity.getId().getJobunitId());
					info.setJobId(entity.getId().getJobId());
					info.setFacilityId(facilityId);
					info.setCommand("");
					int commandType = CommandTypeConstant.NORMAL;
					if(entity.getStatus() == StatusConstant.TYPE_STOPPING){
						commandType = CommandTypeConstant.STOP;
					}
					info.setCommandType(commandType);
					info.setStatus(RunStatusConstant.ERROR);
					info.setMessage(MessageConstant.MESSAGE_AGENT_STOPPED.getMessage());
					info.setErrorMessage("");
					m_log.info("endNodeByAgent " + entity.getId().toString());
					try {
						endNode(info);
					} catch (InvalidRole e) {
						m_log.info("setting status failure. (sessionId = " + info.getSessionId() + ", facilityId = " + info.getFacilityId()
								+ ", status = " + info.getStatus() + ", commandType = " + info.getCommandType() + ")", e);
						jtm.rollback();
						return;
					} catch (JobInfoNotFound e) {
						m_log.info("setting status failure. (sessionId = " + info.getSessionId() + ", facilityId = " + info.getFacilityId()
								+ ", status = " + info.getStatus() + ", commandType = " + info.getCommandType() + ")", e);
						jtm.rollback();
						return;
					} catch (Exception e) {
						m_log.warn(
								"endNodeByAgent() RunresultInfo send error : sessionId=" + info.getSessionId()
										+ ", jobId="+ info.getJobId()
										+ ", facilityId=" + facilityId
										+ ",  : " + e.getClass().getSimpleName()
										+ ", " + e.getMessage(), e);
						jtm.rollback();
						return;
					}
				} catch (JobInfoNotFound e) {
					// JobInfoNotFoundは実際には発生し得ない想定
					m_log.info(
							"setting status failure. (sessionId = " + entity.getId().getSessionId() + ", facilityId = "
									+ entity.getId().getFacilityId() + ", status = " + entity.getStatus() + ")",
							e);
					jtm.rollback();
					return;
				} finally {
					lock.writeUnlock();
				}
			}
			jtm.commit();
		}
	}
	
	
	/**
	 * 承認処理を行います。
	 *
	 * @param info
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws FacilityNotFound 
	 */
	public void approveJob(JobApprovalInfo info) throws JobInfoNotFound, HinemosUnknown, InvalidRole, FacilityNotFound, InvalidApprovalStatus {
		
		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(info.getSessionId(), info.getJobunitId(), info.getJobId());
		// ジョブ情報の取得/設定
		JobInfoEntity jobInfo = sessionJob.getJobInfoEntity();
		
		//セッションジョブに関連するセッションノードを取得
		List<JobSessionNodeEntity> nodeList = sessionJob.getJobSessionNodeEntities();
		JobSessionNodeEntity sessionNode =null;
		
		// 承認ジョブの場合はノードリストは1件のみ
		if(nodeList != null && nodeList.size() == 1){
			//セッションノードを取得
			sessionNode =nodeList.get(0);
		}else{
			m_log.error("approveJob() not found job info:" + info.getJobId());
			throw new JobInfoNotFound();
		}
		
		if(sessionNode.getApprovalStatus() == JobApprovalStatusConstant.TYPE_FINISHED ||
			sessionNode.getApprovalStatus() == JobApprovalStatusConstant.TYPE_STOP){
			m_log.warn("approveJob() There is no approved ready. :" + info.getJobId());
			throw new InvalidApprovalStatus(Messages.getString("MESSAGE_APPROVAL_INVALID_STATE"));
		}

		sessionNode.setApprovalStatus(JobApprovalStatusConstant.TYPE_FINISHED);
		sessionNode.setApprovalResult(info.getResult());
		sessionNode.setApprovalUser(info.getApprovalUser());
		sessionNode.setApprovalComment(info.getComment());
		
		// メッセージ作成
		String approvaluser = info.getApprovalUser()==null ? "":info.getApprovalUser();
		String comment = info.getComment()==null ? "":info.getComment();
		String msg = approvaluser.equals("") ? comment : approvaluser + "：" + comment;
		
		RunResultInfo resultInfo = new RunResultInfo();
		resultInfo.setSessionId(info.getSessionId());
		resultInfo.setJobunitId(info.getJobunitId());
		resultInfo.setJobId(info.getJobId());
		resultInfo.setFacilityId(sessionNode.getId().getFacilityId());
		resultInfo.setCommandType(CommandTypeConstant.NORMAL);
		resultInfo.setCommand("");
		resultInfo.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);
		resultInfo.setStatus(RunStatusConstant.END);
		resultInfo.setTime(HinemosTime.currentTimeMillis());
		resultInfo.setEndValue(info.getResult());
		resultInfo.setMessage(msg);
		resultInfo.setErrorMessage("");
		
		endNode(resultInfo);
		
		//メール送信
		SendApprovalMail sendMail = new SendApprovalMail();
		sendMail.sendResult(jobInfo, info);
	}

	/**
	 * RPAシナリオジョブの終了メッセージを生成します。
	 * 
	 * @param info
	 *            ジョブ実行結果情報
	 * @return メッセージ
	 */
	private String createRpaJobEndMessage(RunResultInfo info) {
		StringBuilder message = new StringBuilder();
		message.append(MessageConstant.MESSAGE_JOB_RPA_SCENARIO_COMPLETED.getMessage()).append("\n");
		// 終了値判定条件に関するメッセージ
		if (info.getRpaJobEndValueConditionInfo() == null) {
			// いずれの条件にも一致しなかった場合
			message.append(MessageConstant.MESSAGE_JOB_RPA_NO_END_VALUE_CONDITION_MATCHED.getMessage());
		} else {
			message.append(MessageConstant.MESSAGE_JOB_RPA_END_VALUE_CONDITION_MATCHED.getMessage()).append("\n");
			if (info.getRpaJobEndValueConditionInfo().getConditionType() == RpaJobEndValueConditionTypeConstant.LOG) {
				// ログファイル判定条件が一致
				message.append(String.join("=", MessageConstant.ORDER.getMessage(),
						String.valueOf(info.getRpaJobEndValueConditionInfo().getOrderNo())))
						.append("\n")
						.append(String.join("=", MessageConstant.LOGFILE_FILENAME.getMessage(),
								info.getRpaJobLogfileName()))
						.append("\n")
						.append(String.join("=", MessageConstant.LOGFILE_PATTERN.getMessage(),
								info.getRpaJobEndValueConditionInfo().getPattern()))
						.append("\n")
						.append(String.join("=", MessageConstant.LOGFILE_LINE.getMessage(), info.getRpaJobLogMessage()));
			} else if (info.getRpaJobEndValueConditionInfo()
					.getConditionType() == RpaJobEndValueConditionTypeConstant.RETURN_CODE) {
				// リターンコード判定条件が一致
				message.append(String.join("=", MessageConstant.ORDER.getMessage(),
						String.valueOf(info.getRpaJobEndValueConditionInfo().getOrderNo())))
						.append("\n")
						.append(String.join("=", MessageConstant.JUDGEMENT_VALUE.getMessage(),
								info.getRpaJobEndValueConditionInfo().getReturnCode()))
						.append("\n")
						.append(String.join("=", MessageConstant.JUDGEMENT_CONDITION.getMessage(),
								conditionConstantToMessage(
										info.getRpaJobEndValueConditionInfo().getReturnCodeCondition())))
						.append("\n").append(String.join("=", MessageConstant.RETURN_CODE.getMessage(),
								String.valueOf(info.getRpaJobReturnCode())));
			}
		}
		// エージェントから送信されたメッセージを表示
		message.append("\n").append(info.getMessage());
		m_log.debug("createRpaJobEndMessage() : message=" + message);
		return message.toString();
	}

	/**
	 * 判定条件の定数をメッセージに変換します。
	 * 
	 * @param constant
	 *            判定条件のの定数
	 * @return 判定条件のメッセージ
	 */
	private String conditionConstantToMessage(Integer constant) {
		String message = "";
		switch (constant) {
		case (RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC):
			message = MessageConstant.EQUAL.getMessage();
			break;
		case (RpaJobReturnCodeConditionConstant.NOT_EQUAL_NUMERIC):
			message = MessageConstant.NOT_EQUAL.getMessage();
			break;
		case (RpaJobReturnCodeConditionConstant.GREATER_THAN):
			message = MessageConstant.GREATER_THAN.getMessage();
			break;
		case (RpaJobReturnCodeConditionConstant.GREATER_THAN_OR_EQUAL_TO):
			message = MessageConstant.GREATER_THAN_OR_EQUAL_TO.getMessage();
			break;
		case (RpaJobReturnCodeConditionConstant.LESS_THAN):
			message = MessageConstant.LESS_THAN.getMessage();
			break;
		case (RpaJobReturnCodeConditionConstant.LESS_THAN_OR_EQUAL_TO):
		default:
			message = MessageConstant.LESS_THAN_OR_EQUAL_TO.getMessage();
			break;
		}
		message = "\"" + message + "\"";
		m_log.debug("conditionConstantToMessage() : constant=" + constant + ", message=" + message);
		return message;
	}

	/**
	 * 異常が発生したRPAシナリオジョブについてノードの終了と通知処理を行います。
	 * 
	 * @param info
	 *            ジョブ実行結果情報
	 * @param job
	 *            ジョブ情報
	 * @param sessionNode
	 *            ノード情報
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	private void endAbnormalRpaJob(RunResultInfo info, JobInfoEntity job, JobSessionNodeEntity sessionNode)
			throws JobInfoNotFound, InvalidRole {
		// ジョブを終了
		sessionNode.setStatus(StatusConstant.TYPE_END);
		//終了日時を設定
		sessionNode.setEndDate(HinemosTime.currentTimeMillis());
		// 収集データ更新
		CollectDataUtil.put(sessionNode);
		String sessionId = sessionNode.getId().getSessionId();
		String jobunitId = sessionNode.getId().getJobunitId();
		String jobId = sessionNode.getId().getJobId();
		String message = "";
		switch (info.getRpaJobErrorType()) {
		case (RpaJobErrorTypeConstant.NOT_LOGIN):
			// ログインされていない場合
			message = MessageConstant.MESSAGE_JOB_RPA_NOT_LOGIN.getMessage();
			if (job.getRpaNotLoginNotify()) {
				// 通知する場合
				new Notice().rpaErrorNotify(sessionId, jobunitId, jobId, RpaJobErrorTypeConstant.NOT_LOGIN);
			}
			// 終了値を設定
			sessionNode.setEndValue(job.getRpaNotLoginEndValue());
			m_log.debug("endAbnormalRpaJob() : not login, sessionId=" + sessionId + ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId + ", notify=" + job.getRpaNotLoginNotify() + ", endValue="
					+ job.getRpaNotLoginEndValue() + ", login=" + job.getRpaLoginFlg());
			break;
		case (RpaJobErrorTypeConstant.ALREADY_RUNNING):
			// RPAツールが既に動作している場合
			message = MessageConstant.MESSAGE_JOB_RPA_ALREADY_RUNNING.getMessage();
			if (job.getRpaAlreadyRunningNotify()) {
				// 通知する場合
				new Notice().rpaErrorNotify(sessionId, jobunitId, jobId, RpaJobErrorTypeConstant.ALREADY_RUNNING);
			}
			// 終了値を設定
			sessionNode.setEndValue(job.getRpaAlreadyRunningEndValue());
			m_log.debug("endAbnormalRpaJob() : already running, sessionId=" + sessionId + ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId + ", notify=" + job.getRpaAlreadyRunningNotify() + ", endValue="
					+ job.getRpaAlreadyRunningEndValue() + ", login=" + job.getRpaLoginFlg());
			break;
		case (RpaJobErrorTypeConstant.ABNORMAL_EXIT):
			// RPAツールが異常終了した場合
			message = MessageConstant.MESSAGE_JOB_RPA_ABNORMAL_EXIT.getMessage();
			if (job.getRpaAbnormalExitNotify()) {
				// 通知する場合
				new Notice().rpaErrorNotify(sessionId, jobunitId, jobId, RpaJobErrorTypeConstant.ABNORMAL_EXIT);
			}
			// 終了値を設定
			sessionNode.setEndValue(job.getRpaAbnormalExitEndValue());
			m_log.debug("endAbnormalRpaJob() : abnormal exit, sessionId=" + sessionId + ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId + ", notify=" + job.getRpaAbnormalExitNotify() + ", endValue="
					+ job.getRpaAbnormalExitEndValue() + ", login=" + job.getRpaLoginFlg());
			break;
		case (RpaJobErrorTypeConstant.FILE_DOES_NOT_EXIST):
			// シナリオファイルパスが存在しない場合
			message = MessageConstant.MESSAGE_JOB_RPA_FILE_DOES_NOT_EXIST.getMessage();
			if (job.getRpaNotLoginNotify()) {
				// 通知する場合（ログインされていない場合の設定）
				new Notice().rpaErrorNotify(sessionId, jobunitId, jobId, RpaJobErrorTypeConstant.FILE_DOES_NOT_EXIST);
			}
			// 終了値を設定
			sessionNode.setEndValue(job.getRpaNotLoginEndValue());
			m_log.debug("endAbnormalRpaJob() : file does not exist, sessionId=" + sessionId + ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId + ", notify=" + job.getRpaNotLoginNotify() + ", endValue="
					+ job.getRpaNotLoginEndValue() + ", login=" + job.getRpaLoginFlg());
			break;
		case (RpaJobErrorTypeConstant.LOGIN_ERROR):
			// ログインに失敗
			message = MessageConstant.MESSAGE_JOB_RPA_LOGIN_ERROR.getMessage();
			if (job.getRpaNotLoginNotify()) {
				// 通知する場合（ログインされていない場合の設定）
				new Notice().rpaErrorNotify(sessionId, jobunitId, jobId, RpaJobErrorTypeConstant.LOGIN_ERROR);
			}
			// 終了値を設定
			sessionNode.setEndValue(job.getRpaNotLoginEndValue());
			m_log.debug("endAbnormalRpaJob() : " + RpaJobErrorTypeConstant.LOGIN_ERROR + ", sessionId=" + sessionId + ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId + ", notify=" + job.getRpaNotLoginNotify() + ", endValue="
					+ job.getRpaNotLoginEndValue() + ", login=" + job.getRpaLoginFlg());
			break;
		case (RpaJobErrorTypeConstant.TOO_MANY_LOGIN_SESSION):
			// ログインセッションが複数あり
			message = MessageConstant.MESSAGE_JOB_RPA_TOO_MANY_LOGIN_SESSION.getMessage();
			if (job.getRpaNotLoginNotify()) {
				// 通知する場合（ログインされていない場合の設定）
				new Notice().rpaErrorNotify(sessionId, jobunitId, jobId, RpaJobErrorTypeConstant.TOO_MANY_LOGIN_SESSION);
			}
			// 終了値を設定
			sessionNode.setEndValue(job.getRpaNotLoginEndValue());
			m_log.debug("endAbnormalRpaJob() : " + RpaJobErrorTypeConstant.TOO_MANY_LOGIN_SESSION + ", sessionId=" + sessionId + ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId + ", notify=" + job.getRpaNotLoginNotify() + ", endValue="
					+ job.getRpaNotLoginEndValue() + ", login=" + job.getRpaLoginFlg());
			break;
		case (RpaJobErrorTypeConstant.NOT_RUNNING_EXECUTOR):
			// RPAシナリオエグゼキューターが起動していない場合
			message = MessageConstant.MESSAGE_JOB_RPA_NOT_RUNNING_EXECUTOR.getMessage();
			if (job.getRpaNotLoginNotify()) {
				// 通知する場合（ログインされていない場合の設定）
				new Notice().rpaErrorNotify(sessionId, jobunitId, jobId, RpaJobErrorTypeConstant.NOT_RUNNING_EXECUTOR);
			}
			// 終了値を設定
			sessionNode.setEndValue(job.getRpaNotLoginEndValue());
			m_log.debug("endAbnormalRpaJob() : " + RpaJobErrorTypeConstant.NOT_RUNNING_EXECUTOR + ", sessionId=" + sessionId + ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId + ", notify=" + job.getRpaNotLoginNotify() + ", endValue="
					+ job.getRpaNotLoginEndValue() + ", login=" + job.getRpaLoginFlg());
			break;
		case (RpaJobErrorTypeConstant.ERROR_OCCURRED):
			// エラーが発生した場合
			message = MessageConstant.MESSAGE_JOB_RPA_ERROR_OCCURRED.getMessage();
			if (job.getRpaNotLoginNotify()) {
				// 通知する場合（ログインされていない場合の設定）
				new Notice().rpaErrorNotify(sessionId, jobunitId, jobId, RpaJobErrorTypeConstant.ERROR_OCCURRED);
			}
			// 終了値を設定
			sessionNode.setEndValue(job.getRpaNotLoginEndValue());
			m_log.debug("endAbnormalRpaJob() : " + RpaJobErrorTypeConstant.ERROR_OCCURRED + ", sessionId=" + sessionId + ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId + ", notify=" + job.getRpaNotLoginNotify() + ", endValue="
					+ job.getRpaNotLoginEndValue() + ", login=" + job.getRpaLoginFlg());
			break;
		case (RpaJobErrorTypeConstant.LOST_LOGIN_SESSION):
			// ログインセッションが失われた場合
			message = MessageConstant.MESSAGE_JOB_RPA_LOST_LOGIN_SESSION.getMessage();
			if (job.getRpaAbnormalExitNotify()) {
				// 通知する場合（RPAツールが異常終了した場合の設定）
				new Notice().rpaErrorNotify(sessionId, jobunitId, jobId, RpaJobErrorTypeConstant.LOST_LOGIN_SESSION);
			}
			// 終了値を設定
			sessionNode.setEndValue(job.getRpaAbnormalExitEndValue());
			m_log.debug("endAbnormalRpaJob() : " + RpaJobErrorTypeConstant.LOST_LOGIN_SESSION + ", sessionId=" + sessionId + ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId + ", notify=" + job.getRpaAbnormalExitNotify() + ", endValue="
					+ job.getRpaAbnormalExitEndValue() + ", login=" + job.getRpaLoginFlg());
			break;
		case (RpaJobErrorTypeConstant.SCREENSHOT_FAILED):
			// スクリーンショットの取得に失敗した場合
			message = MessageConstant.MESSAGE_SYS_JOB_SCREENSHOT_FAILED.getMessage();
			// INTERNALイベント通知
			AplLogger.put(InternalIdCommon.JOB_SYS_033, new String[] { sessionId, jobId, info.getFacilityId() });
			m_log.debug("endAbnormalRpaJob() : failed to take screen shot, sessionId=" + sessionId + ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId + ", login=" + job.getRpaLoginFlg());
			break;
		case (RpaJobErrorTypeConstant.OTHER):
			// コマンドの起動失敗等のエラーの場合
			message = info.getErrorMessage();
			// 終了値を設定
			sessionNode.setEndValue(info.getEndValue());
			m_log.debug("endAbnormalRpaJob() : other error, sessionId=" + sessionId + ", jobunitId=" + jobunitId
					+ ", jobId=" + jobId + ", login=" + job.getRpaLoginFlg());
			break;
		default:
			m_log.warn("endNodeNormalStop() : invalid RpaJobErrorTypeConstant=" + info.getRpaJobErrorType());
		}
		if (job.getRpaLoginFlg()) {
			// ログイン処理が継続中の場合は停止しておく
			RpaJobLoginWorker.cancel(info);
		}
		// メッセージを設定
		setMessage(sessionNode, message);
	}

	/**
	 * 該当セッションがRPA管理ツール監視の監視ジョブであるかどうかを判断する。
	 * （）
	 * 
	 * @param sessionNode セッションノード
	 * @return true/false
	 */
	private boolean isMonitorRpaAccountJob(JobSessionNodeEntity sessionNode) {
		if(m_log.isDebugEnabled()){ 	
			m_log.debug("isMonitorRpaAcountJob() : start ,session= "+ sessionNode.getId());
		}
		JobInfoEntity info = sessionNode.getJobSessionJobEntity().getJobInfoEntity();

		if( info.getJobType() == JobConstant.TYPE_MONITORJOB ){
			JpaTransactionManager jtm = null;
			try {
				MonitorInfo monitorInfo = null;
				jtm = new JpaTransactionManager();
				monitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_NONE(info.getMonitorId());
				jtm.getEntityManager().detach(monitorInfo);
				if (monitorInfo != null && monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_RPA_MGMT_TOOL_SERVICE)) {
					return true;
				}
			} catch (MonitorNotFound e) {
				m_log.warn("isMonitorRpaAcountJob() : invalid getMonitorId=" + info.getMonitorId());
			}finally{
				if(jtm != null){
					jtm.close();
				}
			}
		}
		return false;
	}

}