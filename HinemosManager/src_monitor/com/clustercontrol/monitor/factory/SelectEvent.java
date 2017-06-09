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

package com.clustercontrol.monitor.factory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.bean.CollectGraphFlgConstant;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.bean.EventFilterInfo;
import com.clustercontrol.monitor.bean.ViewListInfo;
import com.clustercontrol.monitor.run.util.EventCache;
import com.clustercontrol.monitor.session.MonitorControllerBean;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.repository.bean.FacilityTargetConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;

/**
 * イベント情報を検索するクラス<BR>
 *
 * @version 3.0.0
 * @since 1.0.0
 */
public class SelectEvent {
	private static Log m_log = LogFactory.getLog( SelectEvent.class );
	
	/**
	 * 表示イベント数（デフォルト値）。<BR>
	 * 監視[イベント]ビューに表示するイベント表示数を格納します。
	 */
	private final static int MAX_DISPLAY_NUMBER = 500;

	/**
	 * イベント情報を取得します。<BR>
	 *
	 * @param monitorId
	 * @param pluginId
	 * @param facilityId
	 * @param outputDate
	 * @return イベント情報
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 *
	 */
	public static EventDataInfo getEventInfo(
			String monitorId,
			String monitorDetailId,
			String pluginId,
			String facilityId,
			Long outputDate) throws MonitorNotFound, InvalidRole {

		EventDataInfo info = null;

		// イベントログ情報を取得
		EventLogEntity event = null;
		try {
			event = QueryUtil.getEventLogPK(monitorId,
					monitorDetailId,
					pluginId,
					outputDate,
					facilityId);
		} catch (EventLogNotFound e) {
			throw new MonitorNotFound(e.getMessage(), e);
		} catch (InvalidRole e) {
			throw e;
		}
		info = new EventDataInfo();
		info.setPriority(event.getPriority());
		if(event.getId().getOutputDate() != null){
			info.setOutputDate(event.getId().getOutputDate());
		}
		if(event.getGenerationDate() != null){
			info.setGenerationDate(event.getGenerationDate());
		}
		info.setPluginId(event.getId().getPluginId());
		info.setMonitorId(event.getId().getMonitorId());
		info.setMonitorDetailId(event.getId().getMonitorDetailId());
		info.setFacilityId(event.getId().getFacilityId());
		info.setScopeText(event.getScopeText());
		info.setApplication(event.getApplication());
		info.setMessage(event.getMessage());
		info.setMessageOrg(event.getMessageOrg());
		info.setConfirmed(event.getConfirmFlg());
		if(event.getConfirmDate() != null){
			info.setConfirmDate(event.getConfirmDate());
		}
		info.setConfirmUser(event.getConfirmUser());
		info.setDuplicationCount(event.getDuplicationCount().intValue());
		info.setComment(event.getComment());
		if(event.getCommentDate() != null) {
			info.setCommentDate(event.getCommentDate());
		}
		info.setCommentUser(event.getCommentUser());
		info.setCollectGraphFlg(event.getCollectGraphFlg());
		info.setOwnerRoleId(event.getOwnerRoleId());
		return info;
	}

