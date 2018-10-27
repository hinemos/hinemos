/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.repository;

import com.clustercontrol.xcloud.model.base.CollectionObserver;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.PropertyId;
import com.clustercontrol.xcloud.model.base.ValueObserver;
import com.clustercontrol.xcloud.model.cloud.IExtendedProperty;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;

public interface IFacility extends IElement {
	public static interface IVisitor {
		void visit(IFacility facility);
		void visit(INode node);
		void visit(IScope scope);
		void visit(ILocationScope scope);
		void visit(IFolderScope scope);
		void visit(ICloudScopeScope scope);
		void visit(ICloudScopeRootScope scope);
		void visit(IInstanceNode node);
		void visit(IEntityNode node);
	}
	public static interface ITransformer<T> {
		T transform(IFacility facility);
		T transform(INode node);
		T transform(IScope scope);
		T transform(ILocationScope scope);
		T transform(IFolderScope scope);
		T transform(ICloudScopeScope scope);
		T transform(ICloudScopeRootScope scope);
		T transform(IInstanceNode node);
		T transform(IEntityNode node);
	}
	
	// プロパティの Id
	interface p {
		static final PropertyId<ValueObserver<String>> name = new PropertyId<ValueObserver<String>>("name"){};
		static final PropertyId<CollectionObserver<IExtendedProperty>> extendedProperties = new PropertyId<CollectionObserver<IExtendedProperty>>("extendedProperties", true){};
	}

	String getFacilityId();
	String getName();
	
	ICloudRepository getCloudRepository();
	IHinemosManager getHinemosManager();
	ICloudScopeScope getCloudScopeScope();
	ILocationScope getLocationScope();
	
	IExtendedProperty[] getExtendedProperties();
	String getExtendedProperty(String name);

	void visit(IVisitor visitor);
	<T> T transform(ITransformer<T> transformor);
}
