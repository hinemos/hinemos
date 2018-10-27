/*

Copyright (C) 2014 NTT DATA Corporation

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

import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.custom.factory.SelectCustom;
import com.clustercontrol.jobmanagement.factory.FullJob;
import com.clustercontrol.logfile.session.MonitorLogfileControllerBean;
import com.clustercontrol.monitor.run.util.EventCache;
import com.clustercontrol.notify.util.MonitorStatusCache;
import com.clustercontrol.notify.util.NotifyCache;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.systemlog.util.SystemlogCache;
import com.clustercontrol.winevent.session.MonitorWinEventControllerBean;

public class CacheInitializerPlugin implements HinemosPlugin {
	public static final Log log = LogFactory.getLog(CacheInitializerPlugin.class);
	private static Set<String> osScopeIdSet = new HashSet<String>();

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(Log4jReloadPlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {
	}

	@Override
	public void activate() {
		new Thread("FullJobInitializerThread") {
			@Override
			public void run() {
				log.debug("FullJob init start.");
				new FullJob();
			}
		}.start();
		
		new Thread("EventCacheInitializerThread") {
			@Override
			public void run() {
				log.debug("EventCache init start.");
				EventCache.initEventCache();
			}
		}.start();
		
		new Thread("NodePropertyInitializerThread") {
			@Override
			public void run() {
				log.debug("NodeProperty init start.");
				new NodeProperty();
			}
		}.start();
		
		new Thread("MonitorStatusCacheInitializerThread") {
			@Override
			public void run() {
				log.debug("MonitorStatusCache init start.");
				new MonitorStatusCache();
			}
		}.start();

		new Thread("NotifyRelationCacheInitializerThread") {
			@Override
			public void run() {
				log.debug("NotifyRelationCache init start.");
				JpaTransactionManager jtm = null;
				jtm = new JpaTransactionManager();
				jtm.begin();
				new NotifyRelationCache();
				jtm.commit();
				jtm.close();
			}
		}.start();
		
		new Thread("UserRoleCacheInitializerThread") {
			@Override
			public void run() {
				log.debug("UserRoleCache init start.");
				new UserRoleCache();
			}
		}.start();
		
		new Thread("MonitorLogfileControllerBeanInitializerThread") {
			@Override
			public void run() {
				log.debug("MonitorLogfileControllerBean init start.");
				new MonitorLogfileControllerBean();
			}
		}.start();

		new Thread("NotifyCacheInitializerThread") {
			@Override
			public void run() {
				log.debug("NotifyCache init start.");
				new NotifyCache();
			}
		}.start();

		new Thread("FacilityTreeCacheInitializerThread") {
			@Override
			public void run() {
				log.debug("FacilityTreeCache init start.");
				JpaTransactionManager jtm = null;
				jtm = new JpaTransactionManager();
				jtm.begin();
				new FacilityTreeCache();
				jtm.commit();
				jtm.close();
			}
		}.start();

		new Thread("SystemlogCacheInitializerThread") {
			@Override
			public void run() {
				log.debug("SystemlogCache init start.");
				new SystemlogCache();
			}
		}.start();

		new Thread("MonitorWinEventControllerBeanInitializerThread") {
			@Override
			public void run() {
				log.debug("MonitorWinEventControllerBean init start.");
				new MonitorWinEventControllerBean();
			}
		}.start();

		new Thread("SelectCustomInitializerThread") {
			@Override
			public void run() {
				log.debug("SelectCustom init start.");
				new SelectCustom();
			}
		}.start();

	}

	@Override
	public void deactivate() {
	}

	@Override
	public void destroy() {
	}

	public static Set<String> getOsScopeIdSet() {
		return osScopeIdSet;
	}
}
