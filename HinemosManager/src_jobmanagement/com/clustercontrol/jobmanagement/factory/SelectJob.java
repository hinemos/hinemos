/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.jpql.compile.QueryPreparator;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.JobApprovalStatusConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.QueryDivergence;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.filtersetting.bean.JobHistoryFilterBaseInfo;
import com.clustercontrol.filtersetting.bean.JobHistoryFilterConditionCriteria;
import com.clustercontrol.jobmanagement.bean.JobApprovalFilter;
import com.clustercontrol.jobmanagement.bean.JobApprovalInfo;
import com.clustercontrol.jobmanagement.bean.JobCommandInfo;
import com.clustercontrol.jobmanagement.bean.JobCommandParam;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobDetailInfo;
import com.clustercontrol.jobmanagement.bean.JobEndStatusInfo;
import com.clustercontrol.jobmanagement.bean.JobEnvVariableInfo;
import com.clustercontrol.jobmanagement.bean.JobFileCheckInfo;
import com.clustercontrol.jobmanagement.bean.JobFileInfo;
import com.clustercontrol.jobmanagement.bean.JobForwardFile;
import com.clustercontrol.jobmanagement.bean.JobHistory;
import com.clustercontrol.jobmanagement.bean.JobHistoryList;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkExpInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkInheritInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkRcvInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkSendInfo;
import com.clustercontrol.jobmanagement.bean.JobNextJobOrderInfo;
import com.clustercontrol.jobmanagement.bean.JobNodeDetail;
import com.clustercontrol.jobmanagement.bean.JobObjectGroupInfo;
import com.clustercontrol.jobmanagement.bean.JobObjectInfo;
import com.clustercontrol.jobmanagement.bean.JobOutputInfo;
import com.clustercontrol.jobmanagement.bean.JobOutputType;
import com.clustercontrol.jobmanagement.bean.JobParameterInfo;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.MonitorJobInfo;
import com.clustercontrol.jobmanagement.bean.ResourceJobInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobCheckEndValueInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobEndValueConditionInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobOptionInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobRunParamInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobScreenshot;
import com.clustercontrol.jobmanagement.model.JobCommandParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobEnvVariableInfoEntity;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobLinkInheritInfoEntity;
import com.clustercontrol.jobmanagement.model.JobLinkJobExpInfoEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobNextJobOrderInfoEntity;
import com.clustercontrol.jobmanagement.model.JobOutputInfoEntity;
import com.clustercontrol.jobmanagement.model.JobParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobRpaLoginResolutionMstEntity;
import com.clustercontrol.jobmanagement.model.JobRpaScreenshotEntity;
import com.clustercontrol.jobmanagement.model.JobRpaScreenshotEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobWaitGroupInfoEntity;
import com.clustercontrol.jobmanagement.model.JobWaitInfoEntity;
import com.clustercontrol.jobmanagement.util.JobEnumConvertMap;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobInfoResponseP1;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobTreeItemResponseP1;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTypeEnum;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

import jakarta.persistence.TypedQuery;
import net.sf.jpasecurity.jpql.parser.JpqlBrackets;
import net.sf.jpasecurity.jpql.parser.JpqlEquals;
import net.sf.jpasecurity.jpql.parser.JpqlExists;
import net.sf.jpasecurity.jpql.parser.JpqlFrom;
import net.sf.jpasecurity.jpql.parser.JpqlIn;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.JpqlSelect;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.jpql.parser.ToStringVisitor;

/**
 * ジョブ情報を検索するクラスです。
 *
 * @version 3.0.0
 * @since 1.0.0
 */
public class SelectJob {
	/** ジョブ階層表示のセパレータ文字列 */
	public static final String SEPARATOR = ">";
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( SelectJob.class );
	/** 最大表示数 */
	private final static int MAX_DISPLAY_NUMBER = 500;

	/** テスト時に置換可能な外部依存処理 */
	public interface External {
		List<String> getRoleIdList(String userId);
	}

	/** デフォルトの外部依存処理実装 */
	private static final External defaultExternal = new External() {
		@Override
		public List<String> getRoleIdList(String userId) {
			return UserRoleCache.getRoleIdList(userId);
		}
	};

	/** デフォルトの外部依存処理実装を返します。 */
	public static External getDefaultExternal() {
		return defaultExternal;
	}

	private final External external;

	public SelectJob() {
		this(getDefaultExternal());
	}

	public SelectJob(External external) {
		this.external = external;
	}

	/**
	 * ジョブツリー情報を取得します。
	 * <P>
	 * <ol>
	 * <li>ジョブツリー情報のルート(最上位)のインスタンスを作成します。</li>
	 * <li>ジョブリレーションマスタを親ジョブIDが"TOP"で検索し取得します。</li>
	 * <li>取得したジョブリレーションマスタの数、以下の処理を行います。</li>
	 *   <ol>
	 *   <li>ジョブリレーションマスタからジョブマスタを取得します。</li>
	 *   <li>ジョブマスタとジョブツリー情報のルートを渡して、ジョブツリー情報の作成を行います。</li>
	 *   </ol>
	 * </ol>
	 *
	 * @param treeOnly true=ジョブ情報を含まない, false=ジョブ情報含む
	 * @param locale ロケール情報
	 * @param userId ログインユーザのユーザID
	 * @return ジョブツリー情報{@link com.clustercontrol.jobmanagement.bean.JobTreeItem}の階層オブジェクト
	 * @throws NotifyNotFound
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 *
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#createJobTree(JobMasterLocal, JobTreeItem, boolean)
	 */
	public JobTreeItem getJobTree(String ownerRoleId, boolean treeOnly, Locale locale, String userId) throws NotifyNotFound, JobMasterNotFound, UserNotFound {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			//JobTreeItemの最上位インスタンスを作成
			JobInfo info = new JobInfo("", "", JobConstant.STRING_COMPOSITE, JobConstant.TYPE_COMPOSITE);
			JobTreeItem tree = new JobTreeItem(null, info);

			//ジョブツリーのルートを生成
			info = new JobInfo("", "", MessageConstant.JOB.getMessage(), JobConstant.TYPE_COMPOSITE);
			JobTreeItem item = new JobTreeItem(tree, info);

			//親ジョブIDが"TOP"のジョブリレーションを取得
			Collection<JobMstEntity> ct = null;
			if (ownerRoleId != null && !ownerRoleId.isEmpty()) {
				ct = em.createNamedQuery_OR("JobMstEntity.findByParentJobunitIdAndJobId", JobMstEntity.class, ownerRoleId)
						.setParameter("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID)
						.setParameter("parentJobId", CreateJobSession.TOP_JOB_ID)
						.getResultList();
			} else {
				ct = em.createNamedQuery("JobMstEntity.findByParentJobunitIdAndJobId", JobMstEntity.class)
						.setParameter("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID)
						.setParameter("parentJobId", CreateJobSession.TOP_JOB_ID)
						.getResultList();
			}

			for (JobMstEntity childJob : ct) {
				String jobunitId = childJob.getId().getJobunitId();

				//ジョブツリーを作成する
				HashMap<String, ArrayList<JobMstEntity>> map = getJobunitMap(jobunitId);
				createJobTree(childJob, item, treeOnly, map);
			}

			// ソートする
			JobUtil.sort(item);
			return tree;
		}
	}
	
	/**
	 * 参照可能な 全てのジョブの構成を取得する
	 * ジョブツリー表示面で必要な情報のみResponseDTOで直接取得
	 * 
	 * @param ownerRoleId
	 * @param locale
	 * @param userId
	 * @return JobTreeItemResponseP1
	 * @throws NotifyNotFound
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 */
	public JobTreeItemResponseP1 getJobTreeItemResponse(String ownerRoleId, Locale locale, String userId) throws NotifyNotFound, JobMasterNotFound, UserNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			//JobTreeItemResponseP1の最上位インスタンスを作成
			JobInfoResponseP1 treeData = new JobInfoResponseP1();
			treeData.setJobunitId("");
			treeData.setId("");
			treeData.setName(JobConstant.STRING_COMPOSITE);
			treeData.setType(JobTypeEnum.COMPOSITE);
			JobTreeItemResponseP1 tree = new JobTreeItemResponseP1();
			tree.setData(treeData);

			//ジョブツリーのルートを生成
			JobInfoResponseP1 item = new JobInfoResponseP1();
			item.setJobunitId("");
			item.setId("");
			item.setName(MessageConstant.JOB.getMessage());
			item.setType(JobTypeEnum.COMPOSITE);
			JobTreeItemResponseP1 childeTree = new JobTreeItemResponseP1();
			childeTree.setData(item);
			tree.getChildren().add(childeTree);

			//親ジョブIDが"TOP"のジョブリレーションを取得
			Collection<JobMstEntity> ct = null;
			if (ownerRoleId != null && !ownerRoleId.isEmpty()) {
				ct = em.createNamedQuery_OR("JobMstEntity.findByParentJobunitIdAndJobId", JobMstEntity.class, ownerRoleId)
						.setParameter("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID)
						.setParameter("parentJobId", CreateJobSession.TOP_JOB_ID)
						.getResultList();
			} else {
				ct = em.createNamedQuery("JobMstEntity.findByParentJobunitIdAndJobId", JobMstEntity.class)
						.setParameter("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID)
						.setParameter("parentJobId", CreateJobSession.TOP_JOB_ID)
						.getResultList();
			}

			for (JobMstEntity childJob : ct) {
				String jobunitId = childJob.getId().getJobunitId();

				//ジョブツリーを作成する
				HashMap<String, ArrayList<JobMstEntity>> map = getJobunitMap(jobunitId);
				createJobTreeResponse(childJob, childeTree, map);
			}

			// ソートする
			JobUtil.sort(childeTree);
			return tree;
		}
	}

	/**
	 * ジョブツリー情報を取得します。
	 * <P>
	 * <ol>
	 * <li>のインスタンスを作成します。</li>
	 * <li>ジョブリレーションマスタを親ジョブIDが"TOP"となっているレコードについて、指定ジョブユニットを検索します。</li>
	 * <li>取得したジョブリレーションマスタの数、以下の処理を行います。</li>
	 *   <ol>
	 *   <li>ジョブリレーションマスタからジョブマスタを取得します。</li>
	 *   <li>ジョブマスタとジョブツリー情報のルートを渡して、ジョブツリー情報の作成を行います。</li>
	 *   </ol>
	 * </ol>
	 *
	 * @param treeOnly true=ジョブ情報を含まない, false=ジョブ情報含む
	 * @param locale ロケール情報
	 * @param userId ログインユーザのユーザID
	 * @return ジョブツリー情報{@link com.clustercontrol.jobmanagement.bean.JobTreeItem}の階層オブジェクト
	 * @throws NotifyNotFound
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 *
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#createJobTree(JobMasterLocal, JobTreeItem, boolean)
	 */
	public JobTreeItem getJobunitTree(String jobunitId,String ownerRoleId, boolean treeOnly, Locale locale, String userId) throws NotifyNotFound, JobMasterNotFound, UserNotFound {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			//親ジョブIDが"TOP"のジョブリレーションを取得
			Collection<JobMstEntity> ct = null;
			if (ownerRoleId != null && !ownerRoleId.isEmpty()) {
				ct = em.createNamedQuery_OR("JobMstEntity.findByParentJobunitIdAndJobId", JobMstEntity.class, ownerRoleId)
						.setParameter("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID)
						.setParameter("parentJobId", CreateJobSession.TOP_JOB_ID)
						.getResultList();
			} else {
				ct = em.createNamedQuery("JobMstEntity.findByParentJobunitIdAndJobId", JobMstEntity.class)
						.setParameter("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID)
						.setParameter("parentJobId", CreateJobSession.TOP_JOB_ID)
						.getResultList();
			}

			//指定ジョブユニットIDのレコードを取得後、ツリー化して返却
			for (JobMstEntity childJob : ct) {
				if( childJob.getId().getJobunitId().equals(jobunitId)){
					JobTreeItem item = new JobTreeItem(null, new JobInfo("", "", "", JobConstant.TYPE_COMPOSITE));
					HashMap<String, ArrayList<JobMstEntity>> map = getJobunitMap(jobunitId);
					createJobTree(childJob, item, treeOnly, map);
					return item.getChildren(0);
				}
			}

			//該当なしならNULLを返却
			return null;

		}
	}

	
	
	/**
	 *  ジョブユニットごとのジョブ一覧を取得する。(高速化のため)
	 * @param jobunitId
	 * @return Map<parent_id, List<job_id>>
	 */
	private HashMap<String, ArrayList<JobMstEntity>> getJobunitMap(String jobunitId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			HashMap<String, ArrayList<JobMstEntity>> map = new HashMap<String, ArrayList<JobMstEntity>>();
			Collection<JobMstEntity> ct =
					em.createNamedQuery("JobMstEntity.findByJobunitId", JobMstEntity.class)
					.setParameter("jobunitId", jobunitId).getResultList();

			for (JobMstEntity job : ct) {
				String parentId = job.getParentJobId();
				ArrayList<JobMstEntity> list = map.get(parentId);
				if (list == null) {
					list = new ArrayList<JobMstEntity>();
					map.put(parentId, list);
				}
				list.add(job);
			}

			return map;
		}
	}


	/**
	 * ジョブツリーResponseDTO情報を作成します。
	 * 再帰呼び出しを行います。
	 * <P>
	 * <ol>
	 * <li>ジョブマスタを基に、ジョブDTO情報を作成します。</li>
	 * <li>ジョブツリー情報のインスタンスを作成します。</li>
	 * <li>ジョブリレーションマスタをジョブマスタのジョブIDで検索し取得します。</li>
	 * <li>取得したジョブリレーションマスタの数、以下の処理を行います。</li>
	 *   <ol>
	 *   <li>ジョブリレーションマスタからジョブマスタを取得します。</li>
	 *   <li>ジョブマスタとジョブツリー情報のルートを渡して、ジョブツリー情報の作成を行います。</li>
	 *   </ol>
	 * </ol>
	 *
	 * @param job ジョブマスタ
	 * @param parent 親ジョブツリー情報
	 * @param map ジョブのリレーションマップ
	 * @throws JobMasterNotFound
	 * @throws NotifyNotFound
	 * @throws UserNotFound
	 * @throws InvalidRole 
	 */
	private void createJobTreeResponse(JobMstEntity job, JobTreeItemResponseP1 parent, HashMap<String, ArrayList<JobMstEntity>> map)
			throws JobMasterNotFound, NotifyNotFound, UserNotFound {

		//JobTreeItemResponseP1に格納するジョブ情報(JobInfoResponseP1)を作成
		JobInfoResponseP1 info = createJobInfoResponse(job);

		//JobTreeItemResponseP1を作成
		JobTreeItemResponseP1 tree = new JobTreeItemResponseP1();
		tree.setData(info);
		parent.getChildren().add(tree);

		Collection<JobMstEntity> collection = null;
		//ジョブリレーションを親ジョブIDで検索
		collection = map.get(job.getId().getJobId());
		if (collection == null) {
			return;
		}

		for (JobMstEntity childJob : collection) {
			//ジョブツリーを作成する
			createJobTreeResponse(childJob, tree, map);
		}
	}

	/**
	 * ジョブツリー情報を作成します。
	 * 再帰呼び出しを行います。
	 * <P>
	 * <ol>
	 * <li>ジョブマスタを基に、ジョブ情報{@link com.clustercontrol.jobmanagement.bean.JobInfo}を作成します。</li>
	 * <li>ジョブツリー情報のインスタンスを作成します。</li>
	 * <li>ジョブリレーションマスタをジョブマスタのジョブIDで検索し取得します。</li>
	 * <li>取得したジョブリレーションマスタの数、以下の処理を行います。</li>
	 *   <ol>
	 *   <li>ジョブリレーションマスタからジョブマスタを取得します。</li>
	 *   <li>ジョブマスタとジョブツリー情報のルートを渡して、ジョブツリー情報の作成を行います。</li>
	 *   </ol>
	 * </ol>
	 *
	 * @param job ジョブマスタ
	 * @param parent 親ジョブツリー情報
	 * @param treeOnly treeOnly true=ジョブ情報を含まない, false=ジョブ情報含む
	 * @throws JobMasterNotFound
	 * @throws NotifyNotFound
	 * @throws UserNotFound
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#createJobData(JobMasterLocal, boolean)
	 */
	private void createJobTree(JobMstEntity job, JobTreeItem parent, boolean treeOnly, HashMap<String, ArrayList<JobMstEntity>> map)
			throws JobMasterNotFound, NotifyNotFound, UserNotFound {

		//JobTreeItemに格納するジョブ情報(JobInfo)を作成
		JobInfo info = createJobData(job, treeOnly);

		//JobTreeItemを作成
		JobTreeItem item = new JobTreeItem(parent, info);

		Collection<JobMstEntity> collection = null;
		//ジョブリレーションを親ジョブIDで検索
		collection = map.get(job.getId().getJobId());
		if (collection == null) {
			return;
		}

		for (JobMstEntity childJob : collection) {
			//ジョブツリーを作成する
			createJobTree(childJob, item, treeOnly, map);
		}
	}

	/**
	 * ツリー表示に使用しているパラメータのみのジョブ情報ResponseDTOを作成します。<BR>
	 * ジョブマスタを基に、ReponseDTOを作成します。
	 *
	 * @param job ジョブマスタ
	 * @return JobInfoResponseP1 ジョブ情報
	 * @throws InvalidRole 
	 */
	private JobInfoResponseP1 createJobInfoResponse(JobMstEntity job) {
		JobEnumConvertMap enumCache = new JobEnumConvertMap();
		//JobInfoResponseP1を作成
		JobInfoResponseP1 info = new JobInfoResponseP1();
		info.setJobunitId(job.getId().getJobunitId());
		info.setId(job.getId().getJobId());
		info.setName(job.getJobName());
		info.setType(enumCache.getJobTypeEnum(job.getJobType()));
		if (job.isRegisteredModule() != null) {
			info.setRegistered(job.isRegisteredModule());
		}

		JobMstEntity fullMaster = null;;
		try {
			fullMaster = FullJob.getJobMasterFull(info.getJobunitId(), info.getId());
		} catch (JobMasterNotFound | InvalidRole | HinemosUnknown e) {
			e.printStackTrace();
			return info;
		}

		// クライアントとマネージャのタイムゾーンは一致している前提の処理である。
		if (job.getUpdateDate() != null) {
			String strTime;
			try {
				strTime = RestCommonConverter.convertHinemosTimeToDTString(job.getUpdateDate());
				info.setUpdateTime(strTime);
			} catch (InvalidSetting e) {
				m_log.warn("createJobInfoResponse() failed set updateTime.updateTime=" + job.getUpdateDate());
			}
		}
		
		info.setDescription(job.getDescription());
		if (job.getExpNodeRuntimeFlg() != null) {
			info.setExpNodeRuntimeFlg(job.getExpNodeRuntimeFlg());
		}
		info.setIconId(job.getIconId());
		info.setOwnerRoleId(job.getOwnerRoleId());

		if (fullMaster != null) {
			if (info.getType().equals(JobTypeEnum.REFERJOBNET) || info.getType().equals(JobTypeEnum.REFERJOB)) {
				info.setReferJobId(fullMaster.getReferJobId());
				info.setReferJobSelectType(enumCache.getReferJobSelectTypeEnum(fullMaster.getReferJobSelectType()));
				info.setReferJobUnitId(job.getReferJobUnitId());
			}
			try {
				info.setWaitRule(FullJob.createWaitRuleResponse(fullMaster, enumCache));
			} catch (InvalidRole e) {
				m_log.warn("createJobInfoResponse() : " + info.getJobunitId() + ", " + info.getId() +": " +e.getMessage());
			}
		}
		return info;
	}

	/**
	 * ジョブ情報{@link com.clustercontrol.jobmanagement.bean.JobInfo}を作成します。<BR>
	 * ジョブマスタを基に、ジョブ情報を作成します。
	 *
	 * @param job ジョブマスタ
	 * @param treeOnly treeOnly true=ジョブ情報を含まない, false=ジョブ情報含む
	 * @return ジョブ情報
	 */
	private JobInfo createJobData(JobMstEntity job, boolean treeOnly) {

		//JobInfoを作成
		JobInfo info = new JobInfo(job.getId().getJobunitId(), job.getId().getJobId(), job.getJobName(), job.getJobType());
		info.setRegisteredModule(job.isRegisteredModule());
		if(treeOnly){
			return info;
		}
		m_log.debug("createJobData() : " + job.getId().getJobunitId() + ", " + job.getId().getJobId());
		m_log.debug("createJobData() : " + info.getJobunitId() + ", " + info.getId());

		JobInfo rules = null;
		try {
			rules = FullJob.getJobFull(info);
		} catch (JobMasterNotFound | UserNotFound | InvalidRole | HinemosUnknown e) {
			e.printStackTrace();
		}
		info.setPropertyFull(false);
		info.setDescription(job.getDescription());
		info.setIconId(job.getIconId());
		info.setOwnerRoleId(job.getOwnerRoleId());
		info.setUpdateTime(job.getUpdateDate());
		if (job.getExpNodeRuntimeFlg() != null) {
			info.setExpNodeRuntimeFlg(job.getExpNodeRuntimeFlg());
		}
		// 待ち案件と参照ジョブの情報をセット（findbugs 対応にて nullチェック追加）
		if (rules != null) {
			info.setWaitRule(rules.getWaitRule());
			info.setReferJobId(rules.getReferJobId());
			info.setReferJobSelectType(rules.getReferJobSelectType());
			info.setReferJobUnitId(rules.getReferJobUnitId());
		}
		return info;
	}

	/**
	 * 検索条件に一致するジョブ履歴一覧情報を取得します。<BR>
	 * 表示履歴数を越えた場合は、表示履歴数分の履歴情報一覧を返します。
	 * <p>
	 * <ol>
	 * <li>検索条件に一致するセッションを取得します。</li>
	 * <li>取得したセッション数、以下の処理を行います。</li>
	 *  <ol>
	 *  <li>セッションからセッションジョブを取得します。</li>
	 *  <li>セッションジョブからジョブ情報を取得します。</li>
	 *  <li>1セッションの情報をリストにセットします。</li>
	 *  </ol>
	 *  <li>履歴情報一覧，全履歴数を、ビュー一覧情報（{@link JobHistoryList}）にセットし返します。</li>
	 * </ol>
	 *
	 * @param filter 検索条件、nullなら条件なし。
	 * @param histories 表示履歴数
	 * @return ジョブ履歴一覧情報
	 */
	public JobHistoryList getHistoryList(JobHistoryFilterBaseInfo filter, int histories) {
		m_log.debug("getHistoryList() start : histories = " + histories);

		if (filter == null) {
			filter = JobHistoryFilterBaseInfo.ofAllHistories();
		}

		if (histories <= 0) {
			histories = MAX_DISPLAY_NUMBER;
		}

		// 検索条件に該当するセッションを取得
		TypedQuery<JobSessionJobEntity> typedQuery = createFilterQueryForList(filter);
		typedQuery = typedQuery.setMaxResults(histories + 1); // 指定件数より多くのデータがあるかを判断するため+1する

		List<JobSessionJobEntity> sessionJobList = typedQuery.getResultList();

		// 履歴数をカウント
		int total = 0;
		if (sessionJobList.size() > histories) {
			// 最大表示件数より大きい場合、別クエリで総件数をカウント
			TypedQuery<Long> countTypedQuery = createFilterQueryForCount(filter);
			total = countTypedQuery.getSingleResult().intValue();
		} else {
			total = sessionJobList.size();
		}
		m_log.debug("getHistoryList() total = " + total);

		// EntityをDTOへ変換
		ArrayList<JobHistory> historyList = new ArrayList<JobHistory>();
		for (JobSessionJobEntity sessionJob : sessionJobList) {
			JobSessionEntity session = sessionJob.getJobSessionEntity();
			JobInfoEntity jobInfo = sessionJob.getJobInfoEntity();

			JobHistory info = new JobHistory();

			info.setStatus(sessionJob.getStatus());
			info.setEndStatus(sessionJob.getEndStatus());
			info.setEndValue(sessionJob.getEndValue());
			info.setSessionId(sessionJob.getId().getSessionId());
			info.setJobId(sessionJob.getId().getJobId());
			info.setJobunitId(sessionJob.getId().getJobunitId());
			info.setOwnerRoleId(sessionJob.getOwnerRoleId());

			// 実行契機削除などでジョブセッション削除のタイミングで履歴情報取得した場合の対策
			if (session != null) {
				info.setScheduleDate(session.getScheduleDate());
				info.setStartDate(sessionJob.getStartDate());
				info.setEndDate(sessionJob.getEndDate());
				if (session.getTriggerInfo() != null && !session.getTriggerInfo().equals("")) {
					info.setJobTriggerType(session.getTriggerType());
					info.setTriggerInfo(session.getTriggerInfo());
				}
			}

			// 実行契機削除などでジョブセッション削除のタイミングで履歴情報取得した場合の対策
			if (jobInfo != null) {
				info.setJobName(jobInfo.getJobName());
				info.setJobType(jobInfo.getJobType());
				if (sessionJob.hasSessionNode()) {
					info.setScope(sessionJob.getScopeText());
					info.setFacilityId(jobInfo.getFacilityId());
				}
			}
			historyList.add(info);

			//取得した履歴を最大表示件数まで格納したら終了
			if (historyList.size() >= histories) break;
		}

		JobHistoryList list = new JobHistoryList();
		list.setTotal(total);
		list.setList(historyList);

		return list;
	}

	private TypedQuery<JobSessionJobEntity> createFilterQueryForList(JobHistoryFilterBaseInfo filter) {
		return createFilterQuery0(filter, JobSessionJobEntity.class);
	}

	private TypedQuery<Long> createFilterQueryForCount(JobHistoryFilterBaseInfo filter) {
		return createFilterQuery0(filter, Long.class);
	}

	private <T> TypedQuery<T> createFilterQuery0(JobHistoryFilterBaseInfo filter, Class<T> resultClass) {
		boolean forCount = resultClass == Long.class;
	
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
	
			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT");
			if (forCount) {
				sbJpql.append(" COUNT(a)");
			} else {
				sbJpql.append(" a");
			}
			sbJpql.append(" FROM JobSessionJobEntity a JOIN a.jobSessionEntity b");
			sbJpql.append(" WHERE a.id.jobunitId = b.jobunitId");
			sbJpql.append(" AND a.id.jobId = b.jobId");
	
			// フィルタ条件のSQLを追記
			List<JobHistoryFilterConditionCriteria> criteriaList = filter.createConditionsCriteria("a", "b");

			sbJpql.append(" AND (");
			String delim = "";
			for (JobHistoryFilterConditionCriteria crt : criteriaList) {
				sbJpql.append(delim).append("(").append(crt.buildExpressions()).append(")");
				delim = " OR ";
			}
			sbJpql.append(")");
	
			// オブジェクト権限チェックのSQLを追記
			sbJpql = getJpql(sbJpql);
	
			// リスト取得クエリの場合はorder-byを追記
			if (!forCount) {
				sbJpql.append(" ORDER BY a.id.sessionId DESC");
			}
	
			// SQLからTypedQueryを生成
			m_log.debug("createFilterQuery0: jpql = " + sbJpql.toString());
			TypedQuery<T> typedQuery = em.createQuery(sbJpql.toString(), resultClass, JobSessionJobEntity.class);
	
			// フィルタ条件のパラメータをセット
			for (JobHistoryFilterConditionCriteria crt : criteriaList) {
				crt.submitParameters(typedQuery);
			}
	
			// オブジェクト権限チェックのパラメータをセット
			typedQuery = setObjectPrivilegeParameter(typedQuery);
	
			return typedQuery;
		}
	}

