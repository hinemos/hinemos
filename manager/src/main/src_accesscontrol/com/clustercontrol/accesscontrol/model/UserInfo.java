package com.clustercontrol.accesscontrol.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 * The persistent class for the cc_cfg_user database table.
 * 
 */

@XmlType(namespace = "http://access.ws.clustercontrol.com")

@Entity
@Table(name="cc_cfg_user", schema="setting")
@Cacheable(true)
public class UserInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String userId;
	private Long createDate = 0l;
	private String createUserId = "";
	private String description = "";
	private Long modifyDate = 0l;
	private String modifyUserId = "";
	private String password = "";
	private String userName = "";
	private String userType = "";
	private String mailAddress = "";
	private List<RoleInfo> roleList;

	public UserInfo() {
	}

	@Id
	@Column(name="user_id")
	public String getUserId() {
		return this.userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Column(name="create_datetime")
	public Long getCreateDate() {
		return this.createDate;
	}
	public void setCreateDate(Long createDate) {
		this.createDate = createDate;
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
	public void setModifyDate(Long modifyDate) {
		this.modifyDate = modifyDate;
	}

	@Column(name="modify_user_id")
	public String getModifyUserId() {
		return this.modifyUserId;
	}
	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	@Column(name="password")
	public String getPassword() {
		return this.password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name="user_name")
	public String getUserName() {
		return this.userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Column(name="user_type")
	public String getUserType() {
		return this.userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	
	@Column(name="mail_address")
	public String getMailAddress() {
		return this.mailAddress;
	}
	public void setMailAddress(String mailAddress) {
		this.mailAddress = mailAddress;
	}

	@XmlTransient
	//bi-directional many-to-many association to RoleInfo
	@ManyToMany(mappedBy="userInfoList", cascade={CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
	public List<RoleInfo> getRoleList() {
		return this.roleList;
	}
	public void setRoleList(List<RoleInfo> roleList) {
		this.roleList = roleList;
	}

	/**
	 * RoleInfoリレーション削除処理<BR>
	 * 
	 * RoleInfoに存在するUserInfoを削除する。
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
	public void unchainRoleInfoList() {

		if (this.roleList != null && this.roleList.size() > 0) {
			for(RoleInfo roleInfo : this.roleList) {
				List<UserInfo> list = roleInfo.getUserInfoList();
				if (list != null) {
					Iterator<UserInfo> iter = list.iterator();
					while(iter.hasNext()) {
						UserInfo info = iter.next();
						if (info.getUserId().equals(this.getUserId())){
							iter.remove();
							break;
						}
					}
				}
			}
		}
	}
}