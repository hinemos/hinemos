/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.util;

public class JobmapImageCacheUtil extends JobmapIconImageCache  {
	private static JobmapImageCacheUtil instance = new JobmapImageCacheUtil();

	private JobmapImageCacheUtil() {
	}
	public static JobmapImageCacheUtil getInstance(){
		return instance;
	}
}
