/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.plugin.impl.RestServicePlugin;
import com.clustercontrol.rest.endpoint.sdml.SdmlRestEndpoints;
import com.clustercontrol.rest.endpoint.sdml.SdmlRestFilterRegistration;
import com.clustercontrol.rest.exception.HinemosRestExceptionMapper;
import com.clustercontrol.rest.filter.ClientSettingAcquisitionFilter;
import com.clustercontrol.rest.filter.ClientVersionCheckFilter;

public class SdmlRestServiceClientPlugin extends RestServicePlugin implements HinemosPlugin {
	private static Log logger = LogFactory.getLog(SdmlRestServiceClientPlugin.class);

	// クリーナーはRestServiceClientPluginで起動しているため不要
	// RestTokenDataCleaner _tokencleaner;

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

		try {
			String sdmlClassName = SdmlRestEndpoints.class.getSimpleName();
			ResourceConfig sdmlResourceConfig = new ResourceConfig().registerClasses(SdmlRestEndpoints.class,
					SdmlRestFilterRegistration.class, ClientSettingAcquisitionFilter.class,
					HinemosRestExceptionMapper.class, ClientVersionCheckFilter.class);
			publish(addressPrefix, BASE_URL + "/" + sdmlClassName, sdmlResourceConfig);
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
