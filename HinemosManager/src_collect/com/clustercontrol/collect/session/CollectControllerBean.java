package com.clustercontrol.collect.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.collect.factory.SelectCollectData;
import com.clustercontrol.collect.factory.SelectCollectKeyInfo;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.collect.util.ExportCollectDataFile;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CollectKeyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.factory.SelectEvent;
import com.clustercontrol.notify.monitor.model.EventLogEntity;


/**
*
* <!-- begin-user-doc --> 収集情報の制御を行うsession bean <!-- end-user-doc --> *
*
*/
public class CollectControllerBean{

	private static Log m_log = LogFactory.getLog( CollectControllerBean.class );
	
	/**
	 * collectoridを取得します。<BR>
	 *
	 *
	 * @return collectorid
	 * @throws InvalidRole 
	 * @throws HinemosUnknown
	 */
	public Integer getCollectId(String itemName, String displayName, String monitorId, String facilityId) throws InvalidRole, HinemosUnknown{
		JpaTransactionManager jtm = null;
		Integer collectorid = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCollectKeyInfo select = new SelectCollectKeyInfo();
			collectorid = select.getCollectId(itemName, displayName, monitorId, facilityId);
			jtm.commit();
		} catch (InvalidRole | MonitorNotFound | CollectKeyNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("getCollectId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return collectorid;
	}
	
	
	/**
	 * IDと時間を指定し、その時間内の収集データのリストを取得します。<BR>
	 *
	 *
	 * @return CollectDataのリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<CollectData> getCollectDataList(List<Integer> idList, Long fromTime, Long toTime) throws InvalidRole, HinemosUnknown{
		JpaTransactionManager jtm = null;
		List<CollectData> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCollectData select = new SelectCollectData();
			list = select.getCollectDataList(idList, fromTime, toTime);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("getCollectId() : "
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
	 * IDを指定し、IDに紐づく収集データのリストを取得します。<BR>
	 *
	 *
	 * @return CollectDataのリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<CollectData> getCollectDataList(Integer id) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<CollectData> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCollectData select = new SelectCollectData();
			list = select.getCollectDataList(id);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("getCollectId() : "
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
	 * IDと時間を指定し、その時間内のサマリデータ(時)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryHourのリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<SummaryHour> getSummaryHourList(List<Integer> idList, Long fromTime, Long toTime) throws InvalidRole, HinemosUnknown{
		JpaTransactionManager jtm = null;
		List<SummaryHour> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCollectData select = new SelectCollectData();
			list = select.getSummaryHourList(idList, fromTime, toTime);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("getCollectId() : "
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
	 * IDを指定し、サマリデータ(時)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryHourのリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<SummaryHour> getSummaryHourList(Integer id) throws InvalidRole, HinemosUnknown{
		JpaTransactionManager jtm = null;
		List<SummaryHour> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCollectData select = new SelectCollectData();
			list = select.getSummaryHourList(id);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("getCollectId() : "
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
	 * IDと時間を指定し、その時間内のサマリデータ(日)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryDayのリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<SummaryDay> getSummaryDayList(List<Integer> idList, Long fromTime, Long toTime) throws HinemosUnknown, InvalidRole{
		JpaTransactionManager jtm = null;
		List<SummaryDay> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCollectData select = new SelectCollectData();
			list = select.getSummaryDayList(idList, fromTime, toTime);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("getCollectId() : "
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
	 * IDを指定し、その時間内のサマリデータ(日)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryDayのリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<SummaryDay> getSummaryDayList(Integer id) throws HinemosUnknown, InvalidRole{
		JpaTransactionManager jtm = null;
		List<SummaryDay> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCollectData select = new SelectCollectData();
			list = select.getSummaryDayList(id);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("getCollectId() : "
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
	 * IDと時間を指定し、その時間内のサマリデータ(月)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryMonthのリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<SummaryMonth> getSummaryMonthList(List<Integer> idList, Long fromTime, Long toTime) throws InvalidRole, HinemosUnknown{
		JpaTransactionManager jtm = null;
		List<SummaryMonth> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCollectData select = new SelectCollectData();
			list = select.getSummaryMonthList(idList, fromTime, toTime);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("getCollectId() : "
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
	 * IDを指定し、サマリデータ(月)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryMonthのリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<SummaryMonth> getSummaryMonthList(Integer id) throws InvalidRole, HinemosUnknown{
		JpaTransactionManager jtm = null;
		List<SummaryMonth> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCollectData select = new SelectCollectData();
			list = select.getSummaryMonthList(id);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("getCollectId() : "
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
	 * 収集項目コードのリストを取得します。<BR>
	 *
	 *
	 * @return 収集項目コードのリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<CollectKeyInfoPK> getItemCode(List<String> facilityIdList) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<CollectKeyInfoPK> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCollectKeyInfo select = new SelectCollectKeyInfo();
			list = select.getCollectKeyList(facilityIdList);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.warn("getCollectId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("getCollectId() : "
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
	 * 性能実績データファイル(csv形式)に出力するファイルパスのリストを返却する。
	 * このメソッドは作成するファイル名を返却し、CSV出力処理は別スレッドで動作する。
	 * 
	 * @param monitorId 監視項目ID
	 * @param facilityId ファシリティID(ノードorスコープ)
	 * @param header ヘッダをファイルに出力するか否か
	 * @param archive ファイルをアーカイブするか否か
	 * 
	 * @return Hinemos マネージャサーバ上に出力されたファイル名
	 * @throws HinemosUnknown
	 */
	public List<String> createPerfFile(TreeMap<String, String> facilityIdNameMap,
			List<String> targetFacilityList,
			List<CollectKeyInfoPK> collectKeyInfoList, 
			Integer summaryType,
			String localeStr,
			boolean header,
			String defaultDateStr) throws HinemosUnknown{
		m_log.debug("createPerfFile() facilityIdNameMap = " + facilityIdNameMap.toString()
				+ ", targetFacilityList =" + targetFacilityList.toString()
				+ ", collectKeyInfoList =" + collectKeyInfoList.toString()
				+ ", summaryType = " + summaryType
				+ ", localeStr = " + localeStr
				+ ", header = " + header
				+ ", defaultDateStr = " + defaultDateStr);
		JpaTransactionManager jtm = null;
		List<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

			list = ExportCollectDataFile.create(facilityIdNameMap, targetFacilityList, 
					collectKeyInfoList, summaryType, localeStr, header, userId, defaultDateStr);
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("createPerfFile() : "
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
	 * 指定したリストのファイルパスを削除する
	 * 
	 * @param filepathList
	 * @throws HinemosUnknown
	 */
	public void deletePerfFile(ArrayList<String> fileNameList) throws HinemosUnknown{
		m_log.debug("deletePerformanceFile()");
		ExportCollectDataFile.deleteFile(fileNameList);
	}
	

	/**
	 * イベント履歴のフラグを取得する。
	 * @throws HinemosUnknown 
	 */
	public HashMap<String, ArrayList<EventDataInfo>> getEventDataMap(ArrayList<String> facilityIdList) throws HinemosUnknown {
		HashMap<String, ArrayList<EventDataInfo>> ret = new HashMap<>();

		if (facilityIdList.size() == 0) {
			return ret;
		}

		JpaTransactionManager jtm = new JpaTransactionManager();
		try {
			jtm.begin();
			HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
	
			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT a FROM EventLogEntity a WHERE a.collectGraphFlg = true AND");
			// ファシリティID設定
			sbJpql.append(" a.id.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", facilityIdList) + ")");
			sbJpql.append(" ORDER BY a.id.outputDate");
			TypedQuery<EventLogEntity> typedQuery = em.createQuery(sbJpql.toString(), EventLogEntity.class);
			// ファシリティID設定
			typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", facilityIdList.toArray(new String[0]));
			typedQuery = typedQuery.setMaxResults(100);
			List<EventLogEntity> list = typedQuery.getResultList();
	
			for (EventLogEntity e : list) {
				String facilityId = e.getId().getFacilityId();
				ArrayList<EventDataInfo> eventList = ret.get(facilityId);
				if (eventList == null) {
					eventList = new ArrayList<>();
					ret.put(facilityId, eventList);
				}
				eventList.add(SelectEvent.getEventDataInfo(e));
			}
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("createPerfFile() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return ret;
	}
}
