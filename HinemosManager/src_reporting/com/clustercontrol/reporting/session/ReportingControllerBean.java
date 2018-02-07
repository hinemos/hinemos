/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.session;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.util.OptionManager;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.reporting.bean.ReportingTypeConstant;
import com.clustercontrol.reporting.bean.ReportingInfo;
import com.clustercontrol.reporting.bean.TemplateSetDetailInfo;
import com.clustercontrol.reporting.bean.TemplateSetInfo;
import com.clustercontrol.reporting.factory.AddReporting;
import com.clustercontrol.reporting.factory.AddTemplateSet;
import com.clustercontrol.reporting.factory.DeleteReporting;
import com.clustercontrol.reporting.factory.DeleteTemplateSet;
import com.clustercontrol.reporting.factory.ModifyReporting;
import com.clustercontrol.reporting.factory.ModifySchedule;
import com.clustercontrol.reporting.factory.ModifyTemplateSet;
import com.clustercontrol.reporting.factory.OperationReporting;
import com.clustercontrol.reporting.factory.SelectReportingInfo;
import com.clustercontrol.reporting.factory.SelectTemplateSetInfo;
import com.clustercontrol.reporting.fault.ReportingDuplicate;
import com.clustercontrol.reporting.fault.ReportingNotFound;
import com.clustercontrol.reporting.util.ReportingValidator;
import com.clustercontrol.util.Messages;

/**
 * 
 * レポーティング機能を管理する Session Bean です。<BR>
 * 
 */
public class ReportingControllerBean {

	private static Log m_log = LogFactory.getLog( ReportingControllerBean.class );

	/**
	 * レポーティング情報を追加します。
	 * 
	 * @throws HinemosUnknown
	 * @throws ReportingDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public boolean addReporting(ReportingInfo reportingInfo)
			throws HinemosUnknown, ReportingDuplicate, InvalidSetting, InvalidRole {

		boolean retFlg;
		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// メイン処理
		try {
			m_log.debug("addReporting : reportingInfo.reportId=" + reportingInfo.getReportScheduleId());
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 入力チェック
			ReportingValidator.validateReportingInfo(reportingInfo);
			
			// TODO 6.1ではマネージャのRoleValidator#validateUserBelongRoleを使うように修正すること
			//ユーザがオーナーロールIDに所属しているかチェック
			validateUserBelongRole(reportingInfo.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));


			// レポーティング情報を登録
			AddReporting add = new AddReporting();
			retFlg = add.addReporting(reportingInfo, loginUser);

			// Quartzへ登録
			ModifySchedule modify = new ModifySchedule();
			modify.addSchedule(reportingInfo, loginUser);

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyRelationCache.refresh();
		} catch (HinemosUnknown | InvalidSetting | InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (EntityExistsException e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new ReportingDuplicate(e.getMessage(), e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addReporting() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		
		return retFlg;
	}

	/**
	 * レポーティング情報を変更します。
	 * 
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws ReportingNotFound
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public boolean modifyReporting(ReportingInfo reportingInfo) throws HinemosUnknown, NotifyNotFound, ReportingNotFound, InvalidSetting, InvalidRole {

		boolean retFlg;
		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// メイン処理
		try {
			m_log.debug("modifyReporting : reportingInfo.reportId=" + reportingInfo.getReportScheduleId());
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 入力チェック
			ReportingValidator.validateReportingInfo(reportingInfo);

			// テンプレートセット情報を登録
			ModifyReporting modify = new ModifyReporting();
			retFlg = modify.modifyReporting(reportingInfo, loginUser);

			// Quartzへ登録
			ModifySchedule modifySchedule = new ModifySchedule();
			modifySchedule.addSchedule(reportingInfo, loginUser);

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyRelationCache.refresh();
		} catch (InvalidSetting | ReportingNotFound | NotifyNotFound | InvalidRole | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyReporting() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		
		return retFlg;
	}

	/**
	 * レポーティング情報を削除します。
	 * @throws HinemosUnknown
	 * @throws ReportingNotFound
	 * @throws InvalidRole
	 * 
	 */
	public boolean deleteReporting(String reportId) throws HinemosUnknown, ReportingNotFound, InvalidRole {
		m_log.debug("deleteReporting() : reportId=" + reportId);

		boolean retFlg;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// メンテナンス情報を削除
			DeleteReporting delete = new DeleteReporting();
			retFlg = delete.deleteReporting(reportId);

			ModifySchedule modify = new ModifySchedule();
			modify.deleteSchedule(reportId);

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyRelationCache.refresh();

		} catch (ReportingNotFound | InvalidRole | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("deleteReporting() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		
		return retFlg;
	}


	/**
	 * レポーティング情報を取得します。
	 *
	 * @return
	 * @throws ReportingNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 */
	public ReportingInfo getReportingInfo(String reportId) throws ReportingNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getReportingInfo() : reportId=" + reportId);

		JpaTransactionManager jtm = null;
		SelectReportingInfo select = new SelectReportingInfo();
		ReportingInfo info;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			info = select.getReportingInfo(reportId);
			jtm.commit();
		} catch (ReportingNotFound | InvalidRole | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getReportingInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return info;
	}

