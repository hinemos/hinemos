/*
 * Copyright (c) 2026 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.view.action;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.JobOperationPropResponse;
import org.openapitools.client.model.JobOperationPropResponse.AvailableOperationListEnum;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.composite.DetailComposite.JobElement;
import com.clustercontrol.jobmanagement.dialog.SelectStopControlDialog;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/*
 * ジョブ[詳細]ビューで選択されたジョブを停止する
 */
public class StopSelectedJobDetailAction  extends AbstractSelectedJobDetailAction {
	/* ログ */
	private static Log log = LogFactory.getLog(StopSelectedJobDetailAction.class);
	
	/* アクションID */
	public static final String ID = StopSelectedJobDetailAction.class.getName();

	@Override
	protected List<AvailableOperationListEnum> getAvailableOperation(JobRestClientWrapper wrapper, String sessionId, String jobunitId, String jobId) {
		try {
			JobOperationPropResponse response = wrapper.getAvailableStopOperationSessionJob(sessionId, jobunitId, jobId);
			return response.getAvailableOperationList();
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
			throw new InternalError("values1 is null");
		} catch (JobInfoNotFound e) {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.job.122"));
			throw new InternalError("values is null.");
		} catch (Exception e) {
			getLogger().warn("getStopProperty(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			throw new InternalError("values1 is null");
		}
	}

	@Override
	protected Log getLogger() {
		return log;
	}

	@Override
	protected SelectStopControlDialog createDialog(Shell shell, Set<JobElement> selected,
			Map<AvailableOperationListEnum, Set<JobElement>> jobMapByOpe) {
		return new SelectStopControlDialog(shell, selected, jobMapByOpe);
	}

	@Override
	protected String getCommandId() {
		return ID;
	}
}
