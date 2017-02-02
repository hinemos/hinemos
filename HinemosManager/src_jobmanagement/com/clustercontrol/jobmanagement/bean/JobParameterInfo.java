/*

 Copyright (C) 2006 NTT DATA Corporation

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * ジョブの変数に関する情報を保持するクラス
 * 
 * @version 2.1.0
 * @since 2.1.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobParameterInfo implements Serializable, Comparable<JobParameterInfo> {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 981926727088488957L;

	private static Log m_log = LogFactory.getLog( JobParameterInfo.class );

	/** パラメータID */
	private String paramId;

	/** パラメータ種別 */
	private Integer type = 0;

	/** 説明 */
	private String description;

	/** 値 */
	private String value;

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
	 * パラメータIDを返す。<BR>
	 * @return パラメータID
	 */
	public String getParamId() {
		return paramId;
	}

	/**
	 * パラメータIDを設定する。<BR>
	 * @param paramId パラメータID
	 */
	public void setParamId(String paramId) {
		this.paramId = paramId;
	}

	/**
	 * パラメータ種別を返す。<BR>
	 * @return パラメータ種別
	 * @see com.clustercontrol.bean.JobParamTypeConstant
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * パラメータ種別を設定する。<BR>
	 * @param type パラメータ種別
	 * @see com.clustercontrol.bean.JobParamTypeConstant
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * パラメータとして設定した値を返す。<BR>
	 * @return パラメータ値
	 */
	public String getValue() {
		return value;
	}

	/**
	 * パラメータとして値を設定する。<BR>
	 * @param value パラメータ値
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((paramId == null) ? 0 : paramId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobParameterInfo)) {
			return false;
		}
		JobParameterInfo o1 = this;
		JobParameterInfo o2 = (JobParameterInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getParamId(), o2.getParamId()) &&
				equalsSub(o1.getType(), o2.getType()) &&
				equalsSub(o1.getDescription(), o2.getDescription()) &&
				equalsSub(o1.getValue(), o2.getValue());
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == o2)
			return true;
		
		if (o1 == null)
			return false;
		
		boolean ret = o1.equals(o2);
		if (!ret) {
			if (m_log.isTraceEnabled()) {
				m_log.trace("equalsSub : " + o1 + "!=" + o2);
			}
		}
		return ret;
	}

	@Override
	public int compareTo(JobParameterInfo o) {
		return (this.paramId + this.type + this.description + this.value).compareTo(
				o.paramId + this.type + this.description + this.value);
	}

	/**
	 * 単体テスト用
	 * @param args
	 */
	public static void main (String args[]) {
		testEquals();
	}

	public static void testEquals() {

		System.out.println("=== JobParameterInfo の単体テスト ===");

		System.out.println("*** 全部一致 ***");
		JobParameterInfo info1 = new JobParameterInfo();
		info1.setParamId("paramId");
		info1.setType(0);
		info1.setDescription("説明");
		info1.setValue("value");

		JobParameterInfo info2 = new JobParameterInfo();
		info2.setParamId("paramId");
		info2.setType(0);
		info2.setDescription("説明");
		info2.setValue("value");

		judge(true,info1.equals(info2));

		System.out.println("*** 「パラメータID」のみ違う ***");
		info2 = new JobParameterInfo();
		info2.setParamId("param_Id");
		info2.setType(0);
		info2.setDescription("説明");
		info2.setValue("value");

		judge(false,info1.equals(info2));

		System.out.println("*** 「パラメータ種別」のみ違う ***");
		info2 = new JobParameterInfo();
		info2.setParamId("paramId");
		info2.setType(1);
		info2.setDescription("説明");
		info2.setValue("value");

		judge(false,info1.equals(info2));

		System.out.println("*** 「説明」のみ違う ***");
		info2 = new JobParameterInfo();
		info2.setParamId("paramId");
		info2.setType(0);
		info2.setDescription("");
		info2.setValue("value");

		judge(false,info1.equals(info2));

		System.out.println("*** 「値」のみ違う ***");
		info2 = new JobParameterInfo();
		info2.setParamId("paramId");
		info2.setType(0);
		info2.setDescription("");
		info2.setValue("value_1");

		judge(false,info1.equals(info2));
	}


	public static JobParameterInfo createSampleInfo() {
		JobParameterInfo info = new JobParameterInfo();
		info.setParamId("paramId");
		info.setType(0);
		info.setDescription("説明");
		info.setValue("value");
		return info;
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
}