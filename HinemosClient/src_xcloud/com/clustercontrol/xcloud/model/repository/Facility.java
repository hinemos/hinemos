/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.repository;

import java.util.ArrayList;
import java.util.List;

import org.openapitools.client.model.ExtendedPropertyResponse;
import org.openapitools.client.model.HFacilityResponse;

import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.model.cloud.ExtendedProperty;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.ILocation;
import com.clustercontrol.xcloud.util.CollectionComparator;


public abstract class Facility extends Element implements IFacility {
	private List<ExtendedProperty> extendedProperties = new ArrayList<>();
	protected String facilityId;
	protected String name;
	
	@Override
	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		internalSetProperty(IFacility.p.name, name, ()->this.name, (s)->this.name=s);
	}
	
	@Override
	public ExtendedProperty[] getExtendedProperties() {
		return extendedProperties.toArray(new ExtendedProperty[extendedProperties.size()]);
	}
	@Override
	public String getExtendedProperty(String name) {
		for (ExtendedProperty property: extendedProperties) {
			if (property.getName().equals(name))
				return property.getValue();
		}
		return null;
	}
	
	protected void update(HFacilityResponse facility) {
		setFacilityId(facility.getId());
		setName(facility.getName());
		updateExtendedProperties(facility.getExtendedProperties());
	}
	
	protected void updateExtendedProperties(List<ExtendedPropertyResponse> extendedProperties) {
		CollectionComparator.compareCollection(this.extendedProperties, extendedProperties, new CollectionComparator.Comparator<ExtendedProperty, ExtendedPropertyResponse>(){
			@Override
			public boolean match(ExtendedProperty o1, ExtendedPropertyResponse o2) {
				return o1.getName().equals(o2.getName());
			}
			@Override
			public void matched(ExtendedProperty o1, ExtendedPropertyResponse o2) {
				o1.setValue(o2.getValue());
			}
			@Override
			public void afterO1(ExtendedProperty o1) {
				internalRemoveProperty(p.extendedProperties, o1, Facility.this.extendedProperties);
			}
			@Override
			public void afterO2(ExtendedPropertyResponse o2) {
				internalAddProperty(p.extendedProperties, ExtendedProperty.convert(o2), Facility.this.extendedProperties);
			}
		});
	}
	
	@Override
	public void visit(IVisitor visitor) {
		visitor.visit(this);
	}
	@Override
	public <T> T transform(ITransformer<T> transformor) {
		return transformor.transform(this);
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == ICloudScopeScope.class) {
			return getCloudScopeScope();
		} else if (adapter == ILocationScope.class) {
			return getLocationScope();
		} else if (adapter == ICloudScope.class) {
			ICloudScopeScope s = getCloudScopeScope();
			return s != null ? s.getCloudScope(): null;
		} else if (adapter == ILocation.class) {
			ILocationScope l = getLocationScope();
			if (l != null)
				return l.getLocation();
			ICloudScopeScope s = getCloudScopeScope();
			if (s != null)
				return s.getLocation();
		}
		return null;
	}

	@Override
	public String toString() {
		return "Facility [facilityId=" + facilityId + ", name=" + name + "]";
	}
}
