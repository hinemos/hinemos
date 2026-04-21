/*
 * Copyright (c) 2026 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.dialog;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.JobOperationPropResponse.AvailableOperationListEnum;

import com.clustercontrol.jobmanagement.composite.DetailComposite.JobElement;
import com.clustercontrol.jobmanagement.view.action.StartSelectedJobDetailAction;
import com.clustercontrol.util.Messages;

/*
 * ジョブ履歴[ジョブ詳細]ビューで選択されたジョブの開始方法を指定するダイアログ
 */
public class SelectStartControlDialog extends AbstractSelectControlDialog {
	private static Log log = LogFactory.getLog(StartSelectedJobDetailAction.class);

	protected static class Viewer extends AbstractViewer {
		private static Log log = LogFactory.getLog(Viewer.class);
		
		public Viewer(Composite parent, Root root) {
			super(parent, root);
		}
		
		@Override
		protected String getImageKey(boolean enable) {
			return enable ? "IMG_RUN_KEY_ENABLE": "IMG_RUN_KEY_DISABLE";
		}

		@Override
		protected String getImagePath(boolean enable) {
			return enable ? "icons/enable/run.png": "icons/disable/run.png";
		}

		@Override
		protected Log getLogger() {
			return log;
		}
	}
	
	public SelectStartControlDialog(Shell parent, Set<JobElement> selected, Map<AvailableOperationListEnum, Set<JobElement>> jobMapByOpe) {
		super(parent, selected, jobMapByOpe);
	}

	@Override
	protected String getDialogTitle() {
		return Messages.getString("dialog.job.start.selected.job");
	}

	@Override
	protected Log getLogger() {
		return log;
	}

	@Override
	protected Viewer createViewer(Composite parent, Root root) {
		return new Viewer(parent, root);
	}
}
