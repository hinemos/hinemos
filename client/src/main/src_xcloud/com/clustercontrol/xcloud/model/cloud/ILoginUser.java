/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import com.clustercontrol.ws.xcloud.Credential;
import com.clustercontrol.ws.xcloud.ModifyCloudLoginUserRequest;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.PropertyId;
import com.clustercontrol.xcloud.model.base.ValueObserver;

public interface ILoginUser extends IElement {
	// プロパティの Id
	public interface p {
		static final PropertyId<ValueObserver<String>> name = new PropertyId<ValueObserver<String>>("name"){};
		static final PropertyId<ValueObserver<String>> description = new PropertyId<ValueObserver<String>>("description"){};
		static final PropertyId<ValueObserver<Integer>> priority = new PropertyId<ValueObserver<Integer>>("priority"){};
		static final PropertyId<ValueObserver<String>> cloudUserType = new PropertyId<ValueObserver<String>>("cloudUserType"){};
		static final PropertyId<ValueObserver<RoleRelation[]>> roleRelations = new PropertyId<ValueObserver<RoleRelation[]>>("roleRelations"){};
		static final PropertyId<ValueObserver<Credential>> credential = new PropertyId<ValueObserver<Credential>>("credential"){};
		static final PropertyId<ValueObserver<Long>> updateDate = new PropertyId<ValueObserver<Long>>("updateDate"){};
		static final PropertyId<ValueObserver<String>> updateUser = new PropertyId<ValueObserver<String>>("updateUser"){};
	}

	String getId();
	String getName();
	String getDescription();
	String getCloudScopeId();
	String getCloudUserType();
	Integer getPriority();
	RoleRelation[] getRoleRelations();
	Credential getCredential();
	Long getRegDate();
	String getRegUser();
	Long getUpdateDate();
	String getUpdateUser();

	ILoginUsers getCloudUserManager();
	ILoginUser modifyCloudUser(ModifyCloudLoginUserRequest request);
	
	void addRoleRelation(String roleId);
	void removeRoleRelation(String roleId);
}
