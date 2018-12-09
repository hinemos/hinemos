/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.util;

import org.eclipse.rap.rwt.SingletonUtil;

public class JobmapImageCacheUtil extends JobmapIconImageCache  {
	private JobmapImageCacheUtil() {
	}
	public static JobmapImageCacheUtil getInstance(){
		return SingletonUtil.getSessionInstance(JobmapImageCacheUtil.class);
	}
}
