/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.action;

import java.util.ArrayList;
import java.util.HashMap;

import org.openapitools.client.model.JobObjectInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.bean.DecisionObjectMessage;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectMessage;
import com.clustercontrol.jobmanagement.editor.JobPropertyDefine;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.util.Messages;

/**
 * 待ち条件用プロパティを作成するクライアント側アクションクラス<BR>
 * 
 * @version 2.1.0
 * @since 1.0.0
 */
public class WaitRuleProperty {

	/** 名前 */
	public static final String ID_JUDGMENT_OBJECT = "judgmentObject";

	/** ジョブID */
	public static final String ID_JOB_ID = "jobId";

	/** セッション横断ジョブID */
	public static final String ID_CROSS_SESSION_JOB_ID = "crossSessionJobId";

	/** ジョブID (コマンドジョブのみ)*/
	public static final String ID_RETURN_VALUE_JOB_ID = "returnValueJobId";

	/** 値（終了状態） */
	public static final String ID_CONDITION_END_STATUS = "conditionEndStatus";

	/** 値（終了値） */
	public static final String ID_CONDITION_END_VALUE = "conditionEndValue";
	
	/** 値（戻り値） */
	public static final String ID_CONDITION_RETURN_VALUE = "conditionReturnValue";

	/** 時刻 */
	public static final String ID_TIME = "time";

	/** セッション開始時の時間（分）*/
	public static final String ID_START_MINUTE = "endStartMinute";

	/** 説明 */
	public static final String ID_DESCRIPTION = "description";

	/** 判定値1 */
	public static final String ID_DECISION_VALUE_1 = "decisionValue1";

	/** 判定条件 */
	public static final String ID_DECISION_CONDITION = "decisionCondition";

	/** 判定値2 */
	public static final String ID_DECISION_VALUE_2 = "decisionValue2";
	
	/** セッション横断待ち条件ジョブ履歴範囲（分） */
	public static final String ID_CROSS_SESSION_RANGE = "cross_session_range";

	/** 判定条件(ジョブ(戻り値)用) */
	public static final String ID_RETURN_VALUE_CONDITION = "returnValueCondition";

	/** 判定条件(終了値用) */
	public static final String ID_END_VALUE_CONDITION = "endValueCondition";