	/**
	 * 引数で指定された条件に一致するイベント一覧情報を返します。<BR>
	 * 表示イベント数を越えた場合は、表示イベント数分のイベント情報一覧を返します。
	 * 各イベント情報は、EventLogDataインスタンスとして保持されます。<BR>
	 * 戻り値のViewListInfoは、クライアントにて表示用の形式に変換されます。
	 *
	 * @param facilityId 取得対象の親ファシリティID
	 * @param property 検索条件
	 * @param messages 表示イベント数
	 * @return ビュー一覧情報
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.monitor.bean.EventDataInfo
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityIdList(String, int)
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogBean#ejbFindEvent(String[], Integer, Timestamp, Timestamp, Timestamp, Timestamp, String, String, Integer, boolean, Integer)
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogBean#ejbHomeCountEvent(String[], Integer, Timestamp, Timestamp, Timestamp, Timestamp, String, String, Integer)
	 * @see com.clustercontrol.monitor.bean.EventTabelDefine
	 */
	public ViewListInfo getEventList(String facilityId, EventFilterInfo filter, int messages)
			throws HinemosUnknown {

		ViewListInfo ret = null;

		Integer[] priorityList = null;
		Long outputFromDate = null;
		Long outputToDate = null;
		Long generationFromDate = null;
		Long generationToDate = null;
		String monitorId = null;
		String monitorDetailId = null;
		int facilityType = FacilityTargetConstant.TYPE_ALL;
		String application = null;
		String message = null;
		Integer confirmFlg = ConfirmConstant.TYPE_UNCONFIRMED;
		String confirmUser = null;
		String comment = null;
		String commentUser = null;
		String ownerRoleId = null;
		Boolean collectGraphFlg = CollectGraphFlgConstant.TYPE_ALL;

		String[] facilityIds = null;

		if(filter != null){
			//重要度取得
			if (filter.getPriorityList() != null && filter.getPriorityList().length>0) {
				priorityList = filter.getPriorityList();
			}

			//更新日時（自）取得
			if (filter.getOutputDateFrom() != null) {
				outputFromDate = filter.getOutputDateFrom();
				outputFromDate -= (outputFromDate % 1000);	//ミリ秒の桁を0にする
			}

			//更新日時（至）取得
			if (filter.getOutputDateTo() != null) {
				outputToDate = filter.getOutputDateTo();
				outputToDate += (999 - (outputToDate % 1000));	//ミリ秒の桁を999にする
			}

			//出力日時（自）取得
			if (filter.getGenerationDateFrom() != null) {
				generationFromDate = filter.getGenerationDateFrom();
				generationFromDate -= (generationFromDate % 1000);	//ミリ秒の桁を0にする
			}

			//出力日時（至）取得
			if (filter.getGenerationDateTo() != null) {
				generationToDate = filter.getGenerationDateTo();
				generationToDate += (999 - (generationToDate % 1000));	//ミリ秒の桁を999にする
			}

			//監視項目ID取得
			if (!"".equals(filter.getMonitorId())) {
				monitorId = filter.getMonitorId();
			}

			//監視詳細取得
			if (!"".equals(filter.getMonitorDetailId())) {
				monitorDetailId = filter.getMonitorDetailId();
			}

			//対象ファシリティ種別取得
			if (filter.getFacilityType() != null) {
				facilityType = filter.getFacilityType();
			}

			//アプリケーション取得
			if (!"".equals(filter.getApplication())) {
				application = filter.getApplication();
			}

			//メッセージ取得
			if (!"".equals(filter.getMessage())) {
				message = filter.getMessage();
			}

			// 確認有無取得
			confirmFlg = filter.getConfirmFlgType();
			if (confirmFlg != null && confirmFlg == ConfirmConstant.TYPE_ALL){
				confirmFlg = null;
			}

			// 確認ユーザ取得
			if (!"".equals(filter.getConfirmedUser())) {
				confirmUser = filter.getConfirmedUser();
			}

			//コメント取得
			if (!"".equals(filter.getComment())){
				comment = filter.getComment();
			}

			//コメントユーザ取得
			if (!"".equals(filter.getCommentUser())){
				commentUser = filter.getCommentUser();
			}

			//オーナーロールID取得
			if (!"".equals(filter.getOwnerRoleId())){
				ownerRoleId = filter.getOwnerRoleId();
			}

			// 性能グラフ用フラグ
			collectGraphFlg = filter.getCollectGraphFlg();
		}

		// 対象ファシリティのファシリティIDを取得
		int level = RepositoryControllerBean.ALL;
		if (FacilityTargetConstant.TYPE_BENEATH == facilityType) {
			level = RepositoryControllerBean.ONE_LEVEL;
		}

		ArrayList<String> facilityIdList
		= new RepositoryControllerBean().getFacilityIdList(facilityId, level);

		if (facilityIdList != null && facilityIdList.size() > 0) {
			// スコープの場合
			if (facilityId.equals(RoleSettingTreeConstant.ROOT_ID)) {
				facilityIdList.add("");
			}
			facilityIds = new String[facilityIdList.size()];
			facilityIdList.toArray(facilityIds);
		}
		else {
			// ノードの場合
			facilityIds = new String[1];
			facilityIds[0] = facilityId;
		}

		if(messages <= 0){
			messages = MAX_DISPLAY_NUMBER;
		}
		
		// イベントログ情報一覧を取得
		long start;
		List<EventLogEntity> sqlList = null;
		List<EventLogEntity> cacheList = null;
		
		boolean allSearch = false;
		if (filter != null && filter.getAllSearch() != null) {
			allSearch = filter.getAllSearch();
		}
		String debugMessage = "allSearch=" + allSearch;
		// SQLでログ取得（試験時にキャッシュとSQLの比較をする場合もここを通る。）
		if (allSearch || HinemosPropertyUtil.getHinemosPropertyBool("notify.event.diff", false)) {
			start = HinemosTime.currentTimeMillis();
			List<EventLogEntity> tmp = QueryUtil.getEventLogByFilter(
					facilityIds,
					priorityList,
					outputFromDate,
					outputToDate,
					generationFromDate,
					generationToDate,
					monitorId,
					monitorDetailId,
					application,
					message,
					confirmFlg,
					confirmUser,
					comment,
					commentUser,
					collectGraphFlg,
					ownerRoleId,
					false,
					Integer.valueOf(messages + 1));
			debugMessage += ", sql-search=" + (HinemosTime.currentTimeMillis() - start) +"[ms]";
			sqlList = new ArrayList<>();
			for (EventLogEntity e : tmp) {
				sqlList.add(EventCache.cloneWithoutOrg(e));
			}
			
		}
		
		// キャッシュからログ取得
		if (!allSearch || HinemosPropertyUtil.getHinemosPropertyBool("notify.event.diff", false)) {
			start = HinemosTime.currentTimeMillis();
			cacheList = EventCache.getEventListByCache(
					facilityIdList,
					priorityList == null ? null : Arrays.asList(priorityList),
					outputFromDate,
					outputToDate,
					generationFromDate,
					generationToDate,
					monitorId,
					monitorDetailId,
					application,
					message,
					confirmFlg,
					confirmUser,
					comment,
					commentUser,
					collectGraphFlg,
					ownerRoleId,
					false,
					Integer.valueOf(messages + 1));
			debugMessage += ", cache-search=" + (HinemosTime.currentTimeMillis() - start) + "[ms]";
		}
		m_log.debug(debugMessage); // debug
		
		if (allSearch) {
			ret = collectionToEventList(sqlList, messages);
		} else {
			ret = collectionToEventList(cacheList, messages);
			EventCache.setEventRange(ret);
		}
		
		if (cacheList != null && sqlList != null) {
			diffEventList(cacheList, sqlList);
		}
		
		return ret;
	}

