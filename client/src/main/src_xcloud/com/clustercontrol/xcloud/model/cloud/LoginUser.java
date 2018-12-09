/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.CloudLoginUser;
import com.clustercontrol.ws.xcloud.CloudManagerException;
import com.clustercontrol.ws.xcloud.Credential;
import com.clustercontrol.ws.xcloud.InvalidRole_Exception;
import com.clustercontrol.ws.xcloud.InvalidUserPass_Exception;
import com.clustercontrol.ws.xcloud.ModifyCloudLoginUserRequest;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.Element;

public class LoginUser extends Element implements ILoginUser {
	private String id;
	private String name;
	private String description;
	private String cloudScopeId;
	private Integer priority;
	private String cloudUserType;
	private RoleRelation[] roleRelations;
	private Credential credential;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;

	private LoginUsers cloudUserManager;

	public LoginUser(LoginUsers container) {
		this.cloudUserManager = container;
	}

	@Override
	public String getId() {return id;}

	@Override
	public String getName() {return name;}

	@Override
	public String getDescription() {return description;}

	@Override
	public String getCloudScopeId() {return cloudScopeId;}

	@Override
	public Integer getPriority() {return priority;}

	@Override
	public String getCloudUserType() {return cloudUserType;}

	@Override
	public RoleRelation[] getRoleRelations() {return roleRelations;}

	@Override
	public Credential getCredential() {return credential;}

	@Override
	public Long getRegDate() {return regDate;}

	@Override
	public String getRegUser() {return regUser;}

	@Override
	public Long getUpdateDate() {return updateDate;}

	@Override
	public String getUpdateUser() {return updateUser;}

	@Override
	public LoginUsers getCloudUserManager() {return cloudUserManager;}

	public void setId(String id) {this.id = id;}

	public void setName(String name) {internalSetProperty(p.name, name, ()->this.name, (s)->this.name=s);}

	public void setDescription(String description) {internalSetProperty(p.description, description, ()->this.description, (s)->this.description=s);}

	public void setCloudScopeId(String cloudScopeId) {this.cloudScopeId = cloudScopeId;}

	public void setPriority(Integer priority) {internalSetProperty(p.priority, priority, ()->this.priority, (s)->this.priority=s);}

	public void setCloudUserType(String cloudUserType) {internalSetProperty(p.cloudUserType, cloudUserType, ()->this.cloudUserType, (s)->this.cloudUserType=s);}

	public void setRoleRelations(RoleRelation[] roleRelations) {internalSetProperty(p.roleRelations, roleRelations, ()->this.roleRelations, (s)->this.roleRelations=s);}

	public void setRegDate(Long regDate) {this.regDate = regDate;}

	public void setRegUser(String regUser) {this.regUser = regUser;}

	public void setUpdateDate(Long updateDate) {internalSetProperty(p.updateDate, updateDate, ()->this.updateDate, (s)->this.updateDate=s);}

	public void setUpdateUser(String updateUser) {internalSetProperty(p.updateUser, updateUser, ()->this.updateUser, (s)->this.updateUser=s);}

	public void setCredential(Credential credential) {internalSetProperty(p.credential, credential, ()->this.credential, (s)->this.credential=s);}

	public boolean equalValues(CloudLoginUser source) {
		return getId().equals(source.getId());
	}

	public CloudLoginUser getSource() throws CloudModelException {
		try {
			return getEndpoint(CloudEndpoint.class).getCloudLoginUser(cloudScopeId, id);
		} catch (CloudManagerException | InvalidRole_Exception | InvalidUserPass_Exception e) {
			throw new CloudModelException(e);
		}
	}

	protected void overwrite(CloudLoginUser source) {
		setId(source.getId());
		setName(source.getName());
		setCloudScopeId(source.getCloudScopeId());
		setCloudUserType(source.getCloudUserType().value());
		setCredential(source.getCredential());
		setDescription(source.getDescription());
		setPriority(source.getPriority());
		setRegDate(source.getRegDate());
		setRegUser(source.getRegUser());
		setUpdateDate(source.getUpdateDate());
		setUpdateUser(source.getUpdateUser());
		List<RoleRelation> tmpRoleRelations = new ArrayList<>();
		for(com.clustercontrol.ws.xcloud.RoleRelation relation: source.getRoleRelations()){
			RoleRelation tmpRelation = new RoleRelation();
			tmpRelation.set(relation);
			tmpRoleRelations.add(tmpRelation);
		}
		setRoleRelations(tmpRoleRelations.toArray(new RoleRelation[tmpRoleRelations.size()]));
	}

	@Override
	public LoginUser modifyCloudUser(ModifyCloudLoginUserRequest request) {
		try {
			overwrite(getEndpoint(CloudEndpoint.class).modifyCloudLoginUser(request));
			return this;
		} catch (CloudManagerException | InvalidRole_Exception | InvalidUserPass_Exception e) {
			throw new CloudModelException(e);
		}
	}

	private <T> T getEndpoint(Class<T> endpointClass){
		return getCloudUserManager().getCloudScope().getCounterScope().getHinemosManager().getEndpoint(endpointClass);
	}

	@Override
	public void addRoleRelation(String roleId) {
		for(RoleRelation relation: roleRelations){
			if(relation.getId().equals(roleId)){
				return;
			}
		}
		List<com.clustercontrol.ws.xcloud.RoleRelation> tmpRelations = getConvertedRoleRelations();
		com.clustercontrol.ws.xcloud.RoleRelation relation = new com.clustercontrol.ws.xcloud.RoleRelation();
		relation.setRoleId(roleId);
		tmpRelations.add(relation);
		modifyRoleRelations(tmpRelations);
	}

	@Override
	public void removeRoleRelation(String roleId) {
		List<com.clustercontrol.ws.xcloud.RoleRelation> tmpRelations = getConvertedRoleRelations();
		for(com.clustercontrol.ws.xcloud.RoleRelation relation: new ArrayList<>(tmpRelations)){
			if(relation.getRoleId().equals(roleId)){
				tmpRelations.remove(relation);
				modifyRoleRelations(tmpRelations);
			}
		}
	}
	
	private List<com.clustercontrol.ws.xcloud.RoleRelation> getConvertedRoleRelations(){
		List<com.clustercontrol.ws.xcloud.RoleRelation> tmpRelations = new ArrayList<>();
		for(RoleRelation relation: roleRelations){
			tmpRelations.add(relation.getDTO());
		}
		return tmpRelations;
	}
	
	private void modifyRoleRelations(List<com.clustercontrol.ws.xcloud.RoleRelation> roleRelations){
		try {
			overwrite(getEndpoint(CloudEndpoint.class).modifyCloudLoginUserRoleRelation(cloudScopeId, id, roleRelations));
		} catch (CloudManagerException | InvalidRole_Exception | InvalidUserPass_Exception e) {
			throw new CloudModelException(e);
		}
	}
}
