/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.calendar.factory.ModifyCalendar;
import com.clustercontrol.calendar.factory.SelectCalendar;
import com.clustercontrol.calendar.model.CalendarDetailInfo;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.model.CalendarPatternInfo;
import com.clustercontrol.calendar.util.CalendarCacheManagementCallback;
import com.clustercontrol.calendar.util.CalendarChangedNotificationCallback;
import com.clustercontrol.calendar.util.CalendarPatternCacheManagementCallback;
import com.clustercontrol.calendar.util.CalendarValidator;
import com.clustercontrol.calendar.util.QueryUtil;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CalendarDuplicate;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 *
 * <!-- begin-user-doc --> カレンダー情報の制御を行うsession bean <!-- end-user-doc --> *
 *
 */
public class CalendarControllerBean {

	private static Log m_log = LogFactory.getLog( CalendarControllerBean.class );

	/**
	 * カレンダ一覧を取得します。<BR>
	 *
	 * @return カレンダ情報のリスト
	 * @throws HinemosUnknown
	 */
	public ArrayList<CalendarInfo> getAllCalendarList() throws HinemosUnknown, InvalidRole {
		JpaTransactionManager jtm = null;
		ArrayList<CalendarInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCalendar select = new SelectCalendar();
			list = select.getAllCalendarList(null);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			m_log.info("getAllCalendarList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage(),e);
		} catch (Exception e){
			m_log.warn("getAllCalendarList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * オーナーロールIDを指定してカレンダ一覧を取得します。<BR>
	 *
	 * @param ownerRoleId オーナーロールID
	 * @return カレンダ情報のリスト
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<CalendarInfo> getCalendarList(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<CalendarInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCalendar select = new SelectCalendar();
			list = select.getAllCalendarList(ownerRoleId);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			m_log.info("getCalendarList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage(), e);
		} catch (Exception e){
			m_log.warn("getCalendarList() :"
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * カレンダIDのリストを取得します。<BR>
	 *
	 * 引数のArrayListにはカレンダーidが含まれています。
	 * <p>
	 *	list.add(cal.getCalendar_id());
	 * </p>
	 *
	 * @return カレンダIDのリスト
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getCalendarIdList() throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCalendar select = new SelectCalendar();
			ArrayList<String> list = select.getCalendarIdList();
			jtm.commit();
			return list;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getCalendarIdList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * 引数で指定したカレンダーIDに対応するカレンダ情報を取得します。<BR>
	 *
	 * @param id
	 * @return カレンダ情報
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public CalendarInfo getCalendar(String id) throws CalendarNotFound, HinemosUnknown, InvalidRole {
		JpaTransactionManager jtm = null;
		CalendarInfo info = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCalendar select = new SelectCalendar();
			info = select.getCalendar(id);
			jtm.commit();
		} catch (CalendarNotFound e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw new CalendarNotFound(MessageConstant.MESSAGE_CALENDAR_ID_NOT_EXIST.getMessage(id) , e);
		} catch (ObjectPrivilege_InvalidRole | InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return info;
	}

	/**
	 * 引数で指定したカレンダーIDに対応するカレンダ情報を取得します。<BR>
	 * CalendarInfo.CalendarDetailInfo.etcInfoも取得します。
	 *
	 * @param id
	 * @return カレンダ情報
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public CalendarInfo getCalendarFull(String id) throws CalendarNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		CalendarInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCalendar select = new SelectCalendar();
			info = select.getCalendarFull(id);
			jtm.commit();
		} catch (CalendarNotFound | InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return info;
	}

	/**
	 * カレンダ[カレンダパターン]情報一覧を取得します。<BR>
	 *
	 * @param ownerRoleId オーナーロールID
	 * @return カレンダ[カレンダパターン]情報のリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws CalendarNotFound
	 */
	public ArrayList<CalendarPatternInfo> getCalendarPatternList(String ownerRoleId) throws HinemosUnknown, InvalidRole, CalendarNotFound {
		JpaTransactionManager jtm = null;
		ArrayList<CalendarPatternInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCalendar select = new SelectCalendar();
			list = select.getCalendarPatternList(ownerRoleId);
			jtm.commit();
		} catch (CalendarNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new CalendarNotFound(MessageConstant.MESSAGE_CALENDAR_PATTERN_ID_NOT_EXIST.getMessage() ,e);
		} catch (ObjectPrivilege_InvalidRole | InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage(), e);
		} catch (Exception e){
			m_log.warn("getCalendarPatternList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * 引数で指定したIDに対応するカレンダ[カレンダパターン]情報を取得します。<BR>
	 *
	 * @param id
	 * @return カレンダ[カレンダパターン]情報
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public CalendarPatternInfo getCalendarPattern(String id) throws CalendarNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		CalendarPatternInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCalendar select = new SelectCalendar();
			info = select.getCalendarPattern(id);
			jtm.commit();
		} catch (CalendarNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new CalendarNotFound(MessageConstant.MESSAGE_CALENDAR_PATTERN_ID_NOT_EXIST.getMessage(id), e);
		} catch (ObjectPrivilege_InvalidRole | InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getCalendarPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return info;
	}

	/**
	 * カレンダ（基本）情報を登録します。<BR>
	 *
	 * 引数のDTOの内容をマネージャに登録します。
	 *
	 * @param info　登録するカレンダー（基本）情報
	 * @return
	 * @throws CalendarDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public CalendarInfo addCalendar(CalendarInfo info)
			throws CalendarDuplicate, HinemosUnknown, InvalidSetting, InvalidRole {
		JpaTransactionManager jtm = null;
		//findbugs対応 ret は nullで初期化とする
		CalendarInfo ret = null;
		try{
			jtm = new JpaTransactionManager();

			jtm.begin();

			//入力チェック
			CalendarValidator.validateCalendarInfo(info);
			
			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(info.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

			ModifyCalendar modify = new ModifyCalendar();
			modify.addCalendar(info, loginUser);

			jtm.addCallback(new CalendarCacheManagementCallback(info.getCalendarId()));
			jtm.addCallback(new CalendarChangedNotificationCallback());
			jtm.commit();
			
			ret = new SelectCalendar().getCalendar(info.getCalendarId());
		} catch (CalendarDuplicate e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new CalendarDuplicate(MessageConstant.MESSAGE_CALENDAR_ID_IS_ALREADY_REGISTERED.getMessage(info.getCalendarId()),e);
		} catch (InvalidSetting e){
			throw e; // validate内でメッセージをセット済みのため new しない
		} catch (ObjectPrivilege_InvalidRole | InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			m_log.info("addCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage(), e);
		} catch(Exception e){
			m_log.info("addCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			// EntityManagerクリア
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * カレンダ（基本）情報を変更します。<BR>
	 *
	 * 引数のプロパティの内容で更新します。
	 *
	 * @param info
	 * @return
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public CalendarInfo modifyCalendar(CalendarInfo info) throws CalendarNotFound, HinemosUnknown, InvalidSetting, InvalidRole {
		JpaTransactionManager jtm = null;
		CalendarInfo ret = null; 

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			String ownerRoleId = QueryUtil.getCalInfoPK(info.getCalendarId()).getOwnerRoleId();
			info.setOwnerRoleId(ownerRoleId);
			
			//入力チェック
			CalendarValidator.validateCalendarInfo(info);

			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			ModifyCalendar modify = new ModifyCalendar();
			modify.modifyCalendar(info, loginUser);
			
			jtm.addCallback(new CalendarCacheManagementCallback(info.getCalendarId()));
			jtm.addCallback(new CalendarChangedNotificationCallback());
			
			jtm.commit();
			
			ret = new SelectCalendar().getCalendar(info.getCalendarId());
		} catch (CalendarNotFound e){
			if (jtm != null){
				jtm.rollback();
			}
			throw new CalendarNotFound(MessageConstant.MESSAGE_CALENDAR_ID_NOT_EXIST.getMessage(info.getCalendarId()), e);
		} catch (InvalidSetting  e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole | InvalidRole e) {
			m_log.warn("modifyCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * カレンダ(基本）情報を 削除します。<BR>
	 *
	 * 引数のIDに対応するカレンダー（基本）情報を削除します。
	 *
	 * @param idList カレンダーID
	 * @return
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public List<CalendarInfo> deleteCalendar(List<String> idList) throws CalendarNotFound, HinemosUnknown, InvalidRole, InvalidSetting {
		JpaTransactionManager jtm = null;
		List<CalendarInfo> ret = new ArrayList<>();
		String calId = null; // 例外メッセージ用
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for(String id : idList) {
				calId = id;
				CalendarValidator.valideDeleteCalendar(id);
				
				SelectCalendar select = new SelectCalendar();
				CalendarInfo info = select.getCalendar(id);
				ret.add(info);

				ModifyCalendar modify = new ModifyCalendar();
				modify.deleteCalendar(id);
			}
			
			for (String id : idList) {
				jtm.addCallback(new CalendarCacheManagementCallback(id));
			}
			jtm.addCallback(new CalendarChangedNotificationCallback());
			
			jtm.commit();

		} catch (CalendarNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new CalendarNotFound(MessageConstant.MESSAGE_CALENDAR_ID_NOT_EXIST.getMessage(calId) , e);
		}catch (InvalidSetting e){
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole | InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("deleteCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * カレンダ[カレンダパターン]情報を登録します。<BR>
	 *
	 * 引数のDTOの内容をマネージャに登録します。<BR>
	 * 
	 * 本メソッドは呼び出し元側にてトランザクション制御されるケースがある。（ImportCalendarPatternControllerから）
	 * 上記の場合に 本メソッド内でSelectCalendar.getCalendarPattern(String)を呼出しても
	 * em.refresh(pattern) の影響でメソッドが正常に動作しないので、本メソッドでは結果エンテイティの返却は行わない。
	 * 
	 * @param info カレンダ[カレンダパターン]情報
	 * @return
	 * @throws CalendarDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public void addCalendarPattern(CalendarPatternInfo info)
			throws CalendarDuplicate, HinemosUnknown, InvalidSetting, InvalidRole {
		JpaTransactionManager jtm = null;
		try{
			jtm = new JpaTransactionManager();

			jtm.begin();

			//入力チェック
			CalendarValidator.validateCalendarPatternInfo(info);
			
			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(info.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

			ModifyCalendar modify = new ModifyCalendar();
			modify.addCalendarPattern(info, loginUser);

			jtm.addCallback(new CalendarPatternCacheManagementCallback(info.getCalPatternId()));
			jtm.addCallback(new CalendarChangedNotificationCallback());
			jtm.commit();
		} catch (CalendarDuplicate e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new CalendarDuplicate(MessageConstant.MESSAGE_CALENDAR_PATTERN_ID_IS_ALREADY_REGISTERED.getMessage(info.getCalPatternId()), e);
		} catch (InvalidSetting e){
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole | InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage(), e);
		} catch(Exception e){
			m_log.warn("addCalendarPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			// EntityManagerクリア
			if (jtm != null)
				jtm.close();
		}
		return;
	}

	/**
	 * カレンダ[カレンダパターン]情報を変更します。<BR>
	 *
	 * 引数のプロパティの内容で更新します。<BR>
	 *
	 * 本メソッドは呼び出し元側にてトランザクション制御されるケースがある。（ImportCalendarPatternControllerから）
	 * 上記の場合に 本メソッド内でSelectCalendar.getCalendarPattern(String)を呼出しても
	 * em.refresh(pattern) の影響でメソッドが正常に動作しないので、本メソッドでは結果エンテイティの返却は行わない。
	 * 
	 * @param info
	 * @return
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public void modifyCalendarPattern(CalendarPatternInfo info)
			throws CalendarNotFound, HinemosUnknown, InvalidSetting, InvalidRole {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			CalendarPatternInfo entity = QueryUtil.getCalPatternInfoPK(info.getCalPatternId(), ObjectPrivilegeMode.NONE);
			jtm.getEntityManager().detach(entity);
			String ownerRoleId = entity.getOwnerRoleId();
			info.setOwnerRoleId(ownerRoleId);
			
			//入力チェック
			CalendarValidator.validateCalendarPatternInfo(info);

			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			ModifyCalendar modify = new ModifyCalendar();
			modify.modifyCalendarPattern(info, loginUser);
			
			jtm.addCallback(new CalendarPatternCacheManagementCallback(info.getCalPatternId()));
			jtm.addCallback(new CalendarChangedNotificationCallback());
			
			jtm.commit();
		} catch (CalendarNotFound e){
			if (jtm != null){
				jtm.rollback();
			}
			throw new CalendarNotFound(MessageConstant.MESSAGE_CALENDAR_PATTERN_ID_NOT_EXIST.getMessage(info.getCalPatternId()), e);
		} catch (InvalidSetting e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole | InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyCalendarPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return;
	}

	/**
	 * カレンダ[カレンダパターン]情報を 削除します。<BR>
	 *
	 * 引数のIDに対応するカレンダ[カレンダパターン]情報を削除します。
	 *
	 * @param idList IDリスト
	 * @return
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<CalendarPatternInfo> deleteCalendarPattern(List<String> idList) throws CalendarNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<CalendarPatternInfo> ret = new ArrayList<CalendarPatternInfo>();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for(String id : idList) {
				CalendarValidator.valideDeleteCalendarPattern(id);

				SelectCalendar select = new SelectCalendar();
				CalendarPatternInfo info = select.getCalendarPattern(id);
				ret.add(info);

				ModifyCalendar modify = new ModifyCalendar();
				modify.deleteCalendarPattern(id);
			}
			
			for (String id : idList) {
				jtm.addCallback(new CalendarPatternCacheManagementCallback(id));
			}
			jtm.addCallback(new CalendarChangedNotificationCallback());
			
			jtm.commit();
		} catch (CalendarNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new CalendarNotFound(MessageConstant.MESSAGE_CALENDAR_PATTERN_ID_NOT_EXIST.getMessage(idList.toArray(new String[idList.size()])), e);
		} catch (ObjectPrivilege_InvalidRole | InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("deleteCalendarPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * 月間カレンダビューに表示する情報
	 * 引数で指定したカレンダーIDに対応するカレンダ詳細情報を取得します。<BR>
	 *
	 * @param id
	 * @param orderNo
	 * @return カレンダ詳細情報
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public Map<Integer, Integer> getCalendarMonth(String id, Integer year, Integer month) throws CalendarNotFound, HinemosUnknown, InvalidRole {
		JpaTransactionManager jtm = null;
		Map<Integer, Integer> ret = null;

		//カレンダ一覧を指定してないときに操作した場合、空のリストを返す
		if (id == null || year <= 0 || month <= 0) {
			return new HashMap<Integer, Integer>();
		}
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCalendar select = new SelectCalendar();
			ret = select.getCalendarMonth(id, year, month);
			jtm.commit();
		} catch (CalendarNotFound e) {
			jtm.rollback();
			throw new CalendarNotFound(MessageConstant.MESSAGE_CALENDAR_ID_NOT_EXIST.getMessage(id), e);
		} catch (ObjectPrivilege_InvalidRole | InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getCalendarMonthInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}
	/**
	 * 週間カレンダビューにて表示する情報
	 * 引数で指定した、カレンダID、年、月、日に対応するカレンダ詳細情報を取得します<BR>
	 * @param id
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public ArrayList<CalendarDetailInfo> getCalendarWeek(String id, Integer year, Integer month, Integer day) throws CalendarNotFound, HinemosUnknown, InvalidRole {
		JpaTransactionManager jtm = null;
		ArrayList<CalendarDetailInfo> ret = new ArrayList<>();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCalendar select = new SelectCalendar();
			Calendar now = Calendar.getInstance((HinemosTime.getTimeZone()));
			now.clear();
			for (int i = 0; i < 7; i++) {
				List<CalendarDetailInfo> list =  select.getCalendarWeek(id, year, month, day);
				if (list != null) {
					for (CalendarDetailInfo info : list) {
						info.setYear(year);
						info.setMonth(month);
						info.setDate(day);
					}
					ret.addAll(list);
				}

				now.set(year, month - 1, day);
				// 月の変わり目判定
				if (day + 1 > now.getActualMaximum(Calendar.DAY_OF_MONTH)) {
					// 年の変わり目判定
					if (month == 12) {
						year++;
						month = 1;
					} else {
						month++;
					}
					day = 1;
				} else {
					day++;
				}
			}
			jtm.commit();
		} catch (CalendarNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new CalendarNotFound(MessageConstant.MESSAGE_CALENDAR_ID_NOT_EXIST.getMessage(id), e);
		} catch (ObjectPrivilege_InvalidRole | InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage(),e);
		} catch (Exception e) {
			m_log.warn("getCalendarDisp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * カレンダが実行可能かチェックします。<BR>
	 *
	 * 引数のカレンダーidが引数のタイムスタンプにおいて実行可能であるかチェックします。
	 *
	 * @param id
	 * @param checkTimestamp
	 * @return カレンダが実行可能か
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public Boolean isRun(String id, Long checkTimestamp) throws CalendarNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		Boolean isRun = false;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCalendar select = new SelectCalendar();
			isRun = select.isRun(id, checkTimestamp);
			jtm.commit();
		} catch (CalendarNotFound | InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("isRun() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return isRun;
	}


}

