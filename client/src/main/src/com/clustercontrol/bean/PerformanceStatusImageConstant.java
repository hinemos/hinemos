/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
