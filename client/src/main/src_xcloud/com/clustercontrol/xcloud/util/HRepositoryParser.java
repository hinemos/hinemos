/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import com.clustercontrol.ws.xcloud.HCloudScopeRootScope;
import com.clustercontrol.ws.xcloud.HCloudScopeScope;
import com.clustercontrol.ws.xcloud.HEntityNode;
import com.clustercontrol.ws.xcloud.HFolder;
import com.clustercontrol.ws.xcloud.HInstanceNode;
import com.clustercontrol.ws.xcloud.HLocationScope;
import com.clustercontrol.ws.xcloud.HNode;
import com.clustercontrol.ws.xcloud.HRepository;
import com.clustercontrol.ws.xcloud.HScope;

public class HRepositoryParser {
	public static class Handler {
		public boolean cloudScopeRootScope(HCloudScopeRootScope s){return true;};
		public boolean cloudScopeScope(HCloudScopeScope s){return true;};
		public boolean locationScope(HLocationScope s){return true;};
		public boolean folder(HFolder s){return true;};
		public boolean scope(HScope s){return true;};
		public void instanceNode(HInstanceNode n){};
		public void entityNode(HEntityNode n){};
		public void node(HNode n){};
	}
	
	protected HRepository repository;
	
	public HRepositoryParser(HRepository repository) {
		this.repository = repository;
	}

	public void parse(HRepositoryParser.Handler h) {
		for (Object o: repository.getFacilities()) {
			recursive(h, o);
		}
	}
	
	private void recursive(HRepositoryParser.Handler h, Object o) {
		if (o instanceof HCloudScopeRootScope) {
			HCloudScopeRootScope s = (HCloudScopeRootScope)o;
			if (!h.cloudScopeRootScope(s)) return;
			for (Object c: s.getFacilities()) {
				recursive(h, c);
			}
		} else if (o instanceof HCloudScopeScope) {
			HCloudScopeScope s = (HCloudScopeScope)o;
			if (!h.cloudScopeScope(s)) return;
			for (Object c: s.getFacilities()) {
				recursive(h, c);
			}
		} else if (o instanceof HLocationScope) {
			HLocationScope s = (HLocationScope)o;
			if (!h.locationScope(s)) return;
			for (Object c: s.getFacilities()) {
				recursive(h, c);
			}
		} else if (o instanceof HFolder) {
			HFolder s = (HFolder)o;
			if (!h.folder(s)) return;
			for (Object c: s.getFacilities()) {
				recursive(h, c);
			}
		} else if (o instanceof HScope) {
			HScope s = (HScope)o;
			if (!h.scope(s)) return;
			for (Object c: s.getFacilities()) {
				recursive(h, c);
			}
		} else if (o instanceof HInstanceNode) {
			h.instanceNode((HInstanceNode)o);
		} else if (o instanceof HEntityNode) {
			h.entityNode((HEntityNode)o);
		} else if (o instanceof HNode) {
			h.node((HNode)o);
		}
	}
	
	public static void parse(HRepository r, HRepositoryParser.Handler h) {
		new HRepositoryParser(r).parse(h);
	}
}
