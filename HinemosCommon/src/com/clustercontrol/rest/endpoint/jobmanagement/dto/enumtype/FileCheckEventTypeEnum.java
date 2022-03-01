/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.FileCheckConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum FileCheckEventTypeEnum implements EnumDto<Integer> {
	/** 作成の場合 */
	CREATE(FileCheckConstant.TYPE_CREATE),
	/** 削除の場合 */
	DELETE(FileCheckConstant.TYPE_DELETE),
	/** 変更の場合 */
	MODIFY (FileCheckConstant.TYPE_MODIFY);

	private final Integer code;

	private FileCheckEventTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
