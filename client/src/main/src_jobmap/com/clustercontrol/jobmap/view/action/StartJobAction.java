/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyConstant;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.OperationMessage;
import com.clustercontrol.jobmanagement.action.OperationJob;
import com.clustercontrol.jobmanagement.bean.JobOperationConstant;
import com.clustercontrol.jobmanagement.dialog.JobOperationDialog;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmap.composite.JobMapComposite;
import com.clustercontrol.jobmap.figure.JobFigure;
import com.clustercontrol.jobmap.view.JobMapHistoryView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;

/**
 * ジョブ[履歴]・ジョブ[ジョブ詳細]・ジョブ[ノード詳細]ビューの「開始」のクライアント側アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class StartJobAction extends BaseAction {
	private static Log m_log = LogFactory.getLog( StartJobAction.class );
	public static final String ID = ActionIdBase + StartJobAction.class.getSimpleName();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
	
		String sessionId = null;
		String jobunitId = null;
		String jobId = null;
		String facilityId = null;
		String managerName = null;
		
		if (viewPart instanceof JobMapHistoryView) { // ボタンが押された場合
			JobMapHistoryView view = (JobMapHistoryView) viewPart
					.getAdapter(JobMapHistoryView.class);
			JobMapComposite composite = view.getCanvasComposite();
			JobFigure jobFigure = composite.getFocusFigure();

			if (jobFigure == null) {
				return null;
			}

			//セッションID取得
			sessionId = composite.getSessionId();
			//ジョブユニットID取得
			jobunitId = jobFigure.getJobTreeItem().getData().getJobunitId();
			//ジョブID取得
			jobId = jobFigure.getJobTreeItem().getData().getId();

			managerName = composite.getManagerName();
		}
		

		if (sessionId != null && sessionId.length() > 0 && managerName != null && jobunitId != null
				&& jobunitId.length() > 0 && jobId != null
				&& jobId.length() > 0) {

			JobOperationDialog dialog = new JobOperationDialog(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getShell());

			//プロパティ設定
			dialog.setProperty(getStartProperty(managerName, 
					sessionId, jobunitId, jobId, facilityId));
			dialog.setTitleText(Messages.getString("job") + "["
					+ Messages.getString("start") + "]");

			//ダイアログ表示
			if (dialog.open() == IDialogConstants.OK_ID) {
				//ジョブ開始
				OperationJob operation = new OperationJob();
				operation.operationJob(managerName, dialog.getProperty());
			}
		}
		
		return null;
	}

	/**
	 * 開始用プロパティ取得
	 * 
	 * @param managerName
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @return ジョブ開始操作用プロパティ
	 * 
	 */
	private Property getStartProperty(String managerName, String sessionId, String jobunitId, String jobId, String facilityId) {
		Locale locale = Locale.getDefault();
		//セッションID
		Property session =
			new Property(JobOperationConstant.SESSION, Messages.getString("session.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//ジョブユニットID
		Property jobUnit =
			new Property(JobOperationConstant.JOB_UNIT, Messages.getString("jobunit.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//ジョブID
		Property job =
			new Property(JobOperationConstant.JOB, Messages.getString("job.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//ファシリティID
		Property facility =
			new Property(JobOperationConstant.FACILITY, Messages.getString("facility.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//制御
		Property control =
			new Property(JobOperationConstant.CONTROL, Messages.getString("control", locale), PropertyDefineConstant.EDITOR_SELECT);

		List<Integer> values = null;
		try {
			values = JobEndpointWrapper.getWrapper(managerName).getAvailableStartOperation(sessionId, jobunitId, jobId, facilityId);
		} catch (InvalidRole_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"), Messages.getString("message.accesscontrol.16"));
			return null;
		} catch (Exception e) {
			m_log.warn("getStartProperty() getAvailableStartOperation, " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			return null;
		}

		// 値をtypeからStringに変換
		List<String> valuesStr = new ArrayList<String>();
		for (Integer controlType : values) {
			valuesStr.add(OperationMessage.typeToString(controlType));
		}
		
		//値を初期化
		if(values.size() >= 1){
			Object controlValues[][] = {valuesStr.toArray(), valuesStr.toArray()};
			control.setSelectValues(controlValues);
			control.setValue(valuesStr.get(0));
		}
		else{
			Object controlValues[][] = {{""}, {""}};
			control.setSelectValues(controlValues);
			control.setValue("");
		}

		session.setValue(sessionId);
		jobUnit.setValue(jobunitId);
		job.setValue(jobId);
		if(facilityId != null && facilityId.length() > 0){
			facility.setValue(facilityId);
		}
		else{
			facility.setValue("");
		}

		//変更の可/不可を設定
		session.setModify(PropertyConstant.MODIFY_NG);
		jobUnit.setModify(PropertyConstant.MODIFY_NG);
		job.setModify(PropertyConstant.MODIFY_NG);
		facility.setModify(PropertyConstant.MODIFY_NG);
		control.setModify(PropertyConstant.MODIFY_OK);

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(session);
		property.addChildren(jobUnit);
		property.addChildren(job);
		if(facilityId != null && facilityId.length() > 0){
			property.addChildren(facility);
		}
		property.addChildren(control);

		return property;
	}
}