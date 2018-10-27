/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.mbean.Manager;
import com.clustercontrol.mbean.ManagerMXBean;
import com.clustercontrol.plugin.api.HinemosPlugin;

public class SummaryPlugin implements HinemosPlugin {
	public static final Log log = LogFactory.getLog(SummaryPlugin.class);
	
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
		try {
			ManagementFactory.getPlatformMBeanServer().registerMBean(
					new Manager(),
					new ObjectName(
							"com.clustercontrol.mbean:type=" + ManagerMXBean.class.getSimpleName()));
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Override
	public void deactivate() {
	}

	@Override
	public void destroy() {
	}
}
