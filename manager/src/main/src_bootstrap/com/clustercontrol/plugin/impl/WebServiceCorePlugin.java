/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.plugin.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.ws.access.AccessEndpoint;
import com.clustercontrol.ws.calendar.CalendarEndpoint;
import com.clustercontrol.ws.cloud.CloudCommonEndpoint;
import com.clustercontrol.ws.collect.CollectEndpoint;
import com.clustercontrol.ws.collectmaster.PerformanceCollectMasterEndpoint;
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

	public static final Log log = LogFactory.getLog(WebServiceCorePlugin.class);

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(WebServiceJobMapPlugin.class.getName());
		dependency.add(WebServiceNodeMapPlugin.class.getName());
		dependency.add(WebServiceUtilityPlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {
	}

	@Override
	public void activate() {
		final String addressPrefix = HinemosPropertyUtil.getHinemosPropertyStr("ws.client.address" , "http://0.0.0.0:8080");

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

		// ログインは最後にpublishする。
		publish(addressPrefix, "/HinemosWS/AccessEndpoint", new AccessEndpoint());
	}

	@Override
	public void deactivate() {
		super.deactivate();
	}

}
