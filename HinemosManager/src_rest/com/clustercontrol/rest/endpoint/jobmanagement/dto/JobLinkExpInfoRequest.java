/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Infoクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
public class JobLinkExpInfoRequest implements RequestDto {
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
	public void correlationCheck() throws InvalidSetting {
	}
}
