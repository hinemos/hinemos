/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.performance.bean.PerformanceStatusConstant;

public class PerformanceStatusImageConstant extends PerformanceStatusConstant {

	private static Image running = null;

	private static Image stop = null;

	public static Image typeToImage(boolean type) {

		ImageRegistry registry = ClusterControlPlugin.getDefault().getImageRegistry();

		if (type) {
			if (running == null)
				running = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_BLUE).createImage();
			return running;
		} else  {
			if (stop == null)
				stop = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_RED).createImage();
			return stop;
		}

	}

}
