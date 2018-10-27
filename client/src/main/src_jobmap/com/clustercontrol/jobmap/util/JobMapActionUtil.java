/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobModuleView;
import com.clustercontrol.jobmap.view.JobTreeView;

public class JobMapActionUtil {
	private final static Log m_log = LogFactory.getLog( JobMapActionUtil.class );
	
	public static JobTreeView getJobTreeView() {
		IViewPart view = getView(JobTreeView.ID);
		if (view == null) {
			return null;
		}
		return (JobTreeView)view;
	}
	
	public static JobTreeView getJobTreeModuleRegistView() {
		IViewPart view = getView(JobModuleView.ID);
		if (view == null) {
			return null;
		}
		return (JobTreeView)view;
	}
	
	public static JobMapEditorView getJobMapEditorView() {
		IViewPart view = getView(JobMapEditorView.ID);
		if (view == null) {
			return null;
		}
		return (JobMapEditorView)view;
	}

	private static IViewPart getView(String id) {
		//アクティブページを手に入れる
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		//ジョブマップ[設定]ビューを更新する
		IViewReference viewReference = page.findViewReference(id);
		if (viewReference == null){
			return null;
		}
		IViewPart viewPart = viewReference.getView(false);
		if (viewPart == null) {
			m_log.debug("viewPart is null. or ErrorViewPart.");
		}
		return viewPart;
	}
}
