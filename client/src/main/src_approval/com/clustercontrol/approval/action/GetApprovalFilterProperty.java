/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.approval.action;

import java.util.Locale;

import com.clustercontrol.approval.bean.ApprovalFilterPropertyConstant;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.bean.JobApprovalResultMessage;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.Messages;

/**
 * 承認ビューのフィルタ用プロパティを取得するクライアント側アクションクラス<BR>
 *
 * マネージャにSessionBean経由でアクセスし、フィルタ用プロパティを取得する
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class GetApprovalFilterProperty {

	/**
	 * マネージャにSessionBean経由でアクセスし、フィルタ用プロパティを取得する
	 *
	 * @return フィルタ用プロパティ
	 *
	 */
	public Property getProperty() {

		Locale locale = Locale.getDefault();

		//マネージャ
		Property m_manager =
				new Property(ApprovalFilterPropertyConstant.MANAGER, Messages.getString("facility.manager", locale),
						PropertyDefineConstant.EDITOR_SELECT);
		//承認状態
		Property m_status =
				new Property(ApprovalFilterPropertyConstant.APPROVAL_STATUS, Messages.getString("approval.status", locale), PropertyDefineConstant.EDITOR_TEXT);
		//承認状態（承認待）
		Property m_status_pending =
				new Property(ApprovalFilterPropertyConstant.APPROVAL_STATUS_PENDING, Messages.getString("approval.pending", locale), PropertyDefineConstant.EDITOR_BOOL);
		//承認状態（未承認）
		Property m_status_still =
				new Property(ApprovalFilterPropertyConstant.APPROVAL_STATUS_STILL, Messages.getString("approval.still", locale), PropertyDefineConstant.EDITOR_BOOL);
		//承認状態（中断中）
		Property m_status_suspend =
				new Property(ApprovalFilterPropertyConstant.APPROVAL_STATUS_SUSPEND, Messages.getString("approval.suspend", locale), PropertyDefineConstant.EDITOR_BOOL);
		//承認状態（停止(取り下げ)）
		Property m_status_stop =
				new Property(ApprovalFilterPropertyConstant.APPROVAL_STATUS_STOP, Messages.getString("approval.stop", locale), PropertyDefineConstant.EDITOR_BOOL);
		//承認状態（承認済）
		Property m_status_finished =
				new Property(ApprovalFilterPropertyConstant.APPROVAL_STATUS_FINISHED, Messages.getString("approval.finished", locale), PropertyDefineConstant.EDITOR_BOOL);
		
		//承認結果
		Property m_result =
				new Property(ApprovalFilterPropertyConstant.APPROVAL_RESULT, Messages.getString("approval.result", locale),
						PropertyDefineConstant.EDITOR_SELECT);
		//セッションID
		Property m_sessionId =
				new Property(ApprovalFilterPropertyConstant.SESSION_ID, Messages.getString("session.id", locale),
						PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//ジョブユニットID
		Property m_jobunitId =
				new Property(ApprovalFilterPropertyConstant.JOBUNIT_ID, Messages.getString("jobunit.id", locale),
						PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//ジョブID
		Property m_jobId =
				new Property(ApprovalFilterPropertyConstant.JOB_ID, Messages.getString("job.id", locale),
						PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//ジョブ名
		Property m_jobName =
				new Property(ApprovalFilterPropertyConstant.JOB_NAME, Messages.getString("job.name", locale),
						PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//実行ユーザ
		Property m_requestUser =
				new Property(ApprovalFilterPropertyConstant.RQUEST_USER, Messages.getString("approval.request.user", locale),
						PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//承認ユーザ
		Property m_approvalUser =
				new Property(ApprovalFilterPropertyConstant.APPROVAL_USER, Messages.getString("approval.user", locale),
						PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//承認依頼日時（開始）
		Property m_startFromDate =
				new Property(ApprovalFilterPropertyConstant.START_FROM_DATE, Messages.getString("start", locale),
						PropertyDefineConstant.EDITOR_DATETIME);
		//承認依頼日時（終了）
		Property m_startToDate =
				new Property(ApprovalFilterPropertyConstant.START_TO_DATE, Messages.getString("end", locale),
						PropertyDefineConstant.EDITOR_DATETIME);
		//承認完了日時（開始）
		Property m_endFromDate =
				new Property(ApprovalFilterPropertyConstant.END_FROM_DATE, Messages.getString("start", locale),
						PropertyDefineConstant.EDITOR_DATETIME);
		//承認完了日時（終了）
		Property m_endToDate =
				new Property(ApprovalFilterPropertyConstant.END_TO_DATE, Messages.getString("end", locale),
						PropertyDefineConstant.EDITOR_DATETIME);
		//承認依頼文
		Property m_reqSentence =
				new Property(ApprovalFilterPropertyConstant.RQUEST_SENTENCE, Messages.getString("approval.request.sentence", locale),
						PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.TEXT);
		//コメント
		Property m_comment =
				new Property(ApprovalFilterPropertyConstant.COMMENT, Messages.getString("comment", locale),
						PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//承認依頼日時
		Property m_startDate =
				new Property(ApprovalFilterPropertyConstant.START_DATE, Messages.getString("approval.request.time", locale),
						PropertyDefineConstant.EDITOR_TEXT);
		//承認完了日時
		Property m_endDate =
				new Property(ApprovalFilterPropertyConstant.END_DATE, Messages.getString("approval.completion.time", locale),
						PropertyDefineConstant.EDITOR_TEXT);
		
		
		Object[] obj = EndpointManager.getActiveManagerSet().toArray();
		Object[] val = new Object[obj.length + 1];
		val[0] = "";
		for(int i = 0; i<obj.length; i++) {
			val[i + 1] = obj[i];
		}
		Object[][] managerValues = {val, val};
		m_manager.setSelectValues(managerValues);
		m_manager.setValue("");

		//値を初期化（承認状態）		
		m_status.setValue("");
		m_status_pending.setValue(true);
		m_status_still.setValue(false);
		m_status_suspend.setValue(false);
		m_status_stop.setValue(false);
		m_status_finished.setValue(false);
		
		//値を初期化（承認結果）
		Object resultStatusV[] = { "", JobApprovalResultMessage.STRING_APPROVAL, JobApprovalResultMessage.STRING_DENIAL};
		Object resultValues[][] = {resultStatusV, resultStatusV};
		
		m_result.setSelectValues(resultValues);
		m_result.setValue("");
		
		m_sessionId.setValue("");
		m_jobunitId.setValue("");
		m_jobId.setValue("");
		m_jobName.setValue("");
		m_requestUser.setValue("");
		m_approvalUser.setValue("");
	
		m_startFromDate.setValue("");
		m_startToDate.setValue("");
		m_endFromDate.setValue("");
		m_endToDate.setValue("");
		
		m_reqSentence.setValue("");
		m_comment.setValue("");

		m_startDate.setValue("");
		m_endDate.setValue("");

		//変更の可/不可を設定
		m_manager.setModify(PropertyDefineConstant.MODIFY_OK);
		m_status_pending.setModify(PropertyDefineConstant.MODIFY_OK);
		m_status_still.setModify(PropertyDefineConstant.MODIFY_OK);
		m_status_suspend.setModify(PropertyDefineConstant.MODIFY_OK);
		m_status_stop.setModify(PropertyDefineConstant.MODIFY_OK);
		m_status_finished.setModify(PropertyDefineConstant.MODIFY_OK);
		m_result.setModify(PropertyDefineConstant.MODIFY_OK);
		m_sessionId.setModify(PropertyDefineConstant.MODIFY_OK);
		m_jobunitId.setModify(PropertyDefineConstant.MODIFY_OK);
		m_jobId.setModify(PropertyDefineConstant.MODIFY_OK);
		m_jobName.setModify(PropertyDefineConstant.MODIFY_OK);
		m_requestUser.setModify(PropertyDefineConstant.MODIFY_OK);
		m_approvalUser.setModify(PropertyDefineConstant.MODIFY_OK);
		m_startFromDate.setModify(PropertyDefineConstant.MODIFY_OK);
		m_startToDate.setModify(PropertyDefineConstant.MODIFY_OK);
		m_endFromDate.setModify(PropertyDefineConstant.MODIFY_OK);
		m_endToDate.setModify(PropertyDefineConstant.MODIFY_OK);
		m_reqSentence.setModify(PropertyDefineConstant.MODIFY_OK);
		m_comment.setModify(PropertyDefineConstant.MODIFY_OK);

		m_status.setModify(PropertyDefineConstant.MODIFY_NG);
		m_startDate.setModify(PropertyDefineConstant.MODIFY_NG);
		m_endDate.setModify(PropertyDefineConstant.MODIFY_NG);

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(m_manager);
		property.addChildren(m_status);
		property.addChildren(m_result);
		property.addChildren(m_sessionId);
		property.addChildren(m_jobunitId);
		property.addChildren(m_jobId);
		property.addChildren(m_jobName);
		property.addChildren(m_startDate);
		property.addChildren(m_endDate);
		property.addChildren(m_requestUser);
		property.addChildren(m_approvalUser);
		property.addChildren(m_reqSentence);
		property.addChildren(m_comment);
		
		// 承認状態
		m_status.removeChildren();
		m_status.addChildren(m_status_pending);
		m_status.addChildren(m_status_still);
		m_status.addChildren(m_status_suspend);
		m_status.addChildren(m_status_stop);
		m_status.addChildren(m_status_finished);
		
		// 承認依頼日時
		m_startDate.removeChildren();
		m_startDate.addChildren(m_startFromDate);
		m_startDate.addChildren(m_startToDate);

		// 承認完了日時
		m_endDate.removeChildren();
		m_endDate.addChildren(m_endFromDate);
		m_endDate.addChildren(m_endToDate);

		return property;
	}
}
