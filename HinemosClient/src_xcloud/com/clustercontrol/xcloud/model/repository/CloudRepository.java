/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openapitools.client.model.HFacilityResponse;
import org.openapitools.client.model.HFacilityResponse.TypeEnum;
import org.openapitools.client.model.HRepositoryResponse;

import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.model.cloud.CloudScope;
import com.clustercontrol.xcloud.model.cloud.HinemosManager;
import com.clustercontrol.xcloud.model.cloud.ILocation;
import com.clustercontrol.xcloud.model.cloud.Instance;
import com.clustercontrol.xcloud.model.cloud.Location;
import com.clustercontrol.xcloud.util.CloudRepositoryParser;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.HRepositoryParser;

public class CloudRepository extends Element implements ICloudRepository {
	private List<CloudScopeRootScope> rootScopes = new ArrayList<>();
	
	public CloudRepository(HinemosManager manager) {
		setOwner(manager);
	}

	public void update(HRepositoryResponse repository) throws CloudModelException {
		CollectionComparator.compareCollection(rootScopes, repository.getFacilities(), new CollectionComparator.Comparator<CloudScopeRootScope, HFacilityResponse>(){
			@Override
			public boolean match(CloudScopeRootScope o1, HFacilityResponse o2) {
				return CloudRepository.this.equals(o1, o2);
			}
			@Override
			public void matched(CloudScopeRootScope o1, HFacilityResponse o2) {
				recursiveUpdate(o1, o2);
			}
			@Override
			public void afterO1(CloudScopeRootScope o1) {
				internalRemoveProperty(p.rootScopes, o1, rootScopes);
			}
			@Override
			public void afterO2(HFacilityResponse o2) {
				convertChild(null, o2);
			}
		});
	}
	
	public void updateLocation(final ILocation location, HRepositoryResponse repository) throws CloudModelException {
		HRepositoryParser.parse(repository, new HRepositoryParser.Handler() {
			@Override
			public boolean cloudScopeScope(HFacilityResponse f){
				if (f.getLocation() != null && location.getId().equals(f.getLocation().getId())) {
					recursiveUpdate((Scope)location.getCounterScope(), f);
					return false;
				}
				return true;
			};
			@Override
			public boolean locationScope(HFacilityResponse f){
				if (location.getId().equals(f.getLocation().getId())) {
					recursiveUpdate((Scope)location.getCounterScope(), f);
				}
				return false;
			};
		});
	}
	
	private boolean equals(Facility facility, HFacilityResponse hFacility) {
		if (!facility.getFacilityId().equals(hFacility.getId()))
			return false;
		
		if (
			hFacility.getType() == TypeEnum.ROOT &&
			facility instanceof CloudScopeRootScope
			) {
			return true;
		} else if (
			hFacility.getType() == TypeEnum.CLOUDSCOPE &&
			facility instanceof CloudScopeScope
			) {
			return true;
		} else if (
			hFacility.getType() == TypeEnum.LOCATION &&
			facility instanceof LocationScope
			) {
			return true;
		} else if (
			hFacility.getType() == TypeEnum.FOLDER &&
			facility instanceof FolderScope
			) {
			return true;
		} else if (
			hFacility.getType() == TypeEnum.SCOPE &&
			facility instanceof Scope
			) {
			return true;
		} else if (
			hFacility.getType() == TypeEnum.NODE &&
			facility instanceof Node
			) {
			return true;
		} else if (
			hFacility.getType() == TypeEnum.INSTANCE &&
			facility instanceof InstanceNode
			) {
			return true;
		} else if (
			hFacility.getType() == TypeEnum.ENTITY &&
			facility instanceof EntityNode
			) {
			return true;
		}

		return false;
	}
	
	private void recursiveUpdate(Facility facility, HFacilityResponse hFacility) {
		if (hFacility.getType() == TypeEnum.ROOT && facility instanceof CloudScopeRootScope) {
			((CloudScopeRootScope) facility).update(hFacility);
			recursiveUpdateScope((Scope) facility, hFacility);
		} else if (hFacility.getType() == TypeEnum.CLOUDSCOPE && facility instanceof CloudScopeScope) {
			((CloudScopeScope) facility).update(hFacility);
			recursiveUpdateScope((Scope) facility, hFacility);
		} else if (hFacility.getType() == TypeEnum.LOCATION && facility instanceof LocationScope) {
			((LocationScope) facility).update(hFacility);
			recursiveUpdateScope((Scope) facility, hFacility);
		} else if (hFacility.getType() == TypeEnum.FOLDER && facility instanceof FolderScope) {
			((FolderScope) facility).update(hFacility);
			recursiveUpdateScope((FolderScope) facility, hFacility);
		} else if (hFacility.getType() == TypeEnum.SCOPE && facility instanceof Scope) {
			((Scope) facility).update(hFacility);
			recursiveUpdateScope((Scope) facility, hFacility);
		} else if (hFacility.getType() == TypeEnum.NODE && facility instanceof Node) {
			((Node) facility).update(hFacility);
		} else if (hFacility.getType() == TypeEnum.INSTANCE && facility instanceof InstanceNode) {
			((InstanceNode) facility).update(hFacility);
		} else if (hFacility.getType() == TypeEnum.ENTITY && facility instanceof EntityNode) {
			((EntityNode) facility).update(hFacility);
		}
	}

