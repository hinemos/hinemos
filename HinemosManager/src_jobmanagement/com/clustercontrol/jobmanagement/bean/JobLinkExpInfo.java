/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * ジョブ連携メッセージの拡張情報設定を保持するクラス
 * 
 */
/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Requestクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobLinkExpInfo implements Serializable, Comparable<JobLinkExpInfo>, RequestDto {

	/** シリアライズ可能クラスに定義するUID */
	@JsonIgnore
	private static final long serialVersionUID = -2690504181259485106L;

	@JsonIgnore
	private static Log m_log = LogFactory.getLog( JobLinkExpInfo.class );

	/** キー */
	@RestItemName(value = MessageConstant.KEY)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 128, type = CheckType.ID)
	private String key;

	/** 値 */
	@RestItemName(value = MessageConstant.VALUE)
	private String value;

	/**
	 * キーを返す。<BR>
	 * @return キー
	 */
	public String getKey() {
		return key;
	}
	/**
	 * キーを設定する。<BR>
	 * @param key キー
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * 値を返す。<BR>
	 * @return 値
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 値を設定する。<BR>
	 * @param value 値
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobLinkExpInfo)) {
			return false;
		}
		JobLinkExpInfo o1 = this;
		JobLinkExpInfo o2 = (JobLinkExpInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getKey(), o2.getKey()) &&
				equalsSub(o1.getValue(), o2.getValue());
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null) {
			return false;
		}
		boolean ret = o1.equals(o2);
		if (!ret) {
			if (m_log.isTraceEnabled()) {
				m_log.trace("equalsSub : " + o1 + "!=" + o2);
			}
		}
		return ret;
	}

	@Override
	public int compareTo(JobLinkExpInfo o) {
		if (this.key.compareTo(o.key) == 0) {
			return this.value.compareTo(o.value);
		} else {
			return this.key.compareTo(o.key);
		}
	}
	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}