	/**
	 * 待ち条件用プロパティを取得します。<BR>
	 * 
	 * <p>
	 * <ol>
	 *  <li>待ち条件の設定項目毎にID, 名前, 処理定数（{@link com.clustercontrol.bean.PropertyDefineConstant}）を指定し、
	 *      プロパティ（{@link com.clustercontrol.bean.Property}）を生成します。</li>
	 *  <li>各設定項目のプロパティをツリー状に定義します。</li>
	 * </ol>
	 * 
	 * @param item
	 * @param typeEnum 種別
	 * @return 待ち条件用プロパティ
	 * 
	 * @see com.clustercontrol.bean.Property
	 * @see com.clustercontrol.bean.PropertyDefineConstant
	 * @see com.clustercontrol.bean.JudgmentObjectConstant
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public Property getProperty(JobTreeItemWrapper item, JobObjectInfoResponse.TypeEnum typeEnum) {

		//プロパティ項目定義
		Property judgmentObject = new Property(ID_JUDGMENT_OBJECT, Messages.getString("name"), PropertyDefineConstant.EDITOR_SELECT);
		Property job = new Property(ID_JOB_ID, Messages.getString("job.id"), PropertyDefineConstant.EDITOR_JOB);
		Property jobCrossSession = new Property(ID_CROSS_SESSION_JOB_ID, Messages.getString("job.id"), PropertyDefineConstant.EDITOR_JOB);
		Property jobReturnValue = new Property(ID_RETURN_VALUE_JOB_ID, Messages.getString("job.id"), PropertyDefineConstant.EDITOR_JOB);
		Property conditionEndStatus = new Property(ID_CONDITION_END_STATUS, 	Messages.getString("value"), PropertyDefineConstant.EDITOR_SELECT);
		Property conditionEndValue = new Property(ID_CONDITION_END_VALUE,
				Messages.getString("value"), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		Property conditionReturnValue = new Property(ID_CONDITION_RETURN_VALUE,
				Messages.getString("comparison.value"), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		Property time = new Property(ID_TIME, Messages.getString("wait.rule.time.example"), PropertyDefineConstant.EDITOR_TIME);
		Property startMinute = new Property(ID_START_MINUTE,
				Messages.getString("minute"), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.SMALLINT_HIGH, 0);
		Property description = new Property(ID_DESCRIPTION,
				Messages.getString("description"), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		Property decisionValue1 = new Property(ID_DECISION_VALUE_1,
				Messages.getString("wait.rule.decision.value1"), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		Property decisionCondition = new Property(ID_DECISION_CONDITION,
				Messages.getString("wait.rule.decision.condition"), PropertyDefineConstant.EDITOR_SELECT);
		Property decisionValue2 = new Property(ID_DECISION_VALUE_2,
				Messages.getString("wait.rule.decision.value2"), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		Property crossSessionRange = new Property(ID_CROSS_SESSION_RANGE,
				Messages.getString("wait.rule.cross.session.range"), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.SMALLINT_HIGH, 1);
		Property returnValueCondition = new Property(ID_RETURN_VALUE_CONDITION,
				Messages.getString("wait.rule.decision.condition"), PropertyDefineConstant.EDITOR_SELECT);
		Property endValueCondition = new Property(ID_END_VALUE_CONDITION,
				Messages.getString("wait.rule.decision.condition"), PropertyDefineConstant.EDITOR_SELECT);

		//JobPropertyDefineクラスはClusterControlでは定義されていない
		JobPropertyDefine define = new JobPropertyDefine(item);
		job.setDefine(define);
		// JobConstant.TYPE_REFERJOBを渡すことで参照ジョブと同様にジョブユニット配下を全て表示する
		JobPropertyDefine defineCrossSession = new JobPropertyDefine(item, JobInfoWrapper.TypeEnum.REFERJOB, null);
		jobCrossSession.setDefine(defineCrossSession);
		// コマンドジョブのみ表示するように設定
		ArrayList<JobInfoWrapper.TypeEnum> targetJobTypeList = new ArrayList<JobInfoWrapper.TypeEnum>();
		targetJobTypeList.add(JobInfoWrapper.TypeEnum.JOB);
		JobPropertyDefine defineReturn = new JobPropertyDefine(item, null, targetJobTypeList);
		jobReturnValue.setDefine(defineReturn);

		//ジョブ終了状態
		ArrayList<Object> jobEndStatusPropertyList = new ArrayList<Object>();
		jobEndStatusPropertyList.add(job);
		jobEndStatusPropertyList.add(conditionEndStatus);
		jobEndStatusPropertyList.add(description);
		
		HashMap<String, Object> jobEndStatusMap = new HashMap<String, Object>();
		jobEndStatusMap.put("value", JudgmentObjectMessage.STRING_JOB_END_STATUS);
		jobEndStatusMap.put("property", jobEndStatusPropertyList);

		//ジョブ終了値
		ArrayList<Object> jobEndValuePropertyList = new ArrayList<Object>();
		jobEndValuePropertyList.add(job);
		jobEndValuePropertyList.add(endValueCondition);
		jobEndValuePropertyList.add(conditionEndValue);
		jobEndValuePropertyList.add(description);
		
		HashMap<String, Object> jobEndValueMap = new HashMap<String, Object>();
		jobEndValueMap.put("value", JudgmentObjectMessage.STRING_JOB_END_VALUE);
		jobEndValueMap.put("property", jobEndValuePropertyList);

		//セッション横断ジョブ終了状態
		ArrayList<Object> jobCrossSessionEndStatusPropertyList = new ArrayList<Object>();
		jobCrossSessionEndStatusPropertyList.add(jobCrossSession);
		jobCrossSessionEndStatusPropertyList.add(conditionEndStatus);
		jobCrossSessionEndStatusPropertyList.add(crossSessionRange);
		jobCrossSessionEndStatusPropertyList.add(description);
		
		HashMap<String, Object> jobCrossSessionEndStatusMap = new HashMap<String, Object>();
		jobCrossSessionEndStatusMap.put("value", JudgmentObjectMessage.STRING_CROSS_SESSION_JOB_END_STATUS);
		jobCrossSessionEndStatusMap.put("property", jobCrossSessionEndStatusPropertyList);

		//セッション横断ジョブ終了値
		ArrayList<Object> jobCrossSessionEndValuePropertyList = new ArrayList<Object>();
		jobCrossSessionEndValuePropertyList.add(jobCrossSession);
		jobCrossSessionEndValuePropertyList.add(endValueCondition);
		jobCrossSessionEndValuePropertyList.add(conditionEndValue);
		jobCrossSessionEndValuePropertyList.add(crossSessionRange);
		jobCrossSessionEndValuePropertyList.add(description);
		
		HashMap<String, Object> jobCrossSessionEndValueMap = new HashMap<String, Object>();
		jobCrossSessionEndValueMap.put("value", JudgmentObjectMessage.STRING_CROSS_SESSION_JOB_END_VALUE);
		jobCrossSessionEndValueMap.put("property", jobCrossSessionEndValuePropertyList);

		//時刻
		ArrayList<Object> timePropertyList = new ArrayList<Object>();
		timePropertyList.add(time);
		timePropertyList.add(description);
		
		HashMap<String, Object> timeMap = new HashMap<String, Object>();
		timeMap.put("value", JudgmentObjectMessage.STRING_TIME);
		timeMap.put("property", timePropertyList);

		//セッション開始時の時間（分）
		ArrayList<Object> startMinuteList = new ArrayList<Object>();
		startMinuteList.add(startMinute);
		startMinuteList.add(description);
		
		HashMap<String, Object> startMinuteMap = new HashMap<String, Object>();
		startMinuteMap.put("value", JudgmentObjectMessage.STRING_START_MINUTE);
		startMinuteMap.put("property", startMinuteList);

		//ジョブ変数
		ArrayList<Object> jobParamPropertyList = new ArrayList<Object>();
		jobParamPropertyList.add(decisionValue1);
		jobParamPropertyList.add(decisionCondition);
		jobParamPropertyList.add(decisionValue2);
		jobParamPropertyList.add(description);
		
		HashMap<String, Object> jobParamMap = new HashMap<String, Object>();
		jobParamMap.put("value", JudgmentObjectMessage.STRING_JOB_PARAMETER);
		jobParamMap.put("property", jobParamPropertyList);

		//ジョブ戻り値
		ArrayList<Object> jobReturnValueList = new ArrayList<Object>();
		jobReturnValueList.add(jobReturnValue);
		jobReturnValueList.add(returnValueCondition);
		jobReturnValueList.add(conditionReturnValue);
		jobReturnValueList.add(description);
		
		HashMap<String, Object> jobReturnValueMap = new HashMap<String, Object>();
		jobReturnValueMap.put("value", JudgmentObjectMessage.STRING_JOB_RETURN_VALUE);
		jobReturnValueMap.put("property", jobReturnValueList);

		//判定対象コンボボックスの選択項目
		Object judgmentObjectValues[][] = {
				{ JudgmentObjectMessage.STRING_JOB_END_STATUS,
					JudgmentObjectMessage.STRING_JOB_END_VALUE,
					JudgmentObjectMessage.STRING_TIME,
					JudgmentObjectMessage.STRING_START_MINUTE,
					JudgmentObjectMessage.STRING_JOB_PARAMETER,
					JudgmentObjectMessage.STRING_CROSS_SESSION_JOB_END_STATUS,
					JudgmentObjectMessage.STRING_CROSS_SESSION_JOB_END_VALUE,
					JudgmentObjectMessage.STRING_JOB_RETURN_VALUE},
					{ jobEndStatusMap, jobEndValueMap, timeMap, startMinuteMap, jobParamMap , jobCrossSessionEndStatusMap, jobCrossSessionEndValueMap, jobReturnValueMap} };

		Object conditionEndStatuses[][] = {
				{ EndStatusMessage.STRING_NORMAL,
					EndStatusMessage.STRING_WARNING,
					EndStatusMessage.STRING_ABNORMAL,
					EndStatusMessage.STRING_ANY},
					{ JobObjectInfoResponse.StatusEnum.NORMAL,
						JobObjectInfoResponse.StatusEnum.WARNING,
						JobObjectInfoResponse.StatusEnum.ABNORMAL,
						JobObjectInfoResponse.StatusEnum.ANY} };

		Object endValueConditions[][] = {
				{ DecisionObjectMessage.STRING_EQUAL_NUMERIC,
					DecisionObjectMessage.STRING_NOT_EQUAL_NUMERIC,
					DecisionObjectMessage.STRING_IN_NUMERIC,
					DecisionObjectMessage.STRING_NOT_IN_NUMERIC},
					{ JobObjectInfoResponse.DecisionConditionEnum.EQUAL_NUMERIC,
						JobObjectInfoResponse.DecisionConditionEnum.NOT_EQUAL_NUMERIC,
						JobObjectInfoResponse.DecisionConditionEnum.IN_NUMERIC,
						JobObjectInfoResponse.DecisionConditionEnum.NOT_IN_NUMERIC} };

		Object decisionConditions[][] = {
				{ DecisionObjectMessage.STRING_EQUAL_NUMERIC,
					DecisionObjectMessage.STRING_NOT_EQUAL_NUMERIC,
					DecisionObjectMessage.STRING_GREATER_THAN,
					DecisionObjectMessage.STRING_GREATER_THAN_OR_EQUAL_TO,
					DecisionObjectMessage.STRING_LESS_THAN,
					DecisionObjectMessage.STRING_LESS_THAN_OR_EQUAL_TO,
					DecisionObjectMessage.STRING_IN_NUMERIC,
					DecisionObjectMessage.STRING_NOT_IN_NUMERIC,
					DecisionObjectMessage.STRING_EQUAL_STRING,
					DecisionObjectMessage.STRING_NOT_EQUAL_STRING},
					{ JobObjectInfoResponse.DecisionConditionEnum.EQUAL_NUMERIC,
						JobObjectInfoResponse.DecisionConditionEnum.NOT_EQUAL_NUMERIC,
						JobObjectInfoResponse.DecisionConditionEnum.GREATER_THAN,
						JobObjectInfoResponse.DecisionConditionEnum.GREATER_THAN_OR_EQUAL_TO,
						JobObjectInfoResponse.DecisionConditionEnum.LESS_THAN,
						JobObjectInfoResponse.DecisionConditionEnum.LESS_THAN_OR_EQUAL_TO,
						JobObjectInfoResponse.DecisionConditionEnum.IN_NUMERIC,
						JobObjectInfoResponse.DecisionConditionEnum.NOT_IN_NUMERIC,
						JobObjectInfoResponse.DecisionConditionEnum.EQUAL_STRING,
						JobObjectInfoResponse.DecisionConditionEnum.NOT_EQUAL_STRING} };

		Object returnValueConditions[][] = {
				{ DecisionObjectMessage.STRING_EQUAL_NUMERIC,
					DecisionObjectMessage.STRING_NOT_EQUAL_NUMERIC,DecisionObjectMessage.STRING_GREATER_THAN,
					DecisionObjectMessage.STRING_GREATER_THAN_OR_EQUAL_TO,
					DecisionObjectMessage.STRING_LESS_THAN,
					DecisionObjectMessage.STRING_LESS_THAN_OR_EQUAL_TO,
					DecisionObjectMessage.STRING_IN_NUMERIC,
					DecisionObjectMessage.STRING_NOT_IN_NUMERIC,},
					{ JobObjectInfoResponse.DecisionConditionEnum.EQUAL_NUMERIC,
						JobObjectInfoResponse.DecisionConditionEnum.NOT_EQUAL_NUMERIC,
						JobObjectInfoResponse.DecisionConditionEnum.GREATER_THAN,
						JobObjectInfoResponse.DecisionConditionEnum.GREATER_THAN_OR_EQUAL_TO,
						JobObjectInfoResponse.DecisionConditionEnum.LESS_THAN,
						JobObjectInfoResponse.DecisionConditionEnum.LESS_THAN_OR_EQUAL_TO,
						JobObjectInfoResponse.DecisionConditionEnum.IN_NUMERIC,
						JobObjectInfoResponse.DecisionConditionEnum.NOT_IN_NUMERIC,} };

		judgmentObject.setSelectValues(judgmentObjectValues);
		conditionEndStatus.setSelectValues(conditionEndStatuses);
		decisionCondition.setSelectValues(decisionConditions);
		returnValueCondition.setSelectValues(returnValueConditions);
		endValueCondition.setSelectValues(endValueConditions);

		//値を初期化
		judgmentObject.setValue("");
		job.setValue("");
		conditionEndStatus.setValue("");
		conditionEndValue.setValue("");
		conditionReturnValue.setValue("");
		time.setValue("");
		startMinute.setValue("");
		description.setValue("");
		decisionValue1.setValue("");
		decisionCondition.setValue("");
		decisionValue2.setValue("");
		crossSessionRange.setValue(60);  //セッション横断待ち条件ジョブ履歴範囲のデフォルト60（分）とする
		returnValueCondition.setValue("");
		endValueCondition.setValue("");

		//変更の可/不可を設定
		judgmentObject.setModify(PropertyDefineConstant.MODIFY_OK);
		judgmentObject.setStringHighlight(true);
		job.setModify(PropertyDefineConstant.MODIFY_OK);
		jobCrossSession.setModify(PropertyDefineConstant.MODIFY_OK);
		jobReturnValue.setModify(PropertyDefineConstant.MODIFY_OK);
		conditionEndStatus.setModify(PropertyDefineConstant.MODIFY_OK);
		conditionEndValue.setModify(PropertyDefineConstant.MODIFY_OK);
		conditionReturnValue.setModify(PropertyDefineConstant.MODIFY_OK);
		time.setModify(PropertyDefineConstant.MODIFY_OK);
		startMinute.setModify(PropertyDefineConstant.MODIFY_OK);
		description.setModify(PropertyDefineConstant.MODIFY_OK);
		decisionValue1.setModify(PropertyDefineConstant.MODIFY_OK);
		decisionCondition.setModify(PropertyDefineConstant.MODIFY_OK);
		decisionValue2.setModify(PropertyDefineConstant.MODIFY_OK);
		crossSessionRange.setModify(PropertyDefineConstant.MODIFY_OK);
		returnValueCondition.setModify(PropertyDefineConstant.MODIFY_OK);
		endValueCondition.setModify(PropertyDefineConstant.MODIFY_OK);

		if (typeEnum == JobObjectInfoResponse.TypeEnum.JOB_END_STATUS) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_JOB_END_STATUS);
			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(job);
			judgmentObject.addChildren(conditionEndStatus);
			judgmentObject.addChildren(description);

		} else if (typeEnum == JobObjectInfoResponse.TypeEnum.JOB_END_VALUE) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_JOB_END_VALUE);
			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(job);
			judgmentObject.addChildren(endValueCondition);
			judgmentObject.addChildren(conditionEndValue);
			judgmentObject.addChildren(description);

		} else if (typeEnum == JobObjectInfoResponse.TypeEnum.TIME) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_TIME);
			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(time);
			judgmentObject.addChildren(description);

		} else if (typeEnum == JobObjectInfoResponse.TypeEnum.START_MINUTE) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_START_MINUTE);
			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(startMinute);
			judgmentObject.addChildren(description);

		} else if (typeEnum == JobObjectInfoResponse.TypeEnum.JOB_PARAMETER) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_JOB_PARAMETER);
			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(decisionValue1);
			judgmentObject.addChildren(decisionCondition);
			judgmentObject.addChildren(decisionValue2);
			judgmentObject.addChildren(description);

		} else if (typeEnum == JobObjectInfoResponse.TypeEnum.CROSS_SESSION_JOB_END_STATUS) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_CROSS_SESSION_JOB_END_STATUS);
			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(jobCrossSession);
			judgmentObject.addChildren(conditionEndStatus);
			judgmentObject.addChildren(crossSessionRange);
			judgmentObject.addChildren(description);

		} else if (typeEnum == JobObjectInfoResponse.TypeEnum.CROSS_SESSION_JOB_END_VALUE) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_CROSS_SESSION_JOB_END_VALUE);
			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(jobCrossSession);
			judgmentObject.addChildren(endValueCondition);
			judgmentObject.addChildren(conditionEndValue);
			judgmentObject.addChildren(crossSessionRange);
			judgmentObject.addChildren(description);

		} else if (typeEnum == JobObjectInfoResponse.TypeEnum.JOB_RETURN_VALUE) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_JOB_RETURN_VALUE);
			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(jobReturnValue);
			judgmentObject.addChildren(returnValueCondition);
			judgmentObject.addChildren(conditionReturnValue);
			judgmentObject.addChildren(description);
		}

		return judgmentObject;
	}
}
