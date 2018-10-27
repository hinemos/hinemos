/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.validation.ValidationUtil.AbstractValidator;
import com.clustercontrol.xcloud.validation.annotation.ValidatedBy;

@Retention(RetentionPolicy.RUNTIME)  
@Target({ElementType.METHOD, ElementType.PARAMETER})
@ValidatedBy(com.clustercontrol.xcloud.validation.ModifiableCloudScope.Validator.class)
public @interface ModifiableCloudScope {
	public class Validator extends AbstractValidator<ModifiableCloudScope, String> {
		@Override
		public void init(ModifiableCloudScope annotation) {
			setElementId(annotation.elementId());
			setValidationId(annotation.validationId());
			setGroups(annotation.groups());
		}

		@Override
		protected void internalValidate(String property, String group) throws PluginException {
			if (property == null || property.isEmpty()) {
				return;
			}
			
			if (
				!Boolean.TRUE.equals(HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR))
				) {
				try {
					HinemosEntityManager em = Session.current().getEntityManager();
					Query query1 = em.createNamedQuery("findCloudScopeByHinemosUser");
					query1.setParameter("cloudScopeId", property);
					query1.setParameter("userId", Session.current().getHinemosCredential().getUserId());

					query1.getSingleResult();
				}
				catch (NoResultException e1) {
					throw ErrorCode.CLOUDSCOPE_INVALID_CLOUDSCOPE_NOT_FOUND.cloudManagerFault(property);
				}
			}
		}
	}
	
	String elementId() default "";
	String validationId() default "com.clustercontrol.xcloud.validation.ModifiableCloudScope";
	String[] groups() default {};
}
