/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.enterprise;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ObjectSharingService;
import com.clustercontrol.notify.util.INotifyOwnerDeterminer;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.plugin.impl.SnmpTrapPlugin;
import com.clustercontrol.plugin.impl.SystemLogPlugin;
import com.clustercontrol.plugin.impl.WebServicePlugin;
import com.clustercontrol.reporting.ReportingEventOwnerDeterminer;
import com.clustercontrol.util.KeyCheck;
import com.clustercontrol.ws.inquiry.InquiryEndpoint;
import com.clustercontrol.ws.jobmanagement.JobMapEndpoint;
import com.clustercontrol.ws.nodemap.NodeMapEndpoint;
import com.clustercontrol.ws.reporting.ReportingEndpoint;
import com.clustercontrol.ws.utility.UtilityEndpoint;

/**
 * JAX-WSによるWEBサービスの初期化(publish)/停止(stop)を制御するジョブマップオプション用プラグイン.
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class EnterprisePlugin extends WebServicePlugin implements HinemosPlugin {
	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(SnmpTrapPlugin.class.getName());
		dependency.add(SystemLogPlugin.class.getName());
		return dependency;
	}

	@Override
	public Set<String> getRequiredKeys() {
		return new HashSet<>(Arrays.asList(KeyCheck.TYPE_ENTERPRISE));
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
		// 遠隔管理
		publish(addressPrefix, "/HinemosWS/InquiryEndpoint", new InquiryEndpoint());

		publish(addressPrefix, "/HinemosWS/JobMapEndpoint", new JobMapEndpoint());

		publish(addressPrefix, "/HinemosWS/NodeMapEndpoint", new NodeMapEndpoint());

		publish(addressPrefix, "/HinemosWS/ReportingEndpoint", new ReportingEndpoint());
		// Reportingオプションで出力されたイベント（具体的にはPluginIDがREPORTING）のオーナロールを決定するクラスを登録する
		//ObjectSharingService.objectRegistry().put(INotifyOwnerDeterminer.class, HinemosModuleConstant.REPORTING, ReportingEventOwnerDeterminer.class);
		ObjectSharingService.objectRegistry().put(INotifyOwnerDeterminer.class, "REPORTING", ReportingEventOwnerDeterminer.class);

		publish(addressPrefix, "/HinemosWS/UtilityEndpoint", new UtilityEndpoint());
	}
}