	private static void diffEventList (List<EventLogEntity> cacheList, List<EventLogEntity> sqlList) throws HinemosUnknown {
		if (cacheList == null || sqlList == null) {
			return;
		}
		if (cacheList.size() != sqlList.size()) {
			m_log.info("cacheList.size=" + cacheList.size() + ", sqlList.size=" + sqlList.size());
		} else {
			m_log.debug("cacheList.size=" + cacheList.size() + ", sqlList.size=" + sqlList.size());
		}
		int size = cacheList.size() < sqlList.size() ? cacheList.size() : sqlList.size();
		int count = 0;
		for (int i = 0; i < size; i++) {
			EventLogEntity cache = cacheList.get(i);
			EventLogEntity sql = sqlList.get(i);
			if (!cache.equals(sql)) {
				if (count < 3) {
					// タイミング依存で稀にこのログが出ることがあるが、継続的に出ていたら不具合。
					m_log.warn("diffEventList " + i + " cache=" + cache.getId() + ", sql=" + sql.getId());
				}
				count++;
			}
		}
		String message = "diffEventList count=" + count;
		if (count == 0) {
			m_log.debug(message);
		} else {
			// タイミング依存で稀にこのログが出ることがあるが、継続的に出ていたら不具合。
			m_log.warn(message);
		}
	}

	
	/**
	 * 重要度が最高で受信日時が最新のイベント情報を返します。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたファシリティ配下のファシリティを、指定されたファシリティのターゲットで取得します。</li>
	 * <li>取得したファシリティに属する重要度が最高 かつ 受信日時が最新の未確認のイベント情報を取得し返します。</li>
	 * </ol>
	 *
	 * @param facilityId 取得対象の親ファシリティID
	 * @param level 取得対象のファシリティのターゲット（配下全て／直下のみ）
	 * @param orderFlg ソートの有無
	 * @return イベントのローカルコンポーネントインターフェース
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#ALL
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#ONE_LEVEL
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityIdList(String, int)
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogBean#ejbFindHighPriorityEvent(String[], Integer, Timestamp, Timestamp, Timestamp, Timestamp, String, String, Integer)
	 */
	protected EventLogEntity getHighPriorityEvent(String facilityId, int level, boolean orderFlg)
			throws HinemosUnknown {

		EventLogEntity event = null;

		String[] facilityIds = null;
		if (level == MonitorControllerBean.ONLY) {
			if (facilityId != null && !"".equals(facilityId)) {
				facilityIds = new String[1];
				facilityIds[0] = facilityId;
			} else {
				return null;
			}
		} else {
			// 直下 または 配下すべてのファシリティIDを取得
			ArrayList<String> facilityIdList
			= new RepositoryControllerBean().getFacilityIdList(facilityId,
					level);

			if (facilityIdList != null && facilityIdList.size() > 0) {
				// スコープの場合
				if(facilityId != null){
					facilityIdList.add(facilityId);
				}
				facilityIds = new String[facilityIdList.size()];
				facilityIdList.toArray(facilityIds);
			} else {
				if(facilityId != null){
					// ノードの場合
					facilityIds = new String[1];
					facilityIds[0] = facilityId;
				}
				else{
					// リポジトリが1件も登録されていない場合
					return null;
				}
			}
		}

		// 重要度のリストを取得する
		int[] priorityList = PriorityConstant.PRIORITY_LIST;
		for(int i=0; i<priorityList.length; i++){
			// イベントログ情報一覧から重要度が危険のもので、最近出力されたイベントを取得する。
			List<EventLogEntity> ct = QueryUtil.getEventLogByHighPriorityFilter(
					facilityIds,
					priorityList[i],
					null,
					null,
					null,
					null,
					null,
					null,
					ConfirmConstant.TYPE_UNCONFIRMED,
					null,
					orderFlg);

			// 重要度の高いもの順にループされるため、取得できた場合は、それを返す。
			Iterator<EventLogEntity> itr = ct.iterator();
			// イテレータで参照するが、0件か１件しかない。
			if (itr.hasNext()) {
				event = itr.next();
				return event;
			}
		}

		return event;
	}

