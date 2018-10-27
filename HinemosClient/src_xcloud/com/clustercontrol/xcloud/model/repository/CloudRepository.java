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

import com.clustercontrol.ws.xcloud.HCloudScopeRootScope;
import com.clustercontrol.ws.xcloud.HCloudScopeScope;
import com.clustercontrol.ws.xcloud.HEntityNode;
import com.clustercontrol.ws.xcloud.HFacility;
import com.clustercontrol.ws.xcloud.HFolder;
import com.clustercontrol.ws.xcloud.HInstanceNode;
import com.clustercontrol.ws.xcloud.HLocationScope;
import com.clustercontrol.ws.xcloud.HNode;
import com.clustercontrol.ws.xcloud.HRepository;
import com.clustercontrol.ws.xcloud.HScope;
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

	public void update(HRepository repository) throws CloudModelException {
		CollectionComparator.compareCollection(rootScopes, repository.getFacilities(), new CollectionComparator.Comparator<CloudScopeRootScope, Object>(){
			@Override
			public boolean match(CloudScopeRootScope o1, Object o2) {
				return CloudRepository.this.equals(o1, (HCloudScopeRootScope)o2);
			}
			@Override
			public void matched(CloudScopeRootScope o1, Object o2) {
				recursiveUpdate(o1, (HCloudScopeRootScope)o2);
			}
			@Override
			public void afterO1(CloudScopeRootScope o1) {
				internalRemoveProperty(p.rootScopes, o1, rootScopes);
			}
			@Override
			public void afterO2(Object o2) {
				convertChild(null, (HFacility)o2);
			}
		});
	}
	
	public void updateLocation(final ILocation location, HRepository repository) throws CloudModelException {
		HRepositoryParser.parse(repository, new HRepositoryParser.Handler() {
			@Override
			public boolean cloudScopeScope(HCloudScopeScope s){
				if (s.getLocation() != null && location.getId().equals(s.getLocation().getId())) {
					recursiveUpdate((Scope)location.getCounterScope(), s);
					return false;
				}
				return true;
			};
			@Override
			public boolean locationScope(HLocationScope s){
				if (location.getId().equals(s.getLocation().getId())) {
					recursiveUpdate((Scope)location.getCounterScope(), s);
				}
				return false;
			};
		});
	}
	
	private boolean equals(Facility facility, HFacility hFacility) {
		if (!facility.getFacilityId().equals(hFacility.getId()))
			return false;
		
		if (
			hFacility instanceof HCloudScopeScope &&
			facility instanceof CloudScopeScope
			) {
			return true;
		} else if (
			hFacility instanceof HLocationScope &&
			facility instanceof LocationScope
			) {
			return true;
		} else if (
			hFacility instanceof HFolder &&
			facility instanceof FolderScope
			) {
			return true;
		} else if (
			hFacility instanceof HScope &&
			facility instanceof Scope
			) {
			return true;
		} else if (
			hFacility instanceof HNode &&
			facility instanceof Node
			) {
			return true;
		} else if (
			hFacility instanceof HInstanceNode &&
			facility instanceof InstanceNode
			) {
			return true;
		} else if (
			hFacility instanceof HEntityNode &&
			facility instanceof EntityNode
			) {
			return true;
		}

		return false;
	}
	
	private void recursiveUpdate(Facility facility, Object hFacility){
		if (
			hFacility instanceof HCloudScopeScope &&
			facility instanceof CloudScopeScope
			) {
			((CloudScopeScope)facility).update((HCloudScopeScope)hFacility);
			recursiveUpdateScope((Scope)facility, (HScope)hFacility);
		} else if (
			hFacility instanceof HLocationScope &&
			facility instanceof LocationScope
			) {
			((LocationScope)facility).update((HLocationScope)hFacility);
			recursiveUpdateScope((Scope)facility, (HScope)hFacility);
		} else if (
			hFacility instanceof HFolder &&
			facility instanceof FolderScope
			) {
			((FolderScope)facility).update((HFolder)hFacility);
			recursiveUpdateScope((FolderScope)facility, (HFolder)hFacility);
		} else if (
			hFacility instanceof HScope &&
			facility instanceof Scope
			) {
			((Scope)facility).update((HScope)hFacility);
			recursiveUpdateScope((Scope)facility, (HScope)hFacility);
		} else if (
			hFacility instanceof HNode &&
			facility instanceof Node
			) {
			((Node)facility).update((HNode)hFacility);
		} else if (
			hFacility instanceof HInstanceNode &&
			facility instanceof InstanceNode
			) {
			((InstanceNode)facility).update((HInstanceNode)hFacility);
		} else if (
			hFacility instanceof HEntityNode &&
			facility instanceof EntityNode
			) {
			((EntityNode)facility).update((HEntityNode)hFacility);
		}
	}

	@Override
	public HinemosManager getHinemosManager() {
		return (HinemosManager)getOwner();
	}

	private Scope recursiveUpdateScope(final Scope scope, final HScope hScope) {
		CollectionComparator.compareCollection(Arrays.asList(scope.getFacilities()), hScope.getFacilities(), new CollectionComparator.Comparator<Facility, Object>(){
			@Override
			public boolean match(Facility o1, Object o2) {
				return CloudRepository.this.equals(o1, (HFacility)o2);
			}
			@Override
			public void matched(Facility o1, Object o2) {
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
			public void afterO2(Object o2) {
				Facility childFacility = convertChild(null, o2);
				if (childFacility != null)
					scope.addFacility(childFacility);
			}
		});
		
		return scope;
	}
	
	private Scope convertScope(HScope hScope, Scope scope) {
		for (Object child: hScope.getFacilities()) {
			convertChild(scope, child);
		}
		return scope;
	}
	
	private Facility convertChild(Scope parent, Object hFacility) {
		if (hFacility instanceof HCloudScopeRootScope) {
			assert parent == null;
			
			HCloudScopeRootScope hRoot = (HCloudScopeRootScope)hFacility;
			CloudScopeRootScope root = CloudScopeRootScope.convert(this, hRoot);
			convertScope(hRoot, root);
			
			internalAddProperty(p.rootScopes, root, rootScopes);
			return root;
		} else if (hFacility instanceof HCloudScopeScope) {
			HCloudScopeScope hCloudScope = (HCloudScopeScope)hFacility;
			CloudScopeScope cloudScope = CloudScopeScope.convert(hCloudScope);
			convertScope(hCloudScope, cloudScope);
			
			if (parent != null)
				parent.addFacility(cloudScope);
			
			CloudScope cs = getHinemosManager().getCloudScopes().getCloudScope(((com.clustercontrol.ws.xcloud.CloudScope)hCloudScope.getCloudScope()).getId());
			cloudScope.setCloudScope(cs);
			cs.setCounterScope(cloudScope);
			
			if (hCloudScope.getLocation() != null) {
				Location l = cs.getLocation(hCloudScope.getLocation().getId());
				cloudScope.setLocation(l);
				l.setCounterScope(cloudScope);
			}
			
			return cloudScope;
		} else if (hFacility instanceof HLocationScope) {
			HLocationScope hLocation = (HLocationScope)hFacility;
			LocationScope location = LocationScope.convert(hLocation);
			convertScope(hLocation, location);
			
			if (parent != null)
				parent.addFacility(location);

			CloudScope cs = getHinemosManager().getCloudScopes().getCloudScope(((com.clustercontrol.ws.xcloud.CloudScope)((HCloudScopeScope)hLocation.getParent()).getCloudScope()).getId());
			Location l = cs.getLocation(hLocation.getLocation().getId());
			location.setLocation(l);
			l.setCounterScope(location);

			return location;
		} else if (hFacility instanceof HFolder) {
			HFolder hScope = (HFolder)hFacility;
			FolderScope scope = FolderScope.convert(hScope);
			convertScope(hScope, scope);

			if (parent != null)
				parent.addFacility(scope);
			
			return scope;
		} else if (hFacility instanceof HScope) {
			HScope hScope = (HScope)hFacility;
			Scope scope = Scope.convert(hScope);
			convertScope(hScope, scope);
			
			if (parent != null)
				parent.addFacility(scope);
			
			return scope;
		} else if (hFacility instanceof HInstanceNode) {
			HInstanceNode hInstanceNode = (HInstanceNode)hFacility;
			InstanceNode instanceNode = InstanceNode.convert(hInstanceNode);
			
			CloudScope cs = getHinemosManager().getCloudScopes().getCloudScope(((com.clustercontrol.ws.xcloud.Instance)hInstanceNode.getInstance()).getCloudScopeId());
			Location l = cs.getLocation(((com.clustercontrol.ws.xcloud.Instance)hInstanceNode.getInstance()).getLocationId());
			Instance i = l.getComputeResources().getInstance(((com.clustercontrol.ws.xcloud.Instance)hInstanceNode.getInstance()).getId());
			
			i.addCounterNode(instanceNode);
			if (parent != null)
				parent.addFacility(instanceNode);
			
			return instanceNode;
		} else if (hFacility instanceof HEntityNode) {
			HEntityNode hNode = (HEntityNode)hFacility;
			EntityNode node = EntityNode.convert(hNode);
			if (parent != null)
				parent.addFacility(node);
			return node;
		} else if (hFacility instanceof HNode) {
			HNode hNode = (HNode)hFacility;
			Node node = Node.convert(hNode);
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
