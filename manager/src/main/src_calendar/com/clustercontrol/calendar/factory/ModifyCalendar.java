/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.calendar.factory;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.calendar.model.CalendarDetailInfo;
import com.clustercontrol.calendar.model.CalendarDetailInfoPK;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.model.CalendarPatternInfo;
import com.clustercontrol.calendar.model.YMD;
import com.clustercontrol.calendar.model.YMDPK;
import com.clustercontrol.calendar.util.QueryUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CalendarDuplicate;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.util.HinemosTime;

/**
 * カレンダ更新を行うファクトリークラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class ModifyCalendar {
	private static Log m_log = LogFactory.getLog(ModifyCalendar.class);

	/**
	 * カレンダ追加
	 * 
	 * @param info
	 * @param userName
	 * @return
	 * @throws HinemosUnknown
	 * @throws CalendarDuplicate
	 * @throws CalendarNotFound
	 */
	public void addCalendar(CalendarInfo info, String userName)
			throws HinemosUnknown, CalendarDuplicate, CalendarNotFound {

		JpaTransactionManager jtm = new JpaTransactionManager();
		HinemosEntityManager em = jtm.getEntityManager();
		//カレンダを作成
		try {
			//現在日時を取得
			Long now = HinemosTime.currentTimeMillis();
			
			// 重複チェック
			jtm.checkEntityExists(CalendarInfo.class, info.getCalendarId());
			
			info.setRegDate(now);
			info.setRegUser(userName);
			info.setUpdateDate(now);
			info.setUpdateUser(userName);
			em.persist(info);
			
			// カレンダ詳細情報登録
			for (int i = 0 ; i < info.getCalendarDetailList().size();  i++ ) {
				CalendarDetailInfo calDetailInfoEntity = info.getCalendarDetailList().get(i);
				calDetailInfoEntity.setCalendarId(info.getCalendarId());
				calDetailInfoEntity.setOrderNo(i + 1);
				em.persist(calDetailInfoEntity);
				calDetailInfoEntity.relateToCalInfoEntity(info);
			}
		} catch (EntityExistsException e) {
			m_log.info("addCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new CalendarDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * カレンダ詳細情報を追加します。<BR>
	 * 
	 * @param calDetailInfoEntity
	 * @param CalendarDetailInfo
	 * @return
	 */
	public void copyProperties(CalendarDetailInfo calDetailInfoEntity, CalendarDetailInfo info) {

		//説明
		calDetailInfoEntity.setDescription(info.getDescription());
		//年
		calDetailInfoEntity.setYear(info.getYear());
		//月
		calDetailInfoEntity.setMonth(info.getMonth());
		//曜日
		calDetailInfoEntity.setDayType(info.getDayType());
		calDetailInfoEntity.setDayOfWeek(info.getDayOfWeek());
		calDetailInfoEntity.setDayOfWeekInMonth(info.getDayOfWeekInMonth());
		calDetailInfoEntity.setDate(info.getDate());
		calDetailInfoEntity.setCalPatternId(info.getCalPatternId());
		calDetailInfoEntity.setAfterday(info.getAfterday());
		//時間
		calDetailInfoEntity.setTimeFrom(info.getTimeFrom());
		calDetailInfoEntity.setTimeTo(info.getTimeTo());
		//振り替える
		calDetailInfoEntity.setSubstituteFlg(info.isSubstituteFlg());
		calDetailInfoEntity.setSubstituteTime(info.getSubstituteTime());
		calDetailInfoEntity.setSubstituteLimit(info.getSubstituteLimit());
		//稼動・非稼動
		calDetailInfoEntity.setOperateFlg(info.isOperateFlg());
	}

	/**
	 * カレンダ[カレンダパターン]情報追加
	 * 
	 * @param info
	 * @param userName
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws CalendarDuplicate
	 * @throws CalendarNotFound
	 */
	public void addCalendarPattern(CalendarPatternInfo info, String userName)
			throws HinemosUnknown, InvalidRole, CalendarDuplicate, CalendarNotFound {

		JpaTransactionManager jtm = new JpaTransactionManager();
		//カレンダパターンを作成
		String id = null;
		try {
			//現在日時を取得
			long now = HinemosTime.currentTimeMillis();
			//ID取得
			id = info.getCalPatternId();
			//名前を取得
			String name = info.getCalPatternName();
			//オーナーロールIDを取得
			String ownerRoleId = info.getOwnerRoleId();
			// 重複チェック
			jtm.checkEntityExists(CalendarPatternInfo.class, id);
			// インスタンス生成
			CalendarPatternInfo entity = new CalendarPatternInfo(id);
			entity.setCalPatternName(name);
			entity.setOwnerRoleId(ownerRoleId);
			entity.setRegDate(now);
			entity.setRegUser(userName);
			entity.setUpdateDate(now);
			entity.setUpdateUser(userName);

			if(info.getYmd() != null){
				int num = 1;
				for(YMD ymd : info.getYmd()){
					m_log.trace("No." + num + " : YMD= " + ymd.yyyyMMdd());
					addCalendarPatternDetail(id, ymd);
					num++;
				}
			}
		} catch (InvalidRole e) {
			throw e;
		} catch (EntityExistsException e) {
			m_log.info("addCalendarPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new CalendarDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addCalendarPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * カレンダ[カレンダパターン]詳細情報追加
	 * @param id
	 * @param ymd
	 * @throws InvalidRole
	 * @throws CalendarDuplicate
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 */
	private void addCalendarPatternDetail(String id, YMD ymd)
			throws InvalidRole, CalendarDuplicate, CalendarNotFound, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();

		try {
			CalendarPatternInfo calPatternEntity = QueryUtil.getCalPatternInfoPK(id);

			//年を取得
			Integer year = ymd.getYear();

			//月を取得
			Integer month = ymd.getMonth();

			//日を取得
			Integer day = ymd.getDay();

			//カレンダパターン詳細情報を作成
			// 主キー作成
			YMDPK entityPk = new YMDPK(id,year,month,day);
			// インスタンス生成
			ymd.setCalPatternId(id);
			jtm.getEntityManager().persist(ymd);
			ymd.relateToCalPatternInfoEntity(calPatternEntity);

			// 重複チェック
			jtm.checkEntityExists(YMD.class, entityPk);
		} catch (InvalidRole e) {
			throw e;
		} catch (CalendarNotFound e) {
			throw e;
		} catch (EntityExistsException e) {
			m_log.info("addCalPatternDetailInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new CalendarDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addCalPatternDetailInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}


	/**
	 * カレンダ情報を変更します。<BR>
	 * @param info
	 * @param userName
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public void modifyCalendar(CalendarInfo info, String userName)
			throws CalendarNotFound, HinemosUnknown, InvalidRole {
		String id = null;
		try {
			//現在日時を取得
			Long now = HinemosTime.currentTimeMillis();
			//ID取得
			id = info.getCalendarId();
			//名前を取得
			String name = info.getCalendarName();
			//説明を取得
			String description = info.getDescription();
			//オーナーロールを取得
			String ownerRoleId = info.getOwnerRoleId();
			//カレンダを作成
			CalendarInfo entity = QueryUtil.getCalInfoPK(id, ObjectPrivilegeMode.MODIFY);
			entity.setCalendarName(name);
			entity.setDescription(description);
			/* 作成時刻、作成ユーザは変更時に更新されるはずがないので、再度登録しない
				entity.setRegDate(now);
				entity.setRegUser(userName);
			 */
			entity.setOwnerRoleId(ownerRoleId);
			entity.setUpdateDate(now);
			entity.setUpdateUser(userName);
			entity.setValidTimeFrom(info.getValidTimeFrom());
			entity.setValidTimeTo(info.getValidTimeTo());

			// カレンダ詳細情報登録
			if (info.getCalendarDetailList() != null) {
				CalendarDetailInfo calDetailInfoEntity = null;
				List<CalendarDetailInfoPK> calDetailInfoEntityPkList
				= new ArrayList<CalendarDetailInfoPK>();
				
				HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
				for (int i = 0 ; i < info.getCalendarDetailList().size();  i++ ) {
					try {
						calDetailInfoEntity = QueryUtil.getCalDetailInfoPK(id, i + 1);
					} catch (CalendarNotFound e) {
						calDetailInfoEntity = new CalendarDetailInfo(entity.getCalendarId(), i + 1);
						em.persist(calDetailInfoEntity);
						calDetailInfoEntity.relateToCalInfoEntity(entity);
					}
					calDetailInfoEntityPkList.add(new CalendarDetailInfoPK(id, i + 1));
					copyProperties(calDetailInfoEntity, info.getCalendarDetailList().get(i));
				}
				// 不要なCalDetailInfoEntityを削除
				entity.deleteCalDetailInfoEntities(calDetailInfoEntityPkList);
			}
		} catch (CalendarNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
	/**
	 * カレンダ[カレンダパターン]情報を変更します。<BR>
	 * @param info
	 * @param userName
	 * @throws HinemosUnknown
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public void modifyCalendarPattern(CalendarPatternInfo info, String userName)
			throws HinemosUnknown, CalendarNotFound, InvalidRole {
		String id = null;
		try {
			//現在日時を取得
			long now = HinemosTime.currentTimeMillis();
			//ID取得
			id = info.getCalPatternId();
			//名前を取得
			String name = info.getCalPatternName();
			//オーナーロールIDを取得
			String ownerRoleId = info.getOwnerRoleId();
			//カレンダを作成
			CalendarPatternInfo entity = QueryUtil.getCalPatternInfoPK(id, ObjectPrivilegeMode.MODIFY);
			entity.setCalPatternId(id);
			entity.setCalPatternName(name);
			entity.setOwnerRoleId(ownerRoleId);
			/* 作成時刻、作成ユーザは変更時に更新されるはずがないので、再度登録しない
				entity.setRegDate(now);
				entity.setRegUser(userName);
			 */
			entity.setUpdateDate(now);
			entity.setUpdateUser(userName);

			// カレンダパターン詳細情報登録
			//TODO: 実装を見直す
			if (info.getYmd() != null) {
				List<YMDPK> calPatternDetailInfoEntityPkList
				= new ArrayList<YMDPK>();
				
				JpaTransactionManager jtm = new JpaTransactionManager();
				for (YMD ymd : info.getYmd()) {
					try {
						QueryUtil.getCalPatternDetailInfoPK(id, ymd.getYear(), ymd.getMonth(), ymd.getDay());
					} catch (CalendarNotFound e) {
						YMD y = new YMD(id, ymd.getYear(), ymd.getMonth(), ymd.getDay());
						jtm.getEntityManager().persist(y);
						y.relateToCalPatternInfoEntity(entity);
					}
					calPatternDetailInfoEntityPkList.add(new YMDPK(id, ymd.getYear(), ymd.getMonth(), ymd.getDay()));
				}
				// 不要なCalDetailInfoEntityを削除
				entity.deleteCalPatternDetailInfoEntities(calPatternDetailInfoEntityPkList);
			}
		} catch (CalendarNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("modifyCalendarPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyCalendarPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * カレンダ（基本）情報を削除します。<BR>
	 * 
	 * @param id
	 * @return
	 * @throws HinemosUnknown
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public void deleteCalendar(String id) throws HinemosUnknown, CalendarNotFound, InvalidRole {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		try {
			//カレンダ情報を検索し取得
			CalendarInfo cal = QueryUtil.getCalInfoPK(id, ObjectPrivilegeMode.MODIFY);

			//カレンダ情報を削除
			em.remove(cal);

		} catch (CalendarNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * カレンダ[カレンダパターン]情報を削除します。<BR>
	 * 
	 * @param id
	 * @return
	 * @throws HinemosUnknown
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public void deleteCalendarPattern(String id) throws HinemosUnknown, CalendarNotFound, InvalidRole {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		m_log.info("deleteCalendarPattern : deleted " + id);
		try {
			//カレンダ[カレンダパターン]情報を検索し取得
			CalendarPatternInfo calPa = QueryUtil.getCalPatternInfoPK(id, ObjectPrivilegeMode.MODIFY);
			//カレンダ[カレンダパターン]情報を削除
			em.remove(calPa);
		} catch (CalendarNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteCalendarPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
}
