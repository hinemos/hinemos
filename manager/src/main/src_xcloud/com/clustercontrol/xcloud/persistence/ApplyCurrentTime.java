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
import java.util.Date;

@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.METHOD)
@ProcessedBy(ApplyCurrentTime.ApplyCurrentTimeProcessor.class)
public @interface ApplyCurrentTime {
	public class ApplyCurrentTimeProcessor extends AbstractProcessor<ApplyCurrentTime> {
		@Override
		public void prePersist(Object entity) throws Exception {
			setMethod.invoke(entity, new Date().getTime());
		}
		@Override
		public void preUpdate(Object entity) throws Exception {
			if (!annotation.onlyPersist()) {
				setMethod.invoke(entity, new Date().getTime());
			}
		}
	}
	
	boolean onlyPersist() default false;
}
