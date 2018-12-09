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

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.bean.DecisionObjectConstant;
import com.clustercontrol.jobmanagement.bean.DecisionObjectMessage;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectMessage;
import com.clustercontrol.jobmanagement.editor.JobPropertyDefine;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

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

	/** 値（終了状態） */
	public static final String ID_CONDITION_END_STATUS = "conditionEndStatus";

	/** 値（終了値） */
	public static final String ID_CONDITION_END_VALUE = "conditionEndValue";

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
	 * <p>プロパティに定義する待ち条件設定項目は、下記の通りです。
	 * <p>
	 * <ul>
	 *  <li>プロパティ（親。ダミー）
	 *  <ul>
	 *   <li>名前（子。コンボボックス）
	 *   <ul>
	 *    <li>ジョブ(終了状態)（名前の選択肢）
	 *    <ul>
	 *     <li>ジョブID（孫。ダイアログ）
	 *     <li>値（孫。コンボボックス）
	 *     <li>説明（孫。テキスト）
	 *    </ul>
	 *    <li>ジョブ(終了値)（名前の選択肢）
	 *    <ul>
	 *     <li>ジョブID（孫。ダイアログ）
	 *     <li>値（孫。テキスト）
	 *     <li>説明（孫。テキスト）
	 *    </ul>
	 *    <li>時刻（名前の選択肢）
	 *    <ul>
	 *     <li>時刻（孫。テキスト）
	 *     <li>説明（孫。テキスト）
	 *    </ul>
	 *    <li>セッション開始時の時間（分）（名前の選択肢）
	 *    <ul>
	 *     <li>セッション開始時の時間（分）（孫。テキスト）
	 *     <li>説明（孫。テキスト）
	 *    </ul>
	 *    <li>ジョブ変数（名前の選択肢）
	 *    <ul>
	 *     <li>判定値1（孫。テキスト）
	 *     <li>判定条件（孫。コンボボックス）
	 *     <li>判定値2（孫。テキスト）
	 *     <li>説明（孫。テキスト）
	 *   </ul>
	 *  </ul>
	 * </ul>
	 * 
	 * @param parentJobId 親ジョブID
	 * @param jobId ジョブID
	 * @param type ジョブ変数の種別
	 * @return 待ち条件用プロパティ
	 * 
	 * @see com.clustercontrol.bean.Property
	 * @see com.clustercontrol.bean.PropertyDefineConstant
	 * @see com.clustercontrol.bean.JudgmentObjectConstant
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public Property getProperty(JobTreeItem item, int type) {
		//プロパティ項目定義
		Property judgmentObject = new Property(ID_JUDGMENT_OBJECT,
				Messages.getString("name"), PropertyDefineConstant.EDITOR_SELECT);
		Property job = new Property(ID_JOB_ID,
				Messages.getString("job.id"), PropertyDefineConstant.EDITOR_JOB);
		Property jobCrossSession = new Property(ID_CROSS_SESSION_JOB_ID,
				 Messages.getString("job.id"), PropertyDefineConstant.EDITOR_JOB);
		Property conditionEndStatus = new Property(ID_CONDITION_END_STATUS,
				Messages.getString("value"), PropertyDefineConstant.EDITOR_SELECT);
		Property conditionEndValue = new Property(ID_CONDITION_END_VALUE,
				Messages.getString("value"), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.SMALLINT_HIGH, DataRangeConstant.SMALLINT_LOW);
		Property time = new Property(ID_TIME,
				Messages.getString("wait.rule.time.example"), PropertyDefineConstant.EDITOR_TIME);
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
				Messages.getString("wait.rule.cross.session.range"), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.SMALLINT_HIGH, 0);

		//JobPropertyDefineクラスはClusterControlでは定義されていない
		JobPropertyDefine define = new JobPropertyDefine(item);
		job.setDefine(define);
		// JobConstant.TYPE_REFERJOBを渡すことで参照ジョブと同様にジョブユニット配下を全て表示する
		JobPropertyDefine defineCrossSession = new JobPropertyDefine(item, JobConstant.TYPE_REFERJOB);
		jobCrossSession.setDefine(defineCrossSession);

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

		//判定対象コンボボックスの選択項目
		Object judgmentObjectValues[][] = {
				{ JudgmentObjectMessage.STRING_JOB_END_STATUS,
					JudgmentObjectMessage.STRING_JOB_END_VALUE,
					JudgmentObjectMessage.STRING_TIME,
					JudgmentObjectMessage.STRING_START_MINUTE,
					JudgmentObjectMessage.STRING_JOB_PARAMETER,
					JudgmentObjectMessage.STRING_CROSS_SESSION_JOB_END_STATUS,
					JudgmentObjectMessage.STRING_CROSS_SESSION_JOB_END_VALUE},
					{ jobEndStatusMap, jobEndValueMap, timeMap, startMinuteMap, jobParamMap , jobCrossSessionEndStatusMap, jobCrossSessionEndValueMap} };

		Object conditionEndStatuss[][] = {
				{ EndStatusMessage.STRING_NORMAL,
					EndStatusMessage.STRING_WARNING,
					EndStatusMessage.STRING_ABNORMAL,
					EndStatusMessage.STRING_ANY},
					{ EndStatusMessage.STRING_NORMAL,
						EndStatusMessage.STRING_WARNING,
						EndStatusMessage.STRING_ABNORMAL,
						EndStatusMessage.STRING_ANY} };

		Object decisionConditions[][] = {
				{ DecisionObjectMessage.STRING_EQUAL_NUMERIC,
					DecisionObjectMessage.STRING_NOT_EQUAL_NUMERIC,
					DecisionObjectMessage.STRING_GREATER_THAN,
					DecisionObjectMessage.STRING_GREATER_THAN_OR_EQUAL_TO,
					DecisionObjectMessage.STRING_LESS_THAN,
					DecisionObjectMessage.STRING_LESS_THAN_OR_EQUAL_TO,
					DecisionObjectMessage.STRING_EQUAL_STRING,
					DecisionObjectMessage.STRING_NOT_EQUAL_STRING},
					{ DecisionObjectConstant.EQUAL_NUMERIC,
						DecisionObjectConstant.NOT_EQUAL_NUMERIC,
						DecisionObjectConstant.GREATER_THAN,
						DecisionObjectConstant.GREATER_THAN_OR_EQUAL_TO,
						DecisionObjectConstant.LESS_THAN,
						DecisionObjectConstant.LESS_THAN_OR_EQUAL_TO,
						DecisionObjectConstant.EQUAL_STRING,
						DecisionObjectConstant.NOT_EQUAL_STRING} };

		judgmentObject.setSelectValues(judgmentObjectValues);
		conditionEndStatus.setSelectValues(conditionEndStatuss);
		decisionCondition.setSelectValues(decisionConditions);

		//値を初期化
		judgmentObject.setValue("");
		job.setValue("");
		conditionEndStatus.setValue("");
		time.setValue("");
		startMinute.setValue("");
		description.setValue("");
		decisionValue1.setValue("");
		decisionCondition.setValue("");
		decisionValue2.setValue("");
		crossSessionRange.setValue(60);  //セッション横断待ち条件ジョブ履歴範囲のデフォルト60（分）とする
		
		//変更の可/不可を設定
		judgmentObject.setModify(PropertyDefineConstant.MODIFY_OK);
		job.setModify(PropertyDefineConstant.MODIFY_OK);
		jobCrossSession.setModify(PropertyDefineConstant.MODIFY_OK);
		conditionEndStatus.setModify(PropertyDefineConstant.MODIFY_OK);
		conditionEndValue.setModify(PropertyDefineConstant.MODIFY_OK);
		time.setModify(PropertyDefineConstant.MODIFY_OK);
		startMinute.setModify(PropertyDefineConstant.MODIFY_OK);
		description.setModify(PropertyDefineConstant.MODIFY_OK);
		decisionValue1.setModify(PropertyDefineConstant.MODIFY_OK);
		decisionCondition.setModify(PropertyDefineConstant.MODIFY_OK);
		decisionValue2.setModify(PropertyDefineConstant.MODIFY_OK);
		crossSessionRange.setModify(PropertyDefineConstant.MODIFY_OK);

		Property property = new Property(null, null, null);

		if (type == JudgmentObjectConstant.TYPE_JOB_END_STATUS) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_JOB_END_STATUS);

			// 初期表示ツリーを構成。
			property.removeChildren();
			property.addChildren(judgmentObject);

			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(job);
			judgmentObject.addChildren(conditionEndStatus);
			judgmentObject.addChildren(description);
		}
		else if (type == JudgmentObjectConstant.TYPE_JOB_END_VALUE) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_JOB_END_VALUE);

			// 初期表示ツリーを構成。
			property.removeChildren();
			property.addChildren(judgmentObject);

			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(job);
			judgmentObject.addChildren(conditionEndValue);
			judgmentObject.addChildren(description);
		}
		else if (type == JudgmentObjectConstant.TYPE_TIME) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_TIME);

			// 初期表示ツリーを構成。
			property.removeChildren();
			property.addChildren(judgmentObject);

			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(time);
			judgmentObject.addChildren(description);
		}
		else if (type == JudgmentObjectConstant.TYPE_START_MINUTE) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_START_MINUTE);

			// 初期表示ツリーを構成。
			property.removeChildren();
			property.addChildren(judgmentObject);

			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(startMinute);
			judgmentObject.addChildren(description);
		}
		else if (type == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_JOB_PARAMETER);

			// 初期表示ツリーを構成。
			property.removeChildren();
			property.addChildren(judgmentObject);

			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(decisionValue1);
			judgmentObject.addChildren(decisionCondition);
			judgmentObject.addChildren(decisionValue2);
			judgmentObject.addChildren(description);
		}
		else if (type == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_CROSS_SESSION_JOB_END_STATUS);

			// 初期表示ツリーを構成。
			property.removeChildren();
			property.addChildren(judgmentObject);

			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(jobCrossSession);
			judgmentObject.addChildren(conditionEndStatus);
			judgmentObject.addChildren(crossSessionRange);
			judgmentObject.addChildren(description);
		}
		else if (type == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE) {
			judgmentObject.setValue(JudgmentObjectMessage.STRING_CROSS_SESSION_JOB_END_VALUE);

			// 初期表示ツリーを構成。
			property.removeChildren();
			property.addChildren(judgmentObject);

			// 判定対象ツリー
			judgmentObject.removeChildren();
			judgmentObject.addChildren(jobCrossSession);
			judgmentObject.addChildren(conditionEndValue);
			judgmentObject.addChildren(crossSessionRange);
			judgmentObject.addChildren(description);
		}

		return property;
	}
}
