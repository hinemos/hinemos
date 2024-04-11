/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.factory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.filtersetting.bean.EventFilterBaseInfo;
import com.clustercontrol.filtersetting.bean.EventFilterConditionInfo;
import com.clustercontrol.monitor.bean.CollectGraphFlgConstant;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.EventNoDisplayInfo;
import com.clustercontrol.monitor.bean.EventSelectionInfo;
import com.clustercontrol.monitor.bean.EventUserExtensionItemInfo;
import com.clustercontrol.monitor.bean.ViewListInfo;
import com.clustercontrol.monitor.run.bean.CollectMonitorDisplayNameConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.EventCache;
import com.clustercontrol.monitor.run.util.EventUtil;
import com.clustercontrol.monitor.session.MonitorControllerBean;
import com.clustercontrol.monitor.util.EventHinemosPropertyUtil;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.model.EventLogOperationHistoryEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.repository.bean.FacilityTargetConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rest.util.RestTempFileType;
import com.clustercontrol.rest.util.RestTempFileUtil;
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
	 * @param monitorDetailId
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
		
		//イベント操作履歴情報を取得
		List<EventLogOperationHistoryEntity> history = null;
		
		history = QueryUtil.getEventLogOperationHistoryListByEventLogPK(
				monitorId,
				monitorDetailId,
				pluginId,
				outputDate,
				facilityId);
		
		info = new EventDataInfo();
		copyEventEntityToEventData(event, info, history);
		return info;
	}

	/**
	 * イベント情報をコピーします。
	 * 
	 * @param event
	 * @param info
	 */
	private static void copyEventEntityToEventData(EventLogEntity event, EventDataInfo info, List<EventLogOperationHistoryEntity> history) {
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
		for (int i = 0; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			EventUtil.setUserItemValue(info, i, EventUtil.getUserItemValue(event, i));
		}
		info.setNotifyUUID(event.getNotifyUUID());
		info.setPosition(event.getPosition());
		info.setEventLogHitory(history);
	}
	
	/**
	 * 引数で指定された条件に一致するイベント一覧情報を返します。<BR>
	 * 表示イベント数を越えた場合は、表示イベント数分のイベント情報一覧を返します。
	 * 各イベント情報は、EventLogDataインスタンスとして保持されます。<BR>
	 * 戻り値のViewListInfoは、クライアントにて表示用の形式に変換されます。
	 *
	 * @param filter 検索条件
	 * @param messages 表示イベント数
	 * @return ビュー一覧情報
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.monitor.bean.EventDataInfo
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityIdList(String, int)
	 */
	public ViewListInfo getEventList(EventFilterBaseInfo filter, int messages)
			throws HinemosUnknown {

		ViewListInfo ret = null;

		// 現在の呼び出し階層では null が渡ることはないはずだが念のため
		if (filter == null) {
			filter = EventFilterBaseInfo.ofClientViewDefault();
		}
		
		if(messages <= 0){
			messages = MAX_DISPLAY_NUMBER;
		}
		
		// イベントログ情報一覧を取得
		long start;
		List<EventLogEntity> sqlList = null;
		List<EventLogEntity> cacheList = null;
		
		boolean allSearch = filter.getEntire().booleanValue();
		String debugMessage = "allSearch=" + allSearch;

		// SQLでログ取得（試験時にキャッシュとSQLの比較をする場合もここを通る。）
		if (allSearch || HinemosPropertyCommon.notify_event_diff.getBooleanValue()) {
			start = HinemosTime.currentTimeMillis();
			List<EventLogEntity> tmp = QueryUtil.getEventLogByFilter(
				filter,
				false,
				Integer.valueOf(messages + 1));
			debugMessage += ", sql-search=" + (HinemosTime.currentTimeMillis() - start) +"[ms]";
			sqlList = new ArrayList<>();
			for (EventLogEntity e : tmp) {
				sqlList.add(EventCache.cloneEntity(e));
			}
			
		}
		
		// キャッシュからログ取得
		if (!allSearch || HinemosPropertyCommon.notify_event_diff.getBooleanValue()) {
			start = HinemosTime.currentTimeMillis();
			cacheList = EventCache.getEventListByCache(filter, false, Integer.valueOf(messages + 1));
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

	private static void diffEventList(List<EventLogEntity> cacheList, List<EventLogEntity> sqlList) {
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
		String exportDirectory = HinemosPropertyDefault.performance_export_dir.getStringValue();
		File file = new File(exportDirectory + "/" + filename);
		if (!file.delete())
			Logger.getLogger(this.getClass()).debug("Fail to delete " + file.getAbsolutePath());
	}

	/**
	 * 引数で指定された条件に一致する帳票ファイルのデータハンドラを返します。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたファシリティ配下のファシリティと検索条件を基に、イベント情報を取得します。</li>
	 * <li>取得したイベント情報をファイルに出力します。</li>
	 * <li>出力したファイルのデータハンドラを返します。<BR>
	 * </ol>
	 *
	 * @param filter 検索条件 (選択されているイベントがある場合は無視)
	 * @param selectedEvents 選択されているイベント(null なら無選択)
	 * @param filename 出力ファイル名
	 * @param username ユーザID
	 * @param locale ロケール
	 * 
	 * @return 出力ファイルのデータハンドラ
	 */
	public File getEventFile(EventFilterBaseInfo filter, List<EventSelectionInfo> selectedEvents, String filename, String username, Locale locale)
			throws IOException {

		Path path = RestTempFileUtil.createTempDirectory(RestTempFileType.EVENT);
		File file = path.resolve(filename).toFile();

		boolean UTF8_BOM = HinemosPropertyCommon.monitor_common_report_event_bom.getBooleanValue();
		if (UTF8_BOM) {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write( 0xef );
			fos.write( 0xbb );
			fos.write( 0xbf );
			fos.close();
		}
		FileWriter filewriter = new FileWriter(file, true);

		try {
			Map<Integer, EventUserExtensionItemInfo> userItemMap = SelectEventHinemosProperty.getEventUserExtensionItemInfo();
			EventNoDisplayInfo eventNoInfo = SelectEventHinemosProperty.geEventNoDisplayInfo();
		
			boolean isSelectionAvailable = selectedEvents != null && selectedEvents.size() > 0;
			
			// 全範囲検索はCSVファイル出力の場合は加味せずDBより全件取得する。
			List<EventLogEntity> ct = null;
			if (!isSelectionAvailable) {
				// --- フィルタで検索の時
				if (filter == null) {
					filter = EventFilterBaseInfo.ofDownloadDefault();
				}
				// ヘッダ情報を出力
				headerToFile(filewriter, filter, username, isSelectionAvailable, locale, userItemMap, eventNoInfo);
				// イベント履歴をクエリして取得
				ct = QueryUtil.getEventLogByFilter(
						filter,
						true,
						HinemosPropertyCommon.monitor_common_report_event_count.getIntegerValue());
			} else {
				// --- 選択したイベントの時
				// ヘッダ情報を出力
				headerToFile(filewriter, null, username, isSelectionAvailable, locale, userItemMap, eventNoInfo);
				// 選択されているイベント履歴の詳細をひとつひとつ取得
				ct = new ArrayList<>();
				for (EventSelectionInfo key : selectedEvents) {
					try {
						EventLogEntity event = QueryUtil.getEventLogPK(
									key.getMonitorId(), key.getMonitorDetailId(),
									key.getPluginId(), key.getOutputDate(), key.getFacilityId());
						ct.add(event);
					} catch (EventLogNotFound e) {
						//実行前と、実行時点でイベントの状態に変更があった場合なので、エラーにしない
						m_log.info("event not found", e);
					} catch(InvalidRole e) {
						//実行前と、実行時点でユーザの権限に変更があった場合なので、エラーにしない
						m_log.info("invalid role", e);
					}
				}
			}

			// 帳票出力用に変換
			collectionToFile(ct, filewriter, locale, userItemMap, eventNoInfo);

		} finally {
			filewriter.close();
		}

		return file;
	}
	
	private static String convObject(Object obj) {
		if (obj == null) {
			return "";
		}
		return obj.toString();
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
		EventUtil.copyEventLogEntityToEventDataInfo(event, eventInfo);
		
		// 監視設定
		if (event.getId().getPluginId().startsWith("MON_")) {
			// 変化量
			if (event.getId().getMonitorDetailId().startsWith(CollectMonitorDisplayNameConstant.CHANGE_MONITOR_DETAIL_PREFIX)) {
				eventInfo.setParentMonitorDetailId(
						event.getId().getMonitorDetailId().replace(CollectMonitorDisplayNameConstant.CHANGE_MONITOR_DETAIL_PREFIX, ""));
			}
			// 将来予測
			if (event.getId().getMonitorDetailId().startsWith(CollectMonitorDisplayNameConstant.PREDICTION_MONITOR_DETAIL_PREFIX)) {
				eventInfo.setParentMonitorDetailId(
						event.getId().getMonitorDetailId().replace(CollectMonitorDisplayNameConstant.PREDICTION_MONITOR_DETAIL_PREFIX, ""));
				if (event.getGenerationDate() != null) {
					try {
						MonitorInfo monitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_NONE(event.getId().getMonitorId());
						if (monitorInfo.getPredictionTarget() != null) {
							eventInfo.setPredictGenerationDate(event.getGenerationDate() + monitorInfo.getPredictionTarget().longValue() * 60 * 1000);
						}
					} catch (MonitorNotFound e) {
						m_log.warn("monitorInfo is not found. : monitorId=" + event.getId().getMonitorId());
					}
				}
			}
		}
		
		return eventInfo;
	}
	
	
	
	
	/**
	 * イベントCSVファイルのヘッダを出力する
	 * <pre>
	 * イベント情報,,,,,,,,,,,,,
	 * 出力日時,2012/02/08 17:20:10,,,,,,,,,,,,
	 * 出力ユーザ,Hinemos Administrator,,,,,,,,,,,,
	 * 重要度,受信日時,出力日時,ファシリティID,アプリケーション,オーナーロールID,確認,確認ユーザ,メッセージ,,,,,,
	 * ,,,,,未,,,,,,,,
	 * 
	 * </pre>
	 * 
	 * @param filewriter
	 * @param filter
	 * @param username
	 * @param isSelectEvent
	 * @param locale
	 * @param userItemMap
	 * @param eventNoInfo
	 * @throws IOException
	 */
	private void headerToFile(FileWriter filewriter, EventFilterBaseInfo filter2, 
			String username, boolean isSelectEvent, Locale locale,
			Map<Integer, EventUserExtensionItemInfo> userItemMap, EventNoDisplayInfo eventNoInfo
			) throws IOException {
		
		final String CR = "\n";
		final String SEPARATOR = HinemosPropertyCommon.monitor_common_report_event_separator.getStringValue();
		
		StringJoiner csvData = null;
		
		filewriter.write(Messages.getString("REPORT_TITLE_MONITOR_EVENT", locale) + CR);
		filewriter.write(
				Messages.getString("REPORT_OUTPUT_DATE", locale) + SEPARATOR +
				l2s(HinemosTime.getDateInstance().getTime()) + CR);
		filewriter.write(Messages.getString("REPORT_OUTPUT_USER", locale) + SEPARATOR + 
				username + CR);
		
		if (isSelectEvent) {
			filewriter.write(Messages.getString("MESSAGE_MONITOR_EVENT_EXPORT_BY_SELECT", locale) + CR);
		}
		
		// フィルタ情報(ヘッダ)
		csvData = new StringJoiner(SEPARATOR);
		csvData.add(Messages.getString("DEF_RESULT", locale));
		csvData.add(Messages.getString("RECEIVE_TIME", locale));
		csvData.add(Messages.getString("REPORT_OUTPUT_DATE", locale));
		csvData.add(Messages.getString("MONITOR_ID", locale));
		csvData.add(Messages.getString("MONITOR_DETAIL_ID", locale));
		csvData.add(Messages.getString("FACILITY_ID", locale));
		csvData.add(Messages.getString("APPLICATION", locale));
		csvData.add(Messages.getString("CONFIRMED", locale));
		csvData.add(Messages.getString("CONFIRM_USER", locale));
		csvData.add(Messages.getString("MESSAGE", locale));
		csvData.add(Messages.getString("COMMENT", locale));
		csvData.add(Messages.getString("COMMENT_USER", locale));
		csvData.add(Messages.getString("COLLECT_GRAPH_FLG", locale));
		csvData.add(Messages.getString("OWNER_ROLE_ID", locale));
		csvData.add(Messages.getString("NOTIFY_UUID", locale));
		for (int i = 1 ; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			if (userItemMap.get(i).getDisplayEnable()) {
				csvData.add(EventHinemosPropertyUtil.getDisplayName(userItemMap.get(i).getDisplayName(), i));
			}
		}
		
		if (eventNoInfo.getDisplayEnable()) {
			csvData.add(Messages.getString("EVENT_NO", locale));
		}
		
		filewriter.write(csvData.toString() + CR);
		
		// フィルタ情報(フィルタ内容)
		if (filter2 != null) {
			for (EventFilterConditionInfo filter : filter2.getConditions()) {
				csvData = new StringJoiner(SEPARATOR);

				// 重要度リストの文字列化
				StringBuilder priorityMsg = new StringBuilder();
				for (Integer priority : filter.getPriorityCodes()) {
					priorityMsg.append(
							Messages.getString(PriorityConstant.typeToMessageCode(priority), locale) + " ");
				}
				// 確認リストの文字列化
				StringBuilder confirmMsg = new StringBuilder();
				for (Integer conirmFlg : filter.getConfirmFlagCodes()) {
					confirmMsg.append(
							Messages.getString(ConfirmConstant.typeToMessageCode(conirmFlg), locale) + " ");
				}

				//性能グラフの文字列化
				String collectGraphStr = "";
				if (filter.getGraphFlag() != null) {
					collectGraphStr = Messages.getString(CollectGraphFlgConstant.typeToMessageCode(filter.getGraphFlag()), locale);
				}

				int facilityType = 0;
				String facilityTypeStr = "";
				//対象ファシリティ種別取得
				if (filter2.getFacilityTarget() != null) {
					facilityType = filter2.getFacilityTarget().getCode();
					facilityTypeStr = Messages.getString(FacilityTargetConstant.typeToMessageCode(facilityType), locale);
				}

				csvData.add(priorityMsg);
				csvData.add(l2s(filter.getOutputDateFrom()) + " - " + l2s(filter.getOutputDateTo()));
				csvData.add(l2s(filter.getGenerationDateFrom()) + " - " + l2s(filter.getGenerationDateTo()));
				csvData.add(convObject(filter.getMonitorId()));
				csvData.add(convObject(filter.getMonitorDetail()));
				csvData.add(facilityTypeStr);
				csvData.add(convObject(filter.getApplication()));
				csvData.add(confirmMsg);
				csvData.add(convObject(filter.getConfirmUser()));
				csvData.add(convObject(filter.getMessage()));
				csvData.add(convObject(filter.getComment()));
				csvData.add(convObject(filter.getCommentUser()));
				csvData.add(collectGraphStr);
				csvData.add(convObject(filter.getOwnerRoleId()));
				csvData.add(convObject(filter.getNotifyUUID()));
				for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
					if (userItemMap.get(i).getDisplayEnable()) {
						csvData.add(convObject(filter.getUserItem(i)));
					}
				}
				if (eventNoInfo.getDisplayEnable()) {
					csvData.add(convObject(filter.getPositionFrom()) + " - " + convObject(filter.getPositionTo()));
				}
				filewriter.write(csvData.toString() + CR);
			}
		}

		// イベント情報リストのヘッダ情報
		csvData = new StringJoiner(SEPARATOR);
		csvData.add(Messages.getString("NUMBER", locale));
		csvData.add(Messages.getString("DEF_RESULT", locale));
		csvData.add(Messages.getString("RECEIVE_TIME", locale));
		csvData.add(Messages.getString("REPORT_OUTPUT_DATE", locale));
		csvData.add(Messages.getString("FACILITY_ID", locale));
		csvData.add(Messages.getString("SCOPE", locale));
		csvData.add(Messages.getString("MONITOR_ID", locale));
		csvData.add(Messages.getString("MONITOR_DETAIL_ID", locale));
		csvData.add(Messages.getString("PLUGIN_ID", locale));
		csvData.add(Messages.getString("APPLICATION", locale));
		csvData.add(Messages.getString("OWNER_ROLE_ID", locale));
		csvData.add(Messages.getString("CONFIRMED", locale));
		csvData.add(Messages.getString("CONFIRM_TIME", locale));
		csvData.add(Messages.getString("CONFIRM_USER", locale));
		csvData.add(Messages.getString("COMMENT", locale));
		csvData.add(Messages.getString("COMMENT_DATE", locale));
		csvData.add(Messages.getString("COMMENT_USER", locale));
		csvData.add(Messages.getString("MESSAGE", locale));
		csvData.add(Messages.getString("MESSAGE_ORG", locale));
		csvData.add(Messages.getString("COLLECT_GRAPH_FLG", locale));
		csvData.add(Messages.getString("NOTIFY_UUID", locale));
		
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			if (userItemMap.get(i).getExportEnable()) {
				csvData.add(EventHinemosPropertyUtil.getDisplayName(userItemMap.get(i).getDisplayName(), i));
			}
		}
		
		if (eventNoInfo.getExportEnable()) {
			csvData.add(Messages.getString("EVENT_NO", locale));
		}
		
		filewriter.write(csvData.toString() + CR);
	}
	
	/**
	 * DBより取得したイベント情報をファイルに追記します。
	 *
	 * @param ct イベント情報取得結果
	 * @param filewriter ファイルオブジェクト
	 * @param locale ロケール
	 * @Param userItemMap ユーザ項目表示情報
	 * @Param eventNoInfo イベント番号表示情報
	 *
	 * @version 2.1.0
	 * @throws IOException
	 * @since 2.1.0
	 */
	private void collectionToFile(Collection<EventLogEntity> ct, FileWriter filewriter, 
			Locale locale, Map<Integer, EventUserExtensionItemInfo> userItemMap, EventNoDisplayInfo eventNoInfo) throws IOException {
		
		int n = 0;
		
		final String CR = "\n";
		final String SEPARATOR = HinemosPropertyCommon.monitor_common_report_event_separator.getStringValue();
		
		StringJoiner csvData = null;
		
		for (EventLogEntity event : ct) {
			csvData = new StringJoiner(SEPARATOR);
			n++;
			
			csvData.add(getDoubleQuote(String.valueOf(n)) );
			csvData.add(getDoubleQuote(Messages.getString(PriorityConstant.typeToMessageCode(event.getPriority()), locale)) );
			csvData.add(getDoubleQuote(l2s(event.getId().getOutputDate())) );
			csvData.add(getDoubleQuote(l2s(event.getGenerationDate())) );
			csvData.add(getDoubleQuote(event.getId().getFacilityId()) );
			csvData.add(getDoubleQuote(event.getScopeText()) );
			csvData.add(getDoubleQuote(event.getId().getMonitorId()) );
			csvData.add(getDoubleQuote(event.getId().getMonitorDetailId()) );
			csvData.add(getDoubleQuote(event.getId().getPluginId()) );
			csvData.add(getDoubleQuote(HinemosMessage.replace(event.getApplication(), locale)) );
			csvData.add(getDoubleQuote(event.getOwnerRoleId()) );
			csvData.add(getDoubleQuote(Messages.getString(ConfirmConstant.typeToMessageCode(event.getConfirmFlg().intValue()), locale)) );
			csvData.add(getDoubleQuote(l2s(event.getConfirmDate())) );
			csvData.add(getDoubleQuote(event.getConfirmUser()) );
			csvData.add(getDoubleQuote(event.getComment()) );
			csvData.add(getDoubleQuote(l2s(event.getCommentDate())) );
			csvData.add(getDoubleQuote(event.getCommentUser()) );
			csvData.add(getDoubleQuote(HinemosMessage.replace(event.getMessage(), locale)) );
			csvData.add(getDoubleQuote(HinemosMessage.replace(event.getMessageOrg(), locale)) );
			csvData.add(getDoubleQuote(Messages.getString(CollectGraphFlgConstant.typeToMessageCode(event.getCollectGraphFlg()), locale)) );
			csvData.add(getDoubleQuote(event.getNotifyUUID()));
			
			for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
				if (userItemMap.get(i).getExportEnable()) {
					csvData.add(getDoubleQuote(EventUtil.getUserItemValue(event, i)));
				}
			}

			if (eventNoInfo.getExportEnable()) {
				csvData.add(getDoubleQuote(convObject(event.getPosition())));
			}
			filewriter.write(csvData.toString() + CR);
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
		String DATE_FORMAT = HinemosPropertyCommon.monitor_common_report_event_format.getStringValue();
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
