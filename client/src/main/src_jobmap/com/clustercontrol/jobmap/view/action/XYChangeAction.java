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

public class XYChangeAction extends BaseAction {
	public static final String ID = ActionIdBase + XYChangeAction.class.getSimpleName();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
		
		if (viewPart instanceof JobMapEditorView) {
			JobMapEditorView view = (JobMapEditorView)viewPart.getAdapter(JobMapEditorView.class);
			view.setXyChange(!view.isXyChange());
			view.updateNotManagerAccess();
		} else if (viewPart instanceof JobMapHistoryView) {
			JobMapHistoryView view = (JobMapHistoryView)viewPart.getAdapter(JobMapHistoryView.class);
			view.setXyChange(!view.isXyChange());
			view.updateNotManagerAccess();
		}

		return null;
	}
}