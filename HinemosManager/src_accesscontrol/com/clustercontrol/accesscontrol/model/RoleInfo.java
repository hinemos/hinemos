/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlType;

/**
 * The persistent class for the cc_cfg_role database table.
 * 
 */

@XmlType(namespace = "http://access.ws.clustercontrol.com")

@Entity
@Table(name="cc_cfg_role", schema="setting")
@Cacheable(true)
public class RoleInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String roleId;
	private Long createDate = 0l;
	private String createUserId = "";
	private String description = "";
	private Long modifyDate = 0l;
	private String modifyUserId = "";
	private String roleName = "";
	private String roleType = "";
	private List<UserInfo> userInfoList;
	private List<SystemPrivilegeInfo> systemPrivilegeList ;

	public RoleInfo() {
	}

	@Id
	@Column(name="role_id")
	public String getRoleId() {
		return this.roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	@Column(name="create_datetime")
	public Long getCreateDate() {
		return this.createDate;
	}
	public void setCreateDate(Long createDatetime) {
		this.createDate = createDatetime;
	}

	@Column(name="create_user_id")
	public String getCreateUserId() {
		return this.createUserId;
	}
	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	@Column(name="description")
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="modify_datetime")
	public Long getModifyDate() {
		return this.modifyDate;
	}
	public void setModifyDate(Long modifyDatetime) {
		this.modifyDate = modifyDatetime;
	}

	@Column(name="modify_user_id")
	public String getModifyUserId() {
		return this.modifyUserId;
	}
	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	@Column(name="role_name")
	public String getRoleName() {
		return this.roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	@Column(name="role_type")
	public String getRoleType() {
		return this.roleType;
	}
	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}

	//bi-directional many-to-many association to UserInfo
	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(
			name="cc_cfg_user_role_relation"
			,schema="setting"
			, joinColumns={
					@JoinColumn(name="role_id", referencedColumnName="role_id")
			}
			, inverseJoinColumns={
					@JoinColumn(name="user_id", referencedColumnName="user_id")
			}
			)
	public List<UserInfo> getUserInfoList() {
		return this.userInfoList ;
	}
	public void setUserInfoList(List<UserInfo> userList) {
		if (userList != null && userList.size() > 0) {
			Collections.sort(userList, new Comparator<UserInfo>() {
				@Override
				public int compare(UserInfo o1, UserInfo o2) {
					return o1.getUserId().compareTo(o2.getUserId());
				}
			});
		}
		this.userInfoList  = userList ;
	}
	
	@Transient
	public List<String> getUserList() {
		if (userInfoList != null) {
			List<String> userList = new ArrayList<>(); 
			for (UserInfo u: userInfoList) {
				userList.add(u.getUserId());
			}
			return userList;
		}
		return Collections.emptyList();
	}

	public void setUserList(List<String> userList) {
		throw new UnsupportedOperationException();
	}

	//bi-directional many-to-many association to SystemPrivilegeInfo
	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(
			name="cc_system_privilege_role_relation"
			,schema="setting"
			, joinColumns={
					@JoinColumn(name="role_id", referencedColumnName="role_id")
			}
			, inverseJoinColumns={
					@JoinColumn(name="system_function", referencedColumnName="system_function"),
					@JoinColumn(name="system_privilege", referencedColumnName="system_privilege")
			}
			)
	public List<SystemPrivilegeInfo> getSystemPrivilegeList() {
		return this.systemPrivilegeList ;
	}
	public void setSystemPrivilegeList(List<SystemPrivilegeInfo> systemPrivilegeList ) {
		this.systemPrivilegeList  = systemPrivilegeList ;
	}

	/**
	 * UserInfoリレーション削除処理<BR>
	 * 
	 * UserInfoに存在するRoleInfoを削除する。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchainUserInfoList() {
		if (this.userInfoList != null && this.userInfoList.size() > 0) {
			for(UserInfo userInfo : this.userInfoList) {
				List<RoleInfo> list = userInfo.getRoleList();
				if (list != null) {
					Iterator<RoleInfo> iter = list.iterator();
					while(iter.hasNext()) {
						RoleInfo info = iter.next();
						if (info.getRoleId().equals(this.getRoleId())){
							iter.remove();
							break;
						}
					}
				}
			}
		}
	}


	/**
	 * SystemPrivilegeEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteSystemPrivilegeEntities(List<SystemPrivilegeInfoPK> notDelPkList) {
		List<SystemPrivilegeInfo> list = this.getSystemPrivilegeList();
		Iterator<SystemPrivilegeInfo> iter = list.iterator();
		while(iter.hasNext()) {
			SystemPrivilegeInfo entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				entity.getRoleList().remove(this);
			}
		}
	}

	/**
	 * SystemPrivilegeInfoリレーション削除処理<BR>
	 * 
	 * SystemPrivilegeInfoに存在するRoleInfoを削除する。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchainSystemPrivilegeInfoList() {
		if (this.systemPrivilegeList != null && this.systemPrivilegeList.size() > 0) {
			for(SystemPrivilegeInfo systemPrivilegeInfo : this.systemPrivilegeList) {
				List<RoleInfo> list = systemPrivilegeInfo.getRoleList();
				if (list != null) {
					Iterator<RoleInfo> iter = list.iterator();
					while(iter.hasNext()) {
						RoleInfo info = iter.next();
						if (info.getRoleId().equals(this.getRoleId())){
							iter.remove();
							break;
						}
					}
				}
			}
		}
	}
}