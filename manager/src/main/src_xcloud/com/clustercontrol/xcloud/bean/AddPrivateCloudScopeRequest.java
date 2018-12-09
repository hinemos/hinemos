/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.List;

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
import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.Into;
import com.clustercontrol.xcloud.validation.annotation.NotNull;
import com.clustercontrol.xcloud.validation.annotation.NotNullContainer;


@CustomEntityValidation(AddPrivateCloudScopeRequest.AccountValidator.class)
@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com") 
public class AddPrivateCloudScopeRequest extends AddCloudScopeRequest {
	public static class AccountValidator implements CustomEntityValidator<AddPrivateCloudScopeRequest>, ValidationConstants {
		@Override
		public void validate(final AddPrivateCloudScopeRequest entity, String group, EntityValidationContext context) throws PluginException {
			CloudManager.singleton().optionExecute(entity.getPlatformId(), new CloudManager.OptionExecutor() {
				@Override
				public void execute(ICloudOption option) throws CloudManagerException {
					option.visit(new ICloudOption.IVisitor() {
						@Override
						public void visit(IPrivateCloudOption cloudOption) throws CloudManagerException {
							cloudOption.validCredentialAsAccount(entity.getAccount().getCredential(), entity.getPrivateLocations());
						}
						@Override
						public void visit(IPublicCloudOption cloudOption) throws CloudManagerException {
							throw new CloudManagerException();
						}
					});
				}
			});
		}
	}
	
	private List<PrivateLocation> privateLocations;
	
	@ElementId("privateLocations")
	@NotNull
	@NotNullContainer
	@Into
	public List<PrivateLocation> getPrivateLocations() {
		return privateLocations;
	}
	public void setPrivateLocations(List<PrivateLocation> privateLocations) {
		this.privateLocations = privateLocations;
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
