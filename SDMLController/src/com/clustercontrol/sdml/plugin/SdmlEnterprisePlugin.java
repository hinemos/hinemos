/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.plugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.clustercontrol.bean.ActivationKeyConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.plugin.impl.RestServicePlugin;
import com.clustercontrol.rest.endpoint.sdml.SdmlUtilityRestEndpoints;
import com.clustercontrol.rest.endpoint.sdml.SdmlUtilityRestFilterRegistration;

/**
 * SDMLでエンタープライズに関わる機能を有効化するプラグイン
 */
public class SdmlEnterprisePlugin extends RestServicePlugin implements HinemosPlugin {
	private static Log logger = LogFactory.getLog(SdmlEnterprisePlugin.class);

	@Override
	public Set<String> getRequiredKeys() {
		return new HashSet<>(Arrays.asList(ActivationKeyConstant.TYPE_ENTERPRISE));
	}

	@Override
	public void create() {
		logger.info("create() : creating " + getClass().getSimpleName() + "...");

		try {
			addressPrefix = HinemosPropertyCommon.rest_client_address.getStringValue();
		} catch (Exception e) {
			logger.error("create() : failed.", e);
		}
	}

	@Override
	public void activate() {
		logger.info("activate() : activating " + getClass().getSimpleName() + "...");
		// Check if key exists
		if (!this.checkRequiredKeys()) {
			logger.info("KEY NOT FOUND! Unable to activate " + this.getClass().getName());
			return;
		}

		try {

			String utilityclassName = SdmlUtilityRestEndpoints.class.getSimpleName();
			Set<Class<?>> registerClasseSetUtility = commonRegisterClasses();
			registerClasseSetUtility.add(SdmlUtilityRestEndpoints.class);
			registerClasseSetUtility.add(SdmlUtilityRestFilterRegistration.class);
			ResourceConfig utilityResourceConfig = new ResourceConfig().registerClasses(registerClasseSetUtility);
			publish(addressPrefix, BASE_URL + "/" + utilityclassName, utilityResourceConfig);

		} catch (Exception e) {
			logger.error("acivate() : failed.", e);
		}
	}

	@Override
	public void deactivate() {
		logger.info("deactivate() : deactivating " + getClass().getSimpleName() + "...");
		super.deactivate();
	}
}
