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

package com.clustercontrol.monitor.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.commons.session.CheckFacility;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.bean.EventBatchConfirmInfo;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.bean.EventFilterInfo;
import com.clustercontrol.monitor.bean.ScopeDataInfo;
import com.clustercontrol.monitor.bean.StatusDataInfo;
import com.clustercontrol.monitor.bean.StatusFilterInfo;
import com.clustercontrol.monitor.bean.ViewListInfo;
import com.clustercontrol.monitor.factory.DeleteStatus;
import com.clustercontrol.monitor.factory.ManageStatus;
import com.clustercontrol.monitor.factory.ModifyEventCollectGraphFlg;
import com.clustercontrol.monitor.factory.ModifyEventComment;
import com.clustercontrol.monitor.factory.ModifyEventConfirm;
import com.clustercontrol.monitor.factory.SelectEvent;
import com.clustercontrol.monitor.factory.SelectScope;
import com.clustercontrol.monitor.factory.SelectStatus;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.util.MonitorStatusCache;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * 監視管理機能の管理を行う Session Bean です。<BR>
 * クライアントからの Entity Bean へのアクセスは、Session Bean を介して行います。
 * 
 */
public class MonitorControllerBean implements CheckFacility {

	/** ファシリティの配下全てのエントリ。 */
	public static final int ALL = RepositoryControllerBean.ALL;
	/** ファシリティの直下のエントリ。 */
	public static final int ONE_LEVEL = RepositoryControllerBean.ONE_LEVEL;
	/** ファシリティの自エントリ。 */
	public static final int ONLY = -1;

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( MonitorControllerBean.class );


	/**
	 * スコープ情報一覧を取得します。<BR><BR>
	 * 引数で指定されたファシリティの配下全てのファシリティのスコープ情報一覧を返します。<BR>
	 * 各スコープ情報は、ScopeDataInfoのインスタンスとして保持されます。<BR>
	 * 
	 * 
	 * @param facilityId 取得対象の親ファシリティID
	 * @return スコープ情報一覧（ScopeDataInfoが格納されたArrayList）
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.monitor.bean.ScopeDataInfo
	 * @see com.clustercontrol.monitor.factory.SelectScope#getScopeList(String)
	 */
	public ArrayList<ScopeDataInfo> getScopeList(String facilityId, boolean statusFlag, boolean eventFlag, boolean orderFlg)
			throws MonitorNotFound, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;

