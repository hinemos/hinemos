package com.clustercontrol.accesscontrol.bean;

/**
 * オブジェクト権限チェックの対象用Bean
 * 
 */
public class ObjectPrivilegeTargetBean {

	// 対象オブジェクトのEntityクラス
	private Class<?> entityClass;
	// 対象オブジェクトの主キー
	private String objectId;
	// 対象オブジェクトのオーナーロールID
	private String ownerRoleId;
	// 削除する場合はtrue
	private boolean deleteFlg = false;
	// 削除時にオブジェクトチェックしない場合はtrue
	private boolean uncheckFlg;

	public ObjectPrivilegeTargetBean(
			Class<?> entityClass,
			String objectId,
			String ownerRoleId,
			boolean deleteFlg,
			boolean uncheckFlg) {
		this.entityClass = entityClass;
		this.objectId = objectId;
		this.ownerRoleId = ownerRoleId;
		this.deleteFlg = deleteFlg;
		this.uncheckFlg = uncheckFlg;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(
			Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public boolean isDeleteFlg() {
		return deleteFlg;
	}

	public void setDeleteFlg(boolean deleteFlg) {
		this.deleteFlg = deleteFlg;
	}

	public boolean isUncheckFlg() {
		return uncheckFlg;
	}

	public void setUncheckFlg(boolean uncheckFlg) {
		this.uncheckFlg = uncheckFlg;
	}

}