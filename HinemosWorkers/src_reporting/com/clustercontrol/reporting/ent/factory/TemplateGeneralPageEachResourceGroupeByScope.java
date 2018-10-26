/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.factory;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.ent.bean.DataKey;
import com.clustercontrol.reporting.ent.bean.OutputFacilityInfo;
import com.clustercontrol.reporting.ent.bean.OutputFacilityInfo.FacilityComparator;
import com.clustercontrol.reporting.ent.bean.OutputFacilityInfo.OutputNodeInfo;
import com.clustercontrol.reporting.ent.bean.OutputFacilityInfo.OutputScopeInfo;
import com.clustercontrol.reporting.ent.bean.ResourceChart;
import com.clustercontrol.reporting.ent.bean.ResourcePage;
import com.clustercontrol.reporting.ent.bean.ResourcePageHolder;
import com.clustercontrol.reporting.ent.session.ReportingPerformanceControllerBean;
import com.clustercontrol.reporting.ent.util.PropertiesConstant;
import com.clustercontrol.reporting.factory.TemplateBase;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;
import com.clustercontrol.util.Messages;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRCsvDataSource;

/**
 * リソース毎のレポート作成を行うテンプレートクラス
 * 
 * @version 5.0.a
 * @since 5.0.a
 */
public class TemplateGeneralPageEachResourceGroupeByScope extends TemplateBase {
	private static Log m_log = LogFactory.getLog(TemplateGeneralPageEachResourceGroupeByScope.class);

	@Override
	public List<JasperPrint> getReport(Integer pageOffset) throws ReportingPropertyNotFound {
		// プロパティチェック
		if (m_propertiesMap.get(ReportingConstant.OUTPUT_PERIOD_TYPE_KEY_VALUE) == null || m_propertiesMap.get(ReportingConstant.OUTPUT_PERIOD_TYPE_KEY_VALUE).isEmpty()){
			throw new ReportingPropertyNotFound(ReportingConstant.OUTPUT_PERIOD_TYPE_KEY_VALUE + " is not defined.");
		}
		String outputPeriodType = m_propertiesMap.get(ReportingConstant.OUTPUT_PERIOD_TYPE_KEY_VALUE);

		if (m_propertiesMap.get(ReportingConstant.CHART_NUM_KEY_VALUE) == null || m_propertiesMap.get(ReportingConstant.CHART_NUM_KEY_VALUE).isEmpty()) {
			throw new ReportingPropertyNotFound(ReportingConstant.CHART_NUM_KEY_VALUE + " is not defined.");
		}
		if (m_propertiesMap.get(PropertiesConstant.CHART_TYPE_NUM_KEY) == null || m_propertiesMap.get(PropertiesConstant.CHART_TYPE_NUM_KEY).isEmpty()) {
			throw new ReportingPropertyNotFound(PropertiesConstant.CHART_TYPE_NUM_KEY + " is not defined.");
		}

		// 出力単位で、期間を分割
		if (outputPeriodType.equals(ReportingConstant.OUTPUT_TYPE_ALL)) {
			return getReportAll(collectFacility(), pageOffset);
		}
		else if (outputPeriodType.equals(ReportingConstant.OUTPUT_TYPE_WEEK)) {
			// 今後の予定：： 日曜日スタートか、月曜日スタートで週間情報を出力
		}
		else if (outputPeriodType.equals(ReportingConstant.OUTPUT_TYPE_DAY)) {
			return getReportDaily(collectFacility(), pageOffset);
		}
		return null;
	}

