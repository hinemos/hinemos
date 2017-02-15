/*

Copyright (C) 2013 NTT DATA Corporation

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

import com.clustercontrol.plugin.api.HinemosPlugin;

public class WebServiceStartHTTPSPlugin extends WebServicePlugin implements
HinemosPlugin {

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(WebServiceAgentPlugin.class.getName());
		dependency.add(WebServiceCorePlugin.class.getName());
		dependency.add(WebServiceJobMapPlugin.class.getName());
		dependency.add(WebServiceNodeMapPlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {
	}

	@Override
	public void activate() {
		super.startHTTPS();
	}
}
