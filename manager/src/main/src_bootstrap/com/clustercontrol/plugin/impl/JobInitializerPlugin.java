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

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.plugin.api.HinemosPlugin;

public class JobInitializerPlugin implements HinemosPlugin {
	public static final Log log = LogFactory.getLog(JobInitializerPlugin.class);

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(Log4jReloadPlugin.class.getName());
		dependency.add(CacheInitializerPlugin.class.getName());
		return dependency;
	}

	@Override
	public Set<String> getRequiredKeys() {
		return null;
	}

	@Override
	public void create() {
	}

	@Override
	public void activate() {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			JobMultiplicityCache.refresh();
			jtm.commit();
		} catch (Exception e) {
			log.error(e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	@Override
	public void deactivate() {
	}

	@Override
	public void destroy() {
	}

}
