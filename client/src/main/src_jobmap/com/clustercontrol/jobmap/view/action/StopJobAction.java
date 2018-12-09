/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.OperationMessage;
import com.clustercontrol.jobmanagement.action.OperationJob;
import com.clustercontrol.jobmanagement.bean.JobOperationConstant;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.dialog.JobOperationDialog;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmap.composite.JobMapComposite;
import com.clustercontrol.jobmap.figure.JobFigure;
import com.clustercontrol.jobmap.view.JobMapHistoryView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;

/**
 * ジョブ[履歴]・ジョブ[ジョブ詳細]・ジョブ[ノード詳細]ビューの「停止」のクライアント側アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class StopJobAction extends BaseAction {
	private static Log m_log = LogFactory.getLog( StopJobAction.class );
	public static final String ID = ActionIdBase + StopJobAction.class.getSimpleName();

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

		if (sessionId != null && sessionId.length() > 0 && jobunitId != null && managerName != null
				&& jobunitId.length() > 0 && jobId != null
				&& jobId.length() > 0) {

			JobOperationDialog dialog = new JobOperationDialog(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getShell());

			//プロパティ設定
			dialog.setProperty(getStopProperty(managerName, 
					sessionId, jobunitId, jobId, facilityId));
			dialog.setTitleText(Messages.getString("job") + "["
					+ Messages.getString("stop") + "]");

			//ダイアログ表示
			if (dialog.open() == IDialogConstants.OK_ID) {
				//ジョブ停止
				OperationJob operation = new OperationJob();
				operation.operationJob(managerName, dialog.getProperty());
			}
		}
		
		return null;
	}

	/**
	 * 停止用プロパティ取得
	 * 
	 * @param managerName
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @return ジョブ停止操作用プロパティ
	 * 
	 */
	private Property getStopProperty(String managerName, String sessionId, String jobunitId, String jobId, String facilityId) {
		Locale locale = Locale.getDefault();

		//セッションID
		Property session =
			new Property(JobOperationConstant.SESSION, Messages.getString("session.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		session.setValue(sessionId);
		//ジョブユニットID
		Property jobUnit =
			new Property(JobOperationConstant.JOB_UNIT, Messages.getString("jobunit.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		jobUnit.setValue(jobunitId);
		//ジョブID
		Property job =
			new Property(JobOperationConstant.JOB, Messages.getString("job.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		job.setValue(jobId);
		//ファシリティID
		Property facility =
			new Property(JobOperationConstant.FACILITY, Messages.getString("facility.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		ArrayList<Property> endList = new ArrayList<Property>();
		if(facilityId != null && facilityId.length() > 0){
			facility.setValue(facilityId);
		}else{
			facility.setValue("");
		}
		
		//終了状態
		Property endStatus = null;
		if (facilityId == null) {
			endStatus = new Property(JobOperationConstant.END_STATUS, Messages.getString("end.status", locale), PropertyDefineConstant.EDITOR_SELECT);
			Object endStatusList[][] = {
					{"", EndStatusMessage.STRING_NORMAL, EndStatusMessage.STRING_WARNING, EndStatusMessage.STRING_ABNORMAL},
					{"", EndStatusConstant.TYPE_NORMAL, EndStatusConstant.TYPE_WARNING, EndStatusConstant.TYPE_ABNORMAL}
			};
			endStatus.setSelectValues(endStatusList);
			endStatus.setValue("");
			endList.add(endStatus);
		}
		//終了値
		Property endValue =
			new Property(JobOperationConstant.END_VALUE, Messages.getString("end.value", locale), PropertyDefineConstant.EDITOR_NUM,
					DataRangeConstant.SMALLINT_HIGH, DataRangeConstant.SMALLINT_LOW);
		endValue.setValue("");
		endList.add(endValue);
		
		//制御
		Property control =
			new Property(JobOperationConstant.CONTROL, Messages.getString("control", locale), PropertyDefineConstant.EDITOR_SELECT);

		HashMap<String, Object> mainteEndMap = new HashMap<String, Object>();
		mainteEndMap.put("value", OperationMessage.STRING_STOP_MAINTENANCE);
		mainteEndMap.put("property", endList);

		HashMap<String, Object> forceEndMap = new HashMap<String, Object>();
		forceEndMap.put("value", OperationMessage.STRING_STOP_FORCE);
		forceEndMap.put("property", endList);

		List<Integer> values1 = null;
		try {
			values1 = JobEndpointWrapper.getWrapper(managerName).getAvailableStopOperation(sessionId, jobunitId, jobId, facilityId);
		} catch (InvalidRole_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"), Messages.getString("message.accesscontrol.16"));
			return null;
		} catch (Exception e) {
			m_log.warn("getStopProperty() getAvailableStopOperation, " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			return null;
		}

		ArrayList<Object> values2 = new ArrayList<Object>();
		if(values1.contains(OperationConstant.TYPE_STOP_AT_ONCE)) {
			values2.add(OperationMessage.STRING_STOP_AT_ONCE);
		}
		if(values1.contains(OperationConstant.TYPE_STOP_SUSPEND)){
			values2.add(OperationMessage.STRING_STOP_SUSPEND);
		}
		if(values1.contains(OperationConstant.TYPE_STOP_WAIT)){
			values2.add(OperationMessage.STRING_STOP_WAIT);
		}
		if(values1.contains(OperationConstant.TYPE_STOP_SKIP)) {
			values2.add(OperationMessage.STRING_STOP_SKIP);
		}
		if(values1.contains(OperationConstant.TYPE_STOP_MAINTENANCE)) {
			values2.add(mainteEndMap);
		}
		if(values1.contains(OperationConstant.TYPE_STOP_FORCE)) {
			values2.add(forceEndMap);
		}

		List<String> stringValues = new ArrayList<String>();
		for (Integer type : values1) {
			stringValues.add(OperationMessage.typeToString(type));
		}
		Object controlValues[][] = {stringValues.toArray(), values2.toArray()};
		control.setSelectValues(controlValues);
		if(stringValues.size() >= 1){
			control.setValue(stringValues.get(0));
		}else{
			control.setValue("");
		}
		//変更の可/不可を設定
		session.setModify(PropertyDefineConstant.MODIFY_NG);
		jobUnit.setModify(PropertyDefineConstant.MODIFY_NG);
		job.setModify(PropertyDefineConstant.MODIFY_NG);
		facility.setModify(PropertyDefineConstant.MODIFY_NG);
		control.setModify(PropertyDefineConstant.MODIFY_OK);
		if (endStatus != null) {
			endStatus.setModify(PropertyDefineConstant.MODIFY_OK);
		}
		endValue.setModify(PropertyDefineConstant.MODIFY_OK);

		// 初期表示ツリーを構成。
		Property property = new Property(null, null, "");
		property.removeChildren();
		property.addChildren(session);
		property.addChildren(jobUnit);
		property.addChildren(job);
		if(facilityId != null && facilityId.length() > 0){
			property.addChildren(facility);
		}
		property.addChildren(control);

		String controlStr = (String)control.getValue();
		if(OperationMessage.STRING_STOP_MAINTENANCE.equals(controlStr) ||
						OperationMessage.STRING_STOP_FORCE.equals(controlStr)) {
			if (endStatus != null) {
				control.addChildren(endStatus);
			}
			control.addChildren(endValue);
		}


		return property;
	}
}