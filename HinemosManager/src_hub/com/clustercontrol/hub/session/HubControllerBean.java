/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.LogFormatDuplicate;
import com.clustercontrol.fault.LogFormatKeyPatternDuplicate;
import com.clustercontrol.fault.LogFormatNotFound;
import com.clustercontrol.fault.LogFormatUsed;
import com.clustercontrol.fault.LogTransferDuplicate;
import com.clustercontrol.fault.LogTransferNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.hub.bean.PropertyConstants;
import com.clustercontrol.hub.bean.StringData;
import com.clustercontrol.hub.bean.StringQueryInfo;
import com.clustercontrol.hub.bean.StringQueryResult;
import com.clustercontrol.hub.bean.Tag;
import com.clustercontrol.hub.bean.TransferInfoDestTypeMst;
import com.clustercontrol.hub.factory.ModifyLogFormat;
import com.clustercontrol.hub.factory.ModifySchedule;
import com.clustercontrol.hub.factory.ModifyTransfer;
import com.clustercontrol.hub.factory.SelectLogFormat;
import com.clustercontrol.hub.factory.SelectTransfer;
import com.clustercontrol.hub.model.CollectDataTag;
import com.clustercontrol.hub.model.CollectStringData;
import com.clustercontrol.hub.model.CollectStringKeyInfo;
import com.clustercontrol.hub.model.LogFormat;
import com.clustercontrol.hub.model.TransferDestProp;
import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.hub.model.TransferInfoPosition;
import com.clustercontrol.hub.session.Transfer.TrasferCallback;
import com.clustercontrol.hub.session.TransferFactory.Property;
import com.clustercontrol.hub.util.HubValidator;
import com.clustercontrol.hub.util.QueryUtil;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.platform.QueryPertial;
import com.clustercontrol.platform.hub.HubControllerUtil;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.StringBinder;

/**
 * 収集蓄積機能の制御クラス
 *
 */
public class HubControllerBean {

	// Logger
	static Logger logger = Logger.getLogger( HubControllerBean.class );
	
	/*
	 * ロードした転送用プラグインを保存。
	 */
	private static final Map<String, TransferFactory> factories = new HashMap<>();
	
	/*
	 * 転送処理用のロックが必要。
	 */
	private static final Set<String> proccesing = new HashSet<>();
	
	/*
	 * 転送処理用のロックが必要。
	 */
	private static final long saveForCount = 10000;
	
	/*
	 * ログ検索　否定演算子
	 */
	private static final char NEGATION_CHAR = '-';
	
	/*
	 * エスケープ文字
	 */
	private static final char ESCAPE_CHAR = '\\';

	/*
	 * クォート用文字
	 */
	private static final char QUATATION_CHAR = '"';

	/*
	 * キー指定のセパレータ
	 */
	private static final char SEPARATE_CHAR = '=';

	/*
	 * 転送用プラグインのプロパティを変数バインドするクラス
	 * 
	 */
	private static class Binder implements PropertyBinder {
		private static final String var_year = "YEAR";
		private static final String var_month = "MONTH";
		private static final String var_day = "DAY";
		
		private static final String var_monitor_id = "MONITOR_ID";
		private static final String var_facility_id = "FACILITY_ID";
		private static final String var_plugin_id = "PLUGIN_ID";
		private static final String var_log_format_id = "LOG_FORMAT_ID";
		private static final String var_session_id = "SESSION_ID";
		private static final String var_jobunit_id = "JOBUNIT_ID";
		private static final String var_job_id = "JOB_ID";
		
		private String year;
		private String month;
		private String day;
		
		public Binder() {
			// 年月日
			Calendar cal = HinemosTime.getCalendarInstance();
			year = Integer.toString(cal.get(Calendar.YEAR));
			month = Integer.toString(cal.get(Calendar.MONTH) + 1);
			day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
		}
		
		private StringBinder createBinder(Map<String, String> bindStrings) {
			Map<String, String> map = new HashMap<>(bindStrings);
			
			map.put(var_year, year);
			map.put(var_month, month);
			map.put(var_day, day);
			
			return new StringBinder(map);
		}
		
