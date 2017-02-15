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

import com.clustercontrol.monitor.run.util.EventCache;
import com.clustercontrol.plugin.api.HinemosPlugin;

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
		EventCache.initEventCache(); // TODO CacheInitializerPluginを用意すること！
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
