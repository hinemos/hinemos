/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.plugin.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.rest.endpoint.access.AccessRestEndpoints;
import com.clustercontrol.rest.endpoint.access.AccessRestFilterRegistration;
import com.clustercontrol.rest.endpoint.calendar.CalendarRestEndpoints;
import com.clustercontrol.rest.endpoint.calendar.CalendarRestFilterRegistration;
import com.clustercontrol.rest.endpoint.collect.CollectRestEndpoints;
import com.clustercontrol.rest.endpoint.collect.CollectRestFilterRegistration;
import com.clustercontrol.rest.endpoint.common.CommonRestEndpoints;
import com.clustercontrol.rest.endpoint.common.CommonRestFilterRegistration;
import com.clustercontrol.rest.endpoint.filtersetting.FilterSettingRestEndpoints;
import com.clustercontrol.rest.endpoint.filtersetting.FilterSettingRestFilterRegistration;
import com.clustercontrol.rest.endpoint.hub.HubRestEndpoints;
import com.clustercontrol.rest.endpoint.hub.HubRestFilterRegistration;
import com.clustercontrol.rest.endpoint.infra.InfraRestEndpoints;
import com.clustercontrol.rest.endpoint.infra.InfraRestFilterRegistration;
import com.clustercontrol.rest.endpoint.jobmanagement.JobRestEndpoints;
import com.clustercontrol.rest.endpoint.jobmanagement.JobRestFilterRegistration;
import com.clustercontrol.rest.endpoint.maintenance.MaintenanceRestEndpoints;
import com.clustercontrol.rest.endpoint.maintenance.MaintenanceRestFilterRegistration;
import com.clustercontrol.rest.endpoint.monitorresult.MonitorResultFilterRegistration;
import com.clustercontrol.rest.endpoint.monitorresult.MonitorResultRestEndpoints;
import com.clustercontrol.rest.endpoint.monitorsetting.MonitorsettingRestEndpoints;
import com.clustercontrol.rest.endpoint.monitorsetting.MonitorsettingRestFilterRegistration;
import com.clustercontrol.rest.endpoint.notify.NotifyRestEndpoints;
import com.clustercontrol.rest.endpoint.notify.NotifyRestFilterRegistration;
import com.clustercontrol.rest.endpoint.repository.RepositoryRestEndpoints;
import com.clustercontrol.rest.endpoint.repository.RepositoryRestFilterRegistration;
import com.clustercontrol.rest.util.RestHttpBearerAuthenticator;
import com.clustercontrol.rest.util.RestTempFileUtil;
import com.clustercontrol.rest.util.RestTokenDataCleaner;

public class RestServiceClientPlugin extends RestServicePlugin implements HinemosPlugin {
	private static final Log log = LogFactory.getLog(RestServiceClientPlugin.class);

