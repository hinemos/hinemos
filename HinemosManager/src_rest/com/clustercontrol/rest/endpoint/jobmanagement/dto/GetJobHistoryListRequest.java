/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.filtersetting.dto.JobHistoryFilterBaseRequest;

public class GetJobHistoryListRequest implements RequestDto {

	// filter しかプロパティを持たないため本クラスの存在意義が疑われるかもしれませんが、
	// 将来的に filter 以外のパラメータを追加する場合を想定しています。

	@RestValidateObject(notNull = true)
	private JobHistoryFilterBaseRequest filter;

	// 取得されるレコードの上限数
	@RestValidateInteger(notNull = false,minVal=0)
	private Integer size ;

	public GetJobHistoryListRequest() {
	}

	public JobHistoryFilterBaseRequest getFilter() {
		return filter;
	}

	public void setFilter(JobHistoryFilterBaseRequest filter) {
		this.filter = filter;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		filter.correlationCheck();
	}

}
