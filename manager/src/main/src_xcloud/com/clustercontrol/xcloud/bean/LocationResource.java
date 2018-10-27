/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.clustercontrol.xcloud.CloudManagerException;

@XmlSeeAlso({Instance.class})
public abstract class LocationResource {
	public static interface IVisitor {
		void visit(Instance instance) throws CloudManagerException;
		void visit(Storage storage) throws CloudManagerException;
		void visit(Network network) throws CloudManagerException;
	}
	public static interface ITransformer<T> {
		T transform(Instance instance) throws CloudManagerException;
		T transform(Storage storage) throws CloudManagerException;
		T transform(Network network) throws CloudManagerException;
	}

	public static class Visitor implements IVisitor {
		@Override
		public void visit(Instance instance) throws CloudManagerException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void visit(Storage storage) throws CloudManagerException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void visit(Network network) throws CloudManagerException {
			throw new UnsupportedOperationException();
		}
	}
	
	public static class Transformer<T> implements ITransformer<T> {
		@Override
		public T transform(Instance instance) throws CloudManagerException {
			throw new UnsupportedOperationException();
		}
		@Override
		public T transform(Storage storage) throws CloudManagerException {
			throw new UnsupportedOperationException();
		}
		@Override
		public T transform(Network network) throws CloudManagerException {
			throw new UnsupportedOperationException();
		}
	}
	
	public static enum ResourceType {
		Instance,
		Storage,
		Network
	}
	
	private String cloudScopeId;
	private String locationId;
	private String id;
	private String name;
	private ResourceType resourceType;
	private String resourceTypeAsPlatform;
	private List<ExtendedProperty> extendedProperties = new ArrayList<>();

	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}

	public String getLocationId() {
		return locationId;
	}
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}
	
	final public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	final public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public ResourceType getResourceType() {
		return resourceType;
	}
	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceTypeAsPlatform() {
		return resourceTypeAsPlatform;
	}
	public void setResourceTypeAsPlatform(String resourceTypeAsPlatform) {
		this.resourceTypeAsPlatform = resourceTypeAsPlatform;
	}
	
	public List<ExtendedProperty> getExtendedProperties() {
		return extendedProperties;
	}
	public void setExtendedProperties(List<ExtendedProperty> extendedProperties) {
		this.extendedProperties = extendedProperties;
	}
	
	@XmlID
	public String getIdentity() {
		return String.valueOf(hashCode());
	}
	public void setIdentity(String identity) {
		throw new UnsupportedOperationException();
	}
	
	public abstract void visit(IVisitor visitor) throws CloudManagerException;

	public abstract <T> T transform(ITransformer<T> transformer) throws CloudManagerException;
}
