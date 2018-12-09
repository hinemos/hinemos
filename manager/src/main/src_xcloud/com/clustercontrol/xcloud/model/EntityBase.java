/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import com.clustercontrol.xcloud.persistence.EntityExtension;
import com.clustercontrol.xcloud.persistence.IDHolder;

@MappedSuperclass
@EntityListeners(EntityExtension.class)
public abstract class EntityBase implements IDHolder {
}