	/**
	 * セッションIDが一致するジョブ詳細一覧情報を取得します。
	 * <P>
	 * <ol>
	 * <li>セッションをセッションIDで検索し、取得します。</li>
	 * <li>セッションからセッションジョブを取得します。</li>
	 * <li>テーブルツリー情報のルート(最上位)のインスタンスを作成します。</li>
	 * <li>セッションジョブとテーブルツリー情報のルートを渡して、ジョブ詳細一覧情報の作成を行います。</li>
	 * </ol>
	 *
	 * @param sessionId セッションID
	 * @return ジョブ詳細一覧情報
	 * @throws JobInfoNotFound
	 *
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#createDetailTree(JobSessionJobLocal, CommonTableTreeItem)
	 */
	public JobTreeItem getDetailList(String sessionId) throws JobInfoNotFound, InvalidRole {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			JobTreeItem tree = null;

			//セッションをセッションIDで検索し取得
			JobSessionEntity session = em.find(JobSessionEntity.class, sessionId, ObjectPrivilegeMode.READ);
			if (session == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobSessionEntity.findByPrimaryKey"
						+ ", sessionId = " + sessionId);
				m_log.info("getDetailList() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setSessionId(sessionId);
				throw je;
			}

			//セッションジョブを取得
			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(session.getSessionId(), session.getJobunitId(), session.getJobId());
			m_log.debug("sessionJob = " + sessionJob);
			

			HashMap<String, List<JobSessionJobEntity>> map = new HashMap<>();
			List<JobSessionJobEntity> jobList = QueryUtil.getJobSessionJobBySessionId(session.getSessionId());
			for (JobSessionJobEntity e : jobList) {
				String parentId = e.getParentJobId();
				List<JobSessionJobEntity> list = map.get(parentId);
				if (map.get(parentId) == null) {
					list = new ArrayList<JobSessionJobEntity>();
					map.put(parentId, list);
				}
				list.add(e);
			}

			//CommonTableTreeItemの最上位インスタンスを作成
			tree = new JobTreeItem();
			//ジョブ詳細ツリーを作成
			m_log.debug("createDetailTree start");
			createDetailTree(sessionJob, tree, map);
			m_log.debug("createDetailTree end");

			return tree;
		}
	}

	/**
	 * 単一のジョブ詳細情報を返します。
	 * <p>
	 * 参照オブジェクト権限が必要です。
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @return
	 * @throws InvalidRole 
	 * @throws JobInfoNotFound 
	 */
	public JobTreeItem getDetail(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
			m_log.debug("getDetail: sessionJob = " + sessionJob);

			JobTreeItem tree = new JobTreeItem();
			tree.setData(createJobInfo(sessionJob));
			tree.setDetail(createJobDetail(sessionJob));
			return tree;
		}
	}
	
	/**
	 * ジョブ詳細ツリーを作成します。<BR>
	 * 再帰呼び出しを行います。
	 * <P>
	 * <ol>
	 * <li>セッションジョブを基に、ジョブ詳細情報（{@link ArrayList}）を作成します。</li>
	 * <li>テーブルツリー情報を作成します。</li>
	 * <li>セッションIDとジョブIDからジョブリレーション情報を取得します。</li>
	 * <li>ジョブリレーション情報の数、以下の処理を行います。</li>
	 *  <ol>
	 *  <li>ジョブリレーション情報から、セッションジョブを取得します。</li>
	 *  <li>セッションジョブとテーブルツリー情報を渡して、ジョブ詳細一覧情報の作成を行います。</li>
	 *  </ol>
	 * </ol>
	 *
	 * @param sessionJob セッションジョブ
	 * @param parent 親テーブルツリー情報
	 *
	 * @throws JobInfoNotFound
	 *
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#createJobDetail(JobSessionJobLocal)
	 */
	private void createDetailTree(
			JobSessionJobEntity sessionJob,
			JobTreeItem parent,
			HashMap<String, List<JobSessionJobEntity>> map) throws JobInfoNotFound {

		//セッションジョブからジョブ詳細一覧の１行を作成
		JobInfo info = createJobInfo(sessionJob);
		m_log.debug("info = " + info);
		JobDetailInfo detail = createJobDetail(sessionJob);
		m_log.debug("detail = " + detail);

		//CommonTableTreeItemを作成
		JobTreeItem item = new JobTreeItem(parent, info);
		item.setDetail(detail);

		JobInfoEntity job = sessionJob.getJobInfoEntity();
		/** ファイル転送ジョブ展開表示 */
		boolean m_openForwardFileJob = HinemosPropertyCommon.job_open_forward_file_job.getBooleanValue();
		if(m_openForwardFileJob || job.getJobType() != JobConstant.TYPE_FILEJOB){

			//ジョブリレーションを親ジョブIDで検索
			List<JobSessionJobEntity> collection = map.get(sessionJob.getId().getJobId());
			
			
			if(collection != null && collection.size() > 0){
				Collections.sort(collection, new Comparator<JobSessionJobEntity>() {
					@Override
					public int compare(JobSessionJobEntity o1, JobSessionJobEntity o2) {
						return o1.getId().getJobId().compareTo(o2.getId().getJobId());
					}
				});
				Iterator<JobSessionJobEntity> itr = collection.iterator();
				while(itr.hasNext()){
					//セッションジョブを取得
					JobSessionJobEntity childJob = itr.next();
					//ジョブ詳細ツリーを作成
					createDetailTree(childJob, item, map);
				}
			}
		}
	}

	/**
	 * セッションジョブを基にジョブ詳細情報を作成します。
	 * <p>
	 * <ol>
	 * <li>セッションジョブからジョブ情報を取得します。</li>
	 * <li>1セッションジョブの情報をテーブルのカラム順（{@link com.clustercontrol.jobmanagement.bean.JobDetailTableDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>ジョブ詳細情報を返します。</li>
	 * </ol>
	 *
	 * @param sessionJob セッションジョブ
	 * @return ジョブ詳細情報
	 *
	 * @see com.clustercontrol.bean.JobConstant
	 */
	private JobDetailInfo createJobDetail(JobSessionJobEntity sessionJob) {
		//セッションジョブからジョブ詳細一覧の１行を作成
		JobInfoEntity jobInfo = sessionJob.getJobInfoEntity();
		JobDetailInfo detail = new JobDetailInfo();
		detail.setStatus(sessionJob.getStatus());

		detail.setEndStatus(sessionJob.getEndStatus());
		detail.setEndValue(sessionJob.getEndValue());
		if(jobInfo.getJobType() == JobConstant.TYPE_JOBNET ||
				jobInfo.getJobType() == JobConstant.TYPE_JOB ||
				jobInfo.getJobType() == JobConstant.TYPE_FILEJOB ||
				jobInfo.getJobType() == JobConstant.TYPE_APPROVALJOB ||
				jobInfo.getJobType() == JobConstant.TYPE_MONITORJOB ||
				jobInfo.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB ||
				jobInfo.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB ||
				jobInfo.getJobType() == JobConstant.TYPE_FILECHECKJOB ||
				jobInfo.getJobType() == JobConstant.TYPE_RESOURCEJOB ||
				jobInfo.getJobType() == JobConstant.TYPE_RPAJOB){
			detail.setFacilityId(jobInfo.getFacilityId());
			detail.setScope(sessionJob.getScopeText());
		}
		detail.setWaitRuleTimeList(jobInfo.getWaitRuleTimeList());
		if (sessionJob.getStartDate() != null){
			detail.setStartDate(sessionJob.getStartDate());
		}
		if (sessionJob.getEndDate() != null){
			detail.setEndDate(sessionJob.getEndDate());
		}
		detail.setRunCount(sessionJob.getRunCount());
		detail.setSkip(jobInfo.getSkip());

		return detail;
	}

	/**
	 * セッションジョブ情報作成
	 * ジョブ履歴[一覧]ビューでジョブを選択し、
	 * ジョブ履歴[ジョブ詳細]を表示する際に呼ばれます。
	 * 
	 * @param sessionJob
	 * @return
	 */
	private JobInfo createJobInfo(JobSessionJobEntity sessionJob) {

		m_log.debug("createJobInfo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		//セッションジョブからジョブ詳細一覧の１行を作成
		JobInfoEntity jobInfo = sessionJob.getJobInfoEntity();
		JobInfo info = new JobInfo();
		info.setId(sessionJob.getId().getJobId());
		info.setIconId(jobInfo.getIconId());
		info.setName(jobInfo.getJobName());
		info.setJobunitId(jobInfo.getId().getJobunitId());
		info.setType(jobInfo.getJobType());
		info.setRegisteredModule(jobInfo.isRegisteredModule());
		info.setDescription(jobInfo.getDescription());
		if (sessionJob.getJobSessionEntity() != null) {
			info.setExpNodeRuntimeFlg(sessionJob.getJobSessionEntity().getExpNodeRuntimeFlg());
		}

		//待ち条件を取得 (待ち条件はジョブマップで利用。)
		JobWaitRuleInfo waitRule = null;
		//待ち条件を取得
		waitRule = new JobWaitRuleInfo();
		if(jobInfo.getJobType() == JobConstant.TYPE_JOBNET ||
				jobInfo.getJobType() == JobConstant.TYPE_JOB||
				jobInfo.getJobType() == JobConstant.TYPE_FILEJOB ||
				jobInfo.getJobType() == JobConstant.TYPE_APPROVALJOB ||
				jobInfo.getJobType() == JobConstant.TYPE_MONITORJOB ||
				jobInfo.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB ||
				jobInfo.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB ||
				jobInfo.getJobType() == JobConstant.TYPE_FILECHECKJOB ||
				jobInfo.getJobType() == JobConstant.TYPE_RESOURCEJOB ||
				jobInfo.getJobType() == JobConstant.TYPE_RPAJOB){
			waitRule.setSuspend(jobInfo.getSuspend());
			waitRule.setCondition(jobInfo.getConditionType());
			waitRule.setEndCondition(jobInfo.getUnmatchEndFlg());
			waitRule.setEndStatus(jobInfo.getUnmatchEndStatus());
			waitRule.setEndValue(jobInfo.getUnmatchEndValue());
			waitRule.setSkip(jobInfo.getSkip());
			waitRule.setSkipEndStatus(jobInfo.getSkipEndStatus());
			waitRule.setSkipEndValue(jobInfo.getSkipEndValue());
			waitRule.setExclusiveBranch(jobInfo.getExclusiveBranchFlg());
			waitRule.setExclusiveBranchEndStatus(jobInfo.getExclusiveBranchEndStatus());
			waitRule.setExclusiveBranchEndValue(jobInfo.getExclusiveBranchEndValue());
			waitRule.setCalendar(jobInfo.getCalendar());
			waitRule.setCalendarId(jobInfo.getCalendarId());
			waitRule.setCalendarEndStatus(jobInfo.getCalendarEndStatus());
			waitRule.setCalendarEndValue(jobInfo.getCalendarEndValue());
			waitRule.setJobRetryFlg(jobInfo.getJobRetryFlg());
			waitRule.setJobRetry(jobInfo.getJobRetry());
			waitRule.setJobRetryInterval(jobInfo.getJobRetryInterval());
			waitRule.setJobRetryEndStatus(jobInfo.getJobRetryEndStatus());

			waitRule.setStart_delay(jobInfo.getStartDelay());
			waitRule.setStart_delay_session(jobInfo.getStartDelaySession());
			waitRule.setStart_delay_session_value(jobInfo.getStartDelaySessionValue());
			waitRule.setStart_delay_time(jobInfo.getStartDelayTime());
			if (jobInfo.getStartDelayTimeValue() != null){
				waitRule.setStart_delay_time_value(jobInfo.getStartDelayTimeValue());
			}
			waitRule.setStart_delay_condition_type(jobInfo.getStartDelayConditionType());
			waitRule.setStart_delay_notify(jobInfo.getStartDelayNotify());
			waitRule.setStart_delay_notify_priority(jobInfo.getStartDelayNotifyPriority());
			waitRule.setStart_delay_operation(jobInfo.getStartDelayOperation());
			waitRule.setStart_delay_operation_type(jobInfo.getStartDelayOperationType());
			waitRule.setStart_delay_operation_end_status(jobInfo.getStartDelayOperationEndStatus());
			waitRule.setStart_delay_operation_end_value(jobInfo.getStartDelayOperationEndValue());

			waitRule.setEnd_delay(jobInfo.getEndDelay());
			waitRule.setEnd_delay_session(jobInfo.getEndDelaySession());
			waitRule.setEnd_delay_session_value(jobInfo.getEndDelaySessionValue());
			waitRule.setEnd_delay_job(jobInfo.getEndDelayJob());
			waitRule.setEnd_delay_job_value(jobInfo.getEndDelayJobValue());
			waitRule.setEnd_delay_time(jobInfo.getEndDelayTime());
			if (jobInfo.getEndDelayTimeValue() != null) {
				waitRule.setEnd_delay_time_value(jobInfo.getEndDelayTimeValue());
			}
			waitRule.setEnd_delay_condition_type(jobInfo.getEndDelayConditionType());
			waitRule.setEnd_delay_notify(jobInfo.getEndDelayNotify());
			waitRule.setEnd_delay_notify_priority(jobInfo.getEndDelayNotifyPriority());
			waitRule.setEnd_delay_operation(jobInfo.getEndDelayOperation());
			waitRule.setEnd_delay_operation_type(jobInfo.getEndDelayOperationType());
			waitRule.setEnd_delay_operation_end_status(jobInfo.getEndDelayOperationEndStatus());
			waitRule.setEnd_delay_operation_end_value(jobInfo.getEndDelayOperationEndValue());
			waitRule.setEnd_delay_change_mount(jobInfo.getEndDelayChangeMount());
			waitRule.setEnd_delay_change_mount_value(jobInfo.getEndDelayChangeMountValue());
			waitRule.setMultiplicityNotify(jobInfo.getMultiplicityNotify());
			waitRule.setMultiplicityNotifyPriority(jobInfo.getMultiplicityNotifyPriority());
			waitRule.setMultiplicityOperation(jobInfo.getMultiplicityOperation());
			waitRule.setMultiplicityEndValue(jobInfo.getMultiplicityEndValue());
			waitRule.setQueueFlg(jobInfo.getQueueFlg());
			waitRule.setQueueId(jobInfo.getQueueId());
		}

		//待ち条件を取得
		List<JobWaitGroupInfoEntity> waitGroupList = jobInfo.getJobWaitGroupInfoEntities();
		ArrayList<JobObjectGroupInfo> objectGroupList = new ArrayList<>();
		if(waitGroupList != null && waitGroupList.size() > 0){
			for (JobWaitGroupInfoEntity waitGroup : waitGroupList){
				if(waitGroup == null){
					continue;
				}
				JobObjectGroupInfo objectGroupInfo = new JobObjectGroupInfo();
				objectGroupInfo.setOrderNo(waitGroup.getId().getOrderNo());
				objectGroupInfo.setConditionType(waitGroup.getConditionType());
				objectGroupInfo.setIsGroup(waitGroup.getIsGroup());
				objectGroupInfo.setIsGroup(false);
				ArrayList<JobObjectInfo> objectList = new ArrayList<JobObjectInfo>();
				for (JobWaitInfoEntity wait : waitGroup.getJobWaitInfoEntities()) {
					JobObjectInfo objectInfo = new JobObjectInfo();
					if (wait.getId().getTargetJobType() != JudgmentObjectConstant.TYPE_TIME
							&& wait.getId().getTargetJobType() != JudgmentObjectConstant.TYPE_START_MINUTE
							&& wait.getId().getTargetJobType() != JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
						//対象ジョブを取得
						JobInfoEntity targetJob = null;
						JobSessionJobEntity targetSessionJob = null;
						try {
							targetSessionJob = QueryUtil.getJobSessionJobPK(sessionJob.getId().getSessionId(), wait.getTargetJobunitId(), wait.getTargetJobId());
							targetJob = targetSessionJob.getJobInfoEntity();
						} catch (JobInfoNotFound | InvalidRole | RuntimeException e) {
							continue;
						}
						objectInfo.setJobId(wait.getTargetJobId());
						objectInfo.setJobName(targetJob.getJobName());	
					}
					objectInfo.setType(wait.getId().getTargetJobType());
					switch (objectInfo.getType()) {
					case JudgmentObjectConstant.TYPE_JOB_END_STATUS:
						/** ジョブ終了状態 */
						objectInfo.setStatus(wait.getId().getTargetInt1());
						break;
					case JudgmentObjectConstant.TYPE_JOB_END_VALUE:
						/** ジョブ終了値 */
						objectInfo.setValue(wait.getId().getTargetStr1());
						objectInfo.setDecisionCondition(wait.getId().getTargetInt1());
						break;
					case JudgmentObjectConstant.TYPE_TIME:
						/** 時刻 */
						objectInfo.setTime(wait.getId().getTargetLong());
						break;
					case JudgmentObjectConstant.TYPE_START_MINUTE:
						/** セッション開始時の時間（分）  */
						objectInfo.setStartMinute(wait.getId().getTargetInt1());
						break;
					case JudgmentObjectConstant.TYPE_JOB_PARAMETER:
						/** ジョブ変数 */
						objectInfo.setDecisionCondition(wait.getId().getTargetInt1());
						objectInfo.setDecisionValue(wait.getId().getTargetStr1());
						objectInfo.setValue(wait.getId().getTargetStr2());
						break;
					case JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS:
						/** セッション横断ジョブ終了状態 */
						objectInfo.setStatus(wait.getId().getTargetInt1());
						objectInfo.setCrossSessionRange(wait.getId().getTargetInt2());
						break;
					case JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE:
						/** セッション横断ジョブ終了値 */
						objectInfo.setDecisionCondition(wait.getId().getTargetInt1());
						objectInfo.setCrossSessionRange(wait.getId().getTargetInt2());
						objectInfo.setValue(wait.getId().getTargetStr1());
						break;
					case JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE:
						/** ジョブ戻り値 */
						objectInfo.setDecisionCondition(wait.getId().getTargetInt1());
						objectInfo.setValue(wait.getId().getTargetStr1());
						break;
					default:
						break;
					}
					objectInfo.setDescription(wait.getDescription());
					objectList.add(objectInfo);
				}
				objectGroupInfo.setJobObjectList(objectList);
				objectGroupList.add(objectGroupInfo);
			}
		}
		waitRule.setObjectGroup(objectGroupList);

		// 排他分岐後続ジョブ優先度設定
		List<JobNextJobOrderInfoEntity> nextJobOrderEntityList = jobInfo.getJobNextJobOrderInfoEntities();
		List<JobNextJobOrderInfo> nextJobOrderList = new ArrayList<>();
		if (nextJobOrderEntityList != null) {
			//優先度順にソート
			//nextJobOrderListに優先度順で登録する
			nextJobOrderEntityList.sort(Comparator.comparing(orderEntity -> orderEntity.getOrder()));
			for (JobNextJobOrderInfoEntity nextJobOrderEntity: nextJobOrderEntityList) {
				JobNextJobOrderInfo nextJobOrder = new JobNextJobOrderInfo();
				nextJobOrder.setJobunitId(nextJobOrderEntity.getId().getJobunitId());
				nextJobOrder.setJobId(nextJobOrderEntity.getId().getJobId());
				nextJobOrder.setNextJobId(nextJobOrderEntity.getId().getNextJobId());
				nextJobOrderList.add(nextJobOrder);
			}
		}

		waitRule.setExclusiveBranchNextJobOrderList(nextJobOrderList);
		info.setWaitRule(waitRule);
		
		//承認ジョブ
		if (jobInfo.getJobType() == JobConstant.TYPE_APPROVALJOB) {
			info.setApprovalReqRoleId(jobInfo.getApprovalReqRoleId());
			info.setApprovalReqUserId(jobInfo.getApprovalReqUserId());
			info.setApprovalReqSentence(jobInfo.getApprovalReqSentence());
			info.setApprovalReqMailTitle(jobInfo.getApprovalReqMailTitle());
			info.setApprovalReqMailBody(jobInfo.getApprovalReqMailBody());
			info.setUseApprovalReqSentence(jobInfo.isUseApprovalReqSentence());
		}

		return info;
	}

	/**
	 * ノード詳細一覧情報を取得します。
	 * <p>
	 * <ol>
	 * <li>セッションIDとジョブIDが一致するセッションジョブを取得します。</li>.
	 * <li>セッションジョブからジョブ情報を取得します。</li>
	 * <li>取得したジョブ情報の種別がジョブの場合、以下の処理を行います。</li>
	 *  <ol>
	 *  <li>1セッションジョブの情報をテーブルのカラム順（{@link com.clustercontrol.jobmanagement.bean.NodeDetailTableDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 *   <dl>
	 *   <dt>ノード詳細一覧情報（Objectの2次元配列）</dt>
	 *   <dd>{ ノード詳細情報1 {カラム1の値, カラム2の値, … }, ノード詳細情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *   </dl>
	 *  </ol>
	 * <li>取得したジョブ情報の種別がファイル転送ジョブの場合、ファイル転送ジョブのノード詳細一覧情報を作成します。</li>
	 * </ol>
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param locale ロケール情報
	 * @return ノード詳細一覧情報
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getFileJobNodeDetailList(String, String, Locale)
	 */
	public ArrayList<JobNodeDetail> getNodeDetailList(String sessionId, String jobunitId, String jobId, Locale locale) throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		ArrayList<JobNodeDetail> nodeDetail = new ArrayList<JobNodeDetail>();

		if(sessionId != null && sessionId.length() > 0 &&
				jobId != null && jobId.length() > 0){

			//セッションジョブをセッションIDとジョブIDで検索し、取得
			JobSessionJobEntity sessionjob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
			JobInfoEntity job = sessionjob.getJobInfoEntity();
			if(job.getJobType() == JobConstant.TYPE_JOB
					|| job.getJobType() == JobConstant.TYPE_APPROVALJOB
					|| job.getJobType() == JobConstant.TYPE_MONITORJOB
					|| job.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB
					|| job.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB
					|| job.getJobType() == JobConstant.TYPE_FILECHECKJOB
					|| job.getJobType() == JobConstant.TYPE_RESOURCEJOB
					|| job.getJobType() == JobConstant.TYPE_RPAJOB){
				//ジョブの場合

				//セッションジョブに関連するセッションノードを取得
				Collection<JobSessionNodeEntity> nodeList = sessionjob.getJobSessionNodeEntities();

				if(nodeList != null){
					Iterator<JobSessionNodeEntity> itr = nodeList.iterator();
					while(itr.hasNext()){
						//セッションノードを取得
						JobSessionNodeEntity sessionNode = itr.next();

						//ノード詳細一覧の１行を作成
						JobNodeDetail info = new JobNodeDetail(
								sessionNode.getStatus(),
								sessionNode.getEndValue(),
								sessionNode.getId().getFacilityId(),
								sessionNode.getNodeName(),
								sessionNode.getStartDate()==null?null:sessionNode.getStartDate(),
								sessionNode.getEndDate()==null?null:sessionNode.getEndDate(),
								sessionNode.getMessage());
						nodeDetail.add(info);
					}
				}
			} else if(job.getJobType() == JobConstant.TYPE_FILEJOB){
				//ファイル転送ジョブの場合

				//ファイル転送ジョブのノード詳細一覧取得
				nodeDetail = getFileJobNodeDetailList(sessionId, jobunitId, jobId, locale);
			}

		}

		return nodeDetail;
	}

	/**
	 * ファイル転送ジョブのノード詳細一覧情報を取得します。
	 * <p>
	 * <ol>
	 * <li>セッションIDとジョブIDが一致するセッションジョブを取得します。</li>.
	 * <li>セッションジョブからジョブ情報を取得します。</li>
	 * <li>取得したジョブ情報の種別がファイル転送ジョブの場合、以下の処理を行います。</li>
	 *  <ol>
	 *  <li>1セッションジョブの情報をテーブルのカラム順（{@link com.clustercontrol.jobmanagement.bean.NodeDetailTableDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 *   <dl>
	 *   <dt>ノード詳細一覧情報（Objectの2次元配列）</dt>
	 *   <dd>{ ノード詳細情報1 {カラム1の値, カラム2の値, … }, ノード詳細情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *   </dl>
	 *  </ol>
	 * </ol>
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param locale ロケール情報
	 * @return ファイル転送ジョブのノード詳細一覧情報
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getFileJobDetailMessage(String, String, String, String, int, Locale)
	 */
	private ArrayList<JobNodeDetail> getFileJobNodeDetailList(
			String sessionId,
			String jobunitId,
			String jobId,
			Locale locale) throws JobInfoNotFound, InvalidRole, HinemosUnknown {

		ArrayList<JobNodeDetail> nodeDetail = new ArrayList<JobNodeDetail>();

		if(sessionId == null || sessionId.length() == 0 || jobId == null || jobId.length() == 0){
			return nodeDetail;
		}

		//セッションジョブをセッションIDとジョブIDで検索し、取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		if(job.getJobType() != JobConstant.TYPE_FILEJOB){
			return nodeDetail;
		}

		//ファイル転送ジョブの場合
		//ジョブリレーションを親ジョブIDで検索
		Collection<JobSessionJobEntity> collection
		= QueryUtil.getChildJobSessionJob(
					sessionJob.getId().getSessionId(),
					sessionJob.getId().getJobunitId(),
					sessionJob.getId().getJobId());
		if (collection == null) {
			JobInfoNotFound je = new JobInfoNotFound("JobSessionJobEntity.findByParentJobId"
					+ ", [sessionId, parentJobId] = "
					+ "[" + sessionJob.getId().getSessionId()
					+ ", " + sessionJob.getId().getJobId() + "]");
			m_log.info("getFileJobNodeDetailList() : "
					+ je.getClass().getSimpleName() + ", " + je.getMessage());
			je.setSessionId(sessionJob.getId().getSessionId());
			je.setParentJobId(sessionJob.getId().getJobId());
			throw je;
		}

		for(JobSessionJobEntity childSessionJob : collection) {

			JobInfoEntity childJob = childSessionJob.getJobInfoEntity();
			//ジョブIDからファシリティIDを抽出
			String facilityId = childSessionJob.getId().getJobId();
			facilityId = facilityId.replaceFirst(sessionJob.getId().getJobId() + "_", "");
			if(childJob.getJobType() == JobConstant.TYPE_JOBNET){
				//ファシリティパスを取得
				String destFacilityName = new RepositoryControllerBean().getFacilityPath(facilityId, null);

				//ノード詳細一覧の１行を作成
				JobNodeDetail info = new JobNodeDetail(
						childSessionJob.getStatus(),
						childSessionJob.getEndValue(),
						facilityId,
						childSessionJob.getScopeText(),
						childSessionJob.getStartDate()==null?null:childSessionJob.getStartDate(),
						childSessionJob.getEndDate()==null?null:childSessionJob.getEndDate(),
						getFileJobDetailMessage(
							childJob.getId().getSessionId(),
							childJob.getId().getJobunitId(),
							childJob.getId().getJobId(),
							facilityId,
							destFacilityName,
							job.getCheckFlg(),
							locale));
				nodeDetail.add(info);
			} else if (childJob.getJobType() == JobConstant.TYPE_JOB
					|| childJob.getJobType() == JobConstant.TYPE_MONITORJOB
					|| childJob.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB
					|| childJob.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB
					|| childJob.getJobType() == JobConstant.TYPE_FILECHECKJOB
					|| childJob.getJobType() == JobConstant.TYPE_RESOURCEJOB
					|| childJob.getJobType() == JobConstant.TYPE_RPAJOB){
				for (JobSessionNodeEntity sessionNode : childSessionJob.getJobSessionNodeEntities()) {
					if (sessionNode.getEndValue() != null && sessionNode.getEndValue() != 0) {
						// ファイルリストに失敗したときだけノード詳細ビューに表示
						JobNodeDetail info = new JobNodeDetail(
								sessionNode.getStatus(),
								sessionNode.getEndValue(),
								sessionNode.getId().getFacilityId(),
								sessionNode.getNodeName(),
								sessionNode.getStartDate()==null?null:sessionNode.getStartDate(),
								sessionNode.getEndDate()==null?null:sessionNode.getEndDate(),
								sessionNode.getMessage());
						nodeDetail.add(info);
					}
				}
			}
		}

		return nodeDetail;
	}

	/**
	 * ファイル転送ジョブのノード詳細一覧情報のメッセージを取得します。
	 *
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @param jobunitId ジョブユニットID
	 * @param destFacilityId 受信ファシリティID
	 * @param destFacilityName 受信ファシリティ名
	 * @param checksum ファイルチェック
	 * @param locale ロケール情報
	 * @return メッセージ
	 * @throws JobInfoNotFound
	 */
	private String getFileJobDetailMessage(
			String sessionId,
			String jobunitId,
			String jobId,
			String destFacilityId,
			String destFacilityName,
			boolean checksum,
			Locale locale) throws JobInfoNotFound {

		final String START = "_START";
		final String END = "_END";
		final String FILE = "_FILE";
		final String RTN = "\n";

		StringBuilder message = new StringBuilder();

		if(sessionId == null || sessionId.length() == 0 || 	jobId == null || jobId.length() == 0){
			return message.toString();
		}

		// UTILIUPDT_Sのメッセージを取得
		if (CreateHulftJob.isHulftMode()) {
			try {
				JobSessionJobEntity job = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
				JobSessionJobEntity jobUtliupdtS = QueryUtil.getJobSessionJobPK(sessionId, jobunitId,
						job.getParentJobId() + CreateHulftJob.UTILIUPDT_S);
				if (jobUtliupdtS.getEndDate() != null) {
					if(jobUtliupdtS.getEndStatus() == null ||
							jobUtliupdtS.getEndStatus() != EndStatusConstant.TYPE_NORMAL){
						for (JobSessionNodeEntity node : jobUtliupdtS.getJobSessionNodeEntities()) {
							String nodeMessage = node.getMessage();
							if (nodeMessage != null && nodeMessage.length() > 0) {
								message.append(nodeMessage);
								message.append(RTN);
							}
						}
					}
				}
			} catch (JobInfoNotFound e) {
				m_log.debug("getFileJobDetailMessage() : " + e.getMessage());
			} catch (InvalidRole e) {
				m_log.info("getFileJobDetailMessage() : " + e.getMessage());
			}
		}

		//ジョブリレーションを親ジョブIDで検索
		Collection<JobSessionJobEntity> collection =
				QueryUtil.getChildJobSessionJobOrderByStartDate(sessionId, jobunitId, jobId);
		if (collection == null) {
			JobInfoNotFound je = new JobInfoNotFound("JobSessionJobEntity.findByStartDate"
					+ ", [sessionId, parentJobId] = "
					+ "[" + sessionId + ", " + jobId + "]");
			m_log.info("getFileJobDetailMessage() : "
					+ je.getClass().getSimpleName() + ", " + je.getMessage());
			je.setSessionId(sessionId);
			je.setParentJobId(jobId);
			throw je;
		}

		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, locale);
		dateFormat.setTimeZone(HinemosTime.getTimeZone());
		HashMap<String, String> jobIdMap = new HashMap<String, String>();

		for (JobSessionJobEntity childSessionJob : collection) {
			JobInfoEntity childJob = childSessionJob.getJobInfoEntity();
			String childJobId = childSessionJob.getId().getJobId();

			if(childJobId.indexOf(CreateFileJob.GET_KEY) != -1){
				//"_GET_CHECKSUM"を除いたジョブIDを取得
				String fileJobId = childJobId.replaceAll(CreateFileJob.GET_KEY, "");
				//ファイル転送が終了の場合
				if(childSessionJob.getStartDate() != null && childSessionJob.getEndDate() != null &&
						(childSessionJob.getEndStatus() == null || childSessionJob.getEndStatus() != EndStatusConstant.TYPE_NORMAL)){
					for (JobSessionNodeEntity node : childSessionJob.getJobSessionNodeEntities()) {
						String nodeMessage = node.getMessage();
						if (nodeMessage != null && nodeMessage.length() > 0) {
							message.append(nodeMessage);
							message.append(RTN);
						}
					}
					jobIdMap.put(fileJobId, END);
				}

			} else if(childJobId.indexOf(CreateFileJob.ADD_KEY) != -1){
				//"_GET_CHECKSUM"を除いたジョブIDを取得
				String fileJobId = childJobId.replaceAll(CreateFileJob.ADD_KEY, "");
				//ファイル転送が終了の場合
				if(childSessionJob.getStartDate() != null && childSessionJob.getEndDate() != null &&
						(jobIdMap.get(fileJobId) == null || !jobIdMap.get(fileJobId).equals(END))){
					if(childSessionJob.getEndStatus() == null ||
							childSessionJob.getEndStatus() != EndStatusConstant.TYPE_NORMAL){
						for (JobSessionNodeEntity node : childSessionJob.getJobSessionNodeEntities()) {
							String nodeMessage = node.getMessage();
							if (nodeMessage != null && nodeMessage.length() > 0) {
								message.append(nodeMessage);
								message.append(RTN);
							}
						}
						jobIdMap.put(fileJobId, END);
					}
				}

			} else if(childJobId.indexOf(CreateFileJob.GET_CHECKSUM) != -1){
				//"_GET_CHECKSUM"を除いたジョブIDを取得
				String fileJobId = childJobId.replaceAll(CreateFileJob.GET_CHECKSUM, "");
				jobIdMap.put(fileJobId + FILE, childJob.getArgument());
				//ファイル転送が開始の場合
				if(childSessionJob.getStartDate() != null){
					String dateString = dateFormat.format(childSessionJob.getStartDate());
					String file = childJob.getArgument();
					String[] args = { dateString, file, destFacilityName };
					message.append(MessageConstant.MESSAGE_STARTED_TO_TRANSFER_FILE.getMessage(args));
					message.append(RTN);
					jobIdMap.put(fileJobId, START);
				}
				//ファイル転送が終了の場合
				if(childSessionJob.getStartDate() != null && childSessionJob.getEndDate() != null &&
					(jobIdMap.get(fileJobId) == null ||	!jobIdMap.get(fileJobId).equals(END))){
					if(childSessionJob.getEndStatus() == null ||
							childSessionJob.getEndStatus() != EndStatusConstant.TYPE_NORMAL){
						String dateString = dateFormat.format(childSessionJob.getEndDate());
						String file = childJob.getArgument();
						String[] args = { dateString, file, destFacilityName };
						message.append(MessageConstant.MESSAGE_FAILED_TO_TRANSFER_FILE.getMessage(args));
						message.append(RTN);
						for (JobSessionNodeEntity node : childSessionJob.getJobSessionNodeEntities()) {
							String nodeMessage = node.getMessage();
							if (nodeMessage != null && nodeMessage.length() > 0) {
								message.append(nodeMessage);
								message.append(RTN);
							}
						}
						jobIdMap.put(fileJobId, END);
					}
				}

			} else if(childJobId.indexOf(CreateFileJob.FORWARD) != -1){
				//"_FORWARD"を除いたジョブIDを取得
				String fileJobId = childJobId.replaceAll(CreateFileJob.FORWARD, "");
				//ファイル転送が開始の場合
				if(childSessionJob.getStartDate() != null && !checksum){
					String dateString = dateFormat.format(childSessionJob.getStartDate());
					String file = childJob.getArgument();
					String[] args = { dateString, file, destFacilityName };
					message.append(MessageConstant.MESSAGE_STARTED_TO_TRANSFER_FILE.getMessage(args));
					message.append(RTN);
					jobIdMap.put(fileJobId, START);
				}
				//ファイル転送が終了の場合
				if(childSessionJob.getStartDate() != null && childSessionJob.getEndDate() != null &&
						(jobIdMap.get(fileJobId) == null || !jobIdMap.get(fileJobId).equals(END))){
					String dateString = dateFormat.format(childSessionJob.getEndDate());
					String file = childJob.getArgument();
					String[] args = { dateString, file, destFacilityName };
					if(childSessionJob.getEndStatus() != null &&
							childSessionJob.getEndStatus() == EndStatusConstant.TYPE_NORMAL){
						if(!checksum){
							message.append(MessageConstant.MESSAGE_FINISHED_TRANSFERRING_FILE.getMessage(args));
							message.append(RTN);
							jobIdMap.put(fileJobId, END);
						}
					} else {
						message.append(MessageConstant.MESSAGE_FAILED_TO_TRANSFER_FILE.getMessage(args));
						message.append(RTN);
						for (JobSessionNodeEntity node : childSessionJob.getJobSessionNodeEntities()) {
							m_log.debug(node.getId().getJobId());
							String nodeMessage = node.getMessage();
							if (nodeMessage != null && nodeMessage.length() > 0) {
								message.append(nodeMessage);
								message.append(RTN);
							}
						}
						jobIdMap.put(fileJobId, END);
					}
				}
				if(!checksum){
					jobIdMap.remove(fileJobId);
					jobIdMap.remove(fileJobId + FILE);
				}

			} else if(childJobId.indexOf(CreateFileJob.CHECK_CHECKSUM) != -1){
				//"_CHECK_CHECKSUM"を除いたジョブIDを取得
				String fileJobId = childJobId.replaceAll(CreateFileJob.CHECK_CHECKSUM, "");
				//ファイル転送が終了の場合
				if(childSessionJob.getStartDate() != null && childSessionJob.getEndDate() != null &&
						(jobIdMap.get(fileJobId) == null || !jobIdMap.get(fileJobId).equals(END))){
					String dateString = dateFormat.format(childSessionJob.getEndDate());
					String file = jobIdMap.get(fileJobId + FILE);
					String[] args = { dateString, file, destFacilityName };
					if(childSessionJob.getEndStatus() != null &&
							childSessionJob.getEndStatus() == EndStatusConstant.TYPE_NORMAL){
						message.append(MessageConstant.MESSAGE_FINISHED_TRANSFERRING_FILE.getMessage(args));
						message.append(RTN);
					} else {
						message.append(MessageConstant.MESSAGE_FAILED_TO_TRANSFER_FILE.getMessage(args));
						message.append(RTN);
						for (JobSessionNodeEntity node : childSessionJob.getJobSessionNodeEntities()) {
							m_log.debug(node.getId().getJobId());
							String nodeMessage = node.getMessage();
							if (nodeMessage != null && nodeMessage.length() > 0) {
								message.append(nodeMessage);
								message.append(RTN);
							}
						}
						jobIdMap.put(fileJobId, END);
					}
				}
				jobIdMap.remove(fileJobId);
				jobIdMap.remove(fileJobId + FILE);

			} else if(childJobId.indexOf(CreateFileJob.DEL_KEY) != -1){
				//"_DEL_KEY"を除いたジョブIDを取得
				String fileJobId = childJobId.replaceAll(CreateFileJob.DEL_KEY, "");
				//ファイル転送が終了の場合
				if(childSessionJob.getStartDate() != null && childSessionJob.getEndDate() != null &&
						(jobIdMap.get(fileJobId) == null || !jobIdMap.get(fileJobId).equals(END))){
					if(childSessionJob.getEndStatus() == null ||
							childSessionJob.getEndStatus() != EndStatusConstant.TYPE_NORMAL){
						for (JobSessionNodeEntity node : childSessionJob.getJobSessionNodeEntities()) {
							String nodeMessage = node.getMessage();
							if (nodeMessage != null && nodeMessage.length() > 0) {
								message.append(nodeMessage);
								message.append(RTN);
							}
						}
						jobIdMap.put(fileJobId, END);
					}
				}

			} else if(childJobId.indexOf(CreateHulftJob.UTILIUPDT_R) != -1) {
				//"_UTILIUPDT_R"を除いたジョブIDを取得
				String fileJobId = childJobId.replaceAll(CreateHulftJob.UTILIUPDT_R, "");
				//ファイル転送が終了の場合
				if(childSessionJob.getStartDate() != null && childSessionJob.getEndDate() != null &&
						(jobIdMap.get(fileJobId) == null || !jobIdMap.get(fileJobId).equals(END))){
					if(childSessionJob.getEndStatus() == null ||
							childSessionJob.getEndStatus() != EndStatusConstant.TYPE_NORMAL){
						for (JobSessionNodeEntity node : childSessionJob.getJobSessionNodeEntities()) {
							String nodeMessage = node.getMessage();
							if (nodeMessage != null && nodeMessage.length() > 0) {
								message.append(nodeMessage);
								message.append(RTN);
							}
						}
						jobIdMap.put(fileJobId, END);
					}
				}

			} else if(childJobId.indexOf(CreateHulftJob.UTILIUPDT_H_SND) != -1) {
				//"_UTLSEND"を除いたジョブIDを取得
				String fileJobId = childJobId.replaceAll(CreateHulftJob.UTILIUPDT_H_SND, "");
				//ファイル転送が終了の場合
				if(childSessionJob.getStartDate() != null && childSessionJob.getEndDate() != null &&
						(jobIdMap.get(fileJobId) == null || !jobIdMap.get(fileJobId).equals(END))){
					if(childSessionJob.getEndStatus() == null ||
							childSessionJob.getEndStatus() != EndStatusConstant.TYPE_NORMAL){
						for (JobSessionNodeEntity node : childSessionJob.getJobSessionNodeEntities()) {
							String nodeMessage = node.getMessage();
							if (nodeMessage != null && nodeMessage.length() > 0) {
								message.append(nodeMessage);
								message.append(RTN);
							}
						}
						jobIdMap.put(fileJobId, END);
					}
				}

			} else if(childJobId.indexOf(CreateHulftJob.UTILIUPDT_H_RCV) != -1) {
				//"_UTLSEND"を除いたジョブIDを取得
				String fileJobId = childJobId.replaceAll(CreateHulftJob.UTILIUPDT_H_RCV, "");
				//ファイル転送が終了の場合
				if(childSessionJob.getStartDate() != null && childSessionJob.getEndDate() != null &&
						(jobIdMap.get(fileJobId) == null || !jobIdMap.get(fileJobId).equals(END))){
					if(childSessionJob.getEndStatus() == null ||
							childSessionJob.getEndStatus() != EndStatusConstant.TYPE_NORMAL){
						for (JobSessionNodeEntity node : childSessionJob.getJobSessionNodeEntities()) {
							String nodeMessage = node.getMessage();
							if (nodeMessage != null && nodeMessage.length() > 0) {
								message.append(nodeMessage);
								message.append(RTN);
							}
						}
						jobIdMap.put(fileJobId, END);
					}
				}

			} else if(childJobId.indexOf(CreateHulftJob.UTLSEND) != -1) {
				//"_UTLSEND"を除いたジョブIDを取得
				String fileJobId = childJobId.replaceAll(CreateHulftJob.UTLSEND, "");
				//ファイル転送が終了の場合
				if(childSessionJob.getStartDate() != null && childSessionJob.getEndDate() != null &&
						(jobIdMap.get(fileJobId) == null || !jobIdMap.get(fileJobId).equals(END))){
					if(childSessionJob.getEndStatus() == null ||
							childSessionJob.getEndStatus() != EndStatusConstant.TYPE_NORMAL){
						for (JobSessionNodeEntity node : childSessionJob.getJobSessionNodeEntities()) {
							String nodeMessage = node.getMessage();
							if (nodeMessage != null && nodeMessage.length() > 0) {
								message.append(nodeMessage);
								message.append(RTN);
							}
						}
						jobIdMap.put(fileJobId, END);
					}
				}

			} else if(childJobId.indexOf(CreateHulftJob.HULOPLCMD) != -1) {
				//"_HULOPLCMD"を除いたジョブIDを取得
				String fileJobId = childJobId.replaceAll(CreateHulftJob.HULOPLCMD, "");
				//ファイル転送が終了の場合
				if(childSessionJob.getStartDate() != null && childSessionJob.getEndDate() != null &&
						(jobIdMap.get(fileJobId) == null || !jobIdMap.get(fileJobId).equals(END))){
					// HULOPLCMDについては、終了していれば、状態によらずメッセージを追加する。
					for (JobSessionNodeEntity node : childSessionJob.getJobSessionNodeEntities()) {
						String nodeMessage = node.getMessage();
						if (nodeMessage != null && nodeMessage.length() > 0) {
							message.append(nodeMessage);
							message.append(RTN);
						}
					}
					jobIdMap.put(fileJobId, END);
				}
			}

			m_log.debug("getFileJobDetailMessage() : jobid=" + childJobId + ", message=" + message);
		}

		if(message.length() > 0) {
			message.setLength(message.length() - 1);
		}

		return message.toString();
	}

	/**
	 * ファイル転送一覧情報を取得します。
	 * <p>
	 * <ol>
	 * <li>セッションIDとジョブIDが一致するセッションジョブを取得します。</li>.
	 * <li>セッションジョブからジョブ情報を取得します。</li>
	 * <li>取得したジョブ情報の種別がファイル転送ジョブの場合、以下の処理を行います。</li>
	 *  <ol>
	 *  <li>1セッションジョブの情報をテーブルのカラム順（{@link com.clustercontrol.jobmanagement.bean.ForwardFileTableDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 *   <dl>
	 *   <dt>ファイル転送一覧情報（Objectの2次元配列）</dt>
	 *   <dd>{ ファイル転送情報1 {カラム1の値, カラム2の値, … }, ファイル転送情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *   </dl>
	 *  </ol>
	 * </ol>
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @return ファイル転送一覧情報
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getForwardFileListInfo(String, String, String, String, String, String, int)
	 */
	public ArrayList<JobForwardFile> getForwardFileList(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		ArrayList<JobForwardFile> ret = new ArrayList<JobForwardFile>();

		if(sessionId == null || sessionId.length() == 0 || jobId == null || jobId.length() == 0){
			return ret;
		}

		//セッションジョブをセッションIDとジョブIDで検索し、取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		if(job.getJobType() != JobConstant.TYPE_FILEJOB){
			return ret;
		}

		//ファシリティパスを取得
		String srcFacilityId = job.getSrcFacilityId();
		String srcFacilityName = new RepositoryControllerBean().getFacilityPath(srcFacilityId, null);
		m_log.info("getForwardFileList() : " + srcFacilityId + " jobId=" + jobId);

		//ジョブリレーションを親ジョブIDで検索
		Collection<JobSessionJobEntity> collection = QueryUtil.getChildJobSessionJob(sessionId, jobunitId, jobId);
		if (collection == null) {
			JobInfoNotFound je = new JobInfoNotFound("JobSessionJobEntity.findByParentJobId"
					+ ", [sessionId,parentJobId]=" + "[" + sessionId + "," + jobId + "]");
			m_log.info("getForwardFileList() : " + je.getClass().getSimpleName() + ", " + je.getMessage());
			je.setSessionId(sessionId);
			je.setParentJobId(jobId);
			throw je;
		}

		for (JobSessionJobEntity childSessionJob : collection) {
			JobInfoEntity childJob = childSessionJob.getJobInfoEntity();
			if(childJob.getJobType() == JobConstant.TYPE_JOBNET){
				//ジョブIDからファシリティIDを抽出
				String dstFacilityId = childSessionJob.getId().getJobId().replaceFirst(jobId + "_", "");
				//ファシリティパスを取得
				String destFacilityName = new RepositoryControllerBean().getFacilityPath(dstFacilityId, null);
				//ファイル転送一覧の１行を作成
				ArrayList<JobForwardFile> list =
						getForwardFileListInfo(
								childJob.getId().getSessionId(),
								childJob.getId().getJobunitId(),
								childJob.getId().getJobId(),
								srcFacilityId,
								srcFacilityName,
								dstFacilityId,
								destFacilityName,
								job.getCheckFlg());
				ret.addAll(list);
			}
		}

		return ret;
	}

	/**
	 * ファイル転送情報を取得します。
	 * <p>
	 * <ol>
	 * <li>セッションIDと親ジョブIDでジョブリレーション情報を取得します。</li>
	 * <li>取得したジョブリレーション情報の数、以下の処理を行います。</li>
	 *  <ol>
	 *  <li>ジョブリレーション情報からセッションジョブを取得します。</li>
	 *  <li>1セッションジョブの情報をテーブルのカラム順（{@link com.clustercontrol.jobmanagement.bean.ForwardFileTableDefine}）で、</li>
	 *  <li>リストにセットします。</li>
	 *   <dl>
	 *   <dt>ファイル転送一覧情報（Objectの配列）</dt>
	 *   <dd>ファイル転送情報 {カラム1の値, カラム2の値, … }</dd>
	 *   </dl>
	 *  </ol>
	 * </ol>
	 *
	 * @param sessionId セッションID
	 * @param jobId 親ジョブユニットID
	 * @param jobId 親ジョブID
	 * @param srcFacilityId 転送ファシリティID
	 * @param srcFacilityName 転送ファシリティ名
	 * @param destFacilityId 受信ファシリティID
	 * @param destFacilityName 受信ファシリティ名
	 * @param checksum ファイルチェック
	 * @return ファイル転送情報
	 * @throws JobInfoNotFound
	 */
	private ArrayList<JobForwardFile> getForwardFileListInfo(
			String sessionId,
			String jobunitId,
			String jobId,
			String srcFacilityId,
			String srcFacilityName,
			String destFacilityId,
			String destFacilityName,
			boolean checksum) throws JobInfoNotFound {

		final String STATUS = "_STATUS";
		final String END_STATUS = "_END_STATUS";
		final String START_DATE = "_START_DATE";
		final String END_DATE = "_END_DATE";
		final String FILE = "_FILE";

		ArrayList<JobForwardFile> list = new ArrayList<JobForwardFile>();

		if(sessionId != null && sessionId.length() > 0 &&
				jobId != null && jobId.length() > 0){

			//ジョブリレーションを親ジョブIDで検索
			Collection<JobSessionJobEntity> collection
			= QueryUtil.getChildJobSessionJobOrderByStartDate(sessionId, jobunitId, jobId);
			if (collection == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobSessionJobEntity.findByStartDate"
						+ ", [sessionId, parentJobId] = "
						+ "[" + sessionId + ", " + jobId + "]");
				m_log.info("getForwardFileListInfo() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setSessionId(sessionId);
				je.setParentJobId(jobId);
				throw je;
			}

			HashMap<String, Object> jobIdMap = new HashMap<String, Object>();

			for (JobSessionJobEntity childSessionJob : collection) {
				//セッションジョブを取得

				JobInfoEntity job = childSessionJob.getJobInfoEntity();

				//ファイル転送が開始の場合
				if(childSessionJob.getId().getJobId().indexOf(CreateFileJob.GET_CHECKSUM) != -1){
					//"_GET_CHECKSUM"を除いたジョブIDを取得
					String fileJobId = childSessionJob.getId().getJobId().replaceAll(CreateFileJob.GET_CHECKSUM, "");

					jobIdMap.put(fileJobId + STATUS, childSessionJob.getStatus());
					jobIdMap.put(fileJobId + END_STATUS, childSessionJob.getEndStatus());
					jobIdMap.put(fileJobId + END_DATE, childSessionJob.getEndDate());
					jobIdMap.put(fileJobId + FILE, job.getArgument());
					jobIdMap.put(fileJobId + START_DATE, childSessionJob.getStartDate());
				}else if(childSessionJob.getId().getJobId().indexOf(CreateFileJob.FORWARD) != -1){
					//"_FORWARD"を除いたジョブIDを取得
					String fileJobId = childSessionJob.getId().getJobId().replaceAll(CreateFileJob.FORWARD, "");

					if(!checksum){
						jobIdMap.put(fileJobId + STATUS, childSessionJob.getStatus());
						jobIdMap.put(fileJobId + END_STATUS, childSessionJob.getEndStatus());
						jobIdMap.put(fileJobId + END_DATE, childSessionJob.getEndDate());
						jobIdMap.put(fileJobId + FILE, job.getArgument());
						jobIdMap.put(fileJobId + START_DATE, childSessionJob.getStartDate());
					}else if(childSessionJob.getEndStatus() != null &&
								childSessionJob.getEndStatus() != StatusConstant.TYPE_WAIT){
						jobIdMap.put(fileJobId + STATUS, childSessionJob.getStatus());
						jobIdMap.put(fileJobId + END_STATUS, childSessionJob.getEndStatus());
						jobIdMap.put(fileJobId + END_DATE, childSessionJob.getEndDate());
					}

					if(!checksum){
						Object obj;
						obj = jobIdMap.get(fileJobId + START_DATE);
						Long startDate = null;
						if (obj != null) {
							startDate = (Long)obj;
						}
						obj = jobIdMap.get(fileJobId + END_DATE);
						Long endDate = null;
						if (obj != null) {
							endDate = (Long)obj;
						}
						JobForwardFile info = new JobForwardFile(
								(Integer)jobIdMap.get(fileJobId + STATUS),
								(Integer)jobIdMap.get(fileJobId + END_STATUS),
								(String)jobIdMap.get(fileJobId + FILE),
								srcFacilityId,
								srcFacilityName,
								destFacilityId,
								destFacilityName,
								startDate, endDate);
						list.add(info);
					}
				} else if(childSessionJob.getId().getJobId().indexOf(CreateFileJob.CHECK_CHECKSUM) != -1){
					//"_CHECK_CHECKSUM"を除いたジョブIDを取得
					String fileJobId = childSessionJob.getId().getJobId().replaceAll(CreateFileJob.CHECK_CHECKSUM, "");

					if(childSessionJob.getEndStatus() != null &&
							childSessionJob.getEndStatus() != StatusConstant.TYPE_WAIT){
						jobIdMap.put(fileJobId + STATUS, childSessionJob.getStatus());
						jobIdMap.put(fileJobId + END_STATUS, childSessionJob.getEndStatus());
						jobIdMap.put(fileJobId + END_DATE, childSessionJob.getEndDate());
					}

					Object obj;
					obj = jobIdMap.get(fileJobId + START_DATE);
					Long startDate = null;
					if (obj != null) {
						startDate = (Long)obj;
					}
					obj = jobIdMap.get(fileJobId + END_DATE);
					Long endDate = null;
					if (obj != null) {
						endDate = (Long)obj;
					}
					JobForwardFile info = new JobForwardFile(
							(Integer)jobIdMap.get(fileJobId + STATUS),
							(Integer)jobIdMap.get(fileJobId + END_STATUS),
							(String)jobIdMap.get(fileJobId + FILE),
							srcFacilityId,
							srcFacilityName,
							destFacilityId,
							destFacilityName,
							startDate, endDate);
					list.add(info);
				}else if(childSessionJob.getId().getJobId().indexOf(CreateHulftJob.HULOPLCMD) != -1){
					//"_HULOPLCMD"を除いたジョブIDを取得
					String fileJobId = childSessionJob.getId().getJobId().replaceAll(CreateHulftJob.HULOPLCMD, "");

					jobIdMap.put(fileJobId + STATUS, childSessionJob.getStatus());
					jobIdMap.put(fileJobId + END_STATUS, childSessionJob.getEndStatus());
					jobIdMap.put(fileJobId + END_DATE, childSessionJob.getEndDate());
					jobIdMap.put(fileJobId + FILE, job.getArgument());
					jobIdMap.put(fileJobId + START_DATE, childSessionJob.getStartDate());

					Object obj;
					obj = jobIdMap.get(fileJobId + START_DATE);
					Long startDate = null;
					if (obj != null) {
						startDate = (Long)obj;
					}
					obj = jobIdMap.get(fileJobId + END_DATE);
					Long endDate = null;
					if (obj != null) {
						endDate = (Long)obj;
					}
					JobForwardFile info = new JobForwardFile(
							(Integer)jobIdMap.get(fileJobId + STATUS),
							(Integer)jobIdMap.get(fileJobId + END_STATUS),
							(String)jobIdMap.get(fileJobId + FILE),
							srcFacilityId,
							srcFacilityName,
							destFacilityId,
							destFacilityName,
							startDate, endDate);
					list.add(info);
				}
			}
		}

		return list;
	}

	/**
	 * ジョブツリー情報を取得します。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId　所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @return ジョブツリー情報
	 * @throws InvalidRole
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.jobmanagement.bean.JobInfo
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#createSessionJobData(JobInfoLocal)
	 */
	public JobTreeItem getSessionJobInfo(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole, HinemosUnknown {

		//JobTreeItemを作成
		JobTreeItem item = null;

		//セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		JobInfoEntity job = sessionJob.getJobInfoEntity();

		//JobTreeItemに格納するジョブ情報(JobInfo)を作成
		JobInfo info = createSessionJobData(job);

		item = new JobTreeItem(null, info);

		return item;
	}

	/**
	 * セッションジョブ情報作成
	 * ジョブ履歴[ジョブ詳細]ビューでジョブをダブルクリックし、
	 * 設定ダイアログを表示する際に呼ばれます。
	 *
	 * @param job ジョブ情報
	 * @return ジョブ情報
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobInfo
	 */
	private JobInfo createSessionJobData(JobInfoEntity job) throws JobInfoNotFound, InvalidRole, HinemosUnknown {

		m_log.debug("createSessionJobData>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
		//JobInfoを作成
		JobInfo info = new JobInfo(job.getId().getJobunitId(), job.getId().getJobId(), job.getJobName(), job.getJobType());

		info.setDescription(job.getDescription());
		info.setOwnerRoleId(job.getJobSessionJobEntity().getOwnerRoleId());
		info.setRegisteredModule(job.isRegisteredModule());
		info.setIconId(job.getIconId());
		if (job.getJobSessionJobEntity() != null
				&& job.getJobSessionJobEntity().getJobSessionEntity() != null) {
			info.setExpNodeRuntimeFlg(job.getJobSessionJobEntity().getJobSessionEntity().getExpNodeRuntimeFlg());
		}

		if (job.getRegDate() != null) {
			info.setCreateTime(job.getRegDate());
		}
		if (job.getUpdateDate() != null) {
			info.setUpdateTime(job.getUpdateDate());
		}
		info.setCreateUser(job.getRegUser());
		info.setUpdateUser(job.getUpdateUser());

		//待ち条件を取得
		JobWaitRuleInfo waitRule = null;
		//待ち条件を取得
		waitRule = new JobWaitRuleInfo();
		if (job.getJobType() == JobConstant.TYPE_JOBNET ||
				job.getJobType() == JobConstant.TYPE_JOB||
				job.getJobType() == JobConstant.TYPE_FILEJOB ||
				job.getJobType() == JobConstant.TYPE_APPROVALJOB ||
				job.getJobType() == JobConstant.TYPE_MONITORJOB ||
				job.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB ||
				job.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB ||
				job.getJobType() == JobConstant.TYPE_FILECHECKJOB ||
				job.getJobType() == JobConstant.TYPE_RESOURCEJOB ||
				job.getJobType() == JobConstant.TYPE_RPAJOB) {
			waitRule.setSuspend(job.getSuspend());
			waitRule.setCondition(job.getConditionType());
			waitRule.setEndCondition(job.getUnmatchEndFlg());
			waitRule.setEndStatus(job.getUnmatchEndStatus());
			waitRule.setEndValue(job.getUnmatchEndValue());
			waitRule.setSkip(job.getSkip());
			waitRule.setSkipEndStatus(job.getSkipEndStatus());
			waitRule.setSkipEndValue(job.getSkipEndValue());
			waitRule.setExclusiveBranch(job.getExclusiveBranchFlg());
			waitRule.setExclusiveBranchEndStatus(job.getExclusiveBranchEndStatus());
			waitRule.setExclusiveBranchEndValue(job.getExclusiveBranchEndValue());
			waitRule.setCalendar(job.getCalendar());
			waitRule.setCalendarId(job.getCalendarId());
			waitRule.setCalendarEndStatus(job.getCalendarEndStatus());
			waitRule.setCalendarEndValue(job.getCalendarEndValue());
			waitRule.setJobRetryFlg(job.getJobRetryFlg());
			waitRule.setJobRetry(job.getJobRetry());
			waitRule.setJobRetryInterval(job.getJobRetryInterval());
			waitRule.setJobRetryEndStatus(job.getJobRetryEndStatus());

			waitRule.setStart_delay(job.getStartDelay());
			waitRule.setStart_delay_session(job.getStartDelaySession());
			waitRule.setStart_delay_session_value(job.getStartDelaySessionValue());
			waitRule.setStart_delay_time(job.getStartDelayTime());
			if (job.getStartDelayTimeValue() != null) {
				waitRule.setStart_delay_time_value(job.getStartDelayTimeValue());
			}
			waitRule.setStart_delay_condition_type(job.getStartDelayConditionType());
			waitRule.setStart_delay_notify(job.getStartDelayNotify());
			waitRule.setStart_delay_notify_priority(job.getStartDelayNotifyPriority());
			waitRule.setStart_delay_operation(job.getStartDelayOperation());
			waitRule.setStart_delay_operation_type(job.getStartDelayOperationType());
			waitRule.setStart_delay_operation_end_status(job.getStartDelayOperationEndStatus());
			waitRule.setStart_delay_operation_end_value(job.getStartDelayOperationEndValue());

			waitRule.setEnd_delay(job.getEndDelay());
			waitRule.setEnd_delay_session(job.getEndDelaySession());
			waitRule.setEnd_delay_session_value(job.getEndDelaySessionValue());
			waitRule.setEnd_delay_job(job.getEndDelayJob());
			waitRule.setEnd_delay_job_value(job.getEndDelayJobValue());
			waitRule.setEnd_delay_time(job.getEndDelayTime());
			if (job.getEndDelayTimeValue() != null) {
				waitRule.setEnd_delay_time_value(job.getEndDelayTimeValue());
			}
			waitRule.setEnd_delay_condition_type(job.getEndDelayConditionType());
			waitRule.setEnd_delay_notify(job.getEndDelayNotify());
			waitRule.setEnd_delay_notify_priority(job.getEndDelayNotifyPriority());
			waitRule.setEnd_delay_operation(job.getEndDelayOperation());
			waitRule.setEnd_delay_operation_type(job.getEndDelayOperationType());
			waitRule.setEnd_delay_operation_end_status(job.getEndDelayOperationEndStatus());
			waitRule.setEnd_delay_operation_end_value(job.getEndDelayOperationEndValue());
			waitRule.setEnd_delay_change_mount(job.getEndDelayChangeMount());
			waitRule.setEnd_delay_change_mount_value(job.getEndDelayChangeMountValue());
			waitRule.setMultiplicityNotify(job.getMultiplicityNotify());
			waitRule.setMultiplicityNotifyPriority(job.getMultiplicityNotifyPriority());
			waitRule.setMultiplicityOperation(job.getMultiplicityOperation());
			waitRule.setMultiplicityEndValue(job.getMultiplicityEndValue());
			waitRule.setQueueFlg(job.getQueueFlg());
			waitRule.setQueueId(job.getQueueId());
		}

		//待ち条件(ジョブ)を取得
		List<JobWaitGroupInfoEntity> waitGroupList = job.getJobWaitGroupInfoEntities();
		ArrayList<JobObjectGroupInfo> objectGroupList = new ArrayList<>();
		if(waitGroupList != null && waitGroupList.size() > 0){
			for (JobWaitGroupInfoEntity waitGroup : waitGroupList){
				if(waitGroup == null){
					continue;
				}
				JobObjectGroupInfo objectGroupInfo = new JobObjectGroupInfo();
				objectGroupInfo.setOrderNo(waitGroup.getId().getOrderNo());
				objectGroupInfo.setConditionType(waitGroup.getConditionType());
				objectGroupInfo.setIsGroup(waitGroup.getIsGroup());
				objectGroupInfo.setIsGroup(false);
				ArrayList<JobObjectInfo> objectList = new ArrayList<JobObjectInfo>();
				for (JobWaitInfoEntity wait : waitGroup.getJobWaitInfoEntities()) {
					JobObjectInfo objectInfo = new JobObjectInfo();
					if (wait.getId().getTargetJobType() != JudgmentObjectConstant.TYPE_TIME
							&& wait.getId().getTargetJobType() != JudgmentObjectConstant.TYPE_START_MINUTE
							&& wait.getId().getTargetJobType() != JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
						//対象ジョブを取得
						String jobName;
						try {
							JobSessionJobEntity targetJobSessionJob = QueryUtil.getJobSessionJobPK(wait.getId().getSessionId(),
									wait.getId().getJobunitId(),
									wait.getId().getTargetJobId());
							JobInfoEntity targetJob = targetJobSessionJob.getJobInfoEntity();
							jobName = targetJob.getJobName();
						} catch (JobInfoNotFound | InvalidRole | RuntimeException e) {
							jobName = "";
						}

						if (jobName.equals("") && (wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS ||
							wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE)) {
							//セッション横断待ち条件の場合JobSessionJobEntitiyが見つからない場合がある。
							//その場合、JobMstEntitiyから待ち合わせジョブ名を取得する。
							try {
								JobMstEntity targetJob= QueryUtil.getJobMstPK(wait.getId().getJobunitId(), wait.getId().getJobId());
								jobName = targetJob.getJobName();
							} catch(JobMasterNotFound e) {
								jobName = "";
							}
						}
						objectInfo.setJobId(wait.getId().getTargetJobId());
						objectInfo.setJobName(jobName);
					}
					objectInfo.setType(wait.getId().getTargetJobType());
					switch (objectInfo.getType()) {
					case JudgmentObjectConstant.TYPE_JOB_END_STATUS:
						/** ジョブ終了状態 */
						objectInfo.setStatus(wait.getId().getTargetInt1());
						break;
					case JudgmentObjectConstant.TYPE_JOB_END_VALUE:
						/** ジョブ終了値 */
						objectInfo.setDecisionCondition(wait.getId().getTargetInt1());
						objectInfo.setValue(wait.getId().getTargetStr1());
						break;
					case JudgmentObjectConstant.TYPE_TIME:
						/** 時刻 */
						objectInfo.setTime(wait.getId().getTargetLong());
						break;
					case JudgmentObjectConstant.TYPE_START_MINUTE:
						/** セッション開始時の時間（分）  */
						objectInfo.setStartMinute(wait.getId().getTargetInt1());
						break;
					case JudgmentObjectConstant.TYPE_JOB_PARAMETER:
						/** ジョブ変数 */
						objectInfo.setDecisionCondition(wait.getId().getTargetInt1());
						objectInfo.setDecisionValue(wait.getId().getTargetStr1());
						objectInfo.setValue(wait.getId().getTargetStr2());
						break;
					case JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS:
						/** セッション横断ジョブ終了状態 */
						objectInfo.setStatus(wait.getId().getTargetInt1());
						objectInfo.setCrossSessionRange(wait.getId().getTargetInt2());
						break;
					case JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE:
						/** セッション横断ジョブ終了値 */
						objectInfo.setDecisionCondition(wait.getId().getTargetInt1());
						objectInfo.setCrossSessionRange(wait.getId().getTargetInt2());
						objectInfo.setValue(wait.getId().getTargetStr1());
						break;
					case JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE:
						/** ジョブ戻り値 */
						objectInfo.setDecisionCondition(wait.getId().getTargetInt1());
						objectInfo.setValue(wait.getId().getTargetStr1());
						break;
					default:
						break;
					}
					objectInfo.setDescription(wait.getDescription());
					objectList.add(objectInfo);
				}
				objectGroupInfo.setJobObjectList(objectList);
				objectGroupList.add(objectGroupInfo);
			}
		}

		// 排他分岐後続ジョブ優先度設定
		List<JobNextJobOrderInfoEntity> nextJobOrderEntityList = job.getJobNextJobOrderInfoEntities();
		List<JobNextJobOrderInfo> nextJobOrderList = new ArrayList<>();
		if (nextJobOrderEntityList != null) {
			//優先度順にソート
			//nextJobOrderListに優先度順で登録する
			nextJobOrderEntityList.sort(Comparator.comparing(orderEntity -> orderEntity.getOrder()));
			for (JobNextJobOrderInfoEntity nextJobOrderEntity: nextJobOrderEntityList) {
				JobNextJobOrderInfo nextJobOrder = new JobNextJobOrderInfo();
				nextJobOrder.setJobunitId(nextJobOrderEntity.getId().getJobunitId());
				nextJobOrder.setJobId(nextJobOrderEntity.getId().getJobId());
				nextJobOrder.setNextJobId(nextJobOrderEntity.getId().getNextJobId());
				nextJobOrderList.add(nextJobOrder);
			}
		}

		/*
		 * ソート処理
		 */
		Collections.sort(objectGroupList);
		waitRule.setObjectGroup(objectGroupList);
		waitRule.setExclusiveBranchNextJobOrderList(nextJobOrderList);
		info.setWaitRule(waitRule);

		//実行コマンドを取得
		if(job.getJobType() == JobConstant.TYPE_JOB){
			JobCommandInfo commandInfo = null;
			commandInfo = new JobCommandInfo();
			commandInfo.setFacilityID(job.getFacilityId());
			commandInfo.setProcessingMethod(job.getProcessMode());
			commandInfo.setStartCommand(job.getStartCommand());
			commandInfo.setStopType(job.getStopType());
			commandInfo.setStopCommand(job.getStopCommand());
			commandInfo.setSpecifyUser(job.getSpecifyUser());
			commandInfo.setUser(job.getEffectiveUser());
			commandInfo.setMessageRetry(job.getMessageRetry());
			commandInfo.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
			commandInfo.setMessageRetryEndValue(job.getMessageRetryEndValue());
			commandInfo.setCommandRetryFlg(job.getCommandRetryFlg());
			commandInfo.setCommandRetry(job.getCommandRetry());
			commandInfo.setCommandRetryEndStatus(job.getCommandRetryEndStatus());
			// コマンド終了時のジョブ変数が存在する場合は追加
			ArrayList<JobCommandParam> jobCommandParamList = new ArrayList<>();
			if (job.getJobCommandParamInfoEntities() != null && job.getJobCommandParamInfoEntities().size() > 0) {
				for (JobCommandParamInfoEntity jobCommandParamInfoEntity : job.getJobCommandParamInfoEntities()) {
					JobCommandParam jobCommandParam = new JobCommandParam();
					jobCommandParam.setJobStandardOutputFlg(jobCommandParamInfoEntity.getJobStandardOutputFlg());
					jobCommandParam.setParamId(jobCommandParamInfoEntity.getId().getParamId());
					jobCommandParam.setValue(jobCommandParamInfoEntity.getValue());
					jobCommandParamList.add(jobCommandParam);
				}
			}
			commandInfo.setJobCommandParamList(jobCommandParamList);
			commandInfo.setManagerDistribution(job.getManagerDistribution());
			commandInfo.setScriptName(job.getScriptName());
			commandInfo.setScriptEncoding(job.getScriptEncoding());
			commandInfo.setScriptContent(job.getScriptContent());
			List<JobEnvVariableInfo> envList = new ArrayList<JobEnvVariableInfo>();
			for(JobEnvVariableInfoEntity entity : job.getJobEnvVariableInfoEntities()) {
				JobEnvVariableInfo envInfo = new JobEnvVariableInfo();
				envInfo.setEnvVariableId(entity.getId().getEnvVariableId());
				envInfo.setValue(entity.getValue());
				envInfo.setDescription(entity.getDescription());
				envList.add(envInfo);
			}
			commandInfo.setEnvVariableInfo(envList);
			// 標準出力のファイル出力情報
			if (job.getJobOutputInfoEntities() != null) {
				for (JobOutputInfoEntity entity : job.getJobOutputInfoEntities()) {
					JobOutputInfo outputInfo = new JobOutputInfo();
					outputInfo.setSameNormalFlg(entity.getSameNormalFlg());
					outputInfo.setDirectory(entity.getDirectory());
					outputInfo.setFileName(entity.getFileName());
					outputInfo.setAppendFlg(entity.getAppendFlg());
					outputInfo.setFailureOperationFlg(entity.getFailureOperationFlg());
					outputInfo.setFailureOperationType(entity.getFailureOperationType());
					outputInfo.setFailureOperationEndStatus(entity.getFailureOperationEndStatus());
					outputInfo.setFailureOperationEndValue(entity.getFailureOperationEndValue());
					outputInfo.setFailureNotifyFlg(entity.getFailureNotifyFlg());
					outputInfo.setFailureNotifyPriority(entity.getFailureNotifyPriority());
					outputInfo.setValid(entity.getValid());
					if (JobOutputType.STDOUT.getCode().equals(entity.getId().getOutputType())) {
						commandInfo.setNormalJobOutputInfo(outputInfo);
					} else if (JobOutputType.STDERR.getCode().equals(entity.getId().getOutputType())) {
						commandInfo.setErrorJobOutputInfo(outputInfo);
					}
				}
			}
			
			try {
				//ファシリティパスを取得
				commandInfo.setScope(new RepositoryControllerBean().getFacilityPath(job.getFacilityId(), null));
			} catch (HinemosUnknown e) {
				m_log.debug(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("createSessionJobData() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
			info.setCommand(commandInfo);
		}

		//ファイル転送を取得
		if(job.getJobType() == JobConstant.TYPE_FILEJOB){
			JobFileInfo fileInfo = null;
			fileInfo = new JobFileInfo();
			fileInfo.setProcessingMethod(job.getProcessMode());
			fileInfo.setSrcFacilityID(job.getSrcFacilityId());
			fileInfo.setDestFacilityID(job.getDestFacilityId());
			fileInfo.setSrcFile(job.getSrcFile());
			fileInfo.setSrcWorkDir(job.getSrcWorkDir());
			fileInfo.setDestDirectory(job.getDestDirectory());
			fileInfo.setDestWorkDir(job.getDestWorkDir());
			fileInfo.setCompressionFlg(job.getCompressionFlg());
			fileInfo.setCheckFlg(job.getCheckFlg());
			fileInfo.setSpecifyUser(job.getSpecifyUser());
			fileInfo.setUser(job.getEffectiveUser());
			fileInfo.setMessageRetry(job.getMessageRetry());
			fileInfo.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
			fileInfo.setMessageRetryEndValue(job.getMessageRetryEndValue());
			try {
				//ファシリティパスを取得
				fileInfo.setSrcScope(new RepositoryControllerBean().getFacilityPath(job.getSrcFacilityId(), null));
				fileInfo.setDestScope(new RepositoryControllerBean().getFacilityPath(job.getDestFacilityId(), null));
			} catch (HinemosUnknown e) {
				m_log.warn(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("createSessionJobData() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
			info.setFile(fileInfo);
		}
		if (job.getJobType() != JobConstant.TYPE_REFERJOB && job.getJobType() != JobConstant.TYPE_REFERJOBNET) {
			//通知メッセージを取得
			info.setBeginPriority(job.getBeginPriority());
			info.setNormalPriority(job.getNormalPriority());
			info.setWarnPriority(job.getWarnPriority());
			info.setAbnormalPriority(job.getAbnormalPriority());

			ArrayList<NotifyRelationInfo> nriList =  new ArrayList<NotifyRelationInfo>();
			nriList = new NotifyControllerBean().getNotifyRelation(job.getNotifyGroupId());
			Collections.sort(nriList);
			info.setNotifyRelationInfos(nriList);

			//終了状態を取得
			ArrayList<JobEndStatusInfo> endList = new ArrayList<JobEndStatusInfo>();
			// 正常
			JobEndStatusInfo endInfoNormal = new JobEndStatusInfo();
			endInfoNormal.setType(EndStatusConstant.TYPE_NORMAL);
			endInfoNormal.setValue(job.getNormalEndValue());
			endInfoNormal.setStartRangeValue(job.getNormalEndValueFrom());
			endInfoNormal.setEndRangeValue(job.getNormalEndValueTo());
			endList.add(endInfoNormal);
			// 警告
			JobEndStatusInfo endInfoWarn = new JobEndStatusInfo();
			endInfoWarn.setType(EndStatusConstant.TYPE_WARNING);
			endInfoWarn.setValue(job.getWarnEndValue());
			endInfoWarn.setStartRangeValue(job.getWarnEndValueFrom());
			endInfoWarn.setEndRangeValue(job.getWarnEndValueTo());
			endList.add(endInfoWarn);
			// 異常
			JobEndStatusInfo endInfoAbnormal = new JobEndStatusInfo();
			endInfoAbnormal.setType(EndStatusConstant.TYPE_ABNORMAL);
			endInfoAbnormal.setValue(job.getAbnormalEndValue());
			endInfoAbnormal.setStartRangeValue(job.getAbnormalEndValueFrom());
			endInfoAbnormal.setEndRangeValue(job.getAbnormalEndValueTo());
			endList.add(endInfoAbnormal);
			
			info.setEndStatus(endList);
		}
		//承認ジョブ
		if (job.getJobType() == JobConstant.TYPE_APPROVALJOB) {
			info.setApprovalReqRoleId(job.getApprovalReqRoleId());
			info.setApprovalReqUserId(job.getApprovalReqUserId());
			info.setApprovalReqSentence(job.getApprovalReqSentence());
			info.setApprovalReqMailTitle(job.getApprovalReqMailTitle());
			info.setApprovalReqMailBody(job.getApprovalReqMailBody());
			info.setUseApprovalReqSentence(job.isUseApprovalReqSentence());
		}
		// 監視ジョブ
		if (job.getJobType() == JobConstant.TYPE_MONITORJOB) {
			MonitorJobInfo monitorInfo = new MonitorJobInfo();
			monitorInfo.setFacilityID(job.getFacilityId());
			monitorInfo.setProcessingMethod(job.getProcessMode());
			try {
				//ファシリティパスを取得
				monitorInfo.setScope(new RepositoryControllerBean().getFacilityPath(job.getFacilityId(), null));
			} catch (HinemosUnknown e) {
				m_log.debug(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("createSessionJobData() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
			monitorInfo.setMonitorCriticalEndValue(job.getMonitorCriticalEndValue());
			monitorInfo.setMonitorId(job.getMonitorId());
			monitorInfo.setMonitorInfoEndValue(job.getMonitorInfoEndValue());
			monitorInfo.setMonitorUnknownEndValue(job.getMonitorUnknownEndValue());
			monitorInfo.setMonitorWaitEndValue(job.getMonitorWaitEndValue());
			monitorInfo.setMonitorWaitTime(job.getMonitorWaitTime());
			monitorInfo.setMonitorWarnEndValue(job.getMonitorWarnEndValue());
			info.setMonitor(monitorInfo);
		}
		//ジョブ連携送信ジョブ
		if (job.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB) {
			JobLinkSendInfo jobLinkSendInfo = new JobLinkSendInfo();
			jobLinkSendInfo.setRetryFlg(job.getRetryFlg());
			jobLinkSendInfo.setRetryCount(job.getRetryCount());
			jobLinkSendInfo.setFailureOperation(job.getFailureOperation());
			jobLinkSendInfo.setFailureEndStatus(job.getFailureEndStatus());
			jobLinkSendInfo.setJoblinkMessageId(job.getJoblinkMessageId());
			jobLinkSendInfo.setPriority(job.getPriority());
			jobLinkSendInfo.setMessage(job.getMessage());
			jobLinkSendInfo.setSuccessEndValue(job.getSuccessEndValue());
			jobLinkSendInfo.setFailureEndValue(job.getFailureEndValue());
			jobLinkSendInfo.setJoblinkSendSettingId(job.getJoblinkSendSettingId());
			jobLinkSendInfo.setFacilityID(job.getFacilityId());
			jobLinkSendInfo.setProcessingMethod(job.getProcessMode());
			try {
				//ファシリティパスを取得
				jobLinkSendInfo.setScope(new RepositoryControllerBean().getFacilityPath(job.getFacilityId(), null));
			} catch (HinemosUnknown e) {
				m_log.debug(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("createSessionJobData() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
			jobLinkSendInfo.setProtocol(job.getProtocol());
			jobLinkSendInfo.setPort(job.getPort());
			jobLinkSendInfo.setHinemosUserId(job.getHinemosUserId());
			jobLinkSendInfo.setHinemosPassword(job.getHinemosPassword());
			jobLinkSendInfo.setProxyFlg(job.getProxyFlg());
			jobLinkSendInfo.setProxyHost(job.getProxyHost());
			jobLinkSendInfo.setProxyPort(job.getProxyPort()); 
			jobLinkSendInfo.setProxyUser(job.getProxyUser());
			jobLinkSendInfo.setProxyPassword(job.getProxyPassword());

			if (job.getJobLinkJobExpInfoEntities() != null) {
				jobLinkSendInfo.setJobLinkExpList(new ArrayList<>());
				for (JobLinkJobExpInfoEntity entity : job.getJobLinkJobExpInfoEntities()) {
					JobLinkExpInfo jobLinkExpInfo = new JobLinkExpInfo();
					jobLinkExpInfo.setKey(entity.getId().getKey());
					jobLinkExpInfo.setValue(entity.getValue());
					jobLinkSendInfo.getJobLinkExpList().add(jobLinkExpInfo);
				}
			}

			info.setJobLinkSend(jobLinkSendInfo);
		}
		//ジョブ連携待機ジョブ
		if (job.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB) {
			JobLinkRcvInfo jobLinkRcvInfo = new JobLinkRcvInfo();
			jobLinkRcvInfo.setFacilityID(job.getFacilityId());
			try {
				//ファシリティパスを取得
				jobLinkRcvInfo.setScope(new RepositoryControllerBean().getFacilityPath(job.getFacilityId(), null));
			} catch (HinemosUnknown e) {
				m_log.debug(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("createSessionJobData() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
			jobLinkRcvInfo.setMonitorInfoEndValue(job.getMonitorInfoEndValue());
			jobLinkRcvInfo.setMonitorWarnEndValue(job.getMonitorWarnEndValue());
			jobLinkRcvInfo.setMonitorCriticalEndValue(job.getMonitorCriticalEndValue());
			jobLinkRcvInfo.setMonitorUnknownEndValue(job.getMonitorUnknownEndValue());
			jobLinkRcvInfo.setFailureEndFlg(job.getFailureEndFlg());
			jobLinkRcvInfo.setMonitorWaitTime(job.getMonitorWaitTime());
			jobLinkRcvInfo.setMonitorWaitEndValue(job.getMonitorWaitEndValue());
			jobLinkRcvInfo.setJoblinkMessageId(job.getJoblinkMessageId());
			jobLinkRcvInfo.setMessage(job.getMessage());
			jobLinkRcvInfo.setPastFlg(job.getPastFlg());
			jobLinkRcvInfo.setPastMin(job.getPastMin());
			jobLinkRcvInfo.setInfoValidFlg(job.getInfoValidFlg());
			jobLinkRcvInfo.setWarnValidFlg(job.getWarnValidFlg());
			jobLinkRcvInfo.setCriticalValidFlg(job.getCriticalValidFlg());
			jobLinkRcvInfo.setUnknownValidFlg(job.getUnknownValidFlg());
			jobLinkRcvInfo.setApplicationFlg(job.getApplicationFlg());
			jobLinkRcvInfo.setApplication(job.getApplication());
			jobLinkRcvInfo.setMonitorDetailIdFlg(job.getMonitorDetailIdFlg());
			jobLinkRcvInfo.setMonitorDetailId(job.getMonitorDetailId());
			jobLinkRcvInfo.setMessageFlg(job.getMessageFlg());
			jobLinkRcvInfo.setExpFlg(job.getExpFlg());
			jobLinkRcvInfo.setMonitorAllEndValueFlg(job.getMonitorAllEndValueFlg());
			jobLinkRcvInfo.setMonitorAllEndValue(job.getMonitorAllEndValue());
			if (job.getJobLinkJobExpInfoEntities() != null) {
				jobLinkRcvInfo.setJobLinkExpList(new ArrayList<>());
				for (JobLinkJobExpInfoEntity entity : job.getJobLinkJobExpInfoEntities()) {
					JobLinkExpInfo jobLinkExpInfo = new JobLinkExpInfo();
					jobLinkExpInfo.setKey(entity.getId().getKey());
					jobLinkExpInfo.setValue(entity.getValue());
					jobLinkRcvInfo.getJobLinkExpList().add(jobLinkExpInfo);
				}
			}
			if (job.getJobLinkInheritInfoEntities() != null) {
				jobLinkRcvInfo.setJobLinkInheritList(new ArrayList<>());
				for (JobLinkInheritInfoEntity entity : job.getJobLinkInheritInfoEntities()) {
					JobLinkInheritInfo jobLinkInheritInfo = new JobLinkInheritInfo();
					jobLinkInheritInfo.setParamId(entity.getId().getParamId());
					jobLinkInheritInfo.setKeyInfo(entity.getKeyInfo());
					jobLinkInheritInfo.setExpKey(entity.getExpKey());
					jobLinkRcvInfo.getJobLinkInheritList().add(jobLinkInheritInfo);
				}
			}

			info.setJobLinkRcv(jobLinkRcvInfo);
		}
		//ファイルチェックジョブ
		if (job.getJobType() == JobConstant.TYPE_FILECHECKJOB) {
			JobFileCheckInfo jobFileCheckInfo = new JobFileCheckInfo();
			jobFileCheckInfo.setFacilityID(job.getFacilityId());
			try {
				//ファシリティパスを取得
				jobFileCheckInfo.setScope(new RepositoryControllerBean().getFacilityPath(job.getFacilityId(), null));
			} catch (HinemosUnknown e) {
				m_log.debug(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("createSessionJobData() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
			jobFileCheckInfo.setProcessingMethod(job.getProcessMode());
			jobFileCheckInfo.setSuccessEndValue(job.getSuccessEndValue());
			jobFileCheckInfo.setFailureEndFlg(job.getFailureEndFlg());
			jobFileCheckInfo.setFailureWaitTime(job.getFailureWaitTime());
			jobFileCheckInfo.setFailureEndValue(job.getFailureEndValue());
			jobFileCheckInfo.setDirectory(job.getDirectory());
			jobFileCheckInfo.setFileName(job.getFileName());
			jobFileCheckInfo.setCreateValidFlg(job.getCreateValidFlg());
			jobFileCheckInfo.setCreateBeforeJobStartFlg(job.getCreateBeforeJobStartFlg());
			jobFileCheckInfo.setDeleteValidFlg(job.getDeleteValidFlg());
			jobFileCheckInfo.setModifyValidFlg(job.getModifyValidFlg());
			jobFileCheckInfo.setModifyType(job.getModifyType());
			jobFileCheckInfo.setNotJudgeFileInUseFlg(job.getNotJudgeFileInUseFlg());
			jobFileCheckInfo.setMessageRetry(job.getMessageRetry());
			jobFileCheckInfo.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
			jobFileCheckInfo.setMessageRetryEndValue(job.getMessageRetryEndValue());
			info.setJobFileCheck(jobFileCheckInfo);
		}
		// リソース制御ジョブ
		if (job.getJobType() == JobConstant.TYPE_RESOURCEJOB) {
			ResourceJobInfo resourceInfo = new ResourceJobInfo();
			resourceInfo.setResourceCloudScopeId(job.getResourceCloudScopeId());
			resourceInfo.setResourceLocationId(job.getResourceLocationId());
			resourceInfo.setResourceType(job.getResourceType());
			resourceInfo.setResourceAction(job.getResourceAction());
			resourceInfo.setResourceTargetId(job.getResourceTargetId());
			resourceInfo.setResourceStatusConfirmTime(job.getResourceStatusConfirmTime());
			resourceInfo.setResourceStatusConfirmInterval(job.getResourceStatusConfirmInterval());
			resourceInfo.setResourceAttachNode(job.getResourceAttachNode());
			resourceInfo.setResourceAttachDevice(job.getResourceAttachDevice());
			resourceInfo.setResourceNotifyScope(job.getFacilityId());
			//ファシリティパスを取得
			try {
				resourceInfo.setResourceNotifyScopePath(
					new RepositoryControllerBean().getFacilityPath(job.getFacilityId(), null));
				} catch (HinemosUnknown e) {
					m_log.debug(e.getMessage(), e);
				} catch (Exception e) {
					m_log.warn("createSessionJobData() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
				resourceInfo.setResourceSuccessValue(job.getResourceSuccessValue());
				resourceInfo.setResourceFailureValue(job.getResourceFailureValue());
				info.setResource(resourceInfo);
		}
		// RPAシナリオジョブ
		if (job.getJobType() == JobConstant.TYPE_RPAJOB) {
			RpaJobInfo rpaJobInfo = new RpaJobInfo();
			rpaJobInfo.setRpaJobType(job.getRpaJobType());
			// 直接実行
			rpaJobInfo.setFacilityID(job.getFacilityId());
			try {
				//ファシリティパスを取得
				rpaJobInfo.setScope(new RepositoryControllerBean().getFacilityPath(job.getFacilityId(), null));
			} catch (HinemosUnknown e) {
				m_log.debug(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("createSessionJobData() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
			rpaJobInfo.setProcessingMethod(job.getProcessMode());
			rpaJobInfo.setRpaToolId(job.getRpaToolId());
			rpaJobInfo.setRpaExeFilepath(job.getRpaExeFilepath());
			rpaJobInfo.setRpaScenarioFilepath(job.getRpaScenarioFilepath());
			rpaJobInfo.setRpaLogDirectory(job.getRpaLogDirectory());
			rpaJobInfo.setRpaLogFileName(job.getRpaLogFileName());
			rpaJobInfo.setRpaLogEncoding(job.getRpaLogEncoding());
			rpaJobInfo.setRpaLogReturnCode(job.getRpaLogReturnCode());
			rpaJobInfo.setRpaLogPatternHead(job.getRpaLogPatternHead());
			rpaJobInfo.setRpaLogPatternTail(job.getRpaLogPatternTail());
			rpaJobInfo.setRpaLogMaxBytes(job.getRpaLogMaxBytes());
			rpaJobInfo.setRpaDefaultEndValue(job.getRpaDefaultEndValue());
			rpaJobInfo.setRpaLoginFlg(job.getRpaLoginFlg());
			rpaJobInfo.setRpaLoginUserId(job.getRpaLoginUserId());
			rpaJobInfo.setRpaLoginPassword(job.getRpaLoginPassword());
			rpaJobInfo.setRpaLoginRetry(job.getRpaLoginRetry());
			rpaJobInfo.setRpaLoginEndValue(job.getRpaLoginEndValue());
			rpaJobInfo.setRpaLoginResolution(job.getRpaLoginResolution());
			rpaJobInfo.setRpaLogoutFlg(job.getRpaLogoutFlg());
			rpaJobInfo.setRpaScreenshotEndDelayFlg(job.getRpaScreenshotEndDelayFlg());
			rpaJobInfo.setRpaScreenshotEndValueFlg(job.getRpaScreenshotEndValueFlg());
			rpaJobInfo.setRpaScreenshotEndValue(job.getRpaScreenshotEndValue());
			rpaJobInfo.setRpaScreenshotEndValueCondition(job.getRpaScreenshotEndValueCondition());
			rpaJobInfo.setRpaNotLoginNotify(job.getRpaNotLoginNotify());
			rpaJobInfo.setRpaNotLoginNotifyPriority(job.getRpaNotLoginNotifyPriority());
			rpaJobInfo.setRpaNotLoginEndValue(job.getRpaNotLoginEndValue());
			rpaJobInfo.setRpaAlreadyRunningNotify(job.getRpaAlreadyRunningNotify());
			rpaJobInfo.setRpaAlreadyRunningNotifyPriority(job.getRpaAlreadyRunningNotifyPriority());
			rpaJobInfo.setRpaAlreadyRunningEndValue(job.getRpaAlreadyRunningEndValue());
			rpaJobInfo.setRpaAbnormalExitNotify(job.getRpaAbnormalExitNotify());
			rpaJobInfo.setRpaAbnormalExitNotifyPriority(job.getRpaAbnormalExitNotifyPriority());
			rpaJobInfo.setRpaAbnormalExitEndValue(job.getRpaAbnormalExitEndValue());
			rpaJobInfo.setRpaJobOptionInfos(new ArrayList<>());
			job.getJobRpaOptionInfoEntities().forEach( e -> {
				RpaJobOptionInfo optionInfo = new RpaJobOptionInfo();
				optionInfo.setOrderNo(e.getId().getOrderNo());
				optionInfo.setOption(e.getOption());
				optionInfo.setDescription(e.getDescription());
				rpaJobInfo.getRpaJobOptionInfos().add(optionInfo);
			}); 
			rpaJobInfo.setRpaJobEndValueConditionInfos(new ArrayList<>());
			job.getJobRpaEndValueConditionInfoEntities().forEach( e -> {
				RpaJobEndValueConditionInfo endValueInfo = new RpaJobEndValueConditionInfo();
				endValueInfo.setOrderNo(e.getId().getOrderNo());
				endValueInfo.setConditionType(e.getConditionType());
				endValueInfo.setPattern(e.getPattern());
				endValueInfo.setCaseSensitivityFlg(e.getCaseSensitivityFlg());
				endValueInfo.setProcessType(e.getProcessType());
				endValueInfo.setReturnCode(e.getReturnCode());
				endValueInfo.setReturnCodeCondition(e.getReturnCodeCondition());
				endValueInfo.setUseCommandReturnCodeFlg(e.getUseCommandReturnCodeFlg());
				endValueInfo.setEndValue(e.getEndValue());
				endValueInfo.setDescription(e.getDescription());
				rpaJobInfo.getRpaJobEndValueConditionInfos().add(endValueInfo);
			});
			// 間接実行
			rpaJobInfo.setRpaScopeId(job.getRpaScopeId());
			rpaJobInfo.setRpaRunType(job.getRpaRunType());
			rpaJobInfo.setRpaScenarioParam(job.getRpaScenarioParam());
			rpaJobInfo.setRpaStopType(job.getRpaStopType());
			rpaJobInfo.setRpaStopMode(job.getRpaStopMode());
			rpaJobInfo.setRpaRunConnectTimeout(job.getRpaRunConnectTimeout());
			rpaJobInfo.setRpaRunRequestTimeout(job.getRpaRunRequestTimeout());
			rpaJobInfo.setRpaRunEndFlg(job.getRpaRunEndFlg());
			rpaJobInfo.setRpaRunRetry(job.getRpaRunRetry());
			rpaJobInfo.setRpaRunEndValue(job.getRpaRunEndValue());
			rpaJobInfo.setRpaCheckConnectTimeout(job.getRpaCheckConnectTimeout());
			rpaJobInfo.setRpaCheckRequestTimeout(job.getRpaCheckRequestTimeout());
			rpaJobInfo.setRpaCheckEndFlg(job.getRpaCheckEndFlg());
			rpaJobInfo.setRpaCheckRetry(job.getRpaCheckRetry());
			rpaJobInfo.setRpaCheckEndValue(job.getRpaCheckEndValue());
			rpaJobInfo.setRpaJobRunParamInfos(new ArrayList<>());
			job.getJobRpaRunParamInfoEntities().forEach(e -> {
				RpaJobRunParamInfo runParamInfo = new RpaJobRunParamInfo();
				runParamInfo.setParamId(e.getId().getParamId());
				runParamInfo.setParamValue(e.getParamValue());
				rpaJobInfo.getRpaJobRunParamInfos().add(runParamInfo);
			});
			rpaJobInfo.setRpaJobCheckEndValueInfos(new ArrayList<>());
			job.getJobRpaCheckEndValueInfoEntities().forEach(e -> {
				RpaJobCheckEndValueInfo checkEndValueInfo = new RpaJobCheckEndValueInfo();
				checkEndValueInfo.setEndStatusId(e.getId().getEndStatusId());
				checkEndValueInfo.setEndValue(e.getEndValue());
				rpaJobInfo.getRpaJobCheckEndValueInfos().add(checkEndValueInfo);
			});
			// 直接実行・間接実行共通
			rpaJobInfo.setMessageRetry(job.getMessageRetry());
			rpaJobInfo.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
			rpaJobInfo.setMessageRetryEndValue(job.getMessageRetryEndValue());
			rpaJobInfo.setCommandRetry(job.getCommandRetry());
			rpaJobInfo.setCommandRetryFlg(job.getCommandRetryFlg());
			rpaJobInfo.setCommandRetryEndStatus(job.getCommandRetryEndStatus());
			info.setRpa(rpaJobInfo);
		}

		//パラメータを取得
		if(job.getJobType() == JobConstant.TYPE_JOBUNIT){
			ArrayList<JobParameterInfo> paramList = new ArrayList<JobParameterInfo>();
			Collection<JobParamInfoEntity> params = job.getJobParamInfoEntities();
			if(params != null){
				Iterator<JobParamInfoEntity> itr = params.iterator();
				while(itr.hasNext()){
					JobParamInfoEntity param = itr.next();
					JobParameterInfo paramInfo = new JobParameterInfo();
					paramInfo.setParamId(param.getId().getParamId());
					paramInfo.setType(param.getParamType());
					paramInfo.setDescription(param.getDescription());
					paramInfo.setValue(param.getValue());
					paramList.add(paramInfo);
				}
				/*
				 * ソート処理
				 */
				Collections.sort(paramList);
			}
			info.setParam(paramList);
		}

		return info;
	}

	private StringBuffer getJpql(StringBuffer sbJpql){

		// ADMINISTRATORSロールに所属している場合はオブジェクト権限チェックはしない
		Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
		if (isAdministrator != null && isAdministrator) {
			return sbJpql;
		}

		// ログインユーザから、所属ロールの一覧を取得
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		// ユーザ情報が取得できない場合は、そのまま返す
		if (loginUser == null || "".equals(loginUser.trim())) {
			return sbJpql;
		}

		// オブジェクト権限チェックを含むJPQLに変換
		String afterJpql = getObjectPrivilegeJPQL(sbJpql.toString(), HinemosModuleConstant.JOB, ObjectPrivilegeMode.READ);
		List<String> roleIds = external.getRoleIdList(loginUser);
		afterJpql = afterJpql.replaceAll(":roleIds", HinemosEntityManager.getParamNameString("roleId", roleIds.toArray(new String[roleIds.size()])));

		return new StringBuffer(afterJpql);
	}

	private <T> TypedQuery<T> setObjectPrivilegeParameter(TypedQuery<T> typedQuery) {

		// ADMINISTRATORSロールに所属している場合はオブジェクト権限チェックはしない
		Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
		if (isAdministrator != null && isAdministrator) {
			return typedQuery;
		}

		// ログインユーザから、所属ロールの一覧を取得
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		// ユーザ情報が取得できない場合は、そのまま返す
		if (loginUser == null || "".equals(loginUser.trim())) {
			return typedQuery;
		}

		List<String> roleIds = external.getRoleIdList(loginUser);
		return HinemosEntityManager.appendParam(typedQuery, "roleId", roleIds.toArray(new String[roleIds.size()]));
	}


	/**
	 * JPQLにオブジェクト権限チェックを入れて返す<br/>
	 *
	 * @param jpqlString JPQL文
	 * @param entityClass オブジェクト権限チェック対象のEntityクラス
	 * @param mode オブジェクト権限(READ, WRITE, EXEC)
	 * @return
	 */
	private String getObjectPrivilegeJPQL(String jpqlString, String objectType, ObjectPrivilegeMode mode) {

		String rtnString = "";
		try {

			// JPQLの構文解析
			JpqlParser jpqlParser = new JpqlParser();
			JpqlFrom jpqlFrom = null;
			JpqlWhere jpqlWhere = null;
			JpqlStatement statement = jpqlParser.parseQuery(jpqlString);

			if (statement.jjtGetChild(0) instanceof JpqlSelect ) {

				JpqlSelect jpqlSelect = (JpqlSelect)statement.jjtGetChild(0);
				for(int i=0 ; i<jpqlSelect.jjtGetNumChildren() ; i++ ) {
					if (jpqlSelect.jjtGetChild(i) instanceof JpqlFrom) {
						jpqlFrom = (JpqlFrom)jpqlSelect.jjtGetChild(i);
					} else if (jpqlSelect.jjtGetChild(i) instanceof JpqlWhere) {
						jpqlWhere = (JpqlWhere)jpqlSelect.jjtGetChild(i);
						break;
					}
				}

				// オブジェクト権限チェックのJPQLを挿入
				Node jpqlExists = createObjectPrivilegeExists(objectType, mode);

				if (jpqlWhere == null) {
					jpqlWhere = QueryPreparator.createWhere(jpqlExists);
					Node parent = jpqlFrom.jjtGetParent();
					for (int i = parent.jjtGetNumChildren(); i > 2; i--) {
						parent.jjtAddChild(parent.jjtGetChild(i - 1), i);
					}
					parent.jjtAddChild(jpqlWhere, 2);
				} else {
					Node condition = jpqlWhere.jjtGetChild(0);
					if (!(condition instanceof JpqlBrackets)) {
						condition = QueryPreparator.createBrackets(condition);
					}
					Node and = QueryPreparator.createAnd(condition, jpqlExists);
					and.jjtSetParent(jpqlWhere);
					jpqlWhere.jjtSetChild(and, 0);
				}
			}

			ToStringVisitor v = new ToStringVisitor();
			statement.jjtAccept(v, null);
			rtnString = statement.toString();
			m_log.debug("getObjectPrivilegeJPQL() jpql = " + rtnString);
		} catch (Exception e) {
			m_log.warn("getObjectPrivilegeJPQL() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
		return rtnString;
	}


	/**
	 * オブジェクト権限チェックを行うJPQLのノードを返す
	 * ログインユーザIDでチェックを行う。
	 *
	 * @param objectType オブジェクトタイプ
	 * @param mode オブジェクト権限(READ, WRITE, EXEC)
	 * @return オブジェクト権限チェックを行うJPQLのノード
	 */
	private Node createObjectPrivilegeExists(String objectType, ObjectPrivilegeMode mode) {
		String privilegeAlias = "x";		// サブクエリで使用するalias(ObjectPrivilegeEntity)
		String objectAlias = "a";			// 検索対象のテーブルのalias

		JpqlEquals equals1 = QueryPreparator.createEquals(QueryPreparator.createPath(privilegeAlias + ".id.objectId"), QueryPreparator.createPath(objectAlias + ".id.jobunitId"));
		JpqlEquals equals2 = QueryPreparator.createEquals(QueryPreparator.createPath(privilegeAlias + ".id.objectType"), QueryPreparator.createStringLiteral(objectType));
		JpqlEquals equals3 = QueryPreparator.createEquals(QueryPreparator.createPath(privilegeAlias + ".id.objectPrivilege"), QueryPreparator.createStringLiteral(mode.name()));
		JpqlIn in = QueryPreparator.createIn(QueryPreparator.createPath(privilegeAlias + ".id.roleId"), QueryPreparator.createNamedParameter("roleIds"));
		Node node = null;
		// #18669対応 データパターンによりパフォーマンスが大きく違っているため隠しプロパティによりSQLの切り替えを可能にする
		Long queryMode = HinemosPropertyCommon.job_history_select_query_mode.getNumericValue();
		if (queryMode.equals(1L)){
			// #12622対応前
			JpqlExists node1 = QueryPreparator.createExists(
					QueryPreparator.createSubselect(
							QueryPreparator.createSelectClause(privilegeAlias),
							QueryPreparator.createFrom(ObjectPrivilegeInfo.class.getSimpleName(), privilegeAlias),
							QueryPreparator.createWhere(QueryPreparator.createAnd(QueryPreparator.createAnd(QueryPreparator.createAnd(equals1, equals2), equals3), in))));

			JpqlIn node2 = QueryPreparator.createIn(QueryPreparator.createPath(objectAlias + ".ownerRoleId"), QueryPreparator.createNamedParameter("roleIds"));
			node = QueryPreparator.createBrackets(QueryPreparator.createOr(node1, node2));
		} else if (queryMode.equals(2L)){
			// #12622対応後
			JpqlExists node1 = QueryPreparator.createExists(
					QueryPreparator.createSubselect(
							QueryPreparator.createSelectClause(privilegeAlias),
							QueryPreparator.createFrom(ObjectPrivilegeInfo.class.getSimpleName(),privilegeAlias),
							QueryPreparator.createWhere(QueryPreparator.createOr(QueryPreparator.createAnd(QueryPreparator.createAnd(QueryPreparator.createAnd(equals1, equals2), equals3), in),
									QueryPreparator.createIn(QueryPreparator.createPath(objectAlias + ".ownerRoleId"),QueryPreparator.createNamedParameter("roleIds"))))));

			node = QueryPreparator.createBrackets(node1);
		} else {
			// #18669対応
			JpqlIn node1 = QueryPreparator.createIn(
					QueryPreparator.createPath(objectAlias + ".id.jobunitId")
					,QueryPreparator.createSubselect(
							QueryPreparator.createSelectClause(privilegeAlias + ".id.objectId"),
							QueryPreparator.createFrom(ObjectPrivilegeInfo.class.getSimpleName(),privilegeAlias),
							QueryPreparator.createWhere(QueryPreparator.createAnd(QueryPreparator.createAnd( equals2, equals3), in)
						))
					);
			JpqlIn node2 = QueryPreparator.createIn(QueryPreparator.createPath(objectAlias + ".ownerRoleId"), QueryPreparator.createNamedParameter("roleIds"));
			node = QueryPreparator.createBrackets(QueryPreparator.createOr(node1, node2));
		}
		return node;
	}

	/**
	 * 登録済みモジュール一覧情報を取得します。
	 * 
	 * @return 登録済みモジュール一覧情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public ArrayList<JobInfo> getRegisteredModule(String jobunitId) throws InvalidRole, HinemosUnknown {
		m_log.debug("getRegisteredModule()");
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			ArrayList<JobInfo> list = new ArrayList<JobInfo>();
			boolean registeredModule = true;
			Collection<JobMstEntity> ct = em.createNamedQuery("JobMstEntity.findByJobunitIdAndRegisteredModule", JobMstEntity.class)
											.setParameter("jobunitId", jobunitId)
											.setParameter("registeredModule", registeredModule).getResultList();
			
			for (JobMstEntity job : ct) {
				list.add(createJobData(job, false));
			}
			return list;
		}
	}

	/**
	 * 監視情報一覧を返します。
	 * <p>
	 * <ol>
	 * <li>引数で指定された監視対象の監視情報を取得します。</li>
	 * <li>１監視情報をテーブルのカラム順（{@link com.clustercontrol.monitor.run.bean.MonitorTabelDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>この１監視情報を保持するリストを、監視情報一覧を保持するリスト（{@link ArrayList}）に格納し返します。<BR>
	 * </li>
	 * </ol>
	 * 
	 * @param monitorTypeIds 監視対象IDリスト
	 * @param ownerRoleId オーナーロールID
	 * @return 監視情報一覧
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.monitor.run.bean.MonitorTabelDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<MonitorInfo> getMonitorListByMonitorTypeIds(List<String> monitorTypeIds, String ownerRoleId) throws InvalidRole, HinemosUnknown {
		return new ArrayList<>(QueryUtil.getMonitorInfoByMonitorTypeIds_OR(monitorTypeIds, ownerRoleId));
	}

	/**
	 * 承認対象ジョブの一覧情報を取得します。
	 * <p>
	 *
	 * @param property 一覧フィルタ用プロパティ
	 * @param limit 表示上限件数
	 * @return 承認対象ジョブの一覧情報
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<JobApprovalInfo> getApprovalJobList(JobApprovalFilter property, int limit) throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getApprovalJobList()");
		ArrayList<JobApprovalInfo> list = new ArrayList<JobApprovalInfo>();
		List<String> roleIdList = null;
		ArrayList<Integer> statuslist = null;
		Boolean isPending = null;
		
		Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		boolean isApprovalPrivilege = UserRoleCache.isSystemPrivilege(loginUser, new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.APPROVAL));
		roleIdList = external.getRoleIdList(loginUser);
		if(m_log.isDebugEnabled()){
			if(roleIdList != null && !roleIdList.isEmpty()){
				for(String role : roleIdList) {
					m_log.debug("roleIdList():" + role);
				}
			}
		}
		
		//"未承認"状態はDB上では"承認待"として保持されており、ログインユーザの承認可否でどちらかを区別する
		//フィルタ条件で"承認待"/"未承認"の指定がある場合に、依頼先ロール/ユーザ設定をSQLの条件に追加するかを判定する
		if(property.getStatusList() != null && property.getStatusList().length > 0){
			statuslist = new ArrayList<Integer>();
			statuslist.addAll(Arrays.asList(property.getStatusList()));
			
			boolean isShowPending = statuslist.contains(JobApprovalStatusConstant.TYPE_PENDING);
			boolean isShowStill = statuslist.contains(JobApprovalStatusConstant.TYPE_STILL);
			
			//SQLの条件に依頼先ロール/ユーザの設定一致(承認待)/不一致(未承認)を加えるか否かの判定
			//"承認待"/"未承認"のどちらかのみフィルタ指定された場合に依頼先設定を条件に加える("承認待"/"未承認"を区別するため)
			//フィルタ指定に両方有または両方無の場合、一律"承認待"として扱うため、依頼先設定は不要("承認待"/"未承認"の区別不要)
			if(isShowPending && !isShowStill){
				isPending = true;
			}else if(!isShowPending && isShowStill){
				isPending = false;
			}
			
			//ADMINISTRATORS権限有りの場合は一律"承認待"(承認可能)、承認権限無しの場合は一律"未承認"(承認不可)となるため依頼先設定は条件に加えない
			if(isAdministrator || !isApprovalPrivilege){
				isPending = null;
			}
			
			//依頼先設定をSQL条件に追加するか否かで、ステータスの条件指定も変更
			if(isPending != null){
				//"承認待"及び"未承認"は依頼先設定とセットでSQLの条件文に指定し、他のステータスの条件文とは別にするためリストから除外
				statuslist.remove(Integer.valueOf(JobApprovalStatusConstant.TYPE_PENDING));
				statuslist.remove(Integer.valueOf(JobApprovalStatusConstant.TYPE_STILL));
			}else{
				//DBには"未承認"は無いためリストから除外、"承認待"の指定はそのままリストに反映
				//どちらも指定無の場合はリスト更新不要、どちらも指定有の場合は"未承認"のリスト除外のみ
				statuslist.remove(Integer.valueOf(JobApprovalStatusConstant.TYPE_STILL));
				if(!isShowPending && isShowStill){
					//"承認待"は指定無、"未承認"は指定有の場合は"承認待"指定有へフィルタの条件を置換
					statuslist.add(Integer.valueOf(JobApprovalStatusConstant.TYPE_PENDING));
				}
				if(isAdministrator && (!isShowPending && isShowStill)){
					//ADMINISTRATORS権限有りの場合、"未承認"(承認不可)となるものは無いため
					//フィルタで"承認待"は指定無、"未承認"は指定有の場合、"承認待"を一律表示対象外とする
					statuslist.remove(Integer.valueOf(JobApprovalStatusConstant.TYPE_PENDING));
				}
				if(!isApprovalPrivilege && (isShowPending && !isShowStill)){
					//承認権限無しの場合は、"承認待"(承認可能)となるものは無いため
					//フィルタで"承認待"は指定有、"未承認"は指定無の場合、"承認待"を一律表示対象外とする
					statuslist.remove(Integer.valueOf(JobApprovalStatusConstant.TYPE_PENDING));
				}
				if (statuslist.size() == 0) {
					//表示対象のステータスが無くなった場合は、該当無しとして即時復帰する
					return list;
				}
			}
		}
		
		//表示上限+1件まで取得
		TypedQuery<?> typedQuery = getApprovalFilterQuery(property, statuslist, isPending, roleIdList, loginUser, Integer.valueOf(limit + 1));
		@SuppressWarnings("unchecked")
		List<JobInfoEntity> joblist = (List<JobInfoEntity>)typedQuery.getResultList();
		
		m_log.debug("approval job  num:" + joblist.size());
		
		for (JobInfoEntity job : joblist) {
			
			JobSessionJobEntity sessionJob = job.getJobSessionJobEntity();
			//セッションジョブに関連するセッションノードを取得
			List<JobSessionNodeEntity> nodeList = sessionJob.getJobSessionNodeEntities();
			JobSessionNodeEntity sessionNode =null;
			// 承認ジョブの場合はノードリストは1件のみ
			if(nodeList != null && nodeList.size() == 1){
				//セッションノードを取得
				sessionNode = nodeList.get(0);
			}else{
				// 通常ありえないデータのため一覧に表示させない
				continue;
			}
			
			Integer status = sessionNode.getApprovalStatus();
			//承認待状態のジョブに関して、ログインユーザの承認可否を判定
			//ADMINISTRATORS権限の場合は、依頼先設定や権限設定に関わらず表示/承認可能とする
			if(status == JobApprovalStatusConstant.TYPE_PENDING && !isAdministrator){
				//ユーザに承認権限がない、または承認依頼先のロール/ユーザ設定がログインユーザと一致しない場合は、承認不可として"未承認"へ
				if(!isApprovalPrivilege ||
					(roleIdList != null && !roleIdList.contains(job.getApprovalReqRoleId())) ||
					(job.getApprovalReqUserId() != null && (!job.getApprovalReqUserId().equals("*") && !job.getApprovalReqUserId().equals(loginUser)))
				){
					status = JobApprovalStatusConstant.TYPE_STILL;
				}
			}

			if(m_log.isDebugEnabled()){
				m_log.debug("jobinfo:" + job.getId().getSessionId() +" "+ job.getId().getJobunitId() +" "+ job.getId().getJobId());
				m_log.debug("appreqinfo:" + job.getApprovalReqRoleId() +" "+ job.getApprovalReqUserId() + " status:" + status);
				m_log.debug("userinfo:" + loginUser + " isAdmin:"+ isAdministrator + " isPrivilege:" + isApprovalPrivilege);
			}
				
			//承認画面一覧の１行を作成
			JobApprovalInfo info = new JobApprovalInfo(
					null,
					status,
					sessionNode.getApprovalResult()==null?null:sessionNode.getApprovalResult(),
					job.getId().getSessionId(),
					job.getId().getJobunitId(),
					job.getId().getJobId(),
					job.getJobName(),
					sessionNode.getApprovalRequestUser(),
					sessionNode.getApprovalUser(),
					sessionNode.getStartDate()==null?null:sessionNode.getStartDate(),
					sessionNode.getEndDate()==null?null:sessionNode.getEndDate(),
					job.getApprovalReqSentence(),
					sessionNode.getApprovalComment());
				list.add(info);
		}
		return list;
	}
	/**
	 * TypedQuery作成
	 *f
	 * @param property 一覧フィルタ用プロパティ
	 * @param statuslist 承認ステータスフィルタ指定リスト(置換済)
	 * @param isPending null:依頼先設定の条件指定無, true:"承認待"のみ指定(依頼先設定の条件指定有), false:"未承認"のみ指定(依頼先設定の条件指定有)
	 * @param roleIdList ログインユーザ所属先ロールリスト
	 * @param loginUser ログインユーザID
	 * @param limit 表示上限件数
	 * 
	 * @return typedQuery
	 */
	private TypedQuery<?> getApprovalFilterQuery(JobApprovalFilter property, List<Integer> statuslist,
													Boolean isPending, List<String> roleIdList, String loginUser, Integer limit) {

		m_log.debug("getApprovalFilterQuery()");
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			// 「含まない」検索を行うかの判断に使う値
			String notInclude = "NOT:";
			
			String[] userlist = {"*", loginUser};
			
			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT b");
			sbJpql.append(" FROM JobSessionJobEntity a, JobInfoEntity b, JobSessionNodeEntity c");
			sbJpql.append(" WHERE a.id.sessionId = b.id.sessionId");
			sbJpql.append(" AND a.id.jobunitId = b.id.jobunitId");
			sbJpql.append(" AND a.id.jobId = b.id.jobId");
			sbJpql.append(" AND a.id.sessionId = c.id.sessionId");
			sbJpql.append(" AND a.id.jobunitId = c.id.jobunitId");
			sbJpql.append(" AND a.id.jobId = c.id.jobId");
			sbJpql.append(" AND b.jobType = :jobType");
			
			// 承認処理を開始していない状態(待機中等)は表示対象としない
			sbJpql.append(" AND c.approvalStatus IS NOT NULL");
			
			if(property.getStartFromDate() != null) {
				sbJpql.append(" AND c.startDate >= :startFromDate");
			}
			if(property.getStartToDate() != null) {
				sbJpql.append(" AND c.startDate <= :startToDate");
			}
			if(property.getEndFromDate() != null) {
				sbJpql.append(" AND c.endDate >= :endFromDate");
			}
			if(property.getEndToDate() != null) {
				sbJpql.append(" AND c.endDate <= :endToDate");
			}
			
			//依頼先ロール/ユーザ設定をSQL条件に追加するか否かで、ステータスの条件指定も変更
			// ステータスは置換済みの情報(statuslist)を使用する
			if(isPending != null){
				//指定されたロール/ユーザリストに合致するものは"承認待"、合致しないものは"未承認"
				if (isPending){
					//"承認待"指定有、"未承認"指定無
					if (statuslist != null && statuslist.size() > 0) {
						//他のステータス指定有
						sbJpql.append(" AND ((c.approvalStatus = :approvalPendingStatus");
						sbJpql.append(" AND b.approvalReqRoleId IN (" + HinemosEntityManager.getParamNameString("approvalReqRoleId", new String[roleIdList.size()]) + ")");
						sbJpql.append(" AND b.approvalReqUserId IN (" + HinemosEntityManager.getParamNameString("approvalReqUserId", userlist) + "))");
						sbJpql.append(" OR c.approvalStatus IN (" + HinemosEntityManager.getParamNameString("approvalStatus", new String[statuslist.size()])+"))");
					}else{
						//"承認待"のみ指定
						sbJpql.append(" AND c.approvalStatus = :approvalPendingStatus");
						sbJpql.append(" AND b.approvalReqRoleId IN (" + HinemosEntityManager.getParamNameString("approvalReqRoleId", new String[roleIdList.size()]) + ")");
						sbJpql.append(" AND b.approvalReqUserId IN (" + HinemosEntityManager.getParamNameString("approvalReqUserId", userlist) + ")");
					}
				}else{
					//"承認待"指定無、"未承認"指定有
					if (statuslist != null && statuslist.size() > 0) {
						//他のステータス指定有
						sbJpql.append(" AND ((c.approvalStatus = :approvalPendingStatus");
						sbJpql.append(" AND (b.approvalReqRoleId NOT IN (" + HinemosEntityManager.getParamNameString("approvalReqRoleId", new String[roleIdList.size()]) + ")");
						sbJpql.append(" OR b.approvalReqUserId NOT IN (" + HinemosEntityManager.getParamNameString("approvalReqUserId", userlist) + ")))");
						sbJpql.append(" OR c.approvalStatus IN (" + HinemosEntityManager.getParamNameString("approvalStatus", new String[statuslist.size()])+"))");
					}else{
						//"未承認"のみ指定
						sbJpql.append(" AND c.approvalStatus = :approvalPendingStatus");
						sbJpql.append(" AND (b.approvalReqRoleId NOT IN (" + HinemosEntityManager.getParamNameString("approvalReqRoleId", new String[roleIdList.size()]) + ")");
						sbJpql.append(" OR b.approvalReqUserId NOT IN (" + HinemosEntityManager.getParamNameString("approvalReqUserId", userlist) + "))");
					}
				}
			}else{
				//依頼先ロール/ユーザ設定の条件追加不要("承認待"/"未承認"の区別不要)な場合は、ステータスのみ条件指定
				//"未承認"以外でDBで持ち得るステータス(承認待/中断/取り下げ/承認済)全て指定されている場合は、ステータスを条件に指定しない
				if (statuslist != null && statuslist.size() > 0 && statuslist.size() != 4) {
					sbJpql.append(" AND c.approvalStatus IN (" + HinemosEntityManager.getParamNameString("approvalStatus", new String[statuslist.size()])+")");
				}
			}
			
			if(property.getResult() != null && property.getResult() != -1) {
				sbJpql.append(" AND c.approvalResult = :approvalResult");
			}
			if(property.getSessionId() != null && property.getSessionId().length() > 0) {
				if(!property.getSessionId().startsWith(notInclude)) {
					sbJpql.append(" AND a.id.sessionId like :sessionId");
				}else{
					sbJpql.append(" AND a.id.sessionId not like :sessionId");
				}
			}
			if(property.getJobunitId() != null && property.getJobunitId().length() > 0) {
				if(!property.getJobunitId().startsWith(notInclude)) {
					sbJpql.append(" AND a.id.jobunitId like :jobunitId");
				}else{
					sbJpql.append(" AND a.id.jobunitId not like :jobunitId");
				}
			}
			if(property.getJobId() != null && property.getJobId().length() > 0) {
				if(!property.getJobId().startsWith(notInclude)) {
					sbJpql.append(" AND a.id.jobId like :jobId");
				}else{
					sbJpql.append(" AND a.id.jobId not like :jobId");
				}
			}
			if(property.getJobName() != null && property.getJobName().length() > 0) {
				if(!property.getJobName().startsWith(notInclude)) {
					sbJpql.append(" AND b.jobName like :jobName");
				}else{
					sbJpql.append(" AND b.jobName not like :jobName");
				}
			}
			if(property.getRequestUser() != null && property.getRequestUser().length() > 0) {
				if(!property.getRequestUser().startsWith(notInclude)) {
					sbJpql.append(" AND c.approvalRequestUser like :approvalRequestUser");
				}else{
					sbJpql.append(" AND c.approvalRequestUser not like :approvalRequestUser");
				}
			}
			if(property.getApprovalUser() != null && property.getApprovalUser().length() > 0) {
				if(!property.getApprovalUser().startsWith(notInclude)) {
					sbJpql.append(" AND c.approvalUser like :approvalUser");
				}else{
					sbJpql.append(" AND c.approvalUser not like :approvalUser");
				}
			}
			if(property.getRequestSentence() != null && property.getRequestSentence().length() > 0) {
				if(!property.getRequestSentence().startsWith(notInclude)) {
					sbJpql.append(" AND b.approvalReqSentence like :approvalReqSentence");
				}else{
					sbJpql.append(" AND b.approvalReqSentence not like :approvalReqSentence");
				}
			}
			if(property.getComment() != null && property.getComment().length() > 0) {
				if(!property.getComment().startsWith(notInclude)) {
					sbJpql.append(" AND c.approvalComment like :approvalComment");
				}else{
					sbJpql.append(" AND c.approvalComment not like :approvalComment");
				}
			}
			
			// オブジェクト権限チェック
			sbJpql = getJpql(sbJpql);
			
			sbJpql.append(" ORDER BY c.approvalStatus DESC, a.id.sessionId DESC");
			m_log.debug("getApprovalFilterQuery() jpql = " + sbJpql.toString());
			
			TypedQuery<?> typedQuery = null;
			typedQuery = em.createQuery(sbJpql.toString(), JobInfoEntity.class);
			
			typedQuery = typedQuery.setParameter("jobType", JobConstant.TYPE_APPROVALJOB);
			
			if(property.getStartFromDate() != null) {
				typedQuery = typedQuery.setParameter("startFromDate", property.getStartFromDate());
			}
			if(property.getStartToDate() != null) {
				typedQuery = typedQuery.setParameter("startToDate", property.getStartToDate());
			}
			if(property.getEndFromDate() != null) {
				typedQuery = typedQuery.setParameter("endFromDate", property.getEndFromDate());
			}
			if(property.getEndToDate() != null) {
				typedQuery = typedQuery.setParameter("endToDate", property.getEndToDate());
			}
			
			//依頼先ロール/ユーザ設定をSQL条件に追加するか否かで、ステータスの条件指定も変更
			if(isPending != null) {
				//指定されたロール/ユーザリストに合致するものは"承認待"、合致しないものは"未承認"
				typedQuery = typedQuery.setParameter("approvalPendingStatus", JobApprovalStatusConstant.TYPE_PENDING);
				int count = roleIdList.size();
				if (count > 0) {
					for (int i = 0 ; i < count ; i++) {
						typedQuery = typedQuery.setParameter("approvalReqRoleId" + i, roleIdList.toArray()[i]);
					}
				}
				count = userlist.length;
				if (count > 0) {
					for (int i = 0 ; i < count ; i++) {
						typedQuery = typedQuery.setParameter("approvalReqUserId" + i, userlist[i]);
					}
				}
				if (statuslist != null && statuslist.size() > 0){
					count = statuslist.size();
					if (count > 0) {
						for (int i = 0 ; i < count ; i++) {
							typedQuery = typedQuery.setParameter("approvalStatus" + i, statuslist.toArray()[i]);
						}
					}
				}
			}else{
				//依頼先ロール/ユーザ設定の条件追加不要("承認待"/"未承認"の区別不要)の場合は、ステータスのみ条件指定
				//"未承認"以外でDBで持ち得るステータス(承認待/中断/取り下げ/承認済)全て指定されている場合は、ステータスを条件に指定しない
				if (statuslist != null && statuslist.size() > 0 && statuslist.size() != 4) {
					int count = statuslist.size();
					if (count > 0) {
						for (int i = 0 ; i < count ; i++) {
							typedQuery = typedQuery.setParameter("approvalStatus" + i, statuslist.toArray()[i]);
						}
					}
				}
			}
			
			if(property.getResult() != null && property.getResult() != -1) {
				typedQuery = typedQuery.setParameter("approvalResult", property.getResult());
			}
			if(property.getSessionId() != null && property.getSessionId().length() > 0) {
				if(!property.getSessionId().startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("sessionId",
							QueryDivergence.escapeLikeCondition(property.getSessionId()));
				}else{
					typedQuery = typedQuery.setParameter("sessionId",
							QueryDivergence.escapeLikeCondition(property.getSessionId().substring(notInclude.length())));
				}
			}
			if(property.getJobunitId() != null && property.getJobunitId().length() > 0) {
				if(!property.getJobunitId().startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("jobunitId",
							QueryDivergence.escapeLikeCondition(property.getJobunitId()));
				}else{
					typedQuery = typedQuery.setParameter("jobunitId",
							QueryDivergence.escapeLikeCondition(property.getJobunitId().substring(notInclude.length())));
				}
			}
			if(property.getJobId() != null && property.getJobId().length() > 0) {
				if(!property.getJobId().startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("jobId",
							QueryDivergence.escapeLikeCondition(property.getJobId()));
				}else{
					typedQuery = typedQuery.setParameter("jobId",
							QueryDivergence.escapeLikeCondition(property.getJobId().substring(notInclude.length())));
				}
			}
			if(property.getJobName() != null && property.getJobName().length() > 0) {
				if(!property.getJobName().startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("jobName",
							QueryDivergence.escapeLikeCondition(property.getJobName()));
				}else{
					typedQuery = typedQuery.setParameter("jobName",
							QueryDivergence.escapeLikeCondition(property.getJobName().substring(notInclude.length())));
				}
			}
			if(property.getRequestUser() != null && property.getRequestUser().length() > 0) {
				if(!property.getRequestUser().startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("approvalRequestUser",
							QueryDivergence.escapeLikeCondition(property.getRequestUser()));
				}else{
					typedQuery = typedQuery.setParameter("approvalRequestUser",
							QueryDivergence.escapeLikeCondition(property.getRequestUser().substring(notInclude.length())));
				}
			}
			if(property.getApprovalUser() != null && property.getApprovalUser().length() > 0) {
				if(!property.getApprovalUser().startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("approvalUser",
							QueryDivergence.escapeLikeCondition(property.getApprovalUser()));
				}else{
					typedQuery = typedQuery.setParameter("approvalUser",
							QueryDivergence.escapeLikeCondition(property.getApprovalUser().substring(notInclude.length())));
				}
			}
			if(property.getRequestSentence() != null && property.getRequestSentence().length() > 0) {
				if(!property.getRequestSentence().startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("approvalReqSentence",
							QueryDivergence.escapeLikeCondition(property.getRequestSentence()));
				}else{
					typedQuery = typedQuery.setParameter("approvalReqSentence",
							QueryDivergence.escapeLikeCondition(property.getRequestSentence().substring(notInclude.length())));
				}
			}
			if(property.getComment() != null && property.getComment().length() > 0) {
				if(!property.getComment().startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("approvalComment",
							QueryDivergence.escapeLikeCondition(property.getComment()));
				}else{
					typedQuery = typedQuery.setParameter("approvalComment",
							QueryDivergence.escapeLikeCondition(property.getComment().substring(notInclude.length())));
				}
			}
			
			// オブジェクト権限チェック
			typedQuery = setObjectPrivilegeParameter(typedQuery);
			
			if (limit != null) {
				typedQuery = typedQuery.setMaxResults(limit);
			}
			
			return typedQuery;
		}
	}
	
	/**
	 * RPAシナリオジョブ（直接実行）のログイン解像度一覧を取得します。
	 * 
	 * @return ログイン解像度のリスト
	 */
	public List<JobRpaLoginResolutionMstEntity> getRpaLoginResolutionList() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			return QueryUtil.getJobRpaLoginResolution();
		}
	}

	/**
	 * RPAシナリオジョブのスクリーンショットを検索します。
	 * ファイルデータは含まれません。
	 * 
	 * @return スクリーンショット情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<RpaJobScreenshot> getRpaScreenshotList(String sessionId, String jobunitId, String jobId, String facilityId) throws ObjectPrivilege_InvalidRole {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			m_log.debug("getRpaScreenshot() sessionId=" + sessionId
					+ ", jobunitId=" + jobunitId 
					+ ", jobId=" + jobId 
					+ ", facilityId=" + facilityId);

			List<JobRpaScreenshotEntity> entities = QueryUtil.getJobRpaScreenshot(sessionId, jobunitId, jobId, facilityId);
			List<RpaJobScreenshot> dtoList = new ArrayList<>();
			for (JobRpaScreenshotEntity entity : entities) {
				RpaJobScreenshot dto = new RpaJobScreenshot();
				dto.setSessionId(entity.getId().getSessionId());
				dto.setJobunitId(entity.getId().getJobunitId());
				dto.setJobId(entity.getId().getJobId());
				dto.setFacilityId(entity.getId().getFacilityId());
				dto.setOutputDate(entity.getId().getOutputDate());
				dto.setDescription(entity.getDescription());
				// ファイルデータは含まない
				dtoList.add(dto);
			}
			return dtoList;
		}
	}

	/**
	 * RPAシナリオジョブのスクリーンショットを検索します。
	 * 
	 * @return スクリーンショット情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public RpaJobScreenshot getRpaScreenshot(String sessionId, String jobunitId, String jobId, String facilityId, Long regDate) throws ObjectPrivilege_InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			m_log.debug("getRpaScreenshot() sessionId=" + sessionId
					+ ", jobunitId=" + jobunitId 
					+ ", jobId=" + jobId 
					+ ", facilityId=" + facilityId);
			HinemosEntityManager em = jtm.getEntityManager();
			JobRpaScreenshotEntity entity = em.find(JobRpaScreenshotEntity.class,
					new JobRpaScreenshotEntityPK(sessionId, jobunitId, jobId, facilityId, regDate), ObjectPrivilegeMode.READ);
			RpaJobScreenshot dto = new RpaJobScreenshot();
			dto.setSessionId(entity.getId().getSessionId());
			dto.setJobunitId(entity.getId().getJobunitId());
			dto.setJobId(entity.getId().getJobId());
			dto.setFacilityId(entity.getId().getFacilityId());
			dto.setOutputDate(entity.getId().getOutputDate());
			dto.setFiledata(entity.getFiledata());
			dto.setDescription(entity.getDescription());
			return dto;
		}
	}
}
