/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;

/**
 * Wrapper class for Utility and Porting
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public interface IUtilityPreferenceStore {
	
	public void setDefault(String name, String value);
	public void setDefault(String name, int value);
    public String getString(String name);
	public int getInt(String name);

}
