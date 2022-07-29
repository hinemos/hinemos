/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum ResourceJobActionEnum implements EnumDto<Integer> {

	/** パワーオン */
	TYPE_POWERON(0),
	/** パワーオフ */
	TYPE_POWEROFF(1),
	/** 再起動 */
	TYPE_REBOOT(2),
	/** サスペンド */
	TYPE_SUSPEND(3),
	/** スナップショット */
	TYPE_SNAPSHOT(4),
	/** アタッチ */
	TYPE_ATTACH(5),
	/** デタッチ */
	TYPE_DETACH(6);

	private Integer code;

	private ResourceJobActionEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}
}
