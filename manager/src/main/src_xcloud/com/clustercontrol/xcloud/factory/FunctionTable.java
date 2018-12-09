/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.clustercontrol.xcloud.InternalManagerError;

public class FunctionTable {
	private static Map<String, Set<String>> table = new HashMap<String, Set<String>>();
	
	public synchronized static void registerFunction(String functionId) {
		Set<String> set = table.get(functionId);
		if (set == null) {
			set = new HashSet<>();
			table.put(functionId, set);
		}
	}
	public synchronized static void enable(String functionId, String serviceId) {
		Set<String> set = table.get(functionId);
		if (set == null) {
			throw new InternalManagerError();
		}
		set.add(serviceId);
	}
	public synchronized static boolean isEnabled(String functionId, String serviceId) {
		Set<String> set = table.get(functionId);
		if (set == null) {
			throw new InternalManagerError();
		}
		return set.contains(serviceId);
	}
}
