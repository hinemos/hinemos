/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto.enumtype;

import com.clustercontrol.notify.bean.CommandSettingTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum CommandSettingTypeEnum implements EnumDto<Integer> {
	/** コマンド入力 */
	DIRECT_COMMAND(CommandSettingTypeConstant.DIRECT_COMMAND),
	/** コマンドテンプレート選択 */
	CHOICE_TEMPLATE(CommandSettingTypeConstant.CHOICE_TEMPLATE);
	
	private final Integer code;

	private CommandSettingTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}
}
