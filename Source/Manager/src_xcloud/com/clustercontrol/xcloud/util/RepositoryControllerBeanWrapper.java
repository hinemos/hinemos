/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import java.util.List;

import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
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

	public NodeInfo addNode(NodeInfo nodeInfo) throws FacilityDuplicate, InvalidSetting, HinemosUnknown {
		NodeInfo ret;
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				ret = super.addNode(nodeInfo);
				scope.complete();
			}
		} else {
			ret = super.addNode(nodeInfo);
		}
		return ret;
	}

	public NodeInfo addNode(NodeInfo nodeInfo, boolean topicSendFlg)
			throws FacilityDuplicate, InvalidSetting, HinemosUnknown {
		NodeInfo ret;
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				ret = super.addNode(nodeInfo, topicSendFlg);
				scope.complete();
			}
		} else {
			ret = super.addNode(nodeInfo, topicSendFlg);
		}
		return ret;
	}

	public NodeInfo modifyNode(NodeInfo info) throws InvalidSetting, InvalidRole, FacilityNotFound, HinemosUnknown {
		NodeInfo ret;
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				ret = super.modifyNode(info);
				scope.complete();
			}
		} else {
			ret = super.modifyNode(info);
		}
		return ret;
	}

	public List<NodeInfo> deleteNode(String[] facilityIds) throws UsedFacility, InvalidRole, FacilityNotFound, HinemosUnknown {
		List<NodeInfo> ret;
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				ret = super.deleteNode(facilityIds);
				scope.complete();
			}
		} else {
			ret = super.deleteNode(facilityIds);
		}
		return ret;
	}

	public ScopeInfo addScope(String parentFacilityId, ScopeInfo property)
			throws FacilityDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		ScopeInfo ret;
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				ret = super.addScope(parentFacilityId, property);
				scope.complete();
			}
		} else {
			ret =super.addScope(parentFacilityId, property);
		}
		return ret;
	}

	public ScopeInfo addScope(String parentFacilityId, ScopeInfo info, int displaySortOrder)
			throws FacilityDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		ScopeInfo ret;
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				 ret = super.addScope(parentFacilityId, info, displaySortOrder);
				scope.complete();
			}
		} else {
			ret = super.addScope(parentFacilityId, info, displaySortOrder);
		}
		return ret;
	}

	public ScopeInfo modifyScope(ScopeInfo info) throws InvalidSetting, InvalidRole, FacilityNotFound, HinemosUnknown {
		ScopeInfo ret;
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				ret = super.modifyScope(info);
				scope.complete();
			}
		} else {
			ret = super.modifyScope(info);
		}
		return ret;
	}

	public List<ScopeInfo> deleteScope(String[] facilityIds) throws UsedFacility, InvalidRole, FacilityNotFound, HinemosUnknown {
		List<ScopeInfo> ret;
		if (autoCommit) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				ret = super.deleteScope(facilityIds);
				scope.complete();
			}
		} else {
			ret = super.deleteScope(facilityIds);
		}
		return ret;
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
