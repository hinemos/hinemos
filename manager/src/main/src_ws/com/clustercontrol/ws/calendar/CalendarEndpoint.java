/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.calendar;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.calendar.model.CalendarDetailInfo;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.model.CalendarPatternInfo;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.fault.CalendarDuplicate;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * カレンダー用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://calendar.ws.clustercontrol.com")
public class CalendarEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( CalendarEndpoint.class );
	private static Log m_opelog = LogFactory.getLog("HinemosOperation");

	/**
	 * echo(WebサービスAPI疎通用)
	 *
	 * 権限必要なし（ユーザ名チェックのみ実施）
	 *
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String echo(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}


	/**
	 * カレンダ一覧を取得します。<BR>
	 * 返り値のCalendarInfoのメンバ変数ArrayList<CalendarDetailInfoは、空っぽ。
	 *
	 * CalendarRead権限が必要
	 *
	 * @return カレンダ情報のリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<CalendarInfo> getAllCalendarList() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getAllCalendarList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Get Calendar, Method=getAllCalendarList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new CalendarControllerBean().getAllCalendarList();
	}


	/**
	 * オーナーロールIDを条件としてカレンダ一覧を取得します。<BR>
	 * 返り値のCalendarInfoのメンバ変数ArrayList<CalendarDetailInfoは、空っぽ。
	 *
	 * CalendarRead権限が必要
	 *
	 * @param ownerRoleId
	 * @return カレンダ情報のリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<CalendarInfo> getCalendarList(String ownerRoleId) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getCalendarListByOwnerRole");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Get Calendar, Method=getCalendarListByOwnerRole, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new CalendarControllerBean().getCalendarList(ownerRoleId);
	}

	/**
	 * 引数で指定したカレンダーIDに対応するカレンダ情報を取得します。<BR>
	 *
	 * CalendarRead権限が必要
	 *
	 * @param id
	 * @return カレンダ情報
	 * @throws HinemosUnknown
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public CalendarInfo getCalendar(String id) throws CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getCalendar");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", CalendarID=");
		msg.append(id);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Get Calendar, Method=getCalendar, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new CalendarControllerBean().getCalendar(id);
	}

	/**
	 * カレンダ（基本）情報を登録します。<BR>
	 *
	 * 引数のDTOの内容をマネージャに登録します。
	 *
	 * CalendarAdd権限が必要
	 *
	 * @param info　登録するカレンダー（基本）情報
	 * @return
	 * @throws HinemosUnknown
	 * @throws CalendarDuplicate
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 */
	public void addCalendar(CalendarInfo info) throws CalendarDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.debug("addCalendar " + info);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", CalendarID=");
			msg.append(info.getCalendarId());
		}

		try {
			new CalendarControllerBean().addCalendar(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Add Calendar Failed, Method=addCalendar, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Add Calendar, Method=addCalendar, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * カレンダ（基本）情報を変更します。<BR>
	 *
	 * 引数のプロパティの内容で更新します。
	 *
	 * CalendarWrite権限が必要
	 *
	 * @param info
	 * @return
	 * @throws HinemosUnknown
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 */
	public void modifyCalendar(CalendarInfo info) throws CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.debug("modifyCalendar " + info);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", CalendarID=");
			msg.append(info.getCalendarId());
		}

		try {
			new CalendarControllerBean().modifyCalendar(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Change Calendar Failed, Method=modifyCalendar, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Change Calendar, Method=modifyCalendar, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * カレンダ(基本）情報を 削除します。<BR>
	 *
	 * 引数のIDに対応するカレンダー（基本）情報を削除します。
	 *
	 * CalendarWrite権限が必要
	 *
	 * @param idList カレンダーIDリスト
	 * @return
	 * @throws HinemosUnknown
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void deleteCalendar(List<String> idList) throws CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.debug("deleteCalendar " + idList);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", CalendarID=");
		msg.append(idList);

		try {
			new CalendarControllerBean().deleteCalendar(idList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Delete Calendar Failed, Method=deleteCalendar, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Delete Calendar, Method=deleteCalendar, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 *
	 * 引数で指定された、年月の1日から順番に、まるさんかくばつを詰めたArrayListを返す。
	 * まる：0
	 * さんかく：1
	 * ばつ：2
	 *
	 * @param year
	 * @param month
	 * @return
	 * @throws HinemosUnkown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws CalendarNotFound
	 */
	public ArrayList<Integer> getCalendarMonth(String id, Integer year, Integer month) throws HinemosUnknown, InvalidUserPass, InvalidRole, CalendarNotFound {
		m_log.debug("getCalendarSummaryInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", id=");
		msg.append(id);
		msg.append(", year=");
		msg.append(year);
		msg.append(", month=");
		msg.append(month);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " getCalendarMonthInfo, Method=getCalendarMonthInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		return new CalendarControllerBean().getCalendarMonth(id, year, month);
	}

	/**
	 * スケジュールバーを表示するための情報取得
	 * 第ｘ何曜日など、その他にて選択される祝日など年月日がDTOとして保持されないものは、
	 * 年月日に変換して保持し、カレンダ実行予定ビューを表示する際に使用する。
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws CalendarNotFound
	 *
	 */
	public ArrayList<CalendarDetailInfo> getCalendarWeek(String id, Integer year, Integer month, Integer day)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, CalendarNotFound{
		m_log.debug("getCalendarWeek");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", calendarId=");
		msg.append(id);
		msg.append(", year=");
		msg.append(year);
		msg.append(", month=");
		msg.append(month);
		msg.append(", day=");
		msg.append(day);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " getCalendarWeek, Method=getCalendarWeek, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new CalendarControllerBean().getCalendarWeek(id, year, month, day);
	}

	/**
	 * カレンダ[カレンダパターン]情報一覧を取得します。<BR>
	 *
	 * CalendarRead権限が必要
	 *
	 * @p9aram ownerRoleId オーナーロールID
	 * @return カレンダ[カレンダパターン]情報のリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<CalendarPatternInfo> getCalendarPatternList(String ownerRoleId) throws HinemosUnknown, InvalidUserPass, InvalidRole, CalendarNotFound {
		m_log.debug("getCalendarPatternList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Get Calendar Pattern, Method=getCalendarPatternList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new CalendarControllerBean().getCalendarPatternList(ownerRoleId);
	}
	/**
	 * 引数で指定したIDに対応するカレンダ[カレンダパターン]情報を取得します。<BR>
	 * @param id
	 * @return
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public CalendarPatternInfo getCalendarPattern(String id) throws CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getCalendarPattern");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", CalendarPatternID=");
		msg.append(id);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Get Calendar Pattern, Method=getCalendarPattern, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		//カレンダパターンを取得する
		return new CalendarControllerBean().getCalendarPattern(id);
	}

	/**
	 * カレンダ[カレンダパターン]情報を登録します。<BR>
	 *
	 * 引数のDTOの内容をマネージャに登録します。
	 *
	 * CalendarAdd権限が必要
	 *
	 * @param info 登録するカレンダ[カレンダパターン]情報
	 * @return
	 * @throws HinemosUnknown
	 * @throws CalendarDuplicate
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 */
	public void addCalendarPattern(CalendarPatternInfo info) throws CalendarDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.debug("addCalendarPattern " + info);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", calendarPatternID=");
			msg.append(info.getCalPatternId());
		}

		try {
			new CalendarControllerBean().addCalendarPattern(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Add Calendar Pattern Failed, Method=addCalendarPattern, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Add Calendar Pattern, Method=addCalendarPattern, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * カレンダ[カレンダパターン]情報を変更します。<BR>
	 *
	 * 引数のプロパティの内容で更新します。
	 *
	 * CalendarWrite権限が必要
	 *
	 * @param info
	 * @return
	 * @throws HinemosUnknown
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 */
	public void modifyCalendarPattern(CalendarPatternInfo info) throws CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.debug("modifyCalendarPattern " + info);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", calendarPatternID=");
			msg.append(info.getCalPatternId());
		}

		try {
			new CalendarControllerBean().modifyCalendarPattern(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Change Calendar Pattern Failed, Method=modifyCalendarPattern, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Change Calendar Pattern, Method=modifyCalendarPattern, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * カレンダ[カレンダパターン]情報を 削除します。<BR>
	 *
	 * 引数のIDに対応するカレンダ[カレンダパターン]情報を削除します。
	 *
	 * CalendarWrite権限が必要
	 *
	 * @param idList IDリスト
	 * @return
	 * @throws HinemosUnknown
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void deleteCalendarPattern(List<String> idList) throws CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("deleteCalendarPattern " + idList);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", calendarPatternID=");
		msg.append(idList);

		try {
			new CalendarControllerBean().deleteCalendarPattern(idList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Delete Calendar Pattern Failed, Method=deleteCalendarPattern, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_CALENDAR + " Delete Calendar Pattern, Method=deleteCalendarPattern, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
}