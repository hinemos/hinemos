/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.common;

import java.io.Serializable;

public interface PropertyType<T> extends Serializable {
	PropertyType<Long> number = new PropertyType<Long>(){
		private static final long serialVersionUID = 1L;};
	PropertyType<Boolean> bool = new PropertyType<Boolean>(){
		private static final long serialVersionUID = 1L;};
	PropertyType<String> string = new PropertyType<String>(){
		private static final long serialVersionUID = 1L;};
}