	@Override
	public HinemosManager getHinemosManager() {
		return (HinemosManager)getOwner();
	}

	private Scope recursiveUpdateScope(final Scope scope, final HFacilityResponse hScope) {
		CollectionComparator.compareCollection(Arrays.asList(scope.getFacilities()), hScope.getFacilities(), new CollectionComparator.Comparator<Facility, HFacilityResponse>(){
			@Override
			public boolean match(Facility o1, HFacilityResponse o2) {
				return CloudRepository.this.equals(o1, o2);
			}
			@Override
			public void matched(Facility o1, HFacilityResponse o2) {
				recursiveUpdate(o1, o2);
			}
			@Override
			public void afterO1(Facility o1) {
				CloudRepositoryParser.parse(o1, new CloudRepositoryParser.Handler() {
					@Override
					public void instanceNode(IInstanceNode n) {
						n.getInstance().removeCounterNode((InstanceNode)n);
					}
				});
				scope.removeFacility(o1);
			}
			@Override
			public void afterO2(HFacilityResponse o2) {
				Facility childFacility = convertChild(null, o2);
				if (childFacility != null)
					scope.addFacility(childFacility);
			}
		});
		
		return scope;
	}
	
	private Scope convertScope(HFacilityResponse hScope, Scope scope) {
		for (HFacilityResponse child: hScope.getFacilities()) {
			convertChild(scope, child);
		}
		return scope;
	}
	
	private Facility convertChild(Scope parent, HFacilityResponse hFacility) {
		if (hFacility.getType() == TypeEnum.ROOT) {
			assert parent == null;
			
			CloudScopeRootScope root = CloudScopeRootScope.convert(this, hFacility);
			convertScope(hFacility, root);
			
			internalAddProperty(p.rootScopes, root, rootScopes);
			return root;
		} else if (hFacility.getType() == TypeEnum.CLOUDSCOPE) {
			CloudScopeScope cloudScope = CloudScopeScope.convert(hFacility);
			convertScope(hFacility, cloudScope);
			
			if (parent != null)
				parent.addFacility(cloudScope);
			
			CloudScope cs = getHinemosManager().getCloudScopes().getCloudScope(hFacility.getCloudScope().getEntity().getCloudScopeId());
			cloudScope.setCloudScope(cs);
			cs.setCounterScope(cloudScope);
			
			if (hFacility.getLocation() != null) {
				Location l = cs.getLocation(hFacility.getLocation().getId());
				cloudScope.setLocation(l);
				l.setCounterScope(cloudScope);
			}
			
			return cloudScope;
		} else if (hFacility.getType() == TypeEnum.LOCATION) {
			LocationScope location = LocationScope.convert(hFacility);
			convertScope(hFacility, location);
			
			if (parent != null)
				parent.addFacility(location);

			CloudScope cs = getHinemosManager().getCloudScopes().getCloudScope(hFacility.getParentCloudScopeId());
			Location l = cs.getLocation(hFacility.getLocation().getId());
			location.setLocation(l);
			l.setCounterScope(location);

			return location;
		} else if (hFacility.getType() == TypeEnum.FOLDER) {
			FolderScope scope = FolderScope.convert(hFacility);
			convertScope(hFacility, scope);

			if (parent != null)
				parent.addFacility(scope);
			
			return scope;
		} else if (hFacility.getType() == TypeEnum.SCOPE) {
			HFacilityResponse hScope = hFacility;
			Scope scope = Scope.convert(hScope);
			convertScope(hScope, scope);
			
			if (parent != null)
				parent.addFacility(scope);
			
			return scope;
		} else if (hFacility.getType() == TypeEnum.INSTANCE) {
			InstanceNode instanceNode = InstanceNode.convert(hFacility);
			
			CloudScope cs = getHinemosManager().getCloudScopes().getCloudScope(hFacility.getInstance().getCloudScopeId());
			Location l = cs.getLocation(hFacility.getInstance().getLocationId());
			Instance i = l.getComputeResources().getInstance(hFacility.getInstance().getId());
			
			i.addCounterNode(instanceNode);
			if (parent != null)
				parent.addFacility(instanceNode);
			
			return instanceNode;
		} else if (hFacility.getType() == TypeEnum.ENTITY) {
			EntityNode node = EntityNode.convert(hFacility);
			if (parent != null)
				parent.addFacility(node);
			return node;
		} else if (hFacility.getType() == TypeEnum.NODE) {
			Node node = Node.convert(hFacility);
			if (parent != null)
				parent.addFacility(node);
			return node;
		} else {
			return null;
		}
	}

	@Override
	public CloudScopeRootScope[] getRootScopes() {
		return rootScopes.toArray(new CloudScopeRootScope[rootScopes.size()]);
	}
}
