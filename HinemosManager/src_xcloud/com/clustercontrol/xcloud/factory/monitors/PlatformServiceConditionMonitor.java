/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory.monitors;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.ICloudOption;
import com.clustercontrol.xcloud.factory.IPlatformServiceMonitor;
import com.clustercontrol.xcloud.factory.IPlatformServiceMonitor.ICloudScopeAreaMonitor;
import com.clustercontrol.xcloud.factory.IPlatformServiceMonitor.IPlatformAreaMonitor;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.model.PlatformAreaServiceConditionEntity;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;
import com.clustercontrol.xcloud.persistence.PersistenceUtil.TransactionScope;
import com.clustercontrol.xcloud.persistence.Transactional;
import com.clustercontrol.xcloud.util.CollectionComparator;

public class PlatformServiceConditionMonitor extends CloudManagerJob {
	public static final String jobName = "PlatformServiceConditionMonitor";
	public static final String jobGroupName = "CLOUD_MANAGEMENT";
	
	private static final ReentrantLock lock = new ReentrantLock();
	
	protected void internalExecute() throws Exception {
		if (lock.tryLock()) {
			try {
				List<CloudScopeEntity> cloudScopes = CloudManager.singleton().getCloudScopes().getAllCloudScopes();
				
				// サービスの監視情報全てにアクセスするので、DB から全て取得し JPA にキャッシュしてもらう。
				PersistenceUtil.findAll(Session.current().getEntityManager(), PlatformAreaServiceConditionEntity.class);
				
				final Set<String> platformIds = new HashSet<String>();
				
				for (CloudScopeEntity cloudScope: cloudScopes) {
					cloudScope.optionExecute(new CloudScopeEntity.OptionExecutor() {
						@Override
						public void execute(final CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
							option.getPlatformServiceMonitor().visit(new IPlatformServiceMonitor.IVisitor() {
								@Override
								public void visit(IPlatformAreaMonitor monitor) throws CloudManagerException {
									if (platformIds.contains(scope.getPlatformId())) 
										return;
									
									// スコープに関連するサービスの監視情報をチェック
									updatePlatformAreaServiceConditions(monitor, monitor.getPlatformId(), monitor.monitorPlatformServiceConditions());
									
									for (LocationEntity location: scope.getLocations()) {
										// ロケーションに関連するサービスの監視情報をチェック
										updatePlatformAreaServiceConditions(monitor, location.getLocationId(), monitor.monitorPlatformServiceConditions(location));
									}
								}
								@Override
								public void visit(ICloudScopeAreaMonitor monitor) throws CloudManagerException {
//									throw new InternalManagerError();
								}
							});
						}
					});
				}
			} finally {
				lock.unlock();
			}
		}
	}

	protected static void updatePlatformAreaServiceConditions(final IPlatformAreaMonitor monitor, final String locationId, List<ICloudOption.PlatformServiceCondition> platformConditions) throws CloudManagerException {
		try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
			HinemosEntityManager em = Session.current().getEntityManager();
			TypedQuery<PlatformAreaServiceConditionEntity> query = em.createNamedQuery("findPlatformAreaServiceConditionsByLocationId", PlatformAreaServiceConditionEntity.class);
			query.setParameter("platformId", monitor.getPlatformId());
			query.setParameter("locationId", locationId);
			List<PlatformAreaServiceConditionEntity> conditionEntities = query.getResultList();
			
			if (!platformConditions.isEmpty()) {
				CollectionComparator.compare(conditionEntities, platformConditions, new CollectionComparator.Comparator<PlatformAreaServiceConditionEntity, ICloudOption.PlatformServiceCondition>() {
					@Override
					public boolean match(PlatformAreaServiceConditionEntity o1, ICloudOption.PlatformServiceCondition o2) throws CloudManagerException {
						return o1.getServiceId().equals(o2.getServiceId());
					}
					@Override
					public void matched(PlatformAreaServiceConditionEntity o1, ICloudOption.PlatformServiceCondition o2) throws CloudManagerException {
						o1.setServiceName(o2.getServiceName());
						o1.setStatus(o2.getStatus());
						o1.setMessage(o2.getMessage());
						o1.setDetail(o2.getDetail());
						o1.setLastDate(new Date().getTime());
						if (o1.getBeginDate() == null)
							o1.setBeginDate(o2.getMonitorDate().getTime());
						o1.setRecordDate(o2.getMonitorDate().getTime());
					}
					@Override
					public void afterO1(PlatformAreaServiceConditionEntity o1) throws CloudManagerException {
						// 観測値なし
					}
					@Override
					public void afterO2(ICloudOption.PlatformServiceCondition o2) throws CloudManagerException {
						// 新規の観測値
						PlatformAreaServiceConditionEntity condition = new PlatformAreaServiceConditionEntity();
						condition.setPlatformId(monitor.getPlatformId());
						condition.setLocationId(locationId);
						condition.setServiceId(o2.getServiceId());
						condition.setServiceName(o2.getServiceName());
						condition.setStatus(o2.getStatus());
						condition.setMessage(o2.getMessage());
						condition.setDetail(o2.getDetail());
						condition.setLastDate(new Date().getTime());
						condition.setBeginDate(new Date().getTime());
						condition.setRecordDate(o2.getMonitorDate().getTime());
						
						PersistenceUtil.persist(Session.current().getEntityManager(), condition);
					}
				});
				
				scope.complete();
			}
		} catch (Exception e) {
			Logger.getLogger(PlatformServiceConditionMonitor.class).warn(e.getMessage(), e);
		}
	}

	/**
	 * 各種サービス状態の自動更新
	 * 
	 * CloudPropertyConstants.platform_service_monitor_interval(hinemos.cloud.platform.service.monitor.interval)を"OFF"にすることで、無効化(Skip)できる。
	 */
	@SuppressWarnings("unchecked")
	public static void start() {
		try {
			String cronString = HinemosPropertyCommon.xcloud_platform_service_monitor_interval.getStringValue();
			if(! "off".equals(cronString)) {
				SchedulerPlugin.scheduleCronJob(SchedulerType.RAM_MONITOR, jobName, jobGroupName, HinemosTime.currentTimeMillis(), cronString, true, PlatformServiceConditionMonitor.class.getName(), "execute", new Class[]{}, new Serializable[]{});
			} else {
				Logger.getLogger(PlatformServiceConditionMonitor.class).debug("Skipping PlatformServiceConditionMonitor.");
			}
		} catch (Exception e) {
			Logger.getLogger(PlatformServiceConditionMonitor.class).warn(e.getMessage(), e);

			// 起動に失敗した場合、cron 文字列を既定で再試行。
			try {
				String cronString = HinemosPropertyCommon.xcloud_platform_service_monitor_interval.getStringValue();
				SchedulerPlugin.scheduleCronJob(SchedulerType.RAM_MONITOR, jobName, jobGroupName, HinemosTime.currentTimeMillis(), cronString, true, PlatformServiceConditionMonitor.class.getName(), "execute", new Class[]{}, new Serializable[]{});
			} catch (HinemosUnknown|NullPointerException e1) {
				Logger.getLogger(PlatformServiceConditionMonitor.class).warn(e.getMessage(), e);
			} catch (Exception e1) {
				Logger.getLogger(PlatformServiceConditionMonitor.class).warn(e.getMessage(), e);
			}
		}
	}
	
	public static void stop() {
		try {
			SchedulerPlugin.deleteJob(SchedulerType.RAM_MONITOR, jobName, jobGroupName);
		} catch (HinemosUnknown e) {
			Logger.getLogger(PlatformServiceConditionMonitor.class).warn(e.getMessage(), e);
		}
	}
}
