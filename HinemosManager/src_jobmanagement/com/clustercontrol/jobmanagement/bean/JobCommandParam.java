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

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * ジョブコマンドタブ ジョブ変数情報を保持するクラス<BR>
 * 
 */
/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Requestクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * #13882
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobCommandParam implements Serializable, Comparable<JobCommandParam>, RequestDto {

	/**
	 * シリアライズ可能クラスに定義するUID
	 */
	@JsonIgnore
	private static final long serialVersionUID = 1L;

	/** 名前 **/
	private String paramId;
	/** 値（デフォルト値） */
	private String value;
	/** 標準出力フラグ */
	private Boolean jobStandardOutputFlg = false;

	public String getParamId() {
		return paramId;
	}
	public void setParamId(String paramId) {
		this.paramId = paramId;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public Boolean getJobStandardOutputFlg() {
		return jobStandardOutputFlg;
	}
	public void setJobStandardOutputFlg(Boolean jobStandardOutputFlg) {
		this.jobStandardOutputFlg = jobStandardOutputFlg;
	}

	@Override
	public String toString() {
		String str = null;
		str += "m_paramId=" + paramId;
		str += " ,m_value=" + value;
		str += " ,m_jobStandardOutputFlg=" + jobStandardOutputFlg;
		return str;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jobStandardOutputFlg == null) ? 0 : jobStandardOutputFlg.hashCode());
		result = prime * result + ((paramId == null) ? 0 : paramId.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JobCommandParam)) {
			return false;
		}
		return this.compareTo((JobCommandParam)obj) == 0;
	}

	@Override
	public int compareTo(JobCommandParam o) {
		return (this.getParamId() + this.getValue() + this.jobStandardOutputFlg).compareTo(
				o.getParamId() + o.getValue() + o.jobStandardOutputFlg);
	}

	public static void main (String args[]) {
		JobCommandParam p1 = new JobCommandParam();
		p1.setParamId("1");
		p1.setValue("2");
		p1.setJobStandardOutputFlg(true);
		JobCommandParam p2 = new JobCommandParam();
		p2.setParamId("1");
		p2.setValue("2");
		p2.setJobStandardOutputFlg(true);
		JobCommandParam p3 = new JobCommandParam();
		p3.setParamId("1");
		p3.setValue("3");
		p3.setJobStandardOutputFlg(true);
		
		System.out.println("hoge " + p1.equals(p2) + "," + p1.equals(p3));
		System.out.println("hoge " + p1.compareTo(p2) + "," + p1.compareTo(p3));
	}
	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}