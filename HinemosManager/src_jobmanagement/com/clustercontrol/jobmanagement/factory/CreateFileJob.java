/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.factory;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.ConditionTypeConstant;
import com.clustercontrol.jobmanagement.bean.DelayNotifyConstant;
import com.clustercontrol.jobmanagement.bean.EndStatusCheckConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobStartJobInfoEntity;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * ファイル転送ジョブの実行用情報を作成するクラスです。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class CreateFileJob {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( CreateFileJob.class );

	/** ファイルリスト取得ジョブのジョブID追加文字列 */
	public static final String FILE_LIST = "_FILE_LIST";
	/** 公開キー取得ジョブのジョブID追加文字列 */
	public static final String GET_KEY = "_GET_KEY";
	/** 公開キー追加ジョブのジョブID追加文字列 */
	public static final String ADD_KEY = "_ADD_KEY";
	/** 公開キー削除ジョブのジョブID追加文字列 */
	public static final String DEL_KEY = "_DEL_KEY";
	/** チェックサム取得ジョブのジョブID追加文字列 */
	public static final String GET_CHECKSUM = "_GET_CS";
	/** チェックサム比較ジョブのジョブID追加文字列 */
	public static final String CHECK_CHECKSUM = "_CHECK_CS";
	/** ファイル転送ジョブのジョブID追加文字列 */
	public static final String FORWARD = "_FORWARD";

	/** 正常の終了値及び終了値の範囲 */
	private static final int NORMAL = 0;
	/** 警告の終了値及び終了値の範囲 */
	private static final int WARNING = 1;
	/** 異常の終了値及び終了値の範囲 */
	private static final int ABNORMAL = -1;
	/** 異常の終了値(ファイル転送ジョブ) */
	private static final int ABNORMAL_FOR_FILE = 9;

	/** ファイル転送コマンド（scp） */
	private static final String COMMAND_SCP = "scp";

	/**
	 * ファイルリスト取得ジョブの実行用情報を作成します。
	 * <p>
	 * <ol>
	 * <li>セッションジョブを作成します。</li>
	 * <li>ジョブリレーション情報を作成します。</li>
	 * <li>ジョブ情報を作成します。</li>
	 * <li>ジョブ待ち条件情報を作成します。</li>
	 * <li>ジョブコマンド情報を作成します。</li>
	 * <li>セッションノードを作成します。</li>
	 * <li>ジョブ通知情報を作成します。</li>
	 * <li>ジョブ終了状態情報を作成します。</li>
	 * </ol>
	 * 
	 * @param jobInfoEntity ファイル転送ジョブのJobInfoEntity
	 * @throws FacilityNotFound
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	protected void createGetFileListJob(JobInfoEntity parentJobInfoEntity, String fileTransferJobId) throws FacilityNotFound, EntityExistsException, HinemosUnknown {

		// インスタンス生成
		JobInfoEntity jobInfoEntity = subCreateJobInfoEntityForJob (parentJobInfoEntity.getJobSessionJobEntity(), FILE_LIST, parentJobInfoEntity.getSrcFacilityId());

		//JobInfoEntityに値を設定
		jobInfoEntity.setRegDate(parentJobInfoEntity.getRegDate());
		jobInfoEntity.setUpdateDate(parentJobInfoEntity.getUpdateDate());
		jobInfoEntity.setRegUser(parentJobInfoEntity.getRegUser());
		jobInfoEntity.setUpdateUser(parentJobInfoEntity.getUpdateUser());

		//待ち条件を設定
		jobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_AND);

		//実行コマンドを設定
		jobInfoEntity.setStartCommand(CommandConstant.GET_FILE_LIST);
		jobInfoEntity.setSpecifyUser(parentJobInfoEntity.getSpecifyUser());
		jobInfoEntity.setEffectiveUser(parentJobInfoEntity.getEffectiveUser());
		jobInfoEntity.setArgument(parentJobInfoEntity.getSrcFile());
		jobInfoEntity.setMessageRetry(parentJobInfoEntity.getMessageRetry());
		jobInfoEntity.setCommandRetryFlg(false);
		jobInfoEntity.setCommandRetry(parentJobInfoEntity.getCommandRetry());

		//通知メッセージを作成
		JobUtil.copyJobNoticeProperties(jobInfoEntity, parentJobInfoEntity.getId().getJobId());
	}

	/**
	 * ファイル転送を行うジョブネットの実行用情報を作成します。
	 * <p>
	 * <ol>
	 * <li>JobSessionJobEntityから、セッションジョブを取得します。</li>
	 * <li>ジョブリレーション情報を取得し、親ジョブのジョブIDを取得します。</li>
	 * <li>JobSessionJobEntityから、親ジョブのセッションジョブを取得します。</li>
	 * <li>親ジョブのセッションジョブからジョブファイル転送情報を取得します。</li>
	 * <li>新規のトランザクションを開始します。</li>
	 * <li>ファイル転送を行うノード単位のジョブネットを作成します。</li>
	 * <li>トランザクションをコミットします。</li>
	 * </ol>
	 * 
	 * @param jobSessionJobEntity JobSessionJobEntity
	 * @param fileList ファイルリスト
	 * @throws JobInfoNotFound
	 * @throws FacilityNotFound
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.jobmanagement.factory.CreateFileJob#createNodeJobNet(String, String, String, JobFileInfoData, List)
	 */
	public void createFileJobNet(JobSessionJobEntity jobSessionJobEntity, List<String> fileList)
			throws JobInfoNotFound, FacilityNotFound, EntityExistsException, HinemosUnknown, InvalidRole {

		//親ジョブのセッションジョブを取得
		JobSessionJobEntity parentJobSessionJobEntity
			= QueryUtil.getJobSessionJobPK(
								jobSessionJobEntity.getId().getSessionId(),
								jobSessionJobEntity.getParentJobunitId(),
								jobSessionJobEntity.getParentJobId());

		//親ジョブのセッションジョブからファイル転送情報を取得
		JobInfoEntity job = parentJobSessionJobEntity.getJobInfoEntity();
		JobFileInfoData fileData = new JobFileInfoData(
				job.getId().getSessionId(),
				job.getId().getJobunitId(),
				job.getId().getJobId(),
				job.getSrcFacilityId(),
				job.getDestFacilityId(),
				job.getProcessMode(),
				job.getSrcFile(),
				job.getSrcWorkDir(),
				job.getDestDirectory(),
				job.getDestWorkDir(),
				job.getCompressionFlg(),
				job.getCheckFlg(),
				job.getSpecifyUser(),
				job.getEffectiveUser(),
				job.getMessageRetry(),
				job.getCommandRetry(),
				job.getCommandRetryFlg());

		JpaTransactionManager jtm = new JpaTransactionManager();

		//リポジトリ(RepositoryControllerBean)を取得
		RepositoryControllerBean repository = new RepositoryControllerBean();

		//転送先ファシリティID取得
		ArrayList<String> nodeIdList = repository.getExecTargetFacilityIdList(fileData.getDest_facility_id(), jobSessionJobEntity.getOwnerRoleId());

		m_log.debug("createFileJobNet : ownerRoleId=" + jobSessionJobEntity.getOwnerRoleId() +
				", dest=" + fileData.getDest_facility_id() + ", size=" + nodeIdList.size());

		//ジョブユニットのジョブIDは親のものと同じとする。
		String jobunitId = parentJobSessionJobEntity.getId().getJobunitId();

		//ノード単位のジョブネットを作成
		String waitJobId = jobSessionJobEntity.getId().getJobId();
		for(int i = 0; i < nodeIdList.size(); i++){
			String nodeJobId = parentJobSessionJobEntity.getId().getJobId() + "_" + nodeIdList.get(i);

			//JobSessionJobを作成
			// インスタンス生成
			JobSessionJobEntity nodeJobSessionJob
			= new JobSessionJobEntity(parentJobSessionJobEntity.getJobSessionEntity(), jobunitId, nodeJobId);
			// 重複チェック
			jtm.checkEntityExists(JobSessionJobEntity.class, nodeJobSessionJob.getId());
			nodeJobSessionJob.setParentJobunitId(parentJobSessionJobEntity.getId().getJobunitId());
			nodeJobSessionJob.setParentJobId(parentJobSessionJobEntity.getId().getJobId());
			nodeJobSessionJob.setStatus(StatusConstant.TYPE_WAIT);
			nodeJobSessionJob.setEndStausCheckFlg(EndStatusCheckConstant.ALL_JOB);
			nodeJobSessionJob.setDelayNotifyFlg(DelayNotifyConstant.NONE);

			//JobInfoEntityを作成
			// インスタンス生成
			JobInfoEntity nodeJobInfoEntity = new JobInfoEntity(nodeJobSessionJob);
			// 重複チェック
			jtm.checkEntityExists(JobInfoEntity.class, nodeJobInfoEntity.getId());
			nodeJobInfoEntity.setJobName(nodeIdList.get(i));
			nodeJobInfoEntity.setJobType(JobConstant.TYPE_JOBNET);
			nodeJobInfoEntity.setRegisteredModule(job.isRegisteredModule());
			nodeJobInfoEntity.setUnmatchEndFlg(true);
			nodeJobInfoEntity.setStartDelayNotifyPriority(PriorityConstant.TYPE_CRITICAL);
			nodeJobInfoEntity.setStartDelayOperationType(OperationConstant.TYPE_STOP_SKIP);
			nodeJobInfoEntity.setEndDelayNotifyPriority(PriorityConstant.TYPE_CRITICAL);
			nodeJobInfoEntity.setEndDelayOperationType(OperationConstant.TYPE_STOP_AT_ONCE);
			// ファイル転送を中止する場合は、プロセス停止とする。
			nodeJobInfoEntity.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);
			nodeJobInfoEntity.setMultiplicityEndValue(job.getMultiplicityEndValue());
			nodeJobInfoEntity.setMultiplicityNotify(job.getMultiplicityNotify());
			nodeJobInfoEntity.setMultiplicityNotifyPriority(job.getMultiplicityNotifyPriority());
			nodeJobInfoEntity.setMultiplicityOperation(job.getMultiplicityOperation());

			// 終了状態を作成
			// 正常
			nodeJobInfoEntity.setNormalEndValue(NORMAL);
			nodeJobInfoEntity.setNormalEndValueFrom(NORMAL);
			nodeJobInfoEntity.setNormalEndValueTo(NORMAL);
			// 警告
			nodeJobInfoEntity.setWarnEndValue(WARNING);
			nodeJobInfoEntity.setWarnEndValueFrom(WARNING);
			nodeJobInfoEntity.setWarnEndValueTo(WARNING);
			// 異常
			nodeJobInfoEntity.setAbnormalEndValue(ABNORMAL_FOR_FILE);
			nodeJobInfoEntity.setAbnormalEndValueFrom(ABNORMAL);
			nodeJobInfoEntity.setAbnormalEndValueTo(ABNORMAL);
			
			//処理方法により待ち条件を作成する
			if(fileData.getProcess_mode() == ProcessingMethodConstant.TYPE_ALL_NODE){
				//全ノードで受信

				nodeJobInfoEntity.setUnmatchEndStatus(EndStatusConstant.TYPE_ABNORMAL);

				if(i == 0){
					//待ち条件を設定
					nodeJobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_AND);
					nodeJobInfoEntity.setUnmatchEndValue(ABNORMAL);

					//JobStartJobInfoEntityを作成
					// インスタンス生成
					JobStartJobInfoEntity jobStartJobInfoEntity = new JobStartJobInfoEntity(
							nodeJobInfoEntity,
							jobSessionJobEntity.getId().getJobunitId(),
							waitJobId,
							JudgmentObjectConstant.TYPE_JOB_END_STATUS,
							EndStatusConstant.TYPE_NORMAL);
					// 重複チェック
					jtm.checkEntityExists(JobStartJobInfoEntity.class, jobStartJobInfoEntity.getId());
				}else{
					//待ち条件を設定
					nodeJobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_OR);
					nodeJobInfoEntity.setUnmatchEndValue(NORMAL);

					//JobStartJobInfoEntityを作成
					Integer[] targetJobEndValues = {EndStatusConstant.TYPE_NORMAL,
							EndStatusConstant.TYPE_WARNING,
							EndStatusConstant.TYPE_ABNORMAL};
					for (Integer targetJobEndValue : targetJobEndValues) {
						// インスタンス生成
						JobStartJobInfoEntity jobStartJobInfoEntity = new JobStartJobInfoEntity(
								nodeJobInfoEntity,
								jobSessionJobEntity.getId().getJobunitId(),
								waitJobId,
								JudgmentObjectConstant.TYPE_JOB_END_STATUS,
								targetJobEndValue);
						// 重複チェック
						jtm.checkEntityExists(JobStartJobInfoEntity.class, jobStartJobInfoEntity.getId());
					}

				}
			}else{
				//1ノードで受信時のみ設定

				if(i == 0){
					//待ち条件を設定
					nodeJobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_AND);
					nodeJobInfoEntity.setUnmatchEndValue(ABNORMAL);
					nodeJobInfoEntity.setUnmatchEndStatus(EndStatusConstant.TYPE_ABNORMAL);

					//JobStartJobInfoEntityを作成
					// インスタンス生成
					JobStartJobInfoEntity jobStartJobInfoEntity = new JobStartJobInfoEntity(
							nodeJobInfoEntity,
							jobSessionJobEntity.getId().getJobunitId(),
							waitJobId,
							JudgmentObjectConstant.TYPE_JOB_END_STATUS,
							EndStatusConstant.TYPE_NORMAL);
					// 重複チェック
					jtm.checkEntityExists(JobStartJobInfoEntity.class, jobStartJobInfoEntity.getId());
				}else{
					//待ち条件を設定
					nodeJobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_OR);
					nodeJobInfoEntity.setUnmatchEndValue(NORMAL);
					nodeJobInfoEntity.setUnmatchEndStatus(EndStatusConstant.TYPE_NORMAL);

					//JobStartJobInfoEntityを作成
					// インスタンス生成
					JobStartJobInfoEntity jobStartJobInfoEntity = new JobStartJobInfoEntity(
							nodeJobInfoEntity,
							jobSessionJobEntity.getId().getJobunitId(),
							waitJobId,
							JudgmentObjectConstant.TYPE_JOB_END_STATUS,
							EndStatusConstant.TYPE_WARNING);
					// 重複チェック
					jtm.checkEntityExists(JobStartJobInfoEntity.class, jobStartJobInfoEntity.getId());

					//JobStartJobInfoEntityを作成
					// インスタンス生成
					jobStartJobInfoEntity = new JobStartJobInfoEntity(
							nodeJobInfoEntity,
							jobSessionJobEntity.getId().getJobunitId(),
							waitJobId,
							JudgmentObjectConstant.TYPE_JOB_END_STATUS,
							EndStatusConstant.TYPE_ABNORMAL);
					// 重複チェック
					jtm.checkEntityExists(JobStartJobInfoEntity.class, jobStartJobInfoEntity.getId());
				}
			}

			//JobSessionJobにファシリティパスを設定
			nodeJobSessionJob.setScopeText(repository.getFacilityPath(nodeIdList.get(i), null));

			//通知メッセージを作成
			JobUtil.copyJobNoticeProperties(nodeJobInfoEntity, job.getId().getJobId());

			//ファイル転送ジョブネットの作成
			createForwardFileJobNet(nodeJobSessionJob, nodeIdList.get(i), fileData, fileList);

			if(fileData.getProcess_mode() == ProcessingMethodConstant.TYPE_RETRY) {
				waitJobId = nodeJobId;
			}
		}
	}

	/**
	 * ファイル転送を行うノード単位のジョブネットの実行用情報を作成します。
	 * <p>
	 * <ol>
	 * <li>公開キーを取得するジョブを作成します。</li>
	 * <li>公開キーを追加するジョブを作成します。</li>
	 * <li>ファイルリストの数、以下の処理を行います。</li>
	 *   <ol>
	 *   <li>ファイルのチェックする場合、チェックサムを取得するジョブを作成します。</li>
	 *   <li>ファイルを転送するジョブを作成します。</li>
	 *   <li>ファイルをチェックする場合、チェックサムを比較するジョブを作成します。</li>
	 *   </ol>
	 * <li>公開キーを削除するジョブを作成します。</li>
	 * </ol>
	 * 
	 * @param sessionId セッションID
	 * @param parentJobunitId 親ジョブが所属するジョブユニットのジョブID
	 * @param parentJobId 親ジョブID
	 * @param destFacilityId 受信ファシリティID
	 * @param fileInfo ファイル転送ジョブ情報
	 * @param fileList ファイルリスト
	 * @throws FacilityNotFound
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	private void createForwardFileJobNet(
			JobSessionJobEntity nodeJobSessionJob,
			String destFacilityId,
			JobFileInfoData fileInfo,
			List<String> fileList) throws FacilityNotFound, EntityExistsException, HinemosUnknown {

		//公開鍵取得ジョブ作成
		String getKeyJobId = createGetKeyJob(nodeJobSessionJob, destFacilityId, fileInfo);

		//公開鍵設定ジョブ作成
		String waitJobId = createAddKeyJob(nodeJobSessionJob, getKeyJobId, getKeyJobId, fileInfo);

		//ファイル単位のジョブネットを作成
		if (fileList != null) {
			for(int i = 0; i < fileList.size(); i++){
				String getCheckSumJobId = null;

				if(fileInfo.getCheck_flg().booleanValue()){
					//チェックサム取得ジョブ作成
					waitJobId =
							createGetCheckSumJob(
									nodeJobSessionJob,
									waitJobId,
									fileList.get(i),
									String.valueOf(i + 1),
									fileInfo);

					getCheckSumJobId = waitJobId;
				}

				//ファイル転送ジョブ作成
				waitJobId =
						createForwardFileJob(
								nodeJobSessionJob,
								waitJobId,
								destFacilityId,
								fileList.get(i),
								String.valueOf(i + 1),
								fileInfo);

				if(fileInfo.getCheck_flg().booleanValue()){
					//チェックサムチェックジョブの作成
					waitJobId =
							createCheckCheckSumJob(
									nodeJobSessionJob,
									waitJobId,
									destFacilityId,
									getCheckSumJobId,
									fileList.get(i),
									String.valueOf(i + 1),
									fileInfo);
				}
			}
		}

		//公開鍵削除ジョブ作成
		createDeleteKeyJob(nodeJobSessionJob, waitJobId, getKeyJobId, fileInfo);

	}

	/**
	 * ファイルを転送するジョブの実行用情報を作成します。
	 * <p>
	 * <ol>
	 * <li>セッションジョブを作成します。</li>
	 * <li>ジョブリレーション情報を作成します。</li>
	 * <li>ジョブ情報を作成します。</li>
	 * <li>ジョブ待ち条件情報を作成します。</li>
	 * <li>ジョブ待ち条件ジョブ情報を作成します。</li>
	 * <li>ジョブコマンド情報を作成します。</li>
	 * <li>セッションノードを作成します。</li>
	 * <li>ジョブ通知情報を作成します。</li>
	 * <li>ジョブ終了状態情報を作成します。</li>
	 * </ol>
	 * 
	 * @param nodeJobSessionJob 親ジョブのJobSessionJob
	 * @param waitJobId 待ち条件の判定対象ジョブID
	 * @param destFacilityId 受信ファシリティID
	 * @param filePath ファイルパス
	 * @param idCount ファイル番号
	 * @param fileInfo ファイル転送ジョブ情報
	 * @return ジョブID
	 * @throws FacilityNotFound
	 * @throws EntityExistsException
	 */
	private String createForwardFileJob(
			JobSessionJobEntity nodeJobSessionJob,
			String waitJobId,
			String destFacilityId,
			String filePath,
			String idCount,
			JobFileInfoData fileInfo
			) throws FacilityNotFound, EntityExistsException, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();

		//リポジトリ(RepositoryControllerBean)を取得
		RepositoryControllerBean repository = new RepositoryControllerBean();

		// インスタンス生成
		JobInfoEntity jobInfoEntity = subCreateJobInfoEntityForJob (nodeJobSessionJob, "_" + idCount + FORWARD, destFacilityId);

		//待ち条件を設定
		jobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_OR);

		//整合性チェックなしの場合

		//JobStartJobInfoEntityを作成
		// インスタンス生成
		JobStartJobInfoEntity jobStartJobInfoEntity = new JobStartJobInfoEntity(
				jobInfoEntity,
				jobInfoEntity.getId().getJobunitId(),
				waitJobId,
				JudgmentObjectConstant.TYPE_JOB_END_STATUS,
				EndStatusConstant.TYPE_NORMAL);
		// 重複チェック
		jtm.checkEntityExists(JobStartJobInfoEntity.class, jobStartJobInfoEntity.getId());

		NodeInfo info = repository.getNode(fileInfo.getSrc_facility_id());

		//SCPコマンド作成
		StringBuilder command = new StringBuilder();
		command.append(COMMAND_SCP);
		command.append(" ");
		if(fileInfo.getCompression_flg().booleanValue()){
			command.append("-C ");
		}
		//		command.append(fileInfo.getEffective_user());
		//		command.append("@");
		String ipAddressStr = info.getAvailableIpAddress();
		try{
			InetAddress address = InetAddress.getByName(ipAddressStr);
			if (address instanceof Inet6Address){
				ipAddressStr = "[" + ipAddressStr + "]";
			}
		} catch (UnknownHostException e) {
			m_log.info("createForwardFileJob() ipAddress is not valid : " + ipAddressStr);
		}

		command.append(ipAddressStr);
		command.append(":");
		command.append(filePath);
		command.append(" ");
		command.append(fileInfo.getDest_directory());

		//実行コマンドを設定
		jobInfoEntity.setStartCommand(command.toString());
		jobInfoEntity.setArgument(filePath);
		jobInfoEntity.setSpecifyUser(fileInfo.getSpecify_user());
		jobInfoEntity.setEffectiveUser(fileInfo.getEffective_user());
		jobInfoEntity.setMessageRetry(fileInfo.getMessage_retry());
		jobInfoEntity.setCommandRetryFlg(fileInfo.getCommand_retry_flg());
		jobInfoEntity.setCommandRetry(fileInfo.getCommand_retry());

		//通知メッセージを作成
		JobUtil.copyJobNoticeProperties(jobInfoEntity, fileInfo.getJob_id());

		return jobInfoEntity.getId().getJobId();
	}

	/**
	 * チェックサムを取得するジョブの実行用情報を作成します。
	 * <p>
	 * <ol>
	 * <li>セッションジョブを作成します。</li>
	 * <li>ジョブリレーション情報を作成します。</li>
	 * <li>ジョブ情報を作成します。</li>
	 * <li>ジョブ待ち条件情報を作成します。</li>
	 * <li>ジョブ待ち条件ジョブ情報を作成します。</li>
	 * <li>ジョブコマンド情報を作成します。</li>
	 * <li>セッションノードを作成します。</li>
	 * <li>ジョブ通知情報を作成します。</li>
	 * <li>ジョブ終了状態情報を作成します。</li>
	 * </ol>
	 * 
	 * @param nodeJobSessionJob 親ジョブのJobSessionJob
	 * @param waitJobId 待ち条件の判定対象ジョブID
	 * @param filePath ファイルパス
	 * @param idCount ファイル番号
	 * @param fileInfo ファイル転送ジョブ情報
	 * @return ジョブID
	 * @throws FacilityNotFound
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	private String createGetCheckSumJob(
			JobSessionJobEntity nodeJobSessionJob,
			String waitJobId,
			String filePath,
			String idCount,
			JobFileInfoData fileInfo
			) throws FacilityNotFound, EntityExistsException, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();

		// インスタンス生成
		JobInfoEntity jobInfoEntity = subCreateJobInfoEntityForJob (nodeJobSessionJob, "_" + idCount + GET_CHECKSUM, fileInfo.getSrc_facility_id());

		//待ち条件を設定
		jobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_OR);

		//判定対象の作成
		// インスタンス生成
		JobStartJobInfoEntity jobStartJobInfoEntity = new JobStartJobInfoEntity(
				jobInfoEntity,
				jobInfoEntity.getId().getJobunitId(),
				waitJobId,
				JudgmentObjectConstant.TYPE_JOB_END_STATUS,
				EndStatusConstant.TYPE_NORMAL);
		// 重複チェック
		jtm.checkEntityExists(JobStartJobInfoEntity.class, jobStartJobInfoEntity.getId());

		//実行コマンドを設定
		jobInfoEntity.setStartCommand(CommandConstant.GET_CHECKSUM);
		jobInfoEntity.setArgument(filePath);
		jobInfoEntity.setSpecifyUser(fileInfo.getSpecify_user());
		jobInfoEntity.setEffectiveUser(fileInfo.getEffective_user());
		jobInfoEntity.setMessageRetry(fileInfo.getMessage_retry());
		jobInfoEntity.setCommandRetryFlg(false);
		jobInfoEntity.setCommandRetry(fileInfo.getCommand_retry());

		//通知メッセージを作成
		JobUtil.copyJobNoticeProperties(jobInfoEntity, fileInfo.getJob_id());

		return jobInfoEntity.getId().getJobId();
	}


	/**
	 * チェックサムを比較するジョブの実行用情報を作成します。
	 * <p>
	 * <ol>
	 * <li>セッションジョブを作成します。</li>
	 * <li>ジョブリレーション情報を作成します。</li>
	 * <li>ジョブ情報を作成します。</li>
	 * <li>ジョブ待ち条件情報を作成します。</li>
	 * <li>ジョブ待ち条件ジョブ情報を作成します。</li>
	 * <li>ジョブコマンド情報を作成します。</li>
	 * <li>セッションノードを作成します。</li>
	 * <li>ジョブ通知情報を作成します。</li>
	 * <li>ジョブ終了状態情報を作成します。</li>
	 * </ol>
	 * 
	 * @param nodeJobSessionJob 親ジョブのJobSessionJob
	 * @param waitJobId 待ち条件の判定対象ジョブID
	 * @param destFacilityId 受信ファシリティID
	 * @param argumentJobId 引数のジョブID
	 * @param filePath ファイルパス
	 * @param idCount ファイル番号
	 * @param fileInfo ファイル転送ジョブ情報
	 * @return ジョブID
	 * @throws EntityExistsException
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	private String createCheckCheckSumJob(
			JobSessionJobEntity nodeJobSessionJob,
			String waitJobId,
			String destFacilityId,
			String argumentJobId,
			String filePath,
			String idCount,
			JobFileInfoData fileInfo
			) throws FacilityNotFound, EntityExistsException, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();

		// インスタンス生成
		JobInfoEntity jobInfoEntity = subCreateJobInfoEntityForJob (nodeJobSessionJob, "_" + idCount + CHECK_CHECKSUM, destFacilityId);

		//待ち条件を設定
		jobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_AND);

		//JobStartJobInfoEntityを作成
		// インスタンス生成
		JobStartJobInfoEntity jobStartJobInfoEntity = new JobStartJobInfoEntity(
				jobInfoEntity,
				jobInfoEntity.getId().getJobunitId(),
				waitJobId,
				JudgmentObjectConstant.TYPE_JOB_END_STATUS,
				EndStatusConstant.TYPE_NORMAL);
		// 重複チェック
		jtm.checkEntityExists(JobStartJobInfoEntity.class, jobStartJobInfoEntity.getId());

		//ファイルパス取得
		int index = filePath.lastIndexOf("/");
		String fileName = null;
		Pattern p = Pattern.compile(".*/");
		Matcher m = p.matcher(fileInfo.getDest_directory());
		if (m.matches()) {
			fileName = fileInfo.getDest_directory() + filePath.substring(index + 1);
		}
		else{
			fileName = fileInfo.getDest_directory() + "/" + filePath.substring(index + 1);
		}

		//実行コマンドを設定
		jobInfoEntity.setStartCommand(CommandConstant.CHECK_CHECKSUM);
		jobInfoEntity.setArgumentJobId(argumentJobId);
		jobInfoEntity.setArgument(fileName);
		jobInfoEntity.setSpecifyUser(fileInfo.getSpecify_user());
		jobInfoEntity.setEffectiveUser(fileInfo.getEffective_user());
		jobInfoEntity.setMessageRetry(fileInfo.getMessage_retry());
		jobInfoEntity.setCommandRetryFlg(false);
		jobInfoEntity.setCommandRetry(fileInfo.getCommand_retry());

		//通知メッセージを作成
		JobUtil.copyJobNoticeProperties(jobInfoEntity, fileInfo.getJob_id());

		return jobInfoEntity.getId().getJobId();
	}

	/**
	 * 公開キーを取得するジョブの実行用情報を作成します。
	 * <p>
	 * <ol>
	 * <li>セッションジョブを作成します。</li>
	 * <li>ジョブリレーション情報を作成します。</li>
	 * <li>ジョブ情報を作成します。</li>
	 * <li>ジョブ待ち条件情報を作成します。</li>
	 * <li>ジョブ待ち条件ジョブ情報を作成します。</li>
	 * <li>ジョブコマンド情報を作成します。</li>
	 * <li>セッションノードを作成します。</li>
	 * <li>ジョブ通知情報を作成します。</li>
	 * <li>ジョブ終了状態情報を作成します。</li>
	 * </ol>
	 * 
	 * @param nodeJobSessionJob 親ジョブのJobSessionJob
	 * @param destFacilityId 受信ファシリティID
	 * @param fileInfo ファイル転送ジョブ情報
	 * @return ジョブID
	 * @throws EntityExistsException
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	private String createGetKeyJob(
			JobSessionJobEntity nodeJobSessionJob,
			String destFacilityId,
			JobFileInfoData fileInfo
			) throws EntityExistsException, FacilityNotFound, HinemosUnknown {

		// インスタンス生成
		JobInfoEntity jobInfoEntity = subCreateJobInfoEntityForJob (nodeJobSessionJob, GET_KEY, destFacilityId);

		//待ち条件を設定
		jobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_AND);

		//実行コマンドを設定
		jobInfoEntity.setStartCommand(CommandConstant.GET_PUBLIC_KEY);
		jobInfoEntity.setSpecifyUser(fileInfo.getSpecify_user());
		jobInfoEntity.setEffectiveUser(fileInfo.getEffective_user());
		jobInfoEntity.setMessageRetry(fileInfo.getMessage_retry());
		jobInfoEntity.setCommandRetryFlg(false);
		jobInfoEntity.setCommandRetry(fileInfo.getCommand_retry());

		//通知メッセージを作成
		JobUtil.copyJobNoticeProperties(jobInfoEntity, fileInfo.getJob_id());

		return jobInfoEntity.getId().getJobId();
	}

	/**
	 * 公開キーを追加するジョブの実行用情報を作成します。
	 * <p>
	 * <ol>
	 * <li>セッションジョブを作成します。</li>
	 * <li>ジョブリレーション情報を作成します。</li>
	 * <li>ジョブ情報を作成します。</li>
	 * <li>ジョブ待ち条件情報を作成します。</li>
	 * <li>ジョブ待ち条件ジョブ情報を作成します。</li>
	 * <li>ジョブコマンド情報を作成します。</li>
	 * <li>セッションノードを作成します。</li>
	 * <li>ジョブ通知情報を作成します。</li>
	 * <li>ジョブ終了状態情報を作成します。</li>
	 * </ol>
	 * 
	 * @param nodeJobSessionJob 親ジョブのJobSessionJob
	 * @param waitJobId 待ち条件の判定対象ジョブID
	 * @param argumentJobId 引数のジョブID
	 * @param fileInfo ファイル転送ジョブ情報
	 * @return ジョブID
	 * @throws EntityExistsException
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	private String createAddKeyJob(
			JobSessionJobEntity nodeJobSessionJob,
			String waitJobId,
			String argumentJobId,
			JobFileInfoData fileInfo
			) throws EntityExistsException, FacilityNotFound, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();

		// インスタンス生成
		JobInfoEntity jobInfoEntity = subCreateJobInfoEntityForJob (nodeJobSessionJob, ADD_KEY, fileInfo.getSrc_facility_id());

		//待ち条件を設定
		jobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_AND);

		if(waitJobId != null && waitJobId.length() > 0){
			//JobStartJobInfoEntityを作成
			JobStartJobInfoEntity jobStartJobInfoEntity = new JobStartJobInfoEntity(
					jobInfoEntity,
					jobInfoEntity.getId().getJobunitId(),
					waitJobId,
					JudgmentObjectConstant.TYPE_JOB_END_STATUS,
					EndStatusConstant.TYPE_NORMAL);
			// 重複チェック
			jtm.checkEntityExists(JobStartJobInfoEntity.class, jobStartJobInfoEntity.getId());
		}

		//実行コマンドを設定
		jobInfoEntity.setStartCommand(CommandConstant.ADD_PUBLIC_KEY);
		jobInfoEntity.setArgumentJobId(argumentJobId);
		jobInfoEntity.setSpecifyUser(fileInfo.getSpecify_user());
		jobInfoEntity.setEffectiveUser(fileInfo.getEffective_user());
		jobInfoEntity.setMessageRetry(fileInfo.getMessage_retry());
		jobInfoEntity.setCommandRetryFlg(false);
		jobInfoEntity.setCommandRetry(fileInfo.getCommand_retry());

		//通知メッセージを作成
		JobUtil.copyJobNoticeProperties(jobInfoEntity, fileInfo.getJob_id());

		return jobInfoEntity.getId().getJobId();
	}

	/**
	 * 公開キーを削除するジョブの実行用情報を作成します。
	 * <p>
	 * <ol>
	 * <li>セッションジョブを作成します。</li>
	 * <li>ジョブリレーション情報を作成します。</li>
	 * <li>ジョブ情報を作成します。</li>
	 * <li>ジョブ待ち条件情報を作成します。</li>
	 * <li>ジョブ待ち条件ジョブ情報を作成します。</li>
	 * <li>ジョブコマンド情報を作成します。</li>
	 * <li>セッションノードを作成します。</li>
	 * <li>ジョブ通知情報を作成します。</li>
	 * <li>ジョブ終了状態情報を作成します。</li>
	 * </ol>
	 * 
	 * @param nodeJobSessionJob 親ジョブのJobSessionJob
	 * @param waitJobId 待ち条件の判定対象ジョブID
	 * @param argumentJobId 引数のジョブID
	 * @param fileInfo ファイル転送ジョブ情報
	 * @return ジョブID
	 * @throws EntityExistsException
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	private String createDeleteKeyJob(
			JobSessionJobEntity nodeJobSessionJob,
			String waitJobId,
			String argumentJobId,
			JobFileInfoData fileInfo
			) throws EntityExistsException, FacilityNotFound, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();

		// インスタンス生成
		JobInfoEntity jobInfoEntity = subCreateJobInfoEntityForJob (nodeJobSessionJob, DEL_KEY, fileInfo.getSrc_facility_id());

		//待ち条件を設定
		jobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_OR);

		if(waitJobId != null && waitJobId.length() > 0){
			//JobStartJobInfoEntityを作成
			Integer[] targetJobEndValues = {EndStatusConstant.TYPE_NORMAL,
					EndStatusConstant.TYPE_WARNING,
					EndStatusConstant.TYPE_ABNORMAL};
			for (Integer targetJobEndValue : targetJobEndValues) {
				// インスタンス生成
				JobStartJobInfoEntity jobStartJobInfoEntity = new JobStartJobInfoEntity(
						jobInfoEntity,
						jobInfoEntity.getId().getJobunitId(),
						waitJobId,
						JudgmentObjectConstant.TYPE_JOB_END_STATUS,
						targetJobEndValue);
				// 重複チェック
				jtm.checkEntityExists(JobStartJobInfoEntity.class, jobStartJobInfoEntity.getId());
			}
		}

		//実行コマンドを設定
		jobInfoEntity.setStartCommand(CommandConstant.DELETE_PUBLIC_KEY);
		jobInfoEntity.setArgumentJobId(argumentJobId);
		jobInfoEntity.setSpecifyUser(fileInfo.getSpecify_user());
		jobInfoEntity.setEffectiveUser(fileInfo.getEffective_user());
		jobInfoEntity.setMessageRetry(fileInfo.getMessage_retry());
		jobInfoEntity.setCommandRetryFlg(false);
		jobInfoEntity.setCommandRetry(fileInfo.getCommand_retry());

		//通知メッセージを作成
		JobUtil.copyJobNoticeProperties(jobInfoEntity, fileInfo.getJob_id());

		return jobInfoEntity.getId().getJobId();
	}

	/**
	 * JobSessionJobEntity、JobSessionNodeEntity、JobInfoEntityを作成する。
	 * 
	 * jobSessionJobEntityのセッションID、ジョブユニットID、ジョブID＋jobIdSuffixを主キーとした
	 * JobSessionJobEntityとJobInfoEntityを作成する。
	 * JobInfoEntityにはファイル転送ジョブ用に設定が必要な項目も一緒に設定しています。
	 * 
	 * @param jobSessinJobEntity 引用するjobSessionJobEntity
	 * @param jobIdSuffix 接尾辞
	 * @param facilityId ファシリティID
	 * @return 作成したJobInfoEntity
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	private JobInfoEntity subCreateJobInfoEntityForJob (JobSessionJobEntity jobSessionJobEntity, String jobIdSuffix, String facilityId)
			throws FacilityNotFound, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();

		//リポジトリ(RepositoryControllerBean)を取得
		RepositoryControllerBean repository = new RepositoryControllerBean();

		String jobunitId = jobSessionJobEntity.getId().getJobunitId();
		String jobId = jobSessionJobEntity.getId().getJobId() + jobIdSuffix;

		//JobSessionEntityを設定
		JobSessionEntity jobSessionEntity = jobSessionJobEntity.getJobSessionEntity();

		//JobSessionJobを作成
		// インスタンス作成
		JobSessionJobEntity sessionJob
		= new JobSessionJobEntity(jobSessionEntity, jobunitId, jobId);
		// 重複チェック
		jtm.checkEntityExists(JobSessionJobEntity.class, sessionJob.getId());
		sessionJob.setStatus(StatusConstant.TYPE_WAIT);
		sessionJob.setParentJobunitId(jobSessionJobEntity.getId().getJobunitId());
		sessionJob.setParentJobId(jobSessionJobEntity.getId().getJobId());
		sessionJob.setEndStausCheckFlg(EndStatusCheckConstant.NO_WAIT_JOB);
		sessionJob.setScopeText(repository.getFacilityPath(facilityId, null));

		//JobInfoEntityを作成
		// インスタンス生成
		JobInfoEntity jobInfoEntity = new JobInfoEntity(sessionJob);
		// 重複チェック
		jtm.checkEntityExists(JobInfoEntity.class, jobInfoEntity.getId());
		jobInfoEntity.setJobType(JobConstant.TYPE_JOB);
		jobInfoEntity.setUnmatchEndFlg(true);
		jobInfoEntity.setUnmatchEndValue(ABNORMAL);
		jobInfoEntity.setUnmatchEndStatus(EndStatusConstant.TYPE_ABNORMAL);
		jobInfoEntity.setStartDelayNotifyPriority(PriorityConstant.TYPE_CRITICAL);
		jobInfoEntity.setStartDelayOperationType(OperationConstant.TYPE_STOP_SKIP);
		jobInfoEntity.setEndDelayNotifyPriority(PriorityConstant.TYPE_CRITICAL);
		jobInfoEntity.setEndDelayOperationType(OperationConstant.TYPE_STOP_AT_ONCE);
		jobInfoEntity.setProcessMode(ProcessingMethodConstant.TYPE_ALL_NODE);
		jobInfoEntity.setMessageRetryEndFlg(true);
		jobInfoEntity.setMessageRetryEndValue(ABNORMAL);
		jobInfoEntity.setFacilityId(facilityId);

		// ファイル転送を中止する場合は、プロセス停止とする。
		jobInfoEntity.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);
		// 多重度の設定
		JobInfoEntity jobInfo = jobSessionJobEntity.getJobInfoEntity();
		jobInfoEntity.setMultiplicityEndValue(jobInfo.getMultiplicityEndValue());
		jobInfoEntity.setMultiplicityNotify(jobInfo.getMultiplicityNotify());
		jobInfoEntity.setMultiplicityNotifyPriority(jobInfo.getMultiplicityNotifyPriority());
		jobInfoEntity.setMultiplicityOperation(jobInfo.getMultiplicityOperation());
		jobInfoEntity.setRegisteredModule(jobInfo.isRegisteredModule());

		// 終了状態を作成
		// 正常
		jobInfoEntity.setNormalEndValue(NORMAL);
		jobInfoEntity.setNormalEndValueFrom(NORMAL);
		jobInfoEntity.setNormalEndValueTo(NORMAL);
		// 警告
		jobInfoEntity.setWarnEndValue(WARNING);
		jobInfoEntity.setWarnEndValueFrom(WARNING);
		jobInfoEntity.setWarnEndValueTo(WARNING);
		// 異常
		jobInfoEntity.setAbnormalEndValue(ABNORMAL);
		jobInfoEntity.setAbnormalEndValueFrom(ABNORMAL);
		jobInfoEntity.setAbnormalEndValueTo(ABNORMAL);

		//JobSessionNodeを作成
		// インスタンス生成
		JobSessionNodeEntity jobSessionNodeEntity
		= new JobSessionNodeEntity(jobInfoEntity.getJobSessionJobEntity(), facilityId);
		// 重複チェック
		jtm.checkEntityExists(JobSessionNodeEntity.class, jobSessionNodeEntity.getId());
		NodeInfo info = repository.getNode(facilityId);
		jobSessionNodeEntity.setNodeName(info.getFacilityName());
		jobSessionNodeEntity.setStatus(StatusConstant.TYPE_WAIT);
		jobSessionNodeEntity.setMessage(null);

		return jobInfoEntity;
	}

	private static class JobFileInfoData
	{
		private String session_id;
		private String jobunit_id;
		private String job_id;
		private String src_facility_id;
		private String dest_facility_id;
		private Integer process_mode;
		private String dest_directory;
		private Boolean compression_flg;
		private Boolean check_flg;
		private Boolean specify_user;
		private String effective_user;
		private Integer message_retry;
		private Integer command_retry;
		private Boolean command_retry_flg;

		public JobFileInfoData( String session_id,String jobunit_id,String job_id,String src_facility_id,String dest_facility_id,Integer process_mode,String src_file,String src_work_dir,String dest_directory,String dest_work_dir,Boolean compression_flg,Boolean check_flg,Boolean specify_user, String effective_user, Integer message_retry, Integer command_retry, Boolean command_retry_flg)
		{
			setSession_id(session_id);
			setJobunit_id(jobunit_id);
			setJob_id(job_id);
			setSrc_facility_id(src_facility_id);
			setDest_facility_id(dest_facility_id);
			setProcess_mode(process_mode);
			setDest_directory(dest_directory);
			setCompression_flg(compression_flg);
			setCheck_flg(check_flg);
			setSpecify_user(specify_user);
			setEffective_user(effective_user);
			setMessage_retry(message_retry);
			setCommand_retry(command_retry);
			setCommand_retry_flg(command_retry_flg);
		}

		public void setSession_id( String session_id )
		{
			this.session_id = session_id;
		}

		public void setJobunit_id( String jobunit_id )
		{
			this.jobunit_id = jobunit_id;
		}

		public void setJob_id( String job_id )
		{
			this.job_id = job_id;
		}

		public String getSrc_facility_id()
		{
			return this.src_facility_id;
		}
		public void setSrc_facility_id( String src_facility_id )
		{
			this.src_facility_id = src_facility_id;
		}

		public String getDest_facility_id()
		{
			return this.dest_facility_id;
		}
		public void setDest_facility_id( String dest_facility_id )
		{
			this.dest_facility_id = dest_facility_id;
		}

		public Integer getProcess_mode()
		{
			return this.process_mode;
		}
		public void setProcess_mode( Integer process_mode )
		{
			this.process_mode = process_mode;
		}

		public String getDest_directory()
		{
			return this.dest_directory;
		}
		public void setDest_directory( String dest_directory )
		{
			this.dest_directory = dest_directory;
		}

		public Boolean getCompression_flg()
		{
			return this.compression_flg;
		}
		public void setCompression_flg( Boolean compression_flg )
		{
			this.compression_flg = compression_flg;
		}

		public Boolean getCheck_flg()
		{
			return this.check_flg;
		}
		public void setCheck_flg( Boolean check_flg )
		{
			this.check_flg = check_flg;
		}

		public Boolean getSpecify_user()
		{
			return this.specify_user;
		}
		public void setSpecify_user( Boolean specify_user )
		{
			this.specify_user = specify_user;
		}

		public String getEffective_user()
		{
			return this.effective_user;
		}
		public void setEffective_user( String effective_user )
		{
			this.effective_user = effective_user;
		}

		public Integer getMessage_retry() {
			return message_retry;
		}
		public void setMessage_retry(Integer message_retry) {
			this.message_retry = message_retry;
		}

		public Integer getCommand_retry() {
			return command_retry;
		}

		public void setCommand_retry(Integer command_retry) {
			this.command_retry = command_retry;
		}

		public Boolean getCommand_retry_flg() {
			return command_retry_flg;
		}

		public void setCommand_retry_flg(Boolean command_retry_flg) {
			this.command_retry_flg = command_retry_flg;
		}

		public String getJobunit_id() {
			return jobunit_id;
		}

		public String getJob_id() {
			return job_id;
		}

		@Override
		public String toString()
		{
			StringBuffer str = new StringBuffer("{");

			str.append("session_id=" + this.session_id + " jobunit_id=" + this.getJobunit_id() + " job_id=" + this.getJob_id() +
					" src_facility_id=" + this.src_facility_id + " dest_facility_id=" + this.dest_facility_id +
					" process_mode=" + this.process_mode + " dest_directory=" + this.dest_directory +
					" compression_flg=" + this.compression_flg + " check_flg=" + this.check_flg +
					" effective_user=" + this.effective_user + " message_retry=" + this.message_retry +
					" command_retry=" + this.command_retry + " " + "command_retry_flg=" + this.command_retry_flg);
			str.append('}');

			return(str.toString());
		}

	}

}