	public void deleteEventFile(String filename) {
		String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr("performance.export.dir", 
				HinemosPropertyDefault.getString(HinemosPropertyDefault.StringKey.PERFORMANCE_EXPORT_DIR));
		File file = new File(exportDirectory + "/" + filename);
		if (!file.delete())
			Logger.getLogger(this.getClass()).debug("Fail to delete " + file.getAbsolutePath());
	}

	/**
	 * 引数で指定された条件に一致する帳票出力用のイベント情報一覧を返します。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたプロパティに格納された検索条件を、プロパティユーティリティ（{@link com.clustercontrol.util.PropertyUtil}）を使用して取得します。</li>
	 * <li>引数で指定されたファシリティ配下のファシリティと検索条件を基に、イベント情報を取得します。</li>
	 * <li>１イベント情報を帳票出力用イベント情報（{@link com.clustercontrol.monitor.bean.ReportEventInfo}）にセットします。</li>
	 * <li>この帳票出力用イベント情報を、イベント情報一覧を保持するリスト（{@link ArrayList}）にセットし返します。<BR>
	 * </ol>
	 *
	 * @param facilityId 取得対象の親ファシリティID
	 * @param property 検索条件
	 * @return 帳票出力用イベント情報一覧（{@link com.clustercontrol.monitor.bean.ReportEventInfo}のリスト）
	 * @throws HinemosUnknown
	 * @throws IOException
	 *
	 * @since 2.1.0
	 *
	 * @see com.clustercontrol.util.PropertyUtil#getPropertyValue(com.clustercontrol.bean.Property, java.lang.String)
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityIdList(String, int)
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogBean#ejbFindEvent(String[], Integer, Timestamp, Timestamp, Timestamp, Timestamp, String, String, Integer, boolean, Integer)
	 * @see com.clustercontrol.monitor.bean.ReportEventInfo
	 */
	public DataHandler getEventFile(String facilityId, EventFilterInfo filter, String filename, String username, Locale locale)
			throws HinemosUnknown, IOException {

		Integer[] priorityList = null;
		Long outputFromDate = null;
		Long outputToDate = null;
		Long generationFromDate = null;
		Long generationToDate = null;
		String monitorId = null;
		String monitorDetailId = null;
		int facilityType = 0;
		String facilityTypeStr = "";
		String application = null;
		String message = null;
		Integer confirmFlg = null;
		String confirmStr = "";
		String confirmUser = null;
		String comment = null;
		String commentUser = null;
		String ownerRoleId = null;
		Boolean collectGraphFlg = null;
		String collectGraphStr = "";

		String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr("performance.export.dir",
				HinemosPropertyDefault.getString(HinemosPropertyDefault.StringKey.PERFORMANCE_EXPORT_DIR));
		String filepath = exportDirectory + "/" + filename;
		File file = new File(filepath);
		boolean UTF8_BOM = HinemosPropertyUtil.getHinemosPropertyBool("monitor.common.report.event.bom", true);
		if (UTF8_BOM) {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write( 0xef );
			fos.write( 0xbb );
			fos.write( 0xbf );
			fos.close();
		}
		FileWriter filewriter = new FileWriter(file, true);

		try {
			//重要度取得
			if (filter.getPriorityList() != null && filter.getPriorityList().length>0) {
				priorityList = filter.getPriorityList();
			}

			//更新日時（自）取得
			if (filter.getOutputDateFrom() != null) {
				outputFromDate = filter.getOutputDateFrom();
				outputFromDate -= (outputFromDate % 1000);	//ミリ秒の桁を0にする
			}

			//更新日時（至）取得
			if (filter.getOutputDateTo() != null) {
				outputToDate = filter.getOutputDateTo();
				outputToDate += (999 - (outputToDate % 1000));	//ミリ秒の桁を999にする
			}

			//出力日時（自）取得
			if (filter.getGenerationDateFrom() != null) {
				generationFromDate = filter.getGenerationDateFrom();
				generationFromDate -= (generationFromDate % 1000);	//ミリ秒の桁を0にする
			}

			//出力日時（至）取得
			if (filter.getGenerationDateTo() != null) {
				generationToDate = filter.getGenerationDateTo();
				generationToDate += (999 - (generationToDate % 1000));	//ミリ秒の桁を999にする
			}

			//監視項目ID取得
			if (!"".equals(filter.getMonitorId())) {
				monitorId = filter.getMonitorId();
			}

			//監視詳細取得
			if (!"".equals(filter.getMonitorDetailId())) {
				monitorDetailId = filter.getMonitorDetailId();
			}

			//対象ファシリティ種別取得
			if (filter.getFacilityType() != null) {
				facilityType = filter.getFacilityType();
			}
			facilityTypeStr = Messages.getString(FacilityTargetConstant.typeToMessageCode(facilityType), locale);
			
			//アプリケーション取得
			if (!"".equals(filter.getApplication())) {
				application = filter.getApplication();
			}

			//メッセージ取得
			if (!"".equals(filter.getMessage())) {
				message = filter.getMessage();
			}

			// 確認有無取得
			int confirmFlgType = filter.getConfirmFlgType();
			if (confirmFlgType != -1) {
				confirmFlg = confirmFlgType;
			}
			confirmStr = Messages.getString(ConfirmConstant.typeToMessageCode(confirmFlgType), locale);
			
			// 確認ユーザ
			if (!"".equals(filter.getConfirmedUser())) {
				confirmUser = filter.getConfirmedUser();
			}

			// コメント
			if (!"".equals(filter.getComment())){
				comment = filter.getComment();
			}

			// コメントユーザ
			if (!"".equals(filter.getCommentUser())){
				commentUser = filter.getCommentUser();
			}

			// 性能グラフ用フラグ
			collectGraphFlg = filter.getCollectGraphFlg();
			if (collectGraphFlg == null) {
				collectGraphStr = "";
			} else {
				collectGraphStr = Messages.getString(CollectGraphFlgConstant.typeToMessageCode(collectGraphFlg), locale);
			}
			
			// オーナーロールID
			if (!"".equals(filter.getOwnerRoleId())){
				ownerRoleId = filter.getOwnerRoleId();
			}

			// ヘッダを追記
			/*
			イベント情報,,,,,,,,,,,,,
			出力日時,2012/02/08 17:20:10,,,,,,,,,,,,
			出力ユーザ,Hinemos Administrator,,,,,,,,,,,,
			重要度,受信日時,出力日時,ファシリティID,アプリケーション,オーナーロールID,確認,確認ユーザ,メッセージ,,,,,,
			,,,,,未,,,,,,,,
			 */
			String SEPARATOR = HinemosPropertyUtil.getHinemosPropertyStr("MONITOR_COMMON_REPORT_EVENT_SEPARATOR", ",");
			String DATE_FORMAT = HinemosPropertyUtil.getHinemosPropertyStr("MONITOR_COMMON_REPORT_EVENT_FORMAT",  "yyyy/MM/dd HH:mm:ss");

			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			sdf.setTimeZone(HinemosTime.getTimeZone());
			filewriter.write(Messages.getString("REPORT_TITLE_MONITOR_EVENT", locale) + "\n");
			filewriter.write(Messages.getString("REPORT_OUTPUT_DATE", locale) + SEPARATOR +
					sdf.format(HinemosTime.getDateInstance()) + "\n");
			filewriter.write(Messages.getString("REPORT_OUTPUT_USER", locale) + SEPARATOR + username + "\n");
			
			// フィルタ情報(ヘッダ)
			filewriter.write(
					Messages.getString("DEF_RESULT", locale) + SEPARATOR + // 重要度
					Messages.getString("RECEIVE_TIME", locale) + SEPARATOR +
					Messages.getString("REPORT_OUTPUT_DATE", locale) + SEPARATOR +
					Messages.getString("MONITOR_ID", locale) + SEPARATOR +
					Messages.getString("MONITOR_DETAIL_ID", locale) + SEPARATOR +
					Messages.getString("FACILITY_ID", locale) + SEPARATOR +
					Messages.getString("APPLICATION", locale) + SEPARATOR +
					Messages.getString("CONFIRMED", locale) + SEPARATOR +
					Messages.getString("CONFIRM_USER", locale) + SEPARATOR +
					Messages.getString("MESSAGE", locale) + SEPARATOR +
					Messages.getString("COMMENT", locale) + SEPARATOR +
					Messages.getString("COMMENT_USER", locale) +SEPARATOR +
					Messages.getString("COLLECT_GRAPH_FLG", locale) + SEPARATOR +
					Messages.getString("OWNER_ROLE_ID", locale) +
					"\n");
			
			// 重要度リストの文字列化
			StringBuilder priorityMsg = new StringBuilder();
			if(priorityList != null) {
				for(int i = 0; i<priorityList.length; i++) {
					priorityMsg.append(Messages.getString(PriorityConstant.typeToMessageCode(priorityList[i]), locale) + " ");
				}
			}
			// フィルタ情報
			filewriter.write(
					(priorityList == null ? "" : priorityMsg) + SEPARATOR +
					(outputFromDate == null ? "" : sdf.format(outputFromDate)) + " - " +
					(outputToDate == null ? "" : sdf.format(outputToDate)) + SEPARATOR +
					(generationFromDate == null ? "" : sdf.format(generationFromDate)) + " - " +
					(generationToDate == null ? "" : sdf.format(generationToDate)) + SEPARATOR +
					(monitorId == null ? "" : monitorId) + SEPARATOR +
					(monitorDetailId == null ? "" : monitorDetailId) + SEPARATOR +
					facilityTypeStr  + SEPARATOR +
					(application == null ? "" : application) + SEPARATOR +
					(confirmFlg == null ? "" : confirmStr) + SEPARATOR +
					(confirmUser == null ? "" : confirmUser) + SEPARATOR +
					(message == null ? "" : message) + SEPARATOR +
					(comment == null ? "" : comment) + SEPARATOR +
					(commentUser == null ? "" : commentUser) + SEPARATOR +
					(collectGraphFlg == null ? "" : collectGraphStr) + SEPARATOR +
					(ownerRoleId == null ? "" : ownerRoleId) +
					"\n");
			
			// イベント情報リストのヘッダ情報
			filewriter.write(
					Messages.getString("NUMBER", locale) + SEPARATOR +
					Messages.getString("DEF_RESULT", locale) + SEPARATOR +
					Messages.getString("RECEIVE_TIME", locale) + SEPARATOR +
					Messages.getString("REPORT_OUTPUT_DATE", locale) + SEPARATOR +
					Messages.getString("FACILITY_ID", locale) + SEPARATOR +
					Messages.getString("SCOPE", locale) + SEPARATOR +
					Messages.getString("MONITOR_ID", locale) + SEPARATOR +
					Messages.getString("MONITOR_DETAIL_ID", locale) + SEPARATOR +
					Messages.getString("PLUGIN_ID", locale) + SEPARATOR +
					Messages.getString("APPLICATION", locale) + SEPARATOR +
					Messages.getString("OWNER_ROLE_ID", locale) + SEPARATOR +
					Messages.getString("CONFIRMED", locale) + SEPARATOR +
					Messages.getString("CONFIRM_TIME", locale) + SEPARATOR +
					Messages.getString("CONFIRM_USER", locale) + SEPARATOR +
					Messages.getString("COMMENT", locale) + SEPARATOR +
					Messages.getString("COMMENT_DATE", locale) + SEPARATOR +
					Messages.getString("COMMENT_USER", locale) + SEPARATOR +
					Messages.getString("MESSAGE", locale) + SEPARATOR +
					Messages.getString("MESSAGE_ORG", locale) + SEPARATOR +
					Messages.getString("COLLECT_GRAPH_FLG", locale) + "\n");

			// 対象ファシリティのファシリティIDを取得
			String[] facilityIds = null;

			int level = RepositoryControllerBean.ALL;
			if (FacilityTargetConstant.TYPE_BENEATH == facilityType) {
				level = RepositoryControllerBean.ONE_LEVEL;
			}

			ArrayList<String> facilityIdList
			= new RepositoryControllerBean().getFacilityIdList(facilityId, level);

			if (facilityIdList != null && facilityIdList.size() > 0) {
				// スコープの場合
				facilityIds = new String[facilityIdList.size()];
				facilityIdList.toArray(facilityIds);
			}
			else {
				// ノードの場合
				facilityIds = new String[1];
				facilityIds[0] = facilityId;
			}
			
			// 全範囲検索はCSVファイル出力の場合は加味せずDBより全件取得する。

			List<EventLogEntity> ct = null;
			// イベントログ情報一覧を取得
			ct = QueryUtil.getEventLogByFilter(
					facilityIds,
					priorityList,
					outputFromDate,
					outputToDate,
					generationFromDate,
					generationToDate,
					monitorId,
					monitorDetailId,
					application,
					message,
					confirmFlg,
					confirmUser,
					comment,
					commentUser,
					collectGraphFlg,
					ownerRoleId,
					true,
					HinemosPropertyUtil.getHinemosPropertyNum("monitor.common.report.event.count", Long.valueOf(2000)).intValue());

			// 帳票出力用に変換
			collectionToFile(ct, filewriter, locale);

		} finally {
			filewriter.close();
		}

		// リストをファイルに書き出し。
		FileDataSource source = new FileDataSource(file);
		DataHandler handler = new DataHandler(source);

		return handler;
	}

