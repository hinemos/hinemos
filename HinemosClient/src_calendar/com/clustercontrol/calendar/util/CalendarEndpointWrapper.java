package com.clustercontrol.calendar.util;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.calendar.CalendarDetailInfo;
import com.clustercontrol.ws.calendar.CalendarDuplicate_Exception;
import com.clustercontrol.ws.calendar.CalendarEndpoint;
import com.clustercontrol.ws.calendar.CalendarEndpointService;
import com.clustercontrol.ws.calendar.CalendarInfo;
import com.clustercontrol.ws.calendar.CalendarNotFound_Exception;
import com.clustercontrol.ws.calendar.CalendarPatternInfo;
import com.clustercontrol.ws.calendar.HinemosUnknown_Exception;
import com.clustercontrol.ws.calendar.InvalidRole_Exception;
import com.clustercontrol.ws.calendar.InvalidSetting_Exception;
import com.clustercontrol.ws.calendar.InvalidUserPass_Exception;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class CalendarEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( CalendarEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public CalendarEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static CalendarEndpointWrapper getWrapper(String managerName) {
		return new CalendarEndpointWrapper(EndpointManager.get(managerName));
	}

	private static List<EndpointSetting<CalendarEndpoint>> getCalendarEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(CalendarEndpointService.class, CalendarEndpoint.class);
	}

	/**
	 * カレンダ情報を返す。
	 * @param id
	 * @return
	 * @throws CalendarNotFound_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 */
	public CalendarInfo getCalendar(String id) throws CalendarNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CalendarEndpoint> endpointSetting : getCalendarEndpoint(endpointUnit)) {
			try {
				CalendarEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getCalendar(id);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getCalendar(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	/**
	 * カレンダ情報を登録する。
	 * カレンダ設定ダイアログから情報を登録する際に使用されている。
	 * @param calendarInfo
	 * @throws CalendarDuplicate_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidSetting_Exception
	 */
	public void addCalendar(CalendarInfo calendarInfo) throws CalendarDuplicate_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception,InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CalendarEndpoint> endpointSetting : getCalendarEndpoint(endpointUnit)) {
			try {
				CalendarEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addCalendar(calendarInfo);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addCalendar(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * カレンダ情報を変更する。
	 * @param calendarInfo
	 * @throws CalendarNotFound_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidSetting_Exception
	 */
	public void modifyCalendar(CalendarInfo calendarInfo) throws CalendarNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CalendarEndpoint> endpointSetting : getCalendarEndpoint(endpointUnit)) {
			try {
				CalendarEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyCalendar(calendarInfo);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyCalendar(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}


	/**
	 * カレンダ情報を削除する。
	 * @param uidList
	 * @throws CalendarNotFound_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 */
	public void deleteCalendar(List<String> uidList) throws CalendarNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CalendarEndpoint> endpointSetting : getCalendarEndpoint(endpointUnit)) {
			try {
				CalendarEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteCalendar(uidList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteCalendar(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * すべてのカレンダ情報を取得する。
	 * @return
	 * @throws CalendarNotFound_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 */
	public List<CalendarInfo> getAllCalendarList() throws CalendarNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CalendarEndpoint> endpointSetting : getCalendarEndpoint(endpointUnit)) {
			try {
				CalendarEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getAllCalendarList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getAllCalendarList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * オーナーロールIDを条件としてカレンダ情報を取得する。
	 * @param ownerRoleId
	 * @return
	 * @throws CalendarNotFound_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 */
	public List<CalendarInfo> getCalendarList(String ownerRoleId) throws CalendarNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CalendarEndpoint> endpointSetting : getCalendarEndpoint(endpointUnit)) {
			try {
				CalendarEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getCalendarList(ownerRoleId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getCalendarListByOwnerRole(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * CalendarMonthViewに表示する一月分のカレンダ詳細情報を取得する。
	 * @param year
	 * @param month
	 * @return
	 * @throws CalendarNotFound_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 */
	public List<Integer> getCalendarMonth(String id, Integer year,Integer month) throws CalendarNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CalendarEndpoint> endpointSetting : getCalendarEndpoint(endpointUnit)) {
			try {
				CalendarEndpoint endpoint = (CalendarEndpoint) endpointSetting.getEndpoint();
				return endpoint.getCalendarMonth(id, year, month);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getCalendarMonth(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	/**
	 * CalendarWeekViewに表示する1週間分のカレンダ情報を取得する。
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 * @throws CalendarNotFound_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 */
	public List<CalendarDetailInfo> getCalendarWeek(String id, Integer year, Integer month, Integer day)
			throws CalendarNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CalendarEndpoint> endpointSetting : getCalendarEndpoint(endpointUnit)) {
			try {
				CalendarEndpoint endpoint = (CalendarEndpoint) endpointSetting.getEndpoint();
				List<CalendarDetailInfo> list = endpoint.getCalendarWeek(id, year, month, day);
				return list;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getCalendarWeek(), " + HinemosMessage.replace(e.getMessage()));
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * カレンダ[カレンダパターン]情報の一覧を取得する<BR>
	 * @param ownerRoleId オーナーロールID
	 * @return
	 * @throws CalendarNotFound_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 */
	public List<CalendarPatternInfo> getCalendarPatternList(String ownerRoleId) throws CalendarNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CalendarEndpoint> endpointSetting : getCalendarEndpoint(endpointUnit)) {
			try {
				CalendarEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getCalendarPatternList(ownerRoleId);

			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getCalendarPatternList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	/**
	 * IDと一致するカレンダ[カレンダパターン]情報を取得する<BR>
	 * @param id
	 * @return
	 * @throws CalendarNotFound_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 */
	public CalendarPatternInfo getCalendarPattern(String id) throws CalendarNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CalendarEndpoint> endpointSetting : getCalendarEndpoint(endpointUnit)) {
			try {
				CalendarEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getCalendarPattern(id);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getCalendarPattern(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	/**
	 * カレンダ[カレンダパターン]情報を登録する<BR>
	 * @param info
	 * @throws CalendarDuplicate_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidSetting_Exception
	 * @throws CalendarNotFound_Exception
	 */
	public void addCalendarPattern(CalendarPatternInfo info)
			throws CalendarDuplicate_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception,InvalidSetting_Exception, CalendarNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CalendarEndpoint> endpointSetting : getCalendarEndpoint(endpointUnit)) {
			try {
				CalendarEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addCalendarPattern(info);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addCalendarPattern(), " + HinemosMessage.replace(e.getMessage()));
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * カレンダ[カレンダパターン]情報を変更する<BR>
	 * @param info
	 * @throws CalendarNotFound_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidSetting_Exception
	 */
	public void modifyCalendarPattern(CalendarPatternInfo info) throws CalendarNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CalendarEndpoint> endpointSetting : getCalendarEndpoint(endpointUnit)) {
			try {
				CalendarEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyCalendarPattern(info);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyCalendarPattern(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	/**
	 * IDと一致するカレンダ[カレンダパターン]情報を削除する<BR>
	 * @param idList
	 * @throws CalendarNotFound_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 */
	public void deleteCalendarPattern(List<String> idList) throws CalendarNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CalendarEndpoint> endpointSetting : getCalendarEndpoint(endpointUnit)) {
			try {
				CalendarEndpoint endpoint = (CalendarEndpoint) endpointSetting.getEndpoint();
				endpoint.deleteCalendarPattern(idList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteCalendarPattern(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
}
