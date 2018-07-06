/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.ws.access.AccessEndpoint;
import com.clustercontrol.ws.calendar.CalendarEndpoint;
import com.clustercontrol.ws.cloud.CloudCommonEndpoint;
import com.clustercontrol.ws.collect.CollectEndpoint;
import com.clustercontrol.ws.collectmaster.PerformanceCollectMasterEndpoint;
import com.clustercontrol.ws.hub.BinaryEndpoint;
import com.clustercontrol.ws.hub.HubEndpoint;
import com.clustercontrol.ws.infra.InfraEndpoint;
import com.clustercontrol.ws.jmxmaster.JmxMasterEndpoint;
import com.clustercontrol.ws.jobmanagement.JobEndpoint;
import com.clustercontrol.ws.mailtemplate.MailTemplateEndpoint;
import com.clustercontrol.ws.maintenance.HinemosPropertyEndpoint;
import com.clustercontrol.ws.maintenance.MaintenanceEndpoint;
import com.clustercontrol.ws.monitor.MonitorEndpoint;
import com.clustercontrol.ws.monitor.MonitorSettingEndpoint;
import com.clustercontrol.ws.notify.NotifyEndpoint;
import com.clustercontrol.ws.repository.RepositoryEndpoint;

/**
 * JAX-WSによるWEBサービスの初期化(publish)/停止(stop)を制御するHinemos本体コンポーネント間接続用プラグイン.
 *
 */
public class WebServiceCorePlugin extends WebServicePlugin implements HinemosPlugin {
	private static final Log log = LogFactory.getLog(WebServiceCorePlugin.class);

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(AsyncWorkerPlugin.class.getName());
		// TODO Why need the followings?
		//dependency.add(WebServiceJobMapPlugin.class.getName());
		//dependency.add(WebServiceNodeMapPlugin.class.getName());
		//dependency.add(WebServiceUtilityPlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {
	}

	@Override
	public void activate() {
		// Check if key exists
		if(!checkRequiredKeys()){
			log.warn("KEY NOT FOUND! Unable to activate " + this.getClass().getName());
			return;
		}

		final String addressPrefix = HinemosPropertyCommon.ws_client_address.getStringValue();

		/** Webサービスの起動処理 */
		publish(addressPrefix, "/HinemosWS/CalendarEndpoint", new CalendarEndpoint());
		publish(addressPrefix, "/HinemosWS/CloudCommonEndpoint", new CloudCommonEndpoint());
		publish(addressPrefix, "/HinemosWS/CollectEndpoint", new CollectEndpoint());
		publish(addressPrefix, "/HinemosWS/JobEndpoint", new JobEndpoint());
		publish(addressPrefix, "/HinemosWS/MailTemplateEndpoint", new MailTemplateEndpoint());
		publish(addressPrefix, "/HinemosWS/MaintenanceEndpoint", new MaintenanceEndpoint());
		publish(addressPrefix, "/HinemosWS/HinemosPropertyEndpoint", new HinemosPropertyEndpoint());
		publish(addressPrefix, "/HinemosWS/MonitorEndpoint", new MonitorEndpoint());
		publish(addressPrefix, "/HinemosWS/MonitorSettingEndpoint", new MonitorSettingEndpoint());
		publish(addressPrefix, "/HinemosWS/NotifyEndpoint", new NotifyEndpoint());
		publish(addressPrefix, "/HinemosWS/RepositoryEndpoint", new RepositoryEndpoint());
		publish(addressPrefix, "/HinemosWS/PerformanceCollectMasterEndpoint", new PerformanceCollectMasterEndpoint());
		publish(addressPrefix, "/HinemosWS/JmxMasterEndpoint", new JmxMasterEndpoint());
		publish(addressPrefix, "/HinemosWS/InfraEndpoint", new InfraEndpoint());
		publish(addressPrefix, "/HinemosWS/HubEndpoint", new HubEndpoint());
		publish(addressPrefix, "/HinemosWS/BinaryEndpoint", new BinaryEndpoint());

		// ログインは最後にpublishする。
		publish(addressPrefix, "/HinemosWS/AccessEndpoint", new AccessEndpoint());
	}

	@Override
	public void deactivate() {
		super.deactivate();
	}

}
