/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.util.AuthorizingUtil;
import com.clustercontrol.xcloud.validation.CustomEntityValidator;
import com.clustercontrol.xcloud.validation.EntityValidator.EntityValidationContext;
import com.clustercontrol.xcloud.validation.ValidationConstants;
import com.clustercontrol.xcloud.validation.annotation.CustomEntityValidation;
import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.Identity;
import com.clustercontrol.xcloud.validation.annotation.IntRange;
import com.clustercontrol.xcloud.validation.annotation.NotNull;


@CustomEntityValidation(ModifyBillingSettingRequest.ModifyBillingSettingRequestValidator.class)
public class ModifyBillingSettingRequest extends Request {
	public static class ModifyBillingSettingRequestValidator implements CustomEntityValidator<ModifyBillingSettingRequest>, ValidationConstants {
		@Override
		public void validate(final ModifyBillingSettingRequest entity, String group, EntityValidationContext context) throws PluginException {
			if (!AuthorizingUtil.checkHinemousUser_administrators_account(Session.current().getHinemosCredential().getUserId(), entity.getCloudScopeId())) {
				throw ErrorCode.NEED_ADMINISTRATORS_ROLE_OR_ACCOUNT_USER.cloudManagerFault(Session.current().getHinemosCredential().getUserId(), entity.getCloudScopeId());
			}
		}
	}
	
	private String cloudScopeId;
	private boolean billingDetailCollectorFlg;
	private Integer retentionPeriod;

	public ModifyBillingSettingRequest() {
	}	

	@ElementId("XCLOUD_CORE_CLOUDSCOPE_ID")
	@Identity
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}
	
	@ElementId("XCLOUD_CORE_BILLINGDETAIL_COLLECTOR_FLG")
	@NotNull
	public Boolean isBillingDetailCollectorFlg() {
		return billingDetailCollectorFlg;
	}
	public void setBillingDetailCollectorFlg(Boolean billingDetailCollectorFlg) {
		this.billingDetailCollectorFlg = billingDetailCollectorFlg;
	}

	@ElementId("XCLOUD_CORE_RETENTION_PERIOD")
	@NotNull
	@IntRange(min=0, max=180)
	public Integer getRetentionPeriod() {
		return retentionPeriod;
	}

	public void setRetentionPeriod(Integer retentionPeriod) {
		this.retentionPeriod = retentionPeriod;
	}
}
