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

public enum FileCheckModifyTypeEnum implements EnumDto<Integer> {
	/** 変更 - タイムスタンプの場合 */
	TIMESTAMP(FileCheckConstant.TYPE_MODIFY_TIMESTAMP),
	/** 変更 - ファイルサイズの場合 */
	FILESIZE (FileCheckConstant.TYPE_MODIFY_FILESIZE);

	private final Integer code;

	private FileCheckModifyTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

}
