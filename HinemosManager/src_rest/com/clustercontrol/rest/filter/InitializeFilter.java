/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.filter;


import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosSessionContext;

/**
 * REST向けスレッドの初期化用フィルター
 */
@Provider
@Priority(FilterPriorities.INITIALIZE)
public class InitializeFilter implements ContainerRequestFilter  {
	private static final Log log = LogFactory.getLog(InitializeFilter.class);
	@Override
	public void filter(ContainerRequestContext reqContext) {
		if (log.isDebugEnabled()) {
			log.debug("filter() start");
		}
		try {
			//スレッドローカルの変数をクリア
			HinemosSessionContext.instance().clearProperties();
		} catch (Exception e) {
			log.error("filter() : Exception=" + e.getMessage());
		}
	}
}
