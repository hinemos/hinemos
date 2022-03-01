/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import org.openapitools.client.model.HFacilityResponse;
import org.openapitools.client.model.HFacilityResponse.TypeEnum;
import org.openapitools.client.model.HRepositoryResponse;

public class HRepositoryParser {
	public static class Handler {
		public boolean cloudScopeRootScope(HFacilityResponse f){return true;};
		public boolean cloudScopeScope(HFacilityResponse f){return true;};
		public boolean locationScope(HFacilityResponse f){return true;};
		public boolean folder(HFacilityResponse f){return true;};
		public boolean scope(HFacilityResponse f){return true;};
		public void instanceNode(HFacilityResponse f){};
		public void entityNode(HFacilityResponse f){};
		public void node(HFacilityResponse f){};
	}
	
	protected HRepositoryResponse repository;
	
	public HRepositoryParser(HRepositoryResponse repository) {
		this.repository = repository;
	}

	public void parse(HRepositoryParser.Handler h) {
		for (HFacilityResponse f: repository.getFacilities()) {
			recursive(h, f);
		}
	}
	
	private void recursive(HRepositoryParser.Handler h, HFacilityResponse f) {
		if (f.getType() == TypeEnum.ROOT) {
			if (!h.cloudScopeRootScope(f)) return;
			for (HFacilityResponse c: f.getFacilities()) {
				recursive(h, c);
			}
		} else if (f.getType() == TypeEnum.CLOUDSCOPE) {
			if (!h.cloudScopeScope(f)) return;
			for (HFacilityResponse c: f.getFacilities()) {
				recursive(h, c);
			}
		} else if (f.getType() == TypeEnum.LOCATION) {
			if (!h.locationScope(f)) return;
			for (HFacilityResponse c: f.getFacilities()) {
				recursive(h, c);
			}
		} else if (f.getType() == TypeEnum.FOLDER) {
			if (!h.folder(f)) return;
			for (HFacilityResponse c: f.getFacilities()) {
				recursive(h, c);
			}
		} else if (f.getType() == TypeEnum.SCOPE) {
			if (!h.scope(f)) return;
			for (HFacilityResponse c: f.getFacilities()) {
				recursive(h, c);
			}
		} else if (f.getType() == TypeEnum.INSTANCE) {
			h.instanceNode(f);
		} else if (f.getType() == TypeEnum.ENTITY) {
			h.entityNode(f);
		} else if (f.getType() == TypeEnum.NODE) {
			h.node(f);
		}
	}
	
	public static void parse(HRepositoryResponse r, HRepositoryParser.Handler h) {
		new HRepositoryParser(r).parse(h);
	}
}
