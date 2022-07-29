/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto.enumtype;

import com.clustercontrol.repository.bean.AgentUpdateStatus;
import com.clustercontrol.rest.dto.EnumDto;

public enum AgentUpdateStatusEnum implements EnumDto<String> {

	/** 済 : 最新版 */
	DONE(AgentUpdateStatus.DONE.name()),
	/** 未 : 他のいずれの状況にも該当しない場合 */
	NOT_YET(AgentUpdateStatus.NOT_YET.name()),
	/** 再起動中: 再起動キューに入っている状態 */
	RESTARTING(AgentUpdateStatus.RESTARTING.name()),
	/** 更新中: 更新キューに入っている状態 */
	UPDATING(AgentUpdateStatus.UPDATING.name()),
	/** 判定中: マネージャがプロファイル情報を未受領の状態 */
	UNKNOWN(AgentUpdateStatus.UNKNOWN.name()),
	/** 非対応: アップデート非対応のバージョン */
	UNSUPPORTED(AgentUpdateStatus.UNSUPPORTED.name());

	private final String code;

	private AgentUpdateStatusEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
