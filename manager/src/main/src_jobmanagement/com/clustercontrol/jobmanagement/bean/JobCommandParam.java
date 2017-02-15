/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;


/**
 * ジョブコマンドタブ ジョブ変数情報を保持するクラス<BR>
 * 
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobCommandParam implements Serializable, Comparable<JobCommandParam> {

	/**
	 * シリアライズ可能クラスに定義するUID
	 */
	private static final long serialVersionUID = 1L;

	/** 名前 **/
	private String m_paramId;
	/** 値（デフォルト値） */
	private String m_value;
	/** 標準出力フラグ */
	private Boolean m_jobStandardOutputFlg = false;

	public String getParamId() {
		return m_paramId;
	}
	public void setParamId(String paramId) {
		this.m_paramId = paramId;
	}

	public String getValue() {
		return m_value;
	}
	public void setValue(String value) {
		this.m_value = value;
	}

	public Boolean getJobStandardOutputFlg() {
		return m_jobStandardOutputFlg;
	}
	public void setJobStandardOutputFlg(Boolean jobStandardOutputFlg) {
		this.m_jobStandardOutputFlg = jobStandardOutputFlg;
	}

	@Override
	public String toString() {
		String str = null;
		str += "m_paramId=" + m_paramId;
		str += " ,m_value=" + m_value;
		str += " ,m_jobStandardOutputFlg=" + m_jobStandardOutputFlg;
		return str;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_jobStandardOutputFlg == null) ? 0 : m_jobStandardOutputFlg.hashCode());
		result = prime * result + ((m_paramId == null) ? 0 : m_paramId.hashCode());
		result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
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
		return (this.getParamId() + this.getValue() + this.m_jobStandardOutputFlg).compareTo(
				o.getParamId() + o.getValue() + o.m_jobStandardOutputFlg);
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
}