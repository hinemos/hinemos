/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.poller.impl;

import java.util.ArrayList;
import java.util.TreeMap;

import org.snmp4j.smi.OID;

/**
 * RootOidとOidの1対１のマッピング 
 */
public class RootOidAndOidMapping {
	private TreeMap<OID, OID> rootOidToOidMap = new TreeMap<OID, OID>();
	private TreeMap<OID, OID> oidToRootOidMap = new TreeMap<OID, OID>();
	
	/**
	 * 自身 vs 自身のマッピングを作成
	 * 
	 * @param rootOids ルートOIDリスト
	 */
	public RootOidAndOidMapping(OID[] rootOids) {
		for (OID oid : rootOids) {
			rootOidToOidMap.put(oid, oid);
			oidToRootOidMap.put(oid, oid);
		}
	}
	
	public RootOidAndOidMapping(RootOidAndOidMapping old) {
		rootOidToOidMap.putAll(old.rootOidToOidMap);
		oidToRootOidMap.putAll(old.oidToRootOidMap);
	}
	
	public OID getOidByRootOid(OID rootOid) {
		return rootOidToOidMap.get(rootOid);
	}
	
	public OID getRootOidByOid(OID oid) {
		return oidToRootOidMap.get(oid);
	}
	
	public void removeByOid(OID oid) {
		OID rootOid  = oidToRootOidMap.remove(oid);
		if (rootOid != null) {
			rootOidToOidMap.remove(rootOid);
		}
	}
	
	public void removeByRootOid(OID rootOid) {
		OID oid = rootOidToOidMap.remove(rootOid);
		if (oid != null) {
			oidToRootOidMap.remove(oid);
		}
	}
	
	public void put(OID rootOid, OID oid) {
		OID oldOid = rootOidToOidMap.put(rootOid, oid);
		if (oldOid != null) {
			oidToRootOidMap.remove(oldOid);
		}
		oidToRootOidMap.put(oid, rootOid);
	}
	
	public boolean isEmpty() {
		return rootOidToOidMap.isEmpty();
	}

	public ArrayList<OID> getOidList() {
		return new ArrayList<OID>(rootOidToOidMap.values());
	}
}