	/**
	 * Comparator for sorting node list by facility name
	 */
	private static class NodeListFacilityNameComparator implements Comparator<OutputNodeInfo>, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(OutputNodeInfo o1, OutputNodeInfo o2) {
			return o1.getFacilityName().compareTo(o2.getFacilityName());
		}
	}

	/**
	 * 指定された全期間の情報をひとつのレポートとして出力
	 * @param rootFacility 出力設定のファシリティを親とした所属スコープ及びノードの関係オブジェクト
	 * @param pageOffset
	 * @return
	 * @throws ReportingPropertyNotFound
	 */
	public List<JasperPrint> getReportAll(OutputFacilityInfo rootFacility, Integer pageOffset) throws ReportingPropertyNotFound {
		m_log.info("templateId = " + m_templateId + " : getReportAll() start.");
		List<JasperPrint> jpList = new ArrayList<>();
		m_curPage = pageOffset;

		int chartTypeNum = Integer.parseInt(m_propertiesMap.get(PropertiesConstant.CHART_TYPE_NUM_KEY));
		int pageChartNum;
		try {
			pageChartNum = Integer.parseInt(m_propertiesMap.get(ReportingConstant.CHART_NUM_KEY_VALUE));
		} catch (NumberFormatException e) {
			throw new ReportingPropertyNotFound(ReportingConstant.CHART_NUM_KEY_VALUE + " is not defined.");
		}

		// ノード毎の性能情報の設定方法を踏襲し、アイテム名が指定される
		for (int chartType = 1; chartType < chartTypeNum+1; chartType++) {
			int maxLine;
			try {
				maxLine = Integer.parseInt(isDefine(PropertiesConstant.REPORT_GRAPH_LINE_MAX_KEY+"." + chartType, PropertiesConstant.GRAPH_LINE_MAX_DEFAULT));
			} catch (NumberFormatException e) {
				m_log.warn(PropertiesConstant.REPORT_GRAPH_LINE_MAX_KEY+"."+chartType + " is invalid.");
				maxLine = Integer.parseInt(PropertiesConstant.GRAPH_LINE_MAX_DEFAULT);
			}
			ResourcePageHolder pageHolder = new ResourcePageHolder(pageChartNum, maxLine);

			try {
				//リソース情報の全取得(データソースクラスのインスタンス化)
				DatasourceSamePattern ds = instanceDatasource(rootFacility, chartType, m_startDate, m_endDate);
				if (rootFacility instanceof OutputNodeInfo) {
					OutputNodeInfo rootNode = (OutputNodeInfo)rootFacility;
					for (DataKey dataKey : ds.getKeys(rootNode.getFacilityId())) {
						pageHolder.appendItem(dataKey, null);
					}
				} else if (rootFacility instanceof OutputScopeInfo) {
					OutputScopeInfo rootScope = (OutputScopeInfo)rootFacility;
					for (OutputScopeInfo scope: rootScope.getScopes()) {
						List<OutputNodeInfo> nodeList = scope.getNodes();
						String legendType = isDefine(PropertiesConstant.LEGEND_TYPE_KEY, PropertiesConstant.LEGEND_TYPE_DEFAULT);
						if (PropertiesConstant.TYPE_FACILITY_NAME.equals(legendType)) {
							// 凡例表記にファシリティ名が設定されている場合, ファシリティ名順に並べ替える
							Collections.sort(nodeList, new NodeListFacilityNameComparator());
						}
						for( OutputNodeInfo node: nodeList ){
							// Sub title for this chart
							String chartSubTitle = scope.getFacilityName() + "(" + scope.getFacilityId() + ")";

							for (DataKey dataKey : ds.getKeys(node.getFacilityId())) {
								pageHolder.appendItem(dataKey, chartSubTitle);
							}
						}
						pageHolder.newChart();
					}

					for (OutputNodeInfo node: rootScope.getNodes()) {
						for (DataKey dataKey : ds.getKeys(node.getFacilityId())) {
							// Sub title for this chart
							String chartSubTitle = rootScope.getFacilityName() + "(" + rootScope.getFacilityId() + ")";
							pageHolder.appendItem(dataKey, chartSubTitle);
						}
					}
				}
				for (ResourcePage page: pageHolder.list()) {
					JasperPrint jp = createJRReport(createJPReportParams(ds, page), chartType);
					if (jp == null)
						continue;
					jpList.add(jp);
					m_curPage += jp.getPages().size();
				}
				if (pageHolder.isEmpty()) {
					JasperPrint jp = createJRReport(createJPReportParams(ds, null), chartType);
					if (jp == null)
						continue;
					jpList.add(jp);
					m_curPage += jp.getPages().size();
				}
			} catch (ReportingPropertyNotFound e) {
				throw e;
			} catch (Exception e) {
				m_log.error(e, e);
			}
		}
		m_log.info("templateId = " + m_templateId + " : getReportAll() completed.");

		if (jpList.isEmpty())
			throw new ReportingPropertyNotFound("not create report. The cause might be template file." + m_templateId);

		return jpList;
	}

	private Map<String, Object> createJPReportParams(DatasourceSamePattern ds, ResourcePage page) throws ReportingPropertyNotFound {
		Map<String, Object> params = new HashMap<>();
		params.putAll(m_propertiesMap);
		m_log.debug("createJPReport: resouce: ");

		if(null == page){
			return params;
		}

		try {
			int chartIndex = 0;
			for (ResourceChart chart: page.getChartList()) {
				chartIndex++;
				Map<String, Object> dsMap = ds.createDataSource(chart, chartIndex);
				if(dsMap != null) {
					params.putAll(dsMap);
				}
			}
		}catch (ReportingPropertyNotFound e) {
			throw e;
		}
		return params;
	}

	private JasperPrint createJRReport(Map<String, Object> params, int chartTypeNum) throws ReportingPropertyNotFound {
		JasperPrint retJp = null;
		m_log.debug("createJPReport: resouce: ");
		/*
		 * 共通パラメータ
		 */
		params.putAll(getCommonProperty());

		// 時刻表記
		String fmtStr = isDefine(ReportingConstant.DATE_FORMAT_KEY_VALUE, Messages.getString("COMMON_DATE_FORMAT"));
		SimpleDateFormat fmt = new SimpleDateFormat(fmtStr);
		params.put("START_DATE_STR", fmt.format(m_startDate.getTime()));
		params.put("END_DATE_STR", fmt.format(new Date(m_endDate.getTime() - 1)));

		params.put(PropertiesConstant.CATEGORY_TITLE_KEY, m_propertiesMap.get(PropertiesConstant.CATEGORY_TITLE_KEY+"."+chartTypeNum));
		params.put("PAGE_OFFSET", m_curPage);
		params.put("LOGO_FILENAME", ReportUtil.getLogoFilePath());
		params.put("SHOW_PAGE", ReportUtil.isPageValid());
		long startMargin = (long)(((m_endDate.getTime() - m_startDate.getTime()) / 1000D) * 0.01 * 1000);
		long endMargin = (long)(((m_endDate.getTime() - m_startDate.getTime()) / 1000D) * 0.02 * 1000);
		params.put("START_DATE", new Timestamp(m_startDate.getTime() - startMargin));
		params.put("END_DATE", new Timestamp(m_endDate.getTime() + endMargin));
		params.put("DATE", m_startDate);

		try {
			JasperReport report = JasperCompileManager.compileReport(m_jrxmlFilePath);

			// グラフ生成のフラグ
			int dsPassType = 0;
			if( m_propertiesMap.get(ReportingConstant.DATASOURCE_PASS_TYPE_KEY_VALUE) != null
					&& !(m_propertiesMap.get(ReportingConstant.DATASOURCE_PASS_TYPE_KEY_VALUE).isEmpty()) ) {
				dsPassType = Integer.parseInt(m_propertiesMap.get(ReportingConstant.DATASOURCE_PASS_TYPE_KEY_VALUE));
			}

			// DataSourceをparamに格納して渡す場合
			if(dsPassType == ReportingConstant.DS_PASS_TYPE_PARAM) {
				m_log.info("Datasource pass type : params");
				retJp = JasperFillManager.fillReport(report, params, new JREmptyDataSource());
			}
			// Datasourceを直接引数で渡す場合（ただし、1つのDatasourceしか渡せない）
			else if (dsPassType == ReportingConstant.DS_PASS_TYPE_DIRECT){
				m_log.info("Datasource pass type : direct");
				retJp = JasperFillManager.fillReport(report, params, (JRCsvDataSource)params.get(ReportingConstant.STR_DS+"_1"));
			}
			else {
				m_log.error("Datasource pass type : " + dsPassType + " not exist.");
			}
		}
		catch (Exception e) {
			m_log.error(e, e);
		}
		return retJp;
	}

	private DatasourceSamePattern instanceDatasource(OutputFacilityInfo facility, int chartType, Date startDate, Date endDate) throws ReportingPropertyNotFound {
		if (m_propertiesMap.get(PropertiesConstant.ITEM_CODE_KEY+"."+chartType) == null || m_propertiesMap.get(PropertiesConstant.ITEM_CODE_KEY+"."+chartType).isEmpty()) {
			throw new ReportingPropertyNotFound(PropertiesConstant.ITEM_CODE_KEY+"."+chartType+"in not defined");
		}
		if(m_log.isDebugEnabled()){
			m_log.debug("createData: " + m_propertiesMap.get(PropertiesConstant.ITEM_CODE_KEY) + "." + chartType);
		}

		DatasourceSamePattern datasourceClass = null;
		try {
			String datasourceClassStr = m_propertiesMap.get(ReportingConstant.DATASOURCE_CLASS_KEY_VALUE + "." + chartType);

			try {
				@SuppressWarnings("unchecked")
				Class<? extends DatasourceSamePattern> clazz = (Class<? extends DatasourceSamePattern>) Class.forName(datasourceClassStr);
				datasourceClass = clazz.newInstance();
			} catch (Exception e) {
				m_log.error(e,e);
			}

			// データソース生成用のクラスができなければ、終了
			if(datasourceClass == null)
				throw new ReportingPropertyNotFound(ReportingConstant.DATASOURCE_CLASS_KEY_VALUE + "." + chartType +  " : " + datasourceClassStr + " is not exists.");

			datasourceClass.setTemplateId(m_templateId);
			datasourceClass.setPropertiesMap(m_propertiesMap);
			datasourceClass.setStartDate(startDate);
			datasourceClass.setEndDate(endDate);
			datasourceClass.collectDataSource(facility, chartType);
			return datasourceClass;
		}
		catch (ReportingPropertyNotFound e) {
			throw e;
		}
		catch (Exception e) {
			m_log.error(e, e);
		}

		throw new InternalError("datasourceClass not create instance.");
	}

	private OutputFacilityInfo collectFacility() {
		try {
			ReportingPerformanceControllerBean performanceController = new ReportingPerformanceControllerBean();
			FacilityInfo facilityInfo = performanceController.getFacilityInfo(ReportUtil.getFacilityId());

			if (facilityInfo != null) {
				String facilityName = facilityInfo.getFacilityName();
				int type = facilityInfo.getFacilityType();

				if (type == 1) {
					OutputNodeInfo rootNode = new OutputNodeInfo(ReportUtil.getFacilityId());
					rootNode.setFacilityName(facilityName);
					return rootNode;
				} else {
					OutputScopeInfo rootScope = new OutputScopeInfo(ReportUtil.getFacilityId());
					rootScope.setFacilityName(facilityName);
					rootScope.getScopes().addAll(collectScopes(rootScope));
					rootScope.getNodes().addAll(collectNodes(rootScope));
					return rootScope;
				}
			}
		} catch (Exception e) {
			m_log.error(e, e);
		}
		throw new InternalError("can not collect facility info. " + ReportUtil.getFacilityId());
	}

	private List<OutputScopeInfo> collectScopes(OutputFacilityInfo rootScope) {
		List<OutputScopeInfo> facilities = new ArrayList<>();

		try {
			ReportingPerformanceControllerBean performanceController = new ReportingPerformanceControllerBean();
			List<FacilityRelationEntity> facilityRelationList = performanceController.getChildFacilityRelationEntity(rootScope.getFacilityId());
			if (facilityRelationList != null) {
				for (FacilityRelationEntity facilityRelationEntity : facilityRelationList) {
					FacilityInfo facilityInfo = performanceController.getFacilityInfo(facilityRelationEntity.getChildFacilityId());
					String facilityId = null;
					String facilityName = null;
					Integer type = null;
					if (facilityInfo == null) {
						throw new HinemosUnknown("facilityid is null.");
					}
					facilityId = facilityInfo.getFacilityId();
					facilityName = facilityInfo.getFacilityName();
					type = facilityInfo.getFacilityType();
					if (type == null) {
						throw new HinemosUnknown("facilityInfo's type is null.");
					}

					if (type != 1) {
						if(m_log.isDebugEnabled()){
							m_log.debug("  found scope: " + facilityId);
						}
						OutputScopeInfo scope = new OutputScopeInfo(facilityId);
						scope.setFacilityName(facilityName);
						scope.setParentId(rootScope.getFacilityId());
						List<OutputNodeInfo> childrenList = collectChildNodes(facilityId, facilityId); // may have duplicate nodes
						scope.getNodes().addAll(listWithoutDuplicate(childrenList));

						facilities.add(scope);
					}
					for (OutputScopeInfo scope: facilities) {
						Collections.sort(scope.getNodes(), new FacilityComparator());
					}
				}
			}
		} catch (Exception e) {
			m_log.error(e, e);
		}
		return facilities;
	}

	private List<OutputNodeInfo> collectNodes(OutputFacilityInfo rootScope) {
		List<OutputNodeInfo> facilities = new ArrayList<>();

		try {
			ReportingPerformanceControllerBean performanceController = new ReportingPerformanceControllerBean();
			List<FacilityRelationEntity> facilityRelationList = performanceController.getChildFacilityRelationEntity(rootScope.getFacilityId());
			if (facilityRelationList != null) {
				for (FacilityRelationEntity facilityRelationEntity : facilityRelationList) {
					FacilityInfo facilityInfo = performanceController.getFacilityInfo(facilityRelationEntity.getChildFacilityId());
					String facilityId = null;
					String facilityName = null;
					Integer type = null;
					if (facilityInfo == null) {
						throw new HinemosUnknown("facilityInfo is null.");
					}
					facilityId = facilityInfo.getFacilityId();
					facilityName = facilityInfo.getFacilityName();
					type = facilityInfo.getFacilityType();
					if (type == null) {
						throw new HinemosUnknown("facilityInfo's type is null.");
					}
					if (type == 1) {
						if(m_log.isDebugEnabled()){
							m_log.debug("  found node: " + facilityId);
						}
						OutputNodeInfo node = new OutputNodeInfo(facilityId);
						node.setFacilityName(facilityName);
						node.setParentId(rootScope.getFacilityId());
						facilities.add(node);
					}
				}
			}
		} catch (Exception e) {
			m_log.error(e, e);
		}
		return facilities;
	}

	/**
	 * Remove duplicate nodes from list
	 * 
	 * @param list
	 * @return
	 */
	private List<OutputNodeInfo> listWithoutDuplicate(List<OutputNodeInfo> list) {
		List<OutputNodeInfo> newList = new ArrayList<>();
		Set<String> checkHash = new HashSet<String>();
		OutputNodeInfo checkNode;

		for (Iterator<OutputNodeInfo> nodes = list.iterator(); nodes.hasNext();){
			checkNode = nodes.next();
			// 重複していない場合
			if(! checkHash.contains(checkNode.getFacilityId())){
				newList.add(checkNode);
				checkHash.add(checkNode.getFacilityId());
			}
		}
		return newList;
	}

	private List<OutputNodeInfo> collectChildNodes(String scopeId, String parentId) {
		List<OutputNodeInfo> nodes = new ArrayList<>();
		try {
			ReportingPerformanceControllerBean performanceController = new ReportingPerformanceControllerBean();
			List<FacilityRelationEntity> facilityRelationList = performanceController.getChildFacilityRelationEntity(scopeId);
			if (facilityRelationList != null) {
				for (FacilityRelationEntity facilityRelationEntity : facilityRelationList) {
					FacilityInfo facilityInfo = performanceController.getFacilityInfo(facilityRelationEntity.getChildFacilityId());
					
					String facilityId = null;
					String facilityName = null;
					Integer type = null;
					if (facilityInfo == null) {
						throw new HinemosUnknown("facilityInfo is null.");
					}
					facilityId = facilityInfo.getFacilityId();
					facilityName = facilityInfo.getFacilityName();
					type = facilityInfo.getFacilityType();
					if (type == null) {
						throw new HinemosUnknown("facilityInfo's type is null.");
					}
					if (type == 1) {
						if(m_log.isDebugEnabled()){
							m_log.debug("  found node: " + facilityId);
						}
						OutputNodeInfo node = new OutputNodeInfo(facilityId);
						node.setFacilityName(facilityName);
						node.setParentId(parentId);
						nodes.add(node);
					} else {
						nodes.addAll(collectChildNodes(facilityId, parentId));
					}
				}
			}
		} catch (Exception e) {
			m_log.error(e, e);
		}
		return nodes;
	}

	/**
	 * 指定された全期間の情報を1日ごとにまとめてレポートとして出力
	 * @param rootFacility
	 * @param pageOffset
	 * @return
	 * @throws ReportingPropertyNotFound
	 */
	public List<JasperPrint> getReportDaily(OutputFacilityInfo rootFacility, Integer pageOffset) throws ReportingPropertyNotFound {

		m_log.info("templateId = " + m_templateId + " : getReportDaily() start.");

		List<JasperPrint> jpList = new ArrayList<>();

		m_curPage = pageOffset;

		try {
			// 1日ごとに分割しレポートを作成する
			Calendar startDay = Calendar.getInstance();
			Calendar endDay = Calendar.getInstance();
			startDay.setTime(m_startDate);

			int chartTypeNum = Integer.parseInt(m_propertiesMap.get(PropertiesConstant.CHART_TYPE_NUM_KEY));
			int pageChartNum;
			try {
				pageChartNum = Integer.parseInt(m_propertiesMap.get(ReportingConstant.CHART_NUM_KEY_VALUE));
			} catch (NumberFormatException e) {
				throw new ReportingPropertyNotFound(ReportingConstant.CHART_NUM_KEY_VALUE + " is not defined.");
			}
			for ( ; startDay.getTimeInMillis() < m_endDate.getTime() ; startDay.add(Calendar.DATE, 1)) {

				endDay.setTime(startDay.getTime());
				endDay.add(Calendar.DATE, 1);
				// ノード毎の性能情報の設定方法を踏襲し、アイテム名が指定される
				for (int chartType = 1; chartType < chartTypeNum+1; chartType++) {
					int maxLine;
					try {
						maxLine = Integer.parseInt(isDefine(PropertiesConstant.REPORT_GRAPH_LINE_MAX_KEY+"."+chartType, PropertiesConstant.GRAPH_LINE_MAX_DEFAULT));
					} catch (NumberFormatException e) {
						m_log.warn(PropertiesConstant.REPORT_GRAPH_LINE_MAX_KEY+"."+chartType + " is invalid.");
						maxLine = Integer.parseInt(PropertiesConstant.GRAPH_LINE_MAX_DEFAULT);
					}
					ResourcePageHolder pageHolder = new ResourcePageHolder(pageChartNum, maxLine);

					try {
						//リソース情報の全取得(データソースクラスのインスタンス化)
						DatasourceSamePattern ds = instanceDatasource(rootFacility, chartType, new Date(startDay.getTimeInMillis()), new Date(endDay.getTimeInMillis()));

						if (rootFacility instanceof OutputNodeInfo) {
							OutputNodeInfo rootNode = (OutputNodeInfo)rootFacility;
							for (DataKey dataKey : ds.getKeys(rootNode.getFacilityId())) {
								pageHolder.appendItem(dataKey, null);
							}
						} else if (rootFacility instanceof OutputScopeInfo) {
							OutputScopeInfo rootScope = (OutputScopeInfo)rootFacility;
							for (OutputScopeInfo scope: rootScope.getScopes()) {
								for (OutputNodeInfo node: scope.getNodes()) {
									// Sub title for this chart
									String chartSubTitle = scope.getFacilityName() + "(" + scope.getFacilityId() + ")";

									for (DataKey dataKey : ds.getKeys(node.getFacilityId())) {
										pageHolder.appendItem(dataKey, chartSubTitle);
									}
								}
								pageHolder.newChart();
							}

							for (OutputNodeInfo node: rootScope.getNodes()) {
								for (DataKey dataKey : ds.getKeys(node.getFacilityId())) {
									// Sub title for this chart
									String chartSubTitle = rootScope.getFacilityName() + "(" + rootScope.getFacilityId() + ")";
									pageHolder.appendItem(dataKey, chartSubTitle);
								}
							}
						}

						for (ResourcePage page: pageHolder.list()) {
							JasperPrint jp = createJRReport(createJPReportParams(ds, page), chartType);
							if (jp == null)
								continue;
							jpList.add(jp);
							m_curPage += jp.getPages().size();
						}
						if (pageHolder.isEmpty()) {
							JasperPrint jp = createJRReport(createJPReportParams(ds, null), chartType);
							if (jp == null)
								continue;
							jpList.add(jp);
							m_curPage += jp.getPages().size();
						}
					} catch (ReportingPropertyNotFound e) {
						throw e;
					} catch (Exception e) {
						m_log.error(e, e);
					}
				}
				if (jpList.isEmpty())
					throw new ReportingPropertyNotFound("Failed to create report. This might be caused by incorrect template file. TempalteId = " + m_templateId);

			}
		} catch (ReportingPropertyNotFound e) {
			m_log.error(e, e);
		} catch (Exception e){
			m_log.error(e, e);
		}
		m_log.info("templateId = " + m_templateId + " : getReportDaily() completed.");
		return jpList;
	}
}
