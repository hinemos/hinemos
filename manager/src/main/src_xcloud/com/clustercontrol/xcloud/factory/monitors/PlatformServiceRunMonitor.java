/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory.monitors;

import static com.clustercontrol.xcloud.util.CloudMessageUtil.createCloudServiceMonitorExceptionMessage;
import static com.clustercontrol.xcloud.util.CloudMessageUtil.createCloudServiceMonitorExceptionMessageOrg;
import static com.clustercontrol.xcloud.util.CloudMessageUtil.createCloudServiceMonitorExceptionMessageOrg2;
import static com.clustercontrol.xcloud.util.CloudMessageUtil.createCloudServiceMonitorMessage;
import static com.clustercontrol.xcloud.util.CloudMessageUtil.createCloudServiceMonitorMessageOrg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityExistsException;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfo;
import com.clustercontrol.monitor.plugin.model.PluginCheckInfo;
import com.clustercontrol.monitor.plugin.util.QueryUtil;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.bean.TruthConstant;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorTruthValueType;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.bean.PlatformServiceCondition;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.CloudMessageConstant;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.util.FacilityIdUtil;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;

/**
* リソース監視の閾値判定クラス
*
* @version 5.0.0
* @since 2.0.0
*/
public class PlatformServiceRunMonitor extends RunMonitorTruthValueType {
	public static final String monitorTypeId = "MON_CLOUD_SERVICE_CONDITION";
	public static final int monitorType = MonitorTypeConstant.TYPE_TRUTH;
	public static final String STRING_CLOUD_SERVICE_DONDITION = CloudMessageConstant.CLOUDSERVICE_CONDITION_MONITOR.getMessage();
	
	public static final String key_targets = "targets";
	
	/** 監視情報 */
	private PluginCheckInfo m_plugin = null;

	/** 文字列情報リスト */
	private List<MonitorPluginStringInfo> m_monitorPluginStringInfoList;
	
	private static Logger logger = Logger.getLogger(PlatformServiceRunMonitor.class);

