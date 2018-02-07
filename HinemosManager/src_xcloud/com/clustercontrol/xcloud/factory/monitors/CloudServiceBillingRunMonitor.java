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

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
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
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
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

	public static final String monitorTypeId = "MON_CLOUD_SERVICE_BILLING";
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
				notifyException("", e);
				collectErrorSample("", CloudUtil.Priority.FAILURE.type);
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
				collectErrorSample(null, CollectedDataErrorTypeConstant.UNKNOWN);
				throw new HinemosUnknown(message);
			}
			
			String target = String.format("(%s) %s", platform,service);

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
				notifyBilling(target, billings);
				
				if (m_monitor.getPredictionFlg() || m_monitor.getChangeFlg()) {
					for (Map.Entry<String, IBillings.PlatformServiceBilling> entry: billings.entrySet()) {

						// 将来予測監視、変化量監視の処理を行う
						CollectMonitorDataInfo collectMonitorDataInfo
							= CollectMonitorManagerUtil.calculateChangePredict(null, m_monitor, entry.getKey(),
							null, m_monitor.getItemName(), entry.getValue().getUpdateDate(), entry.getValue().getPrice());

						// 将来予測もしくは変更点監視が有効な場合、通知を行う
						Double average = null;
						Double standardDeviation = null;
						if (collectMonitorDataInfo != null) {
							if (collectMonitorDataInfo.getChangeMonitorRunResultInfo() != null) {
								// 変化量監視の通知
								MonitorRunResultInfo collectResult = collectMonitorDataInfo.getChangeMonitorRunResultInfo();
								ret.add(createOutputBasicInfo(true, entry.getKey(), collectResult.getCheckResult(), 
										new Date(collectResult.getNodeDate()), collectResult, m_monitor));
							}
							if (collectMonitorDataInfo.getPredictionMonitorRunResultInfo() != null) {
								// 将来予測監視の通知
								MonitorRunResultInfo collectResult = collectMonitorDataInfo.getPredictionMonitorRunResultInfo();
								ret.add(createOutputBasicInfo(true, entry.getKey(), collectResult.getCheckResult(), 
										new Date(collectResult.getNodeDate()), collectResult, m_monitor));
							}
							average = collectMonitorDataInfo.getAverage();
							standardDeviation = collectMonitorDataInfo.getStandardDeviation();
						}
						logger.debug("average=" + average+ ", standardDeviation=" + standardDeviation);
					}
				}
				
				ts.complete();
			} catch(RuntimeException | CloudManagerException | HinemosUnknown e) {
				notifyException(target, e);
				collectErrorSample(target, CloudUtil.Priority.UNKNOWN.type);
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
	
	protected void notify(OutputBasicInfo output) {
		// 監視結果通知
		if (logger.isDebugEnabled()) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			logger.debug(String.format("notify : monitorTypeId = %s, monitorId = %s, facilityId = %s, target = %s, priority = %s, message = %s, date = %s",
					m_monitorTypeId, m_monitorId, m_facilityId, output.getSubKey(), PriorityConstant.typeToMessageCode(output.getPriority()), output.getMessageOrg(), format.format(output.getGenerationDate())));
		}
		
		if (m_monitor.getMonitorFlg()) {
			try (JpaTransactionManager jtm = new JpaTransactionManager()) {
				output.setNotifyGroupId(m_monitor.getNotifyGroupId());
				// 通知設定
				jtm.addCallback(new NotifyCallback(output));
			} catch (Exception e) {
				notifyException(output.getSubKey(), e);
			}
		}
	}
	
	protected void notifyException(String target, Exception exception) {
		Date d = new Date();
		String message = CloudMessageConstant.MONITOR_PLATFORM_BILLING_SERVICE_NOTIFY_UNKNOWN.getMessage(exception.getMessage());
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String messageOrg = CloudMessageConstant.MONITOR_PLATFORM_BILLING_SERVICE_NOTIFY_ORG_UNKNOWN.getMessage(format.format(d.getTime()), CloudMessageUtil.getExceptionStackTrace(exception));
		OutputBasicInfo output = CloudUtil.createOutputBasicInfoEx(CloudUtil.Priority.UNKNOWN, monitorTypeId, m_monitorId,
				target, m_monitor.getApplication(), m_facilityId, message, messageOrg, d.getTime());
		notify(output);
	}
	
	protected void collect(Sample sample) {
		if (logger.isDebugEnabled()) {
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectWriter writer = objectMapper.writerFor(Sample.class);
			
			try {
				String sampleString = writer.writeValueAsString(sample);
				logger.debug(String.format("collect : monitorTypeId = %s, monitorId = %s, facilityId = %s, sample = %s",
						m_monitorTypeId, m_monitorId, m_facilityId, sampleString));
			} catch(Exception e) {
				logger.warn(e.getMessage(), e);
			}
		}

		if (m_monitor.getCollectorFlg()) {
			CollectDataUtil.put(Arrays.asList(sample));
		}
	}
	
	protected void notifyBilling(String target, Map<String,IBillings.PlatformServiceBilling> billings) {
		for (Entry<String, PlatformServiceBilling> entry : billings.entrySet()) {
			
			String facilityId = entry.getKey();
			PlatformServiceBilling billing = entry.getValue();
			CloudUtil.Priority priority = checkPriorityRange(billing);
			OutputBasicInfo output;
			Sample sample;
			switch(priority.type) {
			case PriorityConstant.TYPE_INFO:
			case PriorityConstant.TYPE_WARNING:
			case PriorityConstant.TYPE_CRITICAL:
			{
				String message = String.format("%s : %f", target, billing.getPrice());
				output = CloudUtil.createOutputBasicInfoEx(priority, monitorTypeId, m_monitorId,
						target, m_monitor.getApplication(), facilityId, message, message, billing.getUpdateDate() == null ? Long.valueOf(new Date().getTime()): billing.getUpdateDate());
				sample = new Sample(new Date(),m_monitorId);
				sample.set(facilityId, target, billing.getPrice(), CollectedDataErrorTypeConstant.NOT_ERROR);
				break;
			}
			default:
			{
				String message = String.format("%s : NaN", target);
				output = CloudUtil.createOutputBasicInfoEx(CloudUtil.Priority.UNKNOWN, monitorTypeId, m_monitorId,
						target, m_monitor.getApplication(), facilityId, message, message, billing.getUpdateDate() == null ? Long.valueOf(new Date().getTime()): billing.getUpdateDate());
				sample = new Sample(new Date(),m_monitorId);
				sample.set(facilityId, target, billing.getPrice(), CollectedDataErrorTypeConstant.UNKNOWN);
				break;
			}}
			notify(output);
			collect(sample);
		}
	}
	
	protected void notifyError(CloudUtil.Priority priority, String subKey, String message) {
		OutputBasicInfo output = CloudUtil.createOutputBasicInfoEx(priority, monitorTypeId, m_monitorId, subKey, m_monitor.getApplication(), m_facilityId, message, message, new Date().getTime());
		notify(output);
	}
	
	protected void collectErrorSample(String target, int errorType) {
		Sample sample = new Sample(new Date(),m_monitorId);
		sample.set(m_facilityId, target, 0.0, errorType);
		collect(sample);
	}
}
