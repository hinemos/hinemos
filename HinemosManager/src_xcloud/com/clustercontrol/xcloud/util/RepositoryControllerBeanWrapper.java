/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.xcloud.persistence.PersistenceUtil.TransactionScope;
import com.clustercontrol.xcloud.persistence.Transactional;

public class RepositoryControllerBeanWrapper extends RepositoryControllerBean {
	private static ThreadLocal<RepositoryControllerBeanWrapper> instance  = new ThreadLocal<RepositoryControllerBeanWrapper>() {
		protected RepositoryControllerBeanWrapper initialValue()
		{
			return null;
		}
	};

	boolean autoCommit = false;

	private RepositoryControllerBeanWrapper() {
		super();
	}

	public static RepositoryControllerBeanWrapper bean() {
		RepositoryControllerBeanWrapper bean = instance.get();
		if (bean == null) {
			bean = new RepositoryControllerBeanWrapper();
			instance.set(bean);
		}
		return bean;
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public void addNode(NodeInfo nodeInfo) throws FacilityDuplicate, InvalidSetting, HinemosUnknown {
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				super.addNode(nodeInfo);
				scope.complete();
			}
		} else {
			super.addNode(nodeInfo);
		}
	}

	public void addNode(NodeInfo nodeInfo, boolean topicSendFlg)
			throws FacilityDuplicate, InvalidSetting, HinemosUnknown {
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				super.addNode(nodeInfo, topicSendFlg);
				scope.complete();
			}
		} else {
			super.addNode(nodeInfo, topicSendFlg);
		}
	}

	public void modifyNode(NodeInfo info) throws InvalidSetting, InvalidRole, HinemosUnknown {
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				super.modifyNode(info);
				scope.complete();
			}
		} else {
			super.modifyNode(info);
		}
	}

	public void deleteNode(String[] facilityIds) throws UsedFacility, InvalidRole, HinemosUnknown {
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				super.deleteNode(facilityIds);
				scope.complete();
			}
		} else {
			super.deleteNode(facilityIds);
		}
	}

	public void addScope(String parentFacilityId, ScopeInfo property)
			throws FacilityDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				super.addScope(parentFacilityId, property);
				scope.complete();
			}
		} else {
			super.addScope(parentFacilityId, property);
		}
	}

	public void addScope(String parentFacilityId, ScopeInfo property, int displaySortOrder)
			throws FacilityDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				super.addScope(parentFacilityId, property, displaySortOrder);
				scope.complete();
			}
		} else {
			super.addScope(parentFacilityId, property, displaySortOrder);
		}
	}

	public void addScope(String parentFacilityId, ScopeInfo info, int displaySortOrder, boolean topicSendFlg)
			throws FacilityDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				super.addScope(parentFacilityId, info, displaySortOrder, topicSendFlg);
				scope.complete();
			}
		} else {
			super.addScope(parentFacilityId, info, displaySortOrder, topicSendFlg);
		}
	}

	public void modifyScope(ScopeInfo info) throws InvalidSetting, InvalidRole, HinemosUnknown {
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				super.modifyScope(info);
				scope.complete();
			}
		} else {
			super.modifyScope(info);
		}
	}

	public void deleteScope(String[] facilityIds) throws UsedFacility, InvalidRole, HinemosUnknown {
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				super.deleteScope(facilityIds);
				scope.complete();
			}
		} else {
			super.deleteScope(facilityIds);
		}
	}

	public void assignNodeScope(String parentFacilityId, String[] facilityIds, boolean topicSendFlg)
			throws InvalidSetting, InvalidRole, HinemosUnknown {
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				super.assignNodeScope(parentFacilityId, facilityIds, topicSendFlg);
				scope.complete();
			}
		} else {
			super.assignNodeScope(parentFacilityId, facilityIds, topicSendFlg);
		}
	}

	public void assignNodeScope(String parentFacilityId, String[] facilityIds)
			throws InvalidSetting, InvalidRole, HinemosUnknown {
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				super.assignNodeScope(parentFacilityId, facilityIds);
				scope.complete();
			}
		} else {
			super.assignNodeScope(parentFacilityId, facilityIds);
		}
	}

	public void releaseNodeScope(String parentFacilityId, String[] facilityIds)
			throws InvalidSetting, InvalidRole, HinemosUnknown {
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				super.releaseNodeScope(parentFacilityId, facilityIds);
				scope.complete();
			}
		} else {
			super.releaseNodeScope(parentFacilityId, facilityIds);
		}
	}

	public void releaseNodeScope(String parentFacilityId, String[] facilityIds, boolean topicSendFlg)
			throws InvalidSetting, InvalidRole, HinemosUnknown {
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				super.releaseNodeScope(parentFacilityId, facilityIds, topicSendFlg);
				scope.complete();
			}
		} else {
			super.releaseNodeScope(parentFacilityId, facilityIds, topicSendFlg);
		}
	}

	public void addNodeWithoutRefresh(NodeInfo nodeInfo) throws FacilityDuplicate, InvalidSetting, HinemosUnknown {
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				super.addNodeWithoutRefresh(nodeInfo);
				scope.complete();
			}
		} else {
			super.addNodeWithoutRefresh(nodeInfo);
		}
	}
}
