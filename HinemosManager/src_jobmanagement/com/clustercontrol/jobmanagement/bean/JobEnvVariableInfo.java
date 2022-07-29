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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * 環境変数に関する情報を保持するクラス
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Requestクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。
 * 詳細は、不具合チケット#13882を参照)
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobEnvVariableInfo implements Serializable, Comparable<JobEnvVariableInfo>, RequestDto {
	/** シリアライズ可能クラスに定義するUID */
	@JsonIgnore
	private static final long serialVersionUID = 981926727088488958L;

	@JsonIgnore
	private static Log m_log = LogFactory.getLog( JobEnvVariableInfo.class );

	/** 環境変数ID */
	private String envVariableId;

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
	 * 環境変数IDを返す。<BR>
	 * @return 環境変数ID
	 */
	public String getEnvVariableId() {
		return envVariableId;
	}

	/**
	 * 環境変数IDを設定する。<BR>
	 * @param paramId 環境変数ID
	 */
	public void setEnvVariableId(String envVariableId) {
		this.envVariableId = envVariableId;
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
		result = prime * result + ((envVariableId == null) ? 0 : envVariableId.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobEnvVariableInfo)) {
			return false;
		}
		JobEnvVariableInfo o1 = this;
		JobEnvVariableInfo o2 = (JobEnvVariableInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getEnvVariableId(), o2.getEnvVariableId()) &&
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
	public int compareTo(JobEnvVariableInfo o) {
		return (this.envVariableId + this.description + this.value).compareTo(
				o.envVariableId + this.description + this.value);
	}

	/**
	 * 単体テスト用
	 * @param args
	 */
	public static void main (String args[]) {
		testEquals();
	}

	public static void testEquals() {

		System.out.println("=== JobEnvVariableInfo の単体テスト ===");

		System.out.println("*** 全部一致 ***");
		JobEnvVariableInfo info1 = new JobEnvVariableInfo();
		info1.setEnvVariableId("envVariableId");
		info1.setDescription("説明");
		info1.setValue("value");

		JobEnvVariableInfo info2 = new JobEnvVariableInfo();
		info2.setEnvVariableId("envVariableId");
		info2.setDescription("説明");
		info2.setValue("value");

		judge(true,info1.equals(info2));

		System.out.println("*** 「環境変数ID」のみ違う ***");
		info2 = new JobEnvVariableInfo();
		info2.setEnvVariableId("envVariableId_1");
		info2.setDescription("説明");
		info2.setValue("value");

		judge(false,info1.equals(info2));

		System.out.println("*** 「説明」のみ違う ***");
		info2 = new JobEnvVariableInfo();
		info2.setEnvVariableId("envVariableId");
		info2.setDescription("");
		info2.setValue("value");

		judge(false,info1.equals(info2));

		System.out.println("*** 「値」のみ違う ***");
		info2 = new JobEnvVariableInfo();
		info2.setEnvVariableId("envVariableId");
		info2.setDescription("");
		info2.setValue("value_1");

		judge(false,info1.equals(info2));
	}


	public static JobEnvVariableInfo createSampleInfo() {
		JobEnvVariableInfo info = new JobEnvVariableInfo();
		info.setEnvVariableId("envVariableId");
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

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}