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
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * ジョブの後続ジョブ優先度に関する情報を保持するクラス
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Requestクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE) //JSONから変換する際、getter名、setter名を無視し、フィールド名のみを参照して変換する。
public class JobNextJobOrderInfo implements Serializable, Cloneable, RequestDto {
	/** シリアライズ可能クラスに定義するUID */
	@JsonIgnore
	private static final long serialVersionUID = 1L;

	/** ジョブユニットID */
	private String jobunit_id;

	/** 先行ジョブID */
	private String job_id;

	/** 後続ジョブID */
	private String next_job_id;

	public String getJobunitId() {
		return jobunit_id;
	}

	public void setJobunitId(String jobunit_id) {
		this.jobunit_id = jobunit_id;
	}

	public String getJobId() {
		return job_id;
	}

	public void setJobId(String job_id) {
		this.job_id = job_id;
	}

	public String getNextJobId() {
		return next_job_id;
	}

	public void setNextJobId(String next_job_id) {
		this.next_job_id = next_job_id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((job_id == null) ? 0 : job_id.hashCode());
		result = prime * result + ((jobunit_id == null) ? 0 : jobunit_id.hashCode());
		result = prime * result + ((next_job_id == null) ? 0 : next_job_id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobNextJobOrderInfo other = (JobNextJobOrderInfo) obj;
		if (job_id == null) {
			if (other.job_id != null)
				return false;
		} else if (!job_id.equals(other.job_id))
			return false;
		if (jobunit_id == null) {
			if (other.jobunit_id != null)
				return false;
		} else if (!jobunit_id.equals(other.jobunit_id))
			return false;
		if (next_job_id == null) {
			if (other.next_job_id != null)
				return false;
		} else if (!next_job_id.equals(other.next_job_id))
			return false;
		return true;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		JobNextJobOrderInfo jobNextJobOrderInfo = (JobNextJobOrderInfo) super.clone();
		return jobNextJobOrderInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
