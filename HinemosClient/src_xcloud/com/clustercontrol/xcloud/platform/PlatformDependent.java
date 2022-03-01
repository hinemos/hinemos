/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.platform;

import java.io.File;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.BillingResultResponse.TypeEnum;

import com.clustercontrol.xcloud.ui.dialogs.DetailDialog;

public abstract class PlatformDependent {
	private static volatile PlatformDependent dependent = null;
	
	public static PlatformDependent getPlatformDependent() {
		if (dependent == null) {
			synchronized (PlatformDependent.class) {
				if (dependent == null) {
					ServiceLoader<PlatformDependent> loader = ServiceLoader.load(PlatformDependent.class);
					
					Iterator<PlatformDependent> iter = loader.iterator();
					if (!iter.hasNext())
						throw new IllegalStateException();
					
					dependent = iter.next();
				}
			}
		}
		return dependent;
	}
	
	public abstract boolean isRapPlatfome();

	public abstract DetailDialog createDetailDialog(Shell parentShell, String dialogTitle);

	public abstract void downloadBillingDetail(Shell parent, TypeEnum type, String targetId, int year, int month, File file) throws Exception;
}
