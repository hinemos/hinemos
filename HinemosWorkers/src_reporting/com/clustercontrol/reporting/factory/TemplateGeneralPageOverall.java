/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.util.Messages;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRCsvDataSource;

/**
 * 全体を通したページの作成を行う共通クラス
 */
public class TemplateGeneralPageOverall extends TemplateBase {

	private static Log m_log = LogFactory.getLog(TemplateGeneralPageOverall.class);
	
	private static class SummaryPageInfo {
		Date start;
		Date end;

		SummaryPageInfo(Date start, Date end) {
			this.start = start;
			this.end = end;
		}
	}
	
	@Override
	public List<JasperPrint> getReport(Integer pageOffset) throws ReportingPropertyNotFound {
		
		// プロパティチェック
		if(m_propertiesMap.get(ReportingConstant.OUTPUT_PERIOD_TYPE_KEY_VALUE).isEmpty()){
			throw new ReportingPropertyNotFound(ReportingConstant.OUTPUT_PERIOD_TYPE_KEY_VALUE + " is not defined.");
		}
		String outputPeriodType = m_propertiesMap.get(ReportingConstant.OUTPUT_PERIOD_TYPE_KEY_VALUE);
		
		// 出力単位で、期間を分割
		if (outputPeriodType.equals(ReportingConstant.OUTPUT_TYPE_ALL)) {
			return getReportAll(pageOffset);
		}
		else if (outputPeriodType.equals(ReportingConstant.OUTPUT_TYPE_MONTH)) {
			return getReportMonth(pageOffset);
		}
		else if (outputPeriodType.equals(ReportingConstant.OUTPUT_TYPE_WEEK)) {
			// TODO 日曜日スタートか、月曜日スタートで週間情報を出力したい
		}
		else if (outputPeriodType.equals(ReportingConstant.OUTPUT_TYPE_DAY)) {
			return getReportDaily(pageOffset);
		}
		
		return null;
	}
	
	/**
	 * 指定された全期間の情報をひとつのレポートとして出力
	 * @param pageOffset
	 * @return
	 * @throws ReportingPropertyNotFound
	 */
	
	public List<JasperPrint> getReportAll(Integer pageOffset) throws ReportingPropertyNotFound {
		
		m_log.info("templateId = " + m_templateId + " : getReportAll() start.");
		
		List<JasperPrint> jpList = new ArrayList<>();
		JasperPrint jp = null;
		
		m_curPage = pageOffset;
		
		try {
			jp = createJPReport(m_startDate, m_endDate);
			
			if(jp != null) {
				jpList.add(jp);
				m_curPage += jp.getPages().size();
			}
		} 
		catch (ReportingPropertyNotFound e) {
			throw e;
		}
		catch (Exception e) {
			m_log.error(e, e);
		}
		
		m_log.info("templateId = " + m_templateId + " : getReportAll() completed.");
		
		return jpList;
	}
	
	/**
	 * 指定された全期間の情報を1ヶ月、もしくは28日ごとにまとめてレポートとして出力
	 * @param pageOffset
	 * @return
	 * @throws ReportingPropertyNotFound
	 */
	public List<JasperPrint> getReportMonth(Integer pageOffset) throws ReportingPropertyNotFound {
		
		m_log.info("templateId = " + m_templateId + " : getReportMonth() start.");
		
		List<JasperPrint> jpList = new ArrayList<>();
		JasperPrint jp = null;
		
		m_curPage = pageOffset;
		
		List<SummaryPageInfo> pageInfoList = new ArrayList<>();
		
		// divide by month or 28 days
		if (ReportUtil.getOutputPeriodType() == ReportUtil.OUTPUT_PERIOD_TYPE_MONTH
				|| ReportUtil.getOutputPeriodType() == ReportUtil.OUTPUT_PERIOD_TYPE_YEAR) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(m_startDate);
			Calendar endCal = Calendar.getInstance();
			endCal.setTime(m_endDate);
			for (; cal.before(endCal); cal.add(Calendar.MONTH, 1)) {
				Calendar next = (Calendar)cal.clone();
				next.add(Calendar.MONTH, 1);
				pageInfoList.add(new SummaryPageInfo(cal.getTime(), next.getTime()));
			}
		} else {
			Calendar startCal = Calendar.getInstance();
			startCal.setTime(m_startDate);
			Calendar endCal = Calendar.getInstance();
			endCal.setTime(m_endDate);
			for (; startCal.before(endCal); startCal.add(Calendar.DATE, 28)) {
				Calendar next = (Calendar)startCal.clone();
				next.add(Calendar.DATE, 28);
				if (next.after(endCal)) {
					pageInfoList.add(new SummaryPageInfo(startCal.getTime(), endCal.getTime()));
				} else {
					pageInfoList.add(new SummaryPageInfo(startCal.getTime(), next.getTime()));
				}
			}
		}
		
