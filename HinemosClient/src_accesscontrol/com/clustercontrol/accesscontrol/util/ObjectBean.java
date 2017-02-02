package com.clustercontrol.accesscontrol.util;

/**
 * オブジェクト権限情報(オブジェクト種別、オブジェクトID)をまとめたクラス
 *
 */


public class ObjectBean{
	private String m_managerName = "";
	private String m_objectType = "";
	private String m_objectId = "";

	public ObjectBean() {
	}

	public ObjectBean(String managerName, String objectType, String objectId) {
		this.m_managerName = managerName;
		this.m_objectType = objectType;
		this.m_objectId = objectId;
	}

	// オブジェクト種別
	public String getObjectType() {
		return m_objectType;
	}

	public void setObjectType(String objectType) {
		this.m_objectType = objectType;
	}

	// オブジェクトID
	public String getObjectId() {
		return m_objectId;
	}

	public void setObjectId(String objectId) {
		this.m_objectId = objectId;
	}

	/**
	 * @return the m_managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}

	/**
	 * @param m_managerName the m_managerName to set
	 */
	public void setManagerName(String m_managerName) {
		this.m_managerName = m_managerName;
	}

}
