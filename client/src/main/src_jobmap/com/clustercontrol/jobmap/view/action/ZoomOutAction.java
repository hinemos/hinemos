/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobMapHistoryView;

public class ZoomOutAction extends BaseAction {
	public static final String ID = ActionIdBase + ZoomOutAction.class.getSimpleName();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
		
		if (viewPart instanceof JobMapEditorView) {
			JobMapEditorView view = (JobMapEditorView)viewPart.getAdapter(JobMapEditorView.class);
			view.zoomOut();
			view.updateNotManagerAccess();
		} else if (viewPart instanceof JobMapHistoryView) {
			JobMapHistoryView view = (JobMapHistoryView)viewPart.getAdapter(JobMapHistoryView.class);
			view.zoomOut();
			view.updateNotManagerAccess();
		}
		
		return null;
	}
}