	RestTokenDataCleaner _tokencleaner;

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(AsyncWorkerPlugin.class.getName());
		dependency.add(JobInitializerPlugin.class.getName());
		return dependency;
	}
	
	@Override
	public void create() {
		log.info("create() start.");
		try {
			addressPrefix = HinemosPropertyCommon.rest_client_address.getStringValue();
		} catch (Exception e) {
			log.error("create() failed.", e);
		}
		
		try {
			RestTempFileUtil.deleteTempFile();
		} catch (Throwable e) {
			log.warn("failed to deleteTempFile. " + e.getMessage());
		}
		
		_tokencleaner = new RestTokenDataCleaner();
	}

	@Override
	public void activate() {
		log.info("activate() start.");

		// Bearer認証におけるログインの有効期間を初期設定（以後変更はしない想定）
		int validTermMinutes = HinemosPropertyCommon.rest_token_valid_term_minutes.getIntegerValue();
		RestHttpBearerAuthenticator.getInstance().setLoginValidTerm(validTermMinutes);

		// Check if key exists
		if(!checkRequiredKeys()){
			log.warn("KEY NOT FOUND! Unable to activate " + this.getClass().getName());
			return;
		}
		
		// Publish向けアドレスを取得
		String addressPrefix = HinemosPropertyCommon.rest_client_address.getStringValue();

		try {
			String calendarClassName = CalendarRestEndpoints.class.getSimpleName();
			Set<Class<?>> registerClasseSetCalendar = commonRegisterClasses();
			registerClasseSetCalendar.add(CalendarRestEndpoints.class);
			registerClasseSetCalendar.add(CalendarRestFilterRegistration.class);
			ResourceConfig calendarResourceConfig = new ResourceConfig().registerClasses(registerClasseSetCalendar);
			publish(addressPrefix, BASE_URL + "/" + calendarClassName, calendarResourceConfig);
			
			String maintenanceClassName = MaintenanceRestEndpoints.class.getSimpleName();
			Set<Class<?>> registerClasseSetMaintenance = commonRegisterClasses();
			registerClasseSetMaintenance.add(MaintenanceRestEndpoints.class);
			registerClasseSetMaintenance.add(MaintenanceRestFilterRegistration.class);
			ResourceConfig maintenanceResourceConfig = new ResourceConfig().registerClasses(registerClasseSetMaintenance);
			publish(addressPrefix, BASE_URL + "/" + maintenanceClassName, maintenanceResourceConfig);

			String collectClassName = CollectRestEndpoints.class.getSimpleName();
			Set<Class<?>> registerClasseSetCollect = commonRegisterClasses();
			registerClasseSetCollect.add(CollectRestEndpoints.class);
			registerClasseSetCollect.add(CollectRestFilterRegistration.class);
			ResourceConfig collectResourceConfig = new ResourceConfig().registerClasses(registerClasseSetCollect);
			publish(addressPrefix, BASE_URL + "/" + collectClassName, collectResourceConfig);

			String CommonClassName = CommonRestEndpoints.class.getSimpleName();
			Set<Class<?>> registerClasseSetCommon = commonRegisterClasses();
			registerClasseSetCommon.add(CommonRestEndpoints.class);
			registerClasseSetCommon.add(CommonRestFilterRegistration.class);
			ResourceConfig CommonResourceConfig = new ResourceConfig().registerClasses(registerClasseSetCommon);
			publish(addressPrefix, BASE_URL + "/" + CommonClassName, CommonResourceConfig);

			String hubClassName = HubRestEndpoints.class.getSimpleName();
			Set<Class<?>> registerClasseSetHub = commonRegisterClasses();
			registerClasseSetHub.add(HubRestEndpoints.class);
			registerClasseSetHub.add(HubRestFilterRegistration.class);
			ResourceConfig hubResourceConfig = new ResourceConfig().registerClasses(registerClasseSetHub);
			publish(addressPrefix, BASE_URL + "/" + hubClassName, hubResourceConfig);

			String monitorsettingClassName = MonitorsettingRestEndpoints.class.getSimpleName();
			Set<Class<?>> registerClasseSetMonitorSetting = commonRegisterClasses();
			registerClasseSetMonitorSetting.add(MonitorsettingRestEndpoints.class);
			registerClasseSetMonitorSetting.add(MonitorsettingRestFilterRegistration.class);
			ResourceConfig monitorsettingResourceConfig = new ResourceConfig().registerClasses(registerClasseSetMonitorSetting);
			publish(addressPrefix, BASE_URL + "/" + monitorsettingClassName, monitorsettingResourceConfig);

			String notifyClassName = NotifyRestEndpoints.class.getSimpleName();
			Set<Class<?>> registerClasseSetNotify = commonRegisterClasses();
			registerClasseSetNotify.add(NotifyRestEndpoints.class);
			registerClasseSetNotify.add(NotifyRestFilterRegistration.class);
			ResourceConfig notifyResourceConfig = new ResourceConfig().registerClasses(registerClasseSetNotify);
			publish(addressPrefix, BASE_URL + "/" + notifyClassName, notifyResourceConfig);

			String infraClassName = InfraRestEndpoints.class.getSimpleName();
			Set<Class<?>> registerClasseSetInfra = commonRegisterClasses();
			registerClasseSetInfra.add(InfraRestEndpoints.class);
			registerClasseSetInfra.add(InfraRestFilterRegistration.class);
			registerClasseSetInfra.add(MultiPartFeature.class);
			ResourceConfig infraResourceConfig = new ResourceConfig().registerClasses(registerClasseSetInfra);
			publish(addressPrefix, BASE_URL + "/" + infraClassName, infraResourceConfig);

			String repositoryClassName = RepositoryRestEndpoints.class.getSimpleName();
			Set<Class<?>> registerClasseSetRepository = commonRegisterClasses();
			registerClasseSetRepository.add(RepositoryRestEndpoints.class);
			registerClasseSetRepository.add(RepositoryRestFilterRegistration.class);
			ResourceConfig repositoryResourceConfig = new ResourceConfig().registerClasses(registerClasseSetRepository);
			publish(addressPrefix, BASE_URL + "/" + repositoryClassName, repositoryResourceConfig);

			String jobClassName = JobRestEndpoints.class.getSimpleName();
			Set<Class<?>> registerClasseSetJob = commonRegisterClasses();
			registerClasseSetJob.add(JobRestEndpoints.class);
			registerClasseSetJob.add(JobRestFilterRegistration.class);
			ResourceConfig jobResourceConfig = new ResourceConfig().registerClasses(registerClasseSetJob);
			publish(addressPrefix, BASE_URL + "/" + jobClassName, jobResourceConfig);

			String monitorResultClassName = MonitorResultRestEndpoints.class.getSimpleName();
			Set<Class<?>> registerClasseSetMonitorResult = commonRegisterClasses();
			registerClasseSetMonitorResult.add(MonitorResultRestEndpoints.class);
			registerClasseSetMonitorResult.add(MonitorResultFilterRegistration.class);
			ResourceConfig monitorResultResourceConfig = new ResourceConfig().registerClasses(registerClasseSetMonitorResult);
			publish(addressPrefix, BASE_URL + "/" + monitorResultClassName, monitorResultResourceConfig);

			String filterSettingClassName = FilterSettingRestEndpoints.class.getSimpleName();
			Set<Class<?>> registerClasseSetFilterSetting = commonRegisterClasses();
			registerClasseSetFilterSetting.add(FilterSettingRestEndpoints.class);
			registerClasseSetFilterSetting.add(FilterSettingRestFilterRegistration.class);
			ResourceConfig filterSettingResourceConfig = new ResourceConfig().registerClasses(registerClasseSetFilterSetting);
			publish(addressPrefix, BASE_URL + "/" + filterSettingClassName, filterSettingResourceConfig);

			// Access は最後に publish する
			String accessClassName = AccessRestEndpoints.class.getSimpleName();
			Set<Class<?>> registerClasseSetAccess = commonRegisterClasses();
			registerClasseSetAccess.add(AccessRestEndpoints.class);
			registerClasseSetAccess.add(AccessRestFilterRegistration.class);
			ResourceConfig accessResourceConfig = new ResourceConfig().registerClasses(registerClasseSetAccess);
			publish(addressPrefix, BASE_URL + "/" + accessClassName, accessResourceConfig);

		} catch (Exception e) {
			log.error("acivate failed.", e);
		}

		_tokencleaner.start();
	}

	@Override
	public void deactivate() {
		super.deactivate();
		_tokencleaner.shutdown();
	}
}
