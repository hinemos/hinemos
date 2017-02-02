package com.clustercontrol.plugin.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.hub.session.HubControllerBean;
import com.clustercontrol.hub.util.StringDataIdGenerator;
import com.clustercontrol.plugin.api.HinemosPlugin;

public class HubPlugin implements HinemosPlugin {

	public static final Log log = LogFactory.getLog(HubPlugin.class);
	
	/**
	 * 
	 */
	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(SchedulerPlugin.class.getName());
		return dependency;
	}

	@Override
	public void activate() {
		StringDataIdGenerator.init();
		HubControllerBean.init();
	}

	@Override
	public void create() {
	}

	@Override
	public void deactivate() {
	}

	@Override
	public void destroy() {
	}
}
