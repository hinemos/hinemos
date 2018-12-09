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
import com.clustercontrol.xcloud.InternalManagerError;

@XmlSeeAlso({HNode.class,HScope.class,HCloudScopeRootScope.class,HLocationScope.class,HEntityNode.class,HInstanceNode.class,HCloudScopeScope.class,HFolder.class})
public abstract class HFacility {
	public interface IVisitor {
		void visit(HCloudScopeScope cloudScope) throws CloudManagerException;
		void visit(HLocationScope location) throws CloudManagerException;
		void visit(HCloudScopeRootScope root) throws CloudManagerException;
		void visit(HScope scope) throws CloudManagerException;
		void visit(HNode node) throws CloudManagerException;
		void visit(HInstanceNode instance) throws CloudManagerException;
		void visit(HEntityNode enity) throws CloudManagerException;
	}

	public interface ITransformer<T> {
		T transform(HCloudScopeScope cloudScope) throws CloudManagerException;
		T transform(HLocationScope location) throws CloudManagerException;
		T transform(HCloudScopeRootScope root) throws CloudManagerException;
		T transform(HScope scope) throws CloudManagerException;
		T transform(HNode node) throws CloudManagerException;
		T transform(HInstanceNode instance) throws CloudManagerException;
		T transform(HEntityNode enity) throws CloudManagerException;
	}
	
	public static class Visitor implements IVisitor {
		@Override
		public void visit(HScope scope) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public void visit(HNode node) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public void visit(HInstanceNode instance) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public void visit(HEntityNode enity) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public void visit(HCloudScopeScope cloudScope) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public void visit(HLocationScope location) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public void visit(HCloudScopeRootScope root) throws CloudManagerException {
			throw new InternalManagerError();
		}
	}

	public static class Transformer<T> implements ITransformer<T> {
		@Override
		public T transform(HScope scope) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public T transform(HNode node) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public T transform(HInstanceNode instance) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public T transform(HEntityNode enity) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public T transform(HCloudScopeScope cloudScope) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public T transform(HLocationScope location) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public T transform(HCloudScopeRootScope root) throws CloudManagerException {
			throw new InternalManagerError();
		}
	}

	public static enum FacilityType {
		Root,
		CloudScope,
		Scope,
		Folder,
		Node,
		Instance,
		Entity,
		HNodeLinckage
	}
	
	private String id;
	private String name;
	private FacilityType type;
	private List<ExtendedProperty> extendedProperties = new ArrayList<>();
	
	public HFacility(FacilityType type) {
		this.type = type;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public FacilityType getType() {
		return type;
	}
	public void setType(FacilityType type) {
		this.type = type;
	}
	
	public List<ExtendedProperty> getExtendedProperties() {
		return extendedProperties;
	}
	public void setExtendedProperties(List<ExtendedProperty> extendedProperties) {
		this.extendedProperties = extendedProperties;
	}
	
	public abstract void visit(IVisitor visitor) throws CloudManagerException;

	public abstract <T> T transform(ITransformer<T> tranformer) throws CloudManagerException;

	@XmlID
	public String getIdentity() {
		return String.valueOf(hashCode());
	}
	public void setIdentity(String identity) {
		throw new UnsupportedOperationException();
	}
}
