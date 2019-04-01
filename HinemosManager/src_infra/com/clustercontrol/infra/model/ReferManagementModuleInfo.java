/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.model;


import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Table;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.infra.bean.AccessInfo;
import com.clustercontrol.infra.bean.ModuleNodeResult;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.util.MessageConstant;

@Entity
@Table(name="cc_infra_refer_management_module_info", schema="setting")
@Inheritance
@DiscriminatorValue(ReferManagementModuleInfo.typeName)
@Cacheable(true)
public class ReferManagementModuleInfo extends InfraModuleInfo<ReferManagementModuleInfo> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int MESSAGE_SIZE = 1024;

	public static final String typeName = "ReferManagementModule";

	private String referManagementId;

	public ReferManagementModuleInfo() {
		this.setPrecheckFlg(false);
		this.setStopIfFailFlg(false);
	}
	
	public ReferManagementModuleInfo(InfraManagementInfo parent, String moduleId) {
		super(parent, moduleId);
		this.setPrecheckFlg(false);
		this.setStopIfFailFlg(false);
	}
	
	@Column(name="refer_management_id")
	public String getReferManagementId() {
		return referManagementId;
	}
	public void setReferManagementId(String referManagementId) {
		this.referManagementId = referManagementId;
	}
	
	@Override
	public String getModuleTypeName() {
		return typeName;
	}

	@Override
	public ModuleNodeResult run(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId, Map<String, String> paramMap) throws HinemosUnknown, InvalidUserPass {
		throw new UnsupportedOperationException("forbidden to call run() method");
	}

	@Override
	public ModuleNodeResult check(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId, Map<String, String> paramMap, boolean check) throws HinemosUnknown, InvalidUserPass {
		throw new UnsupportedOperationException("forbidden to call check() method");
	}

	@Override
	protected void validateSub(InfraManagementInfo infraManagementInfo) throws InvalidSetting, InvalidRole {
		// referManagementId
		CommonValidator.validateId(MessageConstant.INFRA_MANAGEMENT_ID.getMessage(), getReferManagementId(), 64);
	}

	@Override
	public boolean canPrecheck(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId) throws HinemosUnknown, InvalidUserPass {
		return this.getPrecheckFlg();
	}

	@Override
	public void beforeRun(String sessionId) {
		//Do Nothing
	}

	@Override
	public void afterRun(String sessionId) {
		//Do Nothing
	}

	@Override
	protected Class<ReferManagementModuleInfo> getEntityClass() {
		return ReferManagementModuleInfo.class;
	}

	@Override
	protected void overwriteCounterEntity(InfraManagementInfo management, ReferManagementModuleInfo module, HinemosEntityManager em) {
		module.setReferManagementId(getReferManagementId());
	}
}