	/**
	 * DBより取得したイベント情報をイベント一覧情報に格納します。
	 * <p>
	 * <ol>
	 * <li>１イベント情報をEventLogDataのインスタンスとし、イベント情報一覧を保持するリスト（{@link ArrayList}）に格納します。<BR>
	 * <li>イベント情報一覧を、引数で指定されたビュー一覧情報（{@link com.clustercontrol.monitor.bean.ViewListInfo}）にセットします。</li>
	 * </ol>
	 * <p>
	 * また、イベント数を重要度毎にカウントし、
	 * 表示イベント数よりもイベント数が少ない場合は、重要度別イベント数を引数で指定されたビュー一覧情報にセットします。
	 *
	 * @param ct イベント情報取得結果
	 * @param eventList ビュー一覧情報
	 * @param messages イベント最大表示件数
	 *
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogData
	 * @see com.clustercontrol.monitor.bean.EventTabelDefine
	 */
	private ViewListInfo collectionToEventList(Collection<EventLogEntity> ct, int messages) {
		int critical = 0;
		int warning = 0;
		int info = 0;
		int unknown = 0;

		ViewListInfo viewListInfo = new ViewListInfo();
		ArrayList<EventDataInfo> list = new ArrayList<EventDataInfo>();

		for (EventLogEntity event : ct) {
			list.add(getEventDataInfo(event));

			//最大表示件数以下の場合
			if(event.getPriority().intValue() == PriorityConstant.TYPE_CRITICAL)
				critical++;
			else if(event.getPriority().intValue() == PriorityConstant.TYPE_WARNING)
				warning++;
			else if(event.getPriority().intValue() == PriorityConstant.TYPE_INFO)
				info++;
			else if(event.getPriority().intValue() == PriorityConstant.TYPE_UNKNOWN)
				unknown++;

			//取得したイベントを最大表示件数まで格納したら終了
			if(list.size() >= messages)
				break;
		}

		//イベント数を設定
		viewListInfo.setCritical(critical);
		viewListInfo.setWarning(warning);
		viewListInfo.setInfo(info);
		viewListInfo.setUnKnown(unknown);
		viewListInfo.setTotal(ct.size());

		viewListInfo.setEventList(list);

		return viewListInfo;
	}

