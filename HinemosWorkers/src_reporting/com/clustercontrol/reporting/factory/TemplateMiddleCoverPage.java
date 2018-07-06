/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.reporting.ReportUtil;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

/**
 * 中表紙の作成を行うクラス
 */
public class TemplateMiddleCoverPage extends TemplateBase {

	private static Log m_log = LogFactory.getLog(TemplateMiddleCoverPage.class);
	
	@Override
	public List<JasperPrint> getReport(Integer pageOffset) {
		
		m_log.info("TemplateMiddleCoverPage( " + m_templateId + ") : getReport() start.");
		
		List<JasperPrint> jpList = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();
		JasperPrint jp = null;
		
		// プロパティデータすべてjrxmlに渡すパラメータとして定義
		params.putAll(m_propertiesMap);
		
		try {
			JREmptyDataSource ds = new JREmptyDataSource();
			JasperReport report = JasperCompileManager.compileReport(m_jrxmlFilePath);
			params.put("PAGE_OFFSET", pageOffset);
			params.put("LOGO_FILENAME", ReportUtil.getLogoFilePath());
			params.put("SHOW_PAGE", ReportUtil.isPageValid());
			jp = JasperFillManager.fillReport(report, params, ds);
		} catch (Exception e) {
			m_log.error(e, e);
		}
		
		if(jp != null) {
			jpList.add(jp);
			setCurPage(pageOffset + jp.getPages().size());
		}
		
		m_log.info("TemplateMiddleCoverPage( " + m_templateId + ") : getReport() complated.");
		
		return jpList;
	}
}
