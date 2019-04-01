/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.bean.UpdateEventFilterInternal;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;

public class EventCacheModifyCallback implements JpaTransactionCallback {

	private static Log m_log = LogFactory.getLog( EventCache.class );
	
	private boolean addFlag = false; // add=true, modify=false
	private EventLogEntity entity = null;
	private Long removeGenerationDate = null;
	private boolean removeAllFlg = false;
	private UpdateEventFilterInternal filter = null;
	private Integer confirmFlg = null;
	private String confirmUser = null;
	private Long confirmDate = null;
	private String ownerRoleId = null;
	
	public EventCacheModifyCallback (boolean addFlag, EventLogEntity e) {
		this.addFlag = addFlag;
		this.entity = e;
	}
	public EventCacheModifyCallback(long generationDate, boolean removeAllFlg, String ownerRoleId) {
		this.removeGenerationDate = generationDate;
		this.removeAllFlg = removeAllFlg; // 全消しかどうか(falseの場合はconfirmFlgが1(確認)のもの)
		this.ownerRoleId = ownerRoleId; // nullの場合はownerRoleIdを条件に入れない
	}
	public EventCacheModifyCallback (
			UpdateEventFilterInternal filter,
			Integer confirmFlg,
			String confirmUser,
			Long confirmDate) {
		this.filter = filter;
		this.confirmFlg = confirmFlg;
		this.confirmUser = confirmUser;
		this.confirmDate = confirmDate;
	}
	
	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		if (this.removeGenerationDate != null) {
			//一括削除された時
			EventCache.removeEventCache(removeGenerationDate, removeAllFlg, ownerRoleId);
		} else if (addFlag) {
			//追加された時
			try (JpaTransactionManager jtm = new JpaTransactionManager()) {
				HinemosEntityManager em = jtm.getEntityManager();
				try {
					entity = QueryUtil.getEventLogPK(
							entity.getId().getMonitorId(),
							entity.getId().getMonitorDetailId(),
							entity.getId().getPluginId(),
							entity.getId().getOutputDate(),
							entity.getId().getFacilityId());
					
					em.refresh(entity);
					EventCache.addEventCache(entity);
				} catch (Exception e) {
					m_log.info("EventCache.addEventCache failure " + entity.toString(), e);
				}

			}
			
		} else {
			if (entity != null) {
				//１レコードの内容が更新された時
				EventCache.modifyEventCache(entity);
			} else {
				//確認がフィルタで一括変更された時
				EventCache.confirmEventCache(filter, confirmFlg, confirmDate, confirmUser);
			}
		}
	}

	@Override
	public void preRollback() { }

	@Override
	public void postRollback() { }

	@Override
	public void preClose() { }

	@Override
	public void postClose() {}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (addFlag ? 1231 : 1237);
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((removeGenerationDate == null) ? 0 : removeGenerationDate.hashCode());
		result = prime * result + (removeAllFlg ? 1231 : 1237);
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		result = prime * result + ((confirmDate == null) ? 0 : confirmDate.hashCode());
		result = prime * result + ((confirmFlg == null) ? 0 : confirmFlg.hashCode());
		result = prime * result + ((confirmUser == null) ? 0 : confirmUser.hashCode());
		result = prime * result + ((ownerRoleId == null) ? 0 : ownerRoleId.hashCode());
		return result;
		
	}

	@Override
	public boolean equals(Object obj) { 
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventCacheModifyCallback other = (EventCacheModifyCallback) obj;
		if (addFlag != other.addFlag)
			return false;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity)) {
			return false;
		}
		if (removeGenerationDate == null) {
			if (other.removeGenerationDate != null)
				return false;
		} else if (!removeGenerationDate.equals(other.removeGenerationDate)) {
			return false;
		}
		if (removeAllFlg != other.removeAllFlg)
			return false;
		if (filter == null) {
			if (other.filter != null)
				return false;
		} else if (!filter.equals(other.filter)) {
			return false;
		}
		if (confirmDate == null) {
			if (other.confirmDate != null)
				return false;
		} else if (!confirmDate.equals(other.confirmDate)) {
			return false;
		}
		if (confirmFlg == null) {
			if (other.confirmFlg != null)
				return false;
		} else if (!confirmFlg.equals(other.confirmFlg)) {
			return false;
		}
		if (confirmUser == null) {
			if (other.confirmUser != null)
				return false;
		} else if (!confirmUser.equals(other.confirmUser)) {
			return false;
		}
		if (ownerRoleId == null) {
			if (other.ownerRoleId != null)
				return false;
		} else if (!ownerRoleId.equals(other.ownerRoleId)) {
			return false;
		}
		return true;
	}
	
}