	/**
	 * コンストラクタ
	 * 
	 */
	public PlatformServiceRunMonitor() {
		super();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.CallableTask
	 */
	@Override
	protected RunMonitor createMonitorInstance() {
		return new PlatformServiceRunMonitor();
	}

	/**
	 * [リソース監視用]監視を実行します。（並列処理）
	 * 
	 * リソース監視では1つのファシリティIDに対して、複数の収集項目ID及び、デバイスに対するリソースを監視・収集します。
	 * この動作に対応するため、独自のrunMonitorInfoを実装します。
	 * 
	 */
	@Override
	protected List<OutputBasicInfo> runMonitorInfo() throws FacilityNotFound, MonitorNotFound, EntityExistsException, InvalidRole, HinemosUnknown {
		logger.debug("runMonitorInfo()");

		// FIXME
		// 現状の実装だとdeadlockの可能性がある。
		// collectメソッド内のnotifyをret.addに変更すること！
		List<OutputBasicInfo> ret = new ArrayList<>();
		m_now = new Date(System.currentTimeMillis());

		// 監視基本情報を設定
		if (!setMonitorInfo(m_monitorTypeId, m_monitorId)) {
			// 処理終了
			return ret;
		}

		// 判定情報を設定
		setJudgementInfo();

		// チェック条件情報を設定
		setCheckInfo();

		// 対象は、ノードのみ。
		m_isNode = new RepositoryControllerBean().isNode(m_facilityId);

		logger.debug("monitor start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);
		
		// 監視結果取得。
		List<OutputBasicInfo> outputs = collectTargets(m_facilityId);

		// 監視結果通知
		if (m_monitor.getMonitorFlg()) {
			for (OutputBasicInfo output: outputs) {
				output.setNotifyGroupId(m_monitor.getNotifyGroupId());
				// 通知設定
				new JpaTransactionManager().addCallback(new NotifyCallback(output));
			}
		}

		return ret;
	}

	/**
	 * リソース監視はcollectList()でファシリティ毎の処理を動作するように変更
	 */
	@Override
	public boolean collect(String facilityId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMessageOrg(int key) {
		throw new UnsupportedOperationException();
	}
	
	private static List<String> recursiveCollectScopes(FacilityTreeItem treeItem, String targetId) {
		if (targetId.equals(treeItem.getData().getFacilityId()) && treeItem.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE) {
			return Arrays.asList(targetId);
		} else {
			for (FacilityTreeItem fti: treeItem.getChildren()) {
				List<String> result = recursiveCollectScopes(fti, targetId);
				if (!result.isEmpty()) {
					List<String> list = new ArrayList<>();
					list.add(fti.getData().getFacilityId());
					list.addAll(result);
					return list;
				}
			}
		}
		return Collections.emptyList();
	}

	/**
	 * 監視の実態。対象ノードのfacilityId毎に呼ばれる
	 * @throws HinemosUnknown 
	 * 
	 */
	public List<OutputBasicInfo> collectTargets(String facilityId) throws FacilityNotFound, HinemosUnknown {
		try (SessionScope sessionScope = SessionScope.open()) {
			if (m_isNode)
				return Arrays.asList(createFailureOutputBasicInfo(
						facilityId,
						ErrorCode.MONITOR_CLOUDSERVICE_ONLY_NODE.getMessage(),
						ErrorCode.MONITOR_CLOUDSERVICE_ONLY_NODE.getMessage(),
						new Date().getTime()));
			
			FacilityTreeItem treeItem = RepositoryControllerBeanWrapper.bean().getFacilityTree(m_monitor.getOwnerRoleId(), Locale.getDefault());
			List<String> results = recursiveCollectScopes(treeItem, facilityId);
			
			if (results.isEmpty())
				throw new InternalManagerError(String.format("No found Scope. facilityId=%s", facilityId));
			
			// 最終地点は、パブリックあるいはプライベートのルート
			Object rootId = results.get(0);
			if (!CloudConstants.privateRootId.equals(rootId) && !CloudConstants.publicRootId.equals(rootId))
				throw new InternalManagerError(String.format("Invalid facilityId. facilityId = %s", facilityId));
			
			// クラウドスコープのスコープ Id を取得。
			Object cloudScopeScopeId = facilityId;
			if (results.size() > 1)
				cloudScopeScopeId = results.get(1); 
			
			// スコープ Id に該当するクラウドスコープを取得。
			CloudScopeEntity cloudScopeEntity = null;
			for (CloudScopeEntity cs: CloudManager.singleton().getCloudScopes().getAllCloudScopes()) {
				if (FacilityIdUtil.getCloudScopeScopeId(cs).equals(cloudScopeScopeId)) {
					cloudScopeEntity = cs;
					break;
				}
			}
			
			List<OutputBasicInfo> outputs = new ArrayList<>();
			if (cloudScopeEntity == null) {
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("scope in not cloudScope. facilityId = %s", facilityId));
				}
				return outputs;
			}
			
			// サービスの観測値を取得
			List<PlatformServiceCondition> conditions = Collections.emptyList();
			if (FacilityIdUtil.getCloudScopeScopeId(cloudScopeEntity).equals(facilityId)) {
				conditions = CloudManager.singleton().getCloudScopes().getPlatformServiceConditions(cloudScopeEntity.getId());
			} else {
				for (LocationEntity location: cloudScopeEntity.getLocations()) {
					if (FacilityIdUtil.getLocationScopeId(cloudScopeEntity.getId(), location).equals(facilityId)) {
						conditions = CloudManager.singleton().getCloudScopes().getPlatformServiceConditions(cloudScopeEntity.getId(), location.getLocationId());
						break;
					}
				}
			}
			if (conditions.isEmpty()) {
				return Arrays.asList(createFailureOutputBasicInfo(
						facilityId,
						ErrorCode.MONITOR_CLOUDSERVICE_FOUND_NO_SERVICEID.getMessage(),
						ErrorCode.MONITOR_CLOUDSERVICE_FOUND_NO_SERVICEID.getMessage(),
						new Date().getTime()));
			}
		
			// ターゲット取得
			Set<String> serviceIds = new TreeSet<>();
			for (MonitorPluginStringInfo entry: m_monitorPluginStringInfoList) {
				if (key_targets.equals(entry.getId().getKey())) {
					String[] targets = entry.getValue().split(",");
					serviceIds.addAll(Arrays.asList(targets));
					break;
				}
			}
	
			if (serviceIds.isEmpty()) {
				return Arrays.asList(createFailureOutputBasicInfo(
						facilityId,
						ErrorCode.MONITOR_CLOUDSERVICE_FOUND_NO_SERVICEID.getMessage(),
						ErrorCode.MONITOR_CLOUDSERVICE_FOUND_NO_SERVICEID.getMessage(),
						new Date().getTime()));
			}
			
			for (PlatformServiceCondition condition: conditions) {
				if (!serviceIds.contains(condition.getId()))
					continue;
				
				serviceIds.remove(condition.getId());
				
				switch (condition.getStatus()) {
				case normal:
					outputs.add(createAvailableOutputBasicInfo(
						facilityId,
						cloudScopeEntity.getPlatform().getName(),
						condition.getId(),
						condition.getServiceName(),
						condition.getMessage(),
						(condition.getDetail() == null && condition.getDetail().isEmpty()) ? condition.getMessage(): condition.getMessage() + ": " + condition.getDetail(),
						condition.getRecordDate()
						));
					break;
				case warn:
				case abnormal:
					outputs.add(createUnavailableOutputBasicInfo(
						facilityId,
						cloudScopeEntity.getPlatform().getName(),
						condition.getId(),
						condition.getServiceName(),
						condition.getMessage(),
						(condition.getDetail() == null && condition.getDetail().isEmpty()) ? condition.getMessage(): condition.getMessage() + ": " + condition.getDetail(),
						condition.getRecordDate()
						));
					break;
				case unknown:
					outputs.add(createUnknownOutputBasicInfo(
						facilityId,
						cloudScopeEntity.getPlatform().getName(),
						condition.getId(),
						condition.getServiceName(),
						condition.getMessage(),
						(condition.getDetail() == null && condition.getDetail().isEmpty()) ? condition.getMessage(): condition.getMessage() + ": " + condition.getDetail(),
						condition.getRecordDate()
						));
					break;
				case exception:
					outputs.add(createExceptionOutputBasicInfo(
						facilityId,
						cloudScopeEntity.getPlatform().getName(),
						condition.getId(),
						condition.getServiceName(),
						condition.getMessage(),
						condition.getDetail(),
						condition.getRecordDate()
						));
					break;
				}
			}
	
			return outputs;
		} catch (CloudManagerException e) {
			return Arrays.asList(createExceptionOutputBasicInfo2(facilityId, e, new Date().getTime()));
		}
	}

	/**
	 * 監視設定をローカル設定にセット
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {
		// 監視情報を取得
		m_plugin = QueryUtil.getMonitorPluginInfoPK(m_monitorId);
		// 監視情報を設定
		m_monitorPluginStringInfoList = m_plugin.getMonitorPluginStringInfoList();
	}
	
	protected OutputBasicInfo createOutputBasicInfo(CloudUtil.Priority priority, String facilityId, String subKey, String message, String messageOrg, Long generationDate) {
		return CloudUtil.createOutputBasicInfoEx(priority, monitorTypeId, m_monitorId, subKey, m_monitor.getApplication(), facilityId, message, messageOrg, generationDate);
	}
	
	protected OutputBasicInfo createFailureOutputBasicInfo(String facilityId, String message, String messageOrg, Long generationDate) {
		// ノードに紐づいた ID が取得できなかったため、エラーを返す。
		return createOutputBasicInfo(
				CloudUtil.Priority.priority(m_failurePriority),
				facilityId,
				"",
				message,
				messageOrg,
				generationDate
				);
	}

	protected OutputBasicInfo createAvailableOutputBasicInfo(String facilityId, String cloudPlatformName, String targetId, String targetName, String message, String messageOrg, Long generationDate) {
		MonitorJudgementInfo info = m_judgementInfoList.get(TruthConstant.TYPE_TRUE);
		return createFormattedOutputBasicInfo(CloudUtil.Priority.priority(info.getPriority()), CloudMessageConstant.CLOUDSERVICE_AVAILABLE.getMessage(), facilityId, cloudPlatformName, targetId, targetName, message, messageOrg, generationDate);
	}

	protected OutputBasicInfo createUnavailableOutputBasicInfo(String facilityId, String cloudPlatformName, String targetId, String targetName, String message, String messageOrg, Long generationDate) {
		MonitorJudgementInfo info = m_judgementInfoList.get(TruthConstant.TYPE_FALSE);
		return createFormattedOutputBasicInfo(CloudUtil.Priority.priority(info.getPriority()), CloudMessageConstant.CLOUDSERVICE_UNAVAILABLE.getMessage(), facilityId, cloudPlatformName, targetId, targetName, message, messageOrg, generationDate);
	}

	protected OutputBasicInfo createUnknownOutputBasicInfo(String facilityId, String cloudPlatformName, String targetId, String targetName, String message, String messageOrg, Long generationDate) {
		return createFormattedOutputBasicInfo(CloudUtil.Priority.UNKNOWN, CloudMessageConstant.CLOUDSERVICE_UNKNOWN.getMessage(), facilityId, cloudPlatformName, targetId, targetName, message, messageOrg, generationDate);
	}

	protected OutputBasicInfo createFormattedOutputBasicInfo(CloudUtil.Priority priority, String result, String facilityId, String cloudPlatformName, String targetId, String targetName, String message, String messageOrg, Long generationDate) {
		// ノードに紐づいた ID が取得できなかったため、エラーを返す。
		return createOutputBasicInfo(
				priority,
				facilityId,
				targetId,
				createCloudServiceMonitorMessage(result, cloudPlatformName, targetName, message),
				createCloudServiceMonitorMessageOrg(result, cloudPlatformName, targetName, messageOrg),
				generationDate
				);
	}
	
	protected OutputBasicInfo createExceptionOutputBasicInfo(String facilityId, String cloudPlatformName, String targetId, String targetName, String message, String messageOrg, Long generationDate) {
		// ノードに紐づいた ID が取得できなかったため、エラーを返す。
		return createFailureOutputBasicInfo(
				facilityId,
				createCloudServiceMonitorExceptionMessage(cloudPlatformName, targetName),
				createCloudServiceMonitorExceptionMessageOrg(cloudPlatformName, targetName, message, messageOrg),
				generationDate
				);
	}

	protected OutputBasicInfo createExceptionOutputBasicInfo2(String facilityId, Exception exception, Long generationDate) {
		// ノードに紐づいた ID が取得できなかったため、エラーを返す。
		return createFailureOutputBasicInfo(
				facilityId,
				CloudMessageConstant.CLOUDSERVICE_EXCEPTION.getMessage(),
				createCloudServiceMonitorExceptionMessageOrg2(exception),
				generationDate
				);
	}
}