	/**
	 * レポーティング情報の一覧を取得します。<BR>
	 * 
	 * @return レポーティング情報の一覧を保持するArrayList
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 */
	public ArrayList<ReportingInfo> getReportingList() throws InvalidRole, HinemosUnknown {
		m_log.debug("getReportingList()");

		JpaTransactionManager jtm = null;
		SelectReportingInfo select = new SelectReportingInfo();
		ArrayList<ReportingInfo> list;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = select.getReportingList();
			jtm.commit();
		} catch (HinemosUnknown | InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getReportingList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}

		return list;

	}

	/**
	 * 
	 * レポーティングの有効、無効を変更するメソッドです。
	 * @throws HinemosUnknown
	 * @throws ReportingNotFound
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 */
	public void setReportingStatus(String reportId, boolean validFlag) throws NotifyNotFound, ReportingNotFound, InvalidRole, HinemosUnknown{
		m_log.debug("setReportingStatus() : reportId=" + reportId + ", validFlag=" + validFlag);
		// null check
		if(reportId == null || "".equals(reportId)){
			HinemosUnknown e = new HinemosUnknown("target reportId is null or empty.");
			m_log.info("setReportingStatus() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		ReportingInfo info = this.getReportingInfo(reportId);
		info.setValidFlg(validFlag);

		try{
			this.modifyReporting(info);
		} catch (InvalidSetting  e) {
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * レポーティング機能をスケジュール実行します。<BR>
	 * Quartzからスケジュール実行時に呼び出されます。
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * トランザクション開始はユーザが制御する。
	 * また、追加実装により、トランザクションの入れ子が予期せず生じることを避けるため、Neverを採用する。
	 */
	public void scheduleRunReporting(String reportId, String calendarId) throws InvalidRole, HinemosUnknown {
		m_log.debug("scheduleRunReporting() : reportId=" + reportId + ", calendarId=" + calendarId);
		if (OptionManager.checkEnterprise()) {
			m_log.warn("ReportingPlugin is not activated. Skip running reporting.");
			return;
		}

		JpaTransactionManager jtm = null;
		try {
			// トランザクションがすでに開始されている場合は処理終了
			jtm = new JpaTransactionManager();
			jtm.begin(true);

			//カレンダをチェック
			if(calendarId != null && calendarId.length() > 0){
				//カレンダによる実行可/不可のチェック
				if(!(new CalendarControllerBean().isRun(calendarId, new Date().getTime()).booleanValue())){
					return;
				}
			}

			//レポーティング実行
			runReporting(reportId);

			jtm.commit();
		} catch (CalendarNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(),e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch(Exception e){
			m_log.warn("scheduleRunReporting() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * 
	 * レポート機能を実行するメソッドです。
	 * 
	 * @param reportId
	 * @return 作成されるレポートファイル名のリスト
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<String> runReporting(String reportId) throws InvalidRole, HinemosUnknown {
		return runReporting(reportId, null);
	}

	/**
	 * 
	 * レポート機能を実行するメソッドです。追加で即時実行時のパラメータを指定します。
	 * 
	 * @param reportId
	 * @return 作成されるレポートファイル名のリスト
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<String> runReporting(String reportId, ReportingInfo tmpReportingInfo) throws InvalidRole, HinemosUnknown {
		m_log.debug("runReporting() : reportId=" + reportId);

		JpaTransactionManager jtm = null;
		OperationReporting operation = new OperationReporting();

		List<String> ret = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = operation.runReporting(reportId, tmpReportingInfo);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("runReporting() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}

		return ret;
	}
	
	/**
	 * テンプレートセット情報を追加します
	 * 
	 * 
	 * @param templateSetInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws ReportingDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public boolean addTemplateSet(TemplateSetInfo templateSetInfo)
			throws HinemosUnknown, ReportingDuplicate, InvalidSetting, InvalidRole {

		boolean retFlg;
		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// メイン処理
		try {
			m_log.debug("addTemplateSet : templateSetInfo.templateSetId=" + templateSetInfo.getTemplateSetId());
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 入力チェック
			ReportingValidator.validateTemplateSetInfo(templateSetInfo);
			
			// TODO 6.1ではマネージャのRoleValidator#validateUserBelongRoleを使うように修正すること
			//ユーザがオーナーロールIDに所属しているかチェック
			validateUserBelongRole(templateSetInfo.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			// テンプレートセット情報を登録
			AddTemplateSet add = new AddTemplateSet();
			retFlg = add.addTemplateSet(templateSetInfo, loginUser);

			jtm.commit();

		} catch (HinemosUnknown | InvalidSetting | InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (EntityExistsException e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new ReportingDuplicate(e.getMessage(), e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addTemplateSet() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		
		return retFlg;
	}

	/**
	 * テンプレートセット情報を変更します。
	 * 
	 * @param templateSetInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws ReportingNotFound
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public boolean modifyTemplateSet(TemplateSetInfo templateSetInfo) throws HinemosUnknown, NotifyNotFound, ReportingNotFound, InvalidSetting, InvalidRole {

		boolean retFlg;
		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// メイン処理
		try {
			m_log.debug("modifyTemplateSet : templateSetInfo.templateSetId=" + templateSetInfo.getTemplateSetId());
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 入力チェック
			ReportingValidator.validateTemplateSetInfo(templateSetInfo);

			// テンプレートセット情報を登録
			ModifyTemplateSet modify = new ModifyTemplateSet();
			retFlg = modify.modifyTemplateSet(templateSetInfo, loginUser);

			jtm.commit();

		} catch (InvalidSetting | ReportingNotFound | NotifyNotFound | InvalidRole | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyTemplateSet() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		
		return retFlg;
	}

	public boolean deleteTemplateSet(String templateSetId) throws HinemosUnknown, ReportingNotFound, InvalidRole {
		m_log.debug("deleteTemplateSet() : templateSetId=" + templateSetId);

		boolean retFlg;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// テンプレートセット情報を削除
			DeleteTemplateSet delete = new DeleteTemplateSet();
			retFlg = delete.deleteTemplateSet(templateSetId);

			jtm.commit();

		} catch (ReportingNotFound | InvalidRole | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("deleteTemplateSet() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		
		return retFlg;
	}
	
	
	/**
	 * テンプレートセットIDを指定し、テンプレートセット情報を取得します。
	 * 
	 * @param templateSetId
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public TemplateSetInfo getTemplateSetInfo(String templateSetId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		TemplateSetInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// テンプレートセット一覧を取得
			SelectTemplateSetInfo select = new SelectTemplateSetInfo();
			info = select.getTemplateSetInfo(templateSetId);
			
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getTemplateSetInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return info;
	}
	
	
	/**
	 * オーナーロールIDを指定してテンプレートセット情報一覧を取得します。<BR>
	 * 
	 * @param ownerRoleId オーナーロールID
	 * @return テンプレートセット情報一覧
	 * @thorws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.reporting.factory.SelectReportingTemplateSet#getReportingTemplateSetList()
	 */
	public ArrayList<TemplateSetInfo> getTemplateSetListByOwnerRole(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<TemplateSetInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// テンプレートセット一覧を取得
			SelectTemplateSetInfo select = new SelectTemplateSetInfo();
			list = select.getAllTemplateSetList(ownerRoleId);
			
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getTemplateSetListByOwnerRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return list;
	}
	
	/**
	 * テンプレートセットIDを指定し、テンプレートセット詳細情報のリストを取得する
	 * 
	 * @param templateSetId
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<TemplateSetDetailInfo> getTemplateSetDetailInfoList(String templateSetId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<TemplateSetDetailInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// テンプレートセット一覧を取得
			SelectTemplateSetInfo select = new SelectTemplateSetInfo();
			list = select.getTemplateSetDetailList(templateSetId);
			
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getTemplateSetListByOwnerRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return list;
	}
	
	/**
	 * テンプレート配置ディレクトリからテンプレートの一覧を取得し、返す
	 * 
	 * @param ownerRoleId
	 * @return
	 */
	public ArrayList<String> getTemplateIdList(String ownerRoleId) {
		ArrayList<String> list = new ArrayList<String>();
		
		String templateDirPath = System.getProperty("hinemos.manager.etc.dir") + File.separator  + "reporting" + File.separator  + "template";
		File[] templates = new File(templateDirPath).listFiles();

		if (templates != null) {
			for(File template : templates) {
				if(template.isDirectory()) {
					list.add(template.getName());
					
					m_log.debug("template = " + template.getName());
				}
			}
			Collections.sort(list);
		}
		
		return list;
	}
	
	/**
	 * 出力形式を返す
	 * 
	 * @return
	 */
	public ArrayList<String> getReportOutputTypeStrList() {
		return ReportingTypeConstant.getTypeStrList();
	}
	
	/**
	 * 出力形式の文字列を数値に変更する
	 * 
	 * @return
	 */
	public int outputStringToType(String str) {
		return ReportingTypeConstant.outputStringToType(str);
	}
	
	/**
	 * 出力形式の数値を文字列に変更する
	 * 
	 * @return
	 */
	public String outputTypeToString(int type) {
		return ReportingTypeConstant.outputTypeToString(type);
	}
	
	// TODO 6.1ではマネージャのRoleValidator#validateUserBelongRoleを使うようにするため削除する。properties ファイルからkeyも削除すること。
	private void validateUserBelongRole(String role, String user, boolean isAdmin) throws InvalidSetting{

		if(!isAdmin && !UserRoleCache.getRoleIdList(user).contains(role)) {
			String args[] = {user, role};
			throw new InvalidSetting(Messages.getString("MESSAGE_REPORTING_99", args));
		}
	}
}
