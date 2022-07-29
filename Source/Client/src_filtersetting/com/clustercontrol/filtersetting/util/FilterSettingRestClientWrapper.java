/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.util;

import java.util.List;

import org.openapitools.client.model.AddEventFilterSettingRequest;
import org.openapitools.client.model.AddJobHistoryFilterSettingRequest;
import org.openapitools.client.model.AddStatusFilterSettingRequest;
import org.openapitools.client.model.EventFilterSettingResponse;
import org.openapitools.client.model.FilterSettingSummariesResponse;
import org.openapitools.client.model.JobHistoryFilterSettingResponse;
import org.openapitools.client.model.ModifyEventFilterSettingRequest;
import org.openapitools.client.model.ModifyJobHistoryFilterSettingRequest;
import org.openapitools.client.model.ModifyStatusFilterSettingRequest;
import org.openapitools.client.model.StatusFilterSettingResponse;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.FilterSettingDuplicate;
import com.clustercontrol.fault.FilterSettingNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class FilterSettingRestClientWrapper {

	private RestConnectUnit connectUnit;

	private final RestKind restKind = RestKind.FilterSettingRestEndpoints;

	public static FilterSettingRestClientWrapper getWrapper(String managerName) {
		return new FilterSettingRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public FilterSettingRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}
	
	public FilterSettingSummariesResponse getCommonFilterSettingSummaries(FilterCategoryEnum category, String pattern)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<FilterSettingSummariesResponse> proxy = new RestUrlSequentialExecuter<FilterSettingSummariesResponse>(this.connectUnit, this.restKind) {
			@Override
			public FilterSettingSummariesResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingGetCommonFilterSettingSummaries(category.getPath(), pattern);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public FilterSettingSummariesResponse getUserFilterSettingSummaries(FilterCategoryEnum category, String pattern, String userId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<FilterSettingSummariesResponse> proxy = new RestUrlSequentialExecuter<FilterSettingSummariesResponse>(this.connectUnit, this.restKind) {
			@Override
			public FilterSettingSummariesResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingGetUserFilterSettingSummaries(category.getPath(), pattern, userId);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public FilterSettingSummariesResponse getAllUserFilterSettingSummaries(FilterCategoryEnum category, String pattern)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<FilterSettingSummariesResponse> proxy = new RestUrlSequentialExecuter<FilterSettingSummariesResponse>(this.connectUnit, this.restKind) {
			@Override
			public FilterSettingSummariesResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingGetAllUserFilterSettingSummaries(category.getPath(), pattern);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public EventFilterSettingResponse getCommonEventFilterSetting(String filterId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound {
		RestUrlSequentialExecuter<EventFilterSettingResponse> proxy = new RestUrlSequentialExecuter<EventFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public EventFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingGetCommonEventFilterSetting(filterId);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public StatusFilterSettingResponse getCommonStatusFilterSetting(String filterId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound {
		RestUrlSequentialExecuter<StatusFilterSettingResponse> proxy = new RestUrlSequentialExecuter<StatusFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public StatusFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingGetCommonStatusFilterSetting(filterId);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public JobHistoryFilterSettingResponse getCommonJobHistoryFilterSetting(String filterId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound {
		RestUrlSequentialExecuter<JobHistoryFilterSettingResponse> proxy = new RestUrlSequentialExecuter<JobHistoryFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public JobHistoryFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingGetCommonJobHistoryFilterSetting(filterId);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public EventFilterSettingResponse getUserEventFilterSetting(String filterId, String userId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound {
		RestUrlSequentialExecuter<EventFilterSettingResponse> proxy = new RestUrlSequentialExecuter<EventFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public EventFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingGetUserEventFilterSetting(filterId, userId);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public StatusFilterSettingResponse getUserStatusFilterSetting(String filterId, String userId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound {
		RestUrlSequentialExecuter<StatusFilterSettingResponse> proxy = new RestUrlSequentialExecuter<StatusFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public StatusFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingGetUserStatusFilterSetting(filterId, userId);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public JobHistoryFilterSettingResponse getUserJobHistoryFilterSetting(String filterId, String userId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound {
		RestUrlSequentialExecuter<JobHistoryFilterSettingResponse> proxy = new RestUrlSequentialExecuter<JobHistoryFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public JobHistoryFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingGetUserJobHistoryFilterSetting(filterId, userId);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public EventFilterSettingResponse addCommonEventFilterSetting(AddEventFilterSettingRequest filterSettingRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingDuplicate {
		RestUrlSequentialExecuter<EventFilterSettingResponse> proxy = new RestUrlSequentialExecuter<EventFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public EventFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingAddCommonEventFilterSetting(filterSettingRequest);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingDuplicate def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public StatusFilterSettingResponse addCommonStatusFilterSetting(AddStatusFilterSettingRequest filterSettingRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingDuplicate {
		RestUrlSequentialExecuter<StatusFilterSettingResponse> proxy = new RestUrlSequentialExecuter<StatusFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public StatusFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingAddCommonStatusFilterSetting(filterSettingRequest);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingDuplicate def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public JobHistoryFilterSettingResponse addCommonJobHistoryFilterSetting(AddJobHistoryFilterSettingRequest filterSettingRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingDuplicate {
		RestUrlSequentialExecuter<JobHistoryFilterSettingResponse> proxy = new RestUrlSequentialExecuter<JobHistoryFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public JobHistoryFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingAddCommonJobHistoryFilterSetting(filterSettingRequest);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingDuplicate def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public EventFilterSettingResponse addUserEventFilterSetting(AddEventFilterSettingRequest filterSettingRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingDuplicate {
		RestUrlSequentialExecuter<EventFilterSettingResponse> proxy = new RestUrlSequentialExecuter<EventFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public EventFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingAddUserEventFilterSetting(filterSettingRequest);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingDuplicate def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public StatusFilterSettingResponse addUserStatusFilterSetting(AddStatusFilterSettingRequest filterSettingRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingDuplicate {
		RestUrlSequentialExecuter<StatusFilterSettingResponse> proxy = new RestUrlSequentialExecuter<StatusFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public StatusFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingAddUserStatusFilterSetting(filterSettingRequest);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingDuplicate def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public JobHistoryFilterSettingResponse addUserJobHistoryFilterSetting(AddJobHistoryFilterSettingRequest filterSettingRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingDuplicate {
		RestUrlSequentialExecuter<JobHistoryFilterSettingResponse> proxy = new RestUrlSequentialExecuter<JobHistoryFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public JobHistoryFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingAddUserJobHistoryFilterSetting(filterSettingRequest);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingDuplicate def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public EventFilterSettingResponse modifyCommonEventFilterSetting(String filterId, ModifyEventFilterSettingRequest filterSettingRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound {
		RestUrlSequentialExecuter<EventFilterSettingResponse> proxy = new RestUrlSequentialExecuter<EventFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public EventFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingModifyCommonEventFilterSetting(filterId, filterSettingRequest);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public StatusFilterSettingResponse modifyCommonStatusFilterSetting(String filterId, ModifyStatusFilterSettingRequest filterSettingRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound {
		RestUrlSequentialExecuter<StatusFilterSettingResponse> proxy = new RestUrlSequentialExecuter<StatusFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public StatusFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingModifyCommonStatusFilterSetting(filterId, filterSettingRequest);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public JobHistoryFilterSettingResponse modifyCommonJobHistoryFilterSetting(String filterId, ModifyJobHistoryFilterSettingRequest filterSettingRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound {
		RestUrlSequentialExecuter<JobHistoryFilterSettingResponse> proxy = new RestUrlSequentialExecuter<JobHistoryFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public JobHistoryFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingModifyCommonJobHistoryFilterSetting(filterId, filterSettingRequest);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public EventFilterSettingResponse modifyUserEventFilterSetting(String filterId, ModifyEventFilterSettingRequest filterSettingRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound {
		RestUrlSequentialExecuter<EventFilterSettingResponse> proxy = new RestUrlSequentialExecuter<EventFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public EventFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingModifyUserEventFilterSetting(filterId, filterSettingRequest);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public StatusFilterSettingResponse modifyUserStatusFilterSetting(String filterId, ModifyStatusFilterSettingRequest filterSettingRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound {
		RestUrlSequentialExecuter<StatusFilterSettingResponse> proxy = new RestUrlSequentialExecuter<StatusFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public StatusFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingModifyUserStatusFilterSetting(filterId, filterSettingRequest);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public JobHistoryFilterSettingResponse modifyUserJobHistoryFilterSetting(String filterId, ModifyJobHistoryFilterSettingRequest filterSettingRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound {
		RestUrlSequentialExecuter<JobHistoryFilterSettingResponse> proxy = new RestUrlSequentialExecuter<JobHistoryFilterSettingResponse>(this.connectUnit, this.restKind) {
			@Override
			public JobHistoryFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingModifyUserJobHistoryFilterSetting(filterId, filterSettingRequest);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | FilterSettingNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<EventFilterSettingResponse> deleteCommonEventFilterSetting(String filterIds)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<EventFilterSettingResponse>> proxy = new RestUrlSequentialExecuter<List<EventFilterSettingResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<EventFilterSettingResponse> executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingDeleteCommonEventFilterSettings(filterIds);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<StatusFilterSettingResponse> deleteCommonStatusFilterSetting(String filterIds)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<StatusFilterSettingResponse>> proxy = new RestUrlSequentialExecuter<List<StatusFilterSettingResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<StatusFilterSettingResponse> executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingDeleteCommonStatusFilterSettings(filterIds);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<JobHistoryFilterSettingResponse> deleteCommonJobHistoryFilterSetting(String filterIds)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<JobHistoryFilterSettingResponse>> proxy = new RestUrlSequentialExecuter<List<JobHistoryFilterSettingResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<JobHistoryFilterSettingResponse> executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingDeleteCommonJobHistoryFilterSettings(filterIds);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<EventFilterSettingResponse> deleteUserEventFilterSetting(String filterIds, String userId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<EventFilterSettingResponse>> proxy = new RestUrlSequentialExecuter<List<EventFilterSettingResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<EventFilterSettingResponse> executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingDeleteUserEventFilterSettings(filterIds, userId);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<StatusFilterSettingResponse> deleteUserStatusFilterSetting(String filterIds, String userId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<StatusFilterSettingResponse>> proxy = new RestUrlSequentialExecuter<List<StatusFilterSettingResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<StatusFilterSettingResponse> executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingDeleteUserStatusFilterSettings(filterIds, userId);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<JobHistoryFilterSettingResponse> deleteUserJobHistoryFilterSetting(String filterIds, String userId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<JobHistoryFilterSettingResponse>> proxy = new RestUrlSequentialExecuter<List<JobHistoryFilterSettingResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<JobHistoryFilterSettingResponse> executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.filtersettingDeleteUserJobHistoryFilterSettings(filterIds, userId);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
}