	public static EventDataInfo getEventDataInfo (EventLogEntity event) {

		EventDataInfo eventInfo = new EventDataInfo();
		eventInfo.setPriority(event.getPriority());
		if (event.getId().getOutputDate() != null) {
			eventInfo.setOutputDate(event.getId().getOutputDate());
		}
		if (event.getGenerationDate() != null) {
			eventInfo.setGenerationDate(event.getGenerationDate());
		}
		eventInfo.setPluginId(event.getId().getPluginId());
		eventInfo.setMonitorId(event.getId().getMonitorId());
		eventInfo.setMonitorDetailId(event.getId().getMonitorDetailId());
		eventInfo.setFacilityId(event.getId().getFacilityId());
		eventInfo.setScopeText(event.getScopeText());
		eventInfo.setApplication(event.getApplication());
		eventInfo.setMessage(event.getMessage());
		eventInfo.setConfirmed(event.getConfirmFlg());
		eventInfo.setConfirmUser(event.getConfirmUser());
		eventInfo.setComment(event.getComment());
		if (event.getCommentDate() != null ) {
			eventInfo.setCommentDate(event.getCommentDate());
		}
		eventInfo.setCommentUser(event.getCommentUser());
		eventInfo.setCollectGraphFlg(event.getCollectGraphFlg());
		eventInfo.setOwnerRoleId(event.getOwnerRoleId());
		
		return eventInfo;
	}
	