		//ステータス情報を取得
		SelectScope select = new SelectScope();
		ArrayList<ScopeDataInfo> list;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = select.getScopeList(facilityId, statusFlag, eventFlag, orderFlg);
			jtm.commit();
		} catch (MonitorNotFound | InvalidRole | HinemosUnknown e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getScopeList() : "
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
	 * 引数で指定された条件に一致するステータス情報一覧を取得します。<BR>
	 * 各ステータス情報は、StatusInfoDataのインスタンスとして保持されます。<BR>
	 * 
	 * @param facilityId 取得対象の親ファシリティID
	 * @param property 検索条件
	 * @return ステータス情報一覧（StatusInfoDataが格納されたArrayList）
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.monitor.bean.StatusDataInfo
	 * @see com.clustercontrol.monitor.factory.SelectStatus#getStatusList(String, StatusFilterInfo)
	 */
	public ArrayList<StatusDataInfo> getStatusList(String facilityId, StatusFilterInfo filter)
			throws InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;

		//ステータス情報を取得
		SelectStatus select = new SelectStatus();
		ArrayList<StatusDataInfo> list;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = select.getStatusList(facilityId, filter);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getStatusList() : "
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
	 * 引数で指定されたステータス情報を削除します。<BR>
	 * 
	 * 引数のlistは、StatusDataInfoが格納されたListとして渡されます。<BR>
	 * 
	 * @param list 削除対象のステータス情報一覧（StatusDataInfoが格納されたList）
	 * @return 削除に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @since 2.0.0
	 * 
	 * @see com.clustercontrol.monitor.bean.StatusInfoData
	 * @see com.clustercontrol.monitor.factory.DeleteStatus#delete(List)
	 */
	public boolean deleteStatus(ArrayList<StatusDataInfo> list) throws MonitorNotFound, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;

		//ステータス情報を削除
		DeleteStatus status = new DeleteStatus();

		boolean ret = false;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = status.delete(list);

			jtm.commit();
		}catch (InvalidRole | MonitorNotFound e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("deleteStatus() : "
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
	 * 存続期間を経過したステータス情報を削除 または 更新します。<BR><BR>
	 * 
	 * Quartzから呼び出されるコールバックメソッド<BR>
	 * Quartz以外から呼び出さないでください。
	 * 
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @since 2.0.0
	 *
	 * @see com.clustercontrol.monitor.factory.ManageStatus#execute()
	 * @see #addQuartz(String)
	 * @see #deleteQuartz()
	 */
	public void manageStatus() throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 期限切れのステータス情報を削除/更新する
			ManageStatus manage = new ManageStatus();
			manage.execute();

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e) {
			m_log.warn("manageStatus() : "
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
	 * 引数で指定された条件に一致するイベント一覧情報を取得します。(クライアントview用)<BR><BR>
	 * 
	 * 引数のpropertyには、com.clustercontrol.monitor.factory.EventFilterInfoの属性が１つ以上含まれます。<BR>
	 * 各イベント情報は、EventDataInfoインスタンスとして保持されます。<BR>
	 * 戻り値のViewListInfoは、クライアントにて表示用の形式に変換されます。
	 * 
	 * @param facilityId 取得対象の親ファシリティID
	 * @param filter 検索条件
	 * @param messages 表示イベント数
	 * @return ビュー一覧情報
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.monitor.bean.EventDataInfo
	 * @see com.clustercontrol.monitor.factory.SelectEvent#getEventList(String, EventFilterInfo, int)
	 */
	public ViewListInfo getEventList(String facilityId, EventFilterInfo filter, int messages) throws InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;

		// イベントログ情報を取得
		SelectEvent select = new SelectEvent();
		ViewListInfo list;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = select.getEventList(facilityId, filter, messages);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getEventList() : "
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
	 * 引数で指定された条件に一致する帳票出力用イベント情報一覧を返します。<BR><BR>
	 * 
	 * 引数のpropertyには、com.clustercontrol.monitor.factory.StatusFilterPropertyの属性が
	 * １つ以上含まれます。<BR>
	 * 戻り値のArrayListはArrayListのArrayListであり、内部のArrayListには、
	 * com.clustercontrol.monitor.bean.ReportEventInfoがリストとして格納されます。
	 * 
	 * @param facilityId 取得対象の親ファシリティID
	 * @param property 検索条件
	 * @return 帳票出力用イベント情報一覧（{@link com.clustercontrol.monitor.bean.ReportEventInfo}のリスト）
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @since 2.1.0
	 * 
	 * @see com.clustercontrol.monitor.factory.SelectEvent#getEventListForReport(String, EventFilterInfo)
	 */
	public DataHandler downloadEventFile(String facilityId, EventFilterInfo filter, String filename, Locale locale)
			throws InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;

		// 帳票出力用イベントログ情報を取得
		SelectEvent select = new SelectEvent();
		String username = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		DataHandler handler = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			long now = HinemosTime.currentTimeMillis();
			handler = select.getEventFile(facilityId, filter, filename, username, locale);
			long end = HinemosTime.currentTimeMillis();
			m_log.info("downloadEventFile, time=" + (end - now) + "ms");
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (IOException e) {
			m_log.warn("downloadEventFile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("downloadEventFile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return handler;
	}

	/**
	 * 一時ファイルとして作成したイベントファイルの削除。
	 */
	public void deleteEventFile (String filename) {
		SelectEvent select = new SelectEvent();
		select.deleteEventFile(filename);
	}

	/**
	 * イベント詳細情報を取得します。<BR><BR>
	 * 
	 * @param monitorId 取得対象の監視項目ID
	 * @param pluginId 取得対象のプラグインID
	 * @param facilityId 取得対象のファシリティID
	 * @param outputDate 取得対象の受信日時
	 * @param locale ロケール情報
	 * @return イベント詳細情報
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 */
	public EventDataInfo getEventInfo(
			String monitorId,
			String monitorDetailId,
			String pluginId,
			String facilityId,
			Long outputDate) throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;
		EventDataInfo eventDataInfo = null;

		// イベント情報詳細を取得
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			eventDataInfo = SelectEvent.getEventInfo(monitorId, monitorDetailId, pluginId, facilityId, outputDate);
			jtm.commit();
		} catch (MonitorNotFound | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getEventInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return eventDataInfo;
	}

	/**
	 * 引数で指定されたイベント情報のコメントを更新します。<BR><BR>
	 * コメント追記ユーザとして、コメントユーザを設定します。
	 * 
	 * @param monitorId 更新対象の監視項目ID
	 * @param pluginId 更新対象のプラグインID
	 * @param facilityId 更新対象のファシリティID
	 * @param outputDate 更新対象の受信日時ID
	 * @param confirmDate 確認済み日時（更新値）
	 * @param confirmType 確認タイプ（未／済）（更新値）
	 * @throws HinemosUnknown
	 * @throws EventLogNotFound
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.monitor.factory.ModifyEventComment#modifyComment(String, String, String, Date, String, Date, String)
	 */
	public void modifyComment(
			String monitorId,
			String monitorDetailId,
			String pluginId,
			String facilityId,
			Long outputDate,
			String comment,
			Long commentDate,
			String commentUser
			) throws HinemosUnknown, EventLogNotFound, InvalidSetting, InvalidRole {

		JpaTransactionManager jtm = null;

		// コメントの文字数が2048文字より多い場合は、コメント変更不可
		CommonValidator.validateString(MessageConstant.COMMENT.getMessage(),
				comment, false, 0, 2048);

		commentUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// イベント通知のコメント状態を更新する
		ModifyEventComment modify = new ModifyEventComment();

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			modify.modifyComment(monitorId, monitorDetailId, pluginId, facilityId, outputDate, comment, commentDate, commentUser);

			jtm.commit();
		}catch(EventLogNotFound | InvalidRole e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		}catch(Exception e){
			m_log.warn("modifyComment() : "
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
	 * 引数で指定されたイベント情報の確認を更新します。<BR><BR>
	 * 確認ユーザとして、操作を実施したユーザを設定します。
	 * 
	 * @param monitorId 更新対象の監視項目ID
	 * @param pluginId 更新対象のプラグインID
	 * @param facilityId 更新対象のファシリティID
	 * @param outputDate 更新対象の受信日時ID
	 * @param confirmDate 確認済み日時（更新値）
	 * @param confirmType 確認タイプ（未／済）（更新値）
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.monitor.factory.ModifyEventConfirm#modifyConfirm(String, String, String, Date, Date, int)
	 * 
	 */
	public void modifyConfirm(
			String monitorId,
			String monitorDetailId,
			String pluginId,
			String facilityId,
			int priority,
			Long generateDate,
			Long outputDate,
			Long confirmDate,
			int confirmType
			) throws MonitorNotFound, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;
		String confirmUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// イベントの確認状態を更新する
		ModifyEventConfirm modify = new ModifyEventConfirm();

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			modify.modifyConfirm(monitorId, monitorDetailId, pluginId, facilityId, priority, generateDate, outputDate, confirmDate, confirmType, confirmUser);

			jtm.commit();
		} catch (MonitorNotFound | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyConfirm() : "
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
	 * 引数で指定されたイベント情報一覧の確認を更新します。<BR><BR>
	 * 確認ユーザとして、操作を実施したユーザを設定します。<BR>
	 * 複数のイベント情報を更新します。
	 * 
	 * @param list 更新対象のイベント情報一覧（EventDataInfoが格納されたArrayList）
	 * @param confirmType 確認タイプ（未／済）（更新値）
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.monitor.factory.ModifyEventConfirm#modifyConfirm(List, int)
	 */
	public void modifyConfirm(ArrayList<EventDataInfo> list,
			int confirmType
			) throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;
		String confirmUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// イベントの確認状態を更新する
		ModifyEventConfirm modify = new ModifyEventConfirm();

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			modify.modifyConfirm(list, confirmType, confirmUser);

			jtm.commit();
		} catch(InvalidRole | MonitorNotFound e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyConfirm() : "
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
	 * 引数で指定された条件に一致するイベント情報の確認を一括更新します。<BR><BR>
	 * 確認ユーザとして、操作を実施したユーザを設定します。<BR>
	 * 
	 * @param confirmType 確認タイプ（未／済）（更新値）
	 * @param facilityId 更新対象の親ファシリティID
	 * @param property 更新条件
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.monitor.factory.ModifyEventConfirm#modifyBatchConfirm(int, String, EventBatchConfirmInfo)
	 */
	public void modifyBatchConfirm(int confirmType, String facilityId, EventBatchConfirmInfo info) throws InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;
		String confirmUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// イベントの確認状態を一括更新する
		ModifyEventConfirm modify = new ModifyEventConfirm();

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			modify.modifyBatchConfirm(confirmType, facilityId, info, confirmUser);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		}catch(HinemosUnknown e){
			jtm.rollback();
			throw e;
		}catch(Exception e){
			m_log.warn("modifyBatchConfirm() : "
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
	 * 引数で指定されたイベント情報一覧の性能グラフ用フラグを更新します。<BR><BR>
	 * 複数のイベント情報を更新します。
	 * 
	 * @param list 更新対象のイベント情報一覧（EventDataInfoが格納されたArrayList）
	 * @param collectGraphFlg 性能グラフ用フラグ（ON:true、OFF:false）
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void modifyCollectGraphFlg(ArrayList<EventDataInfo> list,
			Boolean collectGraphFlg) throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;

		// イベントの性能グラフ用フラグを更新する
		ModifyEventCollectGraphFlg modify = new ModifyEventCollectGraphFlg();

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			modify.modifyCollectGraphFlg(list, collectGraphFlg);

			jtm.commit();
		} catch(InvalidRole | MonitorNotFound e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyCollectGraphFlg() : "
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
	 * ファシリティが利用されているか確認する。
	 * 
	 * @throws UsedFacility
	 * @throws InvalidRole
	 */
	@Override
	public void isUseFacilityId(String facilityId) throws UsedFacility {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			List<MonitorInfo> infoCollection
			= QueryUtil.getMonitorInfoByFacilityId_NONE(facilityId);
			if (infoCollection != null && infoCollection.size() > 0) {
				// ID名を取得する
				String listID = (MessageConstant.MONITOR_SETTING.getMessage() + " : ");
				for (MonitorInfo entity : infoCollection) {
					listID += (entity.getMonitorId() + ", ");
				}
				UsedFacility e = new UsedFacility(listID);
				m_log.info("isUseFacilityId() : " + e.getClass().getSimpleName() +
						", " + facilityId + ", " + e.getMessage());
				throw e;
			}
			jtm.commit();
		} catch (UsedFacility e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("isUseFacilityId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	public void persistMonitorStatusCache() {
		MonitorStatusCache.persist();
	}
}
