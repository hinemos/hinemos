/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.extensions;

import org.eclipse.swt.graphics.Image;

public interface ICloudModelContentProvider {
	public Object getParent(Object o, Object defaultParent);
	public String getText(Object o, String defaultName);
	public Image getImage(Object o, Image defaultImage);
	public <T> T[] getChildren(Object o, T[] defaultChildren);
}
