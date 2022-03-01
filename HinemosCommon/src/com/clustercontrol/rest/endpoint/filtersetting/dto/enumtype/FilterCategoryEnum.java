/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;
import com.clustercontrol.util.MessageConstant;

/**
 * フィルタ分類
 */
public enum FilterCategoryEnum implements EnumDto<Integer> {

	/** 0: ステータス通知 */
	STATUS(0, MessageConstant.STATUS_FILTER.getMessage(), "status"),

	/** 1: イベント通知 */
	EVENT(1, MessageConstant.EVENT_FILTER.getMessage(), "event"),

	/** 2: (抜け番号) フィルタ分類を新しく追加する際に使用しても構いません。 */

	/** 3: ジョブ履歴 */
	JOB_HISTORY(3, MessageConstant.JOB_HISTORY_FILTER.getMessage(), "job_history"),

	;

	/** DBにおけるコード値 */
	private Integer code;

	/** このフィルタ分類を表示する際の多言語メッセージコード */
	private String label;

	/** URLパス内での表現 */
	private String path;

	private FilterCategoryEnum(int code, String label, String path) {
		this.code = Integer.valueOf(code);
		this.label = label;
		this.path = path;
	}

	public static FilterCategoryEnum fromCode(Integer code) {
		for (FilterCategoryEnum it : FilterCategoryEnum.values()) {
			if (it.code.equals(code)) return it;
		}
		throw new IllegalArgumentException("Unknown code=" + code);
	}

	public static FilterCategoryEnum fromPath(String path) {
		for (FilterCategoryEnum it : FilterCategoryEnum.values()) {
			if (it.path.equals(path)) return it;
		}
		throw new IllegalArgumentException("Unknown path=" + path);
	}

	@Override
	public Integer getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public String getPath() {
		return path;
	}

}
