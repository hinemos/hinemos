/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.util;

import java.util.List;

/**
 * Propertiesのユーティリティクラス
 * 
 * @version 5.0.b
 * @since 5.0.b
 */
public final class PropertiesUtil{

	/**
	 * Convert item list to SQL where clause
	 * 
	 * @return String SQL where clause
	 */
	public static String list2sql(String[] ary, String prefix, String suffix){
		StringBuffer clause = new StringBuffer();

		for( String item : ary){
			item = item.trim();

			// Prevent input miss
			if(0 == item.length()){
				continue;
			}

			if(0 < clause.length()){
				clause.append(',');
			}
			clause.append("'" + item + "'");
		}

		// Only add prefix and suffix when clause is not empty
		if(0 < clause.length()){
			// Add prefix and suffix if specified
			if(null != prefix)
				clause.insert(0, prefix);
			if(null != suffix)
				clause.append(suffix);
		}
		return clause.toString();
	}
	public static String list2sql(String listStr, String separator, String prefix, String suffix){
		return list2sql(listStr.split(separator), prefix, suffix);
	}
	public static String list2sql(List<String> list, String prefix, String suffix){
		return list2sql(list.toArray(new String[list.size()]), prefix, suffix);
	}
}
