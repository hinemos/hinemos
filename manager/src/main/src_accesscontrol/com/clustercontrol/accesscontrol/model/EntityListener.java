package com.clustercontrol.accesscontrol.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.ObjectPrivilegeTargetBean;
import com.clustercontrol.commons.util.HinemosSessionContext;

/**
 * EntityListenerクラス
 * 
 * 更新前、削除前にオブジェクト権限チェック対象に追加する。
 * オブジェクト権限チェックはCommit時に行う。
 *
 */
public class EntityListener {

	private static Log m_log = LogFactory.getLog(EntityListener.class);

	@PreUpdate
	public void preUpdate(ObjectPrivilegeTargetInfo entity) {
		m_log.debug("preUpdate() start : " + entity.getClass().getSimpleName() + "," + entity.getObjectId() + "," + entity.getOwnerRoleId());
		addObjectPrivilegeTargetList(entity, false);
	}

	@PreRemove
	public void preRemove(ObjectPrivilegeTargetInfo entity) {
		m_log.debug("preRemove() start : " + entity.getClass().getSimpleName() + "," + entity.getObjectId() + "," + entity.getOwnerRoleId());
		addObjectPrivilegeTargetList(entity, true);
	}

	private void addObjectPrivilegeTargetList(ObjectPrivilegeTargetInfo entity, boolean deleteFlg) {
		@SuppressWarnings("unchecked")
		List<ObjectPrivilegeTargetBean> targetList
		= (List<ObjectPrivilegeTargetBean>)HinemosSessionContext.instance().getProperty(HinemosSessionContext.OBJECT_PRIVILEGE_TARGET_LIST);
		if (targetList == null) {
			targetList = new ArrayList<ObjectPrivilegeTargetBean>();
		}
		ObjectPrivilegeTargetBean target
		= new ObjectPrivilegeTargetBean(entity.getClass(), entity.getObjectId(),
				entity.getOwnerRoleId(), deleteFlg, entity.tranGetUncheckFlg());
		// オブジェクト権限チェック対象追加
		targetList.add(target);
		// オブジェクトチェック対象外のフラグは元に戻す
		entity.tranSetUncheckFlg(false);
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.OBJECT_PRIVILEGE_TARGET_LIST, targetList);
	}
}
