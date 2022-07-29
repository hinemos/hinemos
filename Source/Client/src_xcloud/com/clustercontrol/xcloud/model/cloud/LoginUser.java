/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.CloudLoginUserInfoResponse;
import org.openapitools.client.model.CredentialResponse;
import org.openapitools.client.model.ModifyCloudLoginUserRequest;
import org.openapitools.client.model.ModifyCloudLoginUserRoleRelationRequest;
import org.openapitools.client.model.RoleRelationRequest;
import org.openapitools.client.model.RoleRelationResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

public class LoginUser extends Element implements ILoginUser {
	private static Log m_log = LogFactory.getLog(LoginUser.class);

	private String id;
	private String name;
	private String description;
	private String cloudScopeId;
	private Integer priority;
	private String cloudUserType;
	private RoleRelation[] roleRelations;
	private CredentialResponse credential;
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
	public CredentialResponse getCredential() {return credential;}

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

	public void setCredential(CredentialResponse credential) {internalSetProperty(p.credential, credential, ()->this.credential, (s)->this.credential=s);}

	public boolean equalValues(CloudLoginUserInfoResponse source) {
		return getId().equals(source.getEntity().getLoginUserId());
	}

	public CloudLoginUserInfoResponse getSource() throws CloudModelException {
		try {
			return getWrapper().getCloudLoginUser(cloudScopeId, id);
		} catch (CloudManagerException | InvalidUserPass | InvalidRole | InvalidSetting | RestConnectFailed | HinemosUnknown e) {
			throw new CloudModelException(e);
		}
	}

	protected void overwrite(CloudLoginUserInfoResponse source) {
		setId(source.getEntity().getLoginUserId());
		setName(source.getEntity().getName());
		setCloudScopeId(source.getEntity().getCloudScopeId());
		setCloudUserType(source.getEntity().getCloudUserType().getValue());
		setCredential(source.getEntity().getCredential());
		setDescription(source.getEntity().getDescription());
		setPriority(source.getEntity().getPriority());
		try {
			setRegDate(TimezoneUtil.getSimpleDateFormat().parse(source.getEntity().getRegDate()).getTime());
		} catch (ParseException e) {
			// ここには入らない想定
			m_log.warn("invalid regTime.", e);
		}
		setRegUser(source.getEntity().getRegUser());
		try {
			setUpdateDate(TimezoneUtil.getSimpleDateFormat().parse(source.getEntity().getUpdateDate()).getTime());
		} catch (ParseException e) {
			// ここには入らない想定
			m_log.warn("invalid updateTime.", e);
		}
		setUpdateUser(source.getEntity().getUpdateUser());
		List<RoleRelation> tmpRoleRelations = new ArrayList<>();
		for(RoleRelationResponse relation: source.getEntity().getRoleRelations()){
			RoleRelation tmpRelation = new RoleRelation();
			tmpRelation.set(relation);
			tmpRoleRelations.add(tmpRelation);
		}
		setRoleRelations(tmpRoleRelations.toArray(new RoleRelation[tmpRoleRelations.size()]));
	}

	@Override
	public LoginUser modifyCloudUser(ModifyCloudLoginUserRequest request) {
		try {
			overwrite(getWrapper().modifyCloudLoginUser(getCloudScopeId(),getId(), request));
			return this;
		} catch (CloudManagerException | InvalidUserPass | InvalidRole | InvalidSetting | RestConnectFailed | HinemosUnknown e) {
			throw new CloudModelException(e);
		}
	}

	private CloudRestClientWrapper getWrapper(){
		return getCloudUserManager().getCloudScope().getCounterScope().getHinemosManager().getWrapper();
	}

	@Override
	public void addRoleRelation(String roleId) {
		for(RoleRelation relation: roleRelations){
			if(relation.getId().equals(roleId)){
				return;
			}
		}
		List<RoleRelationResponse> tmpRelations = getConvertedRoleRelations();
		RoleRelationResponse relation = new RoleRelationResponse();
		relation.setRoleId(roleId);
		tmpRelations.add(relation);
		modifyRoleRelations(tmpRelations);
	}

	@Override
	public void removeRoleRelation(String roleId) {
		List<RoleRelationResponse> tmpRelations = getConvertedRoleRelations();
		for(RoleRelationResponse relation: new ArrayList<>(tmpRelations)){
			if(relation.getRoleId().equals(roleId)){
				tmpRelations.remove(relation);
				modifyRoleRelations(tmpRelations);
			}
		}
	}
	
	private List<RoleRelationResponse> getConvertedRoleRelations(){
		List<RoleRelationResponse> tmpRelations = new ArrayList<>();
		for(RoleRelation relation: roleRelations){
			tmpRelations.add(relation.getDTO());
		}
		return tmpRelations;
	}
	
	private void modifyRoleRelations(List<RoleRelationResponse> roleRelations){
		ModifyCloudLoginUserRoleRelationRequest req = new ModifyCloudLoginUserRoleRelationRequest();
		List<RoleRelationRequest> roleReqList = new ArrayList<>();
		try {
			// レスポンスDTOをリクエストDTOに変換
			for (RoleRelationResponse roleRes : roleRelations){
				RoleRelationRequest roleReq = new RoleRelationRequest();
				RestClientBeanUtil.convertBean(roleRes, roleReq);
				roleReqList.add(roleReq);
			}
			req.setRoleRelations(roleReqList);
			
			overwrite(getWrapper().modifyCloudLoginUserRoleRelation(cloudScopeId, id, req));
		} catch (CloudManagerException | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | RestConnectFailed e) {
			throw new CloudModelException(e);
		}
	}
}
