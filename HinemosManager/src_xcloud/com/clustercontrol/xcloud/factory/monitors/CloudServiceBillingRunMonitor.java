/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory.monitors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageId;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfo;
import com.clustercontrol.monitor.plugin.model.PluginCheckInfo;
import com.clustercontrol.monitor.plugin.util.QueryUtil;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil.CollectMonitorDataInfo;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.HinemosCredential;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.CloudMessageConstant;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.IBillings;
import com.clustercontrol.xcloud.factory.IBillings.PlatformServiceBilling;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.ExtendedProperty;
import com.clustercontrol.xcloud.model.FacilityAdditionEntity;
import com.clustercontrol.xcloud.persistence.PersistenceUtil.TransactionScope;
import com.clustercontrol.xcloud.util.CloudMessageUtil;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class CloudServiceBillingRunMonitor extends RunMonitorNumericValueType {
	/** ログ出力のインスタンス。 */
	private static Log logger = LogFactory.getLog( CloudServiceBillingRunMonitor.class );

	public static final String monitorTypeId = HinemosModuleConstant.MONITOR_CLOUD_SERVICE_BILLING;
	public static final int monitorType = MonitorTypeConstant.TYPE_NUMERIC;
	public static final String STRING_CLOUD_SERVICE_BILLING = CloudMessageConstant.CLOUDSERVICE_BILLING_MONITOR.getMessage();
	
	public static final String key_platform = "platform";
	public static final String key_service = "service";
	
	/** 監視情報 */
	private PluginCheckInfo m_plugin = null;

	/** 文字列情報リスト */
	private List<MonitorPluginStringInfo> m_monitorPluginStringInfoList;
	
	/** メッセージ **/
	private String m_message = null;

	/**
	 * コンストラクタ
	 */
	public CloudServiceBillingRunMonitor() {
		super();
	}
	
	@Override
	protected List<OutputBasicInfo> runMonitorInfo() throws FacilityNotFound, MonitorNotFound, EntityExistsException, InvalidRole, HinemosUnknown {
		logger.debug("runMonitorInfo()");

		List<OutputBasicInfo> ret = new ArrayList<>();
		m_now = HinemosTime.getDateInstance();

		// 監視基本情報を設定
		if (!setMonitorInfo(m_monitorTypeId, m_monitorId)) {
			// 処理終了
			return ret;
		}

		// 判定情報を設定
		setJudgementInfo();

		// チェック条件情報を設定
		setCheckInfo();

		// ノードまたはスコープの判別
		m_isNode = new RepositoryControllerBean().isNode(m_facilityId);
		
		if (!m_monitor.getMonitorFlg() && !m_monitor.getCollectorFlg())
			return ret;

		logger.debug(String.format("monitor start : monitorTypeId = %s, monitorId = %s", m_monitorTypeId, m_monitorId));
		ret = collect();
		logger.debug(String.format("monitor end : monitorTypeId = %s, monitorId = %s", m_monitorTypeId, m_monitorId));

		return ret;
	}

	protected List<OutputBasicInfo> collect() throws HinemosUnknown {
		
		List<OutputBasicInfo> ret = new ArrayList<>();

		try (SessionScope sessionScope = SessionScope.open()) {
			String adminUserId = HinemosPropertyCommon.xcloud_internal_thread_admin_user.getStringValue();
				
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, adminUserId);
			Session.current().setHinemosCredential(new HinemosCredential(adminUserId));
			try {
				HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, new AccessControllerBean().isAdministrator());
			} catch(Exception e) {
				notifyException(m_facilityId, "", e);
				throw new HinemosUnknown(e);
			}
			
			String platform = null;
			String service = null;
			for (MonitorPluginStringInfo entry: m_monitorPluginStringInfoList) {
				if (key_platform.equals(entry.getId().getKey())) {
					platform = entry.getValue();
				}
				if (key_service.equals(entry.getId().getKey())) {
					service = entry.getValue();
				}
			}
			if (platform == null || service == null) {
				logger.debug(String.format("Invalid target : monitorTypeId = %s, monitorId = %s", m_monitorTypeId, m_monitorId));

				String message = CloudMessageConstant.MONITOR_PLATFORM_SERVICE_BILLING_INVALID_TARGET.getMessage(m_monitorId);
				notifyError(CloudUtil.Priority.FAILURE, null, message);
				throw new HinemosUnknown(message);
			}
			
			String target = String.format("(%s) %s", platform,service);
			String notifyFacilityId = m_facilityId;

			// 新しいトランザクションとして作成。でないと、CollectDataUtil.put() > JdbcBatchExecutor.execute()における
			// tm.getEntityManager().unwrap(Connection.class)でNullが返され、NPEとなる
			try (TransactionScope ts = new TransactionScope()) {
				logger.debug("start checking " + m_monitorId);

				List<String> nodeFacilities;
				if (m_isNode) {
					nodeFacilities = Arrays.asList(m_facilityId);
				} else {
					nodeFacilities = RepositoryControllerBeanWrapper.bean().getNodeFacilityIdList(m_facilityId, m_monitor.getOwnerRoleId(), 0);
				}
				
				Map<String,IBillings.PlatformServiceBilling> billings = new HashMap<>();
				CloudManager cManager = CloudManager.singleton();
				HinemosEntityManager em = Session.current().getEntityManager();
				for (String facilityId : nodeFacilities) {
					notifyFacilityId = facilityId;
					FacilityAdditionEntity facilityAdditionEntity = em.find(FacilityAdditionEntity.class, facilityId, ObjectPrivilegeMode.READ);
					if (facilityAdditionEntity == null) {
						logger.debug(String.format("not manage a node by xcloud : facilityId = %s", facilityId));
						continue;
					}
					
					ExtendedProperty property = facilityAdditionEntity.getExtendedProperties().get(CloudConstants.EPROP_CloudScope);
					if (property == null) {
						logger.debug(String.format("node is not cloudscope : facilityId = %s", facilityId));
						continue;
					}
					
					CloudScopeEntity cloudScope = cManager.getCloudScopes().getCloudScope(property.getValue());
					
					IBillings.PlatformServiceBilling billing = cManager.getBillings().getPlatformServiceBilling(cloudScope, service);
					billings.put(facilityId, billing);
				}
				ret.addAll(createBillingOutputBasicInfo(target, billings));
				
				if (m_monitor.getCollectorFlg() || m_monitor.getPredictionFlg() || m_monitor.getChangeFlg()) {
					for (Map.Entry<String, IBillings.PlatformServiceBilling> entry: billings.entrySet()) {

						notifyFacilityId = entry.getKey();
						// 将来予測監視、変化量監視の処理を行う
						CollectMonitorDataInfo collectMonitorDataInfo
							= CollectMonitorManagerUtil.calculateChangePredict(null, m_monitor, notifyFacilityId,
							null, m_monitor.getItemName(), entry.getValue().getUpdateDate(), entry.getValue().getPrice());

						// 将来予測もしくは変更点監視が有効な場合、通知を行う
						Double average = null;
						Double standardDeviation = null;
						if (collectMonitorDataInfo != null) {
							if (collectMonitorDataInfo.getChangeMonitorRunResultInfo() != null) {
								// 変化量監視の通知
								MonitorRunResultInfo collectResult = collectMonitorDataInfo.getChangeMonitorRunResultInfo();
								ret.add(createOutputBasicInfo(true, notifyFacilityId, collectResult.getCheckResult(), 
										new Date(collectResult.getNodeDate()), collectResult, m_monitor));
							}
							if (collectMonitorDataInfo.getPredictionMonitorRunResultInfo() != null) {
								// 将来予測監視の通知
								MonitorRunResultInfo collectResult = collectMonitorDataInfo.getPredictionMonitorRunResultInfo();
								ret.add(createOutputBasicInfo(true, notifyFacilityId, collectResult.getCheckResult(), 
										new Date(collectResult.getNodeDate()), collectResult, m_monitor));
							}
							average = collectMonitorDataInfo.getAverage();
							standardDeviation = collectMonitorDataInfo.getStandardDeviation();
						}
						logger.debug("average=" + average+ ", standardDeviation=" + standardDeviation);
						
						if (m_monitor.getCollectorFlg()) {
							// 重複チェック用のフラグ
							boolean isDuplicate = false;
							Date targetDate = new Date(entry.getValue().getUpdateDate());
							// 収集項目キーを取得
							List<CollectKeyInfo> collectKeyInfoList = com.clustercontrol.collect.util.QueryUtil.getCollectKeyInfoListByMonitorId(m_monitor.getMonitorId());
							
							for (CollectKeyInfo collectKeyInfo : collectKeyInfoList) {
								// 指定の日時の収集情報を探す。endTimeの比較が「<」なので+1しておく。
								List<CollectData> collectData = com.clustercontrol.collect.util.QueryUtil.getCollectDataListOrderByTimeDesc(
										collectKeyInfo.getCollectorid(),
										entry.getValue().getUpdateDate(),
										entry.getValue().getUpdateDate() + 1);
								
								if (collectData.size() > 0) {
									// 重複データを確認
									isDuplicate = true;
									logger.debug("This key (id : " + collectKeyInfo.getCollectorid() + ", date : "
											+ entry.getValue().getUpdateDate() + " ) exists. Not collect.");
									break;
								}
							}
							
							// 重複していなかったらデータ投入
							if (!isDuplicate) {
								Sample sample = new Sample(targetDate, m_monitorId);
								sample.set(notifyFacilityId, m_monitor.getItemName(), entry.getValue().getPrice(), 
										average, standardDeviation,CollectedDataErrorTypeConstant.NOT_ERROR);
								CollectDataUtil.put(Arrays.asList(sample));
							}
						}
					}
				}
				
				ts.complete();
			} catch(RuntimeException | CloudManagerException | HinemosUnknown e) {
				collectErrorSample(notifyFacilityId, m_monitor.getItemName(), CloudUtil.Priority.UNKNOWN.type);
				notifyException(notifyFacilityId, target, e);
				throw new HinemosUnknown(e);
			}
			return ret;
		}
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
	
	protected boolean checkInfoRange(double value) {
		MonitorJudgementInfo info = m_judgementInfoList.get(Integer.valueOf(PriorityConstant.TYPE_INFO));
		return info.getThresholdLowerLimit() <= value && value < info.getThresholdUpperLimit();
	}
	
	protected boolean checkWarnRange(double value) {
		MonitorJudgementInfo warn = m_judgementInfoList.get(Integer.valueOf(PriorityConstant.TYPE_WARNING));
		return warn.getThresholdLowerLimit() <= value && value < warn.getThresholdUpperLimit();
	}
	
	protected boolean checkCriticalRange(double value) {
		MonitorJudgementInfo warn = m_judgementInfoList.get(Integer.valueOf(PriorityConstant.TYPE_WARNING));
		return warn.getThresholdUpperLimit() <= value;
	}

	protected CloudUtil.Priority checkPriorityRange(PlatformServiceBilling billing) {
		if (billing.isValid()) {
			return checkInfoRange(billing.getPrice()) ? CloudUtil.Priority.INFO: (checkWarnRange(billing.getPrice()) ? CloudUtil.Priority.WARNING: (checkCriticalRange(billing.getPrice()) ? CloudUtil.Priority.CRITICAL: CloudUtil.Priority.UNKNOWN));
		} else {
			return CloudUtil.Priority.UNKNOWN;
		}
	}

	@Override
	protected void setCheckInfo() throws MonitorNotFound {
		// 監視情報を取得
		m_plugin = QueryUtil.getMonitorPluginInfoPK(m_monitorId);
		// 監視情報を設定
		m_monitorPluginStringInfoList = m_plugin.getMonitorPluginStringInfoList();
	}

	/**
	 *  マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 */
	@Override
	protected RunMonitor createMonitorInstance() throws HinemosUnknown {
		return new CloudServiceBillingRunMonitor();
	}

	/**
	 * メッセージID
	 */
	//@Override
	public String getMessageId(int key) {
		CloudUtil.Priority priority = CloudUtil.Priority.priority(key);
		return priority == null ? CloudUtil.MESSAGE_ID_UNKNOWN: priority.messageId;
	}
	
	/**
	 * メッセージ
	 */
	@Override
	public String getMessage(int key) {
		return m_message;
	}
	
	/**
	 * 通知実行
	 * 
	 * ※ジョブ連携メッセージIDは呼び出し元で設定すること
	 * 
	 * @param output
	 */
	protected void notify(OutputBasicInfo output) {
		// 監視結果通知
		if (logger.isDebugEnabled()) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			logger.debug(String.format("notify : monitorTypeId = %s, monitorId = %s, facilityId = %s, target = %s, priority = %s, message = %s, date = %s",
					m_monitorTypeId, m_monitorId, output.getFacilityId(), output.getSubKey(), PriorityConstant.typeToMessageCode(output.getPriority()), output.getMessageOrg(), format.format(output.getGenerationDate())));
		}
		
		if (m_monitor.getMonitorFlg()) {
			try (JpaTransactionManager jtm = new JpaTransactionManager()) {
				output.setNotifyGroupId(m_monitor.getNotifyGroupId());
				// 通知設定
				jtm.addCallback(new NotifyCallback(output));
			} catch (Exception e) {
				notifyException(output.getFacilityId(), output.getSubKey(), e);
			}
		}
	}
	
	protected void notifyException(String facilityId, String target, Exception exception) {
		String message = CloudMessageConstant.MONITOR_PLATFORM_BILLING_SERVICE_NOTIFY_UNKNOWN.getMessage(exception.getMessage());
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String messageOrg = CloudMessageConstant.MONITOR_PLATFORM_BILLING_SERVICE_NOTIFY_ORG_UNKNOWN.getMessage(format.format(m_now.getTime()), CloudMessageUtil.getExceptionStackTrace(exception));
		OutputBasicInfo output = CloudUtil.createOutputBasicInfoEx(CloudUtil.Priority.UNKNOWN, monitorTypeId, m_monitorId,
				target, m_monitor.getApplication(), facilityId, message, messageOrg, m_now.getTime());
		output.setJoblinkMessageId(JobLinkMessageId.getId(NotifyTriggerType.MONITOR, monitorTypeId, m_monitorId));
		notify(output);
	}
	
	protected List<OutputBasicInfo> createBillingOutputBasicInfo(String target, Map<String,IBillings.PlatformServiceBilling> billings) {
		List<OutputBasicInfo> ret =  new ArrayList<>();
		for (Entry<String, PlatformServiceBilling> entry : billings.entrySet()) {
			
			String facilityId = entry.getKey();
			PlatformServiceBilling billing = entry.getValue();
			CloudUtil.Priority priority = checkPriorityRange(billing);
			OutputBasicInfo output;
			switch(priority.type) {
			case PriorityConstant.TYPE_INFO:
			case PriorityConstant.TYPE_WARNING:
			case PriorityConstant.TYPE_CRITICAL:
			{
				String message = String.format("%s : %f", target, billing.getPrice());
				output = CloudUtil.createOutputBasicInfoEx(priority, monitorTypeId, m_monitorId,
						target, m_monitor.getApplication(), facilityId, message, message, billing.getUpdateDate() == null ? Long.valueOf(m_now.getTime()): billing.getUpdateDate());
				break;
			}
			default:
			{
				String message = String.format("%s : NaN", target);
				output = CloudUtil.createOutputBasicInfoEx(CloudUtil.Priority.UNKNOWN, monitorTypeId, m_monitorId,
						target, m_monitor.getApplication(), facilityId, message, message, billing.getUpdateDate() == null ? Long.valueOf(m_now.getTime()): billing.getUpdateDate());
				break;
			}}
			output.setNotifyGroupId(m_monitor.getNotifyGroupId());
			output.setJoblinkMessageId(JobLinkMessageId.getId(NotifyTriggerType.MONITOR, monitorTypeId, m_monitorId));
			ret.add(output);
		}
		return ret;
	}
	
	protected void notifyError(CloudUtil.Priority priority, String subKey, String message) {
		OutputBasicInfo output = CloudUtil.createOutputBasicInfoEx(priority, monitorTypeId, m_monitorId, subKey, m_monitor.getApplication(), m_facilityId, message, message, m_now.getTime());
		output.setJoblinkMessageId(JobLinkMessageId.getId(NotifyTriggerType.MONITOR, monitorTypeId, m_monitorId));
		notify(output);
	}
	
	private void collectErrorSample(String facilityId, String target, int errorType) {
		Sample sample = new Sample(HinemosTime.getDateInstance(), m_monitorId);
		sample.set(facilityId, target, null, errorType);

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			CollectDataUtil.put(Arrays.asList(sample));
			jtm.commit();
		} catch(Exception e) {
			//findbugs 対応 nullチェック追加
			if (jtm != null) {
				jtm.rollback();
			}
			logger.warn(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}

		// debugログ出力
		if (logger.isDebugEnabled()) {
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectWriter writer = objectMapper.writerFor(Sample.class);
			
			try {
				String sampleString = writer.writeValueAsString(sample);
				logger.debug(String.format("collect : monitorTypeId = %s, monitorId = %s, facilityId = %s, sample = %s",
						m_monitorTypeId, m_monitorId, facilityId, sampleString));
			} catch(Exception e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}
}
