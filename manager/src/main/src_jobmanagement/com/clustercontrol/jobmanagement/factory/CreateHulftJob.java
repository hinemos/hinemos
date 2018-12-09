/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.JobInfoNotFound;
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
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * ファイル転送ジョブの実行用情報を作成するクラスです。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class CreateHulftJob {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( CreateHulftJob.class );

	/** 配信管理情報の登録(utliupdtコマンド) */
	public static final String UTILIUPDT_S = "_UTLIUPDT_S";
	public static final String UTILIUPDT_S_SH = "utliupdt-s.sh";
	public static final String UTILIUPDT_S_VBS = "utliupdt-s.vbs";
	/** 集信管理情報の登録(utliupdtコマンド) */
	public static final String UTILIUPDT_R = "_UTLIUPDT_R";
	public static final String UTILIUPDT_R_SH = "utliupdt-r.sh";
	public static final String UTILIUPDT_R_VBS = "utliupdt-r.vbs";
	/** ホスト管理情報の登録(utliupdtコマンド) */
	public static final String UTILIUPDT_H_SND = "_UTLIUPDT_H_SND";
	public static final String UTILIUPDT_H_RCV = "_UTLIUPDT_H_RCV";
	public static final String UTILIUPDT_H_SH = "utliupdt-h.sh";
	public static final String UTILIUPDT_H_VBS = "utliupdt-h.vbs";
	/** 配信要求(utlsendコマンド) */
	public static final String UTLSEND = "_UTLSEND";
	public static final String UTLSEND_SH = "utlsendfile.sh";
	public static final String UTLSEND_VBS = "utlsendfile.vbs";
	/** 履歴チェック(huloplcmdファイルのチェックとメッセージファイルへの照会) */
	public static final String HULOPLCMD = "_HULOPLCMD";
	public static final String HULOPLCMD_SH = "huloplcmd.sh";
	public static final String HULOPLCMD_VBS = "huloplcmd.vbs";

	/** HULFT用のスクリプト配置ディレクトリ(LINUX) */
	public final static String SCRIPT_DIR_LINUX = "../../hulft/sh/";
	/** HULFT用のスクリプト配置ディレクトリ(Windows) */
	public final static String SCRIPT_DIR_WIN = "C:\\Program Files (x86)\\Hinemos\\Agent6.0.0\\hulft\\vbs\\";
	
	/** 正常の終了値及び終了値の範囲 */
	private static final int NORMAL = 0;
	/** 警告の終了値及び終了値の範囲 */
	private static final int WARNING = 1;
	/** 異常の終了値及び終了値の範囲 */
	private static final int ABNORMAL = -1;
	/** 異常の終了値(ファイル転送ジョブ) */
	private static final int ABNORMAL_FOR_FILE = 9;

	private String getScriptDirLinux() {
		/** HULFT用のスクリプト配置ディレクトリ */
		String scriptDirLinux = CreateHulftJob.SCRIPT_DIR_LINUX;
		String dir = HinemosPropertyCommon.job_hulft_script_dir_linux.getStringValue();
		if (dir != null && dir.length() >0) {
			scriptDirLinux = dir + "/";
		}
		m_log.info("job.hulft.script.dir.linux=" + scriptDirLinux);

		return scriptDirLinux;
	}

	private String getScriptDirWin() {
		/** HULFT用のスクリプト配置ディレクトリ */
		String scriptDirWin = CreateHulftJob.SCRIPT_DIR_WIN;
		String dir = HinemosPropertyCommon.job_hulft_script_dir_windows.getStringValue();
		if (dir != null && dir.length() >0) {
			scriptDirWin = dir + "\\";
		}
		m_log.info("job.hulft.script.dir.windows=" + scriptDirWin);

		return scriptDirWin;
	}

	public static boolean isHulftMode() {
		boolean hulftMode = HinemosPropertyCommon.job_hulft_mode.getBooleanValue();
		m_log.info("hulftMode=" + hulftMode);
		return hulftMode;
	}

	protected void createHulftDetailJob(JobInfoEntity parentJobInfo)throws FacilityNotFound, EntityExistsException, HinemosUnknown, JobInfoNotFound {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			//親ジョブのセッションジョブからファイル転送情報を取得
			JobSessionJobEntity parentSessionJob = parentJobInfo.getJobSessionJobEntity();
			JobInfoEntity job = parentSessionJob.getJobInfoEntity();
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
			RepositoryControllerBean repository = new RepositoryControllerBean();
			ArrayList<String> nodeIdList = repository.getExecTargetFacilityIdList(
					fileData.getDest_facility_id(), parentSessionJob.getOwnerRoleId());

			//対象ノードをジョブ優先度の降順にて並び替え
			int[][] sortArray =new int[nodeIdList.size()][2];//リスト行番号,優先度の配列を作成
			for(int i = 0; i < sortArray.length; i++){
				sortArray[i][0] = i;
				sortArray[i][1] = repository.getNode( nodeIdList.get(i)).getJobPriority();
			}
			Arrays.sort(sortArray, new Comparator<int[]>() {
				@Override //[1]にセットした優先度をキーに配列をソート(降順)
				public int compare(int[] o1, int[] o2) {
					return o2[1] - o1[1];	 
				}
			});
			ArrayList<String> nodeIdListTmp =new ArrayList<String>();
			for (int ArrayCnt= 0 ; ArrayCnt < sortArray.length; ArrayCnt++	 ){
				nodeIdListTmp.add( nodeIdList.get(sortArray[ArrayCnt][0]));
			}
			nodeIdList = nodeIdListTmp;//ソート結果を反映

			// 配信管理情報の登録ジョブ
			JobInfoEntity jobUtiliupdtS = subCreateJobInfoEntityForJob (parentSessionJob, UTILIUPDT_S, parentJobInfo.getSrcFacilityId());
			JobSessionJobEntity jobSessionJobEntity = jobUtiliupdtS.getJobSessionJobEntity();
			jobUtiliupdtS.setRegDate(parentJobInfo.getRegDate());
			jobUtiliupdtS.setUpdateDate(parentJobInfo.getUpdateDate());
			jobUtiliupdtS.setRegUser(parentJobInfo.getRegUser());
			jobUtiliupdtS.setUpdateUser(parentJobInfo.getUpdateUser());
			jobUtiliupdtS.setConditionType(ConditionTypeConstant.TYPE_AND);
			String command;
			if (repository.getNode(parentJobInfo.getSrcFacilityId()).getPlatformFamily().equals("WINDOWS")) {
				// Windowsの場合の起動コマンド
				command = "cscript.exe //nologo \"" + getScriptDirWin() + UTILIUPDT_S_VBS + "\" " + parentJobInfo.getId().getJobId();
			} else {
				// Linuxの場合の起動コマンド
				command = getScriptDirLinux() + UTILIUPDT_S_SH + " " + parentJobInfo.getId().getJobId();
			}
			m_log.info("startCommand=" + command);
			jobUtiliupdtS.setStartCommand(command);
			jobUtiliupdtS.setSpecifyUser(parentJobInfo.getSpecifyUser());
			jobUtiliupdtS.setEffectiveUser(parentJobInfo.getEffectiveUser());
			jobUtiliupdtS.setArgument(parentJobInfo.getSrcFile());
			jobUtiliupdtS.setMessageRetry(parentJobInfo.getMessageRetry());
			jobUtiliupdtS.setCommandRetry(parentJobInfo.getCommandRetry());
			jobUtiliupdtS.setCommandRetryFlg(false);
			//通知メッセージを作成
			JobUtil.copyJobNoticeProperties(jobUtiliupdtS, job.getId().getJobId());

			//ジョブユニットのジョブIDは親のものと同じとする。
			String jobunitId = parentSessionJob.getId().getJobunitId();

			//ノード単位のジョブネットを作成
			String waitJobId = jobSessionJobEntity.getId().getJobId();
			for(int i = 0; i < nodeIdList.size(); i++){
				String nodeJobId = parentSessionJob.getId().getJobId() + "_" + nodeIdList.get(i);

				//JobSessionJobを作成
				// インスタンス生成
				JobSessionJobEntity nodeJobSessionJob
					= new JobSessionJobEntity(parentSessionJob.getJobSessionEntity(), jobunitId, nodeJobId);
				// 重複チェック
				jtm.checkEntityExists(JobSessionJobEntity.class, nodeJobSessionJob.getId());
				nodeJobSessionJob.setParentJobunitId(parentSessionJob.getId().getJobunitId());
				nodeJobSessionJob.setParentJobId(parentSessionJob.getId().getJobId());
				nodeJobSessionJob.setStatus(StatusConstant.TYPE_WAIT);
				nodeJobSessionJob.setEndStausCheckFlg(EndStatusCheckConstant.ALL_JOB);
				nodeJobSessionJob.setDelayNotifyFlg(DelayNotifyConstant.NONE);
				nodeJobSessionJob.setOwnerRoleId(JobUtil.createSessioniOwnerRoleId(jobunitId));
				// 登録
				em.persist(nodeJobSessionJob);
				nodeJobSessionJob.relateToJobSessionEntity(parentSessionJob.getJobSessionEntity());

				//JobInfoEntityを作成
				// インスタンス生成
				JobInfoEntity nodeJobInfoEntity = new JobInfoEntity(nodeJobSessionJob);
				// 重複チェック
				jtm.checkEntityExists(JobInfoEntity.class, nodeJobInfoEntity.getId());
				// 登録
				em.persist(nodeJobInfoEntity);
				nodeJobInfoEntity.relateToJobSessionJobEntity(nodeJobSessionJob);

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
				// 多重度の設定
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
						// 登録
						em.persist(jobStartJobInfoEntity);
						jobStartJobInfoEntity.relateToJobInfoEntity(nodeJobInfoEntity);
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
							// 登録
							em.persist(jobStartJobInfoEntity);
							jobStartJobInfoEntity.relateToJobInfoEntity(nodeJobInfoEntity);
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
						// 登録
						em.persist(jobStartJobInfoEntity);
						jobStartJobInfoEntity.relateToJobInfoEntity(nodeJobInfoEntity);
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
						// 登録
						em.persist(jobStartJobInfoEntity);
						jobStartJobInfoEntity.relateToJobInfoEntity(nodeJobInfoEntity);

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
						// 登録
						em.persist(jobStartJobInfoEntity);
						jobStartJobInfoEntity.relateToJobInfoEntity(nodeJobInfoEntity);
					}
				}

				//JobSessionJobにファシリティパスを設定
				nodeJobSessionJob.setScopeText(repository.getFacilityPath(nodeIdList.get(i), null));

				//通知メッセージを作成
				JobUtil.copyJobNoticeProperties(nodeJobInfoEntity, job.getId().getJobId());

				//ファイル転送ジョブネットの作成
				createForwardFileJobNet(nodeJobSessionJob, nodeIdList.get(i), fileData,
						parentJobInfo.getSrcFile(), parentJobInfo.getId().getJobId());

				if(fileData.getProcess_mode() == ProcessingMethodConstant.TYPE_RETRY) {
					waitJobId = nodeJobId;
				}
			}
		}
	}

	private void createForwardFileJobNet(
			JobSessionJobEntity nodeJobSessionJob,
			String destFacilityId,
			JobFileInfoData fileInfo,
			String filePath,
			String parentJobId) throws FacilityNotFound, EntityExistsException, HinemosUnknown {

		//集信管理情報の登録(UTILIUPDT_R)
		String waitJobId = utiliupdtR(nodeJobSessionJob, destFacilityId, fileInfo, parentJobId);

		//配信側にホスト管理情報の登録(UTILIUPDT_H)
		waitJobId = utliupdtH_snd(nodeJobSessionJob, waitJobId, destFacilityId, fileInfo);

		//配信側にホスト管理情報の登録(UTILIUPDT_H)
		waitJobId = utliupdtH_rcv(nodeJobSessionJob, waitJobId, destFacilityId, fileInfo);

		//ファイル転送(UTLSEND)
		waitJobId = utlsend(nodeJobSessionJob, waitJobId, destFacilityId,
						filePath, String.valueOf(1), fileInfo, parentJobId);

		//履歴情報の確認(HULOPLCMD)
		huloplcmd(nodeJobSessionJob, waitJobId, filePath, fileInfo);

	}

	private String utlsend(
			JobSessionJobEntity nodeJobSessionJob,
			String waitJobId,
			String destFacilityId,
			String filePath,
			String idCount,
			JobFileInfoData fileInfo,
			String parentJobId) throws FacilityNotFound, EntityExistsException, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			//リポジトリ(RepositoryControllerBean)を取得
			RepositoryControllerBean repository = new RepositoryControllerBean();

			// インスタンス生成
			JobInfoEntity jobInfoEntity = subCreateJobInfoEntityForJob (nodeJobSessionJob,
					"_" + idCount + UTLSEND,  fileInfo.getSrc_facility_id());

			//待ち条件を設定
			jobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_OR);
			jobInfoEntity.setUnmatchEndFlg(true);
			jobInfoEntity.setUnmatchEndValue(ABNORMAL);
			jobInfoEntity.setUnmatchEndStatus(EndStatusConstant.TYPE_ABNORMAL);

			//判定対象を作成
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
			// 登録
			em.persist(jobStartJobInfoEntity);
			jobStartJobInfoEntity.relateToJobInfoEntity(jobInfoEntity);

			NodeInfo info = repository.getNode(destFacilityId);

			//実行コマンドを設定
			String command;
			if (repository.getNode(fileInfo.getSrc_facility_id()).getPlatformFamily().equals("WINDOWS")) {
				// Windowsの場合の起動コマンド
				command = "cscript.exe //nologo \"" + getScriptDirWin() + UTLSEND_VBS + "\" " + parentJobId + " \"" + filePath+ "\" " + info.getNodeName();
			} else {
				// Linuxの場合の起動コマンド
				command = getScriptDirLinux() + UTLSEND_SH + " " + parentJobId + " " + filePath+ " " + info.getNodeName();
			}
			m_log.info("startCommand=" + command);
			jobInfoEntity.setStartCommand(command);
			jobInfoEntity.setSpecifyUser(fileInfo.getSpecify_user());
			jobInfoEntity.setEffectiveUser(fileInfo.getEffective_user());
			jobInfoEntity.setArgument(filePath);
			jobInfoEntity.setMessageRetry(fileInfo.getMessage_retry());
			jobInfoEntity.setCommandRetry(fileInfo.getCommand_retry());
			jobInfoEntity.setCommandRetryFlg(false);
			

			//通知メッセージを作成
			JobUtil.copyJobNoticeProperties(jobInfoEntity, fileInfo.job_id);

			return jobInfoEntity.getId().getJobId();
		}
	}

	private String utiliupdtR(
			JobSessionJobEntity nodeJobSessionJob,
			String destFacilityId,
			JobFileInfoData fileInfo,
			String parentJobId) throws EntityExistsException, FacilityNotFound, HinemosUnknown {

		//リポジトリ(RepositoryControllerBean)を取得
		RepositoryControllerBean repository = new RepositoryControllerBean();

		// インスタンス生成
		JobInfoEntity jobInfoEntity = subCreateJobInfoEntityForJob (nodeJobSessionJob,
				UTILIUPDT_R, destFacilityId);

		//待ち条件を設定
		jobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_AND);
		jobInfoEntity.setUnmatchEndFlg(true);
		jobInfoEntity.setUnmatchEndValue(ABNORMAL);
		jobInfoEntity.setUnmatchEndStatus(EndStatusConstant.TYPE_ABNORMAL);

		//実行コマンドを設定
		String command;
		if (repository.getNode(destFacilityId).getPlatformFamily().equals("WINDOWS")) {
			// Windowsの場合の起動コマンド
			command = "cscript.exe //nologo \"" + getScriptDirWin() + UTILIUPDT_R_VBS + "\" " + parentJobId + " \"" + fileInfo.getDest_directory() + "\"";
		} else {
			// Linuxの場合の起動コマンド
			command = getScriptDirLinux() + UTILIUPDT_R_SH + " " + parentJobId + " " + fileInfo.getDest_directory();
		}
		m_log.info("startCommand=" + command);
		jobInfoEntity.setStartCommand(command);
		jobInfoEntity.setSpecifyUser(fileInfo.getSpecify_user());
		jobInfoEntity.setEffectiveUser(fileInfo.getEffective_user());
		jobInfoEntity.setMessageRetry(fileInfo.getMessage_retry());
		jobInfoEntity.setCommandRetry(fileInfo.getCommand_retry());
		jobInfoEntity.setCommandRetryFlg(false);

		//通知メッセージを作成
		JobUtil.copyJobNoticeProperties(jobInfoEntity, fileInfo.job_id);

		return jobInfoEntity.getId().getJobId();
	}

	private String utliupdtH_snd(
			JobSessionJobEntity nodeJobSessionJob,
			String waitJobId,
			String destFacilityId,
			JobFileInfoData fileInfo
			) throws FacilityNotFound, HinemosUnknown {

		// 配信側に集信側のホスト管理情報を登録する

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// インスタンス生成
			JobInfoEntity jobInfoEntity = subCreateJobInfoEntityForJob (nodeJobSessionJob,
					UTILIUPDT_H_SND, fileInfo.getSrc_facility_id());

			//待ち条件を設定
			jobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_OR);
			jobInfoEntity.setUnmatchEndFlg(true);
			jobInfoEntity.setUnmatchEndValue(ABNORMAL);
			jobInfoEntity.setUnmatchEndStatus(EndStatusConstant.TYPE_ABNORMAL);

			if(waitJobId != null && waitJobId.length() > 0){
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
				// 登録
				em.persist(jobStartJobInfoEntity);
				jobStartJobInfoEntity.relateToJobInfoEntity(jobInfoEntity);
			}

			//リポジトリ(RepositoryControllerBean)を取得
			RepositoryControllerBean repository = new RepositoryControllerBean();
			NodeInfo info = repository.getNode(destFacilityId);

			// ホスト種を指定 (H:汎用機　U:UNIX  N:WindowsNT  W:Windows A:AS/400  K:富士通K)
			String hosttype;
			if (info.getPlatformFamily().equals("WINDOWS")) {
				hosttype = "N";
			} else {
				hosttype = "U";
			}

			// 漢字コード種 (S:SHIFT-JIS  E:EUC  8:UTF-8  J:JEF  I:IBM  K:KEIS  N:NEC)
			String encoding = "";
			if (info.getCharacterSet().contains("UTF")) {
				encoding = "8";
			} else if (info.getCharacterSet().contains("EUC")) {
				encoding = "E";
			} else if (info.getCharacterSet().contains("JIS")) {
				encoding = "S";
			} else if (info.getPlatformFamily().equals("WINDOWS")) {
				encoding = "S";
			} else {
				encoding = "8";
			}

			//実行コマンドを設定
			String command;
			if (repository.getNode(fileInfo.getSrc_facility_id()).getPlatformFamily().equals("WINDOWS")) {
				// Windowsの場合の起動コマンド
				command = "cscript.exe //nologo \"" + getScriptDirWin() + UTILIUPDT_H_VBS + "\" " + info.getNodeName() + " " + hosttype + " " + encoding;
			} else {
				// Linuxの場合の起動コマンド
				command = getScriptDirLinux() + UTILIUPDT_H_SH + " " + info.getNodeName() + " " + hosttype + " " + encoding;
			}
			m_log.info("startCommand=" + command);
			jobInfoEntity.setStartCommand(command);
			jobInfoEntity.setSpecifyUser(fileInfo.getSpecify_user());
			jobInfoEntity.setEffectiveUser(fileInfo.getEffective_user());
			jobInfoEntity.setMessageRetry(fileInfo.getMessage_retry());
			jobInfoEntity.setCommandRetry(fileInfo.getCommand_retry());
			jobInfoEntity.setCommandRetryFlg(false);

			//通知メッセージを作成
			JobUtil.copyJobNoticeProperties(jobInfoEntity, fileInfo.job_id);

			return jobInfoEntity.getId().getJobId();
		}
	}

	private String utliupdtH_rcv(
			JobSessionJobEntity nodeJobSessionJob,
			String waitJobId,
			String destFacilityId,
			JobFileInfoData fileInfo
			) throws FacilityNotFound, HinemosUnknown {

		// 集信側に配信側のホスト管理情報を登録する

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// インスタンス生成
			JobInfoEntity jobInfoEntity = subCreateJobInfoEntityForJob (nodeJobSessionJob,
					UTILIUPDT_H_RCV, destFacilityId);

			//待ち条件を設定
			jobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_OR);
			jobInfoEntity.setUnmatchEndFlg(true);
			jobInfoEntity.setUnmatchEndValue(ABNORMAL);
			jobInfoEntity.setUnmatchEndStatus(EndStatusConstant.TYPE_ABNORMAL);

			if(waitJobId != null && waitJobId.length() > 0){
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
				// 登録
				em.persist(jobStartJobInfoEntity);
				jobStartJobInfoEntity.relateToJobInfoEntity(jobInfoEntity);
			}

			//リポジトリ(RepositoryControllerBean)を取得
			RepositoryControllerBean repository = new RepositoryControllerBean();
			NodeInfo info = repository.getNode(fileInfo.getSrc_facility_id());

			// ホスト種を指定 (H:汎用機　U:UNIX  N:WindowsNT  W:Windows A:AS/400  K:富士通K)
			String hosttype;
			if (info.getPlatformFamily().equals("WINDOWS")) {
				hosttype = "N";
			} else {
				hosttype = "U";
			}

			// 漢字コード種 (S:SHIFT-JIS  E:EUC  8:UTF-8  J:JEF  I:IBM  K:KEIS  N:NEC)
			String encoding = "";
			if (info.getCharacterSet().contains("UTF")) {
				encoding = "8";
			} else if (info.getCharacterSet().contains("EUC")) {
				encoding = "E";
			} else if (info.getCharacterSet().contains("JIS")) {
				encoding = "S";
			} else if (info.getPlatformFamily().equals("WINDOWS")) {
				encoding = "S";
			} else {
				encoding = "8";
			}

			//実行コマンドを設定
			String command;
			if (repository.getNode(destFacilityId).getPlatformFamily().equals("WINDOWS")) {
				// Windowsの場合の起動コマンド
				command = "cscript.exe //nologo \"" + getScriptDirWin() + UTILIUPDT_H_VBS + "\" " + info.getNodeName() + " " + hosttype + " " + encoding;
			} else {
				// Linuxの場合の起動コマンド
				command = getScriptDirLinux() + UTILIUPDT_H_SH + " " + info.getNodeName() + " " + hosttype + " " + encoding;
			}
			m_log.info("startCommand=" + command);
			jobInfoEntity.setStartCommand(command);
			jobInfoEntity.setSpecifyUser(fileInfo.getSpecify_user());
			jobInfoEntity.setEffectiveUser(fileInfo.getEffective_user());
			jobInfoEntity.setMessageRetry(fileInfo.getMessage_retry());
			jobInfoEntity.setCommandRetry(fileInfo.getCommand_retry());
			jobInfoEntity.setCommandRetryFlg(false);

			//通知メッセージを作成
			JobUtil.copyJobNoticeProperties(jobInfoEntity, fileInfo.job_id);

			return jobInfoEntity.getId().getJobId();
		}
	}

	private String huloplcmd(
			JobSessionJobEntity nodeJobSessionJob,
			String waitJobId,
			String filePath,
			JobFileInfoData fileInfo
			) throws EntityExistsException, FacilityNotFound, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// インスタンス生成
			JobInfoEntity jobInfoEntity = subCreateJobInfoEntityForJob (nodeJobSessionJob,
					HULOPLCMD, fileInfo.getSrc_facility_id());

			//待ち条件を設定
			jobInfoEntity.setConditionType(ConditionTypeConstant.TYPE_OR);
			jobInfoEntity.setUnmatchEndFlg(true);
			jobInfoEntity.setUnmatchEndValue(ABNORMAL);
			jobInfoEntity.setUnmatchEndStatus(EndStatusConstant.TYPE_ABNORMAL);

			if(waitJobId != null && waitJobId.length() > 0){
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
				// 登録
				em.persist(jobStartJobInfoEntity);
				jobStartJobInfoEntity.relateToJobInfoEntity(jobInfoEntity);
			}

			//リポジトリ(RepositoryControllerBean)を取得
			RepositoryControllerBean repository = new RepositoryControllerBean();

			//実行コマンドを設定
			String command;
			if (repository.getNode(fileInfo.getSrc_facility_id()).getPlatformFamily().equals("WINDOWS")) {
				// Windowsの場合の起動コマンド
				command = "cscript.exe //nologo \"" + getScriptDirWin() + HULOPLCMD_VBS +"\"";
			} else {
				// Linuxの場合の起動コマンド
				command = getScriptDirLinux() + HULOPLCMD_SH;
			}
			m_log.info("startCommand=" + command);
			jobInfoEntity.setStartCommand(command);
			jobInfoEntity.setSpecifyUser(fileInfo.getSpecify_user());
			jobInfoEntity.setEffectiveUser(fileInfo.getEffective_user());
			jobInfoEntity.setArgument(filePath);
			jobInfoEntity.setArgumentJobId(waitJobId);
			jobInfoEntity.setMessageRetry(fileInfo.getMessage_retry());
			jobInfoEntity.setCommandRetry(fileInfo.getCommand_retry());
			jobInfoEntity.setCommandRetryFlg(false);

			//通知メッセージを作成
			JobUtil.copyJobNoticeProperties(jobInfoEntity, fileInfo.job_id);

			return jobInfoEntity.getId().getJobId();
		}
	}

	private JobInfoEntity subCreateJobInfoEntityForJob (JobSessionJobEntity jobSessionJobEntity, String jobIdSuffix, String facilityId)
			throws FacilityNotFound, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

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
			sessionJob.setOwnerRoleId(JobUtil.createSessioniOwnerRoleId(jobunitId));
			// 登録
			em.persist(sessionJob);
			sessionJob.relateToJobSessionEntity(jobSessionEntity);

			//JobInfoEntityを作成
			// インスタンス生成
			JobInfoEntity jobInfoEntity = new JobInfoEntity(sessionJob);
			// 重複チェック
			jtm.checkEntityExists(JobInfoEntity.class, jobInfoEntity.getId());
			// 登録
			em.persist(jobInfoEntity);
			jobInfoEntity.relateToJobSessionJobEntity(sessionJob);

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
			// 多重度
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
			// 登録
			em.persist(jobSessionNodeEntity);
			jobSessionNodeEntity.relateToJobSessionJobEntity(jobInfoEntity.getJobSessionJobEntity());

			return jobInfoEntity;
		}
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
		private Boolean specify_user;
		private String effective_user;
		private Integer message_retry;
		private Integer command_retry;

		public JobFileInfoData(
				String session_id,String jobunit_id,String job_id,String src_facility_id,String dest_facility_id,Integer process_mode,String src_file,String src_work_dir,String dest_directory,String dest_work_dir,Boolean compression_flg,Boolean check_flg,Boolean specify_user, String effective_user, Integer message_retry, Integer command_retry, Boolean command_retry_flg)
		{
			setSession_id(session_id);
			setJobunit_id(jobunit_id);
			setJob_id(job_id);
			setSrc_facility_id(src_facility_id);
			setDest_facility_id(dest_facility_id);
			setProcess_mode(process_mode);
			setDest_directory(dest_directory);
			setCompression_flg(compression_flg);
			setSpecify_user(specify_user);
			setEffective_user(effective_user);
			setMessage_retry(message_retry);
			setCommand_retry(command_retry);
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

		public void setCompression_flg( Boolean compression_flg )
		{
			this.compression_flg = compression_flg;
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

		@Override
		public String toString()
		{
			StringBuffer str = new StringBuffer("{");

			str.append("session_id=" + this.session_id + " jobunit_id=" + this.jobunit_id + " job_id=" + this.job_id +
					" src_facility_id=" + this.src_facility_id + " dest_facility_id=" + this.dest_facility_id + 
					" process_mode=" + this.process_mode + " dest_directory=" + this.dest_directory + 
					" compression_flg=" + this.compression_flg + " effective_user=" + this.effective_user + 
					" message_retry=" + this.message_retry);
			str.append('}');

			return(str.toString());
		}

	}

}
