/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.JobLinkInheritKeyInfo;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.util.RestItemNameResolver;
import com.clustercontrol.util.MessageConstant;

/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Infoクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
public class JobLinkInheritInfoRequest implements RequestDto {
	/** ジョブ変数 */
	@RestItemName(value = MessageConstant.JOB_PARAM_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String paramId;

	/** メッセージ情報 */
	@RestItemName(value = MessageConstant.MESSAGE_INFO)
	@RestValidateObject(notNull = true)
	@RestBeanConvertEnum
	private JobLinkInheritKeyInfo keyInfo;

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
	public JobLinkInheritKeyInfo getKeyInfo() {
		return keyInfo;
	}
	/**
	 * メッセージ情報を設定する。<BR>
	 * @param key メッセージ情報
	 */
	public void setKeyInfo(JobLinkInheritKeyInfo keyInfo) {
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
	 * @param value 拡張情報キー
	 */
	public void setExpKey(String expKey) {
		this.expKey = expKey;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		// [メッセージ情報]が「拡張情報」の場合、[拡張情報キー]必須
		if (keyInfo == JobLinkInheritKeyInfo.EXP_INFO
			&& (expKey == null || expKey.isEmpty())) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "expKey");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}
	}
}
