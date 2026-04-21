package com.clustercontrol.plugin.enterprise;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.clustercontrol.accesscontrol.util.OptionManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ObjectSharingService;
import com.clustercontrol.notify.util.INotifyOwnerDeterminer;
import com.clustercontrol.reporting.ReportingEventOwnerDeterminer;
import com.clustercontrol.rest.endpoint.jobmap.JobMapRestEndpoints;
import com.clustercontrol.rest.endpoint.jobmap.JobMapRestFilterRegistration;
import com.clustercontrol.rest.endpoint.nodemap.NodeMapRestEndpoints;
import com.clustercontrol.rest.endpoint.nodemap.NodeMapRestFilterRegistration;
import com.clustercontrol.rest.endpoint.reporting.ReportingRestEndpoints;
import com.clustercontrol.rest.endpoint.reporting.ReportingRestFilterRegistration;
import com.clustercontrol.rest.endpoint.utility.UtilityRestEndpoints;
import com.clustercontrol.rest.endpoint.utility.UtilityRestFilterRegistration;

public class EnterprisePlugin2 extends EnterprisePlugin {

	private static final Log log = LogFactory.getLog(EnterprisePlugin2.class);
	
	/**
	 * RPA無効
	 */
	public static final String TYPE_NORPA = "norpa";

	@Override
	protected boolean isSupportedManagerVersion(String managerVersion) {
		return isManagerVersionAtLeast(managerVersion, 7, 2);
	}

	@Override
	protected String getSupportedManagerVersionDescription() {
		return "7.2 or later";
	}

	@Override
	public void activate() {
		validateManagerVersion();

		// Check if key exists
		if (!checkRequiredKeys()) {
			log.warn("KEY NOT FOUND! Unable to activate " + this.getClass().getName());
			return;
		}
		
		// RPAの無効でマークする
		OptionManager.add(TYPE_NORPA);
		
		String addressPrefix = HinemosPropertyCommon.rest_client_address.getStringValue();

		String nodeMapClassName = NodeMapRestEndpoints.class.getSimpleName();
		Set<Class<?>> registerClasseSetNodeMap = commonRegisterClasses();
		registerClasseSetNodeMap.add(NodeMapRestEndpoints.class);
		registerClasseSetNodeMap.add(NodeMapRestFilterRegistration.class);
		registerClasseSetNodeMap.add(MultiPartFeature.class);
		ResourceConfig nodeMapResourceConfig = new ResourceConfig().registerClasses(registerClasseSetNodeMap);
		publish(addressPrefix, BASE_URL + "/" + nodeMapClassName, nodeMapResourceConfig);

		String jobMapClassName = JobMapRestEndpoints.class.getSimpleName();
		Set<Class<?>> registerClasseSetJobMap = commonRegisterClasses();
		registerClasseSetJobMap.add(JobMapRestEndpoints.class);
		registerClasseSetJobMap.add(JobMapRestFilterRegistration.class);
		registerClasseSetJobMap.add(MultiPartFeature.class);
		ResourceConfig jobMapResourceConfig = new ResourceConfig().registerClasses(registerClasseSetJobMap);
		publish(addressPrefix, BASE_URL + "/" + jobMapClassName, jobMapResourceConfig);

		String reportingClassName = ReportingRestEndpoints.class.getSimpleName();
		Set<Class<?>> registerClasseSetReporting = commonRegisterClasses();
		registerClasseSetReporting.add(ReportingRestEndpoints.class);
		registerClasseSetReporting.add(ReportingRestFilterRegistration.class);
		ResourceConfig reportingResourceConfig = new ResourceConfig().registerClasses(registerClasseSetReporting);
		publish(addressPrefix, BASE_URL + "/" + reportingClassName, reportingResourceConfig);
		
		String utilityClassName = UtilityRestEndpoints.class.getSimpleName();
		Set<Class<?>> registerClasseSetUtility = commonRegisterClasses();
		registerClasseSetUtility.add(UtilityRestEndpoints.class);
		registerClasseSetUtility.add(UtilityRestFilterRegistration.class);
		ResourceConfig utilityResourceConfig = new ResourceConfig().registerClasses(registerClasseSetUtility);
		publish(addressPrefix, BASE_URL + "/" + utilityClassName, utilityResourceConfig);
		
		// Reportingオプションで出力されたイベント（具体的にはPluginIDがREPORTING）のオーナロールを決定するクラスを登録する
		// ObjectSharingService.objectRegistry().put(INotifyOwnerDeterminer.class,
		// HinemosModuleConstant.REPORTING,
		// ReportingEventOwnerDeterminer.class);
		ObjectSharingService.objectRegistry().put(INotifyOwnerDeterminer.class, "REPORTING",
				ReportingEventOwnerDeterminer.class);
	}
}
