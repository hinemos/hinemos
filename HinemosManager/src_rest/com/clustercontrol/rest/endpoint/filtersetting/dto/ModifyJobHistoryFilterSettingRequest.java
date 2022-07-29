/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.filtersetting.bean.FilterSettingInfo;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertAssertion(to = FilterSettingInfo.class)
public class ModifyJobHistoryFilterSettingRequest extends AbstractModifyFilterSettingRequest {

	public ModifyJobHistoryFilterSettingRequest() {
	}
	
	@RestItemName(MessageConstant.JOB_HISTORY_FILTER)
	// validate メソッドで値チェック
	private JobHistoryFilterBaseRequest jobHistoryFilter;

	// 管理者のみ指定可能項目のため別枠管理でチェックも個別実装
	private String ownerUserId;
	
	@Override
	public void correlationCheck() throws InvalidSetting {
		// NOP
	}

	/**
	 * DTOに含まれない情報をもとに追加のバリデーションを行います。
	 * @param common
	 * @throws InvalidSetting バリデーションエラー。
	 */
	public void validate(boolean common) throws InvalidSetting {
		// 共通フィルタ設定ならオーナーロールIDをチェック
		if (common) {
			CommonValidator.validateId(MessageConstant.OWNER_ROLE_ID.getMessage(), getOwnerRoleId(),
					DataRangeConstant.OWNER_ROLE_ID_MAXLEN);
		}

		CommonValidator.validateNull(MessageConstant.JOB_HISTORY_FILTER.getMessage(), jobHistoryFilter);
		jobHistoryFilter.correlationCheck();
	}

	public JobHistoryFilterBaseRequest getJobHistoryFilter() {
		return jobHistoryFilter;
	}

	public void setJobHistoryFilter(JobHistoryFilterBaseRequest jobHistoryFilter) {
		this.jobHistoryFilter = jobHistoryFilter;
	}

	public String getOwnerUserId() {
		return ownerUserId;
	}

	public void setOwnerUserId(String ownerUserId) {
		this.ownerUserId = ownerUserId;
	}
	

}
