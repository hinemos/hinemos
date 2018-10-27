/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.clustercontrol.xcloud.Session;

@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.METHOD)
@ProcessedBy(ApplyUserName.ApplyUserNameProcessor.class)
public @interface ApplyUserName {
	public class ApplyUserNameProcessor extends AbstractProcessor<ApplyUserName> {
		@Override
		public void prePersist(Object entity) throws Exception {
			setMethod.invoke(entity, Session.current().getHinemosCredential().getUserId());
		}
		@Override
		public void preUpdate(Object entity) throws Exception {
			if (!annotation.onlyPersist()) {
				setMethod.invoke(entity, Session.current().getHinemosCredential().getUserId());
			}
		}
	}
	
	boolean onlyPersist() default false;
}
