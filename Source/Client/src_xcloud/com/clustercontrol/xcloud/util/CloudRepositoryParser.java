/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import com.clustercontrol.xcloud.model.repository.EntityNode;
import com.clustercontrol.xcloud.model.repository.ICloudRepository;
import com.clustercontrol.xcloud.model.repository.ICloudScopeRootScope;
import com.clustercontrol.xcloud.model.repository.ICloudScopeScope;
import com.clustercontrol.xcloud.model.repository.IEntityNode;
import com.clustercontrol.xcloud.model.repository.IFolderScope;
import com.clustercontrol.xcloud.model.repository.IInstanceNode;
import com.clustercontrol.xcloud.model.repository.ILocationScope;
import com.clustercontrol.xcloud.model.repository.INode;
import com.clustercontrol.xcloud.model.repository.IScope;

public class CloudRepositoryParser {
	public static class Handler {
		public boolean cloudScopeRootScope(ICloudScopeRootScope s){return true;};
		public boolean cloudScopeScope(ICloudScopeScope s){return true;};
		public boolean locationScope(ILocationScope s){return true;};
		public boolean folder(IFolderScope s){return true;};
		public boolean scope(IScope s){return true;};
		public void instanceNode(IInstanceNode n){};
		public void entityNode(IEntityNode n){};
		public void node(INode n){};
		public void any(Object o){};
	}
	
	protected CloudRepositoryParser.Handler h;
	
	public CloudRepositoryParser(CloudRepositoryParser.Handler h) {
		this.h = h;
	}

	public void parse(ICloudRepository repository) {
		for (ICloudScopeRootScope o: repository.getRootScopes()) {
			recursive(o);
		}
	}

	public void parse(Object o) {
		recursive(o);
	}
	
	private void recursive(ICloudScopeRootScope s) {
		if (!h.cloudScopeRootScope(s)) return;
		for (Object c: s.getFacilities()) {
			recursive(c);
		}
	}
	
	private void recursive(Object o) {
		if (o instanceof ICloudScopeScope) {
			h.any(o);
			ICloudScopeScope s = (ICloudScopeScope)o;
			if (!h.cloudScopeScope(s)) return;
			for (Object c: s.getFacilities()) {
				recursive(c);
			}
		} else if (o instanceof ILocationScope) {
			h.any(o);
			ILocationScope s = (ILocationScope)o;
			if (!h.locationScope(s)) return;
			for (Object c: s.getFacilities()) {
				recursive(c);
			}
		} else if (o instanceof IFolderScope) {
			h.any(o);
			IFolderScope s = (IFolderScope)o;
			if (!h.folder(s)) return;
			for (Object c: s.getFacilities()) {
				recursive(c);
			}
		} else if (o instanceof IScope) {
			h.any(o);
			IScope s = (IScope)o;
			if (!h.scope(s)) return;
			for (Object c: s.getFacilities()) {
				recursive(c);
			}
		} else if (o instanceof IInstanceNode) {
			h.any(o);
			h.instanceNode((IInstanceNode)o);
		} else if (o instanceof EntityNode) {
			h.any(o);
			h.entityNode((IEntityNode)o);
		} else if (o instanceof INode) {
			h.any(o);
			h.node((INode)o);
		}
	}
	
	public static void parse(ICloudRepository repository, CloudRepositoryParser.Handler h) {
		new CloudRepositoryParser(h).parse(repository);
	}
	
	
	public static void parse(Object o, CloudRepositoryParser.Handler h) {
		new CloudRepositoryParser(h).parse(o);
	}
}
