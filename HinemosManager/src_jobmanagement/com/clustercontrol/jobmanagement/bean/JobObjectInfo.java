/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * ジョブの待ち条件の判定対象に関する情報を保持するクラス<BR>
 * 
 * @version 2.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobObjectInfo implements Serializable, Comparable<JobObjectInfo>, Cloneable {

	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -4050301670424654620L;

	/** 判定対象種別 */
	private Integer type;

	/** ジョブID */
	private String jobId;

	/** ジョブ名 */
	private String jobName;

	/** 終了状態 */
	private Integer status;

	/** 終了値 */
	private String value;

	/** 時刻 */
	private Long time;

	/** セッション開始時の時間（分） */
	private Integer startMinute;

	/** 説明 */
	private String description;

	/** 判定値1 */
	private String decisionValue;

	/** 判定条件 */
	private Integer decisionCondition;
	
	/** セッション横断ジョブ履歴判定対象範囲（分）*/
	private Integer crossSessionRange;
	
	/**
	 * 待ち条件の判定対象となるジョブIDを返す。<BR>
	 * @return 待ち条件の判定対象となるジョブID
	 */
	public String getJobId() {
		return jobId;
	}

	/**
	 * 待ち条件の判定対象となるジョブIDを設定する。<BR>
	 * @param jobId 待ち条件となる判定対象となるジョブID
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * 待ち条件の時刻を返す。<BR>
	 * @return 待ち条件の時刻
	 */
	public Long getTime() {
		return time;
	}

	/**
	 * 待ち条件の時刻を設定する。<BR>
	 * @param time 待ち条件の時刻
	 */
	public void setTime(Long time) {
		this.time = time;
	}

	/**
	 * 待ち条件の判定対象となるジョブの終了状態を返す。<BR>
	 * @return 待ち条件の判定対象となるジョブの終了状態
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * 待ち条件の判定対象となるジョブの終了状態を設定する。<BR>
	 * @param value 待ち条件の判定対象となるジョブの終了状態
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * 待ち条件の判定対象となるジョブの終了値を返す。<BR>
	 * @return 待ち条件の判定対象となるジョブの終了値
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 待ち条件の判定対象となるジョブの終了値を設定する。<BR>
	 * @param value 待ち条件の判定対象となるジョブの終了値
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * 判定対象種別を返す。<BR>
	 * @return 判定対象種別
	 * @see com.clustercontrol.bean.JudgmentObjectConstant
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * 判定対象種別を設定する。<BR>
	 * @param type 判定対象種別
	 * @see com.clustercontrol.bean.JudgmentObjectConstant
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * ジョブ名を返す。<BR>
	 * @return ジョブ名
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * ジョブ名を設定する。<BR>
	 * @param jobName ジョブ名
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	/**
	 * セッション開始時の時間（分）を返す。<BR>
	 * @return セッション開始時の時間（分）
	 */
	public Integer getStartMinute() {
		return startMinute;
	}

	/**
	 * セッション開始時の時間（分）を設定する。<BR>
	 * @param startMinute セッション開始時の時間（分）
	 */
	public void setStartMinute(Integer startMinute) {
		this.startMinute = startMinute;
	}

	/**
	 * 説明を返す。<BR>
	 * @return 説明
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 説明を設定する。<BR>
	 * @param description 説明
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 判定値を返す。<BR>
	 * @return 判定値
	 */
	public String getDecisionValue() {
		return decisionValue;
	}

	/**
	 * 判定値を設定する。<BR>
	 * @param decisionValue 判定値
	 */
	public void setDecisionValue(String decisionValue) {
		this.decisionValue = decisionValue;
	}

	/**
	 * 判定条件を返す。<BR>
	 * @return 判定条件
	 */
	public Integer getDecisionCondition() {
		return decisionCondition;
	}

	/**
	 * 判定条件を設定する。<BR>
	 * @param decisionCondition 判定条件
	 */
	public void setDecisionCondition(Integer decisionCondition) {
		this.decisionCondition = decisionCondition;
	}
	
	/**
	 * セッション横断ジョブ履歴判定対象範囲を返す。
	 * @return
	 */
	public Integer getCrossSessionRange() {
		return crossSessionRange;
	}

	/**
	 * セッション横断ジョブ履歴判定対象範囲を設定する。
	 * @param crossSessionRange
	 */
	public void setCrossSessionRange(Integer crossSessionRange) {
		this.crossSessionRange = crossSessionRange;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
		result = prime * result
				+ ((startMinute == null) ? 0 : startMinute.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((decisionValue == null) ? 0 : decisionValue.hashCode());
		result = prime * result + ((decisionCondition == null) ? 0 : decisionCondition.hashCode());
		result = prime * result + ((crossSessionRange == null) ? 0 : crossSessionRange.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobObjectInfo)) {
			return false;
		}
		JobObjectInfo o1 = this;
		JobObjectInfo o2 = (JobObjectInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getType(), o2.getType()) &&
				equalsSub(o1.getJobId(), o2.getJobId()) &&
				equalsSub(o1.getStatus(), o2.getStatus()) &&
				equalsSub(o1.getValue(), o2.getValue()) &&
				equalsSub(o1.getTime(), o2.getTime()) &&
				equalsSub(o1.getStartMinute(), o2.getStartMinute()) &&
				equalsSub(o1.getDescription(), o2.getDescription()) &&
				equalsSub(o1.getDecisionValue(), o2.getDecisionValue()) &&
				equalsSub(o1.getDecisionCondition(), o2.getDecisionCondition()) &&
				equalsSub(o1.getCrossSessionRange(), o2.getCrossSessionRange());
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == o2)
			return true;
		
		if (o1 == null)
			return false;
		
		return o1.equals(o2);
	}

	@Override
	public int compareTo(JobObjectInfo o) {
		return ("" + this.jobId + this.type + this.status + this.value + this.time + this.startMinute
				 + this.decisionValue + this.decisionCondition + this.description + this.crossSessionRange)
				.compareTo("" + o.jobId + o.type + o.status + o.value + o.time + o.startMinute
						 + o.decisionValue + o.decisionCondition + o.description + o.crossSessionRange);
	}

	/**
	 * 単体テスト用
	 * @param args
	 */
	public static void main (String args[]) {
		testEquals();
	}
	/**
	 * 単体テスト
	 */
	public static void testEquals(){

		System.out.println("=== JobObjectInfo の単体テスト ===");

		System.out.println("*** 全部一致 ***");
		JobObjectInfo info1 = new JobObjectInfo();
		info1.setType(0);
		info1.setJobId("testJob");
		info1.setJobName("テストジョブ");
		info1.setStatus(0);
		info1.setTime(0L);
		info1.setStartMinute(0);
		info1.setDecisionValue("テスト判定値1");
		info1.setValue("テスト判定値2");
		info1.setDecisionCondition(0);
		info1.setDescription("説明");

		JobObjectInfo info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ");
		info2.setStatus(0);
		info2.setTime(0L);
		info2.setStartMinute(0);
		info1.setDecisionValue("テスト判定値1");
		info1.setValue("テスト判定値2");
		info2.setDecisionCondition(0);
		info2.setDescription("説明");
		
		judge(true,info1.equals(info2));

		System.out.println("*** 「判定対象種別」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(1);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ");
		info2.setStatus(0);
		info2.setTime(0L);
		info2.setStartMinute(0);
		info1.setDecisionValue("テスト判定値1");
		info1.setValue("テスト判定値2");
		info2.setDecisionCondition(0);
		info2.setDescription("説明");

		judge(false,info1.equals(info2));

		System.out.println("*** 「ジョブID」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob ");
		info2.setJobName("テストジョブ");
		info2.setStatus(0);
		info2.setTime(0L);
		info2.setStartMinute(0);
		info1.setDecisionValue("テスト判定値1");
		info1.setValue("テスト判定値2");
		info2.setDecisionCondition(0);
		info2.setDescription("説明");

		judge(false,info1.equals(info2));

		System.out.println("*** 「ジョブ名」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ ");
		info2.setStatus(0);
		info2.setTime(0L);
		info2.setStartMinute(0);
		info1.setDecisionValue("テスト判定値1");
		info1.setValue("テスト判定値2");
		info2.setDecisionCondition(0);
		info2.setDescription("説明");

		judge(false,info1.equals(info2));

		System.out.println("*** 「値」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ");
		info2.setStatus(1);
		info2.setTime(0L);
		info2.setStartMinute(0);
		info1.setDecisionValue("テスト判定値1");
		info1.setValue("テスト判定値2");
		info2.setDecisionCondition(0);
		info2.setDescription("説明");

		judge(false,info1.equals(info2));

		System.out.println("*** 「時刻」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ");
		info2.setStatus(0);
		info2.setTime(1L);
		info1.setDecisionValue("テスト判定値1");
		info1.setValue("テスト判定値2");
		info2.setDecisionCondition(0);
		info2.setStartMinute(0);
		info2.setDescription("説明");

		judge(false,info1.equals(info2));

		System.out.println("*** 「セッション開始時の時間（分）」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ");
		info2.setStatus(0);
		info2.setTime(0L);
		info2.setStartMinute(1);
		info1.setDecisionValue("テスト判定値1");
		info1.setValue("テスト判定値2");
		info2.setDecisionCondition(0);
		info2.setDescription("説明");

		judge(false, info1.equals(info2));

		System.out.println("*** 「判定値1」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ");
		info2.setStatus(0);
		info2.setTime(0L);
		info2.setStartMinute(0);
		info1.setDecisionValue("テスト判定値1");
		info1.setValue("テスト判定値2");
		info2.setDecisionCondition(0);
		info2.setDescription("説明");

		judge(false,info1.equals(info2));

		System.out.println("*** 「判定値2」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ");
		info2.setStatus(0);
		info2.setTime(0L);
		info2.setStartMinute(0);
		info1.setDecisionValue("テスト判定値1");
		info1.setValue("テスト判定値2");
		info2.setDecisionCondition(0);
		info2.setDescription("説明");

		judge(false,info1.equals(info2));

		System.out.println("*** 「判定条件 」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ");
		info2.setStatus(0);
		info2.setTime(0L);
		info2.setStartMinute(0);
		info1.setDecisionValue("テスト判定値1");
		info1.setValue("テスト判定値2");
		info2.setDecisionCondition(1);
		info2.setDescription("説明");

		judge(false, info1.equals(info2));

		System.out.println("*** 「説明」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ");
		info2.setStatus(0);
		info2.setTime(0L);
		info2.setStartMinute(0);
		info1.setDecisionValue("テスト判定値1");
		info1.setValue("テスト判定値2");
		info2.setDecisionCondition(0);
		info2.setDescription("説明 ");

		judge(false,info1.equals(info2));
}

	/**
	 * 単体テストの結果が正しいものか判断する
	 * @param judge
	 * @param result
	 */
	private static void judge(boolean judge, boolean result){

		System.out.println("expect : " + judge);
		System.out.print("result : " + result);
		String ret = "NG";
		if (judge == result) {
			ret = "OK";
		}
		System.out.println("    is ...  " + ret);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		JobObjectInfo jobObjectInfo = (JobObjectInfo) super.clone();
		return jobObjectInfo;
	}
}