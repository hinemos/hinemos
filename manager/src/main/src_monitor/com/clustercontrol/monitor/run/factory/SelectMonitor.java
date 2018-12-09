/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.binary.model.BinaryPatternInfo;
import com.clustercontrol.binary.util.BinaryQueryUtil;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.http.model.Page;
import com.clustercontrol.http.model.Pattern;
import com.clustercontrol.http.model.Variable;
import com.clustercontrol.monitor.bean.MonitorFilterInfo;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfo;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.snmptrap.model.TrapValueInfo;
import com.clustercontrol.snmptrap.model.VarBindPattern;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 監視情報を検索する抽象クラス<BR>
 * <p>
 * 監視種別（真偽値，数値，文字列）の各クラスで継承してください。
 *
 * @version 6.1.0
 * @since 2.0.0
 */
public class SelectMonitor {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectMonitor.class );

	/**
	 * 引数で指定された監視情報を返します。
	 * <p>
	 * <ol>
	 * <li>引数で指定された監視情報を取得します。</li>
	 * <li>Quartzより、有効/無効を取得します。</li>
	 * <li>監視情報より判定情報を取得します。各監視種別（真偽値，数値，文字列）のサブクラスで実装します（{@link #getJudgementInfo()}）。</li>
	 * <li>監視情報よりチェック条件を取得します。各監視管理のサブクラスで実装します（{@link #getCheckInfo()}）。</li>
	 * </ol>
	 * 
	 * @param monitorTypeId 監視対象ID
	 * @param monitorId 監視項目ID
	 * @return 監視情報
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.factory.SelectJobKick#getValid(String, String)
	 * @see #getJudgementInfo()
	 * @see #getCheckInfo()
	 */

	public MonitorInfo getMonitor(String monitorTypeId, String monitorId)
			throws MonitorNotFound, HinemosUnknown, InvalidRole {

		MonitorInfo bean = null;
		try
		{
			// 監視情報を取得
			bean = QueryUtil.getMonitorInfoPK(monitorId);

			// 通知情報を設定する
			bean.setNotifyRelationList(
					NotifyRelationCache.getNotifyList(bean.getNotifyGroupId()));

			if (bean.getMonitorType().equals(MonitorTypeConstant.TYPE_NUMERIC)) {
				// 通知情報(将来予測監視用)を設定する
				bean.setPredictionNotifyRelationList(
						NotifyRelationCache.getNotifyList(
							CollectMonitorManagerUtil.getPredictionNotifyGroupId(bean.getNotifyGroupId())));
	
				// 通知情報(変更点監視用)を設定する
				bean.setChangeNotifyRelationList(
						NotifyRelationCache.getNotifyList(
								CollectMonitorManagerUtil.getChangeNotifyGroupId(bean.getNotifyGroupId())));
			}

			if (bean.getMonitorType() == null) {
				// ここには通らない
			} else if(bean.getMonitorType().equals(MonitorTypeConstant.TYPE_NUMERIC)
				&& bean.getNumericValueInfo() != null) {
				for (MonitorNumericValueInfo monitorNumericValueInfo : bean.getNumericValueInfo()) {
					monitorNumericValueInfo = QueryUtil.getMonitorNumericValueInfoPK(monitorNumericValueInfo.getId());
				}
			} else if(bean.getMonitorType().equals(MonitorTypeConstant.TYPE_STRING)
				&& bean.getStringValueInfo() != null) {
				for (MonitorStringValueInfo monitorStringValueInfo : bean.getStringValueInfo()) {
					monitorStringValueInfo = QueryUtil.getMonitorStringValueInfoPK(monitorStringValueInfo.getId());
				}
			} else if (bean.getMonitorType().equals(MonitorTypeConstant.TYPE_TRUTH)
				&& bean.getTruthValueInfo() != null) {
				for (MonitorTruthValueInfo monitorTruthValueInfo : bean.getTruthValueInfo()) {
					monitorTruthValueInfo = QueryUtil.getMonitorTruthValueInfoPK(monitorTruthValueInfo.getId());
				}
			} else if (bean.getMonitorType().equals(MonitorTypeConstant.TYPE_SCENARIO)
				&& bean.getHttpScenarioCheckInfo() != null
				&& bean.getHttpScenarioCheckInfo().getPages() != null) {
				for (Page page : bean.getHttpScenarioCheckInfo().getPages()) {
					page = com.clustercontrol.http.util.QueryUtil.getPagePK(page.getId());
					if (page.getPatterns() != null) {
						for (Pattern pattern : page.getPatterns()) {
							pattern = com.clustercontrol.http.util.QueryUtil.getPatternPK(pattern.getId());
						}
					}
					if (page.getVariables() != null) {
						for (Variable variable : page.getVariables()) {
							variable = com.clustercontrol.http.util.QueryUtil.getVariablePK(variable.getId());
						}
					}
				}
			} else if (bean.getMonitorType().equals(MonitorTypeConstant.TYPE_TRAP)
				&& bean.getTrapCheckInfo() != null
				&& bean.getTrapCheckInfo().getTrapValueInfos() != null) {
				for (TrapValueInfo trapValueInfo : bean.getTrapCheckInfo().getTrapValueInfos()) {
					trapValueInfo = com.clustercontrol.snmptrap.util.QueryUtil.getMonitorTrapValueInfoPK(trapValueInfo.getId());
					if (trapValueInfo.getVarBindPatterns() != null) {
						for (VarBindPattern varBindPattern : trapValueInfo.getVarBindPatterns()) {
							varBindPattern = com.clustercontrol.snmptrap.util.QueryUtil
									.getMonitorTrapVarbindPatternInfoPK(varBindPattern.getId());
						}
					}
				}
			} else if(bean.getMonitorType().equals(MonitorTypeConstant.TYPE_BINARY)
					&& bean.getStringValueInfo() != null
					&& bean.getBinaryPatternInfo() != null) {
					for (MonitorStringValueInfo monitorStringValueInfo : bean.getStringValueInfo()) {
						monitorStringValueInfo = QueryUtil.getMonitorStringValueInfoPK(monitorStringValueInfo.getId());
					}
					for (BinaryPatternInfo binaryPatternInfo : bean.getBinaryPatternInfo()) {
						binaryPatternInfo = BinaryQueryUtil.getBinaryPatternInfoPK(binaryPatternInfo.getId());
					}
				}

		} catch (MonitorNotFound e) {
			outputLog(monitorTypeId, monitorId, PriorityConstant.TYPE_WARNING, MessageConstant.MESSAGE_SYS_010_MON);
			throw e;
		} catch (InvalidRole e) {
			outputLog(monitorTypeId, monitorId, PriorityConstant.TYPE_WARNING, MessageConstant.MESSAGE_SYS_010_MON);
			throw e;
		}

		return bean;
	}

	/**
	 * 監視情報一覧を返します。
	 * <p>
	 * <ol>
	 * <li>引数で指定された監視対象の監視情報を取得します。</li>
	 * <li>１監視情報をテーブルのカラム順（{@link com.clustercontrol.monitor.run.bean.MonitorTabelDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>この１監視情報を保持するリストを、監視情報一覧を保持するリスト（{@link ArrayList}）に格納し返します。<BR>
	 *  <dl>
	 *  <dt>監視情報一覧（Objectの2次元配列）</dt>
	 *  <dd>{ 監視情報1 {カラム1の値, カラム2の値, … }, 監視情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *  </dl>
	 * </li>
	 * </ol>
	 * 
	 * @param monitorTypeId 監視対象ID
	 * @return 監視情報一覧（Objectの2次元配列）
	 * @see com.clustercontrol.monitor.run.bean.MonitorTabelDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<MonitorInfo> getMonitorList(String monitorTypeId) {
		return new ArrayList<>(QueryUtil.getMonitorInfoByMonitorTypeId(monitorTypeId));
	}
	
	/**
	 * 監視情報一覧を返します。
	 * <p>
	 * <ol>
	 * <li>引数で指定された監視対象の監視情報を取得します。</li>
	 * <li>１監視情報をテーブルのカラム順（{@link com.clustercontrol.monitor.run.bean.MonitorTabelDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>この１監視情報を保持するリストを、監視情報一覧を保持するリスト（{@link ArrayList}）に格納し返します。<BR>
	 *  <dl>
	 *  <dt>監視情報一覧（Objectの2次元配列）</dt>
	 *  <dd>{ 監視情報1 {カラム1の値, カラム2の値, … }, 監視情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *  </dl>
	 * </li>
	 * </ol>
	 * 
	 * @param monitorTypeId 監視対象ID
	 * @return 監視情報一覧（Objectの2次元配列）
	 * @see com.clustercontrol.monitor.run.bean.MonitorTabelDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<MonitorInfo> getMonitorListObjectPrivilegeModeNONE(String monitorTypeId) {
		return new ArrayList<>(QueryUtil.getMonitorInfoByMonitorTypeId_NONE(monitorTypeId));
	}

	/**
	 * 監視情報一覧を返します。
	 * <p>
	 * <ol>
	 * <li>全ての監視対象の監視情報を取得します。</li>
	 * <li>１監視情報をテーブルのカラム順（{@link com.clustercontrol.monitor.run.bean.MonitorTabelDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>この１監視情報を保持するリストを、監視情報一覧を保持するリスト（{@link ArrayList}）に格納し返します。<BR>
	 *  <dl>
	 *  <dt>監視情報一覧（Objectの2次元配列）</dt>
	 *  <dd>{ 監視情報1 {カラム1の値, カラム2の値, … }, 監視情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *  </dl>
	 * </li>
	 * </ol>
	 * 
	 * @return 監視情報一覧（Objectの2次元配列）
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.monitor.run.bean.MonitorTabelDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<MonitorInfo> getMonitorList() throws MonitorNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getMonitorList() : start");
		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		try
		{
			// 監視情報一覧を取得
			List<MonitorInfo> ct = QueryUtil.getAllMonitorInfo();

			for (MonitorInfo info : ct) {
				list.add(info);

				// for debug
				if(m_log.isDebugEnabled()){
					m_log.debug("getMonitorList() : " +
							"monitorId = " + info.getMonitorId() +
							", monitorTypeId = " + info.getMonitorTypeId() +
							", monitorType = " + info.getMonitorType() +
							", description = " + info.getDescription() +
							", facilityId = " + info.getFacilityId() +
							", runInterval = " + info.getRunInterval() +
							", calendarId = " + info.getCalendarId() +
							", failurePriority = " + info.getFailurePriority() +
							", notifyGroupId = " + info.getNotifyGroupId() +
							", application = " + info.getApplication() +
							", monitorFlg = " + info.getMonitorFlg() +
							", collectorFlg = " + info.getCollectorFlg() +
							", predictionFlg = " + info.getPredictionFlg() +
							", predictionMethod = " + info.getPredictionMethod() +
							", predictionAnalysysRange = " + info.getPredictionAnalysysRange() +
							", predictionTarget = " + info.getPredictionTarget() +
							", regDate = " + info.getRegDate() +
							", updateDate = " + info.getUpdateDate() +
							", regUser = " + info.getRegUser() +
							", updateUser = " + info.getUpdateUser());
				}
			}
		} catch (HinemosUnknown e) {
			outputLog("", "", PriorityConstant.TYPE_WARNING, MessageConstant.MESSAGE_SYS_011_MON);
			throw e;
		}

		m_log.debug("getMonitorList() : end");
		return list;
	}

	/**
	 * 監視情報一覧を返します。
	 * <p>
	 * <ol>
	 * <li>全ての監視対象の監視情報を取得します。</li>
	 * <li>１監視情報をテーブルのカラム順（{@link com.clustercontrol.monitor.run.bean.MonitorTabelDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>この１監視情報を保持するリストを、監視情報一覧を保持するリスト（{@link ArrayList}）に格納し返します。<BR>
	 *  <dl>
	 *  <dt>監視情報一覧（Objectの2次元配列）</dt>
	 *  <dd>{ 監視情報1 {カラム1の値, カラム2の値, … }, 監視情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *  </dl>
	 * </li>
	 * </ol>
	 * 
	 * @return 監視情報一覧（Objectの2次元配列）
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.monitor.run.bean.MonitorTabelDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<MonitorInfo> getPerformanceMonitorList() throws MonitorNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getMonitorList() : start");
		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		try
		{
			// 監視情報一覧を取得
			List<MonitorInfo> ct = QueryUtil.getAllMonitorInfo();

			for (MonitorInfo info : ct) {
				list.add(info);

				// for debug
				if(m_log.isDebugEnabled()){
					m_log.debug("getMonitorList() : " +
							"monitorId = " + info.getMonitorId() +
							", monitorTypeId = " + info.getMonitorTypeId() +
							", monitorType = " + info.getMonitorType() +
							", description = " + info.getDescription() +
							", facilityId = " + info.getFacilityId() +
							", runInterval = " + info.getRunInterval() +
							", calendarId = " + info.getCalendarId() +
							", failurePriority = " + info.getFailurePriority() +
							", notifyGroupId = " + info.getNotifyGroupId() +
							", application = " + info.getApplication() +
							", monitorFlg = " + info.getMonitorFlg() +
							", collectorFlg = " + info.getCollectorFlg() +
							", predictionFlg = " + info.getPredictionFlg() +
							", predictionMethod = " + info.getPredictionMethod() +
							", predictionAnalysysRange = " + info.getPredictionAnalysysRange() +
							", predictionTarget = " + info.getPredictionTarget() +
							", regDate = " + info.getRegDate() +
							", updateDate = " + info.getUpdateDate() +
							", regUser = " + info.getRegUser() +
							", updateUser = " + info.getUpdateUser());
				}
			}
		} catch (HinemosUnknown e) {
			outputLog("", "", PriorityConstant.TYPE_WARNING, MessageConstant.MESSAGE_SYS_011_MON);
			throw e;
		}

		m_log.debug("getMonitorList() : end");
		return list;
	}

	/**
	 * 指定したフィルタにマッチする監視情報一覧を返します。
	 * @param condition
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getMonitorList(MonitorFilterInfo condition) throws HinemosUnknown, InvalidRole, MonitorNotFound {
		m_log.debug("getMonitorList() condition ");
		if(m_log.isDebugEnabled()){
			if(condition != null){
				m_log.debug("getMonitorList() " +
						"monitorId = " + condition.getMonitorId() +
						", monitorTypeId = " + condition.getMonitorTypeId() +
						", description = " + condition.getDescription() +
						", facilityId = " + condition.getFacilityId() +
						", calendarId = " + condition.getCalendarId() +
						", regUser = " + condition.getRegUser() +
						", regFromDate = " + condition.getRegFromDate() +
						", regToDate = " + condition.getRegToDate() +
						", updateUser = " + condition.getUpdateUser() +
						", updateFromDate = " + condition.getUpdateFromDate() +
						", updateToDate = " + condition.getUpdateToDate() +
						", monitorFlg = " + condition.getMonitorFlg() +
						", collectorFlg = " + condition.getCollectorFlg() +
						", ownerRoleId = " + condition.getOwnerRoleId());
			}
		}

		ArrayList<MonitorInfo> filterList = new ArrayList<MonitorInfo>();
		// 条件未設定の場合は空のリストを返却する
		if(condition == null){
			m_log.debug("getMonitorList() condition is null");
			return filterList;
		}

		// facilityId以外の条件で監視設定情報を取得
		List<MonitorInfo> entityList = QueryUtil.getMonitorInfoByFilter(
				condition.getMonitorId(),
				condition.getMonitorTypeId(),
				condition.getDescription(),
				condition.getCalendarId(),
				condition.getRegUser(),
				condition.getRegFromDate(),
				condition.getRegToDate(),
				condition.getUpdateUser(),
				condition.getUpdateFromDate(),
				condition.getUpdateToDate(),
				condition.getMonitorFlg(),
				condition.getCollectorFlg(),
				condition.getOwnerRoleId());

		// facilityIdのみJavaで抽出する。
		for(MonitorInfo entity : entityList){
			// facilityId
			if(condition.getFacilityId() != null && !"".equals(condition.getFacilityId()) && entity.getFacilityId() != null){
				// FacilitySelector.getNodeFacilityIdListの第一引数が登録ノード全ての場合は、空リストを返す。そのため、下記のifを追加。
				if (!ReservedFacilityIdConstant.ROOT_SCOPE.equals(entity.getFacilityId())) {
					ArrayList<String> searchIdList = FacilitySelector.getNodeFacilityIdList(entity.getFacilityId(), entity.getOwnerRoleId(), RepositoryControllerBean.ALL, false, true);

					if(!searchIdList.contains(condition.getFacilityId())){
						m_log.debug("getMonitorList() continue : collectorFlg target = " + entity.getFacilityId() + ", filter = " + condition.getFacilityId());
						continue;
					}
				}
			}

			m_log.debug("getMonitorList() add display list : target = " + entity.getMonitorId());
			filterList.add(entity);
		}
		return filterList;
	}

	/**
	 * 指定された監視種別に一致する監視情報一覧を返します。
	 * 
	 * @param monitorType 監視種別
	 * @param ownerRoleId オーナーロールID
	 * @return 監視情報一覧
	 */
	public ArrayList<MonitorInfo> getMonitorListByMonitorType_OR(List<Integer> monitorTypes, String ownerRoleId) {
		return new ArrayList<>(QueryUtil.getMonitorInfoByMonitorType_OR(monitorTypes, ownerRoleId));
	}

	/**
	 * 以下の条件に一致する監視設定一覧を取得します。
	 *　　オーナーロールIDが参照可能
	 *  文字列監視
	 *　　指定されたファシリティIDもしくはその配下のノードに一致する
	 * 
	 * @param facilityId　ファシリティID
	 * @param ownerRoleId オーナーロールID
	 * @return 監視設定ID一覧
	 * @throws ObjectPrivilege_InvalidRole
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public List<MonitorInfo> getStringMonitoInfoListForAnalytics(String facilityId, String ownerRoleId)
		throws HinemosUnknown {
		List<MonitorInfo> list = new ArrayList<>();
		if (facilityId == null || facilityId.isEmpty()
				|| ownerRoleId == null || ownerRoleId.isEmpty()) {
			return list;
		}
		
		// 参照可能な監視設定を取得する
		List<Integer> typeList = new ArrayList<>();
		typeList.add(MonitorTypeConstant.TYPE_STRING);
		List<MonitorInfo> monitorInfoList 
			= new MonitorSettingControllerBean().getMonitorListByMonitorType(typeList, ownerRoleId);
		if (monitorInfoList == null) {
			return list;
		}
		for (MonitorInfo monitorInfo : monitorInfoList) {
			// 指定したファシリティIDをスコープ、もしくはノードに含む場合のみ対象とする
			if (monitorInfo.getFacilityId().equals(facilityId)
					|| new RepositoryControllerBean().getFacilityIdList(
							monitorInfo.getFacilityId(), 0).contains(facilityId)) {
				list.add(monitorInfo);
			}
		}
		return list;
	}

	/**
	 * アプリケーションログにログを出力します。
	 * 
	 * @param index アプリケーションログのインデックス
	 */
	protected void outputLog(String monitorTypeId, String monitorId, int priority, MessageConstant msgCode) {
		String[] args = {monitorTypeId, monitorId };
		AplLogger.put(priority, HinemosModuleConstant.MONITOR, msgCode, args);
	}
}