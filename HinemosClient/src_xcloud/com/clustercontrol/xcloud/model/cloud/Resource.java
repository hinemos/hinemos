/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.CsvUtil;

public abstract class Resource extends Element implements IResource {
	
	private static final Log logger = LogFactory.getLog(Resource.class);
	
	private List<ExtendedProperty> extendedProperties = new ArrayList<>();
	
	public HinemosManager getHinemosManager() {
		if (getCloudScope() != null && getCloudScope().getCloudScopes() != null)
			return getCloudScope().getCloudScopes().getHinemosManager();
		return null;
	}

	public CloudScope getCloudScope() {
		if (getLocation() != null)
			return getLocation().getCloudScope();
		return null;
	}

	public abstract Location getLocation();
	
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
	@Override
	public List<String> getExtendedPropertyAsList(String name) {
		String prop = getExtendedProperty(name);
		if (prop == null)
			return Collections.emptyList();
		
		try (BufferedReader reader = new BufferedReader(new StringReader(prop))) {
			return Arrays.asList(CsvUtil.parseNextCsvLine(reader));
		} catch(IOException e) {
			logger.warn(e.getMessage(), e);
		}
		return Collections.emptyList();
	}
	
	protected void updateExtendedProperties(List<com.clustercontrol.ws.xcloud.ExtendedProperty> extendedProperties) {
		CollectionComparator.compareCollection(this.extendedProperties, extendedProperties, new CollectionComparator.Comparator<ExtendedProperty, com.clustercontrol.ws.xcloud.ExtendedProperty>(){
			@Override
			public boolean match(ExtendedProperty o1, com.clustercontrol.ws.xcloud.ExtendedProperty o2) {
				return o1.getName().equals(o2.getName());
			}
			@Override
			public void matched(ExtendedProperty o1, com.clustercontrol.ws.xcloud.ExtendedProperty o2) {
				o1.setValue(o2.getValue());
			}
			@Override
			public void afterO1(ExtendedProperty o1) {
				internalRemoveProperty(p.extendedProperties, o1, Resource.this.extendedProperties);
			}
			@Override
			public void afterO2(com.clustercontrol.ws.xcloud.ExtendedProperty o2) {
				internalAddProperty(p.extendedProperties, ExtendedProperty.convert(o2), Resource.this.extendedProperties);
			}
		});
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == ICloudScope.class) {
			return getCloudScope();
		} else if (adapter == ILocation.class) {
			return getLocation();
		} else if (adapter == IHinemosManager.class) {
			return getHinemosManager();
		}
		return super.getAdapter(adapter);
	}
}
