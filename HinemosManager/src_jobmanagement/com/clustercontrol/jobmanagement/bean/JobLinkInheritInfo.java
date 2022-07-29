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
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.annotation.EnumerateString;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.deserializer.EnumToStringDeserializer;
import com.clustercontrol.rest.util.RestItemNameResolver;
import com.clustercontrol.util.MessageConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


/**
 * ジョブ連携メッセージの引継ぎ設定を保持するクラス
 * 
 */
/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Requestクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobLinkInheritInfo implements Serializable, Comparable<JobLinkInheritInfo>, RequestDto {

	/** シリアライズ可能クラスに定義するUID */
	@JsonIgnore
	private static final long serialVersionUID = -8469994270722165079L;

	@JsonIgnore
	private static Log m_log = LogFactory.getLog( JobLinkInheritInfo.class );

	/** ジョブ変数 */
	@RestItemName(value = MessageConstant.JOB_PARAM_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String paramId;

	/** メッセージ情報 */
	@RestItemName(value = MessageConstant.MESSAGE_INFO)
	@RestValidateObject(notNull = true)
	@JsonDeserialize(using=EnumToStringDeserializer.class)
	@EnumerateString(enumDto=JobLinkInheritKeyInfo.class)
	private String keyInfo;

	/** 拡張情報キー */
	@RestItemName(value = MessageConstant.EXTENDED_INFO_KEY)
	@RestValidateString(maxLen = 128, type = CheckType.ID)
	private String expKey;

	/**
	 * ジョブ変数を返す。<BR>
	 * @return ジョブ変数
	 */
	public String getParamId() {
		return paramId;
	}

	/**
	 * ジョブ変数を設定する。<BR>
	 * @param value ジョブ変数
	 */
	public void setParamId(String paramId) {
		this.paramId = paramId;
	}

	/**
	 * メッセージ情報を返す。<BR>
	 * @return メッセージ情報
	 */
	public String getKeyInfo() {
		return keyInfo;
	}
	/**
	 * メッセージ情報を設定する。<BR>
	 * @param key メッセージ情報
	 */
	public void setKeyInfo(String keyInfo) {
		this.keyInfo = keyInfo;
	}

	/**
	 * 拡張情報キーを返す。<BR>
	 * @return 拡張情報キー
	 */
	public String getExpKey() {
		return expKey;
	}
	/**
	 * 拡張情報キーを設定する。<BR>
	 * @param key 拡張情報キー
	 */
	public void setExpKey(String expKey) {
		this.expKey = expKey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((paramId == null) ? 0 : paramId.hashCode());
		result = prime * result + ((keyInfo == null) ? 0 : keyInfo.hashCode());
		result = prime * result + ((expKey == null) ? 0 : expKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobLinkInheritInfo)) {
			return false;
		}
		JobLinkInheritInfo o1 = this;
		JobLinkInheritInfo o2 = (JobLinkInheritInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getParamId(), o2.getParamId()) &&
				equalsSub(o1.getKeyInfo(), o2.getKeyInfo()) &&
				equalsSub(o1.getExpKey(), o2.getExpKey());
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
	public int compareTo(JobLinkInheritInfo o) {
		if (this.paramId.compareTo(o.paramId) == 0) {
			if (this.keyInfo.compareTo(o.keyInfo) == 0) {
				return this.expKey.compareTo(o.expKey);
			} else {
				return this.keyInfo.compareTo(o.keyInfo);
			}
		} else {
			return this.paramId.compareTo(o.paramId);
		}
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		// [メッセージ情報]が「拡張情報」の場合、[拡張情報キー]必須
		if (keyInfo.equals(JobLinkInheritKeyInfo.EXP_INFO.getCode())
			&& (expKey == null || expKey.isEmpty())) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "expKey");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}
	}
}