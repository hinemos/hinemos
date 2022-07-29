/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import javax.xml.bind.annotation.XmlRootElement;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.ICloudOption;
import com.clustercontrol.xcloud.factory.IPrivateCloudOption;
import com.clustercontrol.xcloud.factory.IPublicCloudOption;
import com.clustercontrol.xcloud.validation.CustomEntityValidator;
import com.clustercontrol.xcloud.validation.EntityValidator.EntityValidationContext;
import com.clustercontrol.xcloud.validation.ValidationConstants;
import com.clustercontrol.xcloud.validation.annotation.CustomEntityValidation;


@CustomEntityValidation(AddPublicCloudScopeRequest.AccountValidator.class)
@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com") 
public class AddPublicCloudScopeRequest extends AddCloudScopeRequest {
	public static class AccountValidator implements CustomEntityValidator<AddPublicCloudScopeRequest>, ValidationConstants {
		@Override
		public void validate(final AddPublicCloudScopeRequest entity, String group, EntityValidationContext context) throws PluginException {
			CloudManager.singleton().optionExecute(entity.getPlatformId(), new CloudManager.OptionExecutor() {
				@Override
				public void execute(ICloudOption option) throws CloudManagerException {
					option.visit(new ICloudOption.IVisitor() {
						@Override
						public void visit(IPrivateCloudOption cloudOption) throws CloudManagerException {
							throw new CloudManagerException();
						}

						@Override
						public void visit(IPublicCloudOption cloudOption) throws CloudManagerException {
							cloudOption.validCredentialAsAccount(entity.getAccount().getCredential());
						}
					});
				}
			});
		}
	}

	@Override
	public void visit(IVisitor visitor) throws CloudManagerException, InvalidRole {
		visitor.visit(this);
	}
	@Override
	public <T> T transform(ITransformer<T> transformer) throws CloudManagerException, InvalidRole {
		return transformer.transform(this);
	}
}
