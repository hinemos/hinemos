package com.clustercontrol.accesscontrol.util;

/**
 * オブジェクト権限情報をまとめたクラス
 * 
 */

// TODO：権限が増えた場合はここに追加する必要あり

public class ObjectPrivilegeBean{
	private String m_roleId;
	private boolean m_readPrivilege = false;
	private boolean m_writePrivilege = false;
	private boolean m_execPrivilege = false;
	private boolean m_ownerFlag = false;
	private String m_createUser = "";
	private String m_createTime = "";
	private String m_modifyUser = "";
	private String m_modifyTime = "";

	public ObjectPrivilegeBean() {
	}

	public ObjectPrivilegeBean(String roleId,
			boolean readPrivilege, boolean writePrivilege, boolean execPrivilege,
			boolean ownerFlag) {
		this.m_roleId = roleId;
		this.m_readPrivilege = readPrivilege;
		this.m_writePrivilege = writePrivilege;
		this.m_execPrivilege = execPrivilege;
		this.m_ownerFlag = ownerFlag;
	}

	// roleId
	public String getRoleId() {
		return m_roleId;
	}

	public void setRoleId(String val) {
		this.m_roleId = val;
	}

	// readPrivilege
	public boolean getReadPrivilege() {
		return m_readPrivilege;
	}

	public void setReadPrivilege(boolean val) {
		this.m_readPrivilege = val;
	}

	// writePrivilege
	public boolean getWritePrivilege() {
		return m_writePrivilege;
	}

	public void setWritePrivilege(boolean val) {
		this.m_writePrivilege = val;
	}

	// execPrivilege
	public boolean getExecPrivilege() {
		return m_execPrivilege;
	}

	public void setExecPrivilege(boolean val) {
		this.m_execPrivilege = val;
	}

	// ownerFlag
	public boolean getOwnerFlag() {
		return m_ownerFlag;
	}

	public void setOwnerFlag(boolean val) {
		this.m_ownerFlag = val;
	}

	// createUser
	public String getCreateUser() {
		return m_createUser;
	}

	public void setCreateUser(String val) {
		this.m_createUser = val;
	}

	// createTime
	public String getCreateTime() {
		return m_createTime;
	}

	public void setCreateTime(String val) {
		this.m_createTime = val;
	}

	// modifyUser
	public String getModifyUser() {
		return m_modifyUser;
	}

	public void setModifyUser(String val) {
		this.m_modifyUser = val;
	}

	// modifyTime
	public String getModifyTime() {
		return m_modifyTime;
	}

	public void setModifyTime(String val) {
		this.m_modifyTime = val;
	}
}
