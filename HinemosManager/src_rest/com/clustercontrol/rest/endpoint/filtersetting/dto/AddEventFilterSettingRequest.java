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
public class AddEventFilterSettingRequest extends AbstractAddFilterSettingRequest {

	public AddEventFilterSettingRequest() {
	}
	
	@RestItemName(MessageConstant.EVENT_FILTER)
	// validate メソッドで値チェック
	private EventFilterBaseRequest eventFilter;

	// 管理者のみ指定可能項目のため別枠管理でチェックも個別実装
	private String ownerUserId;
	
	@Override
	public void correlationCheck() throws InvalidSetting {
		// NOP
	}

	/**
	 * DTOに含まれない情報をもとに追加のバリデーションを行います。
	 * @param common
	 * @param filterCategory
	 * @throws InvalidSetting バリデーションエラー。
	 */
	public void validate(boolean common) throws InvalidSetting {
		// 共通フィルタ設定ならオーナーロールIDをチェック
		if (common) {
			CommonValidator.validateId(MessageConstant.OWNER_ROLE_ID.getMessage(), getOwnerRoleId(),
					DataRangeConstant.OWNER_ROLE_ID_MAXLEN);
		}
		
		CommonValidator.validateNull(MessageConstant.EVENT_FILTER.getMessage(), eventFilter);
		eventFilter.correlationCheck();
	}

	public EventFilterBaseRequest getEventFilter() {
		return eventFilter;
	}

	public void setEventFilter(EventFilterBaseRequest eventFilter) {
		this.eventFilter = eventFilter;
	}

	public String getOwnerUserId() {
		return ownerUserId;
	}

	public void setOwnerUserId(String ownerUserId) {
		this.ownerUserId = ownerUserId;
	}
	
}
