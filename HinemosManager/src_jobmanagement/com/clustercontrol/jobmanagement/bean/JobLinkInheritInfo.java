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


/**
 * ジョブ連携メッセージの引継ぎ設定を保持するクラス
 * 
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobLinkInheritInfo implements Serializable, Comparable<JobLinkInheritInfo> {

	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -8469994270722165079L;

	private static Log m_log = LogFactory.getLog( JobLinkInheritInfo.class );

	/** ジョブ変数 */
	private String paramId;

	/** メッセージ情報 */
	private String keyInfo;

	/** 拡張情報キー */
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
}