		@Override
		public String bind(EventLogEntity event, String param) throws TransferException {
			Map<String, String> map = new HashMap<>();
			
			map.put(var_monitor_id, event.getId().getMonitorId());
			map.put(var_plugin_id, event.getId().getPluginId());
			map.put(var_facility_id, event.getId().getFacilityId());
			
			String result = createBinder(map).bindParam(param);
			
			return result;
		}
		@Override
		public Map<String, String> bind(EventLogEntity event, Map<String, String> params) throws TransferException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String bind(JobSessionEntity session, String param) throws TransferException {
			Map<String, String> map = new HashMap<>();
			
			map.put(var_session_id, session.getSessionId());
			map.put(var_jobunit_id, session.getJobunitId());
			map.put(var_job_id, session.getJobId());
			
			return createBinder(map).bindParam(param);
		}
		@Override
		public Map<String, String> bind(JobSessionEntity job, Map<String, String> params) throws TransferException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String bind(CollectStringKeyInfo key, CollectStringData string, String param) throws TransferException {
			Map<String, String> map = new HashMap<>();
			
			map.put(var_monitor_id, key.getId().getMonitorId());
			map.put(var_facility_id, key.getId().getFacilityId());
			map.put(var_log_format_id, string.getLogformatId());
			
			return createBinder(map).bindParam(param);
		}
		@Override
		public Map<String, String> bind(CollectStringKeyInfo key, CollectStringData string, Map<String, String> params) throws TransferException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String bind(CollectKeyInfo key, CollectData numeric, String param) throws TransferException {
			Map<String, String> map = new HashMap<>();
			
			map.put(var_monitor_id, key.getId().getMonitorId());
			map.put(var_facility_id, key.getId().getFacilityid());
			
			return createBinder(map).bindParam(param);
		}
		@Override
		public Map<String, String> bind(CollectKeyInfo key, CollectData numeric, Map<String, String> params) throws TransferException {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * TransferFactory を継承したクラスをロードする。
	 * 
	 */
	public static void init() {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// 転送用プラグインをロード
			ServiceLoader<TransferFactory> serviceLoader = ServiceLoader.load(TransferFactory.class);
			Iterator<TransferFactory> itr = serviceLoader.iterator();
			while (itr.hasNext()) {
				TransferFactory factory = itr.next();
				factories.put(factory.getDestTypeId(), factory);
				logger.info(String.format("init() : Initialize TransferFactory(%s)", factory.getDestTypeId()));
			}
			
			// 転送用プラグインのパラメータを DB と照合
			List<TransferInfo> infos = new ArrayList<>(QueryUtil.getTransferInfoList());
			
			EntityManager em = jtm.getEntityManager();
			for (TransferFactory factory: factories.values()) {
				Iterator<TransferInfo> tiIter = infos.iterator();
				while (tiIter.hasNext()) {
					TransferInfo mst = tiIter.next();
					if (mst.getDestTypeId().equals(factory.getDestTypeId())) {
						List<TransferDestProp> props = new ArrayList<TransferDestProp>(mst.getDestProps());
						for (Property p: factory.properties()) {
							TransferDestProp matchedProp = null;
							Iterator<TransferDestProp> iter = props.iterator();
							while (iter.hasNext()) {
								TransferDestProp prop = iter.next();
								if (prop.getName().equals(p.name)) {
									iter.remove();
									
									matchedProp = prop;
									break;
								}
							}
							if (matchedProp == null) {
								TransferDestProp prop = new TransferDestProp();
								prop.setName(p.name);
								prop.setValue(p.defaultValue.toString());
								
								mst.getDestProps().add(prop);
							}
						}
						
						for (TransferDestProp p: props) {
							mst.getDestProps().remove(p);
						}
						tiIter.remove();
					}
				}
			}
			
			// プラグインが存在しない転送設定は削除
			for (TransferInfo mst: infos) {
				em.remove(mst);
			}
			
			jtm.commit();
		} catch (Exception e) {
			logger.warn("init() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw e;
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}
	
	/**
	 * 指定したログフォーマットIDのログフォーマット情報を取得します。
	 * 
	 * @param id
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public LogFormat getLogFormat(String id) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		LogFormat format = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			format = new SelectLogFormat().getLogFormat(id);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			logger.warn("getLogFormat() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return format;
	}

	/**
	 *  ログフォーマット情報のID一覧を取得します。
	 *  
	 * @return
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<String> getLogFormatIdList(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = new SelectLogFormat().getLogFormatIdList(ownerRoleId);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			logger.warn("getLogFormatList() : "
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
	 *  ログフォーマット情報の一覧を取得します。
	 *  
	 * @return
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<LogFormat> getLogFormatList() throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<LogFormat> list = new SelectLogFormat().getLogFormatList();
			jtm.commit();
			return list;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			logger.warn("getLogFormatList() : "
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
	 * オーナーロールIDを条件として、ログフォーマット情報の一覧を取得します。
	 * 
	 * @param ownerRoleId
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<LogFormat> getLogFormatListByOwnerRole(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<LogFormat> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = new SelectLogFormat().getLogFormatListByOwnerRole(ownerRoleId);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			logger.warn("getLogFormatListByOwnerRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * ログフォーマット情報を追加します。<BR>
	 * 
	 * @param format
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws  
	 * @throws InvalidSetting 
	 */
	public void addLogFormat(LogFormat format) throws LogFormatDuplicate, LogFormatKeyPatternDuplicate, InvalidRole, HinemosUnknown, InvalidSetting {
		JpaTransactionManager jtm = null;
		try{
			jtm = new JpaTransactionManager();

			jtm.begin();

			//入力チェック
			HubValidator.validateLogFormat(format);

			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			new ModifyLogFormat().add(format, loginUser);

			jtm.commit();

		} catch (LogFormatDuplicate | LogFormatKeyPatternDuplicate | HinemosUnknown | InvalidSetting e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch(RuntimeException e){
			logger.warn("addLogFormat() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			// EntityManagerクリア
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * ログ[フォーマット]情報を変更します。<BR>
	 * 
	 * @param format
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws LogFormatDuplicate 
	 * @throws InvalidSetting 
	 */
	public void modifyLogFormat(LogFormat format) throws LogFormatNotFound, LogFormatKeyPatternDuplicate, InvalidRole, HinemosUnknown, InvalidSetting {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			HubValidator.validateLogFormat(format);

			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			new ModifyLogFormat().modify(format, loginUser);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (RuntimeException e) {
			logger.warn("modifyLogFormat() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * ログフォーマット情報を削除します。<BR>
	 * 
	 * @param delFormatId
	 * @throws LogNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidSetting 
	 * @throws LogformatUsed 
	 * @throws  
	 */
	public void deleteLogFormat(List<String> delFormatId) throws LogFormatNotFound, LogFormatUsed, HinemosUnknown, InvalidRole {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for(String id : delFormatId) {
				HubValidator.validateDeleteLogFormat(id);
				new ModifyLogFormat().delete(id);
			}
			jtm.commit();
		} catch (LogFormatNotFound | LogFormatUsed | HinemosUnknown | InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (RuntimeException e) {
			logger.warn("deleteLogFormat() : "
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
	 * 指定した受け渡し設定IDの受け渡し設定情報を取得します。
	 * 
	 * @param id
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public TransferInfo getTransferInfo(String id) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		TransferInfo export = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			export = new SelectTransfer().getTransferInfo(id);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			logger.warn("getLogTransfer() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return export;
	}
	/**
	 *  受け渡し設定情報のID一覧を取得します。
	 *  
	 * @return
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<String> getTransferInfoIdList(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = new SelectTransfer().getTransferIdList(ownerRoleId);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			logger.warn("getLogTransferIdList() : "
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
	 *  受け渡し設定情報の一覧を取得します。
	 *  
	 * @return
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<TransferInfo> getTransferInfoList() throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<TransferInfo> list = new SelectTransfer().getTransferInfoList();
			jtm.commit();
			return list;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			logger.warn("getLogTransferList() : "
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
	 * オーナーロールIDを条件として、受け渡し設定情報の一覧を取得します。
	 * 
	 * @param ownerRoleId
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<TransferInfo> getTransferInfoListByOwnerRole(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<TransferInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = new SelectTransfer().getTransferListByOwnerRole(ownerRoleId);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			logger.warn("getLogTransferListByOwnerRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}
	
	
	/**
	 * 転送設定情報を追加します。<BR>
	 * 
	 * @param transferInfo
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws  
	 * @throws InvalidSetting 
	 */
	public void addTransferInfo(TransferInfo transferInfo) throws LogTransferDuplicate, InvalidRole, HinemosUnknown, InvalidSetting {
		JpaTransactionManager jtm = null;
		try{
			jtm = new JpaTransactionManager();

			jtm.begin();

			//入力チェック
			HubValidator.validateTransferInfo(transferInfo);

			// 情報登録
			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			new ModifyTransfer().add(transferInfo, loginUser);

			// スケジュール登録
			new ModifySchedule().updateSchedule(transferInfo, loginUser);
			
			jtm.commit();
			
		} catch (LogTransferDuplicate | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch(RuntimeException e){
			logger.warn("addLogTransfer() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			// EntityManagerクリア
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * 転送設定情報を変更します。<BR>
	 * 
	 * @param transferInfo
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws LogFormatDuplicate 
	 * @throws InvalidSetting 
	 */
	public void modifyTransferInfo(TransferInfo transferInfo) throws LogTransferNotFound, InvalidRole, HinemosUnknown, InvalidSetting {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			HubValidator.validateTransferInfo(transferInfo);

			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			new ModifyTransfer().modify(transferInfo, loginUser);
			
			new ModifySchedule().updateSchedule(transferInfo, loginUser);
			
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (RuntimeException e) {
			logger.warn("modifyLogTransfer() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * 転送設定情報を削除します。<BR>
	 * 
	 * @param delFormatId
	 * @throws LogNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidSetting 
	 * @throws LogformatUsed 
	 * @throws  
	 */
	public void deleteTransferInfo(List<String> delTransferId) throws LogTransferNotFound, HinemosUnknown, InvalidRole {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for(String id : delTransferId) {
				new ModifyTransfer().delete(id);
				// スケジューラから転送設定を削除
				new ModifySchedule().deleteSchedule(id);
			}
			jtm.commit();
		} catch (LogTransferNotFound | HinemosUnknown | InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (RuntimeException e) {
			logger.warn("deleteLogTransfer() : "
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
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<TransferInfoDestTypeMst> getTransferInfoDestTypeMstList() throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<TransferInfoDestTypeMst> list = new SelectTransfer().getTransferInfoDestTypeMstList();
			jtm.commit();
			return list;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			logger.warn("getLogTransferDestTypeMstList() : "
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
	 * 転送先プラグインを取得。
	 * 
	 * @param destTypeId
	 * @return
	 */
	public static TransferFactory getTransferFactory(String destTypeId) {
		return factories.get(destTypeId);
	}
	
	/**
	 * 転送先プラグインを取得。
	 * 
	 * @return
	 */
	public static Map<String, TransferFactory> getTransferFactoryList() {
		return Collections.unmodifiableMap(factories);
	}

	/**
	 * 
	 * 転送機能をスケジュール実行します。<BR>
	 * Quartzからスケジュール実行時に呼び出されます。
	 * 
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * トランザクション開始はユーザが制御する。
	 * また、追加実装により、トランザクションの入れ子が予期せず生じることを避けるため、Neverを採用する。
	 */
	public void scheduleRunTransfer(String transferId, String calendarId) throws InvalidRole, HinemosUnknown {
		logger.debug("scheduleRunTransfer() : transferId=" + transferId + ", calendarId=" + calendarId);

		try {
			//カレンダをチェック
			boolean check = false;
			if(calendarId != null && calendarId.length() > 0){
				//カレンダによる実行可/不可のチェック
				if(new CalendarControllerBean().isRun(calendarId, HinemosTime.getDateInstance().getTime()).booleanValue()){
					check = true;
				}
			}
			else{
				check = true;
			}

			if(!check)
				return;

			transfer(transferId);
		} catch (CalendarNotFound e) {
			throw new HinemosUnknown(e.getMessage(),e);
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			throw e;
		} catch(Exception e){
			logger.warn("scheduleRunMaintenance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
		}
	}
	
	/**
	 * 指定した転送設定に従って、転送を行う。
	 * 
	 * @param transferId
	 * @throws Exception
	 */
	public void transfer(String transferId) throws Exception {
		synchronized (proccesing) {
			if (proccesing.contains(transferId))
				return;
			proccesing.add(transferId);
			logger.debug(String.format("transfer() : add %s to exclusive.", transferId));
		}
		
		try {
			// 送信処理
			JpaTransactionManager jtm = null;
			try {
				jtm = new JpaTransactionManager();
				HinemosEntityManager em = jtm.getEntityManager();
				
				TransferInfo info;
				try {
					info = QueryUtil.getTransferInfo(transferId);
				} catch (LogTransferNotFound | InvalidRole e) {
					logger.info(String.format("transfer() : %s", e.getMessage()));
					return;
				}
				
				logger.debug(String.format("transfer() : info=%s", info.toString()));
				
				TransferFactory factory = factories.get(info.getDestTypeId());
				if (factory == null) {
					logger.warn(String.format("transfer() : Not found TransferFactory(%s)", info.getDestTypeId()));
					return;
				}
				
				Long lastPosition = null;
				try (Transfer transfer = factory.createTansfer(info, new Binder())) {
					switch(info.getDataType()) {
					case event:
						{
							try (JpaRowIterable<EventLogEntity, EventLogEntity> iter = new JpaRowIterable<>(JpaQueryUtil.createEventUtil(info, em))) {
								EventLogEntity last = transfer.transferEvents(iter, new TrasferCallback<EventLogEntity>() {
									private long count = 0;
									@Override
									public void onTransferred(EventLogEntity last) throws TransferException {
										if (++count % saveForCount == 0) {
											saveLastPosition(transferId, last.getPosition());
										}
									}
								});
								if (last != null) {
									lastPosition = last.getPosition();
								}
							}
						}
						break;
					case job:
						{
							try (JpaRowIterable<JobSessionEntity, JobSessionEntity> iter = new JpaRowIterable<>(JpaQueryUtil.createJobUtil(info, em))) {
								JobSessionEntity last = transfer.transferJobs(iter, new TrasferCallback<JobSessionEntity>() {
									private long count = 0;
									@Override
									public void onTransferred(JobSessionEntity last) throws TransferException {
										if (++count % saveForCount == 0) {
											saveLastPosition(transferId, last.getPosition());
										}
									}
								});
								if (last != null) {
									lastPosition = last.getPosition();
								}
							}
						}
						break;
					case numeric:
						{
							try (JpaRowIterable<CollectData, TransferNumericData> iter = new JpaRowIterable<>(JpaQueryUtil.createNumericUtil(info, em))) {
								TransferNumericData last = transfer.transferNumerics(iter, new TrasferCallback<TransferNumericData>() {
									private long count = 0;
									@Override
									public void onTransferred(TransferNumericData last) throws TransferException {
										if (++count % saveForCount == 0) {
											saveLastPosition(transferId, last.data.getPosition());
										}
									}
								});
								if (last != null) {
									lastPosition = last.data.getPosition();
								}
							}
						}
						break;
					case string:
						{
							try (JpaRowIterable<CollectStringData, TransferStringData> iter = new JpaRowIterable<>(JpaQueryUtil.createStringUtil(info, em))) {
								TransferStringData last = transfer.transferStrings(iter, new TrasferCallback<TransferStringData>() {
									private long count = 0;
									@Override
									public void onTransferred(TransferStringData last) throws TransferException {
										if (++count % saveForCount == 0) {
											saveLastPosition(transferId, last.data.getDataId());
											logger.debug(String.format("transfer() : save LastPosition. transferId=%s, lastPosition=%d", info.getTransferId(), last.data.getDataId()));
										}
									}
								});
								if (last != null) {
									lastPosition = last.data.getDataId();
								}
							}
						}
						break;
					}
				}
				
				// 位置情報を更新するかどうか確認
				if (lastPosition == null) {
					logger.debug(String.format("transfer() : transferId=%s, lastPosition is null", info.getTransferId()));
					return;
				}
				
				logger.debug(String.format("transfer() : transferId=%s, lastPosition=%d", info.getTransferId(), lastPosition));
				
				saveLastPosition(transferId, lastPosition);
			} catch(Exception e) {
				if (jtm != null)
					jtm.rollback();
				logger.warn(String.format("transfer() : %s, %s", e.getClass().getSimpleName(), e.getMessage()), e);
				throw e;
			} finally {
				if (jtm != null)
					jtm.close();
			}
		} finally {
			// 排他制御から、実施した転送設定を解除。
			synchronized (proccesing) {
				logger.debug(String.format("transfer() : release %s from exclusive.", transferId));
				proccesing.remove(transferId);
			}
		}
	}
	
	private void saveLastPosition(String transferId, long lastPosition) {
		JpaTransactionManager jtm = new JpaTransactionManager();

		// 位置情報更新
		jtm.begin();
		
		// メモリの圧迫を防ぐため、EntityManager.clear を上記処理内で呼び出している。
		// したがって、処理冒頭に取得した TransferInfo は、jpa のコンテキストから分離しているので、再度取得し直す。
		TransferInfo forUpdate;
		try {
			forUpdate = QueryUtil.getTransferInfo(transferId);
		} catch (LogTransferNotFound | InvalidRole e) {
			logger.info(String.format("transfer() : %s", e.getMessage()));
			return;
		}
		
		TransferInfoPosition position = forUpdate.getPosition();
		if (position == null) {
			position = new TransferInfoPosition(forUpdate.getTransferId());
			forUpdate.setPosition(position);
		}
		
		position.setLastPosition(lastPosition);
		position.setLastDate(HinemosTime.currentTimeMillis());
		
		jtm.commit();
	}

	/**
	 * 文字列収集情報を検索する。
	 * 
	 * @param format
	 * @return
	 * @throws InvalidSetting
	 * @throws HinemosUnknown 
	 * @throws InvalidRole 
	 */
	public StringQueryResult queryCollectStringData(StringQueryInfo queryInfo) throws InvalidSetting, HinemosUnknown, InvalidRole  {
		long start = System.currentTimeMillis();
		
		logger.debug(String.format("queryCollectStringData() : start query. query=%s", queryInfo));
		
		// 検索タイムアウト値を取得
		int logSearchTimeout = PropertyConstants.hub_search_timeout.number();
		logger.info("queryCollectStringData() : query timeout=" + logSearchTimeout);
		
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("hinemos");
		EntityManager em = factory.createEntityManager();

		Connection cn = null;
		try {
			// 検索タイムアウト設定
			cn = HubControllerUtil.setStatementTimeout(em, logSearchTimeout);
			
			// キーのクエリ
			StringBuilder keyQueryStr = new StringBuilder("SELECT DISTINCT k FROM CollectStringKeyInfo k");
			List<String> facilityIds = new ArrayList<>();
			if (queryInfo.getFacilityId() != null ||
					queryInfo.getMonitorId() != null) {
				StringBuilder whereStr = new StringBuilder();
				if (queryInfo.getFacilityId() != null) {
					if (!FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(queryInfo.getFacilityId())) {
						RepositoryControllerBean repositoryCtrl = new RepositoryControllerBean();
						try {
							if (repositoryCtrl.isNode(queryInfo.getFacilityId())){
								facilityIds.add(queryInfo.getFacilityId());
							} else {
								List<NodeInfo> nodeinfoList = repositoryCtrl.getNodeList(queryInfo.getFacilityId(), 0);
								if (!nodeinfoList.isEmpty()) {
									for (NodeInfo node: nodeinfoList) {
										facilityIds.add(node.getFacilityId());
									}
								}
							}
						} catch (InvalidRole e ){
							logger.warn("queryCollectStringData " + e.getMessage());
							throw e;
						} catch(FacilityNotFound e) {
							logger.warn("queryCollectStringData " + e.getMessage());
							throw new IllegalStateException("preCollect() : can't get NodeInfo. facilityId = " + queryInfo.getFacilityId());
						}
					} else {
						facilityIds.add(FacilityTreeAttributeConstant.UNREGISTERED_SCOPE);
					}
					whereStr.append("k.id.facilityId IN :nodeIds");
				}
				
				if (queryInfo.getMonitorId() != null) {
					// クエリを実施しているユーザーが、指定した監視設定にアクセスできるかどうかは、考慮しない。
					// 既に上記処理でアクセスできるノードの範囲を絞っているので。
					if (whereStr.length() != 0) {
						whereStr.append(" AND ");
					}
					whereStr.append(String.format("k.id.monitorId = '%s'", queryInfo.getMonitorId()));
				}

				if (whereStr.length() != 0) {
					keyQueryStr.append(" WHERE ").append(whereStr);
				}
			}

			TypedQuery<CollectStringKeyInfo> keyQuery = HubControllerUtil.createQuery(em, keyQueryStr.toString(), CollectStringKeyInfo.class, logSearchTimeout);
			if (!facilityIds.isEmpty()) {
				keyQuery.setParameter("nodeIds", facilityIds);
				logger.debug(String.format("queryCollectStringData() : target nodes. nodes=%s, query=%s", facilityIds, queryInfo));
			} else {
				// 検索対象となるノードがないので終了。
				throw new InvalidSetting(MessageConstant.MESSAGE_HUB_SEARCH_NO_NODE.getMessage());
			}

			Map<Long, CollectStringKeyInfo> keys = new HashMap<>();
			List<CollectStringKeyInfo> ketResults = keyQuery.getResultList();
			for (CollectStringKeyInfo r: ketResults) {
				keys.put(r.getCollectId(), r);
			}
			logger.debug(String.format("queryCollectStringData() : target data key. keys=%s, query=%s", keys.values(), queryInfo));
			
			if (keys.isEmpty()) {
				// アクセスできるキーがないので終了。
				StringQueryResult result = new StringQueryResult();
				result.setOffset(queryInfo.getOffset());
				result.setSize(0);
				result.setCount((queryInfo.isNeedCount() != null && queryInfo.isNeedCount()) ? 0: null);
				result.setTime(System.currentTimeMillis() - start);
				logger.debug(String.format("queryCollectStringData() : end query. result=%s, query=%s", result, queryInfo));
				return result;
			}

			// データのクエリ
			StringBuilder dataQueryStr;
			if (PropertyConstants.hub_search_switch_join.bool()) {
				dataQueryStr = new StringBuilder("FROM CollectStringData d JOIN d.tagList t "); 
			} else {
				dataQueryStr = new StringBuilder("FROM CollectStringData d "); 
			}
			if (queryInfo.getFrom() != null ||
					queryInfo.getTo() != null ||
					queryInfo.getKeywords() != null
					) {

				if (queryInfo.getFrom() != null && queryInfo.getTo() != null && queryInfo.getFrom() > queryInfo.getTo()){
					throw new InvalidSetting(MessageConstant.MESSAGE_HUB_SEARCH_DATE_INVALID.getMessage());
				}

				StringBuilder whereStr = new StringBuilder();
				if (queryInfo.getFrom() != null) {
					whereStr.append(" AND ");
					whereStr.append(String.format("d.time >= '%d'", queryInfo.getFrom()));
				}

				if (queryInfo.getTo() != null) {
					whereStr.append(" AND ");
					whereStr.append(String.format("d.time < '%d'", queryInfo.getTo()));
				}

				if (queryInfo.getKeywords() != null && !queryInfo.getKeywords().isEmpty()) {
					
					StringBuffer conditionValueBuffer=new StringBuffer();
					String operator="";
					if (com.clustercontrol.hub.bean.StringQueryInfo.Operator.AND ==  queryInfo.getOperator() ){
						operator = " AND ";
					}else{
						operator = " OR ";
					}
					
					String keywords = queryInfo.getKeywords();
					
					List<Token> tokens = parseKeywors(keywords);
					for (Token token: tokens) {
						if (conditionValueBuffer.length() != 0){
							conditionValueBuffer.append(operator);
						} else {
							conditionValueBuffer.append("(");
						}

						if (token.key == null) {
							// タグ指定ではない場合
							if (token.negate){
								if (token.word.length() == 1){
									throw new InvalidSetting(MessageConstant.MESSAGE_HUB_SEARCH_KEYWORD_INVALID.getMessage());
								}
								conditionValueBuffer
									.append("(")
									.append(String.format("d.value NOT LIKE '%s'", token.word.substring(1)))
									.append(" OR ")
									.append(String.format("EXISTS(SELECT t FROM IN(d.tagList) t WHERE t.value NOT LIKE '%s')", token.word.substring(1)))
									.append(")");
							} else {
								conditionValueBuffer
									.append("(")
									.append(String.format("d.value LIKE '%s'", token.word))
									.append(" OR ")
									.append(String.format("EXISTS(SELECT t FROM IN(d.tagList) t WHERE t.value LIKE '%s')", token.word))
									.append(")");
							}
						} else {
							// タグ指定の場合
							if (token.negate){
								conditionValueBuffer.append(String.format("EXISTS(SELECT t FROM IN(d.tagList) t WHERE (t.key LIKE '%s' AND t.value NOT LIKE '%s'))", token.key, token.word));
							}else{
								conditionValueBuffer.append(String.format("EXISTS(SELECT t FROM IN(d.tagList) t WHERE (t.key LIKE '%s' AND t.value LIKE '%s'))", token.key, token.word));
							}
						}
					}
					if (conditionValueBuffer.length() != 0){
						whereStr.append(" AND " + conditionValueBuffer.toString()).append(")");
					}
				}
				
				dataQueryStr.append("WHERE d.id.collectId IN :collectIds");
				
				if (whereStr.length() != 0) {
					dataQueryStr.append(whereStr);
				}
			}
			
			StringQueryResult result = new StringQueryResult();
			result.setOffset(queryInfo.getOffset());
			
			// データの最大数を取得する。
			if (queryInfo.isNeedCount() != null && queryInfo.isNeedCount()) {
				String queryStr = "SELECT COUNT(DISTINCT d) " + dataQueryStr.toString();
				logger.debug(String.format("queryCollectStringData() : query count. queryStr=%s, query=%s", queryStr, queryInfo));
				
				TypedQuery<Long> dataQuery = em.createQuery(queryStr, Long.class);
				dataQuery.setParameter("collectIds", keys.keySet());
				
				Long count = dataQuery.getSingleResult();
				if (count == null || count == 0) {
					result.setSize(0);
					result.setCount(0);
					result.setTime(System.currentTimeMillis() - start);
					logger.debug(String.format("queryCollectStringData() : end query. result=%s, query=%s", result, queryInfo));
					return result;
				}

				result.setCount(Integer.valueOf(count.toString()));
			}
			
			String queryStr = "SELECT DISTINCT d " + dataQueryStr.toString() + " ORDER BY d.time DESC";
			logger.debug(String.format("queryCollectStringData() : query data. queryStr=%s, query=%s", queryStr, queryInfo));
			
			TypedQuery<CollectStringData> dataQuery = HubControllerUtil.createQuery(em, queryStr.toString(), CollectStringData.class, logSearchTimeout);
			
			dataQuery.setParameter("collectIds", keys.keySet());
			
			dataQuery.setFirstResult(queryInfo.getOffset());
			dataQuery.setMaxResults(queryInfo.getSize());
			List<CollectStringData> dataResults = dataQuery.getResultList();
			
			if (dataResults == null || dataResults.isEmpty()) {
				result.setSize(0);
				result.setTime(System.currentTimeMillis() - start);
				logger.debug(String.format("queryCollectStringData() : end query. result=%s, query=%s", result, queryInfo));
				return result;
			}
			
			result.setOffset(queryInfo.getOffset());
			result.setSize(dataResults.size());
			
			List<StringData> stringDataList = new ArrayList<StringData>();
			for (CollectStringData r: dataResults) {
				CollectStringKeyInfo key = keys.get(r.getCollectId());
				StringData data = new StringData();
				data.setFacilityId(key.getFacilityId());
				data.setMonitorId(key.getMonitorId());
				data.setTargetName(key.getTargetName());
				data.setTime(r.getTime());
				data.setData(r.getValue());
				List<Tag> tagList = new ArrayList<Tag>();
				Tag tag = null;
				for (CollectDataTag t: r.getTagList()) {
					tag = new Tag();
					tag.setKey(t.getKey());
					tag.setValue(t.getValue());
					tagList.add(tag);
				}
				data.setTagList(tagList);
				stringDataList.add(data);
			}
			result.setDataList(stringDataList);
			
			result.setTime(System.currentTimeMillis() - start);
			
			logger.debug(String.format("queryCollectStringData() : end query. result=%s, query=%s", result.toResultString(), queryInfo));
			
			return result;
		}catch(SQLException e){
			logger.warn(e.getMessage());
			throw new HinemosUnknown(e.getMessage(), e);
		}catch(PersistenceException e){
			// クエリタイムアウトか判定。
			if (QueryPertial.isQueryTimeout(e)){
				throw new InvalidSetting(MessageConstant.MESSAGE_HUB_SEARCH_TIMEOUT.getMessage());
			}
			logger.warn(e.getMessage());
			throw new HinemosUnknown(e.getMessage(), e);
		} catch(InvalidSetting e) {
			logger.warn(e.getMessage());
			throw e;
		}catch(Exception e){
			logger.warn(e.getMessage());
			throw new HinemosUnknown(e.getMessage(), e);
		}finally{
			if (cn != null) {
				try {
					// クエリタイムアウト解除。
					HubControllerUtil.resetStatementTimeout(cn, em, logSearchTimeout);
				} catch (SQLException e) {
					logger.warn(e.getMessage());
				} finally {
					try {
						cn.close();
					} catch (SQLException e) {
						logger.warn(e.getMessage());
					}
				}
			}
			em.close();
			factory.close();
		}
	}
	
	/**
	 * 解析したワードを格納。
	 *
	 */
	public static class Token {
		public final String key;
		public final String word;
		public final boolean negate;
		
		public Token(String key, String word, boolean negate) {
			this.key = key;
			this.word = word;
			this.negate = negate;
		}
		
		public Token(String word, boolean negate) {
			this(null, word, negate);
		}
		
		public Token(String word) {
			this(null, word, false);
		}
		
		public Token(String key, String word) {
			this(key, word, false);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + (negate ? 1231 : 1237);
			result = prime * result + ((word == null) ? 0 : word.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (negate != other.negate)
				return false;
			if (word == null) {
				if (other.word != null)
					return false;
			} else if (!word.equals(other.word))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "Token [key=" + key + ", word=" + word + ", negate=" + negate + "]";
		}
	}
	
	/**
	 * 指定したキーワード文字列を解析する。
	 * 
	 * @param keywords
	 * @return
	 */
	public static List<Token> parseKeywors(String keywords) {
		boolean inQuote = false;
		boolean inEscape = false;
		boolean inNegate = false;
		String qoutedString = null;
		
		List<Token> tokens = new ArrayList<>();
		
		int pos = 0;
		String key = null;
		StringBuilder buf = new StringBuilder();
		while (pos < keywords.length()) {
			char c = keywords.charAt(pos++);
			
			if (Character.isWhitespace(c)) {
				// クォートされた文字列があれば、抽出文字列として追加。
				if (qoutedString != null) {
					tokens.add(new Token(qoutedString, inNegate));
					qoutedString = null;
					inNegate = false;
				}
				
				// 空白文字
				if (inQuote) {
					// クォート中
					buf.append(c);
				} else {
					// クォート中でないならワードの区切り
					// 追加するワードがあるか？
					if (buf.length() != 0) {
						tokens.add(new Token(key, buf.toString(), inNegate));
						key = null;
						buf = new StringBuilder();
					} else if (key != null) {
						// 追加するワードがないが、キーが存在
						// キーをワードとして登録。
						tokens.add(new Token(key, inNegate));
						key = null;
						buf = new StringBuilder();
					}
					inNegate = false;
				}
				inEscape = false;
			} else if (ESCAPE_CHAR == c) {
				// エスケープ
				if (inEscape) {
					buf.append(c);
					inEscape = false;
				} else {
					inEscape = true;
				}
			} else if (QUATATION_CHAR == c) {
				// クォートされた文字列があれば、抽出文字列として追加。
				if (qoutedString != null) {
					tokens.add(new Token(qoutedString, inNegate));
					qoutedString = null;
					inNegate = false;
				}
				
				// 現在の文字がエスケープされているか？
				if (inEscape) {
					buf.append(c);
					inEscape = false;
				} else {
					// クォートされたか？
					if (inQuote) {
						// タグ検索のキーが既に追加されているか？
						if (key != null) {
							// 抽出された文字列を追加。
							if (buf.length() != 0) {
								tokens.add(new Token(key, buf.toString(), inNegate));
								key = null;
								buf = new StringBuilder();
							} else {
								tokens.add(new Token(key, inNegate));
								key = null;
							}
							inNegate = false;
						} else {
							// タグ検索の可能性があるので、クォートされた文字列を一旦退避。
							// 次の文字でタグ検索のキーにする判断。
							qoutedString = buf.toString();
							buf = new StringBuilder();
						}
						inQuote = false;
					} else {
						if (key == null) {
							// 追加するワードがあるか？
							if (buf.length() != 0) {
								tokens.add(new Token(buf.toString(), inNegate));
								buf = new StringBuilder();
							}
							inNegate = false;
						} else {
							if (buf.length() != 0) {
								tokens.add(new Token(key, buf.toString(), inNegate));
								key = null;
								buf = new StringBuilder();
								inNegate = false;
							}
						}
						inQuote = true;
					}
				}
			} else if (SEPARATE_CHAR == c) {
				// クォート中か？
				if (!inQuote) {
					// タグ検索のキーは、抽出済みか？
					if (key == null) {
						if (buf.length() != 0) {
							// キー取得
							key = buf.toString();
							buf = new StringBuilder();
							inNegate = false;
						} else if (qoutedString != null) {
							key = qoutedString;
							qoutedString = null;
							inNegate = false;
						}
					} else {
						// タグ検索のキーは既にあるため、タグ検索の値を抽出中と判断。
						// セパレータは無視する。
					}
				} else {
					buf.append(c);
				}
				inEscape = false;
			} else if (NEGATION_CHAR == c) {
				// クォートされた文字列があれば、抽出文字列として追加。
				if (qoutedString != null) {
					tokens.add(new Token(qoutedString, inNegate));
					qoutedString = null;
					inNegate = false;
				}
				
				if (!inQuote) {
					if (buf.length() == 0) {
						// 最初の一文字目が、否定文字なので、
						// 否定モードとする。
						inNegate = true;
					}
				} else {
					buf.append(c);
				}
				inEscape = false;
			} else {
				// クォートされた文字列があれば、抽出文字列として追加。
				if (qoutedString != null) {
					tokens.add(new Token(qoutedString, inNegate));
					qoutedString = null;
					inNegate = false;
				}
				
				buf.append(c);
				inEscape = false;
			}
		}

		if (qoutedString != null) {
			tokens.add(new Token(qoutedString, inNegate));
			qoutedString = null;
			inNegate = false;
		}
		
		if (!inQuote) {
			// 追加するワードがあるか？
			if (buf.length() != 0) {
				tokens.add(new Token(key, buf.toString(), inNegate));
			// 追加するワードがないが、キーが存在するか？
			} else if (key != null) {
				// キーをワードとして登録。
				tokens.add(new Token(key, inNegate));
			}
		}
		
		return tokens;
	}
}