		// output to JasperPrint
		try {
			// 分割した範囲ごとにレポートを作成する
			for (SummaryPageInfo info : pageInfoList) {
				
				jp = createJPReport(info.start, info.end);
				
				if(jp != null) {
					jpList.add(jp);
					m_curPage += jp.getPages().size();
				}
			}
		} catch (Exception e) {
			m_log.error(e, e);
		}
		m_log.info("templateId = " + m_templateId + " : getReportMonth() completed.");

		return jpList;
	}
	
	/**
	 * 指定された全期間の情報を1日ごとにまとめてレポートとして出力
	 * @param pageOffset
	 * @return
	 * @throws ReportingPropertyNotFound
	 */
	public List<JasperPrint> getReportDaily(Integer pageOffset) throws ReportingPropertyNotFound {
		
		m_log.info("templateId = " + m_templateId + " : getReportDaily() start.");
		
		List<JasperPrint> jpList = new ArrayList<>();
		JasperPrint jp = null;
		
		m_curPage = pageOffset;
		
		// output to JasperPrint
		try {
			// 1日ごとに分割しレポートを作成する
			Calendar startDay = Calendar.getInstance();
			Calendar endDay = Calendar.getInstance();
			startDay.setTime(m_startDate);
			for ( ; startDay.getTimeInMillis() < m_endDate.getTime() ; startDay.add(Calendar.DATE, 1)) {
				
				endDay.setTime(startDay.getTime());
				endDay.add(Calendar.DATE, 1);
				
				jp = createJPReport(new Date(startDay.getTimeInMillis()), new Date(endDay.getTimeInMillis()));
				
				if(jp != null) {
					jpList.add(jp);
					m_curPage += jp.getPages().size();
				}
			}
		} catch (Exception e) {
			m_log.error(e, e);
		}
		m_log.info("templateId = " + m_templateId + " : getReportDaily() completed.");

		return jpList;
	}
	
	
	private JasperPrint createJPReport(Date startDate, Date endDate) throws ReportingPropertyNotFound {
		
		JasperPrint retJp = null;
		
		Map<String, Object> params = new HashMap<>();
		params.putAll(m_propertiesMap);
		
		m_log.debug("createJPReport:");
		
		try {
			// 生成するグラフ数をプロパティより取得
			if(m_propertiesMap.get(ReportingConstant.CHART_NUM_KEY_VALUE).isEmpty()) {
				throw new ReportingPropertyNotFound(ReportingConstant.CHART_NUM_KEY_VALUE + " is not defined.");
			}
			int chartNum = Integer.parseInt(m_propertiesMap.get(ReportingConstant.CHART_NUM_KEY_VALUE));
			for (int num = 1 ; num < chartNum+1; num++) {
			
				DatasourceBase datasourceClass = null;
				String datasourceClassStr = "";
				
				datasourceClassStr = m_propertiesMap.get(ReportingConstant.DATASOURCE_CLASS_KEY_VALUE + "." + num);
				
				try {
					@SuppressWarnings("unchecked")
					Class<? extends DatasourceBase> clazz = (Class<? extends DatasourceBase>) Class.forName(datasourceClassStr);
					datasourceClass = clazz.newInstance();
				} catch (Exception e) {
					m_log.error(e,e);
				}
				
				// データソース生成用のクラスができなければ、終了
				if(datasourceClass == null) {
					throw new ReportingPropertyNotFound(datasourceClassStr + " is not exists.");
				}
				
				datasourceClass.setTemplateId(m_templateId);
				datasourceClass.setPropertiesMap(m_propertiesMap);
				datasourceClass.setStartDate(startDate);
				datasourceClass.setEndDate(endDate);
				
				HashMap<String, Object> dsMap = datasourceClass.createDataSource(num);
				if(dsMap != null) {
					params.putAll(dsMap);
				}	
			}
			/*
			* 共通パラメータ
			*/
			params.putAll(getCommonProperty());
			
			// 時刻表記
			String fmtStr = isDefine(ReportingConstant.DATE_FORMAT_KEY_VALUE, Messages.getString("COMMON_DATE_FORMAT"));
			SimpleDateFormat fmt = new SimpleDateFormat(fmtStr);
			params.put("START_DATE_STR", fmt.format(startDate.getTime()));
			params.put("END_DATE_STR", fmt.format(new Date(endDate.getTime() - 1)));
			
			params.put("PAGE_OFFSET", m_curPage);
			params.put("LOGO_FILENAME", ReportUtil.getLogoFilePath());
			params.put("SHOW_PAGE", ReportUtil.isPageValid());
			long startMargin = (long)(((endDate.getTime() - startDate.getTime()) / 1000D) * 0.01 * 1000);
			long endMargin = (long)(((endDate.getTime() - startDate.getTime()) / 1000D) * 0.02 * 1000);
			params.put("START_DATE", new Timestamp(startDate.getTime() - startMargin));
			params.put("END_DATE", new Timestamp(endDate.getTime() + endMargin));
			params.put("DATE", startDate);

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
		catch (ReportingPropertyNotFound e) {
			throw e;
		}
		catch (Exception e) {
			m_log.error(e, e);
		}
		
		return retJp;
	}
}
