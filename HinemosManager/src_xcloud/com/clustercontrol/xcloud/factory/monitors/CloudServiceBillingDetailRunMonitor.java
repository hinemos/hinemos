/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory.monitors;

import static com.clustercontrol.xcloud.util.CloudMessageUtil.notifyDelta;
import static com.clustercontrol.xcloud.util.CloudMessageUtil.notifySum;
import static com.clustercontrol.xcloud.util.CloudMessageUtil.notifyUnknown;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityExistsException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil.CollectMonitorDataInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.xcloud.HinemosCredential;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.common.CloudMessageConstant;
import com.clustercontrol.xcloud.factory.IBillings;
import com.clustercontrol.xcloud.factory.IBillings.PlatformServiceBilling;
import com.clustercontrol.xcloud.model.BillingDetailEntity;
import com.clustercontrol.xcloud.model.BillingDetailMonitorStatusEntity;
import com.clustercontrol.xcloud.model.BillingDetailRelationEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;
import com.clustercontrol.xcloud.persistence.PersistenceUtil.TransactionScope;
import com.clustercontrol.xcloud.util.CloudMessageUtil;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class CloudServiceBillingDetailRunMonitor extends RunMonitorNumericValueType {
	/** ログ出力のインスタンス。 */
	private static Log logger = LogFactory.getLog( CloudServiceBillingDetailRunMonitor.class );

	public static final String monitorTypeId = "MON_CLOUD_SERVICE_BILLING_DETAIL";
	public static final int monitorType = MonitorTypeConstant.TYPE_NUMERIC;
	public static final String STRING_CLOUD_SERVICE_BILLING_DETAIL = CloudMessageConstant.CLOUDSERVICE_BILLING_DETAIL_MONITOR.getMessage();
	
	public static final String key_facilityType = "FacilityType";
	public static final String key_monitorKind = "MonitorKind";
	
	/** スコープ別 */
	public static final String PER_SCOPE = "scope";

	/** ノード別 */
	public static final String PER_NODE = "node";
	
	/** sum */
	public static final String sum = "sum";

	/** delta */
	public static final String delta = "delta";
	
	/** 監視情報 */
	private PluginCheckInfo m_plugin = null;

	/** 文字列情報リスト */
	private List<MonitorPluginStringInfo> m_monitorPluginStringInfoList;
	
	/** メッセージ **/
	private String m_message = null;

	/**
	 * コンストラクタ
	 */
	public CloudServiceBillingDetailRunMonitor() {
		super();
	}
	
	@Override
	protected List<OutputBasicInfo> runMonitorInfo() throws FacilityNotFound, MonitorNotFound, EntityExistsException, InvalidRole, HinemosUnknown {
		logger.debug("CloudServiceBillingDetailRunMonitor.runMonitorInfo()");

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
		
		//監視と収集の有効無効判定
		if (!m_monitor.getMonitorFlg() && !m_monitor.getCollectorFlg()){
			logger.debug(m_monitorId + " is disabled.");
			return ret;
		}

		logger.debug(String.format("monitor start : monitorTypeId = %s, monitorId = %s", m_monitorTypeId, m_monitorId));
		ret = collect();
		logger.debug(String.format("monitor end : monitorTypeId = %s, monitorId = %s", m_monitorTypeId, m_monitorId));

		return ret;
	}

	protected List<OutputBasicInfo> collect() throws HinemosUnknown {
		
		logger.debug("collect()");
		// FIXME
		// 現状の実装だとdeadlockの可能性がある。
		// collectメソッド内のnotifyをret.addに変更すること！
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
			
			String facilityType = null;
			String monitorKind = null;
			
			for (MonitorPluginStringInfo entry: m_monitorPluginStringInfoList) {
				if (key_facilityType.equals(entry.getId().getKey())) {
					facilityType = entry.getValue();
				}
				if (key_monitorKind.equals(entry.getId().getKey())) {
					monitorKind = entry.getValue();
				}
			}
			
			if (facilityType == null || !(facilityType.equals(PER_SCOPE) || facilityType.equals(PER_NODE))) {
				logger.debug(String.format("Invalid facility type: monitorTypeId=%s, monitorId=%s, facilityType=%s", m_monitorTypeId, m_monitorId , facilityType));
				
				String message = CloudMessageConstant.BILLINGALARM_NOTIFY_UNKNOWN_FACILITY_TYPE.getMessage(m_monitorId, facilityType);
				notifyError(CloudUtil.Priority.FAILURE, null, message);
				collectErrorSample(null, CollectedDataErrorTypeConstant.UNKNOWN);
				throw new HinemosUnknown(message);
			}
			
			if (monitorKind == null || !(monitorKind.equals(sum) || monitorKind.equals(delta))) {
				logger.debug(String.format("Invalid kind type: monitorTypeId=%s, monitorId=%s, monitorKind=%s", m_monitorTypeId, m_monitorId, monitorKind));
				
				String message = CloudMessageConstant.BILLINGALARM_NOTIFY_UNKNOWN_KIND_TYPE.getMessage(m_monitorId, monitorKind);
				notifyError(CloudUtil.Priority.FAILURE, null, message);
				collectErrorSample(null, CollectedDataErrorTypeConstant.UNKNOWN);
				throw new HinemosUnknown(message);
			}

			try (TransactionScope ts = new TransactionScope()) {
				logger.debug("start checking" + m_monitorId + ".");

				// NotifyGroupId が無いと無効。スキップして処理継続。
				if (m_monitor.getNotifyGroupId() == null || m_monitor.getNotifyGroupId().isEmpty()) {
					String message = m_monitorId + " is misssing notifyGroupId.";
					logger.error(message);
					throw new HinemosUnknown(message);
				}

				// 対象ファシリティの配下のファシリティリストを取得。
				List<String> nodeIds = new ArrayList<>();
				List<String> scopeIds = new ArrayList<>();
				try {
					FacilityTreeItem treeItem = RepositoryControllerBeanWrapper.bean().getFacilityTree(null, Locale.getDefault());

					FacilityTreeItem item = CloudUtil.searchFacility(treeItem, m_facilityId);
					if (item == null) {
						// 監視の対象なので、無いはずない！！
						throw new InternalManagerError();
					}
					
					CloudUtil.walkFacilityTree(null, item, new CloudUtil.IFacilityTreeVisitor() {
						@Override
						public void visitScope(FacilityTreeItem parent, FacilityTreeItem item) {
							scopeIds.add(item.getData().getFacilityId());
						}
						@Override
						public void visitNode(FacilityTreeItem parent, FacilityTreeItem item) {
							nodeIds.add(item.getData().getFacilityId());
						}
					});
				} catch (Exception e) {
					if (m_monitor.getMonitorFlg())
						notifyUnknown(m_monitor, e);
					
					if (m_monitor.getCollectorFlg()) {
						Sample sample = new Sample(new Date(), m_monitorId);
						sample.set(m_facilityId, monitorKind, 0d, CollectedDataErrorTypeConstant.UNKNOWN);
						CollectDataUtil.put(Arrays.asList(sample));
					}
					throw new HinemosUnknown(e);
				}

				HinemosEntityManager em = Session.current().getEntityManager();
				
				// 監視種別毎に処理を分岐。
				switch (monitorKind) {
				case sum:
					{
						// 月初の時刻を作成。
						Calendar beginning = Calendar.getInstance();
						beginning.setTime(new Date());
						beginning.set(Calendar.DAY_OF_MONTH, 1);
						beginning.set(Calendar.HOUR_OF_DAY, 0);
						beginning.set(Calendar.MINUTE, 0);
						beginning.set(Calendar.SECOND, 0);
						beginning.set(Calendar.MILLISECOND, 0);
						Long beginningTime = beginning.getTime().getTime();
						
						if (PER_SCOPE.equals(facilityType)) {
							List<String> facilityIds = new ArrayList<>();
							facilityIds.addAll(nodeIds);
							facilityIds.addAll(scopeIds);
							
							// 月初以降でノードに紐づいている観測データを取得。
							TypedQuery<BillingDetailRelationEntity> query = em.createNamedQuery(BillingDetailRelationEntity.selectBillingDetailRelationEntityAfter, BillingDetailRelationEntity.class);
							query.setParameter("facilityIds", facilityIds);
							query.setParameter("beginningTime", beginningTime);
							
							double cost = 0;
							Set<String> set = new LinkedHashSet<>();
							try {
								List<BillingDetailRelationEntity> billingDetails = query.getResultList();
								for(BillingDetailRelationEntity r: billingDetails){
									cost += r.getBillingDetail().getCost();
									set.add("\"" + r.getBillingDetail().getCloudScopeId() + "\"-\"" + r.getBillingDetail().getResourceId() + "\"");
								}
							} catch (Exception e) {
								notifyUnknown(m_monitor, e);
								throw e;
							}
							
							//通知
							if (m_monitor.getMonitorFlg()){
								notifySum(m_monitor, m_monitor.getFacilityId(), m_judgementInfoList, cost, set);
							}
							
							//収集
							if (m_monitor.getCollectorFlg()) {
								Sample sample = new Sample(new Date(), m_monitorId);
								sample.set(m_facilityId, monitorKind, cost, CollectedDataErrorTypeConstant.NOT_ERROR);
								CollectDataUtil.put(Arrays.asList(sample));
							}
						} else /*if (PER_NODE.equals(facilityType)) */{
							Map<String, Double> costs = new HashMap<>();
							Map<String, Set<String>> resouceLists = new HashMap<>();
							if (!nodeIds.isEmpty()) {
								TypedQuery<BillingDetailRelationEntity> query = em.createNamedQuery(BillingDetailRelationEntity.selectBillingDetailRelationEntityAfter, BillingDetailRelationEntity.class);
								query.setParameter("facilityIds", nodeIds);
								query.setParameter("beginningTime", beginningTime);
								
								try {
									List<BillingDetailRelationEntity> billingDetails = query.getResultList();
									for (BillingDetailRelationEntity r: billingDetails) {
										if (r.getBillingDetail().getCost() == null) {
											logger.debug(String.format("invalid cost value: %s", r));
											continue;
										}
										
										Double cost = costs.get(r.getFacilityId());
										costs.put(r.getFacilityId(), r.getBillingDetail().getCost() + (cost != null ? cost: 0));
										
										Set<String> resources = resouceLists.get(r.getFacilityId());
										if (resources == null) {
											resources = new HashSet<>();
											resouceLists.put(r.getFacilityId(), resources);
										}
										resources.add("\"" + r.getBillingDetail().getCloudScopeId() + "\"-\"" + r.getBillingDetail().getResourceId() + "\"");
									}
								} catch (Exception e) {
									notifyUnknown(m_monitor, e);
									throw e;
								}
							}
							
							//通知
							if (m_monitor.getMonitorFlg()){
								for (Entry<String, Double> entry : costs.entrySet()) {
									notifySum(m_monitor, entry.getKey(), m_judgementInfoList, entry.getValue(), resouceLists.get(entry.getKey()));
								}
							}
							
							//収集
							if (m_monitor.getCollectorFlg()) {
								Sample sample = new Sample(new Date(), m_monitorId);
								for (Entry<String, Double> entry : costs.entrySet()) {
									sample.set(entry.getKey(), monitorKind, entry.getValue(), CollectedDataErrorTypeConstant.NOT_ERROR);
								}
								CollectDataUtil.put(Arrays.asList(sample));
							}
						}
					}
					break;
				case delta:
					{
						List<CloudScopeEntity> cloudScopes = PersistenceUtil.findAll(em, CloudScopeEntity.class);
						if (cloudScopes.isEmpty()) {
							// アカウントリソースがない場合は、スキップ。
							return ret;
						}
						
						// 全アカウントリソースで、最過去の観測値取得日時を抽出。
						Long minNotifiedDate = null;
						for (CloudScopeEntity a: cloudScopes) {
							if (!a.getBillingDetailCollectorFlg()) continue;
							
							if (a.getBillingLastDate() != null) {
								if (minNotifiedDate == null) {
									minNotifiedDate = a.getBillingLastDate();
								} else {
									minNotifiedDate = minNotifiedDate > a.getBillingLastDate() ? a.getBillingLastDate(): minNotifiedDate;
								}
							}
							else {
								// 取得
								Calendar regdate = Calendar.getInstance();
								regdate.setTime(new Date());
								regdate.set(Calendar.HOUR_OF_DAY, 0);
								regdate.set(Calendar.MINUTE, 0);
								regdate.set(Calendar.SECOND, 0);
								regdate.set(Calendar.MILLISECOND, 0);
								regdate.add(Calendar.DAY_OF_YEAR, a.getRetentionPeriod() * -1);
								
								Long regtime = regdate.getTime().getTime();
	
								if (minNotifiedDate == null) {
									minNotifiedDate = regtime;
								} else {
									minNotifiedDate = minNotifiedDate > regtime ? regtime: minNotifiedDate;
								}
							}
						}
						
						// 監視対象となるクラウドスコープがないので監視終了
						if (minNotifiedDate == null) {
							return ret;
						}
						
						
						//通知最終日時を取得
						TypedQuery<BillingDetailMonitorStatusEntity> query = em.createNamedQuery(BillingDetailMonitorStatusEntity.getLastNotifiedDay, BillingDetailMonitorStatusEntity.class);
						query.setParameter("monitorId", m_monitorId);
						
						BillingDetailMonitorStatusEntity bdmc = null;
						try {
							bdmc = query.getSingleResult();
						} catch (NoResultException e) {
							bdmc = new BillingDetailMonitorStatusEntity();
							bdmc.setMonitorId(m_monitorId);
							//bdmc.setLastNotifiedDay(new Date().getTime());
							PersistenceUtil.persist(em, bdmc);
						} catch (NonUniqueResultException e) {
							throw e;
						}
						
						// 通知可能日
						Calendar end = Calendar.getInstance();
						end.setTime(new Date(minNotifiedDate));
						end.set(Calendar.HOUR_OF_DAY, 0);
						end.set(Calendar.MINUTE, 0);
						end.set(Calendar.SECOND, 0);
						end.set(Calendar.MILLISECOND, 0);
						
						if (bdmc.getLastNotifiedDay() == null) {
							// 通知した最終日。
							Calendar today = Calendar.getInstance();
							today.setTime(new Date(m_monitor.getRegDate()));
							today.set(Calendar.HOUR_OF_DAY, 0);
							today.set(Calendar.MINUTE, 0);
							today.set(Calendar.SECOND, 0);
							today.set(Calendar.MILLISECOND, 0);
							bdmc.setLastNotifiedDay(today.getTime().getTime());
						}
						
						// 通知した最終日。
						Calendar start = Calendar.getInstance();
						start.setTime(new Date(bdmc.getLastNotifiedDay()));
						start.set(Calendar.HOUR_OF_DAY, 0);
						start.set(Calendar.MINUTE, 0);
						start.set(Calendar.SECOND, 0);
						start.set(Calendar.MILLISECOND, 0);
						
						Calendar i = Calendar.getInstance();
						i.setTime(start.getTime());

						if (PER_SCOPE.equals(facilityType)) {
							List<String> facilityIds = new ArrayList<>();
							facilityIds.addAll(nodeIds);
							facilityIds.addAll(scopeIds);
							
							TypedQuery<BillingDetailEntity> q = em.createNamedQuery(BillingDetailEntity.selectBillingDetailEntityRange, BillingDetailEntity.class);
							q.setParameter("facilityIds", facilityIds);
							q.setParameter("start", start.getTime().getTime());
							q.setParameter("end", end.getTime().getTime());

							List<BillingDetailEntity> billingDetails = q.getResultList();
							for (; i.before(end); i.add(Calendar.DAY_OF_MONTH, 1)) {
								Set<String> set = new LinkedHashSet<>();
								double cost = 0;
								Iterator<BillingDetailEntity> iter = billingDetails.iterator(); 
								while (iter.hasNext()) {
									BillingDetailEntity d = iter.next();
									Calendar t = Calendar.getInstance();
									t.setTime(new Date(d.getTargetDate()));
									if (t.get(Calendar.YEAR) == i.get(Calendar.YEAR) &&
										t.get(Calendar.MONTH) == i.get(Calendar.MONTH) &&
										t.get(Calendar.DAY_OF_MONTH) == i.get(Calendar.DAY_OF_MONTH)
										) {
										cost += d.getCost();
										set.add("\"" + d.getCloudScopeId() + "\"-\"" + d.getResourceId() + "\"");
										iter.remove();
									}
								}
								
								if (m_monitor.getMonitorFlg())
									notifyDelta(m_monitor, m_monitor.getFacilityId(), m_judgementInfoList, i.getTime().getTime(), cost, set);
								
								if (m_monitor.getCollectorFlg()) {
									Sample sample = new Sample(new Date(), m_monitorId);
									sample.set(m_facilityId,monitorKind, cost, CollectedDataErrorTypeConstant.NOT_ERROR);
									CollectDataUtil.put(Arrays.asList(sample));
								}
							}
						} else /*if (PER_NODE.equals(facilityType)) */{
							List<BillingDetailEntity> billingDetails = Collections.emptyList();
							if (!nodeIds.isEmpty()) {
								TypedQuery<BillingDetailEntity> q = em.createNamedQuery(BillingDetailEntity.selectBillingDetailEntityRange, BillingDetailEntity.class);
								q.setParameter("facilityIds", nodeIds);
								q.setParameter("start", start.getTime().getTime());
								q.setParameter("end", end.getTime().getTime());
								billingDetails = q.getResultList();
							}
							
							for (; i.before(end); i.add(Calendar.DAY_OF_MONTH, 1)) {
								Iterator<BillingDetailEntity> iter = billingDetails.iterator(); 
								Map<String, Double> costs = new HashMap<>();
								Map<String, Set<String>> resouceLists = new HashMap<>();
								while (iter.hasNext()) {
									BillingDetailEntity d = iter.next();
									Calendar t = Calendar.getInstance();
									t.setTime(new Date(d.getTargetDate()));
									if (t.get(Calendar.YEAR) == i.get(Calendar.YEAR) &&
										t.get(Calendar.MONTH) == i.get(Calendar.MONTH) &&
										t.get(Calendar.DAY_OF_MONTH) == i.get(Calendar.DAY_OF_MONTH)
										) {
										for (BillingDetailRelationEntity r : d.getBillingDetailRelations()) {
											if (r.getBillingDetail().getCost() == null) {
												logger.debug(String.format("invalid cost value: %s", r));
												continue;
											}
											
											Double cost = costs.get(r.getFacilityId());
											costs.put(r.getFacilityId(), r.getBillingDetail().getCost() + (cost != null ? cost: 0));
											
											Set<String> resources = resouceLists.get(r.getFacilityId());
											if (resources == null) {
												resources = new HashSet<>();
												resouceLists.put(r.getFacilityId(), resources);
											}
											resources.add("\"" + r.getBillingDetail().getCloudScopeId() + "\"-\"" + r.getBillingDetail().getResourceId() + "\"");
										}
										iter.remove();
									}
								}
								
								if (m_monitor.getMonitorFlg()){
									for (Map.Entry<String, Double> entry: costs.entrySet()) {
										notifyDelta(m_monitor, entry.getKey(), m_judgementInfoList, i.getTime().getTime(), entry.getValue(), resouceLists.get(entry.getKey()));
									}
								}
								
								if (m_monitor.getCollectorFlg() 
										|| m_monitor.getPredictionFlg() 
										|| m_monitor.getChangeFlg()) {
									Sample sample = new Sample(new Date(), m_monitorId);
									for (Map.Entry<String, Double> entry: costs.entrySet()) {

										// 将来予測監視、変化量監視の処理を行う
										CollectMonitorDataInfo collectMonitorDataInfo
											= CollectMonitorManagerUtil.calculateChangePredict(null, m_monitor, entry.getKey(),
												null, m_monitor.getItemName(), i.getTime().getTime(), entry.getValue());

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
										if (m_monitor.getCollectorFlg()) {
											sample.set(entry.getKey(), monitorKind, entry.getValue(), 
													average, standardDeviation,CollectedDataErrorTypeConstant.NOT_ERROR);
										}
									}
									if (m_monitor.getCollectorFlg()) {
										CollectDataUtil.put(Arrays.asList(sample));
									}
								}
							}
						}
						
						// 通知判定を行った最終日を保存。
						bdmc.setLastNotifiedDay(end.getTime().getTime());
					}
				}
				ts.complete();
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
		return new CloudServiceBillingDetailRunMonitor();
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
	
	protected boolean notifyBilling(String target, Map<String,IBillings.PlatformServiceBilling> billings) {
		boolean success = false;
		for (Entry<String, PlatformServiceBilling> entry : billings.entrySet()) {
			
			String facilityId = entry.getKey();
			PlatformServiceBilling billing = entry.getValue();
			
			CloudUtil.Priority prioriry = CloudUtil.checkPriorityRange(m_judgementInfoList, billing.getPrice());
			OutputBasicInfo output;
			Sample sample;
			switch(prioriry.type) {
			case PriorityConstant.TYPE_INFO:
			case PriorityConstant.TYPE_WARNING:
			case PriorityConstant.TYPE_CRITICAL:
				output = createOutput(prioriry, facilityId, target, billing.getPrice(), billing.getUpdateDate());
				sample = new Sample(new Date(),m_monitorId);
				sample.set(facilityId, target, billing.getPrice(), CollectedDataErrorTypeConstant.NOT_ERROR);
				success = true;
				break;
			default:
				output = createOutput(prioriry, facilityId, target, billing.getPrice(), billing.getUpdateDate());
				sample = new Sample(new Date(),m_monitorId);
				sample.set(facilityId, target, billing.getPrice(), CollectedDataErrorTypeConstant.UNKNOWN);
				break;
			}
			notify(output);
			collect(sample);
		}
		return success;
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
	
	protected OutputBasicInfo createOutput(CloudUtil.Priority priority, String facilityId, String target, double value, Long generationDate) {
		String message = String.format("%s : %f", target, value);
		return CloudUtil.createOutputBasicInfoEx(priority, monitorTypeId, m_monitorId,
				target, m_monitor.getApplication(), facilityId, message, message, generationDate == null ? Long.valueOf(new Date().getTime()): generationDate);
	}
	
	protected OutputBasicInfo createUnknownOutput(String target, Long generationDate) {
		String message = CloudMessageConstant.MONITOR_UNKNOWN.getMessage();
		return CloudUtil.createOutputBasicInfoEx(CloudUtil.Priority.UNKNOWN, monitorTypeId, m_monitorId,
				target, m_monitor.getApplication(), m_facilityId, message, message, new Date().getTime());
	}
}
