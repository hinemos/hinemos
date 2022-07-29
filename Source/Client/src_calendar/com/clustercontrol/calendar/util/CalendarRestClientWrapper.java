/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.calendar.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AddCalendarPatternRequest;
import org.openapitools.client.model.AddCalendarRequest;
import org.openapitools.client.model.CalendarDetailInfoResponseP1;
import org.openapitools.client.model.CalendarInfoResponse;
import org.openapitools.client.model.CalendarMonthResponse;
import org.openapitools.client.model.CalendarPatternInfoResponse;
import org.openapitools.client.model.ModifyCalendarPatternRequest;
import org.openapitools.client.model.ModifyCalendarRequest;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.CalendarDuplicate;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class CalendarRestClientWrapper {
	private static Log m_log = LogFactory.getLog(CalendarRestClientWrapper.class);
	private RestConnectUnit connectUnit;

	private final RestKind restKind = RestKind.CalendarRestEndpoints;

	public static CalendarRestClientWrapper getWrapper(String managerName) {
		return new CalendarRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public CalendarRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}

	public List<CalendarInfoResponse> getCalendarList(String ownerRoleId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getCalendarList : start");
		RestUrlSequentialExecuter<List<CalendarInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CalendarInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<CalendarInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CalendarInfoResponse> result = apiClient.calendarGetCalendarList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CalendarInfoResponse getCalendarInfo(String calendarId)
			throws RestConnectFailed, CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getCalendarInfo : start");
		RestUrlSequentialExecuter<CalendarInfoResponse> proxy = new RestUrlSequentialExecuter<CalendarInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public CalendarInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CalendarInfoResponse result = apiClient.calendarGetCalendar(calendarId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CalendarInfoResponse addCalendar(AddCalendarRequest addCalendarRequest)
			throws RestConnectFailed, CalendarDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {

		m_log.debug("addCalendar : start");
		RestUrlSequentialExecuter<CalendarInfoResponse> proxy = new RestUrlSequentialExecuter<CalendarInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public CalendarInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CalendarInfoResponse result = apiClient.calendarAddCalendar(addCalendarRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | CalendarDuplicate def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CalendarInfoResponse modifyCalendar(String calendarId, ModifyCalendarRequest modifyCalendarRequest)
			throws RestConnectFailed, CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.debug("call modifyCalendar : start");
		RestUrlSequentialExecuter<CalendarInfoResponse> proxy = new RestUrlSequentialExecuter<CalendarInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public CalendarInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CalendarInfoResponse result = apiClient.calendarModifyCalendar(calendarId, modifyCalendarRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting def) {// 想定内例外
			// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CalendarInfoResponse> deleteCalendar(String calendarIds)
			throws RestConnectFailed, CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.debug("call deleteCalendar : start");
		RestUrlSequentialExecuter<List<CalendarInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CalendarInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<CalendarInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CalendarInfoResponse> result = apiClient.calendarDeleteCalendar(calendarIds);

				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | CalendarNotFound | HinemosUnknown | InvalidUserPass | InvalidRole
				| InvalidSetting def) {// 想定内例外
			// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CalendarMonthResponse> getCalendarMonth(String calendarId, Integer year, Integer month)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, CalendarNotFound {
		m_log.debug("call getCalendarMonth : start");
		RestUrlSequentialExecuter<List<CalendarMonthResponse>> proxy = new RestUrlSequentialExecuter<List<CalendarMonthResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<CalendarMonthResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CalendarMonthResponse> result = apiClient.calendarGetCalendarMonth(calendarId, String.valueOf(year), String.valueOf(month));

				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | CalendarNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {// 想定内例外
			// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}

	}

	public List<CalendarDetailInfoResponseP1> getCalendarWeek(String calendarId, Integer year, Integer month,
			Integer day) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, CalendarNotFound {
		m_log.debug("call getCalendarWeek : start");
		RestUrlSequentialExecuter<List<CalendarDetailInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<CalendarDetailInfoResponseP1>>(this.connectUnit, this.restKind) {
			@Override
			public List<CalendarDetailInfoResponseP1> executeMethod(DefaultApi apiClient) throws Exception {
				List<CalendarDetailInfoResponseP1> result = apiClient.calendarGetCalendarWeek(calendarId, String.valueOf(year), String.valueOf(month),
						String.valueOf(day));

				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | CalendarNotFound def) {// 想定内例外
			// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	//

	public List<CalendarPatternInfoResponse> getCalendarPatternList(String ownerRoleId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, CalendarNotFound {
		m_log.debug("call getCalendarPatternList : start");
		RestUrlSequentialExecuter<List<CalendarPatternInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CalendarPatternInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<CalendarPatternInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CalendarPatternInfoResponse> result = apiClient.calendarGetCalendarPatternList(ownerRoleId);

				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | CalendarNotFound def) {// 想定内例外
			// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CalendarPatternInfoResponse getCalendarPattern(String calendarPatternId)
			throws RestConnectFailed, CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("call getCalendarPattern : start");
		RestUrlSequentialExecuter<CalendarPatternInfoResponse> proxy = new RestUrlSequentialExecuter<CalendarPatternInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public CalendarPatternInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CalendarPatternInfoResponse result = apiClient.calendarGetCalendarPattern(calendarPatternId);

				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | CalendarNotFound def) {// 想定内例外
			// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CalendarPatternInfoResponse addCalendarPattern(AddCalendarPatternRequest addCalendarPatternRequest)
			throws RestConnectFailed, CalendarDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.debug("call addCalendarPattern : start");
		RestUrlSequentialExecuter<CalendarPatternInfoResponse> proxy = new RestUrlSequentialExecuter<CalendarPatternInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public CalendarPatternInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CalendarPatternInfoResponse result = apiClient.calendarAddCalendarPattern(addCalendarPatternRequest);

				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | CalendarDuplicate | InvalidUserPass | InvalidRole | HinemosUnknown
				| InvalidSetting def) {// 想定内例外
			// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CalendarPatternInfoResponse modifyCalendarPattern(String calendarPatternId,
			ModifyCalendarPatternRequest modifyCalendarPatternRequest)
			throws RestConnectFailed, CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.debug("call modifyCalendarPattern : start");
		RestUrlSequentialExecuter<CalendarPatternInfoResponse > proxy = new RestUrlSequentialExecuter<CalendarPatternInfoResponse >(this.connectUnit, this.restKind) {
			@Override
			public CalendarPatternInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CalendarPatternInfoResponse result = apiClient.calendarModifyCalendarPattern(calendarPatternId,
						modifyCalendarPatternRequest);

				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting
				| CalendarNotFound def) {// 想定内例外
			// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CalendarPatternInfoResponse> deleteCalendarPattern(String calendarPatternIds)
			throws RestConnectFailed, CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("call deleteCalendarPattern : start");
		RestUrlSequentialExecuter<List<CalendarPatternInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CalendarPatternInfoResponse> >(this.connectUnit, this.restKind) {
			@Override
			public List<CalendarPatternInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CalendarPatternInfoResponse> result = apiClient.calendarDeleteCalendarPattern(calendarPatternIds);

				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | CalendarNotFound def) {// 想定内例外
			// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
}
