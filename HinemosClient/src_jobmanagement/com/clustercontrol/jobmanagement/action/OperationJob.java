/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.bean.Property;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;

/**
 * ジョブ操作を実行するクライアント側アクションクラス<BR>
 *
 * マネージャにSessionBean経由でアクセスし、ジョブ操作を実行する
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class OperationJob {

	/**
	 * マネージャにSessionBean経由でアクセスし、ジョブ操作を実行する
	 *
	 * @param property ジョブ開始操作用プロパティまたはジョブ停止操作用プロパティ
	 * @return 処理結果
	 *
	 */
	public boolean operationJob(String managerName, Property property) {
		boolean result = false;
		PropertyUtil.deletePropertyDefine(property);

		try {
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
			wrapper.operationJob(JobPropertyUtil.property2jobOperation(property));
			result = true;
		} catch (InvalidRole_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			if(e.getCause() instanceof NullPointerException){
				// 終了値未入力時、エラーダイアログを表示する
				MessageDialog.openError(
						null,
						Messages.getString("message"),
						Messages.getString("message.job.21"));
			}
			else if(e.getCause() instanceof IllegalStateException){
				// 実行エラー時、エラーダイアログを表示する
				MessageDialog.openError(
						null,
						Messages.getString("message"),
						Messages.getString("message.job.36"));
			}
			else{
				// 実行エラー時、エラーダイアログを表示する
				MessageDialog.openError(
						null,
						Messages.getString("message"),
						Messages.getString("message.job.34") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}
		return result;
	}
}
