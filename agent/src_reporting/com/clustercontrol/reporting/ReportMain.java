/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.factory.TemplateBase;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

/**
 * Hinemos Reporting main class
 * 
 * @version 5.0.a
 * @since 5.0.a
 */
public class ReportMain {

	private static final Log m_log = LogFactory.getLog(ReportMain.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		
		m_log.info("report create start");
		
		ReportUtil.init();

		String outFileName = "";
		String reportScheduleId = "";
		String templateSetId = "";
		
		// parse arguments
		if (args.length > 0 && ( args[0].startsWith("/") || args[0].startsWith(":",1) )) {
			outFileName = args[0];
			ReportUtil.setReportFileName(outFileName);
			m_log.info("create file name:" + outFileName);
		}
		if (args.length > 1) {
			reportScheduleId = args[1];
		}

		if (reportScheduleId == null || reportScheduleId.isEmpty()) {
			m_log.error("reportScheduleId is not specified");
			System.exit(1);
		}
		if (ReportUtil.loadReportingInfo(reportScheduleId) == false) {
			m_log.error("Reporting information is invaild: " + reportScheduleId);
			System.exit(1);
		}

		// templateMap を抽出
		templateSetId = ReportUtil.getTemplateSetId();
		if (ReportUtil.loadTemplateMap(templateSetId) == false) {
			m_log.error("Reporting template is invaild: " + templateSetId);
			System.exit(1);
		}
		
		if (outFileName == null || outFileName.isEmpty()) {
			outFileName = ReportUtil.getReportFileName();
		}
		
		ReportUtil.removeExpiredDirectories();

		// get target nodes
		List<String[]> nodes = ReportUtil.getNodesInScope(ReportUtil.getFacilityId());
		for (int i = 0; i < nodes.size(); i++) {
			m_log.info("node: " + nodes.get(i)[1] + " (" + nodes.get(i)[0] + ")");
		}
		
		// get target templates
		// key : templateId, orderNoのリスト, value : テンプレートのプロパティ情報
		LinkedHashMap<String[], HashMap<String, String>> templateMap = 
				ReportUtil.getTemplateMap();
		
		boolean showPage = ReportUtil.isPageValid();
		Date start = ReportUtil.getStartDate();
		Date end = ReportUtil.getEndDate();
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy/M/d HH:mm:ss");
		m_log.info("start date : " + fmt.format(start));
		m_log.info("end date : " + fmt.format(end));
		
		// get each reports
		// add all JasperPrint objects into one List
		List<JasperPrint> jpList = new ArrayList<>();
		int pageNo = 0;
		
		String templateClassStr = "";
		TemplateBase templateClass = null;
		
		for(Map.Entry<String[], HashMap<String, String>> entry : templateMap.entrySet()) {
			
			String templateId = entry.getKey()[0];
			String orderNo = entry.getKey()[1];
			
			m_log.info("Create page start: templateId = " + templateId + ", orderNo = " + orderNo);
			// テンプレートのプロパティを取得
			HashMap<String, String> propertiesMap = entry.getValue();
			
			// テンプレート毎のプロパティが取得できない場合は、終了
			if (propertiesMap == null || propertiesMap.size() == 0) {
				m_log.error(templateId + " property is not specified.");
				System.exit(1);
			}
				
			templateClassStr = propertiesMap.get(ReportingConstant.TEMPLATE_CLASS_KEY_VALUE);
			
			m_log.info("templateClassStr = " + templateClassStr);
			
			// テンプレートクラスを初期化
			templateClass = null;
			try {
				@SuppressWarnings("unchecked")
				Class<? extends TemplateBase> clazz = (Class<? extends TemplateBase>) Class.forName(templateClassStr);
				templateClass = clazz.newInstance();
			} catch (Exception e) {
				m_log.error(e,e);
			}
			
			// テンプレート生成用のクラスができなければ、終了
			if(templateClass == null) {
				m_log.error(templateClassStr + " is not exists.");
				System.exit(1);
			}
			
			templateClass.setTemplateId(templateId);
			templateClass.setShowPage(showPage);
			templateClass.setReportPeriod(start, end);
			templateClass.setNodes(nodes);
			templateClass.setPropertiesMap(propertiesMap);
			
			try {
				jpList.addAll(templateClass.getReport(pageNo));
				pageNo = templateClass.getCurPage();
			}
			catch (ReportingPropertyNotFound e) {
				m_log.error(e, e);
				m_log.error("create report terminated.");
				System.exit(1);
			}
			m_log.info("Create page complated : templateId = " + templateId);
		}
		m_log.info("All pages created.");
		
		// export to PDF
		try {
			String tmpFileName = outFileName + ".tmp";
			
			if (ReportUtil.getOutputType() == ReportingConstant.TYPE_PDF) {
				
				m_log.info("Exporting to PDF: " + outFileName);
				JRPdfExporter exporter = new JRPdfExporter();
				exporter.setExporterInput(SimpleExporterInput.getInstance(jpList));
				exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(tmpFileName));
				exporter.exportReport();
				File tmpFile = new File(tmpFileName);
				File outFile = new File(outFileName);
				if (tmpFile.renameTo(outFile)) {
					m_log.info("Exporting done.");
				} else {
					m_log.error("Exporting failed.");
				}
			}
			else if (ReportUtil.getOutputType() == ReportingConstant.TYPE_XLSX) {
				
				m_log.info("Exporting to XLSX: " + outFileName);
				JRXlsxExporter xlsxExporter = new JRXlsxExporter();
				xlsxExporter.setExporterInput(SimpleExporterInput.getInstance(jpList));
				xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(tmpFileName));
				xlsxExporter.exportReport();
				File tmpFile = new File(tmpFileName);
				File outFile = new File(outFileName);
				if (tmpFile.renameTo(outFile)) {
					m_log.info("Exporting done.");
				} else {
					m_log.error("Exporting failed.");
				}
			}
			
		} catch (Exception e) {
			m_log.error(e, e);
			System.exit(1);
		}
		
		long endTime = System.currentTimeMillis();
		m_log.info("elapsed time: " + (double)(endTime - startTime) / 1000 + " seconds");
	}

}