	/**
	 * DBより取得したイベント情報を帳票出力用イベント情報一覧に格納します。
	 *
	 * @param ct イベント情報取得結果
	 * @return 帳票出力用イベント情報一覧
	 *
	 * @version 2.1.0
	 * @throws IOException
	 * @since 2.1.0
	 */
	private void collectionToFile(Collection<EventLogEntity> ct, FileWriter filewriter, Locale locale) throws IOException {

		int n = 0;
		String SEPARATOR = HinemosPropertyUtil.getHinemosPropertyStr("monitor.common.report.event.separator", ",");
		
		for (EventLogEntity event : ct) {
			n ++;
			filewriter.write(
					getDoubleQuote(String.valueOf(n)) + SEPARATOR +
					getDoubleQuote(Messages.getString(PriorityConstant.typeToMessageCode(event.getPriority()), locale)) + SEPARATOR +
					getDoubleQuote(l2s(event.getId().getOutputDate())) + SEPARATOR +
					getDoubleQuote(l2s(event.getGenerationDate())) + SEPARATOR +
					getDoubleQuote(event.getId().getFacilityId()) + SEPARATOR +
					getDoubleQuote(event.getScopeText()) + SEPARATOR +
					getDoubleQuote(event.getId().getMonitorId()) + SEPARATOR +
					getDoubleQuote(event.getId().getMonitorDetailId()) + SEPARATOR +
					getDoubleQuote(event.getId().getPluginId()) + SEPARATOR +
					getDoubleQuote(HinemosMessage.replace(event.getApplication(), locale)) + SEPARATOR +
					getDoubleQuote(event.getOwnerRoleId()) + SEPARATOR +
					getDoubleQuote(Messages.getString(ConfirmConstant.typeToMessageCode(event.getConfirmFlg().intValue()), locale)) + SEPARATOR +
					getDoubleQuote(l2s(event.getConfirmDate())) + SEPARATOR +
					getDoubleQuote(event.getConfirmUser()) + SEPARATOR +
					getDoubleQuote(event.getComment()) + SEPARATOR +
					getDoubleQuote(l2s(event.getCommentDate())) + SEPARATOR +
					getDoubleQuote(event.getCommentUser()) + SEPARATOR +
					getDoubleQuote(HinemosMessage.replace(event.getMessage(), locale)) + SEPARATOR +
					getDoubleQuote(HinemosMessage.replace(event.getMessageOrg(), locale)) + SEPARATOR +
					getDoubleQuote(Messages.getString(CollectGraphFlgConstant.typeToMessageCode(event.getCollectGraphFlg()), locale)) +
					"\n");
		}
	}

	/**
	 * Long型のエポックミリ秒を日付型に整形する。
	 * @param t
	 * @return
	 */
	private String l2s(Long l) {
		if (l == null) {
			return "";
		}
		// 日付フォーマットおよびタイムゾーンの設定
		String DATE_FORMAT = HinemosPropertyUtil.getHinemosPropertyStr("monitor.common.report.event.format",  "yyyy/MM/dd HH:mm:ss");
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		sdf.setTimeZone(HinemosTime.getTimeZone());
		return sdf.format(new Date(l));
	}

	/**
	 * メッセージやオリジナルメッセージに改行が含まれている場合や、
	 * 「"」が含まれている場合はMS Excelで読もうとするとおかしくなる。
	 * 改行等が含まれる可能性のある箇所は下記を利用すること。
	 */
	private String getDoubleQuote(String in) {
		if (in == null) {
			return "";
		}
		return "\"" + in.replace("\"", "\"\"") + "\"";
